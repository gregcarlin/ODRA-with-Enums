package odra.wrapper.sql.result;

import odra.wrapper.model.Column;

/**
 * A class for encapsulating a query result from a database table row. 
 * @author jacenty
 * @version   2007-01-06
 * @since   2007-01-06
 */
public class ColumnResult
{
	/** column */
	private final Column column;
	/** value */
	private final Object value;
	
	/**
	 * The constructor.
	 * 
	 * @param column column
	 * @param value value
	 */
	ColumnResult(Column column, Object value)
	{
		this.column = column;
		this.value = value;
	}
	
	/**
	 * Returns the associated column.
	 * 
	 * @return column
	 */
	public Column getColumn()
	{
		return column;
	}
	
	/**
	 * Returns the column value.
	 * 
	 * @return value
	 */
	public Object getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		String indent = "\t\t\t";
		String column = indent + "COLUMN (" + this.column.getName() + "): " + value;
		
		return column;
	}
}
