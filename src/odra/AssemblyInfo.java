package odra;

import java.lang.reflect.Method;
import java.util.ResourceBundle;

import odra.exceptions.OdraCoreRuntimeException;

public abstract class AssemblyInfo<Type extends AssemblyInfo> {

	protected final static String CORE_RESOURCE_BUNDLE_BASE_NAME = "odra-core";

	public final static String DOT = ".";

	public final static String PROPERTY_NAME_SEPARATOR = DOT;

	private final ResourceBundle resourceBundle;

	private final ResourceBundle coreResourceBundle;

	protected AssemblyInfo(Class<Type> assemblyInfoClass, String resourceBundleBaseName) {
		this.resourceBundle = ResourceBundle.getBundle(resourceBundleBaseName);
		this.coreResourceBundle = ResourceBundle.getBundle(CORE_RESOURCE_BUNDLE_BASE_NAME);
	}

	public final ResourceBundle getResourceBundle() {
		return this.resourceBundle;
	}

	private final static String LOCALIZED_KEY_SEPARATOR = ".";

	public static String getFullyQualifiedKey(Class originClass, String key) {
		String className = originClass.getCanonicalName();
		return className + LOCALIZED_KEY_SEPARATOR + key;
	}

	private final static String ERROR_DETAILS_KEY = "ERROR_DETAILS";

	protected final String getLocalizedDetails(String details) {
		return this.getLocalizedMessageFromCoreResourceBundle(AssemblyInfo.class, ERROR_DETAILS_KEY) + ": " + details;
	}

	protected final String getLocalizedMessageFromCoreResourceBundle(Class originClass, String key) {
		return this.coreResourceBundle.getString(getFullyQualifiedKey(originClass, key));
	}

	public final String getLocalizedMessage(Class originClass, String key) {
		return this.resourceBundle.getString(getFullyQualifiedKey(originClass, key));
	}

	public final String getLocalizedMessage(Class originClass, String key, String details) {
		String localizedDetails = getLocalizedDetails(details);
		localizedDetails = (localizedDetails != null) ? ", " + localizedDetails : "";
		return this.getLocalizedMessage(originClass, key) + localizedDetails;
	}

	private final static String FACTORY_METHOD = "getInstance";

	private final static String ERROR_GETTING_FACTORY_METHOD = "ERROR_GETTING_FACTORY_METHOD";

	public static <AnotherType extends AssemblyInfo> AssemblyInfo genericGetInstance(Class<AnotherType> assemblyInfoClass)
				throws OdraCoreRuntimeException {
		try {
			Method factoryMethod = assemblyInfoClass.getDeclaredMethod(FACTORY_METHOD, (Class[]) null);
			return (AssemblyInfo) factoryMethod.invoke(null, (Object[]) null);
		} catch (Exception ex) {
			throw new OdraCoreRuntimeException(AssemblyInfo.class, ERROR_GETTING_FACTORY_METHOD, FACTORY_METHOD);
		}
	}
}