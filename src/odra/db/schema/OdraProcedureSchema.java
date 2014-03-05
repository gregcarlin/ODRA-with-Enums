package odra.db.schema;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import odra.system.config.ConfigDebug;
import odra.transactions.ITransactionCapabilitiesContainer;
import odra.transactions.ast.IASTTransactionCapabilities;

/**
 * OdraProcedureSchema
 * Transfer object to convey the information about 
 * Procedure in the store independent format
 * @author radamus, edek (transaction capabilities)
 *last modified: 2008-05-03 renamed & moved 
 */

public class OdraProcedureSchema extends OdraObjectSchema implements ITransactionCapabilitiesContainer {


	

	private OdraProcedureHeaderSchema header;
	
	private ProcedureAST ast;
	private BinaryCodeInfo bincode;

	private final IASTTransactionCapabilities capsTransaction;

	private Map<String, Vector<OdraVariableSchema>> locals = new Hashtable<String, Vector<OdraVariableSchema>>();
	private Map<String, OdraVariableSchema> exceptions = new Hashtable<String, OdraVariableSchema>();

	

	
	public OdraProcedureSchema(String pname, ProcArgument[] args, ProcedureAST ast,
		OdraTypeSchema res) {
	    this(pname, args, ast, new BinaryCodeInfo(), res, null);               
	}
	/**
	 * @param pname
	 * @param capsTransaction
	 */
	public OdraProcedureSchema(String pname,
		IASTTransactionCapabilities capsTransaction) {
	    this(pname, new ProcArgument[0], new ProcedureAST(), new BinaryCodeInfo(),null, capsTransaction);
	}

	
	
	
	public OdraProcedureSchema(String pname, ProcArgument[] args, ProcedureAST ast, BinaryCodeInfo bincode,
				OdraTypeSchema res, IASTTransactionCapabilities capsTransaction) {
		super(pname);
		this.ast = ast;
		this.bincode = bincode;
		this.header = new OdraProcedureHeaderSchema(pname, args, res);		
		this.capsTransaction = capsTransaction;
	}
	
	
	
	public OdraProcedureSchema(String pname, ProcArgument[] args, ProcedureAST ast, BinaryCodeInfo bincode,
				OdraTypeSchema res) {
		this(pname, args, ast, bincode,res, null);
	}	
	

	/**
	 * @param name
	 * @param typeName
	 * @param args
	 * @param astBody
	 * @param min
	 * @param max
	 * @param refs
	 */
	public OdraProcedureSchema(String name, String typeName,
		ProcArgument[] args, byte[] astBody, int min, int max, int refs) {
	    this(name, args, new ProcedureAST(astBody), new BinaryCodeInfo(),new OdraTypeSchema(typeName,min,max,refs), null);
	}
	/**
	 * 
	 */
	public OdraProcedureSchema() {
	    this("", new ProcArgument[0], new ProcedureAST(), new BinaryCodeInfo(),new OdraTypeSchema("void",1,1,0), null);
	}
	public void insertLocalBlock(String blockName) {
		Vector<OdraVariableSchema> prev = locals.put(blockName, new Vector<OdraVariableSchema>());
		if (ConfigDebug.ASSERTS) {
			assert prev == null : "block already defined";
		}
	}

	public void insertLocalVariable(String blockName, OdraVariableSchema varinfo) {
		Vector<OdraVariableSchema> blockvariables = this.locals.get(blockName);

		if (blockvariables == null) {
			blockvariables = new Vector<OdraVariableSchema>();
			this.locals.put(blockName, blockvariables);
		}
		blockvariables.add(varinfo);
	}
	
	public void insertCatchBlockExceptionVariable(String blockName, OdraVariableSchema varinfo) {
		assert this.exceptions.get(blockName) == null : "catch block can catch only one exception";

		this.exceptions.put(blockName, varinfo);
	}
	
	public IASTTransactionCapabilities getASTTransactionCapabilities() {
		return this.capsTransaction;
	}
	
	/**
	 * @return the ast as byte[]
	 */
	public byte[] getAstAsBytes() {
	    return this.ast.getAsBytes();
	}

	
	/**
	 * @return the obj
	 */
	public byte[] getDebug() {
	    return this.bincode.getDebug();
	}

	/**
	 * @return the constants
	 */
	public byte[] getConstants() {
	    return this.bincode.getConstants();
	}

	/**
	 * @return the args
	 */
	public ProcArgument[] getArgs() {
	    return header.getArguments();
	}

	/**
	 * @return the maxcard
	 */
	public int getMaxcard() {
	    return header.getResult().getMaxCard();
	}

	/**
	 * @return the maxcard
	 */
	public int getMincard() {
	    return header.getResult().getMinCard();
	}
	
	/**
	 * @return the refs
	 */
	public int getRefs() {
	    return header.getResult().getRefs();
	}
	
	/**
	 * @return the refs
	 */
	public String getTypeName() {
	    return header.getResult().getTypeName();
	}
	
	/**
	 * @return the pname
	 */
	public String getPname() {
	    return this.getName();
	}

	/**
	 * @param ast the ast to set
	 */
	public void setAst(byte[] ast) {
	    this.ast.setAst(ast);
	}

	

	/**
	 * @param args the args to set
	 */
	public void setArgs(ProcArgument[] args) {
	    this.header.setArguments(args);
	}

	/**
	 * @param res the res to set
	 */
	public void setResult(OdraTypeSchema res) {
	    this.header.setResult(res);
	}
	
	public final static String MAIN_LOCAL_BLOCK_NAME = "$main";



	/**
	 * @return the locals
	 */
	public Map<String, Vector<OdraVariableSchema>> getLocals()
	{
	    return locals;
	}

	/**
	 * @return the catchParams
	 */
	public Map<String, OdraVariableSchema> getExceptions()
	{
	    return exceptions;
	}

	/**
	 * @return the catches
	 */
	public byte[] getCatches()
	{
	    return this.bincode.getCatches();
	}

	

	/**
	 * @return the bin
	 */
	public byte[] getBinary()
	{
	    return this.bincode.getBinary();
	}
	
	
	public static class BinaryCodeInfo {

	   
	    private byte[] bin;
	    private byte[] constants;
	    private byte[] catches;
	    private byte[] debug;
	    
	    public BinaryCodeInfo() {
		
		this.bin = new byte[0];
		this.constants = new byte[0];
		this.catches = new byte[0];
		this.debug = new byte[0];
	    }
	    /**
	     * @param debug
	     * @param bin
	     * @param constants
	     * @param catches
	     */
	    public BinaryCodeInfo(byte[] bin,
		    byte[] constants, byte[] catches, byte[] debug) {
		this.debug = debug;
		this.bin = bin;
		this.constants = constants;
		this.catches = catches;
	    }
	    /**
	     * @return the debug
	     */
	    public byte[] getDebug()
	    {
	        return debug;
	    }
	    /**
	     * @return the bin
	     */
	    public byte[] getBinary()
	    {
	        return bin;
	    }
	    /**
	     * @return the constants
	     */
	    public byte[] getConstants()
	    {
	        return constants;
	    }
	    /**
	     * @return the catches
	     */
	    public byte[] getCatches()
	    {
	        return catches;
	    }
	    /**
	     * @param debug the debug to set
	     */
	    public void setDebug(byte[] debug)
	    {
	        this.debug = debug;
	    }
	    /**
	     * @param bin the bin to set
	     */
	    public void setBinary(byte[] bin)
	    {
	        this.bin = bin;
	    }
	    /**
	     * @param constants the constants to set
	     */
	    public void setConstants(byte[] constants)
	    {
	        this.constants = constants;
	    }
	    /**
	     * @param catches the catches to set
	     */
	    public void setCatches(byte[] catches)
	    {
	        this.catches = catches;
	    }
	    
	    
	    
	}
	
	public static class ProcedureAST {
	    byte[] serializedAst;
	    
	    
	    public ProcedureAST() {
		serializedAst = new byte[0];
	    }
	    
	    /**
	     * @param serializedAst
	     */
	    public ProcedureAST(byte[] serializedAst) {
		this.serializedAst = serializedAst;
	    }
	    
	    /**
	     * @return the serializedAst
	     */
	    public byte[] getAsBytes()
	    {		
	        return serializedAst;
	    }
	    
	    /**
	     * @param serializedAst the serializedAst to set
	     */
	    public void setAst(byte[] serializedAst)
	    {
	        this.serializedAst = serializedAst;
	    }
	    
	    
	}

	/**
	 * @return the header
	 */
	public OdraProcedureHeaderSchema getProcedureHeader() {
	    return header;
	}
}