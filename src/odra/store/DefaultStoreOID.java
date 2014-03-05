package odra.store;

import java.util.Date;

import odra.db.*;
import odra.store.sbastore.ODRAObjectKind;

/**
 * This class describes objects representing identifiers of database objects
 * stored in the default data store. OID is like an address of an object.
 * 
 * Methods defined in this class are mostly a more convenient way
 * of using their counterparts created in the DefaultStore. 
 * 
 * @author raist
 */

public class DefaultStoreOID extends OID {
	private int offset;
	private DefaultStore store;

	public DefaultStoreOID(int offset, DefaultStore store) {
		this.offset = offset;
		this.store = store;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof DefaultStoreOID && ((DefaultStoreOID) obj).offset == offset && ((DefaultStoreOID) obj).store == store;
	}
	
	public int hashCode() {
		return offset;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public DefaultStore getStore() {
		return store;
	}    
	
	public String getObjectName() throws DatabaseException {
		return store.getObjectName(this);
	}

	public int getObjectNameId() throws DatabaseException {
		return store.getObjectNameId(this);
	}
	
	public OID getParent() throws DatabaseException {
		return store.getParent(this);
	}
  
	/* (non-Javadoc)
	 * @see odra.db.OID#getReversePointer()
	 */
	@Override
	public OID getReversePointer() throws DatabaseException
	{
	    return store.getReversePointer(this);
	}

	public String derefString() throws DatabaseException {
		return store.derefStringObject(this);
	}
	
	public int derefInt() throws DatabaseException {
		return store.derefIntegerObject(this);
	}
	
	public double derefDouble() throws DatabaseException {
		return store.derefDoubleObject(this);
	}
	
	public boolean derefBoolean() throws DatabaseException {
		return store.derefBooleanObject(this);
	}
	
	public Date derefDate() throws DatabaseException {
		return store.derefDateObject(this);
	}
	
	public OID[] derefComplex() throws DatabaseException {
		return store.derefComplexObject(this);
	}
	
	public byte[] derefBinary() throws DatabaseException {
		return store.derefBinaryObject(this);
	}
	
	public OID derefReference() throws DatabaseException {
		return store.derefReferenceObject(this);
	}
	
	public OID getChildAt(int childnum) throws DatabaseException {
		return store.getChildAt(this, childnum);
	}
	
	public int countChildren() throws DatabaseException {
		return store.countChildren(this);
	}
	
	public boolean isComplexObject() throws DatabaseException {
		return store.isComplexObject(this);
	}

	public boolean isIntegerObject() throws DatabaseException {
		return store.isIntegerObject(this);
	}

	public boolean isStringObject() throws DatabaseException {
		return store.isStringObject(this);
	}

	public boolean isDoubleObject() throws DatabaseException {
		return store.isDoubleObject(this);
	}

	public boolean isBooleanObject() throws DatabaseException {
		return store.isBooleanObject(this);
	}

	public boolean isBinaryObject() throws DatabaseException {
		return store.isBinaryObject(this);
	}

	public boolean isReferenceObject() throws DatabaseException {
		return store.isReferenceObject(this);
	}
	
	/* (non-Javadoc)
	 * @see odra.db.OID#isReverseReferenceObject()
	 */
	@Override
	public boolean isReverseReferenceObject() throws DatabaseException
	{
	    return store.isReverseReferenceObject(this);
	}

	public boolean isPointerObject() throws DatabaseException {
		return store.isPointerObject(this);
	}
	
	public boolean isDateObject() throws DatabaseException {
		return store.isDateObject(this);
	}
	
	public boolean isAggregateObject() throws DatabaseException {
		return store.isAggregateObject(this);
	}
	
	public void updateStringObject(String val) throws DatabaseException {
		store.updateStringObject(this, val);
	}
	
	public void updateIntegerObject(int val) throws DatabaseException {
		store.updateIntegerObject(this, val);
	}
	
	public void updateBooleanObject(boolean val) throws DatabaseException {
		store.updateBooleanObject(this, val);
	}
	
	public void updateDoubleObject(double val) throws DatabaseException {
		store.updateDoubleObject(this, val);
	}
	
	public void updateBinaryObject(byte[] val) throws DatabaseException {
		store.updateBinaryObject(this, val);
	}
	
	public void updateReferenceObject(OID val) throws DatabaseException {
		store.updateReferenceObject(this, val);
	}

	public void updatePointerObject(OID val) throws DatabaseException {
		store.updatePointerObject(this, val);
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

	public void updateDateObject(Date val) throws DatabaseException {
		store.updateDateObject(this, val);
	}

	public void delete() throws DatabaseException {
	    assert this.offset != -1 :"already deleted";
		store.delete(this, false);
		this.offset = -1;
	}
	
	public void deleteAllChildren() throws DatabaseException {
	    assert this.offset != -1 :"already deleted";
		store.deleteAllChildren(this, false);
	}
	
	public void deleteSafe() throws DatabaseException {
	    assert this.offset != -1 :"already deleted";
		store.delete(this, true);
		this.offset = -1;
	}
	
	public void deleteAllChildrenSafe() throws DatabaseException {
	    assert this.offset != -1 :"already deleted";
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

	public int internalOID() {
		return offset;
	}
	
	public String toString() {
		return "" + offset;
	}

	//M1 model
	
	/* (non-Javadoc)
	 * @see odra.db.OID#derefInstanceOfReference()
	 */
	@Override
	public OID derefInstanceOfReference() throws DatabaseException {
		
		return store.derefInstanceOfReference(this);
	}

	/* (non-Javadoc)
	 * @see odra.db.OID#isClassInstance()
	 */
	@Override
	public boolean isClassInstance() throws DatabaseException {
		
		return store.isClassInstance(this);
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
	    return store.getObjectKind(this);
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
