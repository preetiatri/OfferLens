const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function checkCreatedAt() {
    try {
        console.log('Checking offers for "createdAt" field...\n');

        const snapshot = await db.collection('offers').get();
        const total = snapshot.size;
        let withCreatedAt = 0;
        let missingCreatedAt = 0;

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            if (data.createdAt) {
                withCreatedAt++;
            } else {
                missingCreatedAt++;
            }
        });

        console.log(`Total Offers: ${total}`);
        console.log(`✅ With createdAt: ${withCreatedAt}`);
        console.log(`❌ Missing createdAt: ${missingCreatedAt}`);

        if (missingCreatedAt > 0) {
            console.log('\n⚠️ WARNING: Offers missing "createdAt" will NOT appear in the sorted app query!');
        } else {
            console.log('\n✅ All offers have "createdAt". If app is empty, check Firestore Index.');
        }

        // Also try the actual query the app uses
        console.log('\nTesting App Query (orderBy createdAt DESC):');
        try {
            const appQuery = await db.collection('offers')
                .where('isActive', '==', true)
                .orderBy('createdAt', 'desc')
                .limit(5)
                .get();
            console.log(`✅ Query successful! Returned ${appQuery.size} docs.`);
        } catch (e) {
            console.log(`❌ Query failed: ${e.message}`);
            if (e.message.includes('index')) {
                console.log('👉 THIS IS THE CAUSE! Missing Index.');
            }
        }

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

checkCreatedAt();
