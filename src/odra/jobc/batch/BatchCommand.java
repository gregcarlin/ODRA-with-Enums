/**
 * 
 */
package odra.jobc.batch;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import odra.jobc.ODRACommand;

/**
 * BatchCommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public class BatchCommand implements Iterable{
	List commands = new Vector();
	
	
	
	/**
	 * @param command - ODRAComand to add to the BatchCommand
	 */
	public void add(ODRACommand command){
		commands.add(command);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator iterator() {
		return commands.iterator();
	}
	
	
}
