package odra.wrapper.resultpattern;

import odra.sbql.ast.terminals.Operator;

/**
 * SBQL operator representation pattern.
 * 
 * @author jacenty
 * @version 2007-05-27
 * @since 2007-05-26
 */
public class OperatorPattern extends ResultPattern
{
	/** operator */
	private final Operator operator;
	
	/**
	 * The constructor.
	 * 
	 * @param operator operator
	 * @param left left result pattern
	 * @param right right result pattern
	 */
	public OperatorPattern(Operator operator, ResultPattern left, ResultPattern right)
	{
		this.operator = operator;
		addElement(left);
		addElement(right);
	}
	
	/**
	 * The package-visible constructor, mainly for building parsed patterns.
	 * 
	 * @param operatorCode
	 */
	OperatorPattern(int operatorCode)
	{
		this.operator = Operator.get(operatorCode);
	}
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	/**
	 * Creates a string representation of this constant pattern.
	 * 
	 * @param level nesting level
	 * @return string representation
	 */
	@Override
	protected String toString(int level)
	{
		String patternString = "<" + level + "o ";
		
		patternString += operator.getAsInt() + " ";
		
		for(ResultPattern subPattern : this)
			patternString += subPattern.toString(level + 1) + " ";

		patternString += " o" + level + ">";
		
		return patternString;
	}
}
