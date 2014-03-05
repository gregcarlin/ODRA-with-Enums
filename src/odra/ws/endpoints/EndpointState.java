package odra.ws.endpoints;


/**
 * Represents possible endpoint states. 
 * 
 * 
 * @since 2007-01-05
 * @version 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public enum EndpointState {
	/** Indicates, that endpoint is operating */
	STARTED,
	/** Indicates, that endpoint has been suspended */
	STOPPED,
	/** Represents unknown endpoint state, probably due to internal database error */
	UNKNOWN

}
