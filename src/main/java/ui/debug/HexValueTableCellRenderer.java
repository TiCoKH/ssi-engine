package ui.debug;

import java.awt.Color;
import java.awt.Component;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import io.vavr.collection.Map;
import io.vavr.collection.Set;

import engine.debug.CodeBlock;
import engine.debug.CodeSection;
import engine.debug.EclInstructionData;
import ui.debug.EclCodeViewer.EclDisassembly;

public class HexValueTableCellRenderer extends DefaultTableCellRenderer {

	public Optional<Map<CodeSection, EclDisassembly>> data = Optional.empty();
	public Optional<Set<EclInstructionData>> selectedInstSet = Optional.empty();

	private int codeBase;

	public HexValueTableCellRenderer(int codeBase) {
		this.codeBase = codeBase;
	}

	public void setCode(@Nullable Map<CodeSection, EclDisassembly> blocks) {
		this.data = Optional.ofNullable(blocks);
	}

	public void setSelectedInst(@Nullable Set<EclInstructionData> instSet) {
		this.selectedInstSet = Optional.ofNullable(instSet);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column) {
		int address = codeBase + (row << 4) + (column - 1);

		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		c.setBackground(Color.black);
		c.setForeground(Color.white);
		boolean backgroundSet = false;
		if (selectedInstSet.isPresent()) {
			for (EclInstructionData inst : selectedInstSet.get()) {
				int startAddress = codeBase + inst.getPosition();
				int endAddress = startAddress + inst.getSize();
				if (startAddress <= address && address < endAddress) {
					c.setForeground(Color.black);
					c.setBackground(Color.orange);
					backgroundSet = true;
					break;
				}
			}
		}
		if (!backgroundSet) {
			data.ifPresent(d -> {
				for (CodeSection section : CodeSection.values()) {
					Set<CodeBlock> blocks = d.get(section).get().asmBlocks;
					for (CodeBlock b : blocks) {
						if (b.getStartAddress() <= address && address < b.getEndAddress()) {
							c.setBackground(Color.green.darker().darker());
						}
					}
				}
			});
		}
		return c;
	}
}
