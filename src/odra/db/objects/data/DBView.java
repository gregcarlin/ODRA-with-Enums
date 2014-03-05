package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBProcedure;
import odra.db.schema.OdraViewSchema;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides an interface for database objects representing views.
 * 
 * @author raist
 */
public class DBView extends DBObject {
	/**
	 * Initializes the MBView object.
	 * @param oid OID of an existing view or an empty complex object
	 */
	public DBView(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	/**
	 * Initializes the view in the metabase by creating some system-level subobjects.
	 * @param - oid of the virtual object procedure
	 * @throws DatabaseException
	 */
	public void initialize(OID vobjprc) throws DatabaseException {		
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.VIEW_OBJECT);
		store.createComplexObject(store.addName(Names.namesstr[Names.GENPROCS_ID]), oid, 0);	
		store.createComplexObject(store.addName(Names.namesstr[Names.VIRTFLDS_ID]), oid, 0);	
		store.createComplexObject(store.addName(Names.namesstr[Names.FIELDS_ID]), oid, 0);
		store.createComplexObject(store.addName(Names.namesstr[Names.SUBVIEWS_ID]), oid, 0);
		store.createPointerObject(store.addName(Names.namesstr[Names.VIRTOBJREF_ID]), oid, vobjprc);
	}

	/**
	 * @return true if object's oid represents a valid view
	 * @throws DatabaseException
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.VIEW_OBJECT;
	}

	/**
	 * @return OID of the complex object which is used as a parent
	 * object of all fields (procedures, simple objects, etc) of the view
	 * @throws DatabaseException
	 */
	public OID getVirtualFieldsEntry() throws DatabaseException {
		return geVirtualFieldsRef();
	}
	
	/**
	 * @return OID of the complex object which is used as a parent
	 * object of all fields (procedures, simple objects, etc) of the view
	 * @throws DatabaseException
	 */
	public OID getViewFieldsEntry() throws DatabaseException {
		return geViewFieldsRef();
	}
	
	/**
	 * @return oids of this view sub views
	 * @throws DatabaseException
	 */
	public OID[] getSubViews() throws DatabaseException {
		return this.getSubViewsRef().derefComplex();
	}
	
	
	/**
	 * @param name - name of the generic procedure 
	 * @see OdraViewSchema
	 * @return the oid of the generic procedure (if found), null otherwise
	 * @throws DatabaseException
	 */
	public OID getGenericProcByName(String name) throws DatabaseException {
		OID[] procs = this.getGenPrcsRef().derefComplex();
		for(OID proc : procs){
			if(name.compareTo(proc.getObjectName()) == 0)
				return proc;
		}
		return null;
	}
	
	/**
	 * @return OID of the object being virtual object procedure
	 * @throws DatabaseException
	 */
	public OID getVirtualObject() throws DatabaseException {
		return getVirtualObjectRef().derefReference();
	}
	
	/**
	 * @param name - name of the subview
	 * @return the oid of the subview (if found), null otherwise
	 * @throws DatabaseException
	 */
	public OID getSubViewByName(String name) throws DatabaseException {
		OID[] sviews = this.getSubViewsRef().derefComplex();
		for(OID sview : sviews){
			if(name.compareTo(sview.getObjectName()) == 0)
				return sview;
		}
		return null;
	}
	/**
	 * Creates a new generic runtime procedure and connects it to the view.
	 * @param name name of the procedure
	 * @param astBody serialized AST of the body
	 * @param objBody intermediate code of the procedure (unused)
	 * @param binBody binary code of the procedure
	 * @param constants constant pool
	 * @param catches - description of catch blocks in the procedure
	 */	
	public OID createGenericProcedure(String name,byte[] objBody, byte[] binBody, byte[] constants, byte[] catches) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert name != null && binBody != null;

		OID procid = store.createComplexObject(store.addName(name), getGenPrcsRef(), MBProcedure.FIELD_COUNT);

		DBProcedure prc = new DBProcedure(procid);
		prc.initialize(objBody, binBody, constants, catches);

		return procid;
	}
	
	/**
	 * Creates a new runtime procedure and connects it to the view.
	 * @param name name of the procedure
	 * @param objBody intermediate code of the procedure (unused)
	 * @param binBody binary code of the procedure
	 * @param constants constant pool
	 * @param catches - description of catch blocks in the procedure
	 */	
	public OID createProcedureField(String name,byte[] objBody, byte[] binBody, byte[] constants, byte[] catches) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert name != null && binBody != null;

		OID procid = store.createComplexObject(store.addName(name), geViewFieldsRef(), MBProcedure.FIELD_COUNT);

		DBProcedure prc = new DBProcedure(procid);
		prc.initialize(objBody, binBody, constants, catches);

		return procid;
	}
	
	
	/**
	 * Creates a new subview.
	 * @param vwname - name of the new view
	 * @param voname - name of the virtual object defined by the view
	 * @param debugBody - debug code of the virtual objects procedure body
	 * @param binBody - binary code of the virtual objects procedure body
	 * @param cnst - list of constants used by virtual objects procedure
	 * @param catches - description of catch blocks in the virtual objects procedure
	 * @return - oid of the new subview
	 * @throws DatabaseException
	 */
	public OID createSubView(String vwname, String voname,  byte[] debugBody, byte[] binBody, byte[] cnst, byte[] catches) throws DatabaseException {		
		OID dbv = store.createComplexObject(store.addName(vwname), getSubViewsRef(), DBView.FIELD_COUNT);
		OID vop = store.createComplexObject(store.addName(voname), geVirtualFieldsRef(), DBVirtualObjectsProcedure.FIELD_COUNT);

		new DBView(dbv).initialize(vop);
		new DBVirtualObjectsProcedure(vop).initialize(debugBody, binBody, cnst, catches, dbv);

		return dbv;
	}

	/***********************************
	 * access to subobjects describing the declaration
	 * */
		
	private final OID getGenPrcsRef() throws DatabaseException {
		return oid.getChildAt(GENPRCS_POS);
	}	

	private final OID geVirtualFieldsRef() throws DatabaseException {
		return oid.getChildAt(VIRTUAL_FIELDS_POS);
	}	
	
	private final OID geViewFieldsRef() throws DatabaseException {
		return oid.getChildAt(VIEW_FIELDS_POS);
	}
	
	private final OID getSubViewsRef() throws DatabaseException {
		return oid.getChildAt(SUBVIEWS_POS);
	}
	
	private final OID getVirtualObjectRef() throws DatabaseException {
		return oid.getChildAt(VIRTUAL_OBJECT_POS);
	}
	
	private final static int GENPRCS_POS = 1;
	private final static int VIRTUAL_FIELDS_POS = 2;
	private final static int VIEW_FIELDS_POS = 3;
	private final static int SUBVIEWS_POS = 4;
	private final static int VIRTUAL_OBJECT_POS = 5;

	public final static int FIELD_COUNT = 6;
}
