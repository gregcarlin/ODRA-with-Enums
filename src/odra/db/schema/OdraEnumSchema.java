package odra.db.schema;

/**
 * OdraEnumSchema
 * Transfer object to convey the information about 
 * Enum in the store independent format
 * @author blejam
 */
public class OdraEnumSchema extends OdraObjectSchema{
	
	public OdraEnumSchema(String ename,String tname,int ref) {
		super(ename);
	}	

}
