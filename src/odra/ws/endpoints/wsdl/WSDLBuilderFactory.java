package odra.ws.endpoints.wsdl;

import odra.db.objects.meta.MBObject;
import odra.helpers.DatabaseHelper;
import odra.ws.bindings.BindingsHelper;
import odra.ws.endpoints.WSClassEndpoint;
import odra.ws.endpoints.WSEndpoint;
import odra.ws.endpoints.WSGenericEndpoint;
import odra.ws.endpoints.WSProcEndpoint;
import odra.ws.facade.Config;

/** Factory resposible for creation and proper initialization of all supported kinds of builders.
 * 
 * @since 2007-06-22
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSDLBuilderFactory {
	/** Creates and initializes requested contract builder
	 * @param endpoint
	 * @param mbObject
	 * @return
	 * @throws WSDLBuilderFactoryException
	 */
	public static WSDLBuilder create(WSEndpoint endpoint, MBObject mbObject) throws WSDLBuilderFactoryException {
		try {
			WSDLBuilder builder = null;
			// dispatch	(in next version consider dependency injection first)
			if (endpoint instanceof WSGenericEndpoint) {
				builder = new GenericWSDLBuilder();
				
				
			} else if (endpoint instanceof WSProcEndpoint) {
				ProcWSDLBuilder tmp = new ProcWSDLBuilder();
				tmp.setObjectContext(mbObject);
				builder = tmp;
				
			} else if (endpoint instanceof WSClassEndpoint) {
				ClassWSDLBuilder tmp = new ClassWSDLBuilder();
				tmp.setObjectContext(mbObject);
				builder = tmp;
				
			} else {
				throw new WSDLBuilderFactoryException(String.format("Error while building wsdl contract - dispatcher could not find handler for '%s' ", DatabaseHelper.getName(mbObject)));
		
			}
			
			BindingsHelper.injectBindings(builder, Config.WS_BINDINGS);
			builder.setMapper(endpoint.getMapper());
			return builder;
			
		} catch (WSDLBuilderException ex) {
			throw new WSDLBuilderFactoryException(ex);
			
		}
	}
	
}
