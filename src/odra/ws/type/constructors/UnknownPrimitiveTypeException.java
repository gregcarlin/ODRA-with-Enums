package odra.ws.type.constructors;



/***
 * Represents type mapper error about unsupported primitive type
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-23
 * @since 2007-06-16
 *
 */
public class UnknownPrimitiveTypeException extends TypeConstructorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5116084403466202682L;

	public UnknownPrimitiveTypeException(String message) {
		super(message);
	}
	
	public UnknownPrimitiveTypeException(String message, Throwable thrown) {
		super(message, thrown);
	}
}
