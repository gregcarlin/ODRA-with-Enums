/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * AddProxyCommand
 * 
 * @author Radek Adamus
 * @since 2008-05-16 last modified: 2008-05-16
 * @version 1.0
 */
public class AddProxyCommand implements DDLCommand {
	
	
	private  String name;
	private String wsdl;

	/**
	 * 
	 */
	public AddProxyCommand(String name, String wsdl) {
		this.name = name;
		this.wsdl = wsdl;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.jobc.ODRACommand#getRequest(java.lang.String)
	 */

	public DBRequest getRequest(String moduleName) {
		return new DBRequest(DBRequest.ADD_MODULE_RQST, new String[] {
				name, wsdl, moduleName });
	}

}
