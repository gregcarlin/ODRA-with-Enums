package odra.sbql.external;

import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;

import odra.sbql.external.ws.DocumentWSDL;
import odra.sbql.external.ws.OperationWSDL;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.CommonsHTTPTransportSender;




/**
 * @author pietia
 *
 */
public class WSLib {
		
	private OMFactory fac = OMAbstractFactory.getOMFactory();
	private OMNamespace omNs;
	private String namespace;
	private String endpoint;
	private String wsdlloc;
	private String messageName;
	private String operationName;
	private String returnName;
	private OMElement payload;
	
	
	//internal store OMElement
	private Hashtable<Integer, OMElement> parameters = new Hashtable<Integer, OMElement>();
	private int paramcntr = 101;
	
	
	public Result callService() throws Exception
	{
		Options options = new Options();
        options.setTo(new EndpointReference(endpoint));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        TransportOutDescription tod = new TransportOutDescription(Constants.TRANSPORT_HTTP);
        tod.setSender(new CommonsHTTPTransportSender());
		options.setTransportOut(tod);
		
        options.setUseSeparateListener(false);
        
        ServiceClient servicClient = new ServiceClient();

        servicClient.setOptions(options);

        OMElement result = servicClient.sendReceive(payload);
		
		StringWriter writer = new StringWriter();
		
		payload.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
        writer.flush();	
        System.out.println(writer.toString());
        result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
        writer.flush();
        System.out.println(writer.toString());
		
        Result res = deserializeOutput(result);
        return res;
        

	}
	




	

	private Result deserializeOutput (OMElement output)
	{
		Result tmp = processElement(output);
		return tmp;
	}
	
	private SingleResult processElement(OMElement output)
	{
		Iterator it = output.getChildElements();
		Iterator atit = output.getAllAttributes();
		String name = output.getLocalName();
		if (name.equals("return")) name="returnValue";
		
		if (!((it.hasNext())||(atit.hasNext())))
		{
			//no attributes or child nodes - simple result node. determine type, value and return result
			String text = output.getText();
				//is it a double?
				try
				{
					Double dvalue = Double.valueOf(text);
					return new BinderResult(name, new DoubleResult(dvalue.doubleValue()));
				} catch (Exception e) {}
				//or maybe an Integer
				try
				{
					Integer ivalue = Integer.parseInt(text);
					return new BinderResult(name, new IntegerResult(ivalue.intValue()));
				} catch (Exception e) {}
				//Boolean by any chance?
				if (text.equals("false"))  return new BinderResult(name, new BooleanResult(false));
				if (text.equals("true"))  return new BinderResult(name, new BooleanResult(true));
				//if everything else fails, it's a String
				return new BinderResult(name, new StringResult(text));
		}
		else
		{
			//it has attributes, so it is a structure
			StructResult ret = new StructResult();
			//process the attributes first, 
			while (atit.hasNext())
			{
				OMAttribute att = (OMAttribute)atit.next();
				String aname = att.getLocalName();
				String avalue = att.getAttributeValue();
				//xml attributes are different types of strings, no need to worry too much
				ret.addField(new BinderResult(aname, new StringResult(avalue)));
			}
			while (it.hasNext())
	    	{
				OMElement child = (OMElement)it.next(); 
				SingleResult field = processElement(child);
				ret.addField(field);
	    	}
			return new BinderResult(name, ret);
		}
	}
	

	

	public void resetParams() {
		parameters.clear();
		paramcntr = 101;
		payload = fac.createOMElement(messageName, omNs);
		parameters.put(new Integer(100), payload);
	}

	public Result addField(int fnum, String fname) {
		OMElement value = fac.createOMElement(fname, null);
		OMElement parent = parameters.get(new Integer(fnum));
		parent.addChild(value);
		int number = paramcntr;
		paramcntr++;
		parameters.put(new Integer(number), value);
		return new IntegerResult(number);
	}

	public Result addText(int fnum, String text) {
		OMElement parent = parameters.get(new Integer(fnum));
		parent.addChild(fac.createOMText(parent, text));
		return new IntegerResult(fnum);
	}

	public Result addAttr(int fnum, String attname, String attvalue) {
		OMElement parent = parameters.get(new Integer(fnum));
		parent.addAttribute(attname,attvalue,null);
		return new IntegerResult(fnum);
	}

	public Signature config(String namespace2, String endpoint2, String wsdlloc2, String operationName) {
		namespace = namespace2;
		wsdlloc = wsdlloc2;
		
		DocumentWSDL doc = new DocumentWSDL(wsdlloc2);
		
		OperationWSDL op = doc.operations.get(operationName);
		
		endpoint = endpoint2;
		messageName = op.getMessageIn().getName();
		returnName = op.getMessageOut().getName();
		
		System.out.println("endpoint: "+endpoint+" in: "+messageName+" out: "+returnName);
		Signature retsig = doc.types.get(returnName).sig();
		
		omNs = fac.createOMNamespace(namespace, "ns1");
		resetParams();
		return retsig;
	}
	


	
	
}
 