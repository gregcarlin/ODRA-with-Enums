package odra.filters.XML;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.logging.Level;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;
import odra.db.DatabaseException;
import odra.db.objects.data.DataObjectKind;
import odra.filters.FilterException;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.RemoteReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.Names;
import odra.system.config.ConfigClient;

//  (("kk", "http://ll" as @xmlns_m) as kk, "ll" as @l, "http://ll" as @xmlns_SOAP_ENV) as oo; 
// ("kk", "http://def" as |xmlns|m) as |m|o;
//  ("kk" as |m|KK, "http://def" as |xmlns|m) as o;
//  ("http://def" as |xmlns|m, "kk" as |m|KK) as o;
// ("http://def" as |xmlns|m, "kk" as |m|KK) as o;
// ----
// ( ( "http://" as uri, "oo" as prefix) as @namespaceDef, "kkk" as KK, "kkkk" as |oo|KKK ) as OO;
// ( ( "http://" as uri, "oo" as prefix) as @namespaceDef, ( "http://" as uri, "outer" as prefix) as @namespaceUse, "kkk" as KK, "kkkk" as |oo|KKK ) as OO;

/**
 * This class converts query results to XML strings.
 * 
 * @author Krzysztof Kaczmarski
 */
public class XMLResultPrinter {
	/** output encoding name */
	private String outputEncoding = "UTF-8";
	
	Stack<Integer> nsCounter = new Stack<Integer>();
	NamespaceStack ns = new NamespaceStack();

	/**
	 * Function converting query results to XML strings.
	 * 
	 * @param res Results to be converted to XML string.
	 * @return XML string with converted Result.
	 */
	public String print(Result res) {
		
		try {
			Document doc = getXMLDocument(res);
			
			Serializer serializer;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
		
			serializer = new Serializer( output, outputEncoding);
			serializer.setIndent(4);
			serializer.setMaxLength(80);
			serializer.write(doc);
			
			return new String(output.toByteArray(), outputEncoding);
			
		} catch (Exception e) {
			ConfigClient.getLogWriter().getLogger().log( Level.SEVERE, "Cannot export query result to XML!", e );
		}
		return "XMLResultPrinter: Problems with xml output. Read log.";
	}

	/**
	 * Returns XML document tree for given result.
	 * 
	 * @param res Result to be converted to XML
	 * @return properly formed XML document
	 * @throws DatabaseException
	 * @throws FilterException
	 */
	public Document getXMLDocument(Result res) throws DatabaseException, FilterException
	{
		Element root = new Element("RESULT");
		if (res instanceof BinderResult) 
			processBinder((BinderResult)res, root, true);
		else if (res instanceof StructResult)
			processStruct( (StructResult)res, root, false, false );
		else
		{
			Element result = new Element("RESULT");
			root.appendChild(result);
			produceElement( res, result, false, false );
		}
		
		root = clearRoot(root);
		return new Document(root);
	}
	
	//***********************************************************************************

	private Element clearRoot(Element root) {
		if ((root.getChildCount() == 1) && 
				(root.getChild(0) instanceof Element) && 
				(root.getAttributeCount()==0))
		{
			root = (Element)root.getChild(0);
			root.detach();
		}
		return root;
	}

	private String fixString(String s)
	{
		return s.replace("$", "_").replace("@", "");
	}

	private Element produceElement(	Result res, Element parentElement, boolean isBag, boolean isBinder ) throws DatabaseException, FilterException {

		Element elem = null;

		if (res instanceof BinderResult) 
			processBinder( (BinderResult)res, parentElement, isBinder );
		else if (res instanceof StructResult) 
			processStruct( (StructResult)res, parentElement, isBag, isBinder);
		else if (res instanceof BagResult) 
			processBag( (BagResult)res, parentElement, isBinder );
		else if (res instanceof IntegerResult)
			produceValue( parentElement, ((Integer)((IntegerResult) res).value).toString(), isBag, isBinder );
		else if (res instanceof StringResult)
			produceValue( parentElement, (((StringResult) res).value), isBag, isBinder );
		else if (res instanceof DoubleResult)
			produceValue( parentElement, ((Double)((DoubleResult) res).value).toString(), isBag, isBinder );
		else if (res instanceof BooleanResult)
			produceValue( parentElement, ((Boolean)((BooleanResult) res).value).toString(), isBag, isBinder );
		else if (res instanceof ReferenceResult)
			produceValue( parentElement, "&" + ((ReferenceResult) res).value.toString(), isBag, isBinder );
		else if (res instanceof DateResult)
			produceValue( parentElement, ((DateResult) res).format(), isBag, isBinder );
		else if (res instanceof RemoteReferenceResult)
			produceValue( parentElement, "&" + ((RemoteReferenceResult) res).id.toString() + "@" + ((RemoteReferenceResult) res).host + "/" + ((RemoteReferenceResult) res).schema, isBag, isBinder );
		else
			assert false;
	
		return elem;
	}

	private String getValueAsString( Result res ) throws FilterException
	{
		String s;
		if (res instanceof DoubleResult)
			s = ((DoubleResult)res).value+"";
		else if (res instanceof IntegerResult)
			s = ((IntegerResult)res).value+"";
		else if (res instanceof BooleanResult)
			s = ((BooleanResult)res).value+"";
		else if (res instanceof StringResult)
			s = ((StringResult)res).value;
		else if (res instanceof DateResult)
			s = ((DateResult)res).format();
		else
		{
			if (res==null)
				s = "";
			else
				throw new FilterException("Unknown Result Value", null);
		}
		return s;
	}
		
	private Element processBinder( BinderResult res, Element parent, boolean isTagged ) throws DatabaseException, FilterException
	{
		XMLAnnotationsInfo annInfo = new XMLAnnotationsInfo( getAnn(res.value) );
		if (annInfo.isAttribute)
		{
			if (isTagged)
			{
				StructResult attrValue = (StructResult)res.value;
				
				String value = getValueAsString((findFirstFieldByName(attrValue, XMLImportFilter.PCDATA)));
				Attribute attr = new Attribute(fixString(res.getName()), value);
				if (parent!=null)
					parent.addAttribute(attr);
				if (annInfo.prefix.length()>0)
					attr.setNamespace(annInfo.prefix, annInfo.URI);
			}
		}
		else
		{
			if (res.getName().equals(XMLImportFilter.PCDATA))
				produceValue(parent, getValueAsString(res.value), false, false);
			else if (res.getName().startsWith( XMLImportFilter.XMLNS_DEF ))
			{
				StructResult attrValue = (StructResult)res.value;
				
				String uri = getValueAsString((findFirstFieldByName(attrValue, XMLImportFilter.NS_URI)));
				String prefix = getValueAsString((findFirstFieldByName(attrValue, XMLImportFilter.NS_PREFIX)));
				parent.addNamespaceDeclaration(prefix, uri );
				addToNamespaceScope(prefix, uri);
			}
			else if (res.getName().startsWith( XMLImportFilter.XMLNS_USE ))
			{
				StructResult attrValue = (StructResult)res.value;
				
				String uri = getValueAsString((findFirstFieldByName(attrValue, XMLImportFilter.NS_URI)));
				String prefix = getValueAsString((findFirstFieldByName(attrValue, XMLImportFilter.NS_PREFIX)));
				parent.setNamespaceURI(uri);
				parent.setNamespacePrefix(prefix);
				addToNamespaceScope(prefix, uri);
			}
			else if (res.getName().startsWith(XMLImportFilter.ATTR_SIGN+"") && (parent!=null))
			{ 
				if (isTagged)
				{
					String value = getValueAsString(res.value);
					Attribute attr = new Attribute(fixString(res.getName().substring(1)), value);
					parent.addAttribute(attr);
				}
			}
			else if (res.getName().startsWith(XMLImportFilter.NS_SEPARATOR))
			{
				String fullName = res.getName().substring(1);
				int splitAt = fullName.indexOf( XMLImportFilter.NS_SEPARATOR);
				String name = fullName.substring( splitAt+1 );
				String prefix = fullName.substring( 0, splitAt );
				Element elem = new Element( name );
				String URI = ns.getSourceURI( prefix );
				if (URI != null)
				{
					elem.setNamespaceURI( URI );
					elem.setNamespacePrefix(prefix);
				}
				if (parent!=null)
					parent.appendChild(elem);
				produceElement( res.value, elem, false, true );
				return elem;
			}
			else 
			{
				Element elem = new Element( fixString(res.getName()) ); 
				if (parent!=null)
					parent.appendChild(elem);
				produceElement( res.value, elem, false, true );
				return elem;
			}
		}
		return parent;
	}

	private Element processStruct( StructResult res, Element parent, boolean isBag, boolean isBinder ) throws DatabaseException, FilterException
	{
		int kind = getKind(res);
		if (kind==DataObjectKind.ANNOTATED_STRING_OBJECT)
			produceElement( getVal(res), parent, isBag, isBinder );
		if (kind>DataObjectKind.ANNOTATED_STRING_OBJECT && kind<=DataObjectKind.ANNOTATED_REFERENCE_OBJECT)
		{
			SingleResult[] resarr = ((StructResult)res).fieldsToArray();
			XMLAnnotationsInfo annInfo = new XMLAnnotationsInfo( getAnn(res) );
			handleNamespaces(parent, annInfo);
			Result value = getVal(res);
			if (value!=null)
				produceElement( value, parent, false, false );
			if (!annInfo.isAttribute)
			{
				openNamespaceScope(annInfo);
				int firstField = findFirstRealFieldIndex(resarr);
				if (firstField>=0)
					for (int i = firstField; i < resarr.length; i++)
						produceElement( resarr[i], parent, false, isBinder );
				closeNamespaceScope();
			}
			else
				produceElement(findFirstFieldByName(res, XMLImportFilter.PCDATA), parent, false, isBinder);
		}
		else if (kind==0)
		{
			SingleResult[] resarr = ((StructResult)res).fieldsToArray();
			for (int i = 0; i < resarr.length; i++)
				produceElement( resarr[i], parent, false, isBinder );
		}
		return null;
	}

	private Element processBag( BagResult res, Element parentElement, boolean isBinder ) throws DatabaseException, FilterException {
		SingleResult[] resarr = res.elementsToArray();
		
		for (int i = 0; i < resarr.length; i++)
			produceElement( resarr[i], parentElement, i>0, isBinder );
		return null;
	}

	private Element produceValue( Element elem, String value, boolean isBag, boolean isBinder)
	{
		if (elem == null)
			return null;
		if (elem.getChildCount()>0)
			if (elem.getChild(elem.getChildCount()-1) instanceof Text)
				elem.appendChild("\n\r");
		
		elem.appendChild( value);
		
		return elem;
	}

	private int findFirstRealFieldIndex(SingleResult[] resarr) {
		for (int i=0; i<resarr.length; i++)
			if (resarr[i] instanceof BinderResult)
				if (!((BinderResult)resarr[i]).getName().startsWith("$"))
					return i;
		return -1;
	}
	
	private Result findFirstFieldByName(Result res, String name)
	{
		if ( !(res instanceof StructResult) )
			return null;
		StructResult structRes = (StructResult)res;
		for ( Result r : structRes.fieldsToArray())
			if ( r instanceof BinderResult )
				if ( ((BinderResult)r).getName().equals(name) )
					return ((BinderResult)r).value;
		return null;
	}
	
	private int getKind(Result res)
	{
		Result kindRes = findFirstFieldByName(res, "$kind");
		if (kindRes!=null)
			return ((IntegerResult)kindRes).value;
		else
			return 0;
	}

	private Result getVal( Result res ) throws DatabaseException
	{
		Result kindRes = findFirstFieldByName(res, Names.namesstr[Names.VALUE_ID]);
		if (kindRes!=null)
			return kindRes;
		else
			return null;
	}

	private StructResult getAnn( Result res ) throws DatabaseException
	{
		Result kindRes = findFirstFieldByName(res, Names.namesstr[Names.ANNOTATION_ID]);
		if (kindRes!=null)
			return (StructResult)kindRes;
		else
			return null;
	}

	private void openNamespaceScope(XMLAnnotationsInfo annInfo)
	{
		nsCounter.push(annInfo.namespaceDefs.size());
		for (NamespaceStack.NamespaceDef nsDef:annInfo.namespaceDefs)
			ns.push(nsDef);
	}
	
	private void closeNamespaceScope()
	{
		for( int i=nsCounter.pop(); i>0; i--)
			ns.pop();
	}
	private void openNamespaceScope()
	{
		nsCounter.push( 0 );	
	}
	
	private void addToNamespaceScope(String prefix, String URI)
	{
		ns.push( new NamespaceStack.NamespaceDef(prefix, URI, null) );
		if (nsCounter.size()>0)
			nsCounter.push( nsCounter.pop()+1 );
		else
			nsCounter.push( 1 );
	}
	
	private void handleNamespaces(Element elem, XMLAnnotationsInfo annInfo)
	{
		if (elem==null)
			return;
		for (NamespaceStack.NamespaceDef n:annInfo.namespaceDefs)
		{
			String previousURI = ns.getSourceURI(n.prefix);
			if ( n.prefix.length()>0 )
				elem.addNamespaceDeclaration( n.prefix, n.uri );
			else if ( previousURI!=null && !n.uri.equals(previousURI) ) 
				elem.setNamespaceURI( n.uri );
		}
		if (annInfo.URI.length()>0)
		{	
			elem.setNamespaceURI(annInfo.URI);
			elem.setNamespacePrefix(annInfo.prefix);
		}
	}
	
	/**
	 * Sets the output encoding.
	 * 
	 * @param outputEncoding encoding name
	 */
	public void setOutputEncoding(String outputEncoding)
	{
		this.outputEncoding = outputEncoding;
	}
}