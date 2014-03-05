package odra.store.sbastore;

import java.util.logging.Level;

import odra.OdraCoreAssemblyInfo;
import odra.db.DatabaseException;
import odra.db.DatabaseRuntimeException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.dataaccess.NameIndexAccess;
import odra.db.indices.keytypes.StringKeyType;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.recordtypes.SimpleRecordType;
import odra.db.indices.structures.IndexStructureKind;
import odra.db.indices.structures.LinearHashingMap;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.system.log.UniversalLogger;

/**
 * The class represents the functionality of so called naming indices. <br>
 * The indexes are blocks of memory stored in the database and contains names of objects stored in the databased or used
 * in a queries.
 * 
 * @author tkowals
 */
public class NameIndex {

	private final static UniversalLogger logger = UniversalLogger.getInstance(OdraCoreAssemblyInfo.class,
				NameIndex.class);

	private final OID oid;

	private final IDataStore store;

	private OID tableoid;

	private LinearHashingMap nidxmap;

	private int buffer; // preallocated namespace

	private final static String ERROR_INSTANTIATE = "ERROR_INSTANTIATE";

	/**
	 * Initializes a new {@link NameIndex} object using a reference to an existing NameIndex object (or an empty complex
	 * object).
	 */
	public NameIndex(OID oid) {
		try {
			if (ConfigDebug.ASSERTS) {
				assert oid.isComplexObject();
			}

			this.oid = oid;
			this.store = oid.getStore();

			if (oid.countChildren() == FIELDS_COUNT) {
				logger.debug("store.countChildren(oid): true");
				RecordType recordType = new SimpleRecordType(getRecordTypeRef());
				DataAccess dataAccess = new NameIndexAccess(getDataAccessRef());
				this.tableoid = oid.getChildAt(TABLEOID_POS);
				this.nidxmap = (LinearHashingMap) IndexStructureKind.generateIndex(IndexStructureKind.LINEARHASHINGMAP_ID,
							oid.getChildAt(INDEXOID_POS), recordType, dataAccess);
				logger.debug("(NameIndex) this.nidxmap: " + this.nidxmap);
				this.buffer = this.tableoid.countChildren();
			} else {
				logger.debug("store.countChildren(oid): false");
				this.tableoid = null;
				this.nidxmap = null;
			}
		} catch (Exception ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Name index exception", ex);
			throw new DatabaseRuntimeException(NameIndex.class, ERROR_INSTANTIATE, ex);
		}
	}

	/**
	 * Initializes a new memory index
	 */
	public void initialize() {
		try {

			this.buffer = 31 * 3;

			RecordType recordType = new SimpleRecordType(new StringKeyType());
			recordType.initialize(this.store.createComplexObject(Names.RECORDTYPE_ID, oid, 1));
			DataAccess dataAccess = new NameIndexAccess();
			dataAccess.initialize(this.store.createComplexObject(Names.DATAACCESS_ID, oid, 1));

			this.tableoid = this.store.createComplexObject(Names.ID2NAME_ID, this.oid, this.buffer);

			this.nidxmap = (LinearHashingMap) IndexStructureKind.generateIndex(IndexStructureKind.LINEARHASHINGMAP_ID,
						store.createComplexObject(Names.NAME2ID_ID, oid, LinearHashingMap.FIELDS_COUNT), recordType,
						dataAccess);
			this.nidxmap.initialize(31, 3, 75, 65);

			for (int i = 0; i < Names.namesstr.length; i++)
				addName(Names.namesstr[i]);

		} catch (Exception ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Name index exception", ex);
		}

	}

	/**
	 * Adds a new name to the index and returns its id
	 * 
	 * @throws DatabaseException
	 */
	public int addName(String name) throws DatabaseException {
		logger.debug("nidxmap: " + this.nidxmap);
		int regname = (Integer) this.nidxmap.lookupItem(name);
		if (regname != NameIndex.NAMENOTFOUND) {
			return regname;
		}

		if (nidxmap.getRecordCount() + 1 > buffer) buffer += buffer;

		// TODO: to optimize if names are removed from index
		regname = store.createStringObject(Names.NAME_ID, tableoid, name, 0).getParent().countChildren() - 1;
		nidxmap.insertItem(name, regname);

		return regname;
	}

	/**
	 * @return id of a name
	 * @throws DatabaseException
	 */
	public int name2id(String name) throws DatabaseException {
		return (Integer) nidxmap.lookupItem(name);
	}

	/**
	 * Returns the user readable name of the object using the name id of an object
	 * 
	 * @throws DatabaseException
	 */
	public String id2name(int id) throws DatabaseException {
		return tableoid.getChildAt(id).derefString();
	}

	private final OID getRecordTypeRef() throws DatabaseException {
		return oid.getChildAt(RECORDTYPE_POS);
	}

	private final OID getDataAccessRef() throws DatabaseException {
		return oid.getChildAt(DATAACCESS_POS);
	}

	public final static int NAMENOTFOUND = -1;

	private final static int FIELDS_COUNT = 4;

	public final static int RECORDTYPE_POS = 0;

	public final static int DATAACCESS_POS = 1;

	public final static int TABLEOID_POS = 2;

	public final static int INDEXOID_POS = 3;
}