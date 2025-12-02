const axios = require('axios');
const fs = require('fs');
const path = require('path');
const https = require('https');

const ca = fs.readFileSync(path.join(__dirname, '../certs/ca_certificates.pem'));

const httpsAgent = new https.Agent({ ca: ca, rejectUnauthorized: true});

// Create a custom Axios instance using a specific HTTPS agent configuration.
const axiosClient = axios.create({
    httpsAgent: httpsAgent
});

module.exports = axiosClient;
module.exports.httpsAgent = httpsAgent;