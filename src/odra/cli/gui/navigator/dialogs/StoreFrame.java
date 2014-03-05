package odra.cli.gui.navigator.dialogs;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import odra.cli.gui.components.storage.HeapPanel;
import odra.cli.gui.components.storage.Mode;
import odra.exceptions.rd.RDException;
import odra.network.transport.DBConnection;
import odra.system.config.ConfigClient;

/**
 * Store structure presentation frame.
 *
 * @author jacenty
 * @version 2007-02-20
 * @since 2007-01-03
 */
public class StoreFrame extends JFrame 
{
	/** database connection */
	private final DBConnection connection;
	/** persistent store? (else: transient) */
	private final boolean persistent;
	/** transferred data fragment size in bytes */
	private final int fragmentSize;
	
	private JPanel jContentPane = null;

	private HeapPanel heapPanel = null;

	/**
	 * This is the default constructor
	 * 
	 * @param connection database connection
	 * @param persistent persistent store? (else: transient)
	 * @param fragmentSize transferred data fragment size in bytes
	 */
	public StoreFrame(DBConnection connection, boolean persistent, int fragmentSize)
	{
		super();
		this.connection = connection;
		this.persistent = persistent;
		this.fragmentSize = fragmentSize;
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setSize(800, 600);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setContentPane(getJContentPane());
		
		String storeType = "transient";
		if(persistent)
			storeType = "persistent";
		this.setTitle("Heap structure: " + storeType + " store");
		this.addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				thisWindowClosing(e);
			}
		});
	}
	
	void thisWindowClosing(@SuppressWarnings("unused") WindowEvent e)
	{
		heapPanel.stop();
		setVisible(false);
		dispose();
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
			jContentPane.add(getHeapPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes heapPanel	
	 * 	
	 * @return odra.cli.gui.components.storage.HeapPanel	
	 */
	private HeapPanel getHeapPanel()
	{
		if(heapPanel == null)
		{
			try
			{
				if(!connection.isConnected())
					throw new IOException("connection is currently closed");
				
				heapPanel = new HeapPanel(connection, Mode.HEXADECIMAL, persistent, fragmentSize);
				heapPanel.startDownload();
			}
			catch(IOException exc)
			{
				exc.printStackTrace();
				ConfigClient.getLogWriter().getLogger().log(Level.SEVERE, exc.getMessage());
			}
			catch (RDException exc)
			{
				exc.printStackTrace();
				ConfigClient.getLogWriter().getLogger().log(Level.SEVERE, exc.getMessage());
			}
		}
		return heapPanel;
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		heapPanel.cleanup();
	}
}
