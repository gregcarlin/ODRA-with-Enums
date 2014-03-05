/**
 * 
 */
package odra.jobc.query;

import odra.jobc.DMLCommand;
import odra.jobc.SBQLQuery;
import odra.network.transport.DBRequest;

/**
 * SBQLQueryCommand
 * @author Radek Adamus
 *@since 2008-05-16
 *last modified: 2008-05-16
 *@version 1.0
 */
public class SBQLQueryCommand implements DMLCommand {

	SBQLQuery query;
	
	
	
	/**
	 * 
	 */
	public SBQLQueryCommand(SBQLQuery query) {
		this.query = query;
	}
	/* (non-Javadoc)
	 * @see odra.jobc.ODRACommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		String pquery = query.prepare();
		return new DBRequest(
				DBRequest.EXECUTE_SBQL_RQST, new String[] { pquery,
						moduleName, "on", "off" });		
	}

}
