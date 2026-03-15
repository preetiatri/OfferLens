# Fix Icons PowerShell - Convert JPEG to PNG and Resize
try {
    Add-Type -AssemblyName System.Drawing
}
catch {
    Write-Error "Failed to load System.Drawing."
    exit 1
}

# The source file is actually a JPEG despite .png extension
$sourcePath = "app\asset release\offerlens_app_icon_production_1767462390324.png"
$baseDir = "app\src\main\res"

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

# System.Drawing.Image.FromFile auto-detects format (JPEG)
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
    if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }

    $fileNames = @("ic_launcher.png", "ic_launcher_round.png")

    foreach ($fileName in $fileNames) {
        $targetPath = Join-Path $targetDir $fileName
        $absTargetPath = Join-Path $startDir $targetPath

        try {
            # Create new bitmap (defaults to ARGB)
            $bmp = New-Object System.Drawing.Bitmap($size, $size)
            $graph = [System.Drawing.Graphics]::FromImage($bmp)
            
            $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graph.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graph.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $graph.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

            # Draw source image (JPEG) onto PNG bitmap
            $graph.DrawImage($srcImage, 0, 0, $size, $size)
            $graph.Dispose()

            if (Test-Path $absTargetPath) { Remove-Item $absTargetPath -Force }
            
            # Save strictly as PNG
            $bmp.Save($absTargetPath, [System.Drawing.Imaging.ImageFormat]::Png)
            $bmp.Dispose()
            
            Write-Host "FIXED: $folderName / $fileName ($size px)"
        }
        catch {
            Write-Error "Failed to process $targetPath : $_"
        }
    }
}

$srcImage.Dispose()
Write-Host "Icon fix completed."
