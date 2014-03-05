package odra.wrapper.sql;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import odra.system.config.ConfigDebug;
import odra.wrapper.Wrapper;
import odra.wrapper.WrapperException;
import odra.wrapper.model.Column;
import odra.wrapper.model.Database;
import odra.wrapper.model.ForeignKey;
import odra.wrapper.model.SwardDatabase;
import odra.wrapper.model.Table;
import odra.wrapper.net.Server;
import odra.wrapper.sql.builder.Operator;
import Zql.ParseException;
import Zql.ZConstant;
import Zql.ZDelete;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZGroupBy;
import Zql.ZInsert;
import Zql.ZOrderBy;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZStatement;
import Zql.ZUpdate;
import Zql.ZqlParser;

/**
 * Query parser/generator class.
 * 
 * @author jacenty
 * @version 2008-01-28
 * @since 2006-09-12
 */
public class Query
{
	private final String AUX_PREFIX = "___aux_";
	private int auxCounter = 0;
	private Hashtable<String, String> auxNames = new Hashtable<String, String>(); 

	/** parser */
	private ZqlParser parser;
	/** statement */
	private ZStatement statement;
	/** database */
	private final Database database;
	
	/**
	 * Constructor.
	 * 
	 * @param queryString query string
	 * @param database database
	 * @throws WrapperException
	 */
	public Query(String queryString, Database database) throws WrapperException
	{
		try
		{
			if(!queryString.endsWith(";"))
				queryString += ";";

			queryString = introduceAuxNames(queryString);
			
			this.database = database;
      this.parser = new ZqlParser();
      
      parser.initParser(new ByteArrayInputStream(queryString.getBytes()));
      statement = parser.readStatement();

      if(getType() == Type.SELECT)
      {
				if(Server.WRAPPER_EXPAND_WITH_IDS)
					expandWithRefs();
				if(Server.WRAPPER_EXPAND_WITH_REFS)
					expandWithTids();
      }
    }
		catch(ParseException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new WrapperException("Query parsing (" + queryString + ")", exc, WrapperException.Error.QUERY_SYNTAX);
    }
		catch(WrapperException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new WrapperException("Query parsing (" + queryString + ")", exc, WrapperException.Error.QUERY_SYNTAX);
    }
		catch(Error exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new WrapperException("Query parsing (" + queryString + ")", exc, WrapperException.Error.QUERY_SYNTAX);
    }
		catch(Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			throw new WrapperException("Query parsing (" + queryString + ")", exc, WrapperException.Error.QUERY_SYNTAX);
    }
	}

	/**
	 * Returns the query string.
	 * 
	 * @return query
	 */
	public String getQueryString()
	{
		String queryString = statement.toString();
		queryString = removeAuxNames(queryString);
		
		if(getType().equals(Type.DELETE) && queryString.indexOf("from") < 0)
			queryString = queryString.replaceFirst("delete", "delete from");
		
		return queryString;
	}
	
	/**
	 * Returns the query string for a given mode.
	 * 
	 * @return query
	 * @throws WrapperException 
	 */
	public String getQueryStringForMode(int mode) throws WrapperException
	{
		if(mode == Wrapper.MODE_SD)
		{
			String sqQueryString = "sd_" + getQueryString();
			String[] split = sqQueryString.split(" ", 2);
			sqQueryString = split[0] + " '" + split[1] + "'";

			return sqQueryString;
		}
		else if(mode == Wrapper.MODE_SWARD)
		{
			SwardDatabase database = (SwardDatabase)this.database;
			
			String transformed = "select ";
			for(ZSelectItem selectItem : getSelects())
				transformed += "?" + selectItem.getColumn() + ", ";
			if(transformed.length() > 0)
				transformed = transformed.substring(0, transformed.length() - 2);
			
			transformed += 
				" from <" + database.getUpvUri() + "> ";
			
			//the first triple is only for order of variables
			transformed +=
				" where (?" + database.getSubject() + ", ?" + database.getPredicate() + ", ?" + database.getObject() + ")";
			
			//actual conditions are expressed as logical filters
			ZExpression wheres = getWhere();
			String filters = transformRdqlCondition(wheres);
			if(filters.length() > 0)
				transformed += " AND " + filters;
			
			transformed = removeAuxNames(transformed);
			
			return transformed;
		}
		else
			return getQueryString();
	}
	
	/**
	 * Transforms SQL where conditions into RDQL logical filters.
	 * 
	 * @param where SQL condition as {@link ZExpression}
	 * @return RDQL logical filter
	 */
	private String transformRdqlCondition(ZExpression where)
	{
		String filter = "";
		if(where == null)
			return filter;
		
		Operator operator = Operator.getOperator(where.getOperator());
		
		if(operator.equals(Operator.AND) || operator.equals(Operator.OR))
		{
			ZExpression left = (ZExpression)where.getOperand(0);		
			ZExpression right = (ZExpression)where.getOperand(1);
			filter += 
				transformRdqlCondition(left) + 
				" " + operator.getRdqlOperator() + " " +
				transformRdqlCondition(right);
		}
		else
		{
			String column = where.getOperand(0).toString().split("\\.")[1];
			String value = where.getOperand(1).toString();
			
			String rdqlOperatorString = operator.getRdqlOperator();
			//check if the actual value is a number or a string and get appropriate RDQL operator
			try
			{
				Double.parseDouble(value);
			}
			catch(Exception exc)
			{
				if(operator.equals(Operator.EQUAL) || operator.equals(Operator.NOT_EQUAL))
					rdqlOperatorString = Operator.getRdqlStringOperator(operator);
				value = "\"" + value.substring(1, value.length() - 1) + "\"";//replace ' with "
			}
			
			filter +=
				"?" + column + 
				" " + rdqlOperatorString + " " +
				value;
		}
		
		return filter;
	}
	
	@Override
	public String toString()
	{
		return getQueryString();
	}
	
	/**
	 * Returns query type.
	 * 
	 * @return type
	 */
	public Type getType()
	{
		if(statement instanceof ZQuery)
			return Type.SELECT;
		else if(statement instanceof ZDelete)
			return Type.DELETE;
		else if(statement instanceof ZInsert)
			return Type.INSERT;
		else if(statement instanceof ZUpdate)
			return Type.UPDATE;
		else 
			return null;
	}
	
	/**
	 * Returns query "from" elements.
	 * 
	 * @return "from" elements
	 * @throws WrapperException
	 */
	@SuppressWarnings("unchecked")
	public Vector<ZFromItem> getFroms() throws WrapperException
	{
		if(getType().equals(Type.SELECT))
			return ((ZQuery)statement).getFrom();
		else
			throw new WrapperException("Invalid query type for this method", WrapperException.Error.QUERY_TYPE);
	}
	
	/**
	 * Returns query "where" conditions.
	 * 
	 * @return "where"
	 * @throws WrapperException
	 */
	@SuppressWarnings("unchecked")
	public ZExpression getWhere() throws WrapperException
	{
		if(getType().equals(Type.SELECT))
			return (ZExpression)((ZQuery)statement).getWhere();
		else if(getType().equals(Type.DELETE))
			return (ZExpression)((ZDelete)statement).getWhere();
		else if(getType().equals(Type.UPDATE))
			return (ZExpression)((ZUpdate)statement).getWhere();
		else
			throw new WrapperException("Invalid query type for this method", WrapperException.Error.QUERY_TYPE);
	}
	
	/**
	 * Returns query "select" elements.
	 * 
	 * @return "select" elements
	 * @throws WrapperException
	 */
	@SuppressWarnings("unchecked")
	public Vector<ZSelectItem> getSelects() throws WrapperException
	{
		if(getType().equals(Type.SELECT))
			return ((ZQuery)statement).getSelect();
		else
			throw new WrapperException("Invalid query type for this method", WrapperException.Error.QUERY_TYPE);
	}
	
	/**
	 * Returns query "group by" element.
	 * 
	 * @return "group by" element
	 * @throws WrapperException
	 */
	public ZGroupBy getGroupBy() throws WrapperException
	{
		if(getType().equals(Type.SELECT))
			return ((ZQuery)statement).getGroupBy();
		else
			throw new WrapperException("Invalid query type for this method", WrapperException.Error.QUERY_TYPE);
	}
	
	/**
	 * Returns query "order by" elements.
	 * 
	 * @return "order by" elements
	 * @throws WrapperException
	 */
	@SuppressWarnings("unchecked")
	public Vector<ZOrderBy> getOrderBys() throws WrapperException
	{
		if(getType().equals(Type.SELECT))
			return ((ZQuery)statement).getOrderBy();
		else
			throw new WrapperException("Invalid query type for this method", WrapperException.Error.QUERY_TYPE);
	}
	
	/**
	 * Returns expression operands.
	 * 
	 * @param expression expression
	 * @return operands
	 */
	public Vector getOperands(ZExpression expression)
	{
		return expression.getOperands();
	}
	
	/**
	 * Returns a query string expanded with TID columns (wheer available), if not present in the original one.
	 * 
	 * @throws WrapperException
	 */
	private void expandWithTids() throws WrapperException
	{
		Vector<ZFromItem> froms = getFroms();
		Vector<ZSelectItem> selects = getSelects();
		ZExpression where = getWhere();
		ZGroupBy groupBy = getGroupBy();
		Vector<ZOrderBy> orderBys = getOrderBys();
		
		Vector<ZSelectItem> newSelects = new Vector<ZSelectItem>();
		
		for(ZSelectItem selectItem : selects)
		{
			Table table = database.getTable(selectItem.getTable());
			Column[] bestRowIdColumns = table.getBestRowIdColumns().values().toArray(new Column[0]);
			for(Column column : bestRowIdColumns)
			{
				ZSelectItem newSelect = new ZSelectItem(column.getFullName());
			
				boolean exists = false;
				for(ZSelectItem testItem : selects)
					if(testItem.getTable().equals(newSelect.getTable()) && testItem.getColumn().equals(newSelect.getColumn()))
					{
						exists = true;
						break;
					}
				
				if(!exists)
					for(ZSelectItem testItem : newSelects)
						if(testItem.getTable().equals(newSelect.getTable()) && testItem.getColumn().equals(newSelect.getColumn()))
						{
							exists = true;
							break;
						}
					
				if(!exists)
					newSelects.add(newSelect);
			}
		}
		
		selects.addAll(newSelects);
		
		ZQuery expandedQuery = new ZQuery();
		expandedQuery.addFrom(froms);
		expandedQuery.addSelect(selects);
		expandedQuery.addWhere(where);
		expandedQuery.addGroupBy(groupBy);
		expandedQuery.addOrderBy(orderBys);

		statement = expandedQuery;
	}
	
	/**
	 * Creates recursively a list of table names in a 'where' expression.
	 * 
	 * @param tables table list
	 * @param expr expression
	 */
	public void listWhereTables(List<String> tables, Object expr)
	{
		if(expr instanceof ZExpression)
		{
			Vector operands = getOperands((ZExpression)expr);
			for(Object operand : operands)
				listWhereTables(tables, operand);
		}
		else if(expr instanceof ZConstant)
		{
			String tableName = expr.toString().split("\\.")[0];
			if(database.getTable(tableName) != null && !tables.contains(tableName))
				tables.add(tableName);
		}
	}
	
	/**
	 * Returns a query string expanded with reference (FK/PK) columns (wheer available), if not present in the original one.
	 * 
	 * @throws WrapperException
	 */
	private void expandWithRefs() throws WrapperException
	{
		Vector<ZFromItem> froms = getFroms();
		Vector<ZSelectItem> selects = getSelects();
		ZExpression where = getWhere();
		ZGroupBy groupBy = getGroupBy();
		Vector<ZOrderBy> orderBys = getOrderBys();
		
		Vector<ZSelectItem> newSelects = new Vector<ZSelectItem>();
		
		for(ZSelectItem selectItem : selects)
		{
			Table table = database.getTable(selectItem.getTable());
			ForeignKey[] foreignKeys = table.getForeignKeys().values().toArray(new ForeignKey[0]);
			for(ForeignKey key : foreignKeys)
			{
				Vector<Column> columns = key.getLocalColumns();
				for(Column column : columns)
				{
					String colName = column.getFullName();
					ZSelectItem newSelect = new ZSelectItem(colName);
							
					boolean exists = false;
					for(ZSelectItem testItem : selects)
						if(testItem.getTable().equals(newSelect.getTable()) && testItem.getColumn().equals(newSelect.getColumn()))
						{
							exists = true;
							break;
						}
					
					if(!exists)
						for(ZSelectItem testItem : newSelects)
							if(testItem.getTable().equals(newSelect.getTable()) && testItem.getColumn().equals(newSelect.getColumn()))
							{
								exists = true;
								break;
							}
					
					if(!exists)
						newSelects.add(newSelect);
				}
			}
		}
		
		selects.addAll(newSelects);
		
		ZQuery expandedQuery = new ZQuery();
		expandedQuery.addFrom(froms);
		expandedQuery.addSelect(selects);
		expandedQuery.addWhere(where);
		expandedQuery.addGroupBy(groupBy);
		expandedQuery.addOrderBy(orderBys);
		
		statement = expandedQuery;
	}
	
	/**
	 * Returns the associated database.
	 * 
	 * @return database
	 */
	public Database getDatabase()
	{
		return database;
	}
	
	/**
	 * Returns if this query returns a result of an aggregate function.
	 * 
	 * @return aggregate?
	 * @throws WrapperException 
	 */
	public boolean isAggregate() throws WrapperException
	{
		for(ZSelectItem selectItem : getSelects())
			if(selectItem.getAggregate() != null)
				return true;
		
		return false;
	}
	
	/**
	 * Replaces string literals with auxilary strings in order to aviod parser errors on diacritic characters.
	 * 
	 * @param queryString query string
	 * @return modified query string
	 */
	private String introduceAuxNames(String queryString)
	{
		Vector<Integer> apostrophes = new Vector<Integer>();
		int index = queryString.indexOf("'");
		while(index >= 0)
		{
			apostrophes.addElement(index);
			index = queryString.indexOf("'", index + 1);
		}

		int auxOffset = 0;
		for(int i = 0; i < apostrophes.size(); i++)
		{
			int start = auxOffset + apostrophes.get(i) + 1;
			int stop = auxOffset + apostrophes.get(++i);
			
			String token = queryString.substring(start, stop);
			String aux = AUX_PREFIX + auxCounter++;
			
			auxOffset += aux.length() - token.length();
			queryString = queryString.substring(0, start) + aux + queryString.substring(stop);

			auxNames.put(aux, token);
		}
		
		return queryString;
	}
	
	/**
	 * Replaces auxiliary names with their values.
	 * 
	 * @param queryString query string
	 * @return modified query string
	 */
	private String removeAuxNames(String queryString)
	{
		String result = queryString;
		for(String auxName : auxNames.keySet())
			result = result.replaceFirst(auxName, auxNames.get(auxName));
		
		return result;
	}
}