# DevOps Scripts

This folder contains automation scripts for remote builds on DigitalOcean droplets.

## Available Scripts

### Primary Build Scripts

#### `spawn-build-server.sh`
Creates a new DigitalOcean droplet, sets up build environment, and starts Maven build.
Supports using snapshots for faster setup.

#### `start-remote-build.sh`
Syncs project to existing remote VPS and starts Maven build in background.

#### `check-remote-build.sh`
Monitors build progress and fetches results when complete.

### Management Scripts

#### `destroy-build-session.sh`
Destroys the VPS session with optional snapshot creation for future reuse.

#### `list-snapshots.sh`
Lists available build environment snapshots.

## Scripts Available

The following scripts are currently available in this folder:
- `spawn-build-server.sh` - Create droplet, setup environment, and build
- `start-remote-build.sh` - Build on existing remote server  
- `check-remote-build.sh` - Monitor build progress
- `destroy-build-session.sh` - Destroy droplet with optional snapshot
- `list-snapshots.sh` - List available snapshots

## Usage

For detailed documentation on remote builds, see:
📖 [Remote Build Guide](../docs/remote-build-guide.md)

## Quick Start

### First Time (Fresh Setup)
```bash
# From project root
./spawn-build-server.sh --ssh-key YOUR_KEY    # Create & build (8-10 min setup)
./check-remote-build.sh                       # Monitor progress
./destroy-build-session.sh                    # Destroy (create snapshot: Y)
```

### Subsequent Builds (Using Snapshots)
```bash
./spawn-build-server.sh --ssh-key YOUR_KEY --use-latest-snapshot  # Fast setup (30s)
./check-remote-build.sh                                           # Monitor
./destroy-build-session.sh                                        # Destroy
```

### Existing Server
```bash
./start-remote-build.sh     # Build on existing server
./check-remote-build.sh     # Monitor
```
