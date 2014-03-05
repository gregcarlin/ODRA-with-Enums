package odra.virtualnetwork.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import odra.cli.ast.ConnectCommand;
import odra.cli.ast.DatabaseURL;
import odra.exceptions.rd.RDNetworkException;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigDebug;
import odra.virtualnetwork.CMUHandlerImpl;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.facade.ICMUHandler;

/**
 * This class represents transport bridge beetween p2p virtual network and db instance in local environment.
 * It is based on DBConnection class. Its equivalent on app-side is
 * {@link LocalDatabase}
 */
public class LocalTransport extends Thread{
	
	IRequestHandler reqHandler = null;
	ICMUHandler cmuHandler = null;
	int port;
	private static LocalTransport instance = null;
	DataOutputStream peerOutput = null;
	DataInputStream peerInput = null;
	Socket listeningSocket = null;
	private boolean isConnected = false;
	Object peer_lock = new Object();

	protected DBConnection db;
	private String currmod = "";
	protected StringBuffer outputBuffer = new StringBuffer();
	protected String NEW_LINE = System.getProperty("line.separator");


	
	
	/**
	 * Construct simple test communication
	 */
	
	public LocalTransport(int peerport){
		this.port = peerport;
		
		//connection to local db instance
		try
		{
			System.out.println("Test LocalTransport - intitialization...");
			Thread.sleep(500);
			
			currmod = "";

			db = new DBConnection("localhost", 1521);
			db.connect();

			DBRequest req = new DBRequest(DBRequest.LOGIN_RQST, new String[] { "admin", "admin" });
			DBReply rep = db.sendRequest(req);
			
			currmod = "admin";
		}
		catch(Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			outputBuffer.append(exc.getMessage() + NEW_LINE);
			System.out.println("Test LocalTransport - not initialized...");
		}
	}

	/**
	 * Contruct TansportPeer for PU (peer unit) with specified {@link IRequestHandler}
	 * @param port where the transport application listen
	 * @param requestHandler request handler implemetation
	 * @see RequestHandlerImpl
	 */
	public LocalTransport(int peerport, IRequestHandler requestHandler) {
		this.port = peerport;
		this.reqHandler = requestHandler;
		instance = this;
		
		getDBConnection();
	}


	/**
	 * Construct TansportPeer for CMU (central management unit) with specified @see IRequestHandler
	 * @param port where the transport application listen (jxta side)
	 * @param requestHandler request handler implemetation
	 * @param cmuHandler request handler for cmu
	 * @see CMUHandlerImpl
	 */
	public LocalTransport(int peerport, IRequestHandler requestHandler, ICMUHandler cmuHandler) {
		this.port = peerport;
		this.reqHandler = requestHandler;
		this.cmuHandler = cmuHandler;
		instance = this;
		
		getDBConnection();
	}	
	
	//	connection to local db instance
	private boolean getDBConnection() {
		try
		{
			System.out.println("Test LocalTransport - intitialization...");
			Thread.sleep(500);
			
			currmod = "";

			db = new DBConnection("localhost", 1521);
			db.connect();

			DBRequest req = new DBRequest(DBRequest.LOGIN_RQST, new String[] { "admin", "admin" });
			DBReply rep = db.sendRequest(req);
			
			currmod = "admin";
		}
		catch(Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			outputBuffer.append(exc.getMessage() + NEW_LINE);
			System.out.println("Test LocalTransport - not initialized...");
		}
	return true;
	}
		
	public static LocalTransport getInstance(){
		if (instance == null){
			System.out.println("LocalTransport class instance is null!");
		}
		return instance;
	}
	/**
	 * It creates connection to local jxta app and starts a listener Thread that waits for remote requests
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
		if (msg_type == PeerMessage.SETUP)			
	//		cmuHandler.setup();
		
		if (msg_type == PeerMessage.CMU_JOINPEER)
			cmuHandler.peerJoined(new String(req[0]));
	
		return new byte[] {'o', 'k'};
	}

}




