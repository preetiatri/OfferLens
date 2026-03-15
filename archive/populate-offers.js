const db = admin.firestore();

// Sample offer data
const sampleOffers = [
    // Dining Offers
    {
        bankName: "HDFC",
        paymentType: "Credit Card",
        merchant: "Swiggy",
        discountType: "Percentage",
        discountValue: 50,
        maxDiscountAmount: 100,
        minOrderValue: 199,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2024-12-31')),
        isActive: true,
        description: "Get 50% off up to ₹100 on orders above ₹199",
        sourceUrl: "https://www.swiggy.com",
        dealScore: 95,
        dealBand: "Green",
        successCount: 245,
        failCount: 5,
        category: "Dining",
        couponCode: "HDFC50"
    },
    {
        bankName: "ICICI",
        paymentType: "Debit Card",
        merchant: "Zomato",
        discountType: "Percentage",
        discountValue: 40,
        maxDiscountAmount: 80,
        minOrderValue: 149,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2024-12-31')),
        isActive: true,
        description: "40% off up to ₹80 on Zomato orders",
        sourceUrl: "https://www.zomato.com",
        dealScore: 88,
        dealBand: "Green",
        successCount: 189,
        failCount: 8,
        category: "Dining",
        couponCode: "ICICI40"
    },
    {
        bankName: "SBI",
        paymentType: "Credit Card",
        merchant: "Domino's Pizza",
        discountType: "Flat",
        discountValue: 150,
        minOrderValue: 400,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2024-12-31')),
        isActive: true,
        description: "Flat ₹150 off on orders above ₹400",
        sourceUrl: "https://www.dominos.co.in",
        dealScore: 82,
        dealBand: "Green",
        successCount: 156,
        failCount: 12,
        category: "Dining",
        couponCode: "SBI150"
    },
    {
        bankName: "Axis",
        paymentType: "Credit Card",
        merchant: "MakeMyTrip",
        discountType: "Percentage",
        discountValue: 30,
        maxDiscountAmount: 3000,
        minOrderValue: 10000,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2025-01-15')),
        isActive: true,
        description: "30% off up to ₹3000 on flight bookings",
        sourceUrl: "https://www.makemytrip.com",
        dealScore: 92,
        dealBand: "Green",
        successCount: 312,
        failCount: 15,
        category: "Travel",
        couponCode: "AXIS30"
    },
    {
        bankName: "HDFC",
        paymentType: "Credit Card",
        merchant: "Cleartrip",
        discountType: "Flat",
        discountValue: 2000,
        minOrderValue: 15000,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2025-01-31')),
        isActive: true,
        description: "Flat ₹2000 off on international flights",
        sourceUrl: "https://www.cleartrip.com",
        dealScore: 90,
        dealBand: "Green",
        successCount: 278,
        failCount: 10,
        category: "Travel",
        couponCode: "HDFC2K"
    },
    {
        bankName: "ICICI",
        paymentType: "Credit Card",
        merchant: "Amazon",
        discountType: "Percentage",
        discountValue: 10,
        maxDiscountAmount: 1000,
        minOrderValue: 2000,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2024-12-31')),
        isActive: true,
        description: "10% instant discount up to ₹1000",
        sourceUrl: "https://www.amazon.in",
        dealScore: 78,
        dealBand: "Yellow",
        successCount: 445,
        failCount: 25,
        category: "Shopping",
        couponCode: "ICICI10"
    },
    {
        bankName: "SBI",
        paymentType: "Debit Card",
        merchant: "Flipkart",
        discountType: "Percentage",
        discountValue: 15,
        maxDiscountAmount: 750,
        minOrderValue: 1500,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2024-12-31')),
        isActive: true,
        description: "15% off up to ₹750 on electronics",
        sourceUrl: "https://www.flipkart.com",
        dealScore: 80,
        dealBand: "Green",
        successCount: 367,
        failCount: 22,
        category: "Shopping",
        couponCode: "SBI15"
    },
    {
        bankName: "HDFC",
        paymentType: "Credit Card",
        merchant: "BookMyShow",
        discountType: "Percentage",
        discountValue: 50,
        maxDiscountAmount: 150,
        minOrderValue: 300,
        startDate: admin.firestore.Timestamp.now(),
        endDate: admin.firestore.Timestamp.fromDate(new Date('2024-12-31')),
        isActive: true,
        description: "50% off up to ₹150 on movie tickets",
        sourceUrl: "https://www.bookmyshow.com",
        dealScore: 88,
        dealBand: "Green",
        successCount: 512,
        failCount: 18,
        category: "Entertainment",
        couponCode: "HDFC50BMS"
    }
];

// Function to add offers to Firestore
async function populateOffers() {
    console.log('Starting to populate Firestore with sample offers...');

    const batch = db.batch();

    sampleOffers.forEach((offer, index) => {
        const docRef = db.collection('offers').doc();
        batch.set(docRef, {
            ...offer,
            id: docRef.id,
            createdAt: admin.firestore.Timestamp.now(),
            updatedAt: admin.firestore.Timestamp.now()
        });
        console.log(`Added offer ${index + 1}/${sampleOffers.length}: ${offer.merchant} - ${offer.discountValue}${offer.discountType === 'Percentage' ? '%' : '₹'} off`);
    });

    await batch.commit();
    console.log('\n✅ Successfully added all sample offers to Firestore!');
    console.log(`Total offers added: ${sampleOffers.length}`);
}

// Run the script
populateOffers()
    .then(() => {
        console.log('\n🎉 All done! You can now see these offers in your app.');
        process.exit(0);
    })
    .catch((error) => {
        console.error('Error populating offers:', error);
        process.exit(1);
    });
