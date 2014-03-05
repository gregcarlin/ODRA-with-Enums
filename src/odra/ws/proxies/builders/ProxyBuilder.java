package odra.ws.proxies.builders;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.ws.bindings.IBindingProvider;
import odra.ws.bindings.IBindingsAware;

/**
 * Common functionalities and schema for all Web Service proxy stub builders
 *
 * @since 2007-06-20
 * @version 2008-01-28
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public abstract class ProxyBuilder implements IBindingsAware {
	protected List<IBindingProvider> bindingProviders = new ArrayList<IBindingProvider>();

	/**
	 * Builds (if necessary) proxy stub and creates set of associated metadada.
	 * Supports promotion only proxy creation scenario. It may be used to reuse already existing piece of supported stub type
	 * by adding remote invocation entries without new proxy object generation.
	 *
	 * @param oid The object to be build
	 * @param wsdlLocation Address of Web Service contract
	 * @throws ProxyBuilderException
	 */
	public abstract void build(OID oid, URL wsdlLocation, boolean promotionOnly) throws ProxyBuilderException;



	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingsAware#addBinding(odra.bridges.wsdl.IBindingProvider)
	 */
	public void addBinding(IBindingProvider provider) {
		this.bindingProviders.add(provider);
	}

	/**
	 * Helper method for checking against known WSDL namespaces
	 * @param ns Namespace to check
	 * @return
	 */
	protected boolean isWsdlNamespace(String ns) {
		ns = ns.trim();
		if ("http://schemas.xmlsoap.org/wsdl/".equals(ns)) {
			return true;
		}
		if ("http://schemas.xmlsoap.org/wsdl/soap/".equals(ns)) {
			return true;
		}
		if ("http://www.w3.org/2001/XMLSchema".equals(ns)) {
			return true;
		}
		if ("http://schemas.xmlsoap.org/soap/encoding/".equals(ns)) {
			return true;
		}
		if ("http://schemas.xmlsoap.org/wsdl/mime/".equals(ns)) {
			return true;
		}
		if ("http://microsoft.com/wsdl/mime/textMatching/".equals(ns)) {
			return true;
		}

		return false;
	}


}
