package odra.wrapper.type;

/**
 * Utility class for data type names' conversion between SQL and SBQL. 
 * @author jacenty
 * @version     2007-03-25
 * @since     2006-12-03
 */
public class SbqlType
{
	public static final String STRING = "string";
	public static final String INTEGER = "integer";
	public static final String REAL = "real";
	public static final String BOOLEAN = "boolean";
	public static final String DATE = "date";
	private final static Object[][] SBQL_TYPES = new Object[][] {
		new Object[] {STRING, SqlType.SQL_STRINGS}, 
		new Object[] {INTEGER, SqlType.SQL_INTEGERS},
		new Object[] {REAL, SqlType.SQL_REALS},
		new Object[] {BOOLEAN, SqlType.SQL_BOOLEANS},
		new Object[] {DATE, SqlType.SQL_DATES},
	};
	
	/**
	 * Return SBQL data type corresponding to SQL type.
	 * 
	 * @param sqlType SQL data type
	 * @return SBQL data type
	 */
	public static String getSbqlType(String sqlType)
	{
		for(int i = 0; i < SBQL_TYPES.length; i++)
		{
			String[] patterns = (String[])SBQL_TYPES[i][1];
			for(int j = 0; j < patterns.length; j++)
				if(patterns[j].equalsIgnoreCase(sqlType))
					return (String)SBQL_TYPES[i][0];
		}
		
		return STRING;
	}
	
	//TODO boolean type recognition for non-boolean types
}
