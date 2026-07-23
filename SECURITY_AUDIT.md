# SECURITY AUDIT: OfferLens Project

**Date:** 2025-12-19
**Status:** ⚠️ AT RISK - Remediation Required (historical - see root-level status corrections added to LEGAL_COMPLIANCE.md, COMPLIANCE_VERIFICATION.md, and LEGAL_RELIABILITY_SUMMARY.md for what's actually been remediated since)

════════════════════════════════════
1️⃣ FILE-BY-FILE RISK REPORT
════════════════════════════════════

| File Path | Risk Level | Issue | Fix |
| :--- | :--- | :--- | :--- |
| `manage-offers.html` | **HIGH** | **Client-Side Admin Exposure.** Admin logic, UI, and Firebase interactions are exposed in a static HTML file. Any user can analyze the code to find admin endpoints or attempt to bypass client-side checks. Authentication is handled, but the *existence* and *structure* of the admin panel is public. | **Move to Backend/Admin SDK.** Best practice is to host this on a private internal URL or wrap it in a server-side function. For now, **MOVE** to a protected `admin/` directory or add build-time restrictions. Ensure Firestore rules strictly enforce `isAdmin()`. |
| `setup-admin.js` | **MEDIUM** | **Hardcoded Emails.** Contains hardcoded admin email addresses (`preetiatri89@...`). Publishing this leaks personal info and potential targets for social engineering. | **Replace with Env Var.** Update script to read from `process.env.ADMIN_EMAIL` or command line arguments. |
| `create-admin-user.js` | **HIGH** | **Hardcoded Credentials.** Contains hardcoded password `TempPassword123!`. If committed or deployed, this is a major security hole. | **DELETE or .gitignore.** This script should never be part of the production bundle. If needed for dev, use `.env` for the password. |
| `merchant-opt-out.html` | **MEDIUM** | **Missing Backend Validation.** Writes to `opt_out_merchants` collection, but `firestore.rules` has **NO RULE** for this collection. Any write attempt will fail (Default Deny), rendering the feature broken. If opened without checks, it becomes a spam vector. | **Add Security Rule.** Add a focused rule allowing unauthenticated creation with rate limiting or CAPTCHA validation. |
| `functions/src/sources-config.json` | **LOW** | **Placeholder Secrets.** contains `"apiKey": "${API_KEY}"`. Low risk as it's a placeholder, but high risk if replaced with a real key and committed. | **Use Secret Manager.** Access keys via `functions.config()` or Google Cloud Secret Manager at runtime, never inject into JSON files. |
| `app/google-services.json` | **LOW** | **API Key Exposure.** Contains Android API keys. Detailed, but standard for Android apps. | **Restrict Key.** Ensure the key `AIzaSyDw...` is restricted in Google Cloud Console to *only* this Android package SHA-1. |
| `gradle.properties` | **LOW** | **Build Configs.** Standard properties. | **Review.** Ensure no signing keys are added here in the future. |

════════════════════════════════════
2️⃣ SECRETS & KEYS EXPOSURE REPORT
════════════════════════════════════

*   **Firebase API Key (`AIzaSyDs...`)**: Found in `manage-offers.html`.
    *   **Status**: EXPOSED (Client-side).
    *   **Action**: Restrict this key in Google Cloud Console to allow *only* `offerlens.firebaseapp.com` and `localhost` (for dev).
*   **Android API Key (`AIzaSyDw...`)**: Found in `app/google-services.json`.
    *   **Status**: BUNDLED (APK).
    *   **Action**: Restrict key to Android App usage only (SHA-1 fingerprint lock).
*   **Service Account Key**:
    *   **Status**: **SECURE**. `firebase-config.js` correctly loads the key from `os.homedir()`, keeping it out of the repo.
    *   **Action**: None. Maintain this practice.

════════════════════════════════════
3️⃣ BILLING & ABUSE PREVENTION
════════════════════════════════════

*   **Risk Vector**: `merchant-opt-out.html` (once enabled).
    *   **Issue**: Public write access to Firestore. An attacker could flood the `opt_out_merchants` collection, driving up write costs.
    *   **Fix**: Implement **App Check** on the web client or a Cloud Function strict rate limit.
*   **Risk Vector**: `functions/src/offerFetcher.ts` (Dynamic scraping).
    *   **Issue**: If triggered frequently by users or loop, could incur compute costs.
    *   **Fix**: Ensure `offerFetcher` is triggered by **Cron Only** (Pub/Sub) and not by direct HTTP User request unless authenticated/admin.

════════════════════════════════════
4️⃣ ADMIN AUTHORITY & ACCESS CONTROL
════════════════════════════════════

*   **Logic**: `manage-offers.html` uses `firebase.auth()` client SDK.
*   **Enforcement**: `firestore.rules` checks `request.auth.token.admin == true`.
*   **Status**: **ROBUST**. The actual data security relies on Firestore Rules + Custom Claims, which is the correct way to secure client-side admin panels.
*   **Weakness**: The *Login UI* is public. Brute force attacks on the admin email are possible via standard Firebase Auth endpoints.
*   **Fix**: None critical, but strong passwords/MFA for admin accounts are mandatory.

════════════════════════════════════
5️⃣ IMMEDIATE ACTIONS BEFORE RELEASE
════════════════════════════════════

1.  [ ] **DELETE** `create-admin-user.js` or add `PROMPT` for password.
2.  [ ] **UPDATE** `firestore.rules` to include `match /opt_out_merchants/{id} { allow create: if true; }` (Temporary) or better, implement a backend function for opt-out to avoid opening Firestore.
3.  [ ] **RESTRICT** Google Cloud API Keys (Browser & Android).
4.  [ ] **MOVE** `manage-offers.html` to a local-only folder or put behind a basic auth middleware if hosted on a server (e.g., Firebase Hosting rewrites).

════════════════════════════════════
6️⃣ SECURE ARCHITECTURE SUMMARY
════════════════════════════════════

*   **Database**: Firestore (Rules-protected).
*   **Auth**: Firebase Auth (Custom Claims for Admin).
*   **Backend**: Firebase Cloud Functions (Node.js).
*   **Client**: Android (Kotlin) + Web Admin (HTML/JS).
*   **Build**: Android R8 Enabled (Obfuscation Active).

**VERDICT: ⚠️ CONDITIONALLY SAFE**
The core architecture is secure, but **admin scripts** and **missing rules** for the new opt-out feature prevent a "Go" status.

**Audit Complete.**
