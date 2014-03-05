package odra.ws.bindings;

import java.util.Hashtable;


/**
 * Represents generic binding information
 * 
 * @since 2007-03-26
 * @version  2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public abstract class BindingInfo {

	/** Detrmines if binding info is in valid state
	 * @return
	 */
	public abstract boolean isValid();
	
	/** Gets all option entries
	 * @return
	 */
	public abstract Hashtable<String, String> getEntries();
	
	/** Loads binding specifi options from hashtable
	 * @param table
	 */
	public abstract void load(Hashtable<String, String> table);
}
