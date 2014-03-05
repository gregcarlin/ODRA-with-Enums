/**
 * 
 */
package odra.store.sbastore;

import java.util.Map;
import java.util.TreeMap;


/**
 * ODRAObjectKind
 * representing native ODRA object kinds
 * refactorized from ObjectManager static fields
 * @author Radek Adamus
 *@since 2007-10-07
 *last modified: 2007-10-07
 *@version 1.0
 */
public enum ODRAObjectKind {
    	UNKNOWN_OBJECT(0),
    	 STRING_OBJECT(1), // object kinds
	 INTEGER_OBJECT(2),
	 DOUBLE_OBJECT(3),
	 BOOLEAN_OBJECT(4),
	 COMPLEX_OBJECT(5),
	 REFERENCE_OBJECT(6),
	 BINARY_OBJECT(7),
	 AGGREGATE_OBJECT(8),
	 POINTER_OBJECT(9),
	 DATE_OBJECT(10),
	 REVERSE_REFERENCE_OBJECT(11);
	 
    	 private int kind;
    	 private ODRAObjectKind(int kindID){
    	     this.kind = kindID;
    	 }
	 public byte getKindAsByte(){
	  return (byte)kind;   
	 }
	 public static final ODRAObjectKind getForByte(byte kind){
	     return kinds.get(kind);
	 }
	 private final static Map<Byte,  ODRAObjectKind> kinds = new TreeMap<Byte,ODRAObjectKind>();
	 
	 static{
	     for(ODRAObjectKind kind : ODRAObjectKind.values()){
		 kinds.put((byte)kind.kind, kind);
	     }
	 }
}
