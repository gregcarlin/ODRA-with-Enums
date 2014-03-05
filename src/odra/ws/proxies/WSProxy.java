package odra.ws.proxies;

import java.io.IOException;
import java.io.InputStream;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProxy;
import odra.db.objects.data.DataObjectKind;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.Result;
import odra.system.config.ConfigDebug;
import odra.ws.bindings.BindingFactory;
import odra.ws.bindings.IBindingProvider;
import odra.ws.bindings.soap.BindingProviderException;
import odra.ws.common.SBQLHelper;
import odra.ws.facade.WSProxyException;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.ITypeMapperAware;
import odra.ws.type.mappers.TypeMapperException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.NullArgumentException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Web service proxy.
 *
 * @since 2006-11-29
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public abstract class WSProxy  implements ITypeMapperAware {

	protected ITypeMapper typeMapper;

	private HttpClient client;
	protected WSProxyOptions options;
	protected IBindingProvider binding;


	public WSProxy(OID oid) throws WSProxyException {
		try {
			DBProxy dbProxy = new DBProxy(oid);

			if (ConfigDebug.ASSERTS) {
				assert dbProxy.isValid();
			}

			this.options = new WSProxyOptions();
			this.options.load(dbProxy);

			this.binding = BindingFactory.createProvider(this.options.getBindingType());

			this.client = new HttpClient();

		} catch (Exception ex) {
			throw new WSProxyException("Error when creating web service proxy ", ex);
		}
	}

	/**
	 * Calls remote web service procedure
	 *
	 * @param procName name
	 * @param params parameters
	 * @return query result
	 *
	 * @throws WSProxyException
	 * @throws DatabaseException
	 */
	public abstract Result call(String procName, AbstractQueryResult[] params) throws WSProxyException, DatabaseException;

	/**
	 * Real processing of proxied call
	 * @param mbProc
	 * @param opInfo
	 * @param params
	 * @return
	 * @throws WSProxyException
	 * @throws DatabaseException
	 * @throws DOMException
	 */
	protected String callInternal(MBProcedure mbProc, OperationInfo opInfo, AbstractQueryResult[] params) throws WSProxyException, DatabaseException, DOMException {
		if (mbProc == null) { throw new NullArgumentException("mbProc"); }
		if (opInfo == null) { throw new NullArgumentException("opInfo"); }
		if (params == null) { throw new NullArgumentException("params"); }

		PostMethod post;

		try {
			Document context = this.binding.createContext();

			DocumentFragment fragment = context.createDocumentFragment();

			int i = params.length;
			for (OID p : mbProc.getArguments() )
			{
				i--;

				MBVariable var = new MBVariable(p);
				if (!var.isValid()) {
					throw new WSProxyException("Internal error. ");

				}
				String tagName = var.getName();
				NodeList nodes = this.typeMapper.mapOdraResultToXML(context, (Result) params[i], null, options.getNamespace());


				Element elem = context.createElementNS(this.options.getNamespace(), tagName);

				while (nodes.getLength() != 0) {
					elem.appendChild(nodes.item(0));
				}
				fragment.appendChild(elem);

			}

			// wrap in post (injectible)
			post = this.binding.wrapRequest(fragment, opInfo, this.options);


		} catch (BindingProviderException ex) {
			throw new WSProxyException("Error while wrapping request", ex);

		} catch (TypeMapperException ex) {
			throw new WSProxyException("Error while wrapping request", ex);

		}

		InputStream response = null;

		try {
			int statusCode = this.client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new WSProxyException("Remote method failed. ");

		    }

			response = post.getResponseBodyAsStream();

			// extract payload using proper binding
		    Element e = this.binding.extractPayload(response);

		    if (mbProc.isValid()) {
		    	NodeList nodes = e.getChildNodes();
		    	Element outElement = firstElement(nodes);


		    	OID type = mbProc.getType();
		    	OID outParam = null;

		    	// TODO this part should be consistent for module and class proxies and is subject to change in future
		    	OID parent = mbProc.getOID().getParent().getParent();
		    	MBObject procMetaContainer = new MBObject(parent);
		    	DBObject procDataContainer = new DBObject(parent);
		    	if (procMetaContainer.getObjectKind() == MetaObjectKind.CLASS_OBJECT) {

		    		outParam = mbProc.getType();


		    	} else {
		    		MBStruct strObj = new MBStruct(new MBTypeDef(type).getType());

			    	OID[] flds = strObj.getFields();

			    	// we assume there is at most one output parameter
			    	if (flds.length == 0) {
			    		// void output
			    		return null;
			    	}
			    	if (flds.length != 1) {
			    		throw new WSProxyException("źInternal proxy error. ");

			    	}
			    	outParam = flds[0];

		    	}

		    	//  we assume there is only one element node
		    	if (outElement == null) {
		    		throw new WSProxyException("Incorrect message received from web service");

		    	}

		    	// do real mapping

		    	if (outElement.hasChildNodes()) {
		    		if (procMetaContainer.getObjectKind() == MetaObjectKind.CLASS_OBJECT) {
		    			return this.typeMapper.mapXMLToOdra(outParam, outElement);	
		    		} else {
		    			StringBuilder sb = new StringBuilder();
				    	sb.append("(");
				    	sb.append(this.typeMapper.mapXMLToOdra(outParam, outElement));
				    	sb.append(") as ");
				    	sb.append(outElement.getLocalName());
				    	sb.append(";");
				    	return sb.toString();
		    			
		    		}
		    	} else {
		    		return null;

		    	}


		    } else {
		    	throw new WSProxyException("Internal web service proxy error.");
		    }


		} catch (HttpException ex) {
			throw new WSProxyException("Transport error occured when calling " + mbProc.getName() + ". ", ex);

	    } catch (IOException ex) {
	    	throw new WSProxyException("Connection error while calling remote procedure " + mbProc.getName() + ". ", ex);

	    } catch (BindingProviderException ex) {
	    	throw new WSProxyException("Extracting payload from response to remote call " + mbProc.getName() + " finished with error. ", ex);

	    } catch (TypeMapperException ex) {
	    	throw new WSProxyException("Type mapping error. ", ex);

	    } finally {
	    	// Release the connection.
	    	if (post != null) {
	    		post.releaseConnection();
	    	}

	    }

	}


	private Element firstElement(NodeList nodes) {
		Element result = null;
    	for (int i=0; i < nodes.getLength(); i++) {
    		if (nodes.item(i) instanceof Element) {
    			result = (Element) nodes.item(i);
    			break;
    		}
    	}
    	return result;
	}

	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapperAware#setMapper(odra.ws.type.mappers.ITypeMapper)
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


}
