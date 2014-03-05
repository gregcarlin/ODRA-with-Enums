package odra.exceptions;

import odra.AssemblyInfo;

public abstract class OdraException extends Exception {

	private final static long serialVersionUID = -5408822550621848471L;

	protected OdraException(AssemblyInfo assemblyInfo, Class originClass, String key, String details, Throwable cause) {
		super(getExceptionMessageWithDetails(assemblyInfo, originClass, key, details), cause);
	}

	protected OdraException(AssemblyInfo assemblyInfo, Class originClass, String key, String details) {
		super(getExceptionMessageWithDetails(assemblyInfo, originClass, key, details));
	}

	protected OdraException(AssemblyInfo assemblyInfo, Class originClass, String key, Throwable cause) {
		super(getExceptionMessage(assemblyInfo, originClass, key), cause);
	}

	protected OdraException(AssemblyInfo assemblyInfo, Class originClass, String key) {
		super(getExceptionMessage(assemblyInfo, originClass, key));
	}

	protected OdraException(String message) {
		super(message);
	}

	protected OdraException(String message, Throwable cause) {
		super(message, cause);
	}

	protected OdraException(Throwable ex) {
		super(ex);
	}

	protected static String getExceptionMessageWithDetails(AssemblyInfo assemblyInfo, Class originClass, String key,
				String details) {
		return assemblyInfo.getLocalizedMessage(originClass, key, details);
	}

	protected static String getExceptionMessage(AssemblyInfo assemblyInfo, Class originClass, String key) {
		return assemblyInfo.getLocalizedMessage(originClass, key);
	}
}