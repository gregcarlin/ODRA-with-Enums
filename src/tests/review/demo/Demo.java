package tests.review.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import odra.cli.gui.components.layout.ColumnLayout;
import odra.cli.gui.components.layout.RowLayout;
import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import odra.sbql.optimizers.Type;
import tests.review.components.AddressHistoryPanel;
import tests.review.components.CitizenPanel;
import tests.review.demo.objects.Citizen;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;

/**
 * Integration demo GUI.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-29
 */
public class Demo extends JFrame
{
	private Database db = new Database();  //  @jve:decl-index=0:
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JTabbedPane tabbedPane = null;
	private JPanel citizensTabPanel = null;

	private JPanel searchPanel = null;

	private JTextField peselTextField = null;

	private JButton searchCitizenButton = null;

	private JScrollPane peselsScrollPane = null;

	private DefaultListModel peselsModel = new DefaultListModel();
	private JList peselsList = null;

	private JScrollPane displayScrollPane = null;

	private CitizenPanel citizenPanel = null;
	private CitizenPanel fatherPanel = null;
	private CitizenPanel motherPanel = null;

	private JTabbedPane citizenDetailsTabbedPane = null;

	private JPanel citizenAddressesPanel = null;

	private JPanel citizenParentsPanel = null;

	private JPanel displayPanel = null;

	private JScrollPane citizenAddressHistoryScrollPane = null;

	private AddressHistoryPanel citizenAddressHistoryPanel = null;

	/**
	 * This is the default constructor
	 */
	public Demo()
	{
		super();
		initialize();
		
		try
		{
			db.openConnection();
		}
		catch(JOBCException exc)
		{
			showError(exc);
			System.exit(1);
		}
	}
	
	/**
	 * This method initializes searchPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSearchPanel()
	{
		if(searchPanel == null)
		{
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			searchPanel = new JPanel();
			searchPanel.setLayout(flowLayout);
			searchPanel.add(getPeselTextField(), null);
			searchPanel.add(getSearchCitizenButton(), null);
		}
		return searchPanel;
	}

	/**
	 * This method initializes peselTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getPeselTextField()
	{
		if(peselTextField == null)
		{
			peselTextField = new JTextField();
			peselTextField.setColumns(11);
			peselTextField.addKeyListener(new java.awt.event.KeyAdapter()
			{
				public void keyReleased(java.awt.event.KeyEvent e)
				{
					if(e.getKeyCode() == KeyEvent.VK_ENTER)
						searchCitizens();
				}
			});
		}
		return peselTextField;
	}

	/**
	 * This method initializes searchCitizenButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSearchCitizenButton()
	{
		if(searchCitizenButton == null)
		{
			searchCitizenButton = new JButton();
			searchCitizenButton.setText("search");
			searchCitizenButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					searchCitizens();
				}
			});
		}
		return searchCitizenButton;
	}
	
	private void showError(Throwable error)
	{
		showError(error.getLocalizedMessage());
	}
	
	private void showError(String message)
	{
		showMessage(message, JOptionPane.ERROR_MESSAGE);
	}
	
	private void showWaring(String message)
	{
		showMessage(message, JOptionPane.WARNING_MESSAGE);
	}
	
	private void showMessage(String message, int type)
	{
		JOptionPane.showMessageDialog(this, message, "Error", type);
	}
	
	private void searchCitizens()
	{
		peselsModel.removeAllElements();
		
		String pesel = peselTextField.getText();
		try
		{
			Result result = db.execute("((citizens where pesel ~~ \"" + pesel + "%\") orderby pesel).pesel");
			for(int i = 0; i < result.size(); i++)
				peselsModel.addElement(result.get(i).getString());
			
			getPeselsList().setModel(peselsModel);
			
			getPeselsScrollPane().getVerticalScrollBar().setValue(0);
			if(peselsModel.size() > 0)
				getPeselsList().setSelectedIndex(0);
		}
		catch (JOBCException exc)
		{
			showError(exc);
		}
	}
	
	private void clear()
	{
		citizenPanel.clear();
		motherPanel.clear();
		fatherPanel.clear();
		citizenAddressHistoryPanel.clear();
	}
	
	private void showCitizen()
	{
		clear();
		
		if(getPeselsList().getSelectedIndex() >= 0)
		{
			String pesel = getPeselsList().getSelectedValue().toString();
			try
			{
				Citizen citizen = new Citizen(pesel, db);
				citizenPanel.setCitizen(citizen);
				motherPanel.setCitizen(citizen.getMother());
				fatherPanel.setCitizen(citizen.getFather());
				citizenAddressHistoryPanel.setHistory(citizen.getAddressHistory());
			}
			catch (JOBCException exc)
			{
				showError(exc);
			}
		}
	}

	/**
	 * This method initializes peselsScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getPeselsScrollPane()
	{
		if(peselsScrollPane == null)
		{
			peselsScrollPane = new JScrollPane();
			peselsScrollPane.setViewportView(getPeselsList());
			peselsScrollPane.setPreferredSize(new Dimension(100, 0));
		}
		return peselsScrollPane;
	}

	/**
	 * This method initializes peselsList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getPeselsList()
	{
		if(peselsList == null)
		{
			peselsList = new JList(peselsModel);
			peselsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			peselsList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
			{
				public void valueChanged(javax.swing.event.ListSelectionEvent e)
				{
					if(!e.getValueIsAdjusting())
						showCitizen();
				}
			});
		}
		return peselsList;
	}

	/**
	 * This method initializes displayScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDisplayScrollPane()
	{
		if(displayScrollPane == null)
		{
			displayScrollPane = new JScrollPane();
			displayScrollPane.setViewportView(getDisplayPanel());
		}
		return displayScrollPane;
	}

	/**
	 * This method initializes citizenPanel	
	 * 	
	 * @return tests.review.components.CitizenPanel	
	 */
	private CitizenPanel getCitizenPanel()
	{
		if(citizenPanel == null)
		{
			citizenPanel = new CitizenPanel();
			citizenPanel.add(getCitizenDetailsTabbedPane(), null);
		}
		return citizenPanel;
	}
	
	/**
	 * This method initializes motherPanel	
	 * 	
	 * @return tests.review.components.CitizenPanel	
	 */
	private CitizenPanel getMotherPanel()
	{
		if(motherPanel == null)
		{
			motherPanel = new CitizenPanel();
			motherPanel.setBorder(BorderFactory.createTitledBorder(null, "mother", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), Color.black));
		}
		return motherPanel;
	}
	
	/**
	 * This method initializes fatherPanel	
	 * 	
	 * @return tests.review.components.CitizenPanel	
	 */
	private CitizenPanel getFatherPanel()
	{
		if(fatherPanel == null)
		{
			fatherPanel = new CitizenPanel();
			fatherPanel.setBorder(BorderFactory.createTitledBorder(null, "father", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), Color.black));
		}
		return fatherPanel;
	}

	/**
	 * This method initializes citizenDetailsTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getCitizenDetailsTabbedPane()
	{
		if(citizenDetailsTabbedPane == null)
		{
			citizenDetailsTabbedPane = new JTabbedPane();
			citizenDetailsTabbedPane.addTab("address history", null, getCitizenAddressesPanel(), null);
			citizenDetailsTabbedPane.addTab("parents", null, getCitizenParentsPanel(), null);
		}
		return citizenDetailsTabbedPane;
	}

	/**
	 * This method initializes citizenAddressesPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCitizenAddressesPanel()
	{
		if(citizenAddressesPanel == null)
		{
			citizenAddressesPanel = new JPanel();
			citizenAddressesPanel.setLayout(new BorderLayout());
			citizenAddressesPanel.add(getCitizenAddressHistoryScrollPane(), BorderLayout.CENTER);
		}
		return citizenAddressesPanel;
	}

	/**
	 * This method initializes citizenParentsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCitizenParentsPanel()
	{
		if(citizenParentsPanel == null)
		{
			citizenParentsPanel = new JPanel();
			citizenParentsPanel.setLayout(new ColumnLayout(0, ColumnLayout.Align.LEFT, false));
			
			citizenParentsPanel.add(getMotherPanel());
			citizenParentsPanel.add(getFatherPanel());
		}
		return citizenParentsPanel;
	}

	/**
	 * This method initializes displayPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDisplayPanel()
	{
		if(displayPanel == null)
		{
			displayPanel = new JPanel();
			displayPanel.setLayout(new BorderLayout());
			
			displayPanel.add(getCitizenPanel(), BorderLayout.NORTH);
			displayPanel.add(getCitizenDetailsTabbedPane(), BorderLayout.CENTER);
		}
		return displayPanel;
	}

	/**
	 * This method initializes citizenAddressHistoryScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCitizenAddressHistoryScrollPane()
	{
		if(citizenAddressHistoryScrollPane == null)
		{
			citizenAddressHistoryScrollPane = new JScrollPane();
			citizenAddressHistoryScrollPane.setViewportView(getCitizenAddressHistoryPanel());
		}
		return citizenAddressHistoryScrollPane;
	}

	/**
	 * This method initializes citizenAddressHistoryPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCitizenAddressHistoryPanel()
	{
		if(citizenAddressHistoryPanel == null)
		{
			citizenAddressHistoryPanel = new AddressHistoryPanel();
		}
		return citizenAddressHistoryPanel;
	}

	public static void main(String[] args)
	{
		new Demo().setVisible(true);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } 
    catch (Exception exc) 
    {
      exc.printStackTrace();
    }
    
		this.setSize(800, 600);
		this.setContentPane(getJContentPane());
		this.setTitle("ODRA integration demo");
		this.addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				close();
			}
		});
	}
	
	private void close()
	{
		if(JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			try
			{
				db.closeConnection();
			}
			catch (IOException exc)
			{
				showError(exc);
				System.exit(1);
			}
			System.exit(0);
		}
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if(jContentPane == null)
		{
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getTabbedPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane()
	{
		if(tabbedPane == null)
		{
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("citizens", null, getCitizensTabPanel(), null);
		}
		return tabbedPane;
	}

	/**
	 * This method initializes citizensPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCitizensTabPanel()
	{
		if(citizensTabPanel == null)
		{
			citizensTabPanel = new JPanel();
			citizensTabPanel.setLayout(new BorderLayout());
			citizensTabPanel.setName("citizensPanel");
			citizensTabPanel.add(getSearchPanel(), BorderLayout.NORTH);
			citizensTabPanel.add(getPeselsScrollPane(), BorderLayout.WEST);
			citizensTabPanel.add(getDisplayScrollPane(), BorderLayout.CENTER);
		}
		return citizensTabPanel;
	}

	public Database getDb()
	{
		return db;
	}
}
