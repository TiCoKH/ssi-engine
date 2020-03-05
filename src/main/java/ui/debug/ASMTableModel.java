package ui.debug;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.table.AbstractTableModel;

import engine.debug.CodeBlock;
import engine.debug.EclInstructionData;
import ui.debug.EclCodeViewer.EclDisassembly;

class ASMTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 6730901626650684935L;

	private transient Optional<EclDisassembly> disassembly = Optional.empty();
	private int rowCount;

	public ASMTableModel() {
		rowCount = 1;
	}

	public void setCode(@Nullable EclDisassembly disassembly) {
		this.disassembly = Optional.ofNullable(disassembly);
		this.rowCount = this.disassembly.map(d -> countRows(d.asmBlocks)).orElse(1);
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		EclInstructionData inst = getInst(rowIndex);
		if (inst == null) {
			return null;
		}
		if (columnIndex == 0 || columnIndex == 1) {
			return inst.getPosition();
		}
		return (inst.isConditional() ? "    " : "") + inst.toString();
	}

	public EclInstructionData getInst(int row) {
		return disassembly.map(d -> findIn(d.asmBlocks, row)).orElse(null);
	}

	private static int countRows(@Nonnull Set<CodeBlock> blocks) {
		int rows = blocks.size() - 1;
		for (CodeBlock block : blocks) {
			rows += block.getCode().size();
		}
		return rows;
	}

	private static EclInstructionData findIn(Set<CodeBlock> blocks, int row) {
		int start = 0;
		for (CodeBlock block : blocks) {
			if (start <= row && (start + block.getCode().size()) > row) {
				return block.getCode().get(row - start);
			}
			start += block.getCode().size();
			start++;
		}
		return null;
	}
}