package odra.db.objects.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBClass;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.ws.bindings.BindingFactory;
import odra.ws.bindings.BindingInfo;
import odra.ws.common.Pair;
import odra.ws.facade.WSBindingType;
import odra.ws.proxies.OperationInfo;


/**
 * Provides information needed to persist and recognize modules which are web service proxy
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @since 2007-03-25
 * @version 2007-04-16
 *
 */
public class DBProxy extends DBObject {


	public DBProxy(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	/**
	 * Initializes empty database object with necessary web service proxy entry structure
	 * @param proxiedObjectOid OID of object to expose
	 * @param wsdlLocation Location of WSDL contract
	 * @param serviceAddress Service of target service
	 * @param namespace Namespace of target service
	 * @param operations List of operations to be exposed
	 * @param bindingType Type of binding to be used for exposure
	 * @throws DatabaseException
	 */
	public void initialize(OID proxiedObjectOid, URL wsdlLocation, URL serviceAddress, String namespace,
			List<Pair<DBProcedure, OperationInfo>> operations, WSBindingType bindingType) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.PROXY_OBJECT);
		store.createReferenceObject(store.addName(Names.namesstr[Names.PROXIED_OBJECT]), oid, proxiedObjectOid);
		store.createStringObject(store.addName(Names.namesstr[Names.WSDL_LOCATION]), oid, wsdlLocation.toExternalForm(), 0);
		store.createStringObject(store.addName(Names.namesstr[Names.SERVICE_ADDRESS]), oid , serviceAddress.toExternalForm(), 0);
		store.createStringObject(store.addName(Names.namesstr[Names.NAMESPACE]), oid , namespace, 0);
		OID opsRef = store.createAggregateObject(store.addName(Names.namesstr[Names.OPERATIONS]), oid, 0);
		store.createStringObject(store.addName(Names.namesstr[Names.BINDING_TYPE]), oid, bindingType.getName(), 0);
		for (Pair<DBProcedure, OperationInfo> op : operations) {
			addOperation(opsRef, op.getKey(), op.getValue());
		}
	}

	/**
	 * Adds per operation specific meta information
	 * @param proc
	 * @param soapAction
	 * @throws DatabaseException
	 */
	private void addOperation(OID opsRef, DBProcedure proc, OperationInfo info) throws DatabaseException {
		OID op = store.createComplexObject(store.addName(Names.namesstr[Names.OPERATION]), opsRef, 2);
		store.createReferenceObject(store.addName(Names.namesstr[Names.PROC]), op, proc.getOID());
		OID binding = store.createComplexObject(store.addName(Names.namesstr[Names.BINDING_INFO]), op, 0);

		BindingInfo bindingInfo = info.getBindingInfo();
		if (( bindingInfo != null) && (bindingInfo.isValid())) {
			for (Entry<String, String> e : bindingInfo.getEntries().entrySet()) {
				store.createStringObject(store.addName("$"+e.getKey()), binding, e.getValue(), 0);
			}
		}

		String name = info.getName();
		store.createStringObject(store.addName(Names.namesstr[Names.OPERATION_NAME]), op, name, 0);

	}


	/**
	 * @return true if object's oid represents a valid proxy
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.PROXY_OBJECT;
	}

	/**
	 * @return
	 * @throws DatabaseException
	 */
	public final OID getProxiedObject() throws DatabaseException {
		return getProxiedObjectRef().derefReference();
	}

	/**
	 * @return
	 * @throws DatabaseException
	 */
	public final URL getWSDLLocation() throws DatabaseException {
		String wsdlString = getWSDLLocationRef().derefString();
		return convertToURL(wsdlString);

	}

	/**
	 * @return
	 * @throws DatabaseException
	 */
	public final URL getServiceAddress() throws DatabaseException {
		String addressString = getServiceAddressRef().derefString();
		return convertToURL(addressString);
	}

	/**
	 * @return
	 * @throws DatabaseException
	 */
	public final String getNamespace() throws DatabaseException {
		return getNamespaceRef().derefString();
	}

	/**
	 * @return
	 * @throws DatabaseException
	 */
	public final WSBindingType getBindingType() throws DatabaseException {
		String bindingType = getBindingTypeRef().derefString();
		WSBindingType result = WSBindingType.getByCode(bindingType);
		if (result != null) {
			return result;

		} else {
			throw new DatabaseException("Incorrect binding type for proxy " + oid);

		}

	}


	/**
	 * @param proc
	 * @return
	 * @throws DatabaseException
	 */
	public final OperationInfo getOperationInfo(DBProcedure proc) throws DatabaseException {
		OID[] ops = getOperationsRef().derefComplex();
		for (OID op : ops) {
			OID[] fields = op.derefComplex();
			if (fields[OP_PROC_REF].derefReference() == proc.getOID())
			{
				OperationInfo info = extractOperationInfo(fields);
				return info;
			}
		}

		return null;
	}

	/**
	 * @return
	 * @throws DatabaseException
	 */
	public final Hashtable<String, OperationInfo> getOperations() throws DatabaseException {
		Hashtable<String, OperationInfo> map = new  Hashtable<String, OperationInfo>();

		OID[] ops = getOperationsRef().derefComplex();
		for (OID op : ops) {
			OID[] fields = op.derefComplex();

			OID procOID = fields[OP_PROC_REF].derefReference();
			DBProcedure dbProc = new DBProcedure(procOID);
			if (ConfigDebug.ASSERTS) assert dbProc.isValid();

			OperationInfo info = extractOperationInfo(fields);
			String name = fields[OP_NAME].derefString();
			info.setName(name);


			map.put(dbProc.getName(), info);

		}

		return map;
	}

	private OperationInfo extractOperationInfo(OID[] fields)
			throws DatabaseException {
		OperationInfo info = new OperationInfo();
		BindingInfo bindingInfo = BindingFactory.createProvider(getBindingType()).getBindingInfo();

		Hashtable<String, String> table = new Hashtable<String, String>();
		for (OID entry : fields[OP_BINDING_INFO].derefComplex() ) {
			String value = entry.derefString();
			String key = new DBObject(entry).getName();
			table.put(key, value);
		}
		bindingInfo.load(table);
		info.setBindingInfo(bindingInfo);
		return info;
	}

	/* direct access properties of the endpoint */

	private final OID getProxiedObjectRef() throws DatabaseException {
		return oid.getChildAt(PROXIED_OBJECT_POS);
	}

	private final OID getWSDLLocationRef() throws DatabaseException {
		return oid.getChildAt(WSDL_LOCATION_POS);
	}

	private final OID getServiceAddressRef() throws DatabaseException {
		return oid.getChildAt(SERVICE_ADDRESS_POS);
	}

	private final OID getNamespaceRef() throws DatabaseException {
		return oid.getChildAt(NAMESPACE_POS);
	}

	private final OID getOperationsRef() throws DatabaseException {
		return oid.getChildAt(OPERATIONS_POS);
	}

	private final OID getBindingTypeRef() throws DatabaseException {
		return oid.getChildAt(BINDING_TYPE_POS);
	}


	/* helper methods */
	private URL convertToURL(String address) throws DatabaseException {
		try {
			return new URL(address);

		} catch (MalformedURLException ex) {
			throw new DatabaseException("Incorrect url for WSDL in proxy module " + oid);

		}

	}
	/* properties positions */
	private static final int PROXIED_OBJECT_POS = 1;
	private static final int WSDL_LOCATION_POS = 2;
	private static final int SERVICE_ADDRESS_POS = 3;
	private static final int NAMESPACE_POS = 4;
	private static final int OPERATIONS_POS = 5;
	private static final int BINDING_TYPE_POS = 6;
	public static final int FIELD_COUNT = 8;
	private static final int OP_PROC_REF = 0;
	private static final int OP_BINDING_INFO  = 1;
	private static final int OP_NAME = 2;
	private static final int OP_FIELD_COUNT = 4;


}
