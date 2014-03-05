package odra.filters;

import java.util.EnumSet;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.store.DefaultStoreOID;
import odra.store.sbastore.ObjectManager;

/**
 * This class hides low level kinds of objects. For example usage see dump method. 
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class ShadowObject {

	public enum Kind{ 
		STRING(1, "S_")
		{
			public Object getValue(OID oid)  throws DatabaseException {
				return oid.derefString();
			}
			
			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createStringObject(name, parent, (String)value, 0);
			}

			public Object getValueFromString( String value )
			{
				return value;
			}
		},

		INTEGER(2, "I_") 
		{
			public Object getValue(OID oid)  throws DatabaseException {
				return oid.derefInt();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createIntegerObject(name, parent, (Integer)value);
			}

			public Object getValueFromString( String value )
			{
				return Integer.parseInt(value);
			}
		},
		
		DOUBLE(3, "D_") 
		{
			public Object getValue(OID oid) throws DatabaseException {
				return oid.derefDouble();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createDoubleObject(name, parent, (Double)value);
			}

			public Object getValueFromString( String value )
			{
				return Double.parseDouble(value);
			}

		},
		BOOLEAN(4, "B_") 
		{
			public Object getValue(OID oid) throws DatabaseException {
				return oid.derefBoolean();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createBooleanObject(name, parent, (Boolean)value);
			}

			public Object getValueFromString( String value )
			{
				return Boolean.parseBoolean(value);
			}
		},

		COMPLEX(5, "C_") 
		{
			public Object getValue(OID oid) throws DatabaseException {
				return oid.derefComplex();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createComplexObject(name, parent, 0);
			}

			public Object getValueFromString( String value ) throws ShadowObjectException
			{
				throw new ShadowObjectException( "Cannot create Object from String (" + value + ")", null ); 
			}
		},
			
		REFERENCE(6, "R_") 
		{
			public Object getValue(OID oid) throws DatabaseException {
				return oid.derefReference();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createReferenceObject(name, parent, (OID)value);
			}

			public Object getValueFromString( String value ) throws ShadowObjectException
			{
				throw new ShadowObjectException( "Cannot create Reference from String (" + value + ")", null ); 
			}
		},

		// FIXME: (KK) Find a method to get a value from binary objects and create them.
		BINARY(7, "N_")
		{
			public Object getValue(OID oid) throws DatabaseException {
				return "BINARY OBJECT";
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return null;
			}

			public Object getValueFromString( String value ) throws ShadowObjectException
			{
				throw new ShadowObjectException( "Cannot create Binary objects (" + value + ")", null ); 
			}
		},

		AGGREGATE(8, "A_") 
		{
			public Object getValue(OID oid) throws DatabaseException {
				return oid.derefComplex();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createAggregateObject(name, parent, 0);
			}

			public Object getValueFromString( String value ) throws ShadowObjectException
			{
				throw new ShadowObjectException( "Cannot create Object from String (" + value + ")", null ); 
			}
		},

		POINTER(9, "P_") 
		{
			public Object getValue(OID oid) throws DatabaseException {
				return oid.derefReference();
			}

			public OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException
			{
				return mod.createReferenceObject(name, parent, (OID)value);
			}

			public Object getValueFromString( String value ) throws ShadowObjectException
			{
				throw new ShadowObjectException( "Cannot create Reference from String (" + value + ")", null ); 
			}
		};
		
		public byte code;
		private String stringCode;
		
		Kind( int code, String stringCode ){
			this.code = (byte)code;
			this.stringCode = stringCode;
		}
		
		public abstract Object getValue( OID oid ) throws DatabaseException;
		public abstract OID createObject( DBModule mod, OID parent, String name, Object value ) throws DatabaseException;
		public abstract Object getValueFromString( String value ) throws ShadowObjectException;
		
		public String getObjectNameId(int id, ObjectManager om) throws DatabaseException
		{
			return stringCode + om.getObjectNameId(id);
		}
		
	};

 	public static EnumSet<Kind> ComplexKind = EnumSet.of(Kind.COMPLEX, Kind.AGGREGATE);
 	public static EnumSet<Kind> ReferenceKind = EnumSet.of(Kind.REFERENCE, Kind.POINTER);
 	public static EnumSet<Kind> SimpleKind = EnumSet.of(Kind.STRING, Kind.INTEGER, Kind.BOOLEAN, Kind.DOUBLE);
 	public static EnumSet<Kind> BinaryKind = EnumSet.of(Kind.BINARY);
	
 	
 	
	public static StringBuffer dump( StringBuffer buf, DefaultStoreOID start, ObjectManager om,  String indent ) throws DatabaseException 
	{
		Kind kind = getObjectKind( start );
			
		buf.append(start + "\t\t ");
		
		if ( ComplexKind.contains(kind) )
		{	
			buf.append(indent + start.getObjectName() + "\n");
					
			DefaultStoreOID[] ch = (DefaultStoreOID[]) kind.getValue(start );
			for (int i = 0; i < ch.length; i++)
				dump(buf, ch[i], om, indent + " ");
			
		}
		else if ( SimpleKind.contains(kind) || ReferenceKind.contains(kind) )
		{
			buf.append(indent + start.getObjectName() + " = " + kind.getValue(start) + "\n");
		}
		else //BinaryKind
		{
			buf.append(indent + start.getObjectName() + " = ?" + "\n");
		}
			
		return buf;
	}

	/**
	 * A simple and inefficient wrapper over object kind value. 
	 * Very stupid method but forced by OID construction.
	 * 
	 * FIXME: (KK) Repair OID to be able to return any value (independent of kind) by single method deref()
	 * 
	 * @param oid 
	 * @return
	 * @throws DatabaseException  
	 */
	public static Kind getObjectKind(OID oid) throws DatabaseException
	{
		if (oid.isComplexObject())
			return Kind.COMPLEX;
		else if (oid.isAggregateObject())
			return Kind.AGGREGATE;
		else if (oid.isBooleanObject())
			return Kind.BOOLEAN;
		else if (oid.isDoubleObject())
			return Kind.DOUBLE;
		else if (oid.isIntegerObject())
			return Kind.INTEGER;
		else if (oid.isStringObject())
			return Kind.STRING;
		else if (oid.isBinaryObject())
			return Kind.BINARY;
		else if (oid.isReferenceObject())
			return Kind.REFERENCE;
		else
			throw new DatabaseException("Object " + oid + " of an unknown kind.");
	}
	
}
