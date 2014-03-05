package odra.store;

import java.util.Hashtable;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.store.sbastore.ObjectManager;

/**
 * TransientStore - defines local store for user session
 * is it based on the same memory manager and object manager as default persistent store
 * except for reference object.
 * Local reference objects can lead to persistent object
 * default object manager is not prepared for such a situation
 * this class represent a walk around of the problem (a temporary "quick fix")
 * and should be solved more generally in the future
 * @author Radek Adamus
 *since: 2007-02-21
 *last modified: 2007-04-02
 * added global instanceof reference manegement 
 *@version 1.0
 */
public class TransientStore extends DefaultStore {
	private Hashtable <Integer, OID> globalReferences = new Hashtable <Integer, OID>();
	private Hashtable <Integer, OID> globalInstanceOfReferences = new Hashtable <Integer, OID>();
	/**
	 * @param store
	 */
	public TransientStore(ObjectManager store) {
		super(store);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#createReferenceObject(int, odra.db.OID, odra.db.OID)
	 */
	@Override
	public OID createReferenceObject(int name, OID parent, OID value) throws DatabaseException {
		if(value != null){
			if(this != value.getStore()){
				OID ref = super.createReferenceObject(name, parent, null);
				this.globalReferences.put(this.OID2offset(ref), value);
				return ref;
			}
		}
		return super.createReferenceObject(name, parent, value);
	}

	
	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#derefReferenceObject(odra.db.OID)
	 */
	@Override
	public OID derefReferenceObject(OID obj) throws DatabaseException {
		OID externalTgt = this.globalReferences.get(this.OID2offset(obj));
		if(externalTgt != null)
			return externalTgt;
		return super.derefReferenceObject(obj);
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#updateReferenceObject(odra.db.OID, odra.db.OID)
	 */
	@Override
	public void updateReferenceObject(OID obj, OID val) throws DatabaseException {
		if(val != null){
			if(this != val.getStore()){
				this.globalReferences.put(this.OID2offset(obj), val);
			}
		}
		super.updateReferenceObject(obj, null);
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#delete(odra.db.OID)
	 */
	@Override
	public void delete(OID obj, boolean controlCardinality) throws DatabaseException {
		int offset = OID2offset(obj);
		if( manager.isComplexObject(offset) || manager.isAggregate(offset)){
			deleteAllChildren(obj, controlCardinality);
		}
		this.globalReferences.remove(offset);
		this.globalInstanceOfReferences.remove(offset);
		
		manager.deleteObject(offset, controlCardinality);
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#deleteAllChildren(odra.db.OID)
	 */
	@Override
	public void deleteAllChildren(OID parent, boolean controlCardinality) throws DatabaseException {
		assert manager.isComplexObject(OID2offset(parent)) || manager.isAggregate(OID2offset(parent));
		
		int[] children = manager.getComplexObjectValue(OID2offset(parent));

		for (int i = 0; i < children.length; i++)
		{
			if( manager.isComplexObject(children[i])|| manager.isAggregate(children[i])){
				deleteAllChildren(this.offset2OID(children[i]), controlCardinality);
			}
			this.globalReferences.remove(children[i]);
			this.globalInstanceOfReferences.remove(children[i]);
			
			manager.deleteObject(children[i], controlCardinality, true);
		}
		
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#derefInstanceOfReference(odra.db.OID)
	 */
	@Override
	public OID derefInstanceOfReference(OID obj) throws DatabaseException {
		OID cls = this.globalInstanceOfReferences.get(OID2offset(obj));
		if(cls != null)
			return cls;
		return super.derefInstanceOfReference(obj);
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#isClassInstance(odra.db.OID)
	 */
	@Override
	public boolean isClassInstance(OID obj) throws DatabaseException {
		if(this.globalInstanceOfReferences.get(OID2offset(obj)) != null)
			return true;
		return super.isClassInstance(obj);
	}

	/* (non-Javadoc)
	 * @see odra.store.DefaultStore#setInstanceOfReference(odra.db.OID, odra.db.OID)
	 */
	@Override
	public void setInstanceOfReference(OID obj, OID clsObj) throws DatabaseException {
		if(clsObj != null){
			if(Database.getStore() == clsObj.getStore()){
				this.globalInstanceOfReferences.put(OID2offset(obj), clsObj);
			}else
				super.setInstanceOfReference(obj, clsObj);
		}else { //clsObj == null
			if(this.globalInstanceOfReferences.get(OID2offset(obj)) != null){
				this.globalInstanceOfReferences.remove(OID2offset(obj));
			}else
				super.setInstanceOfReference(obj, null);
		}
	}
	
}
