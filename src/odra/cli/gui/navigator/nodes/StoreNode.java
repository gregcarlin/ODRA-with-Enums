package odra.cli.gui.navigator.nodes;

import javax.swing.ImageIcon;

import odra.cli.gui.images.IconStore;

public class StoreNode extends NavigatorNode {	
	public StoreNode(int kind, String name) {
		super(kind, name);
	}
	
	public boolean isTransient() {
		return kind == NodeKinds.TRANSIENT_STORE_NODE;
	}
	
	public boolean isPersistent() {
		return kind == NodeKinds.PERSISTENT_STORE_NODE;
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().storeIcon; 
	}
}
