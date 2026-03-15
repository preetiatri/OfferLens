const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

// ========================================
// CONFIGURATION - UPDATE THESE VALUES
// ========================================

// Get admin email from command line argument
const ADMIN_EMAILS = [];
if (process.argv[2]) {
    ADMIN_EMAILS.push(process.argv[2]);
} else {
    console.error('❌ Error: Please provide an admin email as an argument.');
    console.error('Usage: node setup-admin.js <email>');
    process.exit(1);
}

// ========================================
// SETUP ADMIN CLAIM FOR MULTIPLE USERS
// ========================================

async function setAdminClaim() {
    try {
        console.log('🔧 Setting up admin access...\n');
        console.log(`Processing ${ADMIN_EMAILS.length} admin email(s)...\n`);

        let successCount = 0;
        let failCount = 0;

        for (const email of ADMIN_EMAILS) {
            try {
                console.log(`\n📧 Processing: ${email}`);
                console.log('─'.repeat(50));

                // Get user by email
                const user = await admin.auth().getUserByEmail(email);

                console.log('✅ User found:');
                console.log(`   Email: ${user.email}`);
                console.log(`   UID: ${user.uid}`);
                console.log(`   Display Name: ${user.displayName || 'Not set'}`);

                // Check current claims
                const currentUser = await admin.auth().getUser(user.uid);
                console.log('   Current claims:', currentUser.customClaims || 'None');

                // Set admin custom claim
                console.log('   Setting admin claim...');
                await admin.auth().setCustomUserClaims(user.uid, {
                    admin: true,
                    role: 'admin',
                    permissions: ['read', 'write', 'delete', 'manage_users']
                });

                // Verify the claim was set
                const updatedUser = await admin.auth().getUser(user.uid);
                console.log('✅ Admin claim set successfully!');
                console.log('   New claims:', JSON.stringify(updatedUser.customClaims, null, 2));

                successCount++;
            } catch (error) {
                console.error(`❌ Failed to set admin for ${email}:`, error.message);
                if (error.code === 'auth/user-not-found') {
                    console.error('   → User not found. Make sure they signed in to the app at least once.');
                }
                failCount++;
            }
        }

        console.log('\n');
        console.log('========================================');
        console.log('✅ ADMIN SETUP COMPLETE!');
        console.log('========================================');
        console.log(`✅ Success: ${successCount} admin(s)`);
        if (failCount > 0) {
            console.log(`❌ Failed: ${failCount} admin(s)`);
        }
        console.log('');
        console.log('📋 NEXT STEPS:');
        console.log('1. All admins must sign out from the app/website');
        console.log('2. Sign in again (to get new token with admin claim)');
        console.log('3. Test admin access by trying to create/update an offer');
        console.log('');
        console.log('⚠️  IMPORTANT:');
        console.log('Custom claims only apply to NEW tokens.');
        console.log('All admins MUST sign out and sign in again for changes to take effect.');
        console.log('');

        process.exit(0);
    } catch (error) {
        console.error('❌ ERROR setting admin claims:');
        console.error('');
        console.error('Error details:', error.message);
        console.error('');
        process.exit(1);
    }
}

// ========================================
// LIST ALL USERS (OPTIONAL)
// ========================================

async function listAllUsers() {
    try {
        console.log('📋 Listing all users...\n');

        const listUsersResult = await admin.auth().listUsers(10);

        if (listUsersResult.users.length === 0) {
            console.log('No users found.');
            console.log('Sign in to your app at least once to create a user.');
            return;
        }

        console.log(`Found ${listUsersResult.users.length} user(s):\n`);

        listUsersResult.users.forEach((userRecord, index) => {
            console.log(`${index + 1}. ${userRecord.email || 'No email'}`);
            console.log(`   UID: ${userRecord.uid}`);
            console.log(`   Display Name: ${userRecord.displayName || 'Not set'}`);
            console.log(`   Custom Claims: ${JSON.stringify(userRecord.customClaims || {})}`);
            console.log('');
        });
    } catch (error) {
        console.error('Error listing users:', error.message);
    }
}

// ========================================
// REMOVE ADMIN CLAIM (OPTIONAL)
// ========================================

async function removeAdminClaim(email) {
    try {
        const user = await admin.auth().getUserByEmail(email);
        await admin.auth().setCustomUserClaims(user.uid, null);
        console.log(`✅ Admin claim removed for ${email}`);
    } catch (error) {
        console.error('Error removing admin claim:', error.message);
    }
}

// ========================================
// MAIN EXECUTION
// ========================================

// Uncomment the function you want to run:

// Set admin claim (default)
setAdminClaim();

// List all users
// listAllUsers().then(() => process.exit(0));

// Remove admin claim
// removeAdminClaim('user-email@example.com').then(() => process.exit(0));
