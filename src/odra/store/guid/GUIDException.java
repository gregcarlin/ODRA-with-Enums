package odra.store.guid;

import odra.exceptions.OdraCoreException;

public class GUIDException extends OdraCoreException {

	public GUIDException(String message) {
		super(message);
	}

	public GUIDException(String message, Throwable ex) {
		super(message, ex);
	}
}