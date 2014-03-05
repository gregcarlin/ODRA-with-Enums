package odra.dbinstance.processes;

import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.dbinstance.DBInstance;
import odra.system.*;
import odra.system.config.ConfigServer;

/**
 * This class is responsible for monitoring server processes.
 * It activates from time to time and checks clients' availability.
 * If a client is unavailable, its server process is closed.
 * 
 * TODO: this is only a stub
 * 
 * @author raist
 */

public class ProcMonitorProcess extends Thread {
	public DBInstance instance;
	private SocketChannel channel;
	
	private boolean loop; // indicates whether the thread should keep running or not
	private Object monitor = new Object(); // used when putting the thread to sleep/waking it up
	private int errCnt = 0; // counts errors
	
	public ProcMonitorProcess(DBInstance instance, SocketChannel channel) throws DatabaseException {
		super("pmon");

		this.instance = instance;
		this.channel = channel;
	}
	
	public void run() {		
		loop = true;

		while (loop) {
			try {	
				Thread.sleep(ConfigServer.PMON_SLEEP_TIME);

				// when the thread is woken up, it analyzes server processes
				// TODO
//				instance.ge

				errCnt = 0;
			}
			catch (Exception ex) {
				ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during PMON execution", ex);

				if (errCnt++ > ConfigServer.PMON_STOP_ON_ERRORS) {
					// instance.pmonFailed();
					shutdown();
				}
			}
		}
	}

	// called when the server process should be stopped
	public void shutdown() {
		loop = false;

		synchronized (monitor) {
			monitor.notify();
		}
	}
}
