const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function inspectAllManualOffers() {
    try {
        console.log('Inspecting ALL manually added offers...\n');

        const snapshot = await db.collection('offers')
            .where('sourceType', '==', 'manual_entry')
            .get();

        console.log(`Found ${snapshot.size} manually added offer(s):\n`);

        snapshot.docs.forEach((doc, index) => {
            const data = doc.data();
            console.log(`\n========== OFFER ${index + 1} ==========`);
            console.log(`Document ID: ${doc.id}`);
            console.log(`Merchant: ${data.merchant}`);
            console.log(`Bank: ${data.bankName}`);
            console.log(`Category: ${data.category}`);
            console.log(`isActive: ${data.isActive} (${typeof data.isActive})`);
            console.log(`dealBand: "${data.dealBand}"`);
            console.log(`dealScore: ${data.dealScore}`);
            console.log(`discountValue: ${data.discountValue} (${typeof data.discountValue})`);
            console.log(`discountType: ${data.discountType}`);

            // Check for issues
            const issues = [];
            if (!data.isActive) issues.push('isActive is false');
            if (!data.dealBand || data.dealBand === '') issues.push('dealBand missing');
            if (!data.merchant || data.merchant === '') issues.push('merchant missing');
            if (!data.bankName || data.bankName === '') issues.push('bankName missing');
            if (!data.category || data.category === '') issues.push('category missing');
            if (data.discountValue === undefined || data.discountValue === null) issues.push('discountValue missing');

            if (issues.length > 0) {
                console.log(`\n⚠️ ISSUES FOUND:`);
                issues.forEach(issue => console.log(`   - ${issue}`));
            } else {
                console.log(`\n✅ No issues found`);
            }
        });

        // Now test if they appear in the app's query
        console.log('\n\n========== TESTING APP QUERY ==========');
        const appQuery = await db.collection('offers')
            .where('isActive', '==', true)
            .limit(100)
            .get();

        console.log(`\nApp query returned ${appQuery.size} offers`);

        const manualOffersInQuery = appQuery.docs.filter(doc =>
            doc.data().sourceType === 'manual_entry'
        );

        console.log(`Manual offers in app query: ${manualOffersInQuery.length}`);

        if (manualOffersInQuery.length > 0) {
            console.log('\n✅ Manual offers ARE included in app query:');
            manualOffersInQuery.forEach(doc => {
                const data = doc.data();
                console.log(`   - ${data.merchant} (${data.bankName})`);
            });
        } else {
            console.log('\n❌ Manual offers NOT found in app query!');
        }

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

inspectAllManualOffers();
