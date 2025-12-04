# Panduan Proses Onboarding Nasabah Baru

**Aplikasi Minibank - Sistem Perbankan Syariah**

---

**Tanggal Pembuatan:** 05 November 2025  
**Versi:** 3.0  
**Target Pengguna:** Customer Service (CS), Branch Manager, dan Teller  
**Status:** Aktif  

---

## Daftar Isi

1. [Gambaran Umum](#gambaran-umum)
2. [Proses Bisnis End-to-End](#proses-bisnis-end-to-end)
   1. [Customer Service: Registrasi Nasabah & Pembukaan Rekening](#customer-service-registrasi-nasabah--pembukaan-rekening)
   2. [Branch Manager: Persetujuan Nasabah & Rekening](#branch-manager-persetujuan-nasabah--rekening)
   3. [Teller: Transaksi & Layanan](#teller-transaksi--layanan)
3. [Prasyarat](#prasyarat)
4. [Alur Kerja Approval](#alur-kerja-approval)
5. [Langkah-langkah Detail](#langkah-langkah-detail)
   1. [Bagian 1: Customer Service - Membuat Nasabah Baru](#bagian-1-customer-service---membuat-nasabah-baru)
   2. [Bagian 2: Branch Manager - Review dan Approval](#bagian-2-branch-manager---review-dan-approval)
6. [Video Tutorial](#video-tutorial)
7. [Status Approval](#status-approval)
8. [Tips dan Catatan Penting](#tips-dan-catatan-penting)
9. [Pemecahan Masalah Umum](#pemecahan-masalah-umum)

---

## Gambaran Umum

Panduan ini menjelaskan proses **Approval Workflow** dalam sistem Minibank. Approval Workflow adalah mekanisme kontrol yang memastikan setiap nasabah baru yang didaftarkan oleh Customer Service harus disetujui terlebih dahulu oleh Branch Manager sebelum dapat melakukan transaksi.

**Manfaat Approval Workflow:**
- ✅ Kontrol kualitas data nasabah
- ✅ Pencegahan fraud dan data tidak valid
- ✅ Audit trail lengkap untuk setiap persetujuan
- ✅ Dual control untuk operasi penting

**Waktu Estimasi:** 10-15 menit per approval request

---

## Proses Bisnis End-to-End

Panduan ini menjelaskan alur bisnis lengkap dari pendaftaran nasabah hingga transaksi pertama. Sistem Minibank menggunakan **dual approval workflow** untuk memastikan keamanan dan akurasi data.

### Customer Service: Registrasi Nasabah & Pembukaan Rekening

#### 1. Pilih Jenis Nasabah
Customer Service memulai dengan memilih jenis nasabah:
- **Personal Customer (Nasabah Perorangan)**
  - Untuk individu
  - Memerlukan KTP/Passport
  - Data personal lengkap (FR.002)
- **Corporate Customer (Nasabah Korporat)**
  - Untuk perusahaan/badan hukum
  - Memerlukan dokumen perusahaan
  - Informasi PIC (Person In Charge)

#### 2. Input Data Nasabah
CS mengisi formulir lengkap dengan data yang diverifikasi dari dokumen:
- **Identitas:** KTP/Passport, tanggal lahir, tempat lahir
- **Kontak:** Email, nomor telepon, alamat lengkap
- **Data Pribadi:** Pendidikan, agama, status pernikahan, tanggungan
- **Pekerjaan:** Occupation, perusahaan, bidang usaha, penghasilan
- **Tujuan:** Purpose of account opening, estimasi transaksi
- **Compliance:** Source of funds (anti money laundering)

#### 3. Simpan Data & Dapatkan Customer Number
Setelah data divalidasi:
- Sistem generate **Customer Number** otomatis (contoh: C1000001)
- Status nasabah: **PENDING_APPROVAL** (INACTIVE)
- Sistem membuat **ApprovalRequest** otomatis untuk Branch Manager
- CS mendapat konfirmasi: "Customer submitted for approval"

#### 4. Buka Layar Account Opening
CS melanjutkan dengan membuka rekening untuk nasabah:
- Navigate ke menu "Open Account"
- Sistem menampilkan form pencarian customer

#### 5. Cari & Pilih Nasabah
CS dapat mencari nasabah dengan berbagai cara:
- **Search by Customer Number:** Ketik nomor nasabah (C1000001)
- **Search by Name:** Ketik nama lengkap atau sebagian nama
- **Search by Email:** Ketik alamat email
- Sistem menampilkan hanya nasabah dengan status **ACTIVE**
- ⚠️ **Catatan:** Nasabah PENDING_APPROVAL tidak dapat dibuka rekeningnya

#### 6. Pilih Jenis Rekening (Account Type)
Sistem menampilkan produk yang tersedia berdasarkan jenis nasabah:
- **Conventional Products:**
  - SAVINGS (Tabungan konvensional)
  - CHECKING (Giro konvensional)
- **Islamic Banking Products:**
  - TABUNGAN_WADIAH (Tabungan Syariah dengan akad Wadiah)
  - TABUNGAN_MUDHARABAH (Tabungan Syariah dengan akad Mudharabah)
  - DEPOSITO_MUDHARABAH (Deposito Syariah)

#### 7. Pilih Akad Syariah (Islamic Contract)
Akad sudah embedded dalam product type:
- **Wadiah (Safekeeping)**
  - Produk: TABUNGAN_WADIAH
  - Prinsip: Titipan dengan izin penggunaan
  - Bonus: Bank boleh memberikan bonus (tidak dijanjikan)
  - Cocok untuk: Transaction account, liquidity management
- **Mudharabah (Profit Sharing)**
  - Produk: TABUNGAN_MUDHARABAH, DEPOSITO_MUDHARABAH
  - Prinsip: Bagi hasil (profit sharing)
  - Nisbah: Customer 60% - Bank 40% (configurable)
  - Cocok untuk: Savings account, investment
- **Qardh (Benevolent Loan)** - Coming soon

#### 8. Simpan Rekening Baru
CS melengkapi form pembukaan rekening:
- **Account Name:** Nama untuk rekening (e.g., "John - Savings")
- **Product:** Pilihan produk Islamic/conventional
- Sistem generate **Account Number** otomatis (contoh: ACC0000001)
- Status rekening: **PENDING_APPROVAL** (INACTIVE)
- Sistem membuat **ApprovalRequest** otomatis untuk Branch Manager
- **Catatan:** Rekening dibuat dengan saldo nol, setoran awal dilakukan di menu Teller setelah approval

**Summary CS Flow:**
```
CS Login → Select Customer Type → Input Data → Save (get C1000001)
       → Open Account → Search Customer → Select Product & Akad
       → Save Account (get ACC0000001)
       → Both waiting for Branch Manager approval
       → Setoran awal dilakukan Teller setelah approval
```

---

### Branch Manager: Persetujuan Nasabah & Rekening

#### 1. Cari Nasabah (Search Customer)
Branch Manager login dan navigate ke **Approval Queue**:
- Queue menampilkan semua pending approval
- Filter by **Request Type:**
  - CUSTOMER_CREATION: Nasabah baru
  - ACCOUNT_OPENING: Rekening baru
- Filter by **Entity Type:**
  - CUSTOMER
  - ACCOUNT
- Search by customer number, name, atau requested date

#### 2. Temukan Data Nasabah Baru
Manager klik "View Details" pada request **CUSTOMER_CREATION**:
- **Request Information:**
  - Request Type: CUSTOMER_CREATION
  - Entity Type: CUSTOMER
  - Requested By: CS username (e.g., cs1)
  - Requested Date: Timestamp pembuatan
  - Request Notes: Catatan dari CS
- **Customer Details (Complete):**
  - Customer Number: C1000001
  - Customer Type: PERSONAL/CORPORATE
  - Personal data: nama, identitas, kontak
  - Employment data: pekerjaan, penghasilan
  - Compliance data: source of funds, account purpose
  - All FR.002 compliant fields

#### 3. Authorize Customer Registration
Manager melakukan review dan approval:
- **Review Checklist:**
  - ✅ Data lengkap dan akurat
  - ✅ Sesuai dengan dokumen pendukung
  - ✅ Tidak ada red flag (data mencurigakan)
  - ✅ Compliant dengan regulasi banking
- **Action: Approve**
  - Isi "Review Notes" (optional but recommended)
  - Klik tombol "Approve"
  - Hasil: Customer status → **APPROVED & ACTIVE**
  - Customer dapat digunakan untuk account opening
- **Action: Reject**
  - **Wajib** isi "Rejection Reason" dengan detail
  - Isi "Additional Notes" (optional)
  - Klik tombol "Reject"
  - Hasil: Customer status → **REJECTED & INACTIVE**
  - CS dapat melihat rejection reason untuk perbaikan

#### 4. Cari Rekening by Customer (Search Account)
Setelah approve customer, Manager mencari account request:
- Kembali ke **Approval Queue**
- Filter by Request Type: **ACCOUNT_OPENING**
- Dapat search by customer number atau account number
- Sistem menampilkan accounts yang pending approval

#### 5. Temukan Rekening Baru untuk Nasabah
Manager klik "View Details" pada request **ACCOUNT_OPENING**:
- **Request Information:**
  - Request Type: ACCOUNT_OPENING
  - Entity Type: ACCOUNT
  - Requested By: CS username
  - Request Notes: e.g., "New account opening for [Customer Name]"
- **Account Details:**
  - Account Number: ACC0000001
  - Account Name: As per CS input
  - Customer: Link to customer (C1000001)
  - Product: Product name, type, category
  - Product Type: TABUNGAN_WADIAH, TABUNGAN_MUDHARABAH, etc.
  - Balance: IDR 0 (setoran awal dilakukan di Teller)
  - Currency: IDR
  - Status: PENDING_APPROVAL

#### 6. Authorize Account Opening
Manager melakukan review dan approval rekening:
- **Review Checklist:**
  - ✅ Customer sudah APPROVED & ACTIVE
  - ✅ Product sesuai dengan customer type
  - ✅ Account name dan details sesuai
- **Action: Approve**
  - Isi "Review Notes"
  - Klik tombol "Approve"
  - Hasil: Account status → **APPROVED & ACTIVE**
  - Nasabah dapat mulai bertransaksi
- **Action: Reject**
  - **Wajib** isi "Rejection Reason"
  - Hasil: Account status → **REJECTED & INACTIVE**
  - Rekening tidak dapat digunakan

**Summary Manager Flow:**
```
Manager Login → Approval Queue
             → Search & Filter (CUSTOMER_CREATION)
             → View Details Customer (C1000001)
             → Approve/Reject Customer
             → Search & Filter (ACCOUNT_OPENING)
             → View Details Account (ACC0000001)
             → Approve/Reject Account
             → Customer & Account now ACTIVE
```

---

### Teller: Transaksi & Layanan

Setelah customer dan account diapprove, Teller dapat melayani transaksi.

> **💡 Catatan Penting:** Setoran awal (initial deposit) dilakukan di menu Teller setelah rekening diapprove oleh Branch Manager. Rekening dibuat dengan saldo nol oleh Customer Service.

#### 1. Cari Nasabah untuk Transaksi
Teller login dan navigate ke menu transaksi:
- **Menu:** Cash Deposit / Cash Withdrawal / Transfer
- **Search Account:**
  - By Account Number: ACC0000001
  - By Account Name: Sebagian nama
  - By Customer Number: C1000001
- Sistem hanya menampilkan accounts dengan status **ACTIVE**
- Sistem validasi approval_status = **APPROVED**

#### 2. Input Transaksi Cash Deposit (Setoran Tunai)
Teller memproses setoran tunai:
- **Form Transaction:**
  - Account: Selected account (auto-filled)
  - Amount: Jumlah setoran (IDR)
  - Description: "Setoran Tunai" atau custom
  - Reference Number: Optional transaction reference
  - Channel: TELLER (auto-filled)
  - Created By: Teller username
- **Process:**
  - Sistem generate Transaction Number (e.g., TXN0000001)
  - Record balance before and after
  - Update account balance using `account.deposit(amount)`
  - Create audit trail dengan timestamp
  - Display success message dengan transaction number

#### 3. Print Account Statement (Rekening Koran) ke PDF
Teller dapat generate dan print rekening koran:
- **Navigate:** Account List → Select Account → "Statement"
- **Form Parameters:**
  - Start Date: Tanggal awal periode (default: 3 bulan lalu)
  - End Date: Tanggal akhir periode (default: hari ini)
- **Klik:** "Generate PDF"
- **Output PDF Content:**
  - Bank letterhead dan logo
  - Account information (number, name, type, product)
  - Customer information
  - Statement period
  - Transaction table:
    - Date | Description | Transaction Type
    - Debit | Credit | Running Balance
  - Opening balance dan closing balance
  - Total debit dan total credit
  - Generated timestamp dan user
- **Download:** File format: `statement_ACC0000001_2025-01-01_to_2025-03-31.pdf`

**Bonus: Transaction Receipt PDF**
Setiap transaksi juga dapat di-print sebagai receipt:
- Navigate ke Transaction List
- Klik "View" pada transaction
- Klik "Download Receipt (PDF)"
- Professional receipt format dengan transaction details lengkap

**Summary Teller Flow:**
```
Teller Login → Cash Deposit Menu
            → Search Account (by number/name/customer)
            → Select ACTIVE Account (ACC0000001)
            → Input Amount & Details
            → Process Transaction (get TXN0000001)
            → Print Receipt PDF (optional)
            → OR Generate Account Statement PDF
```

---

### 🎯 Complete Business Flow Summary

```mermaid
sequenceDiagram
    participant CS as Customer Service
    participant System
    participant Manager as Branch Manager
    participant Teller
    
    Note over CS: Part 1: Customer & Account Creation
    CS->>System: 1. Select Customer Type (Personal/Corporate)
    CS->>System: 2. Input Customer Details (FR.002)
    System->>System: 3. Generate Customer Number (C1000001)
    System->>System: Save with PENDING_APPROVAL status
    CS->>System: 4. Navigate to Account Opening
    CS->>System: 5. Search & Select Customer
    CS->>System: 6. Select Account Type (SAVINGS/CHECKING)
    CS->>System: 7. Select Akad (WADIAH/MUDHARABAH)
    System->>System: 8. Generate Account Number (ACC0000001)
    System->>System: Save with PENDING_APPROVAL status
    
    Note over Manager: Part 2: Approval Process
    Manager->>System: 1. Search Customer in Approval Queue
    System->>Manager: 2. Display Customer Details (C1000001)
    Manager->>System: 3. Approve Customer Registration
    System->>System: Customer → APPROVED & ACTIVE
    Manager->>System: 4. Search Account in Approval Queue
    System->>Manager: 5. Display Account Details (ACC0000001)
    Manager->>System: 6. Approve Account Opening
    System->>System: Account → APPROVED & ACTIVE
    
    Note over Teller: Part 3: Transaction Processing
    Teller->>System: 1. Search Customer/Account
    System->>Teller: Display ACTIVE Accounts only
    Teller->>System: 2. Input Cash Deposit Transaction
    System->>System: Generate Transaction Number (TXN0000001)
    System->>System: Update Balance & Create Audit Trail
    Teller->>System: 3. Generate Account Statement PDF
    System->>Teller: Download statement_ACC0000001.pdf
```

**🔑 Key Points:**
1. **Customer Number** auto-generated saat CS save customer (C1000001)
2. **Account Number** auto-generated saat CS save account (ACC0000001)
3. **Akad Selection** embedded dalam product type (TABUNGAN_WADIAH = Wadiah akad)
4. **Dual Approval** required: Customer approval → then Account approval
5. **Status Flow:**
   - Customer: PENDING_APPROVAL → APPROVED → ACTIVE (can transact)
   - Account: PENDING_APPROVAL → APPROVED → ACTIVE (can transact)
6. **Complete Audit Trail** for all operations with timestamp and username

---

## Prasyarat

### Untuk Customer Service:
1. **Akses Sistem**
   - Username dan password CS yang valid
   - Koneksi internet stabil
   - Browser web yang didukung (Chrome, Firefox, Safari)

2. **Dokumen Nasabah**
   - KTP/Passport asli dan fotokopi
   - NPWP (jika ada)
   - Dokumen pendukung sesuai kebijakan bank

### Untuk Branch Manager:
1. **Akses Sistem**
   - Username dan password Branch Manager yang valid
   - Authority untuk melakukan approval

2. **Tanggung Jawab**
   - Memverifikasi kelengkapan data nasabah
   - Memastikan kepatuhan terhadap regulasi
   - Memberikan catatan review yang jelas

---

## Alur Kerja Approval

```mermaid
graph TD
    A[CS: Input Data Nasabah] --> B[Sistem: Simpan dengan Status PENDING_APPROVAL]
    B --> C[Sistem: Buat Approval Request]
    C --> D[Manager: Lihat Approval Queue]
    D --> E[Manager: Review Detail Nasabah]
    E --> F{Manager: Keputusan}
    F -->|Approve| G[Sistem: Ubah Status ke APPROVED & ACTIVE]
    F -->|Reject| H[Sistem: Ubah Status ke REJECTED]
    G --> I[Nasabah dapat bertransaksi]
    H --> J[Nasabah tidak dapat digunakan]
```

---

## Langkah-langkah Detail

### Bagian 1: Customer Service - Membuat Nasabah Baru

#### Langkah 1: Login sebagai Customer Service

![Halaman Login](screenshots/2025-11-05_22-53-53_customer_onboarding_01_halaman_login.png)

![Username Terisi](screenshots/2025-11-05_22-53-55_customer_onboarding_02_username_terisi.png)

![Password Terisi](screenshots/2025-11-05_22-53-58_customer_onboarding_03_password_terisi.png)

![Siap Login](screenshots/2025-11-05_22-53-58_customer_onboarding_04_siap_login.png)

**Detail Langkah:**
1. Buka halaman login aplikasi Minibank
2. Masukkan username CS Anda (contoh: cs1)
3. Masukkan password yang telah ditentukan
4. Klik tombol "Login"
5. Verifikasi bahwa Anda berhasil masuk ke dashboard

![Dashboard Customer Service](screenshots/2025-11-05_22-54-09_customer_onboarding_05_dashboard_cs_berhasil_login.png)

![Dashboard Customer Service](screenshots/2025-11-05_23-04-35_customer_onboarding_05_dashboard_cs_berhasil_login.png)

![Dashboard Customer Service](screenshots/2025-11-05_23-11-13_customer_onboarding_05_dashboard_cs_berhasil_login.png)


---

#### Langkah 2: Navigasi ke Menu Customer Management

![Menu Navigasi](screenshots/2025-11-05_22-54-10_customer_onboarding_06_menu_navigasi_dashboard.png)

![Pilih Jenis Nasabah](screenshots/2025-11-05_22-54-13_customer_onboarding_07_halaman_pilih_jenis_nasabah.png)

**Detail Langkah:**
1. Dari dashboard, cari menu navigasi di sidebar
2. Klik menu "Customer Management"
3. Klik tombol "Add Customer" atau "Tambah Nasabah"
4. Sistem menampilkan pilihan jenis nasabah

---

#### Langkah 3: Pilih Jenis Nasabah dan Isi Form

![Pilihan Jenis Nasabah](screenshots/2025-11-05_22-54-13_customer_onboarding_08_pilihan_jenis_nasabah.png)

![Form Nasabah Personal Kosong](screenshots/2025-11-05_22-54-16_customer_onboarding_09_form_nasabah_personal_kosong.png)

![Data Pribadi Terisi](screenshots/2025-11-05_22-55-19_customer_onboarding_10_form_data_pribadi_terisi.png)

![Data Kontak Terisi](screenshots/2025-11-05_22-55-20_customer_onboarding_11_form_data_kontak_terisi.png)

![Form Lengkap Siap Disimpan](screenshots/2025-11-05_22-55-20_customer_onboarding_12_form_lengkap_siap_disimpan.png)

**Detail Langkah:**
1. Pilih "Personal Customer"
2. Isi semua field yang wajib (ditandai dengan *)
3. **Data Pribadi:**
   - Nama Depan dan Nama Belakang
   - Nomor Identitas (KTP/Passport)
   - Tanggal Lahir dan Tempat Lahir
   - Jenis Kelamin
   - Nama Ibu Kandung
4. **Data Kontak:**
   - Email (opsional)
   - Nomor Telepon
   - Alamat Lengkap
   - Kota dan Kode Pos
5. **Data Pekerjaan (FR.002):**
   - Pekerjaan dan Perusahaan
   - Bidang Usaha
   - Penghasilan Rata-rata
   - Tujuan Pembukaan Rekening
6. Periksa kembali semua data yang diisi

---

#### Langkah 4: Simpan Nasabah (Pending Approval)

![Tombol Simpan](screenshots/2025-11-05_22-55-21_customer_onboarding_13_tombol_simpan.png)

![Nasabah Berhasil Disimpan](screenshots/2025-11-05_22-55-24_customer_onboarding_14_nasabah_berhasil_disimpan.png)

![Pesan Sukses](screenshots/2025-11-05_22-55-25_customer_onboarding_15_pesan_sukses_pending_approval.png)

**Detail Langkah:**
1. Klik tombol "Simpan" atau "Save"
2. Tunggu proses penyimpanan selesai
3. Sistem menampilkan pesan sukses
4. **Penting:** Nasabah tersimpan dengan status **PENDING_APPROVAL**
5. Nasabah belum dapat melakukan transaksi
6. Approval request otomatis dibuat untuk Branch Manager

> **💡 Catatan:** Pesan sukses akan menyebutkan bahwa nasabah "submitted for approval" atau "menunggu persetujuan".

---

#### Langkah 5: Logout Customer Service

![Dashboard Sebelum Logout](screenshots/2025-11-05_22-55-26_customer_onboarding_16_dashboard_sebelum_logout.png)

![Setelah Logout](screenshots/2025-11-05_22-55-31_customer_onboarding_17_setelah_logout_cs.png)

**Detail Langkah:**
1. Klik menu user di pojok kanan atas
2. Pilih "Logout"
3. Sistem kembali ke halaman login

---

### Bagian 2: Branch Manager - Review dan Approval

#### Langkah 6: Login sebagai Branch Manager

![Halaman Login](screenshots/2025-11-05_22-55-33_customer_onboarding_18_halaman_login_manager.png)

![Username Manager Terisi](screenshots/2025-11-05_22-55-36_customer_onboarding_19_username_manager_terisi.png)

![Password Manager Terisi](screenshots/2025-11-05_22-55-39_customer_onboarding_20_password_manager_terisi.png)

![Dashboard Branch Manager](screenshots/2025-11-05_22-55-49_customer_onboarding_21_dashboard_manager_berhasil_login.png)

**Detail Langkah:**
1. Buka halaman login aplikasi Minibank
2. Masukkan username Branch Manager (contoh: manager1)
3. Masukkan password Branch Manager
4. Klik tombol "Login"
5. Verifikasi berhasil masuk ke dashboard Branch Manager

---

#### Langkah 7: Navigasi ke Approval Queue

![Menu Approval Queue](screenshots/2025-11-05_22-55-50_customer_onboarding_22_menu_approval_queue.png)

![Halaman Approval Queue](screenshots/2025-11-05_22-55-54_customer_onboarding_23_halaman_approval_queue.png)

![Daftar Pending Approval](screenshots/2025-11-05_22-55-54_customer_onboarding_24_daftar_pending_approval.png)

**Detail Langkah:**
1. Dari dashboard, cari menu "Approval Queue" di sidebar
2. Klik menu "Approval Queue"
3. Sistem menampilkan daftar approval yang pending
4. Badge "X Pending" menunjukkan jumlah approval yang menunggu
5. Tabel menampilkan:
   - Request Type (CUSTOMER_CREATION, ACCOUNT_OPENING)
   - Entity Type (CUSTOMER, ACCOUNT)
   - Requested By (nama CS yang membuat)
   - Requested Date (tanggal pembuatan)
   - Request Notes (catatan dari CS)

---

#### Langkah 8: Lihat Detail Approval Request

![Halaman Detail Approval](screenshots/2025-11-05_22-55-57_customer_onboarding_25_halaman_detail_approval.png)

![Informasi Request](screenshots/2025-11-05_22-55-58_customer_onboarding_26_informasi_request.png)

![Detail Data Nasabah](screenshots/2025-11-05_22-55-59_customer_onboarding_27_detail_data_nasabah.png)

![Form Approval Actions](screenshots/2025-11-05_22-55-59_customer_onboarding_28_form_approval_actions.png)

**Detail Langkah:**
1. Klik link "View Details" pada approval request yang akan direview
2. Sistem menampilkan halaman detail approval
3. **Informasi Request:**
   - Request Type: CUSTOMER_CREATION
   - Entity Type: CUSTOMER
   - Requested By: customer-service
   - Requested Date: tanggal dan waktu pembuatan
   - Request Notes: catatan dari CS
4. **Detail Data Nasabah:**
   - Customer Number (auto-generated)
   - Customer Type (PERSONAL/CORPORATE)
   - Nama Lengkap
   - Email dan Nomor Telepon
   - Alamat Lengkap
   - Semua data FR.002 lainnya
5. **Form Approval Actions:**
   - Form Approve (hijau): untuk menyetujui
   - Form Reject (merah): untuk menolak

---

#### Langkah 9: Approve atau Reject Request

##### Opsi A: Approve Request

![Catatan Review Terisi](screenshots/2025-11-05_22-56-02_customer_onboarding_29_catatan_review_terisi.png)

![Siap Approve](screenshots/2025-11-05_22-56-02_customer_onboarding_30_siap_approve.png)

![Approval Berhasil](screenshots/2025-11-05_22-56-08_customer_onboarding_31_approval_berhasil.png)

![Pesan Sukses](screenshots/2025-11-05_22-56-08_customer_onboarding_32_pesan_sukses_approval.png)

**Detail Langkah untuk Approve:**
1. Periksa semua data nasabah dengan teliti
2. Pastikan data sesuai dengan dokumen
3. Pastikan tidak ada red flag atau data mencurigakan
4. Isi field "Review Notes" (opsional) dengan catatan review Anda
   - Contoh: "Data nasabah lengkap dan sesuai dokumen. Disetujui untuk aktivasi."
5. Klik tombol "Approve" (hijau)
6. Sistem memproses approval
7. **Hasil:**
   - Approval status berubah menjadi **APPROVED**
   - Customer status berubah menjadi **ACTIVE**
   - Nasabah dapat melakukan transaksi
   - Reviewed By: diisi dengan username Branch Manager
   - Reviewed Date: diisi dengan tanggal/waktu approval
8. Sistem menampilkan pesan sukses
9. Otomatis redirect ke Approval Queue

##### Opsi B: Reject Request

**Detail Langkah untuk Reject:**
1. Jika menemukan data tidak valid atau mencurigakan
2. Scroll ke form "Reject Request" (merah)
3. **Wajib** isi field "Rejection Reason" dengan alasan penolakan
   - Contoh: "Dokumen identitas tidak sesuai dengan data yang diinput"
   - Contoh: "Data pekerjaan tidak lengkap dan tidak jelas"
   - Contoh: "Nomor telepon tidak dapat dihubungi"
4. Isi field "Additional Notes" (opsional) dengan catatan tambahan
5. Klik tombol "Reject" (merah)
6. **Hasil:**
   - Approval status berubah menjadi **REJECTED**
   - Customer tetap dengan status **INACTIVE**
   - Nasabah tidak dapat melakukan transaksi
   - Rejection reason tersimpan untuk audit
   - CS dapat melihat alasan penolakan

---

#### Langkah 10: Verifikasi Approval Queue

![Queue Setelah Approval](screenshots/2025-11-05_22-56-09_customer_onboarding_33_queue_setelah_approval.png)

![Setelah Logout](screenshots/2025-11-05_22-56-14_customer_onboarding_34_setelah_logout_manager.png)

![Halaman Login](screenshots/2025-11-05_22-56-17_customer_onboarding_35_halaman_login_cs_kembali.png)

![Dashboard Customer Service](screenshots/2025-11-05_22-56-32_customer_onboarding_36_dashboard_cs_login_kembali.png)

![Screenshot](screenshots/2025-11-05_22-56-36_customer_onboarding_37_halaman_pilih_nasabah_untuk_rekening.png)

![Screenshot](screenshots/2025-11-05_22-56-36_customer_onboarding_38_daftar_nasabah_aktif.png)

![Screenshot](screenshots/2025-11-05_22-56-40_customer_onboarding_39_form_pembukaan_rekening.png)

![Screenshot](screenshots/2025-11-05_22-56-42_customer_onboarding_40_produk_dipilih.png)

![Screenshot](screenshots/2025-11-05_22-56-45_customer_onboarding_41_setoran_awal_terisi.png)

![Screenshot](screenshots/2025-11-05_22-56-48_customer_onboarding_42_form_rekening_lengkap.png)

![Screenshot](screenshots/2025-11-05_22-56-51_customer_onboarding_43_rekening_berhasil_disimpan.png)

![Halaman Login](screenshots/2025-11-05_23-04-18_customer_onboarding_01_halaman_login.png)

![Username Terisi](screenshots/2025-11-05_23-04-21_customer_onboarding_02_username_terisi.png)

![Password Terisi](screenshots/2025-11-05_23-04-23_customer_onboarding_03_password_terisi.png)

![Siap Login](screenshots/2025-11-05_23-04-23_customer_onboarding_04_siap_login.png)

![Dashboard Customer Service](screenshots/2025-11-05_23-04-35_customer_onboarding_05_dashboard_cs_berhasil_login.png)

![Menu Navigasi](screenshots/2025-11-05_23-04-36_customer_onboarding_06_menu_navigasi_dashboard.png)

![Pilih Jenis Nasabah](screenshots/2025-11-05_23-04-39_customer_onboarding_07_halaman_pilih_jenis_nasabah.png)

![Pilihan Jenis Nasabah](screenshots/2025-11-05_23-04-39_customer_onboarding_08_pilihan_jenis_nasabah.png)

![Form Nasabah Personal Kosong](screenshots/2025-11-05_23-04-42_customer_onboarding_09_form_nasabah_personal_kosong.png)

![Data Pribadi Terisi](screenshots/2025-11-05_23-05-45_customer_onboarding_10_form_data_pribadi_terisi.png)

![Data Kontak Terisi](screenshots/2025-11-05_23-05-46_customer_onboarding_11_form_data_kontak_terisi.png)

![Form Lengkap Siap Disimpan](screenshots/2025-11-05_23-05-46_customer_onboarding_12_form_lengkap_siap_disimpan.png)

![Tombol Simpan](screenshots/2025-11-05_23-05-47_customer_onboarding_13_tombol_simpan.png)

![Nasabah Berhasil Disimpan](screenshots/2025-11-05_23-05-50_customer_onboarding_14_nasabah_berhasil_disimpan.png)

![Pesan Sukses](screenshots/2025-11-05_23-05-51_customer_onboarding_15_pesan_sukses_pending_approval.png)

![Dashboard Sebelum Logout](screenshots/2025-11-05_23-05-52_customer_onboarding_16_dashboard_sebelum_logout.png)

![Setelah Logout](screenshots/2025-11-05_23-05-57_customer_onboarding_17_setelah_logout_cs.png)

![Halaman Login](screenshots/2025-11-05_23-05-59_customer_onboarding_18_halaman_login_manager.png)

![Username Manager Terisi](screenshots/2025-11-05_23-06-02_customer_onboarding_19_username_manager_terisi.png)

![Password Manager Terisi](screenshots/2025-11-05_23-06-05_customer_onboarding_20_password_manager_terisi.png)

![Dashboard Branch Manager](screenshots/2025-11-05_23-06-16_customer_onboarding_21_dashboard_manager_berhasil_login.png)

![Menu Approval Queue](screenshots/2025-11-05_23-06-17_customer_onboarding_22_menu_approval_queue.png)

![Halaman Approval Queue](screenshots/2025-11-05_23-06-20_customer_onboarding_23_halaman_approval_queue.png)

![Daftar Pending Approval](screenshots/2025-11-05_23-06-21_customer_onboarding_24_daftar_pending_approval.png)

![Halaman Detail Approval](screenshots/2025-11-05_23-06-24_customer_onboarding_25_halaman_detail_approval.png)

![Informasi Request](screenshots/2025-11-05_23-06-25_customer_onboarding_26_informasi_request.png)

![Detail Data Nasabah](screenshots/2025-11-05_23-06-25_customer_onboarding_27_detail_data_nasabah.png)

![Form Approval Actions](screenshots/2025-11-05_23-06-26_customer_onboarding_28_form_approval_actions.png)

![Catatan Review Terisi](screenshots/2025-11-05_23-06-28_customer_onboarding_29_catatan_review_terisi.png)

![Siap Approve](screenshots/2025-11-05_23-06-29_customer_onboarding_30_siap_approve.png)

![Approval Berhasil](screenshots/2025-11-05_23-06-34_customer_onboarding_31_approval_berhasil.png)

![Pesan Sukses](screenshots/2025-11-05_23-06-35_customer_onboarding_32_pesan_sukses_approval.png)

![Queue Setelah Approval](screenshots/2025-11-05_23-06-36_customer_onboarding_33_queue_setelah_approval.png)

![Setelah Logout](screenshots/2025-11-05_23-06-41_customer_onboarding_34_setelah_logout_manager.png)

![Halaman Login](screenshots/2025-11-05_23-06-44_customer_onboarding_35_halaman_login_cs_kembali.png)

![Dashboard Customer Service](screenshots/2025-11-05_23-06-59_customer_onboarding_36_dashboard_cs_login_kembali.png)

![Screenshot](screenshots/2025-11-05_23-07-02_customer_onboarding_37_halaman_pilih_nasabah_untuk_rekening.png)

![Screenshot](screenshots/2025-11-05_23-07-03_customer_onboarding_38_daftar_nasabah_aktif.png)

![Screenshot](screenshots/2025-11-05_23-07-06_customer_onboarding_39_form_pembukaan_rekening.png)

![Screenshot](screenshots/2025-11-05_23-07-09_customer_onboarding_40_produk_dipilih.png)

![Screenshot](screenshots/2025-11-05_23-07-12_customer_onboarding_41_setoran_awal_terisi.png)

![Screenshot](screenshots/2025-11-05_23-07-14_customer_onboarding_42_form_rekening_lengkap.png)

![Screenshot](screenshots/2025-11-05_23-07-18_customer_onboarding_43_rekening_berhasil_disimpan.png)

![Pesan Sukses](screenshots/2025-11-05_23-07-18_customer_onboarding_44_pesan_sukses_rekening_pending.png)

![Setelah Logout](screenshots/2025-11-05_23-07-23_customer_onboarding_45_setelah_logout_cs.png)

![Halaman Login](screenshots/2025-11-05_23-07-26_customer_onboarding_46_halaman_login_manager_kembali.png)

![Dashboard Branch Manager](screenshots/2025-11-05_23-07-42_customer_onboarding_47_dashboard_manager_login_kembali.png)

![Screenshot](screenshots/2025-11-05_23-07-45_customer_onboarding_48_approval_queue_rekening.png)

![Daftar Pending Approval](screenshots/2025-11-05_23-07-45_customer_onboarding_49_daftar_pending_approval_rekening.png)

![Halaman Detail Approval](screenshots/2025-11-05_23-07-49_customer_onboarding_50_halaman_detail_approval_rekening.png)

![Informasi Request](screenshots/2025-11-05_23-07-49_customer_onboarding_51_informasi_request_rekening.png)

![Screenshot](screenshots/2025-11-05_23-07-50_customer_onboarding_52_detail_data_rekening.png)

![Catatan Review Terisi](screenshots/2025-11-05_23-07-52_customer_onboarding_53_catatan_review_rekening_terisi.png)

![Screenshot](screenshots/2025-11-05_23-07-58_customer_onboarding_54_approval_rekening_berhasil.png)

![Pesan Sukses](screenshots/2025-11-05_23-07-58_customer_onboarding_55_pesan_sukses_approval_rekening.png)

![Setelah Logout](screenshots/2025-11-05_23-08-03_customer_onboarding_56_setelah_logout_manager_final.png)

![Halaman Login](screenshots/2025-11-05_23-08-06_customer_onboarding_57_halaman_login_teller.png)

![Screenshot](screenshots/2025-11-05_23-08-08_customer_onboarding_58_username_teller_terisi.png)

![Screenshot](screenshots/2025-11-05_23-08-11_customer_onboarding_59_password_teller_terisi.png)

![Screenshot](screenshots/2025-11-05_23-08-21_customer_onboarding_60_dashboard_teller_berhasil_login.png)

![Screenshot](screenshots/2025-11-05_23-08-25_customer_onboarding_61_halaman_pilih_rekening_deposit.png)

![Screenshot](screenshots/2025-11-05_23-08-26_customer_onboarding_62_daftar_rekening_tersedia.png)

![Screenshot](screenshots/2025-11-05_23-08-29_customer_onboarding_63_form_deposit_terbuka.png)

![Screenshot](screenshots/2025-11-05_23-08-32_customer_onboarding_64_jumlah_setoran_terisi.png)

![Screenshot](screenshots/2025-11-05_23-08-34_customer_onboarding_65_keterangan_setoran_terisi.png)

![Screenshot](screenshots/2025-11-05_23-08-37_customer_onboarding_66_form_deposit_lengkap.png)

![Screenshot](screenshots/2025-11-05_23-08-40_customer_onboarding_67_setoran_berhasil_diproses.png)

![Pesan Sukses](screenshots/2025-11-05_23-08-41_customer_onboarding_68_pesan_sukses_setoran.png)

![Screenshot](screenshots/2025-11-05_23-08-44_customer_onboarding_69_halaman_pilih_rekening_cetak.png)

![Screenshot](screenshots/2025-11-05_23-08-45_customer_onboarding_70_daftar_rekening_cetak.png)

![Halaman Login](screenshots/2025-11-05_23-11-06_customer_onboarding_01_halaman_login.png)

![Username Terisi](screenshots/2025-11-05_23-11-07_customer_onboarding_02_username_terisi.png)

![Password Terisi](screenshots/2025-11-05_23-11-09_customer_onboarding_03_password_terisi.png)

![Siap Login](screenshots/2025-11-05_23-11-09_customer_onboarding_04_siap_login.png)

![Dashboard Customer Service](screenshots/2025-11-05_23-11-13_customer_onboarding_05_dashboard_cs_berhasil_login.png)

![Menu Navigasi](screenshots/2025-11-05_23-11-14_customer_onboarding_06_menu_navigasi_dashboard.png)

![Pilih Jenis Nasabah](screenshots/2025-11-05_23-11-15_customer_onboarding_07_halaman_pilih_jenis_nasabah.png)

![Pilihan Jenis Nasabah](screenshots/2025-11-05_23-11-15_customer_onboarding_08_pilihan_jenis_nasabah.png)

![Form Nasabah Personal Kosong](screenshots/2025-11-05_23-11-16_customer_onboarding_09_form_nasabah_personal_kosong.png)

![Data Pribadi Terisi](screenshots/2025-11-05_23-11-33_customer_onboarding_10_form_data_pribadi_terisi.png)

![Data Kontak Terisi](screenshots/2025-11-05_23-11-34_customer_onboarding_11_form_data_kontak_terisi.png)

![Form Lengkap Siap Disimpan](screenshots/2025-11-05_23-11-34_customer_onboarding_12_form_lengkap_siap_disimpan.png)

![Tombol Simpan](screenshots/2025-11-05_23-11-34_customer_onboarding_13_tombol_simpan.png)

![Nasabah Berhasil Disimpan](screenshots/2025-11-05_23-11-36_customer_onboarding_14_nasabah_berhasil_disimpan.png)

![Pesan Sukses](screenshots/2025-11-05_23-11-37_customer_onboarding_15_pesan_sukses_pending_approval.png)

![Dashboard Sebelum Logout](screenshots/2025-11-05_23-11-38_customer_onboarding_16_dashboard_sebelum_logout.png)

![Setelah Logout](screenshots/2025-11-05_23-11-40_customer_onboarding_17_setelah_logout_cs.png)

![Halaman Login](screenshots/2025-11-05_23-11-41_customer_onboarding_18_halaman_login_manager.png)

![Username Manager Terisi](screenshots/2025-11-05_23-11-42_customer_onboarding_19_username_manager_terisi.png)

![Password Manager Terisi](screenshots/2025-11-05_23-11-43_customer_onboarding_20_password_manager_terisi.png)

![Dashboard Branch Manager](screenshots/2025-11-05_23-11-46_customer_onboarding_21_dashboard_manager_berhasil_login.png)

![Menu Approval Queue](screenshots/2025-11-05_23-11-47_customer_onboarding_22_menu_approval_queue.png)

![Halaman Approval Queue](screenshots/2025-11-05_23-11-49_customer_onboarding_23_halaman_approval_queue.png)

![Daftar Pending Approval](screenshots/2025-11-05_23-11-50_customer_onboarding_24_daftar_pending_approval.png)

![Halaman Detail Approval](screenshots/2025-11-05_23-11-52_customer_onboarding_25_halaman_detail_approval.png)

![Informasi Request](screenshots/2025-11-05_23-11-52_customer_onboarding_26_informasi_request.png)

![Detail Data Nasabah](screenshots/2025-11-05_23-11-53_customer_onboarding_27_detail_data_nasabah.png)

![Form Approval Actions](screenshots/2025-11-05_23-11-53_customer_onboarding_28_form_approval_actions.png)

![Catatan Review Terisi](screenshots/2025-11-05_23-11-54_customer_onboarding_29_catatan_review_terisi.png)

![Siap Approve](screenshots/2025-11-05_23-11-55_customer_onboarding_30_siap_approve.png)

![Approval Berhasil](screenshots/2025-11-05_23-11-57_customer_onboarding_31_approval_berhasil.png)

![Pesan Sukses](screenshots/2025-11-05_23-11-58_customer_onboarding_32_pesan_sukses_approval.png)

![Queue Setelah Approval](screenshots/2025-11-05_23-11-59_customer_onboarding_33_queue_setelah_approval.png)

![Setelah Logout](screenshots/2025-11-05_23-12-01_customer_onboarding_34_setelah_logout_manager.png)

![Halaman Login](screenshots/2025-11-05_23-12-02_customer_onboarding_35_halaman_login_cs_kembali.png)

![Dashboard Customer Service](screenshots/2025-11-05_23-12-07_customer_onboarding_36_dashboard_cs_login_kembali.png)

![Screenshot](screenshots/2025-11-05_23-12-09_customer_onboarding_37_halaman_pilih_nasabah_untuk_rekening.png)

![Screenshot](screenshots/2025-11-05_23-12-09_customer_onboarding_38_daftar_nasabah_aktif.png)

![Screenshot](screenshots/2025-11-05_23-12-11_customer_onboarding_39_form_pembukaan_rekening.png)

![Screenshot](screenshots/2025-11-05_23-12-12_customer_onboarding_40_produk_dipilih.png)

![Screenshot](screenshots/2025-11-05_23-12-14_customer_onboarding_41_setoran_awal_terisi.png)

![Screenshot](screenshots/2025-11-05_23-12-15_customer_onboarding_42_form_rekening_lengkap.png)

![Screenshot](screenshots/2025-11-05_23-12-17_customer_onboarding_43_rekening_berhasil_disimpan.png)

![Pesan Sukses](screenshots/2025-11-05_23-12-17_customer_onboarding_44_pesan_sukses_rekening_pending.png)

![Setelah Logout](screenshots/2025-11-05_23-12-19_customer_onboarding_45_setelah_logout_cs.png)

![Halaman Login](screenshots/2025-11-05_23-12-20_customer_onboarding_46_halaman_login_manager_kembali.png)

![Dashboard Branch Manager](screenshots/2025-11-05_23-12-25_customer_onboarding_47_dashboard_manager_login_kembali.png)

![Screenshot](screenshots/2025-11-05_23-12-27_customer_onboarding_48_approval_queue_rekening.png)

![Daftar Pending Approval](screenshots/2025-11-05_23-12-27_customer_onboarding_49_daftar_pending_approval_rekening.png)

![Halaman Detail Approval](screenshots/2025-11-05_23-12-29_customer_onboarding_50_halaman_detail_approval_rekening.png)

![Informasi Request](screenshots/2025-11-05_23-12-30_customer_onboarding_51_informasi_request_rekening.png)

![Screenshot](screenshots/2025-11-05_23-12-30_customer_onboarding_52_detail_data_rekening.png)

![Catatan Review Terisi](screenshots/2025-11-05_23-12-31_customer_onboarding_53_catatan_review_rekening_terisi.png)

![Screenshot](screenshots/2025-11-05_23-12-34_customer_onboarding_54_approval_rekening_berhasil.png)

![Pesan Sukses](screenshots/2025-11-05_23-12-34_customer_onboarding_55_pesan_sukses_approval_rekening.png)

![Setelah Logout](screenshots/2025-11-05_23-12-36_customer_onboarding_56_setelah_logout_manager_final.png)

![Halaman Login](screenshots/2025-11-05_23-12-37_customer_onboarding_57_halaman_login_teller.png)

![Screenshot](screenshots/2025-11-05_23-12-38_customer_onboarding_58_username_teller_terisi.png)

![Screenshot](screenshots/2025-11-05_23-12-40_customer_onboarding_59_password_teller_terisi.png)

![Screenshot](screenshots/2025-11-05_23-12-42_customer_onboarding_60_dashboard_teller_berhasil_login.png)

![Screenshot](screenshots/2025-11-05_23-12-44_customer_onboarding_61_halaman_pilih_rekening_deposit.png)

![Screenshot](screenshots/2025-11-05_23-12-45_customer_onboarding_62_daftar_rekening_tersedia.png)

![Screenshot](screenshots/2025-11-05_23-12-47_customer_onboarding_63_form_deposit_terbuka.png)

![Screenshot](screenshots/2025-11-05_23-12-48_customer_onboarding_64_jumlah_setoran_terisi.png)

![Screenshot](screenshots/2025-11-05_23-12-49_customer_onboarding_65_keterangan_setoran_terisi.png)

![Screenshot](screenshots/2025-11-05_23-12-50_customer_onboarding_66_form_deposit_lengkap.png)

![Screenshot](screenshots/2025-11-05_23-12-52_customer_onboarding_67_setoran_berhasil_diproses.png)

![Pesan Sukses](screenshots/2025-11-05_23-12-53_customer_onboarding_68_pesan_sukses_setoran.png)

![Screenshot](screenshots/2025-11-05_23-12-54_customer_onboarding_69_halaman_pilih_rekening_cetak.png)

![Screenshot](screenshots/2025-11-05_23-12-55_customer_onboarding_70_daftar_rekening_cetak.png)

![Screenshot](screenshots/2025-11-05_23-12-57_customer_onboarding_71_tampilan_buku_tabungan.png)

![Screenshot](screenshots/2025-11-05_23-12-57_customer_onboarding_72_detail_mutasi_rekening.png)

![Tutorial Selesai](screenshots/2025-11-05_23-12-58_customer_onboarding_73_tutorial_selesai.png)

**Detail Langkah:**
1. Setelah approve/reject, sistem redirect ke Approval Queue
2. Badge "Pending" akan berkurang 1
3. Approval request yang sudah diproses tidak tampil lagi di queue
4. Request berikutnya (jika ada) dapat diproses

---

## Status Approval

### Status Approval Request

| Status | Deskripsi | Aksi yang Tersedia |
|--------|-----------|--------------------|
| **PENDING** | Request menunggu review dari Branch Manager | Approve / Reject |
| **APPROVED** | Request telah disetujui, entity menjadi ACTIVE | Tidak ada (readonly) |
| **REJECTED** | Request ditolak, entity tetap INACTIVE | Tidak ada (readonly) |

### Status Customer

| Approval Status | Customer Status | Dapat Bertransaksi? | Keterangan |
|-----------------|-----------------|---------------------|------------|
| PENDING_APPROVAL | INACTIVE | ❌ Tidak | Menunggu approval dari Branch Manager |
| APPROVED | ACTIVE | ✅ Ya | Customer aktif dan dapat bertransaksi |
| REJECTED | INACTIVE | ❌ Tidak | Customer ditolak, tidak dapat digunakan |

---

## Tips dan Catatan Penting

### Tips untuk Customer Service:

1. **Kelengkapan Data**
   - Pastikan semua field wajib terisi dengan benar
   - Verifikasi data dengan dokumen asli
   - Gunakan data yang jelas dan akurat

2. **Request Notes**
   - Berikan catatan jika ada hal khusus yang perlu diperhatikan
   - Sebutkan jenis dokumen yang telah diverifikasi
   - Informasikan tujuan pembukaan rekening

3. **Follow Up**
   - Informasikan kepada nasabah bahwa rekening perlu approval
   - Estimasi waktu approval: 1-2 hari kerja
   - Jika reject, koordinasi dengan Manager untuk perbaikan data

### Tips untuk Branch Manager:

1. **Review yang Teliti**
   - Periksa kelengkapan dan keakuratan data
   - Verifikasi kesesuaian dengan regulasi banking
   - Perhatikan red flag: data tidak konsisten, nomor tidak valid, dll

2. **Review Notes yang Jelas**
   - Berikan catatan review yang spesifik
   - Untuk rejection, jelaskan alasan dengan detail
   - Review notes akan menjadi audit trail

3. **SLA Approval**
   - Usahakan approve/reject dalam 1 hari kerja
   - Prioritaskan request yang urgent
   - Koordinasi dengan CS jika ada data yang perlu dikonfirmasi

### Keamanan:

- 🔒 Jangan share password dengan siapapun
- 🔒 Selalu logout setelah selesai
- 🔒 Laporkan aktivitas mencurigakan kepada IT Security
- 🔒 Approval decision tidak dapat diubah setelah diproses

---

## Pemecahan Masalah Umum

### Masalah Customer Service:

**1. Form tidak bisa disimpan**
   - Pastikan semua field wajib (*) sudah terisi
   - Periksa format email (harus valid)
   - Periksa format nomor telepon
   - Pastikan nomor identitas belum terdaftar

**2. Tidak tahu status approval**
   - Lihat customer list, cek kolom "Approval Status"
   - Status PENDING_APPROVAL: masih menunggu manager
   - Status APPROVED: sudah disetujui
   - Status REJECTED: ditolak, lihat rejection reason

### Masalah Branch Manager:

**1. Approval Queue kosong**
   - Refresh halaman (F5)
   - Periksa filter jika ada
   - Mungkin memang tidak ada pending request

**2. Tidak bisa approve/reject**
   - Pastikan login sebagai Branch Manager
   - Periksa authority/permission Anda
   - Untuk reject, wajib isi rejection reason
   - Refresh halaman jika tombol tidak responsif

**3. Error saat approve**
   - Periksa koneksi internet
   - Coba refresh dan ulangi
   - Hubungi IT jika error berlanjut

### Kontak Dukungan:

- 📞 IT Help Desk: ext. 123
- 📞 Supervisor CS: ext. 456
- 📞 Manager Operasional: ext. 789

---

## Informasi Dokumen

**Dibuat oleh:** Sistem Aplikasi Minibank  
**Tanggal:** 05 November 2025  
**Versi:** 2.0  
**Format:** Markdown (.md)  

**Hak Cipta:** © 2025 Aplikasi Minibank - Sistem Perbankan Syariah  

---

*Panduan ini dibuat secara otomatis menggunakan Playwright Test Framework dan Java. Untuk pembaruan atau perbaikan, hubungi tim IT atau maintainer sistem.*

**Generator:** ApprovalWorkflowDocGenerator.java  
**Test Source:** ApprovalWorkflowTutorialTest.java  
**Framework:** Playwright + Java  
