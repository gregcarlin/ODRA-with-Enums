package odra.db.links;


import java.util.Date;

import odra.db.AbstractDataStore;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.links.encoders.RemoteQueryResultDecoder;
import odra.db.objects.data.DBLink;
import odra.exceptions.rd.RDException;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.security.UserContext;
import odra.store.sbastore.ODRAObjectKind;

public class RemoteDefaultStore extends AbstractDataStore {
	public String host, schema;
	public int port;
	public UserContext usrctx;

	protected RemoteDefaultStore(){
		
	}
	
	public RemoteDefaultStore(String host, int port, String schema, UserContext usrctx) {
		this.host = host;
		this.port = port;
		this.schema = schema;
		this.usrctx = usrctx;
	}
	
	
	public void close() {
	}

	public int countChildren(OID obj) throws DatabaseException {	
		return ((IntegerResult) sendRemoteDBRqst(obj, DBRequest.COUNT_CHILDREN_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public OID createAggregateObject(int name, OID parent, int children) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}
	public OID createAggregateObject(int name, OID parent, int children, int minCard, int maxCard) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}
	public OID createBinaryObject(int name, OID parent, byte[] value, int buffer) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public OID createBooleanObject(int name, OID parent, boolean value) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public OID createComplexObject(int name, OID parent, int children) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public OID createDoubleObject(int name, OID parent, double value) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public OID createIntegerObject(int name, OID parent, int value) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public OID createPointerObject(int name, OID parent, OID value) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public OID createReferenceObject(int name, OID parent, OID value) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#createReverseReferenceObject(int, odra.db.OID, odra.db.OID, odra.db.OID)
	 */
	public OID createReverseReferenceObject(int name, OID parent,
		OID value, OID reverse) throws DatabaseException {
		assert false : "unimplemented";
	// TODO Auto-generated method stub
	return null;
	}


	public OID createStringObject(int name, OID parent, String value, int buffer) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(OID obj, boolean controlCardinality) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
	}

	public void deleteAllChildren(OID obj, boolean controlCardinality) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
	}

	public String dump() throws DatabaseException {
		throw new DatabaseException("Operation not supported by the remote store");
	}

	public String dumpMemory(boolean verbose) throws DatabaseException {
		throw new DatabaseException("Operation not supported by the remote store");
	}

	public OID findFirstByNameId(int nameid, OID parent) throws DatabaseException {
		assert false : "unimplemented";
		return null;
	}

	public OID getChildAt(OID parent, int childnum) throws DatabaseException {		
		return ((ReferenceResult) sendRemoteDBRqst(parent,DBRequest.GET_CHILD_AT, new String[] {String.valueOf(parent.internalOID()), String.valueOf(childnum)})).value;	}

	public OID getEntry() {
		assert false : "unimplemented";
		return null;
	}
	
	public OID getRoot() throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	private DBLink findLink() throws DatabaseException {
		try {
			return LinkManager.getInstance().findLink(host, schema, port, usrctx);
		}
		catch (RDException ex) {
			throw new DatabaseException("Remote store exception"); 
		}		
	}
	
	
	
	public String getObjectName(OID obj) throws DatabaseException {
		try {
			DBLink link = LinkManager.getInstance().findLink(host, schema, port, usrctx);
			DBConnection conn = LinkManager.getInstance().getConnectionForLink(link);		
			RemoteQueryResultDecoder decoder = new RemoteQueryResultDecoder(usrctx, link);
			
			DBRequest qreq = new DBRequest(DBRequest.GET_NAME_RQST, new String[] { String.valueOf(obj.internalOID()) });
			DBReply qrep = conn.sendRequest(qreq);

			byte[] rawres = qrep.getRawResult();

			return ((StringResult) decoder.decodeResult(rawres)).value;
			
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}
	}


	public OID[] getReferencesPointingAt(OID obj) throws DatabaseException {
		throw new DatabaseException("Operation not supported by the remote store");
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#getReversePointer(odra.db.OID)
	 */
	public OID getReversePointer(OID obj) throws DatabaseException
	{
	    throw new DatabaseException("Operation not supported by the remote store");
	}


	public boolean isAggregateObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_AGGREGATE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public boolean isBinaryObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_BINARY_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public  boolean isBooleanObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_BOOLEAN_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public boolean isComplexObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_COMPLEX_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public boolean isDoubleObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_DOUBLE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public  boolean isIntegerObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_INTEGER_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public boolean isReferenceObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_REFERENCE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}
	/* (non-Javadoc)
	 * @see odra.db.IDataStore#isReverseReferenceObject(odra.db.OID)
	 */
	public boolean isReverseReferenceObject(OID obj)
		throws DatabaseException {
	    throw new DatabaseException("Operation not supported by the remote store");
	    
	}
	public boolean isStringObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_STRING_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public void move(OID obj, OID newparent) throws DatabaseException {
		throw new DatabaseException("Operation not supported by the remote store");
	}

	public OID offset2OID(int oid) {
		assert false : "not supported";
		
		return null;
	}

	public void preallocateNewChildren(OID map_oid, int bucketsCount) throws DatabaseException {
		throw new DatabaseException("Operation not supported by the remote store");		
	}

	public void updateBinaryObject(OID obj, byte[] val) throws DatabaseException {
		sendRemoteDBRqst(obj, DBRequest.UPDATE_BINARY_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), String.valueOf(val) } );
	}

	public void updateBooleanObject(OID obj, boolean val) throws DatabaseException {
		sendRemoteDBRqst(obj, DBRequest.UPDATE_BOOLEAN_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), String.valueOf(val) } );
	}

	public void updateDoubleObject(OID obj, double val) throws DatabaseException {
		sendRemoteDBRqst(obj, DBRequest.UPDATE_DOUBLE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), String.valueOf(val) } );
	}
	
	public void updateIntegerObject(OID obj, int val) throws DatabaseException {
		sendRemoteDBRqst(obj, DBRequest.UPDATE_INTEGER_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), String.valueOf(val) } );
	}

	public void updateReferenceObject(OID obj, OID val) throws DatabaseException {
		sendRemoteDBRqst(obj, DBRequest.UPDATE_REFERENCE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), String.valueOf(val.internalOID()) } );
	}
	/* (non-Javadoc)
	 * @see odra.db.IDataStore#updateReverseReferenceObject(odra.db.OID, odra.db.OID, odra.db.OID)
	 */
	public void setReversePointer(OID obj, OID val)
		throws DatabaseException {
	    assert false : "unimplemented";
	    
	}
	public void updateStringObject(OID obj, String val) throws DatabaseException {
		sendRemoteDBRqst(obj, DBRequest.UPDATE_STRING_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), val });
	}

	public boolean derefBooleanObject(OID obj) throws DatabaseException {
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.DEREF_BOOLEAN_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}
	
	public double derefDoubleObject(OID obj) throws DatabaseException {
		return ((DoubleResult) sendRemoteDBRqst(obj, DBRequest.DEREF_DOUBLE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}
	
	public Date derefDateObject(OID obj) throws DatabaseException {
		return ((DateResult) sendRemoteDBRqst(obj, DBRequest.DEREF_DATE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public int derefIntegerObject(OID obj) throws DatabaseException {
		return ((IntegerResult) sendRemoteDBRqst(obj, DBRequest.DEREF_INTEGER_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public OID derefReferenceObject(OID obj) throws DatabaseException {
		return ((ReferenceResult) sendRemoteDBRqst(obj, DBRequest.DEREF_REFERENCE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public String derefStringObject(OID obj) throws DatabaseException {
		return ((StringResult) sendRemoteDBRqst(obj, DBRequest.DEREF_STRING_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}
	
	public byte[] derefBinaryObject(OID obj) throws DatabaseException {
		throw new DatabaseException("Operation not supported by the remote store");
	}

	public OID[] derefComplexObject(OID obj) throws DatabaseException {
		StructResult strres = ((StructResult) sendRemoteDBRqst(obj, DBRequest.DEREF_COMPLEX_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) }));
		OID[] oids = new OID[strres.fieldsCount()];
		for(int i = 0; i < oids.length; i++)
			oids[i] = ((ReferenceResult) strres.fieldAt(i)).value;
		
		return oids;
	}	
	
	protected  Result sendRemoteDBRqst(OID obj, byte rqst, String[] params) throws DatabaseException {
		RemoteDefaultStoreOID oid = (RemoteDefaultStoreOID) obj;

		try {
			DBLink link = LinkManager.getInstance().findLink(oid, usrctx);
			DBConnection conn = LinkManager.getInstance().getConnectionForLink(link);
			RemoteQueryResultDecoder decoder = new RemoteQueryResultDecoder(usrctx, link);

			DBRequest qreq = new DBRequest(rqst, params);
			DBReply qrep = conn.sendRequest(qreq);
			
			
			byte[] rawres = qrep.getRawResult();

			return decoder.decodeResult(rawres);
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}		
	}

	public OID createDateObject(int name, OID parent, Date value) throws DatabaseException
	{
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDateObject(OID obj) throws DatabaseException
	{
		return ((BooleanResult) sendRemoteDBRqst(obj, DBRequest.IS_DATE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()) })).value;
	}

	public void updateDateObject(OID obj, Date date) throws DatabaseException
	{
		sendRemoteDBRqst(obj, DBRequest.UPDATE_DATE_OBJECT_RQST, new String[] { String.valueOf(obj.internalOID()), Long.toString(date.getTime()) } );
	}

	
	//M1
	/* (non-Javadoc)
	 * @see odra.db.IDataStore#derefInstanceOfReference(odra.db.OID)
	 */
	public OID derefInstanceOfReference(OID obj) throws DatabaseException {
		
		// TODO : this method is to be implemented to support M1
		//assert false : "unimplemented";
		
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#isClassInstance(odra.db.OID)
	 */
	public boolean isClassInstance(OID obj) throws DatabaseException {
		assert false : "unimplemented";
		return false;
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#setInstanceOfReference(odra.db.OID, odra.db.OID)
	 */
	public void setInstanceOfReference(OID obj, OID clsObj) throws DatabaseException {
		assert false : "unimplemented";
		
	}

	public int getAggregateMaxCard(OID obj) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return 0;
	}

	public int getAggregateMinCard(OID obj) throws DatabaseException {
		assert false : "unimplemented";
		// TODO Auto-generated method stub
		return 0;
	}
	/***********************************************************
	 * This part is used to deal with names.
	 * Naming indexes convert name ids into strings (and vice versa).
	 * They are stored as binary objects and usually are associated
	 * with database modules.
	 */

	public String getName(int nameid) throws DatabaseException {
		return Database.getNameIndex().id2name(nameid);
	}

	public int addName(String name) throws DatabaseException {
		return Database.getNameIndex().addName(name);
	}

	public int getNameId(String name) throws DatabaseException {
		return Database.getNameIndex().name2id(name);
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#getObjectKind(odra.db.OID)
	 */
	public ODRAObjectKind getObjectKind(OID obj) throws DatabaseException
	{
	   
	    return ODRAObjectKind.UNKNOWN_OBJECT;
	}

	/* (non-Javadoc)
	 * @see odra.db.IDataStore#rename(odra.db.OID, int)
	 */
	@Override
	public void renameObject(OID obj, int newName) throws DatabaseException {
		assert false : "unimplemented";
		
	}
	
}
