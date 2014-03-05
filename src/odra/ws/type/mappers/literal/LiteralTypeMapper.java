package odra.ws.type.mappers.literal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.db.objects.meta.PrimitiveTypeKind;
import odra.helpers.XmlHelpers;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.CollectionResult;
import odra.sbql.results.runtime.ComparableResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.config.ConfigServer;
import odra.ws.common.InitializationDef;
import odra.ws.common.Pair;
import odra.ws.common.SBQLHelper;
import odra.ws.type.constructors.TypeConstructor;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.NotImplementedException;
import odra.ws.type.mappers.TypeMapperException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;

/** Literal XML srvices style implementation
 *
 * @since 2006-12-24
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class LiteralTypeMapper implements ITypeMapper {

	private SBQLHelper sbqlHelper = new SBQLHelper();

	/*
	 * (non-Javadoc)
	 *
	 * @see odra.bridges.wsdl.ITypeMapper#mapPrimitiveOdraType(odra.db.objects.meta.MBPrimitiveType)
	 */
	public QName mapPrimitiveOdraType(MBPrimitiveType type) {

		try {
			switch (type.getTypeKind()) {
			case BOOLEAN_TYPE:
				return new QName(XSD_NS, "boolean", XSD_NS_PREFIX);

			case INTEGER_TYPE:
				return new QName(XSD_NS, "int", XSD_NS_PREFIX);

			case REAL_TYPE:
				return new QName(XSD_NS, "double", XSD_NS_PREFIX);

			case STRING_TYPE:
				return new QName(XSD_NS, "string", XSD_NS_PREFIX);

			case DATE_TYPE:
				return new QName(XSD_NS, "dateTime", XSD_NS_PREFIX);

			case VOID_TYPE:
				// it shouldn't happen
				return null;
			}
		} catch (DatabaseException e) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Unsupported primitive type. ");

		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see odra.bridges.type.ITypeMapper#mapPrimitiveXMLType(javax.xml.namespace.QName)
	 */
	public String mapPrimitiveXMLType(QName type) {
		if (!type.getNamespaceURI().equals(XSD_NS)) {
			throw new RuntimeException(
			"Incorrect argument - type doesn't come from XSD namespace. ");

		}

		return xmlToOdra.get(type.getLocalPart());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see odra.bridges.wsdl.ITypeMapper#mapXMLToOdra(odra.db.OID,
	 *      org.w3c.dom.Node)
	 */
	public String mapXMLToOdra(OID type, Node root) throws TypeMapperException {
		try {
			return this.mapXMLToOdra(type, root, false, new InitializationDef());
		} catch (DatabaseException ex) {
			throw new TypeMapperException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see odra.bridges.wsdl.ITypeMapper#mapXMLToOdra(odra.db.OID,
	 *      org.w3c.dom.Node)
	 */
	public String mapXMLToOdra(OID type, Node root, InitializationDef preInitialization) throws TypeMapperException {
		try {
			return this.mapXMLToOdra(type, root, false, preInitialization);
		} catch (DatabaseException ex) {
			throw new TypeMapperException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see odra.bridges.wsdl.ITypeMapper#mapOdraResultToXML(org.w3c.dom.Document,
	 *      odra.sbql.results.runtime.Result)
	 */
	public NodeList mapOdraResultToXML(Document context, Result res,
			String collectionItemName, String namespace) throws TypeMapperException {


		Queue<Pair<Result, Element>> queue = new LinkedList<Pair<Result, Element>>();

		Element localRoot = context.createElementNS(namespace, "localRoot");
		queue.add(new Pair(res, localRoot));

		while (!queue.isEmpty()) {
			Pair<Result, Element> curPair = queue.poll();
			Result curRes = curPair.getKey();
			Element curElement = curPair.getValue();

			if (curRes instanceof SingleResult) {
				if (curRes instanceof BinderResult) {
					BinderResult bRes = (BinderResult) curRes;
					String name = bRes.getName();
					Element subElement = context.createElementNS(namespace, name);

					curElement.appendChild(subElement);
					queue
					.add(new Pair<Result, Element>(bRes.value,
							subElement));

				} else if (curRes instanceof ComparableResult) {
					if (curRes instanceof IntegerResult) {
						IntegerResult iRes = (IntegerResult) curRes;
						Node node = context.createTextNode(new Integer(
								iRes.value).toString());

						curElement.appendChild(node);

					} else if (curRes instanceof BooleanResult) {
						BooleanResult bRes = (BooleanResult) curRes;
						Node node = context.createTextNode(new Boolean(
								bRes.value).toString());

						curElement.appendChild(node);

					} else if (curRes instanceof DoubleResult) {
						DoubleResult dRes = (DoubleResult) curRes;
						Node node = context.createTextNode(new Double(
								dRes.value).toString());

						curElement.appendChild(node);

					} else if (curRes instanceof StringResult) {
						StringResult sRes = (StringResult) curRes;
						Node node = context.createTextNode(sRes.value);

						curElement.appendChild(node);

					} else if (curRes instanceof DateResult) {
						DateResult dRes = (DateResult) curRes;

						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
						String dateTime = df.format(dRes.value);
						Node node = context.createTextNode(dateTime);

						curElement.appendChild(node);

					}
				} else if (curRes instanceof StructResult) {
					StructResult tRes = (StructResult) curRes;

					for (SingleResult f : tRes.fieldsToArray()) {
						// throw it out and pass curElement instead

						queue.add(new Pair<Result, Element>(f, curElement));
					}

				}

			} else if (curRes instanceof CollectionResult) {
				CollectionResult colRes = (CollectionResult) curRes;
				for (SingleResult e : colRes.elementsToArray()) {
					// it need to be packed it into element tags

					String typeName = "";
					if (e instanceof IntegerResult) {
						typeName = "int";

					} else if (e instanceof StringResult) {
						typeName = "string";

					} else if (e instanceof BooleanResult) {
						typeName = "boolean";

					} else if (e instanceof DoubleResult) {
						typeName = "double";

					} else if (e instanceof DateResult) {
						typeName = "string";

					} else {
						// complex one
						if (collectionItemName == null) {
							throw new TypeMapperException("Internal error.");
						}

						typeName = collectionItemName;

					}

					Element arrayElement = context.createElementNS(namespace, typeName);
					curElement.appendChild(arrayElement);

					queue.add(new Pair<Result, Element>(e, arrayElement));
				}
			}
		}

		return localRoot.getChildNodes();
	}

	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapper#mapXSDToModel(com.sun.xml.xsom.XSSchemaSet, java.util.List, java.util.List, odra.db.objects.data.DBModule)
	 */
	public void mapXSDToModel(XSSchemaSet xss, List<QName> rootTypes, List<QName> rootElements, DBModule module) throws TypeMapperException {
		try	{
			LiteralXSFunction xsFunction = new LiteralXSFunction();

			for (QName type : rootTypes) {
				xsFunction.addType(type);
			}

			for (QName element : rootElements) {
				xsFunction.addElement(element);
			}

			for (XSSchema xs : xss.getSchemas()) {
				TypeConstructor constructor = xs.apply(xsFunction);
				constructor.construct(module);
				if (xsFunction.isComplete()) {
					break;
				}
			}

			if (!xsFunction.isComplete()) {
				throw new RuntimeException("Some required elements and types have not been found. ");
			}
		} catch (DatabaseException ex) {
			throw new TypeMapperException(ex);

		}
	}

	private String mapXMLToOdra(OID type, Node root, boolean asSuffix,
			InitializationDef preInitialization) throws DatabaseException, TypeMapperException {

		// substract from variable
		String asGroup = "";

		MBObject tmpObj = null;
		MBVariable var = new MBVariable(type);
		if (var.isValid()) {
			type = var.getType();
			if (asSuffix) {
				asGroup = " as " + var.getName();
			}
			// expand type defs
			if (var.isTypeTypeDef()) {
				tmpObj = new MBObject(type);
			}
		} else {
			// class promotion case
			tmpObj = new MBObject(type);

		}

		if (tmpObj != null) {
			MetaObjectKind tmpKind = tmpObj.getObjectKind();

			// expand type def
			while (tmpKind == MetaObjectKind.TYPEDEF_OBJECT) {
				MBTypeDef tdef = new MBTypeDef(type);

				tmpObj = this.sbqlHelper.expandTypeDef(tdef);
				type = tmpObj.getOID();
				tmpKind = tmpObj.getObjectKind();

			}
		}

		MBObject obj = new MBObject(type);
		MetaObjectKind kind = obj.getObjectKind();

		if (kind == MetaObjectKind.VARIABLE_OBJECT) {
			// ref procedure parameters marked case
			MBVariable refVar = new MBVariable(type);

			type = refVar.getType();
			obj = new MBObject(type);
			kind = obj.getObjectKind();

		}

		if (kind == MetaObjectKind.PRIMITIVE_TYPE_OBJECT) {
			MBPrimitiveType primitiveType = new MBPrimitiveType(obj.getOID());

			return this.renderPrimitiveParameter(primitiveType, root.getFirstChild()) + asGroup;

		} else if (kind == MetaObjectKind.STRUCT_OBJECT) {
			return mapStructFromXMLToOdra(root, preInitialization, asGroup, obj);

		} else if (kind == MetaObjectKind.CLASS_OBJECT) {
			return mapClassFromXMLToOdra(root, preInitialization, asGroup, obj);
		}

		return null;

	}

	/**
	 * Class type instance is mapped as pointer to collection variable exact index.
	 * Initialization need to take place before.
	 * @param root
	 * @param preInitialization
	 * @param asGroup
	 * @param obj
	 * @return
	 * @throws DatabaseException
	 * @throws TypeMapperException
	 */
	private String  mapClassFromXMLToOdra(Node root, InitializationDef preInitialization,
			String asGroup, MBObject obj) throws DatabaseException, TypeMapperException {
		MBClass mbClass = new MBClass(obj.getOID());

		int index = preInitialization.getNextIndex(mbClass.getName());
		String variableHandle = String.format("ws%s[%d] %s", mbClass.getName(), index, asGroup);

		DBModule module = mbClass.getModule();
		OID structOid = module.findFirstByName(mbClass.getStructureTypeName(), module.getMetabaseEntry());
		MBStruct struct = new MBStruct(structOid);
		if (!struct.isValid()) {
			throw new TypeMapperException("Problem occured while mapping " + mbClass.getName() + " type. ");
		}
		String constr = mapStructFromXMLToOdra(root, preInitialization, "", struct);
		preInitialization.addStatement(mbClass.getName(), String.format("create ws%s%s;", mbClass.getName(), constr));

		return variableHandle;
	}


	private String mapStructFromXMLToOdra(Node root, InitializationDef preInitialization,
			String asGroup, MBObject obj) throws DatabaseException, TypeMapperException {
		// initialize
		String param = "(";
		List<Element> nodes = XmlHelpers.filterElements(root.getChildNodes());
		MBStruct strObj = new MBStruct(obj.getOID());
		int j = 0;
		for (int i = 0; i < strObj.getFields().length; i++) {

			boolean first = false;
			boolean passed = true;
			if (j + 1 < nodes.size()) {
				String curName = nodes.get(j).getLocalName();
				String sibName = nodes.get(j + 1).getLocalName();

				first = curName.equals(sibName);
				boolean end = !first;
				while (!end) {
					passed = false;
					curName = nodes.get(j).getLocalName();
					if (j + 1 >= nodes.size()) {
						end = true;
						param += this.mapXMLToOdra(strObj.getFields()[i], nodes.get(j), true, preInitialization);
						param += " )";
						continue;
					}
					sibName = nodes.get(j + 1).getLocalName();
					if (first) {
						param += " (";
						first = false;
					}

					param += this.mapXMLToOdra(strObj.getFields()[i], nodes.get(j), true, preInitialization);
					if (curName.equals(sibName)) {
						param += " union ";
					} else {
						param += " )";
						end = true;
					}
					j++;
				}

			}
			if (passed) {
				param += this.mapXMLToOdra(strObj.getFields()[i], nodes.get(j),
						true, preInitialization);

				if (j != nodes.size() - 1) {
					param += ", ";
				}

				j++;
			}

		}
		param += ")" + asGroup;

		return param;
	}

	private String renderPrimitiveParameter(MBPrimitiveType type, Node node) throws DatabaseException {

		PrimitiveTypeKind kind = type.getTypeKind();

		if (kind == PrimitiveTypeKind.STRING_TYPE) {
			String filteredValue = "";
			if (node != null) {
				filteredValue = node.getNodeValue().replaceAll("[\\t\\n\\x0B\\f\\r]", "");
			}
			return "\"" + filteredValue + "\"";
			// FIXME we currently filter out all special characters

		} else if (kind == PrimitiveTypeKind.DATE_TYPE) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
			try {
				if (node != null) {
					Date date = df.parse(node.getNodeValue());
					SimpleDateFormat tdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
					return tdf.format(date);
				} else {
					// TODO find better way to produce nulls
					return "2008-10-10 intersect 2008-10-11";
				}
			} catch (ParseException ex) {
				throw new DatabaseException("Cannot covert from xsd:dateTime to ODRA date format. ", ex);
			}
		}
		if (node != null) {
			return node.getNodeValue();
		} else {
			// TODO need some convenient way create null results first
			throw new NotImplementedException();

		}


	}

	// primitive types helper maps
	private static HashMap<String, String> xmlToOdra;

	private static HashMap<String, String> odraToXml;

	static String[][] mapping = { { "string", "string" },
		{ "integer", "integer" }, { "int", "integer" },
		{ "long", "integer" }, { "short", "integer" },
		{ "decimal", "real" }, { "float", "real" }, { "double", "real" },
		{ "boolean", "boolean" }, { "byte", "integer" },
		{ "unsignedInt", "integer" }, { "unsignedShort", "integer" },
		{ "unsignedByte", "integer" }, { "QName", "string" },
		{ "dateTime", "date" }, { "date", "date" },
		{ "time", "string" }, { "anyURI", "string" },
		{ "base64Binary", "string" }, { "hexBinary", "string" },
		{ "anySimpleType", "string" }, { "duration", "string" },
		{ "gYearMonth", "string" }, { "gYear", "string" },
		{ "gMonthDay", "string" }, { "gDay", "string" },
		{ "gMonth", "string" }, { "normalizedString", "string" },
		{ "token", "string" }, { "language", "string" },
		{ "Name", "string" }, { "NCName", "string" }, { "ID", "string" },
		{ "NMTOKEN", "string" }, { "NMTOKENS", "string" },
		{ "nonPositiveInteger", "integer" },
		{ "negativeInteger", "integer" },
		{ "nonNegativeInteger", "integer" }, { "unsignedLong", "integer" },
		{ "positiveInteger", "integer" }, { "IDREF", "string" },
		{ "IDREFS", "string" }, { "date", "date" }, { "dateTime", "date" }, };

	static {
		xmlToOdra = new HashMap<String, String>();
		odraToXml = new HashMap<String, String>();

		for (String[] entry : mapping) {
			xmlToOdra.put(entry[0], entry[1]);
			odraToXml.put(entry[1], entry[0]);
		}
	}

	/* (non-Javadoc)
	 * @see odra.bridges.type.mappers.ITypeMapper#mapXML(java.lang.String)
	 */
	public String mapXML(String type) {
		return xmlToOdra.get(type);
	}

	/* (non-Javadoc)
	 * @see odra.bridges.type.mappers.ITypeMapper#mapOdra(java.lang.String)
	 */
	public String mapOdra(String type) {
		return odraToXml.get(type);
	}


	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapper#mapTypeDefName(java.lang.String)
	 */
	public String mapTypeDefName(String name) {
		return name + "Type";
	}
}
