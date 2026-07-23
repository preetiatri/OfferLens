# Web Scraping Legal Compliance & Best Practices

> **⚠️ STATUS CORRECTION (2026-07-23):** This document analyzes the legal framework for a
> scraping strategy that is **not currently deployed** in this app. The current Cloud Functions
> backend (`functions/src/index.ts`) contains no scraping logic; offer data is entered manually
> by an admin. This is a historical design/legal reference for if automated scraping is
> reintroduced in the future — it does not describe current app behavior.

## ⚖️ Legal Considerations

### Is Web Scraping Legal in India?

**Short Answer:** Web scraping is **generally legal** in India, but with important conditions and limitations.

### Legal Framework

1. **No Specific Anti-Scraping Law**: India does not have specific legislation prohibiting web scraping
2. **Information Technology Act, 2000**: Governs cyber activities but doesn't explicitly ban scraping
3. **Copyright Act, 1957**: Protects original content - you cannot republish copyrighted content
4. **Contract Law**: Must respect Terms of Service (ToS) agreements

### What's Legal ✅

- ✅ Scraping **publicly available** information (no login required)
- ✅ Extracting **factual data** (offer details, prices, dates)
- ✅ Using data for **personal use** or **aggregation**
- ✅ Respecting `robots.txt` directives
- ✅ Reasonable request rates (not DDoS-like behavior)
- ✅ Proper attribution and linking to original sources

### What's Illegal ❌

- ❌ Bypassing **authentication** or paywalls
- ❌ Scraping **personal data** without consent (GDPR/DPDP Act)
- ❌ Republishing **copyrighted content** verbatim
- ❌ Violating **Terms of Service** that explicitly prohibit scraping
- ❌ **DDoS-like behavior** (overwhelming servers)
- ❌ Using scraped data for **fraud** or **impersonation**

---

## 🛡️ OfferLens Compliance Strategy

### 1. Publicly Available Data Only

**What we scrape:**
- ✅ Offer titles and descriptions
- ✅ Discount percentages/amounts
- ✅ Validity dates
- ✅ Terms & conditions
- ✅ Merchant names
- ✅ Coupon codes

**What we DON'T scrape:**
- ❌ User accounts or login data
- ❌ Personal information
- ❌ Payment details
- ❌ Private/authenticated content

### 2. Respect robots.txt

**Implementation:**
```typescript
import axios from 'axios';

async function checkRobotsTxt(url: string): Promise<boolean> {
  try {
    const domain = new URL(url).origin;
    const robotsUrl = `${domain}/robots.txt`;
    
    const response = await axios.get(robotsUrl);
    const robotsTxt = response.data;
    
    // Check if our user agent is disallowed
    const userAgent = 'OfferLens-Bot/1.0';
    const disallowed = robotsTxt.includes(`User-agent: ${userAgent}`) &&
                       robotsTxt.includes('Disallow: /');
    
    return !disallowed;
  } catch (error) {
    // If robots.txt doesn't exist, proceed cautiously
    return true;
  }
}
```

### 3. Rate Limiting & Politeness

**Current implementation:**
```json
{
  "globalSettings": {
    "fetchInterval": "24h",
    "retryAttempts": 3,
    "retryDelay": 5000,
    "timeout": 30000,
    "requestsPerMinute": 10
  }
}
```

**Best practices:**
- ⏱️ Fetch once per 24 hours (not continuous)
- 🐌 10 requests per minute maximum
- ⏸️ 5-second delay between retries
- 🎯 Target specific offer pages, not entire sites

### 4. Attribution & Source Links

**Always provide:**
- 🔗 Link to original offer page (`sourceUrl`)
- 📝 Merchant attribution
- 🏷️ "View on [Merchant] website" button
- ⚠️ Disclaimer: "Please verify on merchant website"

### 5. Fair Use & Transformation

**How we transform data:**
- 📊 Aggregate offers from multiple sources
- 🎯 Categorize and score offers
- 🔍 Add search and filtering
- 📱 Mobile-optimized presentation
- 🔔 Personalized recommendations

This constitutes **fair use** as we're adding significant value, not just republishing.

---

## 🚨 Risk Mitigation Strategies

### High-Risk Approach ❌ (Avoid)

```typescript
// DON'T DO THIS
- Scrape every hour
- Ignore robots.txt
- Bypass CAPTCHAs
- Scrape user-generated content
- Remove attribution
- Copy entire pages
```

### Low-Risk Approach ✅ (Recommended)

```typescript
// DO THIS
✅ Use official APIs when available
✅ Scrape once per day
✅ Respect robots.txt
✅ Rate limit requests
✅ Provide attribution
✅ Extract only factual data
✅ Add transformation/value
```

---

## 📋 Recommended Implementation

### Priority 1: Official APIs (Safest)

**Approach:**
1. Contact banks/merchants for API partnerships
2. Use official offer feeds
3. Sign data sharing agreements

**Examples:**
- Paytm Offers API
- PhonePe Partner API
- Bank offer RSS feeds

**Legal Status:** ✅ Completely legal (contractual agreement)

### Priority 2: Public RSS/JSON Feeds

**Approach:**
1. Check for public offer feeds
2. Use structured data (JSON-LD, schema.org)
3. Subscribe to official channels

**Legal Status:** ✅ Legal (publicly provided data)

### Priority 3: Ethical Web Scraping

**Approach:**
1. Check `robots.txt`
2. Scrape only public offer pages
3. Rate limit to 1 request per 6 seconds
4. Provide attribution
5. Cache aggressively (24h+)

**Legal Status:** ⚠️ Generally legal with precautions

### Priority 4: User Contributions

**Approach:**
1. Let users submit offers
2. Verify with automated checks
3. Crowdsource data collection

**Legal Status:** ✅ Completely legal (user-generated content)

---

## 🔒 Data Protection Compliance

### India's Digital Personal Data Protection Act (DPDP), 2023

**Requirements:**
- ✅ Only collect **necessary** data
- ✅ Obtain **consent** for personal data
- ✅ Provide **opt-out** mechanisms
- ✅ Secure data storage
- ✅ Data deletion on request

**OfferLens Compliance:**
- ✅ No personal data scraped (only public offers)
- ✅ User submissions require consent
- ✅ Users can delete submissions
- ✅ Firebase security rules enforced
- ✅ No data sold to third parties

---

## 📝 Terms of Service Considerations

### Common ToS Restrictions

Many websites have ToS that state:
> "You may not use automated tools to access our website"

**Legal Reality:**
- 📖 ToS are **contracts**, not laws
- ⚖️ Enforceability varies by jurisdiction
- 🌐 Public data scraping often permitted despite ToS
- 🇮🇳 Indian courts have not strongly enforced anti-scraping ToS

**Notable Case:** *LinkedIn vs. hiQ Labs (US)*
- Court ruled scraping **public data** is legal
- ToS cannot override public access rights
- Applies to data visible without login

### OfferLens Approach

**Conservative Strategy:**
1. ✅ Prioritize API partnerships
2. ✅ Scrape only if ToS doesn't explicitly prohibit
3. ✅ Use user submissions as primary source
4. ✅ Provide opt-out for merchants

---

## 🛠️ Technical Safeguards

### 1. Robots.txt Checker

```typescript
// Add to offerFetcher.ts
async function shouldScrape(url: string): Promise<boolean> {
  const allowed = await checkRobotsTxt(url);
  if (!allowed) {
    console.log(`Scraping disallowed by robots.txt: ${url}`);
    return false;
  }
  return true;
}
```

### 2. Rate Limiter

```typescript
class RateLimiter {
  private lastRequest: number = 0;
  private minDelay: number = 6000; // 6 seconds

  async throttle() {
    const now = Date.now();
    const timeSinceLastRequest = now - this.lastRequest;
    
    if (timeSinceLastRequest < this.minDelay) {
      const delay = this.minDelay - timeSinceLastRequest;
      await new Promise(resolve => setTimeout(resolve, delay));
    }
    
    this.lastRequest = Date.now();
  }
}
```

### 3. Error Handling

```typescript
async function scrapeSafely(url: string) {
  try {
    // Check robots.txt
    if (!await shouldScrape(url)) {
      return null;
    }
    
    // Rate limit
    await rateLimiter.throttle();
    
    // Scrape with timeout
    const response = await axios.get(url, { timeout: 30000 });
    return response.data;
    
  } catch (error) {
    if (error.response?.status === 403) {
      console.log('Access forbidden - removing source');
      // Disable this source
      await disableSource(url);
    }
    return null;
  }
}
```

### 4. Merchant Opt-Out

```typescript
// Allow merchants to request removal
export const merchantOptOut = functions.https.onRequest(async (req, res) => {
  const { merchant, email, reason } = req.body;
  
  // Verify merchant ownership (email verification)
  // Disable scraping for this merchant
  await db.collection('opt_out_merchants').add({
    merchant,
    email,
    reason,
    timestamp: admin.firestore.FieldValue.serverTimestamp()
  });
  
  res.json({ success: true, message: 'Merchant opted out' });
});
```

---

## ✅ Recommended Configuration

### Update sources-config.json

```json
{
  "sources": [
    {
      "id": "hdfc-api",
      "type": "api",
      "priority": 1,
      "enabled": true,
      "legal": "API partnership",
      "apiEndpoint": "https://api.hdfcbank.com/offers"
    },
    {
      "id": "paytm-scrape",
      "type": "scrape",
      "priority": 2,
      "enabled": true,
      "legal": "Public data, robots.txt compliant",
      "url": "https://paytm.com/offers",
      "respectRobotsTxt": true,
      "rateLimit": {
        "requestsPerMinute": 10,
        "delayBetweenRequests": 6000
      }
    },
    {
      "id": "user-submissions",
      "type": "user_submission",
      "priority": 3,
      "enabled": true,
      "legal": "User-generated content with consent"
    }
  ]
}
```

---

## 📊 Legal Risk Assessment

| Approach | Legal Risk | Reliability | Scalability |
|----------|-----------|-------------|-------------|
| **Official APIs** | 🟢 None | 🟢 High | 🟢 High |
| **Public RSS Feeds** | 🟢 Very Low | 🟢 High | 🟡 Medium |
| **Ethical Scraping** | 🟡 Low-Medium | 🟡 Medium | 🟡 Medium |
| **Aggressive Scraping** | 🔴 High | 🔴 Low | 🔴 Low |
| **User Submissions** | 🟢 None | 🟡 Medium | 🟢 High |

---

## 🎯 Final Recommendations

### For Production Launch

1. **Phase 1: Safe Start**
   - ✅ Use only official APIs
   - ✅ Enable user submissions
   - ✅ Manual data entry for key merchants

2. **Phase 2: Expand Carefully**
   - ✅ Add scraping for merchants with permissive ToS
   - ✅ Check robots.txt for all sources
   - ✅ Implement rate limiting
   - ✅ Provide merchant opt-out

3. **Phase 3: Partnerships**
   - ✅ Contact banks for API access
   - ✅ Partner with offer aggregators
   - ✅ Sign data sharing agreements

### Legal Disclaimer (Add to App)

```
DISCLAIMER:
OfferLens aggregates publicly available offer information from various 
sources. We make reasonable efforts to ensure accuracy, but offers may 
change without notice. Please verify all offers on the merchant's 
official website before making purchases. OfferLens is not responsible 
for offer validity, terms, or merchant actions.

By using this app, you agree that offer information is provided "as is" 
for informational purposes only.
```

---

## 📞 When to Seek Legal Advice

Consult a lawyer if:
- ❗ You receive cease-and-desist letters
- ❗ Planning to scrape sites with explicit ToS prohibitions
- ❗ Handling sensitive financial data
- ❗ Expanding to international markets
- ❗ Monetizing scraped data directly

---

## 🌟 Best Practice Summary

**DO:**
- ✅ Prioritize APIs and partnerships
- ✅ Respect robots.txt
- ✅ Rate limit aggressively
- ✅ Provide attribution
- ✅ Add value through aggregation
- ✅ Allow merchant opt-out
- ✅ Include disclaimers
- ✅ Focus on user submissions

**DON'T:**
- ❌ Scrape personal data
- ❌ Bypass authentication
- ❌ Ignore robots.txt
- ❌ Overwhelm servers
- ❌ Republish verbatim
- ❌ Remove attribution
- ❌ Scrape continuously

---

## 📚 Legal References

1. **Information Technology Act, 2000** (India)
2. **Digital Personal Data Protection Act, 2023** (India)
3. **Copyright Act, 1957** (India)
4. **LinkedIn Corp. v. hiQ Labs, Inc.** (US precedent)
5. **Computer Fraud and Abuse Act** (US - for reference)

---

## Conclusion

**Web scraping for OfferLens is legal** when done ethically:
- ✅ Scrape only **public offer data**
- ✅ Respect **robots.txt** and rate limits
- ✅ Provide **attribution** and source links
- ✅ Prioritize **APIs** and **user submissions**
- ✅ Add **transformative value**
- ✅ Include **disclaimers**

**Recommended approach:** Hybrid model with API partnerships as primary source, ethical scraping as fallback, and user submissions for niche offers.

This minimizes legal risk while maximizing data quality and reliability! 🚀
