package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations on metabase objects representing
 * binary operators.
 * 
 * @author raist
 */

public class MBBinaryOperator extends MBObject {
	/**
	 * Initializes a new MBBinaryOperator object.
	 * @param oid existing binary operator or an empty complex object
	 */
	public MBBinaryOperator(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the binary object declaration in the metabase.
	 * @param terminal textual representation of the operator (e.g. "+")
	 * @param resType OID of a primitive type object representing the result of this object
	 * @param leftType OID of a primitive type object representing the type of the left operand
	 * @param rightType OID of a primitive type object representing the type of the right operand
	 * @param leftCoercionType OID of a primitive type to which the left operand should be coerced before the operator can be applied
	 * @param rightCoercionType OID of a primitive type to which the right operand should be coerced before the operator can be applied 
	 * @param opcode binary code equivalent of the operator
	 */
	public void initialize(String terminal, OID resType, OID leftType, OID rightType, OID leftCoercionType, OID rightCoercionType, int opcode) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.BINARY_OPERATOR_OBJECT.kindAsInt());
		store.createStringObject(store.addName("$terminal"), oid, terminal, 0);
		store.createPointerObject(store.addName("$restype"), oid, resType);
		store.createPointerObject(store.addName("$ltype"), oid, leftType);
		store.createPointerObject(store.addName("$rtype"), oid, rightType);
		store.createPointerObject(store.addName("$lcoercion"), oid, leftCoercionType);
		store.createPointerObject(store.addName("$rcoercion"), oid, rightCoercionType);
		store.createIntegerObject(store.addName("$opcode"), oid, opcode);
	}

	/**
	 * @return true if the object is really a metavariable
	 */
	public boolean isValid() throws DatabaseException {
	    return getObjectKind() == MetaObjectKind.BINARY_OPERATOR_OBJECT;
	}

	/**
	 * @return textual representation of the operator
	 */	
	public String getTerminal() throws DatabaseException {
		return getTerminalRef().derefString();
	}

	/**
	 * @return type of the value returned by this operator
	 */	
	public OID getResultType() throws DatabaseException {
		return getResultTypeRef().derefReference();
	}

	/**
	 * @return type of the left argument of this operator
	 */	
	public OID getLeftType() throws DatabaseException {
		return getLeftTypeRef().derefReference();
	}

	/**
	 * @return type of the right argument of this operator
	 */	
	public OID getRightType() throws DatabaseException {
		return getRightTypeRef().derefReference();
	}

	/**
	 * @return type to which the left operand should be coerced before the operator is applied
	 */	
	public OID getLeftCoercion() throws DatabaseException {
		return getLeftCoercionRef().derefReference();
	}
	
	/**
	 * @return type to which the right operand should be coerced before the operator is applied
	 */	
	public OID getRightCoercion() throws DatabaseException {
		return getRightCoercionRef().derefReference();
	}
	
	/**
	 * @return binary code representation of the operator
	 */
	public int getOpCode() throws DatabaseException {
		return getOpCodeRef().derefInt();
	}
	
	/***********************************
	 * debugging
	 * */

	public String dump(String indend) throws DatabaseException {
		String terminal = oid.getChildAt(TERMINAL_POS).derefString();
		
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();

		String left = getLeftType() != null ? new MBPrimitiveType(getLeftType()).getName() : "?";
		String right = getLeftType() != null ? new MBPrimitiveType(getRightType()).getName() : "?";
		String leftc = getLeftCoercion() != null ? new MBPrimitiveType(getLeftCoercion()).getName() : "no"; 
		String rightc = getRightCoercion() != null ? new MBPrimitiveType(getRightCoercion()).getName() : "no";

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
		metastr += " " + left + " " + terminal + " " + right;
		metastr += ", left coercion: " + leftc + ", right coercion: " + rightc;
		metastr += ", opcode: " + getOpCode();
		metastr += " [simple binary operator]\n";

		return metastr;
	}
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getTerminalRef() throws DatabaseException {
		return oid.getChildAt(TERMINAL_POS);
	}
	
	private final OID getResultTypeRef() throws DatabaseException {
		return oid.getChildAt(RESTYPE_POS);
	}

	private final OID getLeftTypeRef() throws DatabaseException {
		return oid.getChildAt(LTYPE_POS);
	}
	
	private final OID getRightTypeRef() throws DatabaseException {
		return oid.getChildAt(RTYPE_POS);
	}

	private final OID getLeftCoercionRef() throws DatabaseException {
		return oid.getChildAt(LCOERCION_POS);
	}

	private final OID getRightCoercionRef() throws DatabaseException {
		return oid.getChildAt(RCOERCION_POS);
	}
	
	private final OID getOpCodeRef() throws DatabaseException {
		return oid.getChildAt(OPCODE_POS);
	}
	
	private final static int TERMINAL_POS = 1;
	private final static int RESTYPE_POS = 2;
	private final static int LTYPE_POS = 3;
	private final static int RTYPE_POS = 4;
	private final static int LCOERCION_POS = 5;
	private final static int RCOERCION_POS = 6;
	private final static int OPCODE_POS = 7;
	
	public final static int FIELD_COUNT = 8;
}
