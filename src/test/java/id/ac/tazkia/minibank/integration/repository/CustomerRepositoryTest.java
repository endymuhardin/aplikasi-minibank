package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PersonalCustomerRepository personalCustomerRepository;
    
    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    private Branch testBranch;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        personalCustomerRepository.deleteAll();
        corporateCustomerRepository.deleteAll();
        branchRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // Create test branch
        testBranch = new Branch();
        testBranch.setBranchCode("TEST");
        testBranch.setBranchName("Test Branch");
        testBranch.setAddress("Test Address");
        testBranch.setCity("Test City");
        testBranch.setCountry("Indonesia");
        testBranch.setStatus(Branch.BranchStatus.ACTIVE);
        testBranch.setCreatedBy("TEST");
        testBranch = branchRepository.save(testBranch);
        entityManager.flush();
    }

    @Test
    void shouldFindCustomerByCustomerNumber() {
        // Given
        saveTestCustomers();

        // When
        Optional<Customer> personalCustomer = customerRepository.findByCustomerNumber("C1000001");
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000003");

        // Then
        assertThat(personalCustomer).isPresent();
        assertThat(personalCustomer.get().getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        
        assertThat(corporateCustomer).isPresent();
        assertThat(corporateCustomer.get().getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
    }

    @Test
    void shouldFindCustomerByEmail() {
        // Given
        saveTestCustomers();

        // When
        Optional<Customer> personalCustomer = customerRepository.findByEmail("ahmad.suharto@email.com");
        Optional<Customer> corporateCustomer = customerRepository.findByEmail("info@teknologimaju.com");

        // Then
        assertThat(personalCustomer).isPresent();
        assertThat(personalCustomer.get().getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        
        assertThat(corporateCustomer).isPresent();
        assertThat(corporateCustomer.get().getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
    }

    @Test
    void shouldFindCustomersWithSearchTerm() {
        // Given
        saveTestCustomers();

        // When - Search by email term
        List<Customer> emailResults = customerRepository.findCustomersWithSearchTerm("ahmad");
        List<Customer> customerNumberResults = customerRepository.findCustomersWithSearchTerm("C1000001");
        List<Customer> emptyResults = customerRepository.findCustomersWithSearchTerm("nonexistent");

        // Then
        assertThat(emailResults).hasSizeGreaterThan(0);
        assertThat(emailResults.get(0).getEmail()).containsIgnoringCase("ahmad");
        assertThat(emailResults.get(0).getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        
        assertThat(customerNumberResults).hasSizeGreaterThan(0);
        assertThat(customerNumberResults.get(0).getCustomerNumber()).isEqualTo("C1000001");
        
        assertThat(emptyResults).isEmpty();
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // Given
        saveTestCustomers();

        // When & Then
        assertThat(customerRepository.existsByCustomerNumber("C1000001")).isTrue();
        assertThat(customerRepository.existsByCustomerNumber("C9999999")).isFalse();
        
        assertThat(customerRepository.existsByEmail("ahmad.suharto@email.com")).isTrue();
        assertThat(customerRepository.existsByEmail("nonexistent@email.com")).isFalse();
    }

    @Test
    void shouldCountAllCustomers() {
        // Given
        saveTestCustomers();

        // When
        Long count = customerRepository.countAllCustomers();

        // Then
        assertThat(count).isEqualTo(2); // One personal, one corporate
    }

    @Test
    void shouldFindAllCustomersPolymorphically() {
        // Given
        saveTestCustomers();

        // When
        List<Customer> allCustomers = customerRepository.findAll();

        // Then
        assertThat(allCustomers).hasSize(2);
        
        // Verify we have both types
        boolean hasPersonal = allCustomers.stream()
            .anyMatch(c -> c.getCustomerType() == Customer.CustomerType.PERSONAL);
        boolean hasCorporate = allCustomers.stream()
            .anyMatch(c -> c.getCustomerType() == Customer.CustomerType.CORPORATE);
            
        assertThat(hasPersonal).isTrue();
        assertThat(hasCorporate).isTrue();
    }

    @Test
    void shouldSaveAndRetrieveDifferentCustomerTypes() {
        // Given
        PersonalCustomer personalCustomer = new PersonalCustomer();
        personalCustomer.setCustomerNumber("P001");
        personalCustomer.setFirstName("John");
        personalCustomer.setLastName("Doe");
        personalCustomer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        personalCustomer.setIdentityNumber("1234567890");
        personalCustomer.setIdentityType(Customer.IdentityType.KTP);
        personalCustomer.setEmail("john.doe@email.com");
        personalCustomer.setPhoneNumber("081234567890");
        personalCustomer.setAddress("Test Address");
        personalCustomer.setCity("Test City");
        personalCustomer.setPostalCode("12345");
        personalCustomer.setCountry("Indonesia");
        personalCustomer.setCreatedBy("TEST");
        personalCustomer.setBranch(testBranch);

        CorporateCustomer corporateCustomer = new CorporateCustomer();
        corporateCustomer.setCustomerNumber("C001");
        corporateCustomer.setCompanyName("Test Company");
        corporateCustomer.setCompanyRegistrationNumber("123456789");
        corporateCustomer.setTaxIdentificationNumber("12.345.678.9-123.456");
        corporateCustomer.setEmail("test@company.com");
        corporateCustomer.setPhoneNumber("021123456");
        corporateCustomer.setAddress("Company Address");
        corporateCustomer.setCity("Jakarta");
        corporateCustomer.setPostalCode("54321");
        corporateCustomer.setCountry("Indonesia");
        corporateCustomer.setCreatedBy("TEST");
        corporateCustomer.setBranch(testBranch);

        // When
        Customer savedPersonal = customerRepository.save(personalCustomer);
        Customer savedCorporate = customerRepository.save(corporateCustomer);
        entityManager.flush();

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
        // Given
        saveTestCustomers();

        // When
        List<Customer> results = customerRepository.findCustomersWithSearchTerm("");

        // Then
        assertThat(results).hasSize(2); // Should return all customers when search term is empty
    }

    private void saveTestCustomers() {
        // Personal Customer for polymorphic testing
        PersonalCustomer personal1 = new PersonalCustomer();
        personal1.setCustomerNumber("C1000001");
        personal1.setFirstName("Ahmad");
        personal1.setLastName("Suharto");
        personal1.setDateOfBirth(LocalDate.of(1985, 3, 15));
        personal1.setIdentityNumber("3271081503850001");
        personal1.setIdentityType(Customer.IdentityType.KTP);
        personal1.setEmail("ahmad.suharto@email.com");
        personal1.setPhoneNumber("081234567890");
        personal1.setAddress("Jl. Sudirman No. 123");
        personal1.setCity("Jakarta");
        personal1.setPostalCode("10220");
        personal1.setCountry("Indonesia");
        personal1.setCreatedBy("TEST");
        personal1.setBranch(testBranch);

        // Corporate Customer for polymorphic testing
        CorporateCustomer corporate1 = new CorporateCustomer();
        corporate1.setCustomerNumber("C1000003");
        corporate1.setCompanyName("PT. Teknologi Maju");
        corporate1.setCompanyRegistrationNumber("1234567890123456");
        corporate1.setTaxIdentificationNumber("01.234.567.8-901.000");
        corporate1.setEmail("info@teknologimaju.com");
        corporate1.setPhoneNumber("02123456789");
        corporate1.setAddress("Jl. HR Rasuna Said No. 789");
        corporate1.setCity("Jakarta");
        corporate1.setPostalCode("12950");
        corporate1.setCountry("Indonesia");
        corporate1.setCreatedBy("TEST");
        corporate1.setBranch(testBranch);

        // Save using the base repository to test polymorphic behavior
        customerRepository.save(personal1);
        customerRepository.save(corporate1);
        entityManager.flush();
    }
}