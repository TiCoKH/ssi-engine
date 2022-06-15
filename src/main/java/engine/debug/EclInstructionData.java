package engine.debug;

public interface EclInstructionData extends Comparable<EclInstructionData> {

	int getPosition();

	int getSize();

	boolean isConditional();

	String getCodeline();

	@Override
	default int compareTo(EclInstructionData o) {
		return getPosition() - o.getPosition();
	}
}
