package odra.ws.proxies.builders;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.lang.NotImplementedException;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.meta.MBProcedure;
import odra.ws.bindings.BindingInfo;
import odra.ws.bindings.IBindingProvider;
import odra.ws.common.Pair;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;
import odra.ws.facade.WSProxyException;
import odra.ws.proxies.OperationInfo;
import odra.ws.type.mappers.ITypeMapper;

/**
 * Creates operations meta information for given WSDL contract
 *
 * @since 2008-01-26
 * @version 2008-01-26
 * @author Marcin Daczkowski <merdacz@gmail.com>
 *
 */
public class ClassProxyBuilder extends ProxyBuilder{

	/* (non-Javadoc)
	 * @see odra.ws.builders.ProxyBuilder#build(odra.db.objects.data.DBModule, java.net.URL)
	 */
	@Override
	public void build(OID oid, URL wsdlLocation, boolean promotionOnly) throws ProxyBuilderException {
		if (!promotionOnly) { throw new RuntimeException("Class stub automatic build not supported. "); }

		try {
			DBClass clas = new DBClass(oid);

			IProxyFacade proxyManager = WSManagersFactory.createProxyManager();
			if (proxyManager == null) {
				throw new ProxyBuilderException("Cannot obtain proxy manager. ");

			}

			javax.wsdl.factory.WSDLFactory factory = javax.wsdl.factory.WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();
			reader.setFeature("javax.wsdl.importDocuments", true);
			Definition def = reader.readWSDL(wsdlLocation.toExternalForm());

			// persist meta info in db

			String namespace = null;
			URL serviceAddress = null;

			List<Pair<DBProcedure, OperationInfo>> operations = new ArrayList<Pair<DBProcedure, OperationInfo>>();

			// extract from contract
			Map<QName, Service> services = def.getServices();
			if (services.entrySet().size() == 1) {
				Entry<QName, Service> entry = services.entrySet().iterator().next();
				QName serviceName = entry.getKey();
				Service service = entry.getValue();

				namespace = serviceName.getNamespaceURI();

				Map<String, Port> ports = service.getPorts();
				for (Entry<String, Port> p : ports.entrySet()) {
					String portName = p.getKey();
					Port port = p.getValue();

					IBindingProvider chosenBinding = null;
					ITypeMapper typeMapper = null;

					for (IBindingProvider provider : this.bindingProviders) {
						String tmpAddress = provider.getAddress(port);

						if (tmpAddress != null) {
							serviceAddress = new URL(tmpAddress);
							chosenBinding = provider;
							typeMapper = provider.getTypeMapper();
							break;
						}

					}

					if (serviceAddress != null) {
						List<Operation> ops = port.getBinding().getPortType().getOperations();

						// cache methods for potential promotion
						OID[] methodsOids = clas.getMethodsEntry().derefComplex();
						HashMap<String, DBProcedure> methods = new HashMap<String, DBProcedure>();
						for (OID method : methodsOids) {
							DBProcedure methodObject = new DBProcedure(method);
							methods.put(methodObject.getName(), methodObject);
						}

						for (Operation op : ops) {

							if (op.isUndefined()) { continue; }

							BindingOperation bindOp = port.getBinding().getBindingOperation(op.getName(), null, null);
							BindingInfo bindingInfo = chosenBinding.getBindingInfo(bindOp);

							if (bindingInfo.isValid()) {
								OperationInfo opInfo = new OperationInfo();
								opInfo.setBindingInfo(bindingInfo);

								String requestElementName = op.getInput().getName();
								if (requestElementName == null) {
									requestElementName =  op.getName();
								}
								opInfo.setName(requestElementName);

								// find in class to be promoted
								DBProcedure dbProc = methods.get(requestElementName);
								// only matching procedures will be proxies
								// TODO consider more strict behavior (exception throwing)
								if (dbProc != null) {
									operations.add(new Pair<DBProcedure, OperationInfo>(dbProc, opInfo));
								}

							} else {
								throw new ProxyBuilderException("Error receiving specific binding info for "  + chosenBinding.getBindingType().getName());

							}
						}


						// create persitent proxy meta information
						OID dbProxyOid = proxyManager.createProxy(clas.getOID(), wsdlLocation, serviceAddress, namespace, operations, chosenBinding.getBindingType());

						break;

					}
				}

			} else {
				throw new ProxyBuilderException("Proxy implementation does not support multi service based contracts. ");

			}


		} catch (WSDLException ex) {
			throw new ProxyBuilderException(ex.getMessage(), ex);

		} catch (WSProxyException ex) {
			throw new ProxyBuilderException(ex.getMessage(), ex);

		} catch (DatabaseException ex) {
			throw new ProxyBuilderException(ex.getMessage(), ex);

		} catch (Exception ex) {
			throw new ProxyBuilderException("Internal error. ", ex);

		}
	}

}
