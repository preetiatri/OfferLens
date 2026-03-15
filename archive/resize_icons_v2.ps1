# Robust Icon Resizer V2
Add-Type -AssemblyName System.Drawing

$sourcePath = "app\asset release\offerlens_app_icon_production_1767462390324.png"
$baseDir = "app\src\main\res"

# Map Android densities to pixels
$sizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

if (-not (Test-Path $sourcePath)) {
    Write-Error "Source file not found: $sourcePath"
    exit 1
}

$srcImage = [System.Drawing.Image]::FromFile((Resolve-Path $sourcePath))

foreach ($folderName in $sizes.Keys) {
    $size = $sizes[$folderName]
    $targetDir = Join-Path $baseDir $folderName
    
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }

    $fileNames = @("ic_launcher.png", "ic_launcher_round.png")

    foreach ($fileName in $fileNames) {
        $targetPath = Join-Path $targetDir $fileName
        
        # Create new bitmap with target size
        $bmp = New-Object System.Drawing.Bitmap($size, $size)
        $graph = [System.Drawing.Graphics]::FromImage($bmp)
        
        # High quality settings
        $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graph.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graph.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graph.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

        # Draw source image onto new bitmap
        $graph.DrawImage($srcImage, 0, 0, $size, $size)
        
        # Dispose graphics to release lock on new bitmap
        $graph.Dispose()

        # Delete existing file to ensure clean write
        if (Test-Path $targetPath) {
            Remove-Item $targetPath -Force
        }

        # Save
        $bmp.Save($targetPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $bmp.Dispose()
        
        # Verify
        if (Test-Path $targetPath) {
            $fileItem = Get-Item $targetPath
            if ($fileItem.Length -lt 200000) {
                # 200KB is plenty for a 192x192 icon, 571KB is the original
                Write-Host "SUCCESS: Created $folderName\$fileName ($size x $size) - Size: $($fileItem.Length) bytes" -ForegroundColor Green
            }
            else {
                Write-Host "WARNING: File size seems large for $folderName\$fileName: $($fileItem.Length) bytes" -ForegroundColor Yellow
            }
        }
        else {
            Write-Error "FAILED to create $targetPath"
        }
    }
}

$srcImage.Dispose()
Write-Host "Done."
