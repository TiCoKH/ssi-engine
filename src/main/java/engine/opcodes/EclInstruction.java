package engine.opcodes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EclInstruction {
	private EclOpCode opCode;
	private EclArgument[] arguments;

	private static final Map<Integer, EclOpCode> OP_CODES = Arrays.asList(EclOpCode.values()).stream()
			.collect(Collectors.toMap(EclOpCode::getId, Function.identity()));

	private EclInstruction(EclOpCode opCode, EclArgument[] arguments) {
		this.opCode = opCode;
		this.arguments = arguments;
	}

	public static EclInstruction parseNext(ByteBuffer eclBlock) {
		int id = eclBlock.get() & 0xFF;
		EclOpCode opCode = OP_CODES.get(id);

		EclArgument[] arguments = new EclArgument[opCode.getArgCount()];
		for (int i = 0; i < opCode.getArgCount(); i++) {
			arguments[i] = EclArgument.parseNext(eclBlock);
		}
		return new EclInstruction(opCode, arguments);
	}

	public EclOpCode getOpCode() {
		return opCode;
	}

	public EclArgument[] getArguments() {
		return arguments;
	}

	public EclArgument getArgument(int index) {
		if (index < 0 || index >= arguments.length) {
			return null;
		}
		return arguments[index];
	}

	@Override
	public String toString() {
		return opCode.getDescription() + "("
				+ String.join(",",
						Arrays.asList(arguments).stream().map(EclArgument::toString).collect(Collectors.toList()))
				+ ")";
	}
}
