const config = {
    server: {
        port: 3000,
        amfkBasePath: '/satinweb/orderbridge/methods',
        amspBasePath: '/amsp/api/sams/v1/methods',
        areaCodes: [
            '+49711811',
            '+49711801',
            '+00(000)1234'
        ],
        endpointsForResponseCheck: {
            getOrder: '/GetOrder.php',
            listUserExtensions: '/ListUserExtensions.php'
        },
        logLevel: 'debug',
        keyPath: './certs/server-key.pem',
        certPath: './certs/server-cert.pem'
    },
    systems: {
        amfkSystem: 'https://rngava-amfktest.avaya.bosch-ucc.com',
        amspSystem: 'https://fe-avapoc-amsp1.avaya.bosch-ucc.com'
    },
    env: 'PROD'
};

module.exports = config;