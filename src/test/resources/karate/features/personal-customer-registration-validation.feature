Feature: Personal Customer Registration API - Validation Tests

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Customer Service (has CUSTOMER_CREATE permissions)
  * def authString = 'cs1:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * def authHeader = 'Basic ' + encodedAuth
  * configure headers = { Authorization: '#(authHeader)' }
  
  * def testData = read('classpath:fixtures/customer/personal/personal-customer-registration-validation.csv')

Scenario Outline: Personal customer validation error - <testCase>
  * def customerData = {}
  * if ('<customerNumber>' != null && '<customerNumber>' != '') customerData.customerNumber = '<customerNumber>'
  * if ('<firstName>' != null && '<firstName>' != '') customerData.firstName = '<firstName>'
  * if ('<lastName>' != null && '<lastName>' != '') customerData.lastName = '<lastName>'
  * if ('<dateOfBirth>' != null && '<dateOfBirth>' != '') customerData.dateOfBirth = '<dateOfBirth>'
  * if ('<identityNumber>' != null && '<identityNumber>' != '') customerData.identityNumber = '<identityNumber>'
  * if ('<identityType>' != null && '<identityType>' != '') customerData.identityType = '<identityType>'
  * if ('<email>' != null && '<email>' != '') customerData.email = '<email>'
  * if ('<phoneNumber>' != null && '<phoneNumber>' != '') customerData.phoneNumber = '<phoneNumber>'
  * if ('<address>' != null && '<address>' != '') customerData.address = '<address>'
  * if ('<city>' != null && '<city>' != '') customerData.city = '<city>'
  * if ('<postalCode>' != null && '<postalCode>' != '') customerData.postalCode = '<postalCode>'
  * if ('<country>' != null && '<country>' != '') customerData.country = '<country>'

  Given path '/api/customers/personal/register'
  And request customerData
  When method POST
  Then status 400
  And eval karate.match(response['<expectedError>'], '<expectedErrorMessage>')

Examples:
| read('classpath:fixtures/customer/personal/personal-customer-registration-validation.csv') |