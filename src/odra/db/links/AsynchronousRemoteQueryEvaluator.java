package odra.db.links;

import odra.db.links.encoders.RemoteQueryResultDecoder;
import odra.db.objects.data.DBLink;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.Result;
import odra.sbql.stack.SBQLStack;
import odra.security.UserContext;

/**
 * This class is responsible for maintaining asychronous remote query call 
 * for current session
 * 
 * @author tkowals
 * @version 1.0
 */
public class AsynchronousRemoteQueryEvaluator extends Thread {

	DBRequest qreq; 
	DBConnection conn; 
	AsynchronousRemoteQueriesManager asynchronousRemoteQueriesManager;
	UserContext usrctx;
	DBLink link; 
	SBQLStack stack; 
	int qresindex;
	int rquery_id;
	
	Exception caughtException; 
	
	public AsynchronousRemoteQueryEvaluator(DBRequest qreq, DBConnection conn, UserContext usrctx, DBLink link, SBQLStack stack, int qresindex, int rquery_id, AsynchronousRemoteQueriesManager asynchronousRemoteQueriesManager) {
		super();
		this.qreq = qreq;
		this.conn = conn;
		this.usrctx = usrctx;
		this.link = link;
		this.stack = stack;
		this.qresindex = qresindex;
		this.rquery_id = rquery_id;
		this.asynchronousRemoteQueriesManager = asynchronousRemoteQueriesManager;
	}

	public void run() {
		
		try {
			
			DBReply qrep = conn.sendRequest(qreq);
			
			byte[] rawres = qrep.getRawResult();
			
			RemoteQueryResultDecoder decoder = new RemoteQueryResultDecoder(usrctx, link);
			Result res = decoder.decodeResult(rawres);
			
			stack.replaceEmptyResultFrame(qresindex, res);
			
			asynchronousRemoteQueriesManager.unregisterAsynchronousRemoteQuery(rquery_id);
			
		} catch (Exception e) {
			caughtException = e;
			asynchronousRemoteQueriesManager.unregisterAsynchronousRemoteQueryWithException(rquery_id, e);
		}
	}
	
}
