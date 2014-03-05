package tests.review.components;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JPanel;
import odra.cli.gui.components.layout.RowLayout;
import tests.review.demo.objects.Address;

/**
 * Address display panel.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-30
 */
public class AddressPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ValuePanel addressPanel = new ValuePanel("address", 60);
	private ValuePanel addressSincePanel = new ValuePanel("since", 10);

	/**
	 * This is the default constructor
	 */
	public AddressPanel()
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
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		this.setLayout(flowLayout);
		
		add(addressPanel);
		add(addressSincePanel);
	}
	
	public void clear()
	{
		addressPanel.setValue("");
		addressSincePanel.setValue("");
	}
	
	public void setValue(Address address, Date sinceDate)
	{
		addressPanel.setValue(address.toString());
		addressSincePanel.setValue(DateFormat.getDateInstance(DateFormat.MEDIUM).format(sinceDate));
	}
}
