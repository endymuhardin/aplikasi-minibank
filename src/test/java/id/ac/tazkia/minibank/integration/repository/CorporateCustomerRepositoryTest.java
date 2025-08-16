package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;

@Sql("/sql/corporate-customer-test-data.sql")
class CorporateCustomerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;
    
    @Autowired
    private BranchRepository branchRepository;

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
        // Get test branch from SQL data
        Branch testBranch = branchRepository.findByBranchCode("HO001")
            .orElseThrow(() -> new IllegalStateException("Test branch HO001 should be available from SQL script"));
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
        // When
        Optional<CorporateCustomer> customer = corporateCustomerRepository
                .findByCompanyRegistrationNumber("1234567890123456");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("Test Corp 2");
    }

    @Test
    void shouldFindCorporateCustomerByTaxIdentificationNumber() {
        // When
        Optional<CorporateCustomer> customer = corporateCustomerRepository
                .findByTaxIdentificationNumber("01.234.567.8-901.000");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("Test Corp 2");
    }

    @Test
    void shouldFindCorporateCustomerByEmail() {
        // When
        Optional<CorporateCustomer> customer = corporateCustomerRepository.findByEmail("test.corp2@email.com");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("Test Corp 2");
    }

    @Test
    void shouldFindCorporateCustomersByCompanyName() {
        // When
        List<CorporateCustomer> customers = corporateCustomerRepository
                .findByCompanyNameContainingIgnoreCase("Test Corp");

        // Then
        assertThat(customers).hasSizeGreaterThan(0);
        assertThat(customers.get(0).getCompanyName()).containsIgnoringCase("Test Corp");
    }

    @Test
    void shouldFindCorporateCustomersWithSearchTerm() {
        // When
        List<CorporateCustomer> results = corporateCustomerRepository
                .findCorporateCustomersWithSearchTerm("Test Corp");

        // Then
        assertThat(results).hasSizeGreaterThan(0);
        assertThat(results.get(0).getCompanyName()).containsIgnoringCase("Test Corp");
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // When & Then
        assertThat(corporateCustomerRepository.existsByCustomerNumber("C2000001")).isTrue();
        assertThat(corporateCustomerRepository.existsByCustomerNumber("C9999999")).isFalse();

        assertThat(corporateCustomerRepository.existsByCompanyRegistrationNumber("1234567890123456")).isTrue();
        assertThat(corporateCustomerRepository.existsByCompanyRegistrationNumber("9999999999999999")).isFalse();

        assertThat(corporateCustomerRepository.existsByEmail("test.corp1@email.com")).isTrue();
        assertThat(corporateCustomerRepository.existsByEmail("nonexistent@email.com")).isFalse();
    }

    @Test
    void shouldCountCorporateCustomers() {
        // When
        Long count = corporateCustomerRepository.countCorporateCustomers();

        // Then
        assertThat(count).isGreaterThan(0);
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