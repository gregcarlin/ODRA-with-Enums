/**
 * 
 */
package odra.sbql.interpreter.exceptions;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;

/**
 * ExceptionTable
 * the runtime representation of procedure catch blocks
 * 
 * @author Radek Adamus
 *@since 2007-09-24
 *last modified: 2007-09-24
 *@version 1.0
 */
public class ExceptionTable {
    private TryCatchBlock[] exceptions;
    
    public ExceptionTable(byte[] buf) {
    	this.getFromBytes(buf);
    }
    
    private void getFromBytes(byte[] buf){
		ByteBuffer buffer = ByteBuffer.wrap(buf);
		int rows = buffer.getInt();
		int tryNumber = buffer.getInt();
		this.exceptions = new TryCatchBlock[rows];
		int exceptionCounter = 0;
		for(int i = 0 ; i < tryNumber; i++){
		    int from = buffer.getInt();
		    int to = buffer.getInt();
		    int level = buffer.getInt();
		    int catchNumber = buffer.getInt();
		    for(int j = 0; j < catchNumber; j++){
			int label = buffer.getInt();
			int offset = buffer.getInt();
			this.exceptions[exceptionCounter] = new TryCatchBlock(from, to, level, label, offset);
			exceptionCounter++;
	    }
	}
	
    }
    public String getAsString(){
	    StringBuffer buffer = new StringBuffer();
	    for(TryCatchBlock ex : this.exceptions){
	    	buffer.append(ex.getAsString());
	    	buffer.append(NEW_LINE);		
	    }
	    return buffer.toString();
    }
    
    /**
     * @param offset
     * @return list of trycatch block avaliable for an exception
     * thrown at the given offset 
     */
    public List<TryCatchBlock> getHandlersForOffset(int offset){
    	Vector<TryCatchBlock> excs = new Vector<TryCatchBlock>();
    	for(TryCatchBlock exc : this.exceptions){
    		if(exc.getFrom() < offset && exc.getTo() > offset ){
    			excs.add(exc);
    		}
    	}
    	return excs;
    }
    
    /**
     * TryCatchBlock
     * @author Radek Adamus
     *@since 2007-09-26
     *last modified: 2007-09-26
     *@version 1.0
     */
    public static class TryCatchBlock{
	private int from;
	private int to;
	private int level;
	private int exceptionClassNameid;
	private int offset;
	
	/**
	 * @param from
	 * @param to
	 * @param exceptionClassNameid
	 * @param offset
	 * @param level
	 */
	private TryCatchBlock(int from, int to, int level, int exceptionClassNameid, int offset) {
	    this.from = from;
	    this.to = to;
	    this.exceptionClassNameid = exceptionClassNameid;
	    this.offset = offset;
	    this.level = level;
	}
	/**
	 * @return the from
	 */
	int getFrom()
	{
	    return from;
	}
	/**
	 * @return the to
	 */
	int getTo()
	{
	    return to;
	}
	/**
	 * @return the exceptionNameid
	 */
	public int getExceptionClassNameid()
	{
	    return exceptionClassNameid;
	}
	/**
	 * @return true if the trycatch block catch any exception
	 */
	public boolean isCatchAnyBlock(){
	    return this.exceptionClassNameid == CATCH_ALL;
	}
	/**
	 * @return the starting offset of a catch code 
	 * 
	 */
	public int getOffset()
	{
	    return offset;
	}
	/**
	 * @return the level
	 */
	public int getLevel()
	{
	    return level;
	}
	
	public String getAsString(){
	    
	    try
		{
		    String exceptionName;
		    if(exceptionClassNameid == CATCH_ALL)
		    	exceptionName = "all";
		    else
		    	exceptionName = Database.getNameIndex().id2name(exceptionClassNameid);
		    return "_catch " + exceptionName + " from: " + from + " to: " + to + " using: " + offset;
		    
		} catch (DatabaseException e)
		{
		    assert false : e.getMessage();
		    e.printStackTrace();
		}
		
		return "";
	}
	 private static final int CATCH_ALL = -1;
    }
    
    private static final String NEW_LINE = System.getProperty("line.separator");
   
}
