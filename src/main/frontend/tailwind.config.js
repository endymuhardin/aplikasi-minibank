/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "../resources/templates/**/*.html",
    "../resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        // Bank BSI Primary Colors
        'bsi-primary': '#00a39d',
        'bsi-secondary': '#f15922',
        'bsi-accent': '#eb914e',
        'bsi-success': '#65ad59',
        'bsi-info': '#4eabeb',
        'bsi-warning': '#9acb34',
        'bsi-light-green': '#76c16a',
        
        // Bank BSI Greys
        'bsi-gray': '#7f7f7f',
        'bsi-light-gray': '#7a7e81',
        'bsi-bg-light': '#f2f2f2',
        'bsi-bg-cream': '#fdf5ed',
        'bsi-bg-neutral': '#fcfcfc',
        
        // Bank BSI Dark Colors
        'bsi-dark': '#353e4a',
        'bsi-darker': '#222',
        
        // Bank BSI Additional Colors
        'bsi-blue-light': '#4eabeb',
        'bsi-orange-dark': '#ec7272',
        'bsi-teal': '#39a49a',
      }
    },
  },
  plugins: [],
}