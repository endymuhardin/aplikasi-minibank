Feature: Personal Customer Registration API

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Customer Service (has CUSTOMER_CREATE permissions)
  * def authString = 'cs1:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * def authHeader = 'Basic ' + encodedAuth
  * configure headers = { Authorization: '#(authHeader)' }
  
  * def testData = read('classpath:fixtures/customer/personal/personal-customer-registration-normal.csv')

Scenario Outline: Register personal customer - <testCase>
  * def customerData = {}
  * customerData.customerNumber = '<customerNumber>'
  * customerData.firstName = '<firstName>'
  * customerData.lastName = '<lastName>'
  * customerData.dateOfBirth = '<dateOfBirth>'
  * customerData.identityNumber = '<identityNumber>'
  * customerData.identityType = '<identityType>'
  * customerData.email = '<email>'
  * customerData.phoneNumber = '<phoneNumber>'
  * customerData.address = '<address>'
  * customerData.city = '<city>'
  * customerData.postalCode = '<postalCode>'
  * customerData.country = '<country>'

  Given path '/api/customers/personal/register'
  And request customerData
  When method POST
  Then status 201
  And match response.customerNumber == '<customerNumber>'
  And match response.firstName == '<firstName>'
  And match response.lastName == '<lastName>'
  And match response.email == '<email>'
  And match response.id == '#uuid'
  And match response.createdDate == '#present'

Examples:
| read('classpath:fixtures/customer/personal/personal-customer-registration-normal.csv') |