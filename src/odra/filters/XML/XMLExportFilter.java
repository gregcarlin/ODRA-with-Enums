package odra.filters.XML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Serializer;
import nu.xom.xslt.XSLException;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBAnnotatedObject;
import odra.filters.DataExporter;
import odra.filters.FilterException;
import odra.filters.ShadowObject;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

/**
 * Main class exporting objects to a stream. Specific export behavior may be changed by supplying different node creators.  
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class XMLExportFilter implements DataExporter{

	OutputStream output;
	Element root;
	String outputEncoding = "UTF-8";
	XMLNodeExporter nodeCreator;
	private XMLTransformer transformer;
	
	/**
	 * 
	 * @param output export destination stream. Default encoding is UTF-8. May be changed by {@link setOutputEncoding} 
	 * @param nodeCreator specific node creator to be used during export. 
	 */
	public XMLExportFilter(OutputStream output, XMLNodeExporter nodeCreator)
	{
		this.output = output;
		this.nodeCreator = nodeCreator;
	}

	public void SetTransformer(XMLTransformer transformer)
	{
		this.transformer = transformer;
	}
	
	public void initialize() {
		root = nodeCreator.createRootElement();
	}

	public void setOutputEncoding(String outputEncoding){
		this.outputEncoding = outputEncoding;
	}
	
	public void exportObject(OID oid) throws DatabaseException {
		Element elem = exportNode( oid, root );
		if (root==null)
			root = elem;
	}

	public void finish() throws FilterException {
		
		try {
			Document doc = new Document(root);
		
			if (transformer != null)
			{
				Nodes nodes = transformer.transform( doc );
				if ((nodes.size()>1) || ! (nodes.get(0) instanceof Element))
					throw new FilterException("Wrong result of XML transformation (cannot be converted to XML document", null);
				doc = new Document((Element)nodes.get(0));
			}
				
			Serializer serializer = new Serializer(output, outputEncoding);
			serializer.setIndent(4);
			serializer.setMaxLength(80);
			serializer.write(doc);  
		}
		catch (XSLException e) {
				throw new FilterException("Invalid XSLT transformation ", e);
		} catch (UnsupportedEncodingException e) {
			throw new FilterException( "Error while exporting to XML (problem with output stream)", e );
		} catch (IOException e) {
			throw new FilterException( "Error while exporting to XML (I/O problem)", e );
		}
	}

	//==========================================================================
	
	private Element exportNode( OID oid, Element parentElement ) throws DatabaseException {
		
		ConfigServer.getLogWriter().getLogger().fine( "EXPORTING: " + oid.getObjectName() );

		String name = oid.getObjectName();
		if ( name.charAt(0) == '$' )
			return null;
		ShadowObject.Kind kind;
		DBAnnotatedObject ann = new DBAnnotatedObject(oid);
		if (ann.isValid())
			kind = ShadowObject.getObjectKind(ann.getValueRef());
		else
			kind = ShadowObject.getObjectKind(oid);
		Element elem = nodeCreator.createNode( oid, kind, parentElement );
		//nodeCreator.processObjectId( elem, oid );
		if ((ShadowObject.ComplexKind.contains(kind)) && (elem!=null))
		{
			nodeCreator.openScope(oid);
			for( int i=0; i<oid.countChildren(); i++ )
				exportNode( oid.getChildAt(i), elem );
			nodeCreator.closeScope();
		}
		else if (ShadowObject.SimpleKind.contains(kind) && elem!=null)
		{
			if (ann.isValid())
				nodeCreator.createNodeValue(elem, parentElement, ann.getValueRef(), kind);
			else
				nodeCreator.createNodeValue(elem, parentElement, oid, kind);
		}
		else if ((ShadowObject.ReferenceKind.contains(kind)) && (elem!=null))
		{
			if (ann.isValid())
				nodeCreator.createReferenceValue(elem, parentElement, ann.getValueRef(), kind);
			else
				nodeCreator.createReferenceValue(elem, parentElement, oid, kind);
		}
		return elem;
	}
}
