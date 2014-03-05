package odra.db;

import java.util.Date;

import odra.store.sbastore.ODRAObjectKind;

/**
 * This interface described services that have to be implemented
 * by all kinds of object identifiers.
 * 
 * @author raist
 */

public abstract class OID {
	public abstract IDataStore getStore();
	
	public abstract OID createComplexChild(int name, int children) throws DatabaseException;
	public abstract OID createIntegerChild(int name, int value) throws DatabaseException;
	public abstract OID createDoubleChild(int name, double value) throws DatabaseException;
	public abstract OID createStringChild(int name, String value, int buffer) throws DatabaseException;
	public abstract OID createBooleanChild(int name, boolean value) throws DatabaseException;
	public abstract OID createBinaryChild(int name, byte[] value, int buffer) throws DatabaseException;
	public abstract OID createAggregateChild(int name, int children) throws DatabaseException;
	public abstract OID createAggregateChild(int name, int children, int minCard, int maxCard) throws DatabaseException;
	public abstract OID createReferenceChild(int name, OID value) throws DatabaseException;
	public abstract OID createPointerChild(int name, OID value) throws DatabaseException;
	public abstract OID createDateChild(int name, Date value) throws DatabaseException;
	
	public abstract String derefString() throws DatabaseException;
	public abstract int derefInt() throws DatabaseException;	
	public abstract double derefDouble() throws DatabaseException;
	public abstract boolean derefBoolean() throws DatabaseException;
	public abstract OID[] derefComplex() throws DatabaseException;
	public abstract byte[] derefBinary() throws DatabaseException;
	public abstract OID derefReference() throws DatabaseException;
	public abstract Date derefDate() throws DatabaseException;
	public abstract OID getReversePointer() throws DatabaseException;
	
	public abstract OID findFirstChildByNameId(int nameid) throws DatabaseException;
	public abstract OID getChildAt(int childnum) throws DatabaseException;
	public abstract OID getParent() throws DatabaseException;
	public abstract String getObjectName() throws DatabaseException;
	public abstract int getObjectNameId() throws DatabaseException;
	public abstract void renameObject(int newName) throws DatabaseException;
	
	public abstract OID[] getReferencesPointingAt() throws DatabaseException;
	
	
	public abstract void updateStringObject(String val) throws DatabaseException;
	public abstract void updateIntegerObject(int val) throws DatabaseException;
	public abstract void updateBooleanObject(boolean val) throws DatabaseException;
	public abstract void updateDoubleObject(double val) throws DatabaseException;
	public abstract void updateBinaryObject(byte[] val) throws DatabaseException;
	public abstract void updateReferenceObject(OID val) throws DatabaseException;
	public abstract void updatePointerObject(OID val) throws DatabaseException;
	public abstract void setReversePointer(OID val) throws DatabaseException;
	public abstract void updateDateObject(Date date) throws DatabaseException;
	
	public abstract int countChildren() throws DatabaseException;
	
	public abstract boolean isComplexObject() throws DatabaseException;
	public abstract boolean isIntegerObject() throws DatabaseException;
	public abstract boolean isStringObject() throws DatabaseException;
	public abstract boolean isDoubleObject() throws DatabaseException;
	public abstract boolean isBooleanObject() throws DatabaseException;
	public abstract boolean isBinaryObject() throws DatabaseException;
	public abstract boolean isReferenceObject() throws DatabaseException;
	public abstract boolean isReverseReferenceObject() throws DatabaseException;
	public abstract boolean isPointerObject() throws DatabaseException;
	public abstract boolean isAggregateObject() throws DatabaseException;
	public abstract boolean isDateObject() throws DatabaseException;
	public abstract ODRAObjectKind getObjectKind() throws DatabaseException;

	public abstract void move(OID newparent) throws DatabaseException;
	public abstract void delete() throws DatabaseException;
	public abstract void deleteAllChildren() throws DatabaseException;
	public abstract void deleteSafe() throws DatabaseException;	
	public abstract void deleteAllChildrenSafe() throws DatabaseException; 	
	
	public abstract int internalOID(); // TODO : kill it

	public abstract String toString();
	public abstract boolean equals(Object obj);
	
	public abstract int getAggregateMinCard() throws DatabaseException;
	public abstract int getAggregateMaxCard() throws DatabaseException;
	
	//M1 
	public abstract boolean isClassInstance() throws DatabaseException;
	public abstract OID derefInstanceOfReference() throws DatabaseException;
	public abstract void setInstanceOfReference(OID value) throws DatabaseException;
	//M2
//	public abstract boolean hasSuperRole() throws DatabaseException;
//	public abstract OID derefRoleOfReference() throws DatabaseException;


}
