package tests.jobc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import odra.jobc.SBQLQuery;

public class JOBCTestGUI extends JFrame {
    private final String ALL = "all"; //  @jve:decl-index=0:

    private JOBC db = new JOBC("admin", "admin", "localhost", 1521 ); //  @jve:decl-index=0:

    private Vector<String> distinctColors;

    private Result cars;

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JPanel northPanel = null;

    private JComboBox colorComboBox = null;

    private JLabel jLabel = null;

    private JSplitPane centerSplitPane = null;

    private JScrollPane carsScrollPane = null;

    private DefaultListModel carListModel = new DefaultListModel();

    private JList carList = null;

    private JPanel ownerPanel = null;

    private JPanel firstNamePanel = null;

    private JLabel jLabel1 = null;

    private JTextField firstNameTextField = null;

    private JPanel lastNamePanel = null;

    private JLabel jLabel11 = null;

    private JTextField lastNameTextField = null;

    private JPanel agePanel = null;

    private JLabel jLabel12 = null;

    private JTextField ageTextField = null;

    private JPanel addressPanel = null;

    private JLabel jLabel111 = null;

    private JTextField addressTextField = null;

    private JPanel marriedPanel = null;

    private JLabel jLabel121 = null;

    private JCheckBox marriedCheckBox = null;

    /**
     * This is the default constructor
     */
    public JOBCTestGUI() {
	super();

	try {
	    db.connect();
	    db.setCurrentModule("admin.test");
	    distinctColors = loadColors();
	    searchCars(ALL);
	} catch (JOBCException exc) {
	    exc.printStackTrace();
	    JOptionPane.showMessageDialog(this, exc.getMessage(), "Error",
		    JOptionPane.ERROR_MESSAGE);
	}

	initialize();
    }

    /**
     * This method initializes centerSplitPane	
     * 	
     * @return javax.swing.JSplitPane	
     */
    private JSplitPane getCenterSplitPane() {
	if (centerSplitPane == null) {
	    centerSplitPane = new JSplitPane();
	    centerSplitPane.setDividerLocation(200);
	    centerSplitPane.setRightComponent(getOwnerPanel());
	    centerSplitPane.setLeftComponent(getCarsScrollPane());
	}
	return centerSplitPane;
    }

    /**
     * This method initializes carsScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getCarsScrollPane() {
	if (carsScrollPane == null) {
	    carsScrollPane = new JScrollPane();
	    carsScrollPane.setViewportView(getCarList());
	}
	return carsScrollPane;
    }

    /**
     * This method initializes carList	
     * 	
     * @return javax.swing.JList	
     */
    private JList getCarList() {
	if (carList == null) {
	    carList = new JList(carListModel);
	    carList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    carList
		    .addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(
				javax.swing.event.ListSelectionEvent e) {
			    if (!e.getValueIsAdjusting()) {
				showOwner(e.getLastIndex());
			    }
			}
		    });
	}
	return carList;
    }

    private void showOwner(int index) {
	clearOwner();

	try {
	    Result car = cars.get(index);
	    Result owner = car.getByName("owner");
	    Result address = owner.getByName("address");

	    try {
		getFirstNameTextField().setText(
			owner.getByName("fName").getString());
		getLastNameTextField().setText(
			owner.getByName("lName").getString());
		getAgeTextField().setText(
			Integer.toString(owner.getByName("age")
				.getInteger()));
		getMarriedCheckBox().setSelected(
			owner.getByName("married").getBoolean());

		getAddressTextField().setText(
			address.getByName("city").getString()
				+ ", "
				+ address.getByName("street")
					.getString());
	    } catch (JOBCException exc) {
		exc.printStackTrace();
		JOptionPane.showMessageDialog(this, exc.getMessage(), "Error",
			JOptionPane.ERROR_MESSAGE);
	    }
	} catch (ArrayIndexOutOfBoundsException exc) {
	    clearOwner();
	}

    }

    private void clearOwner() {

	getFirstNameTextField().setText("");
	getLastNameTextField().setText("");
	getAgeTextField().setText("");
	getAddressTextField().setText("");
	getMarriedCheckBox().setSelected(false);

    }

    /**
     * This method initializes ownerPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getOwnerPanel() {

	if (ownerPanel == null) {
	    FlowLayout flowLayout1 = new FlowLayout();
	    flowLayout1.setAlignment(java.awt.FlowLayout.LEFT);
	    ownerPanel = new JPanel();
	    ownerPanel.setLayout(flowLayout1);
	    ownerPanel.add(getFirstNamePanel(), null);
	    ownerPanel.add(getLastNamePanel(), null);
	    ownerPanel.add(getAgePanel(), null);
	    ownerPanel.add(getMarriedPanel(), null);
	    ownerPanel.add(getAddressPanel(), null);
	}

	return ownerPanel;

    }

    /**
     * This method initializes firstNamePanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getFirstNamePanel() {

	if (firstNamePanel == null) {
	    jLabel1 = new JLabel();
	    jLabel1.setText("first name");
	    firstNamePanel = new JPanel();
	    firstNamePanel.setLayout(new BorderLayout());
	    firstNamePanel.add(jLabel1, BorderLayout.NORTH);
	    firstNamePanel.add(getFirstNameTextField(), BorderLayout.SOUTH);
	}

	return firstNamePanel;

    }

    /**
     * This method initializes firstNameTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getFirstNameTextField() {

	if (firstNameTextField == null) {
	    firstNameTextField = new JTextField();
	    firstNameTextField.setColumns(10);
	    firstNameTextField.setEditable(false);
	}

	return firstNameTextField;

    }

    /**
     * This method initializes lastNamePanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getLastNamePanel() {

	if (lastNamePanel == null) {
	    jLabel11 = new JLabel();
	    jLabel11.setText("last name");
	    lastNamePanel = new JPanel();
	    lastNamePanel.setLayout(new BorderLayout());
	    lastNamePanel.add(jLabel11, BorderLayout.NORTH);
	    lastNamePanel.add(getLastNameTextField(), BorderLayout.SOUTH);
	}

	return lastNamePanel;

    }

    /**
     * This method initializes lastNameTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getLastNameTextField() {

	if (lastNameTextField == null) {
	    lastNameTextField = new JTextField();
	    lastNameTextField.setColumns(20);
	    lastNameTextField.setEditable(false);
	}

	return lastNameTextField;

    }

    /**
     * This method initializes agePanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getAgePanel() {

	if (agePanel == null) {
	    jLabel12 = new JLabel();
	    jLabel12.setText("age");
	    agePanel = new JPanel();
	    agePanel.setLayout(new BorderLayout());
	    agePanel.add(jLabel12, BorderLayout.NORTH);
	    agePanel.add(getAgeTextField(), BorderLayout.SOUTH);
	}

	return agePanel;

    }

    /**
     * This method initializes ageTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getAgeTextField() {

	if (ageTextField == null) {
	    ageTextField = new JTextField();
	    ageTextField.setColumns(3);
	    ageTextField.setEditable(false);
	}

	return ageTextField;

    }

    /**
     * This method initializes addressPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getAddressPanel() {

	if (addressPanel == null) {
	    jLabel111 = new JLabel();
	    jLabel111.setText("address");
	    addressPanel = new JPanel();
	    addressPanel.setLayout(new BorderLayout());
	    addressPanel.add(jLabel111, BorderLayout.NORTH);
	    addressPanel.add(getAddressTextField(), BorderLayout.SOUTH);
	}

	return addressPanel;

    }

    /**
     * This method initializes addressTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getAddressTextField() {

	if (addressTextField == null) {
	    addressTextField = new JTextField();
	    addressTextField.setColumns(50);
	    addressTextField.setEditable(false);
	}

	return addressTextField;

    }

    /**
     * This method initializes marriedPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMarriedPanel() {

	if (marriedPanel == null) {
	    jLabel121 = new JLabel();
	    jLabel121.setText("married");
	    marriedPanel = new JPanel();
	    marriedPanel.setLayout(new BorderLayout());
	    marriedPanel.add(jLabel121, java.awt.BorderLayout.NORTH);
	    marriedPanel.add(getMarriedCheckBox(), BorderLayout.CENTER);
	}

	return marriedPanel;

    }

    /**
     * This method initializes marriedCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getMarriedCheckBox() {

	if (marriedCheckBox == null) {
	    marriedCheckBox = new JCheckBox();
	    marriedCheckBox.setEnabled(false);
	}

	return marriedCheckBox;

    }

    public static void main(String[] args) {

	new JOBCTestGUI().setVisible(true);

    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {

	this.setSize(800, 600);
	this.setContentPane(getJContentPane());
	this.setTitle("JOBC test GUI");
	this.addWindowListener(new java.awt.event.WindowAdapter() {
	    public void windowClosing(java.awt.event.WindowEvent e) {
		System.exit(0);
	    }
	});

    }

    private Vector<String> loadColors() throws JOBCException {

	Vector<String> distinctColors = new Vector<String>();
	distinctColors.addElement(ALL);
	Result result = db.execute("distinct (Car.color);");
	for (Result color : result.toArray())
	    distinctColors.addElement(color.getString());

	return distinctColors;

    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {

	if (jContentPane == null) {
	    jContentPane = new JPanel();
	    jContentPane.setLayout(new BorderLayout());
	    jContentPane.add(getNorthPanel(), BorderLayout.NORTH);
	    jContentPane.add(getCenterSplitPane(), BorderLayout.CENTER);
	}

	return jContentPane;

    }

    /**
     * This method initializes northPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getNorthPanel() {

	if (northPanel == null) {
	    jLabel = new JLabel();
	    jLabel.setText("car color:");
	    FlowLayout flowLayout = new FlowLayout();
	    flowLayout.setAlignment(FlowLayout.LEFT);
	    northPanel = new JPanel();
	    northPanel.setLayout(flowLayout);
	    northPanel.add(jLabel, null);
	    northPanel.add(getColorComboBox(), null);
	}

	return northPanel;

    }

    /**
     * This method initializes colorComboBox	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getColorComboBox() {

	if (colorComboBox == null) {
	    colorComboBox = new JComboBox(distinctColors);
	    colorComboBox.addItemListener(new java.awt.event.ItemListener() {
		public void itemStateChanged(java.awt.event.ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.SELECTED)
			searchCars(colorComboBox.getSelectedItem().toString());
		}
	    });
	}

	return colorComboBox;

    }

    private void searchCars(String color) {

	carListModel.clear();
	clearOwner();

	try 	
	{SBQLQuery query;
	    if (color.equals(ALL)){
		query = db.getSBQLQuery("(Car as car join car.owner.Person as owner).(deref(car) as car, deref(owner) as owner);"); 
	}else {
	    query = db.getSBQLQuery("((Car where color = {color}) as car join car.owner.Person as owner).deref(car as car, deref owner as owner);");
	    query.addStringParam("color", color);
	    
	}
	    cars = db.execute(query);
	    for (Result car : cars.getByName("car").toArray()) {
		String descr = "<html>";
		descr += "model: "
			+ car.getByName("model").firstElement().getString()
			+ "<br />";
		descr += "color: "
			+ car.getByName("color").firstElement().getString()
			+ "<br />";
		descr += "power: "
			+ car.getByName("power").firstElement().getInteger()
			+ "<br />";
		descr += "</html>";
		carListModel.addElement(descr);
	    }

	    getCarList().ensureIndexIsVisible(0);
	} catch (JOBCException exc) {
	    exc.printStackTrace();
	    JOptionPane.showMessageDialog(this, exc.getMessage(), "Error",
		    JOptionPane.ERROR_MESSAGE);
	}

    }

}
