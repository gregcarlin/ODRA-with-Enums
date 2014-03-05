package odra.wrapper.model;

import java.io.Serializable;

/**
 * An index column representation. 
 * @author jacenty
 * @version   2006-12-03
 * @since   2006-05-21
 */
public class IndexColumn extends Column implements Serializable
{
	/** column position */
	private final short position;
	
	/**
	 * Constructor.
	 * 
	 * @param name column name
	 * @param position column position
	 */
	public IndexColumn(String name, short position)
	{
		super(null, name, null, false);
		this.position = position;
	}
	
	/**
	 * Returns column's position in the index.
	 * 
	 * @return position
	 */
	public short getPosition()
	{
		return position;
	}
}
