package odra.sbql.results.compiletime.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * Types of ValueSignature's
 * 
 * @author janek
 * 
 */
public enum ValueSignatureType {
	STRING_TYPE, INTEGER_TYPE, BOOLEAN_TYPE, REAL_TYPE, DATE_TYPE;

	private final static Map<Integer, ValueSignatureType> ikinds = new TreeMap<Integer, ValueSignatureType>();

	public int kindAsInt()
	{
		return this.ordinal();
	}

	public static ValueSignatureType getForInteger(int id)
	{
		return ikinds.get(id);
	}

	static
	{
		for (ValueSignatureType kind : ValueSignatureType.values())
		{
			ikinds.put(kind.ordinal(), kind);
		}
	}
}
