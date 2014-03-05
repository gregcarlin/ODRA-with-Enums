package odra.wrapper.sql.builder;

import odra.wrapper.model.Column;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

/**
 * Recursive "where" conditions builder class. 
 * 
 * @author jacenty
 * @version 2007-06-27
 * @since 2007-02-06
 */
public class WhereBuilder
{
	/**
	 * Builds an expression for a two-column comparison. 
	 * 
	 * @param operator operator
	 * @param leftExpression left expression
	 * @param rightExpression right expression
	 * @return expression
	 */
	public static ZExpression build(Operator operator, ZExp leftExpression, ZExpression rightExpression)
	{
		return new ZExpression(operator.getSqlOperator(), leftExpression, rightExpression);
	}
	
	/**
	 * Builds an expression for a two-column comparison. 
	 * 
	 * @param operator operator
	 * @param leftColumn left column
	 * @param rightColumn right column
	 * @return expression
	 */
	public static ZExpression build(Operator operator, Column leftColumn, Column rightColumn)
	{
		ZConstant left = new ZConstant(leftColumn.getFullName(), ZConstant.COLUMNNAME);
		ZConstant right = new ZConstant(rightColumn.getFullName(), ZConstant.COLUMNNAME);
		
		return new ZExpression(operator.getSqlOperator(), left, right);
	}
	
	/**
	 * Builds an expression for a column-value comparison.
	 * 
	 * @param operator operator
	 * @param column column
	 * @param value value
	 * @return expression
	 */
	@SuppressWarnings("null")
	public static ZExpression build(Operator operator, Column column, Object value)
	{
		if(value instanceof Column)
			return build(operator, column, (Column)value);
		
		ZConstant left = new ZConstant(column.getFullName(), ZConstant.COLUMNNAME);
		
		int type = ZConstant.UNKNOWN;
		if(value == null)
			type = ZConstant.NULL;
		else if(value instanceof Number)
			type = ZConstant.NUMBER;
		else if(value instanceof String)
			type = ZConstant.STRING;
		ZConstant right = new ZConstant(value.toString(), type);
		
		return new ZExpression(operator.getSqlOperator(), left, right);
	}
}
