package odra.db;

/**
 * An obligatory superclass for all {@link IDataStoreExtension} implementations.
 * 
 * @author edek
 */
public abstract class AbstractDataStoreExtension implements IDataStoreExtension {

	private final ExtensionType typeExtension;

	protected AbstractDataStoreExtension(ExtensionType typeExtension) {
		this.typeExtension = typeExtension;
	}

	public final ExtensionType getType() {
		return this.typeExtension;
	}

	public final int compareTo(AbstractDataStoreExtension otherExtension) {
		return this.getType().compareTo(otherExtension.getType());
	}
}