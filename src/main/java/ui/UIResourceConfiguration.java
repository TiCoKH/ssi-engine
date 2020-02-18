package ui;

import common.FileMap;
import data.ContentType;
import data.image.EGADependentImages;
import data.image.EGAImage;
import data.image.ExtendedEGADependentImages;
import data.image.ExtendedEGAImage;
import data.image.ImageContent;
import data.image.TLBTILEBlock;
import data.image.VGADependentImages;
import data.image.VGAImage;
import shared.GameResourceConfiguration;
import ui.FrameType.BackgroundType;

public class UIResourceConfiguration extends GameResourceConfiguration {
	private static final String CONFIG_FORMAT_8X8D = "8x8d.format";
	private static final String CONFIG_FORMAT_BACK = "back.format";
	private static final String CONFIG_FORMAT_BIGPIC = "bigpic.format";
	private static final String CONFIG_FORMAT_BODYHEAD = "bodyHead.format";
	private static final String CONFIG_FORMAT_PICTURE = "picture.format";
	private static final String CONFIG_FORMAT_SPRITE = "sprite.format";
	private static final String CONFIG_FORMAT_TITLE = "title.format";

	private static final String CONFIG_FONT_LOCATION = "font";
	private static final String CONFIG_FONT_UMLAUT_AE = "font.umlaut.ae";
	private static final String CONFIG_FONT_UMLAUT_OE = "font.umlaut.oe";
	private static final String CONFIG_FONT_UMLAUT_UE = "font.umlaut.ue";
	private static final String CONFIG_FONT_SHARP_SZ = "font.sharp.sz";
	private static final String CONFIG_MISC_LOCATION = "misc";
	private static final String CONFIG_MISC_ARROW_INDEX = "misc.arrow";
	private static final String CONFIG_MISC_AREA_MAP_INDEX = "misc.area";

	private static final String CONFIG_FRAME_START = "frames.";
	private static final String CONFIG_FRAME_TYPE = CONFIG_FRAME_START + "type";
	private static final String CONFIG_FRAME_LOCATION = CONFIG_FRAME_START + "location";
	private static final String CONFIG_FRAME_BACKGROUND_TYPE = CONFIG_FRAME_START + "background.type";
	private static final String CONFIG_FRAME_BACKGROUND = CONFIG_FRAME_START + "background";
	private static final String CONFIG_OUTER_FRAME_TOP = "outer.top";
	private static final String CONFIG_OUTER_FRAME_BOTTOM = "outer.bottom";
	private static final String CONFIG_OUTER_FRAME_LEFT = "outer.left";
	private static final String CONFIG_OUTER_FRAME_RIGHT = "outer.right";
	private static final String CONFIG_INNER_FRAME_H = "horizontal.separator";
	private static final String CONFIG_INNER_FRAME_V = "vertical.separator";
	private static final String CONFIG_PORTRAIT_TOP = CONFIG_FRAME_START + "portrait.top";
	private static final String CONFIG_PORTRAIT_BOTTOM = CONFIG_FRAME_START + "portrait.bottom";
	private static final String CONFIG_PORTRAIT_LEFT = CONFIG_FRAME_START + "portrait.left";
	private static final String CONFIG_PORTRAIT_RIGHT = CONFIG_FRAME_START + "portrait.right";

	private static final String CONFIG_TITLE_COUNT = "title.count";

	private static final String MODE_BACKDROP = "back.mode";

	public UIResourceConfiguration(FileMap filemap) throws Exception {
		super(filemap);
	}

	public Class<? extends ImageContent> getImageTypeClass(ContentType content) {
		switch (content) {
			case _8X8D:
				return getFormatClass(CONFIG_FORMAT_8X8D);
			case BACK:
				return getFormatClass(CONFIG_FORMAT_BACK);
			case BIGPIC:
				return getFormatClass(CONFIG_FORMAT_BIGPIC);
			case BODY:
			case HEAD:
				return getFormatClass(CONFIG_FORMAT_BODYHEAD);
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

	private Class<? extends ImageContent> getFormatClass(String key) {
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

	public String getFontUmlautAe() {
		return getProperty(CONFIG_FONT_UMLAUT_AE);
	}

	public String getFontUmlautOe() {
		return getProperty(CONFIG_FONT_UMLAUT_OE);
	}

	public String getFontUmlautUe() {
		return getProperty(CONFIG_FONT_UMLAUT_UE);
	}

	public String getFontSharpSz() {
		return getProperty(CONFIG_FONT_SHARP_SZ);
	}

	public String getMisc() {
		return getProperty(CONFIG_MISC_LOCATION);
	}

	public int getMiscArrowIndex() {
		return Integer.parseInt(getProperty(CONFIG_MISC_ARROW_INDEX, ""));
	}

	public int getMiscAreaMapIndex() {
		return Integer.parseInt(getProperty(CONFIG_MISC_AREA_MAP_INDEX, ""));
	}

	public FrameType getFrameType() {
		return FrameType.valueOf(getProperty(CONFIG_FRAME_TYPE));
	}

	public String getFrameLocation() {
		return getProperty(CONFIG_FRAME_LOCATION);
	}

	public String getOuterFrameTop(UIFrame frame) {
		return getFrameIndexes(frame, CONFIG_OUTER_FRAME_TOP);
	}

	public String getOuterFrameBottom(UIFrame frame) {
		return getFrameIndexes(frame, CONFIG_OUTER_FRAME_BOTTOM);
	}

	public String getOuterFrameLeft(UIFrame frame) {
		return getFrameIndexes(frame, CONFIG_OUTER_FRAME_LEFT);
	}

	public String getOuterFrameRight(UIFrame frame) {
		return getFrameIndexes(frame, CONFIG_OUTER_FRAME_RIGHT);
	}

	public String getInnerFrameHorizontal(UIFrame frame) {
		return getFrameIndexes(frame, CONFIG_INNER_FRAME_H);
	}

	public String getInnerFrameVertical(UIFrame frame) {
		return getFrameIndexes(frame, CONFIG_INNER_FRAME_V);
	}

	private String getFrameIndexes(UIFrame frame, String framePart) {
		return getProperty(CONFIG_FRAME_START + frame.name() + "." + framePart, "");
	}

	public BackgroundType getBackgroundType() {
		return BackgroundType.valueOf(getProperty(CONFIG_FRAME_BACKGROUND_TYPE));
	}

	public String getBackground() {
		return getProperty(CONFIG_FRAME_BACKGROUND, "");
	}

	public String getPortraitTop() {
		return getProperty(CONFIG_PORTRAIT_TOP, "");
	}

	public String getPortraitBottom() {
		return getProperty(CONFIG_PORTRAIT_BOTTOM, "");
	}

	public String getPortraitLeft() {
		return getProperty(CONFIG_PORTRAIT_LEFT, "");
	}

	public String getPortraitRight() {
		return getProperty(CONFIG_PORTRAIT_RIGHT, "");
	}

	public int getTitleCount() {
		return Integer.parseInt(getProperty(CONFIG_TITLE_COUNT, ""));
	}

	public String getTitle(int index) {
		return getProperty(String.format("title.%d", index), "");
	}

	public BackdropMode getBackdropMode() {
		return BackdropMode.valueOf(getProperty(MODE_BACKDROP));
	}
}
