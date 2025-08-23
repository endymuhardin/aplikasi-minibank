#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────
# List Available Build Snapshots
# ────────────────────────────────

# Check if doctl is available
if ! command -v doctl >/dev/null 2>&1; then
    echo "❌ doctl is not installed. Please install it first:"
    echo "   https://docs.digitalocean.com/reference/doctl/how-to/install/"
    exit 1
fi

# Test doctl authentication
if ! doctl account get >/dev/null 2>&1; then
    echo "❌ doctl is not authenticated. Please run: doctl auth init"
    exit 1
fi

echo "📸 Available Build Environment Snapshots:"
echo ""

# Get script directory for latest snapshot info
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Handle both execution from devops folder and root folder
if [[ "$(basename "$SCRIPT_DIR")" == "devops" ]]; then
    # Script is in devops folder
    SESSION_DIR="$SCRIPT_DIR"
else
    # Script is being run from root or copied elsewhere, look for devops folder
    if [[ -d "devops" ]]; then
        SESSION_DIR="$(pwd)/devops"
    else
        SESSION_DIR="$SCRIPT_DIR"
    fi
fi

# Show latest snapshot if available
if [[ -f "$SESSION_DIR/.latest-snapshot-id" ]]; then
    LATEST_ID=$(cat "$SESSION_DIR/.latest-snapshot-id")
    LATEST_NAME=$(cat "$SESSION_DIR/.latest-snapshot-name" 2>/dev/null || echo "unknown")
    echo "🌟 Latest Snapshot: $LATEST_NAME ($LATEST_ID)"
    echo ""
fi

# List all minibank build snapshots
SNAPSHOTS=$(doctl compute snapshot list --format ID,Name,Created,Size --no-header | grep "minibank-build-env" || true)

if [[ -z "$SNAPSHOTS" ]]; then
    echo "   No minibank build environment snapshots found."
    echo ""
    echo "💡 Create a snapshot by:"
    echo "   1. Run: ./spawn-build-server.sh --ssh-key YOUR_KEY"
    echo "   2. When done building, run: ./destroy-build-session.sh"
    echo "   3. Choose 'Y' when asked to create a snapshot"
else
    echo "ID                   Name                           Created              Size"
    echo "────────────────────────────────────────────────────────────────────────────"
    echo "$SNAPSHOTS"
    echo ""
    echo "💡 Use snapshots with:"
    echo "   ./spawn-build-server.sh --ssh-key YOUR_KEY --use-latest-snapshot"
    echo "   ./spawn-build-server.sh --ssh-key YOUR_KEY --snapshot SNAPSHOT_ID"
fi

echo ""
echo "🔍 All DigitalOcean snapshots:"
doctl compute snapshot list --format ID,Name,Created,Size
