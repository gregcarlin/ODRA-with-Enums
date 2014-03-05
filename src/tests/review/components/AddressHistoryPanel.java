package tests.review.components;

import java.awt.GridBagLayout;
import java.util.Date;
import java.util.Vector;
import javax.swing.JPanel;
import odra.cli.gui.components.layout.ColumnLayout;
import tests.review.demo.objects.Address;

/**
 * Address history display panel.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-30
 */
public class AddressHistoryPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the default constructor
	 */
	public AddressHistoryPanel()
	{
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setSize(300, 200);
		this.setLayout(new ColumnLayout(0, ColumnLayout.Align.LEFT, false));
	}
	
	public void clear()
	{
		removeAll();
		validate();
		repaint();
	}
	
	public void setHistory(Vector<Object[]> history)
	{
		clear();
		
		for(Object[] row : history)
		{
			AddressPanel addressPanel = new AddressPanel();
			addressPanel.setValue((Address)row[0], (Date)row[1]);
			
			add(addressPanel);
		}
		validate();
		repaint();
	}
}
