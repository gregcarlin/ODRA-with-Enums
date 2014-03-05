package odra.db.links;

import java.util.HashMap;

import odra.sessions.Session;

/**
 * This class is responsible for managing asynchronous remote queries started 
 * for current session
 * 
 * @author tkowals
 * @version 1.0
 */
public class AsynchronousRemoteQueriesManager {

	private HashMap<Integer, Integer> semaphore = new HashMap<Integer, Integer>(); 	

	private HashMap<Integer, Object> monitors = new HashMap<Integer, Object>(); 

	private HashMap<Integer, Exception> caughtExceptions = new HashMap<Integer, Exception>(); 
	
	public static AsynchronousRemoteQueriesManager getCurrent() {
		return Session.getAsynchronousRemoteQueriesManager();
	}
	
	void registerAsynchronousRemoteQuery(int id) {
		assert id >= 0: "Remote query id cannot be negative!";
		
		synchronized(this) {
			
			Integer counter = semaphore.get(id);
			if (counter == null)
				semaphore.put(id, 1);
			else
				semaphore.put(id, counter + 1);
			
		}
	}

	void unregisterAsynchronousRemoteQuery(int id) {
		assert id >= 0: "Remote query id cannot be negative!";
		assert (semaphore.get(id) != null): "Remote query id not registred";
		assert (semaphore.get(id) > 0): "Cannot unregister (counter at zero)";

		Integer counter;
		
		synchronized(this) {
			counter = semaphore.get(id);
			semaphore.put(id, counter - 1);	
				
			if (counter == 1)
				if (monitors.containsKey(id)) {
					Object monitor = monitors.remove(id);
					synchronized(monitor) {
						monitor.notifyAll();
					}
				}
		}
	}
	
	public void unregisterAsynchronousRemoteQueryWithException(int id, Exception e) {
		assert id >= 0: "Remote query id cannot be negative!";
		assert (semaphore.get(id) != null): "Remote query id not registred";
		assert (semaphore.get(id) > 0): "Cannot unregister (counter at zero)";
		
		if (!caughtExceptions.containsKey(id))
			caughtExceptions.put(id, e);
		
		unregisterAsynchronousRemoteQuery(id);
		
	}
	
	public void waitForAsynchronousRemoteQueries(int id) throws Exception {
		assert id >= 0: "Remote query id cannot be negative!";
		assert (semaphore.get(id) != null): "Remote query id not registred";
		
		Object monitor;
		
		synchronized(this) {
			Integer counter = semaphore.get(id);
			if (counter == 0) {
				if (caughtExceptions.containsKey(id))
					throw caughtExceptions.get(id);				
				return;
			} 

			monitor = new Object();			
			monitors.put(id, monitor);
		}
		
		synchronized(monitor) {
			if (monitors.containsKey(id))
				monitor.wait();
		}
		if (caughtExceptions.containsKey(id))
			throw caughtExceptions.get(id);	
	}
	
}
