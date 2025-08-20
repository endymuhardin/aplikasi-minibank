# Panduan Pengguna: Cetak Rekening Koran PDF

## Gambaran Umum

Fitur Cetak Rekening Koran PDF memungkinkan pengguna untuk mengunduh mutasi rekening dalam format PDF yang profesional dan dapat dicetak. Fitur ini tersedia untuk semua peran pengguna (Customer Service, Teller, Branch Manager) dengan kontrol akses berbasis cabang.

## Akses dan Otoritas

### Hak Akses Berdasarkan Peran

- **Customer Service (CS)**: Dapat mencetak rekening koran untuk semua nasabah di cabang yang sama
- **Teller**: Dapat mencetak rekening koran untuk keperluan transaksi dan layanan nasabah
- **Branch Manager**: Akses penuh untuk semua rekening di cabang yang dikelola
- **System Admin**: Akses lintas cabang untuk keperluan administrasi

### Kontrol Keamanan

- Akses dibatasi berdasarkan cabang (branch-based access control)
- Audit log otomatis untuk setiap generasi rekening koran
- Validasi otoritas pengguna sebelum mengakses data rekening

## Cara Menggunakan Fitur

### 1. Akses Melalui Web Interface

#### Langkah-langkah:

1. **Login ke Sistem**
   - Gunakan kredensial pengguna (CS/Teller/Manager)
   - Pastikan user terdaftar di cabang yang sesuai

2. **Navigasi ke Account List**
   - Akses URL: `http://localhost:8080/account/list`
   - Atau melalui menu navigasi "Account Management"

3. **Pilih Account**
   - Cari account berdasarkan nomor rekening atau nama nasabah
   - Gunakan fitur pencarian untuk menemukan account dengan cepat
   - Klik account yang diinginkan

4. **Akses Statement Generator**
   - Klik tombol/link "Statement" pada account yang dipilih
   - Atau akses langsung: `http://localhost:8080/account/{accountId}/statement`

5. **Set Parameter**
   - **Tanggal Mulai**: Pilih tanggal awal periode (default: 3 bulan yang lalu)
   - **Tanggal Akhir**: Pilih tanggal akhir periode (default: hari ini)
   - Pastikan tanggal akhir tidak lebih awal dari tanggal mulai

6. **Generate dan Download**
   - Klik tombol "Generate PDF"
   - PDF akan diunduh otomatis ke folder Download
   - Nama file: `statement_{nomorRekening}_{tanggalMulai}_to_{tanggalAkhir}.pdf`

### 2. Menggunakan REST API

#### POST Request dengan JSON Body

```bash
curl -X POST http://localhost:8080/api/accounts/statement/pdf \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC0000001",
    "startDate": "2024-01-01", 
    "endDate": "2024-01-31"
  }' \
  --output statement.pdf
```

#### GET Request dengan Parameters

```bash
curl "http://localhost:8080/api/accounts/statement/pdf?accountNumber=ACC0000001&startDate=2024-01-01&endDate=2024-01-31" \
  --output statement.pdf
```

#### Menggunakan Account ID

```bash
curl -X POST http://localhost:8080/api/accounts/statement/pdf \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "123e4567-e89b-12d3-a456-426614174000",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
  }' \
  --output statement.pdf
```

## Format PDF dan Konten

### Struktur Dokumen

1. **Header**
   - Logo dan nama bank: "MINIBANK SYARIAH"
   - Judul: "REKENING KORAN / ACCOUNT STATEMENT"
   - Periode laporan: "Periode: 01/01/2024 s/d 31/01/2024"

2. **Informasi Rekening**
   - Nomor Rekening
   - Nama Rekening
   - Nama Nasabah (firstName + lastName untuk personal, companyName untuk korporat)
   - Jenis Produk (dari tabel products)
   - Saldo Saat Ini (dengan format IDR)

3. **Tabel Mutasi Rekening**
   - **Kolom**: Tanggal | No. Transaksi | Keterangan | Debet | Kredit | Saldo
   - **Format Tanggal**: dd/MM/yyyy HH:mm
   - **Format Mata Uang**: #,##0.00 (contoh: 1.000.000,00)
   - **Keterangan**: Jenis transaksi + deskripsi + channel
   - **Saldo**: Running balance setelah setiap transaksi

4. **Ringkasan**
   - Total Debet (jumlah semua penarikan dalam periode)
   - Total Kredit (jumlah semua setoran dalam periode)
   - Jumlah Transaksi (hitungan total transaksi)

5. **Footer**
   - Timestamp pencetakan: "Dicetak pada: 20/08/2024"

### Contoh Format Keterangan Transaksi

- **SETORAN TUNAI - Setoran dari teller (TELLER)**
- **PENARIKAN TUNAI - Withdrawal ATM (ATM)**
- **TRANSFER MASUK - Transfer dari rekening lain (ONLINE)**
- **BIAYA ADMINISTRASI - Monthly admin fee (TRANSFER)**

### Spesifikasi Teknis

- **Format File**: PDF (Portable Document Format)
- **Ukuran Kertas**: A4
- **Font**: Helvetica family (berbagai ukuran)
- **Kompatibilitas**: Universal - dapat dibuka di semua PDF reader
- **Ukuran File**: Umumnya < 2MB untuk 50 transaksi

## Validasi dan Error Handling

### Validasi Input

1. **Account Validation**
   - Account harus ada dalam database
   - User harus memiliki akses ke account tersebut (branch-based)
   - Account tidak boleh dalam status CLOSED

2. **Date Range Validation**
   - Start date tidak boleh lebih besar dari end date
   - End date tidak boleh di masa depan
   - Range maksimum yang disarankan: 2 tahun

3. **Parameter Validation**
   - Salah satu dari accountId atau accountNumber harus disediakan
   - Format tanggal harus valid: yyyy-MM-dd

### Error Messages

- **Account not found**: "Account tidak ditemukan atau tidak dapat diakses"
- **Invalid date range**: "Tanggal akhir harus lebih besar dari tanggal mulai"
- **Unauthorized access**: "Anda tidak memiliki akses ke rekening ini"
- **PDF generation failed**: "Gagal membuat PDF, silakan coba lagi"

## Skenario Penggunaan

### 1. Customer Service - Layanan Nasabah
**Situasi**: Nasabah meminta rekening koran untuk keperluan administrasi

**Langkah**:
1. Login sebagai CS (cs1/cs2/cs3)
2. Cari account nasabah berdasarkan nomor rekening atau nama
3. Pilih periode sesuai permintaan nasabah
4. Generate PDF dan print untuk diserahkan ke nasabah

### 2. Teller - Verifikasi Transaksi
**Situasi**: Nasabah ingin memverifikasi riwayat transaksi tertentu

**Langkah**:
1. Login sebagai Teller
2. Akses account nasabah yang bersangkutan
3. Set rentang tanggal spesifik untuk periode yang ingin diperiksa
4. Review PDF bersama nasabah untuk konfirmasi transaksi

### 3. Branch Manager - Monitoring dan Audit
**Situasi**: Perlu review riwayat transaksi untuk keperluan audit internal

**Langkah**:
1. Login sebagai Branch Manager
2. Generate rekening koran untuk multiple accounts jika diperlukan
3. Gunakan untuk analisis pola transaksi dan monitoring

### 4. Integrasi dengan Aplikasi Lain
**Situasi**: Sistem lain perlu mengambil data rekening koran

**Implementasi**:
```python
import requests

# Python example
def get_account_statement(account_number, start_date, end_date):
    url = "http://localhost:8080/api/accounts/statement/pdf"
    payload = {
        "accountNumber": account_number,
        "startDate": start_date,
        "endDate": end_date
    }
    
    response = requests.post(url, json=payload)
    
    if response.status_code == 200:
        with open(f"statement_{account_number}.pdf", "wb") as f:
            f.write(response.content)
        return True
    else:
        print(f"Error: {response.status_code}")
        return False
```

## Tips dan Best Practices

### 1. Performance Optimization
- Untuk rekening dengan transaksi banyak, gunakan periode yang lebih pendek
- Hindari generate statement untuk periode > 1 tahun secara bersamaan
- Monitor ukuran file PDF (maksimal yang disarankan: 10MB)

### 2. User Experience
- Berikan feedback visual saat PDF sedang di-generate (loading indicator)
- Validasi input di sisi client sebelum mengirim request
- Gunakan nama file yang deskriptif untuk kemudahan identifikasi

### 3. Keamanan
- Selalu logout setelah selesai menggunakan aplikasi
- Jangan share link direct ke statement tanpa autentikasi
- Pastikan PDF disimpan di lokasi yang aman

### 4. Troubleshooting
- Jika PDF tidak ter-download, periksa browser popup blocker
- Untuk file besar, tunggu hingga proses selesai (indikator loading)
- Jika terjadi error, coba dengan rentang tanggal yang lebih kecil

## FAQ (Frequently Asked Questions)

### Q: Apakah bisa generate statement untuk semua account sekaligus?
**A**: Saat ini fitur hanya mendukung satu account per request. Untuk multiple accounts, perlu generate satu per satu.

### Q: Berapa lama data transaksi disimpan dalam sistem?
**A**: Data transaksi disimpan secara permanen untuk keperluan audit dan regulasi perbankan.

### Q: Apakah bisa mengatur format mata uang selain IDR?
**A**: Saat ini sistem hanya mendukung IDR dengan format Indonesia (menggunakan koma sebagai pemisah desimal).

### Q: Bagaimana jika account tidak memiliki transaksi dalam periode yang dipilih?
**A**: PDF tetap akan di-generate dengan pesan "Tidak ada transaksi dalam periode ini" dan menampilkan saldo awal yang sama dengan saldo akhir.

### Q: Apakah ada batasan maksimum jumlah transaksi dalam satu statement?
**A**: Tidak ada batasan hard limit, namun untuk performance disarankan maksimal 1000 transaksi per statement.

### Q: Bisa tidak mengakses statement account dari cabang lain?
**A**: Tidak, akses dibatasi berdasarkan cabang kecuali untuk System Admin yang memiliki akses lintas cabang.

## Kontak dan Dukungan

Untuk pertanyaan teknis atau masalah dalam penggunaan fitur:

- **Technical Support**: Hubungi IT Support internal
- **User Training**: Koordinasi dengan Branch Manager
- **Feature Request**: Submit melalui sistem ticketing internal

## Changelog

### Version 1.0.0 (August 2024)
- Initial release fitur Account Statement PDF
- Support untuk personal dan corporate customers
- REST API endpoints tersedia
- Web interface terintegrasi dengan account management
- Branch-based access control implemented
- Indonesian language support dengan format IDR

---

*Dokumen ini adalah bagian dari sistem dokumentasi Aplikasi Minibank Syariah. Untuk informasi lebih lengkap, lihat dokumentasi utama di `/docs/user-guide.md`*