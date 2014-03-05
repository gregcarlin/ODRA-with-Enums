package odra.ws.endpoints;

import java.util.Hashtable;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.sbql.results.runtime.Result;
import odra.ws.endpoints.wsdl.GenericWSDLBuilder;
import odra.ws.endpoints.wsdl.WSDLBuilder;
import odra.ws.endpoints.wsdl.WSDLBuilderException;
import odra.ws.type.mappers.TypeMapperException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Endpoint for exposing generic sbql queries functionality through web service.
 * 
 * @since 2006-11-29
 * @version 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSGenericEndpoint extends WSEndpoint {
		
	private WSDLBuilder builder;
	
	public WSGenericEndpoint() {
		 this.builder = new GenericWSDLBuilder();
			
	}

	/* (non-Javadoc)
	 * @see odra.bridges.endpoints.ws.WSEndpoint#getContract()
	 */
	@Override 
	public String getContract() throws WSEndpointException {
		try {
			return this.builder.createDefinition(this.options);
		} catch (WSDLBuilderException ex) {
			throw new WSEndpointException("Error while building WSDL contract. ", ex);
		}
		
	}
	/* (non-Javadoc)
	 * @see odra.bridges.endpoints.ws.WSEndpoint#handleMessage(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	protected DocumentFragment handlePayload(Document context, Element payload, Hashtable<String, Object> map) throws WSEndpointException {	
		// do some sanity checking 
		
		if (payload.getChildNodes().getLength() != 2) {
			// 
			return null;
		}
		
		String sbql = null;
		String module = null;
		
		if (payload.getChildNodes().item(0).getLocalName().equals("sbql")) {
			sbql = payload.getChildNodes().item(0).getTextContent();
			
		} else {
			return null;
		}
		
		if (payload.getChildNodes().item(1).getLocalName().equals("module")) {
			module = payload.getChildNodes().item(1).getTextContent();
			
		} else {			
			return null;
		}
		
		// get module & execute requested code		
		DBModule mod = null;
		
		try {
			mod = Database.getModuleByName(module);
			
		} catch (DatabaseException ex) {
			throw new WSEndpointException("Requested module doesn't exist.");
		
		}
		
		Result result = null;
		
		try {
			result = this.sbqlHelper.execSBQL(sbql, mod);
			
		} catch (Exception ex ) {
			throw new WSEndpointException("Error while executing provided SBQL code.");
				
		}
		
		// create response 
		try {
			DocumentFragment fragment = context.createDocumentFragment();		
			NodeList nodes = this.typeMapper.mapOdraResultToXML(context, result, "result", options.getTargetNamespace());	
			Element responseResult = context.createElementNS(this.options.getTargetNamespace(), "ExecuteResponse");
		
			
			while (nodes.getLength() != 0) {
				responseResult.appendChild(nodes.item(0));
			}
			
			fragment.appendChild(responseResult); 
			
			return fragment;
		} catch (TypeMapperException ex) {
			throw new WSEndpointException(ex);
			
		}
		
		
	}

	/* (non-Javadoc)
	 * @see odra.bridges.endpoints.ws.WSEndpoint#getExposedMetaObject()
	 */
	@Override
	protected MBObject getExposedMetaObject() throws WSEndpointException {	
		throw new WSEndpointException("Get ExposedMetaObject is irrelevant for generic endpoint, but it was called (compare stacktrace)");		
	}

	
}
