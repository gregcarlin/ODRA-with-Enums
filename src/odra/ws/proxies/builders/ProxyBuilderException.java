package odra.ws.proxies.builders;

import odra.exceptions.OdraException;


/***
 * Represents errors which occur when module proxy stub is built
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-23
 * @since 2006-12-24
 *
 */
public class ProxyBuilderException extends OdraException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4761843899339372596L;

	public ProxyBuilderException(String msg) {
		super(msg); 
	}
	
	public ProxyBuilderException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public ProxyBuilderException(Throwable inner)
	{
		super(inner);
	}
	
}
