package odra.ws.endpoints;

import odra.exceptions.OdraException;


/***
 * Represents generic endpoint error
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-23
 * @since 2007-03-17
 *
 */
public class WSEndpointException extends OdraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3093752984527846957L;

	public WSEndpointException(String msg) {
		super(msg);
		
	}
	
	public WSEndpointException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public WSEndpointException(Throwable inner)
	{
		super(inner);
	}
	
}
