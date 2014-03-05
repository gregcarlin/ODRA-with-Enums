/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * OdraModule
 * @author Radek Adamus
 *@since 2008-05-15
 *last modified: 2008-05-15
 *@version 1.0
 */
public class AddModuleCommand implements DDLCommand{
	private final String moduleSource;

	/**
	 * @param moduleSource
	 */
	public AddModuleCommand(String moduleSource) {
		this.moduleSource = moduleSource;
	}

	

	/**
	 * @return the moduleSource
	 */
	public String getModuleSource() {
		return moduleSource;
	}

	/* (non-Javadoc)
	 * @see odra.jobc.DDLCommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		return new DBRequest(DBRequest.ADD_MODULE_RQST, new String[] {
				moduleSource, moduleName });
	}

	
	
	
	
}
