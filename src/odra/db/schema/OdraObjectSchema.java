/**
 * 
 */
package odra.db.schema;

/**
 * SchemaObjectInfo
 * @author Radek Adamus
 *@since 2008-04-25
 *last modified: 2008-04-25
 *@version 1.0
 */
public class OdraObjectSchema {
    private String name;

    /**
     * @param name
     */
    OdraObjectSchema() {	
    }
    /**
     * @param name
     */
    OdraObjectSchema(String name) {
	this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
