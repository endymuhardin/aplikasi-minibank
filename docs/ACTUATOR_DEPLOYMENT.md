# Spring Boot Actuator for Health Check

## Overview

This document describes the Spring Boot Actuator configuration implemented to support automated health checks during Ansible deployments.

## Changes Made

### 1. Dependencies Added

**File**: `pom.xml`

Added Spring Boot Actuator dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. Application Configuration

**File**: `src/main/resources/application.properties`

Added actuator endpoint configuration:

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
```

**Exposed Endpoints**:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information

### 3. Security Configuration

**File**: `src/main/java/id/ac/tazkia/minibank/config/SecurityConfig.java`

Updated security configuration to allow unauthenticated access to actuator endpoints:

```java
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
```

**Rationale**: Health check endpoints must be accessible without authentication for:
- Ansible deployment verification
- Load balancer health checks
- Monitoring systems
- Kubernetes liveness/readiness probes

### 4. Functional Tests

**File**: `src/test/java/id/ac/tazkia/minibank/functional/success/ActuatorHealthCheckTest.java`

Created comprehensive Playwright functional tests:

- ✅ Health endpoint returns UP status
- ✅ Health endpoint accessible without authentication
- ✅ Health endpoint returns JSON content type
- ✅ Info endpoint accessible without authentication

**Test Results**:
```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Health Check Endpoint Details

### Health Endpoint

**URL**: `http://localhost:10002/actuator/health`

**Response** (Status: UP):
```json
{
  "status": "UP"
}
```

**Response** (Status: DOWN):
```json
{
  "status": "DOWN"
}
```

**Content-Type**: `application/vnd.spring-boot.actuator.v3+json`

**HTTP Status Codes**:
- `200 OK` - Application is healthy
- `503 Service Unavailable` - Application is unhealthy

### Info Endpoint

**URL**: `http://localhost:10002/actuator/info`

**Response**:
```json
{}
```

Note: Additional application info can be configured in `application.properties`:
```properties
info.app.name=@project.name@
info.app.description=@project.description@
info.app.version=@project.version@
```

## Ansible Integration

The Ansible deployment playbook (`ansible/deploy.yml`) uses the health endpoint to verify successful deployment:

```yaml
- name: Check application health
  uri:
    url: "http://localhost:{{ server_port }}/actuator/health"
    method: GET
    return_content: yes
  register: health_check
  until: health_check.status == 200
  retries: 5
  delay: 10
  ignore_errors: yes
```

**Benefits**:
1. **Automated Verification**: Confirms application started successfully
2. **Database Connectivity**: Health check validates database connection
3. **Dependencies Check**: Verifies all required components are available
4. **Early Failure Detection**: Catches startup issues immediately

## Testing

### Run Actuator Tests

```bash
# Run actuator health check tests
mvn test -Dtest=ActuatorHealthCheckTest

# Run all functional tests (includes actuator tests)
mvn test -Dtest=**/functional/success/*Test
```

### Manual Testing

```bash
# Start the application
mvn spring-boot:run

# Check health endpoint
curl http://localhost:10002/actuator/health

# Expected output: {"status":"UP"}

# Check info endpoint
curl http://localhost:10002/actuator/info
```

### Production Testing

```bash
# Test health endpoint on production server
curl http://103.103.23.76:10002/actuator/health

# Using Ansible to verify
ansible -i ansible/inventory.ini minibank_servers -m shell \
  -a "curl -s http://localhost:10002/actuator/health"
```

## Security Considerations

### Why Public Access is Safe

1. **Minimal Information Disclosure**
   - Health endpoint only returns `{"status":"UP"}` or `{"status":"DOWN"}`
   - No sensitive application data exposed
   - No authentication credentials required

2. **Industry Standard**
   - Common practice for health check endpoints
   - Required by most deployment and monitoring tools
   - Used by Kubernetes, Docker, AWS ELB, etc.

3. **Details Protected**
   - Detailed health information requires authentication
   - `show-details=when-authorized` ensures sensitive data is protected
   - Only basic status visible to unauthenticated users

### Security Best Practices

1. **Network Security**
   - Use firewalls to restrict external access if needed
   - Consider VPN or private networks for sensitive environments

2. **Rate Limiting**
   - Implement rate limiting at reverse proxy level
   - Prevents health check endpoint abuse

3. **Monitoring**
   - Monitor excessive health check requests
   - Alert on unusual patterns

## Health Check Components

Spring Boot automatically includes health indicators for:

- ✅ **Database** - PostgreSQL connection status
- ✅ **Disk Space** - Available disk space
- ✅ **JVM** - Java Virtual Machine status
- ✅ **DataSource** - HikariCP connection pool

## Troubleshooting

### Health Check Returns DOWN

**Possible Causes**:
1. Database connection failure
2. Insufficient disk space
3. Application startup errors
4. Missing dependencies

**Diagnosis**:
```bash
# Check detailed health (requires authentication)
curl -u admin:password http://localhost:10002/actuator/health

# Check application logs
journalctl -u minibank.service -n 100

# Test database connectivity
psql -h localhost -p 5432 -U minibank -d db_minibank -c "SELECT 1"
```

### Health Endpoint Not Accessible

**Possible Causes**:
1. Application not started
2. Port not open
3. Firewall blocking requests
4. Wrong URL/port

**Diagnosis**:
```bash
# Check if application is running
systemctl status minibank.service

# Check if port is listening
netstat -tlnp | grep 10002

# Test from localhost
curl -v http://localhost:10002/actuator/health

# Check firewall
sudo ufw status
```

### Ansible Deployment Fails at Health Check

**Possible Causes**:
1. Application startup taking too long
2. Database migration errors
3. Configuration errors

**Solutions**:
```bash
# Increase timeout in Ansible playbook
retries: 10
delay: 15

# Check application startup time
journalctl -u minibank.service --since "5 minutes ago"

# Manually verify health
ssh administrator@103.103.23.76 'curl http://localhost:10002/actuator/health'
```

## Additional Actuator Endpoints (Optional)

To enable additional endpoints, update `application.properties`:

```properties
# Expose more endpoints (use with caution in production)
management.endpoints.web.exposure.include=health,info,metrics,env,beans

# Restrict access to sensitive endpoints
management.endpoint.metrics.enabled=true
management.endpoint.env.enabled=true
```

**Available Endpoints**:
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties
- `/actuator/beans` - Spring beans
- `/actuator/loggers` - Logging configuration
- `/actuator/threaddump` - Thread dump
- `/actuator/heapdump` - Heap dump

**Security Warning**: Only expose additional endpoints in development environments or secure with proper authentication.

## References

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Actuator Health Endpoints](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)
- [Ansible URI Module](https://docs.ansible.com/ansible/latest/collections/ansible/builtin/uri_module.html)
- [Kubernetes Health Checks](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-19 | Initial actuator implementation for Ansible deployment |
