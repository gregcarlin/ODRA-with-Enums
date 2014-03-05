package odra.store.guid;

import odra.exceptions.OdraCoreRuntimeException;

public final class GUIDRuntimeException extends OdraCoreRuntimeException {

	public GUIDRuntimeException(String message) {
		super(message);
	}

	public GUIDRuntimeException(String message, Throwable ex) {
		super(message, ex);
	}
}