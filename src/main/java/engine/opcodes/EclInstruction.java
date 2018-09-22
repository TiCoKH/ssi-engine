package engine.opcodes;

import static engine.opcodes.EclOpCode.MENU_HORIZONTAL;
import static engine.opcodes.EclOpCode.ON_GOSUB;
import static engine.opcodes.EclOpCode.ON_GOTO;
import static engine.opcodes.EclOpCode.SELECT_ACTION;
import static engine.opcodes.EclOpCode.TREASURE;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

public class EclInstruction {
	private static final Map<Integer, EclOpCode> OP_CODES = Arrays.asList(EclOpCode.values()).stream()
		.collect(Collectors.toMap(EclOpCode::getId, Function.identity()));
	private static final List<EclOpCode> OP_CODE_DYNARGS = ImmutableList.of(ON_GOTO, ON_GOSUB, TREASURE, MENU_HORIZONTAL, SELECT_ACTION);

	private int position;
	private EclOpCode opCode;
	private EclArgument[] arguments;
	private List<EclArgument> dynArgs;

	private EclInstruction(int position, EclOpCode opCode, EclArgument[] arguments, List<EclArgument> dynArgs) {
		this.position = position;
		this.opCode = opCode;
		this.arguments = arguments;
		this.dynArgs = dynArgs;
	}

	public static EclInstruction parseNext(ByteBuffer eclBlock) {
		int pos = eclBlock.position();
		int id = eclBlock.get() & 0xFF;
		EclOpCode opCode = OP_CODES.get(id);
		if (opCode == null) {
			System.err.println("Unknown opcode " + Integer.toHexString(id) + " at " + Integer.toHexString(pos));
		}

		EclArgument[] arguments = new EclArgument[opCode.getArgCount()];
		for (int i = 0; i < opCode.getArgCount(); i++) {
			arguments[i] = EclArgument.parseNext(eclBlock);
		}
		List<EclArgument> dynArgs = new ArrayList<>();
		if (OP_CODE_DYNARGS.contains(opCode)) {
			for (int i = 0; i < arguments[1].valueAsInt(); i++) {
				dynArgs.add(EclArgument.parseNext(eclBlock));
			}
		}
		return new EclInstruction(pos, opCode, arguments, dynArgs);
	}

	public boolean hasDynArgs() {
		return OP_CODE_DYNARGS.contains(opCode);
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

	public EclArgument getArgument(int index) {
		if (index < 0 || index >= arguments.length) {
			return null;
		}
		return arguments[index];
	}

	@Override
	public String toString() {
		return opCode.getDescription() + "("
			+ String.join(", ", Arrays.asList(arguments).stream().map(EclArgument::toString).collect(Collectors.toList())) + ")"
			+ (hasDynArgs() ? " (" + String.join(", ", dynArgs.stream().map(EclArgument::toString).collect(Collectors.toList())) + ")" : "");
	}
}
