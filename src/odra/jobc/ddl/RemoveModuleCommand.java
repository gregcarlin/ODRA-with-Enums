/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * RemoveModuleCommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public class RemoveModuleCommand implements DDLCommand {

	private String module;
	/**
	 * 
	 */
	public RemoveModuleCommand(String moduleName) {
		module = moduleName;
	}
	
	/* (non-Javadoc)
	 * @see odra.jobc.ODRACommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		return new DBRequest(DBRequest.REMOVE_MODULE_RQST, new String[] {
				moduleName + "." + module });
	}

}
