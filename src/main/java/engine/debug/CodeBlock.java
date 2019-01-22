package engine.debug;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public class CodeBlock implements Comparable<CodeBlock> {

	private List<EclInstructionData> code;
	private int startAddress;
	private int endAddress;

	private Set<CodeBlockConnection> incoming = new HashSet<>();
	private Set<CodeBlockConnection> outgoing = new HashSet<>();

	public CodeBlock(@Nonnull List<EclInstructionData> code, int startAddress, int endAddress) {
		this.code = code;
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	public void addIncoming(@Nonnull CodeBlockConnection conn) {
		incoming.add(conn);
	}

	public void addOutgoing(@Nonnull CodeBlockConnection conn) {
		outgoing.add(conn);
	}

	public Optional<CodeBlockConnection> getIncoming(CodeBlockConnectionType type) {
		return incoming.stream().filter(c -> c.getType() == type).findFirst();
	}

	public Optional<CodeBlockConnection> getOutgoing(CodeBlockConnectionType type) {
		return outgoing.stream().filter(c -> c.getType() == type).findFirst();
	}

	public void removeIncoming(@Nonnull CodeBlockConnection conn) {
		incoming.remove(conn);
	}

	public void removeOutgoing(@Nonnull CodeBlockConnection conn) {
		outgoing.remove(conn);
	}

	public int getIncomingCount() {
		return incoming.size();
	}

	public int getOutgoingCount() {
		return outgoing.size();
	}

	public List<EclInstructionData> getCode() {
		return new ArrayList<>(code);
	}

	public EclInstructionData getLastInst() {
		return code.get(code.size() - 1);
	}

	public CodeBlock splitAfter(EclInstructionData inst, int codeBase) {
		int index = code.indexOf(inst);
		if (index == -1 || index == code.size() - 1) {
			return null;
		}

		List<EclInstructionData> newBlock = new ArrayList<>(code.subList(index + 1, code.size()));

		code.removeAll(newBlock);
		endAddress = codeBase + inst.getPosition() + inst.getSize();

		EclInstructionData lastInst = newBlock.get(newBlock.size() - 1);
		return new CodeBlock(newBlock, codeBase + newBlock.get(0).getPosition(), codeBase + lastInst.getPosition() + lastInst.getSize());
	}

	public void merge(CodeBlock toMerge) {
		code.addAll(toMerge.code);
		endAddress = toMerge.endAddress;

		replace(toMerge);
	}

	public void replace(CodeBlock toBeReplaced) {
		new HashSet<>(toBeReplaced.incoming).stream().forEach(CodeBlockConnection::unregister);
		new HashSet<>(toBeReplaced.outgoing).stream().forEach(c -> {
			CodeBlock to = c.getTo();
			CodeBlockConnectionType type = c.getType();
			c.unregister();
			CodeBlockConnection.register(this, to, type);
		});
	}

	public void setCode(@Nonnull List<EclInstructionData> code) {
		this.code = code;
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
