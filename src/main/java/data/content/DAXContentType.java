package data.content;

public enum DAXContentType {
	_8X8D("8X8D[1-9].DAX"), //
	BACK("BACK[1-9].DAX"), //
	BIGPIC("BIGPIC[1-9].DAX"), //
	ECL("ECL[1-9].DAX"), //
	GEO("GEO[1-9].DAX"), //
	PIC("PIC[1-9].DAX"), //
	SPRIT("SPRIT[1-9].DAX"), //
	TITLE("TITLE.DAX"), //
	WALLDEF("WALLDEF[1-9].DAX");

	private String filePattern;

	private DAXContentType(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getFilePattern() {
		return filePattern;
	}
}
