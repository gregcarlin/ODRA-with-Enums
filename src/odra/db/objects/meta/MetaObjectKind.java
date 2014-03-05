package odra.db.objects.meta;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class is used as an "enumeration" to describe
 * kinds of complex metabase objects.
 *
 * @author raist
 */

public enum MetaObjectKind {
	UNKNOWN_OBJECT(100, "UNKNOWN"),
	PRIMITIVE_TYPE_OBJECT(101, "PRIMITIVE_TYPE"), 
	STRUCT_OBJECT(102, "STRUCT"), 
	VARIABLE_OBJECT(103, "VARIABLE"), 
	TYPEDEF_OBJECT(104, "TYPEDEF"), 
	CLASS_OBJECT(105, "CLASS"), 
	PROCEDURE_OBJECT(106, "PROCEDURE"), 
	LINK_OBJECT(107, "LINK"), 
	BINARY_OPERATOR_OBJECT(108, "BINARY_OPERATOR"), 
	UNARY_OPERATOR_OBJECT(109, "UNARY_OPERATOR"),
	VIEW_OBJECT(110, "VIEW"), 
	VIRTUAL_VARIABLE_OBJECT(111, "VIRTUAL_VARIABLE"),
	INDEX_OBJECT(112, "INDEX"),
	ENDPOINT_OBJECT(113, "ENDPOINT"), 
	ANNOTATED_VARIABLE_OBJECT(114, "ANNOTATED_VARIABLE"),
	INTERFACE_OBJECT(115, "INTERFACE"),
	SCHEMA_OBJECT(116, "SCHEMA"),
	ENUM_OBJECT(117, "ENUM");

	private int kindValue;
	private String name;

	MetaObjectKind(int value, String name) {
		this.kindValue = value;
		this.name = name;
	}

	public int kindAsInt() {
		return kindValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	public static MetaObjectKind getKindForInteger(int value) {
		return kinds.get(value);
	}

	private final static Map<Integer, MetaObjectKind> kinds = new TreeMap<Integer, MetaObjectKind>();

	static {
		for (MetaObjectKind kind : MetaObjectKind.values()) {
			kinds.put(kind.kindValue, kind);
		}

	}
}
