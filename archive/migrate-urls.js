const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js'); // Uses the centralized config loader

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function migrateOffers() {
    console.log('Starting migration: sourceUrl -> merchantUrl...');

    const offersRef = db.collection('offers');
    const snapshot = await offersRef.get();

    if (snapshot.empty) {
        console.log('No offers found.');
        return;
    }

    let batch = db.batch();
    let count = 0;
    let totalUpdated = 0;

    for (const doc of snapshot.docs) {
        const data = doc.data();

        // Check if it needs migration
        if (data.sourceUrl && !data.merchantUrl) {
            const docRef = offersRef.doc(doc.id);

            batch.update(docRef, {
                merchantUrl: data.sourceUrl,
                offerSourceUrl: '', // Initialize new field
                sourceUrl: admin.firestore.FieldValue.delete() // Remove old field
            });

            count++;
            totalUpdated++;

            // Batches allow up to 500 operations
            if (count >= 400) {
                await batch.commit();
                batch = db.batch();
                count = 0;
                console.log(`Committed batch of 400 updates...`);
            }
        }
    }

    if (count > 0) {
        await batch.commit();
    }

    console.log(`Migration Complete. Updated ${totalUpdated} offers.`);
}

migrateOffers().catch(console.error);
