/**
 * 
 */
package odra.sbql.emiter.exceptions;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.instructions.Instruction;
import odra.system.Sizes;

/**
 * CompiletimeCatchTable
 * 
 * @author Radek Adamus
 * @since 2007-09-24 last modified: 2007-09-24
 * @version 1.0
 */
public class CompiletimeExceptionTable {
    Vector<TryBlockData> tryBlocks = new Vector<TryBlockData>();
    Map<Instruction, TryBlockData> tryBlocksMap = new HashMap<Instruction, TryBlockData>();

    public void insertTryBlock(JulietCode trycode, int level){
	TryBlockData tryBlock = tryBlocksMap.get(trycode.getStart());
	assert tryBlock == null: "try block exists";
	tryBlock = new TryBlockData(trycode);
	tryBlock.level = level;
	tryBlocksMap.put(trycode.getStart(), tryBlock);
	tryBlocks.add(tryBlock);
    }
    public void insertCatchForException( JulietCode trycode, JulietCode catchBlockCode, int exceptionNameid){
	TryBlockData tryBlock = tryBlocksMap.get(trycode.getStart());
	assert tryBlock != null: "try block not exists";
	tryBlock.addCatchBlock(new CatchBlockData(exceptionNameid, catchBlockCode));
	
    }
    
    
    public void insertFinally( JulietCode trycode, JulietCode finallBlockCode){
	TryBlockData tryBlock = tryBlocksMap.get(trycode.getStart());
	assert tryBlock != null: "try block not exists";
	
	//finall code must be also called when the exception is thrown inside the catch block  
	for(CatchBlockData catche : tryBlock.getCatches()){
	    this.insertTryBlock(catche.getCode(), tryBlock.level);
	    this.insertCatchForException(catche.getCode(), finallBlockCode, CATCH_ALL);
	}
	this.insertCatchForException(trycode, finallBlockCode, CATCH_ALL);
	
    }
    
    public byte[] getAsBytes(){
	//compute the length of the buffer
	int length = 0;
	int rows = 0;
	for(TryBlockData tb : tryBlocks){
	    length += 4* Sizes.INTVAL_LEN; 
	    length += (CatchBlockData.CATCHBLOCK_LENGTH * tb.getCatches().size());
	    rows += tb.getCatches().size();
	}
	length += 2* Sizes.INTVAL_LEN;
	ByteBuffer buffer = ByteBuffer.allocate(length);
	//number of all catches (rows in the catch table)
	buffer.putInt(rows);
	//number of try blocks
	buffer.putInt(tryBlocks.size());
	
	//foreach try block
	for(TryBlockData tb : tryBlocks){
	    buffer.putInt(tb.getStartInstruction().getIndex());
	    buffer.putInt(tb.getEndInstruction().getIndex());
	    buffer.putInt(tb.level);
	    
	    Vector<CatchBlockData> catchblocks = tb.getCatches();
		  //number of catch blocks
	    buffer.putInt(catchblocks.size());
		    //foreach catch block
	    for(CatchBlockData catchblock : catchblocks){
		buffer.putInt(catchblock.getLabel());
		buffer.putInt(catchblock.getStartInstruction().getIndex());
	    }
	}
	buffer.flip();
	return buffer.array();
    }
    
    public String getAsString(){
	StringBuffer buffer = new StringBuffer();
	for(TryBlockData tbd : this.tryBlocks){
	    for(CatchBlockData cbd : tbd.getCatches()){
		try
		{
		    String exceptionName;
		    if(cbd.getLabel() == CATCH_ALL)
			exceptionName =  "all"; 
		    else
			exceptionName =   Database.getNameIndex().id2name(cbd.label) ;
		    buffer.append("_catch " + exceptionName + " from: " +tbd.getStartInstruction() +" to: " + tbd.getEndInstruction() + " using: " + cbd.getStartInstruction());
		    buffer.append(NEW_LINE);
		    
		} catch (DatabaseException e)
		{
		    
		    e.printStackTrace();
		}
	    }
	}
	return buffer.toString();
    }
    
    private static class TryBlockData {
	private JulietCode code;
	private int level;
	private Vector<CatchBlockData> catches = new Vector<CatchBlockData>();

	/**
	 * @param from
	 * @param to
	 */
	public TryBlockData(JulietCode code) {
	    this.code = code;
	}

	/**
	 * @return the catches
	 */
	public Vector<CatchBlockData> getCatches()
	{
	    return catches;
	}

	public void addCatchBlock(CatchBlockData cb)
	{
	    this.catches.add(cb);
	}
	
	/**
	 * @return the offset
	 */
	public Instruction getStartInstruction()
	{
	    return code.getStart();
	}
	
	/**
	 * @return the offset
	 */
	public Instruction getEndInstruction()
	{
	    return code.getEnd();
	}
    }

    public static class CatchBlockData {

	private int label;
	private JulietCode code;

	/**
	 * @param label
	 * @param offset
	 */
	private CatchBlockData(int label, JulietCode code) {
	    this.label = label;
	    this.code = code;
	}

	private static final int CATCHBLOCK_LENGTH = 2 * Sizes.INTVAL_LEN;

	/**
	 * @return the label
	 */
	public int getLabel()
	{
	    return label;
	}

	/**
	 * @return the offset
	 */
	public Instruction getStartInstruction()
	{
	    return code.getStart();
	}
	
	/**
	 * @return the offset
	 */
	public Instruction getEndInstruction()
	{
	    return code.getEnd();
	}

	/**
	 * @return the code
	 */
	public JulietCode getCode()
	{
	    return code;
	}
    }

    private static String NEW_LINE = System.getProperty("line.separator");
    private static int CATCH_ALL = -1;
}
