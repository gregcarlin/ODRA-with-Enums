package odra.wrapper.model;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * A database table representation. 
 * @author jacenty
 * @version     2007-02-22
 * @since     2006-05-21
 * @uml.dependency   supplier="odra.kis.wrapper.model.Column.Column"
 */
public class Table implements Serializable
{
	/** table name */
	protected final String name;
	/** database */
	protected final Database database;
	
	/** columns */
	private Hashtable<String, Column> columns = new Hashtable<String, Column>();
	/** indices */
	private Hashtable<String, Index> indices = new Hashtable<String, Index>();
	/** foreign keys */
		private Hashtable<String, ForeignKey> foreignKeys = new Hashtable<String, ForeignKey>();
	/** columns */
	private Hashtable<String, Column> bestRowIdColumns = new Hashtable<String, Column>();
	
	/**
	 * Constructor.
	 * 
	 * @param name table name
	 */
	public Table(Database database, String name)
	{
		this.database = database;
		this.name = name.toLowerCase();
	}

	/**
	 * Returns table's name.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns table's databse.
	 * 
	 * @return database
	 */
	public Database getDatabase()
	{
		return database;
	}
	
	/**
	 * Adds a column (if not added before).
	 * 
	 * @param column column
	 * @return column
	 */
	public Column addColumn(Column column)
	{
		if(!columns.containsKey(column.getName()))
			columns.put(column.getName(), column);
		
		return getColumn(column.getName());
	}
	
	/**
	 * Returns a column with a given name, <code>null</code> if column not found.
	 * 
	 * @param name column name
	 * @return column
	 */
	public Column getColumn(String name)
	{
		return columns.get(name.toLowerCase());
	}
	
	/**
	 * Adds an index (if not added before).
	 * 
	 * @param index index
	 * @return index
	 */
	public Index addIndex(Index index)
	{
		if(!indices.containsKey(index.getName()))
			indices.put(index.getName(), index);
		
		return getIndex(index.getName());
	}
	
	/**
	 * Returns an index with a given name, <code>null</code> if column not found.
	 * 
	 * @param name index name
	 * @return index
	 */
	public Index getIndex(String name)
	{
		return indices.get(name);
	}
	
	/**
	 * Adds a foreign key (if not added before).
	 * 
	 * @param foreignKey foreign key
	 * @return foreign key
	 */
	public ForeignKey addForeignKey(ForeignKey foreignKey)
	{
		if(!foreignKeys.containsKey(foreignKey.getName()))
			foreignKeys.put(foreignKey.getName(), foreignKey);
		
		return getForeignKey(foreignKey.getName());
	}
	
	/**
	 * Returns a foreign key with a given name, <code>null</code> if column not found.
	 * 
	 * @param name foreign name
	 * @return foreign key
	 */
	public ForeignKey getForeignKey(String name)
	{
		return foreignKeys.get(name);
	}
	
	/**
	 * Return all foreign keys for the table.
	 * 
	 * @return foreign keys
	 */
	public Hashtable<String, ForeignKey> getForeignKeys()
	{
		return foreignKeys;
	}
	
	/**
	 * Return all indices for the table.
	 * 
	 * @return indices
	 */
	public Hashtable<String, Index> getIndices()
	{
		return indices;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof Table))
			return false;
		else
			return ((Table)object).getName().equals(this.getName());
	}

	@Override
	public String toString()
	{
		return "table: " + getName();
	}
	
	/**
	 * Adds a best row identifier column (if not added before).
	 * 
	 * @param column column
	 * @return column
	 */
	public Column addBestRowIdColumn(Column column)
	{
		if(!bestRowIdColumns.containsKey(column.getName()))
			bestRowIdColumns.put(column.getName(), column);
		
		return getColumn(column.getName());
	}
	
	/**
	 * Returns a best row identifier column with a given name, <code>null</code> if column not found.
	 * 
	 * @param name column name
	 * @return column
	 */
	public Column getBestRowIdColumn(String name)
	{
		return bestRowIdColumns.get(name);
	}
	
	/**
	 * Returns a best row identifier columns.
	 * 
	 * @return best row identifier columns
	 */
	public Hashtable<String, Column> getBestRowIdColumns()
	{
		return bestRowIdColumns;
	}
	
	/**
	 * Returns table's columns.
	 * 
	 * @return columns
	 */
	public Hashtable<String, Column> getColumns()
	{
		return columns;
	}
	
	/**
	 * Returns if the table has the best row identifier columns.
	 * 
	 * @return has best row id?
	 */
	public boolean hasBestRowIdentifier()
	{
		return bestRowIdColumns.size() > 0;
	}
	
	/**
	 * Returns if this table references a table.
	 * 
	 * @param table table
	 * @return references?
	 */
	public boolean references(Table table)
	{
		Hashtable<String, ForeignKey> keys = getForeignKeys();
		Enumeration<String> keyNames = keys.keys();
		while(keyNames.hasMoreElements())
		{
			String keyName = keyNames.nextElement();
			ForeignKey key = keys.get(keyName);
			
			if(key.getRefTable().equals(table))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the table has foreign keys.
	 * 
	 * @return has best foreign keys?
	 */
	public boolean hasForeignKeys()
	{
		return foreignKeys.size() > 0;
	}
	
	/**
	 * Returns if this table contains a column with a given name.
	 * 
	 * @param name column name
	 * @return contains the column?
	 */
	public boolean containsColumn(String name)
	{
		return columns.containsKey(name.toLowerCase());
	}
	
	/**
	 * Returns a list of foreign keys for a referenced table.
	 * 
	 * @param refTable referenced table
	 * @return list of foreign keys
	 */
	public List<ForeignKey> getForeignKeysForTable(Table refTable)
	{
		Vector<ForeignKey> keys = new Vector<ForeignKey>();
		Enumeration foreignKeys = this.foreignKeys.elements();
		while(foreignKeys.hasMoreElements())
		{
			ForeignKey key = (ForeignKey)foreignKeys.nextElement();
			if(key.getRefTable().equals(refTable))
				keys.addElement(key);
		}
		
		return keys;
	}
	
	/**
	 * Returns a list of foreign keys for a referenced table.
	 * 
	 * @param refTableName referenced table name
	 * @return list of foreign keys
	 */
	public List<ForeignKey> getForeignKeysForTable(String refTableName)
	{
		assert getDatabase().containsTable(refTableName) : "The model does not contain a table '" + refTableName + "'.";
		
		return getForeignKeysForTable(getDatabase().getTable(refTableName));
	}
}
