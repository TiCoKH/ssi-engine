package engine.debug;

public interface EclInstructionData {

	int getPosition();

	int getSize();

	boolean isConditional();

	String getCodeline();
}
