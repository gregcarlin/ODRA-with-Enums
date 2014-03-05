package odra.virtualnetwork.pu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.socket.JxtaSocket;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.encoders.messages.MessageEncoder;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.base.DatabaseDiscovery;
import odra.virtualnetwork.cmu.CMUnit;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.pu.services.PeerMonitor;

import org.apache.log4j.Logger;

/**
 *Ta klasa reprezentuje czesc wirtualnego repozytorium, z ktorym bezposrednio wspopracuje dany wezel.
 *Lista wezlow jest uaktualnia przez PeerMonitor, kontrolujacy czas bezczynnosci wspolpracy miedzy 
 *wezlami w interesujacej nas czesci repozytorium.
 *Lista wezlow jest pusta jesli dany wezel "nic nie chcial" od innych przez dluzszy czas.
 */
public class Repository{
	
	public final static int MGMT_JOINTOGRID = 1;
	public final static int MGMT_REMOVEFROMGRID = 2;
	public final static int MGMT_GETMULTICASTID = 3; 

	public static Logger log = Logger.getLogger(Repository.class);
	
	private PeerGroup 				peerGroup = null;
	private PeerGroupAdvertisement 	peerGroupAdvertisement = null;
	
	private ModuleSpecAdvertisement managementModule = null; 
	
	//List of peers
	private Map<String, RepositoryPeer> peers = new Hashtable<String, RepositoryPeer>();
	
	//cmu mgmt service
	//private ModuleSpecAdvertisement cmuMgmtServAdv = null;
	
	private boolean joined = false;
	
	// temporary situation with static access
	public static Repository instance = null;
	public static Repository getInstance() throws Exception  {
		if (instance == null)  throw new Exception("Repository class not initialized!!");
		return instance;
	}
	/**
	 * Create Repository instance from CMU Adverisement
	 * @param adv adverisement of Management Service from CMU
	 * @param peerGroup peerGroup of repo
	 */
	public Repository(PeerGroupAdvertisement adv) {
		this.peerGroupAdvertisement = adv;
    	try {
    		//hotfix!!!
    		if (ClientUnit.rootGroup == null)
    			peerGroup = CMUnit.rootGroup.newGroup(peerGroupAdvertisement);
    		else peerGroup = ClientUnit.rootGroup.newGroup(peerGroupAdvertisement);
		} catch (PeerGroupException e) {
			e.printStackTrace();
		} 
		instance = this; 
	}
	
	/**
	 *Join to the repository. 
	 */
	public boolean join(){
		if (joined) return true;
		
        StructuredDocument creds = null;
        try {
            AuthenticationCredential authCred = new AuthenticationCredential(peerGroup, null, creds );
            MembershipService membership = peerGroup.getMembershipService();
            Authenticator auth = membership.apply( authCred );
            
            // Check if everything is okay to join the group
            if (auth.isReadyForJoin()){
                Credential myCred = membership.join(auth);
                StructuredTextDocument doc = (StructuredTextDocument)
                myCred.getDocument(new MimeMediaType("text/plain"));
            }
            else
                log.error("You cannot join repository");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        log.info("Joined to Virtual Repository (low-level)");
        //change DatabaseDiscovery
        DatabaseDiscovery dd = new DatabaseDiscovery(peerGroup.getParentGroup());
             
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        //looking for cmu service
        try {
			managementModule = dd.obtainManagementServ(peerGroup.getPeerID().toString());
		} catch (GridException e) {
			e.printStackTrace();
			return false;
		}
        
        
        //point to discovery service in joined group
        new DatabaseDiscovery(peerGroup);

        this.joined = true; 
        
		return true;
	}
	
	public void leave() {
		if (joined)
			;
	}
	
	public void updatePeerList(){
	}
	
	/**
	 * Sends a db requests over jxta
	 * @param name
	 * @param req
	 */
	public byte[] putRequest(String peerName, String userName, byte [] req) throws GridException{
		RepositoryPeer rp = getPeer(peerName);
		if (rp == null) throw new GridException("Peer not connected");
		return getPeer(peerName).putRequest(req, userName);
	}

	public String putManagementRequest(int mgmt_request_type, String [] args){
		try {
			DataOutputStream dos =  new DataOutputStream(this.getManagementSocket().getOutputStream());
			DataInputStream dis =  new DataInputStream(this.getManagementSocket().getInputStream());
			
			//pass request_type
			dos.writeInt(mgmt_request_type);

			dos.writeInt(Config.repoIdentity.getBytes().length);
			dos.write(Config.repoIdentity.getBytes());
			
			for (String arg : args){
				dos.writeInt(arg.getBytes().length);
				dos.write(arg.getBytes());	
			}
			
			dos.flush();
			dos.close();
			
			int msglen;
			msglen = dis.readInt();
			if (msglen == 0){
				this.managementCmuSocket = null;
				return null;
			}else if (msglen > 0){
				byte [] resparr = new byte[msglen]; 
				dis.read(resparr);
				this.managementCmuSocket = null;
				return new String(resparr);  
			}
			else if (msglen < 0 ){
				this.getManagementSocket().close();
				this.managementCmuSocket = null;
				//throw new P2PException("Remote peer report internal error");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.managementCmuSocket = null;
		
		return null;
	}

	public RepositoryPeer getPeer(String peerName){
		if (!peers.containsKey(peerName)){
			log.info("Discovering peer " + peerName);

			DatabaseDiscovery dd = DatabaseDiscovery.getDatabaseDiscovery();
			RepositoryPeer rp;
			PeerAdvertisement adv;
			try {
				adv = (PeerAdvertisement) dd.invokeAndWaitForOne(null, DiscoveryService.PEER, peerName, true);
				rp = new RepositoryPeer(adv, peerGroup);
				addPeer(rp);
			} catch (GridException e) {
				e.printStackTrace();
				return null;
			}
			
			return rp;
		}
		return peers.get(peerName);
	}
	
	public boolean contribModule(String schema, String user){
		return true;
	}
	
	public void addPeer(RepositoryPeer rp) throws GridException{
		if (Config.peerMonitor){
			if (peers.size()==0) new PeerMonitor().start(this);
		}
		
	
		rp.initialize();
		
		peers.put(rp.getPeerName(), rp);
	}
	
	public void removePeer(RepositoryPeer rp) {
		
		peers.remove(rp.getPeerName());	
		
		log.info("Peer "+rp.getPeerName()+" removed from peer list because idle timeout");
		rp.finish();
	}
	
	public String getPeerName(){
		return peerGroup.getPeerName();
	}

	public PeerGroup getDatabaseGroup() {
		return peerGroup;
	}
	
	public Map<String, RepositoryPeer> getPeerList(){
		return peers;
	}
	
	
	JxtaSocket managementCmuSocket = null;  
	public JxtaSocket getManagementSocket(){
		if (managementCmuSocket!=null) if (!managementCmuSocket.isClosed()) return managementCmuSocket; 
		
		try {
			managementCmuSocket = new JxtaSocket();
			managementCmuSocket.create(true);
			System.out.println(managementModule.getPipeAdvertisement().toString());
			managementCmuSocket.connect(peerGroup, null, managementModule.getPipeAdvertisement() , 10000);
			managementCmuSocket.setOutputStreamBufferSize(100);
		} catch (IOException e) {
			log.debug(e.getMessage()  + " : recursivly loop");
			managementCmuSocket = null;
			
			//some silly jxta behaviour workaround
			//TODO: cleanup this mess (add recursive depth)
			managementCmuSocket = getManagementSocket();
		}
		return managementCmuSocket;
	}
	
	//this is for translating DBRequest --> byte[] and byte[] --> DBReply --> Result
	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();
	
	//encapsulate request
	public byte [] encodeMessage(DBRequest req){
		return encoder.encodeRequestMessage(new DBRequest[] {req});
	}
	
	public DBReply decodeMessage(byte [] rply){
		try {
			return decoder.decodeReplyMessage(rply)[0];
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Iterator<RepositoryPeer> getPeerEnumerator(){
		return peers.values().iterator();
	}
}
