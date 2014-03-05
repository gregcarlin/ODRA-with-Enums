package odra.security;

/**
 * This class is responsible for granting and checking users rights
 *
 * @author Lukasz Zaczek
 */

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DataObjectKind;
import odra.system.Names;

public class AccessControl {
	
	public static boolean grantAccess( int objKind, OID object, UserContext ctx) throws DatabaseException, AccessControlException {
		boolean accessOk = false;
		
		if ( ctx == null ) 
			throw new AccessControlException("User context does not exist!");
		
		DBModule sysmod = Database.getSystemModule();
		DBModule admod = Database.getModuleByName(Database.ADMIN_SCHEMA);

		OID agg = sysmod.findFirstByNameId(Names.S_SYSROLES_ID, admod.getDatabaseEntry());

		if (agg == null)
			throw new DatabaseException("Invalid database configuration");
		
		
		OID[] roles = agg.derefComplex();
		for (OID oid : roles) {
			// search for role for which is access checking 
			if (oid.derefComplex()[0].derefString().equals(ctx.getRoleName())) {
				OID privileges[] = oid.getChildAt(1).derefComplex();
				
				// checking privileges for given role
				for (OID oid2 : privileges) {

					OID objectType  = oid2.getChildAt(0);
					OID accessMode  = oid2.getChildAt(1);
					OID accessValue = oid2.getChildAt(2);
					OID grantFlag   = oid2.getChildAt(3);
					OID objectID	= oid2.getChildAt(4);
					
					
					if(objectID != null ) { // for given objects
						if(	objectType.derefInt() == objKind &&	
							accessMode.derefInt() == ctx.accessMode &&
							objectID.derefString().equals(object.getObjectName() ))
								if ( accessValue.derefInt() == ALLOW) {
									accessOk = true;
								}
								else {
									accessOk = false;
								//	throw new AccessControlException("Access Denied for this object!! No privileges for this action!");						
								}
					}
					else // access for CRUD actions
					if(	objectType.derefInt() == objKind &&	
						accessMode.derefInt() == ctx.accessMode && object == null)
						if ( accessValue.derefInt() == ALLOW) {
							System.out.println(">>>>Access Granted!<<<<");
							accessOk = true;
						//	return true;
						}
						else {
							accessOk = false;
							 
						//	throw new AccessControlException("Access Denied!! No privileges for this action!");
						}
					
					/* for initializing new object when create action is executing 
					 * */
					else 
					if (objectType.derefInt() == objKind &&	
						accessMode.derefInt() == ctx.accessMode && object != null) {
						if ( accessValue.derefInt() == ALLOW) {
							System.out.println(">>>>Access Granted 2 obj!<<<<");
							accessOk = true;
						//	return true;
						}
						else {
							if(accessOk) accessOk = true;
						//	throw new AccessControlException("Access Denied!! No privileges for this action!2");
						}
					}

				}
			/*
				// if user role is admin.. grant access by default
				if(ctx.getRoleName().equals(Database.ADMIN_SCHEMA)) {
					grantPrivilegeToRole(Database.ADMIN_SCHEMA, objKind, null, ctx.accessMode, AccessControl.ALLOW, 1);
					System.out.println(">>>>Access Granted to ADMIN by default..<<<<");
				}
				else throw new AccessControlException("Access Denied!! No matching rules");
			*/
			}
		}
		
		if(accessOk) System.out.println("Access OK!");
		else throw new AccessControlException("<<<Access Denied!!>>>");
		
		return accessOk;
	}

	public static void grantPrivilegeToRole(String rolename, int objectType, OID objectID, int accessMode, int accessValue, int grantFlag) throws DatabaseException, AccessControlException {
		if (!RoleManager.hasSystemRole(rolename)) 
			throw new AccessControlException("Role does not exist");
		
		DBModule sysmod = Database.getSystemModule();
		DBModule admod = Database.getModuleByName(Database.ADMIN_SCHEMA);

		OID agg = sysmod.findFirstByNameId(Names.S_SYSROLES_ID, admod.getDatabaseEntry());

		if (agg == null)
			throw new DatabaseException("Invalid database configuration");

		OID[] roles = agg.derefComplex();
		for (OID oid : roles) {
			if (oid.derefComplex()[0].derefString().equals(rolename)) {
				
				OID privs[] = oid.getChildAt(1).derefComplex();
				
				// checking privileges for given role
				for (OID oid2 : privs) {
					
					OID objType  = oid2.getChildAt(0);
					OID accMode  = oid2.getChildAt(1);
					OID objID	 = oid2.getChildAt(4);
					
					if (objectID != null) {
						if (objID != null)
						if ( objType.derefInt() == objectType && accMode.derefInt() == accessMode && objID.derefString().equals(objectID.getObjectName())) {
							System.out.println("this privilige should be modified.."+ objType.derefInt() + " "+accMode.derefInt());
							OID accValue  = oid2.getChildAt(2);
							accValue.updateIntegerObject(accessValue);
							OID grntFlag  = oid2.getChildAt(3);
							grntFlag.updateIntegerObject(grantFlag);
							return;
						}
					} 
					else {
						if ( objType.derefInt() == objectType && accMode.derefInt() == accessMode && objID == null) {
							System.out.println("this privilige should be modified..");
							OID accValue  = oid2.getChildAt(2);
							accValue.updateIntegerObject(accessValue);
							OID grntFlag  = oid2.getChildAt(3);
							grntFlag.updateIntegerObject(grantFlag);
							return;
						}
					}
					
				}
				
				OID privileges = oid.getChildAt(1);
				OID privilege = sysmod.createComplexObject("privilege", privileges, 0);
				OID objtype = sysmod.createIntegerObject("ObjectType", privilege, objectType );
			//	OID accessMode = sysmod.createComplexObject("AccessMode", privilege, 0);
				OID accessmode = sysmod.createIntegerObject("AccessMode", privilege, accessMode);
				OID access = sysmod.createIntegerObject("AccessValue", privilege, accessValue);
				OID grantflag = sysmod.createIntegerObject("GrantFlag", privilege, grantFlag);
				if (objectID != null){
					OID object = sysmod.createStringObject("Object", privilege, objectID.getObjectName(), 0 );
				}
			}
		}
	}
	
	public static String getModeAsString(int mode){
		return modeTypeStr[mode];
	}
	
	
	public static int getModeAsInt(String mode){
		if(mode.equals(modeTypeStr[1])) return 1;
		if(mode.equals(modeTypeStr[2])) return 2;
		if(mode.equals(modeTypeStr[3])) return 3;
		if(mode.equals(modeTypeStr[4])) return 4;
		return 0;
	}
	public static int getValueAsInt(String value){
		if(value.toLowerCase().equals("deny"))  return DENY;
		if(value.toLowerCase().equals("allow")) return ALLOW;
		return -1;
	}
	private static String[] modeTypeStr = {
		"??",
		"create",
		"read",
		"update",
		"delete"
	};
	
	public static int CREATE_MODE = 1;
	public static int READ_MODE   = 2;
	public static int UPDATE_MODE = 3;
	public static int DELETE_MODE = 4;
	
	public static final int DENY  = 0;
	public static final int ALLOW = 1;
	
}

