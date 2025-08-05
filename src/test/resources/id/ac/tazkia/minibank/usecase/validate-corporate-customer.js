function(config) {
  var response = config.response;
  var companyName = config.companyName;
  
  karate.match(response.companyName, companyName);
  
  return response;
}