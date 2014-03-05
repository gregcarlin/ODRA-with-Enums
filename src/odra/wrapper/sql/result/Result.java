package odra.wrapper.sql.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import odra.util.StringUtils;
import odra.wrapper.WrapperException;
import odra.wrapper.model.Database;
import odra.wrapper.model.Table;
import odra.wrapper.sql.Query;
import swardAPI.SwardScan;
import Zql.ZFromItem;
import Zql.ZSelectItem;

/**
 * A class for encapsulating a query result. 
 * @author jacenty
 * @version   2007-07-25
 * @since   2006-09-12
 */
public class Result extends Vector<Row>
{
	/** id holder */
	private final List<String> ids = new Vector<String>();
	
	/** rows */
	private final List<Row> rows = new Vector<Row>();
	/** query string */
	private final String queryString;
	/** database */
	private final Database database;
	
	/**
	 * The constructor.
	 * 
	 * @param query {@link Query}
	 * @param rs {@link ResultSet}
	 * @throws SQLException
	 * @throws WrapperException
	 */
	public Result(Query query, ResultSet rs) throws SQLException, WrapperException
	{
		this.queryString = query.getQueryString();
		this.database = query.getDatabase();
		
		Database database = query.getDatabase();
		Vector<ZFromItem> froms = query.getFroms();
		Vector<ZSelectItem> selects = query.getSelects();
		
		while(rs.next())
		{
			List<TableResult> tableResults = new Vector<TableResult>();
			for(ZFromItem from : froms)
			{
				Table table = database.getTable(from.getTable());
				List<ColumnResult> columnResults = new Vector<ColumnResult>();
				for(int i = 0; i < selects.size(); i++)
				{
					ZSelectItem select = selects.get(i);
					if(new Table(database, select.getTable()).equals(table))
						columnResults.add(new ColumnResult(table.getColumn(select.getColumn()), rs.getObject(i + 1)));
				}
				
				TableResult tableResult = new TableResult(table, columnResults, createId(table));
				tableResults.add(tableResult);
			}
			
			Row row = new Row(tableResults);
			rows.add(row);
		}
	}
	
	/**
	 * The constructor.
	 * 
	 * @param query {@link Query}
	 * @param res {@link SwardScan}
	 * @throws WrapperException
	 */
	@SuppressWarnings("unchecked")
	public Result(Query query, SwardScan res) throws WrapperException
	{
		this.queryString = query.getQueryString();
		this.database = query.getDatabase();
		
		Database database = query.getDatabase();
		Vector<ZFromItem> froms = query.getFroms();
		Vector<ZSelectItem> selects = query.getSelects();
		
		while(!res.eof())
		{
			Vector<Vector<String>> tuple = res.next();
			List<TableResult> tableResults = new Vector<TableResult>();
			for(ZFromItem from : froms)
			{
				Table table = database.getTable(from.getTable());
				List<ColumnResult> columnResults = new Vector<ColumnResult>();
				for(int i = 0; i < selects.size(); i++)
				{
					ZSelectItem select = selects.get(i);
					if(new Table(database, select.getTable()).equals(table))
					for(Vector<String> pair : tuple)
							if(pair.get(0).equalsIgnoreCase(select.getColumn()))
								columnResults.add(new ColumnResult(table.getColumn(select.getColumn()), pair.get(1)));
				}
				TableResult tableResult = new TableResult(table, columnResults, createId(table));
				tableResults.add(tableResult);
			}
			
			Row row = new Row(tableResults);
			rows.add(row);
		}
	}
	
	/**
	 * Creates an unique id strong for a table result.
	 * 
	 * @param table table
	 * @return id string
	 */
	private String createId(Table table)
	{
		String id = null;
		if(table.hasBestRowIdentifier())
		{
			do
			{
				id = table.getName() + "_" + StringUtils.randomString(12, true);
			}
			while(ids.contains(id));
		}
		
		return id;
	}
	
	/**
	 * Returns result rows.
	 * 
	 * @return rows
	 */
	public List<Row> getRows()
	{
		return rows;
	}
	
	@Override
	public String toString()
	{
		String result = "Result from: " + queryString + "\n";
		
		for(Row row : rows)
			result += row.toString() + "\n";
		
		return result;
	}
	
	/**
	 * Returns a database.
	 * 
	 * @return database
	 */
	public Database getDatabase()
	{
		return database;
	}
}
