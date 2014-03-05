package odra.network.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

import odra.exceptions.rd.RDCompareTestException;
import odra.exceptions.rd.RDCompilationException;
import odra.exceptions.rd.RDDatabaseException;
import odra.exceptions.rd.RDException;
import odra.exceptions.rd.RDInternalError;
import odra.exceptions.rd.RDNetworkException;
import odra.exceptions.rd.RDOptimizationException;
import odra.exceptions.rd.RDRuntimeException;
import odra.exceptions.rd.RDStaleMetaBaseException;
import odra.exceptions.rd.RDWrapperException;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.encoders.messages.MessageEncoder;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigDebug;

/**
 * This class represents connections between clients and servers.
 * It allows to establish and end socket-based communication,
 * send requests and receive replies. 
 * 
 * @author raist
 */

public class DBConnection {
	public static final String SERVER_HELLO_MESSAGE = "123 ODRA server ready and listening...";
	/*generics not used because of the compatibility with Java 1.4*/
	private static HashMap connectionsSynchronizer = new HashMap();
	
	private Socket socket;

	private String host;
	private int port;
	
	private DataOutputStream output;
	private DataInputStream input;
	
	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();
	  
	private boolean connected = false;
	
	/**
	 * Initializes the connection.
	 * @param host name of the server
	 * @param port port of the server
	 */
	public DBConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Checks if the client is connected to a server
	 * @return true (connected), false (not)
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Opens a connection to the server
	 */
	public void connect() throws IOException {
		connected = false;
		close();

		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port), ConfigClient.CONNECT_TIMEOUT);
		
		SocketIdentifier socketid = new SocketIdentifier(socket);
		if (!connectionsSynchronizer.containsKey(socketid)) 
			connectionsSynchronizer.put(socketid, new Object());
		
		output = new DataOutputStream(socket.getOutputStream());
		input = new DataInputStream(socket.getInputStream());

		if(!verifyConnection())
			throw new IOException("The connected socket (" + host + ":" + port + ") is not a valid ODRA server socket or it didn't respond in required time.");
		
		connected = true;
	}
	
	/**
	 * Verifies if the connected socket is a valid ODRA server socket basing on the server hello string received.
	 * 
	 * @return verified?
	 */
	private boolean verifyConnection()
	{
		byte[] buffer = new byte[SERVER_HELLO_MESSAGE.getBytes().length];
		try
		{
			socket.setSoTimeout(ConfigClient.HELLO_TIMEOUT);
			input.read(buffer);
			socket.setSoTimeout(ConfigClient.NORMAL_TIMEOUT);
			
			return Arrays.equals(buffer, SERVER_HELLO_MESSAGE.getBytes());
		}
		catch (IOException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			return false;
		}
	}

	/**
	 * Closes the connection
	 */
	public void close() throws IOException {
		if (connected) {
			input.close();
			output.close();
			socket.close();
		
			connected = false;
		}
	}
	
	/**
	 * Sends a single request to the server
	 * @param req request
	 * @return result of the request
	 */
	public DBReply sendRequest(DBRequest req) throws IOException, RDException {
		return sendRequests(new DBRequest[] { req })[0];
	}

	/**
	 * Sends a table of requests to the server.
	 * @param reqs table of requests
	 * @return results of execution
	 */
	public DBReply[] sendRequests(DBRequest[] reqs) throws IOException, RDException {
		DBReply[] rply;
		byte[] rplarr;
		
		try {
			byte[] reqarr = encoder.encodeRequestMessage(reqs);
			
			synchronized(DBConnection.connectionsSynchronizer.get(new SocketIdentifier(socket))) 
			{
				// send the request
				output.writeInt(reqarr.length);
				output.write(reqarr);
	
				// retrieve the length of the reply message 
				int msglen = input.readInt();
	
				// retrieve the rest of the message
				rplarr = new byte[msglen];
				
				int toread = rplarr.length;
				int len;
				while (toread > 0) {
					len = input.read(rplarr, rplarr.length - toread, toread);
					
					if (len == -1)
						break;
	
					toread -= len;
				}
			}
			// decode the reply
			rply = decoder.decodeReplyMessage(rplarr);
		}
		catch (IOException ex) {
			connected = false;
			throw ex;
		}
	
		checkErrors(rply);
		
		return rply;
	}

	private final void checkError(DBReply reply) throws RDException {
		Result res;
		StringResult sres;
		IntegerResult ires1, ires2;
		
		switch (reply.getStatus()) {
			case DBReply.STAT_OK:
				break;
			
			case DBReply.STAT_DATABASE_ERROR:
				throw new RDDatabaseException(reply.getErrorMsg());

			case DBReply.STAT_INTERNAL_ERROR:
				throw new RDInternalError(reply.getErrorMsg());
				
			case DBReply.STAT_SECURITY_ERROR:
				throw new RDDatabaseException(reply.getErrorMsg());

			case DBReply.STAT_RUNTIME_ERROR:
				ErrorInfo ei1 = getErrorInfo(reply);
				throw new RDRuntimeException(reply.getErrorMsg(), ei1.module, ei1.line, ei1.column);

			case DBReply.STAT_NETWORK_ERROR:
				throw new RDNetworkException(reply.getErrorMsg());

			case DBReply.STAT_COMPILATION_ERROR:
				ErrorInfo ei2 = getErrorInfo(reply);
				throw new RDCompilationException(reply.getErrorMsg(), ei2.module, ei2.line, ei2.column);
				
			case DBReply.STAT_OPTMIZATION_ERROR:
				throw new RDOptimizationException(reply.getErrorMsg());
				
			case DBReply.STAT_WRAPPER_ERROR:
				throw new RDWrapperException(reply.getErrorMsg());
			
			case DBReply.STAT_STALE_METABASE_ERROR:
				throw new RDStaleMetaBaseException("Stale link metabase");

			case DBReply.STAT_COMPARE_TEST_ERROR:
				throw new RDCompareTestException(reply.getErrorMsg());	
				
			default:
				throw new RDNetworkException("Invalid reply status code");
		}
	}
	
	private final ErrorInfo getErrorInfo(DBReply reply) throws RDNetworkException {
		BagResult res = (BagResult) reply.getResult();

		StringResult sres = (StringResult) res.elementAt(0);
		IntegerResult ires1 = (IntegerResult) res.elementAt(1);
		IntegerResult ires2 = (IntegerResult) res.elementAt(2);		

		return new ErrorInfo(sres.value, ires1.value, ires2.value);
	}

	private final void checkErrors(DBReply[] replies) throws RDException {
		for (int i = 0; i < replies.length; i++)
			checkError(replies[i]);
	}

	private class ErrorInfo {
		public String module;
		public int line, column;
		
		public ErrorInfo(String module, int line, int column) {
			this.module = module;
			this.line = line;
			this.column = column;
		}
	}

}

class SocketIdentifier {
	
	Socket socket;
	
	SocketIdentifier(Socket socket) {
		this.socket = socket;
	}

	public boolean equals(Object arg0) {
		SocketIdentifier socketid = (SocketIdentifier) arg0;
		return socket.getLocalSocketAddress().equals(socketid.socket.getLocalSocketAddress()) && socket.getRemoteSocketAddress().equals(socketid.socket.getRemoteSocketAddress());
	}

	public int hashCode() {
		return socket.getLocalSocketAddress().hashCode() + socket.getRemoteSocketAddress().hashCode();
	}
	
	
	
}
