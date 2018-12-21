package data.content;

public enum DAXContentType {
	_8X8D("8X8D[1-9BC]?\\.(DAX|TLB)"), //
	BACK("BACK[1-9]?\\.(DAX|TLB)"), //
	BIGPIC("BIGPI[CX][1-9]?\\.(DAX|TLB)"), //
	BODY("BODY[1-9]?\\.DAX"), //
	ECL("ECL[1-9]?\\.(DAX|GLB)"), //
	GEO("GEO[1-9]?\\.(DAX|GLB)"), //
	HEAD("HEAD[1-9]?\\.DAX"), //
	PIC("PIC[1-9ABCDEF]?\\.(DAX|TLB)"), //
	SPRIT("SPRIT[1-9]?\\.(DAX|TLB)"), //
	TITLE("TITLE\\.(DAX|TLB)"), //
	WALLDEF("WALLDEF[1-9]?\\.DAX");

	private String filePattern;

	private DAXContentType(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getFilePattern() {
		return filePattern;
	}
}
