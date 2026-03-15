const admin = require('firebase-admin');
const serviceAccount = require('./firebase-config.js');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function checkOffers() {
    try {
        console.log('🔍 Checking Firestore offers collection...\n');

        // Get all offers
        const snapshot = await db.collection('offers').get();

        if (snapshot.empty) {
            console.log('❌ No offers found in Firestore.');
            console.log('');
            console.log('The database is empty. You need to add offers first.');
            console.log('');
            console.log('Options to add offers:');
            console.log('1. Run: node populate-offers.js (adds sample offers)');
            console.log('2. Use the admin HTML form to add offers manually');
            console.log('3. Add directly in Firebase Console');
            console.log('');
            return;
        }

        console.log(`✅ Found ${snapshot.size} offer(s) in Firestore\n`);
        console.log('='.repeat(100));

        let manualCount = 0;
        let autoCount = 0;
        let activeCount = 0;
        let inactiveCount = 0;

        snapshot.docs.forEach((doc, index) => {
            const offer = doc.data();

            console.log(`\n${index + 1}. ${offer.merchant} - ${offer.bankName}`);
            console.log(`   ID: ${doc.id}`);
            console.log(`   Category: ${offer.category}`);
            console.log(`   Discount: ${offer.discountValue}${offer.discountType === 'Percentage' ? '%' : '₹'} off`);
            console.log(`   Payment Type: ${offer.paymentType}`);
            console.log(`   Active: ${offer.isActive ? '✅ YES' : '❌ NO'}`);
            console.log(`   Source: ${offer.sourceType || 'unknown'}`);
            console.log(`   Deal Band: ${offer.dealBand || 'Not set'}`);

            if (offer.couponCode) {
                console.log(`   Coupon Code: ${offer.couponCode}`);
            }

            if (offer.endDate) {
                const endDate = offer.endDate.toDate ? offer.endDate.toDate() : new Date(offer.endDate);
                console.log(`   Valid Until: ${endDate.toLocaleDateString()}`);
            }

            console.log(`   Description: ${offer.description?.substring(0, 80)}${offer.description?.length > 80 ? '...' : ''}`);
            console.log('-'.repeat(100));

            // Count statistics
            if (offer.sourceType === 'manual_entry') manualCount++;
            else autoCount++;

            if (offer.isActive) activeCount++;
            else inactiveCount++;
        });

        console.log('\n');
        console.log('📊 STATISTICS:');
        console.log(`   Total Offers: ${snapshot.size}`);
        console.log(`   Active: ${activeCount}`);
        console.log(`   Inactive: ${inactiveCount}`);
        console.log(`   Manual Entry: ${manualCount}`);
        console.log(`   Auto/Other: ${autoCount}`);
        console.log('');

        console.log('✅ WILL THESE SHOW IN THE APP?');
        console.log(`   Active offers (${activeCount}): ✅ YES - Will show in app`);
        console.log(`   Inactive offers (${inactiveCount}): ❌ NO - Hidden from app`);
        console.log('');

        console.log('📱 APP BEHAVIOR:');
        console.log('   - App shows only ACTIVE offers (isActive: true)');
        console.log('   - Offers are cached locally using Room database');
        console.log('   - First load: Fetches from Firebase');
        console.log('   - Subsequent loads: Shows from cache (works offline)');
        console.log('');

        if (activeCount === 0) {
            console.log('⚠️  WARNING: No active offers! App will show empty state.');
            console.log('   Make sure offers have isActive: true');
        } else {
            console.log(`🎉 SUCCESS: ${activeCount} active offer(s) will display in the app!`);
        }

        process.exit(0);
    } catch (error) {
        console.error('❌ Error checking offers:', error.message);
        process.exit(1);
    }
}

checkOffers();
