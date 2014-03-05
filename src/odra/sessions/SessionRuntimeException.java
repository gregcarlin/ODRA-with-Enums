package odra.sessions;

import odra.exceptions.OdraCoreRuntimeException;

public class SessionRuntimeException extends OdraCoreRuntimeException {

	public SessionRuntimeException(String message) {
		super(message);
	}

	public SessionRuntimeException(String message, Throwable ex) {
		super(message, ex);
	}
}