package odra.filters.XSD;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraVariableSchema;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.LinkerException;
import odra.sbql.builder.ModuleCompiler;
import odra.sbql.builder.ModuleLinker;
import odra.system.config.ConfigServer;


public class XSDSchemaInterpreter implements XMLNodeImporter {

	private DBModule module;
	private MetabaseManager metamanager;
	ModuleLinker linker;
	ModuleCompiler compiler;
	private String schemaPrefix;
	private HashMap<String, TypeConstructor> structures;
	private HashMap<String, Vector<TypeConstructor>> undefinedTypes;
	private Stack<TypeConstructor> parentsStack;
//	private HashMap<String, TypeConstructor> knownTypeDefs;
	private Stack<Boolean> isPushedStack;
	private TypeConstructor lastParent;
	private int level;
	private boolean M0;
	protected boolean noLinking;
	private boolean noPCDATA;
	private long nodesCount;
	
	enum LinkingStrategy
	{
		FRAGILE, SAFE;
	}
	
	enum XMLStructure
	{
		M0, ANNOTATIONS, NO_XML
	}

	public XSDSchemaInterpreter(DBModule mod, boolean M0, boolean noXML, boolean noLinking) {
		this.module = mod;
		this.metamanager = new MetabaseManager(mod);
		this.level = 0;
		this.M0 = M0;
		this.noPCDATA = noXML;
		this.noLinking = noLinking;
		structures = new HashMap<String, TypeConstructor>();
		undefinedTypes = new HashMap<String, Vector<TypeConstructor>>();
		parentsStack = new Stack<TypeConstructor>();
		isPushedStack = new Stack<Boolean>();
//		knownTypeDefs = new HashMap<String, TypeConstructor>();
		linker = BuilderUtils.getModuleLinker();
		compiler = BuilderUtils.getModuleCompiler();
	}

	/**
	 * @param args
	 */


	public long getProcessedNodesCount() {
		return nodesCount;
	}

	public Set<String> getUnresolvedIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	public OID interpretElement(Element node, OID parent) throws DatabaseException, ShadowObjectException, FilterException
	{
		nodesCount++;
		
		// SCHEMA
		if (node.getLocalName().equals("schema"))
			this.schemaPrefix = node.getNamespacePrefix();
		// ELEMENT	 
		else if (node.getLocalName().equals("element"))
		{
			TypeConstructor typeStr = new TypeConstructor(node, false, M0, parentsStack.empty()?null:parentsStack.peek());
			this.lastParent = typeStr;
		}
		// ATTRIBUTE
		else if (node.getLocalName().equals("attribute"))
		{
			TypeConstructor typeStr = new TypeConstructor(node, false, M0, parentsStack.empty()?null:parentsStack.peek());
			this.lastParent = typeStr;
		}
		// EXTENSION OR RESTRICTION
		else if (node.getLocalName().equals("extension") || node.getLocalName().equals("restriction"))
		{
			parentsStack.peek().setTypeExtension( node );
		}
		// COMPLEX TYPE
		else if (node.getLocalName().equals("complexType"))
		{
			if (parentsStack.empty())
			{
				TypeConstructor typeStr = new TypeConstructor(node, false, M0, null);
				this.lastParent = typeStr;
			}
			else
				parentsStack.peek().setTypeMix( node );
		}
		// SIMPLE TYPE
		else if (node.getLocalName().equals("simpleType"))
		{
			if (parentsStack.empty())
			{
				TypeConstructor typeStr = new TypeConstructor(node, false, M0, null);
				this.lastParent = typeStr;
			}
		}
		else
			lastParent = null;
		return null;
	}

	public OID interpretTextElement(Element node, OID parent) throws DatabaseException, ShadowObjectException, FilterException {
		//LogWriter.getLogger().fine("Interpreting Text Element:" + node.getLocalName());
		return null;
	}

	public OID interpretTextNode(Node node, OID parent) throws DatabaseException, ShadowObjectException, FilterException {
		//LogWriter.getLogger().fine("Interpreting text:" + node.getValue());
		return null;
	}

	public void openInnerElementScope(Element element) {
		
		level++;
		if (lastParent!=null)
		{
			ConfigServer.getLogWriter().getLogger().finest("==== pushing parent :" + lastParent);
			parentsStack.push(lastParent);
			isPushedStack.push(true);
			lastParent = null;
		}
		else 
			isPushedStack.push(false);
		//lastParent = null;
		ConfigServer.getLogWriter().getLogger().finest("Going inside level:" + level + element.getLocalName());
		// TODO Auto-generated method stub
		
	}

	public void closeInnerElementScope(Element element) throws FilterException, DatabaseException {
		//LogWriter.getLogger().fine("Leaving level:" + level + "  " + element.getLocalName());
		if ((isPushedStack.pop()) && (level>1))
		{	Object p = parentsStack.pop();
			ConfigServer.getLogWriter().getLogger().finest("==== popping parent :" +  p );
		}
		level--;
	}

	public Collection<TypeConstructor> getResults()
	{
		for (TypeConstructor type:structures.values())
			ConfigServer.getLogWriter().getLogger().finest("##### CREATED TYPE: " + type);
		return structures.values();
	}

	public void rebuildModule(DBModule module) throws LinkerException, DatabaseException
	{
		linker.linkModule(module);
		compiler.compileModule(module);
	}
	
	public class TypeConstructor
	{
		Element root;
		TypeConstructor parent;
		Vector<TypeConstructor> children;
		int minCardinality;
		int maxCardinality;
		String xmlTypeName;
		String name;
		private boolean isTypeDef;
		private MBStruct parentMBStruct;
		private boolean finished;
		private boolean registeredAsUnknown;
		private boolean isAttribute;
		private boolean M0;
		private String sbqlTypeDefName;
		
		public TypeConstructor(Element root, boolean isAttribute, boolean M0, TypeConstructor newParent) throws XSDParsingException
		{
			this.root = root;
			this.isTypeDef = false;
			this.finished = false;
			this.isAttribute = isAttribute;
			this.M0 = M0;
			setParent(newParent);

			// CARDINALITY
			Attribute minCardAttr = root.getAttribute("minOccurs");
			Attribute maxCardAttr = root.getAttribute("maxOccurs");
			if (minCardAttr != null)
				minCardinality = cardinalityMapping( minCardAttr.getValue() );
			else 
				minCardinality = 0;
			if (maxCardAttr != null)
				maxCardinality = cardinalityMapping( maxCardAttr.getValue() );
			else 
				maxCardinality = Integer.MAX_VALUE;
			
			// CHECK TYPEDEF DESTINATION
			Attribute type = root.getAttribute("type");
			if (type!=null)
			{
				xmlTypeName = removePrefix( type.getValue() );
				if (getSimpleType(xmlTypeName)==null)
				{
					this.isTypeDef = true;
					//this.isComplexType = true;
					this.sbqlTypeDefName = xmlTypeName;
				}
			}
			
			// NAME 
			Attribute nameAttr = root.getAttribute("name");
			if (nameAttr!=null)
			{
				this.name = determineName(nameAttr.getValue());
				if (structures.containsKey(name) && this.parent==null)
					ConfigServer.getLogWriter().getLogger().severe("Name definition already declared : " + structures.get(name) +
								"New definition (not accepted): "+ this );
				if (this.parent==null)
					structures.put(name, this);
			}
			else //REF 
			{	
				nameAttr = root.getAttribute("ref");
				if (nameAttr==null)
					throw new XSDParsingException("Undefined type name for an element: " + root.toXML(), null);
				this.name = removePrefix( nameAttr.getValue() );
				this.sbqlTypeDefName = this.name;
				this.isTypeDef = true;
			}
		}

		private String removePrefix(String s)
		{
			int baseStart = s.indexOf(":");
			return s.substring(baseStart+1);
		}

		public void setTypeMix(Element node) {
			Attribute type = node.getAttribute("mixed");
			if (type!=null && type.getValue().equals("true"))
				xmlTypeName = "string";
		}

		public void setTypeExtension(Element node) {
			Attribute type = node.getAttribute("base");
			if (type!=null)
			{
				xmlTypeName = type.getValue().replace(schemaPrefix+":", "");
			}
		}

		private void setParent(TypeConstructor parent)
		{
			if (parent!=null)
			{
				ConfigServer.getLogWriter().getLogger().finest("This is " + this.name +" setting parent -> " + parent.name );
				this.parent = parent;
				parent.addChild(this);
			}
		}
		
		private void addChild(TypeConstructor constructor) {
			if (children==null)
				children = new Vector<TypeConstructor>();
			children.add(constructor);
		}

		public String getName()
		{
			return name;
		}
		
		private int cardinalityMapping(String value) throws XSDParsingException {
			int result;
			try{
				result = Integer.parseInt(value);
			}
			catch(NumberFormatException e)
			{
				if (value.equals("unbounded"))
					result = Integer.MAX_VALUE;
				else
					throw new XSDParsingException("Cannot understand cardinality: " + value, null);
			}
			return result;
		}
		
		private String getSimpleType(String value)
		{
			if (value==null)
				return "no type (null)";
			if (simpleTypeMapping.containsKey(value))
				return simpleTypeMapping.get(value);
			else 
				return null;
		}
		
		public String toString()
		{
			String parentName = "";
			if (parent!=null)
				parentName = parent.getName();
			String res = "Element Type :" + name + "[" + minCardinality + ", " + maxCardinality + ", xmlType:" + xmlTypeName + " sbqlType:" + getSimpleType(xmlTypeName) + " typedef:" + sbqlTypeDefName + ", " + parentName + " ref="  + this.isTypeDef + "]";
			if (children!=null)
				for( TypeConstructor child:children)
					res += "\n\t"+child.toString();
			return res;
		}

		public String typeDefMapping(String name)
		{
			return "_" + name + "_TypeDef";
		}
		
		
		public OID produceMetaObject(DBModule module, MBStruct parent) throws DatabaseException, XSDParsingException {
			return produceMetaObject(module, parent, false);
		}
		
		public OID produceMetaObject(DBModule module, MBStruct parent, boolean typeDefsOnly) throws DatabaseException, XSDParsingException 
		{
			OID oid = null;

			if (!noLinking)
				rebuildModule(module);
					
			if (this.isTypeDef)
			{
				if(children!=null)
					throw new XSDParsingException("Inconsistency: referring to type def and defined children for :" + name, null );
				OID	typeDefOid = module.findFirstByName( typeDefMapping(sbqlTypeDefName), module.getMetabaseEntry() );
				if (typeDefOid == null)
				{
					ConfigServer.getLogWriter().getLogger().finest("@@@@@ NO SUCH TYPE YET :" + typeDefMapping(sbqlTypeDefName));
					this.parentMBStruct = parent;
					if (!registeredAsUnknown)
					{	if (undefinedTypes.get( typeDefMapping(sbqlTypeDefName) )==null)
							undefinedTypes.put(typeDefMapping(sbqlTypeDefName), new Vector<TypeConstructor>());
						undefinedTypes.get( typeDefMapping(sbqlTypeDefName) ).add(this);
						this.registeredAsUnknown = true;
					}
					return null;
				}
				ConfigServer.getLogWriter().getLogger().finest("@@@@@ TYPE FOUND :" + typeDefMapping(sbqlTypeDefName) + " name:" + typeDefOid.getObjectName() );
				MBTypeDef typeDef = new MBTypeDef( typeDefOid );
				if (typeDef == null)
				{
					ConfigServer.getLogWriter().getLogger().fine("@@@@@ STRANGE :" + typeDef + " name:" + typeDefOid.getObjectName() );
					return null;
				}	
				if (parent!=null)
				{
					if ( new MBPrimitiveType(typeDef.getType()).isValid() )
						oid = createSimpleField( parent, name, minCardinality, maxCardinality, typeDef.getName() );
					else	
						oid = createField( parent, name, minCardinality, maxCardinality, typeDef.getName() );
				}
				else
					oid = createMetaVariable(name, minCardinality, maxCardinality, typeDef.getName() );
			}
			else if (children!=null)
			{
				oid = metamanager.createMetaStruct(0);
				MBStruct struct = new MBStruct(oid);
				if (parent!=null)
					createField( parent, name, minCardinality, maxCardinality, struct.getName() );
				else{
					if (typeDefsOnly) {
						createTypeDef( name, minCardinality, maxCardinality, struct.getName() );
					} else {
						createMetaVariableWithTypeDef( name, minCardinality, maxCardinality, struct.getName()  );
					}
				}
				for (TypeConstructor child:children)
					child.produceMetaObject(module, struct, typeDefsOnly);
			}
			else if (xmlTypeName!=null)
			{
				String sbqlType = getSimpleType(xmlTypeName);
				if (sbqlType != null)
				{
					if (parent!=null)
						oid = createSimpleField( parent, name, minCardinality, maxCardinality, sbqlType );
					else {
						if (typeDefsOnly) {
							createTypeDef( name, minCardinality, maxCardinality, sbqlType );
						} else {
							createMetaVariableWithTypeDef( name, minCardinality, maxCardinality, sbqlType );
						}
					}
				}
				else
				{
					ConfigServer.getLogWriter().getLogger().finest("UNKNOWN STRUCTURE?");
				}
			}
			else
				throw new XSDParsingException( "Unknown structure: " + name, null );
			
			finished = true;

			return oid;
		}
		
		public void createTypeDef(String name, int minCardinality, int maxCardinality, String typeName) throws DatabaseException, XSDParsingException
		{
			MBTypeDef typeDef = new MBTypeDef( metamanager.createMetaTypeDef(typeDefMapping(name), typeName) );
			ConfigServer.getLogWriter().getLogger().finest("*** creating type def  " + typeDef.getName() + ":" + typeName + " in module " + module.getName());
			
			// FIXME is it neccessary?
			Vector<TypeConstructor> unknownRefs = undefinedTypes.get( typeDefMapping(name) );
			if ( unknownRefs!=null)
			{
				for( TypeConstructor unknown : unknownRefs )
				{
					ConfigServer.getLogWriter().getLogger().finest("==== RESOLVING UNKNOWN :" +  unknown.name );
					if (unknown!=null && !unknown.finished)
						unknown.produceMetaObject(module, unknown.parentMBStruct, true);
				}
			}
		}	
		
		public OID createMetaVariableWithTypeDef(String name, int minCardinality, int maxCardinality, String typeName) throws DatabaseException, XSDParsingException
		{
			MBTypeDef typeDef = new MBTypeDef( metamanager.createMetaTypeDef(typeDefMapping(name), typeName) );
			ConfigServer.getLogWriter().getLogger().finest("*** creating type def  " + typeDef.getName() + ":" + typeName + " in module " + module.getName());
			OID oid = createMetaVariable(name, minCardinality, maxCardinality, typeDef.getName() );
			Vector<TypeConstructor> unknownRefs = undefinedTypes.get( typeDefMapping(name) );
			if ( unknownRefs!=null)
			{
				for( TypeConstructor unknown : unknownRefs )
				{
					ConfigServer.getLogWriter().getLogger().finest("==== RESOLVING UNKNOWN :" +  unknown.name );
					if (unknown!=null && !unknown.finished)
						unknown.produceMetaObject(module, unknown.parentMBStruct, false);
				}
			}
			return oid;
		}

		public OID createMetaVariable(String name, int minCardinality, int maxCardinality, String typeName) throws DatabaseException
		{
			ConfigServer.getLogWriter().getLogger().finest("*** creating variable " + name + ":" + typeName + " in module " + module.getName());
			return metamanager.createMetaVariable(new OdraVariableSchema(name, typeName,minCardinality, maxCardinality,  0));
			//return module.createMetaAnnotatedVariable(name, minCardinality, maxCardinality, typeName, 0);
		}

		public OID createSimpleField( MBStruct parent, String name, int minCardinality, int maxCardinality, String typeName) throws DatabaseException
		{
			String fieldName;
			if (isAttribute && M0 && !noPCDATA)
				fieldName = XMLImportFilter.ATTR_SIGN+name;
			else
				fieldName = name;
			
			if ((( (!isAttribute) || (!M0) ) && (!noPCDATA)))
			{
				ConfigServer.getLogWriter().getLogger().finest("*** creating PCDATA for " + fieldName + " in " + parent.getName() + " of type " + typeName );
				OID pcdata = metamanager.createMetaStruct(0); 
				MBStruct pcStruct = new MBStruct(pcdata);
				pcStruct.createField(XMLImportFilter.PCDATA, 1, 1, typeName, 0);
				typeName = pcStruct.getName();
			}
			return createField( parent, fieldName, minCardinality, maxCardinality, typeName );
		}

		public OID createField( MBStruct parent, String name, int minCardinality, int maxCardinality, String typeName) throws DatabaseException
		{
			ConfigServer.getLogWriter().getLogger().finest("*** creating field " + name + " in " + parent.getName() + " of type " + typeName );
			return parent.createField(name, minCardinality, maxCardinality, typeName, 0);
		}
	}

	static HashMap<String, String> simpleTypeMapping;
	static String [][] simpleTypeMappingArray = 
		  { {"string", 			"string"},
			{"integer", 		"integer"},
			{"int" , 			"integer"},
			{"long", 			"integer"},
			{"short", 			"integer"},
			{"decimal", 		"real"},
			{"float", 			"real"},
			{"double", 			"real"},
			{"boolean", 		"boolean"},
			{"byte", 			"integer"},
			{"unsignedInt", 	"integer"},
			{"unsignedShort", 	"integer"},
			{"unsignedByte", 	"integer"},
			{"QName", 			"string"},
			{"dateTime", 		"string"},
			{"date", 			"string"},
			{"time", 			"string"},
			{"anyURI", 			"string"},
			{"base64Binary", 	"integer"},
			{"hexBinary", 		"integer"},
			{"anySimpleType", 	"string"},
			{"duration", 		"string"},
			{"gYearMonth", 		"string"},
			{"gYear", 			"string"},
			{"gMonthDay", 		"string"},
			{"gDay", 			"string"},
			{"gMonth", 			"string"},
			{"normalizedString", "string"},
			{"token", 			"string"},
			{"language", 		"string"},
			{"Name", 			"string"},
			{"NCName", 			"string"},
			{"ID", 				"string"},
			{"NMTOKEN", 		"string"},
			{"NMTOKENS", 		"string"},
			{"nonPositiveInteger", "integer"},
			{"negativeInteger", "integer"},
			{"nonNegativeInteger", "integer"},
			{"unsignedLong", 	"integer"},
			{"positiveInteger", "integer"},
//			{"IDREF"		,	"reference"},
//			{"IDREFS"		,	"references"},
			{"IDREF"		,	"string"},
			{"IDREFS"		,	"string"},
			{"date"		,	"date"},
			{"dateTime"		,	"date"},
		  };

	static{
		simpleTypeMapping = new HashMap<String, String>();
		for(String[] entry:simpleTypeMappingArray)
			simpleTypeMapping.put( entry[0], entry[1] );
	}

	public void finalizeUnknownIdrefs() throws DatabaseException {
		
	}

	public HashMap<String, LinkedList<OID>> getUknownReferences() {
		return null;
	}

	public void closeOuterElementScope(Element element) throws FilterException, DatabaseException {
	}

	public void openOuterElementScope(Element element) {
	}
	
	/**
	 * Determines the object name from the name attribute value. The base method does nothing, it is overriden 
	 * in the relational wrapper schema importer, however. 
	 * 
	 * @param nameAttribValue name attribute value
	 * @return object name
	 * @author jacenty
	 */
	protected String determineName(String nameAttribValue)
	{
		return nameAttribValue;
	}
}
