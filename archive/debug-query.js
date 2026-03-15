const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function testAppQuery() {
    console.log('Testing App Query: collection("offers").whereEqualTo("isActive", true).limit(20)...');

    try {
        const snapshot = await db.collection('offers')
            .where('isActive', '==', true)
            // .orderBy('createdAt', 'desc') // The app commented this out, so I will too.
            .limit(20)
            .get();

        console.log(`Query Successful. Found ${snapshot.size} documents.`);

        if (!snapshot.empty) {
            snapshot.docs.forEach(doc => {
                const data = doc.data();
                console.log(`- [${doc.id}] ${data.merchant} (${data.discountValue} ${data.discountType})`);
            });
        }

    } catch (error) {
        console.error('QUERY FAILED:', error);
    }
}

testAppQuery().catch(console.error);
