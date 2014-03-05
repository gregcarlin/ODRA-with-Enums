package odra.ws.endpoints;

import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBEndpoint;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DataObjectKind;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.literal.LiteralTypeMapper;


/**
 * Factory class for creating different types of web service endpoints
 * 
 * @since 2006-11-29
 * @version 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSEndpointFactory {
	// currently it is fixed to literal, it may need to be determined through parameter if more mappers will be 
	// introduced
	private static ITypeMapper literal;
	
	private WSEndpointFactory() {
	}

	static {
		literal = new LiteralTypeMapper();
	}

	/** Produces generic webservice which allows to query database with any sbql code
	 * @return Generic endpoint instance
	 */
	public static WSEndpoint createEndpoint(){
		WSGenericEndpoint endpoint = new WSGenericEndpoint();		
		endpoint.options = WSEndpointOptions.create(ConfigServer.WS_GENERIC_NAME, ConfigServer.WS_GENERIC_PATH, 
				ConfigServer.WS_GENERIC_PORT, ConfigServer.WS_GENERIC_PORTTYPE, ConfigServer.WS_GENERIC_SERVICE,  EndpointState.STARTED, 
				ConfigServer.WS_GENERIC_NS);
		endpoint.setMapper(literal);
		return endpoint;
	}
	
	/** Produces webservice for given database object (procedure or class)
	 * @param dbEndpoint
	 * @param mod
	 * @param name
	 * @return Webservice endpoint for given db object or null if not supported
	 * @throws WSEndpointException
	 */
	public static WSEndpoint createEndpoint(DBEndpoint dbEndpoint, DBModule mod, String name) throws WSEndpointException {
		try {
			OID oid = mod.findFirstByName(name, mod.getDatabaseEntry());
	
			if (oid == null) {
				return null;
			}
	
			DBObject obj = new DBObject(oid);
			int objKind = obj.getObjectKind().getKindAsInt();
			WSEndpoint endpoint = null;
			
			switch (objKind) {
				case DataObjectKind.PROCEDURE_OBJECT:
					endpoint = createProcEndpoint(dbEndpoint, mod, name);
					break;
				case DataObjectKind.CLASS_OBJECT:
					endpoint = createClassEndpoint(dbEndpoint, mod, name);
					break;
				default:
					StringBuilder sb = new StringBuilder();
					sb.append("Type of '");
					sb.append(name);
					sb.append("' is not supported to be exposed as web service endpoint.");
					String msg = sb.toString();
					
					ConfigServer.getLogWriter().getLogger().log(Level.WARNING, msg);
					throw new WSEndpointException(msg);
					
			}
	
			endpoint.setMapper(literal);
			return endpoint;		
		} catch (WSEndpointException ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.WARNING, "Endpoint creating error. ", ex); 
			throw ex;
			
		} catch (DatabaseException ex) {
			throw new WSEndpointException(ex);
		}
		
	}

	private static WSEndpoint createProcEndpoint(DBEndpoint dbEndpoint, DBModule mod, String name) throws DatabaseException, WSEndpointException {
		OID oid = mod.findFirstByName(name, mod.getDatabaseEntry());

		if (ConfigDebug.ASSERTS) {
			assert new DBObject(oid).getObjectKind().getKindAsInt() == DataObjectKind.PROCEDURE_OBJECT;

		}
		
		WSProcEndpoint endpoint = new WSProcEndpoint(mod, name);
		endpoint.setDbEndpoint(dbEndpoint);
		return endpoint;

	}

	private static WSClassEndpoint createClassEndpoint(DBEndpoint dbEndpoint, DBModule mod, String name) throws DatabaseException, WSEndpointException{
		OID oid = mod.findFirstByName(name, mod.getDatabaseEntry());

		if (oid == null) {
			return null;
		}

		if (ConfigDebug.ASSERTS) {
			assert new DBObject(oid).getObjectKind().getKindAsInt() == DataObjectKind.CLASS_OBJECT;

		}

		WSClassEndpoint endpoint = new WSClassEndpoint(mod, name);
		endpoint.setDbEndpoint(dbEndpoint);
		return endpoint;
	}

}
 