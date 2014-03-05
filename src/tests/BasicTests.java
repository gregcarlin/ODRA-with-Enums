package tests;

import java.io.IOException;

import odra.exceptions.rd.RDException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.Result;
import odra.system.Main;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		tests.filters.XML.M0DefaultFilter.class, 
		tests.filters.XML.VerboseFilter.class,
		tests.filters.XML.XSLTFilter.class,
		tests.filters.XML.NamespaceStack.class,
		tests.filters.XML.AnnotatingNamespace.class,
		tests.filters.XML.QueryEngine.class,
		tests.filters.XML.XSDTest.class
})
public class BasicTests {

	static long start, stop;

	@BeforeClass
	public static void setUp() throws Exception {
		new Main().createDatabase("/tmp/test.dbf", 1024 * 1024 * 10);
		new Main().startDatabaseInstance("/tmp/test.dbf");
		ConfigServer.getLogWriter().getLogger().info("Database created");
		ConfigServer.getLogWriter().getLogger().info("Starting jOdra tests.");
		start = System.currentTimeMillis();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		stop = System.currentTimeMillis();
		ConfigServer.getLogWriter().getLogger().info("Finishing jOdra tests. (" + ((stop - start) / 1000F) + "s.)");
	}

	public static String cleanString(String resultString)
	{
		resultString = resultString.replace(" ", "");
		resultString = resultString.replace("\n", "");
		resultString = resultString.replace("\t", "");
		resultString = resultString.replace("\r", "");
		return resultString;
	}
	
	public static DBConnection dbConnect() throws IOException, RDException
	{
		String user = "admin";
		String passwd = "admin";

		DBConnection db = new DBConnection( "127.0.0.1", 1521);
		db.connect();
		DBRequest req =  new DBRequest(DBRequest.LOGIN_RQST, new String[] { user, passwd });
		db.sendRequest(req); 

		return db;
	}
	
	public static Result executeQuery(DBConnection db, String query) throws IOException, RDException
	{
		String currmod = "admin";
		DBRequest req = new DBRequest(DBRequest.EXECUTE_SBQL_RQST, new String[] { query, currmod, "default", "on" });
		DBReply rep = db.sendRequest(req);

		byte[] rawres = rep.getRawResult();
		
		QueryResultDecoder decoder = new QueryResultDecoder();
		return decoder.decodeResult(rawres);
	}
}
