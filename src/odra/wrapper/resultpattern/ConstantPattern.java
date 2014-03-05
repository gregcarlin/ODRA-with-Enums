package odra.wrapper.resultpattern;

import java.text.ParseException;

import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.util.DateUtils;
import odra.wrapper.resultpattern.ResultPattern.Deref;

/**
 * Constant (literal) value representation pattern.
 * 
 * @author jacenty
 * @version 2007-05-27
 * @since 2007-05-26
 */
public class ConstantPattern extends ResultPattern
{
	/** string representation of constant value */
	private final String value;
	/** primitive type */
	private final Deref primitive;
	
	/**
	 * The constructor.
	 * 
	 * @param expression {@link StringExpression}
	 */
	public ConstantPattern(StringExpression expression)
	{
		this.value = expression.getLiteral().value();
		this.primitive = Deref.STRING;
	}
	
	/**
	 * The package-visible constructor, mainly for building parsed patterns.
	 * 
	 * @param value string representation of constant value
	 * @param primitive primitive type
	 */
	ConstantPattern(String value, Deref primitive)
	{
		this.value = value;
		this.primitive = primitive;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param expression {@link IntegerExpression}
	 */
	public ConstantPattern(IntegerExpression expression)
	{
		this.value = Integer.toString(expression.getLiteral().value());
		this.primitive = Deref.INTEGER;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param expression {@link RealExpression}
	 */
	public ConstantPattern(RealExpression expression)
	{
		this.value = Double.toString(expression.getLiteral().value());
		this.primitive = Deref.REAL;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param expression {@link BooleanExpression}
	 */
	public ConstantPattern(BooleanExpression expression)
	{
		this.value = Boolean.toString(expression.getLiteral().value());
		this.primitive = Deref.BOOLEAN;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param expression {@link DateExpression}
	 */
	public ConstantPattern(DateExpression expression)
	{
		this.value = expression.getLiteral().value().toString();
		this.primitive = Deref.DATE;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param expression {@link DateprecissionExpression}
	 */
	public ConstantPattern(DateprecissionExpression expression) throws ParseException
	{
		this.value = DateUtils.formatDatePrecission(
			((DateExpression)expression.getLeftExpression()).getLiteral().value(), 
			((StringExpression)expression.getRightExpression()).getLiteral().value()).toString();
		this.primitive = Deref.DATE;
	}
	
	/**
	 * Returns the primitive constant type.
	 * 
	 * @return primitive constant type
	 */
	public Deref getPrimitive()
	{
		return primitive;
	}
	
	/**
	 * Returns the string value representation.
	 * 
	 * @return string value representation
	 */
	public String getValue()
	{
		return value;
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
		String patternString = "<" + level + "c ";
		
		patternString += "'" + getValue() + "' " + SEPARATOR + " ";
		patternString += getPrimitive().getName() + " " + SEPARATOR + " ";

		patternString += " c" + level + ">";
		
		return patternString;
	}
}
