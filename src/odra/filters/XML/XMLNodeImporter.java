package odra.filters.XML;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import nu.xom.Element;
import nu.xom.Node;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;

/**
 * This interface defines an abstract behavior of interpretation of a XML node in a document.
 * 
 */
public interface XMLNodeImporter{

	/**
	 * This method must perform appropriate actions of XML document complex node interpretation. Typically it creates some database objects. 
	 * Called for elements that have child nodes. 
	 *
	 * @param node
	 * @param parent
	 * @return created object's OID (may be null if nothing to do)
	 * @throws DatabaseException
	 * @throws ShadowObjectException
	 * @throws FilterException
	 */
	public OID interpretElement( Element node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException;

	public void closeInnerElementScope( Element element ) throws FilterException, DatabaseException;

	public void openInnerElementScope( Element element );

	public void closeOuterElementScope( Element element ) throws FilterException, DatabaseException;

	public void openOuterElementScope( Element element ) throws DatabaseException;

	/**
	 * This method must perform appropriate actions of XML document simple node interpretation. Typically it creates some database objects.
	 * Called for elements that have only text child. 
	 *
	 * @param node
	 * @param parent
	 * @return created object's OID (may be null if nothing to do)
	 * @throws DatabaseException
	 * @throws ShadowObjectException
	 * @throws FilterException
	 */
	
	public OID interpretTextElement( Element node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException;
	/**
	 * This method must perform appropriate actions of XML document simple node interpretation. Typically it creates some database objects.
	 * Called for nodes that are texts. 
	 *
	 * @param node
	 * @param parent
	 * @return created object's OID (may be null if nothing to do)
	 * @throws DatabaseException
	 * @throws ShadowObjectException
	 * @throws FilterException
	 */
	
	public OID interpretTextNode( Node node, OID parent ) throws DatabaseException, ShadowObjectException, FilterException;
	
	/**
	 * @return Set of Strings containing OIDs of unresolved object identifiers. If this Set is not empty it means that there are references, 
	 * which wrongly point to parent object because their destination is unknown. However, unknown OIDs may be resolved by another import so 
	 * it is not a fatal error.
	 */
	public Set<String> getUnresolvedIdentifiers();

	public HashMap<String, LinkedList<OID>> getUknownReferences();
	
	public long getProcessedNodesCount();

	void finalizeUnknownIdrefs() throws DatabaseException;
}