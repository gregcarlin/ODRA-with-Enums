package odra.ws.type.mappers;

import odra.exceptions.OdraRuntimeException;

/***
 * Represents not implemented feature error.
 * Used in interfaces wrapping classes.
 * 
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-24
 * @since 2007-06-16
 *
 */
public class NotImplementedException extends OdraRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6232734193595430060L;

	public NotImplementedException() {
		super("unknown");
	}
	
	public NotImplementedException(String message) {
		super(message);
	}
	
	public NotImplementedException(Throwable thrown) {
		super(thrown);
	}
	
	public NotImplementedException(String message, Throwable thrown) {
		super(message, thrown);
	}
	
}
