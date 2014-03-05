package odra.ws.bindings;


/**
 * Implemented by all components, which need to use bindings internally.
 * 
 * @since 2007-03-25
 * @version  2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 */
public interface IBindingsAware {
	
	/** Injects binding provider
	 * @param provider
	 */
	void addBinding(IBindingProvider provider);
}
