const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

async function listAllUsers() {
    try {
        console.log('📋 Listing all Firebase users...\n');

        const listUsersResult = await admin.auth().listUsers(100); // Get up to 100 users

        if (listUsersResult.users.length === 0) {
            console.log('❌ No users found in Firebase Authentication.');
            console.log('');
            console.log('This means:');
            console.log('- No one has signed in to the app yet');
            console.log('- You need to sign in to the app first before running setup-admin.js');
            console.log('');
            return;
        }

        console.log(`✅ Found ${listUsersResult.users.length} user(s):\n`);
        console.log('='.repeat(80));

        listUsersResult.users.forEach((userRecord, index) => {
            console.log(`\n${index + 1}. USER DETAILS:`);
            console.log(`   Email: ${userRecord.email || 'No email (anonymous user)'}`);
            console.log(`   UID: ${userRecord.uid}`);
            console.log(`   Display Name: ${userRecord.displayName || 'Not set'}`);
            console.log(`   Provider: ${userRecord.providerData.length > 0 ? userRecord.providerData[0].providerId : 'anonymous'}`);
            console.log(`   Created: ${new Date(userRecord.metadata.creationTime).toLocaleString()}`);
            console.log(`   Last Sign In: ${new Date(userRecord.metadata.lastSignInTime).toLocaleString()}`);
            console.log(`   Custom Claims: ${JSON.stringify(userRecord.customClaims || {})}`);
            console.log(`   Is Admin: ${userRecord.customClaims?.admin === true ? '✅ YES' : '❌ NO'}`);
            console.log('-'.repeat(80));
        });

        console.log('\n');
        console.log('📊 SUMMARY:');
        const adminUsers = listUsersResult.users.filter(u => u.customClaims?.admin === true);
        const regularUsers = listUsersResult.users.filter(u => !u.customClaims?.admin);
        const anonymousUsers = listUsersResult.users.filter(u => !u.email);

        console.log(`   Total Users: ${listUsersResult.users.length}`);
        console.log(`   Admin Users: ${adminUsers.length}`);
        console.log(`   Regular Users: ${regularUsers.length}`);
        console.log(`   Anonymous Users: ${anonymousUsers.length}`);
        console.log('');

        if (adminUsers.length > 0) {
            console.log('✅ Admin users:');
            adminUsers.forEach(u => console.log(`   - ${u.email || u.uid}`));
        } else {
            console.log('⚠️  No admin users found. Run setup-admin.js to grant admin access.');
        }

        process.exit(0);
    } catch (error) {
        console.error('❌ Error listing users:', error.message);
        process.exit(1);
    }
}

listAllUsers();
