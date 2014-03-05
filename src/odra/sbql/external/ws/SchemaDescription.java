package odra.sbql.external.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.xerces.parsers.DOMParser;

public class SchemaDescription {

	private Element schema;
	private List<SchemaElement> nodes = new ArrayList<SchemaElement>();
	
	
	public SchemaDescription(Element schemaElement, DocumentWSDL documentWSDL) {
		schema = schemaElement;
		NodeList imports = schema.getElementsByTagName("xsd:import");
		List<String> locations = new ArrayList<String>();
		
		
		for (int i=0; i<imports.getLength();i++)
		{
			Node n = imports.item(i);
			if (n.hasAttributes())
			{
				NamedNodeMap map = n.getAttributes();
				for (int j=0; j<map.getLength(); j++)
				{
					Node att = map.item(j);
					if (att.getLocalName()=="schemaLocation") locations.add(att.getNodeValue());
				}
			}
		}
		for (int i=0; i<locations.size();i++)
		{
			String location = locations.get(i);
			DOMParser parser = new DOMParser();
			try {
				parser.parse(location);
			} catch (SAXException e) {
				System.out.println(location + " is not well-formed.");
			} catch (IOException e) {
				System.out.println("Due to an IOException, the parser could not check "+ location); 
			}
			Document spec = parser.getDocument();
			NodeList children = spec.getChildNodes();
			for (int j=0; j<children.getLength();j++)
			{
				SchemaElement tmp = new SchemaElement(children.item(j), documentWSDL);
				nodes.add(tmp);
			}
			
		}
	}

	public void setSchema(Element schema) {
		this.schema = schema;
	}

	public Element getSchema() {
		return schema;
	}

}
