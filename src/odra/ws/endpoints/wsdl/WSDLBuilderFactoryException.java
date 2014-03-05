package odra.ws.endpoints.wsdl;
/***
 * Represents errors which occur while WSDL builder dispatching by {@link odra.ws.endpoints.wsdl.WSDLBuilderFactory}
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-24
 * @since 2006-12-24
 *
 */
public class WSDLBuilderFactoryException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4524629506429219456L;

	public WSDLBuilderFactoryException(String msg) {
		super(msg); 
	}
	
	public WSDLBuilderFactoryException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public WSDLBuilderFactoryException(Throwable inner)
	{
		super(inner);
	}
}
