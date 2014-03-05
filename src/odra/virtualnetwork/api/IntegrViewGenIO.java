package odra.virtualnetwork.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.jdom.input.SAXBuilder;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.Element;
import org.jdom.Attribute;

/** This class represents IO methods for IntegrViewGen Class.
 */
public class IntegrViewGenIO {

	public static String xslTransformSchemaFileName = "res/p2p/integrationscript.xsl";


	public static void main(String[] args) {
		new IntegrViewGenIO();
	        
	}
	 

	/** Writes node and all child nodes into System.out
	 * @param node XML node from from XML tree wrom which will output statement start
	 * @param indent number of spaces used to indent output
	 */
	public static void writeDocumentToOutput(Element node,int indent) {
	    // get element name
	    String nodeName = node.getName();
	    // get element value
	    String nodeValue = node.getValue();
	    // get attributes of element
	    List attributes = node.getAttributes();
	    System.out.println(getIndentSpaces(indent) + "NodeName: " + nodeName + ", NodeValue: " + nodeValue);
	    for (int i = 0; i < attributes.size(); i++) {
	        Attribute attribute = (Attribute)attributes.get(i);
	        System.out.println(getIndentSpaces(indent + 2) + "AttributeName: " + attribute.getName() + ", attributeValue: " + attribute.getValue());
	    }
	    // write all child nodes recursively
	    List children = node.getChildren();
	    for (int i = 0; i < children.size(); i++) {
	        Element child = (Element)children.get(i);
	        Iterator it = child.getDescendants();
	        if (it.hasNext()) {
	            writeDocumentToOutput(child,indent + 2);
	        }
	    }
	}
	
	/** Saves XML Document into XML file.
	 * @param fileName XML file name
	 * @param doc XML document to save
	 * @return <B>true</B> if method success <B>false</B> otherwise
	 */    
	public static boolean saveXMLDocument(String fileName, Document doc) {
	    System.out.println("Saving XML file... " + fileName);
	    // open output stream where XML Document will be saved
	    File xmlOutputFile = new File(fileName);
	    FileOutputStream fos;
	    Transformer transformer;
	    try {
	        fos = new FileOutputStream(xmlOutputFile);
	    }
	    catch (FileNotFoundException e) {
	        System.out.println("Error occured: " + e.getMessage());
	        return false;
	    }
	    // Use a Transformer for output
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    try {
	        transformer = transformerFactory.newTransformer();
	    }
	    catch (TransformerConfigurationException e) {
	        System.out.println("Transformer configuration error: " + e.getMessage());
	        return false;
	    }
	    JDOMSource source = new JDOMSource(doc);
	    StreamResult result = new StreamResult(fos);
	    // transform source into result will do save
	    try {
	        transformer.transform(source, result);
	    }
	    catch (TransformerException e) {
	        System.out.println("Error transform: " + e.getMessage());
	    }
	    System.out.println("XML file saved.");
	    return true;
	}
	
	/** Parses XML file and returns XML document.
	 * @param fileName XML file to parse
	 * @return XML document or <B>null</B> if error occured
	 */
	public static Document parseFile(String fileName) {
	    System.out.println("Parsing XML file... " + fileName);
	    SAXBuilder docBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
	    docBuilder.setValidation(false);
	    
	    Document doc = null;
	
	    try {
	        doc = docBuilder.build(new File(fileName));
	    }
	    // indicates a well-formedness error
	    catch (JDOMException e) { 
	      System.out.println("Document is not well-formed.");
	      System.out.println(e.getMessage());
	    }  
	    catch (IOException e) { 
	      System.out.println(e);
	    }  
	
	    System.out.println("XML file parsed...");
	    return doc;
	}
	
	
	/** Converts XML file and returns TXT flat document as String.
	 * @param fileName XML file to convert
	 * @return XML document or <B>null</B> if error occured
	 */
	public static String convertToString (Document doc)
	{
		String buffer = new String();
		StringBuffer file = new StringBuffer();
		try 
		{
	        BufferedReader in = new BufferedReader(new FileReader(xslTransformSchemaFileName));
	        String inputLine ;
	        while ( (inputLine = in.readLine()) != null )
	        {
	             file.append(inputLine);
	        }
	        in.close(); 
	    }
		catch (Exception e) {
			e.printStackTrace(); 
		}           
	                      
		String XSL = file.toString();
		String xmlString = null;
		try
		{
	        TransformerFactory transfac = TransformerFactory.newInstance();
	        Transformer trans = transfac.newTransformer(new StreamSource(new ByteArrayInputStream(XSL.getBytes())));
	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
	
	        // create string from xml tree
	        StringWriter sw = new StringWriter();
	        StreamResult result = new StreamResult(sw);
	        JDOMSource source = new JDOMSource(doc);
	        trans.transform(source, result);
	        xmlString = sw.toString();
	        
	
	        // print xml
//	        System.out.println("Here's the xml:\n\n" + xmlString);
		}
		catch (Exception e) {
	    System.out.println(e);
		}
		return xmlString;
	}
	
//submethods
	
    private static String getIndentSpaces(int indent) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < indent; i++)
        {
            buffer.append(" ");
        }
        return buffer.toString();
    }


}