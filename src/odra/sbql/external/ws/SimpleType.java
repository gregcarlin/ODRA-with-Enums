package odra.sbql.external.ws;

import org.w3c.dom.Node;

import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;

public class SimpleType extends SchemaElement {
	public SimpleType(Node item, DocumentWSDL documentWSDL) {
		super(item, documentWSDL);
		// TODO Auto-generated constructor stub
	}
	
	//replace with new implementation, add type definition lookup
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
