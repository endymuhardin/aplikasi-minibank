#!/bin/bash

# Minibank Application Deployment Script
# This script automates the deployment process using Ansible

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ANSIBLE_DIR="$PROJECT_ROOT/ansible"
TARGET_DIR="$PROJECT_ROOT/target"

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "\n${GREEN}================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}================================${NC}\n"
}

# Function to check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check if Ansible is installed
    if ! command -v ansible &> /dev/null; then
        print_error "Ansible is not installed"
        print_info "Install Ansible:"
        print_info "  Ubuntu/Debian: sudo apt install ansible"
        print_info "  macOS: brew install ansible"
        exit 1
    fi
    print_success "Ansible is installed: $(ansible --version | head -n1)"

    # Check if ansible directory exists
    if [ ! -d "$ANSIBLE_DIR" ]; then
        print_error "Ansible directory not found: $ANSIBLE_DIR"
        exit 1
    fi
    print_success "Ansible directory found"

    # Check if inventory file exists
    if [ ! -f "$ANSIBLE_DIR/inventory.ini" ]; then
        print_error "Inventory file not found: $ANSIBLE_DIR/inventory.ini"
        exit 1
    fi
    print_success "Inventory file found"

    # Check if deploy-vars.yml exists
    if [ ! -f "$ANSIBLE_DIR/vars/deploy-vars.yml" ]; then
        print_error "Configuration file not found: $ANSIBLE_DIR/vars/deploy-vars.yml"
        print_info "Copy the example file and update it:"
        print_info "  cp $ANSIBLE_DIR/vars/deploy-vars.yml.example $ANSIBLE_DIR/vars/deploy-vars.yml"
        print_info "  vim $ANSIBLE_DIR/vars/deploy-vars.yml"
        exit 1
    fi
    print_success "Configuration file found"
}

# Function to build application
build_application() {
    print_header "Building Application"

    cd "$PROJECT_ROOT"

    if [ -f "$TARGET_DIR/aplikasi-minibank-0.0.1-SNAPSHOT.jar" ]; then
        print_warning "JAR file already exists"
        read -p "Do you want to rebuild? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Skipping build"
            return
        fi
    fi

    print_info "Running Maven build..."
    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        print_success "Build completed successfully"
        JAR_SIZE=$(du -h "$TARGET_DIR/aplikasi-minibank-0.0.1-SNAPSHOT.jar" | cut -f1)
        print_info "JAR file size: $JAR_SIZE"
    else
        print_error "Build failed"
        exit 1
    fi
}

# Function to test SSH connection
test_connection() {
    print_header "Testing Server Connection"

    cd "$ANSIBLE_DIR"
    print_info "Pinging servers..."

    if ansible -i inventory.ini minibank_servers -m ping; then
        print_success "Connection test successful"
    else
        print_error "Connection test failed"
        print_info "Check your SSH configuration and try: ssh administrator@103.103.23.76"
        exit 1
    fi
}

# Function to deploy application
deploy_application() {
    print_header "Deploying Application"

    cd "$ANSIBLE_DIR"

    # Ask for confirmation
    print_warning "This will deploy the application to production server"
    read -p "Continue with deployment? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Deployment cancelled"
        exit 0
    fi

    # Run ansible playbook
    print_info "Running Ansible playbook..."

    ANSIBLE_ARGS=""
    if [ "$VERBOSE" = "true" ]; then
        ANSIBLE_ARGS="-vvv"
    fi

    if [ "$DRY_RUN" = "true" ]; then
        ANSIBLE_ARGS="$ANSIBLE_ARGS --check"
        print_info "Running in DRY RUN mode (no changes will be made)"
    fi

    if [ -n "$TAGS" ]; then
        ANSIBLE_ARGS="$ANSIBLE_ARGS --tags $TAGS"
    fi

    if ansible-playbook -i inventory.ini deploy.yml $ANSIBLE_ARGS; then
        print_success "Deployment completed successfully"
    else
        print_error "Deployment failed"
        exit 1
    fi
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deploy Minibank Islamic Banking Application to production server

OPTIONS:
    -h, --help          Show this help message
    -b, --build         Build application before deployment
    -s, --skip-build    Skip build step (use existing JAR)
    -t, --test-only     Only test connection, don't deploy
    -d, --dry-run       Run deployment in check mode (no changes)
    -v, --verbose       Enable verbose output
    --tags TAGS         Run only specific deployment tags
                        (prepare, build, deploy, service, verify, cleanup)

EXAMPLES:
    # Full deployment with build
    $0 --build

    # Deploy without rebuilding
    $0 --skip-build

    # Test connection only
    $0 --test-only

    # Dry run to see what would change
    $0 --dry-run

    # Deploy only JAR file (skip service config)
    $0 --tags deploy

    # Verbose deployment
    $0 --verbose --build

EOF
}

# Main script
main() {
    print_header "Minibank Application Deployment"

    # Parse command line arguments
    BUILD=false
    SKIP_BUILD=false
    TEST_ONLY=false
    DRY_RUN=false
    VERBOSE=false
    TAGS=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_usage
                exit 0
                ;;
            -b|--build)
                BUILD=true
                shift
                ;;
            -s|--skip-build)
                SKIP_BUILD=true
                shift
                ;;
            -t|--test-only)
                TEST_ONLY=true
                shift
                ;;
            -d|--dry-run)
                DRY_RUN=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            --tags)
                TAGS="$2"
                shift 2
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done

    # Check prerequisites
    check_prerequisites

    # Test connection
    test_connection

    # Exit if test-only mode
    if [ "$TEST_ONLY" = "true" ]; then
        print_success "Test completed"
        exit 0
    fi

    # Build if requested
    if [ "$BUILD" = "true" ]; then
        build_application
    elif [ "$SKIP_BUILD" = "false" ]; then
        # Default: ask user
        if [ ! -f "$TARGET_DIR/aplikasi-minibank-0.0.1-SNAPSHOT.jar" ]; then
            print_warning "JAR file not found"
            build_application
        else
            JAR_SIZE=$(du -h "$TARGET_DIR/aplikasi-minibank-0.0.1-SNAPSHOT.jar" | cut -f1)
            JAR_DATE=$(stat -f "%Sm" -t "%Y-%m-%d %H:%M:%S" "$TARGET_DIR/aplikasi-minibank-0.0.1-SNAPSHOT.jar" 2>/dev/null || stat -c "%y" "$TARGET_DIR/aplikasi-minibank-0.0.1-SNAPSHOT.jar" 2>/dev/null)
            print_info "Found JAR file: $JAR_SIZE, modified: $JAR_DATE"
        fi
    fi

    # Deploy application
    deploy_application

    print_header "Deployment Summary"
    print_success "Application deployed successfully!"
    print_info "Server: 103.103.23.76"
    print_info "Service: minibank.service"
    print_info "Port: 10002"
    print_info ""
    print_info "Useful commands:"
    print_info "  Check status: ssh administrator@103.103.23.76 'sudo systemctl status minibank.service'"
    print_info "  View logs: ssh administrator@103.103.23.76 'sudo journalctl -u minibank.service -f'"
    print_info "  Restart: ssh administrator@103.103.23.76 'sudo systemctl restart minibank.service'"
}

# Run main function
main "$@"
