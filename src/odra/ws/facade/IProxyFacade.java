package odra.ws.facade;

import java.net.URL;
import java.util.List;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.meta.MBClass;
import odra.sbql.results.runtime.Result;
import odra.sbql.stack.SBQLStack;
import odra.ws.common.Pair;
import odra.ws.proxies.OperationInfo;
import odra.ws.proxies.builders.ProxyBuilderException;
/** Interface (facade) to all proxy operations.
 * Implementators may be plugged in/out depending on the current need.
 * @version 2007-06-24
 * @since 2006-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public interface IProxyFacade  {


	/** Initializes the facade
	 * @throws DatabaseException
	 */
	void initialize() throws DatabaseException;

	/**
	 * Builds proxies specific system schema on database creation
	 * @param module
	 * @throws DatabaseException
	 */
	void createMetadata(DBModule module) throws DatabaseException;


	/**
	 * Determines whether whether supplied object is a proxied one
	 * @param oid reference to candidate for a check
	 * @return true if given object is proxy; false otherwise
	 * @throws WSProxyException
	 */
	boolean isProxy(OID oid) throws WSProxyException;


	/**
	 * Creates proxy instance
	 * @param oid Reference to database object to be exposed
	 * @param wsdlLocation Location of WSDL contract
	 * @param serviceAddress Address of target service
	 * @param namespace Namespace of target service
	 * @param operations List of operations to be exposed
	 * @param bindingType Type of service binding to use
	 * @return Proxy entry identifier
	 * @throws DatabaseException
	 * @throws WSProxyException
	 */
	OID createProxy(OID oid, URL wsdlLocation, URL serviceAddress, String namespace, List<Pair<DBProcedure, OperationInfo>> operations, WSBindingType bindingType) throws DatabaseException, WSProxyException;

	/**
	 * Removes proxy instance
	 * @param reference to proxy object to be removed
	 * @throws DatabaseException
	 */
	void removeProxy(OID oid) throws DatabaseException;

	/**
	 * Builds proxy basing on Web Service contract inside containing module.
	 *
	 * @param type Type of proxy to be built
	 * @param oid Object to be built up
	 * @param wsdlLocation Web Service contract
	 *
	 * @throws ProxyBuilderException
	 */
	void buildStub(WSProxyType type, OID oid, URL wsdlLocation) throws ProxyBuilderException;

	/**
	 * Promotes existing object to constitute proper proxy
	 *
	 * @param type Type of proxy to be built
	 * @param oid Object to be promoted
	 * @param wsdlLocation Web Service contract
	 *
	 * @throws ProxyBuilderException
	 * @throws DatabaseException
	 */
	void promoteStub(WSProxyType type, OID oid, URL wsdlLocation) throws ProxyBuilderException, DatabaseException;

	/**
	 * Calls remote procedure
	 *
	 * @param proc local procedure object
	 * @param stack current stack state
	 * @return query result
	 * @throws WSProxyException
	 */
	Result remoteProxyCall(OID proc, SBQLStack stack) throws WSProxyException;

}
