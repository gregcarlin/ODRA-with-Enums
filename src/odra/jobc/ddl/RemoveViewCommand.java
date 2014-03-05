/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * RemoveViewCommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public class RemoveViewCommand implements DDLCommand {
	private String viewName;
	/**
	 * 
	 */
	public RemoveViewCommand(String viewName) {
		this.viewName = viewName;
		
	}
	/* (non-Javadoc)
	 * @see odra.jobc.ODRACommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		return new DBRequest(DBRequest.REMOVE_VIEW_RQST, new String[] {
				viewName, moduleName });
	}

}
