package odra.db;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import odra.db.IDataStoreExtension.ExtensionType;
import odra.transactions.store.IDataStoreTransactionExtension;

/**
 * An abstract superclass for all core aspect {@link IDataStore} implementation.
 * 
 * @author edek
 */
public abstract class AbstractDataStore implements IDataStore {

	private final Map<ExtensionType, IDataStoreExtension> extensions;

	protected AbstractDataStore() {
		this.extensions = new TreeMap<ExtensionType, IDataStoreExtension>();
	}

	public final IDataStoreExtension addExtension(IDataStoreExtension extension) {
		return this.extensions.put(extension.getType(), extension);
	}

	public final IDataStoreExtension removeExtension(IDataStoreExtension.ExtensionType typeExtension) {
		return this.extensions.remove(typeExtension);
	}

	public final Set<IDataStoreExtension> getExtensions() {
		Set<IDataStoreExtension> extensions = new TreeSet<IDataStoreExtension>();
		extensions.addAll(this.extensions.values());
		return extensions;
	}

	public final IDataStoreExtension getExtension(ExtensionType typeExtension) {
		return this.extensions.get(typeExtension);
	}

	public final IDataStoreTransactionExtension getTransactionExtension() {
		return (IDataStoreTransactionExtension) this.extensions.get(ExtensionType.TRANSACTIONS);
	}

	public final boolean hasExtension(ExtensionType typeExtension) {
		return this.getExtension(typeExtension) != null;
	}
}