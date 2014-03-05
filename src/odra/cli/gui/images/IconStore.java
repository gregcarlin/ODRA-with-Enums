//
//  IconDatabase.java
//  Odra
//
//  Created by Michal Lentner on 05-07-20.
//  Copyright 2005 PJIIT. All rights reserved.
//

package odra.cli.gui.images;

import javax.swing.*;

/**
 * 
 * @author raist
 */

public class IconStore {
	private static IconStore instance = new IconStore();

	public ImageIcon worldIcon;
	public ImageIcon moduleIcon;
	public ImageIcon folderIcon;
	public ImageIcon linkIcon;
	public ImageIcon procedureIcon;
	public ImageIcon userIcon;
	public ImageIcon indexIcon;
	public ImageIcon viewIcon;
	public ImageIcon classIcon;
	public ImageIcon variableIcon;
	public ImageIcon spotlightIcon;
	public ImageIcon moduleChangedIcon;
	public ImageIcon moduleErrorIcon;
	public ImageIcon switcherIcon;
	public ImageIcon storeIcon;
	
	private IconStore() {
		readImages();
	}

	public static IconStore getInstance() {
		return instance;
	}

	private void readImages() {
		worldIcon = new ImageIcon(getClass().getResource("world.gif"));
		moduleIcon = new ImageIcon(getClass().getResource("module.gif"));
		folderIcon = new ImageIcon(getClass().getResource("folder.gif"));
		linkIcon = new ImageIcon(getClass().getResource("link.gif"));
		procedureIcon = new ImageIcon(getClass().getResource("procedure.gif"));
		userIcon = new ImageIcon(getClass().getResource("user.gif"));
		indexIcon = new ImageIcon(getClass().getResource("index.gif"));
		viewIcon = new ImageIcon(getClass().getResource("view.gif"));
		classIcon = new ImageIcon(getClass().getResource("class.gif"));
		variableIcon = new ImageIcon(getClass().getResource("variable.gif"));
		spotlightIcon = new ImageIcon(getClass().getResource("spotlight.gif"));
		moduleChangedIcon = new ImageIcon(getClass().getResource("module-changed.gif"));
		moduleErrorIcon = new ImageIcon(getClass().getResource("module-error.gif"));
		switcherIcon = new ImageIcon(getClass().getResource("switcher.gif"));
		storeIcon = new ImageIcon(getClass().getResource("database.gif"));
	}
}
