Feature: Corporate Customer Registration API - Validation Tests

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Customer Service (has CUSTOMER_CREATE permissions)
  * def authString = 'cs1:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * def authHeader = 'Basic ' + encodedAuth
  * configure headers = { Authorization: '#(authHeader)' }
  
  * def testData = read('classpath:fixtures/customer/corporate/corporate-customer-registration-validation.csv')

Scenario Outline: Corporate customer validation error - <testCase>
  * def customerData = {}
  * if ('<customerNumber>' != null && '<customerNumber>' != '') customerData.customerNumber = '<customerNumber>'
  * if ('<companyName>' != null && '<companyName>' != '') customerData.companyName = '<companyName>'
  * if ('<companyRegistrationNumber>' != null && '<companyRegistrationNumber>' != '') customerData.companyRegistrationNumber = '<companyRegistrationNumber>'
  * if ('<taxIdentificationNumber>' != null && '<taxIdentificationNumber>' != '') customerData.taxIdentificationNumber = '<taxIdentificationNumber>'
  * if ('<email>' != null && '<email>' != '') customerData.email = '<email>'
  * if ('<phoneNumber>' != null && '<phoneNumber>' != '') customerData.phoneNumber = '<phoneNumber>'
  * if ('<address>' != null && '<address>' != '') customerData.address = '<address>'
  * if ('<city>' != null && '<city>' != '') customerData.city = '<city>'
  * if ('<postalCode>' != null && '<postalCode>' != '') customerData.postalCode = '<postalCode>'
  * if ('<country>' != null && '<country>' != '') customerData.country = '<country>'

  Given path '/api/customers/corporate/register'
  And request customerData
  When method POST
  Then status 400
  And eval karate.match(response['<expectedError>'], '<expectedErrorMessage>')

Examples:
| read('classpath:fixtures/customer/corporate/corporate-customer-registration-validation.csv') |