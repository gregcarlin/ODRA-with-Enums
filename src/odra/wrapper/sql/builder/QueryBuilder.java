package odra.wrapper.sql.builder;

import java.util.Hashtable;
import java.util.Vector;

import odra.wrapper.WrapperException;
import odra.wrapper.model.Column;
import odra.wrapper.model.Database;
import odra.wrapper.sql.Query;
import odra.wrapper.sql.Type;
import Zql.ZConstant;
import Zql.ZDelete;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZGroupBy;
import Zql.ZInsert;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUpdate;

/**
 * Query builder class. 
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2007-02-06
 */
public class QueryBuilder
{
	/** "from" items */
	private Vector<ZFromItem> froms = new Vector<ZFromItem>();
	/** "select" items */
	private Vector<ZSelectItem> selects = new Vector<ZSelectItem>();
	/** "where" expression */
	private ZExpression where;
	/** "order by" columns */
	private Vector<String> orderBys = new Vector<String>();
	
	/** table for updates, deletes, etc. */
	private odra.wrapper.model.Table imperativeTable = null;
	/** column:value expressions for updates and inserts */
	private Hashtable<String, ZConstant> columnValues = new Hashtable<String, ZConstant>(); 
		
	/** database model */
	private final Database model;
	
	/**
	 * The constructor.
	 *
	 * @param model database model
	 */
	public QueryBuilder(Database model)
	{
		this.model = model;
	}
	
	/**
	 * Builds a query according to the given type.
	 *  
	 * @param type query to build type
	 * @return query
	 * @throws WrapperException 
	 */
	public Query build(Type type) throws WrapperException
	{
		if(type == Type.SELECT)
		{
			ZQuery query = new ZQuery();
			query.addFrom(froms);
			query.addSelect(selects);
			query.addWhere(where);
			if(!orderBys.isEmpty())
				query.addOrderBy(orderBys);
			
			return new Query(query.toString(), model);
		}
		else if(type == Type.UPDATE)
		{
			ZUpdate update = new ZUpdate(imperativeTable.getName());
			update.addWhere(where);
			update.addSet(columnValues);
			return new Query(update.toString(), model);
		}
		else if(type == Type.DELETE)
		{
			ZDelete delete = new ZDelete(imperativeTable.getName());
			delete.addWhere(where);
			
			return new Query(delete.toString(), model);
		}
		else if(type == Type.INSERT)
		{
			ZInsert insert = new ZInsert(imperativeTable.getName());
			
			Vector<String> columns = new Vector<String>();
			ZExpression values = new ZExpression(",");
			for(String columnName : columnValues.keySet())
			{
				columns.addElement(columnName);
				values.addOperand(columnValues.get(columnName));
			}
			insert.addColumns(columns);
			insert.addValueSpec(values);
						
			return new Query(insert.toString(), model);
		}
		
		return null;
	}
	
	/**
	 * Adds a "from" table.
	 * 
	 * @param table table
	 */
	public void addFrom(Table table)
	{
		for(ZFromItem from : froms)
			if(from.getTable().equals(table.getName()))
				return;
		
		froms.add(new ZFromItem(table.getName()));
	}
	
	/**
	 * Adds a "select" table.
	 * 
	 * @param table table
	 */
	public void addSelect(Table table)
	{
		for(String column : table.getSelectedColumns())
		{
			try
			{
				//check if aggregate
				String[] split = column.split("[\\(\\)]");
				String aggregate = split[0];
				String columName = split[1];
				Aggregate.getAggregateForString(aggregate);

				if(!columName.equals(Table.ALL))
					selects.add(new ZSelectItem(aggregate + "(" + table.getName() + "." + columName + ")"));
				else
					selects.add(new ZSelectItem(aggregate + "(" + columName + ")"));
			}
			catch(AssertionError err)
			{
				for(ZSelectItem select : selects)
					if(select.getTable().equals(table.getName()) && select.getColumn().equals(column))
						return;
				
				selects.add(new ZSelectItem(table.getName() + "." + column));
			}
			catch(Exception exc)
			{
				for(ZSelectItem select : selects)
					if(select.getTable().equals(table.getName()) && select.getColumn().equals(column))
						return;
				
				selects.add(new ZSelectItem(table.getName() + "." + column));
			}
		}
	}
	
	/**
	 * Sets the "where" expression.
	 * 
	 * @param whereExpression "where" expression
	 */
	public void setWhere(ZExpression whereExpression)
	{
		where = whereExpression;
	}
	
	/**
	 * Returns the "where" expression.
	 * 
	 * @return "where" expression
	 */
	public ZExpression getWhereExpression()
	{
		return where;
	}
	
	/**
	 * Adds a column:value pair for updates and inserts.
	 * 
	 * @param column column
	 * @param value value
	 */
	@SuppressWarnings("null")
	public void addColumnValue(Column column, Object value)
	{
		int type = ZConstant.UNKNOWN;
		if(value == null)
			type = ZConstant.NULL;
		else if(value instanceof Number)
			type = ZConstant.NUMBER;
		else if(value instanceof String)
			type = ZConstant.STRING;
		ZConstant right = new ZConstant(value.toString(), type);
		
		columnValues.put(column.getName(), right);
	}
	
	/**
	 * Sets a table for imperative statements.
	 * 
	 * @param imperativeTable table
	 */
	public void setImperativeTable(odra.wrapper.model.Table imperativeTable)
	{
		this.imperativeTable = imperativeTable;
	}
	
	/**
	 * Adds sorting columns.
	 * 
	 * @param column column
	 */
	public void addOrderBy(Column column)
	{
		orderBys.addElement(column.getFullName());
	}
}
