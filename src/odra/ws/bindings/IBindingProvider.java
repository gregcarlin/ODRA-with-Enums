package odra.ws.bindings;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;

import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.ProcArgument;
import odra.ws.bindings.soap.BindingProviderException;
import odra.ws.facade.WSBindingType;
import odra.ws.proxies.OperationInfo;
import odra.ws.proxies.WSProxyOptions;
import odra.ws.type.mappers.ITypeMapper;

import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.sun.net.httpserver.Headers;

/** Abstracts binding  extact implementation
 * 
 * @since 2006-12-17
 * @version  2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public interface IBindingProvider {
		
	/** Creates WSDL contract binding section
	 * @param def Context contract defintion
	 * @param portType Context contract port type
	 * @param ops Operations to include in the section
	 * @param targetNamespace 
	 * @param serviceName
	 * @return contract fragment
	 * @throws BindingProviderException
	 * @throws WSDLException
	 */
	Binding createBinding(Definition def, PortType portType, Hashtable<MBProcedure, Operation> ops, String targetNamespace, String serviceName) throws BindingProviderException, WSDLException;
		
	/** Get symbolic representation of binding type
	 * @return binding type
	 */
	WSBindingType getBindingType();
	
	/** Determines, whether exact binding implementation can process given request
	 * @param httpHeaders
	 * @param request 
	 * @return
	 * @throws BindingProviderException
	 */
	boolean isCorrect(Headers httpHeaders, InputStream request) throws BindingProviderException;

	/** Extracts payload from given input message
	 * @param input message
	 * @return payload
	 * @throws BindingProviderException
	 */
	Element extractPayload(InputStream input) throws BindingProviderException;
	
	/** Extracts transport layer header parameters, which are important fot exact binding
	 * @param httpHeaders
	 * @return dictionary of desired parameters
	 * @throws BindingProviderException
	 */
	Hashtable<String, Object> extractParameters(Headers httpHeaders) throws BindingProviderException;
	
	/** Gives requestor the context of output message.
	 * @return
	 * @throws BindingProviderException
	 */	
	Document createContext() throws BindingProviderException;
	
	/** Wrapps output with binding specific context message header/footer
	 * @param output Payload to wrap
	 * @return
	 * @throws BindingProviderException
	 */	
	ByteArrayOutputStream wrapOutput(DocumentFragment output) throws BindingProviderException;
	
	/** Creates binding specific error reply 
	 * @param code error code
	 * @param message associated human-readable message
	 * @return error message
	 * @throws BindingProviderException
	 */
	ByteArrayOutputStream generateFault(String code, String message) throws BindingProviderException;


	/** Gets transport layer address from WSDL contract for exact binding
	 * @param port
	 * @return
	 */
	String getAddress(Port port);


	/** Gets binding specific service scope parameters 
	 * @return
	 */
	BindingInfo getBindingInfo();
	
	/** Gets binding specific operation scope parameters 
	 * @param operation
	 * @return
	 */
	BindingInfo getBindingInfo(BindingOperation operation);
	

	/** Extracts procedure parameters from operation definition
	 * @param module
	 * @param operation
	 * @return procedure parameters
	 */
	ProcArgument[] getParameters(DBModule module, Operation operation);
	
	/** Extracts procedure result from operation defintion
	 * @param operation
	 * @return procedure result
	 */
	OdraTypeSchema getResultType(Operation operation);
	

	/** Wraps proxy requesy with binding specific message context
	 * @param request Payload to wrap
	 * @param info Operation scope options
	 * @param options Service scope options
	 * @return
	 * @throws BindingProviderException
	 */
	PostMethod wrapRequest(DocumentFragment request, OperationInfo info, WSProxyOptions options) throws BindingProviderException;

	/** Gets associated type mapper implementation.
	 * @return type mapper
	 */
	ITypeMapper getTypeMapper();
	
}
