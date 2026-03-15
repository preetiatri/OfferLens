# OfferLens Phase 1 Deployment Script
# Run this script to deploy the backend to Firebase

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  OfferLens Phase 1 Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if in correct directory
if (!(Test-Path "functions")) {
    Write-Host "ERROR: functions directory not found!" -ForegroundColor Red
    Write-Host "Please run this script from the OfferLens root directory" -ForegroundColor Yellow
    exit 1
}

Write-Host "[1/6] Checking Firebase CLI..." -ForegroundColor Yellow
if (!(Get-Command firebase -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Firebase CLI not installed!" -ForegroundColor Red
    Write-Host "Install it with: npm install -g firebase-tools" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ Firebase CLI found" -ForegroundColor Green
Write-Host ""

# Step 2: Install dependencies
Write-Host "[2/6] Installing dependencies..." -ForegroundColor Yellow
Set-Location functions
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: npm install failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Dependencies installed" -ForegroundColor Green
Write-Host ""

# Step 3: Build TypeScript
Write-Host "[3/6] Building TypeScript..." -ForegroundColor Yellow
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build successful" -ForegroundColor Green
Write-Host ""

# Step 4: Check Firebase login
Set-Location ..
Write-Host "[4/6] Checking Firebase authentication..." -ForegroundColor Yellow
firebase login:list
if ($LASTEXITCODE -ne 0) {
    Write-Host "Please login to Firebase..." -ForegroundColor Yellow
    firebase login
}
Write-Host "✓ Authenticated" -ForegroundColor Green
Write-Host ""

# Step 5: Set Firebase project
Write-Host "[5/6] Setting Firebase project..." -ForegroundColor Yellow
Write-Host "Available projects:" -ForegroundColor Cyan
firebase projects:list

Write-Host ""
$projectId = Read-Host "Enter your Firebase project ID"
firebase use $projectId

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to set project!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Project set to: $projectId" -ForegroundColor Green
Write-Host ""

# Step 6: Deploy functions
Write-Host "[6/6] Deploying Cloud Functions..." -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Cyan
firebase deploy --only functions

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Deployment failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✓ DEPLOYMENT SUCCESSFUL!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Deployed Functions:" -ForegroundColor Cyan
Write-Host "  • submitOffer (callable)" -ForegroundColor White
Write-Host "  • moderateSubmission (callable)" -ForegroundColor White
Write-Host "  • fetchOffersFunction (scheduled)" -ForegroundColor White
Write-Host "  • expireOffersFunction (scheduled)" -ForegroundColor White
Write-Host "  • manualFetchOffers (HTTP)" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Test user submission via Android app" -ForegroundColor White
Write-Host "  2. Manually add 20-30 seed offers" -ForegroundColor White
Write-Host "  3. Monitor logs: firebase functions:log" -ForegroundColor White
Write-Host "  4. Launch to beta users!" -ForegroundColor White
Write-Host ""
Write-Host "View logs: firebase functions:log" -ForegroundColor Cyan
Write-Host "Test endpoint: https://YOUR_REGION-$projectId.cloudfunctions.net/manualFetchOffers" -ForegroundColor Cyan
Write-Host ""
