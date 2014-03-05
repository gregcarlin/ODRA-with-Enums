package odra.sbql.external.ws;

import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SequenceElement extends SchemaElement{
	
	public SequenceElement(Node item, DocumentWSDL documentWSDL) {
		NodeList children = item.getChildNodes();
		for (int j=0; j<children.getLength();j++)
		{
			Node tmpN = children.item(j);
			SchemaElement tmp;
			if (tmpN.getNodeName()=="xs:element") 
			{
				tmp = new AtomElement(tmpN, documentWSDL);
				nodes.add(tmp);
			}
		}
	}
	
	public Signature sig()
	{
		if (nodes.size()==0) return null;
		StructSignature ret = new StructSignature();
		for (int i=0;i<nodes.size();i++)
		{
			ret.addField(nodes.get(i).sig());
		}
		return ret;
	}	

}
