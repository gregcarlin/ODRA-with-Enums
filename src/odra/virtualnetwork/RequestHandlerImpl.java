package odra.virtualnetwork;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;

import odra.db.DatabaseException;
import odra.db.links.encoders.RemoteQueryResultDecoder;
import odra.dbinstance.DBInstance;
import odra.dbinstance.processes.ServerProcess;
import odra.exceptions.rd.RDException;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.encoders.messages.MessageEncoder;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.encoders.results.QueryResultEncoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.security.AuthenticationException;
import odra.sessions.Session;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.api.IRequestHandler;
import odra.virtualnetwork.facade.Config;


/**
 * @author mich
 */

public class RequestHandlerImpl implements IRequestHandler {
	
	private MessageDecoder decoder = new MessageDecoder();
	private MessageEncoder encoder = new MessageEncoder();
	
	private static IRequestHandler impl;
	public static RequestHandlerImpl getImpl() {
		if (impl == null)  impl = new RequestHandlerImpl(); 
		return (RequestHandlerImpl) impl;
	} 
	
	public byte[] handleRequest(String userName, byte req[]) {
		// pass request to local database via dbconnection
		try {
			//pass req an get it back to Worker Section
			DBRequest[] rreq = decoder.decodeRequestMessage(req);
			
			DBConnection connection = this.getConnectionFromPool(userName);
			
			DBReply rply = connection.sendRequest(rreq[0]);
			
			this.putConnectionToPool(userName, connection);
			
			return encoder.encodeReplyMessage(new DBReply[] {rply});
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RDException e) {
			e.printStackTrace();
		} catch (GridException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Hashtable<String, DBConnection> connections  = new Hashtable<String, DBConnection>();
	public synchronized DBConnection getConnectionFromPool(String userName) throws GridException{

		if  (!connections.containsKey(userName)){
			//we must decide if we allow connection by hand (add gridlink ...)
			//or allow only to joining to grid
			//throw new P2PException("Local connection to db from jxta not initialized");
			
			//later to fix: get user and pass from decoder 
			prepareConnection("admin", "admin");
		}		
		return connections.remove(userName);
	}
	
	public synchronized void putConnectionToPool(String userName, DBConnection connection) {
		if (connections.containsKey(userName)){
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else connections.put(userName, connection);
	}
	
	public boolean prepareConnection(String user, String password){
		DBConnection new_conn = new DBConnection("127.0.0.1",ConfigServer.LSNR_PORT);
		try {
			DBRequest lreq = new DBRequest(DBRequest.LOGIN_RQST, new String[] { user, password });
			new_conn.connect();
			DBReply lrep = new_conn.sendRequest(lreq);
			if (lrep.isErrorReply()){
				throw new DatabaseException("Local connection not eshtablished");
			}
			
			lreq = new DBRequest(DBRequest.SET_CLIENT_NAME, new String [] { "grid_client" });
			new_conn.sendRequest(lreq);
			
			connections.put(user, new_conn);
			
		} catch (IOException e ){
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (RDException e) {
			e.printStackTrace();
		}
		return true;
	}
	
}
