package odra.helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelpers {

	/**
	 * @param list
	 * @return
	 */
	public static List<Element> filterElements(NodeList list) {
		List output = new ArrayList<Element>();
		
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i) instanceof Element) {
				output.add(list.item(i));
			}
		}
		
		return output;
	}
	
	/**
	 * @param list
	 * @return
	 */
	public static Element firstElement(NodeList list) {
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i) instanceof Element) {
				return (Element) list.item(i);
			}
		}
		
		return null;
	}
}
