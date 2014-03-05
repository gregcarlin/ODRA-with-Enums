/**
 * 
 */
package odra.db.objects.meta;

import odra.AssemblyInfo;
import odra.exceptions.OdraRuntimeException;

/**
 * DatabaseSchemaException
 * @author Radek Adamus
 *@since 2008-04-30
 *last modified: 2008-04-30
 *@version 1.0
 */
public class SchemaException extends OdraRuntimeException {

    /**
     * @param assemblyInfo
     * @param originClass
     * @param key
     * @param details
     * @param cause
     */
    public SchemaException(AssemblyInfo assemblyInfo,
	    Class originClass, String key, String details, Throwable cause) {
	super(assemblyInfo, originClass, key, details, cause);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param assemblyInfo
     * @param originClass
     * @param key
     * @param details
     */
    public SchemaException(AssemblyInfo assemblyInfo,
	    Class originClass, String key, String details) {
	super(assemblyInfo, originClass, key, details);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param assemblyInfo
     * @param originClass
     * @param key
     * @param cause
     */
    public SchemaException(AssemblyInfo assemblyInfo,
	    Class originClass, String key, Throwable cause) {
	super(assemblyInfo, originClass, key, cause);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param assemblyInfo
     * @param originClass
     * @param key
     */
    public SchemaException(AssemblyInfo assemblyInfo,
	    Class originClass, String key) {
	super(assemblyInfo, originClass, key);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public SchemaException(String message) {
	super(message);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public SchemaException(String message, Throwable cause) {
	super(message, cause);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     */
    public SchemaException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

}
