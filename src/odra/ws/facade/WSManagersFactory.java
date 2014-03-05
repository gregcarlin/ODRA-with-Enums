package odra.ws.facade;

import odra.system.config.ConfigServer;
/** Resposible for creating instances of proxy and endpoint facade realizations.
 * Depends on @see Config configuration class.
 * 
 * @version 2007-06-24
 * @since 2006-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSManagersFactory {

	private static IProxyFacade proxyManager = null;
	private static IEndpointFacade endpointManager = null;
	/** Creates proxy manager facade
	 * @return
	 */
	public static IProxyFacade createProxyManager() {
		if (!ConfigServer.WS) {
			return null;
		}
		
		if (proxyManager == null) {
			try {
				proxyManager = (IProxyFacade) Class.forName("odra.ws.proxies.WSProxyManager").newInstance();
			} catch (InstantiationException e) {
				
			} catch (IllegalAccessException e) {
				
			} catch (ClassNotFoundException e) {
				
			}
		}
		return proxyManager;
		
	}
	
	/** Creates endpoint manager facade
	 * @return
	 */
	public static IEndpointFacade createEndpointManager() {
		if (!ConfigServer.WS) {
			return null;
		}
		
		if (endpointManager == null) {
			try {
				endpointManager = (IEndpointFacade) Class.forName("odra.ws.endpoints.WSEndpointManager").newInstance();
			} catch (InstantiationException e) {
				
			} catch (IllegalAccessException e) {
				
			} catch (ClassNotFoundException e) {
				
			}
			
		}

		return endpointManager;
	
	}
}
