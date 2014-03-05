package odra.ws.bindings.soap;

import odra.exceptions.OdraException;



/**
 * Represents binding provider errors.
 * 
 * @since 2006-12-24
 * @version 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public class BindingProviderException extends OdraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6197292650630454251L;

	public BindingProviderException(String msg) {
		super(msg);
	}
	

	public BindingProviderException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public BindingProviderException(Throwable inner)
	{
		super(inner);
	}


}
