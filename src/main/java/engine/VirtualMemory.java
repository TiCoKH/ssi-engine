package engine;

import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.content.DungeonMap.Direction;
import engine.opcodes.EclArgument;
import engine.opcodes.EclString;
import types.GoldboxString;

public class VirtualMemory implements ViewDungeonPosition, ViewSpacePosition, ViewOverlandPosition {
	public static final int MEMLOC_CURRENT_ECL = 0x0000;
	public static final int MEMLOC_AREA_START = 0x0001;
	public static final int MEMLOC_AREA_DECO_START = 0x0004;
	public static final int MEMLOC_CELESTIAL_POS_START = 0x4B85;
	public static final int MEMLOC_SPACE_X = 0x4BBE;
	public static final int MEMLOC_SPACE_Y = 0x4BBF;
	public static final int MEMLOC_SPACE_DIR = 0x4BC0;
	public static final int MEMLOC_OVERLAND_X = 0x4BC3;
	public static final int MEMLOC_OVERLAND_Y = 0x4BC4;
	public static final int MEMLOC_LAST_DUNGEON_X = 0x4BF0;
	public static final int MEMLOC_LAST_DUNGEON_Y = 0x4BF1;
	public static final int MEMLOC_LAST_ECL = 0x4BF2;
	public static final int MEMLOC_ENGINE_CONF_GAME_SPEED = 0x4BFC;
	public static final int MEMLOC_SKY_COLOR_OUTDOORS = 0x4BFD;
	public static final int MEMLOC_SKY_COLOR_INDOORS = 0x4BFE;
	public static final int MEMLOC_MED_SUPPLIES = 0x4C63;
	public static final int MEMLOC_FOR_LOOP_COUNT = 0x4CF6;
	public static final int MEMLOC_EXTENDED_DUNGEON_X = 0x4CFD;
	public static final int MEMLOC_EXTENDED_DUNGEON_Y = 0x4CFE;
	public static final int MEMLOC_HULL = 0x4D16;
	public static final int MEMLOC_SENSORS = 0x4D18;
	public static final int MEMLOC_CONTROL = 0x4D1A;
	public static final int MEMLOC_LIFE = 0x4D1C;
	public static final int MEMLOC_FUEL = 0x4D1E;
	public static final int MEMLOC_ENGINE = 0x4D20;
	public static final int MEMLOC_KCANNON_WEAPONS = 0x4D3E;
	public static final int MEMLOC_KCANNON_AMMO = 0x4D40;
	public static final int MEMLOC_KCANNON_RELOAD = 0x4D41;
	public static final int MEMLOC_MISSILE_WEAPONS = 0x4D44;
	public static final int MEMLOC_MISSILE_AMMO = 0x4D46;
	public static final int MEMLOC_MISSILE_RELOAD = 0x4D47;
	public static final int MEMLOC_LASER_WEAPONS = 0x4D4A;
	public static final int MEMLOC_COMBAT_RESULT = 0x7EC7;
	public static final int MEMLOC_MOVEMENT_BLOCK = 0x7EC9;
	public static final int MEMLOC_TRIED_TO_LEAVE_MAP = 0x7ED5;
	public static final int MEMLOC_PICTURE_HEAD_ID = 0x7EE1;
	public static final int MEMLOC_DIVISION_MODULO = 0x7F3F;
	public static final int MEMLOC_DUNGEON_X = 0xC04B;
	public static final int MEMLOC_DUNGEON_Y = 0xC04C;
	public static final int MEMLOC_DUNGEON_DIR = 0xC04D;
	public static final int MEMLOC_MAP_WALL_TYPE = 0xC04E;
	public static final int MEMLOC_MAP_SQUARE_INFO = 0xC04F;

	private static final int[] CELESTIAL_INITIAL_X = new int[] { 12, 12, 9, 9, 21, 18, 12, 6, 2, 4, 8, 14, 20 };
	private static final int[] CELESTIAL_INITIAL_Y = new int[] { 10, 9, 8, 15, 11, 17, 20, 18, 13, 3, 1, 1, 7 };
	private ByteBufferWrapper mem;

	private int menuChoice;
	private GoldboxString input;

	private EngineConfiguration cfg;

	public VirtualMemory(@Nonnull EngineConfiguration cfg) {
		this.cfg = cfg;

		mem = ByteBufferWrapper.allocateLE(0x10000);

		// set intial locations
		for (Celestial c : Celestial.values()) {
			setCelestialX(c, CELESTIAL_INITIAL_X[c.ordinal()]);
			setCelestialY(c, CELESTIAL_INITIAL_Y[c.ordinal()]);
		}
		setSpaceX(getCelestialX(Celestial.EARTH));
		setSpaceY(getCelestialY(Celestial.EARTH));

		// Spaceship
		setMedSupplies(10);
		setHull(600);
		setSensors(150);
		setControl(150);
		setLife(300);
		setFuel(450);
		setEngine(450);
		setKWeapons(2);
		setKWeaponAmmo(6);
		setKWeaponReloads(5);
		setMissiles(2);
		setMissileAmmo(4);
		setMissileReload(7);
		setLasers(5);
	}

	public void loadFrom(FileChannel fc) throws IOException {
		try {
			mem.position(0).readFrom(fc);
		} finally {
			fc.close();
		}
	}

	public void saveTo(FileChannel fc) throws IOException {
		try {
			mem.position(0).writeTo(fc);
			fc.force(true);
		} finally {
			fc.close();
		}
	}

	public int getMenuChoice() {
		return menuChoice;
	}

	public void setMenuChoice(int menuChoice) {
		this.menuChoice = menuChoice;
	}

	public GoldboxString getInput() {
		return input;
	}

	public void setInput(GoldboxString input) {
		this.input = input;
	}

	public int getCurrentECL() {
		return mem.getUnsigned(MEMLOC_CURRENT_ECL);
	}

	public void setCurrentECL(int currentECL) {
		mem.put(MEMLOC_CURRENT_ECL, (byte) currentECL);
	}

	public int getAreaValue(int id) {
		return mem.getUnsigned(MEMLOC_AREA_START + id);
	}

	public void setAreaValues(int id0, int id1, int id2) {
		mem.put(MEMLOC_AREA_START, (byte) id0);
		mem.put(MEMLOC_AREA_START + 1, (byte) id1);
		mem.put(MEMLOC_AREA_START + 2, (byte) id2);
	}

	public int getAreaDecoValue(int id) {
		return mem.getUnsigned(MEMLOC_AREA_DECO_START + id);
	}

	public void setAreaDecoValues(int id0, int id1, int id2) {
		mem.put(MEMLOC_AREA_DECO_START, (byte) id0);
		mem.put(MEMLOC_AREA_DECO_START + 1, (byte) id1);
		mem.put(MEMLOC_AREA_DECO_START + 2, (byte) id2);
	}

	@Override
	public int getCelestialX(Celestial c) {
		return mem.getUnsigned(MEMLOC_CELESTIAL_POS_START + 2 * c.ordinal());
	}

	public void setCelestialX(Celestial c, int celestialX) {
		mem.put(MEMLOC_CELESTIAL_POS_START + 2 * c.ordinal(), (byte) celestialX);
	}

	@Override
	public int getCelestialY(Celestial c) {
		return mem.getUnsigned(MEMLOC_CELESTIAL_POS_START + 1 + 2 * c.ordinal());
	}

	public void setCelestialY(Celestial c, int celestialY) {
		mem.put(MEMLOC_CELESTIAL_POS_START + 1 + 2 * c.ordinal(), (byte) celestialY);
	}

	@Override
	public int getSpaceX() {
		return mem.getUnsigned(MEMLOC_SPACE_X);
	}

	public void setSpaceX(int spaceX) {
		mem.put(MEMLOC_SPACE_X, (byte) spaceX);
	}

	@Override
	public int getSpaceY() {
		return mem.getUnsigned(MEMLOC_SPACE_Y);
	}

	public void setSpaceY(int spaceY) {
		mem.put(MEMLOC_SPACE_Y, (byte) spaceY);
	}

	@Override
	public SpaceDirection getSpaceDir() {
		return SpaceDirection.values()[mem.getUnsigned(MEMLOC_SPACE_DIR)];
	}

	public void setSpaceDir(SpaceDirection spaceDir) {
		mem.put(MEMLOC_SPACE_DIR, (byte) spaceDir.ordinal());
	}

	@Override
	public int getOverlandX() {
		return mem.getUnsigned(MEMLOC_OVERLAND_X);
	}

	public void setOverlandX(int overlandX) {
		mem.put(MEMLOC_OVERLAND_X, (byte) overlandX);
	}

	@Override
	public int getOverlandY() {
		return mem.getUnsigned(MEMLOC_OVERLAND_Y);
	}

	public void setOverlandY(int overlandY) {
		mem.put(MEMLOC_OVERLAND_Y, (byte) overlandY);
	}

	public int getLastDungeonX() {
		return mem.getUnsigned(MEMLOC_LAST_DUNGEON_X);
	}

	public void setLastDungeonX(int lastDungeonX) {
		mem.put(MEMLOC_LAST_DUNGEON_X, (byte) lastDungeonX);
	}

	public int getLastDungeonY() {
		return mem.getUnsigned(MEMLOC_LAST_DUNGEON_Y);
	}

	public void setLastDungeonY(int lastDungeonY) {
		mem.put(MEMLOC_LAST_DUNGEON_Y, (byte) lastDungeonY);
	}

	public int getLastECL() {
		return mem.getUnsigned(MEMLOC_LAST_ECL);
	}

	public void setLastECL(int lastECL) {
		mem.put(MEMLOC_LAST_ECL, (byte) lastECL);
	}

	public int getGameSpeed() {
		return mem.getUnsigned(MEMLOC_ENGINE_CONF_GAME_SPEED);
	}

	public void setGameSpeed(int gameSpeed) {
		mem.put(MEMLOC_ENGINE_CONF_GAME_SPEED, (byte) gameSpeed);
	}

	@Override
	public int getSkyColorOutdoors() {
		return mem.getUnsigned(MEMLOC_SKY_COLOR_OUTDOORS);
	}

	@Override
	public int getSkyColorIndoors() {
		return mem.getUnsigned(MEMLOC_SKY_COLOR_INDOORS);
	}

	public int getMedSupplies() {
		return mem.getUnsigned(MEMLOC_MED_SUPPLIES);
	}

	public void setMedSupplies(int medSupplies) {
		mem.put(MEMLOC_MED_SUPPLIES, (byte) medSupplies);
	}

	public int getForLoopCount() {
		return mem.getUnsigned(MEMLOC_FOR_LOOP_COUNT);
	}

	public void setForLoopCount(int loopCount) {
		mem.put(MEMLOC_FOR_LOOP_COUNT, (byte) loopCount);
	}

	@Override
	public int getExtendedDungeonX() {
		return mem.getUnsigned(MEMLOC_EXTENDED_DUNGEON_X);
	}

	public void setExtendedDungeonX(int extendedDungeonX) {
		mem.put(MEMLOC_EXTENDED_DUNGEON_X, (byte) extendedDungeonX);
	}

	@Override
	public int getExtendedDungeonY() {
		return mem.getUnsigned(MEMLOC_EXTENDED_DUNGEON_Y);
	}

	public void setExtendedDungeonY(int extendedDungeonY) {
		mem.put(MEMLOC_EXTENDED_DUNGEON_Y, (byte) extendedDungeonY);
	}

	public int getHull() {
		return mem.getUnsignedShort(MEMLOC_HULL);
	}

	public void setHull(int hull) {
		mem.putShort(MEMLOC_HULL, (short) hull);
	}

	public int getSensors() {
		return mem.getUnsignedShort(MEMLOC_SENSORS);
	}

	public void setSensors(int sensors) {
		mem.putShort(MEMLOC_SENSORS, (short) sensors);
	}

	public int getControl() {
		return mem.getUnsignedShort(MEMLOC_CONTROL);
	}

	public void setControl(int control) {
		mem.putShort(MEMLOC_CONTROL, (short) control);
	}

	public int getLife() {
		return mem.getUnsignedShort(MEMLOC_LIFE);
	}

	public void setLife(int life) {
		mem.putShort(MEMLOC_LIFE, (short) life);
	}

	@Override
	public int getFuel() {
		return mem.getUnsignedShort(MEMLOC_FUEL);
	}

	public void setFuel(int fuel) {
		mem.putShort(MEMLOC_FUEL, (short) fuel);
	}

	public int getEngine() {
		return mem.getUnsignedShort(MEMLOC_ENGINE);
	}

	public void setEngine(int engine) {
		mem.putShort(MEMLOC_ENGINE, (short) engine);
	}

	public int getKWeapons() {
		return mem.getUnsigned(MEMLOC_KCANNON_WEAPONS);
	}

	public void setKWeapons(int kWeapons) {
		mem.put(MEMLOC_KCANNON_WEAPONS, (byte) kWeapons);
	}

	public int getKWeaponAmmo() {
		return mem.getUnsigned(MEMLOC_KCANNON_AMMO);
	}

	public void setKWeaponAmmo(int kWeaponAmmo) {
		mem.put(MEMLOC_KCANNON_AMMO, (byte) kWeaponAmmo);
	}

	public int getKWeaponReloads() {
		return mem.getUnsigned(MEMLOC_KCANNON_RELOAD);
	}

	public void setKWeaponReloads(int kWeaponReloads) {
		mem.put(MEMLOC_KCANNON_RELOAD, (byte) kWeaponReloads);
	}

	public int getMissiles() {
		return mem.getUnsigned(MEMLOC_MISSILE_WEAPONS);
	}

	public void setMissiles(int missiles) {
		mem.put(MEMLOC_MISSILE_WEAPONS, (byte) missiles);
	}

	public int getMissileAmmo() {
		return mem.getUnsigned(MEMLOC_MISSILE_AMMO);
	}

	public void setMissileAmmo(int missileAmmo) {
		mem.put(MEMLOC_MISSILE_AMMO, (byte) missileAmmo);
	}

	public int getMissileReload() {
		return mem.getUnsigned(MEMLOC_MISSILE_RELOAD);
	}

	public void setMissileReload(int missileReload) {
		mem.put(MEMLOC_MISSILE_RELOAD, (byte) missileReload);
	}

	public int getLasers() {
		return mem.getUnsigned(MEMLOC_LASER_WEAPONS);
	}

	public void setLasers(int lasers) {
		mem.put(MEMLOC_LASER_WEAPONS, (byte) lasers);
	}

	public int getCombatResult() {
		return mem.getUnsigned(MEMLOC_COMBAT_RESULT);
	}

	public void setCombatResult(int combatResult) {
		mem.put(MEMLOC_COMBAT_RESULT, (byte) combatResult);
	}

	public int getMovementBlock() {
		return mem.getUnsigned(MEMLOC_MOVEMENT_BLOCK);
	}

	public void setMovementBlock(int movementBlock) {
		mem.put(MEMLOC_MOVEMENT_BLOCK, (byte) movementBlock);
	}

	public boolean getTriedToLeaveMap() {
		return mem.getUnsigned(MEMLOC_TRIED_TO_LEAVE_MAP) > 0;
	}

	public void setTriedToLeaveMap(boolean triedToLeaveMap) {
		mem.put(MEMLOC_TRIED_TO_LEAVE_MAP, (byte) (triedToLeaveMap ? 1 : 0));
	}

	public int getPictureHeadId() {
		return mem.getUnsigned(MEMLOC_PICTURE_HEAD_ID);
	}

	public void setPictureHeadId(int pictureHeadId) {
		mem.put(MEMLOC_PICTURE_HEAD_ID, (byte) pictureHeadId);
	}

	public int getDivisionModulo() {
		return mem.getUnsigned(MEMLOC_DIVISION_MODULO);
	}

	public void setDivisionModulo(int divisionModulo) {
		mem.put(MEMLOC_DIVISION_MODULO, (byte) divisionModulo);
	}

	@Override
	public int getDungeonX() {
		return mem.getUnsigned(MEMLOC_DUNGEON_X);
	}

	public void setDungeonX(int dungeonX) {
		mem.put(MEMLOC_DUNGEON_X, (byte) dungeonX);
	}

	@Override
	public int getDungeonY() {
		return mem.getUnsigned(MEMLOC_DUNGEON_Y);
	}

	public void setDungeonY(int dungeonY) {
		mem.put(MEMLOC_DUNGEON_Y, (byte) dungeonY);
	}

	@Override
	public Direction getDungeonDir() {
		return Direction.withId(mem.getUnsigned(MEMLOC_DUNGEON_DIR));
	}

	public void setDungeonDir(Direction dungeonDir) {
		mem.put(MEMLOC_DUNGEON_DIR, (byte) dungeonDir.ordinal());
	}

	public int getWallType() {
		return mem.getUnsigned(MEMLOC_MAP_WALL_TYPE);
	}

	public void setWallType(int wallType) {
		mem.put(MEMLOC_MAP_WALL_TYPE, (byte) wallType);
	}

	public int getSquareInfo() {
		return mem.getUnsigned(MEMLOC_MAP_SQUARE_INFO);
	}

	public void setSquareInfo(int squareInfo) {
		mem.put(MEMLOC_MAP_SQUARE_INFO, (byte) squareInfo);
	}

	@Override
	public int getBackdropIndex() {
		return (getSquareInfo() & 0x80) >> 7;
	}

	public int readMemInt(EclArgument a) {
		if (!a.isMemAddress()) {
			return 0;
		}
		return a.isShortValue() ? mem.getUnsignedShort(a.valueAsInt()) : mem.getUnsigned(a.valueAsInt());
	}

	public int readMemInt(EclArgument base, int offset) {
		if (!base.isMemAddress()) {
			return 0;
		}
		return base.isShortValue() ? mem.getUnsignedShort(base.valueAsInt() + offset) : mem.getUnsigned(base.valueAsInt() + offset);
	}

	public void writeMemInt(EclArgument a, int value) {
		if (!a.isMemAddress()) {
			return;
		}
		if (a.isShortValue()) {
			mem.putShort(a.valueAsInt(), (short) value);
		} else {
			mem.put(a.valueAsInt(), (byte) value);
		}
	}

	public void writeMemInt(EclArgument base, int offset, int value) {
		if (!base.isMemAddress()) {
			return;
		}
		if (base.isShortValue()) {
			mem.putShort(base.valueAsInt() + offset, (short) value);
		} else {
			mem.put(base.valueAsInt() + offset, (byte) value);
		}
	}

	public void copyMemInt(EclArgument base, int offset, EclArgument target) {
		if (!base.isMemAddress() || !target.isMemAddress()) {
			return;
		}
		if (target.isShortValue()) {
			mem.putShort(target.valueAsInt(), (short) readMemInt(base, offset));
		} else {
			mem.put(target.valueAsInt(), (byte) readMemInt(base, offset));
		}
	}

	public GoldboxString readMemString(EclArgument a) {
		if (!a.isMemAddress()) {
			return null;
		}

		int pos = a.valueAsInt();
		int c = mem.getUnsigned(pos);
		while (c != 0) {
			c = mem.getUnsigned(++pos);
		}
		ByteBufferWrapper data = ByteBufferWrapper.allocateLE(pos - a.valueAsInt());
		data.put(mem.array(), a.valueAsInt(), pos - a.valueAsInt());
		return new EclString(data);
	}

	public void writeMemString(EclArgument a, GoldboxString value) {
		if (!a.isMemAddress()) {
			return;
		}

		mem.position(a.valueAsInt());
		for (int i = 0; i < value.getLength(); i++) {
			mem.put(value.getChar(i));
		}
		mem.put((byte) 0);
	}

	public void writeProgram(int startAddress, ByteBufferWrapper eclCode) {
		eclCode.rewind();
		mem.position(startAddress).put(eclCode);
	}
}
