package odra.db.links.encoders;

import odra.db.links.RemoteDefaultStore;
import odra.db.links.RemoteDefaultStoreOID;
import odra.db.objects.data.DBLink;
import odra.exceptions.rd.RDNetworkException;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.security.UserContext;
import odra.store.sbastore.ODRAObjectKind;
import odra.virtualnetwork.RemoteP2PStore;
import odra.virtualnetwork.RemoteP2PStoreOID;


/**
 * Query result decoder specialization for query results
 * sends to the server (remote query execution through links).
 */
public class RemoteQueryResultDecoder extends odra.network.encoders.results.QueryResultDecoder
{
	private DBLink link;
	private UserContext usrctx;

	public RemoteQueryResultDecoder(UserContext usrctx, DBLink link)
	{
		this.usrctx = usrctx;
		this.link = link;
	}
 
	protected Result decodeRemoteReferenceResult() throws RDNetworkException
	{
		int hostlen = buffer.getInt();
		byte[] hostarr = new byte[hostlen];
		buffer.get(hostarr);
		String host = new String(hostarr);

		int port = buffer.getInt();

		int schemalen = buffer.getInt();
		byte[] schemaarr = new byte[schemalen];
		buffer.get(schemaarr);
		String schema = new String(schemaarr);

		int bufferlen = buffer.getInt();
		byte[] idarr = new byte[bufferlen];
		buffer.get(idarr);
 
		int id = Integer.valueOf(new String(idarr));		
		ODRAObjectKind kind = ODRAObjectKind.getForByte(buffer.get());
		if(kind == null){
		    throw new RDNetworkException("unknown remote object kind");
		}
		boolean refFlag = buffer.get() == 1 ? true : false;
		ReferenceResult res = null;
		if(link == null)
			res =  new ReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteDefaultStore(host, port, schema, usrctx)));
		else {
        		try
        		{
        			res = new ReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteDefaultStore(link.getHost(), link.getPort(), link.getSchema(), usrctx)));
        		}
        		catch (Exception e)
        		{
        			throw new RDNetworkException(e.getMessage());
        		}
		}
		res.refFlag = refFlag;
		return res;

	}
	
	protected Result decodeRemoteVirtualReferenceResult() throws RDNetworkException
	{
		int hostlen = buffer.getInt();
		byte[] hostarr = new byte[hostlen];
		buffer.get(hostarr);
		String host = new String(hostarr);

		int port = buffer.getInt();

		int schemalen = buffer.getInt();
		byte[] schemaarr = new byte[schemalen];
		buffer.get(schemaarr);
		String schema = new String(schemaarr);

		int bufferlen = buffer.getInt();
		byte[] idarr = new byte[bufferlen];
		buffer.get(idarr);

		int id = Integer.valueOf(new String(idarr));
		ODRAObjectKind kind = ODRAObjectKind.getForByte(buffer.get());
		if(kind == null){
		    throw new RDNetworkException("unknown remote object kind");
		}
		boolean refFlag = buffer.get() == 1 ? true : false;
		Result seed = this.decode();
		VirtualReferenceResult res = null;
		if(link == null)
			res = new VirtualReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteDefaultStore(host, port, schema, usrctx)), seed);
		else {
        		try
        		{
        			res = new VirtualReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteDefaultStore(link.getHost(), link.getPort(), link.getSchema(), usrctx)), seed);
        		}
        		catch (Exception e)
        		{
        			throw new RDNetworkException(e.getMessage());
        		}
		}
		res.refFlag = refFlag;
		return res;

	}

////////////////////////////////
	protected Result decodeRemoteP2PReferenceResult() throws RDNetworkException {
		
		int peernamelen = buffer.getInt();
		byte[] peernamearr = new byte[peernamelen];
		buffer.get(peernamearr);
		String peer = new String(peernamearr);
		
		byte[] schemaarr = new byte[buffer.getInt()];
		buffer.get(schemaarr);
		String schema = new String(schemaarr);
		
		int bufferlen = buffer.getInt();
		byte[] idarr = new byte[bufferlen];
		buffer.get(idarr);
 
		int id = Integer.valueOf(new String(idarr));		
		ODRAObjectKind kind = ODRAObjectKind.getForByte(buffer.get());
		if(kind == null){
		    throw new RDNetworkException("unknown remote object kind");
		}
		boolean refFlag = buffer.get() == 1 ? true : false;
		ReferenceResult res = null;
		if(link == null)
			res =  new ReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteP2PStore(peer, schema)));
		else {
        		try
        		{
        			res = new ReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteP2PStore(peer, schema)));
        		}
        		catch (Exception e)
        		{
        			throw new RDNetworkException(e.getMessage());
        		}
		}
		res.refFlag = refFlag;
		return res;	
	}


/////////////////////////////////////
	protected Result decodeRemoteP2PVirtualReferenceResult() throws RDNetworkException
	{
		int peernamelen = buffer.getInt();
		byte[] peernamearr = new byte[peernamelen];
		buffer.get(peernamearr);
		String peer = new String(peernamearr);
		
		byte[] schemaarr = new byte[buffer.getInt()];
		buffer.get(schemaarr);
		String schema = new String(schemaarr);
		
		int bufferlen = buffer.getInt();
		byte[] idarr = new byte[bufferlen];
		buffer.get(idarr);
 
		int id = Integer.valueOf(new String(idarr));		
		ODRAObjectKind kind = ODRAObjectKind.getForByte(buffer.get());
		if(kind == null){
		    throw new RDNetworkException("unknown remote object kind");
		}
		boolean refFlag = buffer.get() == 1 ? true : false;
		Result seed = this.decode();
		VirtualReferenceResult res = null;
		if(link == null)
			res = new VirtualReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteP2PStore(peer, schema)), seed);
		else {
        		try
        		{
        			res = new VirtualReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteP2PStore(peer, schema)), seed);
        		}
        		catch (Exception e)
        		{
        			throw new RDNetworkException(e.getMessage());
        		}
		}
		res.refFlag = refFlag;
		return res;

	}

}
