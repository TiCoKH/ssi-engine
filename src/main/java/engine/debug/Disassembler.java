package engine.debug;

import static engine.script.EclOpCode.GOSUB;
import static engine.script.EclOpCode.GOTO;
import static engine.script.EclOpCode.ON_GOSUB;
import static engine.script.EclOpCode.ON_GOTO;
import static io.vavr.API.Map;
import static io.vavr.API.SortedSet;

import io.vavr.collection.Map;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;

import common.ByteBufferWrapper;
import data.script.EclProgram;
import engine.script.EclArgument;
import engine.script.EclInstruction;
import engine.script.EclOpCode;

public class Disassembler {
	private int codeBase;

	public Disassembler(int codeBase) {
		this.codeBase = codeBase;
	}

	public JumpAddresses parseJumpAdresses(EclProgram p, CodeSection section) {
		ByteBufferWrapper eclCode = p.getCode();

		JumpAddresses result = new JumpAddresses();
		disassembleBlock(eclCode, codeBase + section.getStartOffset(), result);

		Integer a = result.getNextUnparsedAddress();
		while (a != null) {
			disassembleBlock(eclCode, a, result);
			a = result.getNextUnparsedAddress();
		}

		return result;
	}

	public SortedSet<CodeBlock> parseCodeblocks(EclProgram p, CodeSection section, JumpAddresses addresses) {
		ByteBufferWrapper eclCode = p.getCode();

		addresses.clearMarks();

		SortedSet<CodeBlock> result = TreeSet.empty();
		result = result.add(disassembleBlock(eclCode, codeBase + section.getStartOffset(), addresses));

		Integer a = addresses.getNextUnparsedAddress();
		while (a != null) {
			result = result.add(disassembleBlock(eclCode, a, addresses));
			a = addresses.getNextUnparsedAddress();
		}

		return result;
	}

	private CodeBlock disassembleBlock(ByteBufferWrapper eclCode, int address, JumpAddresses addresses) {
		SortedSet<EclInstructionData> result = SortedSet();

		EclinstructionWrapper inst = null;
		boolean isOpCodeFinishingBlock = false;
		boolean isConditional = false;
		int pos = address;
		boolean reachedJumpAddress = false;
		try {
			eclCode.position(address - codeBase);
			do {
				inst = new EclinstructionWrapper(EclInstruction.parseNext(eclCode), isConditional);
				addresses.addJumps(inst);
				result = result.add(inst);

				isOpCodeFinishingBlock = inst.isBlockFinisher() && !inst.isConditional();

				isConditional = inst.isIf();

				pos = codeBase + eclCode.position();
				reachedJumpAddress = pos != address && !isConditional && addresses.isJumpAddress(pos);
			} while (!reachedJumpAddress && !isOpCodeFinishingBlock);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		addresses.markAddressParsed(address);

		return new CodeBlock(result, codeBase);
	}

	public class JumpAddresses {
		private Map<Integer, Boolean> gotoAddresses = Map();
		private Map<Integer, Boolean> gosubAddresses = Map();

		JumpAddresses() {
		}

		void addJumps(EclinstructionWrapper inst) {
			EclOpCode opCode = inst.getOpCode();
			if (opCode == ON_GOTO) {
				gotoAddresses = gotoAddresses
					.merge(inst.getDynArgs().toMap(EclArgument::valueAsInt, x -> Boolean.FALSE));
				// TODO implicit jump/continue for unmapped values
				// gotoAddresses.putIfAbsent(config.getCodeBase() + inst.getPosition() + inst.getSize(), Boolean.FALSE);
			} else if (opCode == ON_GOSUB) {
				gosubAddresses = gosubAddresses
					.merge(inst.getDynArgs().toMap(EclArgument::valueAsInt, x -> Boolean.FALSE));
			} else if (opCode == GOTO) {
				gotoAddresses = gotoAddresses.computeIfAbsent(inst.getArgument(0).valueAsInt(),
					key -> Boolean.FALSE)._2;
			} else if (opCode == GOSUB) {
				gosubAddresses = gosubAddresses.computeIfAbsent(inst.getArgument(0).valueAsInt(),
					key -> Boolean.FALSE)._2;
			}
		}

		boolean isAddressParsed(int address) {
			return gotoAddresses.get(address).getOrElse(false) || gosubAddresses.get(address).getOrElse(false);
		}

		void markAddressParsed(int address) {
			gotoAddresses = gotoAddresses.replaceValue(address, Boolean.TRUE);
			gosubAddresses = gosubAddresses.replaceValue(address, Boolean.TRUE);
		}

		void clearMarks() {
			gotoAddresses = gotoAddresses.replaceAll((k, v) -> Boolean.FALSE);
			gosubAddresses = gosubAddresses.replaceAll((k, v) -> Boolean.FALSE);
		}

		Integer getNextUnparsedAddress() {
			return getAllAddresses().find(a -> !isAddressParsed(a)).getOrNull();

		}

		public boolean isJumpAddress(int address) {
			return gotoAddresses.containsKey(address) || gosubAddresses.containsKey(address);
		}

		public SortedSet<Integer> getAllAddresses() {
			return TreeSet.ofAll(gotoAddresses.keySet()).addAll(gosubAddresses.keySet());
		}
	}
}
