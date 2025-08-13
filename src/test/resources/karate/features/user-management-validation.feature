Feature: User Management API Validation

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Branch Manager (has USER_CREATE permissions)
  * call read('classpath:karate/features/auth-helper.feature@Login as Branch Manager')

Scenario Outline: Create user validation - <testDescription>
  * def userData = {}
  * if ('<username>' != 'BLANK') userData.username = '<username>'
  * if ('<email>' != 'BLANK') userData.email = '<email>'
  * if ('<fullName>' != 'BLANK') userData.fullName = '<fullName>'
  * if ('<password>' != 'BLANK') userData.password = '<password>'

  Given path '/api/users'
  And request userData
  When method POST
  Then status 400
  And match response == '#object'

Examples:
| read('classpath:fixtures/user/user_validation_errors.csv') |

Scenario: Create user with duplicate username
  # First create a user
  * def userData1 = { username: 'duplicate', email: 'first@yopmail.com', fullName: 'First User', password: 'Test123456' }
  Given path '/api/users'
  And request userData1
  When method POST
  Then status 201

  # Try to create another user with same username
  * def userData2 = { username: 'duplicate', email: 'second@yopmail.com', fullName: 'Second User', password: 'Test123456' }
  Given path '/api/users'
  And request userData2
  When method POST
  Then status 400
  And match response.username == 'Username already exists'

Scenario: Create user with duplicate email
  # First create a user
  * def userData1 = { username: 'user1', email: 'duplicate@yopmail.com', fullName: 'First User', password: 'Test123456' }
  Given path '/api/users'
  And request userData1
  When method POST
  Then status 201

  # Try to create another user with same email
  * def userData2 = { username: 'user2', email: 'duplicate@yopmail.com', fullName: 'Second User', password: 'Test123456' }
  Given path '/api/users'
  And request userData2
  When method POST
  Then status 400
  And match response.email == 'Email already exists'

Scenario: Update user with duplicate username
  # Create two users
  * def userData1 = { username: 'original1', email: 'orig1@yopmail.com', fullName: 'Original User 1', password: 'Test123456' }
  * def userData2 = { username: 'original2', email: 'orig2@yopmail.com', fullName: 'Original User 2', password: 'Test123456' }
  
  Given path '/api/users'
  And request userData1
  When method POST
  Then status 201
  * def userId1 = response.id

  Given path '/api/users'
  And request userData2
  When method POST
  Then status 201
  * def userId2 = response.id

  # Try to update user2 with user1's username
  * def updateData = { username: 'original1', email: 'orig2@yopmail.com', fullName: 'Original User 2' }
  Given path '/api/users/' + userId2
  And request updateData
  When method PUT
  Then status 400
  And match response.username == 'Username already exists'

Scenario: Update user with duplicate email
  # Create two users
  * def userData1 = { username: 'emailuser1', email: 'email1@yopmail.com', fullName: 'Email User 1', password: 'Test123456' }
  * def userData2 = { username: 'emailuser2', email: 'email2@yopmail.com', fullName: 'Email User 2', password: 'Test123456' }
  
  Given path '/api/users'
  And request userData1
  When method POST
  Then status 201
  * def userId1 = response.id

  Given path '/api/users'
  And request userData2
  When method POST
  Then status 201
  * def userId2 = response.id

  # Try to update user2 with user1's email
  * def updateData = { username: 'emailuser2', email: 'email1@yopmail.com', fullName: 'Email User 2' }
  Given path '/api/users/' + userId2
  And request updateData
  When method PUT
  Then status 400
  And match response.email == 'Email already exists'

Scenario: Get non-existent user
  * def nonExistentId = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  Given path '/api/users/' + nonExistentId
  When method GET
  Then status 404

Scenario: Get user by non-existent username
  Given path '/api/users/username/nonexistent'
  When method GET
  Then status 404

Scenario: Update non-existent user
  * def nonExistentId = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  * def updateData = { username: 'test', email: 'test@yopmail.com', fullName: 'Test User' }
  Given path '/api/users/' + nonExistentId
  And request updateData
  When method PUT
  Then status 404

Scenario: Change password for non-existent user
  * def nonExistentId = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  * def passwordData = { newPassword: 'NewPassword123' }
  Given path '/api/users/' + nonExistentId + '/password'
  And request passwordData
  When method PUT
  Then status 404

Scenario: Change password with invalid data
  # First create a user
  * def createUserData = { username: 'passinvalid', email: 'passinvalid@yopmail.com', fullName: 'Password Invalid User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201
  * def userId = response.id

  # Try to change with short password
  * def passwordData = { newPassword: '123' }
  Given path '/api/users/' + userId + '/password'
  And request passwordData
  When method PUT
  Then status 400
  And match response.newPassword == 'Password must be at least 6 characters'