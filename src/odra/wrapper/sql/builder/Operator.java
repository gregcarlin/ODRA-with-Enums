package odra.wrapper.sql.builder;

/**
 * Operators. 
 * 
 * @author jacenty
 * @version 2007-07-26
 * @since 2007-02-06
 */
public enum Operator
{
	NOT(odra.sbql.ast.terminals.Operator.NOT, "not", "!"),
	
	AND(odra.sbql.ast.terminals.Operator.AND, "and", "&&"),
	OR(odra.sbql.ast.terminals.Operator.OR, "or", "||"),
	
	PLUS(odra.sbql.ast.terminals.Operator.PLUS, "+", "+"),
	MINUS(odra.sbql.ast.terminals.Operator.MINUS, "-", "-"),
	
	EQUAL(odra.sbql.ast.terminals.Operator.EQUALS, "=", "=="),
	NOT_EQUAL(odra.sbql.ast.terminals.Operator.DIFFERENT, "<>", "!="),
	
	GREATER(odra.sbql.ast.terminals.Operator.GREATER, ">", ">"),
	GREATER_OR_EQUAL(odra.sbql.ast.terminals.Operator.GREATEREQUALS, ">=", ">="),
	LESS(odra.sbql.ast.terminals.Operator.LOWER, "<", "<"),
	LESS_OR_EQUAL(odra.sbql.ast.terminals.Operator.LOWEREQUALS, "<=", "<="),
	
	MATCH_STRING(odra.sbql.ast.terminals.Operator.MATCH_STRING, "like", "=~"),
	NOT_MATCH_STRING(odra.sbql.ast.terminals.Operator.NOT_MATCH_STRING, "not like", "!~"),
	;
	
	private final int sbqlOperatorCode;
	private final String sqlOperator;
	private final String rdqlOperator;
	
	Operator(int sbqlOperatorCode, String sqlOperator, String rdqlOperator)
	{
		this.sbqlOperatorCode = sbqlOperatorCode;
		this.sqlOperator = sqlOperator;
		this.rdqlOperator = rdqlOperator;
	}
	
	public String getSqlOperator()
	{
		return sqlOperator;
	}
	
	public String getRdqlOperator()
	{
		return rdqlOperator;
	}
	
	public static Operator getOperator(odra.sbql.ast.terminals.Operator sbqlOperator)
	{
		for(Operator operator : Operator.values())
			if(operator.sbqlOperatorCode == sbqlOperator.getAsInt())
				return operator;
		
		throw new AssertionError("Unsupported SBQL operator: " + sbqlOperator.spell());
	}
	
	public static Operator getOperator(String sqlOperator)
	{
		for(Operator operator : Operator.values())
			if(operator.sqlOperator.equalsIgnoreCase(sqlOperator))
				return operator;
		
		throw new AssertionError("Unsupported SQL operator: " + sqlOperator);
	}
	
	/**
	 * Returns a RDQL-specific string comparison operator.
	 * 
	 * @param operator operator
	 * @return RDQL-specific string operator
	 */
	public static String getRdqlStringOperator(Operator operator)
	{
		if(operator.equals(Operator.EQUAL))
			return "EQ";
		else if(operator.equals(Operator.NOT_EQUAL))
			return "NE";
		
		throw new IllegalArgumentException("Operator '" + operator + "' cannot be used for this method argument.");
	}
}
