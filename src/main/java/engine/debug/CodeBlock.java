package engine.debug;

import static io.vavr.API.Set;
import static io.vavr.API.SortedSet;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;

public class CodeBlock implements Comparable<CodeBlock> {

	private Set<CodeBlockConnection> incoming = Set();
	private Set<CodeBlockConnection> outgoing = Set();

	private final SortedSet<EclInstructionData> code;
	private final int codeBase;

	public CodeBlock(@Nonnull SortedSet<EclInstructionData> code, int codeBase) {
		this.code = code;
		this.codeBase = codeBase;
	}

	public void addIncoming(@Nonnull CodeBlockConnection conn) {
		incoming = incoming.add(conn);
	}

	public void addOutgoing(@Nonnull CodeBlockConnection conn) {
		outgoing = outgoing.add(conn);
	}

	public Optional<CodeBlockConnection> getIncoming(CodeBlockConnectionType type) {
		return incoming.find(c -> c.getType() == type).toJavaOptional();
	}

	public Optional<CodeBlockConnection> getOutgoing(CodeBlockConnectionType type) {
		return outgoing.find(c -> c.getType() == type).toJavaOptional();
	}

	public void removeIncoming(@Nonnull CodeBlockConnection conn) {
		incoming = incoming.remove(conn);
	}

	public void removeOutgoing(@Nonnull CodeBlockConnection conn) {
		outgoing = outgoing.remove(conn);
	}

	public int getIncomingCount() {
		return incoming.size();
	}

	public int getOutgoingCount() {
		return outgoing.size();
	}

	public SortedSet<EclInstructionData> getCode() {
		return code;
	}

	public EclInstructionData getFirstInst() {
		return code.head();
	}

	public EclInstructionData getLastInst() {
		return code.last();
	}

	public SortedSet<CodeBlock> splitAfter(EclInstructionData inst, CodeBlockConnectionType type) {
		if (!code.contains(inst) || getLastInst().equals(inst)) {
			return SortedSet(this);
		}

		SortedSet<EclInstructionData> newBlock = code.dropUntil(data -> data.equals(inst));
		newBlock = newBlock.remove(newBlock.head());

		final CodeBlock first = new CodeBlock(code.removeAll(newBlock), codeBase);
		final CodeBlock second = new CodeBlock(newBlock, codeBase);

		replaceIncoming(this, first);
		replaceOutcoming(this, second);
		CodeBlockConnection.register(first, second, type);

		return SortedSet(first, second);
	}

	public CodeBlock merge(CodeBlock toMerge) {
		final CodeBlock replacement = new CodeBlock(code.addAll(toMerge.code), codeBase);
		replacement.replace(this);
		return replacement;
	}

	public void replace(CodeBlock toBeReplaced) {
		replaceIncoming(toBeReplaced, this);
		replaceOutcoming(toBeReplaced, this);
	}

	private static void replaceIncoming(CodeBlock oldBlock, CodeBlock newBlock) {
		oldBlock.incoming.forEach(c -> {
			c.unregister();
			if (!newBlock.equals(c.getFrom()))
				CodeBlockConnection.register(c.getFrom(), newBlock, c.getType());
		});
	}

	private static void replaceOutcoming(CodeBlock oldBlock, CodeBlock newBlock) {
		oldBlock.outgoing.forEach(c -> {
			c.unregister();
			if (!newBlock.equals(c.getTo()))
				CodeBlockConnection.register(newBlock, c.getTo(), c.getType());
		});
	}

	public void unregister() {
		unregisterIncoming();
		unregisterOutcoming();
	}

	public void unregisterIncoming() {
		incoming.forEach(CodeBlockConnection::unregister);
	}

	public void unregisterOutcoming() {
		outgoing.forEach(CodeBlockConnection::unregister);
	}

	public CodeBlock replace(EclinstructionWrapper current, EclInstructionCompareIf newInst) {
		final CodeBlock replacement = new CodeBlock(code.replace(current, newInst), codeBase);
		replacement.replace(this);
		return replacement;
	}

	public CodeBlock remove(EclinstructionWrapper inst) {
		final CodeBlock replacement = new CodeBlock(code.remove(inst), codeBase);
		replacement.replace(this);
		return replacement;
	}

	public int getEndAddress() {
		return codeBase + getLastInst().getPosition() + getLastInst().getSize();
	}

	public int getStartAddress() {
		return codeBase + getFirstInst().getPosition();
	}

	@Override
	public int compareTo(CodeBlock o) {
		return getStartAddress() - o.getStartAddress();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		CodeBlock other = (CodeBlock) obj;
		return getStartAddress() == other.getStartAddress();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getStartAddress();
		return result;
	}

	@Override
	public String toString() {
		return String.format("[%04X-%04X]", getStartAddress(), getEndAddress());
	}
}
