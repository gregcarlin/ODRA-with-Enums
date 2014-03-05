package odra.wrapper.model;

import java.io.Serializable;
import java.util.Vector;

/**
 * A foreign key representation. 
 * @author jacenty
 * @version   2007-02-25
 * @since   2006-05-22
 */
public class ForeignKey implements Serializable
{
	/** foreign key name */
	private final String name;
	/** local table */
	private final Table localTable;
	/** local columns */
	private final Vector<Column> localColumns;
	/** referenced table */
	private final Table refTable;
	/** referenced columns */
	private final Vector<Column> refColumns;
	
	/**
	 * Constructor.
	 *  
	 * @param localTable local table
	 * @param localColumns local columns
	 * @param refTable referenced table
	 * @param refColumns referenced columns
	 */
	public ForeignKey(Table localTable, Vector<Column> localColumns, Table refTable, Vector<Column> refColumns)
	{
		this.name = localTable.getName() + " -> " + refTable.getName();
		this.localTable = localTable;
		this.localColumns = localColumns;
		this.refTable = refTable;
		this.refColumns = refColumns;
	}

	/**
	 * Returns a list of local columns.
	 * 
	 * @return list of local columns
	 */
	public Vector<Column> getLocalColumns()
	{
		return localColumns;
	}

	/**
	 * Returns the local table.
	 * 
	 * @return local table
	 */
	public Table getLocalTable()
	{
		return localTable;
	}

	/**
	 * Returns key's name.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns a list of referenced columns.
	 * 
	 * @return list of referenced columns
	 */	
	public Vector<Column> getRefColumns()
	{
		return refColumns;
	}

	/**
	 * Returns the referenced table.
	 * 
	 * @return referenced table
	 */
	public Table getRefTable()
	{
		return refTable;
	}
}
