/**
 * 
 */
package odra.filters.XML;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.StringTokenizer;
import java.util.logging.Level;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.xslt.XSLException;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.DataImporter;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Main class importing objects from a stream. 
 * Specific import behavior may be changed by supplying different node interpreters.  
 * 
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class XMLImportFilter implements DataImporter {

	public static final char ATTR_SIGN = '@'; 
	public static final char TEXT_SIGN = '&';
	public static final String PCDATA = "_VALUE";
	public static final String XMLNS_DEF = "@namespaceDef";
	public static final String XMLNS_USE = "@namespaceUse";
	public static final String NS_SEPARATOR = "|";
	public static final String NAMESPACE_DEF = "namespaceDef";
	public static final String NAMESPACE_REF = "namespaceRef";
	public static final String ATTRIBUTE = "attribute";
	public static final String ID = "id";
	public static final String IDREF = "idref";
	public static final String NS_URI = "uri";
	public static final String NS_PREFIX = "prefix";

	protected Reader input;
	private URI baseURI;
	protected Nodes nodes;
	protected XMLNodeImporter interpreter;
	private XMLTransformer transformer;
	private boolean validation;
	private XMLReader xerces;
	private URI XSDSchema;
   
	public XMLImportFilter(){};
	
	public XMLImportFilter( XMLNodeImporter interpreter, Reader input, URI baseURI )
	{
		this.input = input;
		if (baseURI==null)
			try {
				this.baseURI = new URI("");
			} catch (URISyntaxException e) {}
		else
			this.baseURI = baseURI;
		
		this.interpreter = interpreter;
	}
	
	public XMLImportFilter( XMLNodeImporter interpreter, URI baseURI ) 
	{
		this( interpreter, null, baseURI );
	}

	public XMLImportFilter( XMLNodeImporter interpreter, Reader input ) 
	{
		this( interpreter, input, null );
	}

	public void setTransformer( XMLTransformer transformer )
	{
		this.transformer = transformer;
	}
	
	public void setValidation(boolean validate, URI schemaURI)
	{
		this.validation = validate;
		this.XSDSchema = schemaURI;
	}
	
	public enum XMLImportParameter
	{
		M0, noGuessType, useMetabase, noAutoRefs, validate,
		//not interpretted yet:
		importId;
		
		static public EnumSet<XMLImportParameter> createParamsInfo(String params)
		{
			EnumSet<XMLImportParameter> paramsSet = EnumSet.noneOf(XMLImportParameter.class);
			
				StringTokenizer tokenizer = new StringTokenizer(params, " \n\r\t\f,;");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					try {
					     paramsSet.add( Enum.valueOf(XMLImportParameter.class, token) );
					}
					catch (IllegalArgumentException e) {
						ConfigServer.getLogWriter().getLogger().severe("Unknown import parameter: " + token);
					}
				}
			return paramsSet;
		}
	}

	
	public void importData(String modname, String data, String params) throws FilterException 
	{ 
		ConfigServer.getLogWriter().getLogger().info("XML Import plugin started.");
		long start = System.currentTimeMillis();
		ConfigServer.getLogWriter().flushConsole();
		StringReader fileInput = new StringReader( data ); 
		try {
			DBModule mod = Database.getModuleByName(modname);
			XMLNodeImporter nodeInterpreter;
			EnumSet<XMLImportParameter> paramSet = XMLImportParameter.createParamsInfo(params);
			ConfigServer.getLogWriter().getLogger().fine("Import parameters: " + paramSet);
			if (paramSet.contains(XMLImportParameter.M0))
				nodeInterpreter = new M0DefaultImporter(mod, paramSet.contains(XMLImportParameter.noGuessType), paramSet.contains(XMLImportParameter.noAutoRefs));
			else if (paramSet.contains(XMLImportParameter.useMetabase))
				nodeInterpreter = new M0TypedImporter(mod);
			else
				nodeInterpreter = new M0AnnotatedImporter(mod, paramSet.contains(XMLImportParameter.noGuessType), paramSet.contains(XMLImportParameter.noAutoRefs));
				
			XMLImportFilter importFilter = new XMLImportFilter( nodeInterpreter, fileInput, null );

			importFilter.setValidation( paramSet.contains(XMLImportParameter.validate), null );

			OID result[] = importFilter.importInto(mod.getDatabaseEntry());

			float time = (System.currentTimeMillis() - start) / 1000F;
			ConfigServer.getLogWriter().getLogger().info("XML Import finished ("+nodeInterpreter.getProcessedNodesCount()+" nodes imported in " + time +"s.)");
			ConfigServer.getLogWriter().flushConsole();

		} catch (Exception e) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Error while XML import plugin job.", e);
			ConfigServer.getLogWriter().flushConsole();
			throw new FilterException(e.getMessage(), e);
		}	
	}
	
	/**
	 * This method may be called to import object and its all nested objects. 
	 * It may be called only once for a given input stream and ImportFilter.
	 * @throws  
	 */
	public OID[] importInto(OID parent) throws DatabaseException, FilterException, ShadowObjectException
	{
		OID result[] = null;
		if (nodes != null)
			throw new FilterException("importInto method may be called only once. Create a new Filter.", null);
	
		initialize();
		result = new OID[nodes.size()];
		for (int i=0; i<nodes.size(); i++)
			result[i] = importObjectNode(nodes.get(i), parent, false);
		interpreter.finalizeUnknownIdrefs();

		ConfigServer.getLogWriter().getLogger().finest( "Finished parsing of " + input );
		return result;
	}

	
	//==========================================================================

	private static boolean isTextOnly(Element node)
	{
		if ((node.getChildCount() == 1) && (node.getChild(0) instanceof nu.xom.Text))
			return true;
		return false;
	}

	protected OID importObjectNode(Node node, OID parent, boolean textOnly) throws DatabaseException, FilterException, ShadowObjectException {
		
		OID oid = null;
		if (node instanceof Element) 
		{
			ConfigServer.getLogWriter().getLogger().finest("importing xml element: " + ((Element)node).getLocalName() );
			
			boolean textOnlyElement = isTextOnly((Element)node);
			interpreter.openOuterElementScope( (Element)node );
			if (textOnlyElement)
				interpreter.interpretTextElement((Element)node, parent);
			else
			{
				//LogWriter.getLogger().finest("COMPLEX SCOPE OPEN");
				oid = interpreter.interpretElement((Element)node, parent);
				interpreter.openInnerElementScope( (Element)node );
				for( int i=0; i<node.getChildCount(); i++ )
					importObjectNode(node.getChild(i), oid, textOnly);
				interpreter.closeInnerElementScope( (Element)node );
				//LogWriter.getLogger().finest("COMPLEX SCOPE CLOSE");
			}
			interpreter.closeOuterElementScope( (Element)node );
		}
		else
			oid = interpreter.interpretTextNode(node, parent);
		return oid;
	}

	private String getDebugInfo()
	{
		return "(stream="+ input + ", baseURI=" + baseURI + ", xmlTransformer=" + transformer + ")";
	}
	
	protected void initialize() throws FilterException, DatabaseException, ShadowObjectException {
		Document document;
		Builder builder;
		
		ConfigServer.getLogWriter().getLogger().fine("Starting XML parser");
		ConfigServer.getLogWriter().flushConsole();
		long start = System.currentTimeMillis();
		try {
			if (validation)
			{
				ConfigServer.getLogWriter().getLogger().finest("Enabling XSD schema validation on XML parsing.");
				xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser"); 
				xerces.setFeature("http://xml.org/sax/features/validation", true);
				xerces.setFeature("http://apache.org/xml/features/validation/schema", true);
			    if (XSDSchema != null)
			    	xerces.setProperty( "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", XSDSchema.toString() );
			    builder = new Builder(xerces, true); 
			}
			else
				builder = new Builder(false);

			if (input!=null)
			{
				ConfigServer.getLogWriter().getLogger().finest( "Parsing XML from input stream" );
				document = builder.build( input, baseURI.toString() );
			}
			else
			{
				ConfigServer.getLogWriter().getLogger().finest( "Parsing XML from URI" );
				document = builder.build( baseURI.toString() );
			}
			
			if (transformer!=null)
			{
				ConfigServer.getLogWriter().getLogger().info( "Performing XML transformation with given XSLT" );
				nodes = transformer.transform( document );
			}
			else
			{
				nodes = new Nodes();
				nodes.append(document.getRootElement());
			}
			float time = (System.currentTimeMillis() - start) / 1000F;
			ConfigServer.getLogWriter().getLogger().fine("XML parsing finished ("+time+"s.)");
			ConfigServer.getLogWriter().flushConsole();
		}
		catch (XSLException e) {
			throw new FilterException("Invalid XSLT transformation: " + e.getMessage() + getDebugInfo(), e);
		} catch (ValidityException e) {
			throw new FilterException("Invalid XML: " + e.getMessage()  + getDebugInfo(), e);
		} catch (ParsingException e) {
			throw new FilterException("XML Parsing Error: " + e.getMessage() + getDebugInfo(), e);
		} catch (IOException e) {
			throw new FilterException("I/O Error: " + getDebugInfo(), e);
		} catch (SAXException e) {
			throw new FilterException("Could not load Xerces: " + e.getMessage()  + getDebugInfo(), e);
		}
	}
}
