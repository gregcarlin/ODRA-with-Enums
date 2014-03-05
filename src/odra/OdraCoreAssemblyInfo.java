package odra;

public final class OdraCoreAssemblyInfo extends AssemblyInfo<OdraCoreAssemblyInfo> {

	private final static OdraCoreAssemblyInfo singleton;

	private final static String RESOURCE_BUNDLE_BASE_NAME = CORE_RESOURCE_BUNDLE_BASE_NAME;

	static {
		singleton = new OdraCoreAssemblyInfo();
	}

	private OdraCoreAssemblyInfo() {
		super(OdraCoreAssemblyInfo.class, RESOURCE_BUNDLE_BASE_NAME);
	}

	public static OdraCoreAssemblyInfo getInstance() {
		return singleton;
	}
}