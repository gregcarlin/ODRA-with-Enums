package odra.filters.XML;

import nu.xom.Element;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.filters.ShadowObject;
import odra.filters.ShadowObject.Kind;

/**
 * Abstract interface describing behavior of XML node creators.  
 */
public interface XMLNodeExporter{
	
	/**
	 * This method is called for complex objects.
	 */
	public Element createNode( OID oid, ShadowObject.Kind kind, Element parent ) throws DatabaseException;
	
	/**
	 * This method is called for simple type objects.
	 */
	public void createNodeValue( Element elem, Element parent, OID oid, ShadowObject.Kind kind ) throws DatabaseException;
	
	/**
	 * This method is called when entering interior of a complex object. createNode is called before.
	 */
	public void openScope(OID oid) throws DatabaseException;
	
	/**
	 *  This method is called when leaving interior of a complex object.
	 */
	public void closeScope();
	
	/**
	 * This method is called to create an additional root element of a document (if a creator need it). It may return null.
	 * 
	 * @return created new root element.
	 */
	public Element createRootElement();
	
	/**
	 * This method is called to process object's identifier. Some creators may perform additional actions.
	 * 
	 * @param elem element which represents object idetified by oid
	 * @param oid represents exported database object 
	 */
//	public void processObjectId(Element elem, OID oid);
	public void createReferenceValue(Element elem, Element parentElement, OID valueRef, Kind kind) throws DatabaseException;
}