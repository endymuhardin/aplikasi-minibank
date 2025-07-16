# Aplikasi Mini Bank #

## Fitur Aplikasi ##

Aplikasi Mini Bank akan mencakup modul-modul utama sebagai berikut:

* **Modul Pembukaan Rekening:**
    * **Pembukaan Rekening Tabungan:**
        * Input data nasabah (perorangan/badan usaha).
        * Pilihan jenis tabungan.
        * Otomatisasi nomor rekening.
        * Verifikasi data nasabah.
        * Pencetakan formulir pembukaan rekening.
    * **Pembukaan Rekening Deposito:**
        * Input data nasabah.
        * Pilihan jangka waktu deposito.
        * Penentuan suku bunga deposito.
        * Pencetakan bilyet deposito.
* **Modul Transaksi Tunai:**
    * **Setoran Tunai:**
        * Input nomor rekening tujuan.
        * Input nominal setoran.
        * Pencatatan detail transaksi.
        * Otomatisasi *update* saldo rekening.
        * Pencetakan bukti setoran.
* **Modul Cetak Buku Tabungan:**
    * Pencarian rekening nasabah.
    * Tampilan riwayat transaksi.
    * Fungsionalitas pencetakan entri transaksi ke buku tabungan fisik.
    * Sinkronisasi dengan data transaksi terbaru.
* **Modul Pembiayaan Syariah:**
    * **Pembiayaan Murabahah:**
        * Input data nasabah dan objek pembiayaan (barang/jasa).
        * Perhitungan harga pokok, margin keuntungan, dan harga jual.
        * Penentuan jangka waktu dan angsuran.
        * Pencatatan akad pembiayaan.
        * Pencetakan dokumen pembiayaan murabahah.
    * **Pembiayaan Mudharabah:**
        * Input data nasabah dan tujuan pembiayaan (proyek/usaha).
        * Penentuan nisbah bagi hasil (proporsi pembagian keuntungan).
        * Penentuan jangka waktu pembiayaan.
        * Pencatatan akad pembiayaan.
        * Pencetakan dokumen pembiayaan mudharabah.

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
* PostgreSQL 16
* Docker Compose

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