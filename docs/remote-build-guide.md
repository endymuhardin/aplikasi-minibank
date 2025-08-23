# Remote Build Scripts

This document describes the remote build scripts that allow you to run Maven builds on a remote VPS server, which is particularly useful for resource-intensive builds and tests.

## Overview

The remote build system consists of several scripts that work together to:
1. Set up a temporary VPS for building
2. Sync your project to the remote server
3. Run Maven builds in the background
4. Monitor build progress
5. Fetch build results back to your local machine

## Prerequisites

Before using the remote build scripts, ensure you have:
- **doctl** - DigitalOcean CLI tool installed and configured
- **SSH key** - Added to your DigitalOcean account
- **rsync** - For file synchronization (usually pre-installed on macOS/Linux)

### SSH Key Setup

**1. Check existing SSH keys:**
```bash
ls -la ~/.ssh/
```
Look for files like `id_rsa.pub`, `id_ed25519.pub`, or similar `.pub` files.

**2. Generate SSH key if needed:**
```bash
# Generate ED25519 key (recommended)
ssh-keygen -t ed25519 -C "your-email@example.com"

# Or generate RSA key (for older systems)
ssh-keygen -t rsa -b 4096 -C "your-email@example.com"
```

**3. Upload SSH key to DigitalOcean:**

Using doctl (recommended):
```bash
# List existing keys
doctl compute ssh-key list

# Upload your public key
doctl compute ssh-key create "my-build-key" --public-key-file ~/.ssh/id_ed25519.pub

# Verify upload
doctl compute ssh-key list
```

Using DigitalOcean web console:
1. Go to Dashboard → Settings → Security → SSH Keys
2. Click "Add SSH Key"
3. Copy your public key: `cat ~/.ssh/id_ed25519.pub`
4. Paste and name it (e.g., "my-build-key")

**4. Important:** Use the **DigitalOcean key name** in scripts, not your local filename:
```bash
# ✅ Correct - use the name from DigitalOcean
./devops/spawn-build-server.sh --ssh-key "my-build-key"

# ❌ Wrong - don't use local filename
./devops/spawn-build-server.sh --ssh-key "id_ed25519"
```

**Troubleshooting SSH keys:**
```bash
# List your DigitalOcean SSH keys
doctl compute ssh-key list --format Name,ID

# Common issues:
# - "SSH key not found": Use exact name from doctl list
# - "Permission denied": Verify key upload and name
# - "No matching host key": Normal for new droplets, handled automatically
```

## Scripts

### Core Scripts

#### `start-remote-build.sh`
Syncs your project to a remote VPS and starts a Maven build in the background.

**Basic Usage:**
```bash
# From project root
./devops/start-remote-build.sh

# Or from devops folder
cd devops
./start-remote-build.sh
```

**Test-Specific Options:**
```bash
# Run specific test class
./devops/start-remote-build.sh --test "AccountServiceTest"

# Run tests matching pattern
./devops/start-remote-build.sh --test "*Integration*"

# Run only unit tests (surefire plugin)
./devops/start-remote-build.sh --unit-tests-only

# Run only integration tests (failsafe plugin)
./devops/start-remote-build.sh --integration-tests-only

# Skip all tests (compile only)
./devops/start-remote-build.sh --skip-tests

# Custom Maven goals
./devops/start-remote-build.sh --goals "clean compile test"

# Additional Maven options
./devops/start-remote-build.sh --opts "-T1C -Dmaven.test.failure.ignore=true"
```

**Available Options:**
- `-t, --test PATTERN` - Run specific tests (supports Maven test patterns)
- `-g, --goals GOALS` - Custom Maven goals (default: clean verify)
- `-o, --opts OPTS` - Additional Maven options (default: -T1C -DskipTests=false)
- `--skip-tests` - Skip all tests
- `--unit-tests-only` - Run only unit tests
- `--integration-tests-only` - Run only integration tests
- `-h, --help` - Show help with examples

**What it does:**
- Checks for an active VPS session (`.do-session-ip` file)
- Syncs the entire project to the remote server (excluding `.git` and `target` folders)
- Starts a Maven build with `mvn clean verify -DskipTests=false`
- Runs the build in the background using `nohup` and `disown`
- Returns control to your terminal immediately

#### `check-remote-build.sh`
Monitors the build status and fetches results when complete.

**Usage:**
```bash
# From project root
./devops/check-remote-build.sh

# Or from devops folder
cd devops
./check-remote-build.sh

# With debug information (shows detailed detection process)
./devops/check-remote-build.sh --debug
```

**What it does:**
- Checks if the Maven build is still running using multiple detection methods:
  - Process detection (`pgrep` for Maven/Java processes)
  - Log file growth monitoring
  - Build completion markers in logs
- If build is running: tails the build log in real-time
- If build is complete: fetches test reports and shows summary
- Downloads `surefire-reports` and `failsafe-reports` to your local `target/` folder
- Parses XML reports to show test summary with failures

### Supporting Scripts

#### `spawn-build-server.sh`
Creates a new DigitalOcean droplet, sets up the build environment, and starts a build.

**Basic Usage:**
```bash
# Fresh setup with base Ubuntu image
./devops/spawn-build-server.sh --ssh-key my-key

# Use latest snapshot (much faster)
./devops/spawn-build-server.sh --ssh-key my-key --use-latest-snapshot

# Use specific snapshot
./devops/spawn-build-server.sh --ssh-key my-key --snapshot snap-abc123
```

**Test-Specific Builds:**
```bash
# Create droplet and run specific tests
./devops/spawn-build-server.sh --ssh-key my-key --test "AccountServiceTest"

# Create droplet and run integration tests only
./devops/spawn-build-server.sh --ssh-key my-key --integration-tests-only

# Create droplet from snapshot and run pattern-matched tests
./devops/spawn-build-server.sh --ssh-key my-key --use-latest-snapshot --test "*Service*"

# Create droplet and skip tests (compile only)
./devops/spawn-build-server.sh --ssh-key my-key --skip-tests
```

**Configuration Options:**
```bash
# Custom droplet configuration with test options
./devops/spawn-build-server.sh -k my-key -s c-8 -r nyc1 --unit-tests-only
```

**Available Options:**
- **Infrastructure**: `-r/--region`, `-s/--size`, `-i/--image`, `-k/--ssh-key`
- **Snapshots**: `--snapshot ID`, `--use-latest-snapshot`
- **Build Options**: `-t/--test`, `-g/--goals`, `--skip-tests`, `--unit-tests-only`, `--integration-tests-only`

#### `destroy-build-session.sh`
Destroys the VPS when you're done building, with optional snapshot creation.

**Usage:**
```bash
./devops/destroy-build-session.sh
```
You'll be prompted to:
1. Create a snapshot before destroying (recommended for faster future builds)
2. Confirm droplet destruction

#### `list-snapshots.sh`
Lists available build environment snapshots.

**Usage:**
```bash
./devops/list-snapshots.sh
```

## Typical Workflow

### First Time Setup

1. **Create a fresh build server**:
   ```bash
   ./devops/spawn-build-server.sh --ssh-key my-digitalocean-key
   ```
   This will:
   - Create a new Ubuntu 22.04 droplet
   - Install Java 21, Maven, and Docker
   - Sync your project and start building

2. **Monitor the build**:
   ```bash
   ./devops/check-remote-build.sh
   ```

3. **When done, destroy with snapshot**:
   ```bash
   ./devops/destroy-build-session.sh
   ```
   Choose "Y" to create a snapshot for faster future builds.

### Subsequent Builds (Using Snapshots)

1. **Use snapshot for instant setup**:
   ```bash
   ./devops/spawn-build-server.sh --ssh-key my-key --use-latest-snapshot
   ```
   This is much faster since the environment is pre-configured.

2. **Monitor and destroy as before**:
   ```bash
   ./devops/check-remote-build.sh
   ./devops/destroy-build-session.sh
   ```

### Alternative: Manual Build on Existing Server

If you already have a server running:

1. **Start a remote build**:
   ```bash
   ./devops/start-remote-build.sh
   ```

2. **Monitor the build**:
   ```bash
   ./devops/check-remote-build.sh
   ```

3. **Repeat as needed** for code changes.

## Features

### Snapshot-Based Fast Setup

**Problem**: Setting up Java, Maven, and Docker on each new droplet takes 5-10 minutes.

**Solution**: Create snapshots of configured environments for instant reuse.

- **First build**: Use fresh Ubuntu image, setup takes ~8 minutes
- **Subsequent builds**: Use snapshot, ready in ~30 seconds
- **Automatic snapshot management**: Latest snapshot is tracked automatically
- **Manual snapshot selection**: Choose specific snapshots if needed

### Intelligent Build Detection

The `check-remote-build.sh` script uses multiple methods to reliably detect if a build is still running:

1. **Process Detection**: Looks for `mvn`, `maven`, and Java processes running Maven
2. **Log File Monitoring**: Checks if `build.log` is still growing
3. **Completion Markers**: Searches for "BUILD SUCCESS", "BUILD FAILURE", or "Total time:" in logs
4. **Timeout Handling**: Uses SSH timeouts to prevent hanging connections

### Robust File Synchronization

- Uses `rsync` with SSH for efficient file transfer
- Excludes unnecessary files (`.git`, `target`) to speed up sync
- Preserves file permissions and timestamps
- Handles connection timeouts gracefully

### Comprehensive Test Reporting

- Downloads both Surefire (unit tests) and Failsafe (integration tests) reports
- Parses XML reports to show test counts, failures, and errors
- Shows specific failed test names for quick debugging
- Compatible with both macOS and Linux stat commands

### Path Flexibility

Both scripts automatically detect where they're being run from:
- Can be executed from project root using wrapper scripts
- Can be executed from `devops/` folder directly
- Automatically adjusts paths based on execution location

## Test Specification and Patterns

The remote build scripts support fine-grained test control using Maven's test selection capabilities.

### Test Pattern Syntax

**Single Test Class:**
```bash
--test "AccountServiceTest"
```

**Wildcard Patterns:**
```bash
# All integration tests
--test "*Integration*"

# All tests starting with "Account"
--test "Account*"

# All tests ending with "IT"
--test "*IT"
```

**Multiple Test Classes:**
```bash
# Run specific test classes
--test "TestA,TestB,TestC"

# Mix patterns and specific classes
--test "AccountServiceTest,*Integration*"
```

**Package-Level Patterns:**
```bash
# All tests in a package
--test "com.example.service.*"

# Tests in multiple packages
--test "com.example.service.*,com.example.web.*"
```

### Test Type Selection

**Unit Tests Only:**
```bash
# Runs: mvn clean test
./devops/start-remote-build.sh --unit-tests-only
```

**Integration Tests Only:**
```bash
# Runs: mvn clean verify -DskipUnitTests=true
./devops/start-remote-build.sh --integration-tests-only
```

**Skip All Tests:**
```bash
# Runs: mvn clean verify -DskipTests=true
./devops/start-remote-build.sh --skip-tests
```

### Maven Goal Customization

**Custom Goals:**
```bash
# Just compile and run tests (no package/verify)
./devops/start-remote-build.sh --goals "clean compile test"

# Full build with site generation
./devops/start-remote-build.sh --goals "clean verify site"
```

**Custom Maven Options:**
```bash
# Ignore test failures and continue
./devops/start-remote-build.sh --opts "-T1C -Dmaven.test.failure.ignore=true"

# Run with specific profiles
./devops/start-remote-build.sh --opts "-T1C -DskipTests=false -Pintegration"
```

### Common Use Cases

**Development Testing:**
```bash
# Quick feedback: unit tests only
./devops/start-remote-build.sh --unit-tests-only

# Test specific feature
./devops/start-remote-build.sh --test "*Account*"
```

**CI/CD Pipeline:**
```bash
# Full verification with all tests
./devops/spawn-build-server.sh --ssh-key ci-key --use-latest-snapshot

# Smoke tests only
./devops/spawn-build-server.sh --ssh-key ci-key --test "*Smoke*"
```

**Debugging:**
```bash
# Run failing test in isolation
./devops/start-remote-build.sh --test "FailingTestClass"

# Compile-only check (no tests)
./devops/start-remote-build.sh --skip-tests
```

## Configuration

### SSH Options

The scripts use these SSH options for reliable connections:
- `StrictHostKeyChecking=no`: Skips host key verification
- `ConnectTimeout=10`: 10-second connection timeout
- `ServerAliveInterval=5`: Keeps connections alive

### Maven Build Command

**Default Build:**
```bash
mvn -T1C clean verify -DskipTests=false
```

**With Test Patterns:**
```bash
# Specific test class
mvn -T1C clean verify -DskipTests=false -Dtest=AccountServiceTest

# Pattern matching
mvn -T1C clean verify -DskipTests=false -Dtest=*Integration*

# Unit tests only
mvn clean test

# Integration tests only
mvn clean verify -DskipUnitTests=true
```

**Options Explained:**
- `-T1C`: Uses one thread per CPU core for parallel building
- `clean verify`: Full clean build with all tests
- `-DskipTests=false`: Explicitly runs all tests
- `-Dtest=PATTERN`: Run specific tests matching the pattern
- `-DskipUnitTests=true`: Skip unit tests, run integration tests only

### Excluded Files

These files/folders are excluded during sync to speed up transfer:
- `.git/` - Git repository data
- `target/` - Local build artifacts

## Troubleshooting

### Build Not Detected as Complete

If `check-remote-build.sh` thinks the build is still running when it's actually done:
- Check if there are orphaned Java processes on the remote server
- Look at the last few lines of `build.log` for completion markers
- The script will eventually detect completion through log file analysis

### SSH Connection Issues

If you get SSH connection errors:
- Verify the VPS is still running: `cat devops/.do-session-ip`
- Check your internet connection
- The scripts have built-in timeouts to prevent hanging

### Missing Test Reports

If test reports aren't downloaded:
- The build might have failed before generating reports
- Check the build log for Maven errors
- Integration tests (Failsafe) reports are only downloaded if they exist

### Permission Issues

If you get permission errors:
- Make sure scripts are executable: `chmod +x *.sh`
- Check that the `target/` directory is writable

## File Locations

After a successful build, you'll find:
- **Test Reports**: `target/surefire-reports/` and `target/failsafe-reports/`
- **Build Log**: Viewable during build via `check-remote-build.sh`
- **Session Info**: `devops/.do-session-ip` (VPS IP address)

## Troubleshooting

### Common Issues and Solutions

#### APT/GPG Repository Errors

If you encounter errors like:
```
W: GPG error: http://archive.ubuntu.com/ubuntu jammy InRelease: Splitting up /var/lib/apt/lists/archive.ubuntu.com_ubuntu_dists_jammy_InRelease into data and signature failed
E: The repository 'http://archive.ubuntu.com/ubuntu jammy InRelease' is not signed.
```

The enhanced scripts now include automatic recovery mechanisms, but if your build session gets stuck:

**Option 1: Wait for automatic recovery**
- The script will automatically clean APT cache and retry
- Package installations have fallback mechanisms
- Most repository issues resolve automatically

**Option 2: Restart with a fresh droplet**
```bash
# Destroy current session if stuck
./devops/destroy-build-session.sh

# Start fresh with improved error handling
./devops/spawn-build-server.sh --ssh-key "your-key-name"
```

#### SSH Connection Issues

**"SSH key not found" errors:**
```bash
# List your DigitalOcean SSH keys to verify the name
doctl compute ssh-key list --format Name,ID

# Use the exact name from the list
./devops/spawn-build-server.sh --ssh-key "exact-key-name-from-list"
```

**"Permission denied" errors:**
- Verify your SSH key is uploaded to DigitalOcean
- Check that you're using the correct key name (not filename)
- Ensure your local SSH agent has the key loaded: `ssh-add -l`

#### Build Monitoring Issues

**Build appears stuck or not progressing:**
```bash
# Check with debug mode for detailed information
./devops/check-remote-build.sh --debug

# If truly stuck, restart the build
./devops/start-remote-build.sh
```

**Can't connect to remote server:**
```bash
# Check if session file exists
ls -la devops/.do-session-ip

# Manually check connection
ssh -o StrictHostKeyChecking=no root@$(cat devops/.do-session-ip) "echo 'Connection OK'"
```

#### Test-Related Issues

**No tests matching pattern:**
```bash
# Check available test classes first
./devops/start-remote-build.sh --goals "test-compile" --skip-tests
# Then SSH in and run: find /root/project -name "*Test*.class"

# Verify pattern syntax
./devops/start-remote-build.sh --test "YourPattern*" --goals "test -X"
```

**Tests fail on remote but pass locally:**
```bash
# Check environment differences
./devops/start-remote-build.sh --goals "clean compile test -X" --test "FailingTest"

# Run with debug output
./devops/start-remote-build.sh --opts "-T1C -DskipTests=false -X" --test "FailingTest"
```

**Wrong test type running:**
```bash
# Verify Maven command being executed (shown in output)
# Unit tests should use: mvn clean test
# Integration tests should use: mvn clean verify -DskipUnitTests=true

# Check test naming conventions:
# Unit tests: *Test.java (surefire plugin)
# Integration tests: *IT.java, *ITCase.java (failsafe plugin)
```

**Build command preview:**
All scripts show the exact Maven command being run:
```
🔧 Maven command: mvn clean verify -T1C -DskipTests=false -Dtest=AccountServiceTest
```
Use this to verify your options are applied correctly.

#### Cost Management

**Forgot to destroy droplet:**
```bash
# List all your droplets
doctl compute droplet list

# Destroy specific droplet by ID
doctl compute droplet delete <droplet-id>

# Or use the session destroyer if you still have session files
./devops/destroy-build-session.sh
```

## Benefits

### Resource Efficiency
- Offloads CPU and memory intensive builds to cloud VPS
- Frees up your local machine for other work
- Faster builds on high-performance VPS instances

### Parallel Development
- Multiple developers can use separate VPS instances
- No conflicts with local development environment
- Clean build environment every time

### Continuous Integration
- Can be integrated into CI/CD pipelines
- Consistent build environment across team
- Easy to scale up for larger projects

## Security Notes

- SSH connections use temporary VPS instances
- No persistent data stored on VPS after destruction
- Root access is used for simplicity (VPS is disposable)
- Always destroy VPS instances when done to avoid costs
