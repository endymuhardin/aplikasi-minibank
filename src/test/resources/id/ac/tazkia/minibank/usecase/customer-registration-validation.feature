Feature: Customer Registration API - Validation Tests

Background:
  * url baseUrl  
  * def testData = read('classpath:csv/usecase/customer-registration-validation.csv')

Scenario Outline: Validation error - <testCase>
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
  And eval karate.match(response['<expectedError>'], '<expectedErrorMessage>')

Examples:
| read('classpath:csv/usecase/customer-registration-validation.csv') |