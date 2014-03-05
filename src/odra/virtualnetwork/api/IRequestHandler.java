package odra.virtualnetwork.api;

/**
 * @author mich
 * This interface should be implemented on db side. It's only (extendable) its destination is
 * to handle request from remote peers.
 * Implementation of this iterface should be use in {@link LocalTransport} constructor.
 */
public interface IRequestHandler {
	//we don't need any additional infromation about source peer because 
	//transport layer is responsible for authorizations
	public byte [] handleRequest(String UserName,byte[] req);
}
