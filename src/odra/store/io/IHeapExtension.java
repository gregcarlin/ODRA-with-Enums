package odra.store.io;

public interface IHeapExtension extends Comparable<AbstractHeapExtension> {

	ExtensionType getType();

	public enum ExtensionType {
		TRANSACTIONS;
	}
}