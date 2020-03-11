package engine.rulesystem;

import engine.rulesystem.buckrogers.BuckRogersFlavor;
import engine.rulesystem.krynn.KrynnFlavor;
import engine.rulesystem.standard.StandardFlavor;

public enum Flavors {
	KRYNN(new KrynnFlavor()), //
	BUCK_ROGERS(new BuckRogersFlavor()), //
	STANDARD(new StandardFlavor());

	private final Flavor flavor;

	private Flavors(Flavor flavor) {
		this.flavor = flavor;
	}

	public Flavor getFlavor() {
		return flavor;
	}
}
