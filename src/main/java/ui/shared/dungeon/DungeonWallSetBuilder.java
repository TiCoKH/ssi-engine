package ui.shared.dungeon;

import static data.ContentType.WALLDEF;
import static data.ContentType._8X8D;
import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import data.dungeon.WallDef;
import data.dungeon.WallDef.WallDistance;
import data.dungeon.WallDef.WallPlacement;
import data.image.ImageContent;
import data.palette.Palette;
import ui.shared.resource.UIResourceLoader;

public class DungeonWallSetBuilder {
	private UIResourceLoader loader;

	private int id1;
	private int id2;
	private int id3;

	public DungeonWallSetBuilder(@Nonnull UIResourceLoader loader) {
		this.loader = loader;
	}

	public DungeonWallSetBuilder withWallDecoIds(int id1, int id2, int id3) {
		this.id1 = id1;
		this.id2 = id2;
		this.id3 = id3;
		return this;
	}

	@Nonnull
	public List<DungeonWall> build() throws IOException {
		if (loader.idsFor(WALLDEF).isEmpty()) {
			return buildWithoutWallDefs();
		}
		return buildUsingWallDefs();
	}

	@Nonnull
	public List<DungeonWall> buildWithoutWallDefs() throws IOException {
		List<DungeonWall> result = new ArrayList<>();

		if (id1 != 127 && id1 != 255)
			result.addAll(buildWithoutWallDefs(id1));
		if (id2 != 127 && id2 != 255)
			result.addAll(buildWithoutWallDefs(id2));
		if (id3 != 127 && id3 != 255)
			result.addAll(buildWithoutWallDefs(id3));

		return result;
	}

	@Nonnull
	public List<DungeonWall> buildWithoutWallDefs(int id) throws IOException {
		List<BufferedImage> wallParts = loader.findImage(id, _8X8D).toList();

		List<BufferedImage> wall1 = wallParts.subList(0, 10);
		List<BufferedImage> wall2 = wallParts.subList(10, 20);

		List<BufferedImage> decal1 = wallParts.subList(20, 29);
		List<BufferedImage> decal2 = wallParts.subList(29, 38);
		List<BufferedImage> decal3 = wallParts.subList(38, 47);

		List<DungeonWall> result = new ArrayList<>(5);
		result.add(buildWall(wall1, null));
		result.add(buildWall(wall2, null));
		result.add(buildWall(wall1, decal1));
		result.add(buildWall(wall1, decal2));
		result.add(buildWall(wall1, decal3));
		return result;
	}

	@Nonnull
	private DungeonWall buildWall(List<BufferedImage> wall, List<BufferedImage> decal) {
		int i = 0;

		BufferedImage farFiller = wall.get(0);

		Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap = new EnumMap<>(WallDistance.class);
		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				Map<WallPlacement, BufferedImage> plcMap = wallViewsMap.get(dis);
				if (plcMap == null) {
					plcMap = new EnumMap<>(WallPlacement.class);
					wallViewsMap.put(dis, plcMap);
				}

				BufferedImage wallView = wall.get(++i);

				if (decal != null)
					plcMap.put(plc, renderDecalWall(wallView, decal.get(i - 1), WALL_MAX_WIDTH[i], WALL_MAX_HEIGHT[i]));
				else
					plcMap.put(plc, wallView);
			}
		}

		return new DungeonWall(wallViewsMap, farFiller);
	}

	private static final int[] WALL_MAX_WIDTH = { 8, 8, 8, 8, 24, 16, 16, 56, 16, 16 };
	private static final int[] WALL_MAX_HEIGHT = { 8, 8, 24, 24, 24, 56, 56, 56, 88, 88 };

	@Nonnull
	private BufferedImage renderDecalWall(BufferedImage wallView, BufferedImage decalView, int maxWidth, int maxHeight) {
		ColorModel cm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, //
			0xff0000, 0x00ff00, 0x0000ff, 0xff000000, false, DataBuffer.TYPE_INT);
		WritableRaster r = cm.createCompatibleWritableRaster(maxWidth, maxHeight);
		BufferedImage result = new BufferedImage(cm, r, false, null);

		Graphics2D g2d = result.createGraphics();
		g2d.drawImage(wallView, maxWidth - wallView.getWidth(), maxHeight - wallView.getHeight(), null);

		int x = Math.abs((int) decalView.getProperty(X_OFFSET.name()));
		int y = Math.abs((int) decalView.getProperty(Y_OFFSET.name()));
		g2d.drawImage(decalView, x, y, null);
		return result;
	}

	@Nonnull
	public List<DungeonWall> buildUsingWallDefs() throws IOException {
		List<DungeonWall> result = new ArrayList<>(15);

		int wallDefId1 = id1 == 127 ? 0 : id1;
		WallDef wallDef1 = loader.find(wallDefId1, WallDef.class, WALLDEF);
		List<BufferedImage> wallSymbols1 = buildWallSymbolListFor(wallDefId1, wallDef1.getWallCount() > 5);
		result.addAll(buildDungeonWallSetPart(wallDef1, wallSymbols1, 0));

		if (id2 != 255) {
			WallDef wallDef2 = id2 != 127 ? loader.find(id2, WallDef.class, WALLDEF) : null;
			List<BufferedImage> wallSymbols2 = wallSymbols1;
			int wallStart2 = 5;
			if (wallDef2 != null && id2 != 127) {
				wallSymbols2 = buildWallSymbolListFor(id2, wallDef2.getWallCount() > 5);
				wallStart2 = 0;
			} else {
				wallDef2 = wallDef1;
			}
			result.addAll(buildDungeonWallSetPart(wallDef2, wallSymbols2, wallStart2));

			if (id3 != 255) {
				WallDef wallDef3 = id3 != 127 ? loader.find(id3, WallDef.class, WALLDEF) : null;
				List<BufferedImage> wallSymbols3 = wallSymbols2;
				int wallStart3 = wallStart2 + 5;
				if (wallDef3 != null && id3 != 127) {
					wallSymbols3 = buildWallSymbolListFor(id3, wallDef3.getWallCount() > 5);
					wallStart3 = 0;
				} else {
					wallDef3 = wallDef2;
				}
				result.addAll(buildDungeonWallSetPart(wallDef3, wallSymbols3, wallStart3));
			}
		}
		return ImmutableList.copyOf(result);
	}

	private List<DungeonWall> buildDungeonWallSetPart(WallDef wallDef, List<BufferedImage> wallSymbols, int wallStart) {
		List<DungeonWall> result = new ArrayList<>(5);
		if (wallDef.getWallCount() < wallStart + 5) {
			return result;
		}
		for (int i = wallStart; i < wallStart + 5; i++) {
			BufferedImage farFiller = buildWallView(wallSymbols, wallDef.getWallDisplayFarFiller(i));

			Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap = new EnumMap<>(WallDistance.class);
			for (WallDistance dis : WallDistance.values()) {
				for (WallPlacement plc : WallPlacement.values()) {
					Map<WallPlacement, BufferedImage> plcMap = wallViewsMap.get(dis);
					if (plcMap == null) {
						plcMap = new EnumMap<>(WallPlacement.class);
						wallViewsMap.put(dis, plcMap);
					}
					plcMap.put(plc, buildWallView(wallSymbols, wallDef.getWallDisplay(i, dis, plc)));
				}
			}
			result.add(new DungeonWall(wallViewsMap, farFiller));
		}
		return result;
	}

	private BufferedImage buildWallView(List<BufferedImage> wallSymbols, int[][] indexes) {
		int height = indexes.length << 3;
		int width = indexes[0].length << 3;

		BufferedImage result = new BufferedImage(width, height, wallSymbols.get(0).getType(), (IndexColorModel) wallSymbols.get(0).getColorModel());
		WritableRaster r = result.getRaster();

		for (int y = 0; y < indexes.length; y++) {
			int[] row = indexes[y];
			for (int x = 0; x < row.length; x++) {
				BufferedImage s = row[x] >= wallSymbols.size() ? wallSymbols.get(0) : wallSymbols.get(row[x]);
				r.setRect(x << 3, y << 3, s.getData());
			}
		}
		return result;
	}

	private List<BufferedImage> buildWallSymbolListFor(int id, boolean additionalSymbols) throws IOException {
		List<BufferedImage> result = new ArrayList<>();

		ImageContent symbols = loader.find8x8d(id);
		if (symbols != null && symbols.size() >= 255) {
			if (symbols.size() == 255) {
				result.add(createTransparentSymbol());
			}
			result.addAll(symbols.toList());
		} else {
			result.add(createTransparentSymbol());
			result.addAll(loader.findImage(203, _8X8D).toList());
			if (symbols != null)
				result.addAll(symbols.toList());
			if (additionalSymbols) {
				int addSymbolsIdBase = 10 * (id == 0 ? 10 : id);
				if (loader.idsFor(_8X8D).contains(addSymbolsIdBase + 1)) {
					result.addAll(loader.findImage(addSymbolsIdBase + 1, _8X8D).toList());
				}
				if (loader.idsFor(_8X8D).contains(addSymbolsIdBase + 2)) {
					result.addAll(loader.findImage(addSymbolsIdBase + 2, _8X8D).toList());
				}
				if (loader.idsFor(_8X8D).contains(addSymbolsIdBase + 3)) {
					result.addAll(loader.findImage(addSymbolsIdBase + 3, _8X8D).toList());
				}
			}
		}
		return result;
	}

	private BufferedImage createTransparentSymbol() {
		IndexColorModel cm = Palette.createColorModel(_8X8D);
		byte[] data = new byte[32];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) 0xDD;
		}
		WritableRaster r = Raster.createPackedRaster(new DataBufferByte(data, 32, 0), 8, 8, 4, null);
		return new BufferedImage(cm, r, false, null);
	}
}
