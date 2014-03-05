/**
 * 
 */
package odra.jobc.ddl;

import odra.jobc.DDLCommand;
import odra.network.transport.DBRequest;

/**
 * AddLinkCommand
 * 
 * @author Radek Adamus
 * @since 2008-05-16 last modified: 2008-05-16
 * @version 1.0
 */
public class AddLinkCommand implements DDLCommand {
	
	private String name;
	private String schema;
	private String password;
	private String host;
	private int port;
	

	/**
	 * @param name
	 * @param schema
	 * @param password
	 * @param host
	 * @param port
	 */
	public AddLinkCommand(String name, String schema, String password, String host,
			int port) {
		this.name = name;
		this.schema = schema;
		this.password = password;
		this.host = host;
		this.port = port;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.jobc.ODRACommand#getRequest(java.lang.String)
	 */
	public DBRequest getRequest(String moduleName) {
		return new DBRequest(DBRequest.ADD_LINK_RQST, new String[] {
				name, schema, password, host, Integer.toString(port), moduleName });
	}

}
