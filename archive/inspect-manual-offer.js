const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function inspectManualOffer() {
    try {
        console.log('Inspecting manually added offer...\n');

        const snapshot = await db.collection('offers')
            .where('sourceType', '==', 'manual_entry')
            .get();

        if (snapshot.empty) {
            console.log('No manually added offers found!');
            process.exit(0);
            return;
        }

        console.log(`Found ${snapshot.size} manually added offer(s):\n`);

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            console.log(`Document ID: ${doc.id}`);
            console.log('Full data structure:');
            console.log(JSON.stringify(data, null, 2));
            console.log('\n--- Field Type Analysis ---');

            // Check each field type
            const fields = [
                'merchant', 'bankName', 'paymentType', 'category',
                'discountType', 'discountValue', 'minOrderValue',
                'maxDiscountAmount', 'isActive', 'dealBand', 'dealScore',
                'description', 'termsAndConditions', 'couponCode'
            ];

            fields.forEach(field => {
                const value = data[field];
                const type = typeof value;
                const exists = value !== undefined && value !== null;
                console.log(`${field}: ${exists ? type : 'MISSING'} = ${JSON.stringify(value)}`);
            });

            console.log('\n--- Potential Issues ---');

            // Check for common issues
            const issues = [];

            if (!data.isActive) {
                issues.push('❌ isActive is false or missing');
            }

            if (!data.dealBand || data.dealBand === '') {
                issues.push('❌ dealBand is missing or empty');
            }

            if (typeof data.discountValue !== 'number') {
                issues.push(`⚠️ discountValue is ${typeof data.discountValue}, should be number`);
            }

            if (typeof data.minOrderValue !== 'number' && data.minOrderValue !== undefined) {
                issues.push(`⚠️ minOrderValue is ${typeof data.minOrderValue}, should be number`);
            }

            if (typeof data.maxDiscountAmount !== 'number' && data.maxDiscountAmount !== undefined) {
                issues.push(`⚠️ maxDiscountAmount is ${typeof data.maxDiscountAmount}, should be number`);
            }

            if (typeof data.dealScore !== 'number' && data.dealScore !== undefined) {
                issues.push(`⚠️ dealScore is ${typeof data.dealScore}, should be number`);
            }

            if (issues.length === 0) {
                console.log('✅ No obvious issues found!');
            } else {
                issues.forEach(issue => console.log(issue));
            }
        });

        // Now check all active offers to compare
        console.log('\n\n--- Comparing with other active offers ---');
        const allActive = await db.collection('offers')
            .where('isActive', '==', true)
            .limit(3)
            .get();

        console.log('\nSample of other active offers for comparison:');
        allActive.docs.forEach((doc, index) => {
            const data = doc.data();
            if (data.sourceType !== 'manual_entry') {
                console.log(`\nOffer ${index + 1}: ${data.merchant} (${data.bankName})`);
                console.log(`  discountValue type: ${typeof data.discountValue}`);
                console.log(`  isActive type: ${typeof data.isActive}`);
                console.log(`  dealBand: "${data.dealBand}"`);
            }
        });

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

inspectManualOffer();
