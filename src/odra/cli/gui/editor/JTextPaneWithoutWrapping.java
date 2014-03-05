package odra.cli.gui.editor;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;

/**
 * 
 * @author raist
 */

public class JTextPaneWithoutWrapping extends JTextPane {
	public boolean getScrollableTracksViewportWidth() {
		Component parent = getParent();
		ComponentUI ui = getUI();

		return parent != null ? (ui.getPreferredSize(this).width <= parent.getSize().width) : true;
	}
}
