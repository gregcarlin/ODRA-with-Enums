package odra.ws.proxies;

import java.net.URL;
import java.util.List;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBProxy;
import odra.db.objects.data.DataObjectKind;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.stack.SBQLStack;
import odra.system.Names;
import odra.ws.bindings.BindingsHelper;
import odra.ws.common.Pair;
import odra.ws.facade.Config;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSBindingType;
import odra.ws.facade.WSProxyException;
import odra.ws.facade.WSProxyType;
import odra.ws.proxies.builders.ProxyBuilder;
import odra.ws.proxies.builders.ProxyBuilderException;

import org.apache.commons.lang.NotImplementedException;



/**
 * Web service proxy manager.
 *
 * @since 2007-03-17
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSProxyManager implements IProxyFacade {

	private static final String INCORRECT_CONTAINER_ERROR_MESSAGE = "Unknown remote procedure containing type. Only class and module are allowed. ";
	private static WSProxyManager instance = null;
	private static OID proxies;
	private static DBModule rootModule;


	/* (non-Javadoc)
	 * @see odra.ws.facade.IProxyFacade#initialize()
	 */
	public void initialize() throws DatabaseException
	{
		rootModule = Database.getModuleByName(Database.WS_SCHEMA);
		proxies = rootModule.findFirstByNameId(Names.S_PROXIES_ID, rootModule.getDatabaseEntry());

		if (proxies == null) {
			throw new DatabaseException("No global entries for proxies initialized.");
		}
	}


	/* (non-Javadoc)
	 * @see odra.ws.facade.IProxyFacade#createMetadata(odra.db.objects.data.DBModule)
	 */
	public void createMetadata(DBModule mod) throws DatabaseException
	{
		OID oid = mod.createComplexObject(Names.namesstr[Names.S_PROXIES_ID], mod.getDatabaseEntry(), 0);
	}


	/* (non-Javadoc)
	 * @see odra.wsinterface.IProxyManager#isProxy(odra.db.objects.data.DBModule)
	 */
	public boolean isProxy(OID oid) throws WSProxyException
	{
		try {
			String name = this.computeProxyKey(oid);

			if (name == null) {
				return false;
			}

			// sanity check, because we need to relay on rootModule and it may not yet be initialized (on db startup)
			if (rootModule == null) {
				return false;
			}

			// do real check. it is not thread synchronized and cannot be (consider consuming service exposed by the same
			// ODRA instance)
			OID pid = rootModule.findFirstByName(name, proxies);
			return pid != null;


		} catch (DatabaseException ex) {
			throw new WSProxyException("Error checking if module is proxy", ex);

		}
	}




	/* (non-Javadoc)
	 * @see odra.wsinterface.IProxyManager#createProxy(odra.db.objects.data.DBModule, java.net.URL, java.net.URL, java.lang.String, java.util.List, odra.wsinterface.WSBinding)
	 */
	public OID createProxy(OID oid, URL wsdlLocation, URL serviceAddress, String namespace, List<Pair<DBProcedure, OperationInfo>> operations, WSBindingType bindingType) throws DatabaseException, WSProxyException {

		String name = this.computeProxyKey(oid);
		OID result;

		synchronized (proxies) {
			if ( rootModule.findFirstByName(name, proxies) != null){
				throw new DatabaseException("Object already is a Web Service proxy.");
			}
			result = rootModule.createComplexObject(name, proxies, DBProxy.FIELD_COUNT);
		}
		DBProxy proxy = new DBProxy(result);
		proxy.initialize(oid, wsdlLocation, serviceAddress, namespace, operations, bindingType);

		return result;
	}

	/* (non-Javadoc)
	 * @see odra.wsinterface.IProxyManager#removeProxy(odra.db.objects.data.DBModule)
	 */
	public void removeProxy(OID oid) throws DatabaseException {
		String name = this.computeProxyKey(oid);

		synchronized (proxies) {
			OID pid = rootModule.findFirstByName(name, proxies);

			if (pid != null) {
				pid.delete();
			} else {
				throw new DatabaseException("Proxy doesn't exist.");
			}
		}

	}

	/* (non-Javadoc)
	 * @see odra.ws.facade.IProxyFacade#buildStub(odra.ws.facade.WSProxyType, odra.db.objects.data.DBModule, java.net.URL, boolean)
	 */
	public void buildStub(WSProxyType type, OID oid, URL wsdlLocation) throws ProxyBuilderException {
		ProxyBuilder builder = getBuilderForProxy(type);
		BindingsHelper.injectBindings(builder, Config.WS_BINDINGS);
		builder.build(oid, wsdlLocation, false);
	}

	/* (non-Javadoc)
	 * @see odra.ws.facade.IProxyFacade#promoteStub(odra.ws.facade.WSProxyType, odra.db.objects.data.DBModule, odra.db.OID, java.net.URL)
	 */
	public void promoteStub(WSProxyType type, OID oid, URL wsdlLocation) throws ProxyBuilderException, DatabaseException {
		ProxyBuilder builder = getBuilderForProxy(type);
		BindingsHelper.injectBindings(builder, Config.WS_BINDINGS);
		builder.build(oid, wsdlLocation, true);
	}


	/* (non-Javadoc)
	 * @see odra.wsinterface.IProxyManager#remoteProxyCall(odra.db.objects.data.DBModule, odra.db.OID, odra.sbql.stack.SBQLStack)
	 */
	public Result remoteProxyCall(OID proc, SBQLStack stack) throws WSProxyException
	{
		try
		{
			String procName = new DBProcedure(proc).getName();

			OID container = proc.getParent().getParent();
			String proxyKey = this.computeProxyKey(container);

			// procedure parameters
			int paramNum = ((IntegerResult) stack.pop()).value;

			AbstractQueryResult params[] = new AbstractQueryResult[paramNum];
			for (int i = 0; i < paramNum; i++)
			{
				params[i] = stack.pop();
			}


			synchronized (proxies) {
				OID pid = rootModule.findFirstByName(proxyKey, proxies);
				DBProxy dbProxy= new DBProxy(pid);

				if (dbProxy.isValid()) {
					WSProxy proxy = WSProxyFactory.createProxy(dbProxy);
					Result result = proxy.call(procName, params);
					return result;

				} else {
					throw new WSProxyException("Proxy not found. ");

				}
			}


		}
		catch (DatabaseException ex)
		{
			throw new WSProxyException(ex.getMessage());
		}

	}

	private ProxyBuilder getBuilderForProxy(WSProxyType type) {
		ProxyBuilder result = null;
		switch (type) {
			case ModuleProxy:
				result = WSModuleProxy.getBuilder();
				break;
			case ClassProxy:
				result = WSClassProxy.getBuilder();
				break;
			default:
				throw new NotImplementedException();
		}

		return result;
	}


	private String computeProxyKey(OID oid) throws DatabaseException {
		DBObject object = new DBObject(oid);
		String result =  null;
		switch (object.getObjectKind().getKindAsInt()) {
			case DataObjectKind.MODULE_OBJECT:
				DBModule module = new DBModule(oid);
				result = module.getModuleGlobalName();
				break;
			case DataObjectKind.CLASS_OBJECT:
				DBClass clas = new DBClass(oid);
				DBModule parentModule = new DBModule(oid.getParent().getParent());
				result = String.format("%s.%s", parentModule.getModuleGlobalName(), clas.getName());
				break;
		}
		return result;
	}




}
