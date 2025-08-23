# Aplikasi Mini Bank

Aplikasi Mini Bank adalah sistem perbankan syariah berbasis Spring Boot yang menyediakan fungsionalitas lengkap untuk operasional bank syariah, termasuk manajemen nasabah, produk Islamic banking, transaksi, dan sistem RBAC.

## 🚀 Fitur Utama

### Manajemen Nasabah
- **Registrasi Nasabah:** Pendaftaran nasabah perorangan dan korporat dengan validasi lengkap
- **Pencarian & Pengelolaan:** Sistem pencarian dan pengelolaan data nasabah yang efisien
- **Validasi Data:** Validasi comprehensive untuk data nasabah dan constraint database

### Produk Perbankan Syariah
- **Tabungan Syariah:** Tabungan Wadiah dan Tabungan Mudharabah dengan sistem bagi hasil
- **Deposito Mudharabah:** Investasi berjangka dengan sistem profit sharing
- **Pembiayaan Syariah:** Murabahah, Mudharabah, Musharakah, Ijarah, Salam, Istisna
- **Konfigurasi Produk:** Pengaturan nisbah, minimal saldo, dan parameter produk

### Manajemen Rekening
- **Pembukaan Rekening:** Proses pembukaan rekening untuk nasabah perorangan dan korporat
- **Status Rekening:** Pengelolaan status rekening (ACTIVE, INACTIVE, CLOSED, FROZEN)
- **Validasi Bisnis:** Business logic validation untuk operasional rekening

### Transaksi Perbankan
- **Setoran Tunai:** Transaksi deposit dengan multiple channel (TELLER, ATM, ONLINE, MOBILE)
- **Penarikan Tunai:** Transaksi withdrawal dengan validasi saldo dan status rekening
- **Transfer Antar Rekening:** Sistem transfer dengan audit trail lengkap
- **Cetak Buku Tabungan:** Pencetakan riwayat transaksi ke buku tabungan
- **Rekening Koran PDF:** Generate dan download mutasi rekening dalam format PDF profesional

### User Management & RBAC
- **Manajemen User:** Pengelolaan user sistem dengan role-based access control
- **Role & Permission:** Sistem permission granular untuk berbagai jenis user
- **User Roles:** Customer Service (CS), Teller, Branch Manager (Kepala Cabang)
- **Security:** Password hashing dengan BCrypt, account locking, failed login tracking

## 🛠️ Technology Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.5.3** - Application framework
- **Spring Data JPA** - Data access layer
- **Spring Security** - Authentication & authorization
- **PostgreSQL 17** - Primary database
- **Flyway** - Database migration management
- **Bean Validation** - Input validation
- **iText 5.5.13.3** - PDF generation library

### Frontend
- **Thymeleaf** - Server-side templating
- **Tailwind CSS 3.4.17** - Utility-first CSS framework
- **PostCSS 8.5.6** - CSS processing
- **Autoprefixer 10.4.21** - CSS vendor prefixes

### Development & Testing
- **Docker Compose** - Development environment
- **JUnit 5** - Unit testing framework
- **Karate 1.4.1** - API integration testing
- **Selenium** - Web UI testing
- **JaCoCo 0.8.12** - Code coverage
- **TestContainers** - Integration testing with containers

### Build & CI/CD
- **Maven 3.8+** - Build tool
- **SonarCloud** - Code quality analysis
- **GitHub Actions** - Continuous integration

Untuk detail lebih lanjut, lihat [dokumentasi teknis](./docs/technical-practices/index.md).

## 📚 Dokumentasi

### Dokumentasi Teknis
- [📋 Panduan Setup Development](./docs/development-setup.md) - Setup environment dan instalasi
- [👥 Panduan Pengguna](./docs/user-guide.md) - Panduan penggunaan aplikasi
- [🧪 Panduan Testing](./docs/testing-guide.md) - Strategi dan panduan testing
- [🏗️ Praktik Teknis](./docs/technical-practices/index.md) - Arsitektur dan best practices
- [🗃️ Dokumentasi Database](./docs/database-documentation.md) - Schema dan design database
- [📈 Status Implementasi Fitur](./docs/feature-implementation-status.md) - Comprehensive feature status dan roadmap
- [☁️ Remote Build Guide](./docs/remote-build-guide.md) - Panduan build dan testing di VPS remote

### Skenario Testing
- [🧩 Skenario Testing](./docs/test-scenarios/README.md) - Comprehensive test scenarios
- [👤 Customer Management](./docs/test-scenarios/customer-management/) - Test nasabah
- [💳 Account Management](./docs/test-scenarios/account-management/) - Test rekening
- [💰 Transaction Testing](./docs/test-scenarios/transactions/) - Test transaksi
- [🔐 RBAC Testing](./docs/test-scenarios/system-management/) - Test user management

## 🚀 Quick Start

### Prerequisites
- **Java 21+** 
- **Maven 3.8+**
- **Docker Desktop**
- **Node.js 16+** (untuk frontend build)

### 1. Setup Database
```bash
# Start PostgreSQL dengan Docker Compose
docker compose up -d
```

### 2. Build Frontend Assets
```bash
# Masuk ke direktori frontend
cd src/main/frontend

# Install dependencies
npm install

# Start watch mode untuk development
npm run watch
```

### 3. Run Application
```bash
# Di terminal terpisah, jalankan Spring Boot application
mvn spring-boot:run
```

### 4. Access Application
- **Web Application:** [http://localhost:8080/product/list](http://localhost:8080/product/list)
- **REST API:** [http://localhost:8080/api/customers](http://localhost:8080/api/customers)
- **Database:** `localhost:2345/pgminibank` (username: `minibank`, password: `minibank1234`)

## 🧪 Testing Status & Coverage

### ✅ Test Implementation Progress
| Test Type | Coverage | Status | Test Classes |
|-----------|----------|--------|-------------|
| **Unit Tests** | ✅ Complete | 🟢 Active | Entity, Repository, Service layer tests |
| **Integration Tests (Karate)** | ✅ Complete | 🟢 Active | API endpoint testing with BDD scenarios |
| **Selenium UI Tests** | ✅ Complete | 🟢 Active | 17 comprehensive UI test classes |
| **Parallel Test Execution** | ✅ Implemented | 🟢 Optimized | Thread-safe Selenium with TestContainers |

### 🎯 Selenium Test Coverage
- **Login & Authentication** - Login flows and RBAC validation
- **Customer Management** - Personal/Corporate customer CRUD operations  
- **Account Management** - Account opening, status management, comprehensive workflows
- **Transaction Processing** - Cash deposits, withdrawals, transfers
- **Islamic Banking Products** - Product management and configuration
- **Passbook Services** - Passbook printing and transaction history
- **User & Permission Management** - RBAC administration, role assignments
- **Dashboard & Navigation** - Main dashboard and navigation flows

### Running Tests

#### Unit Tests
```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AccountRepositoryTest
```

#### Integration Tests (Karate)
```bash
# Run API integration tests
mvn test -Dtest=DepositTest

# Run customer registration tests
mvn test -Dtest=CustomerRegistrationTest
```

#### Selenium UI Tests
```bash
# Run headless (default)
mvn test -Dtest=ProductManagementSeleniumTest

# Run with visible browser (debugging)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false

# Run with recording enabled
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true

# Run parallel tests with optimized containers
mvn test -Dtest=*SeleniumTest
```

## 🏗️ Project Structure

```
aplikasi-minibank/
├── src/main/java/id/ac/tazkia/minibank/
│   ├── AplikasiMinibankApplication.java    # Main application
│   ├── config/                             # Configuration classes
│   ├── controller/                         # Web MVC controllers
│   ├── controller/rest/                    # REST API endpoints
│   ├── dto/                               # Data Transfer Objects
│   ├── entity/                            # JPA entities with business logic
│   ├── repository/                        # Spring Data JPA repositories
│   └── service/                           # Business services
├── src/main/resources/
│   ├── db/migration/                      # Flyway database migrations
│   ├── static/                           # Static web assets
│   └── templates/                        # Thymeleaf templates
├── src/test/
│   ├── java/                             # Test classes
│   └── resources/
│       ├── fixtures/                     # Test data (CSV files)
│       ├── karate/                       # Karate BDD tests
│       └── sql/                          # SQL setup/cleanup scripts
└── docs/                                 # Project documentation
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is part of academic coursework at STEI Tazkia.
