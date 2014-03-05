package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for metabase objects
 * representing unary operators.
 *  
 * @author raist
 */

public class MBUnaryOperator extends MBObject {
	/**
	 * Initializes a new MBUnaryOperator object.
	 * @param oid oid of the object of an existing unary operator (or an empty complex object)
	 */
	public MBUnaryOperator(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the binary object declaration in the metabase.
	 * @param terminal textual representation of the operator
	 * @param restype OID of the primitive type being a result of the operator
	 * @param type OID of the primitive type being an argument of the operator
	 * @param opcode opcode being a binary code equivalent of the operator
	 */
	public void initialize(String terminal, OID restype, OID type, int opcode) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.UNARY_OPERATOR_OBJECT.kindAsInt());
		store.createStringObject(store.addName("$terminal"), oid, terminal, 0);
		store.createPointerObject(store.addName("$restype"), oid, restype);
		store.createPointerObject(store.addName("$argtype"), oid, type);
		store.createIntegerObject(store.addName("$opcode"), oid, opcode);
	}

	/**
	 * @return true if the oid really represent a metavariable
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.UNARY_OPERATOR_OBJECT;
	}
	
	/**
	 * @return textual representation of the operator
	 */
	public String getTerminal() throws DatabaseException {
		return getTerminalRef().derefString();
	}
	
	/**
	 * @return OID of the primitive type being the result of the operator
	 */
	public OID getResultType() throws DatabaseException {
		return getResTypeRef().derefReference();
	}
	
	/**
	 * @return OID of the primitive type being the argument of the operator
	 */
	public OID getArgType() throws DatabaseException {
		return getArgTypeRef().derefReference();
	}
	
	/**
	 * @return binary code equivalent of the operator
	 */
	public int getOpCode() throws DatabaseException {
		return getOpCodeRef().derefInt();
	}

	/***********************************
	 * debugging
	 * */

	public String dump(String indend) throws DatabaseException {
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();

		String terminal = oid.getChildAt(TERMINAL_POS).derefString();
		String type = getArgType() == null ? "?" : new MBPrimitiveType(getArgType()).getName();
		
		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
		metastr += " " + terminal + " " + type;
		metastr += ", opcode: " + getOpCode();
		metastr += " [simple unary operator]\n";

		return metastr;
	}
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getTerminalRef() throws DatabaseException {
		return oid.getChildAt(TERMINAL_POS);
	}
	
	private final OID getResTypeRef() throws DatabaseException {
		return oid.getChildAt(RESTYPE_POS);
	}

	private final OID getArgTypeRef() throws DatabaseException {
		return oid.getChildAt(ARGTYPE_POS);
	}

	private final OID getOpCodeRef() throws DatabaseException {
		return oid.getChildAt(OPCODE_POS);
	}

	private final static int TERMINAL_POS = 1;
	private final static int RESTYPE_POS = 2;
	private final static int ARGTYPE_POS = 3;
	private final static int OPCODE_POS = 4;

	public final static int FIELD_COUNT = 5;
}
