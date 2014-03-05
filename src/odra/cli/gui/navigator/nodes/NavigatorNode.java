//
//  NavigatorNode.java
//  Odra
//
//  Created by Michal Lentner on 05-06-27.
//  Copyright 2005 PJIIT. All rights reserved.
//

package odra.cli.gui.navigator.nodes;

import javax.swing.tree.*;
import javax.swing.ImageIcon;

/**
 * 
 * @author raist
 */

public abstract class NavigatorNode extends DefaultMutableTreeNode {
		public int kind;
		public String name;
			
		public NavigatorNode(int nodekind, String name) {
			this.kind = nodekind;
			this.name = name;
		}

		public String toString() {
			return name;
		}

		public abstract ImageIcon getIcon();
}
