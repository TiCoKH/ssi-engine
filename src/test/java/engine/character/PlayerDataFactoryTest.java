package engine.character;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assume;
import org.junit.Test;

import common.FileMap;
import data.Resource;
import data.ResourceLoader;
import data.character.AbstractCharacter;
import engine.EngineConfiguration;

public class PlayerDataFactoryTest {

	@Test
	public void testLoadCharacterInt() throws Exception {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/ECL1.DAX");
		Assume.assumeTrue(f.exists());

		FileMap fm = new FileMap(f.getParent());
		EngineConfiguration cfg = new EngineConfiguration(fm);
		ResourceLoader res = new ResourceLoader(fm);
		PlayerDataFactory fac = new PlayerDataFactory(res, cfg);
		AbstractCharacter.configValues(cfg.getCharacterValues());
		Resource<? extends AbstractCharacter> character = fac.loadCharacter(60);
		assertTrue(character.isPresentAndSuccess());
	}

}
