# Ansible Deployment Guide

This directory contains Ansible automation scripts for deploying the Minibank Islamic Banking Application to production servers.

## Prerequisites

1. **Ansible installed** on your local machine:
   ```bash
   # Install Ansible (Ubuntu/Debian)
   sudo apt update
   sudo apt install ansible

   # Install Ansible (macOS)
   brew install ansible
   ```

2. **SSH access** to the target server without password (using SSH keys)

3. **Sudo privileges** on the target server

4. **Built application** - JAR file must exist in `target/` directory:
   ```bash
   mvn clean package
   ```

## Quick Start

### 1. Configure Deployment Variables

Copy the example configuration and update with your credentials:

```bash
cd ansible
cp vars/deploy-vars.yml.example vars/deploy-vars.yml
```

Edit `vars/deploy-vars.yml` and update:
- Database password (`db_password`)
- Any other environment-specific settings

**IMPORTANT**: The `deploy-vars.yml` file is gitignored to protect credentials.

### 2. Test Connection

```bash
ansible -i inventory.ini minibank_servers -m ping
```

Expected output:
```
minibank_production | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
```

### 3. Deploy Application

Full deployment:
```bash
ansible-playbook -i inventory.ini deploy.yml
```

Or use the convenient script:
```bash
../scripts/deploy-ansible.sh
```

## Deployment Process

The Ansible playbook performs the following steps:

1. **Preparation**
   - Verify SSH connection
   - Ensure application and backup directories exist

2. **Build Verification**
   - Check if JAR file exists locally
   - Display JAR file information

3. **Deployment**
   - Stop running application service
   - Backup current JAR file (timestamped)
   - Copy new JAR file to server
   - Update symlink to point to new JAR

4. **Service Management**
   - Deploy/update systemd service configuration
   - Reload systemd daemon
   - Start application service

5. **Verification**
   - Wait for application port to be available
   - Check application health endpoint (if available)
   - Display deployment status

6. **Cleanup**
   - Remove old backups (older than 7 days)
   - Keep only last 10 backups

## Deployment Options

### Run Specific Tags

Execute only certain parts of the deployment:

```bash
# Only prepare directories
ansible-playbook -i inventory.ini deploy.yml --tags prepare

# Only deploy JAR (skip service configuration)
ansible-playbook -i inventory.ini deploy.yml --tags deploy

# Only update service configuration
ansible-playbook -i inventory.ini deploy.yml --tags service

# Only verify deployment
ansible-playbook -i inventory.ini deploy.yml --tags verify

# Cleanup old backups only
ansible-playbook -i inventory.ini deploy.yml --tags cleanup
```

### Skip Tags

Skip certain parts of the deployment:

```bash
# Deploy without health check
ansible-playbook -i inventory.ini deploy.yml --skip-tags verify

# Deploy without cleanup
ansible-playbook -i inventory.ini deploy.yml --skip-tags cleanup
```

### Dry Run

Check what would be changed without actually making changes:

```bash
ansible-playbook -i inventory.ini deploy.yml --check
```

### Verbose Output

For debugging:

```bash
ansible-playbook -i inventory.ini deploy.yml -v    # verbose
ansible-playbook -i inventory.ini deploy.yml -vv   # more verbose
ansible-playbook -i inventory.ini deploy.yml -vvv  # very verbose
```

## Directory Structure

```
ansible/
├── README.md                        # This file
├── inventory.ini                    # Server inventory
├── deploy.yml                       # Main deployment playbook
├── templates/
│   └── minibank.service.j2         # Systemd service template
└── vars/
    ├── deploy-vars.yml.example     # Configuration template
    └── deploy-vars.yml             # Actual config (gitignored)
```

## Configuration Files

### inventory.ini

Defines target servers and connection settings:

```ini
[minibank_servers]
minibank_production ansible_host=103.103.23.76 ansible_user=administrator

[minibank_servers:vars]
ansible_python_interpreter=/usr/bin/python3
```

### vars/deploy-vars.yml

Deployment configuration variables (see `deploy-vars.yml.example` for template):

- **Application settings**: JAR name, install directory, user/group
- **Java settings**: JAVA_HOME path (using SDKMan)
- **JVM settings**: Memory allocation
- **Database settings**: Connection details and credentials
- **Service settings**: Systemd configuration

### templates/minibank.service.j2

Systemd service unit file template. Configured to:
- Run as `minibank` user
- Use Java from SDKMan
- Set database connection via environment variables
- Auto-restart on failure
- Use symlink for JAR file (easy version switching)

## Deployment Architecture

### Application Location
- **Install Directory**: `/var/lib/aplikasi-minibank/`
- **JAR File**: `aplikasi-minibank-0.0.1-SNAPSHOT.jar`
- **Symlink**: `minibank.jar` → actual JAR file
- **Backups**: `/var/lib/aplikasi-minibank/backups/`

### Service Management
- **Service Name**: `minibank.service`
- **User**: `minibank`
- **Java**: Managed by SDKMan at `/var/lib/aplikasi-minibank/.sdkman/`
- **Port**: 10002

### Database
- **Host**: localhost
- **Port**: 5432 (PostgreSQL default)
- **Database**: `db_minibank`
- **User**: `minibank`

## Common Operations

### View Application Logs

```bash
ssh administrator@103.103.23.76 'sudo journalctl -u minibank.service -f'
```

### Check Service Status

```bash
ssh administrator@103.103.23.76 'sudo systemctl status minibank.service'
```

### Restart Service Manually

```bash
ssh administrator@103.103.23.76 'sudo systemctl restart minibank.service'
```

### List Backups

```bash
ssh administrator@103.103.23.76 'ls -lh /var/lib/aplikasi-minibank/backups/'
```

### Rollback to Previous Version

```bash
# SSH to server
ssh administrator@103.103.23.76

# Stop service
sudo systemctl stop minibank.service

# Update symlink to backup
sudo ln -sf /var/lib/aplikasi-minibank/backups/aplikasi-minibank-0.0.1-SNAPSHOT.jar.20251219T103045 \
    /var/lib/aplikasi-minibank/minibank.jar

# Start service
sudo systemctl start minibank.service
```

## Troubleshooting

### Deployment Fails: JAR Not Found

**Problem**: `JAR file not found at target/aplikasi-minibank-0.0.1-SNAPSHOT.jar`

**Solution**: Build the application first:
```bash
mvn clean package
```

### SSH Connection Failed

**Problem**: Cannot connect to server

**Solution**:
1. Check SSH key is added: `ssh-add -l`
2. Test manual SSH: `ssh administrator@103.103.23.76`
3. Verify server is accessible: `ping 103.103.23.76`

### Service Fails to Start

**Problem**: Service stops immediately after starting

**Solution**:
1. Check logs: `journalctl -u minibank.service -n 50`
2. Verify Java path: `ls -la /var/lib/aplikasi-minibank/.sdkman/candidates/java/current`
3. Check database connectivity
4. Verify permissions on JAR file

### Health Check Timeout

**Problem**: Health check fails or times out

**Solution**:
1. Application might not expose `/actuator/health` endpoint
2. Add Spring Boot Actuator dependency if needed
3. Or skip health check: `--skip-tags verify`

### Permission Denied

**Problem**: Cannot write to deployment directory

**Solution**:
1. Verify `minibank` user owns the directory
2. Check ansible user has sudo privileges
3. Verify `become: yes` is set in playbook

## Security Notes

1. **Credentials Protection**
   - `deploy-vars.yml` is gitignored
   - Never commit credentials to version control
   - Use Ansible Vault for additional encryption if needed

2. **SSH Security**
   - Use SSH keys instead of passwords
   - Consider restricting SSH access by IP
   - Use `StrictHostKeyChecking` in production

3. **Database Security**
   - Use strong database passwords
   - Restrict database access to localhost
   - Regularly rotate credentials

## Advanced: Using Ansible Vault

For additional security, encrypt sensitive variables:

```bash
# Encrypt the variables file
ansible-vault encrypt vars/deploy-vars.yml

# Deploy with vault password
ansible-playbook -i inventory.ini deploy.yml --ask-vault-pass

# Or use vault password file
echo "your-vault-password" > .vault_pass
ansible-playbook -i inventory.ini deploy.yml --vault-password-file .vault_pass
```

## Support

For issues or questions:
1. Check application logs: `journalctl -u minibank.service`
2. Review Ansible output for error messages
3. Verify all prerequisites are met
4. Consult main project documentation: `../CLAUDE.md`
