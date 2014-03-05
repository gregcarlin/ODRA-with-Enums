package odra.store;

import java.util.Date;

import odra.db.AbstractDataStore;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.store.sbastore.NameIndex;
import odra.store.sbastore.ODRAObjectKind;
import odra.store.sbastore.ObjectManager;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * 
 * @author raist
 */

public class DefaultStore extends AbstractDataStore {
	protected ObjectManager manager;
	private NameIndex nidx = Database.getNameIndex();

	public DefaultStore(ObjectManager manager) {
		this.manager = manager;
	}

	public void close() {
		manager.close();
	}

	public void open() throws DatabaseException {
		nidx = new NameIndex(getChildAt(getEntry(), 0));
		Database.setNameIndex(nidx);
	}

	public void initialize() throws DatabaseException {
		OID nidxoid = createComplexObject(Names.NAMEINDEX_ID, getEntry(), 2);

		nidx = new NameIndex(nidxoid);
		nidx.initialize();
		Database.setNameIndex(nidx);
	}
	
	public NameIndex getNameIndex() {
		return nidx;
	}

	public OID getEntry() {
		return offset2OID(manager.getEntry());
	}

	public OID getRoot() throws DatabaseException {
		return offset2OID(manager.getComplexObjectValue(manager.getEntry())[1]);
	}

	/***********************************************************
	 * This part is used to deal with database objects. The main
	 * job of these methods is to convert internal pointers
	 * into OIDs (and vice versa).
	 */

	public OID getParent(OID obj) throws DatabaseException {
		return offset2OID(manager.getObjectParent(OID2offset(obj)));
	}

	public String getObjectName(OID obj) throws DatabaseException {
		return getName(manager.getObjectNameId(OID2offset(obj)));
	}

	public int getObjectNameId(OID obj) throws DatabaseException {
		return manager.getObjectNameId(OID2offset(obj));
	}

	public void delete(OID obj, boolean controlCardinality) throws DatabaseException {
		manager.deleteObject(OID2offset(obj), controlCardinality);
	}

	public void deleteAllChildren(OID parent, boolean controlCardinality) throws DatabaseException {
		if (ConfigDebug.ASSERTS) manager.isComplexObject(OID2offset(parent));
		
		int[] children = manager.getComplexObjectValue(OID2offset(parent));
		
		for (int i = 0; i < children.length; i++)
			manager.deleteObject(children[i], controlCardinality, true);	
		
	}

	public OID[] getReferencesPointingAt(OID obj) throws DatabaseException {
		return offsets2OIDs(manager.getBackwardReferences(OID2offset(obj)));
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#getReversePointer(odra.db.OID)
	 */
	public OID getReversePointer(OID obj) throws DatabaseException
	{
	    return offset2OID(manager.getReverseReferenceValue(OID2offset(obj)));
	}

	public OID getChildAt(OID parent, int childnum) throws DatabaseException {
		return offset2OID(manager.getChildAt(OID2offset(parent), childnum));
	}

	public int countChildren(OID obj) throws DatabaseException {
		return manager.countChildren(OID2offset(obj));
	}

	public OID createComplexObject(int name, OID parent, int children) throws DatabaseException {
		return offset2OID(manager.createComplexObject(name, OID2offset(parent), children));
	}

	public OID createIntegerObject(int name, OID parent, int value) throws DatabaseException {
		return offset2OID(manager.createIntegerObject(name, OID2offset(parent), value));
	}

	public OID createDoubleObject(int name, OID parent, double value) throws DatabaseException {
		return offset2OID(manager.createDoubleObject(name, OID2offset(parent), value));
	}

	public OID createStringObject(int name, OID parent, String value, int buffer) throws DatabaseException {
		return offset2OID(manager.createStringObject(name, OID2offset(parent), value, buffer));
	}

	public OID createBooleanObject(int name, OID parent, boolean value) throws DatabaseException {
		return offset2OID(manager.createBooleanObject(name, OID2offset(parent), value));
	}

	public OID createBinaryObject(int name, OID parent, byte[] value, int buffer) throws DatabaseException {
		return offset2OID(manager.createBinaryObject(name, OID2offset(parent), value, buffer));
	}

	public OID createReferenceObject(int name, OID parent, OID value) throws DatabaseException {
			if(value != null) assert value.getStore() == this: "reference to object in external store";
			return offset2OID(manager.createReferenceObject(name, OID2offset(parent), OID2offset(value)));
	}

	public OID createPointerObject(int name, OID parent, OID value) throws DatabaseException {
		return offset2OID(manager.createPointerObject(name, OID2offset(parent), OID2offset(value)));
	}

	public OID createAggregateObject(int name, OID parent, int children) throws DatabaseException {
		return createAggregateObject(name, parent, children, 0, Integer.MAX_VALUE);
	}

	public OID createAggregateObject(int name, OID parent, int children, int minCard, int maxCard) throws DatabaseException {
		return offset2OID(manager.createAggregateObject(name, OID2offset(parent), children, minCard, maxCard));
	}

	public void updateIntegerObject(OID obj, int val) throws DatabaseException {
		manager.setIntegerObjectValue(OID2offset(obj), val);
	}

	public void updateBooleanObject(OID obj, boolean val) throws DatabaseException {
		manager.setBooleanObjectValue(OID2offset(obj), val);
	}

	public void updateStringObject(OID obj, String val) throws DatabaseException {
		manager.setStringObjectValue(OID2offset(obj), val);
	}

	public void updateDoubleObject(OID obj, double val) throws DatabaseException {
		manager.setDoubleObjectValue(OID2offset(obj), val);
	}

	public void updateReferenceObject(OID obj, OID val) throws DatabaseException {
		manager.setReferenceObjectValue(OID2offset(obj), OID2offset(val));
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#updateReverseReferenceObject(odra.db.OID, odra.db.OID, odra.db.OID)
	 */
	public void setReversePointer(OID obj, OID val)
		throws DatabaseException {
	    manager.setReverseReferenceObjectValue(OID2offset(obj), OID2offset(val));
	    
	}

	public void updatePointerObject(OID obj, OID val) throws DatabaseException {
		manager.setPointerObjectValue(OID2offset(obj), OID2offset(val));
	}

	public void updateBinaryObject(OID obj, byte[] val) throws DatabaseException {
		manager.setBinaryObjectValue(OID2offset(obj), val);
	}

	public boolean isComplexObject(OID obj) throws DatabaseException {
		return manager.isComplexObject(OID2offset(obj));
	}

	public boolean isIntegerObject(OID obj) throws DatabaseException {
		return manager.isIntegerObject(OID2offset(obj));
	}

	public boolean isStringObject(OID obj) throws DatabaseException {
		return manager.isStringObject(OID2offset(obj));
	}

	public boolean isDoubleObject(OID obj) throws DatabaseException {
		return manager.isDoubleObject(OID2offset(obj));
	}

	public boolean isBooleanObject(OID obj) throws DatabaseException {
		return manager.isBooleanObject(OID2offset(obj));
	}

	public boolean isBinaryObject(OID obj) throws DatabaseException {
		return manager.isBinaryObject(OID2offset(obj));
	}

	public boolean isReferenceObject(OID obj) throws DatabaseException {
		return manager.isReferenceObject(OID2offset(obj));
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#isReverseReferenceObject(odra.db.OID)
	 */
	public boolean isReverseReferenceObject(OID obj)
		throws DatabaseException {
	    return manager.isReverseReferenceObject(OID2offset(obj));
	}

	public boolean isPointerObject(OID obj) throws DatabaseException {
		return manager.isPointerObject(OID2offset(obj));
	}

	public boolean isAggregateObject(OID obj) throws DatabaseException {
		return manager.isAggregate(OID2offset(obj));
	}

	public void move(OID obj, OID newparent) throws DatabaseException {
		manager.move(OID2offset(obj), OID2offset(newparent));
	}

	// finds objects with the name "nameid" being subordinates of "parent".
	// generally the search iterates over all suboobjects, but it breaks
	// when the first aggregate object with the desired name is found
	public OID findFirstByNameId(int nameid, OID parent) throws DatabaseException {
		DefaultStoreOID poid = (DefaultStoreOID) parent;
		int paddr = OID2offset(poid);

		int childnum = manager.findFirstChildNumByName(nameid, paddr);

		// TODO: add findfirst() to the object manager, delete getchildat

		return childnum == -1 ? null : offset2OID(manager.getChildAt(paddr, childnum));
	}

	/***********************************************************
	 * This part is used to deal with names.
	 * Naming indexes convert name ids into strings (and vice versa).
	 * They are stored as binary objects and usually are associated
	 * with database modules.
	 */

	public String getName(int nameid) throws DatabaseException {
		return nidx.id2name(nameid);
	}

	public int addName(String name) throws DatabaseException {
		return nidx.addName(name);
	}

	public int getNameId(String name) throws DatabaseException {
		return nidx.name2id(name);
	}

	/***********************************************************
	 * This part is used to convert internal store
	 * pointers into OIDs (and vice versa)
	 */

	public final DefaultStoreOID offset2OID(int offset) {
		return offset == 0 ? null : new DefaultStoreOID(offset, this);
	}

	protected final int OID2offset(OID oid) {
//		if (Config.ASSERTS) assert oid != null : "oid == " + oid;
//		if (Config.ASSERTS) assert (oid instanceof DefaultStoreOID && oid.getStore() == this) : "the oid does not belong to this datastore";

		return oid == null ? 0 : ((DefaultStoreOID) oid).getOffset();
	}

	private final int[] OIDs2offset(DefaultStoreOID[] oids) {
		int[] offsets = new int[oids.length];

		for (int i = 0; i < oids.length; i++)
			offsets[i] = OID2offset(oids[i]);

		return offsets;
	}

	private final DefaultStoreOID[] offsets2OIDs(int[] offsets) {
		DefaultStoreOID[] oids = new DefaultStoreOID[offsets.length];

		for (int i = 0; i < offsets.length; i++)
			oids[i] = offset2OID(offsets[i]);

		return oids;
	}

	/***********************************************************
	 * Debugging
	 */
	public String dump() throws DatabaseException {
		return manager.dump(manager.getEntry(), nidx);
	}

	public String dumpMemory(boolean verbose) throws DatabaseException {
		return manager.dumpMemory(verbose);
	}

	public byte[] derefBinaryObject(OID obj) throws DatabaseException {
		return manager.getBinaryObjectValue(OID2offset(obj));
	}

	public boolean derefBooleanObject(OID obj) throws DatabaseException {
		return manager.getBooleanObjectValue(OID2offset(obj));
	}

	public OID[] derefComplexObject(OID obj) throws DatabaseException {
		return offsets2OIDs(manager.getComplexObjectValue(OID2offset(obj)));
	}

	public double derefDoubleObject(OID obj) throws DatabaseException {
		return manager.getDoubleObjectValue(OID2offset(obj));
	}
	
	public Date derefDateObject(OID obj) throws DatabaseException {
		return manager.getDateObjectValue(OID2offset(obj));
	}

	public int derefIntegerObject(OID obj) throws DatabaseException {
		return manager.getIntegerObjectValue(OID2offset(obj));
	}

	public OID derefReferenceObject(OID obj) throws DatabaseException {
		return offset2OID(manager.getReferenceObjectValue(OID2offset(obj)));
	}

	public String derefStringObject(OID obj) throws DatabaseException {
		return manager.getStringObjectValue(OID2offset(obj));
	}
	
	public ObjectManager getObjectManager() {
		return manager;
	}

	/**
	 * Returns a total store memory size.
	 * 
	 * @return total memory
	 */
	public int getTotalMemory()
	{
		return manager.getTotalMemory();
	}
	
	/**
	 * Returns a free store memory size.
	 * 
	 * @return free memory
	 */
	public int getFreeMemory()
	{
		return manager.getFreeMemory();
	}
	
	/**
	 * Returns an used store memory size.
	 * 
	 * @return used memory
	 */
	public int getUsedMemory()
	{
		return manager.getUsedMemory();
	}

	public OID createDateObject(int name, OID parent, Date value) throws DatabaseException
	{
		return offset2OID(manager.createDateObject(name, OID2offset(parent), value));
	}

	public boolean isDateObject(OID obj) throws DatabaseException
	{
		return manager.isDateObject(OID2offset(obj));
	}

	public void updateDateObject(OID obj, Date date) throws DatabaseException
	{
		manager.setDateObjectValue(OID2offset(obj), date);
	}

	//M1 model
	
	/* (non-Javadoc)
	 * @see odra.db.IDataStore#derefInstanceOfReference(odra.db.OID)
	 */
	public OID derefInstanceOfReference(OID obj) throws DatabaseException {
	    return offset2OID(manager.getInstanceofReferenceValue(OID2offset(obj)));
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#isClassInstance(odra.db.OID)
	 */
	public boolean isClassInstance(OID obj) throws DatabaseException {
		return manager.getInstanceofReferenceValue(OID2offset(obj)) != 0;
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#setInstanceOfReference(odra.db.OID, odra.db.OID)
	 */
	public void setInstanceOfReference(OID obj, OID clsObj) throws DatabaseException {

		manager.setInstanceofReferenceValue(OID2offset(obj), OID2offset(clsObj));
		
	}

	public int getAggregateMaxCard(OID obj) throws DatabaseException {
		return manager.getAggregateObjectMaxCard(OID2offset(obj));
	}

	public int getAggregateMinCard(OID obj) throws DatabaseException {
		return manager.getAggregateObjectMinCard(OID2offset(obj));
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#getObjectKind(odra.db.OID)
	 */
	public ODRAObjectKind getObjectKind(OID obj) throws DatabaseException
	{
	    
	    return manager.getObjectKind(OID2offset(obj));
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#rename(odra.db.OID, int)
	 */
	@Override
	public void renameObject(OID obj, int newName) throws DatabaseException {
		manager.setObjectNameId(OID2offset(obj), newName);
		
	}

	public String staticDefragmentation() throws DatabaseException {
		return ((ObjectManager) manager).staticDefragmentation();
	}
	
	
}
