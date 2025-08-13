Feature: Authentication Helper

Background:
  * url baseUrl

@ignore
Scenario: Login with credentials
  # Use HTTP Basic Authentication for API testing
  * def authString = username + ':' + password
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * configure headers = { Authorization: 'Basic ' + encodedAuth }
  * print 'Using HTTP Basic Auth for user:', username
  * print 'Auth string:', authString
  * print 'Encoded auth:', encodedAuth

@ignore  
Scenario: Login as Customer Service
  * def username = 'cs1'
  * def password = 'minibank123'
  * call read('classpath:karate/features/auth-helper.feature@Login with credentials') { username: '#(username)', password: '#(password)' }

@ignore
Scenario: Login as Teller
  * def username = 'teller1'
  * def password = 'minibank123'
  * call read('classpath:karate/features/auth-helper.feature@Login with credentials') { username: '#(username)', password: '#(password)' }

@ignore
Scenario: Login as Branch Manager
  * def username = 'admin'
  * def password = 'minibank123'
  * call read('classpath:karate/features/auth-helper.feature@Login with credentials') { username: '#(username)', password: '#(password)' }