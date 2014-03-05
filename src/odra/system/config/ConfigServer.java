package odra.system.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;

import odra.system.log.LogWriter;
import odra.system.log.LoggingLevel;

/**
 * Client configuration.
 * 
 * @author jacenty
 * @version 2007-07-09
 * @since 2007-07-02
 */
public class ConfigServer extends Config
{
    /** log writer */
	protected static LogWriter serverLogWriter;
	//server process options
	/** listener port */
	public static int LSNR_PORT = 1521;
	/** session timeout */
	public static int SESSION_TIMEOUT = 0;
	
	//runtime options
	/** extra flag for controlling printing exception stack traces */ 
	public static boolean DEBUG_EXCEPTIONS = true;
	/** turn it on to enable typechecking */
	public static boolean TYPECHECKING = true; 
	/** whether the transaction support should be enabled */
	public static boolean TRANSACTIONS = false;
	
	/** svrp thread is stopped if there are n subsequent errors */
	public static int SVRP_STOP_ON_ERRORS = 5; 
	/** lsnr thread is stopped if there are n subsequent errors */
	public static int LSNR_STOP_ON_ERRORS = 5;
	/** pmon is stopped if there are n subsequent errors */
	public static int PMON_STOP_ON_ERRORS = 5; 
	/** how long should pmon sleep before analyzing server processes */
	public static int PMON_SLEEP_TIME = 5000;
	/** message decoder buffer length */
	public static int MSG_DECODER_BUFFER = 10000000;
	/** listener buffer read length */
	public static int LSNR_READ_BUFFER = 2 * 1024;
	
	//grid
	/** indicated if grid should be enabled */
	public static boolean JXTA = true;
	
	// web services options
	/** Web Services endpoints user context */
	public static String WS_CONTEXT_USER = "admin"; 
	/**  Web Services endpoint server address */ 
	public static String WS_SERVER_ADDRESS = "localhost";
	/** Web Services endpoint server port */
	public static int WS_SERVER_PORT = 8888;
	
	/** indicates whether stack trace should be included to response when error occur during wsdl generation */
	public static boolean WS_WSDL_DETAILED_ERROR = true; // TODO switch to false before final build
	/** indicated if Web Services endpoints should be served */
	public static boolean WS = true; 
	
	/** indicates whether web service for generic sbql calls should be exposed */
	public static boolean WS_EXPOSE_GENERIC = true;
	/** generic web service options */
	public static String WS_GENERIC_NAME = null;
	public static String WS_GENERIC_PATH = null; 
	public static String WS_GENERIC_SERVICE = null;
	public static String WS_GENERIC_PORTTYPE = null;
	public static String WS_GENERIC_PORT = null;
	public static String WS_GENERIC_NS = null;
	
	/**
	 * transaction related configuration
	 * 
	 * can we make an agreement that each functionality has its own namespace
	 * so that the person responsible for its development do not mix it up with
	 * someone else's ones
	 */
	/** size of heap page (in bytes) */
	public static int TRANSACTIONS_DATA_PAGE_SIZE = 1024;
	/** lock wait timeout (in milis) */
	public static long TRANSACTIONS_LOCK_WAIT_TIMEOUT = 600000;
	
	//relational wrapper options
	/** relational wrapper verbose client? */
	public static boolean WRAPPER_CLIENT_VERBOSE = false;

	//runtime Juliet debug options
	public static boolean DEBUG_ENABLE = false;
	public static boolean DEBUG_INCLUDE_SOURCE = false;
	public static boolean DEBUG_INCLUDE_EXPRESSIONS = false;
	public static boolean DEBUG_INCLUDE_BYTECODE = false;
	// other
	/** plugins registry */
	public static Hashtable<String, String> plugins = new Hashtable<String, String>();
	public static final String filename = "odra-server.properties";
	
	static
	{
		plugins.put("XMLImporter", "odra.filters.XML.XMLImportFilter");
		plugins.put("XSDImporter", "odra.filters.XSD.XSDImportFilter");

		Properties properties = new Properties();
		try
		{
			try
			{
				properties.load(new FileInputStream("./conf/"+filename));
			}
			catch(FileNotFoundException exc)
			{
				try
				{
					//try to get the file from JAR
					InputStream inputStream = ConfigServer.class.getResourceAsStream("/" + filename);
					properties.load(inputStream);
				}
				catch(Exception exc1)
				{
					if(ConfigDebug.DEBUG_EXCEPTIONS)
						exc.printStackTrace();
					
					throw exc;
				}
			}
			serverLogWriter = new LogWriter(
					mapString(properties.getProperty("log.path")), 
					LoggingLevel.getForName(mapString(properties.getProperty("log.level"))));
			String value;
			LSNR_PORT = mapInteger(properties.getProperty("instance.port"));
			SESSION_TIMEOUT = mapInteger(properties.getProperty("timeout.session"));
			
			SVRP_STOP_ON_ERRORS = mapInteger(properties.getProperty("instance.svrp.stop_on_errors"));
			LSNR_STOP_ON_ERRORS = mapInteger(properties.getProperty("instance.lsnr.stop_on_errors"));
			PMON_STOP_ON_ERRORS = mapInteger(properties.getProperty("instance.pmon.stop_on_errors"));
			PMON_SLEEP_TIME = mapInteger(properties.getProperty("instance.pmon.sleep_time"));
			LSNR_READ_BUFFER = mapInteger(properties.getProperty("instance.lsnr.read_buffer"));
			MSG_DECODER_BUFFER = mapInteger(properties.getProperty("instance.msg_decoder_buffer"));
			
			TYPECHECKING = mapBoolean(properties.getProperty("runtime.typechecking"));
			WS = mapBoolean(properties.getProperty("runtime.ws"));
			JXTA = mapBoolean(properties.getProperty("runtime.jxta"));
			TRANSACTIONS = mapBoolean(properties.getProperty("runtime.transactions"));
			
			TRANSACTIONS_DATA_PAGE_SIZE = mapInteger(properties.getProperty("transactions.data_page_size"));
			TRANSACTIONS_LOCK_WAIT_TIMEOUT = mapInteger(properties.getProperty("transactions.lock_wait_time"));
			
			
			WS_CONTEXT_USER = mapString(properties.getProperty("ws.context.username"));
			WS_SERVER_ADDRESS = mapString(properties.getProperty("ws.endpoints.server.address"));
			WS_SERVER_PORT = mapInteger(properties.getProperty("ws.endpoints.server.port"));
			
			WS_EXPOSE_GENERIC = mapBoolean(properties.getProperty("ws.endpoints.generic.enabled"));
			WS_GENERIC_NAME = mapString(properties.getProperty("ws.endpoints.generic.name"));
			WS_GENERIC_PATH = mapString(properties.getProperty("ws.endpoints.generic.path"));
			WS_GENERIC_SERVICE = mapString(properties.getProperty("ws.endpoints.generic.servicename"));
			WS_GENERIC_PORT = mapString(properties.getProperty("ws.endpoints.generic.portname"));
			WS_GENERIC_PORTTYPE = mapString(properties.getProperty("ws.endpoints.generic.porttypename"));
			WS_GENERIC_NS = mapString(properties.getProperty("ws.endpoints.generic.namespace"));
			
			WRAPPER_CLIENT_VERBOSE = mapBoolean(properties.getProperty("wrapper.verbose"));
			
			value = properties.getProperty("debug.enable");
			if(value!= null)
				DEBUG_ENABLE = mapBoolean(value);
			else
				serverLogWriter.getLogger().log(Level.SEVERE, filename +" configuration file does not cointain property '" + "debug.enable" + "' using default value '" + Boolean.toString(DEBUG_ENABLE)+ "'");
			
			value = properties.getProperty("debug.include.source");
			if(value!= null)
				DEBUG_INCLUDE_SOURCE = mapBoolean(value);
			else
				serverLogWriter.getLogger().log(Level.SEVERE, filename +" configuration file does not cointain property '" + "debug.include.source" + "' using default value '" + Boolean.toString(DEBUG_INCLUDE_SOURCE)+ "'");
			
			value = properties.getProperty("debug.include.expressions");
			if(value!= null)
				DEBUG_INCLUDE_EXPRESSIONS = mapBoolean(value);
			else
				serverLogWriter.getLogger().log(Level.SEVERE, filename +" configuration file does not cointain property '" + "debug.include.expressions" + "' using default value '" + Boolean.toString(DEBUG_INCLUDE_EXPRESSIONS)+ "'");
			
			value = properties.getProperty("debug.include.bytecode");
			if(value!= null)
				DEBUG_INCLUDE_BYTECODE = mapBoolean(value);
			else
				serverLogWriter.getLogger().log(Level.SEVERE, filename +" configuration file does not cointain property '" + "debug.include.bytecode" + "' using default value '" + Boolean.toString(DEBUG_INCLUDE_BYTECODE)+ "'");
			
			
		}
		catch (IOException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new RuntimeException("Problem with accessing reading server configuration file: " + exc.getMessage());
		}
	}
	
	/**
	 * Returns the log writer.
	 * 
	 * @return {@link LogWriter}
	 */
	public static LogWriter getLogWriter()
	{
		return serverLogWriter;
	}
}
