package odra.jobc;

import java.io.IOException;
import java.util.Iterator;

import odra.exceptions.rd.RDException;
import odra.jobc.batch.BatchCommand;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.system.config.ConfigClient;

/**
 * JOBC main class.
 * 
 * @author jacenty, radamus
 * @version 2008-04-06
 * @since 2007-03-11
 */
public class JOBC {
	private final DBConnection connection;

	private final String user;
	private final String password;

	private String currentModule;

	/**
	 * The constructor.
	 * 
	 * @param user
	 *            user name
	 * @param password
	 *            password
	 * @param host
	 *            host
	 * @param port
	 *            port
	 */
	public JOBC(String user, String password, String host, int port) {
		connection = new DBConnection(host, port);
		this.user = user;
		this.password = password;

		this.currentModule = user;

	}

	/**
	 * The constructor.
	 * 
	 * @param user
	 *            user name
	 * @param password
	 *            password
	 * @param host
	 *            host
	 */
	public JOBC(String user, String password, String host) {
		this(user, password, host, DEFAULT_PORT);
	}

	public SBQLQuery getSBQLQuery(String query) {
		return new SBQLQuery(query);
	}

	/**
	 * Connects to a database.
	 * 
	 * @throws JOBCException
	 */
	public void connect() throws JOBCException {
		if (isConnected())
			throw new JOBCException("The connection is already established.",
					JOBCException.CONNECTION_ERROR);

		try {
			connection.connect();			
		}catch (IOException e) {
			throw communicationException("Cannot connect to the database",e);
		} 
		
		login();
	}

	/**
	 * @throws JOBCException 
	 * 
	 */
	private void login() throws JOBCException {
		try {
			connection.sendRequest(new DBRequest(DBRequest.LOGIN_RQST,
					new String[] { user, password }));
		}catch (IOException e) {
			throw communicationException(e);
		} catch (RDException e) {
			throw executionException("Login failed", e);
		}
		
	}

	

	/**
	 * Closes this connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		connection.close();
		currentModule = user;
	}

	/**
	 * Returns if this connection is established.
	 * 
	 * @return true if connected
	 */
	public boolean isConnected() {
		return connection.isConnected();
	}

	/**
	 * Returns the current module name.
	 * 
	 * @return current module name
	 * @throws JOBCException
	 */
	public String getCurrentModule() throws JOBCException {
		checkConnection();

		return currentModule;
	}

	/**
	 * Sets the current module.
	 * 
	 * @param moduleGlobalName
	 *            module global name
	 * @throws JOBCException
	 */
	public void setCurrentModule(String moduleGlobalName) throws JOBCException {
		checkConnection();

		try {
			connection.sendRequest(new DBRequest(DBRequest.EXISTS_MODULE_RQST,
					new String[] { moduleGlobalName }));
			this.currentModule = moduleGlobalName;
		} catch (Exception exc) {
			throw new JOBCException("Database communication.", exc,
					JOBCException.COMMUNICATION_ERROR);
		}
	}

	/**
	 * Sets the current optimization sequence.
	 * 
	 * 
	 * @throws JOBCException
	 */
	public void setOptimization(String[] optimizations) throws JOBCException {
		checkConnection();

		try {
			connection.sendRequest(new DBRequest(
					DBRequest.SET_OPTIMIZATION_RQST, optimizations));
		} catch (IOException e) {
			throw communicationException(e);
		} catch (RDException e) {
			throw executionException(e);
		}
	}

	/**
	 * Executes a query.
	 * 
	 * @param query
	 *            query string.
	 * @return {@link Result}
	 * @throws JOBCException
	 */
	public Result execute(String query) throws JOBCException {
		return execute(new SBQLQuery(query));
	}

	/**
	 * Executes a query.
	 * 
	 * @param query
	 *            SBQLQuery to execute.
	 * @return {@link Result}
	 * @throws JOBCException
	 */
	public Result execute(SBQLQuery query) throws JOBCException {
		checkConnection();
		String pquery = query.prepare();
		
			DBReply reply;
			try {
				reply = connection.sendRequest(new DBRequest(
						DBRequest.EXECUTE_SBQL_RQST, new String[] { pquery,
								getCurrentModule(), "on", "off" }));
				return new Result(reply.getResult());
			} catch (IOException e) {
				throw communicationException(e);
			} catch (RDException e) {
				throw executionException(e);
			}
			
		
	}
	
	public Result execute(DMLCommand command) throws JOBCException{
		checkConnection();
		try {
			DBReply reply = connection.sendRequest(prepare(command));
			return new Result(reply.getResult());
		} catch (IOException e) {
			throw communicationException(e);
		} catch (RDException e) {
			throw executionException(e);
		}
	}
	public void executeDDLCommand(DDLCommand command)throws JOBCException {
		checkConnection();
		try {
			DBReply reply = connection.sendRequest(prepare(command));
		} catch (IOException e) {
			throw communicationException(e);			
		} catch (RDException e) {
			throw executionException(e);
		}
	}
	/**
	 * Executes batch command
	 * @param batch - BatchCommand to execute
	 * @param stopOnError - if true the batch processing stops on first ODRA error <br>
	 * (the IO error always stop processing)  
	 * @return the number of single command that finish with an error (if stopOnError == false)  
	 * @throws JOBCException
	 */
	public int executeBatch(BatchCommand batch, boolean stopOnError)throws JOBCException {
		checkConnection();
		int errCount = 0;
		for(Iterator i = batch.iterator(); i.hasNext();){
			ODRACommand command = (ODRACommand)i.next();
			try {
				DBReply reply = connection.sendRequest(command.getRequest(this.currentModule));
			} catch (IOException e) {
				throw communicationException(e);
			} catch (RDException e) {				
				if(stopOnError)
					throw executionException(e);
				errCount++;
			}
		}
		return errCount;
	}	

	private final void checkConnection() throws JOBCException {
		if (!isConnected())
			throw new JOBCException("The connection is not established yet.",
					JOBCException.CONNECTION_ERROR);
	}

	private DBRequest prepare(ODRACommand command) throws JOBCException{
		return command.getRequest(this.getCurrentModule());
	}
	
	private JOBCException executionException(RDException e){
		return executionException("",e);
	}
	/**
	 * @param string
	 * @param e
	 * @return
	 */
	private JOBCException executionException(String message, RDException e) {
		return new JOBCException(message + " Execution failed.", e,
				JOBCException.EXECUTION_ERROR);
	}
	private JOBCException communicationException(IOException e){
		return communicationException("",e);
	}
	private JOBCException communicationException(String message, IOException e){
		return new JOBCException(message + " Communication error.", e,
				JOBCException.EXECUTION_ERROR);
	}
	private static int DEFAULT_PORT = ConfigClient.CONNECT_PORT;
}