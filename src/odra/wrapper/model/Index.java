package odra.wrapper.model;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * A table index representation. 
 * @author jacenty
 * @version   2006-11-26
 * @since   2006-05-21
 */
public class Index implements Serializable
{
	public enum Type 
	{ 
		TABLE_INDEX_STATISTICS ((short)1), 
		TABLE_INDEX_CLUSTERED ((short)2), 
		TABLE_INDEX_HASHED ((short)3), 
		TABLE_INDEX_OTHER ((short)4);
		
		private final short type;
		
		Type(short type)
		{
			this.type = type;
		}
		
		public short getType()
		{
			return type;
		}
		
		public String getTypeName()
		{
			switch (this)
			{
				case TABLE_INDEX_STATISTICS:
					return "tableIndexStatistics";
				case TABLE_INDEX_CLUSTERED:
					return "tableIndexClustered";
				case TABLE_INDEX_HASHED:
					return "tableIndexHashed";
				case TABLE_INDEX_OTHER:
					return "tableIndexOther";
				default: 
					throw new AssertionError("Unknown type: " + this);
			}
		}

		public static Type getTypeForShort(short type)
		{
			switch (type)
			{
				case 1: 
					return TABLE_INDEX_STATISTICS;
				case 2: 
					return TABLE_INDEX_CLUSTERED;
				case 3: 
					return TABLE_INDEX_HASHED;
				case 4: 
					return TABLE_INDEX_OTHER;
			}
			throw new AssertionError("Unknown type: " + type);
		}
		
		@Override
		public String toString()
		{
			return "Index type :" + type;
		}
	};
	
	/** index name */
	private final String name;
	/** unique? */
	private final boolean unique;
	/** type */
	private final Type type;
	/** cardinality */
	private final int cardinality;
	/** pages */
	private final int pages;
	/** filter condition */
	private final String filterCondition;
	
	/** IndexColumn list */
	@SuppressWarnings("unchecked")
	private List<IndexColumn> columns = new Vector();
	
	/**
	 * Constructor.
	 * 
	 * @param name index name
	 * @param unique is unique?
	 * @param type type
	 * @param pages page count
	 * @param cardinality cardinality
	 * @param filterCondition filter condition
	 */
	public Index(String name, boolean unique, Type type, int pages, int cardinality, String filterCondition)
	{
		this.name = name;
		this.unique = unique;
		this.type = type;
		this.pages = pages;
		this.cardinality = cardinality;
		this.filterCondition = filterCondition;
	}
	
	/**
	 * Returns index name.
	 * 
	 * @return index name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns if the index is unique.
	 * 
	 * @return unique?
	 */
	public boolean isUnique()
	{
		return unique;
	}
	
	/**
	 * Returns index cardinality.
	 * 
	 * @return cardinality
	 */
	public int getCardinality()
	{
		return cardinality;
	}
	
	/**
	 * Returns index type.
	 * 
	 * @return type
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Returns index page count.
	 * 
	 * @return page count
	 */
	public int getPages()
	{
		return pages;
	}
	
	/**
	 * Returns index filter condition.
	 * 
	 * @return filter condition
	 */
	public String getFilterCondition()
	{
		return filterCondition;
	}
	
	/**
	 * Adds a column to the index.
	 * 
	 * @param column column
	 */
	public void addColumn(IndexColumn column)
	{
		columns.add(column);
	}
	
	/**
	 * Returns the ordered table of index IndexColumns. 
	 * 
	 * @return ordered table of IndexColumns 
	 */
	@SuppressWarnings("unchecked")
	public IndexColumn[] getOrderedColumns()
	{
		IndexColumn[] order = new IndexColumn[columns.size()];
		
		for(int i = 0; i < columns.size(); i++)
			order[columns.get(i).getPosition() - 1] = columns.get(i);
		
		return order;
	}
	
	/**
	 * Returns the number of IndexColumns in the index.
	 * 
	 * @return number of IndexColumns
	 */
	public int getColumnCount()
	{
		return columns.size();
	}
	
	/**
	 * Retruns if the index is based on a single IndexColumn.
	 * 
	 * @return only single IndexColumn?
	 */
	public boolean isSingleIndexColumn()
	{
		return getColumnCount() == 1;
	}
	
	/**
	 * Returns an unordered list of index IndexColumns. 
	 * 
	 * @return list of index IndexColumns
	 */
	public List<IndexColumn> getColumns()
	{
		return columns;
	}
}
