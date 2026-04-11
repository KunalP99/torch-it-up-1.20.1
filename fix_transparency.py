"""
Fix torch PNG transparency: converts black pixels that should be transparent
back to proper transparency by using the vanilla torch's alpha channel as a mask.

Strategy: any pixel that is fully transparent in the original vanilla torch texture
should also be transparent in our custom textures.
If vanilla torch isn't available, falls back to making pure black (0,0,0) pixels transparent.
"""

from PIL import Image
import os
import zipfile
import glob

TEXTURES_DIR = "src/main/resources/assets/torch-placer/textures/block"

def find_vanilla_torch_alpha():
    """Try to extract the alpha mask from vanilla torch.png inside the Minecraft jar."""
    appdata = os.environ.get("APPDATA", "")
    jar_path = os.path.join(appdata, ".minecraft", "versions", "1.20.1", "1.20.1.jar")
    if not os.path.exists(jar_path):
        print(f"Minecraft jar not found at {jar_path}, using fallback method.")
        return None
    try:
        with zipfile.ZipFile(jar_path, "r") as jar:
            with jar.open("assets/minecraft/textures/block/torch.png") as f:
                vanilla = Image.open(f).convert("RGBA")
                print("Loaded vanilla torch.png alpha mask from Minecraft jar.")
                return vanilla
    except Exception as e:
        print(f"Could not read vanilla torch from jar: {e}")
        return None

def fix_png_with_vanilla_mask(img, vanilla):
    """Apply vanilla torch's alpha channel as a mask to our custom texture."""
    img = img.convert("RGBA")
    vanilla_resized = vanilla.resize(img.size, Image.NEAREST)
    r, g, b, _ = img.split()
    _, _, _, vanilla_alpha = vanilla_resized.split()
    return Image.merge("RGBA", (r, g, b, vanilla_alpha))

def fix_png_black_to_transparent(img):
    """Fallback: make pure black (0,0,0,255) pixels transparent."""
    img = img.convert("RGBA")
    pixels = img.load()
    changed = 0
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if r == 0 and g == 0 and b == 0 and a == 255:
                pixels[x, y] = (0, 0, 0, 0)
                changed += 1
    return img, changed

def main():
    vanilla = find_vanilla_torch_alpha()
    png_files = glob.glob(os.path.join(TEXTURES_DIR, "*.png"))

    if not png_files:
        print("No PNG files found in", TEXTURES_DIR)
        return

    for path in png_files:
        img = Image.open(path).convert("RGBA")
        name = os.path.basename(path)

        if vanilla is not None:
            fixed = fix_png_with_vanilla_mask(img, vanilla)
            print(f"  Fixed {name} using vanilla alpha mask.")
        else:
            fixed, changed = fix_png_black_to_transparent(img)
            print(f"  Fixed {name}: made {changed} black pixels transparent (fallback mode).")

        fixed.save(path)

    print(f"\nDone. Processed {len(png_files)} file(s).")

if __name__ == "__main__":
    main()
