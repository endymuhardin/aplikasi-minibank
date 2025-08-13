Feature: Deposit Transaction Validation API

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Teller (has TRANSACTION_DEPOSIT permissions)
  * call read('classpath:karate/features/auth-helper.feature@Login as Teller')
  
  * def accountLookup = {}
  
  # Set up account ID mappings (using fixed UUIDs from setup SQL)
  * accountLookup['ACC0000001'] = '11111111-1111-1111-aaaa-111111111111'
  * accountLookup['ACC0000002'] = '22222222-2222-2222-bbbb-222222222222'
  * accountLookup['ACC0000003'] = '33333333-3333-3333-cccc-333333333333'
  * accountLookup['ACC0000004'] = '44444444-4444-4444-dddd-444444444444'

Scenario: Invalid account ID
  * def depositData = { accountId: '99999999-9999-9999-9999-999999999999', amount: 1000.00, description: 'Test deposit', referenceNumber: 'REF001', createdBy: 'teller01' }
  Given path '/api/transactions/deposit'
  And request depositData
  When method POST
  Then status 400
  And match response.accountId == 'Account not found'

Scenario: Zero amount
  * def depositData = { accountId: '#(accountLookup.ACC0000001)', amount: 0.00, description: 'Invalid deposit', referenceNumber: 'REF002', createdBy: 'teller01' }
  Given path '/api/transactions/deposit'
  And request depositData
  When method POST
  Then status 400
  And match response.amount == 'Amount must be greater than 0'

Scenario: Negative amount  
  * def depositData = { accountId: '#(accountLookup.ACC0000001)', amount: -100.00, description: 'Invalid deposit', referenceNumber: 'REF003', createdBy: 'teller01' }
  Given path '/api/transactions/deposit'
  And request depositData
  When method POST
  Then status 400
  And match response.amount == 'Amount must be greater than 0'

Scenario: Missing account ID
  * def depositData = { amount: 1000.00, description: 'Test deposit', referenceNumber: 'REF004', createdBy: 'teller01' }
  Given path '/api/transactions/deposit'
  And request depositData
  When method POST
  Then status 400
  And match response.accountId == 'Account ID is required'

Scenario: Missing amount
  * def depositData = { accountId: '#(accountLookup.ACC0000001)', description: 'Test deposit', referenceNumber: 'REF005', createdBy: 'teller01' }
  Given path '/api/transactions/deposit'
  And request depositData
  When method POST
  Then status 400
  And match response.amount == 'Amount is required'

Scenario: Deposit to inactive account
  * def depositData = {}
  * depositData.accountId = accountLookup['ACC0000004']
  * depositData.amount = 1000.00
  * depositData.description = 'Test deposit to inactive account'
  * depositData.createdBy = 'teller01'

  Given path '/api/transactions/deposit'
  And request depositData
  When method POST
  Then status 400
  And match response.accountId == 'Account is not active'