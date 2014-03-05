package odra.cli.gui.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import odra.cli.CLI;
import odra.cli.CLIVariable;
import odra.cli.gui.editor.CodeEditorFrame;
import odra.cli.gui.inquirer.InquirerController;
import odra.cli.gui.navigator.events.NavigatorActionEvent;
import odra.cli.gui.navigator.nodes.ModuleNode;
import odra.cli.gui.navigator.nodes.NavigatorNode;
import odra.cli.gui.navigator.nodes.NodeKinds;
import odra.exceptions.rd.RDException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigDebug;

/**
 * 
 * @author raist
 */

public class NavigatorController {
	private NavigatorFrame frame;
	private CLI cli;	
	
	public NavigatorController(CLI cli) {
		this.cli = cli;
		frame = new NavigatorFrame(cli);
		
		frame.setSize(200, 450);
		frame.setLocation(200, 200);
		frame.addNavigatorActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				if (ConfigDebug.ASSERTS) assert evnt instanceof NavigatorActionEvent : "expected simple navigator action event";

				NavigatorActionEvent sevnt = (NavigatorActionEvent) evnt;
				
				switch (sevnt.getID()) {
					case NavigatorActionEvent.EDIT_MODULE_EVNT: {
						if (ConfigDebug.ASSERTS) assert sevnt.getStringParameters() != null && sevnt.getStringParameters().length == 1 : "invalid event parameters";

						String module = sevnt.getStringParameters()[0];
						StringBuffer strbuf = new StringBuffer("");

						ModuleNode mod = frame.findModuleNode(module);
						String parname = mod.getParent() instanceof ModuleNode ? ((ModuleNode) mod.getParent()).modGlobalName : null;

						try {
							BufferedReader reader = new BufferedReader(new FileReader(ConfigClient.SOURCE_CODE_PATH + module + ".sbql"));

							String line;
							while (true) {
								line = reader.readLine();

								if (line == null)
									break;
								
								strbuf.append(line + "\n");
							}

							openNewCodeEditorFrame(module, module, parname).setSourceCode(strbuf.toString());
						}
						catch (IOException ex) {
							JOptionPane.showMessageDialog(frame, ex.getMessage(), "Source code unavailable", JOptionPane.ERROR_MESSAGE, null);							
						}

						break;
					}
						
					case NavigatorActionEvent.COMPILE_EVNT: {
						if (ConfigDebug.ASSERTS) assert sevnt.getStringParameters() != null && sevnt.getStringParameters().length == 1 : "invalid event parameters";

						String module = sevnt.getStringParameters()[0];
						
						compileModuleEvnt(module);
						
						try {
							refreshModule("admin");
						}
						catch (Exception ex) {
							JOptionPane.showMessageDialog(frame, ex.getMessage(), "Compilation error", JOptionPane.ERROR_MESSAGE, null);
						} 
						
						break;
					}

					case NavigatorActionEvent.CREATE_MODULE_EVNT: {
						String current = sevnt.getStringParameters()[0];
						String parent = sevnt.getStringParameters()[1];

						createModuleEvnt(current, parent);

						break;
					}
					
					case NavigatorActionEvent.SHOW_VARIABLE_EVNT: {
						if (ConfigDebug.ASSERTS) assert sevnt.getStringParameters() != null && sevnt.getStringParameters().length == 2 : "invalid event parameters";

						String variable = sevnt.getStringParameters()[0];
						String module = sevnt.getStringParameters()[1];						

						showVariableEvnt(variable, module);

						break;
					}
						
					case NavigatorActionEvent.SAVE_MODULE_EVNT: {						
						String source = sevnt.getStringParameters()[0];
						String module = sevnt.getStringParameters()[1];	

						CodeEditorFrame frame = (CodeEditorFrame) sevnt.getSource();
						
						saveModuleEvnt(source, module, frame);

						try {
							refreshModule(frame.getParentModule());
						}
						catch (Exception ex) {
							ex.printStackTrace();
							
							JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database inspection error", JOptionPane.ERROR_MESSAGE, null);
						}

						break;
					}

					case NavigatorActionEvent.TREE_WILL_EXPAND_EVNT: {
						NavigatorNode node = sevnt.getNavigatorNodeParameter();

						if (node instanceof ModuleNode) {
							ModuleNode module = (ModuleNode) node;
							
							try {
								refreshModule(module.modGlobalName);
							}
							catch (Exception ex) {
								JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database inspection error", JOptionPane.ERROR_MESSAGE, null);
							}
						}
						
						break;
					}
				}
			}
		});
	}

	public CodeEditorFrame openNewCodeEditorFrame(String title, String module, String parent) {		
		CodeEditorFrame cframe = new CodeEditorFrame(title, module, parent);
		cframe.setSize(400, 300);
		cframe.setLocation(100, 100);
		cframe.setVisible(true);
		cframe.addCodeEditorActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				CodeEditorFrame frame = (CodeEditorFrame) evnt.getSource();
				
				saveModuleEvnt(frame.getText(), frame.getModule(), frame);
				
				try {
					refreshModule(frame.getParentModule());
				}
				catch (Exception ex) {
					ex.printStackTrace();
					
					JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database inspection error", JOptionPane.ERROR_MESSAGE, null);
				}				
			}
		});

		return cframe;
	}
	
	public void openNavigator() throws RDException, IOException {
		frame.setVisible(true);
		
		refreshModule("admin");		
		refreshUsers();
	}

	public void closeNavigator() {
		frame.setVisible(false);
	}

	private void compileModuleEvnt(String module) {
		try {
			cli.execCompile(new String[] { module });
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void createModuleEvnt(String current, String parent) {
		openNewCodeEditorFrame("<new module>", current, parent);	
	}

	private void saveModuleEvnt(String srccode, String pargmodname, CodeEditorFrame frame) {
		try {
			System.out.println("save module " + srccode + " par: " + frame.getParentModule()); 

			String mname = cli.execAddModule(new String[] { srccode }, frame.getParentModule());
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(ConfigClient.SOURCE_CODE_PATH + mname + ".sbql")));
			out.print(srccode);
			out.close();

			frame.setTitle(mname);
			frame.moduleGlobalName = mname;
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, ex.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE, null);
		}
	}

	private void showVariableEvnt(String varname, String gmodname) {
		try {
			InquirerController inq = new InquirerController(cli);
			inq.openReadOnlyInquirer();
			inq.executeQuery(varname + ";", gmodname);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void refreshUsers() throws RDException, IOException {
		// ask the server about its users
		String qry = "sysusers.username;";

		DBRequest req = new DBRequest(DBRequest.EXECUTE_SBQL_RQST, new String[] { qry, "admin", "on", cli.getVar(CLIVariable.TEST) });
		DBReply rep = cli.getConnection().sendRequest(req); 

		byte[] rawres = rep.getRawResult();
		
		QueryResultDecoder decoder = new QueryResultDecoder();
		Result res = decoder.decodeResult(rawres);

		// add user names to the navigator
		for (SingleResult sres : res.elementsToArray())
			frame.addUser(((StringResult) sres).value);
	}

	public void refreshModule(String module) throws RDException, IOException {		
		cli.execLs(module);

		BufferedReader reader = new BufferedReader(new StringReader(cli.getOutput()));

		ModuleNode node = frame.findModuleNode(module);
		
		if (ConfigDebug.ASSERTS)
			assert node != null : "cannot find module " + module;

		node.removeAllChildren();
		
		frame.model.nodeStructureChanged(node);

		String line;
		StringTokenizer tokenizer;
		while (true) {
			line = reader.readLine();
			
			if (line == null)
				break;
		
			tokenizer = new StringTokenizer(line, "\t");

			String kind = tokenizer.nextToken();
			String name = tokenizer.nextToken();

			boolean uncompiled = name.endsWith("*");
			boolean invisible = name.startsWith("$");

			if (uncompiled)
				name = name.substring(0, name.length() - 1);

			if (invisible)
				continue;
			
			switch (decodeKind(kind)) {
				case NodeKinds.MODULE_NODE:
					frame.addModule(name, node.modGlobalName + "." + name, node, uncompiled);
					refreshModule(node.modGlobalName + "." + name);
					break;

				case NodeKinds.LINK_NODE:
					frame.addLink(name, node);
					break;
					
				case NodeKinds.PROCEDURE_NODE:
					frame.addProcedure(name, node);
					break;
					
				case NodeKinds.VIEW_NODE:
					frame.addView(name, node);
					break;
					
				case NodeKinds.VARIABLE_NODE:
					frame.addVariable(name, node);
					break;
					
				case NodeKinds.INDEX_NODE:
					frame.addIndex(name, node);
					break;
			}
		}
	}

	private final int decodeKind(String kind) {
		switch (kind.charAt(0)) {
			case 'M': return NodeKinds.MODULE_NODE;
			case 'L': return NodeKinds.LINK_NODE;
			case 'P': return NodeKinds.PROCEDURE_NODE;
			case 'V': return NodeKinds.VIEW_NODE;
			case 'D': return NodeKinds.VARIABLE_NODE;
			case 'H': if (kind.charAt(1) == 'I') return NodeKinds.INDEX_NODE;
		}
		
		return -1;
	}
}
