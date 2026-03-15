# Firebase Billing Optimization Guide for OfferLens

To ensure your app runs efficiently and saves on billing costs, follow these strategies. Firebase charges primarily for **Storage**, **Reads/Writes**, and **Network Egress**.

## 1. Optimize Database Storage (Firestore)
**Goal:** Reduce `Stored Data` costs.

*   **Delete Old Data (Implemented):**
    *   Use the new **"Delete Permanently"** button in `manage-offers.html` to remove expired or inactive offers.
    *   *Why?* Marking as "inactive" (`isActive: false`) still keeps the document in storage and costs money. Deleting it removes it entirely.
*   **Limit Indexing:**
    *   By default, Firestore indexes *every* field. This doubles your storage size.
    *   *Action:* Go to Firebase Console -> Firestore -> Indexes -> Exemptions. Exclude fields you don't query by (e.g., long description text, image URLs).
*   **Shorten Field Names:**
    *   Field names like `transactionDescription` take up space in every document. Using `desc` saves bytes per document. (Only recommended for new large datasets).

## 2. Optimize Reads & Writes
**Goal:** Reduce `Document Reads/Writes` costs.

*   **Pagination (Critical):**
    *   Currently, the admin panel loads the last 100 offers (`.limit(100)`). This is good practice. Avoid queries that `get()` all documents.
*   **Client-Side Caching:**
    *   Enable persistence in your Android app:
        ```kotlin
        val settings = firestoreSettings { isPersistenceEnabled = true }
        db.firestoreSettings = settings
        ```
    *   *Result:* The app reads from the local cache first instead of the server, saving read costs.
*   **Avoid Realtime Listeners for Static Data:**
    *   Use `get()` (one-time fetch) instead of `onSnapshot()` (realtime listener) for data that doesn't change often (like user profiles or static configs).
    *   *Note:* The admin panel uses `onSnapshot` which is fine for a few admin users, but avoid it in the main user app for feeds.

## 3. Optimize Cloud Functions
**Goal:** Reduce `Invocation` and `Compute Time`.

*   **Set Min Instances to 0:**
    *   Ensure "Min instances" is 0 so you aren't paying for idle server time.
*   **Memory Allocation:**
    *   If your function is simple (e.g., just updating a database field), lower the memory from 256MB to 128MB in the Google Cloud Console.
*   **Avoid Infinite Loops:**
    *   Be careful with functions that trigger on `onWrite`. If a function writes back to the same document, it can trigger itself again. Use `if (change.after.data() === change.before.data()) return;`.

## 4. Optimize Storage (Images)
**Goal:** Reduce `Cloud Storage` GBs.

*   **Resize Images:**
    *   Don't store raw 4MB photos. Resize them to web-friendly sizes (e.g., 800x600 webp) before uploading.
    *   Use a Firebase Extension like "Resize Images" to automate this.
*   **Lifecycle Policies:**
    *   Set up a Google Cloud Storage Lifecycle rule to automatically delete files in a "temp" folder after 30 days.

## Summary Checklist
- [x] **Add Delete Button**: Done. You can now manually clean up old data.
- [ ] **Review Indexes**: Check Firebase Console for unused indexes.
- [ ] **Client Cache**: Ensure Android app has offline persistence enabled.
- [ ] **Monitor Usage**: Check the Usage tab in Firebase Console weekly.
