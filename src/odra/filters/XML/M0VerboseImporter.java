package odra.filters.XML;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import nu.xom.Element;
import nu.xom.Node;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.FilterException;
import odra.filters.ShadowObject;
import odra.filters.ShadowObjectException;
import odra.filters.ShadowObject.Kind;

/**
 * This is a verbose node interpreter. It reads only special kind of XML data which contains some type information. 
 * It also resolves reference objects in the same way as {@link SmartNodeInterpreter}.
 * <br>
 * Its properties are:
 * <ul>
 * 	<li>element name must contain type specification that is: 
 *  STRING, INTEGER, DOUBLE, BOOLEAN, COMPLEX, REFERENCE, AGGREGATE, BINARY, POINTER. These are the
 *  basic Odra types.
 *  <li>object's name must be stored in attribute named 'name'
 *  <li>interprets object id if represented by 'id' attribute (may be string)
 *  <li>interprets references if represented by 'idref' attribute
 *  <li>all other attributes are ignored 
 *  <li>all no name string objects are ignored
 *  <li>all values have type assigned according to element's name
 * </ul>
 * 
 * There is also corresponding node exporter, which performs exactly opposite operation.
 * @see M0VerboseExporter 
 * 
 * @author Krzysztof Kaczmarski
 *
 */	public class M0VerboseImporter implements XMLNodeImporter{

	private long processedNodesCount;
	
	private DBModule module;
	/**
	 * Contains a map from string object identifiers read from XML to database OIDs
	 */
	private HashMap<String, OID> str2oidMap;
	/**
	 * Contains a map from string object identifiers to yet undefined reference object OIDs
	 */
	private HashMap<String, LinkedList<OID>> udefstr2oidMap;
	
	
	public M0VerboseImporter(DBModule module)
	{
		this.module = module;
		str2oidMap = new HashMap<String, OID>();
		udefstr2oidMap = new HashMap<String, LinkedList<OID>>();
	}

	private void resolveOID(String strOid, OID oid) throws DatabaseException
	{
		str2oidMap.put(strOid, oid);
		if (udefstr2oidMap.containsKey(strOid))
		{
			for ( OID o : udefstr2oidMap.get(strOid) )
				o.getStore().updateReferenceObject(o, oid);
			
			udefstr2oidMap.remove(strOid);
		}
	}
	
	public OID interpretElement( Element node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException 
	{
		processedNodesCount++;
		String strKind =  node.getLocalName();
		if (strKind.equals("SBQL_Objects"))
		{
			return parent;
		}
		ShadowObject.Kind kind = Enum.valueOf(Kind.class, node.getLocalName());
		OID oid;
		String name = node.getAttributeValue("name");
		String strOid = node.getAttributeValue("oid");
		
		if ( ShadowObject.ComplexKind.contains(kind) )
			oid = kind.createObject(module, parent, name, null);
		else
			throw new FilterException( "Cannot create object of kind:" + kind, null ); 
		resolveOID(strOid, oid);
		return oid; 
	}

	public OID interpretTextElement( Element node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException 
	{
		OID oid = null;
		ShadowObject.Kind kind = Enum.valueOf(Kind.class, node.getLocalName());
		String strOid = node.getAttributeValue("oid");
		String name = node.getAttributeValue("name");
		if ( ShadowObject.SimpleKind.contains(kind) )
			oid = kind.createObject(module, parent, name, kind.getValueFromString(node.getValue()) );
		else if ( ShadowObject.ReferenceKind.contains(kind) )
		{
			String destOid = node.getValue();
			if (str2oidMap.containsKey(destOid))
				oid = kind.createObject(module, parent, name, str2oidMap.get(destOid));
			else
			{
				oid = kind.createObject(module, parent, name, parent);
				if (!udefstr2oidMap.containsKey(destOid))
					udefstr2oidMap.put(destOid, new LinkedList<OID>());
				udefstr2oidMap.get(destOid).add(oid);
			}
		}
		else
			throw new FilterException( "Cannot create object of kind:" + kind, null ); 
		resolveOID(strOid, oid);
		return oid;
	}

	public OID interpretTextNode(Node node, OID parent) throws DatabaseException, ShadowObjectException, FilterException {

		return null;
	}
	
	public Set<String> getUnresolvedIdentifiers() {
		
		return udefstr2oidMap.keySet();
	}

	public long getProcessedNodesCount() {
		return this.processedNodesCount;
	}

	public void closeInnerElementScope(Element element) {
	}

	public void openInnerElementScope(Element element) {
	}

	public HashMap<String, LinkedList<OID>> getUknownReferences() {
		return null;
	}

	public void finalizeUnknownIdrefs() throws DatabaseException {
	}

	public void closeOuterElementScope(Element element) throws FilterException, DatabaseException {
	}

	public void openOuterElementScope(Element element) {
	}
}