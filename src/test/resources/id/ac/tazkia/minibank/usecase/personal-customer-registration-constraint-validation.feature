Feature: Personal Customer Registration API - Database Constraint Validation Tests
# These tests specifically check for validation that prevents database constraint violations (500 errors)
# All tests should return 400 (validation error) instead of 500 (database constraint violation)

Background:
  * url baseUrl  
  * def testData = read('classpath:csv/usecase/personal-customer-registration-constraint-validation.csv')

Scenario Outline: Personal customer database constraint validation - <testCase>
  * def customerData = {}
  * if ('<customerNumber>' != null && '<customerNumber>' != '' && '<customerNumber>' != 'null') customerData.customerNumber = '<customerNumber>'
  * if ('<firstName>' != null && '<firstName>' != '' && '<firstName>' != 'null') customerData.firstName = '<firstName>'
  * if ('<lastName>' != null && '<lastName>' != '' && '<lastName>' != 'null') customerData.lastName = '<lastName>'
  * if ('<dateOfBirth>' != null && '<dateOfBirth>' != '' && '<dateOfBirth>' != 'null') customerData.dateOfBirth = '<dateOfBirth>'
  * if ('<identityNumber>' != null && '<identityNumber>' != '' && '<identityNumber>' != 'null') customerData.identityNumber = '<identityNumber>'
  * if ('<identityType>' != null && '<identityType>' != '' && '<identityType>' != 'null') customerData.identityType = '<identityType>'
  * if ('<email>' != null && '<email>' != '' && '<email>' != 'null') customerData.email = '<email>'
  * if ('<phoneNumber>' != null && '<phoneNumber>' != '' && '<phoneNumber>' != 'null') customerData.phoneNumber = '<phoneNumber>'
  * if ('<address>' != null && '<address>' != '' && '<address>' != 'null') customerData.address = '<address>'
  * if ('<city>' != null && '<city>' != '' && '<city>' != 'null') customerData.city = '<city>'
  * if ('<postalCode>' != null && '<postalCode>' != '' && '<postalCode>' != 'null') customerData.postalCode = '<postalCode>'
  * if ('<country>' != null && '<country>' != '' && '<country>' != 'null') customerData.country = '<country>'

  Given path '/api/customers/personal/register'
  And request customerData
  When method POST
  Then status 400
  And match response.<expectedField> contains '<expectedErrorSubstring>'

Examples:
| read('classpath:csv/usecase/personal-customer-registration-constraint-validation.csv') |