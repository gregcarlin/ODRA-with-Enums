package tests;

import java.net.UnknownHostException;
import java.util.Random;
import java.util.Vector;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.OID;
import odra.db.StdEnvironment;
import odra.db.indices.updating.IndexableStore;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.ModuleDumper;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaBase;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.dbinstance.DBInstance;
import odra.exceptions.rd.RDNetworkException;
import odra.network.encoders.results.QueryResultDecoder;
import odra.network.encoders.results.QueryResultEncoder;
import odra.network.encoders.stack.SBQLStackDecoder;
import odra.network.encoders.stack.SBQLStackEncoder;
import odra.sbql.assembler.AssemblerEmiter;
import odra.sbql.assembler.Disassembler;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleCompiler;
import odra.sbql.builder.ModuleLinker;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.emiter.ConstantPool;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.emiter.OpCodes;
import odra.sbql.emiter.instructions.Instruction;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.StringResult;
import odra.sessions.Session;
import odra.store.DefaultStore;
import odra.store.io.AutoExpandableHeap;
import odra.store.io.AutoExpandableLinearHeap;
import odra.store.io.AutoExpandablePowerHeap;
import odra.store.memorymanagement.AutoExpandableMemManager;
import odra.store.memorymanagement.ConstantSizeObjectsMemManager;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.store.sbastore.SpecialReferencesManager;
import odra.store.sbastore.ValuesManager;
import odra.store.transience.DataMemoryBlockHeap;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;

/**
 * Jodra main class
 * 
 * @author raist
 * 
 */

public class RAMain {
	private ObjectManager manager;

	private DefaultStore store;

	// private Database db;
	private DBInstance instance;

	private ModuleLinker linker = BuilderUtils.getModuleLinker();

	private ModuleCompiler compiler = BuilderUtils.getModuleCompiler();

	private ModuleOrganizer organizer;

	long start, stop;

	public void create() throws Exception {
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("test.dbf");
		fileHeap.format(1024 * 1024 * 10);
		fileHeap.open();

		allocator = new RevSeqFitMemManager(fileHeap);
		allocator.initialize();

		manager = new ObjectManager(allocator);
		manager.initialize(100);

		store = new IndexableStore(manager);
		store.initialize();

		// prepare the database
		Database.initialize(store);
		Database.open(store);
		ConfigServer.getLogWriter().getLogger().info("Database created");

	}

	public void createOptimized() throws Exception {
		DataFileHeap fileHeap;
		ConstantSizeObjectsMemManager allocator;

		fileHeap = new DataFileHeap("test_obj.dbf");
		fileHeap.format(1024 * 1024 * 20);
		fileHeap.open();

		allocator = new ConstantSizeObjectsMemManager(fileHeap, ObjectManager.MAX_OBJECT_LEN);
		allocator.initialize();

		DataFileHeap valuesFileHeap;
		RevSeqFitMemManager valuesAllocator;

		valuesFileHeap = new DataFileHeap("test_val.dbf");
		valuesFileHeap.format(1024 * 1024 * 20);
		valuesFileHeap.open();
		valuesAllocator = new RevSeqFitMemManager(valuesFileHeap);
		valuesAllocator.initialize();
		ValuesManager valuesManager = new ValuesManager(valuesAllocator);

		DataFileHeap specFileHeap = new DataFileHeap("/tmp/test_spec.dbf");
		specFileHeap.format(1024 * 1024 * 20);
		specFileHeap.open();
		ConstantSizeObjectsMemManager specAllocator = new ConstantSizeObjectsMemManager(specFileHeap,
					SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
		specAllocator.initialize();
		SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);

		ObjectManager manager = new ObjectManager(allocator, valuesManager, specManager);
		manager.initialize(100);

		store = new IndexableStore(manager);
		store.initialize();

		// prepare the database
		Database.initialize(store);
		Database.open(store);
		ConfigServer.getLogWriter().getLogger().info("Database created");

	}

	public void createExtendable() throws Exception {
		AutoExpandableHeap expFileHeap;
		AutoExpandableMemManager expAllocator;

		expFileHeap = AutoExpandableLinearHeap.startPersistantHeap("test_exp_objs");
		expFileHeap.format(1024 * 1024 * 5);
		expFileHeap.open();

		expAllocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(expFileHeap,
					ObjectManager.MAX_OBJECT_LEN);
		expAllocator.initialize();

		AutoExpandableHeap expvaluesFileHeap;
		AutoExpandableMemManager expvaluesAllocator;

		expvaluesFileHeap = AutoExpandablePowerHeap.startPersistantHeap("test_exp_vals");
		expvaluesFileHeap.format(1024 * 1024 * 5);
		expvaluesFileHeap.open();
		expvaluesAllocator = AutoExpandableMemManager.startAutoExpandableRevSeqFitMemManager(expvaluesFileHeap);
		expvaluesAllocator.initialize();
		ValuesManager valuesManager = new ValuesManager(expvaluesAllocator);

		AutoExpandableHeap specFileHeap = AutoExpandableLinearHeap.startPersistantHeap("test_exp_spec");
		specFileHeap.format(1024 * 1024 * 1);
		specFileHeap.open();
		AutoExpandableMemManager specAllocator = AutoExpandableMemManager
					.startAutoExpandableConstantSizeObjectsMemManager(specFileHeap,
								SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
		specAllocator.initialize();
		SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);

		ObjectManager manager = new ObjectManager(expAllocator, valuesManager, specManager);
		manager.initialize(100);

		store = new IndexableStore(manager);
		store.initialize();

		// prepare the database
		Database.initialize(store);
		Database.open(store);
		// LogWriter.getLogger().info("Database created");

	}

	protected void startDatabaseInstance() {
		try {
			instance = new DBInstance();
			instance.startup();
		} catch (UnknownHostException ex) {
			ConfigServer.getLogWriter().getLogger().finer("*** Database instance cannot be started");
		}
	}

	protected void stopDatabaseInstance() {
		instance.shutdown();
	}

	public void createTransient() throws Exception {
		DataMemoryBlockHeap dmbheap = new DataMemoryBlockHeap(1024 * 1024 * 10);

		RevSeqFitMemManager allocator;

		allocator = new RevSeqFitMemManager(dmbheap);
		allocator.initialize();

		manager = new ObjectManager(allocator);
		manager.initialize(6000);

		store = new DefaultStore(manager);
	}

	

	
	public void testJoinExpr(DBModule mod, ConstantPool pool) throws Exception {

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("address"));

		cblock = JulietGen.genJoinExpression(leftcb, rightcb);

		ConfigServer.getLogWriter().getLogger().info("Testing join expression..........");

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done join expression");
	}

	public void testOrderByExpr(DBModule mod, ConstantPool pool) throws Exception {

		ConfigServer.getLogWriter().getLogger().info("Testing order by expression..........");

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("age"));
		rightcb.emit(OpCodes.derefI);

		cblock = JulietGen.genOrderByExpression(leftcb, rightcb);
		cblock.emit(OpCodes.derefColCpx);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done order by expression");
	}

	public void testWhereExpr(DBModule mod, ConstantPool pool) throws Exception {

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("married"));
		rightcb.emit(OpCodes.derefB);
		rightcb.emit(OpCodes.ldTrue);
		rightcb.emit(OpCodes.eqB);

		cblock = JulietGen.genWhereExpression(leftcb, rightcb);

		ConfigServer.getLogWriter().getLogger().info("Testing where expression..........");

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done where expression");
	}

	public void testDotExpr(DBModule mod, ConstantPool pool) throws Exception {

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("lName"));

		cblock = JulietGen.genDotExpression(leftcb, rightcb);

		ConfigServer.getLogWriter().getLogger().info("Testing dot expression..........");

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done dot expression");
	}

	public void testForAllExpr(DBModule mod, ConstantPool pool) throws Exception {

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("married"));
		rightcb.emit(OpCodes.derefB);
		rightcb.emit(OpCodes.ldFalse);
		rightcb.emit(OpCodes.eqB);

		cblock = JulietGen.genForallExpression(leftcb, rightcb);

		ConfigServer.getLogWriter().getLogger().info("Testing for all expression..........");

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done for all expression");
	}

	public void testForSomeExpr(DBModule mod, ConstantPool pool) throws Exception {

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("married"));
		rightcb.emit(OpCodes.derefB);
		rightcb.emit(OpCodes.ldFalse);
		rightcb.emit(OpCodes.nEqB);

		cblock = JulietGen.genForsomeExpression(leftcb, rightcb);

		ConfigServer.getLogWriter().getLogger().info("Testing for any expression..........");

		Disassembler diss = new Disassembler(cblock.getByteCode());
		ConfigServer.getLogWriter().getLogger().info("Code:");
		ConfigServer.getLogWriter().getLogger().info(diss.decode());

		AssemblerEmiter asm = new AssemblerEmiter(diss.decode());
		diss = new Disassembler(asm.getByteCode());
		ConfigServer.getLogWriter().getLogger().info("Disassembler -> Assembler -> Code -> Disassembler:");
		ConfigServer.getLogWriter().getLogger().info(diss.decode());

		SBQLInterpreter interp = new SBQLInterpreter(mod);
		interp.runCode(cblock.getByteCode(), pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("Query result:");
		ConfigServer.getLogWriter().getLogger().info(new odra.sbql.results.runtime.RawResultPrinter().print(interp.getResult()));

		ConfigServer.getLogWriter().getLogger().info("............Done for any expression");

	}

	public void testJulietEmiter() throws Exception {

		ConstantPool pool = new ConstantPool();
		JulietCode cblock = new JulietCode();

		// DBModule sysmod = Database.getSystemModule();
		DBModule mod = Database.getModuleByName("admin");// new DBModule(sysmod.createSubmodule("admin", 0));

		// metabase root objects, necessary to initialize senvs
		OID[] obj = mod.getMetabaseEntry().derefComplex();

		// dump content of the modules
		ConfigServer.getLogWriter().getLogger().info(new odra.db.objects.data.ModuleDumper(mod).dump());
		Session.create();
		Session.initialize("admin", "admin");
		// createSampleData(mod, pool);
		// this.createSampleClassInstances(mod, pool);
		mod.setModuleCompiled(false);
		// testForSomeExpr(mod, pool);
		// testWhereExpr(mod, pool);
		/*
		 * testJoinExpr(mod, pool); testOrderByExpr(mod, pool); testDotExpr(mod, pool); testForAllExpr(mod, pool);
		 */
		// createProcedures(mod);
		/*
		 * testSum(mod, pool); testMinMax(mod, pool); testUnion(mod, pool); testIn(mod, pool); testIntersect(mod, pool);
		 */
		// testUnique(mod, pool);
		// testDifference(mod, pool);
		// testAvg(mod, pool);
		// instance.shutdown();
		// createMetadata(mod);
		// testViews(mod, pool);
		// this.testDeref(mod, pool);
		// LogWriter.getLogger().info("Instance stopped");
		Session.close();
	}

	public void testAssembler() throws Exception {

		DBModule sysmod = Database.getSystemModule();
		DBModule mod = new DBModule(sysmod.createSubmodule("raist"));

		// 1 - with labels
		// String asmcode = ":if \n ldFalse \n braFalse :else \n ldFalse \n bra :end \n:else \n ldTrue \n :end";
		// 2 - with offsets
		String asmcode = "\n00 ldFalse \n08 braFalse 32 \n16 ldFalse \n24 bra 48 \n32 ldTrue\n";

		// don't mix syntax 2 with 3 ;)

		ConfigServer.getLogWriter().getLogger().info("Assembler:");
		ConfigServer.getLogWriter().getLogger().info(asmcode);

		AssemblerEmiter asm = new AssemblerEmiter(asmcode);
		Disassembler diss = new Disassembler(asm.getByteCode());
		ConfigServer.getLogWriter().getLogger().info("Assembler -> Code -> Disassembler:");
		ConfigServer.getLogWriter().getLogger().info(diss.decode());

	}

	

	public void testUnion(DBModule mod, ConstantPool pool) throws Exception {

		ConfigServer.getLogWriter().getLogger().info("Testing union expression..........");
		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));
		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("Car"));

		cblock = JulietGen.genUnionExpression(leftcb, rightcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done union expression");

	}

	public void testSum(DBModule mod, ConstantPool pool) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Testing sum expression..........");

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();

		leftcb.emit(OpCodes.ldBag);
		leftcb.emit(OpCodes.ldI, 0);
		leftcb.emit(OpCodes.ldI, 6);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 1);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 2);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 9);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 3);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.pop);

		cblock = JulietGen.genSumIntExpression(leftcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done sum expression");

	}

	public void testAvg(DBModule mod, ConstantPool pool) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Testing avg expression..........");

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();

		leftcb.emit(OpCodes.ldBag);
		leftcb.emit(OpCodes.ldI, 0);
		leftcb.emit(OpCodes.ldI, 6);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 4);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 1);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 9);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 10);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 6);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.pop);

		cblock = JulietGen.genAvgExpression(leftcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Doneavg expression");

	}

	public void testDeref(DBModule mod, ConstantPool pool) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Testing deref expression..........");

		JulietCode cblock;

		cblock = JulietGen.genCartesianProductExpression(JulietGen.genDynNameExpression(Database.getStore().addName(
					"Person")), JulietGen.genIntegerExpression(2));

		cblock = JulietGen.genDynDeref(cblock);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done deref expression");

	}

	public void testUnique(DBModule mod, ConstantPool pool) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Testing unique expression..........");

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();

		leftcb.emit(OpCodes.ldBag);
		leftcb.emit(OpCodes.ldI, 0);
		leftcb.emit(OpCodes.ldI, 6);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 1);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 6);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 9);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 3);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 3);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.pop);

		cblock = JulietGen.genUniqueExpression(leftcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done unique expression");

	}

	public void testMinMax(DBModule mod, ConstantPool pool) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Testing max expression..........");
		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		/*
		 * leftcb.emit(OpCodes.ldBag); leftcb.emit(OpCodes.ldI, 0); leftcb.emit(OpCodes.ldI, 11);
		 * leftcb.emit(OpCodes.insPrt2); leftcb.emit(OpCodes.ldI, 1); leftcb.emit(OpCodes.insPrt2);
		 * leftcb.emit(OpCodes.ldI, 2); leftcb.emit(OpCodes.insPrt2); leftcb.emit(OpCodes.ldI, -3);
		 * leftcb.emit(OpCodes.insPrt2); leftcb.emit(OpCodes.ldI, 3); leftcb.emit(OpCodes.insPrt2);
		 * leftcb.emit(OpCodes.ldI, 17); leftcb.emit(OpCodes.insPrt2); leftcb.emit(OpCodes.pop);
		 */

		leftcb.emit(OpCodes.bind, Database.getNameIndex().addName("Person"));

		rightcb.emit(OpCodes.bind, Database.getNameIndex().addName("age"));

		cblock = JulietGen.genDotExpression(leftcb, rightcb);
		cblock.emit(OpCodes.derefColI);

		cblock = JulietGen.genMaxExpression(cblock);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done max expression");

	}

	public void testIn(DBModule mod, ConstantPool pool) throws Exception {
		ConfigServer.getLogWriter().getLogger().info("Testing in expression..........");

		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.ldBag);
		leftcb.emit(OpCodes.ldI, 2);
		leftcb.emit(OpCodes.ldI, 6);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 1);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.pop);

		rightcb.emit(OpCodes.ldBag);
		rightcb.emit(OpCodes.ldI, 1);
		rightcb.emit(OpCodes.ldI, 1);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 2);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 3);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 4);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 5);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.pop);

		cblock = JulietGen.genInExpression(leftcb, rightcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done in expression");

	}

	public void testIntersect(DBModule mod, ConstantPool pool) throws Exception {

		ConfigServer.getLogWriter().getLogger().info("Testing intersect expression..........");
		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.ldBag);
		leftcb.emit(OpCodes.ldI, 2);
		leftcb.emit(OpCodes.ldI, 3);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 7);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.pop);

		rightcb.emit(OpCodes.ldBag);
		rightcb.emit(OpCodes.ldI, 1);
		rightcb.emit(OpCodes.ldI, 1);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 2);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 3);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 4);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 5);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.pop);

		cblock = JulietGen.genIntersectExpression(leftcb, rightcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done intersect expression");

	}

	public void testDifference(DBModule mod, ConstantPool pool) throws Exception {

		ConfigServer.getLogWriter().getLogger().info("Testing Difference expression..........");
		JulietCode cblock;
		JulietCode leftcb = new JulietCode();
		JulietCode rightcb = new JulietCode();

		leftcb.emit(OpCodes.ldBag);
		leftcb.emit(OpCodes.ldI, 0);
		leftcb.emit(OpCodes.ldI, 2);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.ldI, 5);
		leftcb.emit(OpCodes.insPrt2);
		leftcb.emit(OpCodes.pop);

		rightcb.emit(OpCodes.ldBag);
		rightcb.emit(OpCodes.ldI, 0);
		rightcb.emit(OpCodes.ldI, 1);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 1);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 3);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 4);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.ldI, 5);
		rightcb.emit(OpCodes.insPrt2);
		rightcb.emit(OpCodes.pop);

		cblock = JulietGen.genDifferenceExpression(leftcb, rightcb);

		byte[] code = cblock.getByteCode();
		dissasemble(code);
		execute(mod, code, pool.getAsBytes());

		ConfigServer.getLogWriter().getLogger().info("............Done Difference expression");

	}

	private void testIndices() throws Exception {

		DBModule mod = Database.getModuleByName("admin");
		Session.create();
		Session.initialize("admin", "admin");
		SBQLInterpreter interpreter = new SBQLInterpreter(mod);
		IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
		odra.db.indices.IndexManager iman = new odra.db.indices.IndexManager(linker, compiler, generator);

		iman.createIndex("testidx", false, new String[0], "Person join (2006 - age, address.city);", "admin");
		Session.close();

	}

	

	private void dissasemble(byte[] code) {
		Disassembler diss = new Disassembler(code);
		ConfigServer.getLogWriter().getLogger().info("Code:");
		ConfigServer.getLogWriter().getLogger().info(diss.decode());
	}

	private void execute(DBModule mod, byte[] code, byte[] pool) throws Exception {

		SBQLInterpreter interp = new SBQLInterpreter(mod);
		start = System.currentTimeMillis();
		interp.runCode(code, pool);

		stop = System.currentTimeMillis();

		ConfigServer.getLogWriter().getLogger().info("Query result (" + ((stop - start) / 1000F) + " sec.):");
		ConfigServer.getLogWriter().getLogger().info(new odra.sbql.results.runtime.RawResultPrinter().print(interp.getResult()));
	}

	private void testStackSerializer() {
		Vector<AbstractQueryResult> qres = new Vector<AbstractQueryResult>();
		qres.add(new IntegerResult(1));
		qres.add(new StringResult("ala"));
		qres.add(new DoubleResult(1));
		qres.add(new BooleanResult(true));
		qres.add(new BagResult());
		

		try {
			SBQLStackEncoder encoder = new SBQLStackEncoder(new QueryResultEncoder("localhost", "admin"));
			byte[] rawstack = encoder.serialize(qres);
			SBQLStackDecoder decoder = new SBQLStackDecoder(new QueryResultDecoder());
			qres = decoder.decodeStack(rawstack);
			qres.size();
		} catch (RDNetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void startCli() throws Exception {

		CLI cli = new CLI();		
		cli.begin(new String[] {"--batch", "res/tests/startupTests.cli"});

	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Test started (debug mode: " + ConfigDebug.ASSERTS + "| typechecking: "
					+ (ConfigServer.TYPECHECKING == true ? "on" : "off") + ")");
		RAMain main = new RAMain();
		
		// main.create(); // reference time : 2844
		// main.createOptimized(); // reference time : 2812
		main.createExtendable(); // reference time linear : 3625; reference time power : 4250

		main.startDatabaseInstance();

		main.startCli();
		main.stopDatabaseInstance();
	}

}
