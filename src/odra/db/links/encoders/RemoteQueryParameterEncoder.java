/**
 * 
 */
package odra.db.links.encoders;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.links.RemoteDefaultStore;
import odra.db.links.RemoteDefaultStoreOID;
import odra.db.objects.data.DBLink;
import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.results.QueryResultEncoder;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.system.Sizes;


/**
 * RemoteQueryParameterEncoder
 * encode remote query (remote procedure call) parameters
 * @author Radek Adamus
 * @since 2007-09-11 last modified: 2007-09-11
 * @version 1.0
 */
public class RemoteQueryParameterEncoder extends QueryResultEncoder {

    private DBLink link;

    private boolean encodeContext;

    /**
     * @param link -
     *                database link to the remote server where the query is to
     *                be send
     */
    public RemoteQueryParameterEncoder(DBLink link) {
	this.link = link;
	this.encodeContext = false;

    }

    /**
     * @param link -
     *                database link to the remote server where the query is to
     *                be send
     * @param encodeContext -
     *                should we also encode information about the reference
     *                context (used for remote procedure call)
     */
    public RemoteQueryParameterEncoder(DBLink link, boolean encodeContext) {
	this.link = link;
	this.encodeContext = encodeContext;

    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.network.transport.QueryResultEncoder#encodeReferenceResult(odra.sbql.results.runtime.ReferenceResult)
     */
    /*
     * (non-Javadoc)
     * 
     * @see odra.network.transport.QueryResultEncoder#encodeReferenceResult(odra.sbql.results.runtime.ReferenceResult)
     */
    @Override
    protected ByteBuffer encodeReferenceResult(ReferenceResult rres)
	    throws RDNetworkException {
	
	if (this.isProprerReference(rres)) {
	    ByteBuffer buf;
	    byte[] idarr = this.serializeOID(rres.value);
	    byte[] context = this.encodeContext(rres);
	    buf = ByteBuffer.allocate(2 * Sizes.INTVAL_LEN + RES_KIND_LEN
		    + Sizes.BOOLEAN_LEN + idarr.length + context.length);
	    buf.put(Result.REMOTE_REFERENCE_RESULT);
	    buf.putInt(idarr.length);
	    buf.put(idarr);
	    buf.put(rres.refFlag ? (byte) 1 : (byte) 0);
	    buf.putInt(context.length);
	    buf.put(context);
	    return buf;
	} 
	throw new RDNetworkException("Invalid remote reference ");
	

	
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.network.transport.QueryResultEncoder#encodeVirtualReferenceResult(odra.sbql.results.runtime.VirtualReferenceResult)
     */
    @Override
    protected ByteBuffer encodeVirtualReferenceResult(
	    VirtualReferenceResult rres) throws RDNetworkException {
	
	if (this.isProprerReference(rres)) {
	    ByteBuffer buf;
	    byte[] idarr = this.serializeOID(rres.value);
	    byte[] context = this.encodeContext(rres);
	    byte[] seed = this.encodeResult(rres.getSeed());
	    buf = ByteBuffer.allocate(2 * Sizes.INTVAL_LEN + RES_KIND_LEN
		    + Sizes.BOOLEAN_LEN + idarr.length +context.length + seed.length );
	    buf.put(Result.REMOTE_VIRTUAL_REFERENCE_RESULT);
	    buf.putInt(idarr.length);
	    buf.put(idarr);
	    buf.put(rres.refFlag ? (byte) 1 : (byte) 0);
	    buf.putInt(context.length);
	    buf.put(context);
	    buf.put(seed);
	    return buf;
	} 
	throw new RDNetworkException("Invalid remote reference ");
	
    }

    private boolean isProprerReference(ReferenceResult rres)
	    throws RDNetworkException {
    	
    	// TODO: make something with that
    	
    	try {
			if (link.getGrid()) return true;
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}
    	
	if (rres.value instanceof RemoteDefaultStoreOID) {
	    RemoteDefaultStoreOID remoteoid = (RemoteDefaultStoreOID) rres.value;
	    try {
		if (((RemoteDefaultStore) remoteoid.getStore()).host
			.equals(link.getHost())
			&& ((RemoteDefaultStore) remoteoid.getStore()).schema
				.equals(link.getSchema())
			&& ((RemoteDefaultStore) remoteoid.getStore()).port == link
				.getPort())
		    return true;
	    } catch (DatabaseException e) {
		throw new RDNetworkException("Database error");
	    }
	}
	return false;

    }
    
    private byte[] encodeContext(ReferenceResult rres)throws RDNetworkException{
	byte[] context;
	if (this.encodeContext && rres.parent != null
		    && rres.parent.value instanceof RemoteDefaultStoreOID) {
		this.encodeContext = false;
		context = this.encodeResult(rres.parent);
		this.encodeContext = true;
	    } else
		context = new byte[0];
	return context;
    }
}
