package odra.cli.gui.editor;

import java.awt.event.ActionEvent;

/**
 * 
 * @author raist
 */

public class CodeEditorActionEvent extends ActionEvent {
	private String[] strpars;
	
	public CodeEditorActionEvent(Object source, int id, String[] pars) {
		super(source, id, null);
		
		this.strpars = pars;
	}

	public String[] getStringParameters() {
		return strpars;
	}

	public final static int CODE_SAVE_EVNT = 1;
}
