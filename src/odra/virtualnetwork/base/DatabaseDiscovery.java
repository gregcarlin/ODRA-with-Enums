package odra.virtualnetwork.base;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.pu.Repository;

import org.apache.log4j.Logger;

/**
 *This class is responsible for discovering virtual repositories
 *If you want to obtain repository class by known name we use <I>findDatabase</I>
 *Discovering all visible repositories is made by <I>discoverInfoService</I>    
 */
public class DatabaseDiscovery implements DiscoveryListener {
	
	private static Logger log = Logger.getLogger(DatabaseDiscovery.class);
	
	private DiscoveryService discovery = null;
	private PeerGroup peerGroup = null;
	private Vector<Advertisement> advertisements = new Vector<Advertisement>();;
	
	
	public DatabaseDiscovery(PeerGroup peerGroup){
		this.peerGroup = peerGroup;
		this.discovery = peerGroup.getDiscoveryService();
		me = this;
	}
	
	private static DatabaseDiscovery me = null;
	
	public static DatabaseDiscovery getDatabaseDiscovery(){
		return me;
	}
		
	public void discoveryEvent(DiscoveryEvent ev) {
        DiscoveryResponseMsg res = ev.getResponse();
        log.debug("Advertisement Found");
        Advertisement adv = null;
        Enumeration en = res.getAdvertisements();

        if (en != null ) {
            while (en.hasMoreElements()) {
                adv = (Advertisement) en.nextElement();
                // dopisac kontrole powtorzen
                advertisements.add(adv);
            }
        }
	}
	
	/**
	 * Try to find Odra Managment Service (published by CMU)
	 * @param peerId if null try to find various peers
	 * @return
	 * @throws GridException 
	 */
	public ModuleSpecAdvertisement obtainManagementServ(String peerId) throws GridException{
		log.debug("Obtaining Managment Service");
		Enumeration en = null;
		ModuleSpecAdvertisement adv = null;
		try {
			en = discovery.getLocalAdvertisements(DiscoveryService.ADV, "Name", "JXTASPEC:ODRAMANAGEMENTSERVICE");
			if (en.hasMoreElements()){
				adv =   (ModuleSpecAdvertisement) en.nextElement();
				log.debug("Managment Service found in local cache");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (adv==null){
					
			/*try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			
			adv = (ModuleSpecAdvertisement)invokeAndWaitForOne(null, DiscoveryService.ADV , "*ODRAMANAGEMENTSERVICE", true);
			log.debug("Managment Service  found in remote peer");
		}				
		return adv;
	}
	
	
	private Iterator invokeAndWait(String peerID, int type, String name) throws GridException{
		advertisements.clear();
		discovery.getRemoteAdvertisements(peerID, type, "Name", name, 1, this);
		try {
			long startSearchTime = System.currentTimeMillis();
			while (advertisements.size()==0){
				System.out.println(".");
				Thread.sleep(Config.searchLoopTimeout);
				if (startSearchTime + Config.searchAdvsTimeout <= System.currentTimeMillis())
					throw new GridException("Search advertisement timeout");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		return advertisements.iterator();
	}
	
	public Advertisement invokeAndWaitForOne(String peerID, int type, String name, boolean withLocalCache) throws GridException{
		log.debug("Looking for adv " + name +" on group "+ peerGroup.getPeerGroupName());
		if (withLocalCache){
			Enumeration en = null;
			try {
				en = discovery.getLocalAdvertisements(type, "Name", name);
				if (en.hasMoreElements()){
					log.debug("Adverisement found in local cache");
					return  (Advertisement) en.nextElement();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		advertisements.clear();
		log.debug("looking on " + peerGroup.getPeerGroupName());
		discovery.getRemoteAdvertisements(peerID, type, "Name", name, 1, this);
		try {
			long startSearchTime = System.currentTimeMillis();
			while (advertisements.size()==0){
				System.out.print(".");
				Thread.sleep(Config.searchLoopTimeout);
				if (startSearchTime + Config.searchAdvsTimeout <= System.currentTimeMillis())
					throw new GridException("Search advertisement timeout");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return advertisements.iterator().next();
	}
	
	public PeerAdvertisement invokeAndWaitForOne(String peerID, boolean withLocalCache) throws GridException{
		log.debug("Looking for peer adv on group "+ peerGroup.getPeerGroupName());
		if (withLocalCache){
			Enumeration en = null;
			try {
				en = discovery.getLocalAdvertisements(DiscoveryService.PEER, "PID", peerID);
				if (en.hasMoreElements()){
					log.debug("Adverisement found in local cache");
					return  (PeerAdvertisement) en.nextElement();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		advertisements.clear();
		discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, null, null, 1, this);
		try {
			long startSearchTime = System.currentTimeMillis();
			while (advertisements.size()==0){
				System.out.println(".");
				Thread.sleep(Config.searchLoopTimeout);
				if (startSearchTime + Config.searchAdvsTimeout <= System.currentTimeMillis())
					throw new GridException("Search advertisement timeout");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (PeerAdvertisement) advertisements.iterator().next();
	}
	
	public Repository findRepository(String name){		
		//find group
		log.debug("Looking for Repository: " + name);
		Enumeration en = null;
		PeerGroupAdvertisement adv = null;
		try {
			en = discovery.getLocalAdvertisements(DiscoveryService.GROUP, "Name", name);
			if (en.hasMoreElements()){
				adv =  (PeerGroupAdvertisement) en.nextElement();
				log.debug("Repository Adverisement found in local cache");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (adv==null){
			try {
				adv = (PeerGroupAdvertisement) invokeAndWaitForOne(null, DiscoveryService.GROUP, name, false);
			} catch (GridException e) {
				e.printStackTrace();
			}
			log.debug("Repository Adverisement found in remote peer");
		}
		
		return new Repository(adv);
	}

	public PipeAdvertisement getPeerAdvs(String peerID) throws GridException {
		log.debug("Getting adv from peer"); 
		Iterator i = invokeAndWait(peerID, DiscoveryService.ADV, "JXTASPEC:DATABASESERVICE");
		
		if (i.hasNext()){
			ModuleSpecAdvertisement adv =(ModuleSpecAdvertisement)i.next();
			return adv.getPipeAdvertisement();
		}
		
		return null;
	}

}
