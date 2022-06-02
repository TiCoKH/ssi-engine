package ui.debug;

import static data.ContentType.BACK;
import static data.ContentType.BIGPIC;
import static data.ContentType.BODY;
import static data.ContentType.GEO;
import static data.ContentType.HEAD;
import static data.ContentType.MONCHA;
import static data.ContentType.PIC;
import static data.ContentType.SPRIT;
import static data.ContentType.TITLE;
import static data.ContentType.WALLDEF;
import static data.ContentType._8X8D;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
import static shared.GameFeature.BODY_HEAD;
import static shared.GameFeature.FLEXIBLE_DUNGEON_SIZE;
import static shared.GameFeature.OVERLAND_DUNGEON;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import common.FileMap;
import data.ContentFile;
import data.ContentType;
import data.DAXFile;
import data.dungeon.DungeonMap;
import data.dungeon.DungeonMap2;
import shared.EngineStub;
import shared.GameFeature;
import ui.ExceptionHandler;
import ui.UISettings;
import ui.shared.BackdropMode;
import ui.shared.UIFrame;
import ui.shared.resource.DungeonMapResource;
import ui.shared.resource.DungeonResource;
import ui.shared.resource.IdTypeResource;
import ui.shared.resource.ImageResource;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceLoader;
import ui.shared.resource.UIResourceManager;

public class ResourceViewer {
	private JFrame frame;
	private RenderSurface drawSurface;

	private UIResourceConfiguration config;
	private UIResourceLoader loader;

	public ResourceViewer(@Nonnull FileMap fileMap, @Nonnull UIResourceConfiguration config, @Nonnull UISettings settings,
		@Nonnull ExceptionHandler excHandler, @Nonnull EngineStub engine) throws IOException {

		this.config = config;
		this.loader = new UIResourceLoader(fileMap, config);

		UIResourceManager resman = new UIResourceManager(config, loader, settings, excHandler);
		this.drawSurface = new RenderSurface(config, resman, settings, engine);

		initFrame();
	}

	private void initFrame() throws IOException {
		JTree resourceTree = new JTree(initModel());
		resourceTree.setDoubleBuffered(true);
		resourceTree.setEditable(false);
		resourceTree.setShowsRootHandles(false);
		resourceTree.addTreeSelectionListener(ev -> {
			DefaultMutableTreeNode sel = (DefaultMutableTreeNode) ev.getPath().getLastPathComponent();
			drawSurface.changeRenderObject(sel.getUserObject());
			frame.pack();
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
		final MutableTreeNode root = new DefaultMutableTreeNode(config.getGameName());

		initChildren(root, "FONT", 201);
		initChildren(root, "MISC", 202);
		if (!loader.idsFor(WALLDEF).isEmpty())
			initChildren(root, _8X8D);
		initFrameChildren(root);
		initChildren(root, MONCHA);
		initChildren(root, BIGPIC);
		BackdropMode mode = config.getBackdropMode();
		if (BackdropMode.SPACE.equals(mode) //
			|| BackdropMode.GEO2.equals(mode) //
			|| BackdropMode.SKYGRND.equals(mode))
			initChildren(root, BACK);
		if (BackdropMode.SKY.equals(mode))
			initSkyChildren(root);
		if (BackdropMode.SKYGRND.equals(mode))
			initSkyGroundChildren(root);
		if (config.isUsingFeature(BODY_HEAD)) {
			initChildren(root, BODY);
			initChildren(root, HEAD);
		}
		initGeoChildren(root);
		initChildren(root, PIC);
		initChildren(root, SPRIT);
		initChildren(root, TITLE);
		initWallChildren(root);

		return root;
	}

	private void initChildren(MutableTreeNode root, ContentType type) throws IOException {
		Set<Integer> ids = new TreeSet<>(loader.idsFor(type));
		if (!ids.isEmpty()) {
			MutableTreeNode parent = new DefaultMutableTreeNode(type.name());
			for (Integer id : ids) {
				Object res;
				if (MONCHA.equals(type))
					res = new IdTypeResource(id, type);
				else
					res = new ImageResource(id, type);
				parent.insert(new DefaultMutableTreeNode(res), parent.getChildCount());
			}
			root.insert(parent, root.getChildCount());
		}
	}

	private void initChildren(MutableTreeNode root, String name, int id) {
		MutableTreeNode parent = new DefaultMutableTreeNode(name);
		parent.insert(new DefaultMutableTreeNode(new ImageResource(id, null)), parent.getChildCount());
		root.insert(parent, root.getChildCount());
	}

	private void initWallChildren(MutableTreeNode root) throws IOException {
		Set<Integer> ids = new TreeSet<>(loader.idsFor(WALLDEF));
		if (ids.isEmpty()) {
			ids = new TreeSet<>(loader.idsFor(_8X8D));
		}

		MutableTreeNode parent = new DefaultMutableTreeNode(WALLDEF.name());
		for (Integer id : ids) {
			parent.insert(new DefaultMutableTreeNode(new DungeonResource(id)), parent.getChildCount());
		}
		root.insert(parent, root.getChildCount());
	}

	private void initFrameChildren(MutableTreeNode root) {
		MutableTreeNode parent = new DefaultMutableTreeNode("FRAME");
		for (UIFrame f : UIFrame.values()) {
			if (f != UIFrame.NONE && (f != UIFrame.SPACE || isSpaceFrameUsed()))
				parent.insert(new DefaultMutableTreeNode(f), parent.getChildCount());
		}
		root.insert(parent, root.getChildCount());
	}

	private void initSkyChildren(MutableTreeNode root) {
		MutableTreeNode parent = new DefaultMutableTreeNode("SKY");
		parent.insert(new DefaultMutableTreeNode(ImageResource.SKY_CLOUD), parent.getChildCount());
		parent.insert(new DefaultMutableTreeNode(ImageResource.SKY_SUN), parent.getChildCount());
		parent.insert(new DefaultMutableTreeNode(ImageResource.SKY_STREET), parent.getChildCount());
		root.insert(parent, root.getChildCount());
	}

	private void initSkyGroundChildren(MutableTreeNode root) throws IOException {
		MutableTreeNode parent = new DefaultMutableTreeNode("SKYGRND");
		Optional<File> skygrndFile = loader.toFile("SKYGRND.DAX");
		if (skygrndFile.isPresent()) {
			Optional<ContentFile> cf = DAXFile.create(skygrndFile.get());
			if (cf.isPresent()) {
				for (int id : cf.get().getIds()) {
					parent.insert(new DefaultMutableTreeNode(new ImageResource("SKYGRND.DAX", id, _8X8D)), parent.getChildCount());
				}
			}
		}
		root.insert(parent, root.getChildCount());
	}

	private void initGeoChildren(MutableTreeNode root) throws IOException {
		Set<Integer> ids = new TreeSet<>(loader.idsFor(GEO));

		MutableTreeNode parent = new DefaultMutableTreeNode(GEO.name());
		for (Integer id : ids) {
			DungeonMap dungeon;
			if (config.isUsingFeature(FLEXIBLE_DUNGEON_SIZE)) {
				dungeon = loader.find(id, DungeonMap2.class, GEO);
			} else {
				dungeon = loader.find(id, DungeonMap.class, GEO);
			}
			if (config.isUsingFeature(OVERLAND_DUNGEON) && id >= 21 && id <= 26)
				parent.insert(new DefaultMutableTreeNode( //
					new DungeonMapResource(dungeon.generateOverlandMap(), //
						new DungeonResource(new int[] { 18, 2 * id + 8, 2 * id + 9 }))),
					parent.getChildCount());
			else
				parent.insert(new DefaultMutableTreeNode(new DungeonMapResource(dungeon.generateWallMap())), parent.getChildCount());
		}
		root.insert(parent, root.getChildCount());
	}

	private boolean isSpaceFrameUsed() {
		return config.isUsingFeature(GameFeature.SPACE_TRAVEL);
	}

	public void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}
}
