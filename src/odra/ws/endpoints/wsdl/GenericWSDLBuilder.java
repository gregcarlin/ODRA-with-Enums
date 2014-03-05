package odra.ws.endpoints.wsdl;

import odra.system.config.ConfigServer;
import odra.ws.endpoints.WSEndpointOptions;
import odra.ws.endpoints.WSHelper;

/**
 * Assembles WSDL contract definition for generic endpoint
 * 
 * @since 2007-06-22
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * 
 */
public class GenericWSDLBuilder extends WSDLBuilder {
	// cached contract
	private String cachedContract = null;

	// this is prepared for future enhancement, whcih will allow to touch genric
	// endpoint configuration
	// dynamically while server is running
	private WSEndpointOptions cachedOptions = null;

	/* (non-Javadoc)
	 * @see odra.ws.builders.WSDLBuilder#createDefinition(odra.ws.endpoints.WSEndpointOptions)
	 */
	@Override
	public String createDefinition(WSEndpointOptions options)
			throws WSDLBuilderException {
		if (options == null ) {
			throw new WSDLBuilderException("Cannot create WSDL document. ");
		}
		if (this.cachedOptions == null || !this.cachedOptions.equals(options) ){
			this.cachedOptions = options;
			this.refreshContract(options);
			
		}
		
		return this.cachedContract;
	}

	private void refreshContract(WSEndpointOptions options) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		sb.append("\t<wsdl:definitions xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"");
		sb.append(options.getTargetNamespace());
		sb.append("\" xmlns:s=\"http://www.w3.org/2001/XMLSchema\" xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\""); 
		sb.append(" targetNamespace=\"");
		sb.append(options.getTargetNamespace());
		sb.append("\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">\n");
		sb.append("\t\t<wsdl:types>\n");
		sb.append("\t\t\t<s:schema elementFormDefault=\"qualified\" targetNamespace=\"");
		sb.append(options.getTargetNamespace());
		sb.append("\">\n");
		sb.append("\t\t\t\t<s:element name=\"Execute\">\n");
		sb.append("\t\t\t\t\t<s:complexType>\n");
		sb.append("\t\t\t\t\t\t<s:sequence>\n");
		sb.append("\t\t\t\t\t\t\t<s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"sbql\" type=\"s:string\" />\n");
		sb.append("\t\t\t\t\t\t\t<s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"module\" type=\"s:string\" />\n");
		sb.append("\t\t\t\t\t\t</s:sequence>\n");
		sb.append("\t\t\t\t\t</s:complexType>\n");
		sb.append("\t\t\t\t</s:element>\n");
		sb.append("\t\t\t\t<s:element name=\"ExecuteResponse\">\n");
		sb.append("\t\t\t\t\t<s:complexType>\n");
		sb.append("\t\t\t\t\t\t<s:sequence>\n");
		sb.append("\t\t\t\t\t\t\t<s:any minOccurs=\"0\" maxOccurs=\"unbounded\" />\n");
		sb.append("\t\t\t\t\t\t</s:sequence>\n");
		sb.append("\t\t\t\t\t</s:complexType>\n");
		sb.append("\t\t\t\t</s:element>\n");
		sb.append("\t\t\t</s:schema>\n");
		sb.append("\t\t</wsdl:types>\n");
		sb.append("\t<wsdl:message name=\"ExecuteSoapIn\">\n");
		sb.append("\t\t<wsdl:part name=\"parameters\" element=\"tns:Execute\" />\n");
		sb.append("\t</wsdl:message>\n");
		sb.append("\t<wsdl:message name=\"ExecuteSoapOut\">\n");
		sb.append("\t\t<wsdl:part name=\"parameters\" element=\"tns:ExecuteResponse\" />");
		sb.append("\t</wsdl:message>\n");
		sb.append("\t<wsdl:portType name=\"");
		sb.append(options.getPortTypeName());
		sb.append("\">\n");
		sb.append("\t\t<wsdl:operation name=\"Execute\">\n");
		sb.append("\t\t\t<wsdl:input message=\"tns:ExecuteSoapIn\" />\n");
		sb.append("\t\t\t<wsdl:output message=\"tns:ExecuteSoapOut\" />\n");
		sb.append("\t\t</wsdl:operation>\n");
		sb.append("\t</wsdl:portType>\n");
		sb.append("\t<wsdl:binding name=\"");
		sb.append(options.getEndpointName());
		sb.append("SoapBinding\" type=\"tns:");
		sb.append(options.getPortTypeName());
		sb.append("\">\n");
		sb.append("\t\t<soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" />\n");
		sb.append("\t\t\t<wsdl:operation name=\"Execute\">\n");
		sb.append("\t\t\t<soap:operation soapAction=\"");
		sb.append(options.getTargetNamespace());
		sb.append("/Execute\" style=\"document\" />\n"); 
		sb.append("\t\t\t\t<wsdl:input>\n");
		sb.append("\t\t\t\t\t<soap:body use=\"literal\" />\n"); 
		sb.append("\t\t\t\t</wsdl:input>\n");
		sb.append("\t\t\t\t<wsdl:output>\n"); 
		sb.append("\t\t\t\t\t<soap:body use=\"literal\" />\n");
		sb.append("\t\t\t\t</wsdl:output>\n");
		sb.append("\t\t\t</wsdl:operation>\n");
		sb.append("\t\t</wsdl:binding>\n");
		sb.append("\t\t<wsdl:service name=\"");
		sb.append(options.getServiceName());
		sb.append("\">\n");
		sb.append("\t\t\t<wsdl:port name=\"");
		sb.append(options.getServiceName());
		sb.append("Soap\" binding=\"tns:");
		sb.append(options.getEndpointName());
		sb.append("SoapBinding\">\n");
		sb.append("\t\t\t<soap:address location=\"");
		sb.append(WSHelper.getServerBaseAddress());
		sb.append(ConfigServer.WS_GENERIC_PATH);
		sb.append("\" />\n");
		sb.append("\t\t</wsdl:port>\n");
		sb.append("\t\t</wsdl:service>\n");
		sb.append("\t</wsdl:definitions>\n");
		
		this.cachedContract = sb.toString();
	}

}
