package odra.dbinstance.processes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import odra.dbinstance.DBInstance;
import odra.system.config.ConfigServer;

/**
 * The class is responsible for dealing with physical network communication.
 * All client connections are served by a single loop built around
 * the concept of asynchronous socket communication. The loop is responsible
 * for spawning new server processes, passing to them requestes received from clients, etc.
 * 
 * @author raist
 */

// TODO: server does not close connections

public class ListenerProcess extends Thread {
	private boolean loop; // indicates whether the thread should keep running or not

	private int port; // port on which the process listens for new connections
	private DBInstance instance;

	private Selector selector;
	
	public ListenerProcess(DBInstance instance, int port) {
		super("lsnr");		
		
		this.instance = instance;
		this.port = port;		
	}

	public void run() {
		loop = true;
		int errCnt = 0;
		
		ServerSocketChannel serverChannel = null;
		ServerSocket serverSocket = null;

		try {
			// configure the socket server
			selector = Selector.open();

			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);

			serverSocket = serverChannel.socket(); 
			serverSocket.bind(new InetSocketAddress(port));

			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (IOException ex) {
			instance.lsnrFailed();
		}
		ByteBuffer tmpbuf = ByteBuffer.allocate(ConfigServer.LSNR_READ_BUFFER);
		// the main loop responsible for handling communication related events
		int n = 0;
		while(loop) {
			// find the events
			try {
				n = selector.select();
			}
			catch (IOException e) {
				ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during LSNR execution", e);

				if (errCnt++ > ConfigServer.LSNR_STOP_ON_ERRORS)
					shutdown();
			}

			// no events no fun
			if (n == 0) 
				continue;
			
			// serve events from chananels
			Iterator it = selector.selectedKeys().iterator();
			while (it.hasNext()) { 
				SelectionKey key = (SelectionKey) it.next();
				it.remove();

				if(!key.isValid())
					continue;
				
				try {
					// when an incoming connection arrives, it is accepted and a new
					// server process is created. from now on, the selector also
					// reacts on write and read operations.
					if (key.isAcceptable()) {						
						ServerSocketChannel server = (ServerSocketChannel) key.channel(); 
						SocketChannel channel = server.accept(); 
						channel.configureBlocking(false);
						
						ServerProcess usrprc = instance.createServerProcess(channel);
						usrprc.start();

						channel.register(selector, SelectionKey.OP_READ, usrprc);
					}

					// incoming data should be read and passed to the correct server process
					if (key.isReadable()) {						
						ServerProcess usrprc = (ServerProcess) key.attachment();
						SocketChannel channel = (SocketChannel) key.channel();

						
					
						int nbytes = 0;
						while (true) {
							nbytes = channel.read(tmpbuf);
							
							if (nbytes < 0)
								instance.shutdownServerProcess(usrprc);
		
							if (nbytes < 1)
								break;

							tmpbuf.flip();
							usrprc.messageReceived(tmpbuf);
						 
							tmpbuf.clear();
						}
					}		
				}
				catch(Exception e) { 
					key.cancel(); 
					ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during LSNR execution", e);
					
					if (errCnt++ > ConfigServer.LSNR_STOP_ON_ERRORS) {
						instance.lsnrFailed();						
						shutdown();
					}
				}
			}
		}

		try {
			selector.close();
			serverSocket.close();
			serverChannel.close();
		}
		catch (IOException ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.WARNING, "Exception during LSNR closing", ex);
		}
	}

	// should be called when the listener process is to be stopped
	public void shutdown() {
		loop = false; 

		if (selector != null)
			selector.wakeup();
	}	
}
