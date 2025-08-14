Feature: Account Opening API

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
  * status 200
  * def personalCustomers = response
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

Scenario Outline: Open account - <testCase>
  * def accountData = {}
  * accountData.customerId = customerLookup['<customerId>']
  * accountData.productId = productLookup['<productId>']
  * accountData.accountName = '<accountName>'
  * accountData.initialDeposit = <initialDeposit>
  * if ('<createdBy>' != null && '<createdBy>' != '') accountData.createdBy = '<createdBy>'

  Given path '/api/accounts/open'
  And request accountData
  When method POST
  Then status 201
  And match response.accountId == '#uuid'
  And match response.accountNumber == '#string'
  And match response.accountNumber == '#regex ACC\\d{7}'
  And match response.accountName == '<accountName>'
  And match response.balance == <initialDeposit>
  And match response.status == 'ACTIVE'
  And match response.openedDate == '#string'
  And match response.createdDate == '#present'
  
  # Verify customer info in response
  And match response.customer.id == accountData.customerId
  And match response.customer.customerNumber == '#string'
  And match response.customer.displayName == '#string'
  And match response.customer.customerType == '#string'
  
  # Verify product info in response
  And match response.product.id == accountData.productId
  And match response.product.productCode == '<productId>'
  And match response.product.productName == '#string'
  And match response.product.productType == '#string'
  And match response.product.profitSharingRatio == '#number'

Examples:
| read('classpath:fixtures/account/account-opening-normal.csv') |