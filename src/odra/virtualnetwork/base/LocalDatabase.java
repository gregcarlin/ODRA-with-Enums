package odra.virtualnetwork.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import odra.exceptions.rd.RDException;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.ast.expressions.InExpression;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.api.PeerMessage;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.pu.Repository;

import org.apache.log4j.Logger;

/**
 * @author mich
 * This class represents an instance of Local Database on transport application. It communicate with
 * database using very simple raw protocol on loopback sockets. Its equivalen on db-side is
 * {@link TransportPeer}
 */
public class LocalDatabase extends Thread {
	
	static Logger log = Logger.getLogger(LocalDatabase.class);	

	int port = 9552;
	private ServerSocket serverSocket = null;
	//socket scenario: remote peer --> local peer --> local database (requestSocket) --> TransportPeer (IRequestHandler) 
	private Socket remoteRequestSocket  = null;
	//socket scenario: TransportPeer.request() --> this socket --> RemotePeer (remote LocalDatabase) --> DB 
	private Socket localRequestSocket = null;
	private boolean isLocalConnected = false;
	
	private Repository repo;
	
	private static LocalDatabase instance = null;
	
	public LocalDatabase(Repository repository){
		this.repo = repository;
		instance = this;
	}
	
	public void setRepository(Repository r){
		this.repo = r;
	}
	
	
	/**
	 * Fall back for listen on specified port
	 * @param port
	 */
	public void server(int port) {
		this.port = port;
		this.start();
	}
	
	boolean loop = true;
	public boolean server = false;
	public void run(){
		while (loop){
			try {
				
				log.debug("Binding server socket on " + InetAddress.getByName("localhost") + ":" + port);
				serverSocket = new ServerSocket(port, 12, InetAddress.getByName("localhost"));
				server = true;
				
				//db connects to this socket and fall back for listening
				localRequestSocket = serverSocket.accept();
				localReqInput = new DataInputStream(localRequestSocket.getInputStream());
				localReqOutput = new DataOutputStream(localRequestSocket.getOutputStream());
				
				//close serverSocket (because: jxtapp <--[1..1]--> db)
				serverSocket.close();
				
				//we want to establish communication (two steps)
				//1. create socket connection for request from remote peers and send port to db
				ServerSocket ssocket = new ServerSocket();
				ssocket.bind(null);
				localReqOutput.writeInt(ssocket.getLocalPort());
				localReqOutput.flush();
				//wait for db
				remoteRequestSocket = ssocket.accept();
				reqInput = new DataInputStream(remoteRequestSocket.getInputStream());
				reqOutput = new DataOutputStream(remoteRequestSocket.getOutputStream());
				
				ssocket.close();
				
				connected = true;				
				connectionEvent();
				
				//2. fall back for tunneling request from db
				while (loop)
					handleLocalRequests();
			} catch (SocketException e){
				if (e.getMessage().equals("Socket closed"))
					log.debug("Server socket has been closed");
				else e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.debug("Finishing loop");
	}
	
	protected void connectionEvent() {
		log.info("Connection with local db established");
	}

	DataInputStream reqInput = null;
	DataOutputStream reqOutput = null;
	public byte[] putRequest(byte[] req) throws GridException{
		
		if (connected == false)
			throw new GridException("Connection with local db not eshtablished");
			
		byte [] msg = null;
		try {
			//inform that this is only request
			reqOutput.writeInt(PeerMessage.REQUEST);
			reqOutput.writeInt(req.length);
			reqOutput.write(req);
			reqOutput.flush();
			
			int msglen = reqInput.readInt();
			msg = new byte[msglen];
			reqInput.read(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return msg;
	}
	
	private boolean connected = false;
	DataInputStream localReqInput = null;
	DataOutputStream localReqOutput = null;
	private void handleLocalRequests(){
		try {			 
			//obtain peer name
			int len = localReqInput.readInt();
			byte [] str = new byte[len];
			localReqInput.read(str);			
			String peerName = new String(str);
			
			//read message length - that will be only single and short messages
			len = localReqInput.readInt();
			
			byte [] req = new byte[len];			
			localReqInput.read(req);
			
			byte[] resp = repo.putRequest(peerName,"admin",  req);
			
			localReqOutput.writeInt(resp.length);
			localReqOutput.write(resp);
			localReqOutput.flush();
		} catch (SocketException e){
			if (e.getMessage().equals("Socket closed"))
				log.debug("Closing socket");				
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GridException e) {
			//handle network error so put back to db -1
			log.error(e.getMessage());
			try {
				localReqOutput.writeInt(-1);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void start(){
		this.setName("lsnr-localdb");
		super.start();
	}
	
	public static LocalDatabase getLocalDatabase(){
		return instance;
	}

	public void shutdown() {
		log.info("Shutdown LocalDatabase instance");
		loop = false;
		try {
			remoteRequestSocket.close();
			localRequestSocket.close();
		} catch (NullPointerException e){
			log.debug("Local connection never opened");
			try {
				serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void putCommand(int msg_type, byte [][] data) throws GridException {
		
		if (connected == false)
			throw new GridException("Connection with local db not eshtablished");
			
		byte [] msg = null;
		try {
			//inform that this is only request
			reqOutput.writeInt(msg_type);
			if (data!=null){
				int retrs = data.length;
				reqOutput.writeInt(retrs);
				for (int i = 0; i< retrs; i++){
					reqOutput.writeInt(data[i].length);
					reqOutput.write(data[i]);	
				}				
				reqOutput.flush();
			} else {
				reqOutput.writeInt(0);
				reqOutput.flush();
			}
			
			int msglen = reqInput.readInt();
			msg = new byte[msglen];
			reqInput.read(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
