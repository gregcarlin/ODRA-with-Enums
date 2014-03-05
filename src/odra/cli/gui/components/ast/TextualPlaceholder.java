package odra.cli.gui.components.ast;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import odra.sbql.ast.AST2TextQueryDumper;

/**
 * Textual tree branch placeholder.
 * 
 * @author jacenty
 * @version 2007-12-01
 * @since 2007-12-01
 */
class TextualPlaceholder extends JLabel
{
	/** maximum text line length in tree */
	private final int MAX_LINE_LENGTH = 30;
	
	/** parent {@link NodePanel} */
	private NodePanel parent;
	
	/**
	 * This method initializes 
	 * 
	 * @param parent parent {@link NodePanel}
	 */
	public TextualPlaceholder(NodePanel parent)
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
		this.setBackground(new Color(230, 230, 230));
		this.setOpaque(true);
		
		String value = "<html><div align=\"left\"><tt>";
		try
		{
			value += parent.formatValue(AST2TextQueryDumper.AST2Text(parent.getNode()), MAX_LINE_LENGTH);
		}
		catch (Exception exc)
		{
			value += exc.getMessage();
		}
		value += "</tt></div></html>";
		setText(value);
		
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
