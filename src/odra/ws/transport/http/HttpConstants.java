package odra.ws.transport.http;


/**
 * Common values for HTTP protocol message handling purposes
 * 
 * @since 2006-12-12
 * @version 2007-06-22
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public interface HttpConstants {
	// http request method types
	static final String GET_METHOD = "GET";
	static final String POST_METHOD = "POST";
	static final String HEAD_METHOD = "HEAD";
	static final String PUT_METHOD = "PUT";
	static final String DELETE_METHOD = "DELETE";

	static final int HTTP_OK = 200;
	static final int HTTP_ERROR = 501; 
	
	static final String CONTENT_LENGTH_HEADER = "Content-Length";
	static final String CONTENT_TYPE_HEADER = "Content-Type";
}
