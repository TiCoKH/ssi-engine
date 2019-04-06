package ui;

import common.FileMap;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.EGADependentImages;
import data.content.EGAImage;
import data.content.ExtendedEGADependentImages;
import data.content.ExtendedEGAImage;
import data.content.TLBTILEBlock;
import data.content.VGADependentImages;
import data.content.VGAImage;
import types.GameResourceConfiguration;

public class UIResourceConfiguration extends GameResourceConfiguration {
	private static final String CONFIG_FORMAT_8X8D = "8x8d.format";
	private static final String CONFIG_FORMAT_BACK = "back.format";
	private static final String CONFIG_FORMAT_BIGPIC = "bigpic.format";
	private static final String CONFIG_FORMAT_PICTURE = "picture.format";
	private static final String CONFIG_FORMAT_SPRITE = "sprite.format";
	private static final String CONFIG_FORMAT_TITLE = "title.format";

	private static final String CONFIG_FONT_LOCATION = "font";

	public UIResourceConfiguration(FileMap filemap) throws Exception {
		super(filemap);
	}

	public Class<? extends DAXImageContent> getImageTypeClass(DAXContentType content) {
		switch (content) {
			case _8X8D:
				return getFormatClass(CONFIG_FORMAT_8X8D);
			case BACK:
				return getFormatClass(CONFIG_FORMAT_BACK);
			case BIGPIC:
				return getFormatClass(CONFIG_FORMAT_BIGPIC);
			case PIC:
				return getFormatClass(CONFIG_FORMAT_PICTURE);
			case SPRIT:
				return getFormatClass(CONFIG_FORMAT_SPRITE);
			case TITLE:
				return getFormatClass(CONFIG_FORMAT_TITLE);
			default:
				throw new IllegalArgumentException("Unknown image content: " + content);
		}
	}

	private Class<? extends DAXImageContent> getFormatClass(String key) {
		String format = getProperty(key);
		switch (format) {
			case "E":
				return EGAImage.class;
			case "ED":
				return EGADependentImages.class;
			case "EE":
				return ExtendedEGAImage.class;
			case "EDE":
				return ExtendedEGADependentImages.class;
			case "T":
				return TLBTILEBlock.class;
			case "V":
				return VGAImage.class;
			case "VD":
				return VGADependentImages.class;
			default:
				throw new IllegalArgumentException("Format " + key + " is unknown.");
		}
	}

	public String getFont() {
		return getProperty(CONFIG_FONT_LOCATION);
	}
}
