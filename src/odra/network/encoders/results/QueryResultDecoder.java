package odra.network.encoders.results;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import odra.exceptions.rd.RDNetworkException;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.RemoteReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;


/**
 * This class is responsible for decoding serialized query results
 * received by database clients. Results are encoded into 
 * byte arrays by a corresponding class - QueryResultEncoder.
 * 
 * @author raist
 */


public class QueryResultDecoder {
	protected ByteBuffer buffer;
	

	public QueryResultDecoder() {
	}
	
	

	/**
	 * Decodes results encoded into the form of a byte array
	 * @param data decoded result
	 */
	public Result decodeResult(byte[] data) throws RDNetworkException {
		buffer = ByteBuffer.wrap(data);
		
		return decode();
	}

	protected Result decode() throws RDNetworkException {
		int resid = buffer.get();
		
		switch (resid) {
			case Result.BAG_RESULT:
				return decodeBagResult();

			case Result.BINDER_RESULT:
				return decodeBinderResult();
				
			case Result.BOOLEAN_RESULT:
				return decodeBooleanResult();
				
			case Result.DOUBLE_RESULT:
				return decodeDoubleResult();
			
			case Result.INTEGER_RESULT:
				return decodeIntegerResult();

			case Result.STRING_RESULT:
				return decodeStringResult();
				
			case Result.STRUCT_RESULT:
				return decodeStructResult();
				
			case Result.DATE_RESULT:
				return decodeDateResult();
				
			case Result.REMOTE_REFERENCE_RESULT:
				return decodeRemoteReferenceResult();
			
			case Result.REMOTE_VIRTUAL_REFERENCE_RESULT:
			    return decodeRemoteVirtualReferenceResult();
			    
			case Result.REMOTE_REFERENCE_RESULT_P2P: 
				return decodeRemoteP2PReferenceResult();

			case Result.REMOTE_VIRTUAL_REFERENCE_RESULT_P2P:
			    return decodeRemoteP2PVirtualReferenceResult();
				
			default:
				throw new RDNetworkException("Invalid query result type (" + resid + ")"); 
		}
	}
	

	protected Result decodeIntegerResult() {
		return new IntegerResult(buffer.getInt()); 
	}
	
	protected Result decodeDoubleResult() {
		return new DoubleResult(buffer.getDouble());
	}
	
	protected Result decodeBooleanResult() {
		return new BooleanResult(buffer.get() == 1 ? true : false);
	}
	
	protected Result decodeStringResult() {
		int len = buffer.getInt();
		byte[] strarr = new byte[len]; 
		
		buffer.get(strarr);
		String value = "";
		try {
		    value = new String(strarr, Charset.forName(ENCODING).name());
		    
		} catch (UnsupportedEncodingException e) {
		    //ignore
		}
		return new StringResult(value);
	}
	
	protected Result decodeDateResult() {
		long value = buffer.getLong();
		return new DateResult(new Date(value));
	}

	protected Result decodeBinderResult() throws RDNetworkException {
		StringResult name = (StringResult) decode();
		Result value = decode();

		return new BinderResult(name.value, value);
	}
	
	protected Result decodeStructResult() throws RDNetworkException {
		int fields = buffer.getInt();

		StructResult struct = new StructResult();
		
		for (int i = 0; i < fields; i++) 
			struct.addField((SingleResult) decode());

		return struct;
	}

	protected Result decodeBagResult() throws RDNetworkException {
		int fields = buffer.getInt();

		BagResult bag = new BagResult();
		
		int i = 0;
		try {
			for (i = 0; i < fields; i++)
				bag.addElement((SingleResult) decode());
			
		}
		catch (java.lang.OutOfMemoryError ex) {
			System.out.println("Brak pamieci");
		}
		catch (RDNetworkException ex) {
			System.out.println("mamy: " + i);
			throw ex;
		}

		return bag;
	}
	
	protected Result decodeRemoteReferenceResult() throws RDNetworkException{
		int hostlen = buffer.getInt();
		byte[] hostarr = new byte[hostlen];
		buffer.get(hostarr);
		String host = "";
		try {
		    host = new String(hostarr,Charset.forName(ENCODING).name());
		} catch (UnsupportedEncodingException e1) {
		    // ignore
		}
		
		int port = buffer.getInt();
				
		int schemalen = buffer.getInt();
		byte[] schemaarr = new byte[schemalen];
		buffer.get(schemaarr);
		String schema = "";
		try {
		    schema = new String(schemaarr,Charset.forName(ENCODING).name());
		} catch (UnsupportedEncodingException e) {
		    // ignore
		}

		int bufferlen = buffer.getInt();
		byte[] idarr = new byte[bufferlen];
		buffer.get(idarr);
		buffer.get(); //kind
		String id = new String(idarr);
		boolean refFlag = buffer.get() == 1 ? true : false;
		return new RemoteReferenceResult(host, port, schema, id);
	} 
	
	/** TODO
	 * @return
	 * @throws RDNetworkException
	 */
	protected Result decodeRemoteVirtualReferenceResult() throws RDNetworkException{
	    int hostlen = buffer.getInt();
		byte[] hostarr = new byte[hostlen];
		buffer.get(hostarr);
		String host = new String(hostarr);
		
		int port = buffer.getInt();
		
		int schemalen = buffer.getInt();
		byte[] schemaarr = new byte[schemalen];
		buffer.get(schemaarr);
		String schema = "";
		try {
		    schema = new String(schemaarr,Charset.forName(ENCODING).name());
		} catch (UnsupportedEncodingException e) {
		 //ignore
		}

		int bufferlen = buffer.getInt();
		byte[] idarr = new byte[bufferlen];
		buffer.get(idarr);
		buffer.get(); //kind
		String id = new String(idarr);
		boolean refFlag = buffer.get() == 1 ? true : false;
		Result seed = decode();
		return new RemoteReferenceResult(host, port, schema, id);
	}
	
	// DEPRECATED ?!?!?
//////////////////////////////////////
	protected  Result decodeRemoteP2PReferenceResult() throws RDNetworkException
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
		buffer.get();
		
		String id = new String(idarr);
		
		boolean refFlag = buffer.get() == 1 ? true : false;
//mich		ReferenceResult res = null;

		//res =  new ReferenceResult(new RemoteDefaultStoreOID(id, kind, new RemoteP2PStore(peer)));
		//res.refFlag = refFlag;
		//return res;
		// where it is used??
//mich		return new RemoteReferenceResult(peer, 0, "", id);
		return new RemoteReferenceResult(peer, schema, id);

		
	}
	
/////////////////////////////
	protected Result decodeRemoteP2PVirtualReferenceResult() throws RDNetworkException{
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
		buffer.get(); //kind

		String id = new String(idarr);
		
		boolean refFlag = buffer.get() == 1 ? true : false;
		Result seed = decode();
//mich		ReferenceResult res = null;
		//		return new RemoteReferenceResult(host, port, schema, id);
//mich		return new RemoteReferenceResult(peer, 0, "", id);
		return new RemoteReferenceResult(peer, schema, id);

	}
	
	private final static String ENCODING = "UTF-8";

}
