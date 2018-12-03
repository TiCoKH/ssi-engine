package ui.debug;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import engine.debug.Disassembler.JumpAddresses;

public class MarkerTableCellRenderer implements TableCellRenderer {
	private JumpAddresses addresses;

	private int codeBase;

	public MarkerTableCellRenderer(int codeBase) {
		this.codeBase = codeBase;
	}

	public void setAddresses(JumpAddresses addresses) {
		this.addresses = addresses;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		boolean isMarker = addresses != null && value != null && addresses.isJumpAddress(codeBase + (int) value);
		return new JLabel(isMarker ? "O" : "");
	}

}
