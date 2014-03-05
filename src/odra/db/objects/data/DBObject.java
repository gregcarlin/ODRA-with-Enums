package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

public class DBObject {
	protected OID oid;
	protected IDataStore store;
	
	/**
	 * Initializes a new DBObject object using the OID of an existing object.
	 * @param oid oid of the object.
	 */
	public DBObject(OID oid) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert oid != null : "oid == null";

		this.oid = oid;
		this.store = oid.getStore();
	}

	/**
	 * @return name of the object
	 */
	public String getName() throws DatabaseException {
		return oid.getObjectName(); 
	}

	/** 
	 * @return kind of the object (e.g. if it is DBModule, DBClass, etc.)
	 */
	public DataObjectKind getObjectKind() throws DatabaseException {
		if (oid.isComplexObject() && oid.countChildren() > 0) {
			OID ch0 = oid.getChildAt(0);
			
			if (ch0.getObjectName().equals(Names.namesstr[Names.KIND_ID]))
				return new DataObjectKind(ch0.derefInt());
		}

		return new DataObjectKind(DataObjectKind.DATA_OBJECT);
	}
	public OID getOID() {return oid;}
	public final static int NO_NAME = -1;
}
