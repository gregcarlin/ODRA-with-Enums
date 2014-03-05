package odra.store.io;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import odra.store.io.IHeapExtension.ExtensionType;
import odra.transactions.store.IHeapTransactionExtension;

/**
 * An obligatory superclass for all core aspect {@link IHeap} implementations
 * 
 * @author edek
 */
public abstract class AbstractHeap implements IHeap {

	private final Map<ExtensionType, IHeapExtension> extensions;

	protected AbstractHeap() {
		this.extensions = new TreeMap<ExtensionType, IHeapExtension>();
	}

	public final IHeapExtension addExtension(IHeapExtension extension) {
		return this.extensions.put(extension.getType(), extension);
	}

	public final IHeapExtension removeExtension(IHeapExtension.ExtensionType typeExtension) {
		return this.extensions.remove(typeExtension);
	}

	public final Set<IHeapExtension> getExtensions() {
		Set<IHeapExtension> extensions = new TreeSet<IHeapExtension>();
		extensions.addAll(this.extensions.values());
		return extensions;
	}

	public final IHeapExtension getExtension(ExtensionType typeExtension) {
		return this.extensions.get(typeExtension);
	}

	public final IHeapTransactionExtension getTransactionExtension() {
		return (IHeapTransactionExtension) this.extensions.get(ExtensionType.TRANSACTIONS);
	}

	public final boolean hasExtension(ExtensionType typeExtension) {
		return this.getExtension(typeExtension) != null;
	}
}