package odra.virtualnetwork.pu.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import odra.virtualnetwork.GridException;
import odra.virtualnetwork.base.Advertisements;
import odra.virtualnetwork.cmu.Network;

import org.apache.log4j.Logger;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

/**
 * @author mich
 * This class deals with jxta low-level service starting feature. It starts a ServerSocket and waits
 * for incomming request. After accepting connection DatabaseWorker thread is started.  
 */
public class DatabaseService extends Thread implements PipeMsgListener {
	
	static Logger log = Logger.getLogger(DatabaseService.class);

	PeerGroup pgroup = null;
	PipeAdvertisement pipeAdv = null; 
	public DatabaseService(PeerGroup peerGroup, Advertisements advManager){
		this.setName("srvc-db");
		pgroup = peerGroup;
		advManager.initializeDatabaseAdverisements(peerGroup);
		
		//saving on exit - later;
		advManager.saveAdvertisements();
		pipeAdv = (PipeAdvertisement) advManager.getAdvertisement(Advertisements.DATABASESOCKET_ADV);

		try {
			peerGroup.getDiscoveryService().publish(advManager.getAdvertisement(Advertisements.DATABASESERVICE_ADV));
			peerGroup.getDiscoveryService().remotePublish(advManager.getAdvertisement(Advertisements.DATABASESERVICE_ADV));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private JxtaServerSocket serverSocket = null;
	public void run(){
		log.info("Database Service starting");
		JxtaSocket socket = null;
		
		try {			
			serverSocket = new JxtaServerSocket(pgroup, pipeAdv, 20);
			serverSocket.setPerformancePreferences(1,0,0);
			serverSocket.setSoTimeout(0);
			
			while (true){
				log.debug("Wait for request");
				socket = (JxtaSocket) (serverSocket).accept();
				if (socket != null)
					new DatabaseWorker(socket).start();
			}
		} catch (SocketException e){
//			if (e.getMessage().equals("Socket closed"))
//				log.debug("Closing server socket")
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	//this is not reely needed for now
	public void pipeMsgEvent(PipeMsgEvent arg0) {
		log.info("$$$$$$$$$$$$$$$"+arg0);
	}

	public void shutdown() {
		try {
			serverSocket.close();
			//pgroup.getPipeService().stopApp();
			//hard kill thread
			//serverSocket.close don't throw SocketException (why?!?!?)
			this.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Stopping DatabaseService");
	}

}
