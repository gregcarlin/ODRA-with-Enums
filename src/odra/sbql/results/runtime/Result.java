package odra.sbql.results.runtime;

import odra.sbql.results.AbstractQueryResult;

public abstract class Result extends AbstractQueryResult {
	public abstract SingleResult[] elementsToArray(); 
	
	public abstract int elementsCount();
	
	public abstract SingleResult elementAt(int i);
	
	public final static byte BAG_RESULT = 1;
	public final static byte BINDER_RESULT = 2;
	public final static byte BOOLEAN_RESULT = 3;
	public final static byte DOUBLE_RESULT = 4;
	public final static byte INTEGER_RESULT = 5;
	public final static byte LOCAL_REFERENCE_RESULT = 6;
	public final static byte REMOTE_REFERENCE_RESULT = 7;
	public final static byte STRING_RESULT = 8;
	public final static byte STRUCT_RESULT = 9;
	public final static byte VIRTUAL_REFERENCE_RESULT = 10;
	public final static byte DATE_RESULT = 11;
	public final static byte REMOTE_VIRTUAL_REFERENCE_RESULT = 12;
	public final static byte REMOTE_REFERENCE_RESULT_P2P = 13;
	public final static byte REMOTE_VIRTUAL_REFERENCE_RESULT_P2P = 14;

}
