package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * CustomerRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@org.junit.jupiter.api.parallel.Execution(org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD)
class CustomerRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Test
    void shouldFindCustomerByCustomerNumber() {
        logTestExecution("shouldFindCustomerByCustomerNumber");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        customerRepository.save(corporateCustomer);
        
        // When
        Optional<Customer> foundPersonal = customerRepository.findByCustomerNumber(personalCustomer.getCustomerNumber());
        Optional<Customer> foundCorporate = customerRepository.findByCustomerNumber(corporateCustomer.getCustomerNumber());
        
        // Then
        assertThat(foundPersonal).isPresent();
        assertThat(foundPersonal.get().getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        assertThat(foundPersonal.get().getCustomerNumber()).isEqualTo(personalCustomer.getCustomerNumber());
        
        assertThat(foundCorporate).isPresent();
        assertThat(foundCorporate.get().getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
        assertThat(foundCorporate.get().getCustomerNumber()).isEqualTo(corporateCustomer.getCustomerNumber());
    }

    @Test
    void shouldFindCustomerByEmail() {
        logTestExecution("shouldFindCustomerByEmail");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        customerRepository.save(corporateCustomer);
        
        // When
        Optional<Customer> foundPersonal = customerRepository.findByEmail(personalCustomer.getEmail());
        Optional<Customer> foundCorporate = customerRepository.findByEmail(corporateCustomer.getEmail());
        
        // Then
        assertThat(foundPersonal).isPresent();
        assertThat(foundPersonal.get().getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        assertThat(foundPersonal.get().getEmail()).isEqualTo(personalCustomer.getEmail());
        
        assertThat(foundCorporate).isPresent();
        assertThat(foundCorporate.get().getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
        assertThat(foundCorporate.get().getEmail()).isEqualTo(corporateCustomer.getEmail());
    }

    @Test
    void shouldFindCustomersWithSearchTerm() {
        logTestExecution("shouldFindCustomersWithSearchTerm");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        customerRepository.save(corporateCustomer);
        
        // When - Search by parts of unique identifiers
        String commonTerm = personalCustomer.getEmail().substring(0, 5); // Get first 5 chars of email as common term
        List<Customer> emailResults = customerRepository.findCustomersWithSearchTerm(commonTerm);
        List<Customer> customerNumberResults = customerRepository.findCustomersWithSearchTerm(personalCustomer.getCustomerNumber().substring(0, 5));
        List<Customer> emptyResults = customerRepository.findCustomersWithSearchTerm("NONEXISTENT_TERM_" + System.currentTimeMillis());
        
        // Then
        assertThat(emailResults).hasSizeGreaterThanOrEqualTo(1); // Should find at least one customer
        assertThat(customerNumberResults).hasSizeGreaterThanOrEqualTo(1); // Should find at least the personal customer
        assertThat(emptyResults).isEmpty();
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        logTestExecution("shouldCheckExistenceByUniqueFields");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        // When & Then
        assertThat(customerRepository.existsByCustomerNumber(personalCustomer.getCustomerNumber())).isTrue();
        assertThat(customerRepository.existsByCustomerNumber("NONEXISTENT_" + personalCustomer.getCustomerNumber())).isFalse();
        
        assertThat(customerRepository.existsByEmail(personalCustomer.getEmail())).isTrue();
        assertThat(customerRepository.existsByEmail("nonexistent_" + personalCustomer.getEmail())).isFalse();
    }

    @Test
    void shouldCountAllCustomers() {
        logTestExecution("shouldCountAllCustomers");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        Long initialCount = customerRepository.countAllCustomers();
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        customerRepository.save(corporateCustomer);
        
        // When
        Long finalCount = customerRepository.countAllCustomers();
        
        // Then
        assertThat(finalCount).isEqualTo(initialCount + 2);
    }

    @Test
    void shouldFindAllCustomersPolymorphically() {
        logTestExecution("shouldFindAllCustomersPolymorphically");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        customerRepository.save(corporateCustomer);
        
        // When
        List<Customer> allCustomers = customerRepository.findAll();
        
        // Then - Check our specific customers exist in the results
        boolean hasOurPersonal = allCustomers.stream()
            .anyMatch(c -> c.getCustomerNumber().equals(personalCustomer.getCustomerNumber()));
        boolean hasOurCorporate = allCustomers.stream()
            .anyMatch(c -> c.getCustomerNumber().equals(corporateCustomer.getCustomerNumber()));
            
        assertThat(hasOurPersonal).isTrue();
        assertThat(hasOurCorporate).isTrue();
        assertThat(allCustomers.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldSaveAndRetrieveDifferentCustomerTypes() {
        logTestExecution("shouldSaveAndRetrieveDifferentCustomerTypes");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        
        // When
        Customer savedPersonal = customerRepository.save(personalCustomer);
        Customer savedCorporate = customerRepository.save(corporateCustomer);
        
        // Then
        assertThat(savedPersonal.getId()).isNotNull();
        assertThat(savedPersonal.getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        assertThat(savedPersonal).isInstanceOf(PersonalCustomer.class);

        assertThat(savedCorporate.getId()).isNotNull();
        assertThat(savedCorporate.getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
        assertThat(savedCorporate).isInstanceOf(CorporateCustomer.class);
    }

    @Test
    void shouldHandleEmptySearchTerm() {
        logTestExecution("shouldHandleEmptySearchTerm");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer personalCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(personalCustomer);
        
        CorporateCustomer corporateCustomer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(branch);
        customerRepository.save(corporateCustomer);
        
        // When
        List<Customer> results = customerRepository.findCustomersWithSearchTerm("");
        
        // Then - Should return all customers including ours when search term is empty
        assertThat(results.size()).isGreaterThanOrEqualTo(2);
        
        boolean hasOurPersonal = results.stream()
            .anyMatch(c -> c.getCustomerNumber().equals(personalCustomer.getCustomerNumber()));
        boolean hasOurCorporate = results.stream()
            .anyMatch(c -> c.getCustomerNumber().equals(corporateCustomer.getCustomerNumber()));
            
        assertThat(hasOurPersonal).isTrue();
        assertThat(hasOurCorporate).isTrue();
    }
}