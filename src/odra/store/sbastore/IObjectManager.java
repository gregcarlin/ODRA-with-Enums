package odra.store.sbastore;

import java.util.Date;
import java.util.Set;

import odra.db.DatabaseException;
import odra.store.io.IHeap;
import odra.store.io.IHeapExtension;
import odra.store.memorymanagement.IMemoryManager;
import odra.store.sbastore.IObjectManagerExtension.ExtensionType;
import odra.transactions.store.IHeapTransactionExtension;
import odra.transactions.store.IObjectManagerTransactionExtension;

public interface IObjectManager {

	/**
	 * creates the root object of the database.
	 * 
	 * @param objects
	 * @throws DatabaseException
	 */
	void initialize(int objects) throws DatabaseException;

	/**
	 * opens an existing data store
	 * 
	 * @throws DatabaseException
	 */
	void open() throws DatabaseException;

	/**
	 * closes the data store
	 * 
	 * @return
	 */
	void close();

	/**
	 * returns the adress of the root object
	 * 
	 * @return
	 */
	int getEntry();

	/********************************************************************************************************************
	 * this part represents functionality of sba objects
	 */

	/**
	 * creates a complex object. preallocates space for children-subobjects. if children == 0, the space containing
	 * references to subobjects is continually allocated and deallocated (which is a performance hog)
	 * 
	 * @param name
	 * @param parent
	 * @param children
	 */
	int createComplexObject(int name, int parent, int children) throws DatabaseException;

	/**
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	boolean isComplexObject(int offset) throws DatabaseException;

	/**
	 * returns the number of subobjects connected to a particular complex object
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	int countChildren(int offset) throws DatabaseException;

	/**
	 * returns the first child of object "parent" which has the name "name". the returned value indicates a position in
	 * the list of children
	 * 
	 * 
	 * @param name
	 * @param parent
	 * @return
	 * @throws DatabaseException
	 */
	int findFirstChildNumByName(int name, int parent) throws DatabaseException;

	/**
	 * returns the next child of object "parent" which has the name "name". the returned value (as well as the attribute
	 * "previous") are not offsets, but numbers indicating a position in the list of children
	 * 
	 * @param name
	 * @param parent
	 * @param previous
	 * @return
	 * @throws DatabaseException
	 */
	int findNextChildNumByName(int name, int parent, int previous) throws DatabaseException;

	/**
	 * returns a pointer to the "child"th subobject (indexing starts from 0)
	 * 
	 * @param offset
	 * @param child
	 * @return
	 * @throws DatabaseException
	 */
	int getChildAt(int offset, int child) throws DatabaseException;

	/**
	 * 
	 * @param object
	 * @param newparent
	 * @throws DatabaseException
	 */
	void move(int object, int newparent) throws DatabaseException;

	/**
	 * creates a new reference object pointing at an object having oid == value
	 * 
	 * @param name
	 * @param parent
	 * @param value
	 * @return
	 * @throws DatabaseException
	 */
	int createReferenceObject(int name, int parent, int value) throws DatabaseException;

	/**
	 * is the object of address 'offset' a reference object?
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	boolean isReferenceObject(int offset) throws DatabaseException;

	/**
	 * returns the value of a reference object
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	int getReferenceObjectValue(int offset) throws DatabaseException;

	/**
	 * returns the value of a pointer object
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	int getPointerObjectValue(int offset) throws DatabaseException;

	/**
	 * returns the list of reference objects pointing at a particular object
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	int[] getBackwardReferences(int offset) throws DatabaseException;

	/**
	 * updates the value of a reference object. first, the object is unregistered from backward references, then a new
	 * backward reference is created
	 * 
	 * @param offset
	 * @param value
	 * @throws DatabaseException
	 */
	void setReferenceObjectValue(int offset, int value) throws DatabaseException;

	/**
	 * updates the value of an existing pointer object
	 * 
	 * @param offset
	 * @param value
	 * @throws DatabaseException
	 */
	void setPointerObjectValue(int offset, int value) throws DatabaseException;

	/**
	 * creates a new pointer object
	 * 
	 * @param name
	 * @param parent
	 * @param value
	 * @return
	 * @throws DatabaseException
	 */
	int createPointerObject(int name, int parent, int value) throws DatabaseException;

	/**
	 * is the object of address 'offset' a pointer object?
	 * 
	 * @param offset
	 * @return
	 * @throws DatabaseException
	 */
	boolean isPointerObject(int offset) throws DatabaseException;

	/**
	 * creates a new integer object
	 */
	int createIntegerObject(int name, int parent, int value) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is an integer object?
	 */
	boolean isIntegerObject(int offset) throws DatabaseException;

	/**
	 * creates a new double object
	 */
	int createDoubleObject(int name, int parent, double value) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is a double object?
	 */
	boolean isDoubleObject(int offset) throws DatabaseException;

	/**
	 * creates a new string object. <br>
	 * values of string objects contain references to separate blocks storing actual string values. <br>
	 * when the value is updated, the block may change its location, but the object is not moved. <br>
	 * the parameter buffer preallocates the space for future updates (realloc works faster, at the cost of unused space)
	 */
	int createStringObject(int name, int parent, String value, int buffer) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is a string object?
	 */
	boolean isStringObject(int offset) throws DatabaseException;

	/**
	 * creates a new boolean object
	 * 
	 * @param name
	 * @param parent
	 * @param value
	 * @return
	 * @throws DatabaseException
	 */
	int createBooleanObject(int name, int parent, boolean value) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is a boolean object?
	 */
	boolean isBooleanObject(int offset) throws DatabaseException;

	/**
	 * creates a new binary object. <br>
	 * the actual value of binary objects is stored in the same way as in the case of string objects
	 */
	int createBinaryObject(int name, int parent, byte[] value, int buffer) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is a binary object?
	 */
	boolean isBinaryObject(int offset) throws DatabaseException;

	/**
	 * creates a new aggregate object. <br>
	 * aggregate objects are complex objects used to speed up name binding. <br>
	 * a single aggegate object contains all objects of the same name stored on the a particular level of the object
	 * tree.
	 */
	int createAggregateObject(int name, int parent, int children, int minCard, int maxCard) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is an aggregate object?
	 */
	boolean isAggregate(int offset) throws DatabaseException;

	/**
	 * creates a new date object
	 */
	int createDateObject(int name, int parent, Date value) throws DatabaseException;

	/**
	 * checks whether the object of address 'offset' is a date object?
	 */
	boolean isDateObject(int offset) throws DatabaseException;

	void setDateObjectValue(int offset, Date val) throws DatabaseException;

	/**
	 * deletes objects from the database by unregistering it from its parent object and deallocating the piece of memory
	 * used by the object:
	 * <li>if the object being deleted is a reference object, a backward reference is removed.</li>
	 * <li>if a complex object is being deleted, its subordinary object are also removed.</li>
	 * <li>if a binary or string objects are being deleted, also the block containing their values is deallocated.</li>
	 * when an object is deleted, the reference objects which point at it are also automatically destroyed.
	 */
	void deleteObject(int offset, boolean controlCardinality) throws DatabaseException;

	int getObjectNameId(int offset) throws DatabaseException;

	int getObjectParent(int offset) throws DatabaseException;

	int getIntegerObjectValue(int offset) throws DatabaseException;

	void setDoubleObjectValue(int offset, double val) throws DatabaseException;

	double getDoubleObjectValue(int offset) throws DatabaseException;

	Date getDateObjectValue(int offset) throws DatabaseException;

	void setBooleanObjectValue(int offset, boolean val) throws DatabaseException;

	boolean getBooleanObjectValue(int offset) throws DatabaseException;

	void setStringObjectValue(int offset, String val) throws DatabaseException;

	String getStringObjectValue(int offset) throws DatabaseException;

	void setBinaryObjectValue(int offset, byte[] val) throws DatabaseException;

	byte[] getBinaryObjectValue(int offset) throws DatabaseException;

	int[] getComplexObjectValue(int offset) throws DatabaseException;

	void setAggregateObjectMinCard(int offset, int minCard) throws DatabaseException;

	int getAggregateObjectMinCard(int offset) throws DatabaseException;

	void setAggregateObjectMaxCard(int offset, int minCard) throws DatabaseException;

	int getAggregateObjectMaxCard(int offset) throws DatabaseException;

	void setIntegerObjectValue(int offset, int val) throws DatabaseException;

	/**
	 * Sets instance_of reference
	 * 
	 * @param offset -
	 *           instance
	 * @param val -
	 *           instanceof target value
	 * @throws DatabaseException
	 */
	void setInstanceofReferenceValue(int owner, int val) throws DatabaseException;

	/**
	 * Sets role of reference
	 * 
	 * @param offset -
	 *           role
	 * @param val -
	 *           roleof target object
	 * @throws DatabaseException
	 */
	void setRoleofReferenceValue(int offset, int val) throws DatabaseException;

	/**
	 * @param offset
	 * @return instanceof reference value
	 * @throws DatabaseException
	 */
	int getInstanceofReferenceValue(int offset) throws DatabaseException;

	/**
	 * @param offset
	 * @return roleof reference value
	 * @throws DatabaseException
	 */
	int getRoleofReferenceValue(int offset) throws DatabaseException;

	/********************************************************************************************************************
	 * this part is used for debugging purposes
	 */
	// TODO: AUTHOR OF OBJECT MANAGER SHOULD VERIFY WHETHER THE BELOW METHOD SHOULD CONSTITUTE A PART OF THE PUBLIC
	// INTERFACE
	String dump(int start, NameIndex nidx) throws DatabaseException;

	String dumpMemory(boolean verbose) throws DatabaseException;

	/**
	 * Returns a {@link IMemoryManager} encapsulated by the present {@link IObjectManager}.
	 * 
	 * @return
	 */
	IMemoryManager getMemoryManager();

	/**
	 * Returns a total memory size managed by {@link IObjectManager).
	 * 
	 * @return total memory
	 */
	int getTotalMemory();

	/**
	 * Returns a free memory size managed by {@link IObjectManager).
	 * 
	 * @return free memory
	 */
	int getFreeMemory();

	/**
	 * Returns an used memory size managed by {@link IObjectManager).
	 * 
	 * @return used memory
	 */
	int getUsedMemory();

	/**
	 * Adds an implementation of an {@link IHeap} extension, such as {@link IHeapTransactionExtension}.<br>
	 * 
	 * NOTE: THERE MAY BE ONLY ONE IMPLEMENTATION OF THE GIVEN EXTENSION ATTACHED TO THE PARTICULAR {@link IHeap}
	 * instance.
	 * 
	 * @param extension
	 * @return added {@link IHeapExtension} implementation
	 */
	IObjectManagerExtension addExtension(IObjectManagerExtension extension);

	/**
	 * Remove an implementation of an {@link IHeap} extension.<br>
	 * 
	 * NOTE: THERE MAY BE ONLY ONE IMPLEMENTATION OF THE GIVEN EXTENSION ATTACHED TO THE PARTICULAR {@link IHeap}
	 * instance.
	 * 
	 * @param typeExtension
	 * @return deleted {@link IHeapExtension} implementation
	 */
	IObjectManagerExtension removeExtension(IObjectManagerExtension.ExtensionType typeExtension);

	/**
	 * Returns the extenstions attached to the present {@link IHeap} instance.
	 * 
	 * @return the set of {@link IHeapExtension} implementations attached to the given {@link IHeap} instance.
	 */
	Set<IObjectManagerExtension> getExtensions();

	/**
	 * Returns an implementation of {@link IObjectManagerExtension} of the particular type.
	 * 
	 * @param typeExtension
	 * @return the specific implementation of the given {@link ExtensionType}
	 */
	IObjectManagerExtension getExtension(ExtensionType typeExtension);

	/**
	 * Returns an implementation of {@link IObjectManagerTransactionExtension}.
	 * 
	 * @return
	 */
	IObjectManagerTransactionExtension getTransactionExtension();

	/**
	 * Checks whether the present {@link IObjectManager} implementation instance has been attached an
	 * {@link IObjectManagerExtension} implementation of the given {@link ExtensionType}.
	 * 
	 * @param typeExtension
	 * @return
	 */
	boolean hasExtension(ExtensionType typeExtension);

	/**
	 * Returns heap of the given {@link IObjectManager}
	 * 
	 * @return
	 */
	IHeap getHeap();


	// TODO: the below methods have been extracted because of compilation problems resulted from encapsulation --- it
	// should be revised by the author(s) whether the SpecialReferenceManager and ValuesManager should be independent of
	// particular IObjectManager implementation or
	// not
	void registerSpecialReference(int owner, int specialref) throws DatabaseException;
	void registerInstanceOfSpecialReference(int owner, int offset) throws DatabaseException;
	void unregisterSpecialReference(int owner, int specialref) throws DatabaseException;

	int[] getSpecialReferences(int offset) throws DatabaseException;
	int getFirstSpecialReference(int offset) throws DatabaseException;


}