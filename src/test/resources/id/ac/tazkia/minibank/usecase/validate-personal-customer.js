function(config) {
  var response = config.response;
  var firstName = config.firstName;
  var lastName = config.lastName;
  
  karate.match(response.firstName, firstName);
  karate.match(response.lastName, lastName);
  
  return response;
}