package odra.ws.type.mappers;

import java.util.List;

import javax.xml.namespace.QName;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBPrimitiveType;
import odra.sbql.results.runtime.Result;
import odra.ws.common.InitializationDef;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.xsom.XSSchemaSet;

/** Type mapper abstraction
 *
 * @since 2006-12-24
 * @version 2007-06-24
 *
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public interface ITypeMapper {

	public final static String XSD_NS = "http://www.w3.org/2001/XMLSchema";
	public final static String XSD_NS_PREFIX = "xsd";

	/** Maps primitive Odra type to XML type
	 * @param type
	 * @return
	 */
	QName mapPrimitiveOdraType(MBPrimitiveType type);

	/** maps primitive XML type to Odra type
	 * @param type
	 * @return
	 */
	String mapPrimitiveXMLType(QName type);

	/** Maps XML fragment to Odra result representation
	 * @param type
	 * @param root
	 * @return
	 * @throws DatabaseException
	 */
	String mapXMLToOdra(OID type, Node root)  throws TypeMapperException;

	/**
	 * @param type
	 * @param root
	 * @param preInitialization
	 * @return
	 * @throws TypeMapperException
	 */
	String mapXMLToOdra(OID type, Node root, InitializationDef preInitialization) throws TypeMapperException;

	/** Maps Odra result abstraction to XML fragment
	 * @param context
	 * @param result
	 * @return
	 */
	NodeList mapOdraResultToXML(Document context, Result result, String collectionItemName, String namespace) throws TypeMapperException;

	/** Maps (creates) XML Schema document to Odra meta schema
	 * @param xss
	 * @param rootTypes
	 * @param module
	 * @throws DatabaseException
	 */
	void mapXSDToModel(XSSchemaSet xss, List<QName> rootTypes, List<QName> rootElements, DBModule module) throws TypeMapperException;

	/** Maps primitive XML type to odra type
	 * @param type
	 * @return
	 */
	String mapXML(String type);

	/** Maps primitive Odra type to XML type
	 * @param type
	 * @return
	 */
	String mapOdra(String type);

	/** Provides naming convention for imported types
	 * @param name
	 * @return
	 */
	String mapTypeDefName(String name);


}
