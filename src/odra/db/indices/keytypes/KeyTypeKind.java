package odra.db.indices.keytypes;

import odra.db.DatabaseException;

/**
 * This class contains only static public members.
 * Is used as factory of key types and an "enumeration" to describe
 * kinds of types of keys used in index.
 * 
 * @author tkowals
 */

public class KeyTypeKind {

	private KeyTypeKind() {
		
	}
	
	/**
	 * @param keyTypeID id of keytype to generate according to "enumeration"
	 * @return key type object
	 * @throws DatabaseException
	 */
	public static KeyType generateKeyType(int keyTypeID) throws DatabaseException {
		switch (keyTypeID) {
		case INTEGERKEYTYPE_ID: return new IntegerKeyType();
		case STRINGKEYTYPE_ID: return new StringKeyType();
		case DOUBLEKEYTYPE_ID: return new DoubleKeyType();
		case BOOLEANKEYTYPE_ID: return new BooleanKeyType();
		case DATEKEYTYPE_ID: return new DateKeyType();
		case REFERENCEKEYTYPE_ID: return new ReferenceKeyType();
		case INDEXUNIQUENKKEYTYPE_ID: return new IndexUniqueNKKeyType();
		case ASTTEXTKEYTYPE_ID: return new ASTTextKeyType();
		}		
		return null;
	}

	/**
	 * ID of keytype according to "enumeration"
	 */
	public final static int 
		INTEGERKEYTYPE_ID = 1,
		STRINGKEYTYPE_ID = 2,
		DOUBLEKEYTYPE_ID = 3,
		BOOLEANKEYTYPE_ID = 4,
		DATEKEYTYPE_ID = 5,
		REFERENCEKEYTYPE_ID = 6,
		MULTIKEYTYPE_ID = 7,
		INDEXUNIQUENKKEYTYPE_ID = 8,
		ASTTEXTKEYTYPE_ID = 9;

}
