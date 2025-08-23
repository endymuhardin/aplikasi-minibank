#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────
# Sync project → Run Maven build (async)
# ────────────────────────────────

# Default Maven options
DEFAULT_MAVEN_GOALS="clean verify"
DEFAULT_MAVEN_OPTS="-T1C -DskipTests=false"

# Variables
MAVEN_GOALS="$DEFAULT_MAVEN_GOALS"
MAVEN_OPTS="$DEFAULT_MAVEN_OPTS"
TEST_PATTERN=""

# Function to show help
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Start a Maven build on a remote VPS server.

OPTIONS:
    -t, --test PATTERN      Run specific tests (e.g., "MyTest", "*Integration*")
    -g, --goals GOALS       Maven goals (default: clean verify)
    -o, --opts OPTS         Additional Maven options (default: -T1C -DskipTests=false)
    --skip-tests           Skip all tests (overrides -DskipTests=false)
    --unit-tests-only      Run only unit tests (surefire plugin)
    --integration-tests-only Run only integration tests (failsafe plugin)
    -h, --help             Show this help

EXAMPLES:
    # Default build with all tests
    $0

    # Run specific test class
    $0 --test "AccountServiceTest"

    # Run tests matching pattern
    $0 --test "*Integration*"

    # Run only unit tests
    $0 --unit-tests-only

    # Run only integration tests  
    $0 --integration-tests-only

    # Skip all tests
    $0 --skip-tests

    # Custom Maven goals
    $0 --goals "clean compile test"

    # With additional Maven options
    $0 --opts "-T1C -DskipTests=false -Dmaven.test.failure.ignore=true"

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--test)
            TEST_PATTERN="$2"
            shift 2
            ;;
        -g|--goals)
            MAVEN_GOALS="$2"
            shift 2
            ;;
        -o|--opts)
            MAVEN_OPTS="$2"
            shift 2
            ;;
        --skip-tests)
            MAVEN_OPTS="${MAVEN_OPTS/-DskipTests=false/-DskipTests=true}"
            shift
            ;;
        --unit-tests-only)
            MAVEN_GOALS="clean test"
            shift
            ;;
        --integration-tests-only)
            MAVEN_GOALS="clean verify -DskipUnitTests=true"
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

echo "🔄 Syncing project to VPS $DROPLET_IP ..."
rsync -az -e "ssh $SSH_OPTS" \
  --delete \
  --exclude '.git' \
  --exclude 'target' \
  "$PROJECT_ROOT"/ root@"$DROPLET_IP":/root/project/

echo "⚡ Starting Maven build on VPS $DROPLET_IP (in background)..."

# Build the Maven command
MAVEN_CMD="mvn $MAVEN_GOALS $MAVEN_OPTS"

# Add test pattern if specified
if [[ -n "$TEST_PATTERN" ]]; then
    MAVEN_CMD="$MAVEN_CMD -Dtest=$TEST_PATTERN"
fi

echo "🔧 Maven command: $MAVEN_CMD"

# Use proper backgrounding with SSH to ensure clean disconnect
# The combination of nohup, </dev/null, and disown ensures the process fully detaches
ssh $SSH_OPTS root@"$DROPLET_IP" bash << EOF
cd /root/project
nohup $MAVEN_CMD > build.log 2>&1 </dev/null &
disown
exit
EOF

echo "✅ Build started in background. Use check-remote-build.sh to monitor."
exit 0
