package odra.filters.XML;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.FilterException;
import odra.filters.ShadowObject;
import odra.filters.ShadowObjectException;
import odra.system.config.ConfigServer;

/**
 * This node interpreter works as described by professor. Have a look here:
 * <a href="http://iolab.pjwstk.edu.pl:8081/forum/Default.aspx?g=posts&t=146">Odra Forum</a>
 * <br>
 * Its properties are:
 * <ul>
 * 	<li>imports all data stored in XML as they are
 *  <li>interprets object id if represented by 'id' attribute (may be string)
 *  <li>interprets references if represented by 'idref' attribute
 *  <li>all attributes are changed into objects with names starting from '@'
 *  <li>all no name string objects are changed to objects named '_VALUE'
 *  <li>all values are treated as strings or the type for value may be guessed
 *  <li>it does not interpret namespaces nor name prefixes
 * </ul>
 * 
 * There is also corresponding node exporter, which performs exactly opposite operation.
 * @see M0DefaultExporter 
 * 
 */

public class M0DefaultImporter implements XMLNodeImporter{

	protected DBModule module;
	/**
	 * Contains a map from string object identifiers read from XML to database OIDs
	 */
	private HashMap<String, OID> str2oidMap;
	/**
	 * Contains a map from string object identifiers to yet undefined reference object OIDs
	 */
	private HashMap<String, LinkedList<OID>> udefstr2oidMap;
	
	protected long processedNodesCount;
	protected boolean noGuessType;
	protected boolean noAutoRefs; 
	
	
	public M0DefaultImporter(DBModule module, boolean noGuessType, boolean noAutoRefs)
	{
		this.module = module;
		this.noGuessType = noGuessType;
		this.noAutoRefs = noAutoRefs;
		str2oidMap = new HashMap<String, OID>();
		udefstr2oidMap = new HashMap<String, LinkedList<OID>>();
	}
	
	protected OID createValueObject(String name, String value, OID oid, boolean isAttribute) throws DatabaseException, FilterException
	{
		OID result;
		if (noGuessType)
			result =  oid.createStringChild(Database.getNameIndex().addName(name), value, 0);
		else
			try 
			{
				int i = Integer.parseInt(value);
				result = oid.createIntegerChild(Database.getNameIndex().addName(name), i);
			}
			catch(NumberFormatException e)
			{
				try
				{
					double d = Double.parseDouble(value);
					result = oid.createDoubleChild(Database.getNameIndex().addName(name), d);
				}
				catch(NumberFormatException f)
				{
					result = oid.createStringChild(Database.getNameIndex().addName(name), value, 0);
				}
			}
		return result;
	}
	
	private void resolveOID(String strOid, OID oid) throws DatabaseException
	{
		str2oidMap.put(strOid, oid);
		if (udefstr2oidMap.containsKey(strOid))
		{
			for ( OID o : udefstr2oidMap.get(strOid) )
				o.updateReferenceObject(oid);
			
			udefstr2oidMap.remove(strOid);
		}
	}

	protected void processID( Element node, OID oid ) throws DatabaseException
	{
		if (!noAutoRefs)
		{
			String strOid = node.getAttributeValue(XMLImportFilter.ID);
			resolveOID( strOid, oid );
		}
	}
	
	protected OID processIDREF( Element node, String name, OID parent ) throws DatabaseException
	{
		if ( !noAutoRefs && (node.getAttribute(XMLImportFilter.IDREF)!= null) )
		{
			OID oid;
			ShadowObject.Kind kind;
			kind = ShadowObject.Kind.REFERENCE;
			String destOid = node.getAttributeValue(XMLImportFilter.IDREF);
			if (str2oidMap.containsKey(destOid))
				oid = kind.createObject(module, parent, name, str2oidMap.get(destOid));
			else
			{
				oid = kind.createObject(module, parent, name, parent);
				if (!udefstr2oidMap.containsKey(destOid))
					udefstr2oidMap.put(destOid, new LinkedList<OID>());
				udefstr2oidMap.get(destOid).add(oid);
			}
			return oid;
		}
		else
			return null;
	}

	protected void handleAttribute( Attribute attr, OID oid ) throws DatabaseException, FilterException
	{
			if ( noAutoRefs || !attr.getLocalName().equals(XMLImportFilter.ID))
			{	
				ConfigServer.getLogWriter().getLogger().finest("IMPORTING ATTR: "+attr.getLocalName());
				ConfigServer.getLogWriter().getLogger().finest("Attr element value:>>"+attr.getValue()+"<<");
				createValueObject(XMLImportFilter.ATTR_SIGN+attr.getLocalName(), attr.getValue(), oid, true);
			}
	}

	protected void createMainAnnotationObject(Element node, OID oid) throws DatabaseException, ShadowObjectException
	{}
	
	public Set<String> getUnresolvedIdentifiers() {
		
		return udefstr2oidMap.keySet();
	}

	public long getProcessedNodesCount() {
		return this.processedNodesCount;
	}

	public OID interpretElement( Element node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException 
	{
		String name = determineName(node.getLocalName());
		ConfigServer.getLogWriter().flushConsole();
		OID agregate = getAgregateObject( name, parent, module, node.getChildCount() );
		OID oid = processIDREF(node, name, agregate);
		long start = System.currentTimeMillis();
		if ( oid == null )
		{
			oid = agregate.createComplexChild(Database.getNameIndex().addName(name), node.getChildCount()+1);
			createMainAnnotationObject(node, oid);
			ConfigServer.getLogWriter().getLogger().fine( processedNodesCount + " > sba time : " + ((System.currentTimeMillis() - start) / 1000F) + "s");
			
			for(int i=0; i<node.getAttributeCount(); i++)
				handleAttribute( node.getAttribute(i), oid );
		}
		processID( node, oid );
		processedNodesCount++;
		long stop = System.currentTimeMillis();
		//LogWriter.getLogger().fine(">> creation time: " + ((stop - start) / 1000F) + "s");
		return oid; 
	}
	
	public OID interpretTextElement(Element node, OID parent) throws DatabaseException, ShadowObjectException, FilterException {
		OID oid = interpretElement(node, parent);
		interpretTextNode( node, oid );
		return oid;
	}
	
	public OID interpretTextNode( Node node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException 
	{
		OID oid = null;
		this.processedNodesCount++;
		String value = node.getValue().trim();
		if (value.length() > 0)
			oid = createValueObject( XMLImportFilter.PCDATA, value, parent, false );
		return oid;
	}

	public HashMap<String, LinkedList<OID>> getUknownReferences() {
		return this.udefstr2oidMap;
	}

	protected OID getAgregateObject(String name, OID parent, DBModule module, int predictedChildCounter) throws DatabaseException {
		
		OID candidate;
		candidate = parent.findFirstChildByNameId(Database.getNameIndex().addName(name)); 
			if (candidate!=null && candidate.isAggregateObject())
				return candidate;
		return parent.createAggregateChild(Database.getNameIndex().addName(name), predictedChildCounter);
	}
	
	public void finalizeUnknownIdrefs() throws DatabaseException
	{
		HashMap<String, LinkedList<OID>> idrefs = getUknownReferences();
		Set<String> refs = idrefs.keySet();
		
		for(String ref : refs)
		{
			LinkedList<OID> oids = idrefs.get(ref);
			for (Iterator<OID> i = oids.iterator(); i.hasNext();  )
			{
				OID oid = i.next();
				String name = oid.getObjectName();
				OID parent = oid.getParent();
				oid.delete();
				OID newOid = parent.createComplexChild(Database.getNameIndex().addName(name), 1);
				newOid.createStringChild(Database.getNameIndex().addName(XMLImportFilter.ATTR_SIGN+XMLImportFilter.IDREF), ref, 0);
			}
		}
	}

	public void closeInnerElementScope(Element element) {
	}

	public void openInnerElementScope(Element element) {
	}

	public void closeOuterElementScope(Element element) throws FilterException, DatabaseException {
	}

	public void openOuterElementScope(Element element) throws DatabaseException {
	}
	
	/**
	 * Determines the object name from the element name. The method is overriden in the relational wrapper 
	 * data importer. 
	 * 
	 * @param elementName element name
	 * @return object name
	 * @author jacenty
	 */
	protected String determineName(String elementName)
	{
		return elementName;
	}
}