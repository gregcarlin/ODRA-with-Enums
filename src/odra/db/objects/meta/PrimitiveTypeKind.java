package odra.db.objects.meta;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class is used as an "enumeration" to describe
 * objectds representing primitive types.
 * 
 * @author raist
 */

public enum PrimitiveTypeKind {
    	INTEGER_TYPE ("integer"),
	REAL_TYPE("real"),
	STRING_TYPE("string"),
	BOOLEAN_TYPE("boolean"),
	VOID_TYPE("void"),
	DATE_TYPE("date");
	
    	private final String externalName;
	PrimitiveTypeKind(String externalName) {
		this.externalName = externalName;
	}
	
	public int kindAsInt() {
		return this.ordinal();
	}
	public static PrimitiveTypeKind getForExternalName(String name){
	    return skinds.get(name);
	}
	
	public static PrimitiveTypeKind getForInteger(int id){
	    return ikinds.get(id);
	}
	private final static Map<String,  PrimitiveTypeKind> skinds = new TreeMap<String,PrimitiveTypeKind>();
	private final static Map<Integer,  PrimitiveTypeKind> ikinds = new TreeMap<Integer,PrimitiveTypeKind>();
	
	static{
	  for(PrimitiveTypeKind kind : PrimitiveTypeKind.values()){
	      skinds.put(kind.externalName, kind);
	      ikinds.put(kind.ordinal(), kind);
	  }
	}
}
