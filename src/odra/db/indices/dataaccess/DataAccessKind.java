package odra.db.indices.dataaccess;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sessions.Session;

/**
 * This class contains only static public members.
 * Is used as factory for data access stored in database and 
 * an "enumeration" to describe kinds of access to indexed data. 
 * 
 * @author tkowals
 * @version 1.0
 */

public final class DataAccessKind {
	
	private DataAccessKind() {
		
	}

	/**
	 * Generates Data Access object serialized in database store. 
	 * @param oid address of data access structure in database store
	 * @param module current session module
	 * @return Data access object
	 * @throws DatabaseException
	 */
	public static DataAccess generateDataAccess(OID oid, DBModule module) throws DatabaseException {
		switch (oid.getChildAt(DATAACCESSKIND_POS).derefInt()) {
		case NAMEINDEXACCESS_ID: return new NameIndexAccess(oid);
		case DBOBJDIRKEYACCESS_ID: return new DBObjectDirectKeyAccess(oid, module);
		case DBOBJ2KEYACCESS_ID: return new DBObjectToKeyAccess(oid, module);
		case OIDACCESS_ID: return new OIDAccess(oid);
		case TEMPORARYRESULTACCESS_ID: return new TemporaryResultAccess(oid, Session.getTemporaryIndexResult(oid.getParent()));
		}		
		return null;
	}
	
	final static int NAMEINDEXACCESS_ID = 0;
	final static int DBOBJDIRKEYACCESS_ID = 1;
	final static int DBOBJ2KEYACCESS_ID = 2;
	final static int OIDACCESS_ID = 3;
	final static int TEMPORARYRESULTACCESS_ID = 4;
	
	private final static int DATAACCESSKIND_POS = 0;
	
}
