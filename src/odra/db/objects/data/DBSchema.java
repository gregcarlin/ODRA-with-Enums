package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBSchema;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class delivers operations performed on database schema.
 * Database schema is a container of contributing links.
 * 
 * @author tkowals
 */

public class DBSchema extends DBObject {
	public DBSchema(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}
	
	public void initialize(OID mbSchema, int minCard, int maxCard) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.SCHEMA_OBJECT);
		store.createAggregateObject(store.addName("$linkslist"), oid, 0, minCard, maxCard);
		store.createPointerObject(store.addName("$mbschema"), oid, mbSchema); 
	}
	
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.SCHEMA_OBJECT;
	}

	public OID[] getLinksOIDs() throws DatabaseException {
		OID[] linksrefs = getLinksListRef().derefComplex();
		OID[] links = new OID[linksrefs.length];
		for(int i = 0; i < linksrefs.length; i++)
			links[i] = linksrefs[i].derefReference();
		return links;
	}
	
	public boolean containsLink(DBLink link) throws DatabaseException {
		return store.findFirstByNameId(link.oid.getObjectNameId(), getLinksListRef()) != null;
	}
	
	public void addLink(DBLink link) throws DatabaseException {
		store.createReferenceObject(link.oid.getObjectNameId(), getLinksListRef(), link.oid);
	}
	
	public void removeLink(DBLink link) throws DatabaseException {
		assert store.findFirstByNameId(link.oid.getObjectNameId(), getLinksListRef()) != null : "Link not found inside DBschema";
		
		store.findFirstByNameId(link.oid.getObjectNameId(), getLinksListRef()).delete();
	}
	
	
	public MBSchema getMBSchema() throws DatabaseException {
		return new MBSchema( getMBSchemaRef().derefReference() );
	}
	
	private final OID getLinksListRef() throws DatabaseException {
		return oid.getChildAt(LINKSLIST_POS);
	}
	
	private final OID getMBSchemaRef() throws DatabaseException {
		return oid.getChildAt(MBSCHEMA_POS);
	}
	
	public final static int LINKSLIST_POS = 1;
	public final static int MBSCHEMA_POS = 2;

	public final static int FIELD_COUNT = 3;
}
