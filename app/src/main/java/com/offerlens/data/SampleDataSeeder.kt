package com.offerlens.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataSeeder @Inject constructor(
    private val offerRepository: OfferRepository
) {
    suspend fun seedData() {
        val sampleOffers = listOf(
            // Groceries Offers
            Offer(
                bankName = "HDFC",
                paymentType = "Credit Card",
                merchant = "BigBasket",
                discountType = "Percentage",
                discountValue = 15.0,
                maxDiscountAmount = 200.0,
                minOrderValue = 1000.0,
                isActive = true,
                description = "Get 15% off on your grocery shopping at BigBasket",
                category = "Groceries",
                couponCode = "HDFC15",
                dealBand = "green",
                dealScore = 85,
                termsAndConditions = "Valid on orders above ₹1000. Maximum discount ₹200.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "ICICI",
                paymentType = "Debit Card",
                merchant = "Zepto",
                discountType = "Flat",
                discountValue = 100.0,
                maxDiscountAmount = 100.0,
                minOrderValue = 500.0,
                isActive = true,
                description = "Flat ₹100 off on Zepto grocery orders",
                category = "Groceries",
                couponCode = "ICICI100",
                dealBand = "green",
                dealScore = 80,
                termsAndConditions = "Valid on orders above ₹500. One time use per user.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "SBI",
                paymentType = "Credit Card",
                merchant = "Blinkit",
                discountType = "Percentage",
                discountValue = 20.0,
                maxDiscountAmount = 150.0,
                minOrderValue = 800.0,
                isActive = true,
                description = "20% off on Blinkit grocery delivery",
                category = "Groceries",
                couponCode = "SBI20",
                dealBand = "green",
                dealScore = 90,
                termsAndConditions = "Valid on orders above ₹800. Maximum discount ₹150.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "Axis",
                paymentType = "Credit Card",
                merchant = "DMart Ready",
                discountType = "Percentage",
                discountValue = 10.0,
                maxDiscountAmount = 100.0,
                minOrderValue = 500.0,
                isActive = true,
                description = "10% off on DMart Ready grocery orders",
                category = "Groceries",
                couponCode = "AXIS10",
                dealBand = "yellow",
                dealScore = 70,
                termsAndConditions = "Valid on orders above ₹500. Maximum discount ₹100.",
                createdAt = com.google.firebase.Timestamp.now()
            ),

            // Bill Pay & Recharges Offers
            Offer(
                bankName = "HDFC",
                paymentType = "Credit Card",
                merchant = "Paytm",
                discountType = "Percentage",
                discountValue = 5.0,
                maxDiscountAmount = 50.0,
                minOrderValue = 500.0,
                isActive = true,
                description = "5% cashback on electricity bill payments via Paytm",
                category = "Bill Pay & Recharges",
                couponCode = "HDFCBILL",
                dealBand = "green",
                dealScore = 75,
                termsAndConditions = "Valid on electricity bill payments above ₹500. Maximum cashback ₹50.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "ICICI",
                paymentType = "UPI",
                merchant = "PhonePe",
                discountType = "Flat",
                discountValue = 30.0,
                maxDiscountAmount = 30.0,
                minOrderValue = 200.0,
                isActive = true,
                description = "Flat ₹30 cashback on mobile recharges",
                category = "Bill Pay & Recharges",
                couponCode = "ICICIPE",
                dealBand = "green",
                dealScore = 85,
                termsAndConditions = "Valid on mobile recharges of ₹200 and above.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "SBI",
                paymentType = "Debit Card",
                merchant = "Google Pay",
                discountType = "Percentage",
                discountValue = 10.0,
                maxDiscountAmount = 100.0,
                minOrderValue = 1000.0,
                isActive = true,
                description = "10% cashback on DTH recharges via Google Pay",
                category = "Bill Pay & Recharges",
                couponCode = "SBIGPAY",
                dealBand = "green",
                dealScore = 80,
                termsAndConditions = "Valid on DTH recharges above ₹1000. Maximum cashback ₹100.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "Axis",
                paymentType = "Credit Card",
                merchant = "CRED",
                discountType = "Percentage",
                discountValue = 2.0,
                maxDiscountAmount = 200.0,
                minOrderValue = 5000.0,
                isActive = true,
                description = "2% cashback on credit card bill payments",
                category = "Bill Pay & Recharges",
                couponCode = "AXISCRED",
                dealBand = "yellow",
                dealScore = 70,
                termsAndConditions = "Valid on credit card bill payments above ₹5000. Maximum cashback ₹200.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "Kotak",
                paymentType = "UPI",
                merchant = "Airtel Thanks",
                discountType = "Flat",
                discountValue = 50.0,
                maxDiscountAmount = 50.0,
                minOrderValue = 300.0,
                isActive = true,
                description = "Flat ₹50 off on broadband bill payments",
                category = "Bill Pay & Recharges",
                couponCode = "KOTAKAIR",
                dealBand = "green",
                dealScore = 82,
                termsAndConditions = "Valid on broadband bill payments of ₹300 and above.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "HDFC",
                paymentType = "Debit Card",
                merchant = "Amazon Pay",
                discountType = "Percentage",
                discountValue = 8.0,
                maxDiscountAmount = 80.0,
                minOrderValue = 500.0,
                isActive = true,
                description = "8% cashback on gas cylinder bookings",
                category = "Bill Pay & Recharges",
                couponCode = "HDFCGAS",
                dealBand = "green",
                dealScore = 88,
                termsAndConditions = "Valid on gas cylinder bookings via Amazon Pay.",
                createdAt = com.google.firebase.Timestamp.now()
            ),

            // Additional Dining Offers
            Offer(
                bankName = "HDFC",
                paymentType = "Credit Card",
                merchant = "Zomato",
                discountType = "Percentage",
                discountValue = 40.0,
                maxDiscountAmount = 150.0,
                minOrderValue = 500.0,
                isActive = true,
                description = "40% off on Zomato food orders",
                category = "Dining",
                couponCode = "HDFCZOM",
                dealBand = "green",
                dealScore = 92,
                termsAndConditions = "Valid on orders above ₹500. Maximum discount ₹150.",
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Offer(
                bankName = "ICICI",
                paymentType = "Credit Card",
                merchant = "Swiggy",
                discountType = "Percentage",
                discountValue = 30.0,
                maxDiscountAmount = 120.0,
                minOrderValue = 400.0,
                isActive = true,
                description = "30% off on Swiggy food delivery",
                category = "Dining",
                couponCode = "ICICISWG",
                dealBand = "green",
                dealScore = 88,
                termsAndConditions = "Valid on orders above ₹400. Maximum discount ₹120.",
                createdAt = com.google.firebase.Timestamp.now()
            )
        )
        
        offerRepository.addOffers(sampleOffers)
    }
}
