package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.transactions.metabase.IMBTransactionCapabilities;

/**
 * This class provides API for operations on metabase objects representing procedures.
 * 
 * @author raist, edek (transaction support)
 */
public class MBProcedure extends MBObject {
	/**
	 * Initializes a new MBProcedure object with no transaction support
	 * 
	 * @param oid
	 *           OID of an existing procedure object
	 */
	public MBProcedure(OID oid) throws DatabaseException {
		this(oid, null);
	}

	/**
	 * Initializes a new transaction capable MBProcedure
	 * 
	 * @param oid
	 * @param capsMBTransaction
	 * @throws DatabaseException
	 */
	public MBProcedure(OID oid, IMBTransactionCapabilities capsMBTransaction) throws DatabaseException {
		super(oid, capsMBTransaction);
		if (ConfigDebug.ASSERTS) {
		     assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
		}
	}

	/**
	 * Initializes the procedure in the metabase
	 * 
	 * @param typenameid
	 * @param mincard
	 *           minimum cardinality of the result
	 * @param maxcard
	 *           maximum cardinality of the result
	 * @param ref
	 *           reference indicator (number of &'s)
	 * @param argbuf
	 *           size of the buffer for arguments (can be 0)
	 * @param astBody
	 *           AST of the procedure body serialized using Java serialization
	 */
	public void initialize(int typenameid, int mincard, int maxcard, int ref, int argbuf, byte[] astBody) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), this.oid, MetaObjectKind.PROCEDURE_OBJECT.kindAsInt());
		store.createIntegerObject(store.addName(Names.namesstr[Names.MIN_CARD_ID]), this.oid, mincard);
		store.createIntegerObject(store.addName(Names.namesstr[Names.MAX_CARD_ID]), this.oid, maxcard);
		store.createIntegerObject(store.addName(Names.namesstr[Names.TYPEID_ID]), oid, typenameid);
		store.createIntegerObject(store.addName(Names.namesstr[Names.REFERENCE_ID]), oid, ref);
		store.createComplexObject(store.addName(Names.namesstr[Names.ARGUMENTS_ID]), oid, argbuf);
		store.createComplexObject(store.addName(Names.namesstr[Names.LOCALS_ID]), oid, 0);
		store.createComplexObject(store.addName(Names.namesstr[Names.CATCH_ID]), oid, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.AST_ID]), oid, astBody, 0);
	}

	/**
	 * Verify whether the OID passed while instantiating MBProcedure.
	 * 
	 * @return true if the oid really represents a procedure.
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.PROCEDURE_OBJECT;
	}

	/**
	 * Registers a new argument of the procedure
	 * 
	 * @param name
	 *           name of the argument
	 * @param type
	 *           type of the procedure
	 * @param minimum
	 *           cardinality of the argument
	 * @param maximum
	 *           cardinality of the argument
	 * @param ref
	 *           reference indicator (number of &'s)
	 * @return OID of the meta variable describing the argument
	 */

	// FIXME type as nameid
	OID addArgument(String name, String type, int mincard, int maxcard, int ref) throws DatabaseException {

		MetaBase metaBase = getMetaBase();
		int typeid = metaBase.addMetaReference(type);

		OID strid = metaBase.createComplexObject(name, getArgumentsRef(), MBVariable.FIELD_COUNT);
		new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);

		return strid;
	}

	/**
	 * @param name - name of the local block
	 * @return - oid of the local block
	 * @throws DatabaseException
	 */
	OID addLocalBlock(String name) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		return metaBase.createComplexObject(name, this.getLocalsRef(), 0);
	}
	/**
	 * @param name - name of the catch block
	 * @return the oid of the new catch block
	 * @throws DatabaseException
	 */
	OID addCatchBlock(String name) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		OID blockid = metaBase.createComplexObject(name, this.getCatchedExceptionsRef(), 0);
		
		return blockid;
	}
	/**
	 * @param name
	 * @return entry to the local block
	 * @throws DatabaseException
	 */
	public OID getLocalBlockEntry(String name) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		return metaBase.findFirstByName(name, this.getLocalsRef());
	}
	/**
	 * @param name
	 * @return true if name is na name of a catch block, false otherwise
	 * @throws DatabaseException
	 */
	public boolean isCatchBlock(String name) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		return metaBase.findFirstByName(name, this.getCatchedExceptionsRef()) != null;		
		
	}
	public OID[] getLocalBlocksEntries() throws DatabaseException {

		return this.getLocalsRef().derefComplex();
	}
	
	public OID[] getExceptionsBlocksEntries() throws DatabaseException {

		return this.getCatchedExceptionsRef().derefComplex();
	}
	
	public OID getCatchBlockExceptionVariable(String name) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		OID block = metaBase.findFirstByName(name, this.getCatchedExceptionsRef());
		assert block != null : "no catch block";
		return block.derefComplex()[0];
	}
	OID addLocalVariable(String blockname, String name, String type, int mincard, int maxcard, int ref)
				throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		OID block = metaBase.findFirstByName(blockname, this.getLocalsRef());
		
		assert block != null : "undefined local block:" + blockname;
		

		int typeid = metaBase.addMetaReference(type);

		OID strid = metaBase.createComplexObject(name, block, MBVariable.FIELD_COUNT);
		new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);

		return strid;
	}
	
	OID addCatchedExceptionVariable(String blockname, String name, String type, int mincard, int maxcard, int ref)
	throws DatabaseException {
	    MetaBase metaBase = getMetaBase();
	    OID blockid = metaBase.findFirstByName(blockname, this.getCatchedExceptionsRef());
		
	    assert blockid != null : "undefined catch block: " + blockname;
	    
	    int typeid = metaBase.addMetaReference(type);
	    OID strid = metaBase.createComplexObject(name, blockid, MBVariable.FIELD_COUNT);
	    new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);
	    return strid;
	    
	}

	/**
	 * @return oids of meta variables representing procedure parameters
	 */
	public OID[] getArguments() throws DatabaseException {
		return getArgumentsRef().derefComplex();
	}

	/**
	 * @return minimum cardinality of the result
	 */
	public int getMinCard() throws DatabaseException {
		return getMinCardRef().derefInt();
	}

	/**
	 * @return maximum cardinality of the result
	 */
	public int getMaxCard() throws DatabaseException {
		return getMaxCardRef().derefInt();
	}

	/**
	 * @return reference indicator of the variable returned by the procedure (e.g. x() : ref integer)
	 */
	public int getRefIndicator() throws DatabaseException {
		int ref = getReferencesRef().derefInt();
		MBVariable var = new MBVariable(this.getType());
		while (var.isValid()) {
			ref++;
			var = new MBVariable(var.getType());
		}

		return ref;
	}

	/**
	 * sets value of the procedure "reference indicator" subobject (e.g. if no & => 0, if &integer => 1, if &&integer =>
	 * 2, etc.)
	 */
	public void setRefIndicator(int level) throws DatabaseException {
		getReferencesRef().updateIntegerObject(level);
	}

	/**
	 * @return position of the type name in the list of logical/physical module references
	 */
	public int getTypeNameId() throws DatabaseException {
		return getTypeRef().derefInt();
	}

	/**
	 * @return name of the type
	 */
	public String getTypeName() throws DatabaseException {
		return getMetaBase().getMetaReferenceAt(getTypeNameId()).derefString();
	}

	/**
	 * @return AST of the serialized procedure's body
	 */
	public byte[] getAST() throws DatabaseException {
		return getASTRef().derefBinary();
	}

	/**
	 * @param val
	 *           new AST of the procedure
	 */
	public void setAST(byte[] val) throws DatabaseException {
		getASTRef().updateBinaryObject(val);
	}

	/**
	 * Can only be used if the module has been linked.
	 * 
	 * @return type of the procedure result.
	 */
	public OID getType() throws DatabaseException {
	    DBModule module = getModule();

		if (ConfigDebug.ASSERTS)
			assert module.isModuleLinked() : "uncompiled module";

		int typeid = getTypeNameId();
		OID typeoid = getMetaBase().getCompiledMetaReferenceAt(typeid).derefReference();
		MBClass cls = new MBClass(typeoid);
		if(cls.isValid() && this.getReferencesRef().derefInt() == 1 )
		    return cls.getDefaultVariable();
		return typeoid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.db.objects.meta.MBObject#getNestedMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
		return getLocalBlocksEntries();
	}

	

	/********************************************************************************************************************
	 * debugging
	 */

	public String dump(String indend) throws DatabaseException {

		int typeid = oid.getChildAt(TYPEID_POS).derefInt();
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
		metastr += " [" + oid.getChildAt(MINCARD_POS).derefInt() + ".." + oid.getChildAt(MAXCARD_POS).derefInt() + "]";
		metastr += " : &" + oid.getChildAt(REFERENCE_POS).derefInt() + " #" + typeid + " (" + getTypeName()
					+ ") [procedure]\n";

		OID[] args = getArguments();

		for (int i = 0; i < args.length; i++) {
			metastr += new MBVariable(args[i]).dump(indend + " arg. " + i + ": ");
		}

		for (OID local : getLocalBlocksEntries()) {
		    	
			metastr += "\t\t\t\t" + indend + " blck: (" + local.getObjectName() + ")\n";
			for (OID locvar : local.derefComplex()) {			    	
			    metastr += new MBVariable(locvar).dump(indend + "  loc.: ");
			}
		}
		for(OID catchBlock: this.getCatchedExceptionsRef().derefComplex()){
		    	metastr += new MBVariable(catchBlock.derefComplex()[0]).dump(indend + "  exc.: ");		    	
		}
		return metastr;
	}

	/********************************************************************************************************************
	 * access to subobjects representing fields of the procedure
	 */

	private final OID getMinCardRef() throws DatabaseException {
		return oid.getChildAt(MINCARD_POS);
	}

	private final OID getMaxCardRef() throws DatabaseException {
		return oid.getChildAt(MAXCARD_POS);
	}

	private final OID getTypeRef() throws DatabaseException {
		return oid.getChildAt(TYPEID_POS);
	}

	private final OID getReferencesRef() throws DatabaseException {
		return oid.getChildAt(REFERENCE_POS);
	}

	private final OID getArgumentsRef() throws DatabaseException {
		return oid.getChildAt(ARGUMENTS_POS);
	}

	private final OID getLocalsRef() throws DatabaseException {
		return oid.getChildAt(LOCALS_POS);
	}
	private final OID getCatchedExceptionsRef() throws DatabaseException {
		return oid.getChildAt(CATCHED_EXCEPTIONS_POS);
	}
	private final OID getASTRef() throws DatabaseException {
		return oid.getChildAt(AST_POS);
	}

	protected final static int MINCARD_POS = 1;

	protected final static int MAXCARD_POS = 2;

	protected final static int TYPEID_POS = 3;

	protected final static int REFERENCE_POS = 4;

	protected final static int ARGUMENTS_POS = 5;

	protected final static int LOCALS_POS = 6;
	
	protected final static int CATCHED_EXCEPTIONS_POS = 7;

	protected final static int AST_POS = 8;

	public final static int FIELD_COUNT = 9;
	
	
}