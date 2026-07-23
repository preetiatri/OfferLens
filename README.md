# OfferLens

OfferLens is a smart Android application that helps users discover and manage offers on their credit cards, debit cards, UPI apps, and digital wallets.

## Features
- **Curated Offer Discovery**: Offers are reviewed and entered by our team via the admin portal.
- **Personalized Dashboard**: Shows offers relevant to your specific cards and wallets.
- **Premium UI**: Modern fintech aesthetic with glassmorphism and neon accents.
- **Offline Support**: Caches offers for offline access.

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later.
- Node.js 18+ (for Cloud Functions).
- Firebase CLI.

### Android App
1. Open the project in Android Studio.
2. Add your `google-services.json` to the `app/` directory.
3. Sync Gradle and Run.

### Backend (Cloud Functions)
1. Navigate to `functions/`:
   ```bash
   cd functions
   npm install
   ```
2. Set up your Firebase project:
   ```bash
   firebase use --add
   ```
3. Deploy functions:
   ```bash
   firebase deploy --only functions
   ```

## Architecture
- **MVVM**: Clean architecture with Jetpack Compose.
- **Hilt**: Dependency Injection.
- **Firestore**: Real-time database.
- **Cloud Functions**: Backend logic and scheduling.
