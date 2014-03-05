//
//  SystemRootNode.java
//  Odra
//
//  Created by Michal Lentner on 05-06-27.
//  Copyright 2005 PJIIT. All rights reserved.
//

package odra.cli.gui.navigator.nodes;

import odra.cli.gui.images.*;
import javax.swing.ImageIcon;

/**
 * 
 * @author raist
 */

public class SystemRootNode extends NavigatorNode {
	public SystemRootNode(String name) {
		super(NodeKinds.SYSTEM_ROOT_NODE, name);
	}
	
	public ImageIcon getIcon() {
		return IconStore.getInstance().worldIcon;
	}
}
