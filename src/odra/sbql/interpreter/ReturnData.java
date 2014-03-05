package odra.sbql.interpreter;

import java.nio.ByteBuffer;

import odra.sbql.emiter.ConstantPool;

/**
 * ProcedureData
 * @author Radek Adamus
 *last modified: 2007-09-21
 *@version 1.0
 */
public class ReturnData {
	private ByteBuffer returnToCode;
	private ConstantPool pool;
	public ReturnData(ByteBuffer code, ConstantPool pool) {
		this.returnToCode = code; this.pool = pool; 
	}
	
	/**
	 * @return the returnToCode
	 */
	public ByteBuffer getReturnToCode()
	{
	    return returnToCode;
	}
	
	/**
	 * @return the pool
	 */
	public ConstantPool getPool()
	{
	    return pool;
	}
	
}
