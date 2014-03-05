package odra.wrapper.resultpattern;

import java.util.Vector;

import odra.system.config.ConfigServer;
import odra.wrapper.WrapperException;

/**
 * A pattern for forming final query results.
 * 
 * @author jacenty
 * @version 2007-05-25
 * @since 2007-03-05
 */
public class ResultPattern extends Vector<ResultPattern>
{
	public enum Type
	{
		REF("ref"),
		STRUCT("struct"),
		BINDER("binder"),
		VALUE("value"),
		VIRTREF("virtref"); // Added by TK for Volatile Index
		
		private final String name;
		
		private Type(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public static Type getTypeForName(String name)
		{
			if(name.equals(REF.getName()))
				return REF;
			else if(name.equals(STRUCT.getName()))
				return STRUCT;
			else if(name.equals(BINDER.getName()))
				return BINDER;
			else if(name.equals(VALUE.getName()))
				return VALUE;
			else if(name.equals(VIRTREF.getName()))
				return VIRTREF;
			
			throw new AssertionError("Unknown result type: '" + name + "'.");
		}
	}
	
	public enum Deref
	{
		NONE("none"),
		STRING("string"),
		BOOLEAN("boolean"),
		INTEGER("integer"),
		REAL("real"),
		DATE("date");
		
		private final String name;
		
		private Deref(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public static Deref getDerefForName(String name)
		{
			if(name.equals(NONE.getName()))
				return NONE;
			else if(name.equals(STRING.getName()))
				return STRING;
			else if(name.equals(BOOLEAN.getName()))
				return BOOLEAN;
			else if(name.equals(INTEGER.getName()))
				return INTEGER;
			else if(name.equals(REAL.getName()))
				return REAL;
			else if(name.equals(DATE.getName()))
				return DATE;
			
			throw new AssertionError("Unknown dereference type: '" + name + "'.");
		}
	}
	
	/** separator */
	public static final String SEPARATOR = "|";
	
	/** result type */
	private Type type = Type.REF;
	/** dereference mode */
	private Deref deref = Deref.NONE;
	/** table name */
	private String tableName;
	/** column name */
	private String columnName;
	/** alias */
	private String alias;

	
	/**
	 * The constructor
	 * 
	 * @param tableName table name
	 * @param columnName column name
	 * @param alias alias
	 * @param deref dereference mode
	 * @param type result type
	 */
	public ResultPattern(String tableName, String columnName, String alias, Deref deref, Type type)
	{
		this();
		
		this.tableName = tableName;
		this.columnName = columnName;
		this.alias = alias;
		this.deref = deref;
		this.type = type;
	}
	
	/**
	 * The constructor.
	 */
	public ResultPattern()
	{
		super();
	}
	
	/**
	 * Returns a table name.
	 * 
	 * @return table name
	 */
	public String getTableName()
	{
    	// Added by TK for Volatile Index
		ResultPattern pattern = this;
		while ((pattern.getType().equals(Type.BINDER) || pattern.getType().equals(Type.VIRTREF))
				&& pattern.size() == 1)
    		pattern = pattern.firstElement();

		return pattern.tableName;
	}
	
	/**
	 * Returns a column name.
	 * 
	 * @return column name
	 */
	public String getColumnName()
	{
		return columnName;
	}
	
	/**
	 * Returns an alias.
	 * 
	 * @return alias
	 */
	public String getAlias()
	{
		return alias;
	}
	
	/**
	 * Returns a dereference mode.
	 * 
	 * @return {@link Deref}
	 */
	public Deref getDeref()
	{
		return deref;
	}
	
	/**
	 * Returns a result type.
	 * 
	 * @return {@link Type}
	 */
	public Type getType()
	{
		return type;
	}
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	/**
	 * Creates a string representation of this result pattern.
	 * 
	 * @param level nesting level
	 * @return string representation
	 */
	protected String toString(int level)
	{
		String patternString = "<" + level + " ";
		
		if(getTableName() != null)
			patternString += getTableName();
		patternString += " " + SEPARATOR + " ";
		
		if(getColumnName() != null)
			patternString += getColumnName();
		patternString += " " + SEPARATOR + " ";
		
		if(getAlias() != null)
			patternString += getAlias();
		patternString += " " + SEPARATOR + " ";
		
		patternString += getDeref().getName() + " " + SEPARATOR + " ";
		patternString += getType().getName();
		
		for(ResultPattern resultPattern : this)
			patternString += " " + resultPattern.toString(level + 1) + " ";
		
		patternString += " " + level + ">";
		
		return patternString;
	}
	
	/**
	 * Sets a table name.
	 * 
	 * @param tableName table name
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
	
	/**
	 * Sets a column name.
	 * 
	 * @param columnName column name
	 */
	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}
	
	/**
	 * Sets an alias.
	 * 
	 * @param alias alias
	 */
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	/**
	 * Sets a dereference mode..
	 * 
	 * @param deref dereference mode
	 */
	public void setDeref(Deref deref)
	{
		this.deref = deref;
	}
	
	/**
	 * Sets a result type.
	 * 
	 * @param type result type
	 */
	public void setType(Type type)
	{
		this.type = type;
	}
	
	/**
	 * Parses a string representation of {@link ResultPattern}.
	 * 
	 * @param patternString pattern string
	 * @return {@link ResultPattern}
	 * @throws WrapperException 
	 */
	public static ResultPattern parse(String patternString) throws WrapperException
	{
		try
		{
			return parse(patternString, 0);
		}
		catch(Exception exc)
		{
			if(ConfigServer.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new WrapperException("Incorrect pattern string: '" + patternString + "'.", exc);
		}
	}
	
	/**
	 * Parses a result patter string at a given level.
	 * 
	 * @param patternString pattern string
	 * @param level nesting level
	 * @return {@link ResultPattern}
	 * @throws Exception 
	 */
	private static ResultPattern parse(String patternString, int level) throws Exception
	{
		ResultPattern resultPattern = new ResultPattern();
		Vector<String> split = isolateLevel(patternString, level);
		for(String string : split)
		{
			String[] separated = isolateData(string, level);
			String data = separated[0];

			resultPattern = parseData(data);
					
			if(separated.length > 1)
				for(String child : isolateLevel(separated[1], level + 1))
					resultPattern.addElement(parse(child, level + 1));
		}
		return resultPattern;
	}
	
	/**
	 * Parses a data string.
	 * 
	 * @param dataString data string
	 * @return {@link ResultPattern}
	 * @throws Exception 
	 */
	private static ResultPattern parseData(String dataString) throws Exception
	{
		String tableName = null;
		String columnName = null;
		String alias = null;
		Deref deref;
		Type type;
		
		try
		{
			String[] split = dataString.trim().split("\\" + SEPARATOR);
			tableName = split[0].trim().length() > 0 ? split[0].trim() : null;
			columnName = split[1].trim().length() > 0 ? split[1].trim() : null;
			alias = split[2].trim().length() > 0 ? split[2].trim() : null;
			deref = Deref.getDerefForName(split[3].trim());
			type = Type.getTypeForName(split[4].trim());
			
			ResultPattern resultPattern = new ResultPattern();
			resultPattern.setTableName(tableName);
			resultPattern.setColumnName(columnName);
			resultPattern.setAlias(alias);
			resultPattern.setDeref(deref);
			resultPattern.setType(type);
			
			return resultPattern;
		}
		catch(ArrayIndexOutOfBoundsException exc)//constant or operator
		{
			String[] split = dataString.trim().split(" ");
			if(split[0].equals("c"))
			{
				dataString = dataString.substring(1, dataString.length() - 1).trim();//cut first and last 'c'
				split = dataString.split("\\" + SEPARATOR);
				
				String value = split[0].trim();
				value = value.substring(1, value.length() - 1);//remove enclosing 's
				Deref primitive = Deref.getDerefForName(split[1].trim());
				
				return new ConstantPattern(value, primitive);
			}
			else if(split[0].equals("o"))
				return new OperatorPattern(Integer.parseInt(split[1].trim()));
			else
				throw new Exception();
		}
	}
	
	/**
	 * Parses a pattern string for data and child patterns.
	 * 
	 * @param patternString pattern string
	 * @param level nesting level
	 * @return data string and the remaining
	 */
	private static String[] isolateData(String patternString, int level)
	{
		String dataSeparator = "<" + (level + 1);
		
		if(patternString.indexOf(dataSeparator) > 0)
			return new String[] {
				patternString.substring(0, patternString.indexOf(dataSeparator)),
				patternString.substring(patternString.indexOf(dataSeparator))};
		
		return new String[] {patternString};
	}
	
	/**
	 * Parses a pattern string at the given level.
	 * 
	 * @param patternString pattern string
	 * @param level nesting level
	 * @return {@link Vector} of pattern strings
	 */
	private static Vector<String> isolateLevel(String patternString, int level)
	{
		Vector<String> result = new Vector<String>();
		
		String regexp = "[<" + level + "][" +  level + ">]";
		for(String string : patternString.split(regexp))
		{
			string = string.trim();
			if(string.endsWith("o") || string.endsWith("c"))
				string = string.substring(0, string.length() - 1).trim();
			if(string.length() > 0)
				result.addElement(string);
		}
		
		return result;
	}
}
