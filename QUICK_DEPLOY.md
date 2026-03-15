# Quick Deployment Commands

## ✅ Step 1: Install Dependencies (RUNNING NOW)
Currently installing npm packages in the background...

## Step 2: Build TypeScript
Once npm install completes, run:
```bash
cd C:\Users\Naveen\Desktop\OfferLens\functions
npm run build
```

## Step 3: Deploy to Firebase
```bash
cd C:\Users\Naveen\Desktop\OfferLens
firebase deploy --only functions
```

## Alternative: All-in-One Command
If you want to do everything at once:
```bash
cd C:\Users\Naveen\Desktop\OfferLens\functions && npm install && npm run build && cd .. && firebase deploy --only functions
```

## Check Firebase Login
Before deploying, make sure you're logged in:
```bash
firebase login
firebase projects:list
firebase use YOUR_PROJECT_ID
```

## Monitor Progress
```bash
# View deployment logs
firebase functions:log

# Test the endpoint
curl https://YOUR_REGION-YOUR_PROJECT.cloudfunctions.net/manualFetchOffers
```
