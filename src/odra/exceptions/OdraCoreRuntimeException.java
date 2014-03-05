package odra.exceptions;

import odra.OdraCoreAssemblyInfo;

/**
 * The root of ODRA runtime exception hierarchy.
 * 
 * @author edek
 */
public class OdraCoreRuntimeException extends OdraRuntimeException {

	private final static OdraCoreAssemblyInfo assemblyInfo = OdraCoreAssemblyInfo.getInstance();

	public OdraCoreRuntimeException(String message) {
		super(message);
	}

	public OdraCoreRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public OdraCoreRuntimeException(Throwable ex) {
		super(ex);
	}

	public OdraCoreRuntimeException(Class c, String key, String details) {
		super(assemblyInfo, c, key, details);
	}

	public OdraCoreRuntimeException(Class c, String key, String details, Throwable ex) {
		super(assemblyInfo, c, key, details, ex);
	}

	public OdraCoreRuntimeException(Class c, String key) {
		super(assemblyInfo, c, key);
	}

	public OdraCoreRuntimeException(Class c, String key, Throwable ex) {
		super(assemblyInfo, c, key, ex);
	}
}