package ui.debug;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class AddressTableCellRenderer implements TableCellRenderer {

	private int codeBase;

	public AddressTableCellRenderer(int codeBase) {
		this.codeBase = codeBase;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value != null)
			return new JLabel(hex4(codeBase + (int) value));
		return new JLabel("");
	}

	private static String hex4(int value) {
		return String.format("%04X", value);
	}

}
