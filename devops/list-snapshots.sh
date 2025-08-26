#!/usr/bin/env bash
set -euo pipefail

# ────────────────────────────────────────────────
# List and Manage Available Build Snapshots
# ────────────────────────────────────────────────
#
# Usage:
#   ./list-snapshots.sh                    # List snapshots
#   ./list-snapshots.sh --delete ID        # Delete snapshot by ID
#   ./list-snapshots.sh --delete-all       # Delete all minibank snapshots

# Parse command line arguments
DELETE_ID=""
DELETE_ALL=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --delete)
            DELETE_ID="$2"
            shift 2
            ;;
        --delete-all)
            DELETE_ALL=true
            shift
            ;;
        -h|--help)
            echo "Usage:"
            echo "  $0                    # List snapshots"
            echo "  $0 --delete ID        # Delete snapshot by ID"
            echo "  $0 --delete-all       # Delete all minibank snapshots"
            exit 0
            ;;
        *)
            echo "❌ Unknown option: $1"
            echo "Run $0 --help for usage information"
            exit 1
            ;;
    esac
done

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
SNAPSHOTS=$(doctl compute snapshot list --format ID,Name,CreatedAt,Size --no-header | grep "minibank-build-env" || true)

if [[ -z "$SNAPSHOTS" ]]; then
    echo "   No minibank build environment snapshots found."
    echo ""
    echo "💡 Create a snapshot by:"
    echo "   1. Run: ./spawn-build-server.sh --ssh-key YOUR_KEY"
    echo "   2. When done building, run: ./destroy-build-session.sh"
    echo "   3. Choose 'Y' when asked to create a snapshot"
else
    echo "ID                   Name                           CreatedAt            Size"
    echo "────────────────────────────────────────────────────────────────────────────"
    echo "$SNAPSHOTS"
    echo ""
    echo "💡 Use snapshots with:"
    echo "   ./spawn-build-server.sh --ssh-key YOUR_KEY --use-latest-snapshot"
    echo "   ./spawn-build-server.sh --ssh-key YOUR_KEY --snapshot SNAPSHOT_ID"
fi

# ────────────────────────────────────────────────
# Handle deletion operations
# ────────────────────────────────────────────────

if [[ -n "$DELETE_ID" ]]; then
    echo "🗑️  Deleting snapshot $DELETE_ID..."
    
    # Check if snapshot exists and is a minibank snapshot
    SNAPSHOT_INFO=$(doctl compute snapshot get "$DELETE_ID" --format ID,Name --no-header 2>/dev/null || echo "")
    
    if [[ -z "$SNAPSHOT_INFO" ]]; then
        echo "❌ Snapshot $DELETE_ID not found"
        exit 1
    fi
    
    SNAPSHOT_NAME=$(echo "$SNAPSHOT_INFO" | awk '{print $2}')
    
    # Confirm deletion
    echo "⚠️  About to delete: $SNAPSHOT_NAME ($DELETE_ID)"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if doctl compute snapshot delete "$DELETE_ID" --force; then
            echo "✅ Snapshot $DELETE_ID deleted successfully"
            
            # Clean up latest snapshot tracking if this was the latest
            if [[ -f "$SESSION_DIR/.latest-snapshot-id" ]]; then
                LATEST_ID=$(cat "$SESSION_DIR/.latest-snapshot-id")
                if [[ "$LATEST_ID" == "$DELETE_ID" ]]; then
                    rm -f "$SESSION_DIR/.latest-snapshot-id"
                    rm -f "$SESSION_DIR/.latest-snapshot-name"
                    echo "🧹 Cleared latest snapshot tracking"
                fi
            fi
        else
            echo "❌ Failed to delete snapshot $DELETE_ID"
            exit 1
        fi
    else
        echo "🚫 Deletion cancelled"
    fi
    
    exit 0
fi

if [[ "$DELETE_ALL" == true ]]; then
    echo "🗑️  Deleting ALL minibank build environment snapshots..."
    
    # Get all minibank snapshots
    ALL_MINIBANK_SNAPSHOTS=$(doctl compute snapshot list --format ID,Name --no-header | grep "minibank-build-env" || true)
    
    if [[ -z "$ALL_MINIBANK_SNAPSHOTS" ]]; then
        echo "ℹ️  No minibank build environment snapshots found to delete"
        exit 0
    fi
    
    echo "⚠️  Found snapshots to delete:"
    echo "$ALL_MINIBANK_SNAPSHOTS"
    echo ""
    
    read -p "Delete ALL these snapshots? This cannot be undone! (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Deleting all minibank snapshots..."
        
        SUCCESS_COUNT=0
        TOTAL_COUNT=0
        
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                SNAPSHOT_ID=$(echo "$line" | awk '{print $1}')
                SNAPSHOT_NAME=$(echo "$line" | awk '{print $2}')
                TOTAL_COUNT=$((TOTAL_COUNT + 1))
                
                echo "   Deleting: $SNAPSHOT_NAME ($SNAPSHOT_ID)..."
                
                if doctl compute snapshot delete "$SNAPSHOT_ID" --force; then
                    echo "   ✅ Deleted: $SNAPSHOT_ID"
                    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
                else
                    echo "   ❌ Failed to delete: $SNAPSHOT_ID"
                fi
            fi
        done <<< "$ALL_MINIBANK_SNAPSHOTS"
        
        # Clean up latest snapshot tracking
        rm -f "$SESSION_DIR/.latest-snapshot-id"
        rm -f "$SESSION_DIR/.latest-snapshot-name"
        
        echo ""
        echo "📊 Deletion Summary:"
        echo "   Successfully deleted: $SUCCESS_COUNT/$TOTAL_COUNT snapshots"
        echo "🧹 Cleared latest snapshot tracking"
        
        if [[ $SUCCESS_COUNT -eq $TOTAL_COUNT ]]; then
            echo "✅ All snapshots deleted successfully!"
        else
            echo "⚠️  Some deletions failed. Please check the output above."
            exit 1
        fi
    else
        echo "🚫 Deletion cancelled"
    fi
    
    exit 0
fi

echo ""
echo "🔍 All DigitalOcean snapshots:"
doctl compute snapshot list --format ID,Name,CreatedAt,Size

echo ""
echo "💡 Management commands:"
echo "   ./list-snapshots.sh --delete SNAPSHOT_ID    # Delete specific snapshot"
echo "   ./list-snapshots.sh --delete-all            # Delete all minibank snapshots"
