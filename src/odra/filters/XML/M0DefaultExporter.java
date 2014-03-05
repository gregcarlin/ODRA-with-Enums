package odra.filters.XML;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.filters.ShadowObject;
import odra.filters.ShadowObject.Kind;

/**
 * This is a default class that is able to produce XML elements out of M0 objects. It cannot handle DBAnnotatedObject,
 * thus you will loose all information stored in annotations. However export will not rise exceptions.
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class M0DefaultExporter implements XMLNodeExporter{
	
	private boolean createIdAttr;
	
	public M0DefaultExporter(boolean createIdAttr)
	{
		this.createIdAttr = createIdAttr;
	}
	
	public Element createNode( OID oid, ShadowObject.Kind kind, Element parent ) throws DatabaseException 
	{
		Element elem;
		String name = oid.getObjectName(); 
		if (name.charAt(0) == XMLImportFilter.ATTR_SIGN)
		{
			kind = ShadowObject.Kind.STRING;
			parent.addAttribute(new Attribute( name.substring(1), kind.getValue(oid).toString() ));
			elem = null;
		}
		else if ( name.equals(XMLImportFilter.PCDATA) )
		{
			kind = ShadowObject.Kind.STRING;
			Text text = new Text( kind.getValue(oid).toString() );
			parent.appendChild(text);
			elem = null;
		}
		else
		{
			if (!oid.isAggregateObject())
			{	elem = new Element(name);
				if (this.createIdAttr) 
					elem.addAttribute( new Attribute( "id", "" + oid ) );
				
				if (parent != null)
					parent.appendChild( elem );
			}
			else
				elem = parent;
		}
		return elem;
	}

	public void createNodeValue( Element elem, Element parent, OID oid, ShadowObject.Kind kind ) throws DatabaseException
	{
		elem.appendChild( kind.getValue(oid).toString() );
	}
	
	public Element createRootElement()
	{
		return null;
	}

	public void closeScope() {
	}

	public void openScope(OID oid) {
	}

	public void createReferenceValue(Element elem, Element parentElement, OID oid, Kind kind) throws DatabaseException {
		if (elem!=null)
			elem.addAttribute( new Attribute( "idref", kind.getValue(oid).toString() ) );
	}

	public void processObjectId(Element elem, OID oid) {
//		if ((elem != null) && (createIdAttr))
//			elem.addAttribute( new Attribute( "id", "" + oid ) );
	}
}