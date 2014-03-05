/**
 * 
 */
package odra.jobc;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;


import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.RemoteReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.config.ConfigClient;

/**
 * XMLResultPrinter the modified copy of odra.filters.XML.XMlResult (by KK -
 * Krzysztof Kaczmarski) printer for JOBC (this was the fastest and easiest way
 * to add this functionality to JOBC) I know it is awful but discussed with KK :)
 * 
 * @author Radek Adamus
 * @since 2008-04-02 last modified: 2008-04-02
 * @version 1.0
 */
class JOBCXMLResultPrinter {
    /** output encoding name */
    private String outputEncoding = "UTF-8";


    /**
     * Function converting query results to XML strings.
     * 
     * @param res
     *                Results to be converted to XML string.
     * @return XML string with converted Result.
     */
    public String print(Result res) {

	try {
	    Document doc = getXMLDocument(res);

	    Serializer serializer;
	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    serializer = new Serializer(output, outputEncoding);
	    serializer.setIndent(4);
	    serializer.setMaxLength(80);
	    serializer.write(doc);

	    return new String(output.toByteArray(), outputEncoding);

	} catch (Exception e) {
	    ConfigClient.getLogWriter().getLogger().log(Level.SEVERE,
		    "Cannot export query result to XML!", e);
	}
	return "XMLResultPrinter: Problems with xml output. Read log.";
    }

    /**
     * Returns XML document tree for given result.
     * 
     * @param res
     *                Result to be converted to XML
     * @return properly formed XML document
     * @throws DatabaseException
     * @throws FilterException
     */
    public Document getXMLDocument(Result res)  {
	Element root = new Element("RESULT");
	if (res instanceof BinderResult)
	    processBinder((BinderResult) res, root, true);
	else if (res instanceof StructResult)
	    processStruct((StructResult) res, root, false, false);
	else {
	    Element result = new Element("RESULT");
	    root.appendChild(result);
	    produceElement(res, result, false, false);
	}

	root = clearRoot(root);
	return new Document(root);
    }

    // ***********************************************************************************

    private Element clearRoot(Element root) {
	if ((root.getChildCount() == 1)
		&& (root.getChild(0) instanceof Element)
		&& (root.getAttributeCount() == 0)) {
	    root = (Element) root.getChild(0);
	    root.detach();
	}
	return root;
    }

    private String fixString(String s) {
	return s.replace("$", "_").replace("@", "");
    }

    private Element produceElement(Result res, Element parentElement,
	    boolean isBag, boolean isBinder) {

	Element elem = null;

	if (res instanceof BinderResult)
	    processBinder((BinderResult) res, parentElement, isBinder);
	else if (res instanceof StructResult)
	    processStruct((StructResult) res, parentElement, isBag, isBinder);
	else if (res instanceof BagResult)
	    processBag((BagResult) res, parentElement, isBinder);
	else if (res instanceof IntegerResult)
	    produceValue(parentElement, Integer.toString(((IntegerResult) res).value) 
		    , isBag, isBinder);
	else if (res instanceof StringResult)
	    produceValue(parentElement,
		    (((StringResult) res).value).toString(), isBag, isBinder);
	else if (res instanceof DoubleResult)
	    produceValue(parentElement, Double.toString(((DoubleResult) res).value) 
		    , isBag, isBinder);
	else if (res instanceof BooleanResult)
	    produceValue(parentElement, Boolean.toString(((BooleanResult) res).value)
		    , isBag, isBinder);	
	else if (res instanceof DateResult)
	    produceValue(parentElement, Utils.date2Str(((DateResult) res).value), isBag,
		    isBinder);
	else if (res instanceof RemoteReferenceResult)
	    produceValue(parentElement, "&"
		    + ((RemoteReferenceResult) res).id.toString() + "@"
		    + ((RemoteReferenceResult) res).host + "/"
		    + ((RemoteReferenceResult) res).schema, isBag, isBinder);
	else
	    assert false;

	return elem;
    }

    private String getValueAsString(Result res) {
	String s;
	if (res instanceof DoubleResult)
	    s = ((DoubleResult) res).value + "";
	else if (res instanceof IntegerResult)
	    s = ((IntegerResult) res).value + "";
	else if (res instanceof BooleanResult)
	    s = ((BooleanResult) res).value + "";
	else if (res instanceof StringResult)
	    s = ((StringResult) res).value;
	else if (res instanceof DateResult)
	    s = Utils.date2Str(((DateResult) res).value);
	else {
	    if (res == null)
		s = "";
	    else {
		assert false : "Unknown Result Value";
		s = "";
	    }
	}
	return s;
    }

    private Element processBinder(BinderResult res, Element parent,
	    boolean isTagged)  {
	Element elem = new Element(fixString(res.getName()));
	if (parent != null)
	    parent.appendChild(elem);
	produceElement(res.value, elem, false, true);
	return elem;

    }

    private Element processStruct(StructResult res, Element parent,
	    boolean isBag, boolean isBinder)  {
	
	
	
	    SingleResult[] resarr = ((StructResult) res).fieldsToArray();
	    for (int i = 0; i < resarr.length; i++)
		produceElement(resarr[i], parent, false, isBinder);
	
	return null;
    }

    private Element processBag(BagResult res, Element parentElement,
	    boolean isBinder)  {
	SingleResult[] resarr = res.elementsToArray();

	for (int i = 0; i < resarr.length; i++)
	    produceElement(resarr[i], parentElement, i > 0, isBinder);
	return null;
    }

    private Element produceValue(Element elem, String value, boolean isBag,
	    boolean isBinder) {
	if (elem == null)
	    return null;
	if (elem.getChildCount() > 0)
	    if (elem.getChild(elem.getChildCount() - 1) instanceof Text)
		elem.appendChild("\n\r");
	elem.appendChild(value);
	return elem;
    }

   
    

    /**
     * Sets the output encoding.
     * 
     * @param outputEncoding
     *                encoding name
     */
    public void setOutputEncoding(String outputEncoding) {
	this.outputEncoding = outputEncoding;
    }
}
