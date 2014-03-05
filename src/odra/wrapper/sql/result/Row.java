package odra.wrapper.sql.result;

import java.util.List;

/**
 * A class for encapsulating a single table result eow (referring to a single queried table). 
 * @author jacenty
 * @version   2007-01-06
 * @since   2006-09-20
 */
public class Row
{
	/** table results */
	private final List<TableResult> tableResults;
	
	/**
	 * Constructor.
	 * 
	 * @param tableResults table results
	 */
	public Row(List<TableResult> tableResults)
	{
		this.tableResults = tableResults;
	}
	
	/**
	 * Returns table results.
	 * 
	 * @return table results
	 */
	public List<TableResult> getTableResults()
	{
		return tableResults;
	}
	
	@Override
	public String toString()
	{
		String indent = "\t";
		String row = indent + "ROW\n";
		
		for(TableResult tableResult : tableResults)
			row += indent + tableResult.toString() + "\n";
		
		return row;
	}
}
