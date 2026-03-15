# PowerShell script to migrate from Android Log to Timber
# This script replaces all Log.d, Log.e, Log.w, Log.i, Log.v with Timber equivalents

$projectPath = "c:\Users\Naveen\Desktop\OfferLens\app\src\main\java"
$files = Get-ChildItem -Path $projectPath -Filter "*.kt" -Recurse

$replacements = @{
    'android\.util\.Log\.d\(' = 'Timber.d('
    'android\.util\.Log\.e\(' = 'Timber.e('
    'android\.util\.Log\.w\(' = 'Timber.w('
    'android\.util\.Log\.i\(' = 'Timber.i('
    'android\.util\.Log\.v\(' = 'Timber.v('
    'Log\.d\('                = 'Timber.d('
    'Log\.e\('                = 'Timber.e('
    'Log\.w\('                = 'Timber.w('
    'Log\.i\('                = 'Timber.i('
    'Log\.v\('                = 'Timber.v('
}

$importReplacements = @{
    'import android\.util\.Log' = 'import timber.log.Timber'
}

$totalFiles = 0
$totalReplacements = 0

Write-Host "Starting Timber migration..." -ForegroundColor Cyan
Write-Host "Scanning directory: $projectPath" -ForegroundColor Cyan
Write-Host ""

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    $fileModified = $false
    $fileReplacements = 0
    
    # Replace Log calls with Timber calls
    foreach ($pattern in $replacements.Keys) {
        $replacement = $replacements[$pattern]
        $matches = [regex]::Matches($content, $pattern)
        if ($matches.Count -gt 0) {
            $content = $content -replace $pattern, $replacement
            $fileReplacements += $matches.Count
            $fileModified = $true
        }
    }
    
    # Replace imports
    foreach ($pattern in $importReplacements.Keys) {
        $replacement = $importReplacements[$pattern]
        if ($content -match $pattern) {
            $content = $content -replace $pattern, $replacement
            $fileModified = $true
        }
    }
    
    # Save if modified
    if ($fileModified) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        $totalFiles++
        $totalReplacements += $fileReplacements
        
        $relativePath = $file.FullName.Replace($projectPath, "").TrimStart('\')
        Write-Host "✅ $relativePath" -ForegroundColor Green
        Write-Host "   Replaced $fileReplacements Log calls" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Migration Complete!" -ForegroundColor Green
Write-Host "Files modified: $totalFiles" -ForegroundColor Yellow
Write-Host "Total replacements: $totalReplacements" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Build the project to verify no errors" -ForegroundColor White
Write-Host "2. Test the app to ensure logging works" -ForegroundColor White
Write-Host "3. Check logcat for Timber logs" -ForegroundColor White
