package id.ac.tazkia.minibank.functional.documentation;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.AccountManagementPage;
import id.ac.tazkia.minibank.functional.pages.ApprovalQueuePage;
import id.ac.tazkia.minibank.functional.pages.CustomerManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;
import id.ac.tazkia.minibank.functional.pages.TransactionPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Tutorial Test for New Customer Onboarding Process Documentation Generation.
 *
 * This test generates screenshots and videos for Indonesian user manual
 * documenting the complete customer onboarding process:
 * 1. Customer Service creates new customer (pending approval)
 * 2. Branch Manager reviews and approves the customer
 * 3. Teller processes first deposit transaction
 * 4. Print passbook/account statement
 *
 * Screenshots are automatically captured with descriptive Indonesian filenames.
 */
@Slf4j
@Tag("playwright-documentation")
@DisplayName("New Customer Onboarding Process - Documentation Test")
class ApprovalWorkflowTutorialTest extends BasePlaywrightTest {

    @Test
    @DisplayName("[DOC] Complete Customer Onboarding Tutorial")
    void tutorialCompleteApprovalWorkflow() {
        log.info("=".repeat(80));
        log.info("DOKUMENTASI: Proses Onboarding Nasabah Baru Lengkap");
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

        // ========================================
        // STEP 12: Logout Branch Manager
        // ========================================
        log.info("\n>>> LANGKAH 12: Logout Branch Manager");

        dashboardPage.logout();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(500);
        captureScreenshot("34_setelah_logout_manager");

        log.info("✓ Branch Manager berhasil logout");

        // ========================================
        // STEP 13: Login kembali sebagai CS untuk Pembukaan Rekening
        // ========================================
        log.info("\n>>> LANGKAH 13: Login kembali sebagai CS untuk Pembukaan Rekening");

        loginPage.navigateTo(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(500);
        captureScreenshot("35_halaman_login_cs_kembali");

        page.locator("#username").fill("cs1");
        page.waitForTimeout(500);
        page.locator("#password").fill("minibank123");
        page.waitForTimeout(500);

        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        assertTrue(dashboardPage.isDashboardLoaded(), "CS should be logged in successfully");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("36_dashboard_cs_login_kembali");

        log.info("✓ CS berhasil login kembali");

        // ========================================
        // STEP 14: Navigasi ke Pembukaan Rekening
        // ========================================
        log.info("\n>>> LANGKAH 14: Navigasi ke Pembukaan Rekening");

        AccountManagementPage accountPage = new AccountManagementPage(page);
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("37_halaman_pilih_nasabah_untuk_rekening");

        log.info("✓ Berhasil navigasi ke halaman pembukaan rekening");

        // ========================================
        // STEP 15: Pilih Nasabah yang Sudah Disetujui
        // ========================================
        log.info("\n>>> LANGKAH 15: Pilih Nasabah yang Sudah Disetujui");

        page.waitForTimeout(500);
        captureScreenshot("38_daftar_nasabah_aktif");

        // Select the first available customer (recently approved)
        page.locator("a:has-text('Open Account')").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("39_form_pembukaan_rekening");

        log.info("✓ Form pembukaan rekening berhasil dimuat");

        // ========================================
        // STEP 16: Isi Form Pembukaan Rekening
        // ========================================
        log.info("\n>>> LANGKAH 16: Isi Form Pembukaan Rekening");

        // Select product (WADIAH - savings account)
        // Get the first option that contains WADIAH
        String productValue = page.locator("#productId option:has-text('WADIAH')").first().getAttribute("value");
        page.locator("#productId").selectOption(productValue);
        page.waitForTimeout(500);
        captureScreenshot("40_produk_dipilih");

        // Enter initial deposit
        page.locator("#initialDeposit").fill("1000000");
        page.waitForTimeout(500);
        captureScreenshot("41_setoran_awal_terisi");

        // Enter account purpose
        page.locator("#accountName").fill("Tabungan Harian");
        page.waitForTimeout(500);
        captureScreenshot("42_form_rekening_lengkap");

        log.info("✓ Form pembukaan rekening berhasil diisi");

        // ========================================
        // STEP 17: Simpan Rekening (Pending Approval)
        // ========================================
        log.info("\n>>> LANGKAH 17: Simpan Rekening (Status: Pending Approval)");

        page.locator("#open-account-submit-btn").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("43_rekening_berhasil_disimpan");

        assertTrue(accountPage.isSuccessMessageVisible(), "Account should be created successfully");
        log.info("✓ Rekening berhasil dibuat dengan status PENDING_APPROVAL");

        page.waitForTimeout(500);
        captureScreenshot("44_pesan_sukses_rekening_pending");

        // ========================================
        // STEP 18: Logout CS
        // ========================================
        log.info("\n>>> LANGKAH 18: Logout CS");

        dashboardPage.logout();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(500);
        captureScreenshot("45_setelah_logout_cs");

        log.info("✓ CS berhasil logout");

        // ========================================
        // STEP 19: Login Branch Manager untuk Approval Rekening
        // ========================================
        log.info("\n>>> LANGKAH 19: Login Branch Manager untuk Approval Rekening");

        loginPage.navigateTo(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(500);
        captureScreenshot("46_halaman_login_manager_kembali");

        page.locator("#username").fill("manager1");
        page.waitForTimeout(500);
        page.locator("#password").fill("minibank123");
        page.waitForTimeout(500);

        dashboardPage = loginPage.loginWith("manager1", "minibank123");
        assertTrue(dashboardPage.isDashboardLoaded(), "Manager should be logged in successfully");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("47_dashboard_manager_login_kembali");

        log.info("✓ Branch Manager berhasil login kembali");

        // ========================================
        // STEP 20: Navigasi ke Approval Queue untuk Rekening
        // ========================================
        log.info("\n>>> LANGKAH 20: Navigasi ke Approval Queue untuk Rekening");

        approvalPage.navigateToQueue();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("48_approval_queue_rekening");

        int accountPendingCount = approvalPage.getPendingCount();
        assertTrue(accountPendingCount > 0, "Should have at least one account approval pending");
        log.info("✓ Ditemukan {} account approval yang pending", accountPendingCount);

        page.waitForTimeout(500);
        captureScreenshot("49_daftar_pending_approval_rekening");

        // ========================================
        // STEP 21: Review Detail Rekening
        // ========================================
        log.info("\n>>> LANGKAH 21: Review Detail Rekening");

        approvalPage.viewFirstApprovalDetail();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("50_halaman_detail_approval_rekening");

        // Verify it's account opening request
        assertEquals("ACCOUNT_OPENING", approvalPage.getRequestType(), "Should be account opening");
        page.waitForTimeout(500);
        captureScreenshot("51_informasi_request_rekening");

        assertTrue(approvalPage.isAccountDetailsVisible(), "Account details should be visible");
        page.waitForTimeout(500);
        captureScreenshot("52_detail_data_rekening");

        log.info("✓ Detail approval request rekening berhasil ditampilkan");

        // ========================================
        // STEP 22: Approve Rekening
        // ========================================
        log.info("\n>>> LANGKAH 22: Approve Rekening");

        page.locator("#approve-review-notes").fill("Rekening disetujui untuk aktivasi.");
        page.waitForTimeout(500);
        captureScreenshot("53_catatan_review_rekening_terisi");

        approvalPage.approveRequest("Rekening disetujui untuk aktivasi.");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("54_approval_rekening_berhasil");

        assertTrue(approvalPage.isSuccessMessageVisible(), "Should show success message");
        log.info("✓ Rekening berhasil diaktifkan");

        page.waitForTimeout(500);
        captureScreenshot("55_pesan_sukses_approval_rekening");

        // ========================================
        // STEP 23: Logout Branch Manager
        // ========================================
        log.info("\n>>> LANGKAH 23: Logout Branch Manager");

        dashboardPage.logout();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(500);
        captureScreenshot("56_setelah_logout_manager_final");

        log.info("✓ Branch Manager berhasil logout");

        // ========================================
        // STEP 24: Login sebagai Teller
        // ========================================
        log.info("\n>>> LANGKAH 24: Login sebagai Teller");

        loginPage.navigateTo(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(500);
        captureScreenshot("57_halaman_login_teller");

        page.locator("#username").fill("teller1");
        page.waitForTimeout(500);
        captureScreenshot("58_username_teller_terisi");

        page.locator("#password").fill("minibank123");
        page.waitForTimeout(500);
        captureScreenshot("59_password_teller_terisi");

        dashboardPage = loginPage.loginWith("teller1", "minibank123");
        assertTrue(dashboardPage.isDashboardLoaded(), "Teller should be logged in successfully");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        captureScreenshot("60_dashboard_teller_berhasil_login");

        log.info("✓ Teller berhasil login");

        // ========================================
        // STEP 25: Navigasi ke Cash Deposit
        // ========================================
        log.info("\n>>> LANGKAH 25: Navigasi ke Cash Deposit");

        TransactionPage transactionPage = new TransactionPage(page);
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("61_halaman_pilih_rekening_deposit");

        log.info("✓ Berhasil navigasi ke halaman cash deposit");

        // ========================================
        // STEP 26: Cari dan Pilih Rekening
        // ========================================
        log.info("\n>>> LANGKAH 26: Cari dan Pilih Rekening Nasabah");

        // Wait for accounts list to be visible
        page.waitForSelector("#accounts-list", new Page.WaitForSelectorOptions().setTimeout(10000));
        page.waitForTimeout(500);
        captureScreenshot("62_daftar_rekening_tersedia");

        // Get the first active account (that was just approved)
        String accountNumber = page.locator("#accounts-list .account-card .account-number").first().textContent().trim();
        log.info("✓ Memilih rekening: {}", accountNumber);

        transactionPage.searchAndSelectAccount(accountNumber);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("63_form_deposit_terbuka");

        log.info("✓ Form deposit berhasil dimuat");

        // ========================================
        // STEP 27: Isi Form Setoran Pertama
        // ========================================
        log.info("\n>>> LANGKAH 27: Isi Form Setoran Pertama");

        page.locator("#amount").fill("1000000");
        page.waitForTimeout(500);
        captureScreenshot("64_jumlah_setoran_terisi");

        page.locator("#description").fill("Setoran awal pembukaan rekening");
        page.waitForTimeout(500);
        captureScreenshot("65_keterangan_setoran_terisi");

        page.waitForTimeout(500);
        captureScreenshot("66_form_deposit_lengkap");

        log.info("✓ Form setoran berhasil diisi (createdBy otomatis dari user login)");

        // ========================================
        // STEP 28: Proses Setoran
        // ========================================
        log.info("\n>>> LANGKAH 28: Proses Setoran");

        page.locator("#process-deposit-btn").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("67_setoran_berhasil_diproses");

        assertTrue(transactionPage.isSuccessMessageVisible(), "Deposit should be successful");
        log.info("✓ Setoran berhasil diproses");

        page.waitForTimeout(500);
        captureScreenshot("68_pesan_sukses_setoran");

        // ========================================
        // STEP 29: Navigasi ke Cetak Buku Tabungan
        // ========================================
        log.info("\n>>> LANGKAH 29: Navigasi ke Cetak Buku Tabungan");

        page.navigate(baseUrl + "/passbook/select-account");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("69_halaman_pilih_rekening_cetak");

        log.info("✓ Berhasil navigasi ke halaman cetak buku tabungan");

        // ========================================
        // STEP 30: Pilih Rekening untuk Cetak Buku
        // ========================================
        log.info("\n>>> LANGKAH 30: Pilih Rekening untuk Cetak Buku");

        page.waitForTimeout(500);
        captureScreenshot("70_daftar_rekening_cetak");

        // Click the first account's print link (it's an <a> tag, not button)
        page.locator("a[id^='print-passbook-']").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        captureScreenshot("71_tampilan_buku_tabungan");

        log.info("✓ Buku tabungan berhasil ditampilkan");

        // ========================================
        // STEP 31: Review Mutasi Rekening
        // ========================================
        log.info("\n>>> LANGKAH 31: Review Mutasi Rekening");

        page.waitForTimeout(500);
        captureScreenshot("72_detail_mutasi_rekening");

        page.waitForTimeout(500);
        captureScreenshot("73_tutorial_selesai");

        log.info("✓ Tutorial selesai - Proses lengkap dari registrasi hingga setoran pertama");

        // ========================================
        // Summary
        // ========================================
        log.info("\n" + "=".repeat(80));
        log.info("RANGKUMAN TUTORIAL - PROSES ONBOARDING NASABAH BARU LENGKAP:");
        log.info("1. CS login dan membuat nasabah baru → PENDING_APPROVAL");
        log.info("2. Branch Manager review dan approve nasabah → APPROVED & ACTIVE");
        log.info("3. CS login kembali dan membuat rekening → PENDING_APPROVAL");
        log.info("4. Branch Manager review dan approve rekening → APPROVED & ACTIVE");
        log.info("5. Teller login dan melakukan setoran pertama → saldo ter-update");
        log.info("6. Cetak buku tabungan dengan mutasi transaksi lengkap");
        log.info("");
        log.info("Total Langkah: 31 | Total Screenshots: 73");
        log.info("=".repeat(80));
    }

    /**
     * Helper method to capture screenshot with descriptive name
     */
    private void captureScreenshot(String description) {
        String timestamp = String.format("%tF_%<tH-%<tM-%<tS", System.currentTimeMillis());
        String filename = String.format("%s_customer_onboarding_%s.png", timestamp, description);
        page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("target/playwright-screenshots/" + filename))
            .setFullPage(false));
        log.debug("📷 Screenshot: {}", filename);
    }
}
