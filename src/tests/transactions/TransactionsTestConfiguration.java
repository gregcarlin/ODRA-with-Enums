package tests.transactions;

import java.io.File;

public final class TransactionsTestConfiguration {

	final static String DATABASE_FILE = "test.dbf";

	final static int DATABASE_SIZE = 1024 * 1024;

	private final static String BATCH_DIRECTORY = "res/transactions";

	final static String BATCH_FILE01 = BATCH_DIRECTORY + File.separator + "transaction.cli";

	final static String BATCH_FILE02 = BATCH_DIRECTORY + File.separator + "transaction-createM0.cli";
}