# Hybrid Offer Fetching System - Setup Guide

## Overview

This guide explains how to deploy and use the hybrid offer fetching system that combines:
- **API Integration** - Fetch offers from partner APIs
- **Web Scraping** - Extract offers from bank/merchant websites
- **User Submissions** - Allow users to contribute offers
- **Comprehensive T&C Extraction** - Extract detailed terms & conditions using multiple strategies

---

## Prerequisites

1. **Node.js 18+** installed
2. **Firebase CLI** installed (`npm install -g firebase-tools`)
3. **Firebase project** created
4. **Google Cloud Project** with Vertex AI API enabled
5. **Service account key** for Firebase Admin SDK

---

## Step 1: Install Dependencies

Navigate to the functions directory and install all required packages:

```bash
cd functions
npm install
```

This will install:
- `puppeteer` - Dynamic web scraping
- `puppeteer-extra` & `puppeteer-extra-plugin-stealth` - Anti-bot detection
- `pdf-parse` - PDF T&C extraction
- `tesseract.js` - OCR for image-based T&C
- `@google-cloud/vertexai` - AI-powered offer parsing
- `cheerio` - HTML parsing
- `axios` - HTTP requests

---

## Step 2: Configure Offer Sources

Edit `functions/src/sources-config.json` to add/modify offer sources:

```json
{
  "sources": [
    {
      "id": "your-source-id",
      "type": "scrape",
      "enabled": true,
      "bankName": "HDFC",
      "paymentType": "Credit Card",
      "url": "https://example.com/offers",
      "scrapeType": "dynamic",
      "selectors": {
        "offerContainer": ".offer-card",
        "merchant": ".merchant-name",
        "description": ".offer-desc",
        "terms": ".terms-conditions"
      }
    }
  ]
}
```

**Source Types:**
- `api` - Fetch from REST API
- `scrape` - Web scraping (static or dynamic)
- `user_submission` - User-contributed offers

---

## Step 3: Set Environment Variables

Set your Google Cloud project ID:

```bash
firebase functions:config:set gcloud.project="your-project-id"
```

For API sources with authentication, add API keys:

```bash
firebase functions:config:set api.hdfc_key="your-api-key"
```

---

## Step 4: Build and Deploy

Build the TypeScript code:

```bash
cd functions
npm run build
```

Deploy to Firebase:

```bash
firebase deploy --only functions
```

This deploys:
- `fetchOffersFunction` - Scheduled to run every 24 hours
- `expireOffersFunction` - Scheduled to run every 24 hours
- `submitOffer` - Callable function for user submissions
- `moderateSubmission` - Callable function for admin moderation
- `manualFetchOffers` - HTTP endpoint for manual testing

---

## Step 5: Test the System

### Manual Trigger (for testing)

Trigger offer fetching manually via HTTP:

```bash
curl https://YOUR_REGION-YOUR_PROJECT.cloudfunctions.net/manualFetchOffers
```

### View Logs

Monitor Cloud Functions logs:

```bash
firebase functions:log
```

### Check Firestore

Verify offers are being added to the `offers` collection in Firestore.

---

## How It Works

### 1. Offer Fetching Flow

```
┌─────────────────┐
│ Scheduled Cron  │ (Every 24 hours)
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Load sources-config.json        │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ For each enabled source:        │
│  - API: Fetch from endpoint     │
│  - Scrape: Use Puppeteer/Cheerio│
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Extract T&C using:              │
│  - HTML selectors               │
│  - PDF parsing                  │
│  - OCR (if needed)              │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Parse with Vertex AI:           │
│  - Extract structured data      │
│  - Validate T&C completeness    │
│  - Calculate deal score         │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Deduplicate using hash          │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Batch write to Firestore        │
└─────────────────────────────────┘
```

### 2. User Submission Flow

```
┌─────────────────┐
│ User submits    │
│ offer via app   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│ submitOffer Cloud Function      │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Parse with AI & calculate       │
│ verification score (0-100)      │
└────────┬────────────────────────┘
         │
         ▼
    ┌────┴────┐
    │ Score?  │
    └────┬────┘
         │
    ┌────┴────────────┐
    │                 │
    ▼                 ▼
Score >= 85      Score < 85
    │                 │
    ▼                 ▼
Auto-approve    Pending review
    │                 │
    ▼                 ▼
Add to offers   Admin moderates
```

### 3. T&C Extraction Strategies

The system tries multiple strategies in order:

1. **HTML Selectors** - Extract from CSS selectors
2. **PDF Parsing** - Download and parse PDF documents
3. **OCR** - Extract from images using Tesseract
4. **Full Page** - Fallback to full page text extraction

Each extracted T&C is validated and scored (0-100) based on completeness.

---

## T&C Completeness Scoring

The system checks for these components:

- ✅ Validity period/dates
- ✅ Minimum/maximum order values
- ✅ Exclusions
- ✅ Usage limits
- ✅ Crediting timeline
- ✅ Cancellation policy
- ✅ Geographic restrictions
- ✅ Payment method requirements

**Score = (Components Found / 8) × 100**

---

## Firestore Data Structure

### Offers Collection

```javascript
{
  id: "auto-generated",
  bankName: "HDFC",
  paymentType: "Credit Card",
  merchant: "Amazon",
  discountType: "Percentage",
  discountValue: 10,
  maxDiscountAmount: 1000,
  minOrderValue: 2000,
  description: "10% off on Amazon",
  termsAndConditions: "Complete T&C text...",
  termsCompleteness: 87, // 0-100 score
  dealScore: 78,
  category: "Shopping",
  sourceType: "scrape_dynamic",
  verificationStatus: "verified",
  isActive: true,
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

### Offer Submissions Collection

```javascript
{
  userId: "user-id",
  merchant: "Swiggy",
  description: "50% off",
  status: "pending", // pending, approved, rejected
  verificationScore: 65,
  submittedAt: Timestamp
}
```

---

## Monitoring & Maintenance

### Check Scraping Success Rate

Query Firestore to see offers by source type:

```javascript
db.collection('offers')
  .where('sourceType', '==', 'scrape_dynamic')
  .get()
```

### Review Failed Fetches

Check Cloud Functions logs for errors:

```bash
firebase functions:log --only fetchOffersFunction
```

### Moderate User Submissions

Query pending submissions:

```javascript
db.collection('offer_submissions')
  .where('status', '==', 'pending')
  .get()
```

---

## Cost Optimization

### Reduce Vertex AI Calls

- Cache parsed offers
- Only re-parse if source content changes
- Batch multiple offers in single AI call

### Reduce Puppeteer Usage

- Use static scraping (Cheerio) when possible
- Only use Puppeteer for JavaScript-heavy sites
- Reuse browser instances

### Limit Scraping Frequency

- Adjust schedule from 24h to 48h or weekly
- Only scrape sources with high success rates
- Prioritize API sources over scraping

---

## Troubleshooting

### "Cannot find module" errors

Run `npm install` in the `functions/` directory.

### Puppeteer fails to launch

Add to Cloud Functions runtime options:

```javascript
export const fetchOffersFunction = functions
  .runWith({ memory: '2GB', timeoutSeconds: 540 })
  .pubsub.schedule("every 24 hours")
  .onRun(async (context) => {
    await fetchOffers();
    return null;
  });
```

### T&C extraction returns empty

- Check if selectors in `sources-config.json` are correct
- Verify the website structure hasn't changed
- Try using dynamic scraping instead of static

### Offers not appearing in app

- Verify `isActive` is `true`
- Check if offers match user's bank/payment preferences
- Ensure `endDate` is in the future

---

## Next Steps

1. **Add More Sources** - Edit `sources-config.json`
2. **Customize AI Prompts** - Edit `vertexParser.ts`
3. **Add Monitoring Dashboard** - Create admin panel
4. **Implement User Feedback** - Allow users to report incorrect offers
5. **Add Notifications** - Notify users of new offers matching their preferences

---

## Support

For issues or questions:
1. Check Cloud Functions logs
2. Review Firestore data
3. Test with `manualFetchOffers` endpoint
4. Verify source configurations

---

## License

This system is part of the OfferLens application.
