package tests.review.components;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ValuePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel label = null;
	private JTextField valueTextField = null;
	
	private final String labelText;
	private final int columns;

	/**
	 * This is the default constructor
	 */
	public ValuePanel(String labelText, int columns)
	{
		super();
		
		this.labelText = labelText; 
		this.columns = columns;
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		label = new JLabel();
		label.setText(labelText);
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.NORTH);
		this.add(getValueTextField(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes valueTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getValueTextField()
	{
		if(valueTextField == null)
		{
			valueTextField = new JTextField();
			valueTextField.setColumns(columns);
			valueTextField.setEditable(false);
		}
		return valueTextField;
	}
	
	public void setValue(String text)
	{
		valueTextField.setText(text);
	}
	
	public String getValue()
	{
		return valueTextField.getText();
	}
	
	@Override
	public void setEnabled(boolean aFlag)
	{
		valueTextField.setEnabled(aFlag);
	}
	
	public void clear()
	{
		valueTextField.setText("");
	}
}
