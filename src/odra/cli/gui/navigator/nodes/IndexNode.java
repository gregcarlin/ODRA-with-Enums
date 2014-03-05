package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class IndexNode extends NavigatorNode {
	private String sysname;

	public IndexNode(String name) {
		super(NodeKinds.INDEX_NODE, name);

		sysname = name;
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().indexIcon;
	}
}
