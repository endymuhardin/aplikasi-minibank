package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * BranchRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@Execution(ExecutionMode.SAME_THREAD)
class BranchRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private BranchRepository branchRepository;

    @Test
    void shouldFindBranchByBranchCode() {
        logTestExecution("shouldFindBranchByBranchCode");
        
        // Given - Create unique test data
        Branch mainBranch = SimpleParallelTestDataFactory.createUniqueBranch("Jakarta Pusat", Branch.BranchStatus.ACTIVE, true);
        mainBranch.setBranchName("Kantor Pusat Jakarta");
        branchRepository.save(mainBranch);

        // When
        Optional<Branch> foundMainBranch = branchRepository.findByBranchCode(mainBranch.getBranchCode());
        Optional<Branch> nonExistentBranch = branchRepository.findByBranchCode("NONEXISTENT_" + System.currentTimeMillis());

        // Then
        assertThat(foundMainBranch).isPresent();
        assertThat(foundMainBranch.get().getBranchName()).isEqualTo("Kantor Pusat Jakarta");
        assertThat(foundMainBranch.get().getIsMainBranch()).isTrue();
        assertThat(nonExistentBranch).isEmpty();
    }

    @Test
    void shouldFindBranchesByStatus() {
        logTestExecution("shouldFindBranchesByStatus");
        
        // Given - Create unique test data
        Branch activeBranch1 = SimpleParallelTestDataFactory.createUniqueBranch("Jakarta", Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(activeBranch1);
        
        Branch activeBranch2 = SimpleParallelTestDataFactory.createUniqueBranch("Bandung", Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(activeBranch2);
        
        Branch inactiveBranch = SimpleParallelTestDataFactory.createUniqueBranch("Yogyakarta", Branch.BranchStatus.INACTIVE, false);
        branchRepository.save(inactiveBranch);

        // When
        List<Branch> activeBranches = branchRepository.findByStatusOrderByBranchCodeAsc(Branch.BranchStatus.ACTIVE);
        List<Branch> inactiveBranches = branchRepository.findByStatusOrderByBranchCodeAsc(Branch.BranchStatus.INACTIVE);

        // Then
        assertThat(activeBranches).hasSizeGreaterThanOrEqualTo(2);
        assertThat(inactiveBranches).hasSizeGreaterThanOrEqualTo(1);
        
        boolean hasActiveBranch1 = activeBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(activeBranch1.getBranchCode()));
        boolean hasActiveBranch2 = activeBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(activeBranch2.getBranchCode()));
        boolean hasInactiveBranch = inactiveBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(inactiveBranch.getBranchCode()));
            
        assertThat(hasActiveBranch1).isTrue();
        assertThat(hasActiveBranch2).isTrue();
        assertThat(hasInactiveBranch).isTrue();
    }

    @Test
    void shouldFindAllBranchesOrderedByCode() {
        logTestExecution("shouldFindAllBranchesOrderedByCode");
        
        // Given - Create unique test data with known ordering
        String uniquePrefix = String.valueOf(System.currentTimeMillis());
        
        Branch branch1 = SimpleParallelTestDataFactory.createUniqueBranch();
        branch1.setBranchCode("A" + uniquePrefix);
        branchRepository.save(branch1);
        
        Branch branch2 = SimpleParallelTestDataFactory.createUniqueBranch();
        branch2.setBranchCode("B" + uniquePrefix);
        branchRepository.save(branch2);
        
        Branch branch3 = SimpleParallelTestDataFactory.createUniqueBranch();
        branch3.setBranchCode("C" + uniquePrefix);
        branchRepository.save(branch3);

        // When
        List<Branch> allBranches = branchRepository.findAllByOrderByBranchCodeAsc();

        // Then
        assertThat(allBranches).hasSizeGreaterThanOrEqualTo(3);
        
        // Find our test branches and verify ordering
        List<Branch> ourBranches = allBranches.stream()
            .filter(b -> b.getBranchCode().contains(uniquePrefix))
            .toList();
            
        assertThat(ourBranches).hasSize(3);
        assertThat(ourBranches.get(0).getBranchCode()).isEqualTo("A" + uniquePrefix);
        assertThat(ourBranches.get(1).getBranchCode()).isEqualTo("B" + uniquePrefix);
        assertThat(ourBranches.get(2).getBranchCode()).isEqualTo("C" + uniquePrefix);
    }

    @Test
    void shouldSearchBranchesWithSearchTerm() {
        logTestExecution("shouldSearchBranchesWithSearchTerm");
        
        // Given - Create unique test data with unique search terms
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Branch jakartaBranch1 = SimpleParallelTestDataFactory.createUniqueBranch();
        jakartaBranch1.setBranchName("Jakarta " + uniqueTimestamp + " Pusat");
        jakartaBranch1.setCity("Jakarta Pusat");
        branchRepository.save(jakartaBranch1);
        
        Branch jakartaBranch2 = SimpleParallelTestDataFactory.createUniqueBranch();
        jakartaBranch2.setBranchName("Jakarta " + uniqueTimestamp + " Timur");
        jakartaBranch2.setCity("Jakarta Timur");
        branchRepository.save(jakartaBranch2);
        
        Branch bandungBranch = SimpleParallelTestDataFactory.createUniqueBranch();
        bandungBranch.setBranchName("Bandung " + uniqueTimestamp);
        bandungBranch.setCity("Bandung");
        branchRepository.save(bandungBranch);

        // When
        List<Branch> jakartaBranches = branchRepository.findBranchesWithSearchTerm("Jakarta " + uniqueTimestamp);
        List<Branch> bandungBranches = branchRepository.findBranchesWithSearchTerm("Bandung " + uniqueTimestamp);
        List<Branch> codeSearchResults = branchRepository.findBranchesWithSearchTerm(jakartaBranch1.getBranchCode());

        // Then
        assertThat(jakartaBranches).hasSizeGreaterThanOrEqualTo(2);
        assertThat(bandungBranches).hasSizeGreaterThanOrEqualTo(1);
        assertThat(codeSearchResults).hasSizeGreaterThanOrEqualTo(1);
        
        boolean hasJakartaBranch1 = jakartaBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(jakartaBranch1.getBranchCode()));
        boolean hasBandungBranch = bandungBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(bandungBranch.getBranchCode()));
        boolean hasCodeSearchResult = codeSearchResults.stream()
            .anyMatch(b -> b.getBranchCode().equals(jakartaBranch1.getBranchCode()));
            
        assertThat(hasJakartaBranch1).isTrue();
        assertThat(hasBandungBranch).isTrue();
        assertThat(hasCodeSearchResult).isTrue();
    }

    @Test
    void shouldFindBranchesWithPageableSearch() {
        logTestExecution("shouldFindBranchesWithPageableSearch");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Branch jakartaBranch1 = SimpleParallelTestDataFactory.createUniqueBranch();
        jakartaBranch1.setBranchName("Jakarta " + uniqueTimestamp + " Search Test 1");
        jakartaBranch1.setCity("Jakarta " + uniqueTimestamp);
        branchRepository.save(jakartaBranch1);
        
        Branch jakartaBranch2 = SimpleParallelTestDataFactory.createUniqueBranch();
        jakartaBranch2.setBranchName("Jakarta " + uniqueTimestamp + " Search Test 2");
        jakartaBranch2.setCity("Jakarta " + uniqueTimestamp);
        branchRepository.save(jakartaBranch2);
        
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Branch> jakartaPage = branchRepository.findByBranchCodeContainingIgnoreCaseOrBranchNameContainingIgnoreCaseOrCityContainingIgnoreCase(
            "Jakarta " + uniqueTimestamp, "Jakarta " + uniqueTimestamp, "Jakarta " + uniqueTimestamp, pageable);

        // Then
        assertThat(jakartaPage.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(jakartaPage.getContent()).hasSizeGreaterThanOrEqualTo(2);
        
        boolean hasJakartaBranch1 = jakartaPage.getContent().stream()
            .anyMatch(b -> b.getBranchCode().equals(jakartaBranch1.getBranchCode()));
        boolean hasJakartaBranch2 = jakartaPage.getContent().stream()
            .anyMatch(b -> b.getBranchCode().equals(jakartaBranch2.getBranchCode()));
            
        assertThat(hasJakartaBranch1).isTrue();
        assertThat(hasJakartaBranch2).isTrue();
    }

    @Test
    void shouldFindBranchesByStatusWithPagination() {
        logTestExecution("shouldFindBranchesByStatusWithPagination");
        
        // Given - Create unique test data
        Long initialActiveCount = branchRepository.countByStatus(Branch.BranchStatus.ACTIVE);
        Long initialInactiveCount = branchRepository.countByStatus(Branch.BranchStatus.INACTIVE);
        
        Branch activeBranch = SimpleParallelTestDataFactory.createUniqueBranch("Test City", Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(activeBranch);
        
        Branch inactiveBranch = SimpleParallelTestDataFactory.createUniqueBranch("Test City 2", Branch.BranchStatus.INACTIVE, false);
        branchRepository.save(inactiveBranch);
        
        Pageable pageable = PageRequest.of(0, 100);

        // When
        Page<Branch> activePage = branchRepository.findByStatus(Branch.BranchStatus.ACTIVE, pageable);
        Page<Branch> inactivePage = branchRepository.findByStatus(Branch.BranchStatus.INACTIVE, pageable);

        // Then
        assertThat(activePage.getTotalElements()).isEqualTo(initialActiveCount + 1);
        assertThat(inactivePage.getTotalElements()).isEqualTo(initialInactiveCount + 1);
    }

    @Test
    void shouldFindBranchesByCity() {
        logTestExecution("shouldFindBranchesByCity");
        
        // Given - Create unique test data
        String uniqueCity1 = "TestCity" + System.currentTimeMillis() + "A";
        String uniqueCity2 = "TestCity" + System.currentTimeMillis() + "B";
        
        Branch branch1 = SimpleParallelTestDataFactory.createUniqueBranch(uniqueCity1, Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(branch1);
        
        Branch branch2 = SimpleParallelTestDataFactory.createUniqueBranch(uniqueCity2, Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(branch2);
        
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Branch> city1Page = branchRepository.findByCityContainingIgnoreCase(uniqueCity1, pageable);
        Page<Branch> city2Page = branchRepository.findByCityContainingIgnoreCase(uniqueCity2, pageable);

        // Then
        assertThat(city1Page.getTotalElements()).isEqualTo(1);
        assertThat(city2Page.getTotalElements()).isEqualTo(1);
        assertThat(city1Page.getContent().get(0).getBranchCode()).isEqualTo(branch1.getBranchCode());
        assertThat(city2Page.getContent().get(0).getBranchCode()).isEqualTo(branch2.getBranchCode());
    }

    @Test
    void shouldCheckBranchCodeExistence() {
        logTestExecution("shouldCheckBranchCodeExistence");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);

        // When & Then
        assertThat(branchRepository.existsByBranchCode(branch.getBranchCode())).isTrue();
        assertThat(branchRepository.existsByBranchCode("NONEXISTENT_" + System.currentTimeMillis())).isFalse();
    }

    @Test
    void shouldFindMainBranch() {
        logTestExecution("shouldFindMainBranch");
        
        // Given - Create unique test data (check if main branch exists first)
        Optional<Branch> existingMainBranch = branchRepository.findByIsMainBranchTrue();
        
        if (existingMainBranch.isEmpty()) {
            Branch mainBranch = SimpleParallelTestDataFactory.createUniqueBranch("Jakarta Pusat", Branch.BranchStatus.ACTIVE, true);
            mainBranch.setBranchName("Kantor Pusat Jakarta");
            branchRepository.save(mainBranch);
        }

        // When
        Optional<Branch> foundMainBranch = branchRepository.findByIsMainBranchTrue();

        // Then
        assertThat(foundMainBranch).isPresent();
        assertThat(foundMainBranch.get().getIsMainBranch()).isTrue();
    }

    @Test
    void shouldCountBranchesByStatus() {
        logTestExecution("shouldCountBranchesByStatus");
        
        // Given - Create unique test data
        Long initialActiveCount = branchRepository.countByStatus(Branch.BranchStatus.ACTIVE);
        Long initialInactiveCount = branchRepository.countByStatus(Branch.BranchStatus.INACTIVE);
        Long initialClosedCount = branchRepository.countByStatus(Branch.BranchStatus.CLOSED);
        
        Branch activeBranch = SimpleParallelTestDataFactory.createUniqueBranch("Active City", Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(activeBranch);
        
        Branch inactiveBranch = SimpleParallelTestDataFactory.createUniqueBranch("Inactive City", Branch.BranchStatus.INACTIVE, false);
        branchRepository.save(inactiveBranch);

        // When
        Long activeCount = branchRepository.countByStatus(Branch.BranchStatus.ACTIVE);
        Long inactiveCount = branchRepository.countByStatus(Branch.BranchStatus.INACTIVE);
        Long closedCount = branchRepository.countByStatus(Branch.BranchStatus.CLOSED);

        // Then
        assertThat(activeCount).isEqualTo(initialActiveCount + 1);
        assertThat(inactiveCount).isEqualTo(initialInactiveCount + 1);
        assertThat(closedCount).isEqualTo(initialClosedCount);
    }

    @Test
    void shouldCountAllBranches() {
        logTestExecution("shouldCountAllBranches");
        
        // Given - Create unique test data
        Long initialCount = branchRepository.countAllBranches();
        
        Branch newBranch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(newBranch);

        // When
        Long totalCount = branchRepository.countAllBranches();

        // Then
        assertThat(totalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void shouldFindBranchesByCityExact() {
        logTestExecution("shouldFindBranchesByCityExact");
        
        // Given - Create unique test data
        String uniqueCity1 = "TestExactCity" + System.currentTimeMillis() + "A";
        String uniqueCity2 = "TestExactCity" + System.currentTimeMillis() + "B";
        
        Branch branch1 = SimpleParallelTestDataFactory.createUniqueBranch(uniqueCity1, Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(branch1);
        
        Branch branch2 = SimpleParallelTestDataFactory.createUniqueBranch(uniqueCity2, Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(branch2);

        // When
        List<Branch> city1Branches = branchRepository.findByCityIgnoreCaseOrderByBranchCodeAsc(uniqueCity1);
        List<Branch> city2Branches = branchRepository.findByCityIgnoreCaseOrderByBranchCodeAsc(uniqueCity2);

        // Then
        assertThat(city1Branches).hasSize(1);
        assertThat(city1Branches.get(0).getBranchCode()).isEqualTo(branch1.getBranchCode());
        assertThat(city2Branches).hasSize(1);
        assertThat(city2Branches.get(0).getBranchCode()).isEqualTo(branch2.getBranchCode());
    }

    @Test
    void shouldFindActiveBranches() {
        logTestExecution("shouldFindActiveBranches");
        
        // Given - Create unique test data
        int initialActiveCount = branchRepository.findActiveBranches().size();
        
        Branch activeBranch1 = SimpleParallelTestDataFactory.createUniqueBranch("Test Active City 1", Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(activeBranch1);
        
        Branch activeBranch2 = SimpleParallelTestDataFactory.createUniqueBranch("Test Active City 2", Branch.BranchStatus.ACTIVE, false);
        branchRepository.save(activeBranch2);
        
        Branch inactiveBranch = SimpleParallelTestDataFactory.createUniqueBranch("Test Inactive City", Branch.BranchStatus.INACTIVE, false);
        branchRepository.save(inactiveBranch);

        // When
        List<Branch> activeBranches = branchRepository.findActiveBranches();

        // Then
        assertThat(activeBranches).hasSizeGreaterThanOrEqualTo(initialActiveCount + 2);
        activeBranches.forEach(branch -> assertThat(branch.getStatus()).isEqualTo(Branch.BranchStatus.ACTIVE));
        
        boolean hasActiveBranch1 = activeBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(activeBranch1.getBranchCode()));
        boolean hasActiveBranch2 = activeBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(activeBranch2.getBranchCode()));
        boolean hasInactiveBranch = activeBranches.stream()
            .anyMatch(b -> b.getBranchCode().equals(inactiveBranch.getBranchCode()));
            
        assertThat(hasActiveBranch1).isTrue();
        assertThat(hasActiveBranch2).isTrue();
        assertThat(hasInactiveBranch).isFalse();
    }

    @Test
    void shouldSaveAndUpdateBranch() {
        logTestExecution("shouldSaveAndUpdateBranch");
        
        // Given - Create unique test data
        Branch newBranch = SimpleParallelTestDataFactory.createUniqueBranch();
        newBranch.setBranchName("Test Branch");
        newBranch.setCity("Test City");

        // When
        Branch savedBranch = branchRepository.save(newBranch);

        // Then
        assertThat(savedBranch.getId()).isNotNull();
        assertThat(savedBranch.getBranchCode()).isEqualTo(newBranch.getBranchCode());

        // Update test
        savedBranch.setBranchName("Updated Test Branch");
        savedBranch.setUpdatedBy("UPDATER");
        Branch updatedBranch = branchRepository.save(savedBranch);

        assertThat(updatedBranch.getBranchName()).isEqualTo("Updated Test Branch");
        assertThat(updatedBranch.getUpdatedBy()).isEqualTo("UPDATER");
    }

    @Test
    void shouldValidateBranchBusinessMethods() {
        logTestExecution("shouldValidateBranchBusinessMethods");
        
        // Given - Create test branch with specific properties
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branch.setBranchName("Test Branch");
        branch.setAddress("Jl. Test No. 123");
        branch.setCity("Test City");
        branch.setPostalCode("12345");
        branch.setCountry("Indonesia");

        // When & Then
        assertThat(branch.getDisplayName()).isEqualTo("Test Branch (" + branch.getBranchCode() + ")");
        assertThat(branch.getFullAddress()).isEqualTo("Jl. Test No. 123, Test City 12345");

        // Test with null values
        branch.setAddress(null);
        branch.setPostalCode(null);
        assertThat(branch.getFullAddress()).isEqualTo("Test City");

        // Test with non-Indonesia country
        branch.setCountry("Malaysia");
        assertThat(branch.getFullAddress()).isEqualTo("Test City, Malaysia");
    }
}