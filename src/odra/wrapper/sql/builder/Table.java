package odra.wrapper.sql.builder;

import java.util.Enumeration;
import java.util.Vector;

import odra.system.config.ConfigDebug;
import odra.wrapper.WrapperException;
import odra.wrapper.WrapperException.Error;
import odra.wrapper.model.Column;

/**
 * Query builder <code>Table</code> specialization.
 * 
 * @author jacenty
 * @version 2007-07-23
 * @since 2007-02-06
 */
public class Table
{
	public static final String ALL = "*";
	
	/** associated table */
	private final odra.wrapper.model.Table table;
	
	/** selected columm names */
	private Vector<String> selectedColumnNames = new Vector<String>();
	/** selected columm aggragate functions */
	private Vector<Aggregate> aggreagates = new Vector<Aggregate>();
	
	/**
	 * The constructor.
	 * 
	 * @param table associated table
	 */
	public Table(odra.wrapper.model.Table table)
	{
		if(ConfigDebug.ASSERTS)
			assert table != null : "The table cannot be null";
		
		this.table = table;
	}
	
	/**
	 * Returns the associated table name.
	 * 
	 * @return associated table name
	 */
	public String getName()
	{
		return table.getName();
	}
		
	/**
	 * Returns columns (with aggregate functions, if defined).
	 * 
	 * @return columns
	 */
	public Vector<String> getSelectedColumns()
	{
		Vector<String> result = new Vector<String>();
		for(int i = 0; i < selectedColumnNames.size(); i++)
		{
			String columnName = selectedColumnNames.get(i);
			if(getAggreagate(i) != null)
				columnName = getAggreagate(i).getFunction() + "(" + columnName + ")";
			result.addElement(columnName);
		}
		
		return result;
	}
	
	/**
	 * Adds a column.
	 * 
	 * @param columnName column name
	 * @throws WrapperException 
	 */
	public void addSelectedColumn(String columnName) throws WrapperException
	{
		if(columnName.equals(ALL))
		{
			selectedColumnNames.removeAllElements();
			Enumeration<String> allColumns = table.getColumns().keys();
			while(allColumns.hasMoreElements())
			{
				selectedColumnNames.addElement(allColumns.nextElement());
				aggreagates.addElement(null);
			}
		}
		else
		{
			if(!table.containsColumn(columnName))
				throw new WrapperException("The table '" + table.getName() + "' does not contain a '" + columnName + "' column.", Error.INVALID_COLUMN);
			
			if(!selectedColumnNames.contains(columnName))
			{
				selectedColumnNames.addElement(columnName);
				aggreagates.addElement(null);
			}
		}
	}
	
	/**
	 * Adds an aggregate function.
	 * 
	 * @param columnName column name
	 * @param aggregate aggregate
	 * @throws WrapperException 
	 */
	public void addAggregate(String columnName, Aggregate aggregate) throws WrapperException
	{
		if(!columnName.equals(ALL) && !table.containsColumn(columnName))
			throw new WrapperException("The table '" + table.getName() + "' does not contain a '" + columnName + "' column.", Error.INVALID_COLUMN);
		
		selectedColumnNames.addElement(columnName);
		aggreagates.addElement(aggregate);
	}
	
	/**
	 * Returns a column object associated with a selected column name at the specified index.
	 * 
	 * @param index index
	 * @return column
	 */
	public Column getColumn(int index)
	{
		return table.getColumn(selectedColumnNames.get(index));
	}
	
	/**
	 * Returns an aggreagate function for a selected column name at the specified index.
	 * 
	 * @param index index
	 * @return aggregate
	 */
	public Aggregate getAggreagate(int index)
	{
		return aggreagates.get(index);
	}
	
	/**
	 * Returns the associated {@link odra.wrapper.model.Table} object.
	 * 
	 * @return table
	 */
	public odra.wrapper.model.Table getTable()
	{
		return table;
	}
}
