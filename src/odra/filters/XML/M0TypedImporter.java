package odra.filters.XML;

import java.text.ParseException;
import java.util.Stack;
import java.util.logging.Level;
import nu.xom.Attribute;
import nu.xom.Element;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBAnnotatedVariableObject;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.db.objects.meta.PrimitiveTypeKind;
import odra.filters.FilterException;
import odra.system.config.ConfigServer;
import odra.util.DateUtils;

/**
 * This is an extension of M0AnnotatedInterpreter. It adds inferring on XML elements type upon metabase entries.
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class M0TypedImporter extends M0AnnotatedImporter
{

	private Stack<MBObject> metabaseScope;

	public M0TypedImporter(DBModule module) throws DatabaseException {
		super(module, false, true);
		metabaseScope = new Stack<MBObject>();
		metabaseScope.push( new MBObject( module.getMetabaseEntry() ) );
	}

	protected void handleAttribute( Attribute attr, OID main ) throws DatabaseException, FilterException
	{
		ConfigServer.getLogWriter().getLogger().finest(" CREATE ATTRIBUTE " + attr.getLocalName() );

		MBObject mo = metabaseScope.peek();
		ConfigServer.getLogWriter().getLogger().finest(" META SCOPE INSIDE " + mo.getName() + "  " + mo.getClass());
		if (!attr.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema-instance"))
			super.handleAttribute(attr, main);
	}

	public MBObject findFirstMetaObjectInside( MBObject parent, String name ) throws DatabaseException, FilterException
	{
		if (parent==null)
			return null;
		// TODO: (KK) add namespaces for metabase type names
		name = determineName(name);
		ConfigServer.getLogWriter().getLogger().finest(" I AM IN : " + parent.getName());
		ConfigServer.getLogWriter().getLogger().finest("  LOOKING FOR :" + name );
		MetaObjectKind kind = parent.getObjectKind();
		switch( kind )
		{
			case VARIABLE_OBJECT:
			{
				OID type = new MBVariable(parent.getOID()).getType();
				MBObject child = new MBObject(type);
				
				return findFirstMetaObjectInside( child, name );
			}
			case ANNOTATED_VARIABLE_OBJECT:
			{
				OID type = new MBVariable( new MBAnnotatedVariableObject(parent.getOID()).getValueRef()).getType();
				MBObject child = new MBObject(type);
				
				return findFirstMetaObjectInside( child, name );
			}
			case TYPEDEF_OBJECT:
			{
				return findFirstMetaObjectInside( new MBObject(new MBTypeDef(parent.getOID()).getType()), name );
			}
			case STRUCT_OBJECT:
			{
				MBStruct str = new MBStruct(parent.getOID());
				return findField( name, str );
			}
			case PRIMITIVE_TYPE_OBJECT:
			{
				return parent;
			}
			case UNKNOWN_OBJECT:
			{
				OID structOid = parent.getOID();
				return new MBObject( module.findFirstByName(name, structOid) );
			}
			default:
			{
				throw new FilterException("Wrong metaobject kind: " + kind, null);
			}
		}
	}
	
	private MBObject findField( String name, MBStruct str ) throws DatabaseException
	{
		for ( OID field : str.getFields() )
			if (field.getObjectName().equals(name))
				return new MBObject( field );
		return null;
	}
	
	private OID createSimpleObject( MBObject mo, String name, String value, OID parent ) throws DatabaseException
	{
		if (mo==null)
			return null;
		ConfigServer.getLogWriter().getLogger().finest("GET SIMPLE TYPE  " + mo + name + " " + value + " parent: " + parent==null?null:parent.getObjectName());
		MetaObjectKind kind = mo.getObjectKind();

		if (kind == MetaObjectKind.PRIMITIVE_TYPE_OBJECT)
		{
			PrimitiveTypeKind type = new MBPrimitiveType(mo.getOID()).getTypeKind();
			if (type == PrimitiveTypeKind.BOOLEAN_TYPE)
				return parent.createBooleanChild(Database.getNameIndex().addName(name), Boolean.parseBoolean(value));
			else if (type == PrimitiveTypeKind.INTEGER_TYPE)
				return parent.createIntegerChild(Database.getNameIndex().addName(name), Integer.parseInt(value));
			else if (type == PrimitiveTypeKind.REAL_TYPE)
				return parent.createDoubleChild(Database.getNameIndex().addName(name), Double.parseDouble(value));
			else if (type == PrimitiveTypeKind.DATE_TYPE)
				try
				{
					return parent.createDateChild(Database.getNameIndex().addName(name), DateUtils.parseDatetime(value));
				}
				catch (ParseException exc)
				{
					if(ConfigServer.DEBUG_EXCEPTIONS)
						exc.printStackTrace();
					
					throw new DatabaseException(exc.getMessage());
				}
			else 
				return parent.createStringChild(Database.getNameIndex().addName(name), value, 0);
		}
		else if (kind == MetaObjectKind.VARIABLE_OBJECT)
		{
			MBObject m = new MBObject(new MBVariable(mo.getOID()).getType());
			return createSimpleObject( m, name, value, parent);
		}
		else if (kind == MetaObjectKind.TYPEDEF_OBJECT)
		{
			MBObject m = new MBObject(new MBTypeDef(mo.getOID()).getType());
			return createSimpleObject( m, name, value, parent);
		}
		return null;
	}
	
	protected OID createValueObject(String name, String value, OID parent, boolean isAttribute) throws DatabaseException, FilterException
	{
		ConfigServer.getLogWriter().getLogger().finest(" CREATE VALUE " + name + " " + value + "  ATTR:" + isAttribute + " Parent: " + parent.getObjectName() );

		MBObject mo = metabaseScope.peek();
		if (isAttribute)
			mo = findFirstMetaObjectInside(mo, parent.getObjectName());
			
		OID result = createSimpleObject(mo, name, value, parent);
		if (result == null)
		{
				MBObject valueObject = findFirstMetaObjectInside( mo, name );
				result = createSimpleObject( valueObject, name, value, parent);
		}
		return result;
	}
	
	private String getTypeName(Element element)
	{
		String typeAttr = element.getAttributeValue("type", "http://www.w3.org/2001/XMLSchema-instance");
		if (typeAttr != null)
			return typeAttr;
		
		return element.getLocalName();
	}
	
	public void openOuterElementScope(Element element) throws DatabaseException
	{
		super.openOuterElementScope(element);
		MBObject parent;
		MBObject metaObject = null; 
		ConfigServer.getLogWriter().getLogger().finest(" OPENING  " + element.getLocalName());
		if (element.getAttributeValue("type", "http://www.w3.org/2001/XMLSchema-instance")!=null)
		{
			ConfigServer.getLogWriter().getLogger().finest("USING TYPEDEF for  " + element.getLocalName());
			parent = new MBObject( module.getMetabaseEntry() );
		}
		else
			parent = metabaseScope.peek();
		
		try {
			metaObject = findFirstMetaObjectInside( parent, getTypeName(element) );
			if (metaObject!=null)
				ConfigServer.getLogWriter().getLogger().finest("IN METABASE FOUND : " + metaObject.getName() );
			else
			{
				ConfigServer.getLogWriter().getLogger().finest(" NOT FOUND : " + element.getLocalName() + " INSIDE " + metabaseScope.peek().getName() );
				return;
			}
			metabaseScope.push( metaObject );
		} catch (DatabaseException e) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Cannot find meta object for: " + element.getLocalName(), e );
		} catch (FilterException e) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Cannot understand meta object structure: ", e );
		}		
	}

	public void closeOuterElementScope(Element element) throws FilterException, DatabaseException {
		super.closeOuterElementScope(element);
		metabaseScope.pop();
	}
	
	@Override
	protected String determineName(String elementName)
	{
		return elementName.substring(elementName.indexOf(":") + 1);
	}
}