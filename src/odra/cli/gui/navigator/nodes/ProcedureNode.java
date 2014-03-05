package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class ProcedureNode extends NavigatorNode {
	public ProcedureNode(String name) {
		super(NodeKinds.PROCEDURE_NODE, name);
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().procedureIcon;
	}
}
