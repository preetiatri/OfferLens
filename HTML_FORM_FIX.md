# How to Add Firebase Config to HTML Form

## Step 1: Get Your Firebase Config

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your **OfferLens** project
3. Click the **gear icon** (⚙️) → **Project settings**
4. Scroll down to **"Your apps"** section
5. Click on your **Web app** (or add one if you haven't)
6. Copy the `firebaseConfig` object

It will look like this:
```javascript
const firebaseConfig = {
  apiKey: "AIzaSyDsYzRXx7iFG-Ezd3Au5-VZbcA1PkhHZHU",
  authDomain: "offerlens.firebaseapp.com",
  projectId: "offerlens",
  storageBucket: "offerlens.firebasestorage.app",
  messagingSenderId: "702877897804",
  appId: "1:702877897804:web:3e62dbfe9237f6a67390e9"
};
```

## Step 2: Update the HTML File

1. Open `add-offer-simple.html`
2. Find line 173 (the firebaseConfig section)
3. Replace the placeholder values with your actual config
4. Save the file

## Step 3: Use the Form

1. Open `add-offer-simple.html` in your browser
2. Fill in the offer details
3. Click "Add Offer"
4. Success! ✅

## Step 4: Refresh App to See New Offers

1. Open your OfferLens app
2. Tap the **cyan refresh button** (top-right)
3. Your new offers will appear!

---

## Quick Fix for "Bill Pay & Recharges" Not Showing

The offers ARE in Firebase (I verified 5 offers in that category).

**Solution:**
1. Open the app
2. Tap the **cyan refresh icon** (top-right corner)
3. Wait 2-3 seconds
4. Tap on "Bill Pay & Recharges" category
5. You should see all 5 offers!

**The 5 Bill Pay & Recharges offers in Firebase:**
1. Airtel Thanks - Kotak (₹50 off)
2. Paytm - HDFC (5% cashback)
3. Amazon Pay - HDFC (8% cashback)
4. PhonePe - ICICI (₹30 cashback)
5. Google Pay - SBI (10% cashback)

All are **active** and will show after refresh!
