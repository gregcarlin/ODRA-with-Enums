package odra.wrapper.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import odra.system.config.ConfigServer;
import odra.wrapper.WrapperException;
import odra.wrapper.WrapperException.Error;
import odra.wrapper.net.Message.Core;


/**
 * Client. 
 * @author jacenty
 * @version   2008-01-28
 * @since   2006-09-11
 */
public class Client implements Runnable
{
	/** server host */
  private final String host;
  
	/** talk socket */
	private Socket socket = null;
	/** talk port */
  private final int talkPort;

  /** data transfer socket */
  private DatagramSocket dataSocket;
  /** data transfer socket */
  private int transferPort;
  
  /** output writer */
  private PrintWriter out = null;
  /** input reader */
  private BufferedReader in = null;
  
  /** client identity string */
  private final String identity;
  
  /** wrapper mode */
  private final int mode;
  
  /** communication target */
  private Target target;
  /** communication message */
  private String targetMessage;
  
  /** object to receive */
  private Object object;
  
  /** error cause (-1: no error occured) */
  private int errorCode = -1;
  /** error description (can be empty) */
  private String errorDescription = "";
  
  /** client thread */
  private Thread thread = new Thread();
  
  private boolean wait = true;
  
  public enum Target
  {
  	COMMUNICATION_TEST,
  	DATABASE,
  	METABASE,
  	QUERY
  }

  /**
   * Constructor.
   * 
   * @param mode wrapper mode
   * @param identity client identity string
   * @param host server host
   * @param talkPort server talk port
   */
  public Client(int mode, String identity, String host, int talkPort)
  {
  	this.mode = mode;
    this.identity = identity;
    this.host = host;
    this.talkPort = talkPort;
    
    output("SBQL wrapper client started...");
  }

  /**
   * Starts a communication with the server.
   * 
   * @param target taret
   * @param targetMessage message, can be <code>null</code> if target does not require it
   * @throws WrapperException 
   */
  public void go(Target target, String targetMessage) throws WrapperException
  {
      
    try
    {
    	thread.join();
    }
    catch(InterruptedException exc)
    {
    	if(ConfigServer.DEBUG_EXCEPTIONS)
    		exc.printStackTrace();
    }
    
  	if(target.equals(Target.QUERY) && targetMessage == null)
  	{
  		close();
  		throw new WrapperException("Invalid client startup params.", WrapperException.Error.CLIENT_PARAMS);
  	}
  		
  	this.target = target;
  	this.targetMessage = targetMessage;

    connect();
    thread = new Thread(this, "wrapper_client");
    this.wait = true;
    thread.start();
  }
  
	/**
   * Connects to the server.
   * 
   * @throws WrapperException
   */
  private synchronized void connect() throws WrapperException
  {
    output("Connecting the server...");
    try
    {
      socket = establishConnection(talkPort, 1000, 10);
      if(socket == null)
        throw new ConnectException("Connection cannot be established...");
      
      output("Connection established...");

      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch(UnknownHostException exc)
    {
      close();
    	throw new WrapperException("Unknown host '" + host + "'", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
    catch(ConnectException exc)
    {
      close();
    	throw new WrapperException("Cannot connect host '" + host + "' on " + talkPort + " port", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
    catch(IOException exc)
    {
    	close();
    	throw new WrapperException("IO error", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
  }

  public synchronized void run()
  {
    if(socket == null || out == null || in == null)
      return;

    boolean sentBye = false;

    new Message(Message.Core.HELLO).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);

  	object = null;
    String fromServer = "";
    int dataLength = 0;
    errorCode = -1;
    errorDescription = "";
    try
    {
      while((fromServer = in.readLine()) != null)
      {
      	fromServer = Message.decode(fromServer);
      	output(identity + " <- " + fromServer);
      	
      	String[] msg = Message.splitMessage(fromServer);
      	try
      	{
	        Message message = new Message(Message.Core.getMessageForString(msg[0]), msg[1]);
	        Core core = message.getCore();
	        String parameter = message.getParameter();
	
	        if(core.equals(Message.Core.BYE) || core.equals(Message.Core.CLOSE))
	        {
	          if(!sentBye)
	          	new Message(Message.Core.BYE).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	          break;
	        }
	        else if(core.equals(Message.Core.ERROR))
	        {
	        	String[] paramSplit = Message.splitMessage(parameter);
	        	errorCode = Integer.parseInt(paramSplit[0]);
	        	if(paramSplit.length == 2)
	        		errorDescription = paramSplit[1];
	        	
	        	break;
	        }
	        else if(core.equals(Message.Core.REQUEST_IDENTITY))
	        	new Message(Message.Core.IDENTITY, identity).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        else if(core.equals(Message.Core.REQUEST_MODE))
	        	new Message(Message.Core.MODE, Integer.toString(mode)).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        else if(core.equals(Message.Core.READY))
	        {
	        	if(target.equals(Target.COMMUNICATION_TEST))
	        	{
	        		Message.sendMiscellaneousMessage(out, "it's only a communication test...", identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        		new Message(Message.Core.BYE).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
		        	sentBye = true;
	        	}
	        	else if(target.equals(Target.QUERY))
	        		new Message(Message.Core.QUERY, targetMessage).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        	else if(target.equals(Target.DATABASE))
	        		new Message(Message.Core.SEND_DATABASE).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        	else if(target.equals(Target.METABASE))
	        		new Message(Message.Core.SEND_METABASE).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        }
	        else if(core.equals(Message.Core.DATA_READY))
	        	new Message(Message.Core.GET_TRANSFER_PORT).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        else if(core.equals(Message.Core.TRANSFER_PORT))
	        {
	        	transferPort = Integer.parseInt(parameter);
	        	new Message(Message.Core.GET_DATA_LENGTH).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        }
	        else if(core.equals(Message.Core.DATA_LENGTH))
	        {
	        	dataLength = Integer.parseInt(parameter);
	        	new Message(Message.Core.SEND_DATA).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        }
	        else if(core.equals(Message.Core.SENDING_DATA))
        	{
	        	try
	        	{
		        	object = receiveObject(dataLength);
		          new Message(Message.Core.DATA_RECEIVED).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        	}
	        	catch(WrapperException exc)
	        	{
	        		if(ConfigServer.DEBUG_EXCEPTIONS)
	        			exc.printStackTrace();
	        		new Message(Message.Core.ERROR).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        	}
        	}
	        else if(core.equals(Message.Core.WANT_ANOTHER))
	        {
	        	new Message(Message.Core.BYE).send(out, identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
	        	sentBye = true;
	        }
      	}
      	catch(AssertionError err)
      	{
      		//miscellaneous messages are not interpreted
      	}
      }

      out.close();
      in.close();
      socket.close();
      if(dataSocket != null)
        dataSocket.close();
    }
    catch(SocketException exc)
    {
    	if(ConfigServer.DEBUG_EXCEPTIONS)
    		exc.printStackTrace();
      output("Connection with '" + host + "' on port " + talkPort + " lost...");
    }
    catch(IOException exc)
    {
    	if(ConfigServer.DEBUG_EXCEPTIONS)
    		exc.printStackTrace();
      output("IO error");
    }
    
    close();
    
    
    notify();
    this.wait = false;
   
  }
  
  /**
   * Establishes a connection with the server.
   * 
   * @param port port
   * @param delay delay between subsequent trials
   * @param trials number of trials
   * @return <code>Socket</code>
   * @throws IOException
   */
  private Socket establishConnection(int port, long delay, int trials) throws IOException
  {
    Socket socket = null;
    for(int i = 1; i <= trials; i++)
    {
    	output("Connecting '" + host + "' on port " + port + " (" + i + " of " + trials + ")...");
      try
      {
        socket = new Socket(host, port);
        break;
      }
      catch(SocketException exc)
      {
        try
        {
          Thread.sleep(delay);
        }
        catch(InterruptedException exc1) {}
      }
    }
    if(socket == null)
      throw new ConnectException("Connection cannot be established...");

    return socket;
  }

  /**
   * Receives an object.
   * 
   * @param dataLength object data length
   * @return object
   * @throws WrapperException
   */
  private synchronized Object receiveObject(int dataLength) throws WrapperException
  {
    try
    {
      long startTime = System.currentTimeMillis();

      String go = "go";
      String next = "next";

      DatagramPacket receivePacket;

      int packetLength = Server.PACKET_LENGTH;
      int bufferLength = packetLength;
      int received = 0;
      byte[] buffer;
      byte[] objectData = new byte[dataLength];

    	dataSocket = new DatagramSocket();
      DatagramPacket sendPacket = new DatagramPacket(go.getBytes(), go.length(), InetAddress.getByName(host), transferPort);
      dataSocket.send(sendPacket);
      while(received < dataLength)
      {
        if(dataLength - received >= packetLength)
          packetLength = bufferLength;
        else
          packetLength = dataLength - received;

        buffer = new byte[packetLength];
        receivePacket = new DatagramPacket(buffer, buffer.length);
        dataSocket.receive(receivePacket);

        System.arraycopy(buffer, 0, objectData, received, buffer.length);

        received += packetLength;

        sendPacket = new DatagramPacket(next.getBytes(), next.length(), InetAddress.getByName(host), transferPort);
        dataSocket.send(sendPacket);
      }

      long timeDiff = System.currentTimeMillis() - startTime;

      Message.sendMiscellaneousMessage(out, "transfer finished in " + timeDiff + " ms", identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
      Message.sendMiscellaneousMessage(out, "transfer rate " + getTransferDescription(objectData.length, timeDiff), identity, ConfigServer.WRAPPER_CLIENT_VERBOSE);
      
      ByteArrayInputStream byteStream = new ByteArrayInputStream(objectData);
      ObjectInputStream inStream = new ObjectInputStream(new BufferedInputStream(byteStream));
      Object object = inStream.readObject();
      inStream.close();

      notify();
      return object;
    }
    catch(ConnectException exc)
    {
    	close();
    	throw new WrapperException("Cannot connect to '" + host + "' on port " + transferPort + ".", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
    catch(SocketException exc)
    {
    	close();
    	throw new WrapperException("Lost connection with '" + host + "' on port " + transferPort + ".", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
    catch(IOException exc)
    {
    	close();
    	throw new WrapperException("IO error.", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
    catch(ClassNotFoundException exc)
    {
    	close();
    	throw new WrapperException("Object transfer error.", exc, WrapperException.Error.CLIENT_SERVER_CONNECTION);
    }
  }

  /**
   * Returns a transfer decription string.
   * 
   * @param dataLength data length
   * @param timeDiff transfer time [ms]
   * @return transfer
   */
  private String getTransferDescription(int dataLength, long timeDiff)
  {
    try
    {
      return (long)dataLength * 1000 / timeDiff / 1024 + " kB/s";
    }
    catch(ArithmeticException exc)
    {
      return Character.toString('\u221E');
    }
  }

  /**
   * Closes the talk socket.
   */
  private void close()
  {
    try
    {
      socket.close();
    }
    catch(NullPointerException exc) {}
    catch(IOException exc)
    {
    	if(ConfigServer.DEBUG_EXCEPTIONS)
    		exc.printStackTrace();
    }
  }

  @Override
  public void finalize() throws Throwable
  {
    super.finalize();
    close();
  }
  
  /**
   * Returns an object received from the server.
   * 
   * @return object
   * @throws WrapperException
   */
  public synchronized Object getReceivedObject() throws WrapperException
  {
  	
      try
  	{	if(wait)
  			wait();
  		wait = true;
  	}
  	catch(InterruptedException exc)
  	{
  		if(ConfigServer.DEBUG_EXCEPTIONS)
  			exc.printStackTrace();
  	}
  	
  	if(errorCode >= 0)
  		throw new WrapperException(errorDescription, Error.getForCode(errorCode));
  	
  	if(object == null)
  		throw new WrapperException("No object received: the target (" + target + ") might not cause object retrieval or some error has occured).");
  	
    return object;
  }
  
  /**
   * Outputs the message.
   * 
   * @param msg message
   */
  private void output(String msg)
  {
  	if(ConfigServer.WRAPPER_CLIENT_VERBOSE)
  		System.out.println(msg);
  }
}