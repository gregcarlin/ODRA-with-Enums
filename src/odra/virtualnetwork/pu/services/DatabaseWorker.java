package odra.virtualnetwork.pu.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.jxta.socket.JxtaSocket;
import odra.dbinstance.DBInstance;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.api.IRequestHandler;
import odra.virtualnetwork.api.PeerMessage;
import odra.virtualnetwork.base.LocalDatabase;

import org.apache.log4j.Logger;

/**
 * @author mich
 * This class handles incoming request connection. 
 * W tym samym czasie moze istniec kilka instancji tej klasy, po jednej dla kazdego wezla, ktory 
 * przesyla zlecenie badz zlecenia. 
 */
public class DatabaseWorker extends Thread{
	
	public static Logger log = Logger.getLogger(DatabaseWorker.class);
	
	JxtaSocket socket = null;
	public DatabaseWorker(JxtaSocket socket){
		this.socket = socket;
		this.setName("socket_worker-" + socket.toString());
		log.info("Opening communication channel");
	}
	
	//temp
	IRequestHandler reqHandler = RequestHandlerImpl.getImpl();
	
	public void run(){
		try {
		socket.setSendBufferSize(35000);
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());

		while (true){

			int msglen, userNameLen;
			

			//wait for message			
			userNameLen = input.readInt();
			byte [] userNameArr = new byte[userNameLen];
			input.read(userNameArr);
			
			msglen = input.readInt();
			
			byte [] rawrqst = new byte[msglen];
			
			int toread = rawrqst.length;
			int len;
			while (toread > 0) {
				len = input.read(rawrqst, rawrqst.length - toread, toread);
				if (len == -1)
				break;
				toread -= len;
			}			
			
			byte [] rep = null;
					
			rep =  reqHandler.handleRequest(new String(userNameArr),rawrqst);					
			
			if (rep != null){
				output.writeInt(rep.length);
				output.write(rep);
			} else output.writeInt(0);
			output.flush();
			
			
		}
		}
		catch(IOException e){
			log.info("Closing communication channel");			
		}

	}
	
	public void start(){
		super.start();
	}
}
