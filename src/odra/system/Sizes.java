package odra.system;

/**
 * @author raist
 */
public class Sizes {
	// Length (in bytes) of particular primitive data types
	public final static int INTVAL_LEN = Integer.SIZE >> 3; 
	public final static int DOUBLEVAL_LEN = Double.SIZE >> 3;
	public final static int SHORTVAL_LEN = Short.SIZE >> 3;
	public final static int BOOLEAN_LEN = Byte.SIZE >> 3;
	public final static int BYTEVAL_LEN = Byte.SIZE >> 3;
	public final static int LONGVAL_LEN = Long.SIZE >> 3;
}
