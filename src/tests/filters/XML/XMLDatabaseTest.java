package tests.filters.XML;

import java.io.FileReader;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBAnnotatedObject;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.filters.DataExporter;
import odra.filters.DataImporter;
import odra.filters.XML.M0AnnotatedImporter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.filters.XSD.XSDImportFilter;
import odra.filters.XSD.XSDSchemaInterpreter;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;

/**
 * XML testing class
 * 
 * @author Krzysztof Kaczmarski
 * 
 */
public class XMLDatabaseTest {

   private static DBModule mod, sysmod;

   private void speedTest() throws DatabaseException {
      DBModule mod;
      mod = Database.getModuleByName("admin");
      long totalStart, sbaStart;

      OID parent = mod.getDatabaseEntry();
      totalStart = System.currentTimeMillis();
      for (int i = 0; i < 200000; i++) {
         sbaStart = System.currentTimeMillis();
         parent = mod.createComplexObject("object" + i, parent, 0);
         System.out.println(i + " >>sba time : " + (System.currentTimeMillis() - sbaStart) / 1000F);
      }
      System.out.println(" total time : " + (System.currentTimeMillis() - totalStart) / 1000F);
   }

   private void XMLDatabaseImport() throws Exception {
      long start, stop;
      DBModule mod;
      DataExporter xmlFilter;
      FileReader fileInput;
      mod = Database.getModuleByName("admin");

      fileInput = new FileReader("res/xml/database.xsd");
      XSDSchemaInterpreter xsdInterpreter = new XSDSchemaInterpreter(mod, false, false, false);
      XSDImportFilter xsdImportFilter = new XSDImportFilter(xsdInterpreter, fileInput);
      OID result[] = xsdImportFilter.importSchema(mod);

      start = System.currentTimeMillis();
      // XMLNodeImporter nodeInterpreter = new M0DefaultImporter(mod, true, true);
      XMLNodeImporter nodeInterpreter = new M0AnnotatedImporter(mod, true, true);
      // XMLNodeImporter nodeInterpreter = new M0TypedImporter(mod);
      fileInput = new FileReader("res/xml/database-medium.xml");
      DataImporter importFilter = new XMLImportFilter(nodeInterpreter, fileInput);
      result = importFilter.importInto(mod.getDatabaseEntry());

      ConfigServer.getLogWriter().getLogger().fine("OIDs unknown yet: " + nodeInterpreter.getUnresolvedIdentifiers());
      ConfigServer.getLogWriter().getLogger().info("Nodes imported: " + nodeInterpreter.getProcessedNodesCount());

      stop = System.currentTimeMillis();
      ConfigServer.getLogWriter().getLogger().info("XML import time: " + ((stop - start) / 1000F) + "s");
      ConfigServer.getLogWriter().flushConsole();

      // xmlFilter = new XMLExportFilter( System.out, new M0AnnotatedExporter() );
      // ObjectExporter.doExport(xmlFilter, result[0] );
   }

   DefaultStore store;

   DBInstance instance;

   ObjectManager manager;

   public void stopDatabase() {
      instance.shutdown();
   }

   public void createObjects() throws Exception {
      int n = 3;

      OID objectsContainer = mod.createComplexObject("container", mod.getDatabaseEntry(), 0);
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

   private void testAnnotatedObjects() throws DatabaseException {
      DBModule mod = Database.getModuleByName("admin");

      OID oid1 = mod.createComplexObject("pierwszy", mod.getDatabaseEntry(), 0);
      DBAnnotatedObject ann = new DBAnnotatedObject(oid1);
      // ann.initialize();
      System.out.println("JEST " + ann.isValid());

   }

   public void createDatabase() throws Exception {

      ObjectManager manager;
      DefaultStore store;

      Database db;
      // DBInstance instance;

      DataFileHeap fileHeap;
      RevSeqFitMemManager allocator;

      fileHeap = new DataFileHeap("/tmp/test.dbf");
      fileHeap.format(1024 * 1024 * 10 * 20);
      fileHeap.open();

      allocator = new RevSeqFitMemManager(fileHeap);
      allocator.initialize();

      manager = new ObjectManager(allocator);
      manager.initialize(100);

      store = new DefaultStore(manager);
      store.initialize();

      // prepare the database
      Database.initialize(store);
      Database.open(store);

      DBModule sysmod = Database.getSystemModule();
      DBModule mod = Database.getModuleByName("admin");
      DBModule newmod = new DBModule(mod.createSubmodule("test", 0));
   }

   public static void main(String[] args) throws Exception {
  	 ConfigServer.getLogWriter().getLogger().info("XML Database Test started (debug mode: " + ConfigDebug.ASSERTS + ")");

      XMLDatabaseTest main = new XMLDatabaseTest();
      main.createDatabase();
      main.XMLDatabaseImport();
      // main.speedTest();
      DBInstance instance = new DBInstance();
      instance.startup();

      CLI cli = new CLI();
      cli.begin();

      instance.shutdown();

      ConfigServer.getLogWriter().getLogger().info("XML Database Test finished");
   }

}
