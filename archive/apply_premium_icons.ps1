# Apply OfferLens Premium Icons using PowerShell
# Resizes and copies the premium app icon to all required mipmap folders

Add-Type -AssemblyName System.Drawing

# Icon sizes for different densities
$iconSizes = @{
    'mdpi'    = 48
    'hdpi'    = 72
    'xhdpi'   = 96
    'xxhdpi'  = 144
    'xxxhdpi' = 192
}

function Resize-Image {
    param(
        [string]$SourcePath,
        [string]$DestPath,
        [int]$Width,
        [int]$Height
    )
    
    $img = [System.Drawing.Image]::FromFile($SourcePath)
    $resized = New-Object System.Drawing.Bitmap($Width, $Height)
    $graphics = [System.Drawing.Graphics]::FromImage($resized)
    
    # High quality resize
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    
    $graphics.DrawImage($img, 0, 0, $Width, $Height)
    
    # Save as PNG
    $resized.Save($DestPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $graphics.Dispose()
    $resized.Dispose()
    $img.Dispose()
}

Write-Host "OfferLens Premium Icon Application" -ForegroundColor Cyan
Write-Host ("=" * 50) -ForegroundColor Cyan

$sourceIcon = "app\asset release\offerlens_app_icon_production_1767462390324.png"

if (-not (Test-Path $sourceIcon)) {
    Write-Host "❌ Source icon not found: $sourceIcon" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Found premium app icon" -ForegroundColor Green

# Create each density
foreach ($density in $iconSizes.Keys) {
    $size = $iconSizes[$density]
    
    # Create directory if it doesn't exist
    $mipmapDir = "app\src\main\res\mipmap-$density"
    if (-not (Test-Path $mipmapDir)) {
        New-Item -ItemType Directory -Path $mipmapDir -Force | Out-Null
    }
    
    # Resize and save
    $outputPath = Join-Path $mipmapDir "ic_launcher.png"
    Resize-Image -SourcePath $sourceIcon -DestPath $outputPath -Width $size -Height $size
    
    # Also create round version
    $roundPath = Join-Path $mipmapDir "ic_launcher_round.png"
    Copy-Item $outputPath $roundPath -Force
    
    Write-Host "✓ Created $density`: $size`x$size`px" -ForegroundColor Green
}

# Copy to deployment_assets
if (-not (Test-Path "deployment_assets")) {
    New-Item -ItemType Directory -Path "deployment_assets" -Force | Out-Null
}

Copy-Item $sourceIcon "deployment_assets\app_icon_512.png" -Force
Write-Host "✓ Copied app icon to deployment_assets/app_icon_512.png" -ForegroundColor Green

Copy-Item "app\asset release\feature_graphic_offerlens_1767463235055.png" "deployment_assets\feature_graphic.png" -Force
Write-Host "✓ Copied feature graphic to deployment_assets/feature_graphic.png" -ForegroundColor Green

Write-Host "`n✓ All assets ready for deployment!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Build the app to see the new icon"
Write-Host "2. Use deployment_assets/ files for Play Store listing"
