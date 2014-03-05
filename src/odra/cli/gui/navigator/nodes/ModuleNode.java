//
//  ModuleInstanceNode.java
//  Odra
//
//  Created by Michal Lentner on 05-06-27.
//  Copyright 2005 PJIIT. All rights reserved.
//

package odra.cli.gui.navigator.nodes;

import odra.cli.gui.images.IconStore;
import javax.swing.ImageIcon;

/**
 * 
 * @author raist
 */

public class ModuleNode extends NavigatorNode {
	public String modGlobalName = "";

	public boolean valid = true;
	public boolean compiled;
	
	public ModuleNode(String locname, String glbname, boolean uncompiled) {
		super(NodeKinds.MODULE_NODE, locname);
		
		modGlobalName = glbname;
		compiled = !uncompiled;
	}

	public ImageIcon getIcon() {
		return compiled ? IconStore.getInstance().moduleIcon : IconStore.getInstance().moduleChangedIcon;
	}
}
