package odra.ws.endpoints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import odra.db.DatabaseException;
import odra.db.objects.data.DBEndpoint;
import odra.db.objects.meta.MBObject;
import odra.system.config.ConfigServer;
import odra.ws.bindings.BindingFactory;
import odra.ws.bindings.IBindingProvider;
import odra.ws.bindings.soap.BindingProviderException;
import odra.ws.common.SBQLHelper;
import odra.ws.endpoints.wsdl.WSDLBuilder;
import odra.ws.endpoints.wsdl.WSDLBuilderException;
import odra.ws.endpoints.wsdl.WSDLBuilderFactory;
import odra.ws.endpoints.wsdl.WSDLBuilderFactoryException;
import odra.ws.facade.Config;
import odra.ws.facade.WSBindingType;
import odra.ws.transport.http.HttpConstants;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.ITypeMapperAware;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Base class for all types of endpoints (for different database objects).
 * 
 * @since 2007-02-25
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * 
 */
public abstract class WSEndpoint implements HttpHandler, HttpConstants, WSEndpointConstants, ITypeMapperAware {

	protected DBEndpoint dbEndpoint;
	// indicates whether endpoint is working correctly
	private boolean error = false;
	// html pages cache
	private String infoPageCache = null;
	private String errorPageCache = null;
	// options cache 
	protected WSEndpointOptions options = null;
	protected SBQLHelper sbqlHelper = new SBQLHelper();	
	protected ITypeMapper typeMapper = null;

	
	/* (non-Javadoc)
	 * @see odra.bridges.type.mappers.ITypeMapperAware#setMapper(odra.bridges.type.mappers.ITypeMapper)
	 */
	public void setMapper(ITypeMapper mapper) {
		this.typeMapper = mapper;
		
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapperAware#getMapper()
	 */
	public ITypeMapper getMapper() {
		return this.typeMapper;
		
	}
		
	/** Sets database object containing all service parameters
	 * @param dbEndpoint
	 */
	public void setDbEndpoint(DBEndpoint dbEndpoint) {
		this.dbEndpoint = dbEndpoint;
		this.infoPageCache = null;
		this.options = new WSEndpointOptions();
		try {
			this.options.load(dbEndpoint);

		} catch (DatabaseException ex) {
			this.error = true;

		}

		this.error = !this.options.isValid();

	}

	/** Gets service parameters persistent representation
	 * @return
	 */
	public DBEndpoint getDbEndpoint() {
		return this.dbEndpoint;
	}

	/** 
	 * @return
	 */
	public WSEndpointOptions getOptions() {
		return this.options;
	}

	/* (non-Javadoc)
	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
	 */
	public void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		int httpStatus = HTTP_OK;
		Exception thrown = null;
		
		if (method.equals(GET_METHOD)) {
			String query = exchange.getRequestURI().getQuery();
			if (this.isMetaDataRequest(query)) {
				exchange.getResponseHeaders().set("Content-Type", "text/xml");
							
				try {
					String contract;
					contract = this.getContract();					
					response.write(this.getContract().getBytes());
				
				} catch (WSEndpointException ex) {
					httpStatus = HTTP_ERROR;
					thrown = ex;
					
				}
				
				
			} else {
				// display user friendly information about endpoint
				exchange.getResponseHeaders().set("Content-Type", "text/html");
				String page;
				if (!this.error) {
					page = this.getInfoPage();
				} else {
					page = this.getErrorInfoPage();
				}

				response.write(page.getBytes());
			}

		} else if (method.equals(POST_METHOD) || method.equals(HEAD_METHOD)
				|| method.equals(PUT_METHOD) || method.equals(DELETE_METHOD)) {

			exchange.getResponseHeaders().set(CONTENT_TYPE_HEADER, "text/xml");
			String lengthStr= exchange.getRequestHeaders().getFirst(CONTENT_LENGTH_HEADER);
			if (lengthStr == null) {
				throw new IOException("Transport protocol error. ");				
			}
			
			// cache input stream from the exchange
			// it is done because we need to able to read it many times
			
			int length = 0;
			try {
				length = Integer.parseInt(lengthStr);
			
			} catch (NumberFormatException ex) {
				throw new IOException("Transport protocol error. ", ex);
				
			}
			
			byte[] data = new byte[length];
			
			InputStream is = exchange.getRequestBody();
			
			//	Read in the bytes
	        int offset = 0;
	        int numRead = 0;
	        while ( offset < data.length
	                &&
	                (numRead=is.read(data, offset, data.length-offset)) >= 0 ) {

	            offset += numRead;

	        }

	        // Ensure all the bytes have been read in
	        if (offset < data.length) {
	            throw new IOException();
	        }
	        
	        // headers
	        Headers httpHeaders = exchange.getRequestHeaders();
	        
			for (WSBindingType b : Config.WS_BINDINGS) {
				IBindingProvider provider = BindingFactory
						.createProvider(b);
				try {
										
					if (provider.isCorrect(httpHeaders, new ByteArrayInputStream(data))) {

						Element payload = provider.extractPayload(new ByteArrayInputStream(data));
						Hashtable<String, Object> params = provider.extractParameters(httpHeaders);
						
						Document context = provider.createContext();
												
						if (this.options.getState() == EndpointState.STARTED && !this.error) {
							try {
								DocumentFragment output = this.handlePayload(context, payload, params);
								response = provider.wrapOutput(output);
							} catch (WSEndpointException ex) {
								response = provider.generateFault("Processing", ex.getMessage());
								
							} catch (BindingProviderException ex) {
								response = provider.generateFault("Processing", ex.getMessage());
								
							}
						} else if (this.options.getState() != EndpointState.STARTED){
							response = provider.generateFault("Offline", this.options.getServiceName() + " is currently offline. ");				
						} else {
							response = provider.generateFault("Server", "Internal server error");
						}

						break;
					}
				} catch (BindingProviderException e) {
					throw new IOException("Binding error occured while processing endpoint request. ", e);
				}
			}

		} else {
			throw new IOException("Unsupported HTTP method");
			
		}

		if ( thrown != null && httpStatus == HTTP_OK ) {
			httpStatus = HTTP_ERROR;
			if (ConfigServer.WS_WSDL_DETAILED_ERROR) {
				thrown.printStackTrace(new PrintWriter(response));
			}
			
		}
		
		exchange.sendResponseHeaders(httpStatus, response.size());
		OutputStream os = exchange.getResponseBody();
		response.writeTo(os);
		os.close();
	}

	
	/** Processes message payload in specific for exact endpoint way
	 * @param context
	 * @param payload
	 * @return soap answer (including fault message)
	 */
	protected abstract DocumentFragment handlePayload(Document context, Element payload, Hashtable<String, Object> params) throws WSEndpointException;
	
	/** Gets underlying database meta object
	 * @return
	 */
	protected abstract MBObject getExposedMetaObject() throws WSEndpointException;

	/** Creates contract 
	 * @return WSDL contract for endpoint
	 */
	protected String getContract() throws WSEndpointException {

		try {
			WSDLBuilder builder = WSDLBuilderFactory.create(this, this.getExposedMetaObject());			
		
			String wsdlDef = builder.createDefinition(this.getOptions());
			return wsdlDef;	
			
		} catch (WSDLBuilderFactoryException ex) {
			throw new WSEndpointException("Erorr while creating WSDL builder. ", ex);
			
		} catch (WSDLBuilderException ex) {
			throw new WSEndpointException("Erorr while creating contract. ", ex);
			
		}
	}
	
	private boolean isMetaDataRequest(String query) {
		return query != null
				&& (query.equalsIgnoreCase("WSDL") || query.startsWith("wsdl"));
	}

	private String getErrorInfoPage() {
		if (this.errorPageCache == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("<h1>");
			sb.append(this.options.getServiceName());
			sb.append(" Internal Error!</h1>");
			sb.append("</body></html>");
			this.errorPageCache = sb.toString();
		}
		return this.errorPageCache;
	}

	private String getInfoPage() {
		if (this.infoPageCache == null) {
			StringBuilder sb = new StringBuilder();
			String wsdlAddress = "?wsdl";
			sb.append("<html><body>");
			sb.append("<h1>");
			sb.append(this.options.getServiceName());
			sb.append("</h1>");
			sb.append("<p>Contract for this service can be found <a href=\"");
			sb.append(wsdlAddress);
			sb.append("\">here</a>.</p>");

			sb.append("<table>");
			sb.append("<tr><td>Service name</td><td>");
			sb.append(this.options.getServiceName());
			sb.append("<tr><td>Port name</td><td>");
			sb.append(this.options.getPortTypeName());
			sb.append("<tr><td>State</td><td>");
			sb.append(this.options.getState());
			sb.append("</td></tr>");
			sb.append("<tr><td>Namespace</td><td>");
			sb.append(this.options.getTargetNamespace());
			sb.append("</td></tr>");
			sb.append("</table>");

			sb.append("</body></html>");

			this.infoPageCache = sb.toString();
		}

		return this.infoPageCache;
	}

}
