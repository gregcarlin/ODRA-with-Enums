package odra.db.indices.recordtypes;

import odra.db.DatabaseException;
import odra.db.OID;

/**
 * This class contains only static public members.
 * Is used as factory for record type stored in database and 
 * an "enumeration" to describe kinds of records in index 
 * 
 * @author tkowals
 * @version 1.0
 */

public class RecordTypeKind {

	private RecordTypeKind() {
		
	}
	
	/**
	 * Generates Record Type object serialized in database store. 
	 * @param oid address of record type structure in database store
	 * @param recordTypeID id of a type of a record
	 * @return Record type object
	 * @throws DatabaseException
	 */
	public static RecordType generateRecordType(OID oid, int recordTypeID) throws DatabaseException {   
		switch (recordTypeID) {
			case MULTIKEYLHRANGERECORDTYPE_ID: return new MultiKeyLHRangeRecordType(oid);
			case MULTIKEYLHENUMRECORDTYPE_ID: return new MultiKeyEnumRecordType(oid);
			case MULTIKEYRECORDTYPE_ID: return new MultiKeyRecordType(oid);
		}
		return generateRecordType(oid);
	}
	
	/**
	 * Generates Record Type object serialized in database store. 
	 * @param oid address of data access structure in database store
	 * @return Record type object
	 * @throws DatabaseException
	 */
	public static RecordType generateRecordType(OID oid) throws DatabaseException {
		switch (oid.getChildAt(RECORDTYPEKIND_POS).derefInt()) {
		case SIMPLERECORDTYPE_ID: return new SimpleRecordType(oid);
		case INTEGERLHRANGERECORDTYPE_ID: return new IntegerLHRangeRecordType(oid);
		case STRINGLHRANGERECORDTYPE_ID: return new StringLHRangeRecordType(oid);
		case DOUBLELHRANGERECORDTYPE_ID: return new DoubleLHRangeRecordType(oid);
		case LHENUMRECORDTYPE_ID: return new LHEnumRecordType(oid);
		case ENUMRECORDTYPE_ID: return new EnumRecordType(oid);
		case INTEGERRANGEENUMRECORDTYPE_ID: return new IntegerRangeEnumRecordType(oid);
		case BOOLEANENUMRECORDTYPE_ID: return new BooleanEnumRecordType(oid);
		case DATELHRANGERECORDTYPE_ID: return new DateLHRangeRecordType(oid);
		}		
		assert false : "unknown record type";
		return null;
	}
	
	final static int 
		SIMPLERECORDTYPE_ID = 1,
		MULTIKEYRECORDTYPE_ID = 2,
		MULTIKEYLHRANGERECORDTYPE_ID = 3,
		INTEGERLHRANGERECORDTYPE_ID = 4,
		DOUBLELHRANGERECORDTYPE_ID = 5,
		STRINGLHRANGERECORDTYPE_ID = 6,
		MULTIKEYLHENUMRECORDTYPE_ID = 7,
		LHENUMRECORDTYPE_ID = 8,
		ENUMRECORDTYPE_ID = 9,
		INTEGERRANGEENUMRECORDTYPE_ID = 10,
		BOOLEANENUMRECORDTYPE_ID = 11,
		DATELHRANGERECORDTYPE_ID = 12;
	
	private final static int RECORDTYPEKIND_POS = 0;

}
