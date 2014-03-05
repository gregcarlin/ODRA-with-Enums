/**
 * 
 */
package odra.sbql.stack;

import odra.sbql.results.AbstractQueryResult;

/** AggregateBinder
 * This class represent special binders to aggregate objects
 * it supports lazy binding in similiar way as @see Nesters
 * but on the level of aggregate objects
 * e.g. if nested is performed on object i0 and inside we have aggregate objects
 * AggregateBinder is created instead of pushing binders to all subobjects of the
 * aggregate
 * @author Radek Adamus
 *@since 2007-03-30
 *last modified: 2007-03-30
 *@version 1.0
 */
public class AggregateBinder extends Binder {

	/**
	 * @param name_id
	 * @param val
	 */
	public AggregateBinder(int name_id, AbstractQueryResult val) {
		super(name_id, val);
	}

}
