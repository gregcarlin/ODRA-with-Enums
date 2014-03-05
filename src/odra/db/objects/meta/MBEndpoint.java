package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;
/**
 * Provides API for metabase object representing web service endpoints.
 *
 * @author merdacz
 *
 */
public class MBEndpoint extends MBObject {

	/**
	 * Initializes a new endpoint meta object
	 * @param oid oid of an existing object
	 * @throws DatabaseException
	 */
	public MBEndpoint(OID oid) throws DatabaseException {
		super(oid);
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 *
	 * @throws DatabaseException
	 */
	public void initialize() throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.ENDPOINT_OBJECT.kindAsInt());

	}

	/**
	 * @return true if the oid really represent a meta endpoint
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.ENDPOINT_OBJECT;
	}


}
