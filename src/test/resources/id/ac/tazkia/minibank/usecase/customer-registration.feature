Feature: Customer Registration API

Background:
  * url baseUrl
  * def testData = read('classpath:csv/usecase/customer-registration-normal.csv')

Scenario Outline: Register customer - <testCase>
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
  Then status 201
  And match response.customerType == '<customerType>'
  And match response.customerNumber == '<customerNumber>'
  And match response.email == '<email>'
  And match response.id == '#uuid'
  And match response.createdDate == '#present'
  * if ('<customerType>' == 'PERSONAL') karate.call('classpath:id/ac/tazkia/minibank/usecase/validate-personal-customer.js', { response: response, firstName: '<firstName>', lastName: '<lastName>' })
  * if ('<customerType>' == 'CORPORATE') karate.call('classpath:id/ac/tazkia/minibank/usecase/validate-corporate-customer.js', { response: response, companyName: '<companyName>' })

Examples:
| read('classpath:csv/usecase/customer-registration-normal.csv') |