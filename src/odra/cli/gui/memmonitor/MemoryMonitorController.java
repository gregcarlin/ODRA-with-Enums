package odra.cli.gui.memmonitor;

import java.io.IOException;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;

/**
 * Memory monitor controller.
 *
 * @author jacenty
 * @version 2007-11-22
 * @since 2007-02-20
 */
public class MemoryMonitorController
{
	private MemoryMonitor clientMonitor;
	private MemoryMonitor serverMonitor;
	
	private DBConnection connection;
	
	public void openMonitor(String[] params)
	{
		if(params.length == 0)
		{
			if(clientMonitor == null)
				clientMonitor = new MemoryMonitor(null);
			
			clientMonitor.setVisible(true);
		}
		else if(params.length == 4)
		{
			try
			{
				connection = new DBConnection(params[2], Integer.parseInt(params[3]));
				connection.connect();
				if(connection.isConnected())
				{
					DBRequest request = new DBRequest(DBRequest.LOGIN_RQST, new String[] {params[0], params[1]});
					DBReply reply = connection.sendRequest(request);
					if(!reply.isErrorReply())
						serverMonitor = new MemoryMonitor(connection);
					else
						System.out.println("Cannot open a server memory monitor: " + reply.getErrorMsg());
				}
				else
					System.out.println("Cannot open a server memory monitor: connection not established");
			}
			catch(Exception exc)
			{
				System.out.println("Cannot open a server memory monitor: " + exc.getMessage());
			}
			
			if(serverMonitor != null)
				serverMonitor.setVisible(true);
		}
	}
	/**
	 * Opens a new monitor based on a given DB connection.
	 * @param connection
	 * @param closeConnection if true then the connection will be closed together with the monitor.
	 */
	public void openMonitor(DBConnection connection, boolean closeConnection)
	{
		serverMonitor = new MemoryMonitor(connection);
		serverMonitor.setCloseConnection(closeConnection);
		if(serverMonitor != null) {
			serverMonitor.setVisible(true);
		}
	}
	
	/**
	 * Closes the connection (if running in the server mode).
	 * @throws IOException 
	 */
	public void close() throws IOException
	{
		if(connection != null && connection.isConnected())
		{
			serverMonitor.setCloseConnection(true);
			serverMonitor.close();
		}
		
		if(clientMonitor != null)
			clientMonitor.close();
	}
}
