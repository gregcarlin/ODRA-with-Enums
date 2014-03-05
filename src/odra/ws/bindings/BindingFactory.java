package odra.ws.bindings;

import odra.ws.bindings.soap.Soap11BindingProvider;
import odra.ws.facade.WSBindingType;

/**
 * Factory for creating binding providers.
 * 
 * @since 2006-12-28
 * @version  2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class BindingFactory {
	
	/** Returns binding provider instance of given type 
	 * It may implement bindings caching in future.
	 * 
	 * @param binding Binding type to create
	 * @return Created binding instance
	 */
	public static IBindingProvider createProvider(WSBindingType binding)  {
		if (binding.equals(WSBindingType.SOAP11)) {
			return new Soap11BindingProvider();
			
		} else {
			throw new RuntimeException("Binding " + binding.getName() + " not supported. ");
			
		}
	}
	
}
