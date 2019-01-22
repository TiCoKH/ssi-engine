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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

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

	private Map<Integer, String> argumentNames = new HashMap<>();

	private Optional<EclArgument> lArgBase = Optional.empty();
	private Optional<EclArgument> lArgOffset = Optional.empty();
	private Optional<String> lArgBaseName = Optional.empty();
	private Optional<String> lArgOffsetName = Optional.empty();
	private Optional<String> rArgExpression = Optional.empty();

	public EclinstructionWrapper(@Nonnull EclInstruction inst, boolean conditional) {
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
		if (lArgBase.isPresent() && lArgOffset.isPresent()) {
			return String.format("[%s + %s] = %s", //
				lArgBaseName.orElse(lArgBase.get().toString()), //
				lArgOffsetName.orElse(lArgOffset.get().toString()), //
				rArgExpression.orElse(toString()));
		}
		return lArgBase.map(a -> lArgBaseName.orElse(a.toString()) + " = ").orElse("") + rArgExpression.orElse(toString());
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

	public String getArgumentName(int index) {
		return argumentNames.get(index);
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

	public void setlArgBase(@Nonnull EclArgument lArgBase) {
		this.lArgBase = Optional.of(lArgBase);
	}

	public void setlArgOffset(@Nonnull EclArgument lArgOffset) {
		this.lArgOffset = Optional.of(lArgOffset);
	}

	public void setlArgBaseName(@Nonnull String lArgBaseName) {
		this.lArgBaseName = Optional.of(lArgBaseName);
	}

	public void setlArgOffsetName(@Nonnull String lArgOffsetName) {
		this.lArgOffsetName = Optional.of(lArgOffsetName);
	}

	public void setrArgExpression(@Nonnull String rArgExpression) {
		this.rArgExpression = Optional.of(rArgExpression);
	}

	public void setrArgName(int index, String name) {
		argumentNames.put(index, name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(inst);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EclinstructionWrapper)) {
			return false;
		}
		EclinstructionWrapper other = (EclinstructionWrapper) obj;
		return Objects.equals(inst, other.inst);
	}

	@Override
	public String toString() {
		return inst.toString();
	}
}
