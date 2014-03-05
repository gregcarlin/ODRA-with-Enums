package odra.wrapper.type;

/**
 * SQL types' dictionary. 
 * @author jacenty
 * @version     2007-03-25
 * @since     2006-12-19
 */
public class SqlType
{
	public final static String[] SQL_STRINGS = new String[] {
		"varchar",
		"varchar2",
		"char",
		"text",
		"memo",
		"clob"
	};
	
	public final static String[] SQL_INTEGERS = new String[] {
		"integer",
		"int",
		"int2",
		"int4",
		"int8",
		"serial",
		"smallint",
		"bigint",
		"byte",
		"serial",
	};
	
	public final static String[] SQL_REALS = new String[] {
		"number",
		"float",
		"real",
		"numeric",
		"decimal"
	};
	
	public final static String[] SQL_BOOLEANS = new String[] {
		"bool",
		"boolean",
		"bit",
	};
	
	public final static String[] SQL_DATES = new String[] {
		"date",
		"timestamp",
	};
}
