# OfferLens Deployment Guide - Manual Steps

## ⚠️ PowerShell Execution Policy Issue Detected

Your system has script execution disabled. Here's how to fix it and deploy:

---

## Step 1: Enable PowerShell Scripts (One-time setup)

Open PowerShell **as Administrator** and run:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

Then close and reopen PowerShell.

---

## Step 2: Deploy Backend to Firebase

### Option A: Using the deployment script (Recommended)

```powershell
cd C:\Users\Naveen\Desktop\OfferLens
.\deploy.ps1
```

### Option B: Manual deployment (if script fails)

```powershell
# Navigate to functions directory
cd C:\Users\Naveen\Desktop\OfferLens\functions

# Install dependencies
npm install

# Build TypeScript
npm run build

# Go back to root
cd ..

# Login to Firebase (if not already logged in)
firebase login

# Set your project
firebase use YOUR_PROJECT_ID

# Deploy functions
firebase deploy --only functions
```

---

## Step 3: Update Firebase Config in merchant-opt-out.html

1. Open: `C:\Users\Naveen\Desktop\OfferLens\merchant-opt-out.html`
2. Find line ~226 (the firebaseConfig object)
3. Replace with your actual Firebase config:

```javascript
const firebaseConfig = {
    apiKey: "YOUR_ACTUAL_API_KEY",
    authDomain: "your-project-id.firebaseapp.com",
    projectId: "your-project-id",
    storageBucket: "your-project-id.appspot.com",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
    appId: "YOUR_APP_ID"
};
```

**Where to find these values:**
- Go to Firebase Console (https://console.firebase.google.com)
- Select your project
- Click Settings (gear icon) → Project Settings
- Scroll to "Your apps" → Web app
- Copy the config values

---

## Step 4: Build Android App

1. Open Android Studio
2. Open the OfferLens project
3. Click "Sync Project with Gradle Files" (elephant icon)
4. Build → Make Project (Ctrl+F9)
5. Run on emulator or device

**Note:** Navigation to AboutScreen has been added automatically!

---

## Step 5: Add Seed Offers

Go to Firebase Console → Firestore → offers collection

Click "Add Document" and use this template:

```json
{
  "bankName": "HDFC",
  "paymentType": "Credit Card",
  "merchant": "Swiggy",
  "discountType": "Percentage",
  "discountValue": 50,
  "maxDiscountAmount": 100,
  "minOrderValue": 199,
  "description": "50% off up to ₹100 on orders above ₹199",
  "termsAndConditions": "Valid till 31st Dec 2024. One time use per user. Not applicable on alcohol.",
  "category": "Dining",
  "couponCode": "HDFC50",
  "sourceUrl": "https://www.swiggy.com/offers",
  "sourceType": "manual_entry",
  "verificationStatus": "admin_verified",
  "isActive": true,
  "dealScore": 85,
  "termsCompleteness": 75
}
```

**Recommended offers to add (20-30 total):**

### Dining (5-7 offers)
- Swiggy + HDFC/ICICI/SBI
- Zomato + HDFC/ICICI/Axis
- Domino's + Various cards

### Shopping (5-7 offers)
- Amazon + HDFC/ICICI/SBI
- Flipkart + Axis/HDFC
- Myntra + ICICI/SBI

### Travel (3-5 offers)
- MakeMyTrip + HDFC/ICICI
- Goibibo + SBI/Axis

### Groceries (3-5 offers)
- BigBasket + HDFC/ICICI
- Blinkit + Various cards

### Bill Pay (3-5 offers)
- Paytm + UPI offers
- PhonePe + UPI offers
- Google Pay + UPI offers

---

## Step 6: Test User Submission

1. Launch the app
2. Create a test user account
3. Navigate to submit offer screen (when implemented)
4. Submit a test offer with:
   - Merchant: "Test Merchant"
   - Bank: "HDFC"
   - Description: "Test offer"
   - Terms: "Valid till Dec 31. Min order ₹100. Max discount ₹50."

5. Check Firebase Console → offer_submissions
6. Verify validation score appears

---

## Step 7: Monitor Deployment

```bash
# View Cloud Functions logs
firebase functions:log

# Test manual fetch endpoint
curl https://YOUR_REGION-YOUR_PROJECT.cloudfunctions.net/manualFetchOffers
```

---

## ✅ What I've Already Done For You

1. ✅ Created `Navigation.kt` with route to AboutScreen
2. ✅ AboutScreen is now accessible from your app
3. ✅ All backend code is ready
4. ✅ Disclaimers are in place
5. ✅ Merchant opt-out form is ready

---

## 🎯 Your Action Items

**MUST DO NOW:**
1. Enable PowerShell scripts (Step 1)
2. Run deployment (Step 2)
3. Update Firebase config in merchant-opt-out.html (Step 3)

**DO SOON:**
4. Build Android app in Android Studio (Step 4)
5. Add 20-30 seed offers (Step 5)

**DO BEFORE LAUNCH:**
6. Test user submission flow (Step 6)
7. Monitor logs (Step 7)

---

## 🆘 Troubleshooting

**If npm install fails:**
```powershell
# Clear npm cache
npm cache clean --force

# Try again
npm install
```

**If Firebase deploy fails:**
```powershell
# Check you're logged in
firebase login:list

# Re-login if needed
firebase login
```

**If Android build fails:**
- File → Invalidate Caches / Restart
- Clean Project
- Rebuild Project

---

## 📞 Need Help?

If you encounter any issues, let me know which step failed and I'll help you troubleshoot!

**You're almost there! Just need to run the deployment.** 🚀
