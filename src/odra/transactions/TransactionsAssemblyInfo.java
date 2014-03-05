package odra.transactions;

import odra.AssemblyInfo;

public final class TransactionsAssemblyInfo extends AssemblyInfo<TransactionsAssemblyInfo> {

	private final static TransactionsAssemblyInfo singleton;

	private final static String RESOURCE_BUNDLE_BASE_NAME = "odra-transactions";

	static {
		singleton = new TransactionsAssemblyInfo();
	}

	private TransactionsAssemblyInfo() {
		super(TransactionsAssemblyInfo.class, RESOURCE_BUNDLE_BASE_NAME);
	}

	public static TransactionsAssemblyInfo getInstance() {
		return singleton;
	}
}