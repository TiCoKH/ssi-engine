package ui.resource;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType.TITLE;
import static data.content.DAXContentType.WALLDEF;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import common.FileMap;
import data.content.DAXContentType;
import ui.ExceptionHandler;
import ui.UIResourceLoader;
import ui.UIResourceManager;
import ui.UISettings;

public class ResourceViewer {
	private JFrame frame;
	private RenderSurface drawSurface;

	private UIResourceLoader loader;

	public ResourceViewer(@Nonnull FileMap fileMap, @Nonnull UISettings settings, @Nonnull ExceptionHandler excHandler) throws IOException {
		this.loader = new UIResourceLoader(fileMap);
		UIResourceManager resman = new UIResourceManager(loader, settings, excHandler);
		this.drawSurface = new RenderSurface(resman, settings);

		initFrame();
	}

	private void initFrame() throws IOException {
		JTree resourceTree = new JTree(initModel());
		resourceTree.setDoubleBuffered(true);
		resourceTree.setEditable(false);
		resourceTree.setShowsRootHandles(false);
		resourceTree.addTreeSelectionListener(ev -> {
			DefaultMutableTreeNode sel = (DefaultMutableTreeNode) ev.getPath().getLastPathComponent();
			if (sel.getUserObject() instanceof RenderInfo) {
				try {
					drawSurface.changeRenderObject((RenderInfo) sel.getUserObject());
					frame.pack();
				} catch (IOException e) {
					e.printStackTrace(System.err);
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Error loading resource", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				drawSurface.clearRenderObject();
			}
			drawSurface.repaint();
		});

		JScrollPane treeScroll = new JScrollPane(resourceTree);
		treeScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		treeScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

		JScrollPane drawScroll = new JScrollPane(drawSurface);
		drawScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		drawScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(treeScroll);
		splitPane.setRightComponent(drawScroll);
		splitPane.setDividerLocation(0.5);

		this.frame = new JFrame("Resource");
		this.frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.add(splitPane, BorderLayout.CENTER);
	}

	private TreeNode initModel() throws IOException {
		MutableTreeNode root = new DefaultMutableTreeNode("Countdown to Doomsday");

		initChildren(root, "FONT", 201);
		initChildren(root, "MISC", 202);
		initChildren(root, BIGPIC);
		initChildren(root, BACK);
		initChildren(root, PIC);
		initChildren(root, SPRIT);
		initChildren(root, TITLE);
		initChildren(root, WALLDEF);

		return root;
	}

	private void initChildren(MutableTreeNode root, DAXContentType type) throws IOException {
		Set<Integer> ids = new TreeSet<>(loader.idsFor(type));
		if (!ids.isEmpty()) {
			MutableTreeNode parent = new DefaultMutableTreeNode(type.name());
			for (Integer id : ids) {
				parent.insert(new DefaultMutableTreeNode(new RenderInfo(type, id)), parent.getChildCount());
			}
			root.insert(parent, root.getChildCount());
		}
	}

	private void initChildren(MutableTreeNode root, String name, int id) {
		MutableTreeNode parent = new DefaultMutableTreeNode(name);
		parent.insert(new DefaultMutableTreeNode(new RenderInfo(null, id)), parent.getChildCount());
		root.insert(parent, root.getChildCount());
	}

	public void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}
}
