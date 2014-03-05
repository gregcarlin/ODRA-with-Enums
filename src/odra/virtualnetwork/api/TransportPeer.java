package odra.virtualnetwork.api;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import net.jxta.impl.rendezvous.PeerConnection;

import odra.db.links.RemoteDefaultStoreOID;
import odra.exceptions.rd.RDNetworkException;
import odra.filters.DataImporter;
import odra.network.encoders.results.QueryResultDecoder;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.security.UserContext;
import odra.virtualnetwork.CMUHandlerImpl;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.base.LocalDatabase;
import odra.virtualnetwork.facade.ICMUHandler;

/**
 * This class represents transport application on database side. It communicate with app using
 * very simple raw protocol on loopback sockets.  Its equivalen on app-side is
 * {@link LocalDatabase}
 */
public class TransportPeer extends Thread{
	
	IRequestHandler reqHandler = null;
	ICMUHandler cmuHandler = null;
	int port;
	private static TransportPeer instance = null;
	
	/**
	 * Contruct TansportPeer for peer unit with specified {@link IRequestHandler}
	 * @param port where the transport application listen
	 * @param requestHandler request handler implemetation
	 * @see RequestHandlerImpl
	 */
	public TransportPeer(int port, IRequestHandler requestHandler) {
		this.port = port;
		this.reqHandler = requestHandler;
		instance = this;
	}


	/**
	 * Contruct TansportPeer for central management unit with specified @see IRequestHandler
	 * @param port where the transport application listen
	 * @param requestHandler request handler implemetation
	 * @param cmuHandler request handler for cmu
	 * @see CMUHandlerImpl
	 */
	public TransportPeer(int port, IRequestHandler requestHandler, ICMUHandler cmuHandler) {
		this.port = port;
		this.reqHandler = requestHandler;
		this.cmuHandler = cmuHandler;
		instance = this;
	}	
	public static TransportPeer getInstance(){
		if (instance == null){
			//later
			System.out.println("Fixmee!!!! TransportPeer class");
		}
		return instance;
	}
	
	Object peer_lock = new Object();
	
	DataOutputStream peerOutput = null;
	DataInputStream peerInput = null;

	/**
	 * Simple pass to Transport peer.
	 * Peer will send <I>rawreq</I> to remote peer with name <I>peerName</I>, 
	 * where request shall be handled
	 * @param Name of the peer that shall be translated into PeerID
	 * @param raw request from local DB  
	 * @return raw response from remote DB
	 * @throws RDNetworkException 
	 */
	public byte[] request(String peerName, byte [] rawreq) throws RDNetworkException{
		if (isConnected == false) throw new RDNetworkException("You must build connection between p2papp and db (TransportPeer.connect)");
		byte [] rawrpl = null;
		try {	
			synchronized (peer_lock) {
				
			//first goes peer name
			peerOutput.writeInt(peerName.length());
			peerOutput.write(peerName.getBytes());
			//then raw request
			peerOutput.writeInt(rawreq.length);
			peerOutput.write(rawreq);
			
			peerOutput.flush();
			
			//read response
			int msglen;
			msglen = peerInput.readInt();
			
			//if error
			if (msglen < 0)
				return null;
			
			rawrpl = new byte[msglen];
			peerInput.read(rawrpl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rawrpl;	
	}
	
	private boolean isConnected = false;
	/**
	 * Connect to local jxta app and starts a listener Thread that waits for remote requests
	 * @return
	 */
	public boolean connect(){
		try {
			Socket socket =  new Socket(InetAddress.getByName("localhost"), port);
			peerInput = new DataInputStream(socket.getInputStream());
			peerOutput = new DataOutputStream(socket.getOutputStream());
		
			//read port 
			int port = peerInput.readInt();
			
			listeningSocket = new Socket();
			listeningSocket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), port));
			isConnected = true;
			
		} catch (IOException e) {
			e.printStackTrace();
			isConnected = false;
		}
		
		this.setName("svrp-p2p");
		this.start();
		return true;
	}
	
	
	Socket listeningSocket = null; 
	//handle request from remote peers (across tranaport app)
	public void run(){
		try {
			DataInputStream input = new DataInputStream(listeningSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(listeningSocket.getOutputStream());
			
			while(true){
				//wait for msg type
				byte [] rep = null;
				int msg_type = input.readInt();
				//handle peer request
				
				int reqlen = input.readInt();				
				
				if (msg_type == PeerMessage.REQUEST){
					byte [] req = new byte[reqlen];
					input.read(req);
					rep = reqHandler.handleRequest("admin", req);					
				}else {
					//check if this is cmu db
					byte [][] req = new byte[reqlen][];
					for (int i=0; i<reqlen;i++){
						req[i] = new byte[input.readInt()];
						input.read(req[i]);
					}
					//if everything allright processRequest
					rep = processCMUCommand(msg_type, req);
				}
				if (rep != null){
					output.writeInt(rep.length);
					output.write(rep);
				} else output.writeInt(0);
				output.flush();
					
			}
		} catch (IOException e) {
			System.out.println("Exception when dbinstance is going down and RequestHandlerImpl don't know about that -- implement later");
			e.printStackTrace();
		}
	}
	
	private byte [] processCMUCommand(int msg_type, byte [][] req) {
		//if (msg_type == PeerMessage.SETUP)			
			//cmuHandler.setup();
		
		if (msg_type == PeerMessage.CMU_JOINPEER)
			cmuHandler.peerJoined(new String(req[0]));
	
		return new byte[] {'o', 'k'};
	}
}
