package odra.store.guid;

public interface IGUIDIdentifiableResource extends Comparable<IGUIDIdentifiableResource> {

	GUID getGUID();
}