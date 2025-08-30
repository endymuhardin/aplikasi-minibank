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
    │   ├── config/                           # Test infrastructure configuration
    │   │   ├── BaseIntegrationTest.java      # Database test foundation
    │   │   ├── BasePlaywrightTest.java       # Functional test foundation
    │   │   └── TestDataFactory.java          # Test data generation utilities
    │   ├── integration/                      # Database integration tests
    │   │   ├── repository/                   # Repository tests with @DataJpaTest
    │   │   └── service/                      # Service layer integration tests
    │   │       └── BranchServiceIntegrationTest.java
    └── resources/
        └── application-test.properties      # Test-specific application configuration
```

### Package Naming Conventions
- **Base package**: `id.ac.tazkia.minibank`
- **Domain-driven organization**: Packages organized by layer (controller, service, repository, entity)
- **Clear separation**: REST vs Web controllers in separate packages
- **Test mirroring**: Test packages mirror main source structure
