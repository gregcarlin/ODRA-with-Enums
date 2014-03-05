package odra.virtualnetwork.pu;

import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import odra.db.objects.data.DBModule;
import odra.virtualnetwork.base.Advertisements;
import odra.virtualnetwork.base.Configurator;
import odra.virtualnetwork.base.DatabaseDiscovery;
import odra.virtualnetwork.base.LocalDatabase;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.facade.IPeerUnit;
import odra.virtualnetwork.pu.services.DatabaseService;
import odra.virtualnetwork.pu.services.MulticastService;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ClientUnit implements IPeerUnit{

	/** module */
	private DBModule module;
	
	static Logger log = Logger.getLogger(ClientUnit.class);
	
	public static PeerGroup rootGroup = null;
	public static PeerGroup databaseGroup = null;
	public String peerName = null;
	public PeerAdvertisement peerAdv = null; 
	
	private DiscoveryService rootDiscoveryService;
	private LocalDatabase localDb = null;
	private DatabaseService db_serv = null;
	private MulticastService mc_serv = null;
	private Repository repo = null;
	
	private RendezVousService rendezvous;

	/** Listen for local db and ready to start jxta app*/
	private boolean runningLight = false;
	/** runningLight == true and jxta app started*/
	private boolean running = false;
	
	public ClientUnit(){
	}

	
	public void initialize(){	

		if (!runningLight) startLight();
		
		Advertisements advManager = new Advertisements(Config.platformHome);
		
		
		log.debug("Initializations");

		try {
			NetworkConfigurator nc = Configurator.createConfigClient(Config.repoIdentity, Config.platformHome);
			rootGroup = new NetPeerGroupFactory((ConfigParams)nc.getPlatformConfig(), Config.platformHome).getInterface();
			peerAdv = rootGroup.getPeerAdvertisement();
			peerName = peerAdv.getName();
			
		} catch (PeerGroupException e) {
			e.printStackTrace();
		}
		
		rendezvous = rootGroup.getRendezVousService();
		rootDiscoveryService = rootGroup.getDiscoveryService();
		
		if (waitForRendezvousConncection(500, 100))
			log.info("Connected to Rendezvous");
		else 
			log.error("Not connected to Rendezvous");

		//DatabaseDiscovery dd = new DatabaseDiscovery(rootGroup);
		//repo = dd.findRepository(Config.repoGroup);		
	}
	
	public void startLight(){
		
		PropertyConfigurator.configure("./conf/log4j.properties");
		
		//localDb = new LocalDatabase(null);
		//localDb.server(Config.peerPort);
		
		//while (!localDb.server) ;
		
		runningLight = true;
	}
	
	public void start(){
		
		if (!runningLight) startLight();
		
		Advertisements advManager = new Advertisements(Config.platformHome);
		
		initialize();
		
		DatabaseDiscovery dd = new DatabaseDiscovery(rootGroup);
		repo = dd.findRepository(Config.repoGroup);
		
		db_serv = new DatabaseService(repo.getDatabaseGroup(), advManager);
		db_serv.start();
				
		if (repo.join())
			log.info("Joined to repository");
		else {
			log.error("Joining to repository failed"); 
			return; 
		}
		
		String mcast_id_uri =  repo.putManagementRequest(Repository.MGMT_GETMULTICASTID, new String[] {});
				
		try {
			PipeID mcast_id = (PipeID) IDFactory.fromURI(new URI(mcast_id_uri));
			
			mc_serv = new MulticastService(repo.getDatabaseGroup(), advManager, mcast_id);
			mc_serv.start();
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void stop(){
		log.debug("Finishing jxta");
		repo.leave();
		localDb.shutdown();
		db_serv.shutdown();
		rootGroup.stopApp();
		repo.getDatabaseGroup().stopApp();
	}

    private final static String connectLock = new String("connectLock");
    public boolean waitForRendezvousConncection(long timeout, int retrs) {
		log.info("Waiting for Rendezvous Connection");
    	while (retrs > 0){
    		if (!rendezvous.isConnectedToRendezVous() || !rendezvous.isRendezVous()) {
    			try {
    				if (!rendezvous.isConnectedToRendezVous()) {
    					synchronized(connectLock) {
    						connectLock.wait(timeout);
    					}
    				} else return true;    			
    			} catch (InterruptedException e) {
    			log.error("Not connected to Rendezvous. Interrupt");
    			return false;
    			}  
    		} else return true; 
    		retrs--;
    	}
		return false;
    }
	
}
