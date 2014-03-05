package odra.db.indices.updating;

import odra.db.DatabaseException;
import odra.store.memorymanagement.AbstractMemoryManager;
import odra.store.sbastore.ObjectManager;
import odra.store.sbastore.SpecialReferencesManager;
import odra.store.sbastore.ValuesManager;

/**
 * This class extends ODRA object manager to maintain convergance of data and indices.
 * 
 * @author tkowals
 * @version 1.0
 */
public class IndexableObjectManager extends ObjectManager {

	IndexableStore store;
	
	private IndexableObjectManager(AbstractMemoryManager allocator, ValuesManager valuesManager, SpecialReferencesManager specManager) {
		super(allocator, valuesManager, specManager);
	}

	IndexableObjectManager(ObjectManager manager) {
		this((AbstractMemoryManager) manager.getMemoryManager(), manager.getValuesManager(), manager.getSpecialReferencesManager());
		this.entry = manager.getEntry();
	}

	public void setStore(IndexableStore store) {
		this.store = store;
	}

	//////////indexing special information management ///////////////////////////////////
	
	/** Sets index key value  
	 * @param offset - object
	 * @param idxupdinfo - object update information address
	 * @param nonkey - nonkey object reference 
	 * @throws DatabaseException
	 */
	public final void setIndexUpdateTrigger(int offset, int idxupdinfo, int nonkey) throws DatabaseException{
		specManager.setIndexUpdateTrigger(offset, idxupdinfo, nonkey);		
	}
	
	/** Gets information about indexes associated with given object as key value  
	 * @param offset - object
	 * @return table containing pairs (idxupdatetrigger, nonkey) for which object is used
	 * @throws DatabaseException
	 */
	public final int[][] getIndexUpdateTriggers(int offset) throws DatabaseException{
		return specManager.getIndexUpdateTriggers(offset);		
	}

	/** Remove index key value  
	 * @param offset - object
	 * @param idxupdinfo - object update information address 
	 * @throws DatabaseException 
	 * @throws DatabaseException
	 */
	public void removeIndexUpdateTrigger(int offset, int idxupdinfo, int nonkey) throws DatabaseException {
		specManager.removeIndexUpdateTrigger(offset, idxupdinfo, nonkey);
	}
	
	
	@Override
	protected void shallowDeleteObject(int offset, boolean controlCardinality, boolean skipControl) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(store, store.offset2OID(offset));
		idxupdater.disableUpdateTriggers();
		super.shallowDeleteObject(offset, controlCardinality, skipControl);
		idxupdater.updateIndices();
	}
	
}
