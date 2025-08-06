Feature: Withdrawal Transaction API Validation

Background:
  * url baseUrl
  * def accountLookup = {}
  
  # Set up account ID mappings (using fixed UUIDs from setup SQL)
  * accountLookup['ACC0000001'] = '11111111-1111-1111-aaaa-111111111111'
  * accountLookup['ACC0000002'] = '22222222-2222-2222-bbbb-222222222222'
  * accountLookup['ACC0000003'] = '33333333-3333-3333-cccc-333333333333'
  * accountLookup['ACC0000004'] = '44444444-4444-4444-dddd-444444444444'
  * accountLookup['ACC0000005'] = '55555555-5555-5555-eeee-555555555555'

Scenario Outline: Withdrawal validation - <testCase>
  * def withdrawalData = {}
  
  # Handle special case for invalid account ID
  * if ('<accountId>' == '00000000-0000-0000-0000-000000000000') withdrawalData.accountId = '<accountId>'
  * if ('<accountId>' != '00000000-0000-0000-0000-000000000000') withdrawalData.accountId = accountLookup['<accountId>']
  
  # Handle optional/missing fields
  * if ('<amount>' != '' && '<amount>' != null && '<amount>' != 'null') withdrawalData.amount = <amount>
  * if ('<description>' != null && '<description>' != '') withdrawalData.description = '<description>'
  * if ('<referenceNumber>' != null && '<referenceNumber>' != '') withdrawalData.referenceNumber = '<referenceNumber>'
  * if ('<createdBy>' != null && '<createdBy>' != '') withdrawalData.createdBy = '<createdBy>'

  Given path '/api/transactions/withdrawal'
  And request withdrawalData
  When method POST
  Then status <expectedStatus>
  And match response['<expectedErrorField>'] == '<expectedErrorMessage>'

Examples:
| read('classpath:fixtures/transaction/withdrawal-validation.csv') |