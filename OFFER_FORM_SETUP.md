# OfferLens Offer Entry Form - Setup Guide

## 🎯 What This Is

A beautiful web form that makes it easy to manually add offers to your Firebase Firestore database. Perfect for adding the 50-60 offers you need to launch!

---

## 🚀 Quick Setup (3 Steps)

### **Step 1: Update Firebase Config**

Open `add-offers-form.html` and find this section (around line 268):

```javascript
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "offerlens.firebaseapp.com",
    projectId: "offerlens",
    storageBucket: "offerlens.appspot.com",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
    appId: "YOUR_APP_ID"
};
```

**Replace with your actual Firebase config:**

1. Go to Firebase Console: https://console.firebase.google.com
2. Select your "offerlens" project
3. Click Settings (gear icon) → Project Settings
4. Scroll to "Your apps" → Web app
5. Copy the config values and paste them

---

### **Step 2: Open the Form**

Simply double-click `add-offers-form.html` to open it in your browser!

---

### **Step 3: Start Adding Offers**

Fill in the form and click "Add Offer to Firebase"!

---

## 📋 How to Use

### **Required Fields:**
- Merchant (e.g., Swiggy, Amazon)
- Bank Name (e.g., HDFC, ICICI)
- Payment Type (Credit Card, Debit Card, UPI, etc.)
- Category (Dining, Shopping, Travel, etc.)
- Discount Type (Percentage, Flat, Cashback)
- Discount Value (number)
- Description

### **Optional Fields:**
- Min Order Value
- Max Discount Amount
- Terms & Conditions
- Coupon Code
- Valid Until (date)
- Source URL

---

## 💡 Tips for Fast Entry

### **1. Use the Screenshots You Provided**

For each offer screenshot:
1. Look at the offer details
2. Fill in the form fields
3. Click "Add Offer"
4. Move to next screenshot

### **2. Common Values**

Save time by using common patterns:

**ICICI Bank Offers:**
- Bank Name: `ICICI`
- Payment Type: `Credit Card`

**HDFC Bank Offers:**
- Bank Name: `HDFC`
- Payment Type: `Credit Card`

**Dining Offers:**
- Category: `Dining`
- Merchants: Swiggy, Zomato, EazyDiner

**Electronics Offers:**
- Category: `Electronics`
- Merchants: Samsung, LG, Google Store

---

## 📊 Features

✅ **Real-time Stats** - See total offers and session count  
✅ **Recent Offers** - View last 5 added offers  
✅ **Auto-calculation** - Deal score and T&C completeness  
✅ **Validation** - Required fields marked  
✅ **Success/Error Messages** - Clear feedback  
✅ **Beautiful UI** - Matches OfferLens design  

---

## 🎯 Example: Adding an Offer from Your Screenshot

**From ICICI EazyDiner Screenshot:**

```
Merchant: EazyDiner
Bank Name: ICICI
Payment Type: Credit Card
Category: Dining
Discount Type: Flat
Discount Value: 300
Min Order Value: 4000
Max Discount: 300
Description: Get additional ₹300 off on a minimum order value of ₹4,000 on dining at select restaurants
Terms & Conditions: The offer is applicable on ICICI Bank Credit Cards. Coupon code for ICICI Bank Credit Cards: CULINARYTREATS. This offer is valid only once a month per Card for every customer. Offer validity: Every Friday to Sunday from Jun 1, 2025 to Mar 31, 2026.
Coupon Code: CULINARYTREATS
Valid Until: 2026-03-31
Source URL: https://www.icicibank.com/offers
```

Click "Add Offer to Firebase" - Done! ✅

---

## ⏱️ Time Estimate

- **Per offer:** 2-3 minutes
- **50 offers:** 1.5-2.5 hours
- **With breaks:** 3 hours total

---

## 🔍 Troubleshooting

### **"Firebase is not defined" error:**
- Make sure you have internet connection
- Firebase SDK loads from CDN

### **"Permission denied" error:**
- Check Firebase Security Rules
- Make sure Firestore is accessible

### **Offers not showing in app:**
- Verify offers are in `offers` collection
- Check `isActive` is set to `true`

---

## ✅ After Adding Offers

1. **Verify in Firebase Console:**
   - Go to Firestore
   - Check `offers` collection
   - You should see all your offers

2. **Test in Android App:**
   - Build and run the app
   - Offers should appear in the list

---

## 🎉 You're Ready!

Open `add-offers-form.html` and start adding those 50-60 offers! 🚀

**Need help?** Let me know if you encounter any issues!
