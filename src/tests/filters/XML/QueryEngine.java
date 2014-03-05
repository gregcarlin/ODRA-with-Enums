package tests.filters.XML;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.exceptions.rd.RDException;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.filters.XML.M0AnnotatedImporter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.filters.XML.XMLResultPrinter;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.RawResultPrinter;
import odra.sbql.results.runtime.Result;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import tests.BasicTests;

public class QueryEngine {

	protected static DBModule mod;
	protected static DBConnection conn; 
	
	
	
	protected static void loadXMLFile(String fname, DBModule mod) throws FileNotFoundException, DatabaseException, FilterException, ShadowObjectException
	{
		FileReader fileInput = new FileReader(fname);
		XMLNodeImporter nodeInterpreter = new M0AnnotatedImporter(mod, false, true);
		XMLImportFilter importFilter = new XMLImportFilter( nodeInterpreter, fileInput, null );
		OID result[] = importFilter.importInto(mod.getDatabaseEntry());
	}

	@BeforeClass
	public  static void setUp() throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Setting up Query Engine tests.");
		mod = Database.getModuleByName("admin");
		Thread.sleep(500);
		conn = BasicTests.dbConnect();
		ConfigServer.getLogWriter().getLogger().info("Database connection opened");

		loadXMLFile("res/xml/xquery/UC-1/bib.xml", mod);
		loadXMLFile("res/xml/xquery/UC-1/reviews.xml", mod);
		loadXMLFile("res/xml/xquery/UC-1/prices.xml", mod);
		loadXMLFile("res/xml/xquery/UC-2/book.xml", mod);
		loadXMLFile("res/xml/xquery/UC-3/patient.xml", mod);
		ConfigServer.getLogWriter().getLogger().info("XML sample files imported");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (conn!=null)
			conn.close();
	}

	@Test
	public void xmlQueryEngine() throws Exception {	
		File file = new File("res/xml/xquery");
		String fnames[] = file.list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				if (name.endsWith(".txt") && name.startsWith("q"))
					return true;
				return false;
			}});
		for (String fname: fnames)
		{
			ConfigServer.getLogWriter().getLogger().info("Excecuting test query from file: " + fname);
			String query;
			StringBuffer required = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader("res/xml/xquery/"+fname));
			query = reader.readLine();
			while ( reader.ready() )
				required.append(reader.readLine());
			Result results = BasicTests.executeQuery(conn, query);
			String resultString = BasicTests.cleanString(new XMLResultPrinter().print(results));
			String requiredString = BasicTests.cleanString(required.toString());
			if (!resultString.equals(requiredString))
			{
				ConfigServer.getLogWriter().getLogger().severe("** Required output: " + requiredString);
				ConfigServer.getLogWriter().getLogger().severe("** Query ouyput   : " + resultString);
				Assert.fail("Assertion failed. Incorrect query output in file: " + fname);
			}
		}
	}
}
