package odra.ws.endpoints;

import odra.system.config.ConfigServer;


/**
 * Web services helper methods
 * 
 * @since 2007-05-06
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSHelper {
	/** Filters transport protocol address port 
	 * @return
	 */
	public static String getServerBaseAddress() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("http://");
		sb.append(ConfigServer.WS_SERVER_ADDRESS);
		
		if (ConfigServer.WS_SERVER_PORT != 80) {
			sb.append(":");
			sb.append(ConfigServer.WS_SERVER_PORT);
		}
		
		
		return sb.toString();
	}
}
