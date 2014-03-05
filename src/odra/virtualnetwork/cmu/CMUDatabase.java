package odra.virtualnetwork.cmu;

import odra.virtualnetwork.GridException;
import odra.virtualnetwork.api.PeerMessage;
import odra.virtualnetwork.base.LocalDatabase;
import odra.virtualnetwork.pu.Repository;

import org.apache.log4j.Logger;

//import odra.virtualnetwork.api.CMUPeer;


/**
 * @author mich
 * @deprecated
 */
public class CMUDatabase extends LocalDatabase{
	
	static Logger log = Logger.getLogger(CMUDatabase.class);
	
	public CMUDatabase(Repository repository) {
		super(repository);
	}
	
	public void setupDatabase(){
		try {
			this.putCommand(PeerMessage.SETUP, null);
		} catch (GridException e) {
			e.printStackTrace();
		}
	}
	
	public void connectionEvent(){
		log.info("Connection with local cmu-db established");
		setupDatabase();
	}


	public void putJoinClient(String peerName, String schema) {
		byte [][] data = {peerName.getBytes(), schema.getBytes()}; 
		try {
			this.putCommand(PeerMessage.CMU_JOINPEER, data);
		} catch (GridException e) {
			e.printStackTrace();
		}
	}
}
