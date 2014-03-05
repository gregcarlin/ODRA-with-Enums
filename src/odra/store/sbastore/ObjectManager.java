package odra.store.sbastore;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.store.memorymanagement.AbstractMemoryManager;
import odra.system.Sizes;
import odra.system.config.ConfigDebug;

/**
 * A sample, persistent, data store, built in accordance with the M0 SBA data model
 * @author raist
 * 
 * 29.03.07 radamus
 * data store extended with special references for creating models > M0
 * implementation of M1 'instanceof' references
 */
public class ObjectManager extends AbstractObjectManager {
	
	protected int entry;
	
	protected final ValuesManager valuesManager;

	protected final SpecialReferencesManager specManager;
		
	public ObjectManager(AbstractMemoryManager allocator, ValuesManager valuesManager,
				SpecialReferencesManager specManager) {
		super(allocator);
		
		this.valuesManager = valuesManager;
		this.valuesManager.setObjectManager(this);

		this.specManager = specManager;
		this.specManager.setObjectManager(this);
	}

	public ObjectManager(AbstractMemoryManager allocator) {
		this(allocator, new ValuesManager(allocator), new SpecialReferencesManager(allocator));
	}
	
	/**
	 * Returns special references manager of the present {@link ObjectManager}.
	 * 
	 * @return
	 */
	public final SpecialReferencesManager getSpecialReferencesManager() {
		return this.specManager;
	}

	/**
	 * Returns values manager of the present {@link ObjectManager}.
	 * 
	 * @return
	 */
	public final ValuesManager getValuesManager() {
		return this.valuesManager;
	}

	
	// creates the root object of the database. 
	public void initialize(int objects) throws DatabaseException {		
		entry = createComplexObject(NO_NAME, NO_PARENT, objects);
		allocator.setEntryOffset(entry);
	}

	// opens an existing data store
	public void open() throws DatabaseException {
		entry = allocator.getEntryOffset();
		valuesManager.open();
		specManager.open();
	}
	
	// closes the data store
	public void close() {
		heap.close();
		valuesManager.close();
		specManager.close();
	}
	
	// returns the adress of the root object
	public final int getEntry() {
		return entry;
	}
	
	/* *************************************************************************************
	 * this part represents functionality of sba objects
	 * */
	
	// creates a complex object. preallocates space for children-subobjects.
	// if children == 0, the space containing references to subobjects is continually
	// allocated and deallocated (which is a performance hog)
	public int createComplexObject(int name, int parent, int children) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.COMPLEX_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);	
		setIntegerObjectValue(offset, valuesManager.preallocateBlockOfInts(children));
		///////special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerChildReference(parent, offset);
		
		return offset;
	}
	
	public boolean isComplexObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.COMPLEX_OBJECT;
	}
	
	// returns the number of subobjects connected to a particular complex object
	public final int countChildren(int offset) throws DatabaseException {
		return valuesManager.countInts(getIntegerObjectValue(offset));
	}
	
	// returns the first child of object "parent" which has the name "name".
	// the returned value indicates a position in the list of children
	public final int findFirstChildNumByName(int name, int parent) throws DatabaseException {
	    assert this.isComplexObject(parent) : "complex object required";
		int nchildren = countChildren(parent);
		
		for (int i = 0; i < nchildren; i++) {
			int childat = getChildAt(parent, i);
			int childname = this.getObjectNameId(childat);

			if (childname == name)
				return i;
		}
		
		return -1;
	}
	
	// returns the next child of object "parent" which has the name "name".
	// the returned value (as well as the attribute "previous") are not offsets,
	// but numbers indicating a position in the list of children
	public final int findNextChildNumByName(int name, int parent, int previous) throws DatabaseException {
		int nchildren = countChildren(parent);		
		
		for (int i = previous + 1; i < nchildren; i++)
			if (this.getObjectNameId(getChildAt(parent, i)) == name)
				return i;
		
		return -1;
	}
	
	// returns a pointer to the "child"th subobject (indexing starts from 0)
	public int getChildAt(int offset, int child) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert child >= 0 && child <= countChildren(offset) - 1 : child + " while last child is " + (countChildren(offset) - 1);

		return valuesManager.getIntFromBlockOfInts(getIntegerObjectValue(offset), child);
	}

	// disconnects a subobject from a particular complex object
	private final void unregisterChildReference(int parent, int child) throws DatabaseException {
		if (parent == 0) return;
		
		if (ConfigDebug.ASSERTS) assert getObjectKind(parent) == ODRAObjectKind.COMPLEX_OBJECT || getObjectKind(parent) == ODRAObjectKind.AGGREGATE_OBJECT : "parent is not a complex object";
		if (ConfigDebug.ASSERTS) assert parent != child : "cannot disconnect objects from themselves";
		
		setIntegerObjectValue(parent, valuesManager.removeIntFromBlockOfInts(getIntegerObjectValue(parent), child));
	}
	
	// connects a new object to a complex object
	final void registerChildReference(int parent, int child) throws DatabaseException {
		if (parent == 0) return;
		
		if (ConfigDebug.ASSERTS) assert getObjectKind(parent) == ODRAObjectKind.COMPLEX_OBJECT || getObjectKind(parent) == ODRAObjectKind.AGGREGATE_OBJECT : "parent (&" + parent + ") is not a complex/aggregate object (" + getObjectKind(parent) + ")";
		if (ConfigDebug.ASSERTS) assert parent != child : "cannot connect objects to itself";
		if (ConfigDebug.ASSERTS) assert this.countChildren(parent) + 1 < Integer.MAX_VALUE : "too many children";

		setIntegerObjectValue(parent, valuesManager.appendIntToBlockOfInts(getIntegerObjectValue(parent), child));
	}
	
	public final void move(int object, int newparent) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert getObjectKind(newparent) == ODRAObjectKind.COMPLEX_OBJECT || getObjectKind(newparent) == ODRAObjectKind.AGGREGATE_OBJECT : "parent (&" + newparent + ") is not a complex/aggregate object (" + getObjectKind(newparent) + ")";
		// TODO: Cardinality verification
		int parent = this.getObjectParent(object);
		
		unregisterChildReference(parent, object);
		registerChildReference(newparent, object);
		setObjectParent(object, newparent);
	}

	// creates a new reference object pointing at an object having oid == value
	public final int createReferenceObject(int name, int parent, int value) throws DatabaseException , CardinalityException{		
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.REFERENCE_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setIntegerObjectValue(offset, value);
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerBackwardReference(value, offset);
		registerChildReference(parent, offset);
		
		return offset;
	}

	// is the object of address 'offset' a reference object?
	public final boolean isReferenceObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.REFERENCE_OBJECT || this.isReverseReferenceObject(offset);
	}
	
	// is the object of address 'offset' a reverse reference object?
	public boolean isReverseReferenceObject(int offset)throws DatabaseException{
	    return getObjectKind(offset) == ODRAObjectKind.REVERSE_REFERENCE_OBJECT;
	}
	
	// returns the value of a reference object 
	public final int getReferenceObjectValue(int offset) throws DatabaseException {
		return heap.readInteger(offset + VALUE_POS);
	}
	
	// returns the value of a pointer object 
	public final int getPointerObjectValue(int offset) throws DatabaseException {
		return heap.readInteger(offset + VALUE_POS);
	}
	
	// returns the list of reference objects pointing at a particular object
	public final int[] getBackwardReferences(int offset) throws DatabaseException {
		return valuesManager.getIntsFromBlock(getObjectBackwardAddr(offset));
	}

	// updates the value of a reference object. first, the object is unregistered
	// from backward references, then a new backward reference is created
	public final void setReferenceObjectValue(int offset, int value) throws DatabaseException {
		int refval = getReferenceObjectValue(offset);
		unregisterBackwardReference(refval, offset);
		
		heap.writeInteger(offset + VALUE_POS, value);
		
		registerBackwardReference(value, offset);
		
		
	}
	// updates the value of a reverse reference for the reference object. first, the object is unregistered
	// from backward references, then a new backward reference is created
	public final void setReverseReferenceObjectValue(int offset, int revvalue) throws DatabaseException {
		setObjectKind(offset, ODRAObjectKind.REVERSE_REFERENCE_OBJECT);
		
		this.setReversePointerValue(offset, revvalue);
	}
	// updates the value of an existing pointer object
	public final void setPointerObjectValue(int offset, int value) throws DatabaseException {
		heap.writeInteger(offset + VALUE_POS, value);
	}

	// unregisters a backward reference (typically used when reference objects are deleted)
	private final void unregisterBackwardReference(int offset, int ref) throws DatabaseException {
		if (offset == NO_PARENT) return;
		
		int baddr = getObjectBackwardAddr(offset);
		int newbaddr = valuesManager.removeIntFromBlockOfInts(baddr, ref);
		
		setObjectBackwardAddr(offset, newbaddr);
	}
	
	// registers a new backward reference (typically used when reference objects are created)
	private final void registerBackwardReference(int offset, int ref) throws DatabaseException {
		if (offset == NO_PARENT) return;
		
		int baddr = getObjectBackwardAddr(offset);
		int newbaddr = valuesManager.appendIntToBlockOfInts(baddr, ref);
		
		setObjectBackwardAddr(offset, newbaddr);
	}	
	
	// creates a new pointer object
	public final int createPointerObject(int name, int parent, int value) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.POINTER_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setIntegerObjectValue(offset, value);
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		registerChildReference(parent, offset);
		
		return offset;		
	}

	// is the object of address 'offset' a pointer object?
	public final boolean isPointerObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.POINTER_OBJECT;
	}

	// creates a new integer object 
	public final int createIntegerObject(int name, int parent, int value) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.INTEGER_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setIntegerObjectValue(offset, value);
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		registerChildReference(parent, offset);
		
		return offset;
	}
	
	// the object of address 'offset' an integer object?
	public final boolean isIntegerObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.INTEGER_OBJECT;
	}
	
	// creates a new double object
	public final int createDoubleObject(int name, int parent, double value) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));		
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.DOUBLEVAL_LEN);		

		setObjectKind(offset, ODRAObjectKind.DOUBLE_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setDoubleObjectValue(offset, value);
		
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerChildReference(parent, offset);
		
		return offset;
	}

	// is the object of address 'offset' a double object?
	public final boolean isDoubleObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.DOUBLE_OBJECT;
	}
	
	// creates a new string object. values of string objects contain references to separate
	// blocks storing actual string values. when the value is updated, the block may change
	// its location, but the object is not moved. the parameter buffer preallocates the space
	// for future updates (realloc works faster, at the cost of unused space)
	public final int createStringObject(int name, int parent, String value, int buffer) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.STRING_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setIntegerObjectValue(offset, valuesManager.preallocateBlockOfBytes(buffer));
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		setStringObjectValue(offset, value);
		registerChildReference(parent, offset);

		return offset;
	}
	
	// is the object of address 'offset' a string object?
	public final boolean isStringObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.STRING_OBJECT;
	}
	
	// creates a new boolean object
	public final int createBooleanObject(int name, int parent, boolean value) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.BOOLEAN_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.BOOLEAN_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setBooleanObjectValue(offset, value);
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerChildReference(parent, offset);
		
		return offset;
	}
	
	// is the object of address 'offset' a boolean object?
	public final boolean isBooleanObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.BOOLEAN_OBJECT;
	}
	
	// creates a new binary object. the actual value of binary objects
	// is stored in the same way as in the case of string objects
	public final int createBinaryObject(int name, int parent, byte[] value, int buffer) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.BINARY_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setIntegerObjectValue(offset, valuesManager.preallocateBlockOfBytes(buffer));

		setBinaryObjectValue(offset, value);
		
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerChildReference(parent, offset);
		
		return offset;
	}
	
	// is the object of address 'offset' a binary object?
	public final boolean isBinaryObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.BINARY_OBJECT;
	}
	
	// creates a new aggregate object. aggregate objects are complex objects used
	// to speed up name binding. a single aggegate object contains all objects of the
	// same name stored on the a particular level of the object tree. 
	public final int createAggregateObject(int name, int parent, int children, int minCard, int maxCard) throws DatabaseException, CardinalityException {		
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + 2 * Sizes.INTVAL_LEN);		
		
		setObjectKind(offset, ODRAObjectKind.AGGREGATE_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setAggregateObjectMinCard(offset, minCard);
		setAggregateObjectMaxCard(offset, maxCard);	
		setIntegerObjectValue(offset, valuesManager.preallocateBlockOfInts(children));
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerChildReference(parent, offset);

		return offset;
	}
	
	// is the object of address 'offset' an aggregate object?
	public final boolean isAggregate(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.AGGREGATE_OBJECT;
	}
	
	private final boolean isRegisteringToParentPossible(int parent) throws DatabaseException {
		return (!isAggregate(parent)) || (countChildren(parent) < getAggregateObjectMaxCard(parent));
	}
	
	private final boolean isUnregisteringFromParentPossible(int parent) throws DatabaseException {
		return (!isAggregate(parent)) || (countChildren(parent) > getAggregateObjectMinCard(parent));
	}
	
	// delete all reference objects which are connected to the given complex object
	// (used to prevent double deletions when removing objects from the database)
	private final void deleteReferenceChildren(int offset, boolean controlCardinality) throws DatabaseException {
		int[] refchi = getComplexObjectValue(offset);
		
		ODRAObjectKind objkind;
		for (int i = 0; i < refchi.length; i++) {
			objkind = getObjectKind(refchi[i]);
			
			switch (objkind) {
			case COMPLEX_OBJECT:
				deleteReferenceChildren(refchi[i], controlCardinality);
				break;
				
			case REFERENCE_OBJECT:
				deleteObject(refchi[i], controlCardinality, true);
				
				break;
			/*  TODO: Verify if following case is not necessary
			 *  case BINARY_OBJECT: 
				deleteObject(refchi[i], controlCardinality);
				
				break;*/
			}
		}
	}
	
	//creates a new date object
	public final int createDateObject(int name, int parent, Date value) throws DatabaseException, CardinalityException {
		if (!isRegisteringToParentPossible(parent))
			throw new CardinalityException("Unable to create an object. Maximal cardinality has been exceeded", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));
		
		int offset = allocator.malloc(OBJECT_HEADER_LEN + Sizes.LONGVAL_LEN);		

		setObjectKind(offset, ODRAObjectKind.DATE_OBJECT);
		setObjectNameId(offset, name);
		setObjectParent(offset, parent);
		setObjectBackwardAddr(offset, 0);
		setDateObjectValue(offset, value);
		
//////	/special refrences
		this.setSpecialIntegerObjectValue(offset, 0);
		
		registerChildReference(parent, offset);
		
		return offset;
	}
	
	//is the object of address 'offset' a date object?
	public final boolean isDateObject(int offset) throws DatabaseException {
		return getObjectKind(offset) == ODRAObjectKind.DATE_OBJECT;
	}
	
	public final void setDateObjectValue(int offset, Date val) throws DatabaseException {
		heap.writeLong(offset + VALUE_POS, val.getTime());
	}
	
	// deletes objects from the database by unregistering it from its parent object
	// and deallocating the piece of memory used by the object. if the object being deleted is
	// a reference object, a backward reference is removed. if a complex object
	// is being deleted, its subordinary object are also removed. if a binary
	// or string objects are being deleted, also the block containing their
	// values is deallocated. when an object is deleted, the reference objects
	// which point at it are also automatically destroyed.
	public void deleteObject(int offset, boolean controlCardinality) throws DatabaseException
	{
		deleteObject(offset, controlCardinality, false);
	}
	
	// each deletion has to be processed by this method!
	public void deleteObject(int offset, boolean controlCardinality, boolean skipControl) throws DatabaseException, CardinalityException {	

		ODRAObjectKind objkind = getObjectKind(offset);
		int parent = getObjectParent(offset);
		
		// TODO: control cardinality in the end of each instruction (not during)
		if (controlCardinality && !skipControl) {
			if (!isAggregate(parent))
				throw new CardinalityException("Unable to delete an object. Object is obligatory.", getObjectNameId(offset), getAggregateObjectMinCard(offset), getAggregateObjectMaxCard(offset));				
			if (!isUnregisteringFromParentPossible(parent))
				throw new CardinalityException("Unable to delete an object. Minimal cardinality has been reached", getObjectNameId(parent), getAggregateObjectMinCard(parent), getAggregateObjectMaxCard(parent));									
		}

		// do object-specific clean up
		switch (objkind) {
			case AGGREGATE_OBJECT: 
				setAggregateObjectMinCard(offset, 0);
			case COMPLEX_OBJECT: {
				deleteReferenceChildren(offset, controlCardinality);
				
				int[] refchi = getComplexObjectValue(offset);
				
				for (int i = 0; i < refchi.length; i++)
					deleteObject(refchi[i], controlCardinality, true);				
				
				if (getIntegerObjectValue(offset) != 0) {
					valuesManager.getMemoryManager().free(getIntegerObjectValue(offset));
					setIntegerObjectValue(offset, 0);
				}
				
				break;
			}
		}
		
		if (objkind != ODRAObjectKind.AGGREGATE_OBJECT) {
//		delete all reference objects which point at this object
			int[] b = getBackwardReferences(offset);
			
			for (int i = 0; i < b.length; i++)
				deleteObject(b[i], controlCardinality);
			
		}
			
		shallowDeleteObject(offset, controlCardinality, skipControl);
		
	}
	
	// each deletion has to be processed by this method!
	// this method cannot directly or indirectly trigger itself recursively
	protected void shallowDeleteObject(int offset, boolean controlCardinality, boolean skipControl) throws DatabaseException  {

		switch (getObjectKind(offset)) {

			case REFERENCE_OBJECT:
			case REVERSE_REFERENCE_OBJECT:
			{
				int refval = getReferenceObjectValue(offset);
				unregisterBackwardReference(refval, offset);
				
				break;
			}

			case BINARY_OBJECT: {
				valuesManager.deleteBinaryObjectValue(offset);
				
				break;
			}
			case STRING_OBJECT: {
				valuesManager.deleteStringObjectValue(offset);
				
				break;
			}
		}
		
		
		//ordinary object can have special references
		for(int sref: this.getSpecialReferences(offset)){	
			this.specManager.deleteSpecialReference(offset, sref, controlCardinality);			
		}
		
//		 unregister the child object from its parent complex object and destroy it	
		unregisterChildReference(getObjectParent(offset), offset);

		allocator.free(offset);
		
	}
	
	/* *************************************************************************************
	 * this part is used as "macros" to operate on fields of memory structures representing objects
	 * */
	final void setObjectKind(int offset, ODRAObjectKind kind) throws DatabaseException {
		heap.writeByte(offset + OBJKIND_POS, kind.getKindAsByte());
	}
	
	final byte getInternalObjectKind(int offset) throws DatabaseException {
		return heap.readByte(offset + OBJKIND_POS);
	}
	
	public final ODRAObjectKind getObjectKind(int offset) throws DatabaseException {
		byte kind =  heap.readByte(offset + OBJKIND_POS);
		return ODRAObjectKind.getForByte(kind);
		
	}
	
	public final void setObjectNameId(int offset, int name) throws DatabaseException {
		heap.writeInteger(offset + OBJNAME_POS, name);
	}
	
	public final int getObjectNameId(int offset) throws DatabaseException {
		return heap.readInteger(offset + OBJNAME_POS);
	}
	
	final void setObjectParent(int offset, int parent) throws DatabaseException {
		heap.writeInteger(offset + PARENT_POS, parent);
	}
	
	public final int getObjectParent(int offset) throws DatabaseException {
		return heap.readInteger(offset + PARENT_POS);
	}
	
	final void setObjectBackwardAddr(int offset, int baddr) throws DatabaseException {
		heap.writeInteger(offset + BACKWARD_POS, baddr);
	}
	
	private final int getObjectBackwardAddr(int offset) throws DatabaseException {
		return heap.readInteger(offset + BACKWARD_POS);
	}
	
	public final void setIntegerObjectValue(int offset, int val) throws DatabaseException {
		heap.writeInteger(offset + VALUE_POS, val);
	}
	
	public final int getIntegerObjectValue(int offset) throws DatabaseException {
		return heap.readInteger(offset + VALUE_POS);
	}
	
	public final void setDoubleObjectValue(int offset, double val) throws DatabaseException {
		heap.writeDouble(offset + VALUE_POS, val);
	}
	
	public final double getDoubleObjectValue(int offset) throws DatabaseException {
		return heap.readDouble(offset + VALUE_POS);
	}
	
	public final Date getDateObjectValue(int offset) throws DatabaseException {
		return new Date(heap.readLong(offset + VALUE_POS));
	}	
	
	public final void setBooleanObjectValue(int offset, boolean val) throws DatabaseException {
		heap.writeBoolean(offset + VALUE_POS, val);
	}
	
	public final boolean getBooleanObjectValue(int offset) throws DatabaseException {
		return heap.readBoolean(offset + VALUE_POS);
	}

	public final void setStringObjectValue(int offset, String val) throws DatabaseException {
		setBinaryObjectValue(offset, val.getBytes());
	}

	public final String getStringObjectValue(int offset) throws DatabaseException {
		return new String(getBinaryObjectValue(offset));
	}

	public void setBinaryObjectValue(int offset, byte[] val) throws DatabaseException {
		valuesManager.setBinaryObjectValue(offset, val);
	}
	
	public byte[] getBinaryObjectValue(int offset) throws DatabaseException {
		return valuesManager.getBinaryObjectValue(offset);
	}
	
	public final int[] getComplexObjectValue(int offset) throws DatabaseException {
		return valuesManager.getIntsFromBlock(getIntegerObjectValue(offset));
	}
	
	public final void setAggregateObjectMinCard(int offset, int minCard) throws DatabaseException {
		heap.writeInteger(offset + AGGMINCARD_POS, minCard);
	}
	
	public final int getAggregateObjectMinCard(int offset) throws DatabaseException {
		return heap.readInteger(offset + AGGMINCARD_POS);
	}
	
	public final void setAggregateObjectMaxCard(int offset, int minCard) throws DatabaseException {
		heap.writeInteger(offset + AGGMAXCARD_POS, minCard);
	}
	
	public final int getAggregateObjectMaxCard(int offset) throws DatabaseException {
		return heap.readInteger(offset + AGGMAXCARD_POS);
	}
	
	//////////special references management ///////////////////////////////////
	/**
	 * @param offset
	 * @param val - value of special object value
	 * @throws DatabaseException
	 */
	private final void setSpecialIntegerObjectValue(int offset, int val) throws DatabaseException {
//		assert getObjectKind(offset) == COMPLEX_OBJECT : "complex object required";
		heap.writeInteger(offset + SPECIALREF_POS, val);
	}
	
	/**
	 * @param offset
	 * @return value of special object value 
	 * @throws DatabaseException
	 */
	private final int getSpecialIntegerObjectValue(int offset) throws DatabaseException {
//		assert getObjectKind(offset) == COMPLEX_OBJECT : "complex object required";
		return heap.readInteger(offset + SPECIALREF_POS);
	}
		
	/**
	 * @param offset
	 * @return special references offsets for the object
	 * @throws DatabaseException
	 */
	public final int[] getSpecialReferences(int offset) throws DatabaseException {
		int[] refsinfo = valuesManager.getIntsFromBlock(getSpecialIntegerObjectValue(offset)); 
		return refsinfo;
	}
	
	/**
	 * @param offset
	 * @return first special reference offset for the object or 0
	 * @throws DatabaseException
	 */
	public final int getFirstSpecialReference(int offset) throws DatabaseException {
		int ref = valuesManager.getFirstIntFromBlock(getSpecialIntegerObjectValue(offset)); 
		return ref;
	}
	
	/** Sets instance_of reference 
	 * @param offset - instance
	 * @param val - instanceof target value
	 * @throws DatabaseException
	 */
	public final void setInstanceofReferenceValue(int owner, int val) throws DatabaseException{
		assert owner != val : "circular instanceof reference";
		specManager.setInstanceofReferenceValue(owner, val);		
	}
	
	/** Sets role of reference
	 * @param offset - role
	 * @param val - roleof target object
	 * @throws DatabaseException
	 */
	public final void setRoleofReferenceValue(int offset, int val) throws DatabaseException{
//		assert getObjectKind(offset) == COMPLEX_OBJECT : "complex object required";

		specManager.setRoleofReferenceValue(offset, val);
	}
	
	private final void setReversePointerValue(int offset, int revval) throws DatabaseException{
		assert getObjectKind(offset) == ODRAObjectKind.REVERSE_REFERENCE_OBJECT : "reverse reference object required";
	    
		specManager.setReversePointerValue(offset, revval);
	}
	/**
	 * @param offset
	 * @return instanceof reference value
	 * @throws DatabaseException
	 */
	public final int getInstanceofReferenceValue(int offset) throws DatabaseException{
//	assert getObjectKind(offset) == COMPLEX_OBJECT : "complex object required";
		
		return specManager.getInstanceofReferenceValue(offset);
	}
	
	/**
	 * @param offset
	 * @return roleof reference value
	 * @throws DatabaseException
	 */
	public final int getRoleofReferenceValue(int offset) throws DatabaseException{
	//	assert getObjectKind(offset) == COMPLEX_OBJECT : "complex object required";
		return specManager.getRoleofReferenceValue(offset);
		
	}
	/**
	 * @param offset
	 * @return reverse reference 
	 * @throws DatabaseException
	 */
	public final int getReverseReferenceValue(int offset) throws DatabaseException{
		assert this.isReferenceObject(offset) : "reverse reference object required";
		return specManager.getReverseReferenceValue(offset);
		
	}
		
	public final void registerSpecialReference(int owner, int specialref) throws DatabaseException {
		if (owner == 0) return;
		assert specManager.getObjectKind(specialref) >= SpecialReferencesManager.S_INSTANCEOF_REF : "special reference required";
		//TODO : fix assertion: in specManager.getObjectKind(owner) owner is not specialReference 
		//assert (specManager.getObjectKind(specialref) >= SpecialReferencesManager.S_KEYVALUE_IDX) || specManager.getObjectKind(owner) < SpecialReferencesManager.S_INSTANCEOF_REF : "not an object";
		
		setSpecialIntegerObjectValue(owner, valuesManager.appendIntToBlockOfInts(getSpecialIntegerObjectValue(owner), specialref));
	}
	
	public final void registerInstanceOfSpecialReference(int owner, int specialref) throws DatabaseException {
		if (owner == 0) return;
		assert specManager.getObjectKind(specialref) == SpecialReferencesManager.S_INSTANCEOF_REF : "instanceof special reference required";
		//TODO : fix assertion: in specManager.getObjectKind(owner) owner is not specialReference 
		//assert (specManager.getObjectKind(specialref) >= SpecialReferencesManager.S_KEYVALUE_IDX) || specManager.getObjectKind(owner) < SpecialReferencesManager.S_INSTANCEOF_REF : "not an object";
		
		setSpecialIntegerObjectValue(owner, valuesManager.insertIntToBlockOfInts(getSpecialIntegerObjectValue(owner), specialref));
	}
	
	public final void unregisterSpecialReference(int owner, int specialref) throws DatabaseException {
		if (owner == 0) return;
		assert specManager.getObjectKind(specialref) >= SpecialReferencesManager.S_INSTANCEOF_REF : "special reference required";
		//TODO : fix assertion: in specManager.getObjectKind(owner) owner is not specialReference
		//assert (specManager.getObjectKind(specialref) >= SpecialReferencesManager.S_KEYVALUE_IDX) || specManager.getObjectKind(owner) < SpecialReferencesManager.S_INSTANCEOF_REF : "not an object";
		
		setSpecialIntegerObjectValue(owner, valuesManager.removeIntFromBlockOfInts(getSpecialIntegerObjectValue(owner), specialref));
	}
	
	/* *************************************************************************************
	 * this part is used for debugging purposes
	 * */
	public String dump(int start, NameIndex nidx) throws DatabaseException {
		return dump(start, "", nidx).toString();
	}

	private StringBuffer dump(int start, String indent, NameIndex nidx) throws DatabaseException {
	    	ODRAObjectKind kind = this.getObjectKind(start);
		int nameid = this.getObjectNameId(start);
		
		StringBuffer buf = new StringBuffer(start + "\t\t ");

		String oname = nameid >= 0 ? nidx.id2name(nameid) : "NO_NAME";

		switch (kind) {
			case AGGREGATE_OBJECT: {
				buf.append(indent + "A_" + oname + ", refs { " + valuesManager.dumpBlockOfInts(this.getObjectBackwardAddr(start)) + "}\n");
				
				int[] ch = this.getComplexObjectValue(start);
				for (int i = 0; i < ch.length; i++)
					buf.append(dump(ch[i], indent + " ", nidx));
				
				return buf;
			}

			case COMPLEX_OBJECT: {
				buf.append(indent + "C_" + oname + ", refs { " + valuesManager.dumpBlockOfInts(this.getObjectBackwardAddr(start)) + "}\n");
				
				int[] ch = this.getComplexObjectValue(start);
				for (int i = 0; i < ch.length; i++)
					buf.append(dump(ch[i], indent + " ", nidx));
				
				return buf;
			}
				
			case STRING_OBJECT:
				buf.append(indent + "S_" + oname + " = \"" + this.getStringObjectValue(start) + "\"");
				
				break;
				
			case INTEGER_OBJECT:
				buf.append(indent + "I_" + oname + " = " + this.getIntegerObjectValue(start));
				break;
				
			case DOUBLE_OBJECT:
				buf.append(indent + "D_" + oname + " = " + this.getDoubleObjectValue(start));
				break;

			case BOOLEAN_OBJECT:
				buf.append(indent + "B_" + oname + " = " + this.getBooleanObjectValue(start));
				break;
				
			case REFERENCE_OBJECT:
				buf.append(indent + "R_" + oname + " = " + this.getReferenceObjectValue(start));
				break;			
	
			case POINTER_OBJECT:
				buf.append(indent + "P_" + oname + " = " + this.getReferenceObjectValue(start));
				break;
				
			case BINARY_OBJECT:
				buf.append(indent + "N_" + oname + " = ?");
				break;
				
			default:
				buf.append(indent + "unknown_" + oname + " = ?");
		}
		
		buf.append(", refs { " + valuesManager.dumpBlockOfInts(this.getObjectBackwardAddr(start)) + " }");
		
		return buf.append("\n");
	}
	
	public String dumpMemory(boolean verbose) throws DatabaseException {
		String dump = valuesManager.dumpMemory(verbose);
		if (valuesManager.getMemoryManager() != allocator)
			dump += "Seperate memory for odra objects " + allocator.dump(verbose);
		if ((specManager.getMemoryManager() != allocator) && (specManager.getMemoryManager() != valuesManager.getMemoryManager()))
			dump += "Seperate memory for special objects " + specManager.dumpMemory(verbose);
		return dump;
	}

	/**
	 * Returns a total memory size managed by ObjectManager.
	 * 
	 * @return total memory
	 */
	public int getTotalMemory()
	{
		return allocator.getTotalMemory();
	}
	
	/**
	 * Returns a free memory size managed by ObjectManager.
	 * 
	 * @return free memory
	 */
	public int getFreeMemory()
	{
		return allocator.getFreeMemory();
	}
	
	/**
	 * Returns an used memory size managed by ObjectManager.
	 * 
	 * @return used memory
	 */
	public int getUsedMemory()
	{
		return allocator.getUsedMemory();
	}
	
	/* *************************************************************************************
	 * this part is responsible for defragmentation routines
	 * */ 
	
	public String staticDefragmentation() throws DatabaseException {
		if (allocator == valuesManager.getMemoryManager())
			return "Defragmentation is not possible - values must be seperated from objects\n";
		
		String dump = "Values memory before defragmentation:\n"
				+ valuesManager.dumpMemory(false);
		
		// prepare table with association between objects and values
		Hashtable<Integer, Integer> oid2valPtrs = new Hashtable<Integer, Integer>();
		int valoffset;
		
		for(int offset: allocator.getObjectsInSequence()) {
			valoffset = this.getSpecialIntegerObjectValue(offset);
			if (valoffset != 0){
				assert !oid2valPtrs.containsKey(valoffset): "value is already associated with object";
				oid2valPtrs.put(valoffset, offset + SPECIALREF_POS);
			}
			if (!this.isAggregate(offset)) {
				valoffset = this.getObjectBackwardAddr(offset);
				if (valoffset != 0) {
					assert !oid2valPtrs.containsKey(valoffset): "value is already associated with object";
					oid2valPtrs.put(valoffset, offset + BACKWARD_POS);
				}
			}
			if (this.isAggregate(offset) || this.isComplexObject(offset) || this.isStringObject(offset) || this.isBinaryObject(offset)) {
				valoffset = this.getIntegerObjectValue(offset);
				if (valoffset != 0) {
					assert !oid2valPtrs.containsKey(valoffset): "value is already associated with object";
					oid2valPtrs.put(valoffset, offset + VALUE_POS);
				}
			}	
		}
		
		Vector<Integer> valoids = valuesManager.getMemoryManager().getObjectsInSequence();
	
		int ptroffset;
		byte value[];
		for(int i = valoids.size() - 1; i >= 0; i--) {
			valoffset = valoids.get(i);
			assert oid2valPtrs.containsKey(valoffset): "value is not associated with any object";			
			ptroffset = oid2valPtrs.get(valoffset);
			value = valuesManager.getValuesMemoryManager().getData(valoffset); 			
			valoffset = valuesManager.getMemoryManager().malloc(valuesManager.getMemoryManager().free(valoffset), value.length);
			valuesManager.getValuesMemoryManager().setData(valoffset, value);
			this.heap.writeInteger(ptroffset, valoffset);
		}
			
		dump += "Values memory after defragmentation:\n"
			+ valuesManager.dumpMemory(false);
		
		return dump;
	}
	
	// the position of particular fields of object headers
	private final static int OBJKIND_POS = 0; // postion of the byte indicating object's kind
	private final static int OBJNAME_POS = 1; // position of the integer representing object's name
	private final static int PARENT_POS = OBJNAME_POS + Sizes.INTVAL_LEN; // position of the integer representing a reference to object's parent object
	private final static int BACKWARD_POS = PARENT_POS + Sizes.INTVAL_LEN; // position of the integer representing a reference to object's backward reference list
	private final static int SPECIALREF_POS = BACKWARD_POS + Sizes.INTVAL_LEN; // postition of object's special references
	private final static int VALUE_POS = SPECIALREF_POS + Sizes.INTVAL_LEN; // postition of object's value

	private final static int AGGMINCARD_POS = BACKWARD_POS; // postition of minCard aggregate object's value
	private final static int AGGMAXCARD_POS = VALUE_POS + Sizes.INTVAL_LEN; // postition of maxCard aggregate object's value
	
	protected final static int OBJECT_HEADER_LEN = SPECIALREF_POS + Sizes.INTVAL_LEN; // space between objectkind_pos and value_pos

	public final static int MAX_OBJECT_LEN = VALUE_POS + 2 * Sizes.INTVAL_LEN; // space between objectkind_pos and SPECIALREF_POS
			
	public final static int NO_PARENT = 0;
	public final static int NO_NAME = -1;

	

}
