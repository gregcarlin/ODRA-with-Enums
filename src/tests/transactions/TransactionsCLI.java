package tests.transactions;

import java.io.IOException;

import odra.cli.CLI;
import odra.cli.batch.BatchException;

public class TransactionsCLI extends CLI {

	public void execBatch(String batchFile) throws IOException, BatchException {
		super.execBatch(new String[] { batchFile });
	}
}