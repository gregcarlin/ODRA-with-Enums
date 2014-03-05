package odra.db.indices.structures;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.updating.IndexRecordLocation;
import odra.sbql.results.runtime.BagResult;
import odra.system.Names;
import odra.system.Sizes;
import odra.system.config.ConfigDebug;

/**
 * The class is responsible for creating, deleting, updating etc. 
 * of database index based on Linear Hashing technique. <br>
 * <br>
 * LinearHashMap structure:<br>
 * <ul>
 * <li>
 * general (properties of index)
 * <ul>
 * <li>counter of records stored in index</li>
 * </ul></li>
 * <li>
 * properties (current properties of linear hash map)
 * <ul>
 * <li>splitBucket</li>  
 * <li>levelBuckets</li>
 * <li>bucketCapacity</li>
 * <li>perSplitLoad</li>
 * <li>perMergeLoad</li>
 * </ul></li>
 * <li>
 * map (a list of bucket objects containing records)
 * <ul>
 * <li>bucket1</li>
 * <li>bucket2</li>
 * <li>...</li>
 * </ul></li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */
public class LinearHashingMap extends Index {

	// FIXME: bucket capacity OVERFLOW can occur
	
	// number of buckets to be split in current hash function level
	private int levelBuckets;
	// maximum number of records stored in one bucket
	private int bucketCapacity;
	// next bucket to be split
	private int splitBucket;
	// split load in percents
	private int perSplitLoad;
	// merge load in percents
	private int perMergeLoad;
	
	/**
	 * Initializes a new LinearHashingMap object using a reference
	 * to an existing LinearHashingMap object (or an empty complex object).
	 * @param oid complex object with or for index description
	 * @param recordType type of records stored in index
	 * @param dataAccess specifies access to data stored in index
	 * @throws DatabaseException
	 */
	public LinearHashingMap(OID oid, RecordType recordType, DataAccess dataAccess) throws DatabaseException {		
		super(oid, recordType, dataAccess);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();		
		
		if (oid.countChildren() == FIELDS_COUNT) {			
			this.splitBucket = getSplitBucketRef().derefInt();
			this.levelBuckets = getLevelBucketsRef().derefInt();
			this.bucketCapacity = getBucketCapacityRef().derefInt();
			this.perSplitLoad = getPerSplitLoadRef().derefInt();
			this.perMergeLoad = getPerMergeLoadRef().derefInt();
		}
	}
	
	/**
	 * Initializes an empty complex object.
	 * @param bucketsCount initial number of buckets
	 * @param bucketCapacity number of records stored in a single bucket
	 * @param perSplitLoad if during insert operation index load (in percents) value is bigger, bucket split is performed
	 * @param perMergeLoad if during remove operation index load (in percents) value is lower, bucket merge is performed    
	 */
	public void initialize(int bucketsCount, int bucketCapacity, int perSplitLoad, int perMergeLoad) throws DatabaseException {
		super.initialize();
		if (ConfigDebug.ASSERTS) assert perMergeLoad < perSplitLoad && perMergeLoad > 0 && perSplitLoad < 100;
		if (ConfigDebug.ASSERTS) assert bucketsCount > 0 && bucketCapacity > 0;
		
		this.recordCount = 0;
		this.splitBucket = 0;
		this.levelBuckets = bucketsCount;
		this.bucketCapacity = bucketCapacity;
		this.perSplitLoad = perSplitLoad;
		this.perMergeLoad = perMergeLoad;

		this.properties_oid = store.createComplexObject(Names.PROPERTIES_ID, oid, PROPERTIES_COUNT);
		
		store.createIntegerObject(Names.SPLITBUCKET_ID, properties_oid, this.splitBucket);
		store.createIntegerObject(Names.LEVELBUCKETS_ID, properties_oid, this.levelBuckets);
		store.createIntegerObject(Names.BUCKETCAPACITY_ID, properties_oid, this.bucketCapacity);
		store.createIntegerObject(Names.PERSPLITLOAD_ID, properties_oid, this.perSplitLoad);
		store.createIntegerObject(Names.PERMERGELOAD_ID, properties_oid, this.perMergeLoad);

		this.index_oid = store.createComplexObject(Names.INDEXSTR_ID, oid, this.levelBuckets * 2);
		
		byte[] emptyBucket = new byte[Sizes.INTVAL_LEN];
		Arrays.fill(emptyBucket, (byte) 0);
		
		for(int i = 0; i < levelBuckets * 2; i++)
			store.createBinaryObject(Names.BUCKET_ID, index_oid, emptyBucket, bucketCapacity * dataAccess.getNonkeyRecordSize() + 1);
			
	}

	@Override
	public Object lookupItem(Object keyValue) throws DatabaseException {
		if (recordCount == 0)
			return dataAccess.getNotFoundValue();
		
		byte[] bucket = getBucket(bucketOffset(keyValue));
		int length = ByteBuffer.wrap(bucket).getInt();
		byte[] nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];
		
		for(int i = 0; i < length; i++) {
			for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
				nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
			if (recordType.keyType.isEqual(keyValue, dataAccess.record2keyValue(nonkeyArray)))
				return dataAccess.record2nonkey(nonkeyArray);
		}
		
		return dataAccess.getNotFoundValue();
	}

	@Override
	public Object[] lookupItemsEqualTo(Object keyValue) throws DatabaseException {
		if (recordCount == 0)
			return new Object[0];
		
		Vector<Object> res = new Vector<Object>(); 
		byte[] bucket;
		keyValue = recordType.filterKeyValue(keyValue);
		try {
			bucket = getBucket(bucketOffset(keyValue));
 		} catch (ClassCastException e) {
 			return new Object[0]; // keyValue has been filtered
 		}
 		
		int length = ByteBuffer.wrap(bucket).getInt();
		byte[] nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];

		if (recordType.isEnumRecordType() && levelBuckets >= recordType.valuesCardinality())
			for(int i = 0; i < length; i++) {
				for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
					nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
				res.add(dataAccess.record2nonkey(nonkeyArray));
			}			
		else 		
			for(int i = 0; i < length; i++) {
				for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
					nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
				if (recordType.keyType.isInKeyValue(keyValue, dataAccess.record2keyValue(nonkeyArray)))
					res.add(dataAccess.record2nonkey(nonkeyArray));
			}
		
		return res.toArray();
	}
	
	@Override
	public Object[] lookupItemsInRange(Object keyValue) throws DatabaseException {
		if (recordCount == 0)
			return new Object[0];
		
		Vector<Object> res = new Vector<Object>();
		byte[] bucket;
		int length;
		byte[] nonkeyArray;
		
		keyValue = recordType.filterKeyValue(keyValue);
		HashSet<Integer> bucketsOffset = bucketsToFilterOffsets(keyValue);
		
		for (int bOff : bucketsOffset) {
			bucket = getBucket(bOff);
			length = ByteBuffer.wrap(bucket).getInt();
			nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];
			
			for(int i = 0; i < length; i++) {
				for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
					nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
				if (recordType.keyType.isInKeyValue(keyValue, dataAccess.record2keyValue(nonkeyArray)))
					res.add(dataAccess.record2nonkey(nonkeyArray));
			}
		}
		
		bucketsOffset = bucketsCompleteOffsets(keyValue, bucketsOffset);

		for (int bOff : bucketsOffset) {
			bucket = getBucket(bOff);
			length = ByteBuffer.wrap(bucket).getInt();
			nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];
			
			for(int i = 0; i < length; i++) {
				for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
					nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
				res.add(dataAccess.record2nonkey(nonkeyArray));
			}
		}
		
		return res.toArray();
	}
	
	@Override
	public void insertItem(Object key, Object nonkey) throws DatabaseException {
	
		int bucketNumber = bucketOffset(dataAccess.key2keyValue(key));
		byte[] bucket = getBucket(bucketNumber);
		int length = ByteBuffer.wrap(bucket).getInt();
		byte[] nonkeyArray = dataAccess.prepareRecordArray(key, nonkey);
		
		ByteBuffer newbucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + (length + 1) * dataAccess.getNonkeyRecordSize());
		newbucket.putInt(length + 1);
		newbucket.put(bucket, Sizes.INTVAL_LEN, length * dataAccess.getNonkeyRecordSize());
		newbucket.put(nonkeyArray);
		bucket = newbucket.array();

		setBucket(bucketNumber, bucket);
		setRecordCount(recordCount + 1);

		if (((recordCount + 1) * 100.0) / ((splitBucket + levelBuckets) * bucketCapacity) >= perSplitLoad)
			split();	
	}
	
	@Override
	public boolean removeItem(Object key, Object nonkey) throws DatabaseException {
		
		if (recordCount == 0)
			return false;
		
		if (((recordCount) * 100.0) / ((splitBucket + levelBuckets) * bucketCapacity) <= perMergeLoad)
			merge();

		byte[] lookupNonkeyArray = dataAccess.prepareRecordArray(key, nonkey);
		boolean removed = false;
		
		int offset = bucketOffset(dataAccess.key2keyValue(key));
		byte[] bucket = getBucket(offset);
		int length = ByteBuffer.wrap(bucket).getInt();
		byte[] nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];
		
		ByteBuffer newBucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + (length - 1) * dataAccess.getNonkeyRecordSize());		
		newBucket.putInt(length - 1);
		
		for(int i = 0; i < length; i++) {
			for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
				nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
			if (!removed && Arrays.equals(lookupNonkeyArray, nonkeyArray)) {
				removed = true;
				setRecordCount(recordCount - 1);
			} else {
				newBucket.put(nonkeyArray);
			}
		}
			
		if (removed)
			setBucket(offset, newBucket.array());
			
		return removed;
		
	}

	@Override
	public IndexRecordLocation getItemLocation(Object key, Object nonkey) throws DatabaseException {
		if ((key instanceof BagResult) && ((BagResult) key).elementsCount() == 0)
			return null;
		return new LHRecordLocation(bucketOffset(dataAccess.key2keyValue(key)), dataAccess.prepareRecordArray(key, nonkey));
	}
	
	public boolean moveItem(IndexRecordLocation beforeRecord, Object newkey, Object nonkey) throws DatabaseException {
		
		if (beforeRecord == null) {
			if (newkey == null)
				return true;
			// only insert
			insertItem(newkey, nonkey);
			return true;
		} 
		if (recordCount == 0)
			return false;
		int bucketNumber = -1; // -1 - only remove
		if (newkey != null)
			bucketNumber = bucketOffset(dataAccess.key2keyValue(newkey));
		
		
		int oldoffset = ((LHRecordLocation) beforeRecord).bucketNumber;
		
		// does not perform movement
		if (oldoffset == bucketNumber) {
			byte[] newnonkey = dataAccess.prepareRecordArray(newkey, nonkey);
			if (!Arrays.equals(((LHRecordLocation) beforeRecord).nonkeyArray, newnonkey)) {
				// modify item record
			
				boolean modyfied = false;
				byte[] bucket = getBucket(oldoffset);
				int length = ByteBuffer.wrap(bucket).getInt();
				byte[] nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];
			
				for(int i = 0; i < length; i++) {
					for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
						nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
					if (!modyfied && Arrays.equals(((LHRecordLocation) beforeRecord).nonkeyArray, nonkeyArray)) {
						for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
							bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j] = newnonkey[j];
						modyfied = true;
						break;
					}
				}
				
				if (modyfied)
					setBucket(oldoffset, bucket);
				else 
					return false;
			}
			return true;
		}

		// remove item
		
		boolean removed = false;
		byte[] bucket = getBucket(oldoffset);
		int length = ByteBuffer.wrap(bucket).getInt();
		byte[] nonkeyArray = new byte[dataAccess.getNonkeyRecordSize()];
		
		ByteBuffer newBucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + (length - 1) * dataAccess.getNonkeyRecordSize());		
		newBucket.putInt(length - 1);
		
		for(int i = 0; i < length; i++) {
			for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
				nonkeyArray[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
			if (!removed && Arrays.equals(((LHRecordLocation) beforeRecord).nonkeyArray, nonkeyArray)) {
				removed = true;
			} else {
				newBucket.put(nonkeyArray);
			}
		}
			
		if (removed)
			setBucket(oldoffset, newBucket.array());
		else 
			return false;


		if (bucketNumber == -1) { 
			setRecordCount(recordCount - 1);

			if (((recordCount) * 100.0) / ((splitBucket + levelBuckets) * bucketCapacity) <= perMergeLoad)
				merge();
			
			return true;
		}
		
		// insert item
		bucket = getBucket(bucketNumber);
		length = ByteBuffer.wrap(bucket).getInt();
		
		ByteBuffer newbucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + (length + 1) * dataAccess.getNonkeyRecordSize());
		newbucket.putInt(length + 1);
		newbucket.put(bucket, Sizes.INTVAL_LEN, length * dataAccess.getNonkeyRecordSize());
		newbucket.put(dataAccess.prepareRecordArray(newkey, nonkey));
		bucket = newbucket.array();

		setBucket(bucketNumber, bucket);
		
		return true;
		
	}

	@Override
	public boolean adjustKey(Object newkey) throws DatabaseException {
		
		if (this.recordType.adjust2KeyValue(dataAccess.key2keyValue(newkey))) {
	
			byte[] emptyBucket = new byte[Sizes.INTVAL_LEN];
			Arrays.fill(emptyBucket, (byte) 0);
			
			byte[][] nonkeys = new byte[recordCount][dataAccess.getNonkeyRecordSize()];
			int nonkeycount = 0;
			for(int bucketNum = 0; bucketNum < index_oid.countChildren(); bucketNum++) {
				byte bucket[] = this.getBucket(bucketNum);
				int length = ByteBuffer.wrap(bucket).getInt();
				
				for(int i = 0; i < length; i++) {
					for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
						nonkeys[nonkeycount][j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
					nonkeycount++;
				}
	
				this.setBucket(bucketNum, emptyBucket);				
			}
			
			setRecordCount(0);
			
			for(byte[] nonkeyArray: nonkeys) {
				Object nonkey = dataAccess.record2nonkey(nonkeyArray);
				insertItem(dataAccess.nonkey2key(nonkey), nonkey);
			}

			return true;
		}		
		
		return false;
			
	}

	private void split() throws DatabaseException {
		if (levelBuckets >= recordType.valuesCardinality())
			return;
			
		int oldBucketOffset = splitBucket;
		int newBucketOffset = splitBucket + levelBuckets;
		
		if (oldBucketOffset + 1 >= levelBuckets) {
			if (recordCount < 2)
				return;
			
			setLevelBuckets(levelBuckets << 1);
			setSplitBucket(0);
			if (this.index_oid.countChildren() < (levelBuckets << 1)) {
				byte[] emptyBucket = new byte[Sizes.INTVAL_LEN];
				Arrays.fill(emptyBucket, (byte) 0);
				
				for(int i = 0; i < levelBuckets; i++)
					store.createBinaryObject(Names.INDEXSTR_ID, this.index_oid, emptyBucket, Sizes.INTVAL_LEN + bucketCapacity * dataAccess.getNonkeyRecordSize());
			}
		} else {
			setSplitBucket(splitBucket + 1);
		}

		byte[] bucket = getBucket(oldBucketOffset);
		int length = ByteBuffer.wrap(bucket).getInt();
		ByteBuffer oldBucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + length * dataAccess.getNonkeyRecordSize());
		ByteBuffer newBucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + length * dataAccess.getNonkeyRecordSize());		
		
		oldBucket.putInt(0);
		newBucket.putInt(0);
		int oldBucketLength = 0;
		byte[] nonkey = new byte[dataAccess.getNonkeyRecordSize()];
		
		for(int i = 0; i < length; i++) {
			for (int j = 0; j < dataAccess.getNonkeyRecordSize(); j++)
				nonkey[j] = bucket[Sizes.INTVAL_LEN + i * dataAccess.getNonkeyRecordSize() + j];
				
			if (bucketOffset(dataAccess.record2keyValue(nonkey)) == oldBucketOffset) {
				oldBucket.put(nonkey);
				oldBucketLength++;
			} else
				newBucket.put(nonkey);
		}
		
		oldBucket.putInt(0, oldBucketLength);
		newBucket.putInt(0, length - oldBucketLength);
			
		setBucket(oldBucketOffset, oldBucket.array());
		setBucket(newBucketOffset, newBucket.array());
	
	}
	
	private void merge() throws DatabaseException {
		
		if (splitBucket == 0)
		{
			if ((recordCount < 1) || (levelBuckets % 2 == 1))
				return;
			
			setLevelBuckets(levelBuckets >> 1);
			splitBucket = levelBuckets - 1;
		}
		else setSplitBucket(splitBucket - 1);
		
		int newBucketOffset = splitBucket;
		int oldBucketOffset = splitBucket + levelBuckets;

		byte[] obucket = getBucket(oldBucketOffset);
		int olength = ByteBuffer.wrap(obucket).getInt();
		
		//TODO: remove or clean oldBucket:)
		
		byte[] nbucket = getBucket(newBucketOffset);
		int nlength = ByteBuffer.wrap(nbucket).getInt();
		
		if (olength == 0)
			setBucket(newBucketOffset, nbucket);
		else if (nlength == 0)
			setBucket(newBucketOffset, obucket);
		else {
			ByteBuffer newBucket = ByteBuffer.allocate(Sizes.INTVAL_LEN + (olength + nlength)* dataAccess.getNonkeyRecordSize());			
			newBucket.putInt(nlength + olength);			
			newBucket.put(nbucket, Sizes.INTVAL_LEN, nlength * dataAccess.getNonkeyRecordSize());
			newBucket.put(obucket, Sizes.INTVAL_LEN, olength * dataAccess.getNonkeyRecordSize());
			
			setBucket(newBucketOffset, newBucket.array());
		}
				
	}
	
	private int bucketOffset(Object keyValue) throws DatabaseException {
		int hashValue = recordType.getHashSeedVal() + recordType.hash(keyValue, levelBuckets << 1);
		return (hashValue % levelBuckets < splitBucket ? 
				hashValue % (levelBuckets << 1) :
				hashValue % levelBuckets); 
	}
	
	private HashSet<Integer> bucketsOffsets(Object keyValue) throws DatabaseException {
		HashSet<Integer> set = recordType.rangeHash(keyValue, levelBuckets << 1);
		HashSet<Integer> realset = new HashSet<Integer>();
		for(int hash : set) {
			hash += recordType.getHashSeedVal();
			hash = (hash % levelBuckets < splitBucket ? 
					hash % (levelBuckets << 1) :
						hash % levelBuckets);
				realset.add(hash); 
		}
		
		return realset;
	}
	
	private HashSet<Integer> bucketsCompleteOffsets(Object keyValue, HashSet<Integer> filterSet) throws DatabaseException {
		if (recordType.isEnumRecordType() && levelBuckets >= recordType.valuesCardinality())
			return bucketsOffsets(keyValue);
		
		if (recordType.isOrderedRecordType() && recordType.isRangeQueryOnAllRangeKeys(keyValue)) {
			
			HashSet<Integer> set = bucketsOffsets(keyValue);
			for(int filterHash : filterSet) {
				set.remove(filterHash);
			}
			return set;
		}
		
		return new HashSet<Integer>();
	}
	
	private HashSet<Integer> bucketsToFilterOffsets(Object keyValue) throws DatabaseException {
		if (recordType.isEnumRecordType() && levelBuckets >= recordType.valuesCardinality())
			return new HashSet<Integer>();
			
		if (recordType.isOrderedRecordType()) {

			if (!recordType.isRangeQuery(keyValue)) {
				if (recordType.isInQuery(keyValue))
					return bucketsOffsets(keyValue);
				HashSet<Integer> realset = new HashSet<Integer>();
				realset.add(bucketOffset(keyValue));
				return realset;
			}

			if (!recordType.isRangeQueryOnAllRangeKeys(keyValue)) {
				return bucketsOffsets(keyValue);
			}
			
			HashSet<Integer> set = recordType.limitHash(keyValue, levelBuckets << 1);
			HashSet<Integer> filterSet = new HashSet<Integer>();
			for(int hash : set) {
				hash += recordType.getHashSeedVal();
				hash = (hash % levelBuckets < splitBucket ? 
						hash % (levelBuckets << 1) :
							hash % levelBuckets);
				filterSet.add(hash); 
			}
			return filterSet;			
		}

		return bucketsOffsets(keyValue);
		
	}
	
	/**
	 * @param bucketCount new number of last bucket to be split in current hash function level
	 */
	private void setLevelBuckets(int levelBuckets) throws DatabaseException {
		// ZMIANY TYLKO WYKLADNICZE DIVIDE/2 LUB ADD
		this.levelBuckets = levelBuckets;
		getLevelBucketsRef().updateIntegerObject(levelBuckets);
	}	

	/**
	 * @param SplitBucket MO�E TYLKO INC I DEC
	 */
	private void setSplitBucket(int splitBucket) throws DatabaseException {
		this.splitBucket = splitBucket;
		getSplitBucketRef().updateIntegerObject(splitBucket);
	}

	/**
	 *
	 */
	
	/** 
	 * @return single bucket
	 * @param number specifies bucket to be retrieved
	 */
	private byte[] getBucket(int number) throws DatabaseException {
		return getBucketRef(number).derefBinary();
	}
	
	/**
	 * @param number specifies bucket to be updated
	 * @param bucket new bucket value
	 */
	private void setBucket(int number, byte[] bucket) throws DatabaseException {
		getBucketRef(number).updateBinaryObject(bucket);
	}
	
	/***********************************
	 * access to subobjects describing the LinearHashMap state
	 * */
	
	private final OID getLevelBucketsRef() throws DatabaseException {
		return properties_oid.getChildAt(LBTS_POS);
	}
	
	private final OID getBucketCapacityRef() throws DatabaseException {
		return properties_oid.getChildAt(BTCP_POS);
	}

	private final OID getSplitBucketRef() throws DatabaseException {
		return properties_oid.getChildAt(SBT_POS);
	}
	
	private final OID getPerSplitLoadRef() throws DatabaseException {
		return properties_oid.getChildAt(PSLOAD_POS);
	}
	
	private final OID getPerMergeLoadRef() throws DatabaseException {
		return properties_oid.getChildAt(PMLOAD_POS);
	}
	
	private final OID getBucketRef(int number) throws DatabaseException {
		return index_oid.getChildAt(number);
	}
	
	private final static int SBT_POS = 0;
	private final static int LBTS_POS = 1;
	private final static int BTCP_POS = 2;
	private final static int PSLOAD_POS = 3;
	private final static int PMLOAD_POS = 4;
	
	private final static int PROPERTIES_COUNT = 5;
	
}	
