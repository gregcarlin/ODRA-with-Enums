package odra.sbql.stack;

import odra.sbql.results.AbstractQueryResult;

/**
 * The class represents binders stored on the environment stack.
 * Binders support name binding.
 * 
 * @author raist
 */

public class Binder extends EnvsElement {
	public int name_id;
	public AbstractQueryResult val;

	// name of the binder and its value
	public Binder(int name_id, AbstractQueryResult val) {
		this.name_id = name_id;
		this.val = val;
	}
}
