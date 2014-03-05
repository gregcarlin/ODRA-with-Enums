package odra.filters.XML;

import nu.xom.Attribute;
import nu.xom.Element;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBAnnotatedObject;
import odra.filters.ShadowObject;
import odra.filters.ShadowObject.Kind;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

/**
 * This creator creates XML nodes from objects that may be accompanied with annotations as described in 
 * XMLImportFilter.M0AnnotatetedInterpreter. This class may also export objects without any annotations, if 
 * created in another way.
 *  
 */
public class M0AnnotatedExporter implements XMLNodeExporter
{
	NamespaceStack namespaceStack = new NamespaceStack();
	int addedNamespacesCount;
	
	public Element createNode( OID oid, ShadowObject.Kind kind, Element parent ) throws DatabaseException 
	{
		String name = oid.getObjectName();
		if ( name.equals( XMLImportFilter.PCDATA) || kind.equals(ShadowObject.Kind.AGGREGATE) ) 
			return parent;
		ConfigServer.getLogWriter().getLogger().finest( " CREATING NODE: " + oid.getObjectName() );			
		Element result = new Element( name );
		DBAnnotatedObject main = new DBAnnotatedObject( oid );
		if (main.isValid())
		{
			OID tmpAnnot = main.getAggregatedAnnotationByName(XMLImportFilter.NAMESPACE_DEF);
			if (tmpAnnot!=null)
			{
				OID[] namespaceDef = tmpAnnot.derefComplex();
				for( OID def:namespaceDef )
				{
					String prefix = def.derefComplex()[0].derefString(); 
					String URI = def.derefComplex()[1].derefString();
					ConfigServer.getLogWriter().getLogger().finest("Namespace DEF -- " + prefix + " --> " + URI);
					String previousURI = namespaceStack.getSourceURI(prefix);
						if ( prefix.length()>0 )
							result.addNamespaceDeclaration( prefix, URI );
						else if ( previousURI!=null && !URI.equals(previousURI) ) 
							result.setNamespaceURI( URI );
				}
			}
			tmpAnnot = main.getAggregatedAnnotationByName(XMLImportFilter.ATTRIBUTE);
			if (tmpAnnot!=null)
				if ( tmpAnnot.derefComplex()[0].isBooleanObject() && tmpAnnot.derefComplex()[0].derefBoolean() )
				{
					String prefix = "", URI = "";
					OID value = oid.getChildAt(3);
					Attribute elemAttr = new Attribute( name, value.derefString() );//main.getValueRef().derefString() );
					OID namespaceRef = main.getAggregatedAnnotationByName(XMLImportFilter.NAMESPACE_REF);
					if (namespaceRef!=null)
					{
						prefix = namespaceRef.derefComplex()[0].derefReference().derefComplex()[0].derefString();
						URI = namespaceRef.derefComplex()[0].derefReference().derefComplex()[1].derefString();
						if (prefix.length()>0)
							elemAttr.setNamespace(prefix, URI);
						ConfigServer.getLogWriter().getLogger().finest("\t ATTRIBUTE NAMESPACE REF: "+ namespaceRef );
						ConfigServer.getLogWriter().getLogger().finest("\t ATTRIBUTE NAMESPACE PREFIX: "+ prefix );
					}
					parent.addAttribute( elemAttr );
					return null;
				}
			OID namespaceRef = main.getAggregatedAnnotationByName(XMLImportFilter.NAMESPACE_REF);
			if (namespaceRef!=null)
			{
				String prefix = namespaceRef.derefComplex()[0].derefReference().derefComplex()[0].derefString();
				String URI = namespaceRef.derefComplex()[0].derefReference().derefComplex()[1].derefString();
				ConfigServer.getLogWriter().getLogger().finest("\t NAMESPACE PREFIX: "+ prefix );
				ConfigServer.getLogWriter().getLogger().finest("\t NAMESPACE URI: "+ URI );
				result.setNamespaceURI( URI );
				result.setNamespacePrefix( prefix );
			}
		}
		if (parent != null)
			parent.appendChild( result );
		return result;
	}
	

	public void createNodeValue( Element elem, Element parent, OID oid, ShadowObject.Kind kind ) throws DatabaseException {
		elem.appendChild( kind.getValue(oid).toString() );
	}
	
	public Element createRootElement() {
		return null;
	}

	public void closeScope() {
		for (int i=0; i<addedNamespacesCount; i++)
			namespaceStack.pop();
	}

	public void openScope(OID oid) throws DatabaseException {
		this.addedNamespacesCount=0;
		DBAnnotatedObject main = new DBAnnotatedObject( oid );
		if (main.isValid())
		{
			OID isAttr = main.getAggregatedAnnotationByName(XMLImportFilter.NAMESPACE_DEF);
			if (isAttr==null)
				return;
			OID[] namespaceDef = isAttr.derefComplex();
			for( OID def:namespaceDef )
			{
				String prefix = def.derefComplex()[0].derefString(); 
				String URI = def.derefComplex()[1].derefString();
				namespaceStack.push( new NamespaceStack.NamespaceDef(prefix, URI, def) );								
			}
		}
	}

	public void createReferenceValue(Element elem, Element parentElement, OID oid, Kind kind) throws DatabaseException {
		if (elem!=null)
			elem.addAttribute( new Attribute( "idref", kind.getValue(oid).toString() ) );
	}

//	public void processObjectId(Element elem, OID oid) {
//	}
}