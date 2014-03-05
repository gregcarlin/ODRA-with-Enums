package odra.cli.gui.navigator;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import odra.cli.CLI;
import odra.cli.gui.editor.CodeEditorFrame;
import odra.cli.gui.navigator.dialogs.StoreFrame;
import odra.cli.gui.navigator.events.NavigatorActionEvent;
import odra.cli.gui.navigator.nodes.ConfigNode;
import odra.cli.gui.navigator.nodes.FolderNode;
import odra.cli.gui.navigator.nodes.IndexNode;
import odra.cli.gui.navigator.nodes.LinkNode;
import odra.cli.gui.navigator.nodes.ModuleNode;
import odra.cli.gui.navigator.nodes.NavigatorNode;
import odra.cli.gui.navigator.nodes.NavigatorTreeCellRenderer;
import odra.cli.gui.navigator.nodes.NodeKinds;
import odra.cli.gui.navigator.nodes.ProcedureNode;
import odra.cli.gui.navigator.nodes.StoreNode;
import odra.cli.gui.navigator.nodes.SystemRootNode;
import odra.cli.gui.navigator.nodes.UserNode;
import odra.cli.gui.navigator.nodes.VariableNode;
import odra.cli.gui.navigator.nodes.ViewNode;
import odra.system.config.ConfigDebug;

/**
 * 
 * @author raist
 */

public class NavigatorFrame extends JFrame {
	private final CLI cli;
	
	private JPanel mainPanel = new JPanel();
	private JScrollPane mainScrollPane = new JScrollPane();
	public JTree mainTree = new JTree();

	private JPopupMenu modulePopupMenu = new JPopupMenu();
	private JPopupMenu hostPopupMenu = new JPopupMenu();
	private JPopupMenu variablePopupMenu = new JPopupMenu();
	private JPopupMenu linkPopupMenu = new JPopupMenu();
	private JPopupMenu userPopupMenu = new JPopupMenu();
	private JPopupMenu procPopupMenu = new JPopupMenu();
	private JPopupMenu storePopupMenu = new JPopupMenu();

	private JMenuItem modpm1, modpm2, modpm3, modpm4, modpm5, modpm6, modpm7, modpm8, modpm9, modpm10, modpm11, modpm12;
	private JMenuItem hodpm1, hodpm2, hodpm3;
	private JMenuItem varpm1, varpm2, varpm3;
	private JMenuItem linkpm1;
	private JMenuItem userpm1;
	private JMenuItem procpm1, procpm2;
	private JMenuItem storepm1;

	public DefaultTreeModel model;

	private SystemRootNode rootNode;
	private FolderNode appobjNode;
	private FolderNode dbobjNode;
	private FolderNode sysNode;
	private FolderNode modNode;
	private FolderNode userNode;
	private ModuleNode rootmodNode;

	private ConfigNode configNode;
	private StoreNode transStoreNode;
	private StoreNode persStoreNode;

	private Vector<ActionListener> listeners = new Vector();
	private NavigatorTreeCellRenderer treeCellRenderer = new NavigatorTreeCellRenderer();

	public NavigatorFrame(CLI cli) {
		super("Navigator");
		
		this.cli = cli;
		
		// tree of database objects	
		rootNode = new SystemRootNode("System");		
		model = new DefaultTreeModel(rootNode);
		
		mainTree.setFont(new Font("Lucida Grande", 0, 11));
		mainTree.setModel(model);
		mainTree.setCellRenderer(treeCellRenderer);
		
		// standard navigator nodes	
		modNode = addFolder("Modules", rootNode);

		appobjNode = addFolder("Application", rootNode);		
		addFolder("Classes", appobjNode);
		addFolder("Procedures", appobjNode);
		addFolder("Variables", appobjNode);
		addFolder("Views", appobjNode);

		dbobjNode = addFolder("Database", rootNode);
		addFolder("Indices", dbobjNode);
		addFolder("Links", dbobjNode);
		userNode = addFolder("Users", dbobjNode);

		sysNode = addFolder("System", rootNode);
		addSettingsNode(sysNode);
		addPersistentStoreNode(sysNode);
		addTransientStoreNode(sysNode);

		rootmodNode = addModule("admin", "admin", modNode, false);
	
		// navigator's menu

		// variable popup menu
		varpm1 = new JMenuItem("Drop");
		varpm2 = new JMenuItem("Delete");
		varpm3 = new JMenuItem("Show contents");
		varpm1.setEnabled(false);
		varpm2.setEnabled(false);
		varpm3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				showContentsAct();
			}
		});
		varpm1.setFont(new Font("Lucida Grande", 0, 11));
		varpm2.setFont(new Font("Lucida Grande", 0, 11));
		varpm3.setFont(new Font("Lucida Grande", 0, 11));
		variablePopupMenu.add(varpm1);
		variablePopupMenu.add(new JSeparator());
		variablePopupMenu.add(varpm2);
		variablePopupMenu.add(varpm3);
		
		// user popup menu
		userpm1 = new JMenuItem("Drop");
		userpm1.setEnabled(false);
		userpm1.setFont(new Font("Lucida Grande", 0, 11));
		userPopupMenu.add(userpm1);			

		// modules popup menu
		modpm1 = new JMenuItem("Drop");
		modpm2 = new JMenuItem("Add empty module");		
		modpm3 = new JMenuItem("Add module from source code");
		modpm4 = new JMenuItem("Edit module source code");
		modpm5 = new JMenuItem("Compile");
		modpm6 = new JMenuItem("Inquire");
		modpm7 = new JMenuItem("Add procedure");
		modpm8 = new JMenuItem("Add variable");
		modpm9 = new JMenuItem("Add view");
		modpm10 = new JMenuItem("Add database link");
		modpm11 = new JMenuItem("Add index");
		modpm12 = new JMenuItem("Import file");
		modpm1.setEnabled(false);
//		modpm4.setEnabled(false);
		modpm6.setEnabled(false);
		modpm7.setEnabled(false);
		modpm8.setEnabled(false);
		modpm9.setEnabled(false);
		modpm10.setEnabled(false);
		modpm11.setEnabled(false);
		modpm12.setEnabled(false);		
		modpm1.setFont(new Font("Lucida Grande", 0, 11));
		modpm2.setFont(new Font("Lucida Grande", 0, 11));
		modpm3.setFont(new Font("Lucida Grande", 0, 11));
		modpm4.setFont(new Font("Lucida Grande", 0, 11));
		modpm5.setFont(new Font("Lucida Grande", 0, 11));
		modpm6.setFont(new Font("Lucida Grande", 0, 11));
		modpm7.setFont(new Font("Lucida Grande", 0, 11));
		modpm8.setFont(new Font("Lucida Grande", 0, 11));
		modpm9.setFont(new Font("Lucida Grande", 0, 11));
		modpm10.setFont(new Font("Lucida Grande", 0, 11));
		modpm11.setFont(new Font("Lucida Grande", 0, 11));
		modpm12.setFont(new Font("Lucida Grande", 0, 11));		
		modulePopupMenu.add(modpm1);
		modulePopupMenu.add(new JSeparator());
		modulePopupMenu.add(modpm2);
		modulePopupMenu.add(modpm3);	
		modulePopupMenu.add(modpm4);
		modulePopupMenu.add(modpm5);
		modulePopupMenu.add(modpm6);
		modulePopupMenu.add(new JSeparator());			
		modulePopupMenu.add(modpm7);
		modulePopupMenu.add(modpm8);
		modulePopupMenu.add(modpm9);
		modulePopupMenu.add(modpm10);
		modulePopupMenu.add(modpm11);
		modulePopupMenu.add(new JSeparator());	
		modulePopupMenu.add(modpm12);
		
		// stores popup menu
		storepm1 = new JMenuItem("Physical structure");
//		storepm1.setEnabled(false);
		storepm1.setFont(new Font("Lucida Grande", 0, 11));
		storePopupMenu.add(storepm1);
		
		storepm1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				showStoreAct();
			}
		});
		
		// procedures popup menu
		procpm1 = new JMenuItem("Drop");
		procpm2 = new JMenuItem("Execute");	
		procpm1.setEnabled(false);
		procpm2.setEnabled(false);
		procpm1.setFont(new Font("Lucida Grande", 0, 11));
		procpm2.setFont(new Font("Lucida Grande", 0, 11));
		procPopupMenu.add(procpm1);
		procPopupMenu.add(new JSeparator());		
		procPopupMenu.add(procpm2);

		modpm1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				dropModuleAct();
			}
		});		

		modpm2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				createEmptyModule();
			}
		});

		modpm3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				createModuleFromSourceAct();
			}
		});
		
		modpm4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				editModuleAct(getModuleNode(getCurrentNavigatorNode()));
			}
		});		

		modpm5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				compileAct();
			}
		});

		// links popup menu
		linkpm1 = new JMenuItem("Drop");
		linkpm1.setEnabled(false);
		linkpm1.setFont(new Font("Lucida Grande", 0, 11));
		linkPopupMenu.add(linkpm1);	
		linkpm1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
//				dropLinkAct();
			}
		});

		// sets up the window
		mainTree.addMouseListener(new TreeMouseListener());
		mainTree.addTreeWillExpandListener(new NavigatorTreeExpansionListener());
		mainScrollPane.getViewport().add(mainTree);
		
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.add(mainScrollPane);

		this.setContentPane(mainPanel);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/********************************
	 * Actions performed on navigator's node
	 */

	public void showStoreAct() {
		new StoreFrame(cli.getConnection(), ((StoreNode)getCurrentNavigatorNode()).isPersistent(), 256 * 1024).setVisible(true);
	}

	public void dropLinkAct() {

	}

	public void createLinkAct() {
		
	}
	
	public void createModuleAct() {
		
	}
	
	public void createEmptyModule() {
		String inputValue = null;

		while (inputValue == null || inputValue.equals(""))
			inputValue = (String) JOptionPane.showInputDialog(this, "Please input a name of the new module", "New module", JOptionPane.INFORMATION_MESSAGE, null, null, "");

		CodeEditorFrame cef = new CodeEditorFrame(null, null, getModuleNode(getCurrentNavigatorNode()).modGlobalName);
		cef.setText("module " + inputValue + " {\n}\n");

		saveModuleAct(cef);
	}


	
	public void createModuleFromSourceAct() {
		NavigatorNode cnode = getCurrentNavigatorNode();
		NavigatorNode pnode = getCurrentParentNavigatorNode();
		
		if (ConfigDebug.ASSERTS) assert cnode instanceof ModuleNode : "Expected a module node";
		
		String cname = getModuleNode(cnode).modGlobalName;
		String pname = pnode instanceof ModuleNode ? getModuleNode(pnode).modGlobalName : null;
	
		System.out.println("Create module: " + cname + " " + pname);

		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(this, NavigatorActionEvent.CREATE_MODULE_EVNT, new String[] { null, cname }));
	}

	public void dropModuleAct() {
		
	}

	public void editModuleAct(ModuleNode mod) {
		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(this, NavigatorActionEvent.EDIT_MODULE_EVNT, new String[] { mod.modGlobalName }));		
	}

	public void treeWillExpandAct(NavigatorNode node) {
		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(this, NavigatorActionEvent.TREE_WILL_EXPAND_EVNT, node));
	}
	
	public void compileAct() {
		NavigatorNode cnode = getCurrentNavigatorNode();
		ModuleNode mnode = getModuleNode(cnode);
		
		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(this, NavigatorActionEvent.COMPILE_EVNT, new String[] { mnode.modGlobalName }));
	}

	public void saveModuleAct(CodeEditorFrame frame) {		
		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(frame, NavigatorActionEvent.SAVE_MODULE_EVNT, new String[] { frame.getText(), frame.moduleGlobalName }));
	}

	public void showContentsAct() {
		NavigatorNode cnode = getCurrentNavigatorNode();
		ModuleNode mnode = getModuleNode(getParentNavigatorNode());

		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(this, NavigatorActionEvent.SHOW_VARIABLE_EVNT, new String[] { cnode.name, mnode.modGlobalName }));
	}

	public void addNavigatorActionListener(ActionListener lsnr) {
		listeners.addElement(lsnr);
	}
	
	/****************************************
	 * Methods used to introduce new tree nodes to the navigator
	 */

	public StoreNode addPersistentStoreNode(NavigatorNode parent) {
		StoreNode node = new StoreNode(NodeKinds.PERSISTENT_STORE_NODE, "Persistent store");
		
		parent.add(node);
		
		return node;
	}
	
	public StoreNode addTransientStoreNode(NavigatorNode parent) {
		StoreNode node = new StoreNode(NodeKinds.TRANSIENT_STORE_NODE, "Transient store");
		
		parent.add(node);
		
		return node;
	}
	
	public ConfigNode addSettingsNode(NavigatorNode parent) {
		ConfigNode node = new ConfigNode("Settings");
		
		parent.add(node);
		
		return node;
	}
	
	public UserNode addUser(String locname) {
		UserNode node = new UserNode(locname);

		model.insertNodeInto(node, userNode, userNode.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}

	public VariableNode addVariable(String locname, NavigatorNode parent) {
		VariableNode node = new VariableNode(locname);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}

	public IndexNode addIndex(String locname, NavigatorNode parent) {
		IndexNode node = new IndexNode(locname);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}

	public FolderNode addFolder(String locname, NavigatorNode parent) {
		FolderNode node = new FolderNode(locname);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}

	public ViewNode addView(String locname, NavigatorNode parent) {
		ViewNode node = new ViewNode(locname);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}
	
	public LinkNode addLink(String locname, NavigatorNode parent) {
		LinkNode node = new LinkNode(locname);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);		

		return node;
	}

	public ProcedureNode addProcedure(String locname, NavigatorNode parent) {
		ProcedureNode node = new ProcedureNode(locname);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}
	
	public ModuleNode addModule(String locname, String glbname, NavigatorNode parent, boolean uncompiled) {
		ModuleNode node = new ModuleNode(locname, glbname, uncompiled);

		model.insertNodeInto(node, parent, parent.getChildCount());
		model.nodeChanged(node);
		
		return node;
	}

	/****************************************
	 * Auxilliary methods used to operate on navigator's tree
	 */
	private final NavigatorNode getCurrentNavigatorNode() {
		TreePath path = mainTree.getSelectionPath();
		
		if (ConfigDebug.ASSERTS)
			assert path.getLastPathComponent() instanceof NavigatorNode : "expected navigator node";
			
		return (NavigatorNode) path.getLastPathComponent();
	}
	
	private final NavigatorNode getCurrentParentNavigatorNode() {			
		return (NavigatorNode) getCurrentNavigatorNode().getParent();
	}		

	private final NavigatorNode getParentNavigatorNode() {
		TreePath path = mainTree.getSelectionPath();
		
		if (ConfigDebug.ASSERTS)
			assert path.getLastPathComponent() instanceof NavigatorNode : "expected navigator node";
			
		NavigatorNode nnode = (NavigatorNode) path.getLastPathComponent();
		
		if (ConfigDebug.ASSERTS)
			assert nnode.getParent() instanceof NavigatorNode : "expected navigator node";
			
		return (NavigatorNode) nnode.getParent();
	}
	
	private ModuleNode getModuleNode(NavigatorNode node) {
		if (ConfigDebug.ASSERTS)
			assert node instanceof ModuleNode : "expected module node";

		return (ModuleNode) node;
	}	
	
	public ModuleNode findModuleNode(String glbname) {
		StringTokenizer tokenizer = new StringTokenizer(glbname, ".");

		NavigatorNode parent = this.modNode;

		if (!tokenizer.hasMoreTokens())
			return (ModuleNode) findNode(glbname, parent, NodeKinds.MODULE_NODE); 

		String token;
		while(true) {
			if (!(tokenizer.hasMoreTokens()))
				break;

			token = tokenizer.nextToken();
						
			parent = (NavigatorNode) findNode(token, parent, NodeKinds.MODULE_NODE);

			if (parent == null)
				return null;
		}

		return (ModuleNode) parent;
	}
	
	private NavigatorNode findNode(String locname, NavigatorNode parent, int nodekind) {
		for (int i = 0; i < model.getChildCount(parent); i++) {
			NavigatorNode node = (NavigatorNode) model.getChild(parent, i);

			if (node.kind == nodekind && locname.equals(node.name))
				return node;
		}
		
		return null;
	}
	
	private class TreeMouseListener extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			Point point = e.getPoint();
			TreePath newPath = mainTree.getPathForLocation(point.x, point.y);
			TreePath path = mainTree.getSelectionPath();
			if (mainTree.getPathForLocation(point.x, point.y) != null && !newPath.equals(path)) {
				path = newPath;
				mainTree.setSelectionPath(path);
			}

			if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
				if (path.getLastPathComponent() instanceof VariableNode)
					variablePopupMenu.show(mainTree, e.getX(), e.getY());
				else if (path.getLastPathComponent() instanceof ModuleNode)
					modulePopupMenu.show(mainTree, e.getX(), e.getY());
				else if (path.getLastPathComponent() instanceof LinkNode)
					linkPopupMenu.show(mainTree, e.getX(), e.getY());
				else if (path.getLastPathComponent() instanceof UserNode)
					userPopupMenu.show(mainTree, e.getX(), e.getY());
				else if (path.getLastPathComponent() instanceof ProcedureNode)
					procPopupMenu.show(mainTree, e.getX(), e.getY());
				else if (path.getLastPathComponent() instanceof StoreNode)
					storePopupMenu.show(mainTree, e.getX(), e.getY());
			}
		}
	}

	private class NavigatorTreeExpansionListener implements TreeWillExpandListener {
		public void treeWillExpand(TreeExpansionEvent event) {
			TreeNode node = (TreeNode) event.getPath().getLastPathComponent();

			treeWillExpandAct((NavigatorNode) node);
		}
		
		public void treeWillCollapse(TreeExpansionEvent event) {
		}
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		this.toFront();
	}
}
