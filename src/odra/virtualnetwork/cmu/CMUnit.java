

package odra.virtualnetwork.cmu;

import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.PeerGroupAdvertisement;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.facade.ICMUHandler;
import odra.virtualnetwork.facade.ICMUUnit;
import odra.virtualnetwork.base.Advertisements;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.base.Configurator;
import odra.virtualnetwork.base.DatabaseDiscovery;
import odra.virtualnetwork.cmu.services.ManagementService;
import odra.virtualnetwork.pu.Repository;
import odra.virtualnetwork.pu.services.DatabaseService;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



public class CMUnit implements ICMUUnit
{

	/** module */
	//private DBModule module;
	
	static Logger log = Logger.getLogger(CMUnit.class);
	
	public static PeerGroup rootGroup = null;
	public static PeerGroup databaseGroup = null;
	public static PeerGroupAdvertisement databaseAdv = null;
	
	//there can be only one!
	private static CMUnit instance = null;

	private DiscoveryService discoveryService = null;
	private DiscoveryService globalDiscoveryService = null;
	
	private DatabaseService db_serv = null;

	public CMUnit()	{
		PropertyConfigurator.configure("./conf/log4j.properties");
		instance = this;
	}
	
	public static CMUnit getInstance () throws GridException {
		if (instance == null) throw new GridException("CMUnnit not initalized");
		return instance; 
	}

	
	public ICMUHandler cmu_handler;
	public CMUnit(ICMUHandler cmu_handler){
		this.cmu_handler = cmu_handler;
		instance = this;
		
		PropertyConfigurator.configure("./conf/log4j.properties");	
	}
	

	/**
	 * Initialize CMU 
	 * @param basedir for advertisements and platform configurations
	 * @param name of the peer - that shall be name of the virual repository
	 */
	public void initialize(){
		
		System.out.println("Virtual Network Initialization...");
		try {
			rootGroup = new NetPeerGroupFactory(
					(ConfigParams)Configurator.createConfigServerRandezVous(Config.platformHome.getRawPath(), Config.repoIdentity).getPlatformConfig(),
					Config.platformHome).getInterface();
		} catch (PeerGroupException e) {
			e.printStackTrace();
		}	
		
		globalDiscoveryService = rootGroup.getDiscoveryService();
		
		//create Management Service Advertisements for root group
		//that shall be moved to database group 
		Advertisements advManager = new Advertisements(Config.platformHome);
		advManager.initializePeerGroupAdvertisement(Config.repoGroup, rootGroup);
		
		Repository repo = Network.createRepository(rootGroup, advManager);
		Network.getInstance().setCMUHandler(cmu_handler);
		
		databaseGroup = repo.getDatabaseGroup();

		//start management service
		advManager.initializeManagementAdverisements(databaseGroup);
		advManager.initializeMulticastAdvertisements(databaseGroup);
		ManagementService mgmt_service = 
			new ManagementService(databaseGroup, advManager, cmu_handler);
		
		Network.mc_socketId = mgmt_service.getMulticastSocketID();
		
		mgmt_service.start();
		
		//start database service (we want to have links to cmu)
		db_serv = new DatabaseService(repo.getDatabaseGroup(), advManager);
		db_serv.start();
			
		//this will be on exit
		advManager.saveAdvertisements();
		
		//point DatabaseDiscovery for this group
		new DatabaseDiscovery(databaseGroup);
	}
	
	public void setCMUHandler(ICMUHandler handler) {
		this.cmu_handler = handler;
		
	}
	
	public static void main(String[] args){
		if (args.length == 0){
			System.out.println("CMunit args:");			
			System.out.println("rdv=rendezvous_ip");
			System.out.println("rdv_server=yes|no");
			System.out.println("PlatformHome=uri_path");
		}
		URI basedir = null;
		for (String arg : args){
			String [] param = arg.split("=");
			if (param[0].equals("rdv")){
					System.out.println("#Error CMUnit can't connect to other jxta network (testing pourposes)");
					return;
			}
			if (param[0].equals("rdv_server")){
					System.out.println("#Error CMUnit is always cmu (testing pourposes)");
					return;
			}
			if (param[0].equals("PlatformHome")){
				try {
					basedir = new URI(param[1]);
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return;
				}
			}
		}
		
		
				
		if (basedir!=null){
			new CMUnit().initialize();
		} else{
			System.out.println("You must point to Platform home (eg. file:///tmp/odrap2p)");
		}

	}


}
