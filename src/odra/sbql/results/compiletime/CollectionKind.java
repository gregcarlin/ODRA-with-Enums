/**
 * 
 */
package odra.sbql.results.compiletime;

/**
 * This class is used as an "enumeration" to describe
 * types of collections used by the type checker.
 *
 * @author stencel
 */
public class CollectionKind {
	private int kind;
	
	/**
	 * Initializes an object which helds the given collection kind.
	 * @param kind is the integer coding the kind of collection.
	 */
	public CollectionKind (int kind) {
		this.kind = kind;
	}
	
	/**
	 * Returns the integer coding the kind of collection.
	 * @return the integer coding the kind of collection. 
	 */
	public int kindAsInteger() {
		return kind;
	}

	/**
	 * Returns the string representing the kind of collection.
	 * @return the string representing the kind of collection. 
	 */
	public String kindAsString() {
		switch(kind) {
 			case NONE_COLLECTION 		: return "none";
 			case BAG_COLLECTION 		: return "bag";
 			case SEQUENCE_COLLECTION 	: return "seq";
 			default						: return "unknown"; // should not happen
		}
	}

	public final static int NONE_COLLECTION = 0;
	public final static int BAG_COLLECTION = 1;
	public final static int SEQUENCE_COLLECTION = 2;
}
