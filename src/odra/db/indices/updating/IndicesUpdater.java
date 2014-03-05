package odra.db.indices.updating;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.updating.triggers.KeyUpdateTrigger;
import odra.db.indices.updating.triggers.NonkeyUpdateTrigger;
import odra.db.indices.updating.triggers.NonkeypathUpdateTrigger;
import odra.db.indices.updating.triggers.UpdateTrigger;
import odra.db.indices.updating.triggers.RootUpdateTrigger;

/**
 * This class provides mechanism to perform automatic index updating.
 * It is used with IndexableStore.
 * 
 * @author tkowals
 * @version 1.0
 */
public class IndicesUpdater {

	IndexableStore store;
	
	OID obj;

	UpdateTrigger[] idxupdtrig;
	
	public IndicesUpdater(IndexableStore store, OID obj) throws DatabaseException {
		this.store = store;
		this.obj = obj;
		OID[][] iuttable = this.store.getIndexUpdateTriggers(obj);
		idxupdtrig = new UpdateTrigger[iuttable.length];
		for(int i = 0; i < iuttable.length; i++) {
			idxupdtrig[i] = UpdateTrigger.generateTrigger(iuttable[i][0], iuttable[i][1]);
			if (idxupdtrig[i] instanceof NonkeyUpdateTrigger) 
				((NonkeyUpdateTrigger) idxupdtrig[i]).prepareUpdate(); // NonkeyUpdateTrigger is superclass of KeyObjectUpdateTrigger
		}
	}
	
	public void updateIndicesAfterCreate(OID createdoid) throws DatabaseException {
		for(UpdateTrigger updateTrig: idxupdtrig) 
			if (updateTrig instanceof KeyUpdateTrigger)
				((KeyUpdateTrigger) updateTrig).update();
			else if (updateTrig instanceof NonkeyUpdateTrigger)
				if (obj.isAggregateObject())
					((NonkeyUpdateTrigger) updateTrig).enableAutomaticUpdating(createdoid);
				else 
					((NonkeyUpdateTrigger) updateTrig).update();
			else if (updateTrig instanceof RootUpdateTrigger) 
				((RootUpdateTrigger) updateTrig).enableAutomaticUpdatingInSubObjects(obj, createdoid);
			else if (updateTrig instanceof NonkeypathUpdateTrigger)
				((NonkeypathUpdateTrigger) updateTrig).enableAutomaticUpdatingInSubObjects(obj, createdoid);
	}
	
	public void updateIndices() throws DatabaseException {
		for(UpdateTrigger updateTrig: idxupdtrig) 
			if (updateTrig instanceof NonkeyUpdateTrigger) // NonkeyUpdateTrigger is superclass of KeyUpdateTrigger
				((NonkeyUpdateTrigger) updateTrig).update();
	}
		
	public void disableUpdateTriggers() throws DatabaseException {
		for(UpdateTrigger updateTrig: idxupdtrig) 
			updateTrig.disableAutomaticUpdating(obj);		
	}
		
}
