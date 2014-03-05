package odra.sbql.results.compiletime.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author janek
 *
 */
public enum SignatureKind
{	
	ValueSignature, BinderSignature, ReferenceSignature, StructSignature;
	
	
	private final static Map<Integer, SignatureKind> ikinds = new TreeMap<Integer, SignatureKind>();

	public int kindAsInt()
	{
		return this.ordinal();
	}

	public static SignatureKind getForInteger(int id)
	{
		return ikinds.get(id);
	}

	static
	{
		for (SignatureKind kind : SignatureKind.values())
		{
			ikinds.put(kind.ordinal(), kind);
		}
	}
}
