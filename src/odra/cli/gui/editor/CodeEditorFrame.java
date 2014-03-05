package odra.cli.gui.editor;

import java.io.BufferedReader;
import java.util.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;

import odra.cli.gui.navigator.events.NavigatorActionEvent;

import java.io.*;

/**
 * 
 * @author raist
 */

public class CodeEditorFrame extends JFrame {
	private JPanel mainPanel = new JPanel();

	private JScrollPane mainScrollPane = new JScrollPane();

	private JTextPaneWithoutWrapping inputArea = new JTextPaneWithoutWrapping();

	private JMenuBar menuBar = new JMenuBar();
	private JMenu mainMenu = new JMenu();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem menuSave = new JMenuItem("Save");

	private String module, parent;
	
	private JMenu menuEdit = new JMenu("Edit");

	// protected UndoAction undoAction;

	// protected RedoAction redoAction;

	protected UndoManager undo = new UndoManager();
	public boolean textChanged = false;
	public String moduleLocalName, moduleGlobalName;
	private Vector<ActionListener> listeners = new Vector();
	private SBQLDocument doc;
	
	public void addCodeEditorActionListener(ActionListener lsnr) {
		listeners.addElement(lsnr);
	}

	public CodeEditorFrame(String title, String module, String parent) {
		this(title, "", module, parent);
	}

	public CodeEditorFrame(String title, String text, String module, String parent) {
		super(title);

		this.module = module;
		this.parent = parent;
		
		// poprawk KubaStaszczyk 15.11
		// undoAction = new UndoAction();
		// JMenuItem undoMenuItem = menuEdit.add(undoAction);
		// undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		// InputEvent.CTRL_MASK));

		// redoAction = new RedoAction();
		// JMenuItem redoMenuItem = menuEdit.add(redoAction);
		// redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
		// InputEvent.CTRL_MASK));


		
		// okno
		mainPanel.setLayout(new GridLayout());

		this.setContentPane(mainPanel);
		this.addWindowListener(new CloseListener());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// edytorek
		inputArea.setFont(new Font("Courier", 0, 12));
		 doc = new SBQLDocument();
//		doc.addDocumentListener(new ChangeListener());

		inputArea.setDocument(doc);

		// troche to glupie, ale nie umiem inaczej
		TabStop[] tabs = new TabStop[1000];
		for (int i = 0; i < tabs.length; i++)
			tabs[i] = new TabStop(30 * (i + 1), TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
		TabSet tabset = new TabSet(tabs);

		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabset);
		inputArea.setParagraphAttributes(aset, false);

		mainScrollPane.getViewport().add(inputArea);

		mainScrollPane.setRowHeaderView(new LineNumber(inputArea));
		mainPanel.add(mainScrollPane);

		// menu

		// poprawka KubaStaszczyk 15.11
		JMenuItem findMenuItem = new JMenuItem("Find");
		findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		findMenuItem.addActionListener(new FindListener());
		menuEdit.add(findMenuItem);

		menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK, false));
		menuSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				saveAct();
			}
		});
		menuFile.add(menuSave);

		// menuRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
		// KeyEvent.META_MASK, false));
		// menuRun.addActionListener(new RunListener());
		// menuBuild.add(menuRun);

		// menuCompile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
		// KeyEvent.META_MASK, false));
		// menuCompile.addActionListener(new CompileListener());
		// menuBuild.add(menuCompile);

		menuBar.add(menuFile);
		menuBar.add(menuEdit);

		setJMenuBar(menuBar);

		// poprawka KubaStaszczyk 15.11
		// inputArea.addMouseListener(CodeEditorFrame.popupListener);
		// MyUndoableEditListener m = new MyUndoableEditListener();
		// inputArea.getStyledDocument().addUndoableEditListener(m);
		// undo.discardAllEdits();

		initDocumentKey(); // poprawka KubaStaszczyk 15.11

	}

	public String getModule() {
		return module;
	}

	public String getParentModule() {
		return parent;
	}

	// poprawka KubaStaszczyk 15.11
	private void initDocumentKey() {
		InputMap inputMap = inputArea.getInputMap();
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.selectAllAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.selectLineAction);
	}

	private class FindListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			// JFindDlg findDlg = new JFindDlg(CodeEditorFrame.this, "Find", true);

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// Dimension frameSize = findDlg.getSize();
			// if (frameSize.height > screenSize.height) {
			// frameSize.height = screenSize.height;
			// }
			// if (frameSize.width > screenSize.width) {
			// frameSize.width = screenSize.width;
			// }
			// findDlg.setLocation((screenSize.width - frameSize.width) / 2,
			// (screenSize.height - frameSize.height) / 2);
			// findDlg.setVisible(true);
		}
	}

	private void saveAct() {
		for (ActionListener lsnr : listeners)
			lsnr.actionPerformed(new NavigatorActionEvent(this, CodeEditorActionEvent.CODE_SAVE_EVNT, new String[] {  }));		
	}

	// reagowanie na komunikaty wewnatrz edytora
	private class DebugDataStoreListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			// reportPrintDataStore();
		}
	}

	private class DebugMetaDataStoreListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			// reportPrintMetaDataStore();
		}
	}

	private class DebugModuleStoreListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			// reportPrintModuleStore();
		}
	}

	private class ChangeListener implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			textChanged = true;
			// reportModification();
		}

		public void removeUpdate(DocumentEvent e) {
			textChanged = true;
			// reportModification();
		}

		public void changedUpdate(DocumentEvent e) {
			textChanged = true;
			// reportModification();
		}
	}

	private class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent ev) {
			// if (destroyAllowed())
			// setVisible(false);
			// }
		}

		private class SaveListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				// reportSave();
			}
		}

		private class RunListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				// reportRun();
			}
		}

		private class CompileListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				// reportCompile();
			}
		}

		// reagowanie na komunikaty z zewnatrz edytora
		public boolean destroyAllowed() {
			if (textChanged) {
				int i = JOptionPane.showConfirmDialog(CodeEditorFrame.this, "Do you want to save the module source before closing?", "Module not saved", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				switch (i) {
					case JOptionPane.YES_OPTION:
						// zapisz modul
						break;

					case JOptionPane.CANCEL_OPTION:
						return false;
				}
			}

			return true;
		}

		public String getInput() {
			return inputArea.getText();
		}

		public void setInput(String mod, String src) {
			setTitle(mod);
			moduleGlobalName = mod;

			inputArea.setText(src);

			textChanged = false;
		}
	}

	public void setSourceCode(String source) {
		inputArea.setText(source);
	}
	
	// poprawka Kuba Staszczyk 15.11

	protected class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			// undoAction.updateUndoState();
			// redoAction.updateRedoState();
		}
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			}
			catch (CannotUndoException ex) {
				ex.printStackTrace();
			}
			updateUndoState();
			// redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			}
			else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			}
			catch (CannotRedoException ex) {
				ex.printStackTrace();
			}
			updateRedoState();
			// undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			}
			else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}

	private static final JPopupMenu cutPasteCopyPopUp;
	private static final MouseListener popupListener;
	private static JTextComponent cutPasteCopyInvoker;
	private static JMenuItem cut;
	private static JMenuItem copy;
	private static JMenuItem paste;

	static {
		cutPasteCopyPopUp = new JPopupMenu();

		popupListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.isMetaDown()) {
					JTextComponent invoker = (JTextComponent) e.getSource();

					if (invoker.getSelectedText() == null) {
						cut.setEnabled(false);
						copy.setEnabled(false);
					}
					else {
						cut.setEnabled(true);
						copy.setEnabled(true);
					}

					cutPasteCopyInvoker = invoker;
					cutPasteCopyPopUp.show(invoker, e.getX(), e.getY());
				}
			};
		};

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();

				try {
					String text = cutPasteCopyInvoker.getSelectedText();

					if (cmd.equals("cut")) {
						if (text == null)
							return;

						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
						String all = cutPasteCopyInvoker.getText();
						cutPasteCopyInvoker.setText(all.substring(0, cutPasteCopyInvoker.getSelectionStart()) + all.substring(cutPasteCopyInvoker.getSelectionEnd(), all.length()));
					}
					else if (cmd.equals("copy")) {
						if (text == null)
							return;

						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
					}
					else if (cmd.equals("paste")) {
						DataFlavor flavor = new DataFlavor("application/x-java-serialized-object; " + "class=java.lang.String");

						BufferedReader br = new BufferedReader(flavor.getReaderForText(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null)));
						StringBuffer sb = new StringBuffer();
						String str;
						while ((str = br.readLine()) != null) {
							sb.append(str);
						}

						if (text != null)
							cutPasteCopyInvoker.replaceSelection(sb.toString());
						else {
							StringBuffer allT = new StringBuffer(cutPasteCopyInvoker.getText());
							allT.insert(cutPasteCopyInvoker.getCaretPosition(), sb.toString());
							cutPasteCopyInvoker.setText(allT.toString());
						}
					}
				}
				catch (Exception se) {
					System.err.println(se.getMessage());
				}
			}
		};

		cut = new JMenuItem("Wytnij");
		copy = new JMenuItem("Kopiuj");
		paste = new JMenuItem("Wklej");

		cut.addActionListener(al);
		copy.addActionListener(al);
		paste.addActionListener(al);

		cutPasteCopyPopUp.add(cut);
		cutPasteCopyPopUp.add(copy);
		cutPasteCopyPopUp.add(paste);

		cut.setActionCommand("cut");
		copy.setActionCommand("copy");
		paste.setActionCommand("paste");

		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
	}

	public String getText() {
		return inputArea.getText();
	}

	public void setText(String text) {
		inputArea.setText(text);
	}
}
