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
import ui.FrameType.BackgroundType;
import ui.FrameType.PortraitType;

public class UIResourceConfiguration extends GameResourceConfiguration {
	private static final String CONFIG_FORMAT_8X8D = "8x8d.format";
	private static final String CONFIG_FORMAT_BACK = "back.format";
	private static final String CONFIG_FORMAT_BIGPIC = "bigpic.format";
	private static final String CONFIG_FORMAT_BODYHEAD = "bodyHead.format";
	private static final String CONFIG_FORMAT_PICTURE = "picture.format";
	private static final String CONFIG_FORMAT_SPRITE = "sprite.format";
	private static final String CONFIG_FORMAT_TITLE = "title.format";

	private static final String CONFIG_FONT_LOCATION = "font";
	private static final String CONFIG_MISC_LOCATION = "misc";

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
	private static final String CONFIG_FRAME_PORTRAIT_TYPE = CONFIG_FRAME_START + "portrait.type";
	private static final String CONFIG_PORTRAIT_TOP = CONFIG_FRAME_START + "portrait.top";
	private static final String CONFIG_PORTRAIT_BOTTOM = CONFIG_FRAME_START + "portrait.bottom";
	private static final String CONFIG_PORTRAIT_LEFT = CONFIG_FRAME_START + "portrait.left";
	private static final String CONFIG_PORTRAIT_RIGHT = CONFIG_FRAME_START + "portrait.right";

	private static final String CONFIG_TITLE_COUNT = "title.count";

	private static final String MODE_BACKDROP = "back.mode";

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

	public String getMisc() {
		return getProperty(CONFIG_MISC_LOCATION);
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

	public PortraitType getPortraitType() {
		return PortraitType.valueOf(getProperty(CONFIG_FRAME_PORTRAIT_TYPE));
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
