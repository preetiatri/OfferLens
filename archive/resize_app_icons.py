"""
Properly resize OfferLens premium icon to all Android densities
"""
try:
    from PIL import Image
    import os
    
    # Icon sizes for different densities
    ICON_SIZES = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    source_icon = r"app\asset release\offerlens_app_icon_production_1767462390324.png"
    
    if not os.path.exists(source_icon):
        print(f"ERROR: Source icon not found: {source_icon}")
        exit(1)
    
    print("Opening premium app icon...")
    img = Image.open(source_icon)
    
    # Ensure it's RGBA
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    
    print(f"Source image: {img.size[0]}x{img.size[1]} pixels")
    
    # Create each density
    for density, size in ICON_SIZES.items():
        mipmap_dir = f"app\\src\\main\\res\\mipmap-{density}"
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # Resize with high quality
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save regular launcher icon
        output_path = os.path.join(mipmap_dir, "ic_launcher.png")
        resized.save(output_path, 'PNG', optimize=True)
        print(f"✓ Created {density}/ic_launcher.png ({size}x{size}px, {os.path.getsize(output_path)} bytes)")
        
        # Save round launcher icon
        round_path = os.path.join(mipmap_dir, "ic_launcher_round.png")
        resized.save(round_path, 'PNG', optimize=True)
        print(f"✓ Created {density}/ic_launcher_round.png ({size}x{size}px, {os.path.getsize(round_path)} bytes)")
    
    print("\n✓ All icons resized successfully!")
    print("Run './gradlew clean assembleDebug' to rebuild")
    
except ImportError:
    print("\nERROR: PIL (Pillow) is not installed.")
    print("Please install it with: pip install Pillow")
    print("\nAlternatively, use Android Studio's Image Asset tool:")
    print("1. Right-click res folder → New → Image Asset")
    print("2. Select 'Launcher Icons (Adaptive and Legacy)'")
    print(f"3. Choose the icon: app\\asset release\\offerlens_app_icon_production_1767462390324.png")
    print("4. Click 'Next' and 'Finish'")
    exit(1)
except Exception as e:
    print(f"\nERROR: {e}")
    import traceback
    traceback.print_exc()
    exit(1)
