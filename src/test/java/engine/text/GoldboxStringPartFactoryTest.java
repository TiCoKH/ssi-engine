package engine.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import shared.CustomGoldboxString;

public class GoldboxStringPartFactoryTest {

	@Test
	public void testCreateLineBreak() {
		assertNotNull(new GoldboxStringPartFactory().createLineBreak());
	}

	@Test
	public void testTextParsing() {
		assertEquals(67, //
			new GoldboxStringPartFactory().from(new CustomGoldboxString( //
				"%AYOU RECOGNIZE THE FAMILIAR FACE OF YOUR OLD FRIEND AMANITAS. " //
					+ "%E'OH, DEAR,' %AHE SAYS, %E'1... 2... 3... " //
					+ "AH, THEY ALL MADE IT! I WAS AFRAID I'D LEFT OUT A WORD OR TWO..."))
				.size());
	}

	@Test
	public void testTextParsingEndingInSpace() {
		assertEquals(22, //
			new GoldboxStringPartFactory().from(new CustomGoldboxString( //
				"YOU SHOW HIM THE LETTER FROM LAURANA AS HE BOWS RESPECTFULLY. ")).size());
	}

	@Test
	public void testTextParsingWithSimpleUmlauts() {
		assertEquals(54, //
			new GoldboxStringPartFactory('%', '&', '$', '"').from(new CustomGoldboxString( //
				"DIE NACHT DES FESTES! EURE GRUPPE HAT DIE KARAWANE DEN GANZEN WEG VON DER ZITADELLE ADBAR BIS NACH YARTAR ERFOLGREICH GESCH$TZT! BESSER NOCH, IHR WURDET BEZAHLT! KANN "))
				.size());
	}

	@Test
	public void testTextParsingWithSimpleUmlauts2() {
		assertEquals(21, //
			new GoldboxStringPartFactory('%', '&', '$', '"').from(new CustomGoldboxString( //
				"AUF DEM SCHILD $BER DER T$R STEHT GESCHRIEBEN: 'ZUM LEUCHTENDEN JUWEL'")) //
				.size());
	}
}
