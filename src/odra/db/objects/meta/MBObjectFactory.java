package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;

/**
 * MBObjectFactory - utility class with a factory method 
 * getTypedMBObject returning (typed) representation of ODRA meta-object 
 * as a proper java class  
 * @author Radek Adamus
 *last modified: 2007-01-22
 *@version 1.0
 */
public class MBObjectFactory {
	
	/**
	 * @param moid - oid of the meta object
	 * @return meta-object with a proper type 
	 * @see MetaObjectKind.java
	 * @see MBObject inheritance tree 
	 */
	public static final MBObject getTypedMBObject(OID moid) throws DatabaseException{
		
		switch (new MBObject(moid).getObjectKind()){
		case PRIMITIVE_TYPE_OBJECT:	
			return new MBPrimitiveType(moid);
		case STRUCT_OBJECT:
			return new MBStruct(moid);
		case TYPEDEF_OBJECT:
			return new MBTypeDef(moid);
		case VARIABLE_OBJECT:
			return new MBVariable(moid);
		case CLASS_OBJECT:
			return new MBClass(moid);
		case VIEW_OBJECT:
			return new MBView(moid);
		case VIRTUAL_VARIABLE_OBJECT:
			return new MBVirtualVariable(moid);
		case INDEX_OBJECT:
			return new MBIndex(moid);
		case ENDPOINT_OBJECT:
			return new MBEndpoint(moid);
		case PROCEDURE_OBJECT:
			return new MBProcedure(moid);
		case BINARY_OPERATOR_OBJECT:
			return new MBBinaryOperator(moid);
		case UNARY_OPERATOR_OBJECT:
			return new MBUnaryOperator(moid);
		case ANNOTATED_VARIABLE_OBJECT:
			return new MBAnnotatedVariableObject(moid);
		case LINK_OBJECT:
		    return new MBLink(moid);
		case SCHEMA_OBJECT:
			return new MBSchema(moid);
		default:
			assert false: "unimplemented meta-object factorization";
			return null;
		}
	}
		
	/**
	 * Please use carefully I am not a meta-expert ;) 
	 * @param moid - oid of the meta object
	 * @return OID of meta-object type for given meta-object 
	 * @see MetaObjectKind.java
	 * @see MBObject inheritance tree 
	 * @author tkowals
	 */
	public static final OID getTypedMBObjectTypeOID(OID moid) throws DatabaseException{
		
		switch (new MBObject(moid).getObjectKind()){
		case TYPEDEF_OBJECT:
			return new MBTypeDef(moid).getType();
		case VARIABLE_OBJECT:
			return new MBVariable(moid).getType();
		case CLASS_OBJECT:
			return new MBClass(moid).getType();
		case VIRTUAL_VARIABLE_OBJECT:
			return new MBVirtualVariable(moid).getType();
		case INDEX_OBJECT:
			return new MBIndex(moid).getType();
		case PROCEDURE_OBJECT:
			return new MBProcedure(moid).getType();
		case VIEW_OBJECT:
		case ENDPOINT_OBJECT:
		case BINARY_OPERATOR_OBJECT:
		case UNARY_OPERATOR_OBJECT:
		case ANNOTATED_VARIABLE_OBJECT:
		case PRIMITIVE_TYPE_OBJECT:	
		case STRUCT_OBJECT:
			assert false: "meta-object has no type";
			return null;
		default:
			assert false: "unimplemented meta-object factorization";
			return null;
			
		}
	}
}
