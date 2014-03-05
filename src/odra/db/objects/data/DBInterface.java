package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

public class DBInterface extends DBObject {
	public DBInterface(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();	
	}

	public void initialize(String objname) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.INTERFACE_OBJECT);
		store.createStringObject(store.addName(Names.namesstr[Names.NAME_ID]), oid, objname, 0);
		store.createComplexObject(store.addName(Names.namesstr[Names.VALUE_ID]), oid, 0);
	}

	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.INTERFACE_OBJECT;
	}

	public String getName() throws DatabaseException {
		return getNameRef().derefString();
	}
	
	public OID addTarget(OID target) throws DatabaseException {
		for (OID oid : getValueRef().derefComplex()) {
			if (oid.derefReference().equals(target))
				return oid;
		}

		return store.createReferenceObject(store.addName(Names.namesstr[Names.VALUE_ID]), getValueRef(), target);
	}

	public OID[] getTargets() throws DatabaseException {
		return getValueRef().derefComplex();
	}

	private final OID getValueRef() throws DatabaseException {
		return oid.getChildAt(VALUE_POS);
	}
	
	private final OID getNameRef() throws DatabaseException {
		return oid.getChildAt(NAME_POS);
	}

	private static final int NAME_POS = 1;
	private static final int VALUE_POS = 2;
	
	public final static int FIELD_COUNT = 3;
}
