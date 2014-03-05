package odra.cli.gui.navigator.events;

import java.awt.event.*;
import odra.cli.gui.navigator.nodes.*;

/** 
 * @author raist
 */

public class NavigatorActionEvent extends ActionEvent {
	private String[] strpars;
	private NavigatorNode node;
	
	public NavigatorActionEvent(Object source, int id, String[] pars) {
		super(source, id, null);
		
		this.strpars = pars;
	}

	public NavigatorActionEvent(Object source, int id, NavigatorNode node) {
		super(source, id, null);
		
		this.node = node;
	}
	
	public String[] getStringParameters() {
		return strpars;
	}

	public NavigatorNode getNavigatorNodeParameter() {
		return node;
	}
	
	public final static int SHOW_VARIABLE_EVNT = 1;
	public final static int SAVE_MODULE_EVNT = 2;
	public final static int COMPILE_EVNT = 3;
	public final static int TREE_WILL_EXPAND_EVNT = 4;
	public final static int EDIT_MODULE_EVNT = 5;
	public final static int CREATE_MODULE_EVNT = 6;
}
