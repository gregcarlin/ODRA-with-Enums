package odra.db;

public interface IDataStoreExtension extends Comparable<AbstractDataStoreExtension> {

	ExtensionType getType();

	public enum ExtensionType {
		TRANSACTIONS;
	}
}