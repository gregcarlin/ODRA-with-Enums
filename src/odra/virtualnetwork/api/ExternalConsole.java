package odra.virtualnetwork.api;

import java.util.logging.*;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;


public class ExternalConsole extends JFrame implements ConsoleListener  {

	private static final long serialVersionUID = 1L;

	private JScrollPane Pane = null;
	
	private JTextArea AreaOutput = null;  //  @jve:decl-index=0:visual-constraint="17,239"
	
	private String newline = "\n"; 

	/**
	 * This is the default constructor
	 */
	public ExternalConsole() {
		super();
		initialize();
		}
	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(700, 200);
		this.setTitle("P2P Monitor Console");
		AreaOutput = new JTextArea();
		Pane = new JScrollPane(AreaOutput);
		getContentPane().add(Pane);
		setVisible(true);
//		Registers this frame as Console Listener
		Console.registerOutputListener(this);
		}

	 
	 /**
	   * This method appends the data to the text area.
	   * 
	   * @param data
	   *            the Logging information data
	   */
	  public void showInfo(String data) {
	    AreaOutput.append(data);
	    this.getContentPane().validate();
	    }
	  
//	 Implements the ConsoleListener interface method to log message to AreaOutput
	  public void logMessage(String message) {
	    this.AreaOutput.append(message);
	  }

}

