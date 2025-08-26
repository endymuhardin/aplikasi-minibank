#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────
# Kill running Maven build on remote VPS
# ────────────────────────────────

# Function to show help
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Kill running Maven build processes on the remote VPS server.

OPTIONS:
    --force            Force kill all Java/Maven processes (SIGKILL)
    --soft             Gracefully terminate Maven processes (SIGTERM)
    -h, --help         Show this help

EXAMPLES:
    # Gracefully terminate Maven build (default)
    $0

    # Force kill all Java/Maven processes
    $0 --force

    # Gracefully terminate with explicit flag
    $0 --soft

DESCRIPTION:
    This script will connect to the remote VPS and terminate any running Maven
    build processes. By default, it uses SIGTERM for graceful shutdown. Use
    --force for immediate termination with SIGKILL.

    The script will:
    1. Find all Maven/Java processes related to the build
    2. Terminate them using the specified method
    3. Clean up any stale lock files
    4. Show a summary of killed processes

EOF
}

# Default options
FORCE_KILL=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --force)
            FORCE_KILL=true
            shift
            ;;
        --soft)
            FORCE_KILL=false
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "❌ Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

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

echo "🔍 Checking for running Maven processes on VPS $DROPLET_IP ..."

# Check if there are any Maven/Java processes running
JAVA_PROCESSES=$(ssh $SSH_OPTS root@"$DROPLET_IP" "ps aux | grep -E '(java.*maven|mvn)' | grep -v grep" || echo "")

if [[ -z "$JAVA_PROCESSES" ]]; then
    echo "ℹ️  No Maven/Java processes found running on the remote server"
    echo "✅ Nothing to kill"
    exit 0
fi

echo "📋 Found the following Maven/Java processes:"
echo "$JAVA_PROCESSES"
echo ""

# Determine signal to use
if [[ "$FORCE_KILL" == "true" ]]; then
    SIGNAL="KILL"
    SIGNAL_DESC="force killing (SIGKILL)"
    echo "⚠️  WARNING: Force kill mode enabled - processes will be terminated immediately"
else
    SIGNAL="TERM"
    SIGNAL_DESC="gracefully terminating (SIGTERM)"
fi

# Confirm the action
read -p "❓ Proceed with $SIGNAL_DESC all Maven/Java processes? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Operation cancelled"
    exit 1
fi

echo "🔄 ${SIGNAL_DESC^} Maven/Java processes..."

# Kill the processes
KILL_RESULT=$(ssh $SSH_OPTS root@"$DROPLET_IP" bash << EOF
set -euo pipefail

# Find all Maven/Java process IDs
PIDS=\$(ps aux | grep -E '(java.*maven|mvn)' | grep -v grep | awk '{print \$2}' || echo "")

if [[ -z "\$PIDS" ]]; then
    echo "NO_PROCESSES"
    exit 0
fi

KILLED_COUNT=0
FAILED_COUNT=0

for PID in \$PIDS; do
    if kill -$SIGNAL "\$PID" 2>/dev/null; then
        echo "✅ Killed process \$PID"
        KILLED_COUNT=\$((KILLED_COUNT + 1))
    else
        echo "❌ Failed to kill process \$PID"
        FAILED_COUNT=\$((FAILED_COUNT + 1))
    fi
done

# Clean up Maven lock files in project directory
if [[ -d "/root/project" ]]; then
    find /root/project -name "*.lock" -type f -delete 2>/dev/null || true
    find /root/project -name ".mvn" -type d -exec rm -rf {} + 2>/dev/null || true
fi

echo "SUMMARY:\$KILLED_COUNT:\$FAILED_COUNT"
EOF
)

# Parse the results
if [[ "$KILL_RESULT" == "NO_PROCESSES" ]]; then
    echo "ℹ️  No processes were found to kill"
    exit 0
fi

# Extract summary from the last line
SUMMARY_LINE=$(echo "$KILL_RESULT" | grep "^SUMMARY:" || echo "SUMMARY:0:0")
KILLED_COUNT=$(echo "$SUMMARY_LINE" | cut -d: -f2)
FAILED_COUNT=$(echo "$SUMMARY_LINE" | cut -d: -f3)

# Show the output (excluding the summary line)
echo "$KILL_RESULT" | grep -v "^SUMMARY:"

echo ""
echo "📊 Operation Summary:"
echo "   ✅ Successfully killed: $KILLED_COUNT processes"
echo "   ❌ Failed to kill: $FAILED_COUNT processes"
echo "   🧹 Cleaned up Maven lock files"

if [[ "$FAILED_COUNT" -gt 0 ]]; then
    echo ""
    echo "⚠️  Some processes could not be killed. They may have already terminated"
    echo "   or require administrative privileges. Consider using --force if needed."
fi

# Wait a moment and check if any processes are still running
echo ""
echo "🔍 Verifying all processes were terminated..."
sleep 2

REMAINING_PROCESSES=$(ssh $SSH_OPTS root@"$DROPLET_IP" "ps aux | grep -E '(java.*maven|mvn)' | grep -v grep" || echo "")

if [[ -z "$REMAINING_PROCESSES" ]]; then
    echo "✅ All Maven/Java processes have been successfully terminated"
else
    echo "⚠️  Some processes are still running:"
    echo "$REMAINING_PROCESSES"
    echo ""
    echo "💡 You may need to use --force to kill stubborn processes"
fi

echo ""
echo "✅ Kill operation completed!"
echo "💡 Use check-remote-build.sh to verify the build status"
