package odra.ws.proxies.builders;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleOrganizer;
import odra.system.config.ConfigDebug;
import odra.ws.bindings.BindingInfo;
import odra.ws.bindings.IBindingProvider;
import odra.ws.common.Pair;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;
import odra.ws.facade.WSProxyException;
import odra.ws.proxies.OperationInfo;
import odra.ws.type.mappers.ITypeMapper;

import org.apache.commons.lang.NotImplementedException;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

/**
 * Creates procedures and operations meta information for given WSDL contract
 *
 * @since 2007-03-26
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class ModuleProxyBuilder extends ProxyBuilder{

	/* (non-Javadoc)
	 * @see odra.ws.builders.ProxyBuilder#build(odra.db.objects.data.DBModule, java.net.URL)
	 */
	@Override
	public void build(OID oid, URL wsdlLocation, boolean promotionOnly) throws ProxyBuilderException {
		if (promotionOnly) { throw new RuntimeException("Module stub promotion not supported. "); }
		try {
			DBModule module = new DBModule(oid);
			IProxyFacade proxyManager = WSManagersFactory.createProxyManager();
			if (proxyManager == null) {
				throw new ProxyBuilderException("Cannot obtain proxy manager. ");

			}

			ModuleOrganizer org = new ModuleOrganizer(module, true);

			javax.wsdl.factory.WSDLFactory factory = javax.wsdl.factory.WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();
			reader.setFeature("javax.wsdl.importDocuments", true);
			Definition def = reader.readWSDL(wsdlLocation.toExternalForm());

			// substract types
			XSSchemaSet xss = null;

			try {
				XSOMParser r = new XSOMParser();
				r.setErrorHandler(new DefaultHandler());
				r.setAnnotationParser(new DomAnnotationParserFactory());

				List<ExtensibilityElement> list = def.getTypes().getExtensibilityElements();
				for (ExtensibilityElement l : list) {
					if (l instanceof Schema) {
						Schema s = (Schema) l;

						Element root = s.getElement();
						for (Object o : def.getNamespaces().keySet()) {
							if (o instanceof String) {
								String prefix = (String) o;
								String ns = def.getNamespace(prefix);
								if (!this.isWsdlNamespace(ns)) {
									String attrName = "xmlns:"+prefix;
									if (!root.hasAttribute(attrName)) {
										root.setAttribute(attrName, ns);
									}
								}
							}
						}

						StringWriter writer = new StringWriter();

						XMLSerializer xs = new XMLSerializer();
						xs.setNamespaces(true);

						xs.setOutputCharStream(writer);
						xs.serialize(root);

						r.parse(new StringReader(writer.toString()));

					}
				}

				xss = r.getResult();
			} catch( SAXException e ) {
				throw new ProxyBuilderException("Error while interpreting XSD. ", e);

			}

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

						List<QName> rootElements = new LinkedList<QName>();
						List<QName> rootTypes = new LinkedList<QName>();

						List<Operation> ops = port.getBinding().getPortType().getOperations();

						// import necessary types
						for (Operation op: ops) {

							if (op.isUndefined()) { continue; }

							BindingOperation bindOp = port.getBinding().getBindingOperation(op.getName(), null, null);
							BindingInfo bindingInfo = chosenBinding.getBindingInfo(bindOp);

							if (bindingInfo.isValid()) {

								Part inPart = ((Map<String, Part>)op.getInput().getMessage().getParts()).entrySet().iterator().next().getValue();
								Part outPart = ((Map<String, Part>)op.getOutput().getMessage().getParts()).entrySet().iterator().next().getValue();
								Part[] parts = new Part[] {inPart, outPart};
								for (Part pt : parts) {
									if (pt.getTypeName() != null) {
										rootTypes.add(pt.getTypeName());

									} else if (pt.getElementName() != null) {
										rootElements.add(pt.getElementName());
									}
								}


							} else {
								throw new ProxyBuilderException("Error receiving specific binding info for "  + chosenBinding.getBindingType().getName());

							}

						}

						// import types
						typeMapper.mapXSDToModel(xss, rootTypes, rootElements, module);


						// link module after changes
						module.setModuleCompiled(true);
						module.setModuleLinked(false);
						BuilderUtils.getModuleLinker().linkModule(module);

						// import procedures
						for (Operation op : ops) {

							if (op.isUndefined()) { continue; }

							BindingOperation bindOp = port.getBinding().getBindingOperation(op.getName(), null, null);
							BindingInfo bindingInfo = chosenBinding.getBindingInfo(bindOp);

							if (bindingInfo.isValid()) {

								ProcArgument[] params = chosenBinding.getParameters(module,op);
								OdraTypeSchema result = chosenBinding.getResultType(op);

								OdraProcedureSchema sprocInfo = new OdraProcedureSchema(
										op.getName(),
										params,
										new ProcedureAST(BuilderUtils.serializeAST(new ReturnWithoutValueStatement())),
										result
								);

								org.createProcedure(sprocInfo);

								// get newly created procedure
								OID procOID = module.findFirstByName(op.getName(), module.getDatabaseEntry());
								DBProcedure dbProc = new DBProcedure(procOID);

								if (ConfigDebug.ASSERTS) {
									assert dbProc.isValid();
								}

								OperationInfo opInfo = new OperationInfo();
								opInfo.setBindingInfo(bindingInfo);

								String requestElementName = op.getInput().getName();
								if (requestElementName == null) {
									requestElementName =  op.getName();
								}
								opInfo.setName(requestElementName);
								operations.add(new Pair<DBProcedure, OperationInfo>(dbProc, opInfo));

							} else {
								throw new ProxyBuilderException("Error receiving specific binding info for "  + chosenBinding.getBindingType().getName());

							}
						}

						// link module after changes
						BuilderUtils.getModuleLinker().linkModule(module);

						// create persitent proxy meta information
						OID dbProxyOid = proxyManager.createProxy(module.getOID(), wsdlLocation, serviceAddress, namespace, operations, chosenBinding.getBindingType());

						break;

					}
				}

			} else {
				throw new ProxyBuilderException("Proxy implementation does not support multi service based contracts. ");

			}


		} catch (WSDLException ex) {
			throw new ProxyBuilderException("", ex);

		} catch (WSProxyException ex) {
			throw new ProxyBuilderException("", ex);

		} catch (DatabaseException ex) {
			throw new ProxyBuilderException("", ex);

		} catch (Exception ex) {
			throw new ProxyBuilderException("", ex);

		}
	}


}
