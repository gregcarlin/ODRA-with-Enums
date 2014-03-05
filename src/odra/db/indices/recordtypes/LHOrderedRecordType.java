package odra.db.indices.recordtypes;

import java.util.HashSet;

import odra.db.DatabaseException;
import odra.db.OID;

/**
 * This is a super class for ordered record types.<br>
 * <p>Ordered record types has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>is obligatory when hash function span all possible values (non-enum record type)</li>
 * </ul></p>
 * <p>LH ordered record type complex object structure:<ul>
 * <li> ID of record type (according to RecordTypeKind "enumeration")</li>
 * <li> ID of key type (according to KeyTypeKind "enumeration")</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public abstract class LHOrderedRecordType extends RecordType {
	
	Object min, max;
	
	/**
	 * @param obligatory true if current key (record) requires to have value specified in index call
	 */
	public LHOrderedRecordType(boolean obligatory) {
		super(obligatory);
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public LHOrderedRecordType(OID oid) throws DatabaseException {
		super(oid);
		min = getMin();
		max = getMax();
	}
		
	protected abstract int rangeSelect(Object value, int rnum);
	
	@Override
	public int hash(Object x, int rnum) {
		return hash(rangeSelect(x, rnum), getLHLevel(rnum), rnum);		
	}
	
	protected int hash(int range, int bitnum, int rnum) {
		return invBits(range % (1 << bitnum), bitnum) * (rnum >> bitnum) 
		+ range / (1 << bitnum);
	}

	@Override
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {
		if (isRangeQuery(keyValue)) {
			Object[] range = ((Object[])((Object[])keyValue)[1]);
			int bitnum = getLHLevel(rnum);					
			int rangecur = rangeSelect(((Object[]) range)[0], rnum);
			int rangemax = rangeSelect(((Object[]) range)[1], rnum);
			
			if (isEnumRecordType() && rnum >= valuesCardinality()) {
				if (!(Boolean) ((Object[]) range)[2]) rangecur++;
				if (!(Boolean) ((Object[]) range)[3]) rangemax--;
			}
			
			int rcount = rangemax - rangecur + 1;
			if (rcount < 0) rcount = 0;
			HashSet<Integer> set = new HashSet<Integer>(rcount);
			while (rangecur <= rangemax) {
				set.add(hash(rangecur, bitnum, rnum));
				rangecur++;
			}
			return set;
		}
		
		return super.rangeHash(keyValue, rnum);
	
	}
	
	@Override
	public HashSet<Integer> limitHash(Object keyValue, int rnum) {
		if (isRangeQuery(keyValue)) {
			Object[] range = ((Object[])((Object[])keyValue)[1]);
			HashSet<Integer> set = new HashSet<Integer>(2);
			int bitnum = getLHLevel(rnum);			
			if (!isBelowMin(((Object[]) range)[0])) {
				int rangenum = rangeSelect(((Object[]) range)[0], rnum);
				set.add(hash(rangenum, bitnum, rnum));			
			}	
			if (!isEqualAboveMax(((Object[]) range)[1]) ||
					(!(Boolean) ((Object[]) range)[3])) {
				int rangenum = rangeSelect(((Object[]) range)[1], rnum);			
				set.add(hash(rangenum, bitnum, rnum));
			}
			return set;
		}
		
		return super.rangeHash(keyValue, rnum);
	}
	
	@Override
	public boolean isOrderedRecordType() {
		return true;
	}
	
	protected final boolean isBelowMin(Object value) {
		return keyType.isLess(value, min);
	}
	
	protected final boolean isEqualAboveMax(Object value){
		return (keyType.isLess(max, value) || keyType.isEqual(max, value));
	}	
	
	protected final int getLHLevel(int rnum) {
		int bitnum = 0;
		while((rnum & (1 << bitnum)) == 0) bitnum++;
		return bitnum;
	}
	
	protected final int invBits(int num, int bitnum) {
		int rev = 0;
		for(int i = 0; i < bitnum; i++) {
			rev <<= 1;
			rev += (num & 1);
			num >>= 1;
		}
		return rev;
	}
	
}
