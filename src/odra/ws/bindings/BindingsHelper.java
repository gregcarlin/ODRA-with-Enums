package odra.ws.bindings;

import odra.ws.facade.WSBindingType;

/**
 * Helper methods for bindings creation and manipulation
 * 
 * @since 2007-06-20
 * @version  2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class BindingsHelper {
	/** Injects requested bindings 
	 * @param object Object to inject into
	 * @param bindings Bindings to inject
	 */
	public static void injectBindings(IBindingsAware object, WSBindingType ... bindings) {
		for (WSBindingType b : bindings) {
			IBindingProvider provider = BindingFactory.createProvider(b);
			object.addBinding(provider);
		}
	}
}
