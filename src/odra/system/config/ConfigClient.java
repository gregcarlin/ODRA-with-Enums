package odra.system.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import odra.system.log.LogWriter;
import odra.system.log.LoggingLevel;

/**
 * Client configuration.
 * 
 * @author jacenty
 * @version 2007-07-09
 * @since 2007-07-02
 */
public class ConfigClient extends Config
{
	
    /** log writer */
	protected static LogWriter clientLogWriter;
    // client options
	/** external text editor */
	public static String TEXT_EDITOR = null;
	/** source code path */
	public static String SOURCE_CODE_PATH = "/tmp/";

	// timeouts
	/** server connection timeout for client */
	public static int CONNECT_TIMEOUT;
	/** server hello message timeout for client */
	public static int HELLO_TIMEOUT;
	/** normal operation timeout for client (O is infinity, i.e. no timeout) */
	public static int NORMAL_TIMEOUT;
	
	//auto-connection parameters
	/** connect on startup? */
	public static boolean CONNECT_AUTO;
	/** connect host */
	public static String CONNECT_HOST;
	/** connect port */
	public static int CONNECT_PORT;
	/** connect user */
	public static String CONNECT_USER;
	/** connect password */
	public static String CONNECT_PASSWORD;
	
	//CLI specific configuration
	/** if 'true' CLI will read batch file on startup*/
	public static boolean CLI_BOOTSTRAP_USEBATCH;
	/** if 'true' CLI will exit after executing bootstrap batch*/
	public static boolean CLI_BOOTSTRAP_BATCHONLY;

	/** printers registry */
	/*generics and foreach not used because of the compatibility with Java 1.4*/
//	public static Hashtable<String, String> printers = new Hashtable<String, String>();
	private static Hashtable printers = new Hashtable();

	static
	{
		printers.put("raw", "odra.sbql.results.runtime.RawResultPrinter");
		printers.put("xml", "odra.filters.XML.XMLResultPrinter");

		Properties properties = new Properties();
		try
		{
			try
			{
				properties.load(new FileInputStream("./conf/odra-client.properties"));
			}
			catch(FileNotFoundException exc)
			{
				try
				{
					//try to get the file from JAR
					InputStream inputStream = ConfigClient.class.getResourceAsStream("/odra-client.properties");
					properties.load(inputStream);
				}
				catch(Exception exc1)
				{
					if(ConfigDebug.DEBUG_EXCEPTIONS)
						exc.printStackTrace();
					
					throw exc;
				}
			}

			TEXT_EDITOR = mapString(properties.getProperty("editor"));
			SOURCE_CODE_PATH = mapString(properties.getProperty("src.path"));
			
			CONNECT_TIMEOUT = mapTimeout(properties.getProperty("timeout.connect"));
			HELLO_TIMEOUT = mapTimeout(properties.getProperty("timeout.hello"));
			NORMAL_TIMEOUT = mapTimeout(properties.getProperty("timeout.normal"));
			
			CONNECT_AUTO = mapBoolean(properties.getProperty("connect.auto"));
			CONNECT_HOST = mapString(properties.getProperty("connect.host"));
			CONNECT_PORT = mapInteger(properties.getProperty("connect.port"));
			CONNECT_USER = mapString(properties.getProperty("connect.user"));
			CONNECT_PASSWORD = mapString(properties.getProperty("connect.password"));
			
			CLI_BOOTSTRAP_USEBATCH = mapBoolean(properties.getProperty("cli.bootstrap.usebatch"));
			CLI_BOOTSTRAP_BATCHONLY = mapBoolean(properties.getProperty("cli.bootstrap.batchonly"));
			clientLogWriter = new LogWriter(
				mapString(properties.getProperty("log.path")), 
				LoggingLevel.getForName(mapString(properties.getProperty("log.level"))));
		}
		catch (IOException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new RuntimeException("Problem with accessing reading client configuration file: " + exc.getMessage());
		}
	}
	
	/**
	 * Returns the log writer.
	 * 
	 * @return {@link LogWriter}
	 */
	public static LogWriter getLogWriter()
	{
		return clientLogWriter;
	}
}
