package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

/**
 * 
 * @author raist
 */

public class ConfigNode extends NavigatorNode {
	public ConfigNode(String name) {
		super(NodeKinds.CONFIG_NODE, name);
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().switcherIcon;
	}
}
