package odra.wrapper.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import odra.wrapper.type.SbqlType;
import odra.wrapper.type.XsdType;


/**
 * An table column representation. 
 * @author jacenty
 * @version   2007-02-06
 * @since   2006-05-21
 */
public class Column implements Serializable
{
	/** table */
	private final Table table;
	/** column name */
	private final String name;
	/** column type */
	private final String type;
	/** nullable */
	private final boolean nullable;
	
	/**
	 * Constructor.
	 * 
	 * @param table table
	 * @param name name
	 * @param type type
	 * @param nullable nullable?
	 */
	public Column(Table table, String name, String type, boolean nullable)
	{
		this.table = table;
		this.name = name.toLowerCase();
		this.type = type;
		this.nullable = nullable;
	}

	/**
	 * Return the table this belongs to.
	 * 
	 * @return table
	 */
	public Table getTable()
	{
		return table;
	}
	
	/**
	 * Returns column's name.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns column's full name (qualified with a table name).
	 * 
	 * @return full name
	 */
	public String getFullName()
	{
		return table.getName() + "." + name;
	}

	/**
	 * Returns column's original type.
	 * 
	 * @return type
	 */
	public String getOriginalType()
	{
		return type;
	}
	
	/**
	 * Returns column's corresponding SBQL type.
	 * 
	 * @return SBQL type
	 */
	public String getSbqlType()
	{
		return SbqlType.getSbqlType(type);
	}
	
	/**
	 * Returns column's corresponding XSD type.
	 * 
	 * @return XSD type
	 */
	public String getXsdType()
	{
		return XsdType.getXsdType(type);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof Column))
			return false;
		else
		{
			Column column = (Column)object;
			return column.getName().equals(this.getName()) && column.getTable().equals(this.getTable());
		}
	}

	@Override
	public String toString()
	{
		return "column: " + getTable().getName() + "." + getName();
	}

	/**
	 * Returns the list of foreign key referencing the column.
	 * 
	 * @return foreign keys
	 */
	@SuppressWarnings("unchecked")
	public List<ForeignKey> getReferences()
	{
		List<ForeignKey> references = new ArrayList();
		
		Hashtable<String, Table> tables = table.getDatabase().getTables();
		Enumeration tableNames = tables.keys();
		while(tableNames.hasMoreElements())
		{
			Object tableName = tableNames.nextElement();
			Table table = tables.get(tableName);
			
			Hashtable<String, ForeignKey> foreignKeys = table.getForeignKeys();
			Enumeration foreignKeyNames = foreignKeys.keys();
			while(foreignKeyNames.hasMoreElements())
			{
				Object foreignKeyName = foreignKeyNames.nextElement();
				ForeignKey foreignKey = foreignKeys.get(foreignKeyName);
				
				if(foreignKey.getRefColumns().contains(this))
					references.add(foreignKey);
			}
		}
		
		return references;
	}
	
	/**
	 * Returns if the column is referenced by another column.
	 * 
	 * @param column column
	 * @return is referenced?
	 */
	public boolean isReferencedBy(Column column)
	{
		List<ForeignKey> references = getReferences();
		for(int i = 0; i < references.size(); i++)
		{
			ForeignKey foreignKey = references.get(i);
			List<Column> localColumns = foreignKey.getLocalColumns();
			
			if(localColumns.contains(column))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the column is indexed.
	 * 
	 * @return is indexed?
	 */
	public boolean isIndexed()
	{
		Hashtable<String, Index> indices = table.getIndices();
		Enumeration<String> indexNames = indices.keys();
		while(indexNames.hasMoreElements())
		{
			Index index = indices.get(indexNames.nextElement());
			if(index.getColumns().contains(this))
				return true;
		}
		
		return false;
	}

	/**
	 * Returns if the column is single unique.
	 * 
	 * @return uniqueness type
	 */
	public boolean isSingleUnique()
	{
		Hashtable<String, Index> indices = table.getIndices();
		Enumeration<String> indexNames = indices.keys();
		while(indexNames.hasMoreElements())
		{
			Index index = indices.get(indexNames.nextElement());
			if(index.getColumns().contains(this) && index.isUnique() && index.isSingleIndexColumn())
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the column is group unique.
	 * 
	 * @return uniqueness type
	 */
	public boolean isGroupUnique()
	{
		Hashtable<String, Index> indices = table.getIndices();
		Enumeration<String> indexNames = indices.keys();
		while(indexNames.hasMoreElements())
		{
			Index index = indices.get(indexNames.nextElement());
			if(index.getColumns().contains(this) && index.isUnique() && !index.isSingleIndexColumn())
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the column is nullable.
	 * 
	 * @return nullable?
	 */
	public boolean isNullable()
	{
		return nullable;
	}
}