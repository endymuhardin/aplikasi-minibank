# Aplikasi Mini Bank

Aplikasi Mini Bank adalah sistem perbankan syariah berbasis Spring Boot yang menyediakan fungsionalitas lengkap untuk operasional bank syariah, termasuk manajemen nasabah, produk Islamic banking, transaksi, dan sistem RBAC.

## 🚀 Fitur Utama

### Manajemen Nasabah
- **Registrasi Nasabah:** Pendaftaran nasabah perorangan dan korporat dengan validasi lengkap
- **🆕 Approval Workflow:** Sistem persetujuan 2 tingkat - Customer Service membuat, Branch Manager menyetujui
- **Pencarian & Pengelolaan:** Sistem pencarian dan pengelolaan data nasabah yang efisien
- **Validasi Data:** Validasi comprehensive untuk data nasabah dan constraint database
- **Audit Trail:** Riwayat lengkap approval/rejection dengan alasan dan catatan

### Produk Perbankan Syariah
- **Tabungan Syariah:** Tabungan Wadiah dan Tabungan Mudharabah dengan sistem bagi hasil
- **Deposito Mudharabah:** Investasi berjangka dengan sistem profit sharing
- **Pembiayaan Syariah:** Murabahah, Mudharabah, Musharakah, Ijarah, Salam, Istisna
- **Konfigurasi Produk:** Pengaturan nisbah, minimal saldo, dan parameter produk

### Manajemen Rekening
- **Pembukaan Rekening:** Proses pembukaan rekening untuk nasabah perorangan dan korporat
- **🆕 Approval Queue:** Antrian approval untuk nasabah dan rekening baru dengan filter dan pencarian
- **Status Rekening:** Pengelolaan status rekening (ACTIVE, INACTIVE, CLOSED, FROZEN)
- **Dual Status System:** Pemisahan approval_status dan operational status
- **Validasi Bisnis:** Business logic validation untuk operasional rekening

### Transaksi Perbankan
- **Setoran Tunai:** Transaksi deposit dengan multiple channel (TELLER, ATM, ONLINE, MOBILE)
- **Penarikan Tunai:** Transaksi withdrawal dengan validasi saldo dan status rekening
- **Transfer Antar Rekening:** Sistem transfer dengan audit trail lengkap
- **Cetak Buku Tabungan:** Pencetakan riwayat transaksi ke buku tabungan
- **Rekening Koran PDF:** Generate dan download mutasi rekening dalam format PDF profesional
- **Struk Transaksi PDF:** Instant PDF receipt generation untuk semua transaksi dengan format receipt professional

### User Management & RBAC
- **Manajemen User:** Pengelolaan user sistem dengan role-based access control
- **Role & Permission:** Sistem permission granular untuk berbagai jenis user
- **User Roles:** Customer Service (CS), Teller, Branch Manager (Kepala Cabang)
- **Security:** Password hashing dengan BCrypt, account locking, failed login tracking
- **Security Context Integration:** Proper audit trail dengan Spring Security context integration

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
- **TestContainers** - Integration testing with containers
- **Playwright** - Modern web UI automation with cross-browser support
- **Karate 1.4.1** - API integration testing (planned)
- **JaCoCo 0.8.12** - Code coverage analysis

### Build & CI/CD
- **Maven 3.8+** - Build tool
- **SonarCloud** - Code quality analysis
- **GitHub Actions** - Continuous integration

Untuk detail lebih lanjut, lihat [dokumentasi teknis](./docs/technical-practices/index.md).

## 📚 Dokumentasi

### 🆕 Panduan Pengguna (Version 2.0)
- [📚 Panduan Approval Workflow](./docs/user-manual/panduan-approval-workflow.md) - **NEW!** Panduan lengkap workflow approval untuk CS dan Branch Manager
- [📖 Daftar Panduan](./docs/user-manual/README.md) - Index semua panduan pengguna

### Dokumentasi Teknis
- [📋 Panduan Setup Development](./docs/development-setup.md) - Setup environment dan instalasi
- [👥 Panduan Pengguna](./docs/user-guide.md) - Panduan penggunaan aplikasi
- [🧪 Panduan Testing](./docs/TESTING.md) - Strategi dan panduan testing
- [🏗️ Praktik Teknis](./docs/technical-practices/index.md) - Arsitektur dan best practices
- [🗃️ Dokumentasi Database](./docs/database-documentation.md) - Schema dan design database
- [📈 Status Implementasi Fitur](./docs/feature-implementation-status.md) - Comprehensive feature status dan roadmap
- [☁️ Remote Build Guide](./docs/remote-build-guide.md) - Panduan build dan testing di VPS remote
- [🌐 GitHub Pages Setup](./docs/GITHUB_PAGES_SETUP.md) - Setup dokumentasi otomatis dengan GitHub Pages

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

**Note:** Node.js and npm are **automatically installed** by Maven during build (via frontend-maven-plugin)

### 1. Setup Database
```bash
# Start PostgreSQL dengan Docker Compose
docker compose up -d
```

### 2. Build & Run Application
```bash
# Maven will automatically:
# 1. Install Node.js and npm (if not present)
# 2. Run npm install for frontend dependencies
# 3. Build Tailwind CSS (compile to output.css)
# 4. Start Spring Boot application
mvn spring-boot:run
```

**That's it!** No need to run separate `npm` commands. Maven handles everything.

### 4. Access Application
- **Web Application:** [http://localhost:8080/product/list](http://localhost:8080/product/list)
- **REST API:** [http://localhost:8080/api/customers](http://localhost:8080/api/customers)
- **Database:** `localhost:2345/pgminibank` (username: `minibank`, password: `minibank1234`)

### 5. Sample Users
Login dengan kredensial berikut (password semua: `minibank123`):
- **Admin:** `admin` - Full system access
- **Customer Service:** `cs1`, `cs2`, `cs3` - Create customers & accounts
- **Branch Manager:** `manager1`, `manager2` - Approve/reject requests
- **Teller:** `teller1`, `teller2`, `teller3` - Process transactions

## 🎓 Practice Tutorial Setup

### Deploying Fresh Application
Untuk deployment aplikasi dari awal:

```bash
# 1. Stop dan hapus database lama (HATI-HATI: ini akan menghapus semua data!)
docker compose down -v

# 2. Start database baru
docker compose up -d

# 3. Jalankan aplikasi (Flyway akan membuat schema dan seed data)
mvn spring-boot:run

# 4. Access aplikasi di http://localhost:8080
```

### Reset Database untuk Latihan Tutorial

Jika Anda ingin **berlatih tutorial dari awal** tanpa menghapus konfigurasi sistem (users, products, branches), gunakan script reset:

```bash
# Reset customer data sambil mempertahankan users, products, dan branches
docker exec -i aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank < scripts/reset-customer-data.sql
```

**Apa yang dihapus:**
- ❌ Semua transaksi
- ❌ Semua rekening
- ❌ Semua nasabah (personal & corporate)
- ❌ Semua approval requests
- 🔄 Reset sequence numbers (Customer, Account, Transaction)

**Apa yang dipertahankan:**
- ✅ Users & authentication (admin, cs1-3, manager1-2, teller1-3)
- ✅ Products (Islamic banking products)
- ✅ Branches (branch information)
- ✅ Roles & permissions (RBAC configuration)

**Kapan menggunakan reset script:**
- 🎯 Ingin berlatih tutorial approval workflow dari awal
- 📚 Training untuk user baru tanpa menghapus konfigurasi sistem
- 🧪 Testing ulang dengan data bersih
- 🔄 Reset demo environment

**Catatan:** Setelah reset, nomor nasabah berikutnya dimulai dari `C1000011` dan nomor rekening dari `A2000007`.

## 🧪 Testing Status & Coverage

### ✅ Test Implementation Progress  
| Test Type | Coverage | Status | Test Classes |
|-----------|----------|--------|-------------|
| **Unit Tests** | 🚧 Planned | 🟡 Pending | Entity, Repository, Service layer tests |
| **Integration Tests** | 🚧 Planned | 🟡 Pending | Database integration and business logic tests |
| **Playwright Functional Tests** | ✅ P0 Complete | 🟢 Active | 3 critical success scenario test classes |
| **API Integration Tests (Karate)** | 🚧 Planned | 🟡 Pending | API endpoint testing with BDD scenarios |

### 🎯 Playwright Test Coverage (Phase 1 Complete - Enhanced)
#### ✅ **P0 Critical Success Scenarios - IMPLEMENTED & ENHANCED**
- **CustomerManagementSuccessTest** - 13 test methods covering customer CRUD operations, search functionality, navigation workflows, and comprehensive form testing with separated CSV fixtures
- **AccountOpeningSuccessTest** - 10 test methods for account opening workflows and Islamic banking compliance
- **TransactionSuccessTest** - 11 test methods for transaction processing, multi-channel support, and balance validation

**Total**: 34 test methods across 3 test classes with improved reliability, enhanced element locators, and proper setup/cleanup

#### 🚧 **Planned Implementation (Phase 2+)**
- **Account Opening Workflows** - Complete account creation flows for Islamic banking products
- **Transaction Processing** - End-to-end cash deposits, withdrawals, transfers  
- **RBAC Management** - User management, role assignments, permissions
- **Customer Management** - Complete CRUD operations for personal/corporate customers
- **Islamic Banking Products** - Product management and nisbah configuration
- **PDF Generation** - Statement and receipt generation workflows

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

#### Playwright Functional Tests
```bash
# Run all Playwright functional tests
mvn test -Dtest=**/functional/**/*Test

# Run specific success scenario tests  
mvn test -Dtest=CustomerManagementSuccessTest
mvn test -Dtest=AccountOpeningSuccessTest
mvn test -Dtest=TransactionSuccessTest

# Run with visible browser (debugging)
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false

# Run with video recording enabled
mvn test -Dtest=**/functional/**/*Test -Dplaywright.record=true

# Run with slow motion for better observation
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.slowmo=500

# Ultimate debugging: visible + slow + recorded
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.slowmo=1000 -Dplaywright.record=true

# Cross-browser testing
mvn test -Dtest=**/functional/**/*Test -Dplaywright.browser=firefox
mvn test -Dtest=**/functional/**/*Test -Dplaywright.browser=webkit
```

#### API Integration Tests (Planned)
```bash
# Run API integration tests
mvn test -Dtest=DepositTest

# Run customer registration tests  
mvn test -Dtest=CustomerRegistrationTest
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
    ├── user-manual/                        # Auto-generated user manuals
    ├── TESTING.md                         # Testing guide
    └── GITHUB_PAGES_SETUP.md              # GitHub Pages configuration
```

## 📚 User Documentation Generation

### Approval Workflow Documentation (V2.0)

This project includes automated Indonesian user manual generation using Playwright:

```bash
# Generate approval workflow documentation with screenshots
mvn test -Dtest=ApprovalWorkflowTutorialTest \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=2000 \
  -Dplaywright.record=true

# Generate user manual from captured media
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.ApprovalWorkflowDocGenerator"

# View generated documentation
open docs/user-manual/panduan-approval-workflow.md
```

### GitHub Pages Deployment

Documentation is automatically deployed to GitHub Pages on every push to main:

- **Live Documentation**: `https://<username>.github.io/<repository>/`
- **Setup Guide**: See `docs/GITHUB_PAGES_SETUP.md`
- **Features**:
  - ✅ Auto-generated screenshots from Playwright tests (34 screenshots for approval workflow)
  - ✅ Step-by-step video tutorials with descriptive filenames
  - ✅ Professional Indonesian banking documentation with proper terminology
  - ✅ Mobile-responsive web interface with Bootstrap styling
  - 🆕 **NEW**: Complete approval workflow documentation (CS + Branch Manager)

### Workflow Overview (Version 2.0)

1. **Push to Main** → Triggers GitHub Actions
2. **Run Tests** → Execute `ApprovalWorkflowTutorialTest` (Playwright)
3. **Capture Media** → 34 screenshots + videos covering complete CS and Branch Manager workflow
4. **Generate Manual** → Indonesian user manual via `ApprovalWorkflowDocGenerator`
5. **Deploy Pages** → Publish to GitHub Pages for public access

**What's NEW in V2.0:**
- 📋 Complete approval workflow documentation (11 detailed steps)
- 🎯 Separate guides for Customer Service and Branch Manager roles
- 📊 Approval status reference tables and workflow diagrams
- 💡 Tips, troubleshooting, and best practices
- 🔍 Detailed screenshots for every step of the approval process

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is part of academic coursework at STEI Tazkia.
