const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function fixDealBandCasing() {
    try {
        console.log('Fixing dealBand casing for manually added offer...\n');

        const snapshot = await db.collection('offers')
            .where('sourceType', '==', 'manual_entry')
            .get();

        if (snapshot.empty) {
            console.log('No manually added offers found!');
            process.exit(0);
            return;
        }

        for (const doc of snapshot.docs) {
            const data = doc.data();
            const currentDealBand = data.dealBand;

            console.log(`Offer: ${data.merchant} (${data.bankName})`);
            console.log(`  Current dealBand: "${currentDealBand}"`);

            // Capitalize first letter to match other offers
            const fixedDealBand = currentDealBand.charAt(0).toUpperCase() + currentDealBand.slice(1).toLowerCase();

            if (fixedDealBand !== currentDealBand) {
                console.log(`  Fixing to: "${fixedDealBand}"`);

                await db.collection('offers').doc(doc.id).update({
                    dealBand: fixedDealBand,
                    updatedAt: admin.firestore.FieldValue.serverTimestamp()
                });

                console.log(`  ✓ Updated successfully\n`);
            } else {
                console.log(`  ✓ Already correct\n`);
            }
        }

        console.log('✓ Done!');
        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

fixDealBandCasing();
