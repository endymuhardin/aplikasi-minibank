Feature: Customer Registration API - Database Constraint Validation Tests
# These tests specifically check for validation that prevents database constraint violations (500 errors)
# All tests should return 400 (validation error) instead of 500 (database constraint violation)

Background:
  * url baseUrl  
  * def testData = read('classpath:csv/usecase/customer-registration-constraint-validation.csv')

Scenario Outline: Database constraint validation - <testCase>
  * def customerData = {}
  * if ('<customerType>' != null && '<customerType>' != '') customerData.customerType = '<customerType>'
  * if ('<customerNumber>' != null && '<customerNumber>' != '') customerData.customerNumber = '<customerNumber>'
  * if ('<firstName>' != null && '<firstName>' != '') customerData.firstName = '<firstName>'
  * if ('<lastName>' != null && '<lastName>' != '') customerData.lastName = '<lastName>'
  * if ('<dateOfBirth>' != null && '<dateOfBirth>' != '') customerData.dateOfBirth = '<dateOfBirth>'
  * if ('<identityNumber>' != null && '<identityNumber>' != '') customerData.identityNumber = '<identityNumber>'
  * if ('<identityType>' != null && '<identityType>' != '') customerData.identityType = '<identityType>'
  * if ('<companyName>' != null && '<companyName>' != '') customerData.companyName = '<companyName>'
  * if ('<companyRegistrationNumber>' != null && '<companyRegistrationNumber>' != '') customerData.companyRegistrationNumber = '<companyRegistrationNumber>'
  * if ('<taxIdentificationNumber>' != null && '<taxIdentificationNumber>' != '') customerData.taxIdentificationNumber = '<taxIdentificationNumber>'
  * if ('<email>' != null && '<email>' != '') customerData.email = '<email>'
  * if ('<phoneNumber>' != null && '<phoneNumber>' != '') customerData.phoneNumber = '<phoneNumber>'
  * if ('<address>' != null && '<address>' != '') customerData.address = '<address>'
  * if ('<city>' != null && '<city>' != '') customerData.city = '<city>'
  * if ('<postalCode>' != null && '<postalCode>' != '') customerData.postalCode = '<postalCode>'
  * if ('<country>' != null && '<country>' != '') customerData.country = '<country>'
  
  Given path '/api/customers/register'
  And request customerData
  When method POST
  Then status 400
  And match response.'<expectedField>' contains '<expectedErrorSubstring>'

Examples:
| read('classpath:csv/usecase/customer-registration-constraint-validation.csv') |