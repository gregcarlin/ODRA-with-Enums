/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * AddViewCommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public class AddViewCommand implements DDLCommand {

	private String viewSource;
	
	/**
	 * 
	 */
	public AddViewCommand(String viewSource) {
		this.viewSource = viewSource;
	}
	/* (non-Javadoc)
	 * @see odra.jobc.ODRACommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		return new DBRequest(DBRequest.ADD_VIEW_RQST, new String[] {
				viewSource, moduleName });
	}

}
