package odra.sbql.ast.terminals;

import java.util.Date;

/**
 * Date literal.
 * 
 * @author jacenty
 * @version 2007-03-19
 * @since 2007-03-19
 */
public class DateLiteral extends Terminal
{
	private Date V;

	public DateLiteral(Date val)
	{
		V = val;
	}

	

	/**
	 * @return the v
	 */
	public Date value()
	{
	    return V;
	}
}
