Add-Type -AssemblyName System.Drawing
$dir = "app\src\main\res\mipmap-xxxhdpi"
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }

$bmp = New-Object System.Drawing.Bitmap(192, 192)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::DarkBlue)
$brush = [System.Drawing.Brushes]::White
$font = New-Object System.Drawing.Font("Arial", 20)
$g.DrawString("ICON", $font, $brush, 10, 80)
$g.Dispose()

$bmp.Save("$dir\ic_launcher.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Save("$dir\ic_launcher_round.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Host "Created placeholder icons."
