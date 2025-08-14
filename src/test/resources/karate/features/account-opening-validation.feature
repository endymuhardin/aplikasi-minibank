Feature: Account Opening API - Validation Tests

Background:
  * url baseUrl
  
  # Authenticate as Customer Service (has CUSTOMER_READ, ACCOUNT_CREATE permissions)
  * def authString = 'cs1:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * def authHeader = 'Basic ' + encodedAuth
  * configure headers = { Authorization: '#(authHeader)' }
  
  * def customerLookup = {}
  * def productLookup = {}
  
  # Lookup customers dynamically from API - get both personal and corporate
  * path '/api/customers/personal'
  * method GET
  * print 'Headers being sent:', karate.get('request.headers')
  * print 'Personal customers API response status:', responseStatus
  * print 'Personal customers API response:', response
  * status 200
  * def personalCustomers = response
  * print 'personalCustomers type:', karate.typeOf(personalCustomers)
  * print 'personalCustomers:', personalCustomers
  * match karate.typeOf(personalCustomers) == 'list'
  * personalCustomers.forEach(function(customer) { customerLookup[customer.customerNumber] = customer.id })
  
  * path '/api/customers/corporate'
  * method GET
  * status 200
  * def corporateCustomers = response
  * corporateCustomers.forEach(function(customer) { customerLookup[customer.customerNumber] = customer.id })
  
  # Lookup products dynamically from API
  * path '/api/products'
  * method GET  
  * status 200
  * def products = response
  * products.forEach(function(product) { productLookup[product.productCode] = product.id })

Scenario Outline: Account opening validation error - <testCase>
  * def accountData = {}
  * if ('<customerId>' != null && '<customerId>' != '' && '<customerId>' != '99999999-9999-9999-9999-999999999999') accountData.customerId = customerLookup['<customerId>']
  * if ('<customerId>' == '99999999-9999-9999-9999-999999999999') accountData.customerId = '<customerId>'
  * if ('<productId>' != null && '<productId>' != '' && '<productId>' != '99999999-9999-9999-9999-999999999999') accountData.productId = productLookup['<productId>']
  * if ('<productId>' == '99999999-9999-9999-9999-999999999999') accountData.productId = '<productId>'
  * if ('<accountName>' != null && '<accountName>' != '') accountData.accountName = '<accountName>'
  * if ('<initialDeposit>' != null && '<initialDeposit>' != '') accountData.initialDeposit = <initialDeposit>
  * if ('<createdBy>' != null && '<createdBy>' != '') accountData.createdBy = '<createdBy>'

  Given path '/api/accounts/open'
  And request accountData
  When method POST
  Then status 400
  And eval karate.match(response['<expectedError>'], '<expectedErrorMessage>')

Examples:
| read('classpath:fixtures/account/account-opening-validation.csv') |