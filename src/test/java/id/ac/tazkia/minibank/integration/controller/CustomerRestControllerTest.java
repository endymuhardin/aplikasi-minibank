package id.ac.tazkia.minibank.integration.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.tazkia.minibank.controller.rest.CustomerRestController;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;

@WebMvcTest(CustomerRestController.class)
public class CustomerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonalCustomerRepository personalCustomerRepository;

    @MockBean
    private CorporateCustomerRepository corporateCustomerRepository;

    @Test
    public void testRegisterPersonalCustomerSuccess() throws Exception {
        PersonalCustomer customer = new PersonalCustomer();
        customer.setCustomerNumber("CUST001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        customer.setIdentityNumber("1234567890123456");
        customer.setIdentityType(id.ac.tazkia.minibank.entity.Customer.IdentityType.KTP);
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("081234567890");
        customer.setAddress("123 Main St");
        customer.setCity("Jakarta");
        customer.setCountry("Indonesia");
        customer.setPostalCode("12345");

        PersonalCustomer savedCustomer = new PersonalCustomer();
        savedCustomer.setId(UUID.randomUUID());
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");
        savedCustomer.setEmail("john.doe@example.com");
        savedCustomer.setCustomerNumber("CUST001");

        when(personalCustomerRepository.save(any(PersonalCustomer.class))).thenReturn(savedCustomer);

        mockMvc.perform(post("/api/customers/personal/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void testRegisterPersonalCustomerWithValidationErrors() throws Exception {
        PersonalCustomer customer = new PersonalCustomer();
        // Missing required fields to trigger validation errors

        mockMvc.perform(post("/api/customers/personal/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterCorporateCustomerSuccess() throws Exception {
        CorporateCustomer customer = new CorporateCustomer();
        customer.setCustomerNumber("CORP001");
        customer.setCompanyName("PT. Test Company");
        customer.setCompanyRegistrationNumber("12345678901234");
        customer.setTaxIdentificationNumber("987654321");
        customer.setContactPersonName("Jane Smith");
        customer.setContactPersonTitle("Manager");
        customer.setEmail("contact@testcompany.com");
        customer.setPhoneNumber("021-12345678");
        customer.setAddress("456 Business St");
        customer.setCity("Jakarta");
        customer.setCountry("Indonesia");
        customer.setPostalCode("54321");

        CorporateCustomer savedCustomer = new CorporateCustomer();
        savedCustomer.setId(UUID.randomUUID());
        savedCustomer.setCompanyName("PT. Test Company");
        savedCustomer.setCustomerNumber("CORP001");

        when(corporateCustomerRepository.save(any(CorporateCustomer.class))).thenReturn(savedCustomer);

        mockMvc.perform(post("/api/customers/corporate/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("PT. Test Company"));
    }

    @Test
    public void testRegisterCorporateCustomerWithValidationErrors() throws Exception {
        CorporateCustomer customer = new CorporateCustomer();
        // Missing required fields to trigger validation errors

        mockMvc.perform(post("/api/customers/corporate/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetPersonalCustomerFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        PersonalCustomer customer = new PersonalCustomer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");

        when(personalCustomerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/personal/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    public void testGetPersonalCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(personalCustomerRepository.findById(customerId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/personal/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetCorporateCustomerFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        CorporateCustomer customer = new CorporateCustomer();
        customer.setId(customerId);
        customer.setCompanyName("PT. Test Company");

        when(corporateCustomerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/corporate/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("PT. Test Company"));
    }

    @Test
    public void testGetCorporateCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(corporateCustomerRepository.findById(customerId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/corporate/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllPersonalCustomersWithoutSearch() throws Exception {
        PersonalCustomer customer1 = new PersonalCustomer();
        customer1.setFirstName("John");
        PersonalCustomer customer2 = new PersonalCustomer();
        customer2.setFirstName("Jane");
        List<PersonalCustomer> customers = Arrays.asList(customer1, customer2);

        when(personalCustomerRepository.findAll()).thenReturn(customers);

        mockMvc.perform(get("/api/customers/personal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetAllPersonalCustomersWithSearch() throws Exception {
        PersonalCustomer customer = new PersonalCustomer();
        customer.setFirstName("John");
        List<PersonalCustomer> customers = Arrays.asList(customer);

        when(personalCustomerRepository.findPersonalCustomersWithSearchTerm(anyString())).thenReturn(customers);

        mockMvc.perform(get("/api/customers/personal").param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testGetAllCorporateCustomersWithoutSearch() throws Exception {
        CorporateCustomer customer1 = new CorporateCustomer();
        customer1.setCompanyName("PT. Company A");
        CorporateCustomer customer2 = new CorporateCustomer();
        customer2.setCompanyName("PT. Company B");
        List<CorporateCustomer> customers = Arrays.asList(customer1, customer2);

        when(corporateCustomerRepository.findAll()).thenReturn(customers);

        mockMvc.perform(get("/api/customers/corporate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetAllCorporateCustomersWithSearch() throws Exception {
        CorporateCustomer customer = new CorporateCustomer();
        customer.setCompanyName("PT. Test Company");
        List<CorporateCustomer> customers = Arrays.asList(customer);

        when(corporateCustomerRepository.findCorporateCustomersWithSearchTerm(anyString())).thenReturn(customers);

        mockMvc.perform(get("/api/customers/corporate").param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testGetCustomerByNumberPersonalFound() throws Exception {
        String customerNumber = "CUST001";
        PersonalCustomer customer = new PersonalCustomer();
        customer.setCustomerNumber(customerNumber);
        customer.setFirstName("John");

        when(personalCustomerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));
        when(corporateCustomerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/number/{customerNumber}", customerNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    public void testGetCustomerByNumberCorporateFound() throws Exception {
        String customerNumber = "CORP001";
        CorporateCustomer customer = new CorporateCustomer();
        customer.setCustomerNumber(customerNumber);
        customer.setCompanyName("PT. Test Company");

        when(personalCustomerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());
        when(corporateCustomerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/number/{customerNumber}", customerNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("PT. Test Company"));
    }

    @Test
    public void testGetCustomerByNumberNotFound() throws Exception {
        String customerNumber = "NOTFOUND";

        when(personalCustomerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());
        when(corporateCustomerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/number/{customerNumber}", customerNumber))
                .andExpect(status().isNotFound());
    }
}