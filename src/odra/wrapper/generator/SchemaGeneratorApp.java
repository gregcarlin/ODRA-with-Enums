package odra.wrapper.generator;

import odra.wrapper.config.TorqueConfig;

import org.apache.commons.configuration.ConfigurationException;


/**
 * A sample generator application.
 *  
 * @author jacenty
 * @version   2007-07-25
 * @since   2006-12-03
 */
public class SchemaGeneratorApp
{
	public static void main(String[] args)
	{
		try
		{
			String configFilePath = "./conf/" + TorqueConfig.CONFIG_FILE_NAME;
	  	String dbName = null;
	  	try
	  	{
	  		configFilePath = args[0];

	    	try
	    	{
	    		dbName = args[1];
	    	}
	    	catch(ArrayIndexOutOfBoundsException exc) {}
	  	}
	  	catch(ArrayIndexOutOfBoundsException exc) {}
			
			TorqueConfig config = new TorqueConfig(dbName, configFilePath);
			
			System.out.println("Schema generation started...");
			long start = System.currentTimeMillis();
			new SchemaGenerator().generateXMLSchemaFile("./conf", config);
			long stop = System.currentTimeMillis();
			System.out.println("Schema generation finished in " + (stop - start) + " ms...");
		}
		catch(ConfigurationException exc)
		{
			System.out.println("Config file error!!!");
  		exc.printStackTrace();
  		System.exit(0);
		}
	}
}
