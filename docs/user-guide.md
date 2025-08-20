## Pengguna Aplikasi ##

## Pengguna Aplikasi ##

Aplikasi ini dirancang untuk melayani kebutuhan berbagai peran pengguna dengan hak akses dan fungsionalitas yang disesuaikan per cabang:

* **Customer Service (CS):**
    * Memiliki akses penuh ke fitur pembukaan rekening tabungan dan deposito.
    * Dapat melihat riwayat transaksi nasabah dalam cabang yang sama.
    * Dapat menginput pengajuan pembiayaan murabahah dan mudharabah (tanpa persetujuan akhir).
    * Dapat mengelola data nasabah yang terdaftar di cabang yang sama.
    * Tidak memiliki akses untuk melakukan transaksi tunai secara langsung.
* **Teller:**
    * Memiliki akses penuh ke fitur setoran tunai dan penarikan tunai.
    * Memiliki akses ke fitur cetak buku tabungan.
    * Dapat melihat data nasabah dan riwayat transaksi yang terkait dengan cabang tugasnya.
    * Dapat memproses transaksi untuk rekening dalam cabang yang sama.
    * Tidak memiliki akses ke fitur pembukaan rekening atau pembiayaan.
* **Kepala Cabang (Branch Manager):**
    * Memiliki akses administratif penuh ke seluruh fitur aplikasi dalam cabang yang dikelola.
    * Dapat memonitor seluruh transaksi dan aktivitas pengguna dalam cabang.
    * Dapat memberikan persetujuan akhir untuk pengajuan pembiayaan murabahah dan mudharabah.
    * Dapat mengelola data pengguna dan hak akses untuk staff cabang.
    * Dapat menghasilkan laporan terkait operasional cabang.
    * **Admin (System Administrator)** memiliki akses lintas cabang untuk keperluan sistem.

## Menggunakan Aplikasi ##

### Login Credentials ###

Aplikasi memiliki sample users untuk setiap role dengan credentials berikut:

#### Branch Manager (Kepala Cabang) ####
| Username | Password | Email | Full Name | Branch Assignment |
|----------|----------|-------|-----------|------------------|
| `admin` | `minibank123` | admin@yopmail.com | System Administrator | Main Branch (Kantor Pusat) |
| `manager1` | `minibank123` | manager1@yopmail.com | Branch Manager Jakarta | Main Branch (Kantor Pusat) |
| `manager2` | `minibank123` | manager2@yopmail.com | Branch Manager Surabaya | Main Branch (Kantor Pusat) |

**Hak Akses:** Akses penuh ke seluruh fitur aplikasi dalam cabang yang dikelola, monitoring, approval, user management, dan laporan cabang.

#### Teller ####
| Username | Password | Email | Full Name | Branch Assignment |
|----------|----------|-------|-----------|------------------|
| `teller1` | `minibank123` | teller1@yopmail.com | Teller Counter 1 | Main Branch (Kantor Pusat) |
| `teller2` | `minibank123` | teller2@yopmail.com | Teller Counter 2 | Main Branch (Kantor Pusat) |
| `teller3` | `minibank123` | teller3@yopmail.com | Teller Counter 3 | Main Branch (Kantor Pusat) |

**Hak Akses:** Transaksi tunai (setoran/tarik tunai/transfer), cetak buku tabungan, lihat data nasabah dan saldo dalam cabang yang sama.

#### Customer Service ####
| Username | Password | Email | Full Name | Branch Assignment |
|----------|----------|-------|-----------|------------------|
| `cs1` | `minibank123` | cs1@yopmail.com | Customer Service Staff 1 | Main Branch (Kantor Pusat) |
| `cs2` | `minibank123` | cs2@yopmail.com | Customer Service Staff 2 | Main Branch (Kantor Pusat) |
| `cs3` | `minibank123` | cs3@yopmail.com | Customer Service Staff 3 | Main Branch (Kantor Pusat) |

**Hak Akses:** Pembukaan rekening tabungan/deposito, input data nasabah, pengajuan pembiayaan (tanpa approval) dalam cabang yang sama.

### Sistem Multi-Branch ###

Aplikasi mendukung sistem multi-branch untuk mengorganisir operasional bank berdasarkan cabang:

#### Informasi Branch Tersedia ####
| Branch Code | Branch Name | Address | City | Status |
|-------------|-------------|---------|------|--------|
| **MAIN** | Main Branch (Kantor Pusat) | Jl. Gatot Subroto No. 1, Jakarta 12190 | Jakarta | ACTIVE |

#### Fitur Multi-Branch ####
* **Branch Assignment untuk Users:** Setiap user ditetapkan ke cabang tertentu
* **Branch Assignment untuk Customers:** Setiap nasabah terdaftar di cabang tertentu  
* **Segmentasi Data:** Data dan operasi dibatasi berdasarkan cabang
* **Isolated Operations:** Operasi dalam cabang bersifat terisolasi
* **Cross-Branch Access:** Admin memiliki akses lintas cabang

#### Branch-Based Access Control ####
* **CS dan Teller:** Hanya dapat mengakses data nasabah dan operasi dalam cabang yang sama
* **Branch Manager:** Akses penuh dalam cabang yang dikelola
* **System Admin:** Akses lintas cabang untuk keperluan administrasi sistem

### Akses URL ###

**Web UI:**
- **Dashboard**: [http://localhost:8080/dashboard](http://localhost:8080/dashboard)
- **Product Management**: [http://localhost:8080/product/list](http://localhost:8080/product/list)
- **Customer Management**: [http://localhost:8080/customer/list](http://localhost:8080/customer/list)
- **Account Opening (Personal)**: [http://localhost:8080/account/open](http://localhost:8080/account/open)
- **Account Opening (Corporate)**: [http://localhost:8080/account/open/corporate](http://localhost:8080/account/open/corporate)
- **Account List**: [http://localhost:8080/account/list](http://localhost:8080/account/list)
- **Transaction List**: [http://localhost:8080/transaction/list](http://localhost:8080/transaction/list)
- **Cash Deposit**: [http://localhost:8080/transaction/cash-deposit](http://localhost:8080/transaction/cash-deposit)
- **User Management**: [http://localhost:8080/user/list](http://localhost:8080/user/list)
- **Role Management**: [http://localhost:8080/role/list](http://localhost:8080/role/list)
- **Passbook Printing**: [http://localhost:8080/passbook/select-account](http://localhost:8080/passbook/select-account)

**REST API**: [http://localhost:8080/api/](http://localhost:8080/api/)
- Customers: `/api/customers`
- Accounts: `/api/accounts`
- Transactions: `/api/transactions`
- Users: `/api/users`
- Branches: `/api/branches`

### Sample Data Nasabah ###

Aplikasi telah memiliki data sample nasabah yang dapat digunakan untuk testing:

**Nasabah Perorangan:**
| Customer Number | Name | Email | KTP | Phone | Address | Branch |
|-----------------|------|-------|-----|-------|---------|--------|
| C1000001 | Ahmad Suharto | ahmad.suharto@email.com | 3271081503850001 | 081234567890 | Jl. Sudirman No. 123, Jakarta 10220 | Main Branch |
| C1000002 | Siti Nurhaliza | siti.nurhaliza@email.com | 3271082207900002 | 081234567891 | Jl. Thamrin No. 456, Jakarta 10230 | Main Branch |
| C1000004 | Budi Santoso | budi.santoso@email.com | 3271081011880003 | 081234567892 | Jl. Gatot Subroto No. 321, Jakarta 12930 | Main Branch |
| C1000006 | Dewi Lestari | dewi.lestari@email.com | 3271081805920004 | 081234567893 | Jl. MH Thamrin No. 654, Jakarta 10350 | Main Branch |

**Nasabah Korporasi:**
| Customer Number | Company Name | Email | Registration Number | Tax ID | Phone | Address | Branch |
|-----------------|--------------|-------|-------------------|--------|-------|---------|--------|
| C1000003 | PT. Teknologi Maju | info@teknologimaju.com | 1234567890123456 | 01.234.567.8-901.000 | 02123456789 | Jl. HR Rasuna Said No. 789, Jakarta 12950 | Main Branch |

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

**Pembukaan Rekening Personal:**
- Login dengan user CS (cs1/cs2/cs3) atau Teller (teller1/teller2/teller3)
- Akses URL: [http://localhost:8080/account/open](http://localhost:8080/account/open)
- Pilih nasabah perorangan dari daftar atau search berdasarkan nomor nasabah
- Pilih produk Islamic banking (Tabungan Wadiah, Mudharabah, atau Deposito)
- Input nama rekening dan setoran awal (minimum sesuai produk)
- Input nama pegawai yang membuka rekening
- Submit form untuk generate nomor rekening otomatis dengan prefix "ACC"

**Pembukaan Rekening Corporate:**
- Login dengan user Manager (manager1/manager2) atau Admin
- Akses URL: [http://localhost:8080/account/open/corporate](http://localhost:8080/account/open/corporate)
- Pilih nasabah korporat dari daftar (ditampilkan dengan badge CORPORATE)
- Lihat informasi perusahaan (nama, SIUP, NPWP, contact person)
- Pilih produk Corporate banking dengan minimum deposit 5x lipat
- Input nama rekening korporat dan setoran awal
- Input nama account manager
- Submit form untuk generate nomor rekening dengan prefix "CORP"

**Fitur Pendukung:**
- Multi-account: Nasabah dapat memiliki beberapa rekening dengan produk berbeda
- Islamic banking compliance: Nisbah profit sharing untuk produk Mudharabah
- Real-time validation: Validasi minimum deposit dan kelayakan produk
- Audit trail: Pencatatan lengkap siapa dan kapan rekening dibuka

#### 2. Teller - Transaksi Tunai ####
- Login dengan user Teller (teller1/teller2/teller3)
- **Setoran Tunai (Cash Deposit):**
  - Akses [Transaction List](http://localhost:8080/transaction/list)
  - Klik "Cash Deposit" untuk mulai transaksi
  - Search dan pilih rekening nasabah
  - Input nominal setoran (minimum IDR 10,000)
  - Input deskripsi dan nomor referensi (opsional)
  - Input nama teller yang memproses
  - Sistem otomatis update saldo dan generate transaction number
  - View transaction details dan balance calculations
  - Print transaction receipt (tersedia melalui transaction view)
- **Riwayat Transaksi:**
  - View semua transaksi dengan pagination
  - Filter by transaction type (DEPOSIT, WITHDRAWAL)
  - Search by transaction number atau deskripsi
  - View detail transaksi dengan audit trail lengkap
- ❌ **Penarikan Tunai:** REST API tersedia, Web UI belum diimplementasi
- ✅ **Cetak Buku Tabungan (Passbook Printing):**
  - Akses [Passbook Selection](http://localhost:8080/passbook/select-account)
  - Search dan pilih rekening nasabah yang aktif
  - Preview riwayat transaksi dengan filter tanggal opsional
  - Print dokumen passbook dengan format bank profesional
  - **Browser Print Settings**: A4 paper, disable headers/footers, enable background graphics
  - **Supported Browsers**: Chrome & Firefox (full), Safari & Edge (basic)
  - Print-optimized layout dengan bank logo dan letterhead

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