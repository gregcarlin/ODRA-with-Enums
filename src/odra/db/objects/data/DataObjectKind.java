package odra.db.objects.data;

/**
 * This class is used as an "enumeration" to describe
 * kinds of complex database objects.
 *
 * @author raist
 */

public class DataObjectKind {
	private int kind;

	public DataObjectKind(int kind) {
		this.kind = kind;
	}

	public int getKindAsInt() {
		return kind;
	}

	public String getKindAsString() {
		return kindstr[kind];
	}

	private final static String[] kindstr = {
		"?",
		"D",
		"P",
		"C",
		"M",
		"V",
		"VP",
		"HI",
		"E",
		"PR",
		"MB",
		"I",
		"L",
		"S",
		"AS",
		"AB",
		"AI",
		"AR",
		"AC",
		"AP"
	};

	public final static int DATA_OBJECT = 1;
	public final static int PROCEDURE_OBJECT = 2;
	public final static int CLASS_OBJECT = 3;
	public final static int MODULE_OBJECT = 4;
	public final static int VIEW_OBJECT = 5;
	public final static int VIRTUAL_OBJECTS_PROCEDURE_OBJECT = 6;
	public final static int INDEX_OBJECT = 7;
	public final static int ENDPOINT_OBJECT = 8;
	public final static int PROXY_OBJECT = 9;
	public final static int META_BASE_OBJECT = 10;	
	public final static int INTERFACE_OBJECT = 11;
	public final static int LINK_OBJECT = 12; 
	public final static int SCHEMA_OBJECT = 13; 
	public final static int ANNOTATED_STRING_OBJECT = 14; // everything >= ANNOTATED_STRING_OBJECT must be an annotated object
	public final static int ANNOTATED_BOOLEAN_OBJECT = 15;
	public final static int ANNOTATED_INTEGER_OBJECT = 16;
	public final static int ANNOTATED_REAL_OBJECT = 17;
	public final static int ANNOTATED_COMPLEX_OBJECT = 18;
	public final static int ANNOTATED_REFERENCE_OBJECT = 19;
	
	// only annotated objects can be here
}
