package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach; // Import BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

class CorporateCustomerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;
    
    @Autowired
    private BranchRepository branchRepository;

    private Branch testBranch; // Declare testBranch field

    @BeforeEach // Initialize testBranch before each test
    void setup() {
        testBranch = branchRepository.findByBranchCode("HO001")
            .orElseThrow(() -> new IllegalStateException("Test branch HO001 should be available from SQL script"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/customer/corporate/corporate_customers.csv", numLinesToSkip = 1)
    void shouldSaveAndFindCorporateCustomerFromCsv(
            String customerNumber,
            String companyName,
            String companyRegistrationNumber,
            String taxIdentificationNumber,
            String contactPersonName,
            String contactPersonTitle,
            String email,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String country) {

        // Given - Create corporate customer from CSV data with unique identifiers
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueCustomerNumber = customerNumber + "_" + timestamp;
        String uniqueEmail = timestamp + "_" + email;
        String uniqueCompanyRegNumber = companyRegistrationNumber + "_" + timestamp;
        String uniqueTaxId = taxIdentificationNumber + "_" + timestamp;
        
        CorporateCustomer customer = new CorporateCustomer();
        customer.setCustomerNumber(uniqueCustomerNumber);
        customer.setCompanyName(companyName);
        customer.setCompanyRegistrationNumber(uniqueCompanyRegNumber);
        customer.setTaxIdentificationNumber(uniqueTaxId);
        customer.setContactPersonName(contactPersonName);
        customer.setContactPersonTitle(contactPersonTitle);
        customer.setEmail(uniqueEmail);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setPostalCode(postalCode);
        customer.setCountry(country);
        customer.setCreatedBy("TEST");
        customer.setBranch(testBranch);

        // When - Save customer
        CorporateCustomer savedCustomer = corporateCustomerRepository.saveAndFlush(customer);

        // Then - Verify customer was saved correctly
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getCustomerNumber()).isEqualTo(uniqueCustomerNumber);
        assertThat(savedCustomer.getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
        assertThat(savedCustomer.getCompanyName()).isEqualTo(companyName);
        assertThat(savedCustomer.getDisplayName()).isEqualTo(companyName);
        assertThat(savedCustomer.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedCustomer.getCreatedDate()).isNotNull();

        // Verify we can find by customer number
        Optional<CorporateCustomer> foundCustomer = corporateCustomerRepository.findByCustomerNumber(uniqueCustomerNumber);
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCustomerNumber()).isEqualTo(uniqueCustomerNumber);
    }

    @Test
    void shouldFindCorporateCustomerByCompanyRegistrationNumber() {
        // Given
        CorporateCustomer customer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer.setCompanyName("Test Corp Reg");
        corporateCustomerRepository.save(customer);

        // When
        Optional<CorporateCustomer> foundCustomer = corporateCustomerRepository
                .findByCompanyRegistrationNumber(customer.getCompanyRegistrationNumber());

        // Then
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCompanyName()).isEqualTo("Test Corp Reg");
    }

    @Test
    void shouldFindCorporateCustomerByTaxIdentificationNumber() {
        // Given
        CorporateCustomer customer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer.setCompanyName("Test Corp Tax");
        corporateCustomerRepository.save(customer);

        // When
        Optional<CorporateCustomer> foundCustomer = corporateCustomerRepository
                .findByTaxIdentificationNumber(customer.getTaxIdentificationNumber());

        // Then
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCompanyName()).isEqualTo("Test Corp Tax");
    }

    @Test
    void shouldFindCorporateCustomerByEmail() {
        // Given
        CorporateCustomer customer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer.setCompanyName("Test Corp Email");
        corporateCustomerRepository.save(customer);

        // When
        Optional<CorporateCustomer> foundCustomer = corporateCustomerRepository.findByEmail(customer.getEmail());

        // Then
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCompanyName()).isEqualTo("Test Corp Email");
    }

    @Test
    void shouldFindCorporateCustomersByCompanyName() {
        // Given
        CorporateCustomer customer1 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer1.setCompanyName("Searchable Company A");
        corporateCustomerRepository.save(customer1);

        CorporateCustomer customer2 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer2.setCompanyName("Another Searchable Company B");
        corporateCustomerRepository.save(customer2);

        CorporateCustomer customer3 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer3.setCompanyName("Non-matching Company");
        corporateCustomerRepository.save(customer3);

        // When
        List<CorporateCustomer> customers = corporateCustomerRepository
                .findByCompanyNameContainingIgnoreCase("Searchable Company");

        // Then
        assertThat(customers).hasSizeGreaterThanOrEqualTo(2);
        assertThat(customers).extracting(CorporateCustomer::getCompanyName)
                .contains(customer1.getCompanyName(), customer2.getCompanyName());
    }

    @Test
    void shouldFindCorporateCustomersWithSearchTerm() {
        // Given
        CorporateCustomer customer1 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer1.setCompanyName("Search Term Company A");
        customer1.setContactPersonName("John Doe");
        corporateCustomerRepository.save(customer1);

        CorporateCustomer customer2 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        customer2.setCompanyName("Another Company");
        customer2.setContactPersonName("Jane Search");
        corporateCustomerRepository.save(customer2);

        // When
        List<CorporateCustomer> results = corporateCustomerRepository
                .findCorporateCustomersWithSearchTerm("Search");

        // Then
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).extracting(CorporateCustomer::getCustomerNumber)
                .contains(customer1.getCustomerNumber(), customer2.getCustomerNumber());
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // Given
        CorporateCustomer customer = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        corporateCustomerRepository.save(customer);

        // When & Then
        assertThat(corporateCustomerRepository.existsByCustomerNumber(customer.getCustomerNumber())).isTrue();
        assertThat(corporateCustomerRepository.existsByCustomerNumber("C9999999")).isFalse();

        assertThat(corporateCustomerRepository.existsByCompanyRegistrationNumber(customer.getCompanyRegistrationNumber())).isTrue();
        assertThat(corporateCustomerRepository.existsByCompanyRegistrationNumber("9999999999999999")).isFalse();

        assertThat(corporateCustomerRepository.existsByEmail(customer.getEmail())).isTrue();
        assertThat(corporateCustomerRepository.existsByEmail("nonexistent@email.com")).isFalse();
    }

    @Test
    void shouldCountCorporateCustomers() {
        // Given
        CorporateCustomer customer1 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        corporateCustomerRepository.save(customer1);
        CorporateCustomer customer2 = SimpleParallelTestDataFactory.createUniqueCorporateCustomer(testBranch);
        corporateCustomerRepository.save(customer2);

        // When
        Long count = corporateCustomerRepository.countCorporateCustomers();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetContactPersonFullName() {
        // Given
        CorporateCustomer customer = new CorporateCustomer();
        customer.setContactPersonName("John Doe");
        customer.setContactPersonTitle("Director");
        // Get test branch from SQL data
        Branch testBranch = branchRepository.findByBranchCode("HO001")
            .orElseThrow(() -> new IllegalStateException("Test branch HO001 should be available from SQL script"));
        customer.setBranch(testBranch);

        // When
        String fullName = customer.getContactPersonFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe (Director)");
    }

    @Test
    void shouldGetContactPersonFullNameWithoutTitle() {
        // Given
        CorporateCustomer customer = new CorporateCustomer();
        customer.setContactPersonName("John Doe");
        // Get test branch from SQL data
        Branch testBranch = branchRepository.findByBranchCode("HO001")
            .orElseThrow(() -> new IllegalStateException("Test branch HO001 should be available from SQL script"));
        customer.setBranch(testBranch);

        // When
        String fullName = customer.getContactPersonFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

}