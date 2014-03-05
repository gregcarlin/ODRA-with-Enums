package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for metabase objects representing variables.
 * 
 * @author raist
 */

public class MBVariable extends MBObject
{
	/**
	 * Initializes a new MBVariable object
	 * 
	 * @param oid
	 *            oid of an existing object
	 */
	public MBVariable(OID oid) throws DatabaseException
	{
		super(oid);

		if (ConfigDebug.ASSERTS)
		    assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the variable in the metabase by creating special system subobjects describing the
	 * variable declaration
	 * 
	 * @param typenameid
	 *            position in the list of metareferences
	 * @param mincard
	 *            minimum cardinality
	 * @param maxcard
	 *            maximum cardinality
	 * @param ref
	 *            reference indicator (number of &'s)
	 */
	public void initialize(int typenameid, int mincard, int maxcard, int ref) throws DatabaseException
	{
		this.initialize(typenameid, mincard, maxcard, ref, NO_REVERSE_NAME);
	}
	
	
	public void initialize(int typenameid, int mincard, int maxcard, int ref, int revnameid) throws DatabaseException
	{
	    	store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.VARIABLE_OBJECT.kindAsInt());
		store.createIntegerObject(store.addName(Names.namesstr[Names.MIN_CARD_ID]), oid, mincard);
		store.createIntegerObject(store.addName(Names.namesstr[Names.MAX_CARD_ID]), oid, maxcard);
		store.createIntegerObject(store.addName(Names.namesstr[Names.TYPEID_ID]), oid, typenameid);
		store.createIntegerObject(store.addName(Names.namesstr[Names.REFERENCE_ID]), oid, ref);
		store.createIntegerObject(store.addName(Names.namesstr[Names.REVERSEID_ID]), oid, revnameid);
	}
	/**
	 * @return true if the oid really represent a metavariable
	 */
	public boolean isValid() throws DatabaseException
	{
	    MetaObjectKind kind = getObjectKind(); 
		return kind == MetaObjectKind.VARIABLE_OBJECT || kind == MetaObjectKind.VIRTUAL_VARIABLE_OBJECT;
	}

	/**
	 * @return minimum cardinality
	 */
	public int getMinCard() throws DatabaseException
	{
		return getMinCardRef().derefInt();
	}

	/**
	 * @return maximum cardinality
	 */
	public int getMaxCard() throws DatabaseException
	{
		return getMaxCardRef().derefInt();
	}

	/**
	 * @return position of the type name in the list of logical/physical module references
	 */
	public int getTypeNameId() throws DatabaseException
	{
		return getTypeNameIdRef().derefInt();
	}

	/**
	 * @return name of the type
	 */
	public String getTypeName() throws DatabaseException
	{
		return getMetaBase().getMetaReferenceAt(getTypeNameId()).derefString();
	}

	/**
	 * @return type of the variable (valid only if the module has been linked)
	 */
	public OID getType() throws DatabaseException
	{
		DBModule module = getModule();

		if (ConfigDebug.ASSERTS)
			assert module.isModuleLinked() : "uncompiled module";

		int typeid = getTypeNameIdRef().derefInt();
		OID typeoid = getMetaBase().getCompiledMetaReferenceAt(typeid).derefReference();
		MBClass cls = new MBClass(typeoid);
		if(cls.isValid() && this.getReferenceRef().derefInt() == 1 )
		    return cls.getDefaultVariable();
		return typeoid;
	}

	/**
	 * @return reverse pointer of the variable (valid only if the module has been linked)
	 * null if the variable is not a pointer variable and has not a reverse pointer
	 */
	public OID getReversePointer() throws DatabaseException{
	    if(this.hasReverseReference()){
		
		return getMetaBase().getCompiledMetaReferenceAt(this.getReverseNameIdRef().derefInt()).derefReference();
	    }
	    return null;
	}
	
	/**
	 * @return name of the reverse pointer (or "" if not exists)
	 */
	public String getReverseName() throws DatabaseException{
	    if(this.hasReverseReference()){
		return getMetaBase().getCompiledMetaReferenceAt(this.getReverseNameIdRef().derefInt()).derefReference().getObjectName(); 
	    }
	    return "";
	}
	
	/**
	 * @return position of the reverse pointer name in the list of logical/physical module references
	 */
	public int getReverseNameId() throws DatabaseException{	    
	    if(this.hasReverseReference()){
		return getMetaBase().getCompiledMetaReferenceAt(this.getReverseNameIdRef().derefInt()).derefReference().getObjectNameId();
	    }
	     return NO_REVERSE_NAME;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.db.objects.meta.MBObject#getNestedMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException
	{
	    if(this.getRefIndicator() > 0)
		return new OID[0];
	    
	    return MBObjectFactory.getTypedMBObject(getType()).getNestedMetabaseEntries();
	}

	/**
	 * @return value of the "reference" subobject (e.g. if no & => 0, if &integer => 1, if &&integer =>
	 *         2, etc.)
	 */
	public int getRefIndicator() throws DatabaseException
	{
		int ref = getReferenceRef().derefInt();
		MBVariable var = this;
		while(var.isTypeReference()){
		    ref ++;
		    var = new MBVariable(var.getType());
		    if(!var.isValid())
			break;
		}
		
		return ref;
	}

	/**
	 * sets value of the "reference" subobject (e.g. if no & => 0, if &integer => 1, if &&integer =>
	 * 2, etc.)
	 */
	public void setRefIndicator(int level) throws DatabaseException
	{
		getReferenceRef().updateIntegerObject(level);
	}

	/**
	 * @return true if the the type of the variable is a primitive object
	 */
	public boolean isTypePrimitive() throws DatabaseException
	{
		return new MBPrimitiveType(getType()).isValid();
	}

	/**
	 * @return true if the type of the variable is a class
	 */
	public boolean isTypeClass() throws DatabaseException
	{
	    //it might be a reference to a class instance 
	    if(this.getReferenceRef().derefInt() != 0)
		return false;
	    return new MBClass(getType()).isValid();
	}
	
	/**
	 * @return true if the type of the variable is an enum
	 */
	public boolean isTypeEnum() throws DatabaseException
	{
		return new MBEnum(getType()).isValid();
	}

	/**
	 * @return true if the type of the variable is a structure
	 */
	public boolean isTypeStruct() throws DatabaseException
	{
		return new MBStruct(getType()).isValid();
	}

	/**
	 * @return true if the type of the variable is a typedef
	 */
	public boolean isTypeTypeDef() throws DatabaseException
	{
		return new MBTypeDef(getType()).isValid();
	}

	
	/**
	 * @return true if the variable is a reference to other variable
	 */
	public boolean isTypeReference() throws DatabaseException
	{
		return new MBVariable(getType()).isValid() || new MBProcedure(getType()).isValid();// || this.isTypeClassReference();
	}
	
	/**
	 * @return true if the variable is a reference to class (for UML compatibility)
	 * the reference to mb class is treated as reference to variable (if ref indicator > 0) 
	 */
	public boolean isTypeClassReference() throws DatabaseException
	{
		return (new MBClass(this.getType()).isValid()) && (this.getReferenceRef().derefInt() > 0);
	}
	
	/**
	 * @return true if the type of the variable is a view definition - i.e. this is a virtual variable
	 */
	public boolean isVirtual() throws DatabaseException
	{
		return new MBView(getType()).isValid();
	}
	
	/**
	 * @return true is the variable is a pointer variable and has reverse pointer 
	 * @throws DatabaseException
	 */
	public boolean hasReverseReference()  throws DatabaseException
	{
	    return this.getReverseNameIdRef().derefInt() != NO_REVERSE_NAME;   
	}
	
	
	/***********************************************************************************************
	 * debugging
	 */

	public String dump(String indend) throws DatabaseException
	{
		int typeid = oid.getChildAt(TYPENAMEID_POS).derefInt();

		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
		metastr += " [" + oid.getChildAt(MINCARD_POS).derefInt() + ".." + oid.getChildAt(MAXCARD_POS).derefInt() + "]";
		metastr += " : &" + oid.getChildAt(REFERENCE_POS).derefInt() + " #" + typeid + " (" + getTypeName() + ") [variable]\n";

		return metastr;
	}

	/***********************************************************************************************
	 * access to subobjects describing the declaration
	 */

	private OID getMinCardRef() throws DatabaseException
	{
		return oid.getChildAt(MINCARD_POS);
	}

	private OID getMaxCardRef() throws DatabaseException
	{
		return oid.getChildAt(MAXCARD_POS);
	}

	private OID getTypeNameIdRef() throws DatabaseException
	{
		return oid.getChildAt(TYPENAMEID_POS);
	}

	private OID getReferenceRef() throws DatabaseException
	{
		return oid.getChildAt(REFERENCE_POS);
	}
	private OID getReverseNameIdRef() throws DatabaseException
	{
		return oid.getChildAt(REVERSENAMEID_POS);
	}
	
	private final static int MINCARD_POS = 1;

	private final static int MAXCARD_POS = 2;

	private final static int TYPENAMEID_POS = 3;

	private final static int REFERENCE_POS = 4;
	private final static int REVERSENAMEID_POS = 5;

	public final static int FIELD_COUNT = 6;
	
	public final static int NO_REVERSE_NAME = -1;
}
