"""
Makes the grayscale lava textures Soulscorch tints by biome.

Usage (needs Python 3 and Pillow: `pip install pillow`):
    python tools/make_gray_lava.py <folder with vanilla lava_still.png, lava_flow.png and their .mcmeta>

Get the vanilla files from the Minecraft jar (it's a zip), under
assets/minecraft/textures/block/. Output goes straight into
src/main/resources/assets/soulscorch/textures/block/.

Each pixel is converted to brightness, then scaled so that multiplying it by
the "normal lava" tint (1800K) gives back roughly the vanilla brightness.
"""
import shutil
import sys
from pathlib import Path

from PIL import Image

# Must match LavaTint.kelvinToRgb(NORMAL_KELVIN)
NORMAL_TINT = (255, 126, 0)
TINT_LUMA = 0.299 * NORMAL_TINT[0] + 0.587 * NORMAL_TINT[1] + 0.114 * NORMAL_TINT[2]

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/soulscorch/textures/block"


def convert(src: Path, dst: Path) -> None:
    img = Image.open(src).convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            luma = 0.299 * r + 0.587 * g + 0.114 * b
            v = min(255, round(luma * 255 / TINT_LUMA))
            px[x, y] = (v, v, v, a)
    img.save(dst)


def main() -> None:
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    src_dir = Path(sys.argv[1])
    OUT.mkdir(parents=True, exist_ok=True)
    for name in ("lava_still", "lava_flow"):
        convert(src_dir / f"{name}.png", OUT / f"{name}_gray.png")
        # Keep vanilla's animation timing
        shutil.copy(src_dir / f"{name}.png.mcmeta", OUT / f"{name}_gray.png.mcmeta")
        print(f"wrote {name}_gray.png")


if __name__ == "__main__":
    main()
