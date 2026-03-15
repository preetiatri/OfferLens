const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

// Initialize Firebase Admin
if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
}

const db = admin.firestore();
const auth = admin.auth();

// Get target UID from command line
const targetUid = process.argv[2];

if (!targetUid) {
    console.error('❌ Usage: node grant-premium.js <USER_UID> [true|false]');
    console.error('   Hint: Get the UID from the user\'s app "Profile" icon.');
    process.exit(1);
}

const setStatus = process.argv[3] !== 'false'; // Default to true

async function grantPremium() {
    try {
        console.log(`🔍 Looking up user ID: ${targetUid}...`);

        let userRecord;
        try {
            userRecord = await auth.getUser(targetUid);
            console.log(`✅ Found user: ${userRecord.uid}`);
            console.log(`   Created: ${userRecord.metadata.creationTime}`);
        } catch (e) {
            console.log(`⚠️  Auth record not found for this ID. Proceeding to check Firestore directly...`);
            // It's possible Auth user is missing but Firestore doc exists if using custom auth or synced data? 
            // Unlikely for this app, but let's be robust.
        }

        const userRef = db.collection('users').document(targetUid);

        // Check current status
        const doc = await userRef.get();
        if (doc.exists) {
            const data = doc.data();
            console.log(`   Current Premium Status: ${data.isPremium || false}`);
        } else {
            console.log('   (User document does not exist yet. Creating it...)');
        }

        // Update (merge)
        console.log(`🛠️  Setting isPremium = ${setStatus}...`);
        await userRef.set({
            isPremium: setStatus,
            premiumGrantedAt: admin.firestore.FieldValue.serverTimestamp(),
            premiumGrantedBy: 'admin_script'
        }, { merge: true });

        console.log('✨ Success! Premium status updated.');
        console.log('   The user needs to restart the app or wait for sync to see changes.');

    } catch (error) {
        console.error('❌ Error:', error.message);
    }
}

grantPremium();
