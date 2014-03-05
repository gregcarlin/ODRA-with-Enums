package tests.filters.XML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import nu.xom.Builder;
import nu.xom.Document;
import odra.cli.CLI;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBAnnotatedObject;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.dbinstance.DBInstance;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.filters.XML.M0AnnotatedImporter;
import odra.filters.XML.M0DefaultImporter;
import odra.filters.XML.M0TypedImporter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.filters.XML.XMLResultPrinter;
import odra.filters.XSD.XSDImportFilter;
import odra.filters.XSD.XSDSchemaInterpreter;
import odra.network.transport.DBConnection;
import odra.sbql.builder.ModuleCompiler;
import odra.sbql.builder.ModuleLinker;
import odra.sbql.results.runtime.Result;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.sbastore.ObjectManager;
import odra.system.Main;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;
import tests.BasicTests;

/**
 * XML testing class
 * 
 * @author Krzysztof Kaczmarski
 *
 */
public class XSDTest {

	protected static DBModule mod;
	protected static DBConnection conn; 
	
	OID objectsContainer, test;
	
	@BeforeClass
	public  static void setUp() throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Setting up XSD Engine tests.");
		mod = Database.getModuleByName("admin");
		Thread.sleep(500);
		conn = BasicTests.dbConnect();
		ConfigServer.getLogWriter().getLogger().info("Database connection opened");

		loadXSDFile( "res/xml/xquery/UC-9/address2.xsd", mod );
		loadXSDFile( "res/xml/xquery/UC-9/ipo2.xsd", mod );
		loadXSDFile( "res/xml/personnel.xsd", mod);

		loadXMLFile("res/xml/personnel.xml", mod);
		loadXMLFile("res/xml/xquery/UC-9/ipo2.xml", mod);

		ConfigServer.getLogWriter().getLogger().info("XSD and XML sample files imported");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (conn!=null)
			conn.close();
	}

	@Test
	public void test1() throws Exception{
		xsdTestingEngine("(personnel.person where id._VALUE=\"one.worker\").salary;", "<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><RESULT>10000.0</RESULT>");
		xsdTestingEngine("personnel.person.id;","<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><RESULT>Big.Bossone.workertwo.workerthree.workerfour.workerfive.worker</RESULT>");
		xsdTestingEngine("personnel.person.id._VALUE;","<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><RESULT>Big.Bossone.workertwo.workerthree.workerfour.workerfive.worker</RESULT>");
	}
	
	public void xsdTestingEngine(String query, String required) throws Exception {	
		
		//System.out.println("DOING TEST:" + query);
		Result results = BasicTests.executeQuery(conn, query);
		String resultString = BasicTests.cleanString(new XMLResultPrinter().print(results));
		//System.out.println(resultString);
		String requiredString = BasicTests.cleanString(required);
		if (!resultString.equals(requiredString))
		{
			ConfigServer.getLogWriter().getLogger().severe("** Required output: " + requiredString);
			ConfigServer.getLogWriter().getLogger().severe("** Query ouyput   : " + resultString);
			Assert.fail("Assertion failed. Incorrect query output for query: " + query);
		}
		
	}
	
	protected static void loadXMLFile(String fname, DBModule mod) throws FileNotFoundException, DatabaseException, FilterException, ShadowObjectException
	{
		FileReader fileInput = new FileReader(fname);
		XMLNodeImporter nodeInterpreter = new M0TypedImporter(mod);
		XMLImportFilter importFilter = new XMLImportFilter( nodeInterpreter, fileInput, null );
		OID result[] = importFilter.importInto(mod.getDatabaseEntry());
	}

	public static  void loadXSDFile(String fname, DBModule destSchemaModule) throws FileNotFoundException, DatabaseException, FilterException, ShadowObjectException 
	{
		ConfigServer.getLogWriter().getLogger().info("Performing XSD schema import.");
		FileReader fileInput = new FileReader(fname);
		XSDSchemaInterpreter nodeInterpreter = new XSDSchemaInterpreter( destSchemaModule, false, false, false );
		XSDImportFilter importFilter = new XSDImportFilter( nodeInterpreter, fileInput );
		OID result[] = importFilter.importSchema( destSchemaModule );
		ConfigServer.getLogWriter().getLogger().fine("Parsing finished.");
	}
}
	

