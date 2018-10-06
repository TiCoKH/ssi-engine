package data.content;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

public class DAXImageContentConverter {

	public BufferedImage asWallSymbol(BufferedImage src) {
		IndexColorModel newCM = DAXPalette.transformToWallPalette((IndexColorModel) src.getColorModel());
		return new BufferedImage(newCM, src.getRaster(), false, null);
	}

	public BufferedImage asSprit(BufferedImage src) {
		IndexColorModel newCM = DAXPalette.transformToSpritPalette((IndexColorModel) src.getColorModel());
		return new BufferedImage(newCM, src.getRaster(), false, null);
	}

	public BufferedImage withGreenFG(BufferedImage src) {
		IndexColorModel newCM = DAXPalette.binaryPaletteWithGreenFG();
		return new BufferedImage(newCM, src.getRaster(), false, null);
	}

	public BufferedImage withMagentaFG(BufferedImage src) {
		IndexColorModel newCM = DAXPalette.binaryPaletteWithMagentaFG();
		return new BufferedImage(newCM, src.getRaster(), false, null);
	}

	public BufferedImage withInvertedColors(BufferedImage src) {
		IndexColorModel newCM = DAXPalette.binaryInvertedPalette();
		return new BufferedImage(newCM, src.getRaster(), false, null);
	}
}
