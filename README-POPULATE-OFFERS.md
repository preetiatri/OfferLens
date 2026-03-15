# Quick Setup Guide

## Step 1: Get Firebase Service Account Key
1. Go to https://console.firebase.google.com/
2. Select your OfferLens project
3. Click ⚙️ → Project settings → Service accounts
4. Click "Generate new private key"
5. Save the downloaded file as `serviceAccountKey.json` in this folder

## Step 2: Install Dependencies
Open PowerShell in this folder and run:
```powershell
npm install firebase-admin
```

## Step 3: Run the Script
```powershell
node populate-offers.js
```

## Step 4: Restart Your App
Close and reopen the OfferLens app to see the new offers!

---

## What This Adds
- 15 sample offers across 5 categories
- Dining: Swiggy, Zomato, Domino's, PhonePe
- Travel: MakeMyTrip, Cleartrip, Goibibo
- Shopping: Amazon, Flipkart, Myntra, Paytm
- Entertainment: BookMyShow, Netflix
- Groceries: BigBasket, Blinkit
