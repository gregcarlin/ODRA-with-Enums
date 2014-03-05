package odra.system.log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Logging level definitions.
 * 
 * @author jacenty
 * radamus: changed from enum to class for the required Java 1.4 compatibility 
 * @version 2007-12-31
 * @since 2007-07-02
 */
public class LoggingLevel
{
	private final static String QUIET = "quiet";// { @Override public Level setConsoleLevel() { return Level.OFF; } },
	private final static String NORMAL = "normal";// { @Override public Level setConsoleLevel() { return Level.INFO; } },
	private final static String VERBOSE = "verbose";// { @Override public Level setConsoleLevel() { return Level.FINE; } },
	private final static String CRAZY ="crazy";// { @Override public Level setConsoleLevel() { return Level.ALL; } };
	private final static Map levels = new HashMap();
	static{
	    levels.put(QUIET, Level.OFF);
	    levels.put(NORMAL, Level.INFO);
	    levels.put(VERBOSE, Level.FINE);
	    levels.put(CRAZY, Level.ALL);
	}
	private final String name;
	private LoggingLevel(String name)
	{
		this.name = name;
	}
	
	public Level setFileLevel() { return Level.ALL; }
	public Level setConsoleLevel() {
	    return (Level)levels.get(name);	   
	}
	
	public static LoggingLevel getForName(String name)
	{
	    if(levels.get(name) != null)
		return new LoggingLevel(name);
		
		return new LoggingLevel(NORMAL);
	}
}
