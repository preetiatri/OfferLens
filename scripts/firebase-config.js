// Firebase Admin Configuration
// This file loads the service account key from a secure location

const path = require('path');
const os = require('os');

// Path to service account key in secure location
const SERVICE_ACCOUNT_PATH = path.join(
    os.homedir(),
    '.firebase',
    'offerlens-service-account.json'
);

// Load and export the service account
let serviceAccount;
try {
    serviceAccount = require(SERVICE_ACCOUNT_PATH);
    console.log('✅ Service account loaded from secure location');
} catch (error) {
    console.error('❌ Failed to load service account key from:', SERVICE_ACCOUNT_PATH);
    console.error('Error:', error.message);
    console.error('\nPlease ensure the service account key is located at:');
    console.error(SERVICE_ACCOUNT_PATH);
    process.exit(1);
}

module.exports = serviceAccount;
