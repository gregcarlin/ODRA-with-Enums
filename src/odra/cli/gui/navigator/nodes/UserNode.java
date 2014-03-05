package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class UserNode extends NavigatorNode {
	private String sysname;

	public UserNode(String name) {
		super(NodeKinds.USER_NODE, name);

		sysname = name;
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().userIcon;
	}
}
