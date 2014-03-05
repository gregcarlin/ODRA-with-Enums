package odra.wrapper.net;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import odra.system.config.ConfigDebug;
import odra.util.Worker;
import odra.wrapper.WrapperException;
import odra.wrapper.config.SwardConfig;
import odra.wrapper.config.TorqueConfig;
import odra.wrapper.model.Database;
import odra.wrapper.model.SwardDatabase;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Listening server. 
 * @author jacenty
 * @version   2007-06-25
 * @since   2006-09-10
 */
public class Server
{
	/** server default listener port */
	public final static int WRAPPER_SERVER_PORT = 2000;
	/** verbose server? */
	public final static boolean WRAPPER_SERVER_VERBOSE = true;
	/** expand query with row IDs? */
	public final static boolean WRAPPER_EXPAND_WITH_IDS = false;
	/** expand query with row foreign key IDs? */
	public final static boolean WRAPPER_EXPAND_WITH_REFS = false;
	
	private static final String configPathParam = "-C";
	private static final String dbNameParam = "-D";
	private static final String portParam = "-P";
	private static final String verboseParam = "-V";
	private static final String modeParam = "-M";

	/** SWARD mode parameter string */
	private static final String SWARD = "sward";
	
	/** worker */
	private Worker worker;
	/** listening socket */
  private ServerSocket serverSocket = null;
  /** is listening? */
  private boolean listening = true;
  /** Torque config */
  private final TorqueConfig torqueConfig;
  /** SWARD config */
  private final SwardConfig swardConfig;
  /** database */
  private final Database database;
  /** port */
  private final int port;
  /** verbose? */
  private static boolean verbose = true;
  
  /** data packet length */
  public static final int PACKET_LENGTH = 1024;

  /**
   * The constructor for relational mode.
   * 
   * @param config connection configuration
   * @param database database
   * @param port port
   * @param verbose verbose?
   */
  public Server(TorqueConfig config, Database database, int port, boolean verbose)
  {
  	this.torqueConfig = config;
  	this.swardConfig = null;
  	this.database = database;
  	this.port = port;
  	Server.verbose = verbose;
  	
    output("SBQL wrapper listener started in JDBC mode on port " + port + "...");
    output("SBQL wrapper listener is running under Java Service Wrapper");
    output("Big thanks to Tanuki Software <http://wrapper.tanukisoftware.org>");
  }

  /**
   * The constructor for SWARD mode.
   * 
   * @param config connection configuration
   * @param database database
   * @param port port
   * @param verbose verbose?
   */
  private Server(SwardConfig config, Database database, int port, boolean verbose)
  {
  	this.torqueConfig = null;
  	this.swardConfig = config;
  	this.database = database;
  	this.port = port;
  	Server.verbose = verbose;
  	
    output("SBQL wrapper listener started in SWARD mode on port " + port + "...");
    output("SBQL wrapper listener is running under Java Service Wrapper");
    output("Big thanks to Tanuki Software <http://wrapper.tanukisoftware.org>");
  }
  
  public void go()
  {
    worker = new Worker()
    {
      @Override
			public Object construct()
      {
        return new ActualTask();
      }
    };
    worker.start();
  }

  class ActualTask
  {
    ActualTask()
    {
      listen();
    }
  }

  /**
   * Listens to incoming connections.
   */
  void listen()
  {
    try
    {
      serverSocket = new ServerSocket(port);
    }
    catch(BindException exc)
    {
      output("Service already started (or port " + port + " is in use)...");
      try
      {
        Thread.sleep(3000);
      }
      catch(InterruptedException exc1) {}
      System.exit(-1);
    }
    catch(IOException exc)
    {
      exc.printStackTrace();
    }

    while(listening)
    {
    	if(torqueConfig != null)
    	{
	      try
	      {
	        new ServerThread(serverSocket.accept(), torqueConfig, database, verbose).start();
	      }
	      catch(IOException exc)
	      {
	        exc.printStackTrace();
	      }
    	}
    	else if(swardConfig != null)
    	{
	      try
	      {
	        new ServerThread(serverSocket.accept(), swardConfig, database, verbose).start();
	      }
	      catch(IOException exc)
	      {
	        exc.printStackTrace();
	      }
    	}
    	else
    		throw new RuntimeException("The wrapper server is not correctly initialized!");
    }

    try
    {
      serverSocket.close();
    }
    catch(IOException exc)
    {
      exc.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
  	String dbName = null;
  	String configPath = "./conf/";
  	int port = Server.WRAPPER_SERVER_PORT;
  	boolean verbose = Server.WRAPPER_SERVER_VERBOSE;
  	boolean swardMode = false;
  	
  	for(String param : args)
  	{
  		if(param.startsWith(configPathParam))
  			configPath = param.replaceFirst(configPathParam, "");
  		else if(param.startsWith(dbNameParam))
  			dbName = param.replaceFirst(dbNameParam, "");
  		else if(param.startsWith(portParam))
  			port = Integer.parseInt(param.replaceFirst(portParam, ""));
  		else if(param.startsWith(verboseParam))
  			verbose = Boolean.parseBoolean(param.replaceFirst(verboseParam, ""));
  		else if(param.startsWith(modeParam))
  			swardMode = param.replaceFirst(modeParam, "").equalsIgnoreCase(SWARD);
  	}
  	
  	if(!swardMode)
  	{
    	String configFilePath = configPath + TorqueConfig.CONFIG_FILE_NAME;
	  	TorqueConfig config = null;
	  	try
	  	{
	  		config = new TorqueConfig(dbName, configFilePath);
	  	}
	  	catch(ConfigurationException exc)
	  	{
	  		output("Config file error!!!");
	  		if(ConfigDebug.DEBUG_EXCEPTIONS)
	  			exc.printStackTrace();
	  		System.exit(1);
	  	}
	  	
	  	if(dbName == null)
	  		dbName = config.getProperty(TorqueConfig.TORQUE_DATABASE_DEFAULT);
	  	
	  	try
	  	{
		  	Database database = new Database(configPath + dbName + "-schema.generated.xml");
		  	output("Database model successfully build from schema in '" + configPath + dbName + "-schema.generated.xml" + "'");
				
		  	new Server(config, database, port, verbose).go();
	  	}
	  	catch(WrapperException exc)
	  	{
	  		output("Database model error!!!");
	
	  		if(ConfigDebug.DEBUG_EXCEPTIONS)
	  			exc.printStackTrace();
	  		System.exit(1);
	  	}
  	}
  	else
  	{
  		String configFilePath = configPath + SwardConfig.CONFIG_FILE_NAME;
	  	SwardConfig config = null;
	  	try
	  	{
	  		config = new SwardConfig(dbName, configFilePath);
	  	}
	  	catch(ConfigurationException exc)
	  	{
	  		output("Config file error!!!");
	  		if(ConfigDebug.DEBUG_EXCEPTIONS)
	  			exc.printStackTrace();
	  		System.exit(1);
	  	}
	  	
	  	if(dbName == null)
	  		dbName = config.getProperty(SwardConfig.SWARD_INSTANCE_DEFAULT);

	  	try
	  	{
		  	SwardDatabase database = new SwardDatabase(configFilePath, dbName);
		  	output("Database model successfully build from schema in '" + configFilePath + "'");
				
		  	new Server(config, database, port, verbose).go();
	  	}
	  	catch(ConfigurationException exc)
	  	{
	  		output("SWARD model error!!!");
	
	  		if(ConfigDebug.DEBUG_EXCEPTIONS)
	  			exc.printStackTrace();
	  		System.exit(1);
	  	}
  	}
  }

  @Override
  public void finalize() throws Throwable
  {
    super.finalize();
    serverSocket.close();
  }

  /**
   * Outputs the message.
   * 
   * @param msg message
   */
  private static void output(String msg)
  {
  	if(verbose)
  		System.out.println(msg);
  }
}
