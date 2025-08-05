function fn() {
  var env = karate.env; // get java system property 'karate.env'
  karate.log('karate.env system property was:', env);
  
  if (!env) {
    env = 'dev'; // a custom 'intelligent' default
  }
  
  // Get port from system property set by Spring Boot test
  var port = karate.properties['karate.port'] || '8080';
  var baseUrl = 'http://localhost:' + port;
  
  var config = {
    env: env,
    baseUrl: baseUrl
  }
  
  karate.log('karate config:', config);
  karate.log('using baseUrl:', baseUrl);
  return config;
}