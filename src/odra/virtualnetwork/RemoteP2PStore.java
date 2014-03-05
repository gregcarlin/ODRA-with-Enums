package odra.virtualnetwork;

import java.io.IOException;
import java.util.Date;

import odra.db.AbstractDataStore;
import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.links.LinkManager;
import odra.db.links.RemoteDefaultStore;
import odra.db.links.RemoteDefaultStoreOID;
import odra.db.links.encoders.RemoteQueryResultDecoder;
import odra.db.objects.data.DBLink;
import odra.dbinstance.DBInstance;
import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.encoders.messages.MessageEncoder;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.security.UserContext;
import odra.sessions.Session;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.pu.Repository;

/**
 * @author mich
 * This class should be moved to somwhere else (network.links)
 */
public class RemoteP2PStore extends RemoteDefaultStore  {
	
	static RemoteP2PStore store = null;
	
	public String peerName = null;
	private Repository repository = null; 

	UserContext usrcntx = null;
	public RemoteP2PStore(String peerName, String schema){
	
		try {
			repository = Repository.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		this.usrcntx = new UserContext("admin", "admin");
		this.peerName = peerName;
		this.schema = schema;
	}
	
	protected  Result sendRemoteDBRqst(OID obj, byte rqst, String[] params) throws DatabaseException {
		DBRequest req = new DBRequest(rqst, params);

		byte [] rawreq = encoder.encodeRequestMessage(new DBRequest[] {req});
		byte [] rawrply = null; 
		
		DBReply rply = null;
		Result result = null;
		try {
			rawrply = repository.putRequest(peerName,Session.getUserContext().getUserName(), rawreq);
			if (rawrply == null) throw new RDNetworkException("Remote request failed"); 
			
			rply = decoder.decodeReplyMessage(rawrply)[0];
			
			if (rply.isErrorReply()) throw new RDNetworkException(rply.getErrorMsg());
			if (rply.getRawResult().length != 0) {
				result = rply.getResult();
			}
			
			RemoteQueryResultDecoder rqResDecoder = new RemoteQueryResultDecoder(usrcntx, null);
			return rqResDecoder.decodeResult(rply.getRawResult());
			
		} catch (Exception e) {
			if (ConfigServer.DEBUG_EXCEPTIONS) e.printStackTrace();
			throw new DatabaseException(e.getMessage());
		}
	}
	
	
	public String getObjectName(OID obj) throws DatabaseException {
		try {
			
			DBRequest qreq = new DBRequest(DBRequest.GET_NAME_RQST, new String[] { String.valueOf(obj.internalOID()) });
			
			byte [] rawreq = encoder.encodeRequestMessage(new DBRequest[] {qreq});
			byte [] rawrply = null;
			
			rawrply = repository.putRequest(peerName,Session.getUserContext().getUserName(), rawreq);
			if (rawrply == null) throw new RDNetworkException("Remote request failed"); 
			
			DBReply rply = decoder.decodeReplyMessage(rawrply)[0];

			byte[] rawres = rply.getRawResult();

			QueryResultDecoder qresdecoder = new QueryResultDecoder(); 
			return ((StringResult) qresdecoder.decodeResult(rawres)).value;
			
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}
	}
	
	//this is for translating DBRequest --> byte[] and byte[] --> DBReply --> Result
	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();

}
