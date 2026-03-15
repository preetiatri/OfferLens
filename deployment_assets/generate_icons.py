"""
OfferLens Icon Generator
Creates a professional app icon for Play Store submission
"""
from PIL import Image, ImageDraw, ImageFont
import os

def create_app_icon(output_path="deployment_assets/app_icon_512.png", size=512):
    """Create a premium app icon for OfferLens"""
    
    # Create image with royal green gradient background
    img = Image.new('RGB', (size, size), '#1B5E20')
    draw = ImageDraw.Draw(img)
    
    # Draw gradient background
    for y in range(size):
        # Gradient from dark green to lighter green
        r = int(27 + (y / size) * 30)
        g = int(94 + (y / size) * 60)
        b = int(32 + (y / size) * 20)
        draw.line([(0, y), (size, y)], fill=(r, g, b))
    
    # Draw magnifying glass icon
    center_x, center_y = size // 2, size // 2 - size // 10
    circle_radius = size // 4
    
    # Magnifying glass circle (white with gold border)
    draw.ellipse(
        [center_x - circle_radius, center_y - circle_radius,
         center_x + circle_radius, center_y + circle_radius],
        fill='#FFFFFF',
        outline='#FFD700',
        width=size // 40
    )
    
    # Percentage symbol inside
    try:
        font_size = size // 4
        font = ImageFont.truetype("arial.ttf", font_size)
    except:
        font = ImageFont.load_default()
    
    text = "%"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    draw.text(
        (center_x - text_width // 2, center_y - text_height // 2),
        text,
        fill='#FFD700',
        font=font
    )
    
    # Magnifying glass handle
    handle_start_x = center_x + int(circle_radius * 0.7)
    handle_start_y = center_y + int(circle_radius * 0.7)
    handle_end_x = center_x + int(circle_radius * 1.5)
    handle_end_y = center_y + int(circle_radius * 1.5)
    
    draw.line(
        [(handle_start_x, handle_start_y), (handle_end_x, handle_end_y)],
        fill='#FFD700',
        width=size // 20
    )
    
    # Save
    os.makedirs(os.path.dirname(output_path) if os.path.dirname(output_path) else ".", exist_ok=True)
    img.save(output_path, 'PNG', quality=100)
    print(f"✓ Created app icon: {output_path}")
    
    return output_path

def create_feature_graphic(output_path="deployment_assets/feature_graphic.png"):
    """Create a Play Store feature graphic (1024x500)"""
    
    width, height = 1024, 500
    img = Image.new('RGB', (width, height), '#1B5E20')
    draw = ImageDraw.Draw(img)
    
    # Gradient background
    for y in range(height):
        r = int(27 + (y / height) * 40)
        g = int(94 + (y / height) * 80)
        b = int(32 + (y / height) * 30)
        draw.line([(0, y), (width, y)], fill=(r, g, b))
    
    # Decorative elements (price tags, coins)
    # Top left tag
    draw.polygon([(50, 80), (150, 80), (150, 180), (130, 200), (50, 180)], 
                 fill='#FFD700', outline='#FFFFFF')
    draw.text((70, 120), "%", fill='#FFFFFF', font=ImageFont.load_default())
    
    # Bottom right coin
    draw.ellipse([850, 350, 950, 450], fill='#FFD700', outline='#FFFFFF', width=3)
    draw.text((880, 385), "$", fill='#FFFFFF', font=ImageFont.load_default())
    
    # Save
    os.makedirs(os.path.dirname(output_path) if os.path.dirname(output_path) else ".", exist_ok=True)
    img.save(output_path, 'PNG', quality=100)
    print(f"✓ Created feature graphic: {output_path}")
    
    return output_path

if __name__ == "__main__":
    print("OfferLens Asset Generator")
    print("=" * 50)
    
    try:
        icon_path = create_app_icon()
        graphic_path = create_feature_graphic()
        
        print("\n✓ All assets created successfully!")
        print(f"\nNext steps:")
        print(f"1. Review assets in deployment_assets/")
        print(f"2. Use app_icon_512.png for Play Store listing")
        print(f"3. Use feature_graphic.png for Play Store feature graphic")
        
    except ImportError:
        print("\n❌ PIL (Pillow) not installed.")
        print("Install with: pip install Pillow")
    except Exception as e:
        print(f"\n❌ Error: {e}")
