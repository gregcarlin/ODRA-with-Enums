package odra.cli.gui.components;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;

/**
 * CLI GUI frame class.
 *
 * @author jacenty
 * @version 2007-06-08
 * @since 2007-06-08
 */
public class CLIFrame extends JFrame
{
	@Override
	public void setVisible(boolean aFlag)
	{
		if(aFlag)
			center();
		
		super.setVisible(aFlag);
	}
	
	/**
	 * Centers the frame on the screen.
	 */
	private final void center()
	{
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    Dimension size = getSize();
    
    if(size.width > bounds.width)
    	size.setSize(new Dimension(bounds.width, size.height));
    if(size.height > bounds.height)
    	size.setSize(new Dimension(size.width, bounds.height));
    
    setLocation((bounds.width - size.width) / 2, (bounds.height - size.height) / 2);
	}
}
