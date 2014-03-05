package odra.sbql.stack;

import odra.db.*;

/**
 * The class represents nesters - entities stored on the environment stack
 * and used to support lazy binding. Nesters are like macros -
 * they represent results of the operation nested performed on
 * particular objects. For example, if nester(i0) is on the stack,
 * the stack behaves like if there were binders based on suboject of the object i0.
 * This helps us minimize the number of binder introductions/removals performed on envs.
 * 
 * @author raist
 */

public class Nester extends EnvsElement {
	public OID val; // parent complex object
	
	public Nester(OID val) {
		this.val = val;
	}
}
