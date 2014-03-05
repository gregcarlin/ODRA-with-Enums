package odra.sbql.external.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;


import org.w3c.dom.Element;

public class DocumentWSDL {
	private String address;
	private String targetNameSpace;
	private String serviceName;
	private PortWSDL[] ports;
	public SchemaDescription[] schema;
	public HashMap<String, OperationWSDL> operations = new HashMap<String, OperationWSDL>();
	public HashMap<String, SchemaElement> types = new HashMap<String, SchemaElement>();
	
	
	
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAddress() {
		return address;
	}
	public void setTargetNameSpace(String targetNameSpace) {
		this.targetNameSpace = targetNameSpace;
	}
	public String getTargetNameSpace() {
		return targetNameSpace;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceName() {
		return serviceName;
	}

	public void setPorts(PortWSDL[] ports) {
		this.ports = ports;
	}
	public PortWSDL[] getPorts() {
		return ports;
	}

	public DocumentWSDL(String url)
	{
		try
    	{
    	WSDLFactory factory = WSDLFactory.newInstance();
    	WSDLReader reader = factory.newWSDLReader();
    	reader.setFeature("javax.wsdl.verbose", true);
    	reader.setFeature("javax.wsdl.importDocuments", true);
    	Definition def = reader.readWSDL(null, url);
    	
    	address = url;
    	
    	
		//target namespace
    	String tns = def.getTargetNamespace();
    	this.targetNameSpace = tns;
    	this.serviceName = def.getQName().getLocalPart();
    	
    	//port
    	Map portsMap = def.getAllPortTypes(); 	
    	Set portKeys = portsMap.keySet();
    	Iterator portIterator = portKeys.iterator();  	
    	ports = new PortWSDL[portKeys.size()];
    	int i=0;
    	while (portIterator.hasNext())
	    	{
	    	QName port = (QName)portIterator.next(); 	
	    	PortType portType = def.getPortType(port);
	    	ports[i] = new PortWSDL(portType, def, this);
	    	i++;
	    	}
    	
    	Types wsdl4jTypes = def.getTypes();
    	Object extensibilityElement;
    	List<SchemaDescription> lista = new ArrayList<SchemaDescription>();
    	
    	if (wsdl4jTypes != null) {
            for (Iterator iterator = wsdl4jTypes.getExtensibilityElements().iterator();
                 iterator.hasNext();) 
            	{
                extensibilityElement = iterator.next();

	                if (extensibilityElement instanceof Schema)
	                {
	                	Element schemaElement = ((Schema) extensibilityElement).getElement();
	                	lista.add(new SchemaDescription(schemaElement, this));
	                	//setSchema(new SchemaDescription(schemaElement));
	                }
                
                }
    	
    		}
    	SchemaDescription[] tmp = new SchemaDescription[lista.size()];
    	lista.toArray(tmp);
    	schema = tmp; 	
    	}
		catch (WSDLException e)
    	{
    	e.printStackTrace();
    	} 
		
    	//System.out.println(this.toString());
	}
	
	public void setSchema(SchemaDescription[] schema) {
		this.schema = schema;
	}
	public SchemaDescription[] getSchema() {
		return schema;
	}
	
}
