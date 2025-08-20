# Project Structure and Package Organization

### Source Code Structure
```
src/
├── main/
│   ├── java/id/ac/tazkia/minibank/
│   │   ├── AplikasiMinibankApplication.java    # Main application class
│   │   ├── config/                             # Configuration classes
│   │   │   └── SecurityConfig.java
│   │   ├── controller/                         # Controller layer
│   │   │   ├── rest/                          # REST API controllers
│   │   │   └── web/                           # Web MVC controllers
│   │   ├── dto/                               # Data Transfer Objects
│   │   ├── entity/                            # JPA entities
│   │   ├── repository/                        # Data access layer
│   │   └── service/                           # Business logic layer
│   ├── resources/
│   │   ├── application.properties             # Main configuration
│   │   ├── db/migration/                      # Flyway migrations
│   │   ├── static/                           # Static web assets
│   │   └── templates/                        # Thymeleaf templates
│   └── frontend/                             # Frontend build assets
│       ├── package.json
│       ├── tailwind.config.js
│       ├── postcss.config.js
│       └── src/input.css
└── test/
    ├── java/id/ac/tazkia/minibank/
    │   ├── integration/                       # Integration tests
    │   │   ├── controller/                   # Controller tests
    │   │   ├── repository/                   # Repository tests
    │   │   └── service/                      # Service tests
    │   ├── functional/                       # Functional tests
    │   │   ├── api/                         # API BDD tests (Karate)
    │   │   └── web/                         # Selenium UI tests
    │   ├── config/                           # Test configuration
    │   │   ├── ParallelSeleniumManager.java # Selenium WebDriver management
    │   │   └── SeleniumTestProperties.java  # Test properties
    │   ├── parallel/                         # Parallel test infrastructure
    │   └── util/                            # Test utilities
    └── resources/
        ├── application-test.yml              # Test configuration
        ├── junit-platform.properties        # JUnit 5 parallel configuration
        ├── fixtures/                        # CSV test data
        ├── karate/                          # Karate feature files
        └── sql/                             # Test SQL scripts
```

### Package Naming Conventions
- **Base package**: `id.ac.tazkia.minibank`
- **Domain-driven organization**: Packages organized by layer (controller, service, repository, entity)
- **Clear separation**: REST vs Web controllers in separate packages
- **Test mirroring**: Test packages mirror main source structure
