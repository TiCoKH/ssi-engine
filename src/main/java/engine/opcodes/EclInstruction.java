package engine.opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import common.ByteBufferWrapper;

public class EclInstruction {
	private static Map<Integer, EclOpCode> OP_CODES;

	private int position;
	private int size;
	private EclOpCode opCode;
	private EclArgument[] arguments;
	private List<EclArgument> dynArgs;

	private EclInstruction(int position, int size, EclOpCode opCode, EclArgument[] arguments, List<EclArgument> dynArgs) {
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
		EclOpCode opCode = OP_CODES.get(id);
		if (opCode == null) {
			System.err.println("Unknown opcode " + Integer.toHexString(id) + " at " + Integer.toHexString(pos));
		}

		int size = 1;

		EclArgument[] arguments = new EclArgument[opCode.getArgCount()];
		for (int i = 0; i < opCode.getArgCount(); i++) {
			arguments[i] = EclArgument.parseNext(eclBlock);
			size += arguments[i].getSize();
		}

		List<EclArgument> dynArgs = new ArrayList<>();
		if (opCode.hasDynArgs()) {
			for (int i = 0; i < arguments[opCode.getArgIndexDynArgs()].valueAsInt(); i++) {
				EclArgument dynArg = EclArgument.parseNext(eclBlock);
				dynArgs.add(dynArg);
				size += dynArg.getSize();
			}
		}

		return new EclInstruction(pos, size, opCode, arguments, dynArgs);
	}

	public List<EclArgument> getDynArgs() {
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
		return opCode.name() + "(" + String.join(", ", Arrays.asList(arguments).stream().map(EclArgument::toString).collect(Collectors.toList()))
			+ ")"
			+ (opCode.hasDynArgs() ? " (" + String.join(", ", dynArgs.stream().map(EclArgument::toString).collect(Collectors.toList())) + ")" : "");
	}
}
