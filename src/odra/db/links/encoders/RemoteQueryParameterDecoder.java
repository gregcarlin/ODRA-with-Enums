/**
 * 
 */
package odra.db.links.encoders;

import odra.db.Database;
import odra.db.OID;
import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.store.DefaultStore;


/**
 * RemoteQueryParameterDecoder
 * decode remote query (remote procedure call) parameters
 * @author Radek Adamus
 *@since 2007-09-11
 *last modified: 2007-09-11
 *@version 1.0
 */
public class RemoteQueryParameterDecoder extends QueryResultDecoder {

    /* (non-Javadoc)
     * @see odra.network.transport.QueryResultDecoder#decodeRemoteReferenceResult()
     */
    @Override
    protected Result decodeRemoteReferenceResult() throws RDNetworkException {
	int bufferlen = buffer.getInt();
	byte[] idarr = new byte[bufferlen];
	buffer.get(idarr);
	String id = new String(idarr);
	ReferenceResult rres = new ReferenceResult(string2OID(id));
	boolean refFlag = buffer.get() == 1 ? true : false;
	rres.refFlag = refFlag;
	rres.parent = this.decodeContextReference();
	return rres;
    }

    /* (non-Javadoc)
     * @see odra.network.transport.QueryResultDecoder#decodeRemoteVirtualReferenceResult()
     */
    @Override
    protected Result decodeRemoteVirtualReferenceResult()
	    throws RDNetworkException {
	
	int bufferlen = buffer.getInt();
	byte[] idarr = new byte[bufferlen];
	buffer.get(idarr);
	String id = new String(idarr);
	boolean refFlag = buffer.get() == 1 ? true : false;
	ReferenceResult contextref = this.decodeContextReference();
	Result seed = this.decode();
	VirtualReferenceResult rres = new VirtualReferenceResult(new odra.store.DefaultStoreOID(Integer.parseInt(id), (DefaultStore) Database.getStore()), seed);
	rres.refFlag = refFlag;
	rres.parent = contextref;
	
	return rres;
    }
    private final OID string2OID(String objidstr)throws RDNetworkException {
	
	return new odra.store.DefaultStoreOID(Integer.parseInt(objidstr), (DefaultStore) Database.getStore());
    }
    
    private ReferenceResult decodeContextReference() throws RDNetworkException{
	if(buffer.getInt() > 0){
	    Result context = this.decode();
	    assert context instanceof ReferenceResult : "wrong remote reference format";
	    return (ReferenceResult)context;
	}
	return null;
    }
    
}
