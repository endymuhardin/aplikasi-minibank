package id.ac.tazkia.minibank.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generator for Indonesian approval workflow documentation based on Playwright test screenshots.
 *
 * This utility creates comprehensive user manuals for the approval workflow process
 * by analyzing generated screenshots from ApprovalWorkflowTutorialTest.
 */
public class ApprovalWorkflowDocGenerator {

    private static final String SCREENSHOT_DIR = "target/playwright-screenshots";
    private static final String VIDEO_DIR = "target/playwright-recordings";
    private static final String DOCS_DIR = "docs/user-manual";
    private static final String OUTPUT_FILE = "panduan-approval-workflow.md";

    private final Path screenshotPath;
    private final Path videoPath;
    private final Path docsPath;
    private final DateTimeFormatter dateFormatter;

    public ApprovalWorkflowDocGenerator() {
        this.screenshotPath = Paths.get(SCREENSHOT_DIR);
        this.videoPath = Paths.get(VIDEO_DIR);
        this.docsPath = Paths.get(DOCS_DIR);
        this.dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    }

    public static void main(String[] args) {
        System.out.println("🚀 Memulai pembuatan panduan Approval Workflow...");

        try {
            ApprovalWorkflowDocGenerator generator = new ApprovalWorkflowDocGenerator();
            generator.generateDocumentation();
            System.out.println("📚 Pembuatan panduan selesai!");
        } catch (Exception e) {
            System.err.println("❌ Error generating documentation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void generateDocumentation() throws IOException {
        // Create docs directory
        Files.createDirectories(docsPath);

        // Find screenshots
        List<Path> screenshots = findScreenshots();
        System.out.println("📷 Ditemukan " + screenshots.size() + " screenshot");

        // Find videos
        List<Path> videos = findVideos();
        System.out.println("🎥 Ditemukan " + videos.size() + " video");

        // Copy media to docs
        copyMediaToDocs(screenshots, videos);

        // Generate markdown
        String markdown = generateMarkdown(screenshots, videos);

        // Write to file
        Path outputFilePath = docsPath.resolve(OUTPUT_FILE);
        Files.write(outputFilePath, markdown.getBytes("UTF-8"));

        System.out.println("✅ Panduan berhasil dibuat: " + outputFilePath.toAbsolutePath());

        // Update index
        updateIndex();
    }

    private List<Path> findScreenshots() throws IOException {
        if (!Files.exists(screenshotPath)) {
            return Collections.emptyList();
        }

        return Files.walk(screenshotPath)
            .filter(path -> path.toString().endsWith(".png"))
            .filter(path -> path.toString().contains("approval_workflow"))
            .sorted()
            .collect(Collectors.toList());
    }

    private List<Path> findVideos() throws IOException {
        if (!Files.exists(videoPath)) {
            return Collections.emptyList();
        }

        return Files.walk(videoPath)
            .filter(path -> path.toString().endsWith(".webm"))
            .filter(path -> path.toString().toLowerCase().contains("approvalworkflow"))
            .sorted()
            .collect(Collectors.toList());
    }

    private void copyMediaToDocs(List<Path> screenshots, List<Path> videos) throws IOException {
        // Create media subdirectories in docs
        Path screenshotDocsDir = docsPath.resolve("screenshots");
        Path videoDocsDir = docsPath.resolve("videos");
        Files.createDirectories(screenshotDocsDir);
        Files.createDirectories(videoDocsDir);

        // Copy screenshots
        for (Path screenshot : screenshots) {
            String filename = screenshot.getFileName().toString();
            Path destination = screenshotDocsDir.resolve(filename);
            Files.copy(screenshot, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy videos
        for (Path video : videos) {
            String filename = video.getFileName().toString();
            Path destination = videoDocsDir.resolve(filename);
            Files.copy(video, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("📁 Media files copied to docs directory");
    }

    private String generateMarkdown(List<Path> screenshots, List<Path> videos) {
        StringBuilder md = new StringBuilder();

        // Header
        md.append("# Panduan Approval Workflow untuk Branch Manager\n\n");
        md.append("**Aplikasi Minibank - Sistem Perbankan Syariah**\n\n");
        md.append("---\n\n");
        md.append("**Tanggal Pembuatan:** ").append(LocalDateTime.now().format(dateFormatter)).append("  \n");
        md.append("**Versi:** 2.0  \n");
        md.append("**Target Pengguna:** Customer Service (CS) dan Branch Manager  \n");
        md.append("**Status:** Aktif  \n\n");
        md.append("---\n\n");

        // Table of Contents
        md.append("## Daftar Isi\n\n");
        md.append("1. [Gambaran Umum](#gambaran-umum)\n");
        md.append("2. [Prasyarat](#prasyarat)\n");
        md.append("3. [Alur Kerja Approval](#alur-kerja-approval)\n");
        md.append("4. [Langkah-langkah Detail](#langkah-langkah-detail)\n");
        md.append("   1. [Bagian 1: Customer Service - Membuat Nasabah Baru](#bagian-1-customer-service---membuat-nasabah-baru)\n");
        md.append("   2. [Bagian 2: Branch Manager - Review dan Approval](#bagian-2-branch-manager---review-dan-approval)\n");
        md.append("5. [Video Tutorial](#video-tutorial)\n");
        md.append("6. [Status Approval](#status-approval)\n");
        md.append("7. [Tips dan Catatan Penting](#tips-dan-catatan-penting)\n");
        md.append("8. [Pemecahan Masalah Umum](#pemecahan-masalah-umum)\n\n");
        md.append("---\n\n");

        // Overview
        md.append("## Gambaran Umum\n\n");
        md.append("Panduan ini menjelaskan proses **Approval Workflow** dalam sistem Minibank. Approval Workflow adalah mekanisme kontrol yang memastikan setiap nasabah baru yang didaftarkan oleh Customer Service harus disetujui terlebih dahulu oleh Branch Manager sebelum dapat melakukan transaksi.\n\n");
        md.append("**Manfaat Approval Workflow:**\n");
        md.append("- ✅ Kontrol kualitas data nasabah\n");
        md.append("- ✅ Pencegahan fraud dan data tidak valid\n");
        md.append("- ✅ Audit trail lengkap untuk setiap persetujuan\n");
        md.append("- ✅ Dual control untuk operasi penting\n\n");
        md.append("**Waktu Estimasi:** 10-15 menit per approval request\n\n");
        md.append("---\n\n");

        // Prerequisites
        md.append("## Prasyarat\n\n");
        md.append("### Untuk Customer Service:\n");
        md.append("1. **Akses Sistem**\n");
        md.append("   - Username dan password CS yang valid\n");
        md.append("   - Koneksi internet stabil\n");
        md.append("   - Browser web yang didukung (Chrome, Firefox, Safari)\n\n");
        md.append("2. **Dokumen Nasabah**\n");
        md.append("   - KTP/Passport asli dan fotokopi\n");
        md.append("   - NPWP (jika ada)\n");
        md.append("   - Dokumen pendukung sesuai kebijakan bank\n\n");
        md.append("### Untuk Branch Manager:\n");
        md.append("1. **Akses Sistem**\n");
        md.append("   - Username dan password Branch Manager yang valid\n");
        md.append("   - Authority untuk melakukan approval\n\n");
        md.append("2. **Tanggung Jawab**\n");
        md.append("   - Memverifikasi kelengkapan data nasabah\n");
        md.append("   - Memastikan kepatuhan terhadap regulasi\n");
        md.append("   - Memberikan catatan review yang jelas\n\n");
        md.append("---\n\n");

        // Workflow
        md.append("## Alur Kerja Approval\n\n");
        md.append("```mermaid\n");
        md.append("graph TD\n");
        md.append("    A[CS: Input Data Nasabah] --> B[Sistem: Simpan dengan Status PENDING_APPROVAL]\n");
        md.append("    B --> C[Sistem: Buat Approval Request]\n");
        md.append("    C --> D[Manager: Lihat Approval Queue]\n");
        md.append("    D --> E[Manager: Review Detail Nasabah]\n");
        md.append("    E --> F{Manager: Keputusan}\n");
        md.append("    F -->|Approve| G[Sistem: Ubah Status ke APPROVED & ACTIVE]\n");
        md.append("    F -->|Reject| H[Sistem: Ubah Status ke REJECTED]\n");
        md.append("    G --> I[Nasabah dapat bertransaksi]\n");
        md.append("    H --> J[Nasabah tidak dapat digunakan]\n");
        md.append("```\n\n");
        md.append("---\n\n");

        // Detailed Steps
        md.append("## Langkah-langkah Detail\n\n");

        // Part 1: CS Creates Customer
        md.append("### Bagian 1: Customer Service - Membuat Nasabah Baru\n\n");
        md.append("#### Langkah 1: Login sebagai Customer Service\n\n");
        addScreenshotsForStep(md, screenshots, "01_halaman_login", "04_siap_login");
        md.append("**Detail Langkah:**\n");
        md.append("1. Buka halaman login aplikasi Minibank\n");
        md.append("2. Masukkan username CS Anda (contoh: cs1)\n");
        md.append("3. Masukkan password yang telah ditentukan\n");
        md.append("4. Klik tombol \"Login\"\n");
        md.append("5. Verifikasi bahwa Anda berhasil masuk ke dashboard\n\n");
        addScreenshotsForStep(md, screenshots, "05_dashboard_cs");
        md.append("\n---\n\n");

        md.append("#### Langkah 2: Navigasi ke Menu Customer Management\n\n");
        addScreenshotsForStep(md, screenshots, "06_menu_navigasi", "07_halaman_pilih");
        md.append("**Detail Langkah:**\n");
        md.append("1. Dari dashboard, cari menu navigasi di sidebar\n");
        md.append("2. Klik menu \"Customer Management\"\n");
        md.append("3. Klik tombol \"Add Customer\" atau \"Tambah Nasabah\"\n");
        md.append("4. Sistem menampilkan pilihan jenis nasabah\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 3: Pilih Jenis Nasabah dan Isi Form\n\n");
        addScreenshotsForStep(md, screenshots, "08_pilihan_jenis", "12_form_lengkap");
        md.append("**Detail Langkah:**\n");
        md.append("1. Pilih \"Personal Customer\"\n");
        md.append("2. Isi semua field yang wajib (ditandai dengan *)\n");
        md.append("3. **Data Pribadi:**\n");
        md.append("   - Nama Depan dan Nama Belakang\n");
        md.append("   - Nomor Identitas (KTP/Passport)\n");
        md.append("   - Tanggal Lahir dan Tempat Lahir\n");
        md.append("   - Jenis Kelamin\n");
        md.append("   - Nama Ibu Kandung\n");
        md.append("4. **Data Kontak:**\n");
        md.append("   - Email (opsional)\n");
        md.append("   - Nomor Telepon\n");
        md.append("   - Alamat Lengkap\n");
        md.append("   - Kota dan Kode Pos\n");
        md.append("5. **Data Pekerjaan (FR.002):**\n");
        md.append("   - Pekerjaan dan Perusahaan\n");
        md.append("   - Bidang Usaha\n");
        md.append("   - Penghasilan Rata-rata\n");
        md.append("   - Tujuan Pembukaan Rekening\n");
        md.append("6. Periksa kembali semua data yang diisi\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 4: Simpan Nasabah (Pending Approval)\n\n");
        addScreenshotsForStep(md, screenshots, "13_tombol_simpan", "15_pesan_sukses");
        md.append("**Detail Langkah:**\n");
        md.append("1. Klik tombol \"Simpan\" atau \"Save\"\n");
        md.append("2. Tunggu proses penyimpanan selesai\n");
        md.append("3. Sistem menampilkan pesan sukses\n");
        md.append("4. **Penting:** Nasabah tersimpan dengan status **PENDING_APPROVAL**\n");
        md.append("5. Nasabah belum dapat melakukan transaksi\n");
        md.append("6. Approval request otomatis dibuat untuk Branch Manager\n\n");
        md.append("> **💡 Catatan:** Pesan sukses akan menyebutkan bahwa nasabah \"submitted for approval\" atau \"menunggu persetujuan\".\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 5: Logout Customer Service\n\n");
        addScreenshotsForStep(md, screenshots, "16_dashboard_sebelum", "17_setelah_logout");
        md.append("**Detail Langkah:**\n");
        md.append("1. Klik menu user di pojok kanan atas\n");
        md.append("2. Pilih \"Logout\"\n");
        md.append("3. Sistem kembali ke halaman login\n\n");
        md.append("---\n\n");

        // Part 2: Manager Approves
        md.append("### Bagian 2: Branch Manager - Review dan Approval\n\n");
        md.append("#### Langkah 6: Login sebagai Branch Manager\n\n");
        addScreenshotsForStep(md, screenshots, "18_halaman_login_manager", "21_dashboard_manager");
        md.append("**Detail Langkah:**\n");
        md.append("1. Buka halaman login aplikasi Minibank\n");
        md.append("2. Masukkan username Branch Manager (contoh: manager1)\n");
        md.append("3. Masukkan password Branch Manager\n");
        md.append("4. Klik tombol \"Login\"\n");
        md.append("5. Verifikasi berhasil masuk ke dashboard Branch Manager\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 7: Navigasi ke Approval Queue\n\n");
        addScreenshotsForStep(md, screenshots, "22_menu_approval", "24_daftar_pending");
        md.append("**Detail Langkah:**\n");
        md.append("1. Dari dashboard, cari menu \"Approval Queue\" di sidebar\n");
        md.append("2. Klik menu \"Approval Queue\"\n");
        md.append("3. Sistem menampilkan daftar approval yang pending\n");
        md.append("4. Badge \"X Pending\" menunjukkan jumlah approval yang menunggu\n");
        md.append("5. Tabel menampilkan:\n");
        md.append("   - Request Type (CUSTOMER_CREATION, ACCOUNT_OPENING)\n");
        md.append("   - Entity Type (CUSTOMER, ACCOUNT)\n");
        md.append("   - Requested By (nama CS yang membuat)\n");
        md.append("   - Requested Date (tanggal pembuatan)\n");
        md.append("   - Request Notes (catatan dari CS)\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 8: Lihat Detail Approval Request\n\n");
        addScreenshotsForStep(md, screenshots, "25_halaman_detail", "28_form_approval");
        md.append("**Detail Langkah:**\n");
        md.append("1. Klik link \"View Details\" pada approval request yang akan direview\n");
        md.append("2. Sistem menampilkan halaman detail approval\n");
        md.append("3. **Informasi Request:**\n");
        md.append("   - Request Type: CUSTOMER_CREATION\n");
        md.append("   - Entity Type: CUSTOMER\n");
        md.append("   - Requested By: customer-service\n");
        md.append("   - Requested Date: tanggal dan waktu pembuatan\n");
        md.append("   - Request Notes: catatan dari CS\n");
        md.append("4. **Detail Data Nasabah:**\n");
        md.append("   - Customer Number (auto-generated)\n");
        md.append("   - Customer Type (PERSONAL/CORPORATE)\n");
        md.append("   - Nama Lengkap\n");
        md.append("   - Email dan Nomor Telepon\n");
        md.append("   - Alamat Lengkap\n");
        md.append("   - Semua data FR.002 lainnya\n");
        md.append("5. **Form Approval Actions:**\n");
        md.append("   - Form Approve (hijau): untuk menyetujui\n");
        md.append("   - Form Reject (merah): untuk menolak\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 9: Approve atau Reject Request\n\n");
        md.append("##### Opsi A: Approve Request\n\n");
        addScreenshotsForStep(md, screenshots, "29_catatan_review", "32_pesan_sukses_approval");
        md.append("**Detail Langkah untuk Approve:**\n");
        md.append("1. Periksa semua data nasabah dengan teliti\n");
        md.append("2. Pastikan data sesuai dengan dokumen\n");
        md.append("3. Pastikan tidak ada red flag atau data mencurigakan\n");
        md.append("4. Isi field \"Review Notes\" (opsional) dengan catatan review Anda\n");
        md.append("   - Contoh: \"Data nasabah lengkap dan sesuai dokumen. Disetujui untuk aktivasi.\"\n");
        md.append("5. Klik tombol \"Approve\" (hijau)\n");
        md.append("6. Sistem memproses approval\n");
        md.append("7. **Hasil:**\n");
        md.append("   - Approval status berubah menjadi **APPROVED**\n");
        md.append("   - Customer status berubah menjadi **ACTIVE**\n");
        md.append("   - Nasabah dapat melakukan transaksi\n");
        md.append("   - Reviewed By: diisi dengan username Branch Manager\n");
        md.append("   - Reviewed Date: diisi dengan tanggal/waktu approval\n");
        md.append("8. Sistem menampilkan pesan sukses\n");
        md.append("9. Otomatis redirect ke Approval Queue\n\n");

        md.append("##### Opsi B: Reject Request\n\n");
        md.append("**Detail Langkah untuk Reject:**\n");
        md.append("1. Jika menemukan data tidak valid atau mencurigakan\n");
        md.append("2. Scroll ke form \"Reject Request\" (merah)\n");
        md.append("3. **Wajib** isi field \"Rejection Reason\" dengan alasan penolakan\n");
        md.append("   - Contoh: \"Dokumen identitas tidak sesuai dengan data yang diinput\"\n");
        md.append("   - Contoh: \"Data pekerjaan tidak lengkap dan tidak jelas\"\n");
        md.append("   - Contoh: \"Nomor telepon tidak dapat dihubungi\"\n");
        md.append("4. Isi field \"Additional Notes\" (opsional) dengan catatan tambahan\n");
        md.append("5. Klik tombol \"Reject\" (merah)\n");
        md.append("6. **Hasil:**\n");
        md.append("   - Approval status berubah menjadi **REJECTED**\n");
        md.append("   - Customer tetap dengan status **INACTIVE**\n");
        md.append("   - Nasabah tidak dapat melakukan transaksi\n");
        md.append("   - Rejection reason tersimpan untuk audit\n");
        md.append("   - CS dapat melihat alasan penolakan\n\n");
        md.append("---\n\n");

        md.append("#### Langkah 10: Verifikasi Approval Queue\n\n");
        addScreenshotsForStep(md, screenshots, "33_queue_setelah", "34_tutorial_selesai");
        md.append("**Detail Langkah:**\n");
        md.append("1. Setelah approve/reject, sistem redirect ke Approval Queue\n");
        md.append("2. Badge \"Pending\" akan berkurang 1\n");
        md.append("3. Approval request yang sudah diproses tidak tampil lagi di queue\n");
        md.append("4. Request berikutnya (jika ada) dapat diproses\n\n");
        md.append("---\n\n");

        // Videos
        if (!videos.isEmpty()) {
            md.append("## Video Tutorial\n\n");
            md.append("Berikut adalah video tutorial yang menunjukkan seluruh proses approval workflow:\n\n");
            for (Path video : videos) {
                String filename = video.getFileName().toString();
                String title = makeVideoTitleReadable(filename);
                md.append("### ").append(title).append("\n\n");
                md.append("**File:** [").append(filename).append("](videos/").append(filename).append(")\n\n");
                md.append("> **Catatan:** Untuk memutar video, klik link di atas atau buka file langsung menggunakan browser yang mendukung format WebM.\n\n");
            }
            md.append("---\n\n");
        }

        // Approval Status
        md.append("## Status Approval\n\n");
        md.append("### Status Approval Request\n\n");
        md.append("| Status | Deskripsi | Aksi yang Tersedia |\n");
        md.append("|--------|-----------|--------------------|\n");
        md.append("| **PENDING** | Request menunggu review dari Branch Manager | Approve / Reject |\n");
        md.append("| **APPROVED** | Request telah disetujui, entity menjadi ACTIVE | Tidak ada (readonly) |\n");
        md.append("| **REJECTED** | Request ditolak, entity tetap INACTIVE | Tidak ada (readonly) |\n\n");

        md.append("### Status Customer\n\n");
        md.append("| Approval Status | Customer Status | Dapat Bertransaksi? | Keterangan |\n");
        md.append("|-----------------|-----------------|---------------------|------------|\n");
        md.append("| PENDING_APPROVAL | INACTIVE | ❌ Tidak | Menunggu approval dari Branch Manager |\n");
        md.append("| APPROVED | ACTIVE | ✅ Ya | Customer aktif dan dapat bertransaksi |\n");
        md.append("| REJECTED | INACTIVE | ❌ Tidak | Customer ditolak, tidak dapat digunakan |\n\n");
        md.append("---\n\n");

        // Tips
        md.append("## Tips dan Catatan Penting\n\n");
        md.append("### Tips untuk Customer Service:\n\n");
        md.append("1. **Kelengkapan Data**\n");
        md.append("   - Pastikan semua field wajib terisi dengan benar\n");
        md.append("   - Verifikasi data dengan dokumen asli\n");
        md.append("   - Gunakan data yang jelas dan akurat\n\n");
        md.append("2. **Request Notes**\n");
        md.append("   - Berikan catatan jika ada hal khusus yang perlu diperhatikan\n");
        md.append("   - Sebutkan jenis dokumen yang telah diverifikasi\n");
        md.append("   - Informasikan tujuan pembukaan rekening\n\n");
        md.append("3. **Follow Up**\n");
        md.append("   - Informasikan kepada nasabah bahwa rekening perlu approval\n");
        md.append("   - Estimasi waktu approval: 1-2 hari kerja\n");
        md.append("   - Jika reject, koordinasi dengan Manager untuk perbaikan data\n\n");

        md.append("### Tips untuk Branch Manager:\n\n");
        md.append("1. **Review yang Teliti**\n");
        md.append("   - Periksa kelengkapan dan keakuratan data\n");
        md.append("   - Verifikasi kesesuaian dengan regulasi banking\n");
        md.append("   - Perhatikan red flag: data tidak konsisten, nomor tidak valid, dll\n\n");
        md.append("2. **Review Notes yang Jelas**\n");
        md.append("   - Berikan catatan review yang spesifik\n");
        md.append("   - Untuk rejection, jelaskan alasan dengan detail\n");
        md.append("   - Review notes akan menjadi audit trail\n\n");
        md.append("3. **SLA Approval**\n");
        md.append("   - Usahakan approve/reject dalam 1 hari kerja\n");
        md.append("   - Prioritaskan request yang urgent\n");
        md.append("   - Koordinasi dengan CS jika ada data yang perlu dikonfirmasi\n\n");

        md.append("### Keamanan:\n\n");
        md.append("- 🔒 Jangan share password dengan siapapun\n");
        md.append("- 🔒 Selalu logout setelah selesai\n");
        md.append("- 🔒 Laporkan aktivitas mencurigakan kepada IT Security\n");
        md.append("- 🔒 Approval decision tidak dapat diubah setelah diproses\n\n");
        md.append("---\n\n");

        // Troubleshooting
        md.append("## Pemecahan Masalah Umum\n\n");
        md.append("### Masalah Customer Service:\n\n");
        md.append("**1. Form tidak bisa disimpan**\n");
        md.append("   - Pastikan semua field wajib (*) sudah terisi\n");
        md.append("   - Periksa format email (harus valid)\n");
        md.append("   - Periksa format nomor telepon\n");
        md.append("   - Pastikan nomor identitas belum terdaftar\n\n");
        md.append("**2. Tidak tahu status approval**\n");
        md.append("   - Lihat customer list, cek kolom \"Approval Status\"\n");
        md.append("   - Status PENDING_APPROVAL: masih menunggu manager\n");
        md.append("   - Status APPROVED: sudah disetujui\n");
        md.append("   - Status REJECTED: ditolak, lihat rejection reason\n\n");

        md.append("### Masalah Branch Manager:\n\n");
        md.append("**1. Approval Queue kosong**\n");
        md.append("   - Refresh halaman (F5)\n");
        md.append("   - Periksa filter jika ada\n");
        md.append("   - Mungkin memang tidak ada pending request\n\n");
        md.append("**2. Tidak bisa approve/reject**\n");
        md.append("   - Pastikan login sebagai Branch Manager\n");
        md.append("   - Periksa authority/permission Anda\n");
        md.append("   - Untuk reject, wajib isi rejection reason\n");
        md.append("   - Refresh halaman jika tombol tidak responsif\n\n");
        md.append("**3. Error saat approve**\n");
        md.append("   - Periksa koneksi internet\n");
        md.append("   - Coba refresh dan ulangi\n");
        md.append("   - Hubungi IT jika error berlanjut\n\n");

        md.append("### Kontak Dukungan:\n\n");
        md.append("- 📞 IT Help Desk: ext. 123\n");
        md.append("- 📞 Supervisor CS: ext. 456\n");
        md.append("- 📞 Manager Operasional: ext. 789\n\n");
        md.append("---\n\n");

        // Footer
        md.append("## Informasi Dokumen\n\n");
        md.append("**Dibuat oleh:** Sistem Aplikasi Minibank  \n");
        md.append("**Tanggal:** ").append(LocalDateTime.now().format(dateFormatter)).append("  \n");
        md.append("**Versi:** 2.0  \n");
        md.append("**Format:** Markdown (.md)  \n\n");
        md.append("**Hak Cipta:** © 2025 Aplikasi Minibank - Sistem Perbankan Syariah  \n\n");
        md.append("---\n\n");
        md.append("*Panduan ini dibuat secara otomatis menggunakan Playwright Test Framework dan Java. ");
        md.append("Untuk pembaruan atau perbaikan, hubungi tim IT atau maintainer sistem.*\n\n");
        md.append("**Generator:** ApprovalWorkflowDocGenerator.java  \n");
        md.append("**Test Source:** ApprovalWorkflowTutorialTest.java  \n");
        md.append("**Framework:** Playwright + Java  \n");

        return md.toString();
    }

    private void addScreenshotsForStep(StringBuilder md, List<Path> screenshots, String startPattern, String endPattern) {
        boolean inRange = false;
        for (Path screenshot : screenshots) {
            String filename = screenshot.getFileName().toString();
            if (filename.contains(startPattern)) {
                inRange = true;
            }
            if (inRange) {
                String description = getDescriptionFromFilename(filename);
                md.append("![").append(description).append("](screenshots/").append(filename).append(")\n\n");
            }
            if (filename.contains(endPattern)) {
                break;
            }
        }
    }

    private void addScreenshotsForStep(StringBuilder md, List<Path> screenshots, String pattern) {
        for (Path screenshot : screenshots) {
            String filename = screenshot.getFileName().toString();
            if (filename.contains(pattern)) {
                String description = getDescriptionFromFilename(filename);
                md.append("![").append(description).append("](screenshots/").append(filename).append(")\n\n");
            }
        }
    }

    private String getDescriptionFromFilename(String filename) {
        // Extract description from filename
        if (filename.contains("halaman_login")) return "Halaman Login";
        if (filename.contains("username_terisi")) return "Username Terisi";
        if (filename.contains("password_terisi")) return "Password Terisi";
        if (filename.contains("siap_login")) return "Siap Login";
        if (filename.contains("dashboard_cs")) return "Dashboard Customer Service";
        if (filename.contains("menu_navigasi")) return "Menu Navigasi";
        if (filename.contains("pilih_jenis")) return "Pilih Jenis Nasabah";
        if (filename.contains("pilihan_jenis")) return "Pilihan Jenis Nasabah";
        if (filename.contains("form_nasabah_personal_kosong")) return "Form Nasabah Personal Kosong";
        if (filename.contains("data_pribadi_terisi")) return "Data Pribadi Terisi";
        if (filename.contains("data_kontak_terisi")) return "Data Kontak Terisi";
        if (filename.contains("form_lengkap")) return "Form Lengkap Siap Disimpan";
        if (filename.contains("tombol_simpan")) return "Tombol Simpan";
        if (filename.contains("nasabah_berhasil")) return "Nasabah Berhasil Disimpan";
        if (filename.contains("pesan_sukses")) return "Pesan Sukses";
        if (filename.contains("sebelum_logout")) return "Dashboard Sebelum Logout";
        if (filename.contains("setelah_logout")) return "Setelah Logout";
        if (filename.contains("login_manager")) return "Login Branch Manager";
        if (filename.contains("username_manager")) return "Username Manager Terisi";
        if (filename.contains("password_manager")) return "Password Manager Terisi";
        if (filename.contains("dashboard_manager")) return "Dashboard Branch Manager";
        if (filename.contains("menu_approval")) return "Menu Approval Queue";
        if (filename.contains("halaman_approval_queue")) return "Halaman Approval Queue";
        if (filename.contains("daftar_pending")) return "Daftar Pending Approval";
        if (filename.contains("detail_approval")) return "Halaman Detail Approval";
        if (filename.contains("informasi_request")) return "Informasi Request";
        if (filename.contains("detail_data_nasabah")) return "Detail Data Nasabah";
        if (filename.contains("form_approval_actions")) return "Form Approval Actions";
        if (filename.contains("catatan_review")) return "Catatan Review Terisi";
        if (filename.contains("siap_approve")) return "Siap Approve";
        if (filename.contains("approval_berhasil")) return "Approval Berhasil";
        if (filename.contains("pesan_sukses_approval")) return "Pesan Sukses Approval";
        if (filename.contains("queue_setelah")) return "Queue Setelah Approval";
        if (filename.contains("tutorial_selesai")) return "Tutorial Selesai";
        return "Screenshot";
    }

    private String makeVideoTitleReadable(String filename) {
        return filename
            .replaceAll("^\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_", "")
            .replaceAll("approvalworkflowtutorialtest", "Tutorial Approval Workflow")
            .replaceAll("_", " ")
            .replaceAll(".webm$", "")
            .trim();
    }

    private void updateIndex() throws IOException {
        Path indexPath = docsPath.resolve("README.md");

        StringBuilder index = new StringBuilder();
        index.append("# Dokumentasi Pengguna Aplikasi Minibank\n\n");
        index.append("## Panduan Yang Tersedia\n\n");
        index.append("1. **[Panduan Approval Workflow](panduan-approval-workflow.md)** ⭐ NEW\n");
        index.append("   - Target: Customer Service (CS) dan Branch Manager\n");
        index.append("   - Proses: Membuat nasabah baru dengan approval workflow\n");
        index.append("   - Format: Panduan lengkap dengan screenshot dan video\n");
        index.append("   - Versi: 2.0 (Updated with Approval Workflow)\n\n");
        index.append("## Cara Menggunakan Panduan\n\n");
        index.append("1. Buka file panduan yang sesuai dengan kebutuhan Anda\n");
        index.append("2. Ikuti langkah-langkah secara berurutan\n");
        index.append("3. Lihat screenshot untuk referensi visual\n");
        index.append("4. Tonton video tutorial jika tersedia\n");
        index.append("5. Gunakan bagian troubleshooting jika mengalami masalah\n\n");
        index.append("## Perubahan dari Versi Sebelumnya\n\n");
        index.append("### Versi 2.0 (").append(LocalDateTime.now().format(dateFormatter)).append(")\n");
        index.append("- ✨ **NEW:** Implementasi Approval Workflow\n");
        index.append("- ✨ Customer baru memerlukan approval dari Branch Manager\n");
        index.append("- ✨ Dual control untuk operasi penting\n");
        index.append("- ✨ Audit trail lengkap untuk setiap approval/rejection\n");
        index.append("- ⚠️ **BREAKING CHANGE:** Customer tidak langsung aktif setelah dibuat\n\n");
        index.append("### Versi 1.0 (Obsolete)\n");
        index.append("- Customer langsung aktif setelah dibuat (tanpa approval)\n");
        index.append("- ❌ **DEPRECATED:** Panduan pembukaan rekening versi lama tidak berlaku lagi\n\n");
        index.append("## Pembaruan Panduan\n\n");
        index.append("Panduan ini dibuat secara otomatis dari test dokumentasi. Untuk memperbarui:\n\n");
        index.append("```bash\n");
        index.append("# 1. Jalankan test dokumentasi (slow mode dengan recording)\n");
        index.append("mvn test -Dtest=ApprovalWorkflowTutorialTest \\\n");
        index.append("  -Dplaywright.headless=false \\\n");
        index.append("  -Dplaywright.slowmo=2000 \\\n");
        index.append("  -Dplaywright.record=true\n\n");
        index.append("# 2. Generate ulang panduan\n");
        index.append("mvn exec:java -Dexec.mainClass=\"id.ac.tazkia.minibank.util.ApprovalWorkflowDocGenerator\"\n");
        index.append("```\n\n");
        index.append("---\n\n");
        index.append("*Dibuat pada: ").append(LocalDateTime.now().format(dateFormatter)).append("*\n");

        Files.write(indexPath, index.toString().getBytes("UTF-8"));
        System.out.println("📋 Index file updated: " + indexPath.toAbsolutePath());
    }
}
