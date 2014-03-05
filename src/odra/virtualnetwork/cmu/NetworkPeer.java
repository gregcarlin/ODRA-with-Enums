package odra.virtualnetwork.cmu;

import org.apache.log4j.Logger;

import odra.virtualnetwork.GridException;
import odra.virtualnetwork.base.DatabaseDiscovery;
import net.jxta.peer.PeerID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

public class NetworkPeer {
	
	public static Logger log = Logger.getLogger(NetworkPeer.class);
	
	private String peerName = null;
	private PeerID peerID = null;
	private PeerAdvertisement peerAdv = null;
	private PipeAdvertisement databasePipe = null;
	private PipeAdvertisement multicastPipe = null;
	private DatabaseDiscovery dd = null;
	
	public NetworkPeer(String peerName, PeerID peerID){
		this.peerName = peerName;
		this.peerID = peerID;
		dd = DatabaseDiscovery.getDatabaseDiscovery();
	}
	
	 
	public NetworkPeer(PeerID peerID) {
		this.peerID = peerID;
	}
	
	public void initialize(){
		log.debug("Ask for peerAdvertisement");
		DatabaseDiscovery dd = DatabaseDiscovery.getDatabaseDiscovery();
		try {
			peerAdv = dd.invokeAndWaitForOne(peerID.toString(), true);
			//multicastPipe = dd.invokeAndWaitForOne(null, 1, "" withLocalCache)
		} catch (GridException e) {
			e.printStackTrace();
		}
		peerName = peerAdv.getName();		
	}


	/**
	 *Obtains adverisements from jxta platform needed to esthablishing various communication 
	 */
	public void obtainAdvs(){
		try {
			databasePipe = dd.getPeerAdvs(peerID.toString());
		} catch (GridException e) {
			e.printStackTrace();
		}
		log.debug(databasePipe.toString());
	}
	
	public void kickOut(){
	}


	public String getPeerName() {
		return peerName;
	}


	public String getPeerID() {
		return peerID.toURI().toString();
	}
	
}
