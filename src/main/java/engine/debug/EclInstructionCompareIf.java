package engine.debug;

import static engine.script.EclOpCode.AND;
import static engine.script.EclOpCode.COMPARE;
import static engine.script.EclOpCode.COMPARE_AND;
import static engine.script.EclOpCode.IF_EQUALS;
import static engine.script.EclOpCode.IF_GREATER;
import static engine.script.EclOpCode.IF_GREATER_EQUALS;
import static engine.script.EclOpCode.IF_LESS;
import static engine.script.EclOpCode.IF_LESS_EQUALS;
import static engine.script.EclOpCode.OR;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import engine.script.EclArgument;
import engine.script.EclOpCode;

public class EclInstructionCompareIf implements EclInstructionData {
	private static final List<EclOpCode> OP_CODE_HEX_ARGS = ImmutableList.of(AND, OR);

	private EclinstructionWrapper compare;
	private EclinstructionWrapper ifInst;

	public EclInstructionCompareIf(@Nonnull EclinstructionWrapper compare, @Nonnull EclinstructionWrapper ifInst) {
		this.compare = compare;
		this.ifInst = ifInst;
	}

	@Override
	public int getPosition() {
		return ifInst.getPosition();
	}

	@Override
	public int getSize() {
		return ifInst.getSize();
	}

	@Override
	public boolean isConditional() {
		return false;
	}

	@Override
	public String getCodeline() {
		EclOpCode ifOp = ifInst.getOpCode();
		if (compare.getOpCode() == COMPARE_AND) {
			String andOp = ifOp == IF_EQUALS ? "&&" : "||";
			return String.format("if (%s %s %s %s %s %s %s)", //
				compArg1(0, 1), operator(ifOp), compArg2(0, 1), //
				andOp, //
				compArg1(2, 3), operator(ifOp), compArg2(2, 3));
		}
		if (compare.getOpCode() == COMPARE && isExchangeCompareArgs(0, 1)) {
			ifOp = exchange(ifOp);
		}
		return String.format("if (%s %s %s)", compareLArg(), operator(ifOp), compareRArg());
	}

	private String compareLArg() {
		switch (compare.getOpCode()) {
			case COMPARE:
				return compArg1(0, 1);
			case COMPARE_AND:
				return "";
			case AND:
				return String.format("%s & %s", compArg1(0, 1), compArg2(0, 1));
			case OR:
				return String.format("%s | %s", compArg1(0, 1), compArg2(0, 1));
			case INPUT_YES_NO_22:
			case INPUT_YES_NO_2C:
				return "INPUT_YES_NO()";
			default:
				return "";
		}
	}

	private String compareRArg() {
		switch (compare.getOpCode()) {
			case COMPARE:
				return compArg2(0, 1);
			case COMPARE_AND:
				return "";
			case AND:
				return "0";
			case OR:
				return "0";
			case INPUT_YES_NO_22:
			case INPUT_YES_NO_2C:
				return "YES";
			default:
				return "";
		}
	}

	private String compArg1(int argIndex1, int argIndex2) {
		if (isExchangeCompareArgs(argIndex1, argIndex2)) {
			return argName(compare, argIndex2);
		} else {
			return argName(compare, argIndex1);
		}
	}

	private String compArg2(int argIndex1, int argIndex2) {
		if (isExchangeCompareArgs(argIndex1, argIndex2)) {
			return argName(compare, argIndex1);
		} else {
			return argName(compare, argIndex2);
		}
	}

	private boolean isExchangeCompareArgs(int argIndex1, int argIndex2) {
		return compare.getArgument(argIndex2).isMemAddress() && !compare.getArgument(argIndex1).isMemAddress();
	}

	private String argName(EclinstructionWrapper inst, int index) {
		EclArgument a = inst.getArgument(index);
		if (a.isMemAddress()) {
			String name = compare.getArgumentName(index);
			if (name == null) {
				return "[" + a.toString() + "]";
			}
			return name;
		}
		if (a.isNumberValue() && OP_CODE_HEX_ARGS.contains(inst.getOpCode())) {
			return "0x" + Integer.toHexString(a.valueAsInt()).toUpperCase();
		}
		return a.toString();
	}

	private static String operator(EclOpCode ifOp) {
		switch (ifOp) {
			case IF_EQUALS:
				return "==";
			case IF_GREATER:
				return ">";
			case IF_GREATER_EQUALS:
				return ">=";
			case IF_LESS:
				return "<";
			case IF_LESS_EQUALS:
				return "<=";
			case IF_NOT_EQUALS:
				return "!=";
			default:
				return "";
		}
	}

	private static EclOpCode exchange(EclOpCode ifOp) {
		switch (ifOp) {
			case IF_EQUALS:
			case IF_NOT_EQUALS:
				return ifOp;
			case IF_GREATER:
				return IF_LESS;
			case IF_GREATER_EQUALS:
				return IF_LESS_EQUALS;
			case IF_LESS:
				return IF_GREATER;
			case IF_LESS_EQUALS:
				return IF_GREATER_EQUALS;
			default:
				return ifOp;
		}
	}
}
