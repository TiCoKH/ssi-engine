package engine.debug;

import java.util.Objects;

import javax.annotation.Nonnull;

public class CodeBlockConnection {
	private CodeBlock from;
	private CodeBlock to;
	private CodeBlockConnectionType type;

	private CodeBlockConnection(@Nonnull CodeBlock from, @Nonnull CodeBlock to, @Nonnull CodeBlockConnectionType type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public static void register(@Nonnull CodeBlock from, @Nonnull CodeBlock to, @Nonnull CodeBlockConnectionType type) {
		CodeBlockConnection c = new CodeBlockConnection(from, to, type);
		from.addOutgoing(c);
		to.addIncoming(c);
	}

	public void unregister() {
		from.removeOutgoing(this);
		to.removeIncoming(this);
	}

	public CodeBlock getFrom() {
		return from;
	}

	public CodeBlock getTo() {
		return to;
	}

	public CodeBlockConnectionType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeBlockConnection)) {
			return false;
		}
		CodeBlockConnection other = (CodeBlockConnection) obj;
		return Objects.equals(from, other.from) && Objects.equals(to, other.to) && type == other.type;
	}

	@Override
	public String toString() {
		return "[from=" + from + ", to=" + to + ", type=" + type + "]";
	}
}
