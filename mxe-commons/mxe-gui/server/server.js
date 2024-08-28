const express = require('express');
const helmet = require('helmet');

const app = express();
const PORT = 8080;

// Enable OWSAP
// TODO: CSP
app.use(
  helmet({
    contentSecurityPolicy: false,
  })
);

// Hide server details
app.disable('x-powered-by');

// Serves static content
app.use(express.static('public'));

// Redirect all call to Home
app.use((req, res) => {
  res.redirect(301, '/');
});

const server = app.listen(PORT, () => {
  console.log(`Server listening in ${PORT}`);
});

app.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  server.close(() => {
    console.log('HTTP server closed');
  });
});
