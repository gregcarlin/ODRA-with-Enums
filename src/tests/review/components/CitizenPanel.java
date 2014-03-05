package tests.review.components;

import java.awt.FlowLayout;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JPanel;
import odra.cli.gui.components.layout.ColumnLayout;
import odra.jobc.JOBCException;
import tests.review.demo.objects.Address;
import tests.review.demo.objects.Citizen;

/**
 * Citizen display panel.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-30
 */
public class CitizenPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JPanel panel1 = null;
	private JPanel panel2 = null;
	
	private ValuePanel peselPanel = new ValuePanel("PESEL", 11);
	private ValuePanel birthDatePanel = new ValuePanel("birth date", 10);
	private ValuePanel sexPanel = new ValuePanel("sex", 1);
	
	private ValuePanel surnamePanel = new ValuePanel("surname", 20);
	private ValuePanel name1Panel = new ValuePanel("name 1", 10);
	private ValuePanel name2Panel = new ValuePanel("name 2", 10);
	
	private ValuePanel nationalityPanel = new ValuePanel("nationality", 15);
	private AddressPanel addressPanel = new AddressPanel();
	
	/**
	 * This is the default constructor
	 */
	public CitizenPanel()
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
		this.add(getPanel1(), null);
		this.add(getPanel2(), null);
		this.add(addressPanel, null);
	}
	
	public void clear()
	{
		nationalityPanel.clear();
		peselPanel.clear();
		birthDatePanel.clear();
		sexPanel.clear();
		
		surnamePanel.clear();
		name1Panel.clear();
		name2Panel.clear();
		
		addressPanel.clear();
	}
	
	public void setCitizen(Citizen citizen) throws JOBCException
	{
		if(citizen == null)
			return;
		
		nationalityPanel.setValue(citizen.getNationality().getName());
		peselPanel.setValue(citizen.getPesel());
		birthDatePanel.setValue(DateFormat.getDateInstance(DateFormat.MEDIUM).format(citizen.getBirthDate()));
		sexPanel.setValue(citizen.getSex());
		
		surnamePanel.setValue(citizen.getSurname());
		name1Panel.setValue(citizen.getName1());
		name2Panel.setValue(citizen.getName2());
		
		Object[] currentAddress = citizen.getCurrentAddress();
		addressPanel.setValue((Address)currentAddress[0], (Date)currentAddress[1]);
	}

	/**
	 * This method initializes panel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanel1()
	{
		if(panel1 == null)
		{
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			panel1 = new JPanel();
			panel1.setLayout(flowLayout);

			panel1.add(nationalityPanel);
			panel1.add(peselPanel);
			panel1.add(birthDatePanel);
			panel1.add(sexPanel);
		}
		return panel1;
	}

	/**
	 * This method initializes panel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanel2()
	{
		if(panel2 == null)
		{
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(FlowLayout.LEFT);
			panel2 = new JPanel();
			panel2.setLayout(flowLayout1);
			
			panel2.add(surnamePanel);
			panel2.add(name1Panel);
			panel2.add(name2Panel);
		}
		return panel2;
	}
}
