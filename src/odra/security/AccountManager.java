package odra.security;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;

public class AccountManager {
	public static boolean hasUserAccount(String username, String password) throws DatabaseException {
		
		String encodedPass;
		try {
			encodedPass = PasswordUtility.getPasswordDigest(password);
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}

		DBModule admod = Database.getModuleByName("admin");

		OID agg = admod.findFirstByNameId(Names.S_SYSUSERS_ID, admod.getDatabaseEntry());
		
		if (agg == null)
			throw new DatabaseException("Invalid database configuration");
		
		OID[] users = agg.derefComplex();
		for (OID oid : users) {
			if (oid.derefComplex()[0].derefString().equals(username) && oid.derefComplex()[1].derefString().equals(encodedPass))
				return true;
		}
		
		return false;
	}
	public static boolean hasUserAccount(String username, String password, String rolename) throws DatabaseException {
		String encodedPass;
		try {
			encodedPass = PasswordUtility.getPasswordDigest(password);
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}

		DBModule admod = Database.getModuleByName("admin");

		OID agg = admod.findFirstByNameId(Names.S_SYSUSERS_ID, admod.getDatabaseEntry());
		
		if (agg == null)
			throw new DatabaseException("Invalid database configuration");
		
		OID[] users = agg.derefComplex();
		for (OID oid : users) {
			if (oid.derefComplex()[0].derefString().equals(username) && oid.derefComplex()[1].derefString().equals(encodedPass) && oid.derefComplex()[2].derefString().equals(rolename))
				return true;
		}
		
		return false;
	} 
	// TODO: to be replaced by sbql code
	public static void registerUserAccount(String username, String password) throws DatabaseException {
		String encodedPass;
		try {
			encodedPass = PasswordUtility.getPasswordDigest(password);
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}

		DBModule sysmod = Database.getSystemModule();
		DBModule admod = Database.getModuleByName(Database.ADMIN_SCHEMA);

		// register the new account in the system variable $users
		OID agg = sysmod.findFirstByNameId(Names.S_SYSUSERS_ID, admod.getDatabaseEntry());

		if (agg == null)
			throw new DatabaseException("Invalid database configuration");

		OID user1 = sysmod.createComplexObject(Names.namesstr[Names.S_SYSUSERS_ID], agg, 0);
		sysmod.createStringObject("username", user1, username, 0);
		sysmod.createStringObject("password", user1, encodedPass, 0);
	}
	
	public static void registerUserAccount(String username, String password, String rolename) throws DatabaseException {
		
		if ( !RoleManager.hasSystemRole(rolename) )
			throw new DatabaseException("Role does not exist!");
		
		String encodedPass;
		try {
			encodedPass = PasswordUtility.getPasswordDigest(password);
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}

		DBModule sysmod = Database.getSystemModule();
		DBModule admod = Database.getModuleByName(Database.ADMIN_SCHEMA);

		// register the new account in the system variable $users
		OID agg = sysmod.findFirstByNameId(Names.S_SYSUSERS_ID, admod.getDatabaseEntry());

		if (agg == null)
			throw new DatabaseException("Invalid database configuration");

		OID user1 = sysmod.createComplexObject(Names.namesstr[Names.S_SYSUSERS_ID], agg, 0);
		sysmod.createStringObject("username", user1, username, 0);
		sysmod.createStringObject("password", user1, encodedPass, 0);
		sysmod.createStringObject("rolename", user1, rolename, 0);
		
	} 
	
	public static void unregisterUserAccount(String username) throws DatabaseException {		
	}
}
