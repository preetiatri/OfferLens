# Firebase CLI Installation & Deployment Guide

## ⚠️ Firebase CLI Not Found

The Firebase CLI is not installed on your system. Here's how to install it and deploy:

---

## Option 1: Install Firebase CLI Globally (Recommended)

### Step 1: Install Firebase CLI

```powershell
npm install -g firebase-tools
```

**Expected time:** 1-2 minutes

### Step 2: Login to Firebase

```powershell
firebase login
```

This will open your browser for authentication.

### Step 3: Set Your Project

```powershell
# Navigate to project directory
cd C:\Users\Naveen\Desktop\OfferLens

# List projects
firebase projects:list

# Set your project
firebase use YOUR_PROJECT_ID
```

### Step 4: Deploy

```powershell
firebase deploy --only functions
```

---

## Option 2: Use npx (No Installation Required)

If you don't want to install Firebase CLI globally, use `npx`:

### Step 1: Login

```powershell
npx firebase-tools login
```

### Step 2: Set Project

```powershell
cd C:\Users\Naveen\Desktop\OfferLens
npx firebase-tools use YOUR_PROJECT_ID
```

### Step 3: Deploy

```powershell
npx firebase-tools deploy --only functions
```

---

## Quick Install & Deploy (Copy-Paste)

```powershell
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Navigate to project
cd C:\Users\Naveen\Desktop\OfferLens

# Deploy
firebase deploy --only functions
```

---

## Troubleshooting

### If npm is not recognized:

1. Install Node.js from https://nodejs.org/
2. Restart PowerShell
3. Try again

### If installation is slow:

```powershell
# Use a faster registry
npm config set registry https://registry.npmjs.org/
npm install -g firebase-tools
```

### If you get permission errors:

Run PowerShell as Administrator and try again.

---

## After Installation

Once Firebase CLI is installed, I can help you with the deployment!

Just let me know when you've run:
```powershell
npm install -g firebase-tools
```

Then we'll proceed with deployment! 🚀
