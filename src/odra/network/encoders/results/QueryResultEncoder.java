package odra.network.encoders.results;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.exceptions.rd.RDNetworkException;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.system.Sizes;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;


/**
 * This class serializes query results so that they could be sent
 * from servers to clients as byte arrays.
 * 
 * @author raist
 */

public class QueryResultEncoder {
	private byte[] hostarr, schemaarr;
	private byte[] peernamearr;

	protected QueryResultEncoder() {
	}
	
	/**
	 * Initializes the query result encoder.
	 * @param host name of the current host. used to augment simple references
	 * in order to construct network-aware references
	 * @param schema current user schema. also used to construct network-aware references
	 */
	public QueryResultEncoder(String host, String schema) {
		try {
		    this.hostarr = host.getBytes(Charset.forName(ENCODING).name());
		    this.schemaarr = schema.getBytes(Charset.forName(ENCODING).name());
		} catch (UnsupportedEncodingException e) {
		    //ignore
		}
		
	}
	
	/**
	 * Initializes the query result encoder.	 
	 * @param Peer Name of the db used to augment simple references
	 * in order to construct network-aware references
	 */
	public QueryResultEncoder(String peerName, String schema, boolean jxta) {
		try {
		    this.peernamearr = peerName.getBytes(Charset.forName(ENCODING).name());
		    this.schemaarr = schema.getBytes(Charset.forName(ENCODING).name());
		} catch (UnsupportedEncodingException e) {
		    //ignore
		}
		
	}	
		
	/**
	 * Serializes a query result into an array of bytes
	 * @param res result which be serialized
	 * @return array of bytes containing serialized query result 
	 */
	public byte[] encodeResult(Result res) throws RDNetworkException {
		if (ConfigDebug.ASSERTS) assert res != null : "res == null";

		ByteBuffer buf = null;
		
		if (res instanceof IntegerResult) {
			IntegerResult ires = (IntegerResult) res;

			buf = ByteBuffer.allocate(Sizes.INTVAL_LEN + RES_KIND_LEN);
			buf.put(Result.INTEGER_RESULT);
			buf.putInt(ires.value);
		}
		else if (res instanceof DoubleResult) {
			DoubleResult dres = (DoubleResult) res;

			buf = ByteBuffer.allocate(Sizes.DOUBLEVAL_LEN + RES_KIND_LEN);
			buf.put(Result.DOUBLE_RESULT);
			buf.putDouble(dres.value);
		}
		else if (res instanceof BooleanResult) {
			BooleanResult bres = (BooleanResult) res;
			
			buf = ByteBuffer.allocate(Sizes.BOOLEAN_LEN + RES_KIND_LEN);
			buf.put(Result.BOOLEAN_RESULT);
			buf.put(bres.value ? (byte) 1 : (byte) 0);
		}
		else if (res instanceof StringResult) {
			StringResult sres = (StringResult) res;

			byte[] strarr = null;
			try {
			    strarr = sres.value.getBytes(Charset.forName(ENCODING).name());
			} catch (UnsupportedEncodingException e) {
			    //ignore
			}

			buf = ByteBuffer.allocate(Sizes.INTVAL_LEN + strarr.length + RES_KIND_LEN);
			buf.put(Result.STRING_RESULT);
			buf.putInt(strarr.length);
			buf.put(strarr);
		}
		else if (res instanceof VirtualReferenceResult) {
		    buf = this.encodeVirtualReferenceResult((VirtualReferenceResult)res);
		}
		else if (res instanceof ReferenceResult) {
		    buf = this.encodeReferenceResult((ReferenceResult)res);			
		}
		else if (res instanceof StructResult) {
			StructResult sres = (StructResult) res;
			
			SingleResult[] fields = sres.fieldsToArray();
			byte[][] fieldsb = new byte[fields.length][];
			
			int flen = 0;
			for (int i = 0; i < fields.length; i++) {
				fieldsb[i] = encodeResult(fields[i]);
				flen += fieldsb[i].length;
			}

			buf = ByteBuffer.allocate(flen + Sizes.INTVAL_LEN + RES_KIND_LEN);
			buf.put(Result.STRUCT_RESULT);
			buf.putInt(fields.length);
			
			for (int i = 0; i < fields.length; i++)
				buf.put(fieldsb[i]);
		}
		else if (res instanceof BinderResult) {
			BinderResult bres = (BinderResult) res;

			byte[] bname = encodeResult(new StringResult(bres.getName())); 
			byte[] bval = encodeResult(bres.value);

			buf = ByteBuffer.allocate(bname.length + bval.length + RES_KIND_LEN);
			buf.put(Result.BINDER_RESULT);
			buf.put(bname);
			buf.put(bval);
		}
		else if (res instanceof BagResult) {
			BagResult sres = (BagResult) res;

			SingleResult[] elements = sres.elementsToArray();
			byte[][] elementsb = new byte[elements.length][];
			
			int flen = 0;
			for (int i = 0; i < elements.length; i++) {
				elementsb[i] = encodeResult(elements[i]);
				flen += elementsb[i].length;
			}

			buf = ByteBuffer.allocate(flen + Sizes.INTVAL_LEN + RES_KIND_LEN);
			buf.put(Result.BAG_RESULT);
			buf.putInt(elements.length);

			for (int i = 0; i < elements.length; i++)
				buf.put(elementsb[i]);	
		}
		else if (res instanceof DateResult) {
			DateResult dres = (DateResult) res;
			
			buf = ByteBuffer.allocate(Sizes.LONGVAL_LEN + RES_KIND_LEN);
			buf.put(Result.DATE_RESULT);
			buf.putLong(dres.value.getTime());
		}
		else
			throw new RDNetworkException("Invalid query result type"); 

		buf.flip();
		
		return buf.array();
	}
	
	
	protected ByteBuffer encodeReferenceResult(ReferenceResult rres) throws RDNetworkException {
	    if (!((hostarr == null || schemaarr == null)||( peernamearr == null)))
		throw new RDNetworkException("Invalid query result encoder");

        	ByteBuffer buf;
        
        	byte[] idarr = this.serializeOID(rres.value);
        	byte kind = this.getObjectKind(rres.value);
        	
        
        	if (peernamearr!=null){
        		//encode reply for grid
        		buf = ByteBuffer.allocate(encodedReferenceResultHeaderLengthJXTA() + idarr.length);
        		buf.put(Result.REMOTE_REFERENCE_RESULT_P2P);
        		buf.putInt(peernamearr.length);
        		buf.put(peernamearr);      
        		buf.putInt(schemaarr.length);
        		buf.put(schemaarr);
        	}else {
        		//otherwise
        		buf = ByteBuffer.allocate(encodedReferenceResultHeaderLength() + idarr.length);
        		buf.put(Result.REMOTE_REFERENCE_RESULT);
        		buf.putInt(hostarr.length);
        		buf.put(hostarr);
        		buf.putInt(ConfigServer.LSNR_PORT);
        		buf.putInt(schemaarr.length);
        		buf.put(schemaarr);
        	}
        	buf.putInt(idarr.length);
        	buf.put(idarr);
        	buf.put(kind);
        	buf.put(rres.refFlag ? (byte)1 : (byte)0);        	
        	return buf;
	}
	
	protected ByteBuffer encodeVirtualReferenceResult(VirtualReferenceResult rres) throws RDNetworkException {
    	    if (!((hostarr == null || schemaarr == null)||( peernamearr == null)))
    		throw new RDNetworkException("Invalid query result encoder");
    	    ByteBuffer buf;
    	    byte[] seed = this.encodeResult(rres.getSeed());
    	    byte[] idarr = this.serializeOID(rres.value);
    	    byte kind = this.getObjectKind(rres.value);

    	    if (peernamearr!=null){
    		//encode reply for jxta
    		buf = ByteBuffer.allocate(encodedReferenceResultHeaderLengthJXTA()+ idarr.length  + seed.length);
    		buf.put(Result.REMOTE_VIRTUAL_REFERENCE_RESULT_P2P);
    		buf.putInt(peernamearr.length);
    		buf.put(peernamearr);
    		buf.putInt(schemaarr.length);
    		buf.put(schemaarr);
    	}else {
    		//otherwise
    		buf = ByteBuffer.allocate(encodedReferenceResultHeaderLength() + idarr.length  + seed.length);
    		buf.put(Result.REMOTE_VIRTUAL_REFERENCE_RESULT);
    		buf.putInt(hostarr.length);
    		buf.put(hostarr);
    		buf.putInt(ConfigServer.LSNR_PORT);
    		buf.putInt(schemaarr.length);
    		buf.put(schemaarr);
    	}
    	buf.putInt(idarr.length);
    	buf.put(idarr);
    	buf.put(kind);
    	buf.put(rres.refFlag ? (byte)1 : (byte)0);
    	buf.put(seed);	    
    	return buf;
	}
	
	
	protected byte[] serializeOID(OID value){
	    return String.valueOf(value.internalOID()).getBytes();
	}
	protected byte getObjectKind(OID value) throws RDNetworkException{
	    try
		{
		    return value.getObjectKind().getKindAsByte();
		} catch (DatabaseException e)
		{
		    throw new RDNetworkException(e.getMessage());
		}
	}
	
	private final int encodedReferenceResultHeaderLength(){
	    	return	RES_KIND_LEN + //kind 
	    		Sizes.INTVAL_LEN + hostarr.length + //host 
	    		Sizes.INTVAL_LEN + schemaarr.length + //schema
	    		Sizes.INTVAL_LEN + //port	    	
	    		Sizes.INTVAL_LEN + //id length
	    		
	    		Sizes.BOOLEAN_LEN + //ref flag
	    		Sizes.BYTEVAL_LEN; //object kind
	}
	private final int encodedReferenceResultHeaderLengthJXTA(){
    	return	RES_KIND_LEN + //kind 
    		Sizes.INTVAL_LEN + peernamearr.length + //peerName 
    		Sizes.INTVAL_LEN + schemaarr.length + //schema
    		Sizes.INTVAL_LEN + //id length
    		
    		Sizes.BOOLEAN_LEN + //ref flag
    		Sizes.BYTEVAL_LEN; //object kind
	}

	
	protected final static int RES_KIND_LEN = 1;
	private final static String ENCODING = "UTF-8";
}
