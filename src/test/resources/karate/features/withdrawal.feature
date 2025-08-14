Feature: Withdrawal Transaction API

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Teller (has TRANSACTION_WITHDRAWAL permissions)
  * def authString = 'teller1:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * def authHeader = 'Basic ' + encodedAuth
  * configure headers = { Authorization: '#(authHeader)' }
  
  * def accountLookup = {}
  
  # Set up account ID mappings (using fixed UUIDs from setup SQL)
  * accountLookup['ACC0000001'] = '11111111-1111-1111-aaaa-111111111111'
  * accountLookup['ACC0000002'] = '22222222-2222-2222-bbbb-222222222222'
  * accountLookup['ACC0000003'] = '33333333-3333-3333-cccc-333333333333'
  * accountLookup['ACC0000004'] = '44444444-4444-4444-dddd-444444444444'
  * accountLookup['ACC0000005'] = '55555555-5555-5555-eeee-555555555555'

Scenario Outline: Withdrawal transaction - <testCase>
  * def withdrawalData = {}
  * withdrawalData.accountId = accountLookup['<accountId>']
  * withdrawalData.amount = <amount>
  * if ('<description>' != null && '<description>' != '') withdrawalData.description = '<description>'
  * if ('<referenceNumber>' != null && '<referenceNumber>' != '') withdrawalData.referenceNumber = '<referenceNumber>'
  * if ('<createdBy>' != null && '<createdBy>' != '') withdrawalData.createdBy = '<createdBy>'

  Given path '/api/transactions/withdrawal'
  And request withdrawalData
  When method POST
  Then status 201
  And match response.transactionId == '#uuid'
  And match response.transactionNumber == '#string'
  And match response.transactionNumber == '#regex TXN\\d{7}'
  And match response.accountId == withdrawalData.accountId
  And match response.accountNumber == '#string'
  And match response.amount == <amount>
  And match response.balanceBefore == '#number'
  And match response.balanceAfter == '#number'
  And match response.balanceAfter == (response.balanceBefore - <amount>)
  And match response.currency == 'IDR'
  And match response.channel == 'TELLER'
  And match response.transactionDate == '#present'
  And match response.processedDate == '#present'
  
  # Verify account info in response
  And match response.account.id == withdrawalData.accountId
  And match response.account.accountNumber == '#string'
  And match response.account.accountName == '#string'
  And match response.account.currentBalance == response.balanceAfter
  
  # Response will have description and referenceNumber if provided (can be null)
  And match response.description == '#present'
  And match response.referenceNumber == '#present'

Examples:
| read('classpath:fixtures/transaction/withdrawal-normal.csv') |