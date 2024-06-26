package engine;

import static engine.EngineAddress.COMBAT_RESULT;
import static engine.EngineAddress.DIVISION_MODULO;
import static engine.EngineAddress.DOOR_FLAGS;
import static engine.EngineAddress.DUNGEON_DIR;
import static engine.EngineAddress.DUNGEON_VALUE;
import static engine.EngineAddress.DUNGEON_X;
import static engine.EngineAddress.DUNGEON_Y;
import static engine.EngineAddress.FOR_LOOP_COUNT;
import static engine.EngineAddress.INDEX_OF_SEL_PC;
import static engine.EngineAddress.LAST_DUNGEON_X;
import static engine.EngineAddress.LAST_DUNGEON_Y;
import static engine.EngineAddress.LAST_ECL;
import static engine.EngineAddress.MAP_SQUARE_INFO;
import static engine.EngineAddress.MAP_WALL_TYPE;
import static engine.EngineAddress.MOVEMENT_BLOCK;
import static engine.EngineAddress.OVERLAND_CITY;
import static engine.EngineAddress.OVERLAND_DIR;
import static engine.EngineAddress.OVERLAND_X;
import static engine.EngineAddress.OVERLAND_Y;
import static engine.EngineAddress.PARTY_COUNT;
import static engine.EngineAddress.PICTURE_HEAD_ID;
import static engine.EngineAddress.SAVED_TEMP_START;
import static engine.EngineAddress.SEARCH_FLAGS;
import static engine.EngineAddress.SEL_PC_START;
import static engine.EngineAddress.SKY_COLOR_INDOORS;
import static engine.EngineAddress.SKY_COLOR_OUTDOORS;
import static engine.EngineAddress.TEMP_START;
import static engine.EngineAddress.TEXT_COLOR;
import static engine.EngineAddress.TRIED_TO_LEAVE_MAP;
import static io.vavr.API.Seq;
import static shared.GameFeature.EXTENDED_DUNGEON;

import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;

import common.ByteBufferWrapper;
import data.dungeon.DungeonMap.Direction;
import engine.character.CharacterSheetImpl;
import engine.script.EclArgument;
import engine.script.EclString;
import shared.GoldboxString;
import shared.ViewDungeonPosition;
import shared.ViewGlobalData;
import shared.ViewOverlandPosition;
import shared.ViewSpacePosition;
import shared.party.PartyMember;

public class VirtualMemory implements ViewDungeonPosition, ViewSpacePosition, ViewOverlandPosition, ViewGlobalData {
	public static final int MEMLOC_CELESTIAL_POS_START = 0x4B85;
	public static final int MEMLOC_SPACE_X = 0x4BBE;
	public static final int MEMLOC_SPACE_Y = 0x4BBF;
	public static final int MEMLOC_SPACE_DIR = 0x4BC0;
	private final int MEMLOC_OVERLAND_DIR;
	private final int MEMLOC_OVERLAND_X;
	private final int MEMLOC_OVERLAND_Y;
	private final int MEMLOC_DUNGEON_VALUE;
	private final int MEMLOC_LAST_DUNGEON_X;
	private final int MEMLOC_LAST_DUNGEON_Y;
	private final int MEMLOC_LAST_ECL;
	public static final int MEMLOC_ENGINE_CONF_GAME_SPEED = 0x4BFC;
	private final int MEMLOC_SKY_COLOR_OUTDOORS;
	private final int MEMLOC_SKY_COLOR_INDOORS;
	public static final int MEMLOC_MED_SUPPLIES = 0x4C63;
	private final int MEMLOC_OVERLAND_CITY;
	private final int MEMLOC_FOR_LOOP_COUNT;
	private final int MEMLOC_DOOR_FLAGS;
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
	private final int MEMLOC_SEL_PC_START;
	private final int MEMLOC_INDEX_OF_SEL_PC;
	private final int MEMLOC_COMBAT_RESULT;
	private final int MEMLOC_MOVEMENT_BLOCK;
	private final int MEMLOC_SEARCH_FLAGS;
	private final int MEMLOC_TRIED_TO_LEAVE_MAP;
	private final int MEMLOC_PICTURE_HEAD_ID;
	private final int MEMLOC_PARTY_COUNT;
	private final int MEMLOC_DIVISION_MODULO;
	private final int MEMLOC_TEXT_COLOR;
	private final int MEMLOC_DUNGEON_X;
	private final int MEMLOC_DUNGEON_Y;
	private final int MEMLOC_DUNGEON_DIR;
	private final int MEMLOC_MAP_WALL_TYPE;
	private final int MEMLOC_MAP_SQUARE_INFO;

	public static final int MEMLOC_CURRENT_ECL = 0xFFF0;
	public static final int MEMLOC_AREA_START = 0xFFF1;
	public static final int MEMLOC_AREA_DECO_START = 0xFFF4;

	private static final int[] CELESTIAL_INITIAL_X = new int[] { 12, 12, 9, 9, 21, 18, 12, 6, 2, 4, 8, 14, 20 };
	private static final int[] CELESTIAL_INITIAL_Y = new int[] { 10, 9, 8, 15, 11, 17, 20, 18, 13, 3, 1, 1, 7 };
	private ByteBufferWrapper mem;

	private Seq<CharacterSheetImpl> members = Seq();
	private int loadedCharacter = 0;

	private int menuChoice;
	private GoldboxString input;

	private EngineConfiguration cfg;

	public VirtualMemory(@Nonnull EngineConfiguration cfg) {
		this.cfg = cfg;

		mem = ByteBufferWrapper.allocateLE(0x10000);

		MEMLOC_OVERLAND_DIR = cfg.getEngineAddress(OVERLAND_DIR);
		MEMLOC_OVERLAND_X = cfg.getEngineAddress(OVERLAND_X);
		MEMLOC_OVERLAND_Y = cfg.getEngineAddress(OVERLAND_Y);
		MEMLOC_DUNGEON_VALUE = cfg.getEngineAddress(DUNGEON_VALUE);
		MEMLOC_LAST_DUNGEON_X = cfg.getEngineAddress(LAST_DUNGEON_X);
		MEMLOC_LAST_DUNGEON_Y = cfg.getEngineAddress(LAST_DUNGEON_Y);
		MEMLOC_LAST_ECL = cfg.getEngineAddress(LAST_ECL);
		MEMLOC_SKY_COLOR_OUTDOORS = cfg.getEngineAddress(SKY_COLOR_OUTDOORS);
		MEMLOC_SKY_COLOR_INDOORS = cfg.getEngineAddress(SKY_COLOR_INDOORS);
		MEMLOC_OVERLAND_CITY = cfg.getEngineAddress(OVERLAND_CITY);
		MEMLOC_FOR_LOOP_COUNT = cfg.getEngineAddress(FOR_LOOP_COUNT);
		MEMLOC_DOOR_FLAGS = cfg.getEngineAddress(DOOR_FLAGS);
		MEMLOC_SEL_PC_START = cfg.getEngineAddress(SEL_PC_START);
		MEMLOC_INDEX_OF_SEL_PC = cfg.getEngineAddress(INDEX_OF_SEL_PC);
		MEMLOC_COMBAT_RESULT = cfg.getEngineAddress(COMBAT_RESULT);
		MEMLOC_MOVEMENT_BLOCK = cfg.getEngineAddress(MOVEMENT_BLOCK);
		MEMLOC_SEARCH_FLAGS = cfg.getEngineAddress(SEARCH_FLAGS);
		MEMLOC_TRIED_TO_LEAVE_MAP = cfg.getEngineAddress(TRIED_TO_LEAVE_MAP);
		MEMLOC_PICTURE_HEAD_ID = cfg.getEngineAddress(PICTURE_HEAD_ID);
		MEMLOC_PARTY_COUNT = cfg.getEngineAddress(PARTY_COUNT);
		MEMLOC_DIVISION_MODULO = cfg.getEngineAddress(DIVISION_MODULO);
		MEMLOC_TEXT_COLOR = cfg.getEngineAddress(TEXT_COLOR);
		MEMLOC_DUNGEON_X = cfg.getEngineAddress(DUNGEON_X);
		MEMLOC_DUNGEON_Y = cfg.getEngineAddress(DUNGEON_Y);
		MEMLOC_DUNGEON_DIR = cfg.getEngineAddress(DUNGEON_DIR);
		MEMLOC_MAP_WALL_TYPE = cfg.getEngineAddress(MAP_WALL_TYPE);
		MEMLOC_MAP_SQUARE_INFO = cfg.getEngineAddress(MAP_SQUARE_INFO);

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

	public void clearTemps() {
		int savedTempStart = cfg.getEngineAddress(SAVED_TEMP_START);
		for (int i = savedTempStart; i < savedTempStart + 0x20; i++) {
			mem.put(i, (byte) 0);
		}
		int tempStart = cfg.getEngineAddress(TEMP_START);
		for (int i = tempStart; i < tempStart + 0xA; i++) {
			mem.put(i, (byte) 0);
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
	public Direction getOverlandDir() {
		return Direction.withId(mem.getUnsigned(MEMLOC_OVERLAND_DIR));
	}

	public void setOverlandDir(Direction overlandDir) {
		mem.put(MEMLOC_OVERLAND_DIR, (byte) overlandDir.ordinal());
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

	public int getDungeonValue() {
		return mem.getUnsigned(MEMLOC_DUNGEON_VALUE);
	}

	public void setDungeonValue(int dungeonValue) {
		mem.put(MEMLOC_DUNGEON_VALUE, (byte) dungeonValue);
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

	public int getOverlandCity() {
		return mem.getUnsigned(MEMLOC_OVERLAND_CITY);
	}

	public int getForLoopCount() {
		return mem.getUnsigned(MEMLOC_FOR_LOOP_COUNT);
	}

	public void setForLoopCount(int loopCount) {
		mem.put(MEMLOC_FOR_LOOP_COUNT, (byte) loopCount);
	}

	public int getDoorFlags() {
		return mem.getUnsigned(MEMLOC_DOOR_FLAGS);
	}

	public void setDoorFlags(int doorFlags) {
		mem.put(MEMLOC_DOOR_FLAGS, (byte) doorFlags);
	}

	@Override
	public int getExtendedDungeonX() {
		if (!cfg.isUsingFeature(EXTENDED_DUNGEON)) {
			return 255;
		}
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

	@Override
	public int getSelectedPartyMember() {
		return mem.getUnsigned(MEMLOC_INDEX_OF_SEL_PC);
	}

	@Override
	public void setSelectedPartyMember(int index) {
		if (index < 0 || index >= members.size()) {
			return;
		}
		setLoadedCharacter(index);
		mem.put(MEMLOC_INDEX_OF_SEL_PC, (byte) index);
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

	public int getSearchFlags() {
		return mem.getUnsigned(MEMLOC_SEARCH_FLAGS);
	}

	@Override
	public boolean getSearchFlagsIsSearchActive() {
		return (getSearchFlags() & 1) > 0;
	}

	public void setSearchFlagsToggleSearchMode() {
		mem.put(MEMLOC_SEARCH_FLAGS, (byte) (getSearchFlags() ^ 1));
	}

	public void setSearchFlagsTurnLookModeOn() {
		mem.put(MEMLOC_SEARCH_FLAGS, (byte) (getSearchFlags() | 2));
	}

	public void setSearchFlagsTurnLookModeOff() {
		mem.put(MEMLOC_SEARCH_FLAGS, (byte) (getSearchFlags() & 1));
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

	public int getPartyCount() {
		return mem.getUnsigned(MEMLOC_PARTY_COUNT);
	}

	public void setPartyCount(int partyCount) {
		mem.put(MEMLOC_PARTY_COUNT, (byte) partyCount);
	}

	public int getDivisionModulo() {
		return mem.getUnsigned(MEMLOC_DIVISION_MODULO);
	}

	public void setDivisionModulo(int divisionModulo) {
		mem.put(MEMLOC_DIVISION_MODULO, (byte) divisionModulo);
	}

	public int getTextColor() {
		return mem.getUnsigned(MEMLOC_TEXT_COLOR);
	}

	public void setTextColor(int textColor) {
		mem.put(MEMLOC_TEXT_COLOR, (byte) textColor);
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

	public boolean isPartyFull() {
		return getPartyMemberCount() >= 6;
	}

	public void addPartyMember(@Nonnull CharacterSheetImpl member) {
		members = members.append(member);
		setPartyCount(members.size());
	}

	public void removePartyMember(@Nonnull CharacterSheetImpl member) {
		int memberIndex = members.indexOf(member);
		if (memberIndex != -1) {
			members = members.remove(member);
			setPartyCount(members.size());
			if (memberIndex <= getSelectedPartyMember()) {
				int newIndex = getSelectedPartyMember() - 1;
				if (newIndex < 0) {
					newIndex = 0;
				}
				setSelectedPartyMember(newIndex);
			}
		}
	}

	public void clearParty() {
		members = Seq();
		setPartyCount(members.size());
	}

	@Override
	public boolean hasPartyMembers() {
		return !members.isEmpty();
	}

	@Override
	public int getPartyMemberCount() {
		return members.size();
	}

	@Override
	public PartyMember getPartyMember(int index) {
		return members.get(index);
	}

	public CharacterSheetImpl getPartyMemberAsCharacterSheet(int index) {
		return members.get(index);
	}

	private boolean isCharacterAddress(int address) {
		return MEMLOC_SEL_PC_START <= address && address < (MEMLOC_SEL_PC_START + 0x1FF);
	}

	public int readMemInt(EclArgument a) {
		if (!a.isMemAddress()) {
			return 0;
		}
		return readMemInt(a.valueAsInt(), a.isShortValue());
	}

	public int readMemInt(EclArgument base, int offset) {
		if (!base.isMemAddress()) {
			return 0;
		}
		return readMemInt(base.valueAsInt() + offset, base.isShortValue());
	}

	private int readMemInt(int address, boolean isShort) {
		if (isCharacterAddress(address)) {
			return members.get(getLoadedCharacter()).getCharacter().readValue(address - MEMLOC_SEL_PC_START, isShort);
		}
		return isShort ? mem.getUnsignedShort(address) : mem.getUnsigned(address);
	}

	public void writeMemInt(EclArgument a, int value) {
		if (!a.isMemAddress()) {
			return;
		}
		writeMemInt(a.valueAsInt(), value, a.isShortValue());
	}

	public void writeMemInt(EclArgument base, int offset, int value) {
		if (!base.isMemAddress()) {
			return;
		}
		writeMemInt(base.valueAsInt() + offset, value, base.isShortValue());
	}

	public void copyMemInt(EclArgument base, int offset, EclArgument target) {
		if (!base.isMemAddress() || !target.isMemAddress()) {
			return;
		}
		writeMemInt(target.valueAsInt(), readMemInt(base, offset), target.isShortValue());
	}

	public void writeMemInt(int address, int value, boolean isShort) {
		if (isCharacterAddress(address)) {
			members.get(getLoadedCharacter()).getCharacter().writeValue(address - MEMLOC_SEL_PC_START, value, isShort);
		} else if (isShort) {
			mem.putShort(address, (short) value);
		} else {
			mem.put(address, (byte) value);
		}
	}

	public GoldboxString readMemString(EclArgument a) {
		if (!a.isMemAddress()) {
			return null;
		}
		if (isCharacterAddress(a.valueAsInt())) {
			return members.get(getLoadedCharacter()).getName();
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

	public int getLoadedCharacter() {
		return loadedCharacter;
	}

	public void setLoadedCharacter(int index) {
		if (index < 0 || index >= members.size()) {
			return;
		}
		this.loadedCharacter = index;
	}

	public void writePartyMember(int index, @Nonnull FileChannel fc) throws IOException {
		if (index < 0 || index >= members.size()) {
			return;
		}
		members.get(index).getCharacter().writeTo(fc);
	}
}
