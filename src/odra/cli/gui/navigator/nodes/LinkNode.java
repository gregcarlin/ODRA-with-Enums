package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class LinkNode extends NavigatorNode {
	public LinkNode(String name) {
		super(NodeKinds.LINK_NODE, name);
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().linkIcon;
	}
}
