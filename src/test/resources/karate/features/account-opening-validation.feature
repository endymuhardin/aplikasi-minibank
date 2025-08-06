Feature: Account Opening API - Validation Tests

Background:
  * url baseUrl
  * def customerLookup = {}
  * def productLookup = {}
  
  # Set up customer ID mappings (using fixed UUIDs from setup SQL)
  * customerLookup['C1000001'] = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
  * customerLookup['C1000002'] = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
  * customerLookup['C1000003'] = 'cccccccc-cccc-cccc-cccc-cccccccccccc'
  * customerLookup['C1000004'] = 'dddddddd-dddd-dddd-dddd-dddddddddddd'
  * customerLookup['C1000005'] = 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'
  * customerLookup['C1000006'] = 'ffffffff-ffff-ffff-ffff-ffffffffffff'
  
  # Set up product ID mappings (using fixed UUIDs from setup SQL)
  * productLookup['SAV001'] = '11111111-1111-1111-1111-111111111111'
  * productLookup['SAV002'] = '22222222-2222-2222-2222-222222222222'
  * productLookup['SAV003'] = '44444444-4444-4444-4444-444444444444'
  * productLookup['CHK001'] = '33333333-3333-3333-3333-333333333333'
  * productLookup['CHK002'] = '55555555-5555-5555-5555-555555555555'

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