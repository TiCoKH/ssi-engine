package engine.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class CodeBlock implements Comparable<CodeBlock> {

	private List<EclInstructionData> code;
	private int startAddress;
	private int endAddress;

	public CodeBlock(@Nonnull List<EclInstructionData> code, int startAddress, int endAddress) {
		this.code = code;
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	public List<EclInstructionData> getCode() {
		return new ArrayList<>(code);
	}

	public int getEndAddress() {
		return endAddress;
	}

	public int getStartAddress() {
		return startAddress;
	}

	@Override
	public int compareTo(CodeBlock o) {
		return startAddress - o.startAddress;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		CodeBlock other = (CodeBlock) obj;
		return startAddress == other.startAddress;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + startAddress;
		return result;
	}

	@Override
	public String toString() {
		return String.format("[%04X-%04X]", startAddress, endAddress);
	}
}
