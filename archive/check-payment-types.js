const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function checkPaymentTypes() {
    try {
        console.log('Checking paymentType field for all manual offers...\n');

        const snapshot = await db.collection('offers')
            .where('sourceType', '==', 'manual_entry')
            .get();

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            console.log(`\n========== ${data.merchant} (${data.bankName}) ==========`);
            console.log(`paymentType: "${data.paymentType}"`);
            console.log(`paymentType type: ${typeof data.paymentType}`);
            console.log(`paymentType length: ${data.paymentType ? data.paymentType.length : 'N/A'}`);

            // Check if it's empty or has weird characters
            if (!data.paymentType || data.paymentType === '') {
                console.log('⚠️ WARNING: paymentType is EMPTY!');
            }

            // Show all fields
            console.log('\nAll fields:');
            console.log(JSON.stringify(data, null, 2));
        });

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

checkPaymentTypes();
