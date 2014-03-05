package odra.ws.facade;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.ws.endpoints.EndpointState;
import odra.ws.endpoints.WSEndpointException;
/**
 * Interface (facade) to all enpoint operations.
 * Implementators may be plugged in/out depending on the current need.
 * 
 * @version 2007-06-24
 * @since 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public interface IEndpointFacade {

	/** Initializes facade
	 * @throws DatabaseException
	 */
	void initialize() throws DatabaseException;
	
	
	/** Builds endpoints specific system schema on database creation
	 * @param module
	 * @throws DatabaseException
	 */
	void createMetadata(DBModule module) throws DatabaseException;
	

	/** Creates endpoint instance
	 * @param name
	 * @param exposedObject
	 * @param state
	 * @param relativePath
	 * @param serviceName
	 * @param portTypeName
	 * @param targetNamespace
	 * @return
	 * @throws DatabaseException
	 * @throws WSEndpointException
	 */
	OID createEndpoint(String name, OID exposedObject, EndpointState state, String relativePath, String portTypeName, String portName, String serviceName, String targetNamespace) throws DatabaseException, WSEndpointException;
	
	/** Removes endpoint instance
	 * @param endpointName
	 * @throws DatabaseException
	 * @throws WSEndpointException
	 */
	void removeEndpoint(String endpointName) throws DatabaseException, WSEndpointException;
	
	/** Supsends endpoint instance
	 * @param name
	 * @throws WSEndpointException
	 */
	void suspendEndpoint(String name) throws WSEndpointException;
	
	
	/** Resumes endpoint instance
	 * @param name
	 * @throws WSEndpointException
	 */
	void resumeEndpoint(String name) throws WSEndpointException;
	
	/** Checks whether endpoint of given name exists.
	 * 
	 * @param name Name of endpoint to check
	 * @return True if endpoint of given name exists; false otherwise.
	 * @throws WSEndpointException
	 */
	boolean endpointExist(String name) throws WSEndpointException;
	
	/** Stops HTTP server
	 * 
	 */
	void stopServer();
}
