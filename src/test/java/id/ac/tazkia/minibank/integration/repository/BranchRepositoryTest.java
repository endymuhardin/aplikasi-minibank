package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BranchRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BranchRepository branchRepository;

    @BeforeEach
    void setUp() {
        branchRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindBranchByBranchCode() {
        // Given
        saveTestBranches();

        // When
        Optional<Branch> mainBranch = branchRepository.findByBranchCode("HO001");
        Optional<Branch> nonExistentBranch = branchRepository.findByBranchCode("NONEXISTENT");

        // Then
        assertThat(mainBranch).isPresent();
        assertThat(mainBranch.get().getBranchName()).isEqualTo("Kantor Pusat Jakarta");
        assertThat(mainBranch.get().getIsMainBranch()).isTrue();
        assertThat(nonExistentBranch).isEmpty();
    }

    @Test
    void shouldFindBranchesByStatus() {
        // Given
        saveTestBranches();

        // When
        List<Branch> activeBranches = branchRepository.findByStatusOrderByBranchCodeAsc(Branch.BranchStatus.ACTIVE);
        List<Branch> inactiveBranches = branchRepository.findByStatusOrderByBranchCodeAsc(Branch.BranchStatus.INACTIVE);

        // Then
        assertThat(activeBranches).hasSize(4);
        assertThat(activeBranches.get(0).getBranchCode()).isEqualTo("BDG01");
        assertThat(inactiveBranches).hasSize(1);
        assertThat(inactiveBranches.get(0).getBranchCode()).isEqualTo("YGY01");
    }

    @Test
    void shouldFindAllBranchesOrderedByCode() {
        // Given
        saveTestBranches();

        // When
        List<Branch> allBranches = branchRepository.findAllByOrderByBranchCodeAsc();

        // Then
        assertThat(allBranches).hasSize(5);
        assertThat(allBranches.get(0).getBranchCode()).isEqualTo("BDG01");
        assertThat(allBranches.get(1).getBranchCode()).isEqualTo("HO001");
        assertThat(allBranches.get(2).getBranchCode()).isEqualTo("JKT01");
        assertThat(allBranches.get(3).getBranchCode()).isEqualTo("SBY01");
        assertThat(allBranches.get(4).getBranchCode()).isEqualTo("YGY01");
    }

    @Test
    void shouldSearchBranchesWithSearchTerm() {
        // Given
        saveTestBranches();

        // When
        List<Branch> jakartaBranches = branchRepository.findBranchesWithSearchTerm("Jakarta");
        List<Branch> bandungBranches = branchRepository.findBranchesWithSearchTerm("Bandung");
        List<Branch> ho001Branches = branchRepository.findBranchesWithSearchTerm("HO001");

        // Then
        assertThat(jakartaBranches).hasSize(2);
        assertThat(bandungBranches).hasSize(1);
        assertThat(bandungBranches.get(0).getBranchCode()).isEqualTo("BDG01");
        assertThat(ho001Branches).hasSize(1);
        assertThat(ho001Branches.get(0).getBranchCode()).isEqualTo("HO001");
    }

    @Test
    void shouldFindBranchesWithPageableSearch() {
        // Given
        saveTestBranches();
        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<Branch> jakartaPage = branchRepository.findByBranchCodeContainingIgnoreCaseOrBranchNameContainingIgnoreCaseOrCityContainingIgnoreCase(
            "Jakarta", "Jakarta", "Jakarta", pageable);

        // Then
        assertThat(jakartaPage.getTotalElements()).isEqualTo(2);
        assertThat(jakartaPage.getContent()).hasSize(2);
        assertThat(jakartaPage.isFirst()).isTrue();
        assertThat(jakartaPage.isLast()).isTrue();
    }

    @Test
    void shouldFindBranchesByStatusWithPagination() {
        // Given
        saveTestBranches();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Branch> activePage = branchRepository.findByStatus(Branch.BranchStatus.ACTIVE, pageable);
        Page<Branch> inactivePage = branchRepository.findByStatus(Branch.BranchStatus.INACTIVE, pageable);

        // Then
        assertThat(activePage.getTotalElements()).isEqualTo(4);
        assertThat(inactivePage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldFindBranchesByCity() {
        // Given
        saveTestBranches();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Branch> jakartaPage = branchRepository.findByCityContainingIgnoreCase("Jakarta", pageable);
        Page<Branch> bandungPage = branchRepository.findByCityContainingIgnoreCase("Bandung", pageable);

        // Then
        assertThat(jakartaPage.getTotalElements()).isEqualTo(2);
        assertThat(bandungPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldCheckBranchCodeExistence() {
        // Given
        saveTestBranches();

        // When & Then
        assertThat(branchRepository.existsByBranchCode("HO001")).isTrue();
        assertThat(branchRepository.existsByBranchCode("NONEXISTENT")).isFalse();
    }

    @Test
    void shouldFindMainBranch() {
        // Given
        saveTestBranches();

        // When
        Optional<Branch> mainBranch = branchRepository.findByIsMainBranchTrue();

        // Then
        assertThat(mainBranch).isPresent();
        assertThat(mainBranch.get().getBranchCode()).isEqualTo("HO001");
        assertThat(mainBranch.get().getBranchName()).isEqualTo("Kantor Pusat Jakarta");
    }

    @Test
    void shouldCountBranchesByStatus() {
        // Given
        saveTestBranches();

        // When
        Long activeCount = branchRepository.countByStatus(Branch.BranchStatus.ACTIVE);
        Long inactiveCount = branchRepository.countByStatus(Branch.BranchStatus.INACTIVE);
        Long closedCount = branchRepository.countByStatus(Branch.BranchStatus.CLOSED);

        // Then
        assertThat(activeCount).isEqualTo(4);
        assertThat(inactiveCount).isEqualTo(1);
        assertThat(closedCount).isEqualTo(0);
    }

    @Test
    void shouldCountAllBranches() {
        // Given
        saveTestBranches();

        // When
        Long totalCount = branchRepository.countAllBranches();

        // Then
        assertThat(totalCount).isEqualTo(5);
    }

    @Test
    void shouldFindBranchesByCityExact() {
        // Given
        saveTestBranches();

        // When
        List<Branch> jakartaBranches = branchRepository.findByCityIgnoreCaseOrderByBranchCodeAsc("Jakarta Pusat");
        List<Branch> bandungBranches = branchRepository.findByCityIgnoreCaseOrderByBranchCodeAsc("Bandung");

        // Then
        assertThat(jakartaBranches).hasSize(1);
        assertThat(jakartaBranches.get(0).getBranchCode()).isEqualTo("HO001");
        assertThat(bandungBranches).hasSize(1);
        assertThat(bandungBranches.get(0).getBranchCode()).isEqualTo("BDG01");
    }

    @Test
    void shouldFindActiveBranches() {
        // Given
        saveTestBranches();

        // When
        List<Branch> activeBranches = branchRepository.findActiveBranches();

        // Then
        assertThat(activeBranches).hasSize(4);
        assertThat(activeBranches.get(0).getBranchCode()).isEqualTo("BDG01");
        assertThat(activeBranches.get(1).getBranchCode()).isEqualTo("HO001");
        assertThat(activeBranches.get(2).getBranchCode()).isEqualTo("JKT01");
        assertThat(activeBranches.get(3).getBranchCode()).isEqualTo("SBY01");
    }

    @Test
    void shouldSaveAndUpdateBranch() {
        // Given
        Branch newBranch = new Branch();
        newBranch.setBranchCode("TEST01");
        newBranch.setBranchName("Test Branch");
        newBranch.setCity("Test City");
        newBranch.setStatus(Branch.BranchStatus.ACTIVE);
        newBranch.setIsMainBranch(false);
        newBranch.setCreatedBy("TEST");

        // When
        Branch savedBranch = branchRepository.save(newBranch);
        entityManager.flush();

        // Then
        assertThat(savedBranch.getId()).isNotNull();
        assertThat(savedBranch.getBranchCode()).isEqualTo("TEST01");
        assertThat(savedBranch.getCreatedDate()).isNotNull();

        // Update test
        savedBranch.setBranchName("Updated Test Branch");
        savedBranch.setUpdatedBy("UPDATER");
        Branch updatedBranch = branchRepository.save(savedBranch);
        entityManager.flush();

        assertThat(updatedBranch.getBranchName()).isEqualTo("Updated Test Branch");
        assertThat(updatedBranch.getUpdatedBy()).isEqualTo("UPDATER");
        assertThat(updatedBranch.getUpdatedDate()).isNotNull();
    }

    @Test
    void shouldValidateBranchBusinessMethods() {
        // Given
        Branch branch = new Branch();
        branch.setBranchCode("TEST01");
        branch.setBranchName("Test Branch");
        branch.setAddress("Jl. Test No. 123");
        branch.setCity("Test City");
        branch.setPostalCode("12345");
        branch.setCountry("Indonesia");

        // When & Then
        assertThat(branch.getDisplayName()).isEqualTo("Test Branch (TEST01)");
        assertThat(branch.getFullAddress()).isEqualTo("Jl. Test No. 123, Test City 12345");

        // Test with null values
        branch.setAddress(null);
        branch.setPostalCode(null);
        assertThat(branch.getFullAddress()).isEqualTo("Test City");

        // Test with non-Indonesia country
        branch.setCountry("Malaysia");
        assertThat(branch.getFullAddress()).isEqualTo("Test City, Malaysia");
    }

    private void saveTestBranches() {
        // Main branch - HO001
        Branch mainBranch = new Branch();
        mainBranch.setBranchCode("HO001");
        mainBranch.setBranchName("Kantor Pusat Jakarta");
        mainBranch.setAddress("Jl. Sudirman Kav. 10-11");
        mainBranch.setCity("Jakarta Pusat");
        mainBranch.setPostalCode("10220");
        mainBranch.setPhoneNumber("021-29345678");
        mainBranch.setEmail("kantor.pusat@bankbsi.co.id");
        mainBranch.setManagerName("H. Ahmad Surya");
        mainBranch.setStatus(Branch.BranchStatus.ACTIVE);
        mainBranch.setIsMainBranch(true);
        mainBranch.setCreatedBy("SYSTEM");
        branchRepository.save(mainBranch);

        // Jakarta Timur branch - JKT01
        Branch jakartaBranch = new Branch();
        jakartaBranch.setBranchCode("JKT01");
        jakartaBranch.setBranchName("Cabang Jakarta Timur");
        jakartaBranch.setAddress("Jl. Ahmad Yani No. 45");
        jakartaBranch.setCity("Jakarta Timur");
        jakartaBranch.setPostalCode("13230");
        jakartaBranch.setPhoneNumber("021-85761234");
        jakartaBranch.setEmail("jakarta.timur@bankbsi.co.id");
        jakartaBranch.setManagerName("Drs. Budi Pratama");
        jakartaBranch.setStatus(Branch.BranchStatus.ACTIVE);
        jakartaBranch.setIsMainBranch(false);
        jakartaBranch.setCreatedBy("SYSTEM");
        branchRepository.save(jakartaBranch);

        // Bandung branch - BDG01
        Branch bandungBranch = new Branch();
        bandungBranch.setBranchCode("BDG01");
        bandungBranch.setBranchName("Cabang Bandung");
        bandungBranch.setAddress("Jl. Asia Afrika No. 88");
        bandungBranch.setCity("Bandung");
        bandungBranch.setPostalCode("40111");
        bandungBranch.setPhoneNumber("022-42056789");
        bandungBranch.setEmail("bandung@bankbsi.co.id");
        bandungBranch.setManagerName("H. Siti Nurhalimah");
        bandungBranch.setStatus(Branch.BranchStatus.ACTIVE);
        bandungBranch.setIsMainBranch(false);
        bandungBranch.setCreatedBy("SYSTEM");
        branchRepository.save(bandungBranch);

        // Surabaya branch - SBY01
        Branch surabayaBranch = new Branch();
        surabayaBranch.setBranchCode("SBY01");
        surabayaBranch.setBranchName("Cabang Surabaya");
        surabayaBranch.setAddress("Jl. Pemuda No. 123");
        surabayaBranch.setCity("Surabaya");
        surabayaBranch.setPostalCode("60271");
        surabayaBranch.setPhoneNumber("031-53419876");
        surabayaBranch.setEmail("surabaya@bankbsi.co.id");
        surabayaBranch.setManagerName("Ir. Wahyu Setiawan");
        surabayaBranch.setStatus(Branch.BranchStatus.ACTIVE);
        surabayaBranch.setIsMainBranch(false);
        surabayaBranch.setCreatedBy("SYSTEM");
        branchRepository.save(surabayaBranch);

        // Yogyakarta branch (inactive) - YGY01
        Branch yogyaBranch = new Branch();
        yogyaBranch.setBranchCode("YGY01");
        yogyaBranch.setBranchName("Cabang Yogyakarta");
        yogyaBranch.setAddress("Jl. Malioboro No. 56");
        yogyaBranch.setCity("Yogyakarta");
        yogyaBranch.setPostalCode("55213");
        yogyaBranch.setPhoneNumber("0274-562789");
        yogyaBranch.setEmail("yogyakarta@bankbsi.co.id");
        yogyaBranch.setManagerName("Dr. Retno Wulandari");
        yogyaBranch.setStatus(Branch.BranchStatus.INACTIVE);
        yogyaBranch.setIsMainBranch(false);
        yogyaBranch.setCreatedBy("SYSTEM");
        branchRepository.save(yogyaBranch);

        entityManager.flush();
    }
}