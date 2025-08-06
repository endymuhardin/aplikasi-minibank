package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CorporateCustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;

    @BeforeEach
    void setUp() {
        corporateCustomerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
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

        // Given - Create corporate customer from CSV data
        CorporateCustomer customer = new CorporateCustomer();
        customer.setCustomerNumber(customerNumber);
        customer.setCompanyName(companyName);
        customer.setCompanyRegistrationNumber(companyRegistrationNumber);
        customer.setTaxIdentificationNumber(taxIdentificationNumber);
        customer.setContactPersonName(contactPersonName);
        customer.setContactPersonTitle(contactPersonTitle);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setPostalCode(postalCode);
        customer.setCountry(country);
        customer.setCreatedBy("TEST");

        // When - Save customer
        CorporateCustomer savedCustomer = corporateCustomerRepository.save(customer);
        entityManager.flush();

        // Then - Verify customer was saved correctly
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getCustomerNumber()).isEqualTo(customerNumber);
        assertThat(savedCustomer.getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE);
        assertThat(savedCustomer.getCompanyName()).isEqualTo(companyName);
        assertThat(savedCustomer.getDisplayName()).isEqualTo(companyName);
        assertThat(savedCustomer.getEmail()).isEqualTo(email);
        assertThat(savedCustomer.getCreatedDate()).isNotNull();

        // Verify we can find by customer number
        Optional<CorporateCustomer> foundCustomer = corporateCustomerRepository.findByCustomerNumber(customerNumber);
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCustomerNumber()).isEqualTo(customerNumber);
    }

    @Test
    void shouldFindCorporateCustomerByCompanyRegistrationNumber() {
        // Given
        saveTestCorporateCustomers();

        // When
        Optional<CorporateCustomer> customer = corporateCustomerRepository
                .findByCompanyRegistrationNumber("1234567890123456");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("PT. Teknologi Maju");
    }

    @Test
    void shouldFindCorporateCustomerByTaxIdentificationNumber() {
        // Given
        saveTestCorporateCustomers();

        // When
        Optional<CorporateCustomer> customer = corporateCustomerRepository
                .findByTaxIdentificationNumber("01.234.567.8-901.000");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("PT. Teknologi Maju");
    }

    @Test
    void shouldFindCorporateCustomerByEmail() {
        // Given
        saveTestCorporateCustomers();

        // When
        Optional<CorporateCustomer> customer = corporateCustomerRepository.findByEmail("info@teknologimaju.com");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("PT. Teknologi Maju");
    }

    @Test
    void shouldFindCorporateCustomersByCompanyName() {
        // Given
        saveTestCorporateCustomers();

        // When
        List<CorporateCustomer> customers = corporateCustomerRepository
                .findByCompanyNameContainingIgnoreCase("Teknologi");

        // Then
        assertThat(customers).hasSizeGreaterThan(0);
        assertThat(customers.get(0).getCompanyName()).containsIgnoringCase("Teknologi");
    }

    @Test
    void shouldFindCorporateCustomersWithSearchTerm() {
        // Given
        saveTestCorporateCustomers();

        // When
        List<CorporateCustomer> results = corporateCustomerRepository
                .findCorporateCustomersWithSearchTerm("Teknologi");

        // Then
        assertThat(results).hasSizeGreaterThan(0);
        assertThat(results.get(0).getCompanyName()).containsIgnoringCase("Teknologi");
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // Given
        saveTestCorporateCustomers();

        // When & Then
        assertThat(corporateCustomerRepository.existsByCustomerNumber("C1000003")).isTrue();
        assertThat(corporateCustomerRepository.existsByCustomerNumber("C9999999")).isFalse();
        
        assertThat(corporateCustomerRepository.existsByCompanyRegistrationNumber("1234567890123456")).isTrue();
        assertThat(corporateCustomerRepository.existsByCompanyRegistrationNumber("9999999999999999")).isFalse();
        
        assertThat(corporateCustomerRepository.existsByEmail("info@teknologimaju.com")).isTrue();
        assertThat(corporateCustomerRepository.existsByEmail("nonexistent@email.com")).isFalse();
    }

    @Test
    void shouldCountCorporateCustomers() {
        // Given
        saveTestCorporateCustomers();

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

        // When
        String fullName = customer.getContactPersonFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    private void saveTestCorporateCustomers() {
        CorporateCustomer corporate1 = new CorporateCustomer();
        corporate1.setCustomerNumber("C1000003");
        corporate1.setCompanyName("PT. Teknologi Maju");
        corporate1.setCompanyRegistrationNumber("1234567890123456");
        corporate1.setTaxIdentificationNumber("01.234.567.8-901.000");
        corporate1.setContactPersonName("Budi Santoso");
        corporate1.setContactPersonTitle("Director");
        corporate1.setEmail("info@teknologimaju.com");
        corporate1.setPhoneNumber("02123456789");
        corporate1.setAddress("Jl. HR Rasuna Said No. 789");
        corporate1.setCity("Jakarta");
        corporate1.setPostalCode("12950");
        corporate1.setCountry("Indonesia");
        corporate1.setCreatedBy("TEST");

        CorporateCustomer corporate2 = new CorporateCustomer();
        corporate2.setCustomerNumber("C1000004");
        corporate2.setCompanyName("PT. Inovasi Digital");
        corporate2.setCompanyRegistrationNumber("9876543210987654");
        corporate2.setTaxIdentificationNumber("02.345.678.9-012.000");
        corporate2.setContactPersonName("Sari Indah");
        corporate2.setContactPersonTitle("CEO");
        corporate2.setEmail("contact@inovasidigital.com");
        corporate2.setPhoneNumber("02187654321");
        corporate2.setAddress("Jl. Gatot Subroto No. 456");
        corporate2.setCity("Jakarta");
        corporate2.setPostalCode("12930");
        corporate2.setCountry("Indonesia");
        corporate2.setCreatedBy("TEST");

        corporateCustomerRepository.save(corporate1);
        corporateCustomerRepository.save(corporate2);
        entityManager.flush();
    }
}