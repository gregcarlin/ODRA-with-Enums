package tests.filters.XML;

import java.io.FileReader;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.filters.DataExporter;
import odra.filters.DataImporter;
import odra.filters.ObjectExporter;
import odra.filters.XML.M0AnnotatedExporter;
import odra.filters.XML.M0AnnotatedImporter;
import odra.filters.XML.XMLExportFilter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.system.Main;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;

/**
 * XML testing class
 * 
 * @author Krzysztof Kaczmarski
 *
 */public class NamespaceTest {
	private DBModule mod;	

	OID objectsContainer, test;
	
	public void start() throws DatabaseException
	{
		mod = Database.getModuleByName("admin");
	}
	
	private void namespaceReading() throws Exception {	
		long start, stop;
		FileReader fileInput;
		start = System.currentTimeMillis();
				
		fileInput = new FileReader("res/xml/annotated-test.xml");
		
		ConfigServer.getLogWriter().getLogger().fine("NAMESPACE READING TEST");
		XMLNodeImporter nodeInterpreter = new M0AnnotatedImporter(mod, false, false);
		DataImporter importFilter = new XMLImportFilter( nodeInterpreter, fileInput );
		OID result[] = importFilter.importInto(mod.getDatabaseEntry());
	
		stop = System.currentTimeMillis();
		ConfigServer.getLogWriter().getLogger().info("XML import time: " + ((stop - start) / 1000F) + "s");
	
		DataExporter xmlFilter;
		
		start = System.currentTimeMillis();		
		xmlFilter = new XMLExportFilter( System.out , new M0AnnotatedExporter() );
		ObjectExporter.doExport(xmlFilter, result[0]);
		stop = System.currentTimeMillis();
		ConfigServer.getLogWriter().getLogger().info("XML export time: " + ((stop - start) / 1000F) + "s");
	}
	
	public static void main(String[] args) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("XML Namespaces Reading Test started (debug mode: " + ConfigDebug.ASSERTS + ")");
		
		NamespaceTest main = new NamespaceTest();
		new Main().createDatabase("/tmp/test.dbf", 1024 * 1024 * 10);
		
		main.start();
		main.namespaceReading();
		
		DBInstance instance = new DBInstance();
		instance.startup(); 
		CLI cli = new CLI();
		cli.begin();

		instance.shutdown();
	}
}
