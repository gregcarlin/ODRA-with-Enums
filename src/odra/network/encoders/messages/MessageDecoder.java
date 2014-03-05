package odra.network.encoders.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;

/**
 * This class deserializes messages which are transferred
 * from clients to servers and the other way round.
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
 * @author raist
 */

public class MessageDecoder {
	/**
	 * Deserializes a reply message.
	 * @param msg serialized reply
	 * @return table of decoded replies
	 */
	public DBReply[] decodeReplyMessage(byte[] msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(msg);
		
		// read the message header
		byte[] header = new byte[MSG_HEADER_LENGTH];
		buffer.get(header);
		if (header[0] != 'R' || header[1] != 'P' || header[2] != 'L' || header[3] != 'Y')
			throw new IOException("Cannot understand server's reply");
		
		// read the number of replies
		int reps = buffer.getInt();

		// read the replies
		DBReply[] repl = new DBReply[reps];
		for (int i = 0; i < reps; i++) {
			// read the status of 'i'-th reply
			byte stat = buffer.get();
			
			// read the query result length of 'i'-th reply
			int reslen = buffer.getInt();
			
			// read the query result
			byte[] resarr = new byte[reslen];
			buffer.get(resarr);
			
			// read the error message length
			int msglen = buffer.getInt();
			
			// read the error message
			byte[] msgarr = new byte[msglen];
			buffer.get(msgarr);

			// store the reply
			repl[i] = new DBReply(stat, resarr, new String(msgarr));
		}

		return repl;
	}
	
	/**
	 * Deserializes a message.
	 * @param msg serialized message
	 * @return table of decoded requests
	 */
	public final DBRequest[] decodeRequestMessage(byte[] msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(msg);

		// read the message header
		byte[] header = new byte[MSG_HEADER_LENGTH];
		buffer.get(header);
		if (header[0] != 'R' || header[1] != 'Q' || header[2] != 'S' || header[3] != 'T')
			throw new IOException("Cannot understand client's request");

		// read the number of requests
		int reqs = buffer.getInt();
		
		// read the requests
		DBRequest[] req = new DBRequest[reqs];
		for (int i = 0; i < reqs; i++) {
			// read the request id
			byte reqid = buffer.get();
			
			// read the number of parameters
			int pars = buffer.getInt();
			
			// read the parameters
			String[] parstr = new String[pars];
			for (int j = 0; j < pars; j++) {
				// read the parameter length
				int parlen = buffer.getInt();
				
				// read the parameter
				byte[] pararr = new byte[parlen];
				buffer.get(pararr);
				
				parstr[j] = new String(pararr, Charset.forName(ENCODING).name());
			}
			
			req[i] = new DBRequest(reqid, parstr);
		}	

		return req;
	}
	
	private final static int MSG_HEADER_LENGTH = 4;
	private final static String ENCODING = "UTF-8";
}
