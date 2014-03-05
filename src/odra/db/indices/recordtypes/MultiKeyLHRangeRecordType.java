package odra.db.indices.recordtypes;

import java.util.HashSet;

import odra.db.DatabaseException;
import odra.db.OID;

/**
 * This class makes support for indexing objects using multiple attributes.
 * This is an non-enum and ordered multikey record type.<br>
 * <p>Ordered and non-enum multikey record type has following properties:<ul>
 * <li>some keys support range queries</li>
 * </ul></p>
 * <p>Multikey LH range record type complex object structure:<ul>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> complex object containing description of all subrecord types
 * <ul>
 * <li> first record type description</li>
 * <li> ... following recordtypes</li>
 * </ul></li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class MultiKeyLHRangeRecordType extends MultiKeyRecordType {
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public MultiKeyLHRangeRecordType(OID oid) throws DatabaseException {
		super(oid);
	}

	/**
	 * @param recordType array of subrecord types
	 */
	public MultiKeyLHRangeRecordType(RecordType[] recordType) {
		super(recordType);		
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.MULTIKEYLHRANGERECORDTYPE_ID;
	}
	
	@Override
	public boolean isEnumRecordType() {
		return false;
	}
	
	public int hash(Object x, int rnum) {
		int hash = STARTHASH;
		for (int i = 0; i < recordType.length; i++)
			hash = hashJoin(hash, recordType[i].hash(((Object[]) x)[i], rnum), recordType[i].hashseed, rnum);
			
		return hash + rnum;
	}
	
	protected int hashJoin(int sumHash, int keyHash, int hashSeed, int rnum) {
		return (sumHash + hashSeed + keyHash) % rnum;
	}
	
	@Override
	public HashSet<Integer> rangeHash(Object x, int rnum) {
		HashSet<Integer> sumset = new HashSet<Integer>();
		sumset.add(STARTHASH);
		for (int i = 0; i < recordType.length; i++) { 
			HashSet<Integer> keyset = recordType[i].rangeHash(((Object[]) x)[i], rnum);
			HashSet<Integer> mulset = new HashSet<Integer>();
			for(Integer keyhash : keyset) 
				for(Integer sumhash : sumset)
					mulset.add(hashJoin(sumhash, keyhash, recordType[i].hashseed, rnum));
			sumset = mulset;	
		} 
		return sumset;
	}

	@Override
	public HashSet<Integer> limitHash(Object x, int rnum) {
/*		TODO: Reanalyze this scenario
 		HashSet<Integer> rangesum = new HashSet<Integer>();
		HashSet<Integer> limitsum = new HashSet<Integer>();
		HashSet<Integer> prangesum;
		HashSet<Integer> plimitsum;
		rangesum.add(STARTHASH);
		for (int i = 0; i < recordType.length; i++) { 
			prangesum = rangesum;
			plimitsum = limitsum;
			rangesum = new HashSet<Integer>();
			limitsum = new HashSet<Integer>();
			HashSet<Integer> rangeset = recordType[i].rangeHash(((Object[]) x)[i], rnum);
			if (recordType[i].isRangeQuery(((Object[]) x)[i])) {
				for(int phash : plimitsum) 
					for(int hash : rangeset)
						limitsum.add(hashJoin(phash, hash, recordType[i].hashseed, rnum));								
				HashSet<Integer> limitset = recordType[i].limitHash(((Object[]) x)[i], rnum);
				for(int hash : limitset) 
					rangeset.remove(hash);
				for(int phash : prangesum) 
					for(int hash : limitset)
						limitsum.add(hashJoin(phash, hash, recordType[i].hashseed, rnum));
				for(int phash : prangesum)
					for(int hash : rangeset)
						rangesum.add(hashJoin(phash, hash, recordType[i].hashseed, rnum));
			} else {
				for(int phash : plimitsum)
					for(int hash : rangeset)
						limitsum.add(hashJoin(phash, hash, recordType[i].hashseed, rnum));
				for(int phash : prangesum)
					for(int hash : rangeset)
						rangesum.add(hashJoin(phash, hash, recordType[i].hashseed, rnum));
			}
		}
			
		return limitsum;*/
		return super.limitHash(x, rnum);
	}	
	
	@Override
	public boolean adjust2KeyValue(Object newKeyValue) throws DatabaseException {
		boolean adjusted = false;
		
		for (int i = 0; i < recordType.length; i++) 
			if (recordType[i].adjust2KeyValue(((Object[]) newKeyValue)[i]))
				adjusted = true;
	
		return adjusted;
	}

	@Override
	public Object filterKeyValue(Object keyValue){
		
		for (int i = 0; i < recordType.length; i++) 
			((Object[]) keyValue)[i] = recordType[i].filterKeyValue(((Object[]) keyValue)[i]);
			
		return keyValue;
		
	}

	
}
