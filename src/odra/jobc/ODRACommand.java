/**
 * 
 */
package odra.jobc;

import odra.network.transport.DBRequest;

/**
 * ODRACommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public interface ODRACommand {
	/**
	 * @return the ODRA server DBRequest  
	 */
	public DBRequest getRequest(String moduleName);
}
