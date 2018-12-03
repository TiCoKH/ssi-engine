package engine.debug;

import static engine.opcodes.EclOpCode.EXIT;
import static engine.opcodes.EclOpCode.GOTO;
import static engine.opcodes.EclOpCode.IF_EQUALS;
import static engine.opcodes.EclOpCode.IF_GREATER;
import static engine.opcodes.EclOpCode.IF_GREATER_EQUALS;
import static engine.opcodes.EclOpCode.IF_LESS;
import static engine.opcodes.EclOpCode.IF_LESS_EQUALS;
import static engine.opcodes.EclOpCode.IF_NOT_EQUALS;
import static engine.opcodes.EclOpCode.NEW_ECL;
import static engine.opcodes.EclOpCode.ON_GOTO;
import static engine.opcodes.EclOpCode.RETURN;
import static engine.opcodes.EclOpCode.STOP_MOVE_23;
import static engine.opcodes.EclOpCode.STOP_MOVE_42;

import java.util.List;

import com.google.common.collect.ImmutableList;

import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class EclinstructionWrapper implements EclInstructionData {
	private static final List<EclOpCode> OP_CODE_STOP = ImmutableList.of(EXIT, STOP_MOVE_23, STOP_MOVE_42, GOTO, ON_GOTO, RETURN, NEW_ECL);
	private static final List<EclOpCode> OP_CODE_IF = ImmutableList.of(IF_EQUALS, IF_GREATER, IF_GREATER_EQUALS, IF_LESS, IF_LESS_EQUALS,
		IF_NOT_EQUALS);

	private EclInstruction inst;
	private boolean conditional;

	public EclinstructionWrapper(EclInstruction inst, boolean conditional) {
		this.inst = inst;
		this.conditional = conditional;
	}

	@Override
	public int getPosition() {
		return inst.getPosition();
	}

	@Override
	public int getSize() {
		return inst.getSize();
	}

	@Override
	public boolean isConditional() {
		return conditional;
	}

	@Override
	public String getCodeline() {
		return toString();
	}

	public boolean isBlockFinisher() {
		return OP_CODE_STOP.contains(inst.getOpCode());
	}

	public boolean isIf() {
		return OP_CODE_IF.contains(inst.getOpCode());
	}

	public EclArgument getArgument(int index) {
		return inst.getArgument(index);
	}

	public EclArgument[] getArguments() {
		return inst.getArguments();
	}

	public List<EclArgument> getDynArgs() {
		return inst.getDynArgs();
	}

	public EclOpCode getOpCode() {
		return inst.getOpCode();
	}

	@Override
	public String toString() {
		return inst.toString();
	}
}
