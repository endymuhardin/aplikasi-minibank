Feature: Customer Registration Test Suite
# This feature file provides a comprehensive test suite for customer registration
# It includes tests for both personal and corporate customers with proper separation

Background:
  * url baseUrl

# Personal Customer Tests
Scenario: Run all personal customer registration tests
  * call read('classpath:id/ac/tazkia/minibank/usecase/personal-customer-registration.feature')
  * call read('classpath:id/ac/tazkia/minibank/usecase/personal-customer-registration-validation.feature')
  * call read('classpath:id/ac/tazkia/minibank/usecase/personal-customer-registration-constraint-validation.feature')

# Corporate Customer Tests  
Scenario: Run all corporate customer registration tests
  * call read('classpath:id/ac/tazkia/minibank/usecase/corporate-customer-registration.feature')
  * call read('classpath:id/ac/tazkia/minibank/usecase/corporate-customer-registration-validation.feature')
  * call read('classpath:id/ac/tazkia/minibank/usecase/corporate-customer-registration-constraint-validation.feature')