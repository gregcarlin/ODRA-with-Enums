package odra.wrapper.type;

import odra.wrapper.generator.XSDGenerator;

/**
 * Utility class for data type names' conversion between SQL and XSD. 
 * @author jacenty
 * @version     2007-03-25
 * @since     2006-12-15
 */
public class XsdType
{
	public static final String STRING = XSDGenerator.NAMESPACE_PREFIX + ":string";
	public static final String INTEGER = XSDGenerator.NAMESPACE_PREFIX + ":integer";
	public static final String REAL = XSDGenerator.NAMESPACE_PREFIX + ":double";
	public static final String BOOLEAN = XSDGenerator.NAMESPACE_PREFIX + ":boolean";
	public static final String DATE = XSDGenerator.NAMESPACE_PREFIX + ":date";
	public static final String ID = XSDGenerator.NAMESPACE_PREFIX + ":ID";
	public static final String IDREF = XSDGenerator.NAMESPACE_PREFIX + ":IDREF";
	public static final String IDREFS = XSDGenerator.NAMESPACE_PREFIX + ":IDREFS";
	
	private final static Object[][] XSD_TYPES = new Object[][] {
		new Object[] {STRING, SqlType.SQL_STRINGS}, 
		new Object[] {INTEGER, SqlType.SQL_INTEGERS},
		new Object[] {REAL, SqlType.SQL_REALS},
		new Object[] {BOOLEAN, SqlType.SQL_BOOLEANS},
		new Object[] {DATE, SqlType.SQL_DATES},
	};
	
	/**
	 * Returns XSD data type corresponding to SQL type.
	 * 
	 * @param sqlType SQL data type
	 * @return XSD data type
	 */
	public static String getXsdType(String sqlType)
	{
		for(int i = 0; i < XSD_TYPES.length; i++)
		{
			String[] patterns = (String[])XSD_TYPES[i][1];
			for(int j = 0; j < patterns.length; j++)
				if(patterns[j].equalsIgnoreCase(sqlType))
					return (String)XSD_TYPES[i][0];
		}
		
		return STRING;
	}
	
	//TODO boolean type recognition for non-boolean types
}