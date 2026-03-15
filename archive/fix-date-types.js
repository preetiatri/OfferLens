const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function fixDateTypes() {
    try {
        console.log('🔍 Scanning offers for invalid date types...');
        const snapshot = await db.collection('offers').get();
        let fixedCount = 0;

        const batch = db.batch();

        snapshot.docs.forEach(doc => {
            const data = doc.data();
            let needsUpdate = false;
            const updates = {};

            // Check endDate
            if (data.endDate && typeof data.endDate === 'string') {
                console.log(`🛠️ Fixing endDate for ${data.merchant} (${doc.id})`);
                updates.endDate = admin.firestore.Timestamp.fromDate(new Date(data.endDate));
                needsUpdate = true;
            }

            // Check startDate
            if (data.startDate && typeof data.startDate === 'string') {
                console.log(`🛠️ Fixing startDate for ${data.merchant} (${doc.id})`);
                updates.startDate = admin.firestore.Timestamp.fromDate(new Date(data.startDate));
                needsUpdate = true;
            }

            if (needsUpdate) {
                batch.update(doc.ref, updates);
                fixedCount++;
            }
        });

        if (fixedCount > 0) {
            await batch.commit();
            console.log(`✅ Successfully fixed ${fixedCount} offers with invalid date types.`);
        } else {
            console.log('✅ No invalid date types found. All looks good!');
        }

    } catch (error) {
        console.error('❌ Error fixing dates:', error);
    } finally {
        process.exit();
    }
}

fixDateTypes();
