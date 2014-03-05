package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class ViewNode extends NavigatorNode {
	private String sysname;

	public ViewNode(String name) {
		super(NodeKinds.VIEW_NODE, name);

		sysname = name;
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().viewIcon;
	}
}
