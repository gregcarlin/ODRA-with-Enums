package odra.ws.endpoints.wsdl;

import java.util.ArrayList;
import java.util.List;

import odra.ws.bindings.IBindingProvider;
import odra.ws.bindings.IBindingsAware;
import odra.ws.endpoints.WSEndpointOptions;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.ITypeMapperAware;
/** Builder for WSDL contracts abstraction
 * 
 * @since 2007-06-20
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public abstract class WSDLBuilder implements IBindingsAware, ITypeMapperAware {
	// bindings
	protected List<IBindingProvider> bindingProviders = new ArrayList<IBindingProvider>();
	protected ITypeMapper typeMapper = null;

	/* (non-Javadoc)
	 * @see odra.bridges.wsdl.IBindingsAware#addBinding(odra.bridges.wsdl.IBindingProvider)
	 */
	public void addBinding(IBindingProvider provider) {
		this.bindingProviders.add(provider);
	}
	
	/* (non-Javadoc)
	 * @see odra.bridges.type.mappers.ITypeMapperAware#setMapper(odra.bridges.type.mappers.ITypeMapper)
	 */
	public void setMapper(ITypeMapper mapper) {
		this.typeMapper = mapper;
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.ITypeMapperAware#getMapper()
	 */
	public ITypeMapper getMapper() {
		return this.typeMapper;
	}
	
	/** Creates WSDL contract definition 
	 * @param options
	 * @return
	 * @throws WSDLBuilderException
	 */
	public abstract String createDefinition(WSEndpointOptions options) throws WSDLBuilderException;

}
