package odra.ws.endpoints.wsdl.typeresolver;

import java.util.Hashtable;
import java.util.Map.Entry;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBTypeDef;

/** Deals with type definitions resolving and inverse resolution
 * 
 * @since 2007-10-20
 * @version 2007-10-20
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 * TODO rewrite resolve method to non recursive version
 */
public class TypeDefResolver implements ITypeDefResolver {

	private Hashtable<String, OID> typeDefs = new Hashtable<String, OID>();
	private Hashtable<OID, String> revTypeDefs = new Hashtable<OID, String>();
	
	private Hashtable<String, OID> typeDefsCache = new Hashtable<String, OID>();
	private Hashtable<OID, String> revTypeDefsCache = new Hashtable<OID, String>();
	
	private boolean cacheDirtyFlag = false;
	
	/* (non-Javadoc)
	 * @see odra.ws.wsdl.ITypeDefResolver#addTypeDef(odra.db.objects.meta.MBTypeDef)
	 */
	public void addTypeDef(MBTypeDef typeDef) throws DatabaseException
	{
		if (!typeDefs.containsKey(typeDef.getName()))
		{
			this.typeDefs.put(typeDef.getName(), typeDef.getType());
			this.revTypeDefs.put(typeDef.getType(), typeDef.getName());
			this.cacheDirtyFlag = true;
			
		}
	}
	
	public void addTypeDef(String name, OID type) throws DatabaseException
	{
		if (!typeDefs.containsKey(name))
		{
			this.typeDefs.put(name, type);
			this.revTypeDefs.put(type, name);
			this.cacheDirtyFlag = true;
			
		}
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.wsdl.ITypeDefResolver#resolveName(java.lang.String)
	 */
	public OID resolveName(String typeDefName) throws DatabaseException
	{
		if (cacheDirtyFlag) 
		{
			regenerateCache();
		}
		
		return typeDefsCache.get(typeDefName);
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.wsdl.ITypeDefResolver#resolveType(odra.db.OID)
	 */
	public String resolveType(OID type) throws DatabaseException
	{
		if (cacheDirtyFlag) 
		{
			regenerateCache();
		}
		
		return revTypeDefsCache.get(type);
	}
	
	private void regenerateCache() throws DatabaseException
	{
		this.typeDefsCache.clear();
		this.revTypeDefsCache.clear();
		
		for (Entry<String, OID> entry : this.typeDefs.entrySet())
		{
			OID oid = resolve(entry.getValue());
			this.typeDefsCache.put(entry.getKey(), oid);
			this.revTypeDefsCache.put(oid, entry.getKey());
			
		}
		
	}

	private OID resolve(OID oid) throws DatabaseException {
		MBTypeDef typeDef = new MBTypeDef(oid);
		if (typeDef.isValid())
		{
			return resolve(typeDef.getType());
		}
		return oid;
	}
	
	
	
	
}
