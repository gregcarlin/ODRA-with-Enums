package odra.store.sbastore;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import odra.store.io.IHeap;
import odra.store.memorymanagement.IMemoryManager;
import odra.store.memorymanagement.AbstractMemoryManager;
import odra.store.sbastore.IObjectManagerExtension.ExtensionType;
import odra.transactions.store.IObjectManagerTransactionExtension;

/**
 * An abstract superclass for all core aspect {@link IObjectManager} implementation.
 * 
 * @author edek
 */
public abstract class AbstractObjectManager implements IObjectManager {

	protected final IMemoryManager allocator;

	protected final IHeap heap;
	
	private final Map<ExtensionType, IObjectManagerExtension> extensions;

	protected AbstractObjectManager(AbstractMemoryManager allocator) {
		this.allocator = allocator;
		this.heap = allocator.getHeap();
		
		this.extensions = new TreeMap<ExtensionType, IObjectManagerExtension>();
	}

	public final IObjectManagerExtension addExtension(IObjectManagerExtension extension) {
		return this.extensions.put(extension.getType(), extension);
	}

	public final IObjectManagerExtension removeExtension(IObjectManagerExtension.ExtensionType typeExtension) {
		return this.extensions.remove(typeExtension);
	}

	public final Set<IObjectManagerExtension> getExtensions() {
		Set<IObjectManagerExtension> extensions = new TreeSet<IObjectManagerExtension>();
		extensions.addAll(this.extensions.values());
		return extensions;
	}

	public final IObjectManagerExtension getExtension(ExtensionType typeExtension) {
		return this.extensions.get(typeExtension);
	}

	public final IObjectManagerTransactionExtension getTransactionExtension() {
		return (IObjectManagerTransactionExtension) this.extensions.get(ExtensionType.TRANSACTIONS);
	}

	public final boolean hasExtension(ExtensionType typeExtension) {
		return this.getExtension(typeExtension) != null;
	}

	public final IMemoryManager getMemoryManager() {
		return this.allocator;
	}

	public final IHeap getHeap() {
		return this.heap;
	}

}