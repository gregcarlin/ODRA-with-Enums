package odra.store.sbastore;

import java.util.Set;

import odra.store.io.IHeap;
import odra.store.io.IHeapExtension;
import odra.transactions.store.IHeapTransactionExtension;

/**
 * An obligatory superclass for all {@link IObjectManagerExtension} implementations.
 * 
 * @author edek
 */
public abstract class AbstractObjectManagerExtension implements IObjectManagerExtension {

	private final ExtensionType typeExtension;

	protected AbstractObjectManagerExtension(ExtensionType typeExtension) {
		this.typeExtension = typeExtension;
	}

	public final ExtensionType getType() {
		return this.typeExtension;
	}

	public final int compareTo(AbstractObjectManagerExtension otherExtension) {
		return this.getType().compareTo(otherExtension.getType());
	}
}