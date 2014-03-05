package odra.ws.endpoints.wsdl;

import odra.exceptions.OdraException;



/***
 * Represents errors which occur while WSDL contract is built
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-23
 * @since 2006-12-24
 *
 */
public class WSDLBuilderException extends OdraException {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 108164373237721087L;

	public WSDLBuilderException(String msg) {
		super(msg); 
	}
	
	public WSDLBuilderException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public WSDLBuilderException(Throwable inner)
	{
		super(inner);
	}
	
	
}
