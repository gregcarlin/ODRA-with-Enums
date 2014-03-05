package tests.filters.XML;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;

import odra.db.Database;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.ObjectExporter;
import odra.filters.XML.M0DefaultExporter;
import odra.filters.XML.M0DefaultImporter;
import odra.filters.XML.XMLExportFilter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.filters.XML.XMLTransformer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class XSLTFilter {

	private static DBModule mod, sysmod;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sysmod = Database.getSystemModule();
		mod = new DBModule(sysmod.createSubmodule("KK", 0));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Test
	public void testXSLTImport() throws Exception {	
		XMLExportFilter xmlFilter;
		FileReader fileInput;
		ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
		
		XMLTransformer transformer = new XMLTransformer( new FileInputStream("res/xml/bookstore-import.xslt") );
		XMLTransformer retransformer = new XMLTransformer( new FileInputStream("res/xml/bookstore-export.xslt") );
		XMLNodeImporter nodeInterpreter = new M0DefaultImporter(mod, true, false);
		fileInput = new FileReader("res/xml/bookstore.xml");
		XMLImportFilter importFilter = new XMLImportFilter( nodeInterpreter, fileInput );
		importFilter.setTransformer(transformer);
		OID result[] = importFilter.importInto(mod.getDatabaseEntry());
	
		Assert.assertTrue(nodeInterpreter.getUnresolvedIdentifiers().size() == 0);
		
		xmlFilter = new XMLExportFilter( tmpOut, new M0DefaultExporter(false) );
		xmlFilter.SetTransformer(retransformer);
		ObjectExporter.doExport(xmlFilter, result[0] );
	
		String resultString= tmpOut.toString();
		FileInputStream valInput = new FileInputStream("res/xml/bookstore.xml");
		byte[] valArray = new byte[valInput.available()];
		valInput.read(valArray);
		String required = new String(valArray);
		
		Assert.assertEquals(required, resultString);

	}
}
