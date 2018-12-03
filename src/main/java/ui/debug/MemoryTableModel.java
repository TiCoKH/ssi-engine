package ui.debug;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.table.AbstractTableModel;

import common.ByteBufferWrapper;

public class MemoryTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 767320373557181608L;

	private Optional<ByteBufferWrapper> eclCode = Optional.empty();
	private int rowCount;

	public MemoryTableModel() {
		rowCount = 1;
	}

	public void setCode(@Nullable ByteBufferWrapper eclCode) {
		this.eclCode = Optional.ofNullable(eclCode);
		rowCount = 1 + (this.eclCode.map(c -> c.limit() >> 4).orElse(0));
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return 18;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// Memory address
		if (columnIndex == 0) {
			return 16 * rowIndex;
		}
		if (columnIndex == 17) {
			return "";
		}
		int pos = (rowIndex << 4) + (columnIndex - 1);
		return hex2(eclCode.filter(c -> c.limit() > pos).map(c -> c.getUnsigned(pos)).orElse(0));
	}

	private static String hex2(int value) {
		return String.format("%02X", value);
	}
}
