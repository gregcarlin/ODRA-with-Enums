package odra.network.encoders.messages;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import odra.network.transport.AutoextendableBuffer;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.system.config.ConfigDebug;

/**
 * This class encodes messages which are transferred between server and clients.
 * There are two methods: encodeRequestMessage (used by clients)
 * and encodeReplyMessage (used by servers).
 * 
 * The format of replies is the following:
 * - RPLY (4 bytes)
 * - number of replies (4 bytes)
 * - replies:
 *   - status code (1 byte)
 *   - length n of the query result (4 bytes)
 *   - the query result (n bytes)
 *   - length m of the error message (4 bytes)
 *   - the error message (m bytes)
 *   
 * The format of requests is the following:
 * - RQST (4 bytes)
 * - number of requests (4 bytes)
 * - replies
 *   - request id (1 byte)
 *   - number of requests (4 bytes)
 *   - parameters:
 *     - length n of the parameter (4 bytes)
 *     - parameter (n bytes)
 *     
 * Each message can store several requests/replies, which may be useful for batch operations.
 * 
 * Additionally, every message preceded by a single integer value
 * indicating the length of the whole message (including the length).
 * 
 * @author raist
 */

public class MessageEncoder {
	/**
	 * Encodes a table of replies to a serialized form
	 * that can be sent to the client as an array of bytes.
	 * @param replies replies to be encoded
	 * @return byte array containing the result of serialization
	 */
	public byte[] encodeReplyMessage(DBReply[] replies) {
		if (ConfigDebug.ASSERTS) assert replies != null : "replies == null";
		
		AutoextendableBuffer buffer = new AutoextendableBuffer();
		
		// write the message header
		buffer.put(new byte[] { 'R', 'P', 'L', 'Y' });

		// write the number of replies
		buffer.putInt(replies.length);
		
		// write the replies
		for (int i = 0; i < replies.length; i++) {
			if (ConfigDebug.ASSERTS) assert replies[i] != null && replies[i].getErrorMsg() != null && replies[i].getRawResult() != null : "parts of the reply == null";

			// write the status of 'i'-th reply
			buffer.put(replies[i].getStatus());
			
			// write the length of query result of 'i'-th reply
			buffer.putInt(replies[i].getRawResult().length);
			
			// write the query result
			buffer.put(replies[i].getRawResult());
			
			// write the message length
			buffer.putInt(replies[i].getErrorMsg().getBytes().length);

			// write the message
			buffer.put(replies[i].getErrorMsg().getBytes());
		}
		
		return buffer.getBytes();
	}
	
	/**
	 * Encodes a table of replies to a serialized form
	 * that can be sent to the server as an array of bytes.
	 * @param requests requests to be encoded
	 * @return byte array containing the result of serialization
	 */
	public byte[] encodeRequestMessage(DBRequest[] requests) {
		AutoextendableBuffer buffer = new AutoextendableBuffer();
		
		// write the message header
		buffer.put(new byte[] { 'R', 'Q', 'S', 'T' });

		// write the number of replies
		buffer.putInt(requests.length);
		
		// write the requests
		for (int i = 0; i < requests.length; i++) {
			// write the request id
			buffer.put(requests[i].opcode);

			// write the number of parameters
			buffer.putInt(requests[i].params.length);
			
			// write the parameters
			for (int j = 0; j < requests[i].params.length; j++) {
			    	byte[] byteparam = null;
				try {
				    byteparam = requests[i].params[j].getBytes(Charset.forName(ENCODING).name());
				} catch (UnsupportedEncodingException e) {
				    //ignore used only for compatibility with Java 1.4 
				}
				// write the parameter length
				buffer.putInt(byteparam.length);

				// write the parameter
				buffer.put(byteparam);
			}
		}

		return buffer.getBytes();
	}
	
	private final static String ENCODING = "UTF-8";
}
