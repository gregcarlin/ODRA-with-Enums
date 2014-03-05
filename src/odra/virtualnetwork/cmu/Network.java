package odra.virtualnetwork.cmu;

import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.URI;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.socket.JxtaMulticastSocket;
import net.jxta.socket.JxtaSocket;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.facade.ICMUHandler;
import odra.virtualnetwork.base.Advertisements;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.base.DatabaseDiscovery;
import odra.virtualnetwork.pu.Repository;

import org.apache.log4j.Logger;

/**
 * @author mich
 * This class represents a group of peers which makes a Virtual Repository on cmu Peer. Its 
 * equivalent on end-peer is @see Repository  
 */
public class Network extends Repository implements RendezvousListener {
	static Logger log = Logger.getLogger(Network.class);
	
	public static Network instance = null;
	public static Network getInstance(){
		return instance;
	}
	
	private PeerGroup peerGroup = null;
	public static PipeID mc_socketId = null; 
	
	public Network(PeerGroup peerGroup) throws Exception{
		super(peerGroup.getPeerGroupAdvertisement());
		if (instance != null)
			throw new Exception("There can be only one instance of this class");
		Network.instance = this;
		this.peerGroup = peerGroup;
	}
	
	private Map<PeerID, NetworkPeer> peers = new Hashtable<PeerID, NetworkPeer>();

	/**
	 * Add peer to Virtual Repository. Peer has joined peer group but we need to present it on this class
	 * @param peerID peer id (URI style) of peer
	 * @param peerName peer name
	 * @return
	 * @see Network.rendezvousEvent
	 */
	public boolean addPeer(PeerID peerID, String peerName){
		
		if (peers.containsKey(peerID)) {
			log.debug("Peer " + peerID +" already in Repository");
			return false;
		}
		
		NetworkPeer peer = null;
		if (peerName == null)
			peer = new NetworkPeer(peerID);
		else peer = new NetworkPeer(peerName, peerID);
				
		peers.put(peerID, peer);
		peer.initialize();
		
		cmu_handler.peerJoined(peer.getPeerName());
		
		return true;
			
	}
	
	/**
	 * Removes peer from Virtual Repository. If Peer was in peer group and he finished connection, we need to remove it from this class
	 * @param peerID peer id (URI style) of peer
	 * @see Network.rendezvousEvent
	 */

	public void removePeer(PeerID peerID) {
		if ((peers.size() != 0)&&(peers.containsKey(peerID))){
			log.info("Peer: " + peers.get(peerID).getPeerName() + " has left repository");
			//handling leaving here
			peers.remove(peerID);
		}
		else log.info("Peer doesn't exist in repository");
	}
	
	public void broadcast(byte[] buff){
		try {
			if (buff.length >= 16384) throw new GridException("Datagram too long"); 
			JxtaMulticastSocket socket = getMulticastSocket();		
			socket.send(new DatagramPacket(buff, buff.length));			
		} catch (GridException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}

	/**
	 * Notifies error during peer's connection with Virtual Repository. If Peer was in peer group and due to network error it has lost connection, we need to remove it from this class
	 * @param peerID peer id (URI style) of peer
	 * @return
	 * @see Network.rendezvousEvent
	 */
	public boolean lostPeer(PeerID peerID){
		if ((peers.size() != 0)&&(peers.containsKey(peerID))){
			log.info("Client " + peers.get(peerID).getPeerName() + " lost, flushing...");
			peers.remove(peerID);
			return true;
		}
		else {
			log.info("Peer doesn't exist in repository");
			return false;
		}
		
	}

	
	public NetworkPeer[] getPeers(){
		//really really strange
		Iterator<NetworkPeer> i = peers.values().iterator();
		Vector<NetworkPeer> v =new Vector<NetworkPeer>();
		while (i.hasNext())
			v.add(i.next());
		
		NetworkPeer[] nps = new NetworkPeer[v.size()];
		int k = 0;
		for (NetworkPeer p : v)
			nps[k] = v.get(k++);
	
		return nps;
	}
	
	/**
	 * This will create peerGroup and credentials lists
	 */
	public static Repository createRepository(PeerGroup parentGroup, Advertisements advManager){
		PeerGroup dbgroup = null;
		PeerGroupAdvertisement adv = null;
		Repository repo = null;
		try {
			adv = (PeerGroupAdvertisement) advManager.getAdvertisement(Advertisements.REPOSITORYGROUP_ADV);
			dbgroup = parentGroup.newGroup(adv);
			adv = dbgroup.getPeerGroupAdvertisement();
			parentGroup.getDiscoveryService().publish(adv);
			parentGroup.getDiscoveryService().remotePublish(adv);
			log.debug("Group adv published in " + parentGroup.getPeerGroupName());
			
			
			try {
				new Network(dbgroup).joinGroup();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			repo = Network.getInstance();
			
		} catch (PeerGroupException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Repository low-level prepared");
		
		return repo;
	}

	private void joinGroup() {
        
        StructuredDocument creds = null;
        try {
            AuthenticationCredential authCred = new AuthenticationCredential(peerGroup, null, creds );
            MembershipService membership = peerGroup.getMembershipService();
            Authenticator auth =  membership.apply( authCred );
            
            // Check if everything is okay to join the group
            if (auth.isReadyForJoin()){
                Credential myCred = membership.join(auth);
                StructuredTextDocument doc = (StructuredTextDocument)
                myCred.getDocument(new MimeMediaType("text/plain"));
            }
            else
                log.error("You cannot join repository");
            
            peerGroup.getRendezVousService().startRendezVous();
            peerGroup.getRendezVousService().addListener(this);
            log.info("PeerGroup rdv " +  peerGroup.getRendezVousService().getRendezVousStatus());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

	public void rendezvousEvent(RendezvousEvent event) {
		log.debug(event);
		if (event.getType() == RendezvousEvent.CLIENTRECONNECT){
			log.info("Client reconnected " + event.getPeer());
			this.addPeer((PeerID)event.getPeerID(), null);
		}
		
		if (event.getType() == RendezvousEvent.CLIENTCONNECT){
			log.info("Client connected " + event.getPeer());
			this.addPeer((PeerID) event.getPeerID(), null);
		}
		
		if (event.getType() == RendezvousEvent.CLIENTDISCONNECT){
			log.info("Client disconnected " + event.getPeer());
			this.removePeer((PeerID)event.getPeerID());
		}
		
	/*	if (event.getType() == RendezvousEvent.CLIENTFAILED){
			log.info("Client lost connection " + event.getPeer());
			this.lostPeer((PeerID)event.getPeerID());
		}*/
		
	}

	private ICMUHandler cmu_handler;
	public void setCMUHandler(ICMUHandler cmu_handler) {
		this.cmu_handler  = cmu_handler;
	}

	private JxtaMulticastSocket mc_socket = null;
	public JxtaMulticastSocket getMulticastSocket() throws GridException, IOException{
		if (mc_socket == null){
			PipeAdvertisement advertisement = (PipeAdvertisement)
				AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
			if (mc_socketId == null )
				throw new GridException("Multicast Socket ID is null");
			
			advertisement.setPipeID(mc_socketId);
			advertisement.setType(PipeService.PropagateType);
	
			System.out.println(advertisement.toString());
			mc_socket = new JxtaMulticastSocket(peerGroup,advertisement);
		}
		return mc_socket;	
	}

}
