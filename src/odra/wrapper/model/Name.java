package odra.wrapper.model;

/**
 * Utility class for bidirectional name conversions (relational/ODRA).
 * 
 * @author jacenty
 * @version 2007-07-23
 * @since 2007-07-23
 */
public final class Name
{
	/** ODRA name prefix */
	public static final String PREFIX = "$";
	
	/**
	 * Converts a relational name to an ODRA name.
	 * 
	 * @param name name
	 * @return
	 */
	public static String r2o(String name)
	{
		if(!name.startsWith(PREFIX))
			return PREFIX + name;
		
		return name;
	}
	
	/**
	 * Converts an ODRA name to a relational name.
	 * 
	 * @param name name
	 * @return
	 */
	public static String o2r(String name)
	{
		if(name.startsWith(PREFIX))
			return name.substring(1);
		
		return name;
	}
}
