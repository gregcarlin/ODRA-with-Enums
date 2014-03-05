package odra.sbql.external.ws;

import java.util.ArrayList;
import java.util.List;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SchemaElement {
	protected String name;
	protected List<SchemaElement> nodes = new ArrayList<SchemaElement>();
	
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}	
	
	public SchemaElement(){};
	
	public SchemaElement(Node item, DocumentWSDL documentWSDL) {
		//System.out.println("item: "+item.getNodeName());
		NodeList children = item.getChildNodes();
		for (int j=0; j<children.getLength();j++)
		{
			Node tmpN = children.item(j);
			if (tmpN.getNodeName()=="xs:complexType") 
			{
				ComplexType tmp = new ComplexType(tmpN, documentWSDL);
				nodes.add(tmp);
			}
			if (tmpN.getNodeName()=="xs:simpleType") 
			{
				SimpleType tmp = new SimpleType(tmpN, documentWSDL);
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
