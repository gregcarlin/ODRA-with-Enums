package odra.sbql.external.ws;

import java.util.HashMap;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.db.StdEnvironment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AtomElement extends SchemaElement{
	private String type;
	private String odraType;
	private String minoccurs;
	private String maxoccurs;
	
	static HashMap<String, String> simpleTypeMapping;
	static String [][] simpleTypeMappingArray = 
		  { {"xs:string", 			"string"},
			{"xs:integer", 		"integer"},
			{"xs:int" , 			"integer"},
			{"xs:long", 			"integer"},
			{"xs:short", 			"integer"},
			{"xs:decimal", 		"real"},
			{"xs:float", 			"real"},
			{"xs:double", 			"real"},
			{"xs:boolean", 		"boolean"},
			{"xs:byte", 			"integer"},
			{"xs:unsignedInt", 	"integer"},
			{"xs:unsignedShort", 	"integer"},
			{"xs:unsignedByte", 	"integer"},
			{"xs:QName", 			"string"},
			{"xs:dateTime", 		"string"},
			{"xs:date", 			"string"},
			{"xs:time", 			"string"},
			{"xs:anyURI", 			"string"},
			{"xs:base64Binary", 	"integer"},
			{"xs:hexBinary", 		"integer"},
			{"xs:anySimpleType", 	"string"},
			{"xs:duration", 		"string"},
			{"xs:gYearMonth", 		"string"},
			{"xs:gYear", 			"string"},
			{"xs:gMonthDay", 		"string"},
			{"xs:gDay", 			"string"},
			{"xs:gMonth", 			"string"},
			{"xs:normalizedString", "string"},
			{"xs:token", 			"string"},
			{"xs:language", 		"string"},
			{"xs:Name", 			"string"},
			{"xs:NCName", 			"string"},
			{"xs:ID", 				"string"},
			{"xs:NMTOKEN", 		"string"},
			{"xs:NMTOKENS", 		"string"},
			{"xs:nonPositiveInteger", "integer"},
			{"xs:negativeInteger", "integer"},
			{"xs:nonNegativeInteger", "integer"},
			{"xs:unsignedLong", 	"integer"},
			{"xs:positiveInteger", "integer"},
			{"xs:IDREF"		,	"string"},
			{"xs:IDREFS"		,	"string"},
		  };

	static{
		simpleTypeMapping = new HashMap<String, String>();
		for(String[] entry:simpleTypeMappingArray)
			simpleTypeMapping.put( entry[0], entry[1] );
	}
	
	public AtomElement(Node item, DocumentWSDL documentWSDL) {
		NamedNodeMap map = item.getAttributes();
		setType(map.getNamedItem("type").getNodeValue());
		setName(map.getNamedItem("name").getNodeValue());
		Node min = map.getNamedItem("minOccurs");
		if (min!=null) setMinoccurs(min.getNodeValue()); else setMinoccurs("1");
		Node max = map.getNamedItem("maxOccurs");
		if (max!=null) setMaxoccurs(max.getNodeValue()); else setMaxoccurs("1");
		if (maxoccurs.equals("unbounded")) setMaxoccurs("*");
		odraType = lookUpOdraType(type);
	}
	
	private String lookUpOdraType(String type2) {
		String rettype = simpleTypeMapping.get(type2);
		if (rettype.equals("null")) rettype=type2;
		return rettype;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setMinoccurs(String minoccurs) {
		this.minoccurs = minoccurs;
	}

	public int getMinoccurs() {
		if (minoccurs.equals("*")) return -1;
		else
			try{
			return Integer.parseInt(minoccurs);
			} catch (NumberFormatException e)
			{
				return 1;
			}
	}

	public void setMaxoccurs(String maxoccurs) {
		this.maxoccurs = maxoccurs;
	}

	public int getMaxoccurs() {
		if (maxoccurs.equals("*")) return -1;
		else
			try{
			return Integer.parseInt(maxoccurs);
			} catch (NumberFormatException e)
			{
				return 1;
			}
	}

	public Signature sig()
	{
		ValueSignature ret;
		StdEnvironment env = StdEnvironment.getStdEnvironment();
		if (type.equals("integer")) ret = new ValueSignature(env.integerType);
		else if (type.equals("real")) ret = new ValueSignature(env.realType);
		else if (type.equals("boolean")) ret = new ValueSignature(env.booleanType);
		else ret = new ValueSignature(env.stringType);
		ret.setMinCard(getMinoccurs());
		ret.setMaxCard(getMaxoccurs());
		if (name.equals("return")) name="returnValue";
		return new BinderSignature(name,ret);
	}
}
