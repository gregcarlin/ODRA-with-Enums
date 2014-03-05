package odra.ws.type.constructors;

import odra.db.DatabaseException;

/***
* Represents errors, which occur during type construction (in XSD -> Odra Meta Schema mapping)
* @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
* @version 2007-06-23
* @since 2007-06-16
*
*/
public class TypeConstructorException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1435564220632140262L;
	
	public TypeConstructorException(String message) {
		super(message);
	}
	
	public TypeConstructorException(String message, Throwable thrown) {
		super(message, thrown);
	}
}
