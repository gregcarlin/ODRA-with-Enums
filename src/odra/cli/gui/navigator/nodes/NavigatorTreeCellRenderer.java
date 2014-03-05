//
//  NavigatorTreeCellRenderer.java
//  Odra
//
//  Created by Michal Lentner on 05-07-20.
//  Copyright 2005 PJIIT. All rights reserved.
//

package odra.cli.gui.navigator.nodes;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import odra.system.config.ConfigDebug;

/**
 * 
 * @author raist
 */

public class NavigatorTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (ConfigDebug.ASSERTS)
            	assert value instanceof NavigatorNode : "invalid node " + value.getClass().getCanonicalName();

            NavigatorNode node = (NavigatorNode) value;

            setIcon(node.getIcon());

    		return this;
		}
}
