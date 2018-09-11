package data.content;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

public class DAXImageContentConverter {

	public BufferedImage asWallSymbol(BufferedImage src) {
		IndexColorModel newCM = DAXPalette.transformToWallPalette((IndexColorModel) src.getColorModel());
		return new BufferedImage(newCM, src.getRaster(), false, null);
	}
}
