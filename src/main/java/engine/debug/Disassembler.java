package engine.debug;

import static engine.opcodes.EclOpCode.GOSUB;
import static engine.opcodes.EclOpCode.GOTO;
import static engine.opcodes.EclOpCode.ON_GOSUB;
import static engine.opcodes.EclOpCode.ON_GOTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import common.ByteBufferWrapper;
import data.content.EclProgram;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class Disassembler {
	private int codeBase;;

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

		SortedSet<CodeBlock> result = new TreeSet<>();
		result.add(disassembleBlock(eclCode, codeBase + section.getStartOffset(), addresses));

		Integer a = addresses.getNextUnparsedAddress();
		while (a != null) {
			result.add(disassembleBlock(eclCode, a, addresses));
			a = addresses.getNextUnparsedAddress();
		}

		return result;
	}

	private CodeBlock disassembleBlock(ByteBufferWrapper eclCode, int address, JumpAddresses addresses) {
		List<EclInstructionData> result = new ArrayList<>();

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
				result.add(inst);

				isOpCodeFinishingBlock = inst.isBlockFinisher() && !inst.isConditional();

				isConditional = inst.isIf();

				pos = codeBase + eclCode.position();
				reachedJumpAddress = pos != address && !isConditional && addresses.isJumpAddress(pos);
			} while (!reachedJumpAddress && !isOpCodeFinishingBlock);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		addresses.markAddressParsed(address);

		int endAddress = inst != null ? codeBase + inst.getPosition() + inst.getSize() : address;

		return new CodeBlock(result, address, endAddress);
	}

	public class JumpAddresses {
		private Map<Integer, Boolean> gotoAddresses = new HashMap<>();
		private Map<Integer, Boolean> gosubAddresses = new HashMap<>();

		JumpAddresses() {
		}

		void addJumps(EclinstructionWrapper inst) {
			EclOpCode opCode = inst.getOpCode();
			if (opCode == ON_GOTO) {
				inst.getDynArgs().stream().forEach(a -> gotoAddresses.putIfAbsent(a.valueAsInt(), Boolean.FALSE));
				// TODO implicit jump/continue for unmapped values
				// gotoAddresses.putIfAbsent(config.getCodeBase() + inst.getPosition() + inst.getSize(), Boolean.FALSE);
			} else if (opCode == ON_GOSUB) {
				inst.getDynArgs().stream().forEach(a -> gosubAddresses.putIfAbsent(a.valueAsInt(), Boolean.FALSE));
			} else if (opCode == GOTO) {
				gotoAddresses.putIfAbsent(inst.getArgument(0).valueAsInt(), Boolean.FALSE);
			} else if (opCode == GOSUB) {
				gosubAddresses.putIfAbsent(inst.getArgument(0).valueAsInt(), Boolean.FALSE);
			}
		}

		boolean isAddressParsed(int address) {
			return (gotoAddresses.containsKey(address) && Boolean.TRUE.equals(gotoAddresses.get(address)))
				|| (gosubAddresses.containsKey(address) && Boolean.TRUE.equals(gosubAddresses.get(address)));
		}

		void markAddressParsed(int address) {
			gotoAddresses.replace(address, Boolean.TRUE);
			gosubAddresses.replace(address, Boolean.TRUE);
		}

		void clearMarks() {
			gotoAddresses.replaceAll((k, v) -> Boolean.FALSE);
			gosubAddresses.replaceAll((k, v) -> Boolean.FALSE);
		}

		Integer getNextUnparsedAddress() {
			return getAllAddresses().stream().filter(a -> !isAddressParsed(a)).findFirst().orElse(null);

		}

		public boolean isJumpAddress(int address) {
			return gotoAddresses.containsKey(address) || gosubAddresses.containsKey(address);
		}

		public SortedSet<Integer> getAllAddresses() {
			SortedSet<Integer> result = new TreeSet<>();
			result.addAll(gotoAddresses.keySet());
			result.addAll(gosubAddresses.keySet());
			return result;
		}
	}
}
