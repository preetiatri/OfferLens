const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function checkLatestOffer() {
    console.log('Fetching latest offer...');

    const offersRef = db.collection('offers');
    // Assuming createdAt is being set. If not, we might just get any offer.
    const snapshot = await offersRef.orderBy('createdAt', 'desc').limit(1).get();

    if (snapshot.empty) {
        console.log('No offers found in DB.');
        return;
    }

    const doc = snapshot.docs[0];
    console.log('--- Latest Offer Data ---');
    console.log('ID:', doc.id);
    console.log(JSON.stringify(doc.data(), null, 2));
}

checkLatestOffer().catch(console.error);
