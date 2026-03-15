const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function fixMissingDealBand() {
    try {
        console.log('Finding offers with missing dealBand...\n');

        const snapshot = await db.collection('offers')
            .where('isActive', '==', true)
            .get();

        const offersToFix = [];

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            if (!data.dealBand || data.dealBand === '') {
                offersToFix.push({
                    id: doc.id,
                    data: data
                });
            }
        });

        if (offersToFix.length === 0) {
            console.log('✓ No offers found with missing dealBand. All good!');
            process.exit(0);
            return;
        }

        console.log(`Found ${offersToFix.length} offer(s) with missing dealBand:\n`);

        for (const offer of offersToFix) {
            console.log(`Fixing: ${offer.data.merchant} (${offer.data.bankName})`);
            console.log(`  Deal Score: ${offer.data.dealScore || 'N/A'}`);

            // Calculate dealBand based on dealScore
            let dealBand = 'yellow'; // default
            const dealScore = offer.data.dealScore || 70;

            if (dealScore >= 80) {
                dealBand = 'green';
            } else if (dealScore >= 60) {
                dealBand = 'yellow';
            } else {
                dealBand = 'red';
            }

            console.log(`  Setting dealBand to: ${dealBand}`);

            // Update the document
            await db.collection('offers').doc(offer.id).update({
                dealBand: dealBand,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            console.log(`  ✓ Updated successfully\n`);
        }

        console.log(`\n✓ Fixed ${offersToFix.length} offer(s)`);
        console.log('\nRunning verification...\n');

        // Verify the fix
        const verifySnapshot = await db.collection('offers')
            .where('isActive', '==', true)
            .get();

        const stillMissing = verifySnapshot.docs.filter(doc => {
            const data = doc.data();
            return !data.dealBand || data.dealBand === '';
        });

        if (stillMissing.length === 0) {
            console.log('✓ Verification passed! All offers now have dealBand field.');
        } else {
            console.log(`⚠ Warning: ${stillMissing.length} offer(s) still missing dealBand`);
        }

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

fixMissingDealBand();
