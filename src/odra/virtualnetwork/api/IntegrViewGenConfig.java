package odra.virtualnetwork.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;

/** This class processes Configuration XML for Integration View Generator.
 */
public class IntegrViewGenConfig {

	public static String xmlIntegrGenConfigFileName = "res/p2p/integrationconfig.xml";
    public static String targetFileName = "res/p2p/integrationconfig_out.xml";

	
	
	public IntegrViewGenConfig() {
//		List x = getConfiguration();
//		int a = numberOfViews(x);
//		Element vi = getViewProperties(x,a);
//		System.out.println();
	}
	

    /** Loads configuration file
     * @return Document
     */
	public List getConfiguration() {
	Document xmlViewConfig = IntegrViewGenIO.parseFile(xmlIntegrGenConfigFileName);
	Element root = xmlViewConfig.getRootElement();
	List children = root.getChildren();
	return children;
	}

	
    /** Counts Views to be processed.
     * @return int
     */
    public int numberOfViews(List viewsConfig) {
    int number = viewsConfig.size();
    return number;
    }

    /** Gets View properties.
     * @return Element
     */
	public Element getViewProperties (List viewsConfig, int viewNumber)
	{
	    if (viewsConfig.size() > 0)
	    {
	    	Element curr = (Element)viewsConfig.get(viewNumber-1);
	    	if (curr.getName() == "View")
	    	{
	    		return curr;
	    	}
	    	else
	    		{
		    	System.out.println("There is no view to process in 'integrationconfig'");
	    		return null;
	    		}
	    }
	    else
	    {
	    	System.out.println("'integrationconfig' structure is failed");
	    	return null;
	    }
	}
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        new IntegrViewGenConfig();


	}

}
