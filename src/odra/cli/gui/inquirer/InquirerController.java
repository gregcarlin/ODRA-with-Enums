package odra.cli.gui.inquirer;

import java.io.IOException;

import odra.cli.CLI;
import odra.cli.CLIVariable;
import odra.exceptions.rd.RDException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.Result;

/**
 * 
 * @author raist
 */

public class InquirerController {
	private InquirerFrame frame = new InquirerFrame(this);;
	private CLI cli;
	private String currmod;

	public InquirerController(CLI cliref) {
		this.cli = cliref;		
	}

	public void openEditableInquirer() throws RDException, IOException {
		frame.setEditable(true);
		frame.setVisible(true);
	}

	public void openReadOnlyInquirer() throws RDException, IOException {
		frame.setEditable(false);
		frame.setVisible(true);
	}
	
	public void closeInquirer() {
		frame.setVisible(false);
	}

	public void executeQuery(String query, String module) throws IOException, RDException {
		DBRequest req = new DBRequest(DBRequest.EXECUTE_SBQL_RQST, new String[] { query, module, "on", cli.getVar(CLIVariable.TEST)});
		DBReply rep = cli.getConnection().sendRequest(req);

		byte[] rawres = rep.getRawResult();

		QueryResultDecoder decoder = new QueryResultDecoder();
		Result res = decoder.decodeResult(rawres);

		frame.setResult(res);
	}
	
	/**
	 * Returns an associated CLI instance reference.
	 * 
	 * @return CLI
	 */
	CLI getCLI()
	{
		return cli;
	}
}
