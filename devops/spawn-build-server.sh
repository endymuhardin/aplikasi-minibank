#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────
# Spawn DO Droplet → Setup Environment → Build Project
# ────────────────────────────────

# Default configuration
DEFAULT_REGION="sgp1"
DEFAULT_SIZE="c-4"
DEFAULT_IMAGE="ubuntu-24-04-x64"
DEFAULT_SSH_KEY_NAME=""
USE_SNAPSHOT=false
SNAPSHOT_ID=""

# Maven build options (sequential profile is default)
DEFAULT_MAVEN_GOALS="clean verify"
DEFAULT_MAVEN_OPTS="-T1C -DskipTests=false"
MAVEN_GOALS="$DEFAULT_MAVEN_GOALS"
MAVEN_OPTS="$DEFAULT_MAVEN_OPTS"
TEST_PATTERN=""
TEST_PROFILE=""

# Parse command line arguments
REGION="${REGION:-$DEFAULT_REGION}"
SIZE="${SIZE:-$DEFAULT_SIZE}"
IMAGE="${IMAGE:-$DEFAULT_IMAGE}"
SSH_KEY_NAME="${SSH_KEY_NAME:-$DEFAULT_SSH_KEY_NAME}"

# Help function
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Spawn a new DigitalOcean droplet, setup build environment, and start project build.

OPTIONS:
    -r, --region REGION     DigitalOcean region (default: sgp1)
    -s, --size SIZE         Droplet size (default: c-4 = 4vCPU, 8GB RAM)
    -i, --image IMAGE       Base image (default: ubuntu-24-04-x64)
    -k, --ssh-key KEY       SSH key NAME in DigitalOcean (required)
    --snapshot ID           Use existing snapshot instead of base image
    --use-latest-snapshot   Use the latest created snapshot
    
    BUILD OPTIONS:
    -t, --test PATTERN      Run specific tests (e.g., "MyTest", "*Integration*")
    -g, --goals GOALS       Maven goals (default: clean verify)
    --skip-tests           Skip all tests
    --unit-tests-only      Run only unit tests
    --integration-tests-only Run only integration tests
    --parallel             Use parallel execution profile (2 threads at class level)
    --sequential           Use sequential execution profile (default, single threaded)
    
    -h, --help              Show this help

SSH KEY SETUP:
    The SSH key must be uploaded to DigitalOcean first. Use the key NAME from DO, not your local filename.
    
    List your available keys:
        doctl compute ssh-key list
    
    Upload a new key:
        doctl compute ssh-key create "my-key-name" --public-key-file ~/.ssh/id_rsa.pub

EXAMPLES:
    # Fresh setup with base image
    $0 --ssh-key my-key                           
    
    # Use latest snapshot (faster)
    $0 -k my-key --use-latest-snapshot           
    
    # Use specific snapshot
    $0 -k my-key --snapshot snap-abc123          
    
    # Run specific test class
    $0 -k my-key --test "AccountServiceTest"
    
    # Run tests matching pattern
    $0 -k my-key --test "*Integration*"
    
    # Run only unit tests
    $0 -k my-key --unit-tests-only
    
    # Use parallel execution (2 threads at class level)
    $0 -k my-key --parallel
    
    # Use sequential execution (explicit)
    $0 -k my-key --sequential
    
    # Skip all tests
    $0 -k my-key --skip-tests
    $0 -k my-key -s c-8 -r nyc1                  # 8vCPU, 16GB in NYC

DROPLET SIZES:
    c-2     : 2 vCPUs, 4GB RAM
    c-4     : 4 vCPUs, 8GB RAM (default)
    c-8     : 8 vCPUs, 16GB RAM
    s-2vcpu-4gb   : 2 vCPUs, 4GB RAM (standard)
    s-4vcpu-8gb   : 4 vCPUs, 8GB RAM (standard)

REQUIREMENTS:
    - doctl installed and configured
    - SSH key added to DigitalOcean account
    - rsync installed

EOF
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--region)
            REGION="$2"
            shift 2
            ;;
        -s|--size)
            SIZE="$2"
            shift 2
            ;;
        -i|--image)
            IMAGE="$2"
            shift 2
            ;;
        -k|--ssh-key)
            SSH_KEY_NAME="$2"
            shift 2
            ;;
        --snapshot)
            USE_SNAPSHOT=true
            SNAPSHOT_ID="$2"
            shift 2
            ;;
        --use-latest-snapshot)
            USE_SNAPSHOT=true
            SNAPSHOT_ID="latest"
            shift 1
            ;;
        -t|--test)
            TEST_PATTERN="$2"
            shift 2
            ;;
        -g|--goals)
            MAVEN_GOALS="$2"
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
        --parallel)
            TEST_PROFILE="parallel"
            shift
            ;;
        --sequential)
            TEST_PROFILE="sequential"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validation
if [[ -z "$SSH_KEY_NAME" ]]; then
    echo "❌ SSH key name is required. Use -k or --ssh-key option."
    echo "   List your keys with: doctl compute ssh-key list"
    exit 1
fi

# Check prerequisites
echo "🔍 Checking prerequisites..."

if ! command -v doctl >/dev/null 2>&1; then
    echo "❌ doctl is not installed. Please install it first:"
    echo "   https://docs.digitalocean.com/reference/doctl/how-to/install/"
    exit 1
fi

if ! command -v rsync >/dev/null 2>&1; then
    echo "❌ rsync is not installed. Please install it first."
    exit 1
fi

# Test doctl authentication
if ! doctl account get >/dev/null 2>&1; then
    echo "❌ doctl is not authenticated. Please run: doctl auth init"
    exit 1
fi

# Validate SSH key exists and get its ID
echo "🔑 Validating SSH key '$SSH_KEY_NAME'..."
SSH_KEY_ID=$(doctl compute ssh-key list --format Name,ID --no-header | grep "^${SSH_KEY_NAME}[[:space:]]" | awk '{print $2}')

if [[ -z "$SSH_KEY_ID" ]]; then
    echo "❌ SSH key '$SSH_KEY_NAME' not found in your DigitalOcean account."
    echo ""
    echo "Available SSH keys:"
    doctl compute ssh-key list --format Name,ID
    echo ""
    echo "Upload a new SSH key with:"
    echo "  doctl compute ssh-key create "$SSH_KEY_NAME" --public-key-file ~/.ssh/id_ed25519.pub"
    exit 1
fi

echo "✅ SSH key found with ID: $SSH_KEY_ID"

# Determine project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ "$(basename "$SCRIPT_DIR")" == "devops" ]]; then
    PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
    SESSION_DIR="$SCRIPT_DIR"
else
    PROJECT_ROOT="$(pwd)"
    # Look for devops folder for session files
    if [[ -d "devops" ]]; then
        SESSION_DIR="$(pwd)/devops"
    else
        SESSION_DIR="$SCRIPT_DIR"
    fi
fi

# Handle snapshot selection
if [[ "$USE_SNAPSHOT" == "true" ]]; then
    if [[ "$SNAPSHOT_ID" == "latest" ]]; then
        # Use latest snapshot if available
        if [[ -f "$SESSION_DIR/.latest-snapshot-id" ]]; then
            SNAPSHOT_ID=$(cat "$SESSION_DIR/.latest-snapshot-id")
            SNAPSHOT_NAME=$(cat "$SESSION_DIR/.latest-snapshot-name" 2>/dev/null || echo "unknown")
            echo "🔍 Using latest snapshot: $SNAPSHOT_NAME ($SNAPSHOT_ID)"
        else
            echo "❌ No latest snapshot found. Available snapshots:"
            doctl compute snapshot list --format ID,Name,Created --no-header | grep "minibank-build-env" || echo "   No minibank build snapshots found"
            echo ""
            echo "💡 Create a snapshot first by destroying a droplet with snapshot option"
            echo "   or specify a snapshot ID with --snapshot ID"
            exit 1
        fi
    fi
    
    # Verify snapshot exists
    if ! doctl compute snapshot get "$SNAPSHOT_ID" >/dev/null 2>&1; then
        echo "❌ Snapshot '$SNAPSHOT_ID' not found. Available snapshots:"
        doctl compute snapshot list --format ID,Name,Created --no-header | grep "minibank-build-env" || echo "   No minibank build snapshots found"
        exit 1
    fi
    
    # Use snapshot as image
    IMAGE="$SNAPSHOT_ID"
    echo "📸 Using snapshot instead of base image"
fi

# Generate unique droplet name
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
DROPLET_NAME="build-server-${TIMESTAMP}"

echo "🚀 Creating DigitalOcean droplet..."
echo "   Name: $DROPLET_NAME"
echo "   Region: $REGION"
echo "   Size: $SIZE"
echo "   Image: $IMAGE"
echo "   SSH Key: $SSH_KEY_NAME (ID: $SSH_KEY_ID)"

# Create droplet
DROPLET_ID=$(doctl compute droplet create "$DROPLET_NAME" \
    --region "$REGION" \
    --size "$SIZE" \
    --image "$IMAGE" \
    --ssh-keys "$SSH_KEY_ID" \
    --wait \
    --format ID \
    --no-header)

if [[ -z "$DROPLET_ID" ]]; then
    echo "❌ Failed to create droplet"
    exit 1
fi

echo "✅ Droplet created with ID: $DROPLET_ID"

# Get droplet IP
echo "🔍 Getting droplet IP address..."
DROPLET_IP=$(doctl compute droplet get "$DROPLET_ID" --format PublicIPv4 --no-header)

if [[ -z "$DROPLET_IP" ]]; then
    echo "❌ Failed to get droplet IP"
    exit 1
fi

echo "✅ Droplet IP: $DROPLET_IP"

# Save session info
SESSION_FILE="$SESSION_DIR/.do-session-ip"
echo "$DROPLET_IP" > "$SESSION_FILE"
echo "$DROPLET_ID" > "$SESSION_DIR/.do-session-id"
echo "$DROPLET_NAME" > "$SESSION_DIR/.do-session-name"

echo "💾 Session info saved to: $SESSION_FILE"

# Wait for SSH to be available
echo "⏳ Waiting for SSH to become available..."
SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=10 -o ServerAliveInterval=5"

for i in {1..30}; do
    if ssh $SSH_OPTS root@"$DROPLET_IP" "echo 'SSH Ready'" >/dev/null 2>&1; then
        echo "✅ SSH is available"
        break
    fi
    if [[ $i -eq 30 ]]; then
        echo "❌ SSH connection timeout after 5 minutes"
        exit 1
    fi
    echo "   Attempt $i/30 - waiting 10 seconds..."
    sleep 10
done

# Setup environment (skip if using snapshot)
if [[ "$USE_SNAPSHOT" == "true" ]]; then
    echo "📸 Using snapshot - skipping environment setup"
    echo "🔍 Verifying snapshot environment..."
    
    # Quick verification that tools are available
    if ssh $SSH_OPTS root@"$DROPLET_IP" "java -version && mvn -version && docker --version" >/dev/null 2>&1; then
        echo "✅ Snapshot environment verified (Java, Maven, Docker available)"
    else
        echo "⚠️  Warning: Snapshot may not have complete build environment"
        echo "   You may need to run setup manually or use a fresh image"
    fi
else
    echo "🔧 Setting up build environment on droplet..."

    # Create setup script
    cat > /tmp/setup-build-env.sh << 'EOF'
#!/bin/bash
set -euo pipefail

# Function to wait for APT lock to be released
wait_for_apt_lock() {
    local max_wait=300  # 5 minutes maximum wait
    local wait_time=0
    
    while fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1 || \
          fuser /var/lib/apt/lists/lock >/dev/null 2>&1 || \
          fuser /var/cache/apt/archives/lock >/dev/null 2>&1; do
        
        if [ $wait_time -ge $max_wait ]; then
            echo "❌ Timeout waiting for APT lock after 5 minutes"
            echo "🔧 Attempting to force unlock..."
            
            # Kill any hanging apt processes
            pkill -f apt-get || true
            pkill -f dpkg || true
            
            # Remove lock files
            rm -f /var/lib/dpkg/lock-frontend
            rm -f /var/lib/apt/lists/lock
            rm -f /var/cache/apt/archives/lock
            
            # Reconfigure dpkg if needed
            dpkg --configure -a || true
            break
        fi
        
        echo "⏳ Waiting for APT lock to be released... (${wait_time}s elapsed)"
        sleep 10
        wait_time=$((wait_time + 10))
    done
}

echo "📦 Updating system packages..."
export DEBIAN_FRONTEND=noninteractive

# Wait for any existing APT processes to complete
wait_for_apt_lock

# Fix common GPG/APT issues
echo "🔑 Fixing APT repository keys..."
apt-get clean
rm -rf /var/lib/apt/lists/*
apt-get update --fix-missing

# If update still fails, try fixing GPG keys
if ! apt-get update -qq 2>/dev/null; then
    echo "⚠️  APT update failed, fixing GPG keys..."
    apt-get clean
    rm -rf /var/lib/apt/lists/*
    
    # Recreate apt lists directory
    mkdir -p /var/lib/apt/lists/partial
    
    # Update with verbose output to see what's happening
    apt-get update
fi

apt-get upgrade -y -qq

echo "☕ Installing Java 21..."
# Wait for APT lock before Java installation
wait_for_apt_lock

# Try standard installation first
if ! apt-get install -y -qq software-properties-common openjdk-21-jdk 2>/dev/null; then
    echo "⚠️  Standard Java installation failed, trying alternative approach..."
    
    # Clean and retry
    apt-get clean
    apt-get update --fix-missing
    
    # Try installing packages individually
    apt-get install -y software-properties-common
    apt-get install -y openjdk-21-jdk
fi

echo "🔧 Installing Maven..."
# Wait for APT lock before Maven installation
wait_for_apt_lock

if ! apt-get install -y -qq maven 2>/dev/null; then
    echo "⚠️  Maven installation failed, retrying..."
    apt-get clean
    apt-get update
    apt-get install -y maven
fi

echo "🐳 Installing Docker..."
# Wait for APT lock before Docker installation
wait_for_apt_lock

apt-get install -y -qq apt-transport-https ca-certificates curl gnupg lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Wait for APT lock before Docker repository update
wait_for_apt_lock
apt-get update -qq

# Wait for APT lock before Docker package installation
wait_for_apt_lock
apt-get install -y -qq docker-ce docker-ce-cli containerd.io

echo "🚀 Starting Docker service..."
systemctl start docker
systemctl enable docker

echo "📁 Creating project directory..."
mkdir -p /root/project

echo "✅ Build environment setup complete!"

# Verify installations
echo "🔍 Verifying installations:"
echo "Java version:"
java -version
echo "Maven version:"
mvn -version
echo "Docker version:"
docker --version

EOF

# Transfer and run setup script
echo "📤 Transferring setup script..."
scp $SSH_OPTS /tmp/setup-build-env.sh root@"$DROPLET_IP":/tmp/

echo "⚡ Running setup script on droplet (this may take 5-10 minutes)..."
ssh $SSH_OPTS root@"$DROPLET_IP" "chmod +x /tmp/setup-build-env.sh && /tmp/setup-build-env.sh" &
SETUP_PID=$!

# Show progress while setup runs
echo "⏳ Setup is running in background..."
while kill -0 $SETUP_PID 2>/dev/null; do
    echo "   Still setting up... ($(date '+%H:%M:%S'))"
    sleep 30
done

wait $SETUP_PID
SETUP_EXIT_CODE=$?

    if [[ $SETUP_EXIT_CODE -ne 0 ]]; then
        echo "❌ Setup script failed with exit code $SETUP_EXIT_CODE"
        exit 1
    fi

    echo "✅ Build environment setup complete!"
fi

# Ensure project directory exists (whether using snapshot or fresh install)
ssh $SSH_OPTS root@"$DROPLET_IP" "mkdir -p /root/project"

echo "🔄 Syncing project to droplet..."
rsync -az -e "ssh $SSH_OPTS" \
    --delete \
    --exclude '.git' \
    --exclude 'target' \
    --exclude 'node_modules' \
    --exclude '.do-session-*' \
    "$PROJECT_ROOT"/ root@"$DROPLET_IP":/root/project/

echo "⚡ Starting Maven build on droplet (in background)..."

# Build the Maven command
MAVEN_CMD="mvn $MAVEN_GOALS $MAVEN_OPTS"

# Add test profile if specified
if [[ -n "$TEST_PROFILE" ]]; then
    MAVEN_CMD="$MAVEN_CMD -Dtest.profile=$TEST_PROFILE"
fi

# Add test pattern if specified
if [[ -n "$TEST_PATTERN" ]]; then
    MAVEN_CMD="$MAVEN_CMD -Dtest=$TEST_PATTERN"
fi

echo "🔧 Maven command: $MAVEN_CMD"

# Start build in background using the same method as start-remote-build.sh
ssh $SSH_OPTS root@"$DROPLET_IP" bash << EOF
cd /root/project
nohup $MAVEN_CMD > build.log 2>&1 </dev/null &
disown
exit
EOF

echo "✅ Build started successfully!"
echo ""
echo "📋 Summary:"
echo "   Droplet Name: $DROPLET_NAME"
echo "   Droplet ID: $DROPLET_ID"
echo "   IP Address: $DROPLET_IP"
echo "   Region: $REGION"
echo "   Size: $SIZE"
if [[ "$USE_SNAPSHOT" == "true" ]]; then
    echo "   Image: Snapshot ($SNAPSHOT_ID)"
else
    echo "   Image: $IMAGE"
fi
echo ""
echo "🔍 Monitor build progress with:"
echo "   ./check-remote-build.sh"
echo ""
echo "🗑️  When done, destroy droplet with:"
echo "   ./destroy-build-session.sh"
echo "💡 Tip: Create a snapshot when destroying to speed up future builds"
echo ""
echo "💡 Tip: Use './check-remote-build.sh --debug' for detailed build detection"
