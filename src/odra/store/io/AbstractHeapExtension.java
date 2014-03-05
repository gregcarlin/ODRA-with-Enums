package odra.store.io;

import odra.store.io.IHeapExtension.ExtensionType;

/**
 * An obligatory abstract superclass for all {@link IHeapExtension} implementations.
 * 
 * @author edek
 */
public abstract class AbstractHeapExtension implements IHeapExtension {

	private final ExtensionType typeExtension;

	protected AbstractHeapExtension(ExtensionType typeExtension) {
		this.typeExtension = typeExtension;
	}

	public final ExtensionType getType() {
		return this.typeExtension;
	}

	public final int compareTo(AbstractHeapExtension otherExtension) {
		return this.getType().compareTo(otherExtension.getType());
	}
}