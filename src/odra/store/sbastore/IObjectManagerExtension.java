package odra.store.sbastore;

public interface IObjectManagerExtension extends Comparable<AbstractObjectManagerExtension> {

	ExtensionType getType();

	public enum ExtensionType {
		TRANSACTIONS;
	}
}