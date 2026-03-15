const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function testOfferQuery() {
    try {
        console.log('Testing the exact query the Android app uses...\n');

        // This is the exact query from OfferRepository.kt
        const snapshot = await db.collection('offers')
            .where('isActive', '==', true)
            .limit(100)
            .get();

        console.log(`Total documents returned: ${snapshot.size}\n`);

        // Check if manual offer is in the results
        let manualOfferFound = false;
        let manualOfferData = null;

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            if (data.sourceType === 'manual_entry') {
                manualOfferFound = true;
                manualOfferData = data;
                console.log('✅ MANUAL OFFER FOUND IN QUERY RESULTS!');
                console.log(`   ID: ${doc.id}`);
                console.log(`   Merchant: ${data.merchant}`);
                console.log(`   Bank: ${data.bankName}`);
                console.log(`   isActive: ${data.isActive}`);
                console.log(`   dealBand: "${data.dealBand}"`);
                console.log(`   category: "${data.category}"`);
            }
        });

        if (!manualOfferFound) {
            console.log('❌ MANUAL OFFER NOT FOUND IN QUERY RESULTS!');
            console.log('\nLet me check if it exists at all...');

            const manualCheck = await db.collection('offers')
                .where('sourceType', '==', 'manual_entry')
                .get();

            if (manualCheck.empty) {
                console.log('❌ No manual offers exist in database!');
            } else {
                console.log(`✅ Found ${manualCheck.size} manual offer(s) in database`);
                manualCheck.docs.forEach(doc => {
                    const data = doc.data();
                    console.log(`\n   Merchant: ${data.merchant}`);
                    console.log(`   isActive value: ${data.isActive}`);
                    console.log(`   isActive type: ${typeof data.isActive}`);

                    if (data.isActive !== true) {
                        console.log('   ⚠️ PROBLEM: isActive is not exactly true!');
                    }
                });
            }
        }

        // Also check for any offers with empty or missing required fields
        console.log('\n\n--- Checking for offers with potential issues ---');
        let issueCount = 0;

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            const issues = [];

            if (!data.merchant || data.merchant === '') issues.push('missing merchant');
            if (!data.bankName || data.bankName === '') issues.push('missing bankName');
            if (!data.category || data.category === '') issues.push('missing category');
            if (!data.dealBand || data.dealBand === '') issues.push('missing dealBand');
            if (data.discountValue === undefined || data.discountValue === null) issues.push('missing discountValue');

            if (issues.length > 0) {
                issueCount++;
                console.log(`\n${doc.id}: ${data.merchant || 'NO MERCHANT'}`);
                console.log(`  Issues: ${issues.join(', ')}`);
            }
        });

        if (issueCount === 0) {
            console.log('✅ No offers with missing required fields');
        } else {
            console.log(`\n⚠️ Found ${issueCount} offer(s) with missing required fields`);
        }

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

testOfferQuery();
