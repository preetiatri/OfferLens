# Resize and Apply OfferLens Premium Icons
# This script properly resizes the premium icon to all required densities

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
        [int]$Size
    )
    
    $img = [System.Drawing.Image]::FromFile((Resolve-Path $SourcePath))
    $resized = New-Object System.Drawing.Bitmap($Size, $Size)
    $graphics = [System.Drawing.Graphics]::FromImage($resized)
    
    # High quality resize
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    
    $graphics.DrawImage($img, 0, 0, $Size, $Size)
    
    # Ensure directory exists
    $destDir = Split-Path $DestPath -Parent
    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }
    
    # Save as PNG
    $resized.Save($DestPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $graphics.Dispose()
    $resized.Dispose()
    $img.Dispose()
}

Write-Host "OfferLens Premium Icon Resizer" -ForegroundColor Cyan
Write-Host ("=" * 50) -ForegroundColor Cyan

$sourceIcon = "app\asset release\offerlens_app_icon_production_1767462390324.png"

if (-not (Test-Path $sourceIcon)) {
    Write-Host "ERROR: Source icon not found: $sourceIcon" -ForegroundColor Red
    exit 1
}

Write-Host "Found premium app icon" -ForegroundColor Green

# Create each density
foreach ($density in $iconSizes.Keys) {
    $size = $iconSizes[$density]
    $mipmapDir = "app\src\main\res\mipmap-$density"
    
    # Regular launcher icon
    $outputPath = Join-Path $mipmapDir "ic_launcher.png"
    Resize-Image -SourcePath $sourceIcon -DestPath $outputPath -Size $size
    Write-Host "Created $density ic_launcher.png ($size x $size px)" -ForegroundColor Green
    
    # Round launcher icon
    $roundPath = Join-Path $mipmapDir "ic_launcher_round.png"
    Resize-Image -SourcePath $sourceIcon -DestPath $roundPath -Size $size
    Write-Host "Created $density ic_launcher_round.png ($size x $size px)" -ForegroundColor Green
}

Write-Host "`nAll icons resized successfully!" -ForegroundColor Green
Write-Host "You can now rebuild the app." -ForegroundColor Yellow
