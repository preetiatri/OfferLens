# User Submission Validation System

## Overview

The OfferLens app uses a **7-layer automated validation system** to verify user-submitted offers are genuine and accurate. Each submission receives a score from 0-100, with automatic approval at 85+ points.

---

## Validation Layers

### 1. AI Parsing Validation (20 points)

**What it checks:**
- Can Vertex AI successfully parse the offer?
- Does parsed data match submitted data?
- Are merchant and bank names consistent?

**How it works:**
```typescript
const offerText = `Merchant: ${merchant}, Bank: ${bank}, Description: ${description}...`;
const parsed = await parseOfferText(offerText);
// Verify parsed.merchant matches submitted merchant
// Verify parsed.bankName matches submitted bank
```

**Scoring:**
- ✅ 20 points: AI parses successfully with matching data
- ⚠️ 5 points: AI parses but finds inconsistencies
- ❌ 0 points: AI cannot parse offer

---

### 2. Source URL Verification (15 points)

**What it checks:**
- Is the URL valid and accessible?
- Does the domain match the merchant?
- Does the page contain offer-related content?

**How it works:**
```typescript
// Verify domain matches merchant
if (merchant === "Swiggy") {
  expect(url).toContain("swiggy.com");
}

// Fetch the URL
const response = await axios.get(url);

// Check for offer keywords
const hasOfferContent = pageText.includes("offer") || 
                        pageText.includes("discount") ||
                        pageText.includes("cashback");
```

**Scoring:**
- ✅ 15 points: Valid URL with matching domain and offer content
- ⚠️ 5 points: Valid URL but domain mismatch or no offer content
- ❌ 0 points: Invalid URL or 404 error

**Supported Merchants:**
- Swiggy → swiggy.com
- Zomato → zomato.com
- Amazon → amazon.in
- Paytm → paytm.com
- PhonePe → phonepe.com
- Google Pay → pay.google.com
- HDFC → hdfcbank.com
- ICICI → icicibank.com
- SBI → sbicard.com

---

### 3. Image Analysis with Vision AI (15 points)

**What it checks:**
- Is the screenshot genuine?
- Does it mention the correct merchant?
- Does it show the correct bank/payment method?
- Does the discount value match?
- Is there a visible coupon code?

**How it works:**
```typescript
// Use Vertex AI Vision API
const prompt = `
Analyze this offer screenshot and verify:
1. Is this genuine?
2. Merchant: ${merchant}?
3. Bank: ${bank}?
4. Discount: ${discountValue}%?
5. Coupon code visible?
`;

const analysis = await visionAI.analyze(imageUrl, prompt);
// Returns: { isGenuine, merchantMatch, bankMatch, discountMatch, confidence }
```

**Scoring:**
- ✅ 15 points: Genuine image with all details matching
- ⚠️ 10 points: Genuine image with partial matches
- ⚠️ 5 points: Image provided but analysis unavailable
- ❌ 0 points: Fake or invalid image

---

### 4. Duplicate Detection (15 points)

**What it checks:**
- Does this offer already exist in the database?
- Is there a similar pending submission?

**How it works:**
```typescript
// Check existing offers
const existing = await db.collection('offers')
  .where('merchant', '==', merchant)
  .where('bankName', '==', bank)
  .where('isActive', '==', true)
  .get();

// Check if discount value is nearly identical
if (Math.abs(existing.discountValue - submitted.discountValue) < 2) {
  return "Duplicate found";
}

// Check pending submissions
const pending = await db.collection('offer_submissions')
  .where('merchant', '==', merchant)
  .where('status', '==', 'pending')
  .get();
```

**Scoring:**
- ✅ 15 points: No duplicates found
- ❌ 0 points: Duplicate offer exists or similar offer pending

---

### 5. User Reputation Score (10 points)

**What it checks:**
- What is the user's submission history?
- What percentage of their submissions were approved?

**How it works:**
```typescript
const userSubmissions = await db.collection('offer_submissions')
  .where('userId', '==', userId)
  .get();

const approved = submissions.filter(s => s.status === 'approved').length;
const rejected = submissions.filter(s => s.status === 'rejected').length;
const approvalRate = approved / (approved + rejected);
```

**Scoring:**
- ✅ 10 points: Trusted user (≥80% approval rate)
- ⚠️ 5 points: Moderate reputation (50-79% approval rate) OR first-time user
- ❌ 0 points: Low reputation (<50% approval rate)

**Reputation Tiers:**
- 🌟 **Trusted User**: 80%+ approval rate → Auto-approve eligible
- 📊 **Moderate User**: 50-79% approval rate → Manual review
- ⚠️ **New User**: First submission → Manual review
- 🚫 **Low Reputation**: <50% approval rate → Strict review

---

### 6. Terms & Conditions Completeness (15 points)

**What it checks:**
- Are T&C provided and detailed?
- Do they contain essential components?

**How it works:**
```typescript
const components = {
  validity: /valid|expire|until|till/i,
  limits: /minimum|maximum|limit/i,
  exclusions: /exclude|not applicable|except/i,
  usage: /per user|per transaction|once/i
};

let found = 0;
for (const regex of Object.values(components)) {
  if (regex.test(termsAndConditions)) found++;
}

const score = (found / 4) * 15;
```

**Required Components:**
1. ✅ Validity period (dates, expiry)
2. ✅ Limits (min/max order value)
3. ✅ Exclusions (what's not covered)
4. ✅ Usage restrictions (per user, per transaction)

**Scoring:**
- ✅ 15 points: All 4 components present
- ⚠️ 10 points: 3 components present
- ⚠️ 5 points: 1-2 components present
- ❌ 0 points: T&C missing or too short (<50 chars)

---

### 7. Cross-Reference with Known Offers (10 points)

**What it checks:**
- Do similar offers exist from official sources?
- Does the submitted offer align with known patterns?

**How it works:**
```typescript
const officialOffers = await db.collection('offers')
  .where('merchant', '==', merchant)
  .where('sourceType', 'in', ['api', 'scrape_dynamic', 'scrape_static'])
  .get();

// Check if discount value is in similar range
for (const offer of officialOffers) {
  if (Math.abs(offer.discountValue - submitted.discountValue) < 10) {
    return "Aligns with known offers";
  }
}
```

**Scoring:**
- ✅ 10 points: Aligns with known official offers
- ⚠️ 5 points: No official offers to compare OR pattern differs (not necessarily invalid)

---

## Scoring System

### Total Score Calculation

```
Total Score = AI Parsing (20)
            + Source Verification (15)
            + Image Analysis (15)
            + Duplicate Check (15)
            + User Reputation (10)
            + T&C Completeness (15)
            + Cross-Reference (10)
            = 100 points maximum
```

### Auto-Approval Criteria

**Automatic Approval** if:
- ✅ Total score ≥ 85 points
- ✅ No critical warnings
- ✅ AI parsing successful

**Manual Review** if:
- ⚠️ Total score 50-84 points
- ⚠️ Warnings present
- ⚠️ First-time user

**Auto-Rejection** if:
- ❌ Total score < 50 points
- ❌ Duplicate detected
- ❌ Fake image detected
- ❌ Source URL invalid

---

## Example Validation Results

### Example 1: High-Quality Submission (Auto-Approved)

```json
{
  "overallScore": 90,
  "checks": {
    "aiParsing": { "passed": true, "score": 20, "details": "AI validated" },
    "sourceVerification": { "passed": true, "score": 15, "details": "URL verified" },
    "imageAnalysis": { "passed": true, "score": 15, "details": "Image genuine" },
    "duplicateCheck": { "passed": true, "score": 15, "details": "No duplicates" },
    "userReputation": { "passed": true, "score": 10, "details": "Trusted user (85%)" },
    "termsCompleteness": { "passed": true, "score": 15, "details": "4/4 components" },
    "crossReference": { "passed": true, "score": 10, "details": "Aligns with known offers" }
  },
  "autoApprove": true,
  "warnings": []
}
```

**Result:** ✅ Automatically approved and added to offers

---

### Example 2: Medium-Quality Submission (Manual Review)

```json
{
  "overallScore": 65,
  "checks": {
    "aiParsing": { "passed": true, "score": 20, "details": "AI validated" },
    "sourceVerification": { "passed": false, "score": 0, "details": "No source URL" },
    "imageAnalysis": { "passed": true, "score": 10, "details": "Image partial match" },
    "duplicateCheck": { "passed": true, "score": 15, "details": "No duplicates" },
    "userReputation": { "passed": true, "score": 5, "details": "New user" },
    "termsCompleteness": { "passed": true, "score": 10, "details": "3/4 components" },
    "crossReference": { "passed": true, "score": 5, "details": "No official offers" }
  },
  "autoApprove": false,
  "warnings": ["Missing source URL"]
}
```

**Result:** ⚠️ Pending manual review by admin

---

### Example 3: Low-Quality Submission (Rejected)

```json
{
  "overallScore": 25,
  "checks": {
    "aiParsing": { "passed": false, "score": 0, "details": "AI parsing failed" },
    "sourceVerification": { "passed": false, "score": 0, "details": "No source URL" },
    "imageAnalysis": { "passed": false, "score": 0, "details": "No proof image" },
    "duplicateCheck": { "passed": false, "score": 0, "details": "Duplicate exists" },
    "userReputation": { "passed": true, "score": 5, "details": "New user" },
    "termsCompleteness": { "passed": false, "score": 0, "details": "T&C too short" },
    "crossReference": { "passed": true, "score": 5, "details": "No comparison" }
  },
  "autoApprove": false,
  "warnings": ["Missing source URL", "Missing proof image", "Duplicate offer"]
}
```

**Result:** ❌ Likely rejected (admin can still manually approve)

---

## User Experience Flow

### Submission Process

```
User fills form:
  - Merchant name
  - Bank/payment method
  - Description
  - Discount details
  - T&C
  - Coupon code
  - Source URL (optional)
  - Screenshot (optional)
    ↓
Submit to Cloud Function
    ↓
Run 7 validation checks
    ↓
Calculate score (0-100)
    ↓
  ┌─────────────┐
  │ Score ≥ 85? │
  └─────────────┘
    ↓         ↓
   YES        NO
    ↓         ↓
Auto-approve  Pending review
    ↓         ↓
Add to       Admin
offers       moderates
    ↓         ↓
User gets    User gets
instant      notification
confirmation when approved
```

### User Notifications

**Auto-Approved:**
> ✅ **Offer Verified!**  
> Your offer has been automatically verified and is now live. Thank you for contributing!  
> Validation Score: 90/100

**Pending Review:**
> ⏳ **Under Review**  
> Your offer is being reviewed by our team. You'll be notified once it's approved.  
> Validation Score: 65/100  
> ⚠️ Missing source URL

**Rejected:**
> ❌ **Submission Declined**  
> Your offer could not be verified. Please ensure all details are accurate and include proof.  
> Validation Score: 25/100  
> Issues: Duplicate offer, Missing proof image

---

## Admin Dashboard

Admins can view pending submissions with validation details:

```
Pending Submissions:

┌─────────────────────────────────────────────────────┐
│ Swiggy - 50% off with HDFC Credit Card             │
│ Submitted by: user123                               │
│ Validation Score: 72/100                            │
│                                                      │
│ ✅ AI Parsing: 20/20                                │
│ ✅ Duplicate Check: 15/15                           │
│ ⚠️ Source Verification: 5/15 (domain mismatch)     │
│ ⚠️ Image Analysis: 10/15 (partial match)           │
│ ✅ User Reputation: 10/10 (trusted user)            │
│ ✅ T&C Completeness: 12/15                          │
│ ⚠️ Cross-Reference: 5/10                            │
│                                                      │
│ Warnings: Source URL domain doesn't match merchant  │
│                                                      │
│ [Approve] [Reject] [View Details]                   │
└─────────────────────────────────────────────────────┘
```

---

## Benefits of This System

### For Users
- ✅ **Fast Approval**: High-quality submissions approved instantly
- ✅ **Clear Feedback**: Know exactly what's missing
- ✅ **Reputation Building**: Earn trust with good submissions
- ✅ **Transparency**: See validation score

### For Admins
- ✅ **Reduced Workload**: 85%+ score submissions auto-approved
- ✅ **Detailed Insights**: See exactly why a submission scored low
- ✅ **Fraud Prevention**: Multiple layers catch fake offers
- ✅ **Quality Control**: Only genuine offers reach users

### For the Platform
- ✅ **High-Quality Data**: Automated validation ensures accuracy
- ✅ **Scalability**: Can handle thousands of submissions
- ✅ **User Engagement**: Gamification through reputation scores
- ✅ **Trust**: Users see verified badges on offers

---

## Future Enhancements

1. **Machine Learning**: Train ML model on approved/rejected submissions
2. **Community Voting**: Let users upvote/downvote offers
3. **Blockchain Verification**: Immutable proof of offer authenticity
4. **Real-Time Scraping**: Auto-verify by scraping merchant website
5. **Reward System**: Give points/badges to top contributors

---

## API Response Format

When a user submits an offer, they receive:

```json
{
  "success": true,
  "submissionId": "abc123",
  "status": "approved",
  "validationScore": 90,
  "message": "Offer verified and approved automatically!",
  "validationDetails": {
    "overallScore": 90,
    "checks": { ... },
    "autoApprove": true,
    "warnings": []
  }
}
```

---

## Summary

The 7-layer validation system ensures that user-submitted offers are:
- ✅ **Genuine** - Verified with AI and image analysis
- ✅ **Accurate** - Cross-referenced with known offers
- ✅ **Complete** - T&C validated for completeness
- ✅ **Unique** - Duplicate detection prevents spam
- ✅ **Trustworthy** - User reputation tracking

**Auto-approval threshold: 85/100 points**

This creates a scalable, automated system that maintains high data quality while encouraging user contributions!
