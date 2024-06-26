package engine.debug;

import static engine.script.EclOpCode.EXIT;
import static engine.script.EclOpCode.GOTO;
import static engine.script.EclOpCode.IF_EQUALS;
import static engine.script.EclOpCode.IF_GREATER;
import static engine.script.EclOpCode.IF_GREATER_EQUALS;
import static engine.script.EclOpCode.IF_LESS;
import static engine.script.EclOpCode.IF_LESS_EQUALS;
import static engine.script.EclOpCode.IF_NOT_EQUALS;
import static engine.script.EclOpCode.NEW_ECL;
import static engine.script.EclOpCode.ON_GOTO;
import static engine.script.EclOpCode.RETURN;
import static engine.script.EclOpCode.STOP_MOVE_23;
import static engine.script.EclOpCode.STOP_MOVE_42;
import static io.vavr.API.Seq;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import engine.script.EclArgument;
import engine.script.EclInstruction;
import engine.script.EclOpCode;

public class EclinstructionWrapper implements EclInstructionData {
	private static final Seq<EclOpCode> OP_CODE_STOP = Seq(EXIT, STOP_MOVE_23, STOP_MOVE_42, GOTO, ON_GOTO, RETURN,
		NEW_ECL);
	private static final Seq<EclOpCode> OP_CODE_IF = Seq(IF_EQUALS, IF_GREATER, IF_GREATER_EQUALS, IF_LESS,
		IF_LESS_EQUALS, IF_NOT_EQUALS);

	private EclInstruction inst;
	private boolean conditional;

	private Map<Integer, String> argumentNames = HashMap.empty();

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
		return lArgBase.map(a -> lArgBaseName.orElse(a.toString()) + " = ").orElse("")
			+ rArgExpression.orElse(toString());
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
		return argumentNames.get(index).getOrElse("");
	}

	public EclArgument[] getArguments() {
		return inst.getArguments();
	}

	public Seq<EclArgument> getDynArgs() {
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
		argumentNames = argumentNames.put(index, name);
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
