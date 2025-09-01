package id.ac.tazkia.minibank.util;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

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
 * from documentation tests and producing markdown-formatted guides in Indonesian using Mustache templates.
 */
public class UserManualGenerator {
    
    private static final String SCREENSHOT_DIR = "target/playwright-screenshots";
    private static final String VIDEO_DIR = "target/playwright-recordings";
    private static final String OUTPUT_DIR = "target/playwright-documentation";
    private static final String OUTPUT_FILE = "panduan-pembukaan-rekening-nasabah-personal.md";
    
    private final Path screenshotPath;
    private final Path videoPath;
    private final Path outputPath;
    private final MustacheFactory mustacheFactory;
    
    public UserManualGenerator() {
        this.screenshotPath = Paths.get(SCREENSHOT_DIR);
        this.videoPath = Paths.get(VIDEO_DIR);
        this.outputPath = Paths.get(OUTPUT_DIR);
        this.mustacheFactory = new DefaultMustacheFactory();
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
        
        // Generate markdown content using Mustache template
        String markdown = generateMarkdownFromTemplate(media);
        
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
    
    private String generateMarkdownFromTemplate(MediaFiles media) {
        try {
            Mustache template = mustacheFactory.compile("templates/user-manual/panduan-template.mustache");
            
            Map<String, Object> templateData = createTemplateData(media);
            
            StringWriter writer = new StringWriter();
            template.execute(writer, templateData);
            
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating markdown from template", e);
        }
    }
    
    private Map<String, Object> createTemplateData(MediaFiles media) {
        Map<String, Object> data = new HashMap<>();
        
        // Basic info
        data.put("dateCreated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"))));
        
        // Steps data
        data.put("steps", createStepsData(media.screenshots));
        
        // Video data
        data.put("hasVideos", !media.videos.isEmpty());
        if (!media.videos.isEmpty()) {
            data.put("videos", createVideosData(media.videos));
        }
        
        return data;
    }
    
    private List<Map<String, Object>> createStepsData(List<Path> screenshots) {
        List<Map<String, Object>> steps = new ArrayList<>();
        
        // Define the steps data
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
            Map<String, Object> step = new HashMap<>();
            step.put("stepNumber", i + 1);
            step.put("title", stepData[i][0]);
            step.put("description", stepData[i][1]);
            step.put("details", stepData[i][4]);
            step.put("isLastStep", i == stepData.length - 1);
            step.put("anchorTitle", stepData[i][0].toLowerCase().replaceAll(" ", "-").replaceAll("[^a-z0-9-]", ""));
            
            // Filter screenshots based on the pattern
            List<Map<String, Object>> stepScreenshots = new ArrayList<>();
            String screenshotPattern = stepData[i][3];
            String[] patterns = screenshotPattern.split("\\|");
            for (Path screenshot : screenshots) {
                String filename = screenshot.getFileName().toString();
                for (String pattern : patterns) {
                    if (filename.contains(pattern)) {
                        Map<String, Object> screenshotData = new HashMap<>();
                        screenshotData.put("filename", filename);
                        screenshotData.put("altText", getAltTextForScreenshot(filename, stepData[i][0]));
                        stepScreenshots.add(screenshotData);
                        break;
                    }
                }
            }
            
            // Sort screenshots to ensure proper order
            stepScreenshots.sort((a, b) -> ((String)a.get("filename")).compareTo((String)b.get("filename")));
            
            step.put("screenshots", stepScreenshots);
            steps.add(step);
        }
        
        return steps;
    }
    
    private List<Map<String, Object>> createVideosData(List<Path> videos) {
        List<Map<String, Object>> videosList = new ArrayList<>();
        
        for (Path video : videos) {
            Map<String, Object> videoData = new HashMap<>();
            String filename = video.getFileName().toString();
            String filenameWithoutExtension = filename.replace(".webm", "");
            String readableTitle = makeVideoTitleReadable(filenameWithoutExtension);
            
            videoData.put("filename", filename);
            videoData.put("title", readableTitle);
            videosList.add(videoData);
        }
        
        return videosList;
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
    
    
    
    
    
    private void generateIndexFile() throws IOException {
        try {
            Mustache indexTemplate = mustacheFactory.compile("templates/user-manual/index-template.mustache");
            
            Map<String, Object> indexData = new HashMap<>();
            indexData.put("dateCreated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"))));
            indexData.put("guideFilename", OUTPUT_FILE);
            
            StringWriter writer = new StringWriter();
            indexTemplate.execute(writer, indexData);
            
            Path indexPath = outputPath.resolve("README.md");
            Files.write(indexPath, writer.toString().getBytes("UTF-8"));
            System.out.println("📋 Index file dibuat: " + indexPath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Error generating index file from template", e);
        }
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