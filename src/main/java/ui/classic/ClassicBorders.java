package ui.classic;

import static ui.BorderSymbols.E3;
import static ui.BorderSymbols.EM;
import static ui.BorderSymbols.HO;
import static ui.BorderSymbols.LL;
import static ui.BorderSymbols.LR;
import static ui.BorderSymbols.N3;
import static ui.BorderSymbols.OH;
import static ui.BorderSymbols.OV;
import static ui.BorderSymbols.S3;
import static ui.BorderSymbols.UL;
import static ui.BorderSymbols.UR;
import static ui.BorderSymbols.VE;
import static ui.BorderSymbols.W3;

import ui.BorderSymbols;

public enum ClassicBorders {

	SCREEN(new BorderSymbols[][] { //
		{ UL, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, UR }, // 0
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 1
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 2
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 3
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 4
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 5
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 6
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 7
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 8
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 9
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 10
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 11
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 12
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 13
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 14
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 15
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 16
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 17
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 18
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 19
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 20
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 21
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 22
		{ LL, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, LR }, // 23
	}), //
	GAME(new BorderSymbols[][] { //
		{ UL, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, S3, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, UR }, // 0
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 1
		{ OV, EM, UL, HO, OH, HO, HO, HO, HO, HO, HO, HO, OH, HO, UR, EM, OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 2
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 3
		{ VE, EM, OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 4
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 5
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 6
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 7
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 8
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 9
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 10
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 11
		{ VE, EM, OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 12
		{ VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 13
		{ OV, EM, LL, HO, OH, HO, HO, HO, HO, HO, HO, HO, OH, HO, LR, EM, OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 14
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 15
		{ E3, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, N3, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, W3 }, // 16
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 17
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 18
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 19
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 20
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 21
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 22
		{ LL, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, LR }, // 23
	}), //
	BIGPIC(new BorderSymbols[][] { //
		{ UL, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, UR }, // 0
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 1
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 2
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 3
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 4
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 5
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 6
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 7
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 8
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 9
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 10
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 11
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 12
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 13
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 14
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 15
		{ E3, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, W3 }, // 16
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 17
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 18
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 19
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 20
		{ OV, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, OV }, // 21
		{ VE, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, EM, VE }, // 22
		{ LL, HO, OH, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, HO, OH, HO, LR }, // 23
	}); //

	private BorderSymbols[][] symbols;

	private ClassicBorders(BorderSymbols[][] symbols) {
		this.symbols = symbols;
	}

	public BorderSymbols[][] getSymbols() {
		return symbols;
	}
}
