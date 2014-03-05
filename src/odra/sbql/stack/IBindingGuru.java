package odra.sbql.stack;

import odra.db.DatabaseException;
import odra.sbql.results.AbstractQueryResult;

/**
 * The interface must be implemented by classes performing binding
 * in envs sections. With the introduction of nesters, the process
 * of binding may generate objects representing references.
 * Since references at runtime are represented by the class ReferenceResult,
 * and their compile-time counterparts are represented by ReferenceSignature,
 * separate binding implementations are necessary.
 *  
 * @author raist
 */
public interface IBindingGuru {
	public void setFrame(StackFrame frame); // sets an envs frame on which the binding is to be performed
	public AbstractQueryResult[] bind(int name_id) throws DatabaseException; // binds a name in the section
}
