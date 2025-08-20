# Configuration and Environment Setup

### 1. Development Environment
```yaml
# compose.yaml
services:
  postgres:
    image: 'postgres:17'
    environment:
      - 'POSTGRES_DB=pgminibank'
      - 'POSTGRES_PASSWORD=minibank1234'
      - 'POSTGRES_USER=minibank'
    ports:
      - '2345:5432'
    volumes:
      - './db-minibank:/var/lib/postgresql/data'
```

### 2. Application Configuration
```properties
# application.properties
spring.application.name=aplikasi-minibank
spring.datasource.url=jdbc:postgresql://localhost:2345/pgminibank
spring.datasource.username=minibank
spring.datasource.password=${DB_PASSWORD:minibank1234}

# Selenium Test Configuration
selenium.recording.enabled=false
```

### 3. Frontend Build Configuration
```javascript
// tailwind.config.js
module.exports = {
  content: [
    "../resources/templates/**/*.html",
    "../resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        'bsi-primary': '#00a39d',
        'bsi-secondary': '#f15922',
        // Brand-specific color palette
      }
    },
  }
}
```

### 4. Maven Configuration
```xml
<properties>
    <java.version>21</java.version>
    <sonar.organization>tazkia-ac-id</sonar.organization>
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    <karate.version>1.4.1</karate.version>
    <jacoco.version>0.8.12</jacoco.version>
</properties>
```
