package odra.dbinstance;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.dbinstance.processes.ListenerProcess;
import odra.dbinstance.processes.ServerProcess;
import odra.system.config.ConfigServer;

/**
 * The class describes objects representing database instances.
 * Database instances are set of processess and memory structures.
 * Each database has one database instance. There is no other
 * way for end users to operate on the deatabase but through
 * a database instance brokarage.
 * 
 * @author raist
 */

public class DBInstance {	
	private ListenerProcess lsnprc;
	private Vector<ServerProcess> usrprc = new Vector();
	
	private String hname;

	public DBInstance() throws UnknownHostException {
		hname = InetAddress.getLocalHost().getCanonicalHostName();
	}

	/**
	 * @return name of the host on which the database is operating
	 */
	public String getHostName() {
		return hname;
	}
	
	/**
	 * Starts the processes belonging to the database instance
	 */
	public void startup() {
		lsnprc = new ListenerProcess(this, ConfigServer.LSNR_PORT);
		lsnprc.start();
	}
	
	/**
	 * Stops the processes belonging to the database instance
	 */
	public void shutdown() {
		lsnprc.shutdown();

		ServerProcess[] prcs = (ServerProcess[]) usrprc.toArray(new ServerProcess[usrprc.size()]);
		for (ServerProcess up : prcs)
			shutdownServerProcess(up);
	}

	/**
	 * Creates a new server process. Usually invoked by the listener process
	 * as a result of an incoming client connection.
	 * @param channel socket connection to the client
	 * @return new server process
	 */
	public ServerProcess createServerProcess(SocketChannel channel) throws DatabaseException {
		ServerProcess sp = new ServerProcess(this, channel);
		usrprc.addElement(sp);

		return sp;
	}

	/**
	 * Stops a server process and removes it from a list of server processes
	 * managed by this database instance.
	 * @param proc server process to be stopped
	 */
	public void shutdownServerProcess(ServerProcess proc) {
		proc.shutdown();

		try {
			proc.getChannel().close();
		}
		catch (IOException ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.WARNING, "Closing SVRP client connection");
		}

		if (!usrprc.remove(proc))
			ConfigServer.getLogWriter().getLogger().log(Level.WARNING, "Attempt to remove an inexisting SVRP from the db instance");
	}

	/**
	 * Returns a list of server processes managed by this database instance
	 * @return server process enumeration object
	 */
	public Enumeration<ServerProcess> getServerProcessEnum() {
		return usrprc.elements(); 
	}

	/**
	 * Invoked when something goes wrong with the listener process
	 */
	public void lsnrFailed() {
		System.out.println("The listener is dead");
		System.exit(1);
	}
}
