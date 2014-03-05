package odra.system.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import odra.system.log.LogWriter;

/**
 * Abstract configuration class.
 * 
 * @author raist, merdacz, jacenty
 */
abstract class Config
{
	
    	
	protected static String mapString(String value) 
	{
		return value.trim();
	}
	
	protected static boolean mapBoolean(String value) 
	{
		return Boolean.valueOf(value.trim()).booleanValue();
	}
	
	protected static int mapInteger(String value) 
	{
		try 
		{
			return Integer.parseInt(value.trim());	
		} 
		catch (NumberFormatException ex) 
		{
		//	logWriter.getLogger().log(Level.SEVERE, "Error in configuration file. ");
			System.exit(-1);
			return -1;
		}
	}
	
	protected static int mapTimeout(String value) 
	{
		try 
		{
			int timeout = Integer.parseInt(value.trim());
			if(timeout < 0)
				timeout = 0;
			
			return timeout;
		} 
		catch (NumberFormatException ex) 
		{

		//	logWriter.getLogger().log(Level.SEVERE, "Error in configuration file. ");
			System.exit(-1);
			return -1;
		}
	}
}