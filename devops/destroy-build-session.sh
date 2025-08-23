#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────
# Destroy DigitalOcean Build Session
# ────────────────────────────────

# Determine script directory
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

SESSION_ID_FILE="$SESSION_DIR/.do-session-id"
SESSION_IP_FILE="$SESSION_DIR/.do-session-ip"
SESSION_NAME_FILE="$SESSION_DIR/.do-session-name"

# Check if session exists
if [[ ! -f "$SESSION_ID_FILE" ]]; then
    echo "❌ No active build session found."
    echo "   Looking for session file: $SESSION_ID_FILE"
    exit 1
fi

DROPLET_ID=$(cat "$SESSION_ID_FILE")
DROPLET_IP=$(cat "$SESSION_IP_FILE" 2>/dev/null || echo "unknown")
DROPLET_NAME=$(cat "$SESSION_NAME_FILE" 2>/dev/null || echo "unknown")

echo "🔍 Found active build session:"
echo "   Droplet ID: $DROPLET_ID"
echo "   Droplet IP: $DROPLET_IP"
echo "   Droplet Name: $DROPLET_NAME"

# Ask about snapshot creation
echo ""
read -p "💾 Do you want to create a snapshot before destroying? (Y/n): " -n 1 -r
echo
CREATE_SNAPSHOT="y"
if [[ $REPLY =~ ^[Nn]$ ]]; then
    CREATE_SNAPSHOT="n"
fi

# Confirm destruction
read -p "❓ Do you want to destroy this droplet? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Destruction cancelled."
    exit 1
fi

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

echo "🗑️  Destroying droplet..."

# Create snapshot if requested
if [[ "$CREATE_SNAPSHOT" == "y" ]]; then
    SNAPSHOT_NAME="minibank-build-env-$(date +%Y%m%d-%H%M%S)"
    echo "📸 Creating snapshot: $SNAPSHOT_NAME"
    echo "   This may take 2-5 minutes..."
    
    if doctl compute droplet-action snapshot "$DROPLET_ID" --snapshot-name "$SNAPSHOT_NAME" --wait; then
        echo "✅ Snapshot created successfully: $SNAPSHOT_NAME"
        
        # Get snapshot ID for future reference
        SNAPSHOT_ID=$(doctl compute snapshot list --format ID,Name --no-header | grep "$SNAPSHOT_NAME" | awk '{print $1}')
        if [[ -n "$SNAPSHOT_ID" ]]; then
            echo "$SNAPSHOT_ID" > "$SESSION_DIR/.latest-snapshot-id"
            echo "$SNAPSHOT_NAME" > "$SESSION_DIR/.latest-snapshot-name"
            echo "💾 Snapshot info saved for future use"
            echo "   Snapshot ID: $SNAPSHOT_ID"
        fi
    else
        echo "⚠️  Snapshot creation failed, but continuing with destruction..."
    fi
fi

if doctl compute droplet delete "$DROPLET_ID" --force; then
    echo "✅ Droplet destroyed successfully"
    
    # Clean up session files
    rm -f "$SESSION_ID_FILE" "$SESSION_IP_FILE" "$SESSION_NAME_FILE"
    echo "🧹 Session files cleaned up"
    
    echo ""
    echo "✅ Build session destroyed completely!"
else
    echo "❌ Failed to destroy droplet"
    echo "   You may need to manually delete it from the DigitalOcean console"
    echo "   Droplet ID: $DROPLET_ID"
    exit 1
fi
