package odra.wrapper.sql.result;

import java.util.List;

import odra.wrapper.model.Table;


/**
 * A class for encapsulating a query result from a database table. 
 * @author jacenty
 * @version   2007-01-07
 * @since   2007-01-06
 */
public class TableResult
{
	/** table */
	private final Table table;
	/** column results */
	private final List<ColumnResult> columnResults;
	/** id string for ID attribute (can be <code>null</code>) */
	private final String id;
	
	/**
	 * The constructor.
	 * 
	 * @param table table
	 * @param columnResults column results
	 * @param id id string for ID attribute (can be <code>null</code>)
	 */
	TableResult(Table table, List<ColumnResult> columnResults, String id)
	{
		this.table = table;
		this.columnResults = columnResults;
		this.id = id;
	}
	
	/**
	 * Returns the associated table.
	 * 
	 * @return table
	 */
	public Table getTable()
	{
		return table;
	}
	
	/**
	 * Returns the column results.
	 * 
	 * @return column results
	 */
	public List<ColumnResult> getColumnResults()
	{
		return columnResults;
	}

	@Override
	public String toString()
	{
		String indent = "\t\t";
		String table = indent + "TABLE (" + this.table.getName() + ")\n";
		
		for(ColumnResult columnResult : columnResults)
			table += indent + columnResult.toString() + "\n";
		
		return table;
	}
	
	/**
	 * Returns id string value.
	 * 
	 * @return id
	 */
	public String getId()
	{
		return id;
	}
}
