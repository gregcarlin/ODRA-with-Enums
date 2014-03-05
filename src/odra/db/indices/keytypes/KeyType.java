package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.dataaccess.DataAccess;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.StructResult;

/**
 * This class is a super-class for description of all types of keys stored 
 * in indexes. 
 * The goal is to make data indexing technique indepentent of indexed data key type
 * <br>
 * Key can in some cases represent set of key values or range of key values.
 * 
 * @author tkowals
 * @version 1.0
 */

public abstract class KeyType {

	DataAccess dataAccess;
	
	protected KeyType() {
		
	}
	
	/**
	 * @param dataAccess sets data access type associated with current index
	 */
	public final void setDataAccess(DataAccess dataAccess) {
		this.dataAccess = dataAccess;		
	}
	
	/**
	 * @param keyValue single key value
	 * @param cmpKeyValue single key value
	 * @return true if keyValue key value is equal to cmpKeyValue key value
	 */
	public boolean isEqual(Object keyValue, Object cmpKeyValue) {
		return (keyValue.equals(cmpKeyValue));				
	}
	
	/**
	 * @param keyValue key value - can be single or an array of key values (or an empty array)
	 * @param cmpKeyValue single key value
	 * @return true if cmpKeyValue key value can be found in keyValue key values set 
	 */
	public boolean isInKeyValue(Object keyValue, Object cmpKeyValue) {
		if (keyValue instanceof Object[]) {
			if ((Integer) ((Object[])keyValue)[0] == RANGE_KEY) {
				Object[] range = ((Object[])((Object[])keyValue)[1]);
				if (isLess(cmpKeyValue, range[0]))
					return false;
				if (!(Boolean) range[2])
					if (isEqual(cmpKeyValue, range[0]))
						return false;
				if (isLess(range[1], cmpKeyValue))
					return false;
				if (!(Boolean) range[3])
					if (isEqual(cmpKeyValue, range[1]))					
						return false;		
				return true;
			}
			if ((Integer) ((Object[])keyValue)[0] == IN_KEY) {
				Object[] keys = ((Object[])((Object[])keyValue)[1]);
				for(int i = 0; i < keys.length; i++)
					if (isEqual(keys[i], cmpKeyValue))
						return true;
				return false;
			}
		}

		return isEqual(keyValue, cmpKeyValue);
	}
	
	/**
	 * @param keyValue single key value
	 * @param cmpKeyValue single key value
	 * @return true if keyValue key value is less then cmpKeyValue key value
	 */
	public abstract boolean isLess(Object keyValue, Object cmpKeyValue);

	/**
	 * @param keyValue single key value
	 * @param rnum twice a current number of buckets to be split
	 * @return hash code calculated on given key value 
	 */
	
	public abstract int hash(Object keyValue, int rnum);
	
	/**
	 * @return id of type according to RecordTypeKind class
	 */
	public abstract int getKeyTypeID();
	
	/**
	 * @param buffer bytebuffer with direcly serialized key value
	 * @return key value retrieved from buffer
	 * @throws DatabaseException
	 */
	public abstract Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException;

	/**
	 * Method is used when key value is stored with nonkey value in index record 
	 * (DBObjectDirectKeyAccess). For string, real or boolean key values are accessed 
	 * through OID in ReferenceResult. Integers are directly written to index. 
	 * @param keyValue contains key value   
	 * @return array with serialized key value (for integers) od OID pointing on key value
	 * @throws DatabaseException
	 */
	public abstract byte[] key2Array(Object key) throws DatabaseException;

	/**
	 * Method is used directly with OIDAccess.
	 * Other data access use this method to convert key to key value when dealing with
	 * values wrapped by ODRA ReferenceResult or IntegerResult.
	 * @param key key contains key value (wrapped by ODRA ReferenceResult or IntegerResult)
	 * @return key value
	 * @throws DatabaseException
	 */
	public abstract Object key2KeyValueDirectly(Object key) throws DatabaseException;

	/**
	 * @param key individual key, range of keys or array of keys
	 * @return key value, range of key values or array of key values
	 * @throws DatabaseException
	 */
	public Object key2KeyValue(Object key) throws DatabaseException {
		if (key instanceof ReferenceResult) 
			return key2KeyValueDirectly(key);	
		BinderResult binres = (BinderResult) key;
		Object[] keyValue = new Object[2];
		if (binres.getName().equals(EQUAL_KEY_LABEL)) {
			return key2KeyValue(binres.value);
		} else if (binres.getName().equals(IN_KEY_LABEL)) {
			keyValue[0] = new Integer(IN_KEY);
			if (binres.value instanceof BagResult) {
				BagResult bagres = (BagResult) binres.value;
				Object[] inKeyValue = new Object[bagres.elementsCount()];
				for (int i = bagres.elementsCount() - 1; i >= 0; i--) 			
					inKeyValue[i] = key2KeyValue(bagres.elementAt(i));
				keyValue[1] = inKeyValue;	
			} else {
				Object[] inKeyValue = new Object[1];
				inKeyValue[0] = key2KeyValue(binres.value);
				keyValue[1] = inKeyValue;
			}
		} else if (binres.getName().equals(RANGE_KEY_LABEL)) {
			keyValue[0] = new Integer(RANGE_KEY);
			StructResult stres = (StructResult) binres.value;
			Object[] rangeKeyValue = new Object[stres.fieldsCount()];
			for (int i = stres.fieldsCount() - 1; i >= 0; i--) {
				if (i > 1) rangeKeyValue[i] = ((BooleanResult) stres.fieldAt(i)).value;
				else rangeKeyValue[i] = key2KeyValue(stres.fieldAt(i));
			keyValue[1] = rangeKeyValue; 
			}
		} else assert false:"Wrong key label";
		return keyValue;
	}
	
	/**
	 * Method is used with some enum or range record types to store significant key values.
	 * @param name name of object to contain the value
	 * @param parentoid complex object where value should be stored 
	 * @param value key value to be written to ODRA store
	 * @throws DatabaseException
	 */
	public abstract void createValueObject(int name, OID parentoid, Object value) throws DatabaseException;
	
	/**
	 * Method is used with some enum or range record types to store significant key values.
	 * @param oid object containing key value
	 * @param value key value to be written to ODRA store
	 * @throws DatabaseException
	 */
	public abstract void updateValueObject(OID oid, Object value) throws DatabaseException;
	
	/**
	 * Method is used with some enum or range record types to read significant key values from store.
	 * @param oid object containing key value
	 * @return key value
	 * @throws DatabaseException
	 */
	public abstract Object OIDToValue(OID oid) throws DatabaseException;
	
	/**
	 * ID indicating that key value is single
	 */
	public static final int EQUAL_KEY = 0;
	/**
	 * ID indicating that key value is a set of individual values
	 */
	public static final int IN_KEY = 1;
	/**
	 * ID indicating that key value is a range of values
	 */
	public static final int RANGE_KEY = 2;
	
	/**
	 * Label used with index call indicating that key value is a single
	 */
	public static final String EQUAL_KEY_LABEL = "$equal";
	/**
	 * Label used with index call indicating that key value is a set of individual values
	 */
	public static final String IN_KEY_LABEL = "$in";
	/**
	 * Label used with index call indicating that key value is a range of values
	 */
	public static final String RANGE_KEY_LABEL = "$range";
	
}
