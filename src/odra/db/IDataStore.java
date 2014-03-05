package odra.db;

import java.util.Date;
import java.util.Set;

import odra.db.IDataStoreExtension.ExtensionType;
import odra.store.sbastore.ODRAObjectKind;
import odra.transactions.store.IDataStoreTransactionExtension;

/**
 * The interface describes services that have to be implemented
 * by any data source which is to be used as a jodra database.
 * 
 * @author raist
 */

public interface IDataStore {
	public void close();

	public OID getEntry(); // the first object of the database 
	public OID getRoot() throws DatabaseException; // the first non-system of the database

	public OID findFirstByNameId(int nameid, OID parent) throws DatabaseException;
	public OID getChildAt(OID parent, int childnum) throws DatabaseException;
	public int countChildren(OID obj) throws DatabaseException;
	public OID[] getReferencesPointingAt(OID obj) throws DatabaseException;
	public OID getReversePointer(OID obj) throws DatabaseException;
	public void delete(OID obj, boolean controlCardinality) throws DatabaseException;
	public void deleteAllChildren(OID obj, boolean controlCardinality) throws DatabaseException;
	public void move(OID obj, OID newparent) throws DatabaseException;

	public OID createComplexObject(int name, OID parent, int children) throws DatabaseException;
	public OID createIntegerObject(int name, OID parent, int value) throws DatabaseException;
	public OID createDoubleObject(int name, OID parent, double value) throws DatabaseException;
	public OID createStringObject(int name, OID parent, String value, int buffer) throws DatabaseException;
	public OID createBooleanObject(int name, OID parent, boolean value) throws DatabaseException;
	public OID createBinaryObject(int name, OID parent, byte[] value, int buffer) throws DatabaseException;
	public OID createAggregateObject(int name, OID parent, int children) throws DatabaseException;
	public OID createAggregateObject(int name, OID parent, int children, int minCard, int maxCard) throws DatabaseException;
	public OID createReferenceObject(int name, OID parent, OID value) throws DatabaseException;
	public OID createPointerObject(int name, OID parent, OID value) throws DatabaseException;
	public OID createDateObject(int name, OID parent, Date value) throws DatabaseException;

	public void updateIntegerObject(OID obj, int val) throws DatabaseException;
	public void updateBooleanObject(OID obj, boolean val) throws DatabaseException;
	public void updateStringObject(OID obj, String val) throws DatabaseException;
	public void updateDoubleObject(OID obj, double val) throws DatabaseException;
	public void updateReferenceObject(OID obj, OID val) throws DatabaseException;
	public void setReversePointer(OID obj, OID val) throws DatabaseException;
	public void updateBinaryObject(OID obj, byte[] val) throws DatabaseException;
	public void updateDateObject(OID obj, Date date) throws DatabaseException;
	public void renameObject(OID obj, int newName) throws DatabaseException;
	
	public boolean isComplexObject(OID obj) throws DatabaseException;
	public boolean isIntegerObject(OID obj) throws DatabaseException;
	public boolean isStringObject(OID obj) throws DatabaseException;
	public boolean isDoubleObject(OID obj) throws DatabaseException;
	public boolean isBooleanObject(OID obj) throws DatabaseException;
	public boolean isBinaryObject(OID obj) throws DatabaseException;
	public boolean isReferenceObject(OID obj) throws DatabaseException;
	public boolean isReverseReferenceObject(OID obj) throws DatabaseException;
	public boolean isAggregateObject(OID obj) throws DatabaseException;
	public boolean isDateObject(OID obj) throws DatabaseException;
	public abstract ODRAObjectKind getObjectKind(OID obj) throws DatabaseException;
	
	public OID[] derefComplexObject(OID obj) throws DatabaseException;
	public int derefIntegerObject(OID obj) throws DatabaseException;
	public String derefStringObject(OID obj) throws DatabaseException;
	public double derefDoubleObject(OID obj) throws DatabaseException;
	public boolean derefBooleanObject(OID obj) throws DatabaseException;
	public byte[] derefBinaryObject(OID obj) throws DatabaseException;
	public OID derefReferenceObject(OID obj) throws DatabaseException;
	public Date derefDateObject(OID obj) throws DatabaseException;

	public int getNameId(String name) throws DatabaseException;
	public String getName(int nameid) throws DatabaseException;
	public int addName(String name) throws DatabaseException;
	
	public String dump() throws DatabaseException;
	public String dumpMemory(boolean verbose) throws DatabaseException;

	public OID offset2OID(int oid);
	
	public abstract int getAggregateMinCard(OID obj) throws DatabaseException;
	public abstract int getAggregateMaxCard(OID obj) throws DatabaseException;
	
	//M1
	public abstract boolean isClassInstance(OID obj) throws DatabaseException;
	public abstract OID derefInstanceOfReference(OID obj) throws DatabaseException;
	public abstract void setInstanceOfReference(OID obj, OID clsObj) throws DatabaseException;
	
	//M2
//	public abstract boolean hasSuperRole(OID obj) throws DatabaseException;
//	public abstract OID derefRoleOfReference(OID obj) throws DatabaseException;
//	public abstract void setRoleOfReference(OID obj, OID superObj) throws DatabaseException;
	
	IDataStoreExtension addExtension(IDataStoreExtension extension);

	IDataStoreExtension removeExtension(IDataStoreExtension.ExtensionType typeExtension);

	Set<IDataStoreExtension> getExtensions();

	IDataStoreExtension getExtension(ExtensionType typeExtension);

	IDataStoreTransactionExtension getTransactionExtension();

	boolean hasExtension(ExtensionType typeExtension);
}
