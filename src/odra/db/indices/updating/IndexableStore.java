package odra.db.indices.updating;

import java.util.Date;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.store.DefaultStore;
import odra.store.sbastore.IObjectManager;
import odra.store.sbastore.ObjectManager;

/**
 * This class extends ODRA default store to maintain convergence of data and indices.
 * For automatic index updating this class must be used as ODRA store.
 * 
 * @author tkowals
 * @version 1.0
 */
public class IndexableStore extends DefaultStore {

	public IndexableStore(IObjectManager manager) {
		super(new IndexableObjectManager((ObjectManager) manager));
		((IndexableObjectManager) this.manager).setStore(this);  
	}

	
	/**
	 * @param obj object
	 * @param outoid associated with object Index Update Trigger object
	 * @param nonkey object indexed with participation of outoid in associated index 
	 * @throws DatabaseException
	 */
	public void setIndexUpdateTrigger(OID obj, OID outoid, OID nonkey) throws DatabaseException {
		((IndexableObjectManager) manager).setIndexUpdateTrigger(OID2offset(obj), OID2offset(outoid), OID2offset(nonkey));
	}
	
	/**
	 * Gets information about indexes associated with the given object
	 * @param obj object
	 * @return table containing pairs (Index Update Trigger oid, Nonkey object oid)
	 * @throws DatabaseException
	 */
	public OID[][] getIndexUpdateTriggers(OID obj) throws DatabaseException {
		int[][] table = ((IndexableObjectManager) manager).getIndexUpdateTriggers(OID2offset(obj));
		OID[][] result = new OID[table.length][];
		for (int i = 0; i < table.length; i++) 
			result[i] = new OID[] {offset2OID(table[i][0]), offset2OID(table[i][1])};
		return result;
	}
	
	public void removeIndexUpdateTrigger(OID obj, OID ouioid, OID nonkey) throws DatabaseException {
		((IndexableObjectManager) manager).removeIndexUpdateTrigger(OID2offset(obj), OID2offset(ouioid), OID2offset(nonkey));
	}
	
	// TODO: overload AS1 and AS2... methods if necessary
	
	// Following methods apply only to KeyObjectUpdateTriggers
	
	@Override
	public void updateBooleanObject(OID obj, boolean val) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj);
		super.updateBooleanObject(obj, val);
		idxupdater.updateIndices();
	}

	@Override
	public void updateDateObject(OID obj, Date date) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj);
		super.updateDateObject(obj, date);
		idxupdater.updateIndices();
	}

	@Override
	public void updateDoubleObject(OID obj, double val) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj); 
		super.updateDoubleObject(obj, val);
		idxupdater.updateIndices();
	}

	@Override
	public void updateIntegerObject(OID obj, int val) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj); 
		super.updateIntegerObject(obj, val);
		idxupdater.updateIndices();
	}

	@Override
	public void updatePointerObject(OID obj, OID val) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj);
		super.updatePointerObject(obj, val);
		idxupdater.updateIndices();
	}

	// works only for obj belonging to key (references are not allowed for nonkeys)
	@Override
	public void updateReferenceObject(OID obj, OID val) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj);
		super.updateReferenceObject(obj, val);
		idxupdater.updateIndices();

	}

	@Override
	public void updateStringObject(OID obj, String val) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, obj);
		super.updateStringObject(obj, val);
		idxupdater.updateIndices();
	}

	// Following methods apply to KeyObjectUpdateTriggers and NonkeyObjectUpdateTriggers

	@Override
	public OID createBooleanObject(int name, OID parent, boolean value) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createBooleanObject(name, parent, value);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj; 
	}

	@Override
	public OID createDateObject(int name, OID parent, Date value) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createDateObject(name, parent, value);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}

	@Override
	public OID createDoubleObject(int name, OID parent, double value) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createDoubleObject(name, parent, value);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}

	@Override
	public OID createIntegerObject(int name, OID parent, int value) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createIntegerObject(name, parent, value);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}

	@Override
	public OID createPointerObject(int name, OID parent, OID value) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createPointerObject(name, parent, value);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}

	@Override
	public OID createReferenceObject(int name, OID parent, OID value) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createReferenceObject(name, parent, value);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}

	@Override
	public OID createStringObject(int name, OID parent, String value, int buffer) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createStringObject(name, parent, value, buffer);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}

	// Following methods apply potentially to all ObjectUpdateTriggers
	
	@Override
	public OID createAggregateObject(int name, OID parent, int children, int minCard, int maxCard) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createAggregateObject(name, parent, children, minCard, maxCard); 
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}
	
	@Override
	public OID createComplexObject(int name, OID parent, int children) throws DatabaseException {
		IndicesUpdater idxupdater = new IndicesUpdater(this, parent);
		OID obj = super.createComplexObject(name, parent, children);
		idxupdater.updateIndicesAfterCreate(obj);
		return obj;
	}
	
	@Override
	public void move(OID obj, OID newparent) throws DatabaseException {
		IndicesUpdater deleteidxupdater = new IndicesUpdater(this, obj); //oldidxupdater similar to delete...
		deleteidxupdater.disableUpdateTriggers();
		
		IndicesUpdater createidxupdater = new IndicesUpdater(this, newparent); //newidxupdater similar to create...
		super.move(obj, newparent);
		deleteidxupdater.updateIndices();		
		createidxupdater.updateIndicesAfterCreate(obj);
	}

	// delete operation overridden in IndexableObjectManager, because delete is performed recursively by ObjectManager
	
}
