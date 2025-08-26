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
./spawn-build-server.sh --ssh-key YOUR_KEY    # Create & build (8-10 min setup, sequential mode default)
./check-remote-build.sh                       # Monitor progress
./destroy-build-session.sh                    # Destroy (create snapshot: Y)
```

### Subsequent Builds (Using Snapshots)
```bash
./spawn-build-server.sh --ssh-key YOUR_KEY --use-latest-snapshot  # Fast setup (30s, sequential mode)
./check-remote-build.sh                                           # Monitor
./destroy-build-session.sh                                        # Destroy
```

### Existing Server
```bash
./start-remote-build.sh     # Build on existing server (sequential mode default)
./check-remote-build.sh     # Monitor
```

### Test Execution Profiles

The scripts support two test execution modes:

#### Sequential Mode (Default)
- Single-threaded test execution
- More predictable and easier for debugging
- Better for environments with limited resources
- Default behavior - no extra flags needed

```bash
# Sequential execution (explicit)
./start-remote-build.sh --sequential

# Sequential with specific tests
./start-remote-build.sh --test "*Integration*"
```

#### Parallel Mode
- Multi-threaded execution at class level (2 threads)
- Faster execution for larger test suites
- Requires more memory and CPU resources

```bash
# Parallel execution
./start-remote-build.sh --parallel

# Parallel with specific tests  
./start-remote-build.sh --parallel --test "*Unit*"

# Parallel execution from fresh server
./spawn-build-server.sh --ssh-key YOUR_KEY --parallel
```

### Additional Build Options

Both `start-remote-build.sh` and `spawn-build-server.sh` support additional build customization:

```bash
# Skip all tests
./start-remote-build.sh --skip-tests

# Run only unit tests  
./start-remote-build.sh --unit-tests-only

# Run only integration tests
./start-remote-build.sh --integration-tests-only

# Run specific test class
./start-remote-build.sh --test "AccountServiceTest"

# Run tests matching pattern
./start-remote-build.sh --test "*Selenium*"

# Custom Maven goals
./start-remote-build.sh --goals "clean compile test"

# Combine options (parallel with specific tests)
./start-remote-build.sh --parallel --unit-tests-only
```
