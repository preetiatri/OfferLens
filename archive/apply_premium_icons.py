"""
Apply OfferLens Premium Icons
Resizes and copies the premium app icon to all required mipmap folders
"""
from PIL import Image
import os
import shutil

# Icon sizes for different densities
ICON_SIZES = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

def apply_app_icon():
    """Resize and apply the premium app icon to all mipmap folders"""
    
    source_icon = r"app\asset release\offerlens_app_icon_production_1767462390324.png"
    
    if not os.path.exists(source_icon):
        print(f"❌ Source icon not found: {source_icon}")
        return False
    
    print(f"✓ Found premium app icon")
    
    # Open the source image
    img = Image.open(source_icon)
    
    # Ensure it's square
    width, height = img.size
    if width != height:
        # Crop to square (center crop)
        size = min(width, height)
        left = (width - size) // 2
        top = (height - size) // 2
        img = img.crop((left, top, left + size, top + size))
        print(f"✓ Cropped to square: {size}x{size}")
    
    # Create each density
    for density, size in ICON_SIZES.items():
        # Create directory if it doesn't exist
        mipmap_dir = f"app\\src\\main\\res\\mipmap-{density}"
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # Resize and save
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        output_path = os.path.join(mipmap_dir, "ic_launcher.png")
        resized.save(output_path, 'PNG', quality=100, optimize=True)
        
        # Also create round version
        round_path = os.path.join(mipmap_dir, "ic_launcher_round.png")
        resized.save(round_path, 'PNG', quality=100, optimize=True)
        
        print(f"✓ Created {density}: {size}x{size}px")
    
    return True

def copy_to_deployment():
    """Copy assets to deployment_assets folder"""
    
    os.makedirs("deployment_assets", exist_ok=True)
    
    # Copy app icon (high res)
    shutil.copy(
        r"app\asset release\offerlens_app_icon_production_1767462390324.png",
        r"deployment_assets\app_icon_512.png"
    )
    print("✓ Copied app icon to deployment_assets/app_icon_512.png")
    
    # Copy feature graphic
    shutil.copy(
        r"app\asset release\feature_graphic_offerlens_1767463235055.png",
        r"deployment_assets\feature_graphic.png"
    )
    print("✓ Copied feature graphic to deployment_assets/feature_graphic.png")

if __name__ == "__main__":
    print("OfferLens Premium Icon Application")
    print("=" * 50)
    
    try:
        if apply_app_icon():
            print("\n✓ App icon applied to all mipmap densities!")
        
        copy_to_deployment()
        
        print("\n✓ All assets ready for deployment!")
        print("\nNext steps:")
        print("1. Build the app to see the new icon")
        print("2. Use deployment_assets/ files for Play Store listing")
        
    except ImportError:
        print("\n❌ PIL (Pillow) not installed.")
        print("Install with: pip install Pillow")
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
