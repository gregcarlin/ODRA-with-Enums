package odra.sbql.external.ws;


import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

public class ComplexType extends SchemaElement{
private String typeName;
	
	public ComplexType(Node item, DocumentWSDL documentWSDL) {
		NamedNodeMap map = item.getAttributes();
		typeName = map.getNamedItem("name").getNodeValue();
		//System.out.println("item: "+item.getNodeName()+" "+typeName);
		NodeList children = item.getChildNodes();
		for (int j=0; j<children.getLength();j++)
		{
			Node tmpN = children.item(j);
			SchemaElement tmp;
			if (tmpN.getNodeName()=="xs:sequence") 
			{
				tmp = new SequenceElement(tmpN, documentWSDL);
				nodes.add(tmp);
			}
			if (tmpN.getNodeName()=="xs:all") 
			{
				tmp = new AllElement(tmpN, documentWSDL);
				nodes.add(tmp);
			}
			if (tmpN.getNodeName()=="xs:choice") 
			{
				tmp = new ChoiceElement(tmpN, documentWSDL);
				nodes.add(tmp);
			}
			if (tmpN.getNodeName()=="xs:simpleType") 
			{
				tmp = new SimpleType(tmpN, documentWSDL);
				nodes.add(tmp);
			}
			
		}
		documentWSDL.types.put(typeName, this);
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}
	
	public String toString()
	{
		String retstring ="";
		if (nodes.get(0).toString().equals("")) return retstring;
		return "type "+typeName+" "+nodes.get(0).toString();
	}
	
	//replace with new implementation, add type definition lookup
	public Signature sig()
	{
		if (nodes.size()==0) return null;
		if (nodes.size()==1) return new BinderSignature(typeName, nodes.get(0).sig());
		StructSignature ret = new StructSignature();
		for (int i=0;i<nodes.size();i++)
		{
			ret.addField(nodes.get(i).sig());
		}
		if (name.equals("typeName")) name="returnValue";
		return new BinderSignature(typeName,ret);
	}
}
