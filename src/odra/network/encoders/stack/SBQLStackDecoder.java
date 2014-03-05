package odra.network.encoders.stack;

import java.nio.ByteBuffer;
import java.util.Vector;

import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.stack.StackFrame;

/** 
 * SBQLStackDecoder
 * @author Radek Adamus
 *last modified: 2007-02-04
 *@version 1.0
 */
public class SBQLStackDecoder {
    	QueryResultDecoder decoder;
	
	/**
	 * @param decoder
	 */
	public SBQLStackDecoder(QueryResultDecoder decoder) {
	    this.decoder = decoder;
	}


	public Vector<StackFrame> decodeEnvironment(byte[] rawEnvs) throws RDNetworkException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Vector<AbstractQueryResult> decodeStack(byte[] rawStack) throws RDNetworkException {
		 
		ByteBuffer buffer = ByteBuffer.wrap(rawStack);
		int stackSize = buffer.getInt();
		Vector<AbstractQueryResult> stack = new Vector<AbstractQueryResult>();
		for(int i = 0; i < stackSize ; i++){
			int resLength = buffer.getInt();
			byte[] rawres = new byte[resLength];
			buffer.get(rawres, 0, resLength);
			stack.add(decoder.decodeResult(rawres));
		}
		return stack;
	}

}
