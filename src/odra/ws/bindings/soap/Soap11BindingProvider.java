package odra.ws.bindings.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.ProcArgument;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;
import odra.ws.bindings.BindingConstants;
import odra.ws.bindings.BindingInfo;
import odra.ws.bindings.IBindingProvider;
import odra.ws.endpoints.WSEndpointConstants;
import odra.ws.facade.WSBindingType;
import odra.ws.proxies.OperationInfo;
import odra.ws.proxies.WSProxyOptions;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.literal.LiteralTypeMapper;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.sun.net.httpserver.Headers;


/***
 * Binding provider implementation for SOAP protocol version 1.1
 *  
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-24
 * @since 2006-12-17
 *
 */
public class Soap11BindingProvider implements IBindingProvider, Soap11Constants, BindingConstants, WSEndpointConstants {
	private SOAPFactory soapFactory = null;
	private MessageFactory soap11messageFactory = null;
	private SOAPMessage responseSoapMsg = null;
	private ITypeMapper typeMapper = null;
	
	public Soap11BindingProvider() {
		this.typeMapper = new LiteralTypeMapper();
	}


	/** Initializes the instance.
	 * It will be used for processing mesage in the both directions. 
	 * @throws BindingProviderException
	 */
	public void initialize() throws BindingProviderException {
		try {
			if (this.soapFactory == null) {
				this.soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL); 
			}
			if (this.soap11messageFactory == null) {
				this.soap11messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL); 
			}
			if (this.responseSoapMsg == null) {
				this.responseSoapMsg = this.soap11messageFactory.createMessage();
			
				this.responseSoapMsg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
				this.responseSoapMsg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "utf-8");
	
			}
		} catch (SOAPException e) {
			LogRecord entry = new LogRecord(Level.SEVERE, "Initializing error ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Initializing error");
		}

	}

	
	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#createBinding(javax.wsdl.Definition, javax.wsdl.PortType, javax.wsdl.Operation, odra.db.objects.meta.MBProcedure, java.lang.String, java.lang.String)
	 */
	public Binding createBinding(Definition def, PortType portType, Hashtable<MBProcedure, Operation> ops, String targetNamespace, String serviceName) throws BindingProviderException, WSDLException {
		try
		{
			Binding bind = def.createBinding();
			ExtensionRegistry ext = def.getExtensionRegistry();
	
			bind.setQName(new QName(targetNamespace, serviceName + this.getBindingType().getName() + BINDING_SUFFIX));
			bind.setPortType(portType);
	
			SOAPBinding soapBinding = (SOAPBinding) ext.createExtension(Binding.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/","binding"));
			soapBinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
			bind.addExtensibilityElement(soapBinding);
	
			for (Entry<MBProcedure, Operation> entry : ops.entrySet()) {
				MBProcedure mbProc = entry.getKey();
				Operation op = entry.getValue();
				
				BindingOperation bindOp = def.createBindingOperation();
				bindOp.setName(mbProc.getName()); 
				SOAPOperation soapOperation = (SOAPOperation) ext.createExtension(
							BindingOperation.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation"));
				soapOperation.setStyle("document");
				soapOperation.setSoapActionURI(targetNamespace+"/"+mbProc.getName()); 
				bindOp.addExtensibilityElement(soapOperation);
				bindOp.setOperation(op);
		
				BindingInput bindingInput = def.createBindingInput();
		
				SOAPBody inputBody = (SOAPBody) ext.createExtension(BindingInput.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body"));
				inputBody.setUse("literal");
				bindingInput.addExtensibilityElement(inputBody);
				bindOp.setBindingInput(bindingInput);
				BindingOutput bindingOutput = def.createBindingOutput();
				bindingOutput.addExtensibilityElement(inputBody);
				bindOp.setBindingOutput(bindingOutput);
		
				bind.addBindingOperation(bindOp);
				bind.setUndefined(false);
			}
		
	
			
			return bind;
		}
		catch (DatabaseException e)
		{
			LogRecord entry = new LogRecord(Level.WARNING, "Error while creating Soap11 WSDL contract binding section. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Error while creating Soap11 WSDL contract binding section");
		}
		
	}

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#createContext(com.sun.net.httpserver.HttpExchange)
	 */
	public Document createContext() throws BindingProviderException {
		this.initialize();
		return this.responseSoapMsg.getSOAPPart();
	}

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#isCorrect(com.sun.net.httpserver.HttpExchange)
	 */
	public boolean isCorrect(Headers httpHeaders, InputStream request) throws BindingProviderException {		
		
		this.initialize();
		try {
			SOAPMessage msg = this.soap11messageFactory.createMessage(new MimeHeaders(), request);
			
		} catch (SOAPException ex) {
			return false;
			
		} catch (IOException ex) {
			return false;
		}
			
		if (httpHeaders.getFirst(SOAP_ACTION_KEY) == null) {
			return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#isCorrect(javax.xml.namespace.QName)
	 */
	public boolean isCorrect(QName qname) throws BindingProviderException {
		this.initialize();
		return qname.getNamespaceURI().equals(SOAP11_NS);
		
	}
	


	/* (non-Javadoc)
	 * @see odra.bridges.bindings.IBindingProvider#extractParameters(com.sun.net.httpserver.HttpExchange)
	 */
	public Hashtable<String, Object> extractParameters(Headers httpHeaders) throws BindingProviderException {
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		// extract soap action
		String soapActionValue = httpHeaders.getFirst(SOAP_ACTION_KEY);
		if (soapActionValue != null) {
					
			// extract from quotes (but if not quotes keep as is) - sound with WS-I BP 1.0 - Rxxxx
			if (soapActionValue.charAt(0) == '"' && soapActionValue.charAt(soapActionValue.length() - 1) == '"') {
				
				// quoted
				params.put(METHOD_TO_CALL, soapActionValue.substring(1, soapActionValue.length() - 1));
			
			} else {
				// non quoted
				params.put(METHOD_TO_CALL, soapActionValue);
				
			}
			
		
		} else {
			throw new BindingProviderException("SOAP Action header not present");
		}
		
		return params;
	}
	

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#substractPayload(com.sun.net.httpserver.HttpExchange)
	 */
	public Element extractPayload(InputStream input) throws BindingProviderException {
		try {
			SOAPMessage inputSoapMsg;
			inputSoapMsg = this.soap11messageFactory.createMessage(new MimeHeaders(), input);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			inputSoapMsg.writeTo(baos);
			ConfigServer.getLogWriter().getLogger().log(Level.FINEST, WSBindingType.SOAP11.getName() + " REQUEST: " + baos.toString());

			javax.xml.soap.SOAPBody body = inputSoapMsg.getSOAPBody();
			Iterator it = body.getChildElements();

			while (it.hasNext()) {
				Object tmp = it.next();
				if (tmp instanceof Element) {
					Element payload = (Element) tmp;
					return payload;
				} 
			}
			
			throw new BindingProviderException("Cannot parse SOAP part of incoming message. ");

		} catch (IOException e) {
			LogRecord entry = new LogRecord(Level.WARNING, "Payload substraction error. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Payload substraction error", e);

		} catch (SOAPException e) {
			LogRecord entry = new LogRecord(Level.WARNING, "Payload substraction error. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Payload substraction error", e);

		}
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.bindings.IBindingProvider#wrapOutput(org.w3c.dom.DocumentFragment)
	 */
	public ByteArrayOutputStream wrapOutput(DocumentFragment output) throws BindingProviderException {
		try {
			ByteArrayOutputStream wrappedOutput = new ByteArrayOutputStream();
			javax.xml.soap.SOAPBody soapBody = this.responseSoapMsg.getSOAPBody();

			soapBody.appendChild(output);

			if (this.responseSoapMsg.saveRequired()) {
				this.responseSoapMsg.saveChanges();
			}

			this.responseSoapMsg.writeTo(wrappedOutput);

			// logging
			ByteArrayOutputStream baos = new ByteArrayOutputStream();			
			wrappedOutput.writeTo(baos);
			ConfigServer.getLogWriter().getLogger().log(Level.FINEST, WSBindingType.SOAP11.getName() + " RESPONSE: " + baos.toString());
			
			return wrappedOutput;
			
		} catch (IOException e) {
			LogRecord entry = new LogRecord(Level.WARNING, "Output wrapping error. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Output wrapping error.");
			
		} catch (SOAPException e) {
			LogRecord entry = new LogRecord(Level.WARNING, "Output wrapping error. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
		
			throw new BindingProviderException("Output wrapping error.");

		}
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.bindings.IBindingProvider#wrapRequest(org.w3c.dom.DocumentFragment)
	 */
	public PostMethod wrapRequest(DocumentFragment fragment, OperationInfo info, WSProxyOptions options)  throws BindingProviderException  {
		try {
			
			Soap11BindingInfo soapInfo = (Soap11BindingInfo) info.getBindingInfo();
			String soapAction = soapInfo.getSoapAction();
			String soapNamespace = options.getNamespace(); 
			
			PostMethod post = new PostMethod(options.getServiceAddress().toExternalForm());
			post.setRequestHeader("SOAPAction", soapAction); 
			post.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
			
			ByteArrayOutputStream wrappedOutput = new ByteArrayOutputStream();
			javax.xml.soap.SOAPBody soapBody = this.responseSoapMsg.getSOAPBody();
	
			Element payload = this.responseSoapMsg.getSOAPPart().createElementNS(soapNamespace, info.getName());
			payload.appendChild(fragment);			
			soapBody.appendChild(payload);

			if (this.responseSoapMsg.saveRequired()) {
				this.responseSoapMsg.saveChanges();
				
			}

			this.responseSoapMsg.writeTo(wrappedOutput);
			

			// logging
			ByteArrayOutputStream baos = new ByteArrayOutputStream();			
			this.responseSoapMsg.writeTo(baos);

			ConfigServer.getLogWriter().getLogger().log(Level.FINEST, WSBindingType.SOAP11.getName() + " RESPONSE: " + baos.toString());
		
			RequestEntity requestEntity = new StringRequestEntity(wrappedOutput.toString());					
			post.setRequestEntity(requestEntity);
			
			return post;
			
		} catch (SOAPException ex) {
			throw new BindingProviderException("Error while creating SOAP11 request. ", ex); 
				
		} catch (IOException ex) {
			throw new BindingProviderException("Error while creating SOAP11 request. ", ex); 
			
		}
	}


	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#generateOfflineFault()
	 */
	public ByteArrayOutputStream generateFault(String code, String message) throws BindingProviderException {
		try {
			ByteArrayOutputStream wrappedOutput = new ByteArrayOutputStream();
			javax.xml.soap.SOAPBody soapBody = this.responseSoapMsg.getSOAPBody();
			SOAPFault fault = soapBody.addFault();
			fault.setFaultCode(new QName(SOAP11_FAULT_NS, code));
			fault.setFaultString(message);
			//fault.setFaultActor(actor);			
			
			if (this.responseSoapMsg.saveRequired()) {
				this.responseSoapMsg.saveChanges();
			}

			this.responseSoapMsg.writeTo(wrappedOutput);

			// logging
			ByteArrayOutputStream baos = new ByteArrayOutputStream();			
			wrappedOutput.writeTo(baos);
			ConfigServer.getLogWriter().getLogger().log(Level.FINEST, WSBindingType.SOAP11.getName() + " FAULT RESPONSE: " + baos.toString());
			
			return wrappedOutput;
			
		} catch (IOException e) {
			LogRecord entry = new LogRecord(Level.WARNING, "Fault generating error. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Fault generating error. ");
			
		} catch (SOAPException e) {
			LogRecord entry = new LogRecord(Level.WARNING, "Fault generating error. ");
			entry.setThrown(e);
			entry.setSourceClassName(this.getClass().getName());
			ConfigServer.getLogWriter().getLogger().log(entry);
			
			throw new BindingProviderException("Fault generating error. ", e);

		}
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#tryGetAddress(javax.wsdl.Port)
	 */
	public String getAddress(Port port) {
		List<ExtensibilityElement> portExts = port.getExtensibilityElements();
		
		for (ExtensibilityElement e : portExts) {
			if (e instanceof SOAPAddress) {
				SOAPAddress address = (SOAPAddress) e;
				return address.getLocationURI();
			}	
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.bridges.bindings.IBindingProvider#getBindingInfo()
	 */
	public BindingInfo getBindingInfo() {
		return new Soap11BindingInfo();
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#tryGetAction(javax.wsdl.BindingOperation)
	 */
	public BindingInfo getBindingInfo(BindingOperation operation) {
		
		Soap11BindingInfo info = new Soap11BindingInfo();
		
		List<ExtensibilityElement> opExts = operation.getExtensibilityElements();
		
		for (ExtensibilityElement e : opExts) {
			if (e instanceof SOAPOperation) {
				SOAPOperation op = (SOAPOperation) e;
				info.setSoapAction(op.getSoapActionURI());
			}
		}
		
		return info;
	}

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#getParameters(javax.wsdl.Operation)
	 */
	public ProcArgument[] getParameters(DBModule module, Operation operation)  {
		Input input = operation.getInput();
		Message message = input.getMessage();
		Map<String, Part> parts = message.getParts();
		
		
		if (parts.size() != 1) {			
			throw new RuntimeException("Not conformant to WS-I BP 1.1");
			
		}
		
		Entry<String, Part> entry = parts.entrySet().iterator().next();
		String name = entry.getKey();
		Part part = entry.getValue();
		
		String typeName;
		
		// type can be referenced by element or by type
		if (part.getElementName() != null) {
			QName outputPartName = part.getElementName();
			typeName  = this.typeMapper.mapTypeDefName(outputPartName.getLocalPart()); 
			
			try {
				OID typeDefOID = module.findFirstByName(typeName, module.getMetabaseEntry());
				
				if (typeDefOID == null) {
					throw new BindingProviderException("Internal Binding error. ");
				}
				
				MBTypeDef typeDef = new MBTypeDef(typeDefOID);
				
				if (!typeDef.isValid()) {
					throw new BindingProviderException("Internal Binding error. ");
				}
				
				OID structOID = typeDef.getType();
				MBStruct struct = new MBStruct(structOID);
				
				OID[] fields = struct.getFields();
				ProcArgument[] args = new ProcArgument[fields.length];
				int i = 0;
				for (OID f : fields) {
					MBVariable var = new MBVariable(f);
					args[i] = new ProcArgument(var.getName(), var.getTypeName(), 1, 1, 0); 
					i++;
				}
				
					
				return args;
			} catch (BindingProviderException ex){
				return new ProcArgument[0]; // graceful fail-over
				
			} catch (DatabaseException ex) {
				return new ProcArgument[0]; // graceful fail-over
				
			}
			
			
		} else {
			QName origTypeName = part.getTypeName();
			if (origTypeName.getNamespaceURI().equals(ITypeMapper.XSD_NS)) {
				typeName = this.typeMapper.mapPrimitiveXMLType(origTypeName);
			} else { 
				typeName =  this.typeMapper.mapTypeDefName(origTypeName.getLocalPart());	
				
			}
			

			ProcArgument[] args = new ProcArgument[1];
			args[0] = new ProcArgument(name, typeName, 1, 1, 0); 
				
			return args;
			
		}	
		
	}

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#getResultType(javax.wsdl.Operation)
	 */
	public OdraTypeSchema getResultType(Operation operation) {

		Map<String, Part> parameters = operation.getOutput().getMessage().getParts();
		if (parameters.size() != 1) {
			//throw new Exception("Not supported by WS-I BP 1.0");
			return null; 
		} else {
			Entry<String,Part> entry = parameters.entrySet().iterator().next();
			Part ret = entry.getValue();
			String typeName = null;
			
			// type can be referenced by element or by type
			if (ret.getElementName() != null) {
				QName outputPartName = ret.getElementName();
				typeName  = this.typeMapper.mapTypeDefName(outputPartName.getLocalPart());
				
			} else {
				QName origTypeName = ret.getTypeName();
				if (origTypeName.getNamespaceURI().equals(ITypeMapper.XSD_NS)) {
					typeName = this.typeMapper.mapPrimitiveXMLType(origTypeName);
					
				} else { 
					typeName =  this.typeMapper.mapTypeDefName(origTypeName.getLocalPart());
					
				}
				
			}	
			OdraTypeSchema result = new OdraTypeSchema(typeName, 1, 1, 0); 
			
			return result;
		}
	}

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingProvider#getBindingName()
	 */
	public WSBindingType getBindingType() {		
		return WSBindingType.SOAP11;
		
	}


	/* (non-Javadoc)
	 * @see odra.ws.bindings.IBindingProvider#getTypeMapper()
	 */
	public ITypeMapper getTypeMapper() {
		return new LiteralTypeMapper();
	}

	
}

