package id.ac.tazkia.minibank.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserRole;

/**
 * Simplified factory for creating unique test data for parallel repository tests.
 * Only uses basic fields that exist in all entity classes.
 */
public class SimpleParallelTestDataFactory {
    
    // Branch Factory
    public static Branch createUniqueBranch() {
        String branchCode = ParallelTestDataContext.generateBranchCode();
        Branch branch = new Branch();
        branch.setBranchCode(branchCode);
        branch.setBranchName(branchCode + " Branch");
        branch.setIsMainBranch(false);
        branch.setStatus(Branch.BranchStatus.ACTIVE);
        branch.setAddress(branchCode + " Address");
        branch.setCity("Test City");
        branch.setCountry("Indonesia");
        branch.setPostalCode("12345");
        branch.setPhoneNumber("021-1234567");
        branch.setEmail(branchCode.toLowerCase() + "@minibank.com");
        branch.setManagerName(branchCode + " Manager");
        branch.setCreatedBy("TEST");
        branch.setUpdatedBy("TEST");
        return branch;
    }
    
    // Branch Factory with custom properties
    public static Branch createUniqueBranch(String city, Branch.BranchStatus status, boolean isMainBranch) {
        Branch branch = createUniqueBranch();
        branch.setCity(city);
        branch.setStatus(status);
        branch.setIsMainBranch(isMainBranch);
        return branch;
    }
    
    // Customer Factory
    public static PersonalCustomer createUniquePersonalCustomer(Branch branch) {
        String customerNumber = ParallelTestDataContext.generateCustomerNumber();
        String email = ParallelTestDataContext.generateEmail("customer", "email.com");
        
        PersonalCustomer customer = new PersonalCustomer();
        customer.setCustomerNumber(customerNumber);
        customer.setBranch(branch);
        customer.setEmail(email);
        customer.setPhoneNumber("081234567890");
        customer.setAddress(customerNumber + " Street");
        customer.setCity("Jakarta");
        customer.setPostalCode("10220");
        customer.setCountry("Indonesia");
        customer.setCreatedBy("TEST");
        customer.setUpdatedBy("TEST");
        
        // Personal customer specific fields
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setDateOfBirth(LocalDate.now().minusYears(25));
        customer.setIdentityNumber("ID" + Math.abs(customerNumber.hashCode()));
        customer.setIdentityType(PersonalCustomer.IdentityType.KTP);
        
        return customer;
    }
    
    public static CorporateCustomer createUniqueCorporateCustomer(Branch branch) {
        String customerNumber = ParallelTestDataContext.generateCustomerNumber();
        String email = ParallelTestDataContext.generateEmail("corporate", "company.com");
        
        CorporateCustomer customer = new CorporateCustomer();
        customer.setCustomerNumber(customerNumber);
        customer.setBranch(branch);
        customer.setEmail(email);
        customer.setPhoneNumber("021-1234567");
        customer.setAddress(customerNumber + " Building");
        customer.setCity("Jakarta");
        customer.setPostalCode("10230");
        customer.setCountry("Indonesia");
        customer.setCreatedBy("TEST");
        customer.setUpdatedBy("TEST");
        
        // Corporate customer specific fields
        customer.setCompanyName("Company " + customerNumber);
        customer.setCompanyRegistrationNumber("REG" + Math.abs(customerNumber.hashCode()));
        customer.setTaxIdentificationNumber("TAX" + Math.abs(customerNumber.hashCode()));
        customer.setContactPersonName("Contact " + customerNumber);
        customer.setContactPersonTitle("Manager");
        
        return customer;
    }
    
    // Product Factory
    public static Product createUniqueProduct(Product.ProductType productType) {
        String productCode = ParallelTestDataContext.generateProductCode(productType.name());
        Product product = new Product();
        product.setProductCode(productCode);
        product.setProductName(productType.name() + " " + productCode);
        product.setProductType(productType);
        product.setProductCategory("Test Category");
        product.setDescription("Test product for " + productCode);
        product.setIsActive(true);
        product.setIsDefault(false);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("100000"));
        product.setMinimumBalance(new BigDecimal("50000"));
        product.setCreatedBy("TEST");
        product.setUpdatedBy("TEST");
        
        // Set type-specific defaults
        if (productType == Product.ProductType.TABUNGAN_WADIAH) {
            product.setDailyWithdrawalLimit(new BigDecimal("5000000"));
            product.setMonthlyTransactionLimit(50);
            product.setMonthlyMaintenanceFee(new BigDecimal("10000"));
            product.setProfitSharingType(Product.ProfitSharingType.WADIAH); // WADIAH safekeeping contract
        } else if (productType == Product.ProductType.TABUNGAN_MUDHARABAH) {
            product.setDailyWithdrawalLimit(new BigDecimal("5000000"));
            product.setMonthlyTransactionLimit(50);
            product.setMonthlyMaintenanceFee(new BigDecimal("10000"));
            product.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
            product.setNisbahCustomer(new BigDecimal("0.6"));
            product.setNisbahBank(new BigDecimal("0.4"));
        } else if (productType == Product.ProductType.DEPOSITO_MUDHARABAH) {
            product.setMinimumOpeningBalance(new BigDecimal("1000000"));
            product.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
            product.setNisbahCustomer(new BigDecimal("0.6"));
            product.setNisbahBank(new BigDecimal("0.4"));
        }
        
        return product;
    }
    
    // Account Factory
    public static Account createUniqueAccount(Customer customer, Product product, Branch branch) {
        String accountNumber = ParallelTestDataContext.generateAccountNumber();
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountName(customer.getCustomerNumber() + " Account");
        account.setCustomer(customer);
        account.setProduct(product);
        account.setBranch(branch);
        account.setBalance(new BigDecimal("1000000"));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setOpenedDate(LocalDate.now());
        account.setCreatedBy("TEST");
        account.setUpdatedBy("TEST");
        return account;
    }
    
    // User Factory  
    public static User createUniqueUser(Branch branch) {
        String username = ParallelTestDataContext.generateUsername("user");
        String email = ParallelTestDataContext.generateEmail(username, "minibank.com");
        
        User user = new User();
        user.setUsername(username);
        user.setFullName("Test User " + username);
        user.setEmail(email);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setIsLocked(false);
        user.setBranch(branch);
        user.setCreatedBy("TEST");
        user.setUpdatedBy("TEST");
        return user;
    }
    
    // Role Factory
    public static Role createUniqueRole() {
        String roleCode = ParallelTestDataContext.generateRoleCode("ROLE");
        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName("Role " + roleCode);
        role.setDescription("Test role: " + roleCode);
        role.setIsActive(true);
        role.setCreatedBy("TEST");
        role.setUpdatedBy("TEST");
        return role;
    }
    
    // Permission Factory
    public static Permission createUniquePermission() {
        String prefix = ParallelTestDataContext.getUniquePrefix();
        String permissionCode = "PERM_" + prefix + "_" + Math.abs(prefix.hashCode()) % 1000;
        Permission permission = new Permission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName("Permission " + permissionCode);
        permission.setPermissionCategory("GENERAL"); // Required field
        permission.setDescription("Test permission: " + permissionCode);
        permission.setCreatedBy("TEST");
        return permission;
    }
    
    // Complete test data graph
    public static TestDataGraph createCompleteTestData() {
        Branch branch = createUniqueBranch();
        PersonalCustomer personalCustomer = createUniquePersonalCustomer(branch);
        CorporateCustomer corporateCustomer = createUniqueCorporateCustomer(branch);
        Product savingsProduct = createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        Product depositProduct = createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        
        Account personalAccount = createUniqueAccount(personalCustomer, savingsProduct, branch);
        Account corporateAccount = createUniqueAccount(corporateCustomer, depositProduct, branch);
        
        User user = createUniqueUser(branch);
        Role role = createUniqueRole();
        Permission permission = createUniquePermission();
        
        return new TestDataGraph(branch, personalCustomer, corporateCustomer, 
                List.of(savingsProduct, depositProduct),
                List.of(personalAccount, corporateAccount),
                user, role, permission);
    }
    
    // UserRole Factory
    public static UserRole createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy("TEST");
        return userRole;
    }
    
    // Data holder class
    public static class TestDataGraph {
        public final Branch branch;
        public final PersonalCustomer personalCustomer;
        public final CorporateCustomer corporateCustomer;
        public final List<Product> products;
        public final List<Account> accounts;
        public final User user;
        public final Role role;
        public final Permission permission;
        
        public TestDataGraph(Branch branch, PersonalCustomer personalCustomer, 
                           CorporateCustomer corporateCustomer, List<Product> products,
                           List<Account> accounts, User user, Role role, Permission permission) {
            this.branch = branch;
            this.personalCustomer = personalCustomer;
            this.corporateCustomer = corporateCustomer;
            this.products = new ArrayList<>(products);
            this.accounts = new ArrayList<>(accounts);
            this.user = user;
            this.role = role;
            this.permission = permission;
        }
    }
}