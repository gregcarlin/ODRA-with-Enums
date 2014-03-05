package odra.cli.gui.components.ast;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Collapsed tree branch placeholder.
 * 
 * @author jacenty
 * @version 2007-12-01
 * @since 2007-11-30
 */
class CollapsedPlaceholder extends JLabel
{
	/** parent {@link NodePanel} */
	private NodePanel parent;
	
	/**
	 * This method initializes 
	 * 
	 * @param parent parent {@link NodePanel}
	 */
	public CollapsedPlaceholder(NodePanel parent)
	{
		super();
		
		this.parent = parent;
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize()
	{
		this.setBackground(Color.red);
		this.setOpaque(true);
		this.setText("(...)");
		this.setHorizontalAlignment(SwingConstants.CENTER);
		
		Font font = getFont();
		font = new Font(font.getName(), font.getStyle(), parent.getFontSize());
		setFont(font);
		
		this.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2)
					parent.switchState(e.getButton() == MouseEvent.BUTTON1);
			}
		});
	}
}
