package odra.sbql.stack;

import java.util.Vector;
import odra.sbql.results.AbstractQueryResult;

/** Serializer interface for all SBQL stack serializers
 * ISBQLStackSerializer
 * @author radamus
 *last modified: 2006-12-28
 *@version 1.0
 */
public interface ISBQLStackSerializer {
	/**
	 * @param stack - query result stack to encode 
	 */
	byte[] serialize(Vector<AbstractQueryResult> stack) throws Exception;
	/**
	 * @param envs - environment stack to encode
	 */
	byte[] serializeEnvironment(Vector<StackFrame> envs) throws Exception;
}
