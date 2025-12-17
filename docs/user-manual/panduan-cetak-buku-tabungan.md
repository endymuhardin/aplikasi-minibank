# Panduan Cetak Buku Tabungan (Passbook Printing)

**Aplikasi Minibank - Sistem Perbankan Syariah**

---

**Tanggal Pembuatan:** 17 Desember 2025
**Versi:** 1.0
**Target Pengguna:** Teller
**Printer:** Epson PLQ-20 Passbook Printer
**Status:** Aktif

---

## Daftar Isi

1. [Gambaran Umum](#gambaran-umum)
2. [Prasyarat](#prasyarat)
3. [Bagian 1: Setup dan Instalasi Printer](#bagian-1-setup-dan-instalasi-printer)
   1. [Spesifikasi Printer Epson PLQ-20](#spesifikasi-printer-epson-plq-20)
   2. [Instalasi Hardware](#instalasi-hardware)
   3. [Konfigurasi Browser](#konfigurasi-browser)
   4. [Test Koneksi Printer](#test-koneksi-printer)
4. [Bagian 2: Inisialisasi Buku Tabungan](#bagian-2-inisialisasi-buku-tabungan)
   1. [Buku Tabungan Baru (Kosong)](#buku-tabungan-baru-kosong)
   2. [Buku Tabungan Lama (Sudah Terisi)](#buku-tabungan-lama-sudah-terisi)
5. [Bagian 3: Proses Cetak Transaksi](#bagian-3-proses-cetak-transaksi)
   1. [Akses Menu Cetak Buku Tabungan](#akses-menu-cetak-buku-tabungan)
   2. [Cari Rekening Nasabah](#cari-rekening-nasabah)
   3. [Cetak ke Printer PLQ-20](#cetak-ke-printer-plq-20)
   4. [Verifikasi Hasil Cetakan](#verifikasi-hasil-cetakan)
6. [Troubleshooting](#troubleshooting)
7. [Tips dan Best Practices](#tips-dan-best-practices)

---

## Gambaran Umum

Panduan ini menjelaskan proses **pencetakan buku tabungan** menggunakan printer Epson PLQ-20 dengan koneksi **Web Serial API** langsung dari browser. Sistem ini memungkinkan Teller untuk mencetak transaksi nasabah secara otomatis ke buku tabungan dot matrix tanpa memerlukan driver printer atau software tambahan.

**Keunggulan Sistem:**
- ✅ Tidak memerlukan instalasi driver printer
- ✅ Koneksi langsung via USB dengan Web Serial API
- ✅ Otomatis melanjutkan dari baris terakhir yang dicetak
- ✅ Support untuk buku tabungan baru dan lama
- ✅ Tracking lengkap setiap pencetakan
- ✅ Format ESC/P2 untuk hasil cetakan berkualitas

**Waktu Estimasi:**
- Setup awal printer: 10-15 menit (sekali saja)
- Inisialisasi buku tabungan: 2-3 menit
- Cetak transaksi: 30-60 detik per buku

---

## Prasyarat

### Perangkat Keras
- ✅ **Printer Epson PLQ-20** (Passbook Printer)
- ✅ **Kabel USB** untuk koneksi printer ke komputer
- ✅ **Komputer/Laptop** dengan port USB
- ✅ **Buku tabungan** nasabah (ukuran standar passbook)

### Perangkat Lunak
- ✅ **Browser:** Google Chrome 89+ atau Microsoft Edge 89+
- ✅ **Akses:** Login sebagai **Teller**
- ✅ **Koneksi:** Akses ke aplikasi Minibank

### Akses dan Izin
- ✅ Login dengan username dan password **Teller**
- ✅ Izin akses menu "Passbook Printing"
- ✅ Izin untuk inisialisasi dan cetak buku tabungan

---

## Bagian 1: Setup dan Instalasi Printer

### Spesifikasi Printer Epson PLQ-20

**Epson PLQ-20** adalah printer dot matrix khusus untuk mencetak buku tabungan (passbook) dengan fitur:

- **Teknologi:** 24-pin dot matrix
- **Kecepatan:** 480 karakter per detik
- **Ukuran Kertas:** Passbook (ukuran buku tabungan standar)
- **Koneksi:** USB 2.0
- **Format Cetak:** ESC/P2 (Epson Standard Code for Printers)
- **Auto Loading:** Ya (otomatis deteksi buku tabungan)

### Instalasi Hardware

#### Langkah 1: Sambungkan Printer ke Komputer

1. **Nyalakan Printer**
   - Pastikan printer dalam kondisi OFF
   - Colokkan kabel power ke listrik
   - Tekan tombol power untuk menyalakan printer

2. **Hubungkan USB**
   - Ambil kabel USB yang disediakan
   - Colokkan satu ujung ke port USB printer (belakang printer)
   - Colokkan ujung lainnya ke port USB komputer/laptop

3. **Tunggu Deteksi Otomatis**
   - Windows/Mac akan otomatis mendeteksi printer
   - **TIDAK PERLU** menginstal driver apapun
   - Printer siap digunakan dengan Web Serial API

#### Langkah 2: Test Printer Hardware

1. **Print Self Test**
   - Matikan printer
   - Tekan dan tahan tombol **LOAD/EJECT**
   - Nyalakan printer sambil tetap menahan tombol
   - Lepas tombol setelah printer mulai print
   - Hasil: Printer akan mencetak halaman test pattern

2. **Verifikasi Printer Head**
   - Pastikan semua baris tercetak dengan jelas
   - Tidak ada garis putus-putus atau blank
   - Jika ada masalah, lakukan head cleaning

### Konfigurasi Browser

Sistem menggunakan **Web Serial API** yang hanya didukung oleh browser tertentu.

#### Browser yang Didukung

| Browser | Versi Minimum | Status |
|---------|---------------|--------|
| **Google Chrome** | 89+ | ✅ Recommended |
| **Microsoft Edge** | 89+ | ✅ Recommended |
| Firefox | Any | ❌ Not Supported |
| Safari | Any | ❌ Not Supported |

#### Langkah Konfigurasi Chrome/Edge

1. **Pastikan Browser Terupdate**
   ```
   1. Buka Chrome/Edge
   2. Klik menu (⋮) → Help → About Chrome/Edge
   3. Pastikan versi ≥ 89
   4. Update jika diperlukan
   ```

2. **Aktifkan Web Serial API** (Biasanya sudah aktif secara default)
   ```
   1. Buka chrome://flags atau edge://flags
   2. Cari "Experimental Web Platform features"
   3. Pastikan status: Enabled
   4. Restart browser
   ```

3. **Izinkan Akses Serial Port**
   - Browser akan meminta izin saat pertama kali koneksi
   - Klik **"Allow"** saat diminta
   - Pilih printer Epson PLQ-20 dari daftar

### Test Koneksi Printer

Setelah hardware dan browser siap, test koneksi printer:

#### Langkah Test Koneksi

1. **Login ke Aplikasi**
   - Buka aplikasi Minibank di Chrome/Edge
   - Login dengan username **teller1** (atau teller lain)
   - Password: **minibank123**

2. **Akses Menu Passbook**
   - Klik menu **"Passbook Printing"**
   - Pilih submenu **"Select Account"**

3. **Pilih Rekening untuk Test**
   - Cari rekening aktif dari daftar
   - Klik tombol **"PLQ-20"** (tombol biru)

4. **Klik "Connect to Printer"**
   - Halaman akan menampilkan tombol **"Connect to Printer"**
   - Klik tombol tersebut
   - Browser akan menampilkan dialog pilih printer

5. **Pilih Epson PLQ-20**
   - Dari daftar yang muncul, pilih **"Epson PLQ-20"** atau **"USB Serial Device"**
   - Klik **"Connect"**

6. **Verifikasi Koneksi Berhasil**
   - Status berubah menjadi: **"Connected to: Epson PLQ-20"**
   - Tombol cetak menjadi aktif (tidak disabled)
   - Jika gagal, lihat bagian [Troubleshooting](#troubleshooting)

---

## Bagian 2: Inisialisasi Buku Tabungan

Setelah printer terhubung, Teller perlu menginisialisasi buku tabungan. Ada dua skenario:

### Buku Tabungan Baru (Kosong)

Untuk nasabah yang **baru membuka rekening** atau **mengganti buku tabungan baru**.

#### Langkah-langkah

1. **Akses Menu Passbook Printing**
   - Login sebagai Teller
   - Klik menu **"Passbook Printing"**

2. **Pilih Rekening Nasabah**
   - Gunakan fitur search untuk mencari rekening
   - Masukkan **Account Number** atau **Nama Nasabah**
   - Klik tombol **"Search"**

3. **Klik "PLQ-20" untuk Direct Print**
   - Dari hasil pencarian, klik tombol **"PLQ-20"** (tombol biru)
   - Sistem akan otomatis membuat passbook record baru
   - Status awal: **Page 1, Line 0** (belum ada yang dicetak)

4. **Tidak Perlu Inisialisasi Manual**
   - Untuk buku baru, sistem otomatis mulai dari halaman 1, baris 0
   - Langsung lanjut ke proses cetak transaksi

5. **Lanjut ke Proses Cetak**
   - Lihat [Bagian 3: Proses Cetak Transaksi](#bagian-3-proses-cetak-transaksi)

### Buku Tabungan Lama (Sudah Terisi)

Untuk nasabah yang **sudah memiliki buku tabungan** yang sebagian halaman sudah terisi.

**Skenario:** Nasabah datang dengan buku yang sudah ada cetakan transaksi sebelumnya, dan ingin mencetak transaksi baru. Sistem perlu tahu posisi terakhir yang sudah dicetak.

#### Langkah-langkah Inisialisasi

1. **Akses Menu Passbook Printing**
   - Login sebagai Teller
   - Klik menu **"Passbook Printing"** → **"Select Account"**

2. **Cari Rekening Nasabah**
   - Masukkan **Account Number** atau **Nama Nasabah**
   - Klik **"Search"**

3. **Klik Tombol "Initialize"** (Tombol Orange)
   - Dari hasil pencarian, klik tombol **"Initialize"**
   - Sistem akan membuka halaman **"Initialize Passbook"**

4. **Lihat Informasi Rekening**
   - Sistem menampilkan:
     - Account Number: A2000001
     - Account Name: Tabungan Ahmad
     - Customer: Ahmad Suharto
     - Balance: IDR 1,500,000.00

5. **Periksa Buku Tabungan Fisik**
   - **Penting:** Ambil buku tabungan nasabah
   - Lihat halaman terakhir yang terisi
   - Catat:
     - **Nomor Halaman** (contoh: Halaman 1)
     - **Baris Terakhir** yang tercetak (contoh: Baris 5)
     - **Transaksi Terakhir** yang tercetak (opsional, untuk validasi)

6. **Isi Form Inisialisasi**

   **Field 1: Current Page Number** *(wajib)*
   - Label: "Current Page Number *"
   - Masukkan: Nomor halaman saat ini (1, 2, 3, dst)
   - Contoh: **1**
   - Validasi: Minimal 1

   **Field 2: Last Printed Line** *(wajib)*
   - Label: "Last Printed Line *"
   - Masukkan: Nomor baris terakhir yang tercetak (0-30)
   - Contoh: **5** (jika ada 5 baris transaksi tercetak)
   - Validasi: 0-30 (maksimal 30 baris per halaman)

   **Field 3: Last Printed Transaction** *(opsional)*
   - Label: "Last Printed Transaction"
   - Dropdown berisi semua transaksi rekening ini
   - Pilih transaksi terakhir yang tercetak di buku
   - **Tips:** Cocokkan dengan tanggal/jumlah di buku fisik
   - Jika tidak yakin, pilih **"-- None (Start from beginning) --"**

7. **Klik "Initialize Passbook"**
   - Tombol: **"Initialize Passbook"** (biru)
   - Sistem akan memvalidasi input
   - Jika valid, redirect ke halaman **Direct Print**

8. **Verifikasi Success Message**
   - Halaman menampilkan pesan sukses:
     ```
     Passbook initialized successfully!
     Passbook Number: PB0000001
     Current Page: 1
     Last Line: 5
     Remaining Lines: 15
     ```
   - Sistem siap untuk mencetak transaksi baru

#### Contoh Kasus Inisialisasi

**Skenario A: Buku Halaman 1, Sudah Ada 5 Transaksi**
- Current Page Number: **1**
- Last Printed Line: **5**
- Last Printed Transaction: Pilih transaksi ke-5 dari dropdown
- **Hasil:** Sistem akan mencetak transaksi ke-6 dan seterusnya di baris 6-20

**Skenario B: Buku Halaman 2, Sudah Ada 12 Transaksi**
- Current Page Number: **2**
- Last Printed Line: **12**
- Last Printed Transaction: Pilih transaksi ke-12
- **Hasil:** Sistem akan mencetak transaksi baru di baris 13-20 halaman 2

**Skenario C: Halaman Penuh, Perlu Halaman Baru**
- Current Page Number: **2**
- Last Printed Line: **20**
- Last Printed Transaction: Pilih transaksi terakhir
- **Hasil:** Sistem otomatis pindah ke halaman 3 untuk transaksi baru

---

## Bagian 3: Proses Cetak Transaksi

Setelah printer terkoneksi dan buku tabungan diinisialisasi, Teller dapat mencetak transaksi.

### Akses Menu Cetak Buku Tabungan

#### Langkah 1: Navigasi ke Menu

1. **Login sebagai Teller**
   - Buka aplikasi Minibank
   - Username: teller1, teller2, atau teller3
   - Password: **minibank123**

2. **Klik Menu "Passbook Printing"**
   - Dari sidebar atau menu utama
   - Pilih **"Passbook Printing"** → **"Select Account"**

3. **Halaman Select Account Terbuka**
   - Menampilkan daftar rekening aktif
   - Search box untuk pencarian
   - Tombol aksi: Preview, Initialize, PLQ-20, Browser

### Cari Rekening Nasabah

#### Metode Pencarian

**Cara 1: Cari dengan Account Number**
- Masukkan nomor rekening di search box (contoh: **A2000001**)
- Klik **"Search"**
- Hasil akan menampilkan rekening yang cocok

**Cara 2: Cari dengan Nama**
- Masukkan nama nasabah di search box (contoh: **Ahmad**)
- Klik **"Search"**
- Hasil menampilkan semua rekening atas nama Ahmad

**Cara 3: Browse All**
- Klik **"Clear"** untuk menampilkan semua rekening
- Scroll untuk mencari rekening yang diinginkan

#### Informasi yang Ditampilkan

Untuk setiap rekening, sistem menampilkan:
- **Account Number:** A2000001
- **Account Name:** Tabungan Ahmad Suharto
- **Customer:** Ahmad Suharto (C1000001)
- **Product:** Tabungan Wadiah (TABUNGAN_WADIAH)
- **Balance:** IDR 1,500,000.00
- **Opened Date:** 15/11/2025

### Cetak ke Printer PLQ-20

#### Langkah-langkah Detail

**Langkah 1: Klik Tombol "PLQ-20"**
- Dari daftar rekening, klik tombol **"PLQ-20"** (tombol biru)
- Sistem redirect ke halaman **"Direct Passbook Printing"**

**Langkah 2: Verifikasi Informasi di Halaman Direct Print**

Halaman menampilkan:

1. **Account Details** (Panel biru)
   - Account Number: A2000001
   - Account Name: Tabungan Ahmad Suharto
   - Product: Tabungan Wadiah

2. **Customer Details** (Panel biru)
   - Customer: Ahmad Suharto
   - Customer Number: C1000001
   - Current Balance: IDR 1,500,000.00

3. **Passbook Information** (Panel hijau)
   - Passbook Number: PB0000001
   - Current Page: 1
   - Last Printed Line: 5
   - Remaining Lines: 15 (dari 20)
   - Last Print Date: 16/12/2025 14:30

4. **Unprinted Transactions** (Panel kuning)
   - Menampilkan jumlah transaksi yang belum dicetak
   - Contoh: "3 unprinted transactions available"

**Langkah 3: Connect to Printer**

1. **Klik "Connect to Printer"**
   - Tombol hijau di bagian atas
   - Browser akan menampilkan dialog

2. **Pilih Printer dari Daftar**
   - Pilih **"Epson PLQ-20"** atau **"USB Serial Device"**
   - Klik **"Connect"**

3. **Verifikasi Status Koneksi**
   - Status berubah: **"Connected to: Epson PLQ-20"**
   - Tombol **"Print to Passbook"** menjadi aktif

**Langkah 4: Siapkan Buku Tabungan**

1. **Masukkan Buku ke Printer**
   - Buka cover printer Epson PLQ-20
   - Masukkan buku tabungan dari atas
   - Posisikan buku hingga terdengar bunyi klik (auto loading)
   - Pastikan halaman yang akan dicetak menghadap roller printer

2. **Verifikasi Posisi Buku**
   - Pastikan buku lurus dan tidak miring
   - Pastikan halaman yang akan dicetak terlihat di viewing window
   - Buku harus masuk hingga posisi yang benar

**Langkah 5: Preview Transaksi yang Akan Dicetak**

Tabel menampilkan transaksi yang akan dicetak:

| Date | Transaction # | Description | Debit | Credit | Balance |
|------|---------------|-------------|-------|--------|---------|
| 16/12/25 | TXN0000123 | Setoran Tunai | - | 500,000.00 | 1,500,000.00 |
| 16/12/25 | TXN0000124 | Tarikan Tunai | 200,000.00 | - | 1,300,000.00 |
| 17/12/25 | TXN0000125 | Transfer Masuk | - | 300,000.00 | 1,600,000.00 |

**Langkah 6: Klik "Print to Passbook"**

1. **Klik Tombol Print**
   - Tombol hijau: **"Print to Passbook"**
   - Sistem mulai mengirim data ke printer

2. **Monitor Progress**
   - Sistem menampilkan progress bar
   - Status: "Sending data to printer..."
   - Progress: 0% → 100%

3. **Tunggu Proses Cetak Selesai**
   - Printer akan mulai mencetak
   - Bunyi dot matrix printing
   - Waktu: ~10-20 detik tergantung jumlah transaksi

**Langkah 7: Verifikasi Hasil Cetakan**

1. **Keluarkan Buku Tabungan**
   - Tekan tombol **EJECT** pada printer (jika perlu)
   - Atau tarik buku perlahan setelah printing selesai

2. **Periksa Hasil Cetakan**
   - Verifikasi semua transaksi tercetak dengan jelas
   - Cocokkan tanggal, jumlah, dan saldo
   - Pastikan tidak ada smudge atau tinta blur

3. **Konfirmasi di Sistem**
   - Sistem otomatis mencatat hasil cetakan
   - Passbook state terupdate:
     - Last Printed Line: 8 (dari 5 + 3 transaksi)
     - Remaining Lines: 12
     - Last Print Date: 17/12/2025 10:15

**Langkah 8: Kembalikan Buku ke Nasabah**
   - Serahkan buku tabungan yang sudah dicetak
   - Minta nasabah untuk memeriksa
   - Selesai

### Verifikasi Hasil Cetakan

#### Checklist Kualitas Cetakan

✅ **Layout dan Format**
- Kolom tanggal, deskripsi, debit, kredit, saldo sejajar
- Baris tidak terpotong atau overlap
- Margin kiri/kanan sesuai

✅ **Keterbacaan**
- Angka dan huruf jelas terbaca
- Tidak ada karakter hilang atau blur
- Tinta tidak terlalu terang atau terlalu gelap

✅ **Akurasi Data**
- Tanggal transaksi benar
- Jumlah debit/kredit sesuai
- Saldo akhir cocok dengan sistem
- Deskripsi transaksi lengkap

✅ **Posisi Baris**
- Transaksi baru tercetak di baris berikutnya
- Tidak overlap dengan transaksi lama
- Spacing antar baris konsisten

#### Tindakan Jika Ada Masalah

**Masalah: Cetakan Blur atau Pudar**
- Cek ribbon printer (mungkin perlu diganti)
- Bersihkan print head
- Adjust darkness setting di printer

**Masalah: Posisi Tidak Sejajar**
- Re-initialize passbook dengan nomor baris yang benar
- Pastikan buku masuk dengan posisi lurus

**Masalah: Transaksi Tidak Lengkap**
- Cek log error di sistem
- Cek koneksi USB tidak terlepas
- Print ulang jika diperlukan

---

## Troubleshooting

### Masalah Koneksi Printer

#### ❌ "Web Serial API not supported"

**Penyebab:**
- Browser tidak mendukung Web Serial API
- Versi browser terlalu lama

**Solusi:**
1. Update browser ke versi terbaru (Chrome 89+ atau Edge 89+)
2. Gunakan Google Chrome atau Microsoft Edge (bukan Firefox/Safari)
3. Restart browser setelah update

#### ❌ "Failed to open serial port"

**Penyebab:**
- Port USB sedang digunakan oleh aplikasi lain
- Kabel USB tidak tercolok dengan benar
- Printer tidak menyala

**Solusi:**
1. Pastikan printer menyala
2. Cek kabel USB terpasang dengan benar
3. Cabut dan colok ulang kabel USB
4. Tutup aplikasi lain yang menggunakan printer
5. Restart komputer jika masih gagal

#### ❌ "Printer not found in device list"

**Penyebab:**
- Driver USB belum terdeteksi sistem operasi
- Port USB rusak

**Solusi:**
1. Coba port USB yang berbeda
2. Tunggu beberapa detik setelah colok USB (Windows perlu waktu deteksi)
3. Cek Device Manager (Windows) atau System Information (Mac) apakah printer terdeteksi
4. Restart komputer

### Masalah Inisialisasi

#### ❌ "Current page must be at least 1"

**Penyebab:**
- Input nomor halaman 0 atau negatif

**Solusi:**
- Masukkan nomor halaman minimal 1

#### ❌ "Last printed line must be between 0 and 30"

**Penyebab:**
- Input nomor baris di luar range

**Solusi:**
- Masukkan nomor baris 0-30
- Pastikan sesuai dengan buku fisik

#### ❌ "Transaction does not belong to this account"

**Penyebab:**
- Memilih transaksi dari rekening lain

**Solusi:**
- Pilih transaksi yang benar dari dropdown
- Atau pilih "None" jika tidak yakin

### Masalah Pencetakan

#### ❌ "No new transactions to print"

**Penyebab:**
- Semua transaksi sudah dicetak sebelumnya
- Tidak ada transaksi baru sejak pencetakan terakhir

**Solusi:**
- Informasikan ke nasabah bahwa tidak ada transaksi baru
- Buku tabungan sudah up to date

#### ❌ Printer mencetak di posisi yang salah

**Penyebab:**
- Inisialisasi nomor baris salah
- Buku tidak masuk dengan benar

**Solusi:**
1. Keluarkan buku
2. Re-initialize dengan nomor baris yang benar
3. Masukkan buku kembali dengan posisi lurus

#### ❌ Cetakan tidak muncul sama sekali

**Penyebab:**
- Ribbon habis atau tidak terpasang
- Print head kotor
- Kabel data terlepas

**Solusi:**
1. Cek ribbon printer (ganti jika habis)
2. Pastikan ribbon terpasang dengan benar
3. Lakukan print self test
4. Bersihkan print head jika perlu

### Masalah Browser

#### ❌ Browser hang atau freeze saat koneksi

**Penyebab:**
- Browser kehabisan memory
- Terlalu banyak tab terbuka

**Solusi:**
1. Tutup tab yang tidak perlu
2. Clear browser cache
3. Restart browser
4. Update browser ke versi terbaru

#### ❌ "Permission denied to access serial port"

**Penyebab:**
- User menolak permission saat diminta
- Browser settings memblokir akses

**Solusi:**
1. Refresh halaman
2. Klik "Connect to Printer" lagi
3. Klik "Allow" saat browser meminta izin
4. Cek browser settings → Site Settings → Permissions → Serial ports

---

## Tips dan Best Practices

### Perawatan Printer

✅ **Harian**
- Bersihkan permukaan printer dari debu
- Pastikan printer cover tertutup saat tidak digunakan
- Matikan printer jika tidak digunakan > 1 jam

✅ **Mingguan**
- Bersihkan roller dengan kain lembut
- Cek ribbon (ganti jika mulai pudar)
- Test print untuk memastikan kualitas

✅ **Bulanan**
- Lakukan head cleaning menggunakan cleaning kit
- Vacuum debu di dalam printer
- Cek kabel USB tidak longgar atau rusak

### Operasional Teller

✅ **Sebelum Mulai Hari**
- Test koneksi printer di awal shift
- Print test page untuk cek kualitas
- Siapkan ribbon cadangan

✅ **Saat Melayani Nasabah**
- Cek kondisi buku tabungan (rusak, penuh, dll)
- Verifikasi nomor rekening dengan ID nasabah
- Pastikan transaksi tercetak dengan jelas sebelum dikembalikan

✅ **Akhir Hari**
- Catat jumlah passbook yang dicetak
- Laporkan jika ada masalah printer
- Matikan printer dengan benar (jangan cabut langsung)

### Keamanan dan Compliance

✅ **Data Privacy**
- Jangan tinggalkan buku tabungan nasabah tanpa pengawasan
- Pastikan layar tidak terlihat dari area publik
- Logout setelah selesai melayani nasabah

✅ **Audit Trail**
- Sistem otomatis mencatat setiap pencetakan
- Branch Manager dapat review print history
- Laporkan anomali atau error ke supervisor

✅ **Backup**
- Sistem otomatis backup data pencetakan
- Jika printer rusak, data tetap aman di sistem
- Bisa print ulang kapanpun diperlukan

### Efisiensi Operasional

✅ **Batch Printing**
- Untuk nasabah dengan banyak transaksi, sistem otomatis handle multiple pages
- Sistem akan memberi tahu jika perlu ganti halaman

✅ **Queue Management**
- Prioritaskan nasabah dengan transaksi sedikit
- Untuk banyak transaksi, estimasikan waktu ke nasabah

✅ **Communication**
- Jelaskan proses ke nasabah ("Mohon tunggu, sedang mencetak buku tabungan")
- Minta nasabah memeriksa hasil cetakan sebelum pulang

---

## Lampiran

### Format Cetakan Buku Tabungan

```
=============================================================
DATE        DESCRIPTION          DEBIT        CREDIT    BALANCE
-------------------------------------------------------------
16/12/25    Setoran Tunai                  500,000.00  1,500,000.00
16/12/25    Tarikan Tunai      200,000.00                1,300,000.00
17/12/25    Transfer Masuk                 300,000.00  1,600,000.00
=============================================================
```

### Spesifikasi Teknis

**ESC/P2 Commands Used:**
- `ESC @` - Initialize printer
- `ESC t` - Select character table
- `ESC x` - Set print quality
- `LF` - Line feed
- `CR` - Carriage return

**Print Parameters:**
- Font: 12 CPI (characters per inch)
- Line spacing: 1/6 inch
- Character set: PC437 (International)
- Lines per page: 20 (configurable)

### Kontak Support

**Technical Support:**
- Email: support@minibank.co.id
- Phone: (021) 1234-5678
- Hours: Senin-Jumat, 08:00-17:00 WIB

**Hardware Service (Epson):**
- Epson Service Center
- Website: www.epson.co.id/support

---

*Panduan ini dibuat pada: 17 Desember 2025*
*Versi: 1.0*
*Status: Aktif*

🤖 Generated with [Claude Code](https://claude.com/claude-code)
