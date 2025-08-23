#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────
# Check build status → Fetch reports → Show summary
# ────────────────────────────────

# Check for debug flag
DEBUG=false
if [[ "${1:-}" == "--debug" ]]; then
  DEBUG=true
  echo "🐛 Debug mode enabled"
fi

# Determine project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ "$(basename "$SCRIPT_DIR")" == "devops" ]]; then
  # Script is in devops folder, go up one level
  PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
  SESSION_FILE="$SCRIPT_DIR/.do-session-ip"
else
  # Script is in root folder or being run from root
  PROJECT_ROOT="$(pwd)"
  SESSION_FILE="$PROJECT_ROOT/devops/.do-session-ip"
fi

if [[ ! -f "$SESSION_FILE" ]]; then
  echo "❌ No active session found. Run spawn-build-server.sh first."
  echo "   Looking for session file: $SESSION_FILE"
  exit 1
fi

DROPLET_IP=$(cat "$SESSION_FILE")

# Add connection timeout for SSH commands
SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=10 -o ServerAliveInterval=5"

echo "🔎 Checking build status on VPS $DROPLET_IP ..."

# Check if maven process is still running with multiple detection methods
BUILD_RUNNING=false
BUILD_COMPLETED=false

# First, check if build has completed by looking for completion markers in log
if ssh $SSH_OPTS root@"$DROPLET_IP" "[ -f /root/project/build.log ]"; then
  if [[ "$DEBUG" == "true" ]]; then
    echo "📄 Checking build.log for completion markers..."
  fi
  # Check for Maven completion markers - look in more lines and be more specific
  if ssh $SSH_OPTS root@"$DROPLET_IP" "tail -n 50 /root/project/build.log | grep -E '(BUILD SUCCESS|BUILD FAILURE|Total time: [0-9]+|Tests run: [0-9]+.*Failures: [0-9]+.*Errors: [0-9]+)' > /dev/null 2>&1"; then
    BUILD_COMPLETED=true
    if [[ "$DEBUG" == "true" ]]; then
      echo "✅ Found build completion markers in log"
    fi
  fi
fi

# Only check for running processes if build hasn't completed
if [[ "$BUILD_COMPLETED" == "false" ]]; then
  if [[ "$DEBUG" == "true" ]]; then
    echo "🔍 Build not marked as complete, checking for running processes..."
  fi
  
  # Method 1: Check for maven processes (including java processes running maven)
  if ssh $SSH_OPTS root@"$DROPLET_IP" "pgrep -f 'mvn|maven' > /dev/null 2>&1"; then
    BUILD_RUNNING=true
    if [[ "$DEBUG" == "true" ]]; then
      echo "📍 Found Maven process running"
    fi
  fi

  # Method 2: Check for Java processes that might be Maven (more reliable)
  if ssh $SSH_OPTS root@"$DROPLET_IP" "pgrep -f 'java.*maven' > /dev/null 2>&1"; then
    BUILD_RUNNING=true
    if [[ "$DEBUG" == "true" ]]; then
      echo "📍 Found Java Maven process running"
    fi
  fi

  # Method 3: Check if build.log is still being written to (file is growing)
  if ssh $SSH_OPTS root@"$DROPLET_IP" "[ -f /root/project/build.log ]"; then
    if [[ "$DEBUG" == "true" ]]; then
      echo "📏 Checking if build.log is still growing..."
    fi
    # Get initial size, wait a moment, then check if it grew
    INITIAL_SIZE=$(ssh $SSH_OPTS root@"$DROPLET_IP" "stat -f%z /root/project/build.log 2>/dev/null || stat -c%s /root/project/build.log 2>/dev/null || echo 0")
    sleep 3
    CURRENT_SIZE=$(ssh $SSH_OPTS root@"$DROPLET_IP" "stat -f%z /root/project/build.log 2>/dev/null || stat -c%s /root/project/build.log 2>/dev/null || echo 0")
    
    if [[ "$CURRENT_SIZE" -gt "$INITIAL_SIZE" ]]; then
      BUILD_RUNNING=true
      if [[ "$DEBUG" == "true" ]]; then
        echo "📍 Build log is still growing ($INITIAL_SIZE -> $CURRENT_SIZE bytes)"
      fi
    else
      if [[ "$DEBUG" == "true" ]]; then
        echo "📋 Build log size unchanged ($CURRENT_SIZE bytes)"
      fi
    fi
  fi
fi

# Final decision: build is running only if it hasn't completed AND we found running processes
if [[ "$BUILD_COMPLETED" == "true" ]]; then
  BUILD_RUNNING=false
  if [[ "$DEBUG" == "true" ]]; then
    echo "🏁 Build has completed (found completion markers)"
  fi
elif [[ "$BUILD_RUNNING" == "true" ]]; then
  if [[ "$DEBUG" == "true" ]]; then
    echo "⏳ Build is still running (active processes detected)"
  fi
else
  if [[ "$DEBUG" == "true" ]]; then
    echo "🤔 No active processes found, assuming build has completed"
  fi
  BUILD_RUNNING=false
fi

if [[ "$BUILD_RUNNING" == "true" ]]; then
  echo "⏳ Build is still in progress. Tailing build log:"
  ssh $SSH_OPTS root@"$DROPLET_IP" "tail -f /root/project/build.log"
  exit 0
fi

echo "✅ Build finished."

# Show debug info if requested
if [[ "$DEBUG" == "true" ]]; then
  echo "🔍 Debug information:"
  echo "   - Checking for completion markers in last 20 lines of build.log..."
  if ssh $SSH_OPTS root@"$DROPLET_IP" "[ -f /root/project/build.log ]"; then
    ssh $SSH_OPTS root@"$DROPLET_IP" "tail -n 20 /root/project/build.log | grep -E '(BUILD SUCCESS|BUILD FAILURE|Total time:|Tests run:.*Failures:.*Errors:)'" || echo "   - No completion markers found"
    
    echo "   - Current running processes related to Maven/Java:"
    ssh $SSH_OPTS root@"$DROPLET_IP" "ps aux | grep -E '(mvn|maven|java)' | grep -v grep" || echo "   - No Maven/Java processes found"
  else
    echo "   - build.log not found"
  fi
fi

echo "📥 Fetching test reports back to local machine..."

# Check if build.log exists and show the last few lines for context
if ssh $SSH_OPTS root@"$DROPLET_IP" "[ -f /root/project/build.log ]"; then
  echo "📄 Last few lines from build.log:"
  ssh $SSH_OPTS root@"$DROPLET_IP" "tail -n 5 /root/project/build.log"
  echo "────────────────────────────────"
else
  echo "⚠️ Warning: build.log not found on remote server"
fi

mkdir -p "$PROJECT_ROOT/target/surefire-reports"
rsync -az -e "ssh $SSH_OPTS" \
  root@"$DROPLET_IP":/root/project/target/surefire-reports/ \
  "$PROJECT_ROOT/target/surefire-reports/" || true

if ssh $SSH_OPTS root@"$DROPLET_IP" "[ -d /root/project/target/failsafe-reports ]"; then
  mkdir -p "$PROJECT_ROOT/target/failsafe-reports"
  rsync -az -e "ssh $SSH_OPTS" \
    root@"$DROPLET_IP":/root/project/target/failsafe-reports/ \
    "$PROJECT_ROOT/target/failsafe-reports/"
fi

echo "✅ Reports available in $PROJECT_ROOT/target/"

# ────────────────────────────────
# Parse summary from surefire XML
# ────────────────────────────────
if command -v xmllint >/dev/null 2>&1; then
  echo "📊 Test Summary:"
  for report in "$PROJECT_ROOT/target/surefire-reports/TEST-"*.xml; do
    [ -e "$report" ] || continue
    classname=$(xmllint --xpath 'string(/testsuite/@name)' "$report")
    tests=$(xmllint --xpath 'string(/testsuite/@tests)' "$report")
    failures=$(xmllint --xpath 'string(/testsuite/@failures)' "$report")
    errors=$(xmllint --xpath 'string(/testsuite/@errors)' "$report")
    skipped=$(xmllint --xpath 'string(/testsuite/@skipped)' "$report")
    echo "  - $classname: $tests tests, $failures failures, $errors errors, $skipped skipped"

    if [[ "$failures" != "0" || "$errors" != "0" ]]; then
      echo "    ❌ Failed tests:"
      xmllint --xpath '//testcase[failure or error]/@name' "$report" \
        | sed -E 's/name=\"([^\"]+)\"/      - \1/g'
    fi
  done
else
  echo "ℹ️ Install xmllint (libxml2-utils) to see test summary."
fi
