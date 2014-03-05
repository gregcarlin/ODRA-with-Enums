package tests.filters.XML;


import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.Reader;

import odra.db.Database;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.DataExporter;
import odra.filters.DataImporter;
import odra.filters.ObjectExporter;
import odra.filters.XML.M0VerboseExporter;
import odra.filters.XML.M0VerboseImporter;
import odra.filters.XML.XMLExportFilter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VerboseFilter {

	private static DBModule mod, sysmod;
	private static OID container;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sysmod = Database.getSystemModule();
		mod = new DBModule(sysmod.createSubmodule("KK", 0));
		container = mod.createComplexObject("Data", mod.getDatabaseEntry(), 0);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Test
	public void verboseXMLImport() throws Exception {	
		DataExporter xmlFilter;
		Reader fileInput;
		
		ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
		
		XMLNodeImporter nodeInterpreter = new M0VerboseImporter(mod);
		
		fileInput = new FileReader("res/xml/verbose-test-part1.xml");
		DataImporter importFilter = new XMLImportFilter( nodeInterpreter, fileInput );
		@SuppressWarnings("unused")
		OID part1[] = importFilter.importInto(container);
		
		//System.out.println( "OIDs unknown yet: "+ nodeInterpreter.getUnresolvedIdentifiers() );
		
		fileInput = new FileReader("res/xml/verbose-test-part2.xml");
		importFilter = new XMLImportFilter( nodeInterpreter, fileInput );
		@SuppressWarnings("unused")
		OID part2[] = importFilter.importInto(container);
		
		//System.out.println( "OIDs unknown yet: "+ nodeInterpreter.getUnresolvedIdentifiers() );
		
		xmlFilter = new XMLExportFilter( tmpOut, new M0VerboseExporter() );
		ObjectExporter.doExport(xmlFilter, container);
	
		//System.out.println( tmpOut.toString() );
		
		/*String result = tmpOut.toString();
		FileInputStream valInput = new FileInputStream("res/verbose-test.xml");
		byte[] valArray = new byte[valInput.available()];
		valInput.read(valArray);
		String required = new String(valArray);
		*/
		//Assert.assertEquals(required, result);
		Assert.assertTrue(nodeInterpreter.getUnresolvedIdentifiers().size() == 0);
	}
}
