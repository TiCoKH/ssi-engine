package engine.script;

import static io.vavr.API.Seq;

import java.util.Objects;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import common.ByteBufferWrapper;

public class EclInstruction {
	private static Map<Integer, EclOpCode> OP_CODES;

	private final int position;
	private final int size;
	private final EclOpCode opCode;
	private final EclArgument[] arguments;
	private final Seq<EclArgument> dynArgs;

	private EclInstruction(int position, int size, EclOpCode opCode, EclArgument[] arguments,
		Seq<EclArgument> dynArgs) {

		this.position = position;
		this.size = size;
		this.opCode = opCode;
		this.arguments = arguments;
		this.dynArgs = dynArgs;
	}

	public static void configOpCodes(Map<Integer, EclOpCode> opCodes) {
		OP_CODES = opCodes;
	}

	public static EclInstruction parseNext(ByteBufferWrapper eclBlock) {
		int pos = eclBlock.position();
		int id = eclBlock.getUnsigned();
		EclOpCode opCode = OP_CODES.get(id).getOrNull();
		if (opCode == null) {
			System.err.println("Unknown opcode " + Integer.toHexString(id) + " at " + Integer.toHexString(pos));
		}

		int size = 1;

		EclArgument[] arguments = new EclArgument[opCode.getArgCount()];
		for (int i = 0; i < opCode.getArgCount(); i++) {
			arguments[i] = EclArgument.parseNext(eclBlock);
			size += arguments[i].getSize();
		}

		Seq<EclArgument> dynArgs = Array.empty();
		if (opCode.hasDynArgs()) {
			dynArgs = Array.range(0, arguments[opCode.getArgIndexDynArgs()].valueAsInt())
				.map(i -> EclArgument.parseNext(eclBlock));
			size += dynArgs.map(EclArgument::getSize).sum().intValue();
		}

		return new EclInstruction(pos, size, opCode, arguments, dynArgs);
	}

	public Seq<EclArgument> getDynArgs() {
		return dynArgs;
	}

	public int getPosition() {
		return position;
	}

	public EclOpCode getOpCode() {
		return opCode;
	}

	public EclArgument[] getArguments() {
		return arguments;
	}

	public int getSize() {
		return size;
	}

	public EclArgument getArgument(int index) {
		if (index < 0 || index >= arguments.length) {
			return null;
		}
		return arguments[index];
	}

	@Override
	public int hashCode() {
		return Objects.hash(opCode, position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EclInstruction)) {
			return false;
		}
		EclInstruction other = (EclInstruction) obj;
		return opCode == other.opCode && position == other.position;
	}

	@Override
	public String toString() {
		return opCode.name() + Seq(arguments).map(EclArgument::toString).mkString("(", ", ", ")")
			+ (opCode.hasDynArgs() ? dynArgs.map(EclArgument::toString).mkString("(", ", ", ")") : "");
	}
}
