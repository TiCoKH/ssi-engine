package data;

public enum ContentType {
	_8X8D("8X8D[1-9BC]?\\.(DAX|TLB)"), //
	BACK("BACK[1-9]?\\.(DAX|TLB)"), //
	BIGPIC("BIGPI[CX][1-9]?\\.(DAX|TLB)"), //
	BODY("BODY[1-9]?\\.DAX"), //
	ECL("ECL[0-9]?\\.(DAX|GLB)"), //
	GEO("GEO[0-9]?\\.(DAX|GLB)"), //
	HEAD("HEAD[1-9]?\\.DAX"), //
	MONCHA("MONST.GLB|MON[0-9]?CHA\\.(DAX|GLB)"), //
	PIC("PIC[1-9ABCDEF]?A?\\.(DAX|TLB)"), //
	SPRIT("SPRIT[1-9]?\\.(DAX|TLB)"), //
	TITLE("TITLE\\.(DAX|TLB)"), //
	WALLDEF("WALLDEF[1-9]?\\.DAX");

	private String filePattern;

	private ContentType(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getFilePattern() {
		return filePattern;
	}
}
