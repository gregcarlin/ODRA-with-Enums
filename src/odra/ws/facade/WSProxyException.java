package odra.ws.facade;

import odra.exceptions.OdraException;


/***
 * Represents generic proxy error
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-23
 * @since 2007-03-17
 *
 */
public class WSProxyException extends OdraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1893993589021099124L;

	public WSProxyException(String msg) {
		super(msg);		
	}
	
	public WSProxyException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public WSProxyException(Throwable inner)
	{
		super(inner);
	}

}
