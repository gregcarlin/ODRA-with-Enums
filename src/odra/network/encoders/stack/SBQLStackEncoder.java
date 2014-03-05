package odra.network.encoders.stack;

import java.util.Vector;

import odra.db.links.encoders.RemoteQueryParameterEncoder;
import odra.db.objects.data.DBLink;
import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.results.QueryResultEncoder;
import odra.network.transport.AutoextendableBuffer;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.stack.ISBQLStackSerializer;
import odra.sbql.stack.StackFrame;
import odra.sessions.Session;

/**
 * SBQLStackEncoder
 * @author Radek Adamus
 *last modified: 2007-02-04
 *@version 1.0
 */
public class SBQLStackEncoder implements ISBQLStackSerializer{

    QueryResultEncoder encoder;
    

	/**
     * @param encoder - encoder use to encode stack elements (query results)
     *  
     */
    public SBQLStackEncoder(QueryResultEncoder encoder) {
	this.encoder = encoder;
    }

	/* (non-Javadoc)
	 * @see odra.network.transport.ISBQLStackSerializer#encodeEnvironment(java.util.Vector)
	 */
	public byte[] serializeEnvironment(Vector<StackFrame> stack) throws RDNetworkException{
	    assert false : "unimplemented";
		return new byte[0];
	}

	/* (non-Javadoc)
	 * @see odra.network.transport.ISBQLStackSerializer#encodeStack(java.util.Vector)
	 */
	public byte[] serialize(Vector<AbstractQueryResult> stack) throws RDNetworkException{
		
		AutoextendableBuffer encodedStack = new AutoextendableBuffer();
		encodedStack.putInt(stack.size());
		for(int i = 0; i < stack.size(); i++){
			byte[] encodedResult = encoder.encodeResult((Result)stack.get(i));
			encodedStack.putInt(encodedResult.length);
			encodedStack.put(encodedResult);
		}
		return encodedStack.getBytes();
	}

}
