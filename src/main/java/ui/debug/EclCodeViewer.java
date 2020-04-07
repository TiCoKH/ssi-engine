package ui.debug;

import static data.ContentType.ECL;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import common.FileMap;
import data.ResourceLoader;
import data.script.EclProgram;
import engine.EngineConfiguration;
import engine.debug.CodeBlock;
import engine.debug.CodeSection;
import engine.debug.Decompiler;
import engine.debug.Disassembler;
import engine.debug.Disassembler.JumpAddresses;
import engine.script.EclInstruction;
import engine.script.EclOpCode;
import engine.debug.EclInstructionData;
import engine.debug.EclinstructionWrapper;

public class EclCodeViewer {
	private JFrame frame;
	private JComboBox<Integer> blockCombo;
	private JComboBox<CodeSection> sectionsCombo;

	private JTable memoryTable;
	private JTable asmTable;
	private JTable codeTable;

	private MarkerTableCellRenderer gotoMarkerRender;
	private HexValueTableCellRenderer hexRenderer;

	private ResourceLoader res;

	private Disassembler disasm;
	private Decompiler decom;
	private ScheduledExecutorService exec;
	private Map<Integer, String> addressNames;

	private Map<CodeSection, EclDisassembly> data;

	private int codeBase;

	public EclCodeViewer(@Nonnull FileMap fm) throws Exception {
		this.res = new ResourceLoader(fm);

		EngineConfiguration cfg = new EngineConfiguration(fm);
		this.codeBase = cfg.getCodeBase();

		EclInstruction.configOpCodes(cfg.getOpCodes());

		disasm = new Disassembler(cfg.getCodeBase());
		decom = new Decompiler(cfg);

		addressNames = new HashMap<>();
		decom.updateKnownAddresses(addressNames);

		exec = Executors.newScheduledThreadPool(1);

		initFrame();
	}

	private void initFrame() throws IOException {
		Set<Integer> blockIds = new TreeSet<Integer>(res.idsFor(ECL));
		blockCombo = new JComboBox<Integer>(blockIds.toArray(new Integer[blockIds.size()]));
		blockCombo.setSelectedIndex(-1);
		blockCombo.addActionListener(ev -> {
			int selIndex = blockCombo.getSelectedIndex();
			if (selIndex != -1) {
				exec.execute(() -> {
					sectionsCombo.setEnabled(false);
					int blockid = blockCombo.getItemAt(selIndex);
					try {
						EclProgram ecl = res.find(blockid, EclProgram.class, ECL);

						MemoryTableModel model = (MemoryTableModel) memoryTable.getModel();
						model.setCode(ecl.getCode());
						memoryTable.invalidate();

						Map<CodeSection, EclDisassembly> code = new EnumMap<>(CodeSection.class);
						for (CodeSection section : CodeSection.values()) {
							EclDisassembly d = new EclDisassembly();
							d.addresses = disasm.parseJumpAdresses(ecl, section);
							d.asmBlocks = disasm.parseCodeblocks(ecl, section, d.addresses);
							code.put(section, d);
							decom.updateKnownAddresses(addressNames, d.asmBlocks, blockid);
						}
						for (CodeSection section : CodeSection.values()) {
							EclDisassembly d = code.get(section);
							d.codeBlocks = decom.decompile(section, d.asmBlocks, addressNames, d.addresses);
						}
						data = code;
						hexRenderer.setCode(data);
						updateCode();
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
					sectionsCombo.setEnabled(true);
				});
			}
		});

		sectionsCombo = new JComboBox<CodeSection>(CodeSection.values());
		sectionsCombo.setEnabled(false);
		sectionsCombo.setSelectedIndex(-1);
		sectionsCombo.addActionListener((ev) -> {
			exec.execute(this::updateCode);
		});

		JPanel comboPanel = new JPanel(new GridLayout(2, 2));
		comboPanel.add(new JLabel("ECL Code Block"), 0);
		comboPanel.add(new JLabel("ECL Code Section"), 1);
		comboPanel.add(blockCombo, 2);
		comboPanel.add(sectionsCombo, 3);

		Font tableFont = new Font(Font.MONOSPACED, Font.BOLD, 16);
		int width = 2 + tableFont.getMaxCharBounds(new FontRenderContext(null, true, false)).getBounds().width;

		memoryTable = new JTable();
		memoryTable.setCellSelectionEnabled(false);
		memoryTable.setDoubleBuffered(true);
		memoryTable.setEnabled(false);
		memoryTable.setFont(tableFont);
		memoryTable.setModel(new MemoryTableModel());
		memoryTable.setShowVerticalLines(false);
		memoryTable.setTableHeader(null);
		hexRenderer = new HexValueTableCellRenderer(codeBase);
		for (int i = 0; i < 17; i++) {
			int size = (i == 0 ? 4 : 2) * width;
			TableCellRenderer renderer = (i == 0 ? new AddressTableCellRenderer(codeBase) : hexRenderer);
			TableColumn tc = memoryTable.getColumnModel().getColumn(i);
			tc.setMinWidth(size);
			tc.setPreferredWidth(size);
			tc.setMaxWidth(size);
			tc.setCellRenderer(renderer);
		}

		JScrollPane memoryScroll = new JScrollPane(memoryTable);
		memoryScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		memoryScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

		asmTable = new JTable();
		asmTable.setCellSelectionEnabled(false);
		asmTable.setDoubleBuffered(true);
		asmTable.setEnabled(true);
		asmTable.setFont(tableFont);
		asmTable.setModel(new ASMTableModel());
		asmTable.setRowSelectionAllowed(true);
		asmTable.setShowVerticalLines(false);
		asmTable.setTableHeader(null);
		asmTable.addMouseListener(new ASMMouseListener());
		asmTable.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int[] selectedRows = asmTable.getSelectedRows();
			Set<EclInstructionData> selectedInst = new HashSet<EclInstructionData>();
			ASMTableModel model = (ASMTableModel) asmTable.getModel();
			for (int row : selectedRows) {
				EclInstructionData inst = model.getInst(row);
				if (inst != null) {
					selectedInst.add(inst);
				}
			}
			hexRenderer.setSelectedInst(selectedInst);
			memoryTable.repaint();
		});
		TableColumn tc0 = asmTable.getColumnModel().getColumn(0);
		tc0.setMinWidth(2 + width);
		tc0.setPreferredWidth(2 + width);
		tc0.setMaxWidth(2 + width);
		gotoMarkerRender = new MarkerTableCellRenderer(codeBase);
		tc0.setCellRenderer(gotoMarkerRender);
		TableColumn tc1 = asmTable.getColumnModel().getColumn(1);
		tc1.setMinWidth(4 * width);
		tc1.setPreferredWidth(4 * width);
		tc1.setMaxWidth(4 * width);
		tc1.setCellRenderer(new AddressTableCellRenderer(codeBase));

		JScrollPane asmScroll = new JScrollPane(asmTable);
		asmScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		asmScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

		codeTable = new JTable();
		codeTable.setCellSelectionEnabled(false);
		codeTable.setDoubleBuffered(true);
		codeTable.setEnabled(true);
		codeTable.setModel(new CodeTableModel());
		codeTable.setRowSelectionAllowed(true);
		codeTable.setShowVerticalLines(false);
		codeTable.setTableHeader(null);
		codeTable.setFont(tableFont);
		codeTable.addMouseListener(new ASMMouseListener());
		codeTable.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int[] selectedRows = codeTable.getSelectedRows();
			Set<EclInstructionData> selectedInst = new HashSet<EclInstructionData>();
			CodeTableModel model = (CodeTableModel) codeTable.getModel();
			for (int row : selectedRows) {
				EclInstructionData inst = model.getInst(row);
				if (inst != null) {
					selectedInst.add(inst);
				}
			}
			hexRenderer.setSelectedInst(selectedInst);
			memoryTable.repaint();
		});
		TableColumn ctc0 = codeTable.getColumnModel().getColumn(0);
		ctc0.setMinWidth(2 + width);
		ctc0.setPreferredWidth(2 + width);
		ctc0.setMaxWidth(2 + width);
		ctc0.setCellRenderer(gotoMarkerRender);
		TableColumn ctc1 = codeTable.getColumnModel().getColumn(1);
		ctc1.setMinWidth(4 * width);
		ctc1.setPreferredWidth(4 * width);
		ctc1.setMaxWidth(4 * width);
		ctc1.setCellRenderer(new AddressTableCellRenderer(codeBase));

		JScrollPane codeScroll = new JScrollPane(codeTable);
		codeScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		codeScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

		JTabbedPane tabsPane = new JTabbedPane();
		tabsPane.add("ASM", asmScroll);
		tabsPane.add("Code", codeScroll);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(memoryScroll);
		splitPane.setRightComponent(tabsPane);
		splitPane.setDividerLocation(0.5);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(splitPane, BorderLayout.CENTER);

		JPanel main = new JPanel(new BorderLayout());
		main.add(comboPanel, BorderLayout.NORTH);
		main.add(tablePanel, BorderLayout.CENTER);

		this.frame = new JFrame("ECL Code");
		this.frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.add(main, BorderLayout.CENTER);
	}

	public void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}

	private void updateCode() {
		int selIndex = sectionsCombo.getSelectedIndex();

		ASMTableModel model = (ASMTableModel) asmTable.getModel();
		model.setCode(selIndex != -1 ? data.get(sectionsCombo.getItemAt(selIndex)) : null);

		CodeTableModel codeModel = (CodeTableModel) codeTable.getModel();
		codeModel.setCode(selIndex != -1 ? data.get(sectionsCombo.getItemAt(selIndex)) : null);

		gotoMarkerRender.setAddresses(selIndex != -1 ? data.get(sectionsCombo.getItemAt(selIndex)).addresses : null);
	}

	private final class ASMMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
				ASMTableModel model = (ASMTableModel) asmTable.getModel();
				EclinstructionWrapper inst = (EclinstructionWrapper) model.getInst(asmTable.rowAtPoint(e.getPoint()));
				if (inst != null && (inst.getOpCode() == EclOpCode.GOTO || inst.getOpCode() == EclOpCode.GOSUB)) {
					int address = inst.getArgument(0).valueAsInt();
					for (int i = 0; i < model.getRowCount(); i++) {
						EclInstructionData rowInst = model.getInst(i);
						if (rowInst != null && (codeBase + rowInst.getPosition()) == address) {
							asmTable.getSelectionModel().clearSelection();
							asmTable.getSelectionModel().addSelectionInterval(i, i);
							asmTable.scrollRectToVisible(asmTable.getCellRect(i, 0, true));
							return;
						}
					}
				}
			}
		}
	}

	static final class EclDisassembly {
		public JumpAddresses addresses;
		public Set<CodeBlock> asmBlocks;
		public Set<CodeBlock> codeBlocks;
	}
}
