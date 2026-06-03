"""
generate_icon.py - Generates PUP Pass Slip System icon
Run once: python generate_icon.py
"""

from PIL import Image, ImageDraw, ImageFont
import os

def generate_icon():
    sizes = [16, 32, 48, 64, 128, 256]
    images = []

    for size in sizes:
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)

        # Red square background with rounded corners
        draw.rounded_rectangle(
            [0, 0, size-1, size-1],
            radius=size//6,
            fill=(139, 0, 0, 255)
        )

        # White shield
        cx = size // 2
        sw = int(size * 0.55)  # shield width
        sh = int(size * 0.60)  # shield height
        sx = cx - sw // 2
        sy = int(size * 0.18)

        # Shield top half - rounded rectangle
        draw.rounded_rectangle(
            [sx, sy, sx + sw, sy + int(sh * 0.65)],
            radius=size // 8,
            fill=(255, 255, 255, 255)
        )

        # Shield bottom half - triangle point
        draw.polygon([
            (sx,          sy + int(sh * 0.45)),
            (sx + sw,     sy + int(sh * 0.45)),
            (cx,          sy + sh),
        ], fill=(255, 255, 255, 255))

        # Dark red "P" letter in shield center
        if size >= 32:
            try:
                font_size = max(int(size * 0.30), 8)
                try:
                    font = ImageFont.truetype("arialbd.ttf", font_size)
                except:
                    try:
                        font = ImageFont.truetype("arial.ttf", font_size)
                    except:
                        font = ImageFont.load_default()

                text = "P"
                bbox = draw.textbbox((0, 0), text, font=font)
                tw = bbox[2] - bbox[0]
                th = bbox[3] - bbox[1]
                tx = cx - tw // 2
                ty = sy + int(sh * 0.15)
                draw.text((tx, ty), text, fill=(139, 0, 0, 255), font=font)
            except Exception as ex:
                pass

        images.append(img)

    output = "pup_passlip.ico"
    images[0].save(
        output,
        format='ICO',
        sizes=[(s, s) for s in sizes],
        append_images=images[1:]
    )
    print(f"[OK] Icon saved: {output}")

if __name__ == "__main__":
    generate_icon()
