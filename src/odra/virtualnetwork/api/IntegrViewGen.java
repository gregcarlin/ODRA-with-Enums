package odra.virtualnetwork.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.openeai.xml.XmlElementLocator;

/** This class represents Integration View Generator from XML file.
 */
public class IntegrViewGen {
	
	public static String xmlViewSchemaFileName = "res/p2p/integrationscript.xml";
    public static String targetFileName = "res/p2p/integrationscript_out.xml";
	public static String xmlGlobalViewSchemaFileName = "res/p2p/globalviewscript.xml";
	
	private final static String _count = "count";
	private final static String _operator = "operator";
	private final static String _seedname = "seedName";
	private final static String _view = "view";
	private final static String _name = "name";
	private final static String _id = "id";
	private final static String _virtobjpathwithdot = "virtObjPathWithDot";
	private final static String _contibutedpeername = "contibutedPeerName";

	

    public  IntegrViewGen() {
    	
 	
 
    	// parse XML file -> XML document will be build        
//    	Document xmlViewSchema = IntegrViewGenIO.parseFile(xmlViewSchemaFileName);

    	// change Seed Source in a View
        //Document doc1 = changeSeedSource(xmlViewSchema, "seedName", "PEER1");
        
        // add Contributed Peer Source
//        Document doc1 = addContributedSource (xmlViewSchema, "contributions", "peer1test_admin_grid", "PatientGrid");
//        Document doc2 = selectViewAsXMLDocument (doc1, "PatientGrid");
//        System.out.print(IntegrViewGenIO.convertToString(doc2));

        // remove Contributed Peer Source
//        Document doc1 = removeContributedSource (xmlViewSchema, "contributions", "peer2test_admin_grid", "PatientGrid");
//        System.out.print(IntegrViewGenIO.convertToString(doc1, xslTransformSchemaFileName));

        // write node and its child nodes into System.out
        //System.out.println("Statemend of XML document...");
        //writeDocumentToOutput(root,0);
        //System.out.println("... end of statement");

        // write changed Document into XML file
//        IntegrViewGenIO.saveXMLDocument(targetFileName, doc1);
    }
    
    
    
    /** Starts XML parsing example
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new IntegrViewGen();
        
    }
    
    
    /** Changes Seed Source in whole XML View.
     * @param doc source as XML parsed document
     * @param tagName XML tag name where seed info is stored
     * @param peerName location of seed source
     * @return XML document or <B>null</B> if error occured
     */
    public Document changeSeedSource (Document doc, String tagName, String peerName)
    {
    	Element root = doc.getRootElement();

        List children = root.getChildren();
        for (int i = 0; i < children.size(); i++) 
        {
        	Element child = (Element)children.get(i);
        	changeTagContent (child, tagName, peerName);
        }
        return doc;
     }

    /** Selects required View.
     * @param view source as XML document
     * @param viewNameAttribute current View to be selected
     * @return XML Element or <B>null</B> if error occured
     */
 
    public Document selectViewAsXMLDocument (Document doc, String viewNameAttribute) {
    	Document out = null;
    	Element root = doc.getRootElement();
        List views = root.getChildren();
        Iterator vIterator = views.iterator();
        while (vIterator.hasNext())
        {
        	Element view = (Element) vIterator.next();
            if (view.getName().equals(_view) && view.getAttributeValue(_name).equals(viewNameAttribute))
            {
            	Element current = (Element)view.clone();
            	current.detach();
            	out = new Document(current);
            }
        }
        return out;
    }
 

    /** Adds Contributed Peer in whole XML View.
     * @param doc source as XML parsed document
     * @param tagName XML tag name where contributions are stored
     * @param gridlinkName current contributed peer to be added
     * @param viewNameAttribute current View to be modified
     * @return XML document or <B>null</B> if error occured
     */
 
    public Document addContributedSource (Document doc, String tagName, String gridlinkName, String viewNameAttribute)
    	{
    	if (doc == null) doc = IntegrViewGenIO.parseFile(xmlViewSchemaFileName);
    	
    	XmlElementLocator xmlLocator = new XmlElementLocator();
    	Element root = doc.getRootElement();
        List views = root.getChildren();
        Iterator vIterator = views.iterator();
        while (vIterator.hasNext())
        {
        	Element view = (Element) vIterator.next();


        	
        	//sets all seedNames in XML Views while contributors count == 0

        	Element contribId = xmlLocator.getElementByName(view, tagName);
        	if (contribId.getName().equals(tagName) && contribId.getAttributeValue(_count).equals("0") && view.getAttributeValue(_name).equals(viewNameAttribute))
        	{
               	changeTagContent (view, _seedname, gridlinkName);
        	}

        	
        	//here starts view processing

        	if (view.getAttributeValue(_name).equals(viewNameAttribute))
        		processView(view, tagName, gridlinkName, viewNameAttribute, "add");
        }
        return doc;
    }
    
 
    public void processView(Element current, String tagName, String gridlinkName, String viewNameAttribute, String direction) {
        if (current.getName().equals(_view) && current.getAttributeValue(_name).equals(viewNameAttribute))
       	{
           	List viewChildren = current.getChildren();
            Iterator vIterator = viewChildren.iterator();
            while (vIterator.hasNext()) {
            	Element vChild = (Element) vIterator.next();
            	processMainView(current, vChild, tagName, gridlinkName, viewNameAttribute, direction);
            	processSubView(current, vChild, tagName, gridlinkName, viewNameAttribute, direction);
            }
       	}
    }

    public void processMainView(Element view, Element current, String tagName, String gridlinkName, String viewNameAttribute, String direction) {
    	if (current.getName().equals("mainView"))
       	{
           	List mainViewChildren = current.getChildren();
            Iterator mvIterator = mainViewChildren.iterator();
            while (mvIterator.hasNext()) {
            	Element mvChild = (Element) mvIterator.next();
            	if (direction.equals("add"))
            		processAddContributions(view, mvChild, tagName, gridlinkName, viewNameAttribute);
            	if (direction.equals("rmv"))
                	processRmvContributions(view, mvChild, tagName, gridlinkName, viewNameAttribute);
            }
       	}
     }

     public void processSubView(Element view, Element current, String tagName, String gridlinkName, String viewNameAttribute, String direction) {
            if (current.getName().equals("subView"))
        	{
            	List subViewChildren = current.getChildren();
            	Iterator svIterator = subViewChildren.iterator();
            	while (svIterator.hasNext()) {
            		Element svChild = (Element) svIterator.next();
                	processMainView(view, svChild, tagName, gridlinkName, viewNameAttribute, direction);
                	processSubView(view, svChild, tagName, gridlinkName, viewNameAttribute, direction);
            		if (direction.equals("add"))
            			processAddContributions(view, svChild, tagName, gridlinkName, viewNameAttribute);
            		if (direction.equals("rmv"))
                    	processRmvContributions(view, svChild, tagName, gridlinkName, viewNameAttribute);
            	}
        	}
      }

     public void processAddContributions(Element view, Element current, String tagName, String gridlinkName, String viewNameAttribute) {
  	  	    if (current.getName().equals(tagName))
		   	{
		   		List contribChildren = current.getChildren();
		   		int contribCount = Integer.parseInt(current.getAttributeValue(_count));
					Element virtObjPathWithDot = current.getChild(_virtobjpathwithdot);
		
					if (current.getAttributeValue(_count).equals("0"))
					{
						Element op = (Element)contribChildren.get(0);
						if (op.getName().equals(_operator) && op.getAttributeValue(_id).equals("0"))
							{
								op.setAttribute(_id, gridlinkName);
							}
						Element cpn = (Element)contribChildren.get(1);
						if (cpn.getName().equals(_contibutedpeername) && cpn.getAttributeValue(_id).equals("0"))
							{
								cpn.setAttribute(_id, gridlinkName).setText(gridlinkName);
							}
						Element vo = (Element)contribChildren.get(2);
						if (vo.getName().equals(_virtobjpathwithdot) && vo.getAttributeValue(_id).equals("0"))
							{
								vo.setAttribute(_id, gridlinkName);
							}
						current.setAttribute(_count, String.valueOf(contribCount+1));
					}
					else
					{
						contribChildren.add(new Element(_operator).setAttribute(_id, gridlinkName).setText(" union "));
			       		contribChildren.add(new Element(_contibutedpeername).setAttribute(_id, gridlinkName).setText(gridlinkName));
			       		contribChildren.add(new Element(virtObjPathWithDot.getName()).setAttribute(_id, gridlinkName).setText(virtObjPathWithDot.getText()));
			       		current.setAttribute(_count, String.valueOf(contribCount+1));
					}
		   	}
    }
     
     public void processRmvContributions(Element view, Element current, String tagName, String gridlinkName, String viewNameAttribute) {
	  	    if (current.getName().equals(tagName))
		   	{
		   		List contribChildren = current.getChildren();
		   		int contribCount = Integer.parseInt(current.getAttributeValue(_count));
				Element virtObjPathWithDot = current.getChild(_virtobjpathwithdot);
		
				//we do the following when there is only one contribution (count == 1)
				if (current.getAttributeValue(_count).equals("1"))
				{
					Element op = (Element)contribChildren.get(0);
					if (op.getName().equals(_operator) && !op.getAttributeValue(_id).equals("0"))
						{
							op.setAttribute(_id, "0").setText("");
						}
					Element cpn = (Element)contribChildren.get(1);
					if (cpn.getName().equals(_contibutedpeername) && !cpn.getAttributeValue(_id).equals("0"))
						{
							cpn.setAttribute(_id, "0").setText("");
						}
					Element vo = (Element)contribChildren.get(2);
					if (vo.getName().equals(_virtobjpathwithdot) && !vo.getAttributeValue(_id).equals("0"))
						{
							vo.setAttribute(_id, "0");
						}
					current.setAttribute(_count, String.valueOf(contribCount-1));
				}
		       		
				//if first contributor left we need to use the second one as a first
				Element first = (Element)contribChildren.get(1);
		       		
		       	if (!current.getAttributeValue(_count).equals("1") && first.getAttributeValue(_id).equals(gridlinkName))
		       	{
		       		for (int r = 0; r <=2; r++)
		       		{
			       		contribChildren.remove(0);
		       		}

		       		current.setAttribute(_count, String.valueOf(contribCount-1));
			       		
			       	contribChildren = current.getChildren();
		       		Element opp = (Element)contribChildren.get(0);
		   			if (opp.getName().equals(_operator))
		   				{
		   					opp.setText("");
		   				}
		
			        //sets all seedNames in XML Views when contributors > 0
			        //onto first gridlinkName from queue of contibutedPeerName field
			        //at contributions tag

			       	Element newSeed = (Element)contribChildren.get(1);
		           	changeTagContent(view, _seedname, newSeed.getText());
	       		}
	   			else
	   				
	   			//removes useless contributor (this one who currently left)
	   			{
		       		int z = contribChildren.size()-1;
		       		while (z != 0)
		       		{
			       		Object o = contribChildren.get(z);
			       		if(o instanceof Element)
			       		{
			       			Element next = (Element)o;
			       			if(next.getAttributeValue(_id).equals(gridlinkName))
			       			{
			       				contribChildren.remove(o);
			       			}
			       		}
			       		z--;      		
		       		}
	   			}
	       		current.setAttribute(_count, String.valueOf(contribCount-1));
	       	}
     }



    /** Removes Contributed Peer in whole XML View.
     * @param doc source as XML parsed document
     * @param tagName XML tag name where contributions are stored
     * @param gridlinkName current contributed peer to be added
     * @param viewNameAttribute current View to be modified
     * @return XML document or <B>null</B> if error occured
     */
    public Document removeContributedSource (Document doc, String tagName, String gridlinkName, String viewNameAttribute)
	{
    	XmlElementLocator xmlLocator = new XmlElementLocator();

    	Element root = doc.getRootElement();
        List views = root.getChildren();
        Iterator vIterator = views.iterator();
        while (vIterator.hasNext())
        {
        	Element view = (Element) vIterator.next();


        	
        	//sets all seedNames in XML View while contributors count = 0

        	Element contribId = xmlLocator.getElementByName(view, tagName);
        	if (contribId.getName().equals(tagName) && contribId.getAttributeValue(_count).equals("1") && view.getAttributeValue(_name).equals(viewNameAttribute))
        	{
               	changeTagContent (view, _seedname, "");
        	}

        	
        	//here starts view processing

        	if (view.getAttributeValue(_name).equals(viewNameAttribute))
        		processView(view, tagName, gridlinkName, viewNameAttribute, "rmv");
        }
    return doc;
	}


    
    //submethods
    public Element changeTagContent(Element node, String tagName, String content)
    {
       	if (node.getChildren() != null)
       	{
	   		List children = node.getChildren();
	   		for (int i = 0; i < children.size(); i++)
	   		{
	   			Element curr = (Element)children.get(i);
	   			changeTagContent(curr, tagName, content);
	   		}
       	}
       	if (node.getName().equals(tagName))
       	{
       		node.setText(content);
       	}
       	return node;
    }
    	
    public Element searchTagss(Element node, String tagName)
    {
    	while (!node.getName().equals(tagName))
	    {
	    	if (!node.getChildren().equals(null))
		    {
		     	List children = node.getChildren();
		   		for (int i = 0; i < children.size(); i++)
		     	{
		   			Element curr = (Element)children.get(i);
		   			if (curr.getName().equals(tagName))
		     		{
		   				node = curr;
		   				break;
		     		}
		   			else
		   			{
		   				node = searchTagss(curr, tagName);
		   			}
		     	}
		   	break;
		    }
	    }
	return node;
    }



 
}
    

