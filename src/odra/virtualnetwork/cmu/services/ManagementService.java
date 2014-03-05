package odra.virtualnetwork.cmu.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;
import odra.virtualnetwork.facade.ICMUHandler;
import odra.virtualnetwork.base.Advertisements;
import odra.virtualnetwork.pu.Repository;

import org.apache.log4j.Logger;

public class ManagementService extends Thread implements PipeMsgListener{

	static Logger log = Logger.getLogger(ManagementService.class);
	
	private PeerGroup pgroup = null;
	private PipeAdvertisement pipeAdv = null;
	
	private Advertisements advManager = null; 
	
	private ICMUHandler cmu_handler; 
	
	/** 
	 * Create Management Service for this group with early specified advertisement.
	 * We can recover service from brakedown so we wan't to have adverisement somwhere else
	 * @param peerGroup
	 * @param pipeAdvertisement
	 */
	public ManagementService(PeerGroup peerGroup, Advertisements advManager, ICMUHandler cmu_handler){
		this.setName("srvc-mgmt");
		pgroup = peerGroup;
		pipeAdv = (PipeAdvertisement) advManager.getAdvertisement(Advertisements.MANAGMENTSOCKET_ADV);
		
		log.debug("publishing management service advertisements");
		
		//publishing in ParentGroup 
		DiscoveryService ds = peerGroup.getParentGroup().getDiscoveryService();
		try {
			//ds.publish(advManager.getAdvertisement(Advertisements.MANAGMENTMODULE_ADV));
			ds.publish(advManager.getAdvertisement(Advertisements.MANAGMENTSERVICE_ADV));
			//ds.publish(advManager.getAdvertisement(Advertisements.MANAGMENTSOCKET_ADV));
			//ds.remotePublish(advManager.getAdvertisement(Advertisements.MANAGMENTMODULE_ADV));
			ds.remotePublish(advManager.getAdvertisement(Advertisements.MANAGMENTSERVICE_ADV));
			//ds.remotePublish(advManager.getAdvertisement(Advertisements.MANAGMENTSOCKET_ADV));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.cmu_handler = cmu_handler;
		this.advManager = advManager;
	}
	
	public void run(){
		log.info("Management Service starting");
		JxtaServerSocket serverSocket = null;
		JxtaSocket socket = null; 
		
		try {	
			serverSocket = new JxtaServerSocket(pgroup, pipeAdv);
			//must be like that... as i think -- now I'm sure
			serverSocket.setSoTimeout(0);
			while (true){
				log.debug("Waiting for message");
				socket = (JxtaSocket) serverSocket.accept();
				if (socket != null){
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					DataOutputStream dos = new DataOutputStream (socket.getOutputStream());
					
					int mgmt_request_type = dis.readInt();
					
					byte [] peerName = new byte[dis.readInt()];
					dis.read(peerName);
							
					byte [] schema, user;
					
					switch (mgmt_request_type) {
						case Repository.MGMT_JOINTOGRID:
							schema = new byte[dis.readInt()];
							dis.read(schema);					
							user = new byte[dis.readInt()];
							dis.read(user);
							cmu_handler.peerContributedSchema(new String(peerName), new String(schema), new String(user));
							
							dos.writeInt(0);
							
							break;
						case Repository.MGMT_REMOVEFROMGRID:
							schema = new byte[dis.readInt()];
							dis.read(schema);					
							user = new byte[dis.readInt()];
							dis.read(user);
							cmu_handler.removeContributedPeer(new String(peerName), new String(schema), new String(user));
							
							dos.writeInt(0);
							
							break;
							
						case Repository.MGMT_GETMULTICASTID:
							String socketURI = getMulticastSocketID().toURI().toString();
							
							dos.writeInt(socketURI.getBytes().length);
							dos.write(socketURI.getBytes());
							
							break;
					}			
					
					dos.flush();
					dos.close();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	public void pipeMsgEvent(PipeMsgEvent arg0) {
	}
	
	public PipeID getMulticastSocketID(){
		PipeAdvertisement adv = (PipeAdvertisement) advManager.getAdvertisement(Advertisements.MULTICASTSOCKET_ADV); 
		return (PipeID) adv.getPipeID();
	}

}
