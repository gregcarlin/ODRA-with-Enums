package odra.ws.proxies;

import java.io.IOException;
import java.io.InputStream;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProxy;
import odra.db.objects.meta.MBClass;
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
import odra.ws.proxies.builders.ClassProxyBuilder;
import odra.ws.proxies.builders.ProxyBuilder;
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
 * @since 2008-01-26
 * @version 2008-01-26
 * @author Marcin Daczkowski <merdacz@gmail.com>
 *
 */
public class WSClassProxy extends WSProxy {

	private DBModule module;
	private DBClass clas;
	private MBClass mbClass;


	public WSClassProxy(OID oid) throws WSProxyException{
		super(oid);
		try
		{
			DBProxy dbProxy = new DBProxy(oid);

			if (ConfigDebug.ASSERTS) {
				assert dbProxy.isValid();
			}

			OID clas = dbProxy.getProxiedObject();
			OID container = clas.getParent().getParent();
			DBClass clasObject = new DBClass(clas);
			DBModule mod = new DBModule(container);

			if (ConfigDebug.ASSERTS) {
				assert mod.isValid();
				assert clasObject.isValid();
			}

			OID mbClass = mod.findFirstByName(clasObject.getName(), mod.getMetabaseEntry());
			MBClass mbClasObject = new MBClass(mbClass);


			this.module = mod;
			this.clas = clasObject;
			this.mbClass = mbClasObject;



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

		OID procOid = this.module.findFirstByName(procName, this.mbClass.getMethodsEntry());

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
		ClassProxyBuilder builder = new ClassProxyBuilder();
		return builder;
	}

}
