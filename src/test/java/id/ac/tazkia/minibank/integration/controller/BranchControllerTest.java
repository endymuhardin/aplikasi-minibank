package id.ac.tazkia.minibank.integration.controller;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.BaseIntegrationTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class BranchControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BranchRepository branchRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        branchRepository.deleteAll();
    }

    @Test
    void shouldDisplayBranchListPage() throws Exception {
        // Given
        createTestBranches();

        // When & Then
        mockMvc.perform(get("/branch/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/list"))
                .andExpect(model().attributeExists("branches"))
                .andExpect(content().string(containsString("Branch Management")))
                .andExpect(content().string(containsString("TEST_HO001")))
                .andExpect(content().string(containsString("Test Kantor Pusat Jakarta")));
    }

    @Test
    void shouldDisplayBranchListWithFilters() throws Exception {
        // Given
        createTestBranches();

        // When & Then - Filter by status
        mockMvc.perform(get("/branch/list")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/list"))
                .andExpect(model().attributeExists("branches"))
                .andExpect(content().string(containsString("TEST_HO001")))
                .andExpect(content().string(containsString("TEST_JKT01")));

        // Filter by city
        mockMvc.perform(get("/branch/list")
                .param("city", "Jakarta"))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/list"))
                .andExpect(content().string(containsString("TEST_HO001")))
                .andExpect(content().string(containsString("TEST_JKT01")));

        // Search
        mockMvc.perform(get("/branch/list")
                .param("search", "Pusat"))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/list"))
                .andExpect(content().string(containsString("TEST_HO001")));
    }

    @Test
    void shouldDisplayCreateBranchForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/branch/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/form"))
                .andExpect(model().attributeExists("branch"))
                .andExpect(model().attributeExists("branchStatuses"))
                .andExpect(content().string(containsString("Create Branch")));
    }

    @Test
    void shouldCreateNewBranch() throws Exception {
        // When & Then
        mockMvc.perform(post("/branch/create")
                .param("branchCode", "TEST01")
                .param("branchName", "Test Branch")
                .param("address", "Jl. Test No. 123")
                .param("city", "Test City")
                .param("postalCode", "12345")
                .param("phoneNumber", "021-1234567")
                .param("email", "test@bankbsi.co.id")
                .param("managerName", "Test Manager")
                .param("status", "ACTIVE")
                .param("isMainBranch", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/branch/list"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify branch was created
        Optional<Branch> savedBranch = branchRepository.findByBranchCode("TEST01");
        assertThat(savedBranch).isPresent();
        assertThat(savedBranch.get().getBranchName()).isEqualTo("Test Branch");
        assertThat(savedBranch.get().getCity()).isEqualTo("Test City");
        assertThat(savedBranch.get().getStatus()).isEqualTo(Branch.BranchStatus.ACTIVE);
        assertThat(savedBranch.get().getIsMainBranch()).isFalse();
    }

    @Test
    void shouldFailToCreateBranchWithDuplicateCode() throws Exception {
        // Given
        createTestBranches();

        // When & Then
        mockMvc.perform(post("/branch/create")
                .param("branchCode", "TEST_HO001") // Duplicate code
                .param("branchName", "Test Branch")
                .param("status", "ACTIVE")
                .param("isMainBranch", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("branch", "branchCode"));
    }

    @Test
    void shouldFailToCreateSecondMainBranch() throws Exception {
        // Given
        createTestBranches(); // Already has a main branch

        // When & Then
        mockMvc.perform(post("/branch/create")
                .param("branchCode", "TEST01")
                .param("branchName", "Test Branch")
                .param("status", "ACTIVE")
                .param("isMainBranch", "true")) // Trying to create second main branch
                .andExpect(status().isOk())
                .andExpect(view().name("branch/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("branch", "isMainBranch"));
    }

    @Test
    void shouldDisplayBranchViewPage() throws Exception {
        // Given
        createTestBranches();
        Branch branch = branchRepository.findByBranchCode("TEST_HO001").orElseThrow();

        // When & Then
        mockMvc.perform(get("/branch/view/{id}", branch.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/view"))
                .andExpect(model().attributeExists("branch"))
                .andExpect(content().string(containsString("TEST_HO001")))
                .andExpect(content().string(containsString("Kantor Pusat Jakarta")))
                .andExpect(content().string(containsString("H. Ahmad Surya")));
    }

    @Test
    void shouldDisplayEditBranchForm() throws Exception {
        // Given
        createTestBranches();
        Branch branch = branchRepository.findByBranchCode("TEST_HO001").orElseThrow();

        // When & Then
        mockMvc.perform(get("/branch/edit/{id}", branch.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("branch/form"))
                .andExpect(model().attributeExists("branch"))
                .andExpect(model().attributeExists("branchStatuses"))
                .andExpect(content().string(containsString("Edit Branch")))
                .andExpect(content().string(containsString("TEST_HO001")));
    }

    @Test
    void shouldUpdateExistingBranch() throws Exception {
        // Given
        createTestBranches();
        Branch branch = branchRepository.findByBranchCode("TEST_HO001").orElseThrow();

        // When & Then
        mockMvc.perform(post("/branch/edit/{id}", branch.getId())
                .param("branchCode", "TEST_HO001")
                .param("branchName", "Updated Kantor Pusat")
                .param("address", "Jl. Sudirman Kav. 15-16")
                .param("city", "Jakarta Pusat")
                .param("postalCode", "10220")
                .param("phoneNumber", "021-29345678")
                .param("email", "kantor.pusat@bankbsi.co.id")
                .param("managerName", "H. Ahmad Surya Updated")
                .param("status", "ACTIVE")
                .param("isMainBranch", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/branch/list"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify branch was updated
        Branch updatedBranch = branchRepository.findByBranchCode("TEST_HO001").orElseThrow();
        assertThat(updatedBranch.getBranchName()).isEqualTo("Updated Kantor Pusat");
        assertThat(updatedBranch.getAddress()).isEqualTo("Jl. Sudirman Kav. 15-16");
        assertThat(updatedBranch.getManagerName()).isEqualTo("H. Ahmad Surya Updated");
    }

    @Test
    void shouldDeactivateBranch() throws Exception {
        // Given
        createTestBranches();
        Branch branch = branchRepository.findByBranchCode("TEST_JKT01").orElseThrow(); // Not main branch

        // When & Then
        mockMvc.perform(post("/branch/deactivate/{id}", branch.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/branch/list"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify branch was deactivated
        Branch deactivatedBranch = branchRepository.findByBranchCode("TEST_JKT01").orElseThrow();
        assertThat(deactivatedBranch.getStatus()).isEqualTo(Branch.BranchStatus.INACTIVE);
    }

    @Test
    void shouldNotDeactivateMainBranch() throws Exception {
        // Given
        createTestBranches();
        Branch mainBranch = branchRepository.findByBranchCode("TEST_HO001").orElseThrow(); // Main branch

        // When & Then
        mockMvc.perform(post("/branch/deactivate/{id}", mainBranch.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/branch/list"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Verify main branch was NOT deactivated
        Branch stillActiveBranch = branchRepository.findByBranchCode("TEST_HO001").orElseThrow();
        assertThat(stillActiveBranch.getStatus()).isEqualTo(Branch.BranchStatus.ACTIVE);
    }

    @Test
    void shouldActivateBranch() throws Exception {
        // Given
        createTestBranches();
        Branch branch = branchRepository.findByBranchCode("TEST_JKT01").orElseThrow();
        branch.setStatus(Branch.BranchStatus.INACTIVE);
        branchRepository.save(branch);

        // When & Then
        mockMvc.perform(post("/branch/activate/{id}", branch.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/branch/list"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify branch was activated
        Branch activatedBranch = branchRepository.findByBranchCode("TEST_JKT01").orElseThrow();
        assertThat(activatedBranch.getStatus()).isEqualTo(Branch.BranchStatus.ACTIVE);
    }

    @Test
    void shouldReturnNotFoundForNonExistentBranch() throws Exception {
        // When & Then
        mockMvc.perform(get("/branch/view/550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/branch/list"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    private void createTestBranches() {
        // Main branch (use unique code to avoid conflicts)
        Branch mainBranch = new Branch();
        mainBranch.setBranchCode("TEST_HO001");
        mainBranch.setBranchName("Test Kantor Pusat Jakarta");
        mainBranch.setAddress("Jl. Sudirman Kav. 10-11");
        mainBranch.setCity("Jakarta Pusat");
        mainBranch.setPostalCode("10220");
        mainBranch.setPhoneNumber("021-29345678");
        mainBranch.setEmail("test.kantor.pusat@bankbsi.co.id");
        mainBranch.setManagerName("H. Ahmad Surya");
        mainBranch.setStatus(Branch.BranchStatus.ACTIVE);
        mainBranch.setIsMainBranch(true);
        mainBranch.setCreatedBy("SYSTEM");
        branchRepository.save(mainBranch);

        // Regular branch (use unique code)
        Branch regularBranch = new Branch();
        regularBranch.setBranchCode("TEST_JKT01");
        regularBranch.setBranchName("Test Cabang Jakarta Timur");
        regularBranch.setAddress("Jl. Ahmad Yani No. 45");
        regularBranch.setCity("Jakarta Timur");
        regularBranch.setPostalCode("13230");
        regularBranch.setPhoneNumber("021-85761234");
        regularBranch.setEmail("test.jakarta.timur@bankbsi.co.id");
        regularBranch.setManagerName("Drs. Budi Pratama");
        regularBranch.setStatus(Branch.BranchStatus.ACTIVE);
        regularBranch.setIsMainBranch(false);
        regularBranch.setCreatedBy("SYSTEM");
        branchRepository.save(regularBranch);
    }
}