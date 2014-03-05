package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class VariableNode extends NavigatorNode {
	public VariableNode(String name) {
		super(NodeKinds.VARIABLE_NODE, name);
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().variableIcon;
	}
}
