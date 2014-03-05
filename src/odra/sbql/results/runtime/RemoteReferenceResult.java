package odra.sbql.results.runtime;

/**
 * Objects of this class does not really appear on qres.
 * They are only used to send reference results to 'user interface ' clients.
 * @author raist
 *restored by radamus 04.07.07
 */

public class RemoteReferenceResult extends SingleResult {
	public String host;
	public int port;
	public String schema;
	public String id;
	public String peer;

	public RemoteReferenceResult(String host, int port, String schema, String id) {
		this.host = host;
		this.port = port;
		this.schema = schema;
		this.id = id;
	}
	
	public RemoteReferenceResult(String peer, String schema, String id) {
		this.peer = peer;
		this.schema = schema;
		this.id = id;
	}


	public SingleResult[] fieldsToArray() {
		return new SingleResult[] { this };
	}
}
