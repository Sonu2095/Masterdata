const path = require('path');
const fs = require('fs');
const winston = require('winston');
const config = require('../config/config');

const LOG_LEVEL = config.server.logLevel;
const logDirectory = path.join(__dirname, '../logs');

// Create logs folder if doesn't exist
if (!fs.existsSync(logDirectory)) {
    fs.mkdirSync(logDirectory);
}

// Create a Winston logger instance with configurable log level and formatting.
const logger = winston.createLogger({
    level: LOG_LEVEL,
    format: winston.format.combine(
        winston.format.colorize(),
        winston.format.timestamp(),
        winston.format.printf(({ timestamp, level, message }) => `${timestamp} [${level.toUpperCase()}]: ${message}`)
    ),
    transports: [
        new winston.transports.File({ filename: path.join(logDirectory, 'app.log'), level: LOG_LEVEL})
    ]
});

module.exports = logger;