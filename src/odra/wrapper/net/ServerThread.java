package odra.wrapper.net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import odra.wrapper.Wrapper;
import odra.wrapper.WrapperException;
import odra.wrapper.config.SwardConfig;
import odra.wrapper.config.TorqueConfig;
import odra.wrapper.generator.XMLGenerator;
import odra.wrapper.model.Database;
import odra.wrapper.net.Message.Core;
import odra.wrapper.sql.Query;
import odra.wrapper.sql.Type;
import odra.wrapper.sql.result.Result;
import odra.wrapper.type.SbqlType;
import swardAPI.Sward;
import swardAPI.SwardScan;


/**
 * Listening server thread. 
 * @author jacenty
 * @version   2008-01-28
 * @since   2006-09-10
 */
public class ServerThread extends Thread
{
	/** random thread id (for debugging) */
  private final int id = new java.util.Random().nextInt(Integer.MAX_VALUE);
  /** thread name with random id */
  private final String threadName = "SBQL wrapper server thread #" + id;
  /** socket */
  private Socket socket = null;
  /** output writer */
  private PrintWriter out;
  /** input reader */
  private BufferedReader in;
  /** datagram socket (for data transfer) */
  private DatagramSocket dataSocket;
  /** database connection config data */
  private final TorqueConfig torqueConfig;
  /** SWARD connection config data */
  private final SwardConfig swardConfig;
  /** database */
  private final Database database;
  /** verbose? */
  private final boolean verbose;

  /**
   * The constructor for the relational mode.
   * 
   * @param socket socket
   * @param config connection config
   * @param database database
   * @param verbose verbose?
   */
  ServerThread(Socket socket, TorqueConfig config, Database database, boolean verbose)
  {
    super("SBQL wrapper server thread");
    this.socket = socket;
    this.torqueConfig = config;
    this.swardConfig = null;
    this.database = database;
    this.verbose = verbose;

    init();
  }
  
  /**
   * The constructor for the relational mode.
   * 
   * @param socket socket
   * @param config connection config
   * @param database database
   * @param verbose verbose?
   */
  ServerThread(Socket socket, SwardConfig config, Database database, boolean verbose)
  {
    super("SBQL wrapper server thread");
    this.socket = socket;
    this.torqueConfig = null;
    this.swardConfig = config;
    this.database = database;
    this.verbose = verbose;

    init();
  }
  
  /**
   * Initializes sockets.
   *
   */
  private void init()
  {
  	output(threadName + " listening...");
    output(threadName + " connected from " + socket.getInetAddress());

    try
    {
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
  
  @Override
  public void run()
  {
  	int mode = Wrapper.MODE_SQL;
    try
    {
      String fromClient = "";
      Object object = null;
      byte[] objectData = null;
      while((fromClient = in.readLine()) != null)
      {
        fromClient = Message.decode(fromClient);
        output("SBQL wrapper server thread #" + id + " <- " + fromClient);

        String[] msg = Message.splitMessage(fromClient);
        try
        {
	        Message message = new Message(Message.Core.getMessageForString(msg[0]), msg[1]);
	        Core core = message.getCore();
	        String parameter = message.getParameter();
	        
	        if(core.equals(Message.Core.BYE))
	        {
	        	new Message(Message.Core.BYE).send(out, threadName, verbose);
	          break;
	        }
	        else if(core.equals(Message.Core.HELLO))
	        {
	        	new Message(Message.Core.HELLO).send(out, threadName, verbose);
	        	Message.sendMiscellaneousMessage(out, threadName + "@" + socket.getLocalAddress().getHostAddress(), threadName, verbose);
	        	Message.sendMiscellaneousMessage(out, "SBQL wrapper server is running under Java Service Wrapper", threadName, verbose);
	        	Message.sendMiscellaneousMessage(out, "Big thanks to Tanuki Software <http://wrapper.tanukisoftware.org>", threadName, verbose);
	          
	        	new Message(Message.Core.REQUEST_IDENTITY).send(out, threadName, verbose);
	        }
	        else if(core.equals(Message.Core.IDENTITY))
	        {
	        	if(acceptIdentity(parameter))
	        	{
		        	Message.sendMiscellaneousMessage(out, "welcome " + parameter + "@" + socket.getInetAddress().getHostAddress(), threadName, verbose);
		        	new Message(Message.Core.REQUEST_MODE).send(out, threadName, verbose);
	        	}
	        	else
	        	{
	        		new Message(Message.Core.REJECT, "Your identity ('" + parameter + "') was rejected.").send(out, threadName, verbose);
	        		new Message(Message.Core.BYE).send(out, threadName, verbose);
	            break;
	        	}
	        }
	        else if(core.equals(Message.Core.MODE))
	        {
	        	mode = Integer.parseInt(parameter);
	        	new Message(Message.Core.READY).send(out, threadName, verbose);
	        }
	        else if(core.equals(Message.Core.GET_TRANSFER_PORT))
	        {
	          dataSocket = new DatagramSocket();
	          new Message(Message.Core.TRANSFER_PORT, Integer.toString(dataSocket.getLocalPort())).send(out, threadName, verbose);
	        }
	        else if(core.equals(Message.Core.GET_DATA_LENGTH))
	        {
	          objectData = objectToBytes(object);
	          new Message(Message.Core.DATA_LENGTH, Integer.toString(objectData.length)).send(out, threadName, verbose);
	        }
	        else if(core.equals(Message.Core.QUERY))
	        {
	          Message.sendMiscellaneousMessage(out, "Processing the query", threadName, verbose);
	          try
	          {
	          	object = executeQuery(mode, message.getParameter());
	          	if(object instanceof Result)//selects
	          	{
	          		Result result = (Result)object;
	          		object = new XMLGenerator(result).getXMLString();
	          		new Message(Message.Core.DATA_READY, result.size() + " row(s).").send(out, threadName, verbose);
	          	}
	          	else//updates
	          	{
	          		new Message(Message.Core.DATA_READY, object + " row(s) affected by update.").send(out, threadName, verbose);
	          	}
	          }
	          catch(WrapperException exc)
	          {
	          	exc.printStackTrace();
	          	
	          	String errorCause = exc.getError().toString();
	          	if(exc.getCause() != null)
	          		errorCause += ": " + exc.getCause().getLocalizedMessage();
	          	else
	          		errorCause += ": unknown cause";
	          	new Message(Message.Core.ERROR, errorCause).send(out, threadName, verbose);
	        		new Message(Message.Core.BYE).send(out, threadName, verbose);
	          }
	        }
	        else if(core.equals(Message.Core.SEND_DATABASE))
	        {
          	object = database;
          	new Message(Message.Core.DATA_READY, ((Database)object).getTables().size() + " table(s).").send(out, threadName, verbose);
	        }
	        else if(core.equals(Message.Core.SEND_METABASE))
	        {
          	object = database.createMetabaseXSD();
          	new Message(Message.Core.DATA_READY, "XSD preapred").send(out, threadName, verbose);
	        }
	        else if(core.equals(Message.Core.SEND_DATA))
	        {
	        	new Message(Message.Core.SENDING_DATA).send(out, threadName, verbose);
	        	sendObject(objectData);
	        }
	        else if(core.equals(Message.Core.DATA_RECEIVED))
	        	new Message(Message.Core.WANT_ANOTHER).send(out, threadName, verbose);
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
    catch(IOException exc)
    {
      exc.printStackTrace();
    }
  }
  
  /**
   * Accepts or rejects client's identity.
   * <br />
   * Temporarily the method always returns <code>true</code>, for further development with some trust mechanisms.
   * 
   * @param identity identity strong
   * @return accepted?
   */
  private boolean acceptIdentity(String identity)
  {
  	//TODO realize client verification
  	return identity != null;
  }

  /**
   * Converts an object to a corresponding byte array.
   * 
   * @param object object
   * @return object data
   * @throws IOException 
   */
  private byte[] objectToBytes(Object object) throws IOException
  {
  	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream outStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));
    outStream.flush();
    outStream.writeObject(object);
    outStream.flush();
    
    byte[] bytes = byteStream.toByteArray();
    outStream.close();
    
    return bytes;
  }
  
  /**
   * Sends an object as a bytestream.
   * 
   * @param bytes object bytes
   * @return sent?
   */
  private boolean sendObject(byte[] bytes)
  {
    if(dataSocket == null)
      return false;

    try
    {
      DatagramPacket sendPacket;
      DatagramPacket receivePacket;

      int packetLength = Server.PACKET_LENGTH;
      int clientPort;
      InetAddress clientAddress;

      byte[] buffer = new byte[2];//go
      receivePacket = new DatagramPacket(buffer, buffer.length);

      dataSocket.receive(receivePacket);
      if(new String(receivePacket.getData()).equalsIgnoreCase("go"))
      {
        clientAddress = receivePacket.getAddress();
        clientPort = receivePacket.getPort();

        buffer = new byte[4];//next
        receivePacket = new DatagramPacket(buffer, buffer.length);

        int sent = 0;
        int bufferLength = packetLength;

        byte[] sendBuffer = bytes;

        int length = sendBuffer.length;
        byte[] objectBytes;
        while(sent < length)
        {
          if(length - sent >= packetLength)
            packetLength = bufferLength;
          else
            packetLength = length - sent;
          objectBytes = new byte[packetLength];

          for(int i = sent; i < sent + packetLength; i++)
            objectBytes[i - sent] = sendBuffer[i];

          sendPacket = new DatagramPacket(objectBytes, 0, packetLength, clientAddress, clientPort);
          dataSocket.send(sendPacket);

          sent += packetLength;
          dataSocket.receive(receivePacket);
        }

        return true;
      }
    }
    catch(IOException e)
    {
      e.printStackTrace();
      dataSocket.close();
      return false;
    }

    return false;
  }

  public void finalize() throws Throwable
  {
    super.finalize();
    socket.close();
  }
  
  /**
   * Processes SQL query.
   * 
   * @param mode wrapper mode
   * @param queryString query
   * @return query result
   * @throws WrapperException
   */
  private Object executeQuery(int mode, String queryString) throws WrapperException
  {
  	Statement statement = null;
		Sward sward = null;
		if(torqueConfig != null)
		{
			try
			{
				Class.forName(torqueConfig.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_DRIVER));
			}
			catch(ClassNotFoundException exc)
			{
				throw new WrapperException("JDBC driver class ('" + torqueConfig.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_DRIVER) + "') not found", exc, WrapperException.Error.DRIVER_CLASS_NOT_FOUND);
			}

			try
			{
				Connection connection = DriverManager.getConnection(
					torqueConfig.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_URL), 
					torqueConfig.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_USER), 
					torqueConfig.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_PASSWORD));
				statement = connection.createStatement();
			}
			catch(SQLException exc)
			{
				throw new WrapperException("Connecting to database", exc, WrapperException.Error.SQL_CONNECTION);
			}
		}
		else if(swardConfig != null)
		{
			sward = new Sward();
			sward.connect(
				swardConfig.getProperty(SwardConfig.SWARD_XXX_UPV_NAME), 
				swardConfig.getProperty(SwardConfig.SWARD_XXX_CONNECTION_USER), 
				swardConfig.getProperty(SwardConfig.SWARD_XXX_CONNECTION_PASSWORD));
		}
		else throw new WrapperException("The wrapper server is not initialized correctly!");
		
		try
		{
			Query query = new Query(queryString, database);
			String modeQuery = query.getQueryStringForMode(mode);
			
			if(query.getType() == Type.SELECT)
			{
				if(mode == Wrapper.MODE_SQL || mode == Wrapper.MODE_SD)
				{
					ResultSet rs = statement.executeQuery(modeQuery);
					if(!query.isAggregate())
					{
						Result result = new Result(query, rs);
						statement.getConnection().close();
						
						return result;
					}
					else
					{
						if(rs.next())
						{
							String resultType = rs.getMetaData().getColumnTypeName(1);
							if(SbqlType.getSbqlType(resultType).equals(SbqlType.INTEGER))
							{
								int result = rs.getInt(1);
								statement.getConnection().close();
								
								return new Integer(result);
							}
							else
							{
								double result = rs.getDouble(1);
								statement.getConnection().close();
								
								return new Double(result);
							}
						}
						else
							return null;
					}
				}
				else if(mode == Wrapper.MODE_SWARD)
				{
					SwardScan res =  sward.query(modeQuery);
					Result result = new Result(query, res);

					sward.disconnect(swardConfig.getProperty(SwardConfig.SWARD_XXX_UPV_NAME));
					
					return result;
				}
				else
					return null;
			}
			else
			{
				if(mode == Wrapper.MODE_SQL || mode == Wrapper.MODE_SD)
				{
					int rows = statement.executeUpdate(modeQuery);
					return new Integer(rows);
				}
				else
					throw new WrapperException("Imperative statements are not implemented in SWARD!");
			}
		}
		catch(SQLException exc)
		{
			throw new WrapperException("Query execution", exc, WrapperException.Error.SQL_QUERY_EXECUTION);
		}
  }
  
  /**
   * Outputs the message.
   * 
   * @param msg message
   */
  private void output(String msg)
  {
  	if(verbose)
  		System.out.println(msg);
  }
}
