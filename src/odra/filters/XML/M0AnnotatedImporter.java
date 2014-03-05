package odra.filters.XML;

import nu.xom.Attribute;
import nu.xom.Element;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBAnnotatedObject;
import odra.db.objects.data.DBModule;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.system.config.ConfigServer;

/**
 * This interpreter utilizes DBAnnotatedObject objects to represent annotations. They are used to store
 * namespace information:
 * <li>
 * 		<ul>namespaceDef -- stores definition of a namespace structure (prefix:String, URI:String)
 * 		<ul>namespaceRef -- stores a reference to a namespaceDef annotation that defines namespace for a given element.
 * </li>
 * namespaceRef is added for all imported elements. 
 * 
 * All PCDATA is stored in a simple type named _VALUE.
 * 
 * An XML attribute is converted to a normal object annotated with annotation 'attribute'=true.
 * Attribute may also have a namespaceRef annotation.
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class M0AnnotatedImporter extends M0DefaultImporter
{
	private NamespaceStack namespaceStack;
	private int newNamespaceDefCount;

	public M0AnnotatedImporter(DBModule module, boolean isGuessType, boolean noAutoRefs) {
		super(module, isGuessType, noAutoRefs);
		this.namespaceStack = new NamespaceStack();
	}

	public void createMainAnnotationObject(Element node, OID oid) throws DatabaseException, ShadowObjectException
	{
		DBAnnotatedObject mainAnnotation = new DBAnnotatedObject( oid );
		mainAnnotation.initialize();
		handleNamespace( oid, node, mainAnnotation );
	}
	
	public DBModule getModule()
	{
		return module;
	}
	
	protected void handleNamespace(OID parent, Element node, DBAnnotatedObject annotation) throws DatabaseException, ShadowObjectException
	{
		for ( int i=0; i<node.getNamespaceDeclarationCount(); i++ )
		{
			String prefix = node.getNamespacePrefix(i);
			String URI = node.getNamespaceURI(prefix);
			String previousURI = namespaceStack.getSourceURI(prefix);
			if ( previousURI==null || !previousURI.equals(URI) )
				if ( URI.length()>0 )
				{
					OID namespaceDef = annotation.addComplexAnnotation( XMLImportFilter.NAMESPACE_DEF );
					namespaceDef.createStringChild(Database.getNameIndex().addName("prefix"), prefix, 0);
					namespaceDef.createStringChild(Database.getNameIndex().addName("URI"), URI, 0);
					namespaceStack.push( new NamespaceStack.NamespaceDef( prefix, URI, namespaceDef ) );
					newNamespaceDefCount++;
					ConfigServer.getLogWriter().getLogger().finest("namespace DEF " + namespaceDef + " : " + prefix + "->" + URI );
				}
		}
		OID namespaceRef = namespaceStack.getSourceOID( node.getNamespacePrefix() );
		if (namespaceRef!=null)
		{	
			annotation.addPointerAnnotation(XMLImportFilter.NAMESPACE_REF, namespaceRef);
			ConfigServer.getLogWriter().getLogger().finest("namespace REF: " + namespaceStack.getSourceOID( node.getNamespacePrefix() ) );
			ConfigServer.getLogWriter().getLogger().finest("  namespace URI: " + namespaceRef.derefComplex()[1].derefString() );
			ConfigServer.getLogWriter().getLogger().finest("  namespace prefix: " + namespaceRef.derefComplex()[0].derefString() );
		}
	}
	
	protected void handleAttribute( Attribute nodeAttr, OID main ) throws DatabaseException, FilterException
	{
		if (noAutoRefs || !nodeAttr.getLocalName().equals(XMLImportFilter.ID))
		{
			OID atr = main.createComplexChild(Database.getNameIndex().addName(nodeAttr.getLocalName()), 0 );
			
			DBAnnotatedObject attrAnnotation = new DBAnnotatedObject( atr );
			attrAnnotation.initialize();
			createValueObject(XMLImportFilter.PCDATA, nodeAttr.getValue(), atr, true);
			attrAnnotation.addBooleanAnnotation( XMLImportFilter.ATTRIBUTE, true );
			OID namespaceDef = namespaceStack.getSourceOID( nodeAttr.getNamespacePrefix() );
			if (namespaceDef!=null)
			{	attrAnnotation.addPointerAnnotation(XMLImportFilter.NAMESPACE_REF, namespaceDef);
				ConfigServer.getLogWriter().getLogger().finest("   atr namespace URI: " + namespaceDef.derefComplex()[1].derefString() );
				ConfigServer.getLogWriter().getLogger().finest("   atr namespace prefix: " + namespaceDef.derefComplex()[0].derefString() );
			}

			ConfigServer.getLogWriter().getLogger().finest("Attribute [" + nodeAttr.getLocalName() + "] initialized to :" + nodeAttr.getValue() );
		}
	}

	public void openInnerElementScope(Element element) {
		super.openInnerElementScope(element);
		newNamespaceDefCount=0;
	}

	public void closeInnerElementScope(Element element) {
		for ( int i=0; i<newNamespaceDefCount; i++ )
			namespaceStack.pop();
		super.closeInnerElementScope(element);
	}
}