# Aplikasi Mini Bank #

## Fitur Aplikasi ##

Aplikasi Mini Bank mencakup modul-modul utama dengan status implementasi sebagai berikut:

### ✅ **MODUL YANG SUDAH SELESAI** ###

* **✅ Modul Manajemen Nasabah:**
    * ✅ **Registrasi Nasabah Perorangan:** Input data lengkap, validasi KTP, verifikasi data
    * ✅ **Registrasi Nasabah Badan Usaha:** Input data perusahaan, validasi dokumen, verifikasi legalitas
    * ✅ **Pencarian & Filtering Nasabah:** Pencarian berdasarkan nama, nomor nasabah, jenis nasabah
    * ✅ **Edit & Update Data Nasabah:** Perubahan data nasabah dengan audit trail
    * ✅ **Aktivasi/Deaktivasi Nasabah:** Pengelolaan status nasabah
    * ✅ **View Detail Nasabah:** Tampilan lengkap informasi nasabah
    * ✅ **REST API Lengkap:** Endpoint untuk semua operasi nasabah
    * ✅ **Testing Komprehensif:** Unit test, integration test, Selenium test, Karate BDD test

* **✅ Modul Manajemen Produk Perbankan:**
    * ✅ **Konfigurasi Produk Tabungan Syariah:** Tabungan Wadiah, Tabungan Mudharabah
    * ✅ **Konfigurasi Produk Deposito:** Deposito Mudharabah dengan nisbah bagi hasil
    * ✅ **Konfigurasi Produk Pembiayaan:** Murabahah, Mudharabah, Musharakah, Ijarah, Salam, Istisna
    * ✅ **Pengaturan Fee & Limit:** Konfigurasi biaya administrasi, limit transaksi
    * ✅ **Pengaturan Nisbah Bagi Hasil:** Sistem profit sharing untuk produk syariah
    * ✅ **CRUD Produk Lengkap:** Create, Read, Update, Delete, Search, Filter
    * ✅ **Aktivasi/Deaktivasi Produk:** Pengelolaan status produk
    * ✅ **REST API Lengkap:** Endpoint untuk semua operasi produk
    * ✅ **Testing Komprehensif:** Unit test, integration test, Selenium test

* **✅ Modul Pembukaan Rekening:**
    * ✅ **REST API Pembukaan Rekening:** Endpoint untuk membuka rekening baru
    * ✅ **Validasi Nasabah & Produk:** Verifikasi kelayakan pembukaan rekening
    * ✅ **Otomatisasi Nomor Rekening:** Generate nomor rekening otomatis dengan prefix
    * ✅ **Pengaturan Saldo Awal:** Set saldo minimum sesuai produk
    * ✅ **Karate BDD Testing:** Test cases lengkap untuk pembukaan rekening
    * ❓ *Web Interface:** Belum ada UI web untuk pembukaan rekening*

* **✅ Modul Transaksi Perbankan:**
    * ✅ **Setoran Tunai (Deposit):**
        * ✅ REST API untuk deposit dengan validasi lengkap
        * ✅ Update saldo otomatis dengan audit trail
        * ✅ Pencatatan riwayat transaksi lengkap
        * ✅ Validasi limit dan aturan bisnis
        * ✅ Karate BDD testing dengan berbagai skenario
    * ✅ **Penarikan Tunai (Withdrawal):**
        * ✅ REST API untuk withdrawal dengan validasi saldo
        * ✅ Pengecekan limit penarikan harian
        * ✅ Update saldo otomatis dengan validasi
        * ✅ Pencatatan audit trail transaksi
        * ✅ Karate BDD testing komprehensif
    * ❓ *Web Interface:** Belum ada UI web untuk transaksi*
    * ❓ *Transfer:** Belum diimplementasi*

* **✅ Modul User Management & RBAC:**
    * ✅ **Manajemen User:** Create, edit, view, activate/deactivate user
    * ✅ **Manajemen Role:** Customer Service, Teller, Branch Manager dengan permissions
    * ✅ **Manajemen Permission:** Fine-grained permissions per fitur
    * ✅ **Assignment Role ke User:** Assign/remove multiple roles per user
    * ✅ **Assignment Permission ke Role:** Configure permissions per role
    * ✅ **Password Management:** Change password dengan BCrypt hashing
    * ✅ **Authentication & Security:** Login/logout, session management
    * ✅ **Web Interface Lengkap:** Form, list, search, pagination
    * ✅ **REST API Lengkap:** Endpoint untuk semua operasi RBAC
    * ✅ **Testing Komprehensif:** Unit test, integration test, Selenium test

### ❌ **MODUL YANG BELUM DIIMPLEMENTASI** ###

* **❌ Modul Cetak Buku Tabungan:**
    * ❌ Pencarian rekening nasabah
    * ❌ Tampilan riwayat transaksi
    * ❌ Fungsionalitas pencetakan entri transaksi ke buku tabungan fisik
    * ❌ Sinkronisasi dengan data transaksi terbaru

* **❌ Modul Pembiayaan Syariah (Web Interface):**
    * ❌ **Pembiayaan Murabahah:**
        * ❌ Input data nasabah dan objek pembiayaan (barang/jasa)
        * ❌ Perhitungan harga pokok, margin keuntungan, dan harga jual
        * ❌ Penentuan jangka waktu dan angsuran
        * ❌ Pencatatan akad pembiayaan
        * ❌ Pencetakan dokumen pembiayaan murabahah
    * ❌ **Pembiayaan Mudharabah:**
        * ❌ Input data nasabah dan tujuan pembiayaan (proyek/usaha)
        * ❌ Penentuan nisbah bagi hasil (proporsi pembagian keuntungan)
        * ❌ Penentuan jangka waktu pembiayaan
        * ❌ Pencatatan akad pembiayaan
        * ❌ Pencetakan dokumen pembiayaan mudharabah

* **❌ Modul Deposito (Web Interface):**
    * ❌ Input data nasabah untuk deposito
    * ❌ Pilihan jangka waktu deposito
    * ❌ Penentuan nisbah bagi hasil deposito
    * ❌ Pencetakan bilyet deposito

### 📊 **PROGRESS SUMMARY** ###
- **✅ Completed Modules:** 5/8 (Customer Management, Product Management, Account Opening API, Transaction API, RBAC)
- **🔄 Partially Implemented:** 0/8 
- **❌ Not Started:** 3/8 (Passbook Printing, Islamic Financing UI, Deposito UI)
- **Overall Progress:** ~62% Complete

## Pengguna Aplikasi ##

Aplikasi ini akan dirancang untuk melayani kebutuhan berbagai peran pengguna dengan hak akses dan fungsionalitas yang disesuaikan:

* **Customer Service (CS):**
    * Memiliki akses penuh ke fitur pembukaan rekening tabungan dan deposito.
    * Dapat melihat riwayat transaksi nasabah.
    * Dapat menginput pengajuan pembiayaan murabahah dan mudharabah (tanpa persetujuan akhir).
    * Tidak memiliki akses untuk melakukan transaksi tunai secara langsung.
* **Teller:**
    * Memiliki akses penuh ke fitur setoran tunai.
    * Memiliki akses ke fitur cetak buku tabungan.
    * Dapat melihat data nasabah dan riwayat transaksi terkait dengan tugasnya.
    * Tidak memiliki akses ke fitur pembukaan rekening atau pembiayaan.
* **Kepala Cabang:**
    * Memiliki akses administratif penuh ke seluruh fitur aplikasi.
    * Dapat memonitor seluruh transaksi dan aktivitas pengguna.
    * Dapat memberikan persetujuan akhir untuk pengajuan pembiayaan murabahah dan mudharabah.
    * Dapat mengelola data pengguna dan hak akses.
    * Dapat menghasilkan laporan terkait operasional bank.

## Technology Stack ##

* Java 21
* Spring Boot 3.5.3
* PostgreSQL 17
* Docker Compose
* Node.js (untuk frontend build)
* Tailwind CSS

## Setup Development Environment ##

### Prerequisites untuk Semua Operating System ###

- **Java 21** (menggunakan SDKMAN)
- **Maven 3.6+** (atau gunakan Maven wrapper `./mvnw`)
- **Node.js 18+** (menggunakan NVM)
- **Docker Desktop** (untuk database dan Selenium tests)
- **Git** (untuk version control)

### Windows Setup ###

#### 1. Install Git Bash ####
```powershell
# Download dan install Git for Windows dari https://git-scm.com/download/win
# Pastikan pilih "Git Bash" saat instalasi
```

#### 2. Install SDKMAN (melalui Git Bash) ####
```bash
# Buka Git Bash dan jalankan:
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

#### 3. Install Java 21 menggunakan SDKMAN ####
```bash
# List available Java versions
sdk list java

# Install Java 21 (Temurin/Eclipse Adoptium)
sdk install java 21.0.5-tem

# Set as default
sdk default java 21.0.5-tem

# Verify installation
java -version
```

#### 4. Install Maven menggunakan SDKMAN ####
```bash
sdk install maven
```

#### 5. Install NVM ####
```bash
# Download nvm-windows dari https://github.com/coreybutler/nvm-windows
# Atau install via chocolatey:
choco install nvm
```

#### 6. Install Node.js menggunakan NVM ####
```bash
# Install Node.js 18 LTS
nvm install 18.19.0
nvm use 18.19.0

# Verify installation
node --version
npm --version
```

#### 7. Install Docker Desktop ####
```powershell
# Download dari https://docs.docker.com/desktop/install/windows-install/
# Atau menggunakan winget
winget install Docker.DockerDesktop
```

### Ubuntu/Debian Setup ###

#### 1. Update sistem dan install dependencies ####
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install curl wget gnupg2 software-properties-common apt-transport-https ca-certificates git -y
```

#### 2. Install SDKMAN ####
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

#### 3. Install Java 21 menggunakan SDKMAN ####
```bash
# List available Java versions
sdk list java

# Install Java 21 (Temurin/Eclipse Adoptium)
sdk install java 21.0.5-tem

# Set as default
sdk default java 21.0.5-tem

# Verify installation
java -version
javac -version
```

#### 4. Install Maven menggunakan SDKMAN ####
```bash
sdk install maven

# Verify installation
mvn -version
```

#### 5. Install NVM ####
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc

# Verify installation
nvm --version
```

#### 6. Install Node.js menggunakan NVM ####
```bash
# Install Node.js 18 LTS
nvm install 18.19.0
nvm use 18.19.0
nvm alias default 18.19.0

# Verify installation
node --version
npm --version
```

#### 7. Install Docker Desktop ####
```bash
# Install Docker Engine
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Desktop (optional GUI)
wget https://desktop.docker.com/linux/main/amd64/docker-desktop-4.25.0-amd64.deb
sudo dpkg -i docker-desktop-4.25.0-amd64.deb
```

### macOS Setup ###

#### 1. Install Homebrew (jika belum ada) ####
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### 2. Install SDKMAN ####
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

#### 3. Install Java 21 menggunakan SDKMAN ####
```bash
# List available Java versions
sdk list java

# Install Java 21 (Temurin/Eclipse Adoptium)
sdk install java 21.0.5-tem

# Set as default
sdk default java 21.0.5-tem

# Verify installation
java -version
```

#### 4. Install Maven menggunakan SDKMAN ####
```bash
sdk install maven

# Verify installation
mvn -version
```

#### 5. Install NVM ####
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.zshrc

# Verify installation
nvm --version
```

#### 6. Install Node.js menggunakan NVM ####
```bash
# Install Node.js 18 LTS
nvm install 18.19.0
nvm use 18.19.0
nvm alias default 18.19.0

# Verify installation
node --version
npm --version
```

#### 7. Install Docker Desktop ####
```bash
brew install --cask docker

# Atau download manual dari https://docs.docker.com/desktop/install/mac-install/
```

#### 8. Install Git (jika belum ada) ####
```bash
brew install git
```

### Verification dan First Setup ###

#### 1. Clone repository ####
```bash
git clone <repository-url>
cd aplikasi-minibank
```

#### 2. Verify semua tools terinstall ####
```bash
# Check Java
java -version
# Expected: openjdk version "21.0.5"

# Check Maven
mvn -version
# Expected: Apache Maven 3.x.x

# Check Node.js
node --version
# Expected: v18.19.0

# Check npm
npm --version

# Check Docker
docker --version
docker-compose --version

# Check SDKMAN
sdk version

# Check NVM
nvm --version
```

#### 3. Setup IDE (Opsional) ####

**Visual Studio Code:**
- Install extension: "Extension Pack for Java"
- Install extension: "Spring Boot Extension Pack"
- Install extension: "Tailwind CSS IntelliSense"

**IntelliJ IDEA:**
- Install Spring Boot plugin
- Import project sebagai Maven project
- Enable annotation processing untuk Lombok

#### 4. First Run Test ####
```bash
# Test Maven build
./mvnw clean compile

# Test frontend build
cd src/main/frontend
npm install
cd ../../..

# Test Docker
docker --version
docker-compose --version
```

### Version Management dengan SDKMAN dan NVM ###

#### SDKMAN Commands ####
```bash
# List installed Java versions
sdk list java

# Switch Java version
sdk use java 21.0.5-tem

# Install additional Java version
sdk install java 17.0.9-tem

# Set default Java version
sdk default java 21.0.5-tem

# Update SDKMAN
sdk update
```

#### NVM Commands ####
```bash
# List installed Node versions
nvm list

# Switch Node version
nvm use 18.19.0

# Install additional Node version
nvm install 20.10.0

# Set default Node version
nvm alias default 18.19.0

# Update NVM
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
```

### Troubleshooting Common Issues ###

#### Windows Issues ####
- **SDKMAN tidak dikenali**: Pastikan menjalankan di Git Bash, bukan Command Prompt
- **Docker permission**: Jalankan Docker Desktop sebagai administrator
- **Port conflicts**: Pastikan port 8080 dan 2345 tidak digunakan aplikasi lain

#### Ubuntu Issues ####
- **Permission denied (Docker)**: Pastikan user sudah di group docker: `sudo usermod -aG docker $USER`
- **SDKMAN not found**: Restart terminal atau jalankan `source ~/.bashrc`
- **NVM command not found**: Restart terminal atau jalankan `source ~/.bashrc`

#### macOS Issues ####
- **SDKMAN not found**: Restart terminal atau jalankan `source ~/.zshrc`
- **NVM command not found**: Restart terminal atau jalankan `source ~/.zshrc`
- **Docker memory**: Tingkatkan Docker Desktop memory allocation di preferences

#### General SDKMAN/NVM Issues ####
- **Version conflicts**: Gunakan `sdk current` dan `nvm current` untuk check active versions
- **Path issues**: SDKMAN dan NVM otomatis manage PATH, hindari manual PATH modification
- **Shell conflicts**: Pastikan menggunakan shell yang benar (bash/zsh)

## Menjalankan Aplikasi ##

1. Jalankan docker desktop

2.  **Menjalankan Tailwind CSS build process:**
    Buka terminal dalam folder `src/main/frontend` dan jalankan perintah berikut :

    ```bash
    npm install
    npm run watch
    ```

3.  **Menjalankan aplikasi Spring Boot:**
    Buka terminal kedua di folder project dan jalankan perintah berikut:

    ```bash
    mvn spring-boot:run
    ```

4. **Browse aplikasi:**
   Buka browser ke alamat [http://localhost:8080/product/list](http://localhost:8080/product/list)

## Menggunakan Aplikasi ##

### Login Credentials ###

Aplikasi memiliki sample users untuk setiap role dengan credentials berikut:

#### Branch Manager (Kepala Cabang) ####
| Username | Password | Email | Full Name |
|----------|----------|-------|-----------|
| `admin` | `minibank123` | admin@yopmail.com | System Administrator |
| `manager1` | `minibank123` | manager1@yopmail.com | Branch Manager Jakarta |
| `manager2` | `minibank123` | manager2@yopmail.com | Branch Manager Surabaya |

**Hak Akses:** Akses penuh ke seluruh fitur aplikasi, monitoring, approval, user management, dan laporan.

#### Teller ####
| Username | Password | Email | Full Name |
|----------|----------|-------|-----------|
| `teller1` | `minibank123` | teller1@yopmail.com | Teller Counter 1 |
| `teller2` | `minibank123` | teller2@yopmail.com | Teller Counter 2 |
| `teller3` | `minibank123` | teller3@yopmail.com | Teller Counter 3 |

**Hak Akses:** Transaksi tunai (setoran/tarik tunai/transfer), cetak buku tabungan, lihat data nasabah dan saldo.

#### Customer Service ####
| Username | Password | Email | Full Name |
|----------|----------|-------|-----------|
| `cs1` | `minibank123` | cs1@yopmail.com | Customer Service Staff 1 |
| `cs2` | `minibank123` | cs2@yopmail.com | Customer Service Staff 2 |
| `cs3` | `minibank123` | cs3@yopmail.com | Customer Service Staff 3 |

**Hak Akses:** Pembukaan rekening tabungan/deposito, input data nasabah, pengajuan pembiayaan (tanpa approval).

### Akses URL ###

- **Web UI**: [http://localhost:8080/product/list](http://localhost:8080/product/list)
- **REST API**: [http://localhost:8080/api/](http://localhost:8080/api/)
  - Customers: `/api/customers`
  - Accounts: `/api/accounts`
  - Transactions: `/api/transactions`
  - Users: `/api/users`

### Sample Data Nasabah ###

Aplikasi telah memiliki data sample nasabah yang dapat digunakan untuk testing:

**Nasabah Perorangan:**
| Customer Number | Name | Email | KTP | Phone | Address |
|-----------------|------|-------|-----|-------|---------|
| C1000001 | Ahmad Suharto | ahmad.suharto@email.com | 3271081503850001 | 081234567890 | Jl. Sudirman No. 123, Jakarta 10220 |
| C1000002 | Siti Nurhaliza | siti.nurhaliza@email.com | 3271082207900002 | 081234567891 | Jl. Thamrin No. 456, Jakarta 10230 |
| C1000004 | Budi Santoso | budi.santoso@email.com | 3271081011880003 | 081234567892 | Jl. Gatot Subroto No. 321, Jakarta 12930 |
| C1000006 | Dewi Lestari | dewi.lestari@email.com | 3271081805920004 | 081234567893 | Jl. MH Thamrin No. 654, Jakarta 10350 |

**Nasabah Korporasi:**
| Customer Number | Company Name | Email | Registration Number | Tax ID | Phone | Address |
|-----------------|--------------|-------|-------------------|--------|-------|---------|
| C1000003 | PT. Teknologi Maju | info@teknologimaju.com | 1234567890123456 | 01.234.567.8-901.000 | 02123456789 | Jl. HR Rasuna Said No. 789, Jakarta 12950 |

### Produk Perbankan Tersedia ###

| Product Code | Product Name | Type | Category | Min Opening | Min Balance | Nisbah Customer | Nisbah Bank | Customer Types | Features |
|--------------|--------------|------|----------|-------------|-------------|-----------------|-------------|----------------|----------|
| **TAB001** | Tabungan Wadiah Basic | TABUNGAN_WADIAH | Tabungan Syariah | Rp 50.000 | Rp 10.000 | - | - | PERSONAL | 10 transaksi gratis/bulan, biaya admin Rp 2.500 |
| **TAB002** | Tabungan Mudharabah Premium | TABUNGAN_MUDHARABAH | Tabungan Syariah | Rp 1.000.000 | Rp 500.000 | 70% | 30% | PERSONAL | 25 transaksi gratis/bulan, tanpa biaya admin |
| **DEP001** | Deposito Mudharabah | DEPOSITO_MUDHARABAH | Deposito Syariah | Rp 100.000 | Rp 50.000 | 70% | 30% | PERSONAL | Bagi hasil on maturity, 20 transaksi gratis/bulan |
| **PEM001** | Pembiayaan Murabahah | PEMBIAYAAN_MURABAHAH | Pembiayaan Syariah | Rp 5.000.000 | Rp 1.000.000 | - | - | CORPORATE | Untuk kebutuhan konsumtif, akad murabahah |
| **PEM002** | Pembiayaan Musharakah | PEMBIAYAAN_MUSHARAKAH | Pembiayaan Syariah | Rp 2.000.000 | Rp 1.000.000 | 60% | 40% | PERSONAL | Untuk modal usaha, akad musharakah |

**Initial Sequence Numbers:**
- Customer Number: Starting from C1000001
- Account Number: Starting from A2000001  
- Transaction Number: Starting from T3000001

### Cara Menggunakan Fitur ###

#### 1. Customer Service - Pembukaan Rekening ####
- Login dengan user CS (cs1/cs2/cs3)
- Akses menu pembukaan rekening tabungan/deposito
- Input data nasabah atau pilih dari nasabah existing
- Pilih jenis produk banking
- Generate nomor rekening otomatis
- Cetak formulir pembukaan rekening

#### 2. Teller - Transaksi Tunai ####
- Login dengan user Teller (teller1/teller2/teller3)
- Akses menu setoran tunai
- Input nomor rekening tujuan
- Input nominal setoran
- Sistem otomatis update saldo
- Cetak bukti setoran
- Akses menu cetak buku tabungan untuk pencetakan transaksi

#### 3. Branch Manager - Monitoring dan Approval ####
- Login dengan user Branch Manager (admin/manager1/manager2)
- Akses semua fitur aplikasi
- Monitor seluruh transaksi dan aktivitas
- Approve pengajuan pembiayaan murabahah/mudharabah
- Kelola data pengguna dan hak akses
- Generate laporan operasional

#### 4. REST API Usage ####
```bash
# Contoh penggunaan API dengan curl

# Get daftar nasabah
curl http://localhost:8080/api/customers

# Get detail nasabah
curl http://localhost:8080/api/customers/{customerId}

# Create nasabah baru
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"customerType":"PERSONAL","email":"test@email.com","phoneNumber":"081234567890"}'

# Get daftar rekening
curl http://localhost:8080/api/accounts

# Proses setoran
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{"accountId":"account-id","amount":100000,"description":"Setoran tunai"}'
```

## Testing ##

### Unit dan Integration Tests ###

```bash
# Run semua test
mvn test

# Run test dengan coverage report
mvn test jacoco:report

# Run test class tertentu
mvn test -Dtest=AccountRepositoryTest

# Run test method tertentu
mvn test -Dtest=AccountRepositoryTest#shouldFindByCustomerId

# Run Karate feature tests
mvn test -Dtest=DepositTest
```

### Selenium Tests ###

Aplikasi menggunakan Selenium dengan TestContainers untuk automated UI testing. Selenium tests berjalan dalam Docker container yang terisolasi dengan recording capability untuk monitoring dan debugging.

#### Menjalankan Selenium Tests ####

```bash
# Mode default (headless mode, tanpa recording - fastest)
mvn test -Dtest=ProductManagementSeleniumTest

# Mode dengan browser window visible (untuk debugging)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false

# Mode dengan video recording untuk monitoring/debugging
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true

# Run semua Selenium tests
mvn test -Dtest="*Selenium*"

# Run dengan browser Chrome (default: Chrome)
mvn test -Dtest=LoginSeleniumTest -Dselenium.browser=chrome

# Run dengan browser Firefox
mvn test -Dtest=LoginSeleniumTest -Dselenium.browser=firefox

# Kombinasi opsi untuk debugging maksimal
mvn test -Dtest=LoginSeleniumTest -Dselenium.headless=false -Dselenium.recording.enabled=true -Dselenium.browser=chrome
```

#### Monitoring Selenium Tests ####

**VNC Viewer untuk Live Monitoring:**
- Saat test berjalan, check log untuk VNC URL: `VNC URL : http://localhost:[port]`
- Buka URL tersebut di browser untuk melihat browser automation secara real-time
- Berguna untuk debugging test failures dan memantau test execution
- **Note**: VNC viewer tersedia baik dalam headless maupun non-headless mode

**Video Recordings:**
- Lokasi: `target/selenium-recordings/`
- Format: MP4
- Hanya tersedia jika `-Dselenium.recording.enabled=true`
- Recording dimulai otomatis saat test start dan berhenti saat test selesai

**Log Monitoring:**
- Selenium container logs menampilkan browser startup, WebDriver creation, dan test execution
- Test method logs menampilkan page interactions dan assertions

#### Available Selenium Tests ####

- **LoginSeleniumTest**: Test login functionality dengan berbagai user roles
- **ProductManagementSeleniumTest**: Test CRUD operations untuk banking products
- **RbacManagementSeleniumTest**: Test role-based access control dan user management

#### Selenium Test Configuration ####

**Runtime Options:**
- **Browser**: Chrome (default), Firefox (dengan `-Dselenium.browser=firefox`)
- **Headless Mode**: Enabled by default (fastest), disable dengan `-Dselenium.headless=false` untuk debugging
- **Recording**: Disabled by default, enable dengan `-Dselenium.recording.enabled=true`
- **TestContainers**: Otomatis start/stop Selenium Grid container

**Test Architecture:**
- **Page Objects**: Menggunakan Page Object Pattern untuk maintainability
- **LoginHelper**: Centralized authentication utilities untuk semua user roles

#### Troubleshooting ####

- **Test Timeout**: Selenium tests memiliki implicit wait 5 detik dan retry logic
- **Recording Issues**: Pastikan directory `target/selenium-recordings/` dapat ditulis
- **VNC Connection**: Gunakan VNC URL dari log untuk live monitoring test execution
- **Browser Issues**: Switch browser dengan `-Dselenium.browser=chrome` jika Firefox bermasalah