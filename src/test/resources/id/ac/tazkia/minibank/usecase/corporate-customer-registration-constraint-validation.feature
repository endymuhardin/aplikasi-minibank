Feature: Corporate Customer Registration API - Database Constraint Validation Tests
# These tests specifically check for validation that prevents database constraint violations (500 errors)
# All tests should return 400 (validation error) instead of 500 (database constraint violation)

Background:
  * url baseUrl  
  * def testData = read('classpath:csv/usecase/corporate-customer-registration-constraint-validation.csv')

Scenario Outline: Corporate customer database constraint validation - <testCase>
  * def customerData = {}
  * if ('<customerNumber>' != null && '<customerNumber>' != '' && '<customerNumber>' != 'null') customerData.customerNumber = '<customerNumber>'
  * if ('<companyName>' != null && '<companyName>' != '' && '<companyName>' != 'null') customerData.companyName = '<companyName>'
  * if ('<companyRegistrationNumber>' != null && '<companyRegistrationNumber>' != '' && '<companyRegistrationNumber>' != 'null') customerData.companyRegistrationNumber = '<companyRegistrationNumber>'
  * if ('<taxIdentificationNumber>' != null && '<taxIdentificationNumber>' != '' && '<taxIdentificationNumber>' != 'null') customerData.taxIdentificationNumber = '<taxIdentificationNumber>'
  * if ('<email>' != null && '<email>' != '' && '<email>' != 'null') customerData.email = '<email>'
  * if ('<phoneNumber>' != null && '<phoneNumber>' != '' && '<phoneNumber>' != 'null') customerData.phoneNumber = '<phoneNumber>'
  * if ('<address>' != null && '<address>' != '' && '<address>' != 'null') customerData.address = '<address>'
  * if ('<city>' != null && '<city>' != '' && '<city>' != 'null') customerData.city = '<city>'
  * if ('<postalCode>' != null && '<postalCode>' != '' && '<postalCode>' != 'null') customerData.postalCode = '<postalCode>'
  * if ('<country>' != null && '<country>' != '' && '<country>' != 'null') customerData.country = '<country>'

  Given path '/api/customers/corporate/register'
  And request customerData
  When method POST
  Then status 400
  And match response.<expectedField> contains '<expectedErrorSubstring>'

Examples:
| read('classpath:csv/usecase/corporate-customer-registration-constraint-validation.csv') |