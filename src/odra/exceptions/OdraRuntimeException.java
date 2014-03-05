package odra.exceptions;

import odra.AssemblyInfo;

public abstract class OdraRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 4704419011160517430L;

	protected OdraRuntimeException(AssemblyInfo assemblyInfo, Class originClass, String key, String details,
				Throwable cause) {
		super(OdraException.getExceptionMessageWithDetails(assemblyInfo, originClass, key, details), cause);
	}

	protected OdraRuntimeException(AssemblyInfo assemblyInfo, Class originClass, String key, String details) {
		super(OdraException.getExceptionMessageWithDetails(assemblyInfo, originClass, key, details));
	}

	protected OdraRuntimeException(AssemblyInfo assemblyInfo, Class originClass, String key, Throwable cause) {
		super(OdraException.getExceptionMessage(assemblyInfo, originClass, key), cause);
	}

	protected OdraRuntimeException(AssemblyInfo assemblyInfo, Class originClass, String key) {
		super(OdraException.getExceptionMessage(assemblyInfo, originClass, key));
	}

	protected OdraRuntimeException(String message) {
		super(message);
	}

	protected OdraRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	protected OdraRuntimeException(Throwable ex) {
		super(ex);
	}
}