package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
class PersonalCustomerRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonalCustomerRepository personalCustomerRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    private Branch testBranch;

    @BeforeEach
    void setUp() {
        logTestExecution("PersonalCustomerRepositoryTest setup");
        
        // Use existing branch from migration data
        testBranch = branchRepository.findByBranchCode("HO001")
                .orElseThrow(() -> new RuntimeException("Test branch HO001 not found in database"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/customer/personal/personal_customers.csv", numLinesToSkip = 1)
    void shouldSaveAndFindPersonalCustomerFromCsv(
            String customerNumber,
            String firstName,
            String lastName,
            String dateOfBirth,
            String identityNumber,
            String identityType,
            String email,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String country) {

        // Given - Create personal customer from CSV data with unique identifiers
        String prefix = getTestPrefix();
        String uniqueCustomerNumber = customerNumber + "_" + prefix;
        String uniqueIdentityNumber = identityNumber + prefix;
        String uniqueEmail = prefix + "_" + email;
        
        PersonalCustomer customer = new PersonalCustomer();
        customer.setCustomerNumber(uniqueCustomerNumber);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setDateOfBirth(LocalDate.parse(dateOfBirth));
        customer.setIdentityNumber(uniqueIdentityNumber);
        customer.setIdentityType(Customer.IdentityType.valueOf(identityType));
        customer.setEmail(uniqueEmail);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setPostalCode(postalCode);
        customer.setCountry(country);
        customer.setCreatedBy("TEST");
        customer.setBranch(testBranch);

        // When - Save customer
        PersonalCustomer savedCustomer = personalCustomerRepository.save(customer);
        entityManager.flush();

        // Then - Verify customer was saved correctly
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getCustomerNumber()).isEqualTo(uniqueCustomerNumber);
        assertThat(savedCustomer.getCustomerType()).isEqualTo(Customer.CustomerType.PERSONAL);
        assertThat(savedCustomer.getFirstName()).isEqualTo(firstName);
        assertThat(savedCustomer.getLastName()).isEqualTo(lastName);
        assertThat(savedCustomer.getDisplayName()).isEqualTo(firstName + " " + lastName);
        assertThat(savedCustomer.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedCustomer.getCreatedDate()).isNotNull();

        // Verify we can find by customer number
        Optional<PersonalCustomer> foundCustomer = personalCustomerRepository.findByCustomerNumber(uniqueCustomerNumber);
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getCustomerNumber()).isEqualTo(uniqueCustomerNumber);
    }

    @Test
    void shouldFindPersonalCustomerByIdentityNumber() {
        // Given
        saveTestPersonalCustomers();

        // When - use the unique identity number from our test data
        String prefix = getTestPrefix();
        Optional<PersonalCustomer> customer = personalCustomerRepository.findByIdentityNumber("327108" + prefix + "01");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getFirstName()).isEqualTo("Ahmad");
        assertThat(customer.get().getLastName()).isEqualTo("Suharto");
    }

    @Test
    void shouldFindPersonalCustomerByEmail() {
        // Given
        saveTestPersonalCustomers();

        // When - use the unique email from our test data
        String prefix = getTestPrefix();
        Optional<PersonalCustomer> customer = personalCustomerRepository.findByEmail("ahmad.suharto." + prefix + "@email.com");

        // Then
        assertThat(customer).isPresent();
        assertThat(customer.get().getFirstName()).isEqualTo("Ahmad");
    }

    @Test
    void shouldFindPersonalCustomersByName() {
        // Given
        saveTestPersonalCustomers();

        // When
        List<PersonalCustomer> customers = personalCustomerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Ahmad", "Ahmad");

        // Then
        assertThat(customers).hasSizeGreaterThan(0);
        assertThat(customers.get(0).getFirstName()).containsIgnoringCase("Ahmad");
    }

    @Test
    void shouldFindPersonalCustomersWithSearchTerm() {
        // Given
        saveTestPersonalCustomers();

        // When
        List<PersonalCustomer> results = personalCustomerRepository.findPersonalCustomersWithSearchTerm("Ahmad");

        // Then
        assertThat(results).hasSizeGreaterThan(0);
        assertThat(results.get(0).getFirstName()).containsIgnoringCase("Ahmad");
    }

    @Test
    void shouldFindPersonalCustomersByAgeBetween() {
        // Given
        saveTestPersonalCustomers();

        // When
        List<PersonalCustomer> customers = personalCustomerRepository.findByAgeBetween(30, 50);

        // Then
        assertThat(customers).hasSizeGreaterThan(0);
        customers.forEach(customer -> {
            Integer age = customer.getAge();
            assertThat(age).isBetween(30, 50);
        });
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // Given
        saveTestPersonalCustomers();

        // When & Then - use unique test data
        String prefix = getTestPrefix();
        assertThat(personalCustomerRepository.existsByCustomerNumber("C" + prefix + "01")).isTrue();
        assertThat(personalCustomerRepository.existsByCustomerNumber("C9999999")).isFalse();
        
        assertThat(personalCustomerRepository.existsByIdentityNumber("327108" + prefix + "01")).isTrue();
        assertThat(personalCustomerRepository.existsByIdentityNumber("9999999999999999")).isFalse();
        
        assertThat(personalCustomerRepository.existsByEmail("ahmad.suharto." + prefix + "@email.com")).isTrue();
        assertThat(personalCustomerRepository.existsByEmail("nonexistent@email.com")).isFalse();
    }

    @Test
    void shouldCountPersonalCustomers() {
        // Given
        saveTestPersonalCustomers();

        // When
        Long count = personalCustomerRepository.countPersonalCustomers();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    private void saveTestPersonalCustomers() {
        String prefix = getTestPrefix();
        
        PersonalCustomer personal1 = new PersonalCustomer();
        personal1.setCustomerNumber("C" + prefix + "01");
        personal1.setFirstName("Ahmad");
        personal1.setLastName("Suharto");
        personal1.setDateOfBirth(LocalDate.of(1985, 3, 15));
        personal1.setIdentityNumber("327108" + prefix + "01");
        personal1.setIdentityType(Customer.IdentityType.KTP);
        personal1.setEmail("ahmad.suharto." + prefix + "@email.com");
        personal1.setPhoneNumber("081234567890");
        personal1.setAddress("Jl. Sudirman No. 123");
        personal1.setCity("Jakarta");
        personal1.setPostalCode("10220");
        personal1.setCountry("Indonesia");
        personal1.setCreatedBy("TEST");
        personal1.setBranch(testBranch);

        PersonalCustomer personal2 = new PersonalCustomer();
        personal2.setCustomerNumber("C" + prefix + "02");
        personal2.setFirstName("Siti");
        personal2.setLastName("Nurhaliza");
        personal2.setDateOfBirth(LocalDate.of(1990, 7, 22));
        personal2.setIdentityNumber("327108" + prefix + "02");
        personal2.setIdentityType(Customer.IdentityType.KTP);
        personal2.setEmail("siti.nurhaliza." + prefix + "@email.com");
        personal2.setPhoneNumber("081234567891");
        personal2.setAddress("Jl. Thamrin No. 456");
        personal2.setCity("Jakarta");
        personal2.setPostalCode("10230");
        personal2.setCountry("Indonesia");
        personal2.setCreatedBy("TEST");
        personal2.setBranch(testBranch);

        personalCustomerRepository.save(personal1);
        personalCustomerRepository.save(personal2);
        entityManager.flush();
    }
}