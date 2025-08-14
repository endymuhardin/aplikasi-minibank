Feature: User Management API

Background:
  * url baseUrl
  * configure cookies = true
  
  # Authenticate as Branch Manager (has USER_CREATE permissions)
  * def authString = 'admin:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * def authHeader = 'Basic ' + encodedAuth
  * configure headers = { Authorization: '#(authHeader)' }

Scenario Outline: Create user - <testDescription>
  * def uniqueId = java.lang.System.currentTimeMillis() + ''
  * def userData = {}
  * userData.username = '<username>' + uniqueId
  * userData.email = '<username>' + uniqueId + '@yopmail.com'
  * userData.fullName = '<fullName>' + ' ' + uniqueId
  * userData.password = 'Test123456'

  Given path '/api/users'
  And request userData
  When method POST
  Then status 201
  And match response.username == userData.username
  And match response.email == userData.email
  And match response.fullName == userData.fullName
  And match response.id == '#uuid'
  And match response.isActive == true
  And match response.isLocked == false
  And match response.createdDate == '#present'

Examples:
| read('classpath:fixtures/user/user_creation_normal.csv') |

Scenario: Get user by ID
  # First create a user
  * def createUserData = { username: 'gettest', email: 'gettest@yopmail.com', fullName: 'Get Test User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201
  * def userId = response.id

  # Now get the user by ID
  Given path '/api/users/' + userId
  When method GET
  Then status 200
  And match response.id == userId
  And match response.username == 'gettest'
  And match response.email == 'gettest@yopmail.com'

Scenario: Get user by username
  # First create a user
  * def createUserData = { username: 'usernametest', email: 'usernametest@yopmail.com', fullName: 'Username Test User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201

  # Now get the user by username
  Given path '/api/users/username/usernametest'
  When method GET
  Then status 200
  And match response.username == 'usernametest'
  And match response.email == 'usernametest@yopmail.com'

Scenario: Get all users
  # Create multiple users first
  * def user1 = { username: 'listuser1', email: 'listuser1@yopmail.com', fullName: 'List User 1', password: 'Test123456' }
  * def user2 = { username: 'listuser2', email: 'listuser2@yopmail.com', fullName: 'List User 2', password: 'Test123456' }
  
  Given path '/api/users'
  And request user1
  When method POST
  Then status 201

  Given path '/api/users'
  And request user2
  When method POST
  Then status 201

  # Get all users
  Given path '/api/users'
  When method GET
  Then status 200
  And match response == '#array'
  And match response[*].username contains 'listuser1'
  And match response[*].username contains 'listuser2'

Scenario: Search users
  # Create a user with searchable name
  * def searchUserData = { username: 'searchtest', email: 'searchtest@yopmail.com', fullName: 'Searchable Test User', password: 'Test123456' }
  Given path '/api/users'
  And request searchUserData
  When method POST
  Then status 201

  # Search by username
  Given path '/api/users'
  And param search = 'searchtest'
  When method GET
  Then status 200
  And match response == '#array'
  And match response[0].username == 'searchtest'

Scenario: Update user
  # First create a user
  * def createUserData = { username: 'updatetest', email: 'updatetest@yopmail.com', fullName: 'Update Test User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201
  * def userId = response.id

  # Update the user
  * def updateData = { username: 'updatetest', email: 'updated@yopmail.com', fullName: 'Updated Test User' }
  Given path '/api/users/' + userId
  And request updateData
  When method PUT
  Then status 200
  And match response.email == 'updated@yopmail.com'
  And match response.fullName == 'Updated Test User'

Scenario: Activate user
  # First create a user
  * def createUserData = { username: 'activatetest', email: 'activatetest@yopmail.com', fullName: 'Activate Test User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201
  * def userId = response.id

  # Deactivate first
  Given path '/api/users/' + userId + '/deactivate'
  When method PUT
  Then status 200
  And match response.isActive == false

  # Then activate
  Given path '/api/users/' + userId + '/activate'
  When method PUT
  Then status 200
  And match response.isActive == true

Scenario: Change password
  # First create a user
  * def createUserData = { username: 'passwordtest', email: 'passwordtest@yopmail.com', fullName: 'Password Test User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201
  * def userId = response.id

  # Change password
  * def passwordData = { newPassword: 'NewPassword123' }
  Given path '/api/users/' + userId + '/password'
  And request passwordData
  When method PUT
  Then status 200
  And match response.message == 'Password changed successfully'

Scenario: Delete user
  # First create a user
  * def createUserData = { username: 'deletetest', email: 'deletetest@yopmail.com', fullName: 'Delete Test User', password: 'Test123456' }
  Given path '/api/users'
  And request createUserData
  When method POST
  Then status 201
  * def userId = response.id

  # Delete the user
  Given path '/api/users/' + userId
  When method DELETE
  Then status 200
  And match response.message == 'User deleted successfully'

  # Verify user is deleted
  Given path '/api/users/' + userId
  When method GET
  Then status 404