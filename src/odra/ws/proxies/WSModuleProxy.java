package odra.ws.proxies;

import java.io.IOException;
import java.io.InputStream;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProxy;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.Result;
import odra.system.config.ConfigDebug;
import odra.ws.bindings.BindingFactory;
import odra.ws.bindings.IBindingProvider;
import odra.ws.bindings.soap.BindingProviderException;
import odra.ws.common.SBQLHelper;
import odra.ws.facade.WSProxyException;
import odra.ws.proxies.builders.ModuleProxyBuilder;
import odra.ws.proxies.builders.ProxyBuilder;
import odra.ws.type.mappers.TypeMapperException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
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
/**
 * @author merdacz
 *
 */
public class WSModuleProxy extends WSProxy {

	private DBModule module;

	public WSModuleProxy(OID oid) throws WSProxyException{
		super(oid);
		try
		{
			DBProxy dbProxy = new DBProxy(oid);

			if (ConfigDebug.ASSERTS) {
				assert dbProxy.isValid();
			}
			OID proxyModule = dbProxy.getProxiedObject();
			DBModule mod = new DBModule(proxyModule);

			if (ConfigDebug.ASSERTS) {
				assert mod.isValid();
			}

			this.module = mod;


		} catch (Exception ex) {
			throw new WSProxyException("Error when creating web service proxy ", ex);
		}


	}


	/* (non-Javadoc)
	 * @see odra.ws.proxies.WSProxy#call(java.lang.String, odra.sbql.results.AbstractQueryResult[])
	 */
	public Result call(String procName, AbstractQueryResult[] params) throws WSProxyException, DatabaseException
	{
		OperationInfo info = this.options.getOperationInfo(procName);
		if ( info == null ) {
			throw new WSProxyException("Unable to call " + procName + " remotely because it is missing required metadata info. ");
		}

		OID procOid = this.module.findFirstByName(procName, this.module.getMetabaseEntry());

		if (procOid == null) {
			throw new WSProxyException("Not found. ");
		}
		MBProcedure mbProc = new MBProcedure(procOid);
		if (ConfigDebug.ASSERTS) {
			assert mbProc.isValid();
		}

		String sbqlResult = this.callInternal(mbProc, info, params);
		if (sbqlResult == null) {
			return new BagResult();
		}

		try {
    		Result result = new SBQLHelper().execSBQL(sbqlResult, this.module);
    		return result;
    	} catch (Exception ex) {
    		throw new WSProxyException("Internal web service proxy error.");
    	}

	}

	/** Gets associated builder type instance
	 * @return Builder for this kind of proxy
	 */
	public static ProxyBuilder getBuilder() {
		ModuleProxyBuilder builder = new ModuleProxyBuilder();
		return builder;
	}

}
