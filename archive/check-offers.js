const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function checkOffers() {
    try {
        console.log('Fetching all active offers from Firestore...\n');

        const snapshot = await db.collection('offers')
            .where('isActive', '==', true)
            .get();

        console.log(`Total active offers in Firestore: ${snapshot.size}\n`);

        // Group by source type
        const bySource = {};
        const byCategory = {};

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            const source = data.sourceType || 'unknown';
            const category = data.category || 'uncategorized';

            bySource[source] = (bySource[source] || 0) + 1;
            byCategory[category] = (byCategory[category] || 0) + 1;

            console.log(`${doc.id}: ${data.merchant} (${data.bankName}) - ${data.category} - Source: ${source}`);
        });

        console.log('\n--- Summary by Source Type ---');
        Object.entries(bySource).forEach(([source, count]) => {
            console.log(`${source}: ${count} offers`);
        });

        console.log('\n--- Summary by Category ---');
        Object.entries(byCategory).forEach(([category, count]) => {
            console.log(`${category}: ${count} offers`);
        });

        // Check for offers missing dealBand
        console.log('\n--- Checking for missing dealBand ---');
        const missingDealBand = snapshot.docs.filter(doc => {
            const data = doc.data();
            return !data.dealBand || data.dealBand === '';
        });

        if (missingDealBand.length > 0) {
            console.log(`Found ${missingDealBand.length} offers missing dealBand:`);
            missingDealBand.forEach(doc => {
                const data = doc.data();
                console.log(`  - ${data.merchant} (${data.bankName})`);
            });
        } else {
            console.log('All offers have dealBand set.');
        }

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

checkOffers();
