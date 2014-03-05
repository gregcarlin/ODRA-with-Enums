package tests.filters.XML;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.DataExporter;
import odra.filters.DataImporter;
import odra.filters.ObjectExporter;
import odra.filters.XML.M0AnnotatedExporter;
import odra.filters.XML.M0AnnotatedImporter;
import odra.filters.XML.XMLExportFilter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import tests.BasicTests;

/**
 * XML testing class
 * 
 * @author Krzysztof Kaczmarski
 * 
 */
public class AnnotatingNamespace {

   static private DBModule mod, sysmod;

   OID objectsContainer, test;

   @BeforeClass
   public static void setUp() throws DatabaseException {
      sysmod = Database.getSystemModule();
      mod = new DBModule(sysmod.createSubmodule("KK", 0));
   }

   public void createObjects() throws Exception {
      int n = 3;

      objectsContainer = mod.createComplexObject("container", mod.getDatabaseEntry(), 0);
      OID[] emp = new OID[n];
      OID[] dog = new OID[n];

      for (int i = 0; i < n; i++) {
         emp[i] = mod.createComplexObject("emp", objectsContainer, 0);
         mod.createStringObject("name", emp[i], (i % 2) == 0 ? "Abacki" : "Babacka", 0);
         mod.createIntegerObject("salary", emp[i], 1000 + i);
         mod.createDoubleObject("weight", emp[i], 71 + 0.01 * i);
         // mod.createReferenceObject("likes", emp[i], emp[i]);
         mod.createStringObject("polish", emp[i], Common.POLISH_NATIVE_CHARACTERS_TEST_STRING, 0);
         mod.createBooleanObject("male", emp[i], (i % 2) == 0);
         dog[i] = mod.createComplexObject("dog", emp[i], 0);
         mod.createStringObject("color", dog[i], "yellow", 0);
         // mod.createReferenceObject("owner", dog[i], emp[i]);
      }

      ConfigServer.getLogWriter().getLogger().fine("XML Export test objects created");
   }

   public void removeObjects() throws DatabaseException {
      objectsContainer.delete();
      ConfigServer.getLogWriter().getLogger().fine("XML Export test objects removed");
   }

   @Test
   public void annotatedImportExport() throws Exception {
      createObjects();

      ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
      FileReader fileInput = new FileReader("res/xml/annotated-test.xml");

      XMLNodeImporter nodeInterpreter = new M0AnnotatedImporter(mod, true, true);
      DataImporter importFilter = new XMLImportFilter(nodeInterpreter, fileInput);
      OID result[] = importFilter.importInto(mod.getDatabaseEntry());

      DataExporter xmlFilter;
      xmlFilter = new XMLExportFilter(tmpOut, new M0AnnotatedExporter());
      ObjectExporter.doExport(xmlFilter, result[0]);

      FileInputStream valInput = new FileInputStream("res/xml/annotated-test.xml");
      String validated = tmpOut.toString();
      byte[] valArray = new byte[valInput.available()];
      valInput.read(valArray);
      String required = new String(valArray);

      removeObjects();

      Assert.assertEquals(BasicTests.cleanString(required), BasicTests.cleanString(validated));
   }

   @Test
   public void exportPureM0() throws Exception {
      createObjects();

      ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
      FileInputStream valInput = new FileInputStream("res/xml/m0-export-test.xml");

      DataExporter xmlFilter;
      xmlFilter = new XMLExportFilter(tmpOut, new M0AnnotatedExporter());
      ObjectExporter.doExport(xmlFilter, objectsContainer);

      String result = tmpOut.toString();
      byte[] valArray = new byte[valInput.available()];
      valInput.read(valArray);
      String required = new String(valArray);

      Assert.assertEquals(required, result);

      removeObjects();
   }
}
