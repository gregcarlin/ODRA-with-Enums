package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.ws.endpoints.EndpointState;


/**
 * Provides functionality of webservice endpoints stored in the database.
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @since
 * @version
 *
 */
public class DBEndpoint extends DBObject {

	/**
	 * Initiaizes an new endpoint data object
	 * @param oid oid of an existing object
	 * @throws DatabaseException
	 */
	public DBEndpoint(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	/**
	 * Initializes the endpoint
	 * @param exposedObject
	 * @param state
	 * @param relativePath
	 * @param serviceName
	 * @param portTypeName
	 * @param targetNamesapace
	 * @throws DatabaseException
	 */
	public void initialize(OID exposedObject, EndpointState state, String relativePath, String portTypeName, String portName, String serviceName, String targetNamesapace) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.ENDPOINT_OBJECT);
		store.createReferenceObject(store.addName(Names.namesstr[Names.EXPOSED_OBJECT]), oid, exposedObject);
		store.createIntegerObject(store.addName(Names.namesstr[Names.STATE]), oid, state.ordinal());
		store.createStringObject(store.addName(Names.namesstr[Names.RELATIVE_PATH]), oid , relativePath, 0);
		store.createStringObject(store.addName(Names.namesstr[Names.PORTTYPE_NAME]), oid , portTypeName, 0);
		store.createStringObject(store.addName(Names.namesstr[Names.PORT_NAME]), oid , portName, 0);
		store.createStringObject(store.addName(Names.namesstr[Names.SERVICE_NAME]), oid , serviceName, 0);
		store.createStringObject(store.addName(Names.namesstr[Names.TARGET_NAMESPACE]), oid , targetNamesapace, 0);
	}



	/**
	 * @return true if object's oid represents a valid endpoint
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.ENDPOINT_OBJECT;
	}

	/**
	 * @return database object exposed through the endpoint
	 * @throws DatabaseException
	 */
	public final OID getExposedObject() throws DatabaseException {
		return getExposedObjectRef().derefReference();
	}

	/**
	 * @return state of the endpoint
	 * @throws DatabaseException
	 */
	public final EndpointState getState() throws DatabaseException {
		int state = getStateRef().derefInt();
		if (EndpointState.STARTED.ordinal() == state) {
			return EndpointState.STARTED;

		} else if (EndpointState.STOPPED.ordinal() == state) {
			return EndpointState.STOPPED;

		} else {
			return EndpointState.UNKNOWN;
		}
	}

	public final void updateState(EndpointState newState) throws DatabaseException {
		getStateRef().updateIntegerObject(newState.ordinal());
	}

	/**
	 * @return relative path of the endpoint
	 * @throws DatabaseException
	 */
	public final String getRelativePath() throws DatabaseException {
		return getRelativePathRef().derefString();
	}

	/**
	 * @return service name of the endpoint
	 * @throws DatabaseException
	 */
	public final String getServiceName() throws DatabaseException {
		return getServiceNameRef().derefString();
	}

	/**
	 * @return port type name of the endpoint
	 * @throws DatabaseException
	 */
	public final String getPortTypeName() throws DatabaseException {
		return getPortTypeNameRef().derefString();
	}

	/**
	 * @return port name of the endpoint
	 * @throws DatabaseException
	 */
	public final String getPortName() throws DatabaseException {
		return getPortNameRef().derefString();
	}


	/**
	 * @return target namespace of the endpoint
	 * @throws DatabaseException
	 */
	public final String getTargetNamespace() throws DatabaseException {
		return getTargetNamespaceRef().derefString();
	}

	/* direct access properties of the endpoint */

	private final OID getExposedObjectRef() throws DatabaseException {
		return oid.getChildAt(EXPOSED_OBJECT_POS);
	}

	private final OID getStateRef() throws DatabaseException {
		return oid.getChildAt(STATE_POS);
	}

	private final OID getRelativePathRef() throws DatabaseException {
		return oid.getChildAt(RELATIVE_PATH_POS);
	}

	private final OID getServiceNameRef() throws DatabaseException {
		return oid.getChildAt(SERVICE_NAME_POS);
	}

	private final OID getPortTypeNameRef() throws DatabaseException {
		return oid.getChildAt(PORTTYPE_NAME_POS);
	}

	private final OID getPortNameRef() throws DatabaseException {
		return oid.getChildAt(PORT_NAME_POS);
	}

	private final OID getTargetNamespaceRef() throws DatabaseException {
		return oid.getChildAt(TARGET_NAMESPACE_POS);
	}

	/* properties positions */

	private static final int EXPOSED_OBJECT_POS = 1;
	private static final int STATE_POS = 2;
	private static final int RELATIVE_PATH_POS = 3;
	private static final int PORTTYPE_NAME_POS = 4;
	private static final int PORT_NAME_POS = 5;
	private static final int SERVICE_NAME_POS = 6;
	private static final int TARGET_NAMESPACE_POS = 7;

	public final static int FIELD_COUNT = 8;

}
