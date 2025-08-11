Feature: Account Opening API - Validation Tests

Background:
  * url baseUrl
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