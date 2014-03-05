package odra.db.indices.updating.triggers;

import java.util.Collection;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.dataaccess.DBObjectToKeyAccess;
import odra.db.indices.updating.IndexRecordLocation;
import odra.db.indices.updating.LoggingSBQLInterpreter;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.ReferenceResult;

/**
 * This class defines methods for triggers run on index nonkey value objects
 * in order to maintain convergence of data and indices.
 * 
 * @author tkowals
 * @version 1.0
 */
public class NonkeyUpdateTrigger extends UpdateTrigger {
		
	ReferenceResult nonkey;
	Index idx;
	IndexRecordLocation beforeRecord;	
	Collection<OID> beforeoids;
	
	NonkeyUpdateTrigger(OID oid, OID nonkey) throws DatabaseException {
		super(oid);
		this.nonkey = new ReferenceResult(nonkey);
		idx = getIndex();
	}

	NonkeyUpdateTrigger(OID oid) {
		super(oid);
	}
	
	public static NonkeyUpdateTrigger initialize(OID oid, String name, OID dbidxoid) throws DatabaseException {
		NonkeyUpdateTrigger nonkeyUT = new NonkeyUpdateTrigger(oid);
		nonkeyUT.initialize(UpdateTrigger.NONKEY_TYPE, name, dbidxoid);
		return nonkeyUT;
	}
	
	public final void prepareUpdate() throws DatabaseException {
		// check if nonkey is not disabled
		if (nonkey.value != null) {
			LoggingSBQLInterpreter interpreter = LoggingSBQLInterpreter.getLoggingSBQLInterpreterInstance(getModule());
			try {
				beforeRecord = idx.getItemLocation(((DBObjectToKeyAccess) idx.dataAccess).nonkey2key(nonkey, interpreter), nonkey);
			} catch (Exception e) {
				beforeRecord = null;
			}
			beforeoids = interpreter.getOidlog();
		}
	}
	
	public final void update() throws DatabaseException {		
		// check if nonkey is not disabled
		if (nonkey.value != null) {
			LoggingSBQLInterpreter interpreter = LoggingSBQLInterpreter.getLoggingSBQLInterpreterInstance(getModule());
			Object newkey;
			try {
				newkey = ((DBObjectToKeyAccess) idx.dataAccess).nonkey2key(nonkey, interpreter);
				if ((newkey instanceof BagResult) && ((BagResult) newkey).elementsCount() == 0)
					newkey = null;
				else if (idx.adjustKey(newkey) && ((beforeRecord != null)))			
					return; // jumps to finally block 
			} catch (Exception e) {
				newkey = null;
			} finally {
				getKeyUpdateTrigger().refreshKeyTriggers(beforeoids, interpreter.getOidlog());
			}
			
			idx.moveItem(beforeRecord, newkey, nonkey);
		}
	}
	
	protected KeyUpdateTrigger getKeyUpdateTrigger() throws DatabaseException {
		return (KeyUpdateTrigger) UpdateTrigger.generateTrigger(this.getUpdateTriggerRef(), nonkey.value);
	}

	void refreshKeyTriggers(Collection<OID> beforeoids, Collection<OID> afteroids) throws DatabaseException {
		beforeoids.remove(nonkey.value);
		afteroids.remove(nonkey.value);
		for(OID obj: beforeoids)
			if (!afteroids.remove(obj))
				store.removeIndexUpdateTrigger(obj, oid, nonkey.value);
		for(OID obj: afteroids)
			store.setIndexUpdateTrigger(obj, oid, nonkey.value);
	}
	
	public void enableAutomaticUpdating(OID obj) throws DatabaseException {
		nonkey = new ReferenceResult(obj);
		store.setIndexUpdateTrigger(obj, oid, nonkey!=null?nonkey.value:null);		
		
		LoggingSBQLInterpreter interpreter = LoggingSBQLInterpreter.getLoggingSBQLInterpreterInstance(getModule());
		try {	
			Object newkey = ((DBObjectToKeyAccess) idx.dataAccess).nonkey2key(nonkey, interpreter);
			if (!(newkey instanceof BagResult)) {
				idx.adjustKey(newkey);
				idx.insertItem(newkey, nonkey);
			}
		} catch (Exception E) {
			// Occurs for aggregate objects and during creation of objects
		} finally {
			UpdateTrigger updateTrig = UpdateTrigger.generateTrigger(this.getUpdateTriggerRef(), nonkey.value);
			for (OID subobj: interpreter.getOidlog())			
				store.setIndexUpdateTrigger(subobj, updateTrig.oid, nonkey.value);
		}
	}
		
	public void disableAutomaticUpdating(OID obj) throws DatabaseException {			
		nonkey = new ReferenceResult(obj);

		store.removeIndexUpdateTrigger(obj, oid, nonkey.value);
		
		if (beforeoids == null) {
			LoggingSBQLInterpreter interpreter = LoggingSBQLInterpreter.getLoggingSBQLInterpreterInstance(getModule());
			try {
				Object key = ((DBObjectToKeyAccess) idx.dataAccess).nonkey2key(nonkey, interpreter);
				if (!(key instanceof BagResult))
					idx.removeItem(key, nonkey);			
			} catch (Exception e) {
				
			}
			beforeoids = interpreter.getOidlog();
		} else 
			idx.moveItem(beforeRecord, null, nonkey);
		
		UpdateTrigger updateTrig = UpdateTrigger.generateTrigger(this.getUpdateTriggerRef(), nonkey.value);
		for (OID subobj: beforeoids)
			store.removeIndexUpdateTrigger(subobj, updateTrig.oid, nonkey.value);
		
		beforeoids = null;
		nonkey.value = null;
	}
	
}
