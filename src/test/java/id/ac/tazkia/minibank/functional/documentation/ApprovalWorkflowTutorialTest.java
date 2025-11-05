package id.ac.tazkia.minibank.functional.documentation;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.ApprovalQueuePage;
import id.ac.tazkia.minibank.functional.pages.CustomerManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.playwright.options.LoadState;

/**
 * Tutorial Test for Approval Workflow Documentation Generation.
 *
 * This test generates screenshots and videos for Indonesian user manual
 * documenting the complete approval workflow process:
 * 1. Customer Service creates new customer (pending approval)
 * 2. Branch Manager reviews and approves the customer
 *
 * Screenshots are automatically captured with descriptive Indonesian filenames.
 */
@Slf4j
@Tag("playwright-documentation")
@DisplayName("Approval Workflow Tutorial - Documentation Test")
class ApprovalWorkflowTutorialTest extends BasePlaywrightTest {

    @Test
    @DisplayName("[DOC] Complete Approval Workflow Tutorial")
    void tutorialCompleteApprovalWorkflow() {
        log.info("=".repeat(80));
        log.info("DOKUMENTASI: Tutorial Lengkap Approval Workflow");
        log.info("=".repeat(80));

        // ========================================
        // STEP 1: Login sebagai Customer Service
        // ========================================
        log.info("\n>>> LANGKAH 1: Login sebagai Customer Service");

        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);

        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("01_halaman_login");

        // Fill username
        page.locator("#username").fill("cs1");
        page.waitForTimeout(500);
        captureScreenshot("02_username_terisi");

        // Fill password
        page.locator("#password").fill("minibank123");
        page.waitForTimeout(500);
        captureScreenshot("03_password_terisi");

        // Click login button
        captureScreenshot("04_siap_login");
        DashboardPage dashboardPage = loginPage.loginWith("cs1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "CS should be logged in successfully");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("05_dashboard_cs_berhasil_login");

        log.info("✓ Customer Service berhasil login");

        // ========================================
        // STEP 2: Navigasi ke Menu Customer Management
        // ========================================
        log.info("\n>>> LANGKAH 2: Navigasi ke Menu Customer Management");

        page.waitForTimeout(1000);
        captureScreenshot("06_menu_navigasi_dashboard");

        CustomerManagementPage customerPage = new CustomerManagementPage(page);
        customerPage.navigateToAddCustomer(baseUrl);

        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("07_halaman_pilih_jenis_nasabah");

        log.info("✓ Berhasil navigasi ke halaman pembuatan nasabah");

        // ========================================
        // STEP 3: Pilih Jenis Nasabah Personal
        // ========================================
        log.info("\n>>> LANGKAH 3: Pilih Jenis Nasabah Personal");

        page.waitForTimeout(500);
        captureScreenshot("08_pilihan_jenis_nasabah");

        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(500);
        captureScreenshot("09_form_nasabah_personal_kosong");

        log.info("✓ Form nasabah personal berhasil dimuat");

        // ========================================
        // STEP 4: Mengisi Data Nasabah Personal
        // ========================================
        log.info("\n>>> LANGKAH 4: Mengisi Data Nasabah Personal");

        // Create unique customer data
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueSuffix = timestamp.substring(timestamp.length() - 6);
        String uniqueName = "Budi Santoso"; // Valid name without numbers
        String uniqueIdentityNumber = "3201020202" + uniqueSuffix; // 16 digits
        String uniqueEmail = "budi" + uniqueSuffix + "@email.com";
        String uniquePhone = "0812346" + uniqueSuffix; // 13 digits

        // Fill the form with FR.002 compliant data
        customerPage.fillPersonalCustomerFormExtended(
            // Basic Personal Information
            uniqueName, "Approval", uniqueIdentityNumber, "KTP",
            "1985-05-15", "Bandung", "MALE", "Ibu Budi",
            // Personal Data (FR.002)
            "S1", "ISLAM", "KAWIN", "3",
            // Identity Information (FR.002)
            "WNI", "Domiciled", "2030-12-31",
            // Contact Information
            uniqueEmail, uniquePhone, "Jl. Soekarno Hatta No. 456", "Bandung", "Jawa Barat", "40132",
            // Employment Data (FR.002)
            "Wiraswasta", "PT. Maju Jaya", "Jl. Asia Afrika No. 10", "Perdagangan",
            "15000000", "Usaha", "Transaksi bisnis rutin", "30", "20000000"
        );

        page.waitForTimeout(500);
        captureScreenshot("10_form_data_pribadi_terisi");

        page.waitForTimeout(500);
        captureScreenshot("11_form_data_kontak_terisi");

        page.waitForTimeout(500);
        captureScreenshot("12_form_lengkap_siap_disimpan");

        log.info("✓ Data nasabah berhasil diisi");

        // ========================================
        // STEP 5: Simpan Nasabah (Pending Approval)
        // ========================================
        log.info("\n>>> LANGKAH 5: Simpan Nasabah (Status: Pending Approval)");

        captureScreenshot("13_tombol_simpan");

        customerPage.clickSave();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(1000);
        captureScreenshot("14_nasabah_berhasil_disimpan");

        // Verify success
        assertTrue(customerPage.isOperationSuccessful(), "Customer should be created successfully");

        if (customerPage.isSuccessMessageVisible()) {
            String successMsg = customerPage.getSuccessMessage();
            page.waitForTimeout(500);
            captureScreenshot("15_pesan_sukses_pending_approval");
            log.info("✓ Success message: " + successMsg);
        }

        log.info("✓ Nasabah berhasil dibuat dengan status PENDING_APPROVAL");

        // ========================================
        // STEP 6: Logout Customer Service
        // ========================================
        log.info("\n>>> LANGKAH 6: Logout Customer Service");

        page.waitForTimeout(1000);
        captureScreenshot("16_dashboard_sebelum_logout");

        dashboardPage.logout();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(500);
        captureScreenshot("17_setelah_logout_cs");

        log.info("✓ Customer Service berhasil logout");

        // ========================================
        // STEP 7: Login sebagai Branch Manager
        // ========================================
        log.info("\n>>> LANGKAH 7: Login sebagai Branch Manager");

        loginPage.navigateTo(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(500);
        captureScreenshot("18_halaman_login_manager");

        // Fill username
        page.locator("#username").fill("manager1");
        page.waitForTimeout(500);
        captureScreenshot("19_username_manager_terisi");

        // Fill password
        page.locator("#password").fill("minibank123");
        page.waitForTimeout(500);
        captureScreenshot("20_password_manager_terisi");

        // Login
        dashboardPage = loginPage.loginWith("manager1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Manager should be logged in successfully");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("21_dashboard_manager_berhasil_login");

        log.info("✓ Branch Manager berhasil login");

        // ========================================
        // STEP 8: Navigasi ke Approval Queue
        // ========================================
        log.info("\n>>> LANGKAH 8: Navigasi ke Approval Queue");

        page.waitForTimeout(1000);
        captureScreenshot("22_menu_approval_queue");

        ApprovalQueuePage approvalPage = new ApprovalQueuePage(page);
        approvalPage.navigateToQueue();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("23_halaman_approval_queue");

        int pendingCount = approvalPage.getPendingCount();
        assertTrue(pendingCount > 0, "Should have at least one pending approval");
        log.info("✓ Ditemukan {} approval yang pending", pendingCount);

        page.waitForTimeout(500);
        captureScreenshot("24_daftar_pending_approval");

        // ========================================
        // STEP 9: Lihat Detail Approval Request
        // ========================================
        log.info("\n>>> LANGKAH 9: Lihat Detail Approval Request");

        approvalPage.viewFirstApprovalDetail();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(1000);
        captureScreenshot("25_halaman_detail_approval");

        // Verify it's customer creation request
        assertEquals("CUSTOMER_CREATION", approvalPage.getRequestType(), "Should be customer creation");
        page.waitForTimeout(500);
        captureScreenshot("26_informasi_request");

        assertTrue(approvalPage.isCustomerDetailsVisible(), "Customer details should be visible");
        page.waitForTimeout(500);
        captureScreenshot("27_detail_data_nasabah");

        assertTrue(approvalPage.isApprovalActionsVisible(), "Approval actions should be visible");
        page.waitForTimeout(500);
        captureScreenshot("28_form_approval_actions");

        log.info("✓ Detail approval request berhasil ditampilkan");

        // ========================================
        // STEP 10: Approve Request
        // ========================================
        log.info("\n>>> LANGKAH 10: Approve Request");

        // Fill review notes
        page.locator("#approve-review-notes").fill("Data nasabah lengkap dan sesuai dokumen. Disetujui untuk aktivasi.");
        page.waitForTimeout(500);
        captureScreenshot("29_catatan_review_terisi");

        page.waitForTimeout(500);
        captureScreenshot("30_siap_approve");

        approvalPage.approveRequest("Data nasabah lengkap dan sesuai dokumen. Disetujui untuk aktivasi.");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(1000);
        captureScreenshot("31_approval_berhasil");

        // Verify success
        assertTrue(approvalPage.isSuccessMessageVisible(), "Should show success message");
        String approvalMsg = approvalPage.getSuccessMessage();
        log.info("✓ Approval message: " + approvalMsg);

        page.waitForTimeout(500);
        captureScreenshot("32_pesan_sukses_approval");

        // ========================================
        // STEP 11: Verifikasi Queue Updated
        // ========================================
        log.info("\n>>> LANGKAH 11: Verifikasi Approval Queue Updated");

        page.waitForTimeout(1000);
        int newPendingCount = approvalPage.getPendingCount();
        log.info("✓ Pending count setelah approval: {}", newPendingCount);

        captureScreenshot("33_queue_setelah_approval");

        page.waitForTimeout(500);
        captureScreenshot("34_tutorial_selesai");

        log.info("✓ Approval workflow selesai - nasabah berhasil diaktifkan");

        // ========================================
        // Summary
        // ========================================
        log.info("\n" + "=".repeat(80));
        log.info("RANGKUMAN TUTORIAL:");
        log.info("1. Customer Service login dan membuat nasabah baru");
        log.info("2. Nasabah tersimpan dengan status PENDING_APPROVAL");
        log.info("3. Branch Manager login dan melihat approval queue");
        log.info("4. Branch Manager review detail nasabah");
        log.info("5. Branch Manager approve request");
        log.info("6. Nasabah berhasil diaktifkan (status: APPROVED & ACTIVE)");
        log.info("=".repeat(80));
    }

    /**
     * Helper method to capture screenshot with descriptive name
     */
    private void captureScreenshot(String description) {
        String timestamp = String.format("%tF_%<tH-%<tM-%<tS", System.currentTimeMillis());
        String filename = String.format("%s_approval_workflow_%s.png", timestamp, description);
        page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("target/playwright-screenshots/" + filename))
            .setFullPage(false));
        log.debug("📷 Screenshot: {}", filename);
    }
}
