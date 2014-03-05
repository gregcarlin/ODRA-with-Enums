package odra.security;

/**
 * This class is used for adding users roles
 *
 * @author Lukasz Zaczek
 */

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;

public class RoleManager {
	
	public static void registerSystemRole(String rolename) throws DatabaseException {
		
		DBModule sysmod = Database.getSystemModule();
		DBModule admod = Database.getModuleByName(Database.ADMIN_SCHEMA);

		// register the new role in the system variable $sysroles
		OID agg = sysmod.findFirstByNameId(Names.S_SYSROLES_ID, admod.getDatabaseEntry());

		if (agg == null)
			throw new DatabaseException("Invalid database configuration");

		OID role1 = sysmod.createComplexObject(Names.namesstr[Names.S_SYSROLES_ID], agg, 0);
		sysmod.createStringObject("rolename", role1, rolename, 0);
		sysmod.createAggregateObject("privileges", role1, 0 );
	}
	
	public static boolean hasSystemRole(String rolename) throws DatabaseException {

		DBModule admod = Database.getModuleByName("admin");

		OID agg = admod.findFirstByNameId(Names.S_SYSROLES_ID, admod.getDatabaseEntry());
		
		if (agg == null)
			throw new DatabaseException("Invalid database configuration");
		
		OID[] roles = agg.derefComplex();
		for (OID oid : roles) {
			if (oid.derefComplex()[0].derefString().equals(rolename))
				return true;
		}
		
		return false;
	}
	
	public static void unregisterSystemRole(String rolename) throws DatabaseException {
	
	}
	
}
