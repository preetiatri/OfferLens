# Robust Icon Resizer V3 - Safer Syntax

try {
    Add-Type -AssemblyName System.Drawing
}
catch {
    Write-Error "Failed to load System.Drawing assembly."
    exit 1
}

$sourcePath = "app\asset release\offerlens_app_icon_production_1767462390324.png"
$baseDir = "app\src\main\res"

# Map Android densities to pixels
$densityMap = @{
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

$startDir = Get-Location
$absSourcePath = Join-Path $startDir $sourcePath
Write-Host "Source: $absSourcePath"

try {
    $srcImage = [System.Drawing.Image]::FromFile($absSourcePath)
}
catch {
    Write-Error "Failed to load source image: $_"
    exit 1
}

foreach ($key in $densityMap.Keys) {
    $folderName = $key
    $size = $densityMap[$key]
    
    $targetDir = Join-Path $baseDir $folderName
    
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }

    $fileNames = @("ic_launcher.png", "ic_launcher_round.png")

    foreach ($fileName in $fileNames) {
        $targetPath = Join-Path $targetDir $fileName
        $absTargetPath = Join-Path $startDir $targetPath

        try {
            # Create new bitmap
            $bmp = New-Object System.Drawing.Bitmap($size, $size)
            $graph = [System.Drawing.Graphics]::FromImage($bmp)
            
            # High quality settings
            $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graph.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graph.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $graph.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

            # Draw source image
            $graph.DrawImage($srcImage, 0, 0, $size, $size)
            $graph.Dispose()

            # Save
            if (Test-Path $absTargetPath) {
                Remove-Item $absTargetPath -Force
            }
            
            $bmp.Save($absTargetPath, [System.Drawing.Imaging.ImageFormat]::Png)
            $bmp.Dispose()
            
            # Verify size
            $finalItem = Get-Item $absTargetPath
            $len = $finalItem.Length
            Write-Host "CREATED: $folderName / $fileName ($size px) - $len bytes"
        }
        catch {
            Write-Error "Failed to process $targetPath : $_"
        }
    }
}

$srcImage.Dispose()
Write-Host "Icon resize process completed."
