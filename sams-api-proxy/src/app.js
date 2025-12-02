require('dotenv').config();
const https = require('https');
const express = require('express');
const fs = require('fs');
const { createProxyMiddleware } = require('http-proxy-middleware');
const config = require('../config/config');
const logger = require('./logger');
const axiosClient = require('./apiClient');
const { httpsAgent } = require('./apiClient');

const PORT = config.server.port;
const ENV = config.env;
const SERVER_KEY_PATH = config.server.keyPath;
const SERVER_CERT_PATH = config.server.certPath;
const AMFK_BASE_PATH = config.server.amfkBasePath;
const AMSP_BASE_PATH = config.server.amspBasePath;
const AREA_CODES = config.server.areaCodes;
const ENDPOINTS_FOR_RESPONSE_CHECK = config.server.endpointsForResponseCheck;
const AMFK_SYSTEM = config.systems.amfkSystem;
const AMSP_SYSTEM = config.systems.amspSystem;

// Recursively searches for an "AreaCode" or "areaCode" property within a nested object.
const findAreaCode = (object) => {
    if (typeof object !== 'object' || object === null) {
        return undefined;
    }
    if ('AreaCode' in object || 'areaCode' in object) {
        return object.AreaCode || object.areaCode;
    }
    // If not found, it recursively searches all nested objects until a match is found.
    for (const key in object) {
        if (typeof object[key] === 'object' && object[key] !== null) {
            const result = findAreaCode(object[key]);
            if (result !== undefined) {
                return result;
            }
        }
    }
    return undefined;
}

// This function first attempts to send the request to AMSP. If the AMSP response’s AreaCode is not in the allowed list, it retries the same request with AMFK.
const getResponseFromAmspOrAmfk = async (req, res) => {
    try {
        // Construct the full AMSP request URL
        const relativePath = req.path.replace(AMFK_BASE_PATH, '');
        const amspUrl = `${AMSP_SYSTEM}${AMSP_BASE_PATH}${relativePath}`;
        logger.info(`Request routed to AMSP: ${amspUrl}`);
        
        // Clone and sanitize request headers before forwarding
        const headers = { ...req.headers };
        delete headers.host;
        delete headers.connection;
        delete headers['user-agent'];
        delete headers['postman-token'];
        delete headers['accept-encoding'];
        delete headers['content-length'];

        // Send the request to AMSP using axiosClient
        const amspResponse = await axiosClient.post(amspUrl, req.body, {
            headers: headers
        });

        // Extract and validate AreaCode from AMSP’s response
        const areaCode = findAreaCode(amspResponse.data);
        const inAreaCodes = areaCode && AREA_CODES.includes(areaCode);
        logger.debug(`AMSP response areaCode: ${areaCode}, In area list: ${inAreaCodes}`);

        // If the AreaCode is not in the allowed list, retry the same request with AMFK
        if (!inAreaCodes) {
            const amfkUrl = `${AMFK_SYSTEM}${AMFK_BASE_PATH}${relativePath}`;
            logger.info(`Retrying POST request with AMFK: ${amfkUrl}`);
            // Forward the same request body and headers to AMFK
            const amfkResponse = await axiosClient.post(amfkUrl, req.body, {
                headers: headers
            });
            // Send AMFK’s response back to the client
            res.status(amfkResponse.status).set(amfkResponse.headers).send(amfkResponse.data);
            return;
        }
        // If AMSP’s response is valid (area code is allowed), return it directly to the client
        res.status(amspResponse.status).set(amspResponse.headers).send(amspResponse.data);
    } catch (error) {
        // Handle errors from either AMSP or AMFK requests.
        if (error.response) {
            logger.error(`${error.response?.data?.message?.errorText}`);
            res.status(error.response.status).set(error.response.headers).send(error.response.data);
        } else {
            // Network error or other unexpected failure
            logger.error(`Error processing request: ${error.message}`);
            res.status(500).send("Error processing request");
        }
    }
}

// Create a proxy middleware to route requests between AMSP and AMFK systems based on areaCode.
const proxyMiddleware = createProxyMiddleware({
    target: AMFK_SYSTEM,
    changeOrigin: true,
    agent: httpsAgent,
    secure: true,
    logLevel: 'warn',
    router: (req) => {
        // If request body exists, route to AMSP_SYSTEM if AreaCode matches, otherwise to AMFK_SYSTEM
        if (req.body) return AREA_CODES.includes(findAreaCode(req.body)) ? AMSP_SYSTEM : AMFK_SYSTEM;
        // Default to AMFK_SYSTEM if no body is present
        return AMFK_SYSTEM;
    },
    pathRewrite: (path, req) => AREA_CODES.includes(findAreaCode(req.body)) ? `${AMSP_BASE_PATH}${path}` : `${AMFK_BASE_PATH}${path}`,
    on: {
        // Fired when a proxy request is created and sent to the target server
        proxyReq: (proxyReq, req, res) => {
            // For POST, PUT, or PATCH requests, serialize and send the request body to the target
            if (['POST', 'PUT', 'PATCH'].includes(req.method)) {
                if (req.body) {
                    const bodyData = JSON.stringify(req.body);
                    proxyReq.setHeader('Content-Type', 'application/json');
                    proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
                    proxyReq.write(bodyData);
                }
            }
            logger.debug(`Proxy Request: ${req.method} ${req.url} ==> Target Host: ${proxyReq.getHeader('host')}`);
        },
        // Fired when a response is received back from the target server
        proxyRes: (proxyRes, req, res) => {
            logger.debug(`Proxy Response: ${proxyRes.statusCode} for ${req.method} ${req.url} ==> Target Host: ${proxyRes.req.getHeader('host')}`);
        },
        // Fired when an error occurs during the proxying process
        error: (err, req, res) => {
            logger.error(`Proxy Error: ${err.message}`);
            res.status(500).send("Proxy Error");
        }
    }
});

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Middleware to log every incoming HTTP request
app.use((req, res, next) => {
    logger.info(`Incoming Request: ${req.method} ${req.originalUrl}`);
    next();
});

// Route handler for POST requests to GetOrder
app.post(`${AMFK_BASE_PATH}${ENDPOINTS_FOR_RESPONSE_CHECK.getOrder}`, getResponseFromAmspOrAmfk);

// Route handler for POST requests to ListUserExtensions
app.post(`${AMFK_BASE_PATH}${ENDPOINTS_FOR_RESPONSE_CHECK.listUserExtensions}`, getResponseFromAmspOrAmfk);

// Default proxy middleware for all other routes under the AMFK_BASE_PATH
app.use(AMFK_BASE_PATH, proxyMiddleware);

// Starting the server
if (ENV === 'DEV') {
    // Start the server using HTTP (development mode only)
    app.listen(PORT, () => {
        logger.info(`API Gateway listening on port ${PORT}`);
    });
} else {
    // Start the server using HTTPS with SSL/TLS certificates for secure connections
    const sslOptions = {
        key: fs.readFileSync(SERVER_KEY_PATH),
        cert: fs.readFileSync(SERVER_CERT_PATH)
    }
    https.createServer(sslOptions, app).listen(PORT, () => {
        logger.info(`API Gateway listening on port ${PORT}`);
    });
}