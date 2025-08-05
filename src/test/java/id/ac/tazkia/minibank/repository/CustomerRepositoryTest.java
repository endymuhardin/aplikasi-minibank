package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        customerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/csv/customers.csv", numLinesToSkip = 1)
    void shouldSaveAndFindCustomerFromCsv(
            String customerType,
            String customerNumber,
            String firstName,
            String lastName,
            String dateOfBirth,
            String identityNumber,
            String identityType,
            String companyName,
            String companyRegistrationNumber,
            String taxIdentificationNumber,
            String email,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String country) {

        // Given - Create customer from CSV data
        Customer customer = new Customer();
        customer.setCustomerType(Customer.CustomerType.valueOf(customerType));
        customer.setCustomerNumber(customerNumber);
        
        if ("PERSONAL".equals(customerType)) {
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setDateOfBirth(LocalDate.parse(dateOfBirth));
            customer.setIdentityNumber(identityNumber);
            customer.setIdentityType(Customer.IdentityType.valueOf(identityType));
        } else {
            customer.setCompanyName(companyName);
            customer.setCompanyRegistrationNumber(companyRegistrationNumber);
            customer.setTaxIdentificationNumber(taxIdentificationNumber);
        }
        
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setPostalCode(postalCode);
        customer.setCountry(country);
        customer.setCreatedBy("TEST");

        // When - Save customer
        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();

        // Then - Verify customer was saved correctly
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getCustomerNumber()).isEqualTo(customerNumber);
        assertThat(savedCustomer.getCustomerType().name()).isEqualTo(customerType);
        assertThat(savedCustomer.getEmail()).isEqualTo(email);
        assertThat(savedCustomer.getCreatedDate()).isNotNull();

        // Verify we can find by customer number
        Optional<Customer> foundCustomer = customerRepository.findByCustomerNumber(customerNumber);
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCustomerNumber()).isEqualTo(customerNumber);
    }

    @Test
    void shouldFindCustomersByType() {
        // Given - Save test customers
        saveTestCustomers();

        // When - Find personal customers
        List<Customer> personalCustomers = customerRepository.findByCustomerType(Customer.CustomerType.PERSONAL);
        List<Customer> corporateCustomers = customerRepository.findByCustomerType(Customer.CustomerType.CORPORATE);

        // Then
        assertThat(personalCustomers).hasSizeGreaterThan(0);
        assertThat(corporateCustomers).hasSizeGreaterThan(0);
        
        personalCustomers.forEach(customer -> 
            assertThat(customer.getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL));
        corporateCustomers.forEach(customer -> 
            assertThat(customer.getCustomerType()).isEqualTo(Customer.CustomerType.CORPORATE));
    }

    @Test
    void shouldFindCustomerByIdentityNumber() {
        // Given
        saveTestCustomers();

        // When
        Optional<Customer> customer = customerRepository.findByIdentityNumber("3271081503850001");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getFirstName()).isEqualTo("Ahmad");
        assertThat(customer.get().getLastName()).isEqualTo("Suharto");
    }

    @Test
    void shouldFindCustomerByCompanyRegistrationNumber() {
        // Given
        saveTestCustomers();

        // When
        Optional<Customer> customer = customerRepository.findByCompanyRegistrationNumber("1234567890123456");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getCompanyName()).isEqualTo("PT. Teknologi Maju");
    }

    @Test
    void shouldFindCustomerByEmail() {
        // Given
        saveTestCustomers();

        // When
        Optional<Customer> customer = customerRepository.findByEmail("ahmad.suharto@email.com");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getFirstName()).isEqualTo("Ahmad");
    }

    @Test
    void shouldCountCustomersByType() {
        // Given
        saveTestCustomers();

        // When
        Long personalCount = customerRepository.countByCustomerType(Customer.CustomerType.PERSONAL);
        Long corporateCount = customerRepository.countByCustomerType(Customer.CustomerType.CORPORATE);

        // Then
        assertThat(personalCount).isGreaterThan(0);
        assertThat(corporateCount).isGreaterThan(0);
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // Given
        saveTestCustomers();

        // When & Then
        assertThat(customerRepository.existsByCustomerNumber("C1000001")).isTrue();
        assertThat(customerRepository.existsByCustomerNumber("C9999999")).isFalse();
        
        assertThat(customerRepository.existsByIdentityNumber("3271081503850001")).isTrue();
        assertThat(customerRepository.existsByIdentityNumber("9999999999999999")).isFalse();
        
        assertThat(customerRepository.existsByEmail("ahmad.suharto@email.com")).isTrue();
        assertThat(customerRepository.existsByEmail("nonexistent@email.com")).isFalse();
    }

    @Test
    void shouldFindCustomersWithFilters() {
        // Given
        saveTestCustomers();

        // When - Search by customer type and search term
        List<Customer> results = customerRepository.findCustomersWithFilters(
            Customer.CustomerType.PERSONAL, "Ahmad");

        // Then
        assertThat(results).hasSizeGreaterThan(0);
        assertThat(results.get(0).getFirstName()).containsIgnoringCase("Ahmad");
    }

    private void saveTestCustomers() {
        // Save a few test customers
        Customer personal1 = new Customer();
        personal1.setCustomerType(Customer.CustomerType.PERSONAL);
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

        Customer corporate1 = new Customer();
        corporate1.setCustomerType(Customer.CustomerType.CORPORATE);
        corporate1.setCustomerNumber("C1000004");
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

        customerRepository.save(personal1);
        customerRepository.save(corporate1);
        entityManager.flush();
    }
}