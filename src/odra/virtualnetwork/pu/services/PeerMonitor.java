package odra.virtualnetwork.pu.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.pu.Repository;
import odra.virtualnetwork.pu.RepositoryPeer;

import org.apache.log4j.Logger;

public class PeerMonitor extends Thread {
	
	public static Logger log = Logger.getLogger(PeerMonitor.class);
	
	private long sleepTime = Config.peerMonitorSleepTime;
	private static PeerMonitor instance = null;
	
	public PeerMonitor(){
		instance = this;
	}
	
	public static PeerMonitor getInstance(){
		if (instance == null)
			new PeerMonitor();
		return instance;
	}
	
	public void run(){
		try {
			//work as long as we have some peers in our repository part
			while (repository.getPeerList().size()!=0){
				
				sleep(sleepTime);
		
				Hashtable<String, RepositoryPeer> peers = new Hashtable(repository.getPeerList());
				
				log.debug("Monitoring " + peers.size() +" peers");
				synchronized (repository.getPeerList()){
					
				Iterator<RepositoryPeer> i = peers.values().iterator();
				while (i.hasNext()){
					RepositoryPeer peer = i.next();
					if ((peer.getDatabaseSocket()!=null)&&(peer.lock==false)){
						peer.peerIdle += 1;
						if (peer.peerIdle > 10){
							repository.removePeer(peer);
						}
					}
					//if (rp.lock) log.debug("Working peer lock");	
				}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Repository repository = null;
	public void start(Repository repository){
		log.info("starting peer monitor");
		this.repository = repository;
		super.start();
	}
}
