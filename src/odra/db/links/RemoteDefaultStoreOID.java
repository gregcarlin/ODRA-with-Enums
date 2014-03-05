package odra.db.links;

import java.util.Date;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.store.sbastore.ODRAObjectKind;

public class RemoteDefaultStoreOID extends OID {
	private RemoteDefaultStore store;
	private int id;
	private int nameid = -1; //lazy initialized object name id (for optimization)
	//TODO object kind
	private ODRAObjectKind kind;
	public RemoteDefaultStoreOID(int id, ODRAObjectKind kind, RemoteDefaultStore store) {
		this.id = id;
		this.store = store;
		this.kind = kind;
	}

	public int countChildren() throws DatabaseException {
		return store.countChildren(this);
	}

	public void delete() throws DatabaseException {
		store.delete(this, false);
	}
	
	public void deleteAllChildren() throws DatabaseException {
		store.deleteAllChildren(this, false);
	}
	
	public void deleteSafe() throws DatabaseException {
		store.delete(this, true);
	}
	
	public void deleteAllChildrenSafe() throws DatabaseException {
		store.deleteAllChildren(this, true);
	}
	
	/* (non-Javadoc)
	 * @see odra.db.OID#move(odra.db.OID)
	 */
	@Override
	public void move(OID newparent) throws DatabaseException {
		assert newparent.getStore() == this.store : "move beetween stores";
		store.move(this, newparent);
		
	}

	public byte[] derefBinary() throws DatabaseException {
	    assert this.isBinaryObject() : "improper object type: " + this.getObjectKind().toString();
		return store.derefBinaryObject(this);
	}

	public boolean derefBoolean() throws DatabaseException {
	    assert this.isBooleanObject() : "improper object type: " + this.getObjectKind().toString();
		return store.derefBooleanObject(this);
	}

	public OID[] derefComplex() throws DatabaseException {
	    assert this.isComplexObject() || this.isAggregateObject(): "improper object type: " + this.getObjectKind().toString();
		return store.derefComplexObject(this);
	}

	public double derefDouble() throws DatabaseException {
	    assert this.isDoubleObject(): "improper object type: " + this.getObjectKind().toString();
		return store.derefDoubleObject(this);
	}
	
	public Date derefDate() throws DatabaseException {
	    assert this.isDateObject() : "improper object type: " + this.getObjectKind().toString();
		return store.derefDateObject(this);
	}

	public int derefInt() throws DatabaseException {
	    assert this.isIntegerObject(): "improper object type: " + this.getObjectKind().toString();
		return store.derefIntegerObject(this);
	}
	
	public OID derefReference() throws DatabaseException {
	    
		return store.derefReferenceObject(this);
	}

	public String derefString() throws DatabaseException {
	    assert this.isStringObject() : "improper object type";
		return store.derefStringObject(this);
	}
	
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	public OID getChildAt(int childnum) throws DatabaseException {
		return store.getChildAt(this, childnum);
	}
	
	public String getObjectName() throws DatabaseException {
	    if(this.nameid == -1){
		String name = store.getObjectName(this);
		this.nameid = store.addName(name);
		return name;
	    }
	    return store.getName(this.nameid);
	}

	public int getObjectNameId() throws DatabaseException {
	    if(this.nameid == -1){
		this.nameid = store.addName(store.getObjectName(this));
	    }
	    return this.nameid;
	}
	
	public OID getParent() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IDataStore getStore() {
		return store;
	}
	
	/* (non-Javadoc)
	 * @see odra.db.OID#getReversePointer()
	 */
	@Override
	public OID getReversePointer() throws DatabaseException
	{
	    return store.getReversePointer(this);
	}

	public int internalOID() {
		return id;
	}

	public boolean isAggregateObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.AGGREGATE_OBJECT;
	}

	public boolean isBinaryObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.BINARY_OBJECT;
	}

	public boolean isBooleanObject() throws DatabaseException {
		return this.kind == ODRAObjectKind.BOOLEAN_OBJECT;
	}
	
	public boolean isComplexObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.COMPLEX_OBJECT;
	}
	
	public boolean isDoubleObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.DOUBLE_OBJECT;
	}
	
	public boolean isIntegerObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.INTEGER_OBJECT;
	}

	public boolean isPointerObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.POINTER_OBJECT;
	}

	public boolean isReferenceObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.REFERENCE_OBJECT;
	}
	
	/* (non-Javadoc)
	 * @see odra.db.OID#isReverseReferenceObject()
	 */
	@Override
	public boolean isReverseReferenceObject() throws DatabaseException
	{
	    return this.kind == ODRAObjectKind.REVERSE_REFERENCE_OBJECT;
	}

	public boolean isStringObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.STRING_OBJECT;
	}
	
	public boolean isDateObject() throws DatabaseException {
	    return this.kind == ODRAObjectKind.DATE_OBJECT;
	}
	
	public String toString() {
		return store.schema + "@" + store.host + ":" + store.port + "/" + this.id;
	}

	public void updateBinaryObject(byte[] val) throws DatabaseException {
		store.updateBinaryObject(this, val);
	}
	
	public void updateBooleanObject(boolean val) throws DatabaseException {
		store.updateBooleanObject(this, val);	
	}
	
	public void updateDoubleObject(double val) throws DatabaseException {
		store.updateDoubleObject(this, val);
	}

	public void updateIntegerObject(int val) throws DatabaseException {
		store.updateIntegerObject(this, val);
	}

	public void updatePointerObject(OID val) throws DatabaseException {
		// TODO Auto-generated method stub	
	}
	
	/* (non-Javadoc)
	 * @see odra.db.OID#updateReversePointerObject(odra.db.OID)
	 */
	@Override
	public void setReversePointer(OID val)
		throws DatabaseException
	{
	    store.setReversePointer(this, val);
	    
	}

	public void updateReferenceObject(OID val) throws DatabaseException {
		store.updateReferenceObject(this, val);
	}
	
	public void updateStringObject(String val) throws DatabaseException {
		store.updateStringObject(this, val);
	}
	
	public void updateDateObject(Date val) throws DatabaseException {
		store.updateDateObject(this, val);
	}
	
	public boolean isClassInstance() throws DatabaseException{
		return store.isClassInstance(this);
	}
	public OID derefInstanceOfReference() throws DatabaseException{
		return store.derefInstanceOfReference(this);
	}

	@Override
	public int getAggregateMaxCard() throws DatabaseException {
		return store.getAggregateMaxCard(this);
	}

	@Override
	public int getAggregateMinCard() throws DatabaseException {
		return store.getAggregateMinCard(this);
	}

	/* (non-Javadoc)
	 * @see odra.db.OID#getObjectKind()
	 */
	@Override
	public ODRAObjectKind getObjectKind() throws DatabaseException
	{	    
	    return this.kind;
	}
	
	@Override
	public OID findFirstChildByNameId(int nameid) throws DatabaseException {
		return store.findFirstByNameId(nameid, this);
	}

	@Override
	public OID createAggregateChild(int name, int children)
			throws DatabaseException {
		return store.createAggregateObject(name, this, children);
	}

	@Override
	public OID createAggregateChild(int name, int children, int minCard,
			int maxCard) throws DatabaseException {
		return store.createAggregateObject(name, this, children, minCard, maxCard);
	}

	@Override
	public OID createBinaryChild(int name, byte[] value, int buffer)
			throws DatabaseException {
		return store.createBinaryObject(name, this, value, buffer);
	}

	@Override
	public OID createBooleanChild(int name, boolean value)
			throws DatabaseException {
		return store.createBooleanObject(name, this, value);
	}

	@Override
	public OID createComplexChild(int name, int children)
			throws DatabaseException {
		return store.createComplexObject(name, this, children);
	}

	@Override
	public OID createDateChild(int name, Date value) throws DatabaseException {
		return store.createDateObject(name, this, value);
	}

	@Override
	public OID createDoubleChild(int name, double value)
			throws DatabaseException {
		return store.createDoubleObject(name, this, value);
	}

	@Override
	public OID createIntegerChild(int name, int value) throws DatabaseException {
		return store.createIntegerObject(name, this, value);
	}

	@Override
	public OID createPointerChild(int name, OID value) throws DatabaseException {
		return store.createPointerObject(name, this, value);
	}

	@Override
	public OID createReferenceChild(int name, OID value)
			throws DatabaseException {
		return store.createReferenceObject(name, this, value);
	}

	@Override
	public OID createStringChild(int name, String value, int buffer)
			throws DatabaseException {
		return store.createStringObject(name, this, value, buffer);
	}

	@Override
	public OID[] getReferencesPointingAt() throws DatabaseException {
		return store.getReferencesPointingAt(this);
	}

	@Override
	public void renameObject(int newName) throws DatabaseException {
		store.renameObject(this, newName);
	}

	@Override
	public void setInstanceOfReference(OID value) throws DatabaseException {
		store.setInstanceOfReference(this, value);
	}
}
