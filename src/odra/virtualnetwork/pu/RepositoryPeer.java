package odra.virtualnetwork.pu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.base.DatabaseDiscovery;

import org.apache.log4j.Logger;

public class RepositoryPeer {
	
	public static Logger log = Logger.getLogger(RepositoryPeer.class);
	
	private String peerName = null;
	private PipeAdvertisement pipeAdv = null;
	private PeerAdvertisement peerAdv = null;
	private PeerGroup peerGroup = null;
	
	public RepositoryPeer(String peerName, PeerGroup peerGroup){
		this.peerName = peerName;
		this.peerGroup = peerGroup;
		log.error("Not implemented constructor: we must find peeradv");
		
	}
	
	public RepositoryPeer(PeerAdvertisement adv, PeerGroup peerGroup) {
		this.peerName = adv.getName();
		this.peerGroup = peerGroup;
		this.peerAdv = adv;
	}

	public void initialize() throws GridException{
		if (pipeAdv == null){
			DatabaseDiscovery dd = DatabaseDiscovery.getDatabaseDiscovery();
			pipeAdv = ((ModuleSpecAdvertisement) 
					dd.invokeAndWaitForOne(peerAdv.getPeerID().toString(), DiscoveryService.ADV, "JXTASPEC:"+peerName+":DATABASESERVICE", true))
					.getPipeAdvertisement();
		}
	}
	
	public String getPeerName(){
		return peerName;
	}
	
	JxtaSocket socket = null;
	public JxtaSocket getDatabaseSocket(){
		if (socket != null) return socket;
		
		try {
			socket = new JxtaSocket();
			socket.create(true);
			socket.connect(peerGroup, null, pipeAdv, 10000);
			socket.setOutputStreamBufferSize(100);
		} catch (IOException e) {
			log.debug("e.getMessage() : " + "recursivly loop");
			socket = null;
			
			//some silly jxta behaviour workaround
			//TODO: cleanup this mess (add recursive depth)
			socket = getDatabaseSocket();
		}
		return socket;
	}
	
	public PipeAdvertisement getPipeAdv() throws GridException {
		if (pipeAdv == null) 
			initialize();
		return pipeAdv;
	}
	
	public Boolean lock = new Boolean(false);
	
	public byte [] putRequest(byte [] req, String userName) throws GridException {
		byte[] rplarr = null;
		synchronized (lock){
			
			lock = true;
			
			JxtaSocket socket = getDatabaseSocket();
			if (socket == null)
				throw new GridException("Communication not established");
		
			DataOutputStream output;
			try {
				output = new DataOutputStream(socket.getOutputStream());
				//first send the userName
				byte [] userNameArr = userName.getBytes();				
				output.writeInt(userNameArr.length);
				output.write(userNameArr);
				output.writeInt(req.length);
				output.write(req);
				output.flush();
			
				DataInputStream input = new DataInputStream(socket.getInputStream());
			
				int msglen;
				msglen = input.readInt();
				if (msglen < 0 ){
					socket.close();
					throw new GridException("Remote peer report internal error");
				}
				rplarr = new byte[msglen];
				input.read(rplarr);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			peerIdle = 0;
			
			lock = false;
		}
		return rplarr;
	}
	
	//for Peer Monitor
	public int peerIdle = 0;

	public void finish() {
		//lock peer for time of finishing
		lock = true;
		try {
	 		socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket = null;		
	}

}
