package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    
    @Autowired
    private PersonalCustomerRepository personalCustomerRepository;
    
    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        personalCustomerRepository.deleteAll();
        corporateCustomerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
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

        // When
        List<Customer> results = customerRepository.findCustomersWithSearchTerm("ahmad");

        // Then
        assertThat(results).hasSizeGreaterThan(0);
        assertThat(results.get(0).getEmail()).containsIgnoringCase("ahmad");
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
        assertThat(count).isGreaterThan(0);
    }

    private void saveTestCustomers() {
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

        personalCustomerRepository.save(personal1);
        corporateCustomerRepository.save(corporate1);
        entityManager.flush();
    }
}