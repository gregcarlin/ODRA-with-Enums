package odra.exceptions;

import odra.OdraCoreAssemblyInfo;

/**
 * The root of ODRA exception hierarchy.
 * 
 * @author edek
 */
public class OdraCoreException extends OdraException {

	protected final static OdraCoreAssemblyInfo assemblyInfo = OdraCoreAssemblyInfo.getInstance();

	public OdraCoreException(String message) {
		super(message);
	}

	public OdraCoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public OdraCoreException(Throwable ex) {
		super(ex);
	}

	public OdraCoreException(Class c, String key, String details) {
		super(assemblyInfo, c, key, details);
	}

	public OdraCoreException(Class c, String key, String details, Throwable ex) {
		super(assemblyInfo, c, key, details, ex);
	}

	public OdraCoreException(Class c, String key) {
		super(assemblyInfo, c, key);
	}

	public OdraCoreException(Class c, String key, Throwable ex) {
		super(assemblyInfo, c, key, ex);
	}
}