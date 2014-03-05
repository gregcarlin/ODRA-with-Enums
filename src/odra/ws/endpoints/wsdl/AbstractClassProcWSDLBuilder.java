package odra.ws.endpoints.wsdl;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.ws.bindings.IBindingProvider;
import odra.ws.bindings.soap.BindingProviderException;
import odra.ws.common.SBQLHelper;
import odra.ws.endpoints.WSEndpointOptions;
import odra.ws.endpoints.WSHelper;
import odra.ws.endpoints.wsdl.typeresolver.FieldInfo;
import odra.ws.endpoints.wsdl.typeresolver.FieldKey;
import odra.ws.endpoints.wsdl.typeresolver.ITypeDefResolver;
import odra.ws.endpoints.wsdl.typeresolver.TypeDefResolver;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.wsdl.extensions.schema.SchemaConstants;

/**
 * Gathers all common parts for class and procedure WSDL builders
 *
 * @since 2007-06-22
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 * TODO consider refactoring createTypes to injectable types schema creator
 * TODO extract common method for handling in and out parameters types
 * TODO refactor names for handleStructType with extension
 */
public abstract class AbstractClassProcWSDLBuilder extends WSDLBuilder
implements WSDLConstants {

	private Hashtable<FieldKey, FieldInfo> fields = new Hashtable<FieldKey, FieldInfo>();
	private Queue<FieldInfo> queue = new LinkedList<FieldInfo>();

	private SBQLHelper sbqlHelper = new SBQLHelper();

	/**
	 * Creates WSDL contract definition skeleton
	 *
	 * @param options
	 * @param procList
	 * @return
	 * @throws WSDLBuilderException
	 */
	protected String createDefinition(WSEndpointOptions options,
			List<MBProcedure> procList) throws WSDLBuilderException {

		// prepare options
		String serviceName = options.getServiceName();
		String portTypeName = options.getPortTypeName();
		String portName = options.getPortName();
		String targetNamespace = options.getTargetNamespace();
		String relativePath = options.getRelativePath();

		// sanity checks
		boolean isServiceNameCorrect = options.getServiceName() != null
		&& !options.getServiceName().trim().equals("");
		boolean isPortTypeNameCorrect = options.getPortTypeName() != null
		&& !options.getPortTypeName().trim().equals("");
		boolean isPortNameCorrect = options.getPortName() != null
		&& !options.getPortName().trim().equals("");
		boolean isTargetNamespaceCorrect = options.getTargetNamespace() != null
		& !options.getTargetNamespace().trim().equals("");
		boolean isRelativePathCorrect = options.getRelativePath() != null
		& !options.getRelativePath().trim().equals("");

		if (!isServiceNameCorrect) {
			throw new WSDLBuilderException(
					String
					.format(
							"Error while building wsdl contract: incorrect service name = '%s'",
							options.getServiceName()));

		}

		if (!isPortTypeNameCorrect) {
			throw new WSDLBuilderException(
					String
					.format(
							"Error while building wsdl contract: incorrect port type name = '%s' for endpoint '%s'",
							options.getPortTypeName(), options
							.getServiceName()));
		}

		if (!isPortNameCorrect) {
			throw new WSDLBuilderException(
					String
					.format(
							"Error while building wsdl contract: incorrect port name = '%s' for endpoint '%s'",
							options.getPortName(), options
							.getServiceName()));
		}


		if (!isTargetNamespaceCorrect) {
			throw new WSDLBuilderException(
					String
					.format(
							"Error while building wsdl contract: incorrect target namespace for %s",
							options.getServiceName()));

		}
		if (!isRelativePathCorrect) {
			throw new WSDLBuilderException(
					String
					.format(
							"Error while building wsdl contract: incorrect relative path for %s",
							options.getServiceName()));
		}

		Document typesDocument;

		try {

			// wsdl definition creation
			WSDLFactory factory;

			factory = WSDLFactory.newInstance();
			Definition def = factory.newDefinition();

			def.setQName(new QName(targetNamespace, serviceName));
			def.setTargetNamespace(targetNamespace);
			def.addNamespace(TNS_NS_PREFIX, targetNamespace);
			def.addNamespace(XSD_NS_PREFIX, XSD_NS);
			def.addNamespace(SOAP_NS_PREFIX, SOAP_NS);

			def.setTypes(def.createTypes());
			typesDocument = this.initializeTypesSection(def, targetNamespace);

			// create port type including its operations
			PortType portType = def.createPortType();
			portType.setQName(new QName(targetNamespace, portTypeName));
			def.addPortType(portType);

			// create service section
			Service service = def.createService();
			service.setQName(new QName(serviceName));
			def.addService(service);

			this.createDefinitionSections(def, typesDocument, targetNamespace,
					relativePath, portName, portTypeName, serviceName, procList);

			ITypeDefResolver resolver = new TypeDefResolver();
			// FIXME think of better solution to get module
			if (procList.size() > 0)
			{
				try {
					DBModule module = procList.get(0).getModule();

					for (OID entry : module.getMetabaseEntry().derefComplex()) {
						MBObject mbObject = new MBObject(entry);
						switch (mbObject.getObjectKind()) {
							case LINK_OBJECT:
								throw new RuntimeException("foo");
							case TYPEDEF_OBJECT:
								MBTypeDef typeDef = new MBTypeDef(entry);
								resolver.addTypeDef(typeDef);
								break;
							case CLASS_OBJECT:
								MBClass clas = new MBClass(entry);
								resolver.addTypeDef(clas.getInstanceName(), clas.getType());
								break;
							default:

						}
					}
				} catch (DatabaseException ex) {
					throw new WSDLBuilderException("Error occured while feeding typedef resolver with data. ", ex);

				}

			}
			this.createTypes(resolver, typesDocument, targetNamespace);

			// serialize it to xml
			StringWriter sw = new StringWriter();

			factory.newWSDLWriter().writeWSDL(def, sw);

			return sw.toString();

		} catch (WSDLException e) {
			throw new WSDLBuilderException(
					"Error while building wsdl contract document", e);

		}

	}

	/**
	 * Creates contract sections skeleton
	 *
	 * @param def
	 * @param typesDocument
	 * @param targetNamespace
	 * @param relativePath
	 * @param portTypeName
	 * @param serviceName
	 * @param listProc
	 * @throws WSDLBuilderException
	 */
	protected void createDefinitionSections(Definition def,
			Document typesDocument, String targetNamespace,
			String relativePath, String portName, String portTypeName, String serviceName,
			List<MBProcedure> listProc) throws WSDLBuilderException {
		try {
			PortType portType = def.getPortType(new QName(targetNamespace,
					portTypeName));

			Hashtable<MBProcedure, Operation> ops = new Hashtable<MBProcedure, Operation>();

			for (MBProcedure mbProc : listProc) {

				// generic types for parameters
				this.createRequestAndResponseType(typesDocument, mbProc,
						targetNamespace);

				// create messages definitions section
				Message inputMsg = this.createInputMessage(mbProc,
						targetNamespace, def);
				Message outputMsg = this.createOutputMessage(mbProc,
						targetNamespace, def);

				def.addMessage(inputMsg);
				def.addMessage(outputMsg);

				Operation op = this.createOperation(mbProc, targetNamespace,
						def, inputMsg, outputMsg);

				portType.addOperation(op);
				portType.setUndefined(false);

				ops.put(mbProc, op);

			}

			// port defintion
			Port port = def.createPort();

			// create bindings section
			for (IBindingProvider provider : this.bindingProviders) {
				Binding binding = provider.createBinding(def, portType, ops,
						targetNamespace, serviceName);
				def.addBinding(binding);

				port.setName(portName
						+ provider.getBindingType().getName());

				port.setBinding(binding); // ref binding in port definition

			}

			// add port info to service section
			ExtensionRegistry ext = def.getExtensionRegistry();
			SOAPAddress soapAddress = (SOAPAddress) ext
			.createExtension(Port.class, new QName(
					"http://schemas.xmlsoap.org/wsdl/soap/", "address"));

			soapAddress.setLocationURI(WSHelper.getServerBaseAddress()
					+ relativePath);
			port.addExtensibilityElement(soapAddress);

			def.getService(new QName(serviceName)).addPort(port);

		} catch (WSDLException e) {
			throw new WSDLBuilderException(
					"Unknown error while creating WSDL contract document", e);

		} catch (BindingProviderException e) {
			throw new WSDLBuilderException(
					"Unknown error while creating WSDL contract document", e);

		} catch (DatabaseException e) {
			throw new WSDLBuilderException(
					"Unknown error while creating WSDL contract document", e);

		}

	}

	/**
	 * Creates types section
	 *
	 * @param doc
	 * @param targetNamespace
	 * @throws WSDLBuilderException
	 */
	protected void createTypes(ITypeDefResolver resolver, Document doc, String targetNamespace) throws WSDLBuilderException {
		try {
			Element root = doc.getDocumentElement();


			while (!queue.isEmpty()) {
				FieldInfo field = queue.poll();
				if (this.fields.containsKey(field.getKey())) {
					continue;
				} else {
					this.fields.put(field.getKey(), field);
				}

				MetaObjectKind kind = new MBObject(field.getType()).getObjectKind();

				// deal with references
				if (kind == MetaObjectKind.VARIABLE_OBJECT) {
					MBVariable variable = new MBVariable(field.getType());
					FieldInfo info = new FieldInfo(field);
					info.setType(variable.getType());
					queue.add(info);
					continue;
				}

				if ((field.getMaxCard() > 1) && ((kind == MetaObjectKind.STRUCT_OBJECT) ||
						(kind == MetaObjectKind.CLASS_OBJECT) || (kind == MetaObjectKind.PRIMITIVE_TYPE_OBJECT))) {
					String typeDefName = resolver.resolveType(field.getType());
					if (typeDefName == null)
					{
						switch (kind) {
							case PRIMITIVE_TYPE_OBJECT:
								typeDefName = new MBPrimitiveType(field.getType()).getName();
								break;
							case CLASS_OBJECT:
								typeDefName = new MBClass(field.getType()).getInstanceName();
								break;
							case STRUCT_OBJECT:
								// TODO
								throw new WSDLBuilderException("Anonymous structure must be packed with type definition. ");
							default:
								// TODO
								throw new WSDLBuilderException("Internal error. ");
						}
					}
					if (typeDefName != null) {
						// Create ArrayOfXXX definition
						Element arrayOfItemsElement = doc.createElement(XSD_NS_PREFIX+":complexType");
						root.appendChild(arrayOfItemsElement);

						MBObject object = new MBObject(field.getType());
						arrayOfItemsElement.setAttribute("name", String.format("ArrayOf%s", typeDefName));

						Element sequenceElement = doc.createElement(XSD_NS_PREFIX+":sequence");
						arrayOfItemsElement.appendChild(sequenceElement);

						Element arrayItemElement = doc.createElement(XSD_NS_PREFIX+":element");
						sequenceElement.appendChild(arrayItemElement);

						arrayItemElement.setAttribute("name", typeDefName);
						if (kind == MetaObjectKind.PRIMITIVE_TYPE_OBJECT) {
							arrayItemElement.setAttribute("type", XSD_NS_PREFIX+":"+typeDefName);
						} else {
							arrayItemElement.setAttribute("type", appendTargetNamespace(typeDefName));
						}
						arrayItemElement.setAttribute("minOccurs", new Integer(field.getMinCard()).toString());
						if (field.getMaxCard() == Integer.MAX_VALUE) {
							arrayItemElement.setAttribute("maxOccurs", "unbounded");

						} else {
							arrayItemElement.setAttribute("maxOccurs", new Integer(field.getMaxCard()).toString());

						}
						arrayItemElement.setAttribute("nillable", "true");
						FieldInfo info = new FieldInfo(field);
						info.setMinCard(1); // TODO check what is more appropriate here: 0 or 1
						info.setMaxCard(1);

						queue.add(info);
						continue;
					}

				}

				MBObject mbObject = new MBObject(field.getType());

				// TODO explicit case action for each object kind
				switch (mbObject.getObjectKind()) {
					case TYPEDEF_OBJECT:
						handleTypeDefType(resolver, queue, field);

						break;
					case PRIMITIVE_TYPE_OBJECT:
						// do nothing for primitive types
						break;
					case STRUCT_OBJECT:
						handleStructType(resolver, queue, field, doc, targetNamespace);
						break;
					case CLASS_OBJECT:
						handleClassType(resolver, queue, field, doc, targetNamespace);
						break;
					default:
				}


			}

		} catch (Exception e) {
			throw new WSDLBuilderException("Unknown error while creating WSDL contract document", e);
		}
	}

	private void handleClassType(ITypeDefResolver resolver,
			Queue<FieldInfo> queue, FieldInfo field, Document doc,
			String targetNamespace) throws DatabaseException, WSDLBuilderException {

		MBClass mbClass = new MBClass(field.getType());
		resolver.addTypeDef(mbClass.getInstanceName(), mbClass.getType());
		FieldInfo structField = new FieldInfo(mbClass.getName(), mbClass.getType(), 1, 1);

		if (mbClass.getDirectSuperClasses().length > 1) {
			throw new WSDLBuilderException("Exposure of classes with multiple superclass not supported. ");
		}
		if (mbClass.getDirectSuperClasses().length == 1) {
			OID oid = mbClass.getDirectSuperClasses()[0];
			MBClass mbSuperClass = new MBClass(oid);
			queue.add(new FieldInfo(mbSuperClass));
			handleStructType(resolver, queue, structField, doc, targetNamespace, mbSuperClass.getInstanceName());

		} else {
			handleStructType(resolver, queue, structField, doc, targetNamespace);

		}
	}

	private void handleStructType(ITypeDefResolver resolver, Queue<FieldInfo> queue,
			FieldInfo field, Document doc, String targetNamespace) throws DatabaseException {
		handleStructType(resolver, queue, field, doc, targetNamespace, null);
	}

	private void handleStructType(ITypeDefResolver resolver, Queue<FieldInfo> queue,
			FieldInfo field, Document doc, String targetNamespace, String superClassName) throws DatabaseException {
		MBStruct mbStruct = new MBStruct(field.getType());
		OID[] flds = mbStruct.getFields();

		Element root = doc.getDocumentElement();

		Element itemsElement = doc.createElement(XSD_NS_PREFIX+":complexType");
		root.appendChild(itemsElement);

		String typeName = resolver.resolveType(field.getType());
		if (typeName == null) {
			typeName = filterStructName(mbStruct.getName());
		}
		itemsElement.setAttribute("name", typeName);
		Element sequenceNode = doc.createElement(XSD_NS_PREFIX+":sequence");

		if (superClassName != null)	{
			Element complexContentElement = doc.createElement(XSD_NS_PREFIX+":complexContent");
			itemsElement.appendChild(complexContentElement);
			complexContentElement.setAttribute("mixed", "false");
			Element extensionElement = doc.createElement(XSD_NS_PREFIX+":extension");
			complexContentElement.appendChild(extensionElement);
			extensionElement.setAttribute("base", appendTargetNamespace(superClassName));
			extensionElement.appendChild(sequenceNode);

		} else {
			itemsElement.appendChild(sequenceNode);

		}

		for (OID fld : flds) {
			MBVariable fldObject = new MBVariable(fld);

			FieldInfo info = new FieldInfo(fldObject);
			FieldKey key = new FieldKey(info);

			queue.add(info);

			QName xsdType = null;
			MBObject mbObject = new MBObject(fldObject.getType());
			if (mbObject.getObjectKind()
					== MetaObjectKind.PRIMITIVE_TYPE_OBJECT) {
				MBPrimitiveType pt = new MBPrimitiveType(fldObject.getType());
				xsdType = this.typeMapper.mapPrimitiveOdraType(pt);

			} else {
				xsdType = new QName(targetNamespace,
						filterStructName(sbqlHelper.filterTypeName(fldObject.getType())), TNS_NS_PREFIX );
			}

			if (info.getMaxCard() > 1) {
				xsdType = new QName(
						targetNamespace,
						String.format("ArrayOf%s", xsdType.getLocalPart()),
						TNS_NS_PREFIX);
			}

			Element elementNode = doc.createElement(XSD_NS_PREFIX+":element");
			elementNode.setAttribute("minOccurs", new Integer(fldObject.getMinCard()).toString());
			// arrays already handled above
			elementNode.setAttribute("maxOccurs", "1");
			elementNode.setAttribute("name", fldObject.getName());
			elementNode.setAttribute("type", xsdType.getPrefix() + ":" + xsdType.getLocalPart());
			sequenceNode.appendChild(elementNode);

		}
	}



	private void handleTypeDefType(ITypeDefResolver resolver,
			Queue<FieldInfo> queue, FieldInfo field)
			throws DatabaseException, WSDLBuilderException {
		MBObject mbObject = new MBObject(field.getType());
		MBObject resolvedObject = null;
		MBTypeDef typeDef = new MBTypeDef(mbObject.getOID());
		OID oid = resolver.resolveName(typeDef.getName());
		if (oid != null) {
			resolvedObject = new MBObject(oid);
		} else {
			throw new WSDLBuilderException(
					String.format("Error while resolving type definition: %s",
							mbObject.getName()));
		}

		// TODO refactor this sanity constrained field put to private method
		FieldInfo info = new FieldInfo(field);
		info.setType(resolvedObject.getOID());
		info.setName(resolvedObject.getName());
		FieldKey key = new FieldKey(info);
		queue.add(info);
	}

	/** Creates qualified name by prepending target namespace to given name
	 * @param name
	 * @return
	 */
	private String appendTargetNamespace(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(TNS_NS_PREFIX);
		sb.append(":");
		sb.append(name);
		return sb.toString();
	}

	/**
	 * Temporary solution for handling $struct_ objects
	 * TODO this should be extracted to stateful class implementing IAnonymousConstructNameCreator
	 * @param name
	 * @return
	 */
	private String filterStructName(String name) {
		if (name.startsWith("$struct")) {
			return String.format("Temp%s", name.substring("$struct".length()));
		} else {
			return name;
		}
	}

	/**
	 * Prepares schema
	 *
	 * @param def
	 * @param targetNamespace
	 * @return
	 * @throws WSDLException
	 */
	protected Document initializeTypesSection(Definition def,
			String targetNamespace) throws WSDLException {
		Types types = def.getTypes();
		ExtensionRegistry ext = def.getExtensionRegistry();
		Schema schema = (Schema) ext.createExtension(Types.class,
				SchemaConstants.Q_ELEM_XSD_2001);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
		.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// throw e wrapped with WSDLAssembleException
		}
		Document doc = builder.newDocument();
		Element root = doc.createElement(XSD_NS_PREFIX + ":schema");
		doc.appendChild(root);
		root.setAttribute("elementFormDefault", "qualified");
		root.setAttribute("targetNamespace", targetNamespace);

		schema.setElement(doc.getDocumentElement());
		types.addExtensibilityElement(schema);

		return doc;
	}

	/**
	 * Creates response/request types for given operation
	 *
	 * @param doc
	 * @param mbProc
	 * @param targetNamespace
	 * @throws WSDLException
	 * @throws DatabaseException
	 * @throws WSDLBuilderException
	 */
	protected void createRequestAndResponseType(Document doc,
			MBProcedure mbProc, String targetNamespace) throws WSDLException,
			DatabaseException, WSDLBuilderException {
		Element root = doc.getDocumentElement();

		// request type
		Element inputParams = doc.createElement(XSD_NS_PREFIX + ":element");
		inputParams.setAttribute("name", mbProc.getName());
		Element cplx = doc.createElement(XSD_NS_PREFIX + ":complexType");

		inputParams.appendChild(cplx);

		if (mbProc.getArguments().length != 0) {
			Element seq = doc.createElement(XSD_NS_PREFIX + ":sequence");
			cplx.appendChild(seq);
			// iterate through each argument
			for (OID arg : mbProc.getArguments()) {
				MBVariable var = new MBVariable(arg);

				FieldInfo field = new FieldInfo(var);
				this.queue.add(field);

				String paramName = var.getName();
				String paramTypeName = sbqlHelper.filterTypeName(var.getType());

				OID paramType = var.getType();
				String paramMinCard = new Integer(var.getMinCard()).toString();
				String paramMaxCard = new Integer(var.getMaxCard()).toString();

				Element e = doc.createElement(XSD_NS_PREFIX + ":element");
				e.setAttribute("name", paramName);
				e.setAttribute("minOccurs", paramMinCard);
				e.setAttribute("maxOccurs", "1"); // arrays are handled on the higher level

				if (new MBObject(paramType).getObjectKind() == MetaObjectKind.PRIMITIVE_TYPE_OBJECT) {
					MBPrimitiveType pt = new MBPrimitiveType(paramType);

					QName xsdType = this.typeMapper.mapPrimitiveOdraType(pt);

					if (xsdType != null) {
						e.setAttribute("type", xsdType.getPrefix() + ":"
								+ xsdType.getLocalPart());

					} else {
						throw new WSDLBuilderException(
						"Error while mapping odra primitive type to xsd");
					}
				} else {
					if (var.getMinCard() == 1 && var.getMaxCard() == 1
							|| var.getMinCard() == 0 && var.getMaxCard() == 1) {
						e.setAttribute("type", appendTargetNamespace(paramTypeName));

					} else {
						e.setAttribute("type", appendTargetNamespace(String.format("ArrayOf%s", paramTypeName)));

					}
				}

				seq.appendChild(e);
			}

		}

		// response type
		FieldInfo field = new FieldInfo(mbProc);
		this.queue.add(field);


		String minCard = new Integer(mbProc.getMinCard()).toString();
		String maxCard = new Integer(mbProc.getMaxCard()).toString();

		Element cplx2 = doc.createElement(XSD_NS_PREFIX + ":complexType");
		Element outputParams = doc.createElement("xsd:element");

		outputParams.setAttribute("name", mbProc.getName() + "Response");

		OID resType = mbProc.getType();
		String resTypeName = sbqlHelper.filterTypeName(resType);
		if (!resTypeName.equals("void")) {
			Element seq2 = doc.createElement(XSD_NS_PREFIX + ":sequence");

			Element res = doc.createElement(XSD_NS_PREFIX + ":element");
			res.setAttribute("name", mbProc.getName() + "ResponseResult");

			MBObject resTypeObject = new MBObject(resType);
			if (mbProc.getMinCard() == 1 && mbProc.getMaxCard() == 1
					|| mbProc.getMinCard() == 0 && mbProc.getMaxCard() == 1) {

				// unfold
				if (resTypeObject.getObjectKind() == MetaObjectKind.VARIABLE_OBJECT) {
					// ref procedure parameters marked case
					MBVariable refVar = new MBVariable(resType);

					resType = refVar.getType();
					resTypeObject = new MBObject(resType);
				}

				switch (resTypeObject.getObjectKind()) {
					case PRIMITIVE_TYPE_OBJECT:
						MBPrimitiveType pt = new MBPrimitiveType(resType);
						QName xsdType = this.typeMapper.mapPrimitiveOdraType(pt);
						res.setAttribute("type", String.format("%s:%s", xsdType.getPrefix(), xsdType.getLocalPart()));
						break;
					case CLASS_OBJECT:
					case STRUCT_OBJECT:
					case TYPEDEF_OBJECT:
						res.setAttribute("type", appendTargetNamespace(resTypeName));
						break;
					default:
						throw new WSDLBuilderException("Error mapping from odra to xsd. ");
				}

				res.setAttribute("minOccurs", new Integer(mbProc.getMinCard()).toString());
				res.setAttribute("maxOccurs", "1");

			} else {
				// if multiple occurences are required use ArrayOftypename mapping convention
				res.setAttribute("type", appendTargetNamespace(String.format("ArrayOf%s", resTypeName)));
				res.setAttribute("minOccurs", "1");
				res.setAttribute("maxOccurs", "1");

			}



			seq2.appendChild(res);
			cplx2.appendChild(seq2);

		}

		outputParams.appendChild(cplx2);

		root.appendChild(inputParams);
		root.appendChild(outputParams);

	}



	/**
	 * Creates single operation definition
	 *
	 * @param mbProc
	 * @param targetNamespace
	 * @param def
	 * @param inputMsg
	 * @param outputMsg
	 * @return
	 * @throws DatabaseException
	 */
	protected Operation createOperation(MBProcedure mbProc,
			String targetNamespace, Definition def, Message inputMsg,
			Message outputMsg) throws DatabaseException {
		Operation op = def.createOperation();

		Input input = def.createInput();
		Output output = def.createOutput();
		input.setMessage(inputMsg);
		output.setMessage(outputMsg);

		op.setName(mbProc.getName());
		op.setInput(input);
		op.setOutput(output);
		op.setUndefined(false);

		op.setStyle(OperationType.REQUEST_RESPONSE);

		return op;
	}

	/**
	 * Creates input message
	 *
	 * @param mbProc
	 * @param targetNamespace
	 * @param def
	 * @return
	 * @throws DatabaseException
	 */
	protected Message createInputMessage(MBProcedure mbProc,
			String targetNamespace, Definition def) throws DatabaseException {

		Message msg = def.createMessage();
		msg.setQName(new QName(targetNamespace, mbProc.getName() + "Input"));
		msg.setUndefined(false);

		Part part = def.createPart();
		part.setName("parameters");
		part.setElementName(new QName(targetNamespace, mbProc.getName()));

		msg.addPart(part);
		return msg;

	}

	/**
	 * Creates output message
	 *
	 * @param mbProc
	 * @param targetNamespace
	 * @param def
	 * @return
	 * @throws DatabaseException
	 */
	protected Message createOutputMessage(MBProcedure mbProc,
			String targetNamespace, Definition def) throws DatabaseException {

		Message msg = def.createMessage();
		msg.setQName(new QName(targetNamespace, mbProc.getName() + "Output"));
		msg.setUndefined(false);

		Part part = def.createPart();
		part.setName("parameters");
		part.setElementName(new QName(targetNamespace, mbProc.getName()
				+ "Response"));

		msg.addPart(part);

		return msg;

	}
}
