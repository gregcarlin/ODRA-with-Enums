package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBLink;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class delivers operations performed on database links.
 * Database links are used in distributed communication.
 * 
 * TODO: this is only a stub
 * 
 * @author raist
 */

public class DBLink extends DBObject {
	public DBLink(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}
	
	public void initialize(String host, int port, String schema, String password, OID mbLink) throws DatabaseException {
		String user = schema;
		
		int dot = user.indexOf(".");
		
		if (dot > 0)
			user = user.substring(0, dot);

		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.LINK_OBJECT);
		store.createStringObject(store.addName(Names.namesstr[Names.HOST_ID]), oid, host, 0);
		store.createIntegerObject(store.addName(Names.namesstr[Names.PORT_ID]), oid, port);
		store.createStringObject(store.addName(Names.namesstr[Names.SCHEMA_ID]), oid, schema, 0);
		store.createStringObject(store.addName("$user"), oid, user, 0);
		store.createStringObject(store.addName("$password"), oid, password, 0);
		store.createBooleanObject(store.addName("$grid"), oid, false);
		store.createPointerObject(store.addName("$mblink"), oid, mbLink); 
	}
	
	public void initialize(String peerName, String schema, String password, OID mbLink) throws DatabaseException {

		String user = schema;
		
		int dot = user.indexOf(".");
		
		if (dot > 0)
			user = user.substring(0, dot);

		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.LINK_OBJECT);
		store.createStringObject(store.addName(Names.namesstr[Names.HOST_ID]), oid, peerName, 0);
		store.createIntegerObject(store.addName(Names.namesstr[Names.PORT_ID]), oid, 0);
		store.createStringObject(store.addName(Names.namesstr[Names.SCHEMA_ID]), oid, schema, 0);
		store.createStringObject(store.addName("$user"), oid, user, 0);
		store.createStringObject(store.addName("$password"), oid, password, 0);
		store.createBooleanObject(store.addName("$grid"), oid, true);
		store.createPointerObject(store.addName("$mblink"), oid, mbLink); 
	}	

	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.LINK_OBJECT;
	}

	public String getHost() throws DatabaseException {
		return getHostRef().derefString();
	}
	
	public int getPort() throws DatabaseException {
		return getPortRef().derefInt();
	}
	
	public String getSchema() throws DatabaseException {
		return getSchemaRef().derefString();
	}
	
	public String getUser() throws DatabaseException {
		return getUserRef().derefString();
	}

	public String getPassword() throws DatabaseException {
		return getPasswordRef().derefString();
	}
	
	public boolean getGrid() throws DatabaseException {
		return getGridReg().derefBoolean();
	}
	
	public MBLink getMBLink() throws DatabaseException {
		return new MBLink( getMBLinkRef().derefReference() );
	}
	
	private final OID getHostRef() throws DatabaseException {
		return oid.getChildAt(HOST_POS);
	}
	
	private final OID getPortRef() throws DatabaseException {
		return oid.getChildAt(PORT_POS);
	}

	private final OID getSchemaRef() throws DatabaseException {
		return oid.getChildAt(SCHEMA_POS);
	}
	
	private final OID getUserRef() throws DatabaseException {
		return oid.getChildAt(USER_POS);
	}
	
	private final OID getPasswordRef() throws DatabaseException {
		return oid.getChildAt(PASSWORD_POS);
	}
	
	private final OID getGridReg() throws DatabaseException {
		return oid.getChildAt(GRID_POS);
	}
	
	private final OID getMBLinkRef() throws DatabaseException {
		return oid.getChildAt(MBLINK_POS);
	}
	
	public final static int HOST_POS = 1;
	public final static int PORT_POS = 2;
	public final static int SCHEMA_POS = 3;
	public final static int USER_POS = 4;
	public final static int PASSWORD_POS = 5;
	public final static int GRID_POS = 6;
	public final static int MBLINK_POS = 7;

	public final static int FIELD_COUNT = 8;
}
