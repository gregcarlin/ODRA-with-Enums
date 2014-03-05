/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * AddWrapperModuleCommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public class AddWrapperModuleCommand implements DDLCommand {

	private String moduleName;
	private String wrapperHostName;
	private int wrapperPortNumber;
	/**
	 * 
	 */
	public AddWrapperModuleCommand(String moduleName,
			String wrapperHostName, int wrapperPortNumber) {
				this.moduleName = moduleName;
				this.wrapperHostName = wrapperHostName;
				this.wrapperPortNumber = wrapperPortNumber;
	}
	/* (non-Javadoc)
	 * @see odra.jobc.DDLCommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		
		return new DBRequest(DBRequest.ADD_MODULE_AS_WRAPPER_RQST, new String[] {
				moduleName, wrapperHostName,
				Integer.toString(wrapperPortNumber),
				Integer.toString(1) });
	}

}
