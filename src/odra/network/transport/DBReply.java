package odra.network.transport;

import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.sbql.results.runtime.Result;
import odra.system.config.ConfigDebug;

/**
 * Instances of this class are constructed at the server side
 * when the server wants to communicate the result
 * of some command execution.
 * 
 * @author raist
 */

public class DBReply {
	private byte stat;
	private String msg;
	private byte[] result;
	
	/**
	 * Initializes the reply
	 * @param stat status code of the reply (e.g. STAT_OK)
	 * @param result query result. can be byte[0] if there is no query result
	 * @param msg a textual message which the client can print on screen   
	 */
	public DBReply(byte stat, byte[] result, String msg) {
		if (ConfigDebug.ASSERTS) assert result != null && msg != null : "invalid arguments (null)";

		this.stat = stat;
		this.result = result;
		this.msg = msg;
	}

	/**
	 * Creates a reply object which only indicates that a command
	 * has successfully been executed
	 * @return created reply object
	 */
	public static DBReply createOKReply() {
		return new DBReply(STAT_OK, new byte[0], "");
	}
	
	/**
	 * Creates a reply object which indicates that a command
	 * has successfuly been executed. Additionally, it also bears
	 * a query result.
	 * @param res encoded (by QueryResultEncoder) query result
	 * @return created reply object
	 */
	public static DBReply createOKReply(byte[] res) {
		if (ConfigDebug.ASSERTS) assert res != null : "res cannot be null";
		
		return new DBReply(STAT_OK, res, "");
	}

	/**
	 * Creates a reply object which indicates that a command
	 * has not been successfully executed. The reply also bears
	 * a textual message describing the nature of the error
	 * (can be printed on the screen).
	 * @param msg a message for the user
	 * @return created reply object
	 */
	public static DBReply createERRReply(String msg) {
		return createERRReply(msg, new byte[0]);
	}

	/**
	 * Creates a reply object which indicates that a command
	 * has not been successfully executed. It also bears a textual
	 * message about the nature of the error, as well as 
	 * a query result. The query result is currently used
	 * to transfer line, column and module name where the error occured. 
	 * @param msg a message for the user
	 * @param res encoded query result
	 * @return created reply object
	 */
	public static DBReply createERRReply(String msg, byte[] res) {
		if (ConfigDebug.ASSERTS) assert msg != null : "msg cannot be null";
		
		return new DBReply(STAT_INTERNAL_ERROR, res, msg);
	}

	/**
	 * Creates a reply object which indicates that a command
	 * has not been successfully executed. It also bears
	 * a textual message describing the nature of the error,
	 * as well as a specific, non-default error code.
	 * @param msg a message for the user
	 * @param errcode status code (e.g. STAT_RUNTIME_ERROR)
	 * @return created reply object
	 */
	public static DBReply createERRReply(String msg, byte errcode) {
		if (ConfigDebug.ASSERTS) assert msg != null : "msg cannot be null";
		
		return new DBReply(errcode, new byte[0], msg);
	}

	/**
	 * Creates a reply object which indicates that a command
	 * has not been successfully executed.
	 * @param msg a textual message describing the nature of the error
	 * @param res encoded query result
	 * @param errcode encode (e.g. STAT_RUNTIME_ERROR)
	 * @return created reply object
	 */	
	public static DBReply createERRReply(String msg, byte[] res, byte errcode) {
		if (ConfigDebug.ASSERTS) assert msg != null : "msg cannot be null";
		
		return new DBReply(errcode, res, msg);
	}

	/**
	 * Does the reply indicate that a command has not been successfully executed.
	 * @return true if yes, false if the command was ok
	 */
	public boolean isErrorReply() {
		return stat != STAT_OK;
	}

	/**
	 * Returns the textual message about an error, set by the server  
	 * @return
	 */
	public String getErrorMsg() {
		return msg;
	}
	
	/**
	 * Returns the status of the reply
	 * @return the status
	 */
	public byte getStatus() {
		return stat;
	}
	
	/**
	 * Returns the encoded query result of the reply
	 * @return the result
	 */
	public byte[] getRawResult() {
		return result;
	}

	/**
	 * Returns the decoded query result of the reply
	 * @return the result
	 */
	public Result getResult() throws RDNetworkException {
		return new QueryResultDecoder().decodeResult(result);
	}

	public final static byte STAT_OK = 0; // result = result of an sbql query
	public final static byte STAT_INTERNAL_ERROR = 1; // result = nothing
	public final static byte STAT_COMPILATION_ERROR = 2; // result = bag (module (string), line (integer), column (integer))
	public final static byte STAT_SECURITY_ERROR = 3; // result = nothing
	public final static byte STAT_RUNTIME_ERROR = 4; // result = bag(module (string), line (integer), column (integer))
	public final static byte STAT_DATABASE_ERROR = 5; // result = nothing
	public final static byte STAT_NETWORK_ERROR = 6; // result = nothing
	public final static byte STAT_OPTMIZATION_ERROR = 7; // result = nothing
	public final static byte STAT_WRAPPER_ERROR = 8; // result = nothing
	public final static byte STAT_PROXY_ERROR = 9; // result = nothing
	public final static byte STAT_ENDPOINT_ERROR = 10; // result = nothing
	public final static byte STAT_STALE_METABASE_ERROR = 11; // result = nothing
	public final static byte STAT_COMPARE_TEST_ERROR = 12; // result = nothing
}
