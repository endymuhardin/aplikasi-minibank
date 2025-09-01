package id.ac.tazkia.minibank.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generator for Indonesian user manual documentation based on Playwright test screenshots and videos.
 * 
 * This utility creates comprehensive user manuals by analyzing generated screenshots and videos
 * from documentation tests and producing markdown-formatted guides in Indonesian.
 */
public class UserManualGenerator {
    
    private static final String SCREENSHOT_DIR = "target/playwright-screenshots";
    private static final String VIDEO_DIR = "target/playwright-recordings";
    private static final String OUTPUT_DIR = "target/playwright-documentation";
    private static final String OUTPUT_FILE = "panduan-pembukaan-rekening-nasabah-personal.md";
    
    private final Path screenshotPath;
    private final Path videoPath;
    private final Path outputPath;
    
    public UserManualGenerator() {
        this.screenshotPath = Paths.get(SCREENSHOT_DIR);
        this.videoPath = Paths.get(VIDEO_DIR);
        this.outputPath = Paths.get(OUTPUT_DIR);
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Memulai pembuatan panduan pengguna...");
        
        try {
            UserManualGenerator generator = new UserManualGenerator();
            generator.generateManual();
            System.out.println("📚 Pembuatan panduan selesai!");
        } catch (Exception e) {
            System.err.println("❌ Error generating user manual: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void generateManual() throws IOException {
        // Create output directory
        Files.createDirectories(outputPath);
        
        // Find media files
        MediaFiles media = findMediaFiles();
        
        // Copy screenshots and videos to output directory for simpler paths
        copyMediaFiles(media);
        
        // Generate markdown content
        String markdown = generateMarkdownContent(media);
        
        // Write to file
        Path outputFilePath = outputPath.resolve(OUTPUT_FILE);
        Files.write(outputFilePath, markdown.getBytes("UTF-8"));
        
        System.out.println("✅ Panduan pengguna berhasil dibuat: " + outputFilePath.toAbsolutePath());
        System.out.println("📊 Total screenshot: " + media.screenshots.size());
        System.out.println("🎥 Total video: " + media.videos.size());
        
        // Generate index file
        generateIndexFile();
    }
    
    private void copyMediaFiles(MediaFiles media) throws IOException {
        // Create subdirectories
        Path screenshotOutputDir = outputPath.resolve("screenshots");
        Path videoOutputDir = outputPath.resolve("videos");
        Files.createDirectories(screenshotOutputDir);
        Files.createDirectories(videoOutputDir);
        
        // Copy screenshots with simplified names
        for (Path screenshot : media.screenshots) {
            String filename = screenshot.getFileName().toString();
            Path destination = screenshotOutputDir.resolve(filename);
            Files.copy(screenshot, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Copy videos with simplified names
        for (Path video : media.videos) {
            String filename = video.getFileName().toString();
            Path destination = videoOutputDir.resolve(filename);
            Files.copy(video, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        
        System.out.println("📁 Media files copied to documentation directory");
    }
    
    private MediaFiles findMediaFiles() throws IOException {
        List<Path> screenshots = new ArrayList<>();
        List<Path> videos = new ArrayList<>();
        
        // Find screenshots
        if (Files.exists(screenshotPath)) {
            screenshots = Files.walk(screenshotPath)
                .filter(path -> path.toString().endsWith(".png"))
                .sorted()
                .collect(Collectors.toList());
            System.out.println("📷 Ditemukan " + screenshots.size() + " screenshot");
        }
        
        // Find videos
        if (Files.exists(videoPath)) {
            videos = Files.walk(videoPath)
                .filter(path -> path.toString().endsWith(".webm"))
                .sorted()
                .collect(Collectors.toList());
            System.out.println("🎥 Ditemukan " + videos.size() + " video");
        }
        
        return new MediaFiles(screenshots, videos);
    }
    
    private String generateMarkdownContent(MediaFiles media) {
        StringBuilder markdown = new StringBuilder();
        
        markdown.append(generateHeader());
        markdown.append(generateTableOfContents());
        markdown.append(generateOverview());
        markdown.append(generatePrerequisites());
        markdown.append(generateSteps(media.screenshots));
        markdown.append(generateVideoSection(media.videos));
        markdown.append(generateTipsSection());
        markdown.append(generateTroubleshootingSection());
        markdown.append(generateFooter());
        
        return markdown.toString();
    }
    
    private String generateHeader() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", 
            new Locale("id", "ID")));
        
        return String.format("""
# Panduan Pembukaan Rekening Nasabah Personal untuk Customer Service

**Aplikasi Minibank - Sistem Perbankan Syariah**

---

**Tanggal Pembuatan:** %s  
**Versi:** 1.0  
**Target Pengguna:** Customer Service (CS)  
**Status:** Aktif  

---

""", today);
    }
    
    private String generateTableOfContents() {
        return """
## Daftar Isi

1. [Gambaran Umum](#gambaran-umum)
2. [Prasyarat](#prasyarat)
3. [Langkah-langkah Detail](#langkah-langkah-detail)
   1. [Langkah 1: Login ke Sistem](#langkah-1-login-ke-sistem)
   2. [Langkah 2: Navigasi ke Menu Customer Management](#langkah-2-navigasi-ke-menu-customer-management)
   3. [Langkah 3: Memulai Pembuatan Nasabah Baru](#langkah-3-memulai-pembuatan-nasabah-baru)
   4. [Langkah 4: Mengisi Informasi Pribadi](#langkah-4-mengisi-informasi-pribadi)
   5. [Langkah 5: Mengisi Informasi Kontak](#langkah-5-mengisi-informasi-kontak)
   6. [Langkah 6: Menyimpan dan Verifikasi](#langkah-6-menyimpan-dan-verifikasi)
   7. [Langkah 7: Navigasi ke Pembukaan Rekening](#langkah-7-navigasi-ke-pembukaan-rekening)
   8. [Langkah 8: Menyelesaikan Proses Pembukaan Rekening](#langkah-8-menyelesaikan-proses-pembukaan-rekening)
4. [Video Tutorial](#video-tutorial)
5. [Tips dan Catatan Penting](#tips-dan-catatan-penting)
6. [Pemecahan Masalah Umum](#pemecahan-masalah-umum)

---

""";
    }
    
    private String generateOverview() {
        return """
## Gambaran Umum

Panduan ini menjelaskan langkah-langkah lengkap untuk membuka rekening nasabah personal menggunakan aplikasi Minibank. Panduan ini ditujukan khusus untuk petugas Customer Service (CS) yang bertugas melayani nasabah dalam proses pembukaan rekening.

**Target Pengguna:** Customer Service (CS)
**Sistem:** Aplikasi Minibank Syariah
**Browser:** Chromium, Firefox, atau WebKit
**Waktu Estimasi:** 5-10 menit per nasabah

---

""";
    }
    
    private String generatePrerequisites() {
        return """
## Prasyarat

Sebelum memulai proses pembukaan rekening, pastikan:

1. **Akses Sistem**
   - Memiliki username dan password CS yang valid
   - Koneksi internet stabil
   - Browser web yang didukung (Chrome, Firefox, Safari)

2. **Dokumen Nasabah**
   - KTP/Passport asli dan fotokopi
   - NPWP (jika ada)
   - Dokumen pendukung lainnya sesuai kebijakan bank

3. **Informasi Yang Diperlukan**
   - Data pribadi lengkap nasabah
   - Alamat domisili terkini
   - Nomor telepon yang aktif
   - Alamat email (opsional)
   - Setoran awal minimum

---

""";
    }
    
    private String generateSteps(List<Path> screenshots) {
        StringBuilder steps = new StringBuilder();
        steps.append("## Langkah-langkah Detail\n\n");
        
        // Define the steps with better screenshot mapping
        String[][] stepData = {
            {"Login ke Sistem", "Masuk ke aplikasi Minibank menggunakan kredensial CS", "step_1", "_01_|_02_|_03_|_04_|_05_",
             "1. Buka halaman login aplikasi Minibank\n" +
             "2. Masukkan username CS Anda (contoh: cs1)\n" +
             "3. Masukkan password yang telah ditentukan\n" +
             "4. Klik tombol \"Login\" atau tekan Enter\n" +
             "5. Verifikasi bahwa Anda berhasil masuk ke dashboard"},
            
            {"Navigasi ke Menu Customer Management", "Akses menu pengelolaan nasabah untuk mulai proses pembukaan rekening", "step_2", "_06_|_07_|_08_",
             "1. Dari dashboard utama, cari menu navigasi\n" +
             "2. Pilih atau klik menu \"Customer Management\" atau \"Kelola Nasabah\"\n" +
             "3. Sistem akan menampilkan halaman daftar nasabah\n" +
             "4. Pastikan halaman nasabah telah dimuat dengan benar"},
            
            {"Memulai Pembuatan Nasabah Baru", "Inisiasi proses pembuatan profil nasabah personal baru", "step_3", "_09_|_10_|_11_|_12_",
             "1. Pada halaman daftar nasabah, cari tombol \"Tambah Nasabah\" atau \"Add Customer\"\n" +
             "2. Klik tombol tersebut untuk memulai proses\n" +
             "3. Sistem akan menampilkan halaman pemilihan jenis nasabah\n" +
             "4. Pilih \"Personal Customer\" atau \"Nasabah Personal\"\n" +
             "5. Sistem akan mengarahkan ke formulir pendaftaran nasabah personal"},
            
            {"Mengisi Informasi Pribadi", "Lengkapi data pribadi nasabah sesuai dokumen identitas", "step_4", "_13_|_14_|_15_|_16_|_17_|_18_",
             "1. Isi field \"Nama Depan\" sesuai KTP/Passport\n" +
             "2. Isi field \"Nama Belakang\" (jika ada)\n" +
             "3. Masukkan nomor identitas (KTP/Passport)\n" +
             "4. Pilih jenis identitas dari dropdown menu\n" +
             "5. Isi tanggal lahir dalam format DD/MM/YYYY atau DD-MM-YYYY\n" +
             "6. Masukkan tempat lahir sesuai dokumen\n" +
             "7. Pilih jenis kelamin dari dropdown\n" +
             "8. Isi nama ibu kandung untuk verifikasi keamanan"},
            
            {"Mengisi Informasi Kontak", "Lengkapi data kontak dan alamat nasabah", "step_5", "_20_|_21_|_22_|_23_",
             "1. Masukkan alamat email yang valid (opsional)\n" +
             "2. Isi nomor telepon/handphone yang aktif\n" +
             "3. Masukkan alamat lengkap domisili\n" +
             "4. Isi nama kota tempat tinggal\n" +
             "5. Masukkan kode pos wilayah\n" +
             "6. Isi nama negara (default: Indonesia)\n" +
             "7. Periksa kembali semua data yang telah diisi"},
            
            {"Menyimpan dan Verifikasi", "Simpan data nasabah dan verifikasi keberhasilan pembuatan profil", "step_6", "_24_|_25_|_26_",
             "1. Pastikan semua field wajib telah terisi dengan benar\n" +
             "2. Klik tombol \"Simpan\" atau \"Save\" untuk menyimpan data\n" +
             "3. Tunggu proses penyimpanan selesai\n" +
             "4. Sistem akan menampilkan pesan konfirmasi atau redirect ke halaman detail\n" +
             "5. Verifikasi bahwa nomor nasabah telah digenerate otomatis\n" +
             "6. Catat nomor nasabah untuk proses selanjutnya"},
            
            {"Melihat Daftar Nasabah", "Verifikasi nasabah yang telah dibuat tampil di daftar", "step_7", "_27_|_28_",
             "1. Kembali ke halaman daftar nasabah\n" +
             "2. Verifikasi nasabah baru tampil dalam daftar\n" +
             "3. Periksa informasi nasabah sudah benar\n" +
             "4. Siap untuk melanjutkan ke pembukaan rekening"},
            
            {"Rangkuman Tutorial", "Ringkasan keseluruhan proses pembukaan rekening nasabah", "step_8", "_29_|_30_",
             "1. Login berhasil dilakukan dengan akun CS\n" +
             "2. Navigasi ke menu Customer Management berhasil\n" +
             "3. Pembuatan nasabah personal berhasil diselesaikan\n" +
             "4. Data nasabah tersimpan dengan baik\n" +
             "5. Siap untuk proses pembukaan rekening selanjutnya"}
        };
        
        for (int i = 0; i < stepData.length; i++) {
            int stepNum = i + 1;
            String title = stepData[i][0];
            String description = stepData[i][1];
            String stepKey = stepData[i][2];
            String screenshotPattern = stepData[i][3];
            String details = stepData[i][4];
            
            steps.append(String.format("### Langkah %d: %s\n\n", stepNum, title));
            steps.append(String.format("**Deskripsi:** %s\n\n", description));
            
            // Filter screenshots based on the pattern
            List<Path> stepScreenshots = new ArrayList<>();
            String[] patterns = screenshotPattern.split("\\|");
            for (Path screenshot : screenshots) {
                String filename = screenshot.getFileName().toString();
                for (String pattern : patterns) {
                    if (filename.contains(pattern)) {
                        stepScreenshots.add(screenshot);
                        break;
                    }
                }
            }
            
            // Sort screenshots to ensure proper order
            stepScreenshots.sort((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()));
            
            if (!stepScreenshots.isEmpty()) {
                steps.append("**Screenshot:**\n\n");
                for (Path screenshot : stepScreenshots) {
                    // Use simple relative path since files are copied to same directory structure
                    String filename = screenshot.getFileName().toString();
                    String altText = getAltTextForScreenshot(filename, title);
                    steps.append(String.format("![%s](screenshots/%s)\n\n", altText, filename));
                }
            }
            
            steps.append("**Detail Langkah:**\n\n");
            steps.append(details);
            steps.append("\n\n");
            
            if (stepNum < stepData.length) {
                steps.append("---\n\n");
            }
        }
        
        steps.append("---\n\n");
        return steps.toString();
    }
    
    private String getAltTextForScreenshot(String filename, String stepTitle) {
        // Extract description from filename
        if (filename.contains("login_page_loaded")) return "Halaman login aplikasi";
        if (filename.contains("username_filled")) return "Input username diisi";
        if (filename.contains("password_filled")) return "Input password diisi";
        if (filename.contains("ready_to_login")) return "Form login siap";
        if (filename.contains("dashboard_after_login")) return "Dashboard setelah login";
        if (filename.contains("dashboard_navigation")) return "Menu navigasi dashboard";
        if (filename.contains("customer_list_page")) return "Halaman daftar nasabah";
        if (filename.contains("customer_table")) return "Tabel daftar nasabah";
        if (filename.contains("add_customer_button")) return "Tombol tambah nasabah";
        if (filename.contains("customer_type_selection")) return "Halaman pemilihan jenis nasabah";
        if (filename.contains("personal_customer_option")) return "Opsi nasabah personal";
        if (filename.contains("personal_customer_form")) return "Form nasabah personal";
        if (filename.contains("empty_personal_form")) return "Form kosong siap diisi";
        if (filename.contains("first_name_filled")) return "Field nama depan terisi";
        if (filename.contains("identity_info")) return "Informasi identitas terisi";
        if (filename.contains("birth_info")) return "Informasi kelahiran terisi";
        if (filename.contains("mother_name")) return "Nama ibu kandung terisi";
        if (filename.contains("email_field")) return "Field email terisi";
        if (filename.contains("phone_field")) return "Field telepon terisi";
        if (filename.contains("address_info")) return "Informasi alamat terisi";
        if (filename.contains("complete_form")) return "Form lengkap terisi";
        if (filename.contains("save_button")) return "Tombol simpan siap diklik";
        if (filename.contains("after_save")) return "Hasil setelah penyimpanan";
        if (filename.contains("customer_created")) return "Nasabah berhasil dibuat";
        if (filename.contains("with_created_customer")) return "Daftar dengan nasabah baru";
        if (filename.contains("complete_customer_table")) return "Tabel nasabah lengkap";
        if (filename.contains("tutorial_complete")) return "Tutorial selesai";
        
        // Default to step title if no match
        return stepTitle;
    }
    
    private String generateVideoSection(List<Path> videos) {
        StringBuilder videoSection = new StringBuilder();
        videoSection.append("## Video Tutorial\n\n");
        
        if (!videos.isEmpty()) {
            videoSection.append("Berikut adalah video tutorial yang menunjukkan seluruh proses pembukaan rekening nasabah personal:\n\n");
            
            for (int i = 0; i < videos.size(); i++) {
                Path video = videos.get(i);
                String filename = video.getFileName().toString();
                String filenameWithoutExtension = filename.replace(".webm", "");
                String readableTitle = makeVideoTitleReadable(filenameWithoutExtension);
                
                videoSection.append(String.format("### %s\n\n", readableTitle));
                // Use simple relative path to videos directory since files are copied there
                videoSection.append(String.format("**File:** [%s](videos/%s)\n\n", filename, filename));
                videoSection.append("> **Catatan:** Untuk memutar video, klik link di atas atau buka file langsung menggunakan browser yang mendukung format WebM.\n\n");
            }
        } else {
            videoSection.append("Video tutorial belum tersedia. Silakan jalankan test dokumentasi terlebih dahulu:\n\n");
            videoSection.append("```bash\n");
            videoSection.append("mvn test -Dtest=PersonalCustomerAccountOpeningTutorialTest \\\n");
            videoSection.append("  -Dplaywright.headless=false \\\n");
            videoSection.append("  -Dplaywright.slowmo=2000 \\\n");
            videoSection.append("  -Dplaywright.record=true\n");
            videoSection.append("```\n\n");
        }
        
        videoSection.append("---\n\n");
        return videoSection.toString();
    }
    
    private String generateTipsSection() {
        return """
## Tips dan Catatan Penting

**Tips untuk Customer Service:**

1. **Verifikasi Data**
   - Selalu cocokkan data dengan dokumen asli
   - Pastikan ejaan nama sesuai dokumen identitas
   - Verifikasi nomor telepon dengan menghubungi nasabah

2. **Keamanan**
   - Jangan simpan informasi sensitif di tempat yang tidak aman
   - Selalu logout setelah selesai melayani nasabah
   - Laporkan aktivitas mencurigakan kepada supervisor

3. **Efisiensi**
   - Siapkan semua dokumen sebelum mulai input
   - Gunakan shortcut keyboard untuk mempercepat proses
   - Manfaatkan fitur auto-complete jika tersedia

4. **Customer Service Excellence**
   - Jelaskan setiap langkah kepada nasabah
   - Berikan informasi tentang produk yang dipilih
   - Pastikan nasabah memahami fitur dan ketentuan rekening

---

""";
    }
    
    private String generateTroubleshootingSection() {
        return """
## Pemecahan Masalah Umum

**Masalah yang Sering Terjadi:**

1. **Login Gagal**
   - Pastikan username dan password benar
   - Periksa caps lock dan layout keyboard
   - Hubungi IT jika terus gagal login

2. **Form Tidak Bisa Disimpan**
   - Periksa field yang wajib diisi (biasanya ditandai *)
   - Pastikan format tanggal dan nomor sudah benar
   - Refresh halaman dan coba lagi

3. **Nomor Rekening Tidak Ter-generate**
   - Pastikan semua data nasabah sudah tersimpan
   - Periksa koneksi internet
   - Hubungi supervisor jika masalah berlanjut

4. **Error Saat Upload Dokumen**
   - Pastikan format file sesuai (JPG, PNG, PDF)
   - Periksa ukuran file tidak melebihi limit
   - Scan ulang dokumen jika perlu

**Kontak Dukungan:**
- IT Help Desk: ext. 123
- Supervisor CS: ext. 456
- Manager Operasional: ext. 789

---

""";
    }
    
    private String generateFooter() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", 
            new Locale("id", "ID")));
        
        return String.format("""
## Informasi Dokumen

**Dibuat oleh:** Sistem Aplikasi Minibank  
**Tanggal:** %s  
**Versi:** 1.0  
**Format:** Markdown (.md)  

**Hak Cipta:** © 2025 Aplikasi Minibank - Sistem Perbankan Syariah  

---

*Panduan ini dibuat secara otomatis menggunakan Playwright Test Framework dan Java. Untuk pembaruan atau perbaikan, hubungi tim IT atau maintainer sistem.*

**Generator:** UserManualGenerator.java  
**Framework:** Playwright + Java  
**Template:** Indonesian Banking Documentation Standard  
""", today);
    }
    
    private void generateIndexFile() throws IOException {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", 
            new Locale("id", "ID")));
        
        String indexContent = String.format("""
# Dokumentasi Pengguna Aplikasi Minibank

## Panduan Yang Tersedia

1. **[Panduan Pembukaan Rekening Nasabah Personal](%s)**
   - Target: Customer Service (CS)
   - Proses: Pembukaan rekening nasabah personal
   - Format: Panduan lengkap dengan screenshot dan video

## Cara Menggunakan Panduan

1. Buka file panduan yang sesuai dengan kebutuhan Anda
2. Ikuti langkah-langkah secara berurutan
3. Lihat screenshot untuk referensi visual
4. Tonton video tutorial jika tersedia
5. Gunakan bagian troubleshooting jika mengalami masalah

## Pembaruan Panduan

Panduan ini dibuat secara otomatis dari test dokumentasi. Untuk memperbarui:

```bash
# 1. Jalankan test dokumentasi
mvn test -Dtest=PersonalCustomerAccountOpeningTutorialTest \\
  -Dplaywright.headless=false \\
  -Dplaywright.slowmo=2000 \\
  -Dplaywright.record=true

# 2. Generate ulang panduan
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.UserManualGenerator"
```

---

*Dibuat pada: %s*
""", OUTPUT_FILE, today);
        
        Path indexPath = outputPath.resolve("README.md");
        Files.write(indexPath, indexContent.getBytes("UTF-8"));
        System.out.println("📋 Index file dibuat: " + indexPath.toAbsolutePath());
    }
    
    private String makeVideoTitleReadable(String filename) {
        return filename
            .replaceAll("^\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_", "") // Remove timestamp
            .replaceAll("_", " ")
            .replaceAll("personalcustomeraccountopeningtutorialtest", "Tutorial Pembukaan Rekening")
            .replaceAll("tutorial step (\\d+)", "Langkah $1")
            .trim();
    }
    
    // Helper class to hold media files
    private static class MediaFiles {
        final List<Path> screenshots;
        final List<Path> videos;
        
        MediaFiles(List<Path> screenshots, List<Path> videos) {
            this.screenshots = screenshots;
            this.videos = videos;
        }
    }
}