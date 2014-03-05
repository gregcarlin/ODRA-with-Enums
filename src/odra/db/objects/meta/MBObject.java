package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sbql.builder.ModuleLinker;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.transactions.ITransactionCapabilities;
import odra.transactions.metabase.IMBTransactionCapabilities;
import odra.transactions.metabase.ITransactionCapableMBObject;
import odra.transactions.metabase.TransactionCapableMBObject;

/**
 * This is a parent class of all metabase objects.
 * 
 * @author raist, edek (transaction capable)
 */

public class MBObject implements ITransactionCapableMBObject {
	protected final OID oid;

	protected final IDataStore store;

	private DBModule module;

	private ITransactionCapableMBObject transMBObjectImpl;

	/**
	 * Initializes a new MBObject object using an OID with no transaction support
	 * 
	 * @param oid
	 *           OID of an existing object
	 */
	public MBObject(OID oid) {
		this(oid, null);
	}

	/**
	 * Initializes a new transaction capable MBObject
	 * 
	 * @param oid
	 * @param capsMBTransaction
	 */
	public MBObject(OID oid, IMBTransactionCapabilities capsMBTransaction) {
		if (ConfigDebug.ASSERTS) {
			assert oid != null : "oid == null";
		}
		this.oid = oid;
		this.store = oid.getStore();
		this.transMBObjectImpl = TransactionCapableMBObject.getInstance(this, capsMBTransaction);
	}

	/**
	 * @return Oid of the metaobject
	 */
	public OID getOID() {
		return oid;
	}

	/**
	 * @return Name of the metaobject
	 */
	public String getName() throws DatabaseException {
		return oid.getObjectName();
	}

	/**
	 * @return Name ID of the metaobject
	 */
	public int getNameId() throws DatabaseException {
		return oid.getObjectNameId();
	}

	/**
	 * @return Minimum cardinality
	 */
	public int getMinCard() throws DatabaseException {
		// get all subobjects
		OID[] subobj = oid.derefComplex();

		// find the $mincard subobject
		for (OID i : subobj) {
			if (i.getObjectNameId() == store.getNameId(Names.namesstr[Names.MIN_CARD_ID])) return i.derefInt();
		}

		// if none found, return default (1)
		return 1;
	}

	/**
	 * @return Maximum cardinality
	 */
	public int getMaxCard() throws DatabaseException {
		// get all subobjects
		OID[] subobj = oid.derefComplex();

		// find the $maxcard subobject
		for (OID i : subobj) {
			if (i.getObjectNameId() == store.getNameId(Names.namesstr[Names.MAX_CARD_ID])) return i.derefInt();
		}

		// if none found, return default (1)
		return 1;
	}

	/**
	 * @return kind of the object (e.g. if it is MBProcedure, MBClass, etc.)
	 */
	public MetaObjectKind getObjectKind() throws DatabaseException {
		if (oid.isComplexObject()) {
			OID ch0 = oid.getChildAt(0);

			if (ch0.getObjectName().equals(Names.namesstr[Names.KIND_ID])){
			    MetaObjectKind kind = MetaObjectKind.getKindForInteger(ch0.derefInt());
			    if(kind != null)
			    return kind;
			}
		}

		return MetaObjectKind.UNKNOWN_OBJECT;
	}

	/**
	 * some MBObject (eg. MBStruct) types posses "nested" meta-objects (not accessible through metabase entry) his method
	 * provides generic interface to those meta-objects mainly used to allow creating meta - references to sub-objects
	 * 
	 * @return oid[] of nested metabase entries (redefined in selected subclasses)
	 * @see ModuleLinker.bindMetaReferences
	 */
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
		return new OID[0];
	}

	/**
	 * Goes up the tree of objects and finds the module to which the object belongs. Since it is a slow operation, it
	 * shouldn't be used too often.
	 * 
	 * @return module that has been found
	 */
	public DBModule getModule() throws DatabaseException {
		if (module == null) {
			OID moid = oid;

			do {
				moid = moid.getParent();

				if (ConfigDebug.ASSERTS) assert moid != null : "parent object expected";
			} while (moid.countChildren() < 2 || !new DBModule(moid).isValid());
			// TODO: explain the first part of the expression

			module = new DBModule(moid);
		}

		if (module == null && ConfigDebug.ASSERTS) ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.SEVERE,
					"Couldn't find a module");

		return module;
	}

	/**
	 * Look throught the tree of objects and finds the MetaBase for current object.
	 * 
	 * @return MetaBase for current object
	 */
	protected MetaBase getMetaBase() throws DatabaseException {
		OID metaOID = oid;
		MetaBase metaBaase = null;

		do {
			metaOID = metaOID.getParent();
			if (metaOID == null) metaOID = this.getModule().getMetabaseEntry();

		} while (!new MetaBase(metaOID).isValid());

		metaBaase = new MetaBase(metaOID);

		if (metaBaase == null && ConfigDebug.ASSERTS) {
			ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.SEVERE, "Couldn't find a matabase");
		}

		return metaBaase;
	}

	/********************************************************************************************************************
	 * access to subobjects describing the declaration
	 */

	protected OID getKindRef() throws DatabaseException {
		return oid.getChildAt(OBJECT_KIND_POS);
	}

	protected final static int OBJECT_KIND_POS = 0;

	public final boolean isTransactionCapable() {
		return this.transMBObjectImpl != null;
	}

	public final MBObject getMBObjectContainer() {
		this.checkTransactionCapableImplementation();
		return this.transMBObjectImpl.getMBObjectContainer();
	}

	public final ITransactionCapabilities getTransactionCapabilities() {
		this.checkTransactionCapableImplementation();
		return this.transMBObjectImpl.getTransactionCapabilities();
	}

	public final IMBTransactionCapabilities getMBTransactionCapabilities() {
		this.checkTransactionCapableImplementation();
		return this.transMBObjectImpl.getMBTransactionCapabilities();
	}

	private final void checkTransactionCapableImplementation() {
		if (ConfigDebug.ASSERTS) {
			assert this.isTransactionCapable() : ITransactionCapableMBObject.class + " implementation has not been set";
		}
	}
}