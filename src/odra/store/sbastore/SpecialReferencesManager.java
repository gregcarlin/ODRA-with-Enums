package odra.store.sbastore;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.store.io.IHeap;
import odra.store.memorymanagement.AbstractMemoryManager;
import odra.store.memorymanagement.IMemoryManager;
import odra.system.Sizes;

public class SpecialReferencesManager {

	private final IMemoryManager specAllocator;

	private final IHeap specHeap;
	
	private IObjectManager objectManager;
	
	public SpecialReferencesManager(AbstractMemoryManager specAllocator) {
		this.specAllocator = specAllocator;
		this.specHeap = specAllocator.getHeap();
	}
	
	public void setObjectManager(IObjectManager objectManager) {
		this.objectManager = objectManager;
	}
	
	void open() throws DatabaseException {
	
	}
	
	void close() {
		specHeap.close();
	}
	
	//////////special references management ///////////////////////////////////

	/** create instanceof special reference
	 * @param owner
	 * @param value
	 * @return offset of the instanceof reference
	 * @throws DatabaseException
	 */
	private final int createInstanceOfReference( int owner, int value) throws DatabaseException {
		int offset = this.createSpecialReference(owner, value, S_INSTANCEOF_REF); 
//		register special reference in the owner 
		objectManager.registerInstanceOfSpecialReference(owner, offset);
//		register backwards for instanceof refrence in the same way as for regular references
		objectManager.registerSpecialReference(value, offset);
		return offset;
	}
	

	/** create roleof special reference
	 * @param owner
	 * @param value
	 * @return offset of the roleof reference
	 * @throws DatabaseException
	 */
	final int createRoleOfReference(int owner, int value) throws DatabaseException {				
		int offset = this.createSpecialReference(owner, value, S_ROLEOF_REF);
//		register special reference in the owner 
		objectManager.registerSpecialReference(owner, offset);
		//FIXME we do not register backward reference 
		//instead of we need to register S_SUBROLE_REF
		return offset;
	}
	/** create reverse special reference
	 * @param owner
	 * @param value
	 * @return offset of the roleof reference
	 * @throws DatabaseException
	 */
	final int createReverseReference(int owner, int value) throws DatabaseException {				
		int offset = this.createSpecialReference(owner, value, S_REVERSE_REF);
//		register special reference in the owner 
		objectManager.registerSpecialReference(owner, offset);
		return offset;
	}
	/** create special referenced
	 * @param owner
	 * @param value
	 * @param kind - kind of a special reference
	 * @return special reference offset
	 * @throws DatabaseException
	 */
	final int createSpecialReference(int owner, int value, byte kind) throws DatabaseException {		

		int offset = specAllocator.malloc(MAX_SPECIALOBJECT_LEN);		
		
		setObjectKind(offset, kind);
		//this is an owner, not a parent but we reuse parent position
		setObjectOwner(offset, owner);
		setSpecialReferenceValue(offset, value);
		
		return offset;
	}
	
	/** delete special reference
	 * @param offset
	 * @throws DatabaseException
	 */
	final void deleteSpecialReference(int owner, int offset, boolean controlCardinality) throws DatabaseException{

		int target = getSpecialReferenceValue(offset);
		
		switch(getObjectKind(offset)){
		case S_INSTANCEOF_REF:
//			unregister backward in the instanceof reference target
			objectManager.unregisterSpecialReference(target, offset);
			objectManager.unregisterSpecialReference(getObjectOwner(offset), offset);
			specAllocator.free(offset);	
		break;
		case S_ROLEOF_REF:
			assert false : "unimplemented special refrence";
			break;
		case S_IDXUPDT_TRIG:
			objectManager.unregisterSpecialReference(owner, offset);
			specAllocator.free(offset);	
			break;
		case S_REVERSE_REF:			
			objectManager.unregisterSpecialReference(owner, offset);
			if (this.getReverseReferenceValue(target) == owner)
				objectManager.deleteObject(target, controlCardinality);
			specAllocator.free(offset);
		    
		    break;
		default: 
				assert false : "unimplemented special refrence";
		}		
	}

	/* *************************************************************************************
	 * this part is used as "macros" to operate on fields of memory structures representing objects
	 * */
	final void setObjectKind(int offset, byte kind) throws DatabaseException {
		specHeap.writeByte(offset + OBJKIND_POS, kind);
	}
	
	final byte getObjectKind(int offset) throws DatabaseException {
		return specHeap.readByte(offset + OBJKIND_POS);
	}
	
	final void setObjectOwner(int offset, int parent) throws DatabaseException {
		specHeap.writeInteger(offset + OWNER_POS, parent);
	}
	
	final int getObjectOwner(int offset) throws DatabaseException {
		return specHeap.readInteger(offset + OWNER_POS);
	}
	
	/** set the special reference value
	 * @param offset
	 * @param val - special reference target
	 * @throws DatabaseException
	 */
	final void setSpecialReferenceValue(int offset, int val) throws DatabaseException {
		specHeap.writeInteger(offset + VALUE_POS, val);
	}
	
	/** 
	 * @param offset
	 * @return special reference target
	 * @throws DatabaseException
	 */
	final int getSpecialReferenceValue(int offset) throws DatabaseException {
		return specHeap.readInteger(offset + VALUE_POS);
	}

	/** Sets instance_of reference 
	 * @param offset - instance
	 * @param val - instanceof target value
	 * @throws DatabaseException
	 */
	final void setInstanceofReferenceValue(int offset, int val) throws DatabaseException{
		
		int ref = objectManager.getFirstSpecialReference(offset); 
		if(isInstanceof(offset, ref))
			deleteSpecialReference(offset, ref, true);
		
		if(val != 0)
			createInstanceOfReference(offset, val);
		
	}
	
	/** Sets role of reference
	 * @param offset - role
	 * @param val - roleof target object
	 * @throws DatabaseException
	 */
	final void setRoleofReferenceValue(int offset, int val) throws DatabaseException{

		int[] refs = objectManager.getSpecialReferences(offset);
		for(int ref:refs){
			if(isRoleof(ref)){
				deleteSpecialReference(offset, ref, true);
				break; //this is only one reference
			}
		}
		createRoleOfReference(offset, val);		
		
	}
	/** Sets reverse reference
	 * @param offset - owner
	 * @param val - reverse target object
	 * @throws DatabaseException
	 */
	final void setReversePointerValue(int offset, int val) throws DatabaseException{

		int[] refs = objectManager.getSpecialReferences(offset);
		
		for(int ref:refs){
			if(isReversePointer(ref)){
				int oldreverse = this.getSpecialReferenceValue(ref);
				this.setSpecialReferenceValue(ref, val);
				objectManager.deleteObject(oldreverse, true);
				return;//this is only one reference
			}
		}
		
		createReverseReference(offset, val);		
		
	}
	/**
	 * @param offset - instance
	 * @return instanceof reference value
	 * @throws DatabaseException
	 */
	final int getInstanceofReferenceValue(int offset) throws DatabaseException{

		int ref = objectManager.getFirstSpecialReference(offset); 
		if(isInstanceof(offset, ref))
			return getSpecialReferenceValue(ref);
				
		return 0;
	}
	
	/**
	 * @param offset - role
	 * @return roleof reference value
	 * @throws DatabaseException
	 */
	final int getRoleofReferenceValue(int offset) throws DatabaseException{

		int[] refs =  objectManager.getSpecialReferences(offset);
		for(int ref:refs){
			if(isRoleof(ref))
				return getSpecialReferenceValue(ref);
				
		}
		return 0;
		
	}
	/**
	 * @param offset - reference
	 * @return reverse reference value
	 * @throws DatabaseException
	 */
	final int getReverseReferenceValue(int offset) throws DatabaseException{

		int[] refs =  objectManager.getSpecialReferences(offset);
		for(int ref:refs){
			if(isReversePointer(ref))
				return getSpecialReferenceValue(ref);
				
		}
		return 0;
		
	}
	// is the object of address 'offset' an instanceof special object?
	final boolean isInstanceof(int owner, int offset) throws DatabaseException {
		return getObjectKind(offset) == S_INSTANCEOF_REF && getObjectOwner(offset) == owner;
	}
	
//	 is the object of address 'offset' an roleof special object?
	final boolean isRoleof(int offset) throws DatabaseException {
		return getObjectKind(offset) == S_ROLEOF_REF;
	}
//	 is the object of address 'offset' an reverse reference special object?
	final boolean isReversePointer(int offset) throws DatabaseException {
		return getObjectKind(offset) == S_REVERSE_REF;
	}
//	 is the object of address 'offset' an backward reference special object?
	final boolean isBackward(int owner, int offset) throws DatabaseException {
		return getObjectKind(offset) == S_BACKWARD_REF  && getSpecialReferenceValue(offset) == owner;
	}

//	 is the object of address 'offset' an index update trigger special object?
	final boolean isIndexUpdateTrigger(int offset) throws DatabaseException {
		return getObjectKind(offset) == S_IDXUPDT_TRIG;
	}
	
	//////////indexing special information management ///////////////////////////////////
	// TODO: Move this part to index updating package 
	
	/** Sets index update trigger value  
	 * @param offset - object
	 * @param idxupdinfo - index update info address <- is stored in owner position of special reference
	 * @param nonkey - nonkey object reference 
	 * @throws DatabaseException
	 */
	public final void setIndexUpdateTrigger(int owner, int idxupdinfo, int nonkey) throws DatabaseException{
		int offset = createSpecialReference(idxupdinfo, nonkey, S_IDXUPDT_TRIG);

//		register special reference in the owner 
		objectManager.registerSpecialReference(owner, offset);
		
	}
	
	/** Gets information about indexes associated with object   
	 * @param offset - object
	 * @return table containing pairs (idxupdoid, nonkeyoid)
	 * @throws DatabaseException
	 */
	public final int[][] getIndexUpdateTriggers(int offset) throws DatabaseException{
		Vector<int[]> idxpairs = new Vector<int[]>();   
		for(int ref : objectManager.getSpecialReferences(offset)){
			if(isIndexUpdateTrigger(ref))
				idxpairs.add(new int[] {getObjectOwner(ref), getSpecialReferenceValue(ref)});
				
		}
		return idxpairs.toArray(new int[idxpairs.size()][2]);
	}

	/** Removes index update trigger  
	 * @param offset - object
	 * @param idxupdinfo - index update info address <- is stored in owner position of special reference 
	 * @throws DatabaseException 
	 */
	public void removeIndexUpdateTrigger(int offset, int idxupdinfo, int nonkey) throws DatabaseException {
		if (offset == 0) return;
		
		for(int sref: objectManager.getSpecialReferences(offset))
			if (isIndexUpdateTrigger(sref) && (getObjectOwner(sref) == idxupdinfo) && (getSpecialReferenceValue(sref) == nonkey)) { 
				deleteSpecialReference(offset, sref, true);
				return;
			}
		
		System.err.println("Couldn't find special index value to " + idxupdinfo);
		
	}
	
	/* *************************************************************************************
	 * this part is used for debugging purposes
	 * */
	
	final String dumpMemory(boolean verbose) throws DatabaseException {
		return specAllocator.dump(verbose);
	}
	
	final IMemoryManager getMemoryManager() {
		return specAllocator;
	}

	final int getFreeMemory() {
		return specAllocator.getFreeMemory();
	}

	final int getTotalMemory() {
		return specAllocator.getTotalMemory();
	}

	final int getUsedMemory() {
		return specAllocator.getUsedMemory();
	}	
	
//	 the position of particular fields of object headers
	private final static int OBJKIND_POS = 0; // postion of the byte indicating object's kind
	private final static int VALUE_POS = 1; // postition of special reference's  value
	private final static int OWNER_POS = VALUE_POS + Sizes.INTVAL_LEN; // position of the integer representing a reference to object's parent object	

	public final static int MAX_SPECIALOBJECT_LEN = OWNER_POS + Sizes.INTVAL_LEN;
	
	//special references (new values must be > S_INSTANCEOF_REF)
	public final static byte S_INSTANCEOF_REF = 31;
	public final static byte S_BACKWARD_REF = 31; // S_INSTANCEOF_REF is S_BACKWARD_REF in the same time 
	public final static byte S_ROLEOF_REF = 33;
	public final static byte S_REVERSE_REF = 34;
//	public final static byte S_SUBCLASSOF_REF = 33;
//	public final static byte S_SUBROLE_REF = 34;	

	//special trigger objects (new values must be > S_IDXUPDT_TRIG)
	public final static byte S_IDXUPDT_TRIG = 41;

}
