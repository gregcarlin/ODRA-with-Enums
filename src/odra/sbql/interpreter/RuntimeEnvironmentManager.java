package odra.sbql.interpreter;

import java.util.Stack;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBInterface;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DataObjectKind;
import odra.sbql.SBQLException;
import odra.sbql.debugger.runtime.SBQLInstructionTable;
import odra.sbql.interpreter.exceptions.ExceptionTable;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.sbql.stack.Binder;
import odra.sbql.stack.CounterData;
import odra.sbql.stack.IBindingGuru;
import odra.sbql.stack.Nester;
import odra.sbql.stack.SBQLStack;
import odra.sbql.stack.StackFrame;
import odra.sessions.Session;

/**
 * RuntimeEnvironmentManager Manages runtime environment (perform runtime
 * nested) create different types of environment
 * 
 * @author Radek Adamus
 * @since 2007-03-28 last modified: 2007-04-12
 * @version 1.0
 */
class RuntimeEnvironmentManager {

	protected RuntimeNestingManager nestingManager;

	private Stack<EnvironmentMetaData> environmentMetaData = new Stack<EnvironmentMetaData>();

	protected SBQLStack sbqlStack;
	private RuntimeAggregateBindingManager aggregateBindingManager;

	private SBQLStack systemModule;

	private int localBlocksLevel = 0;

	protected RuntimeEnvironmentManager() {
		this.aggregateBindingManager = new RuntimeAggregateBindingManager(Session.getUserContext());
	}

	public RuntimeEnvironmentManager(SBQLStack sbqlStack)
			throws DatabaseException {
		this();
		this.sbqlStack = sbqlStack;		
		init();
	}

	private void init() throws DatabaseException {
		this.sbqlStack.initialize();
		this.systemModule = new SBQLStack(new RuntimeBindingManager(Session
				.getUserContext()));
		this.systemModule.initialize(new Nester(Database.getSystemModule()
				.getDatabaseEntry()));
		this.nestingManager = new RuntimeNestingManager();
	}

	void createNestedEnvironment(SingleResult res) throws DatabaseException {
		assert res != null : "res == null";

		// perform nested
		Stack<StackFrame> stack = this.nestingManager.nested(res);
		environmentMetaData.push(new EnvironmentMetaData(stack.size(),
				EnvironmentMetaData.Type.NESTED_ENV));
		// push nested result on the ENVS
		this.sbqlStack.createNestedEnvironment(stack);

	}

	void initializeModuleEnvironment(DBModule mod, boolean base)
			throws DatabaseException {
		this.initializeModuleEnvironment(this.sbqlStack, mod, base, true);
	}

	void initializeCompilationModuleEnvironment(DBModule mod, boolean base)
			throws DatabaseException {
		this.initializeModuleEnvironment(this.sbqlStack, mod, base, false);
	}

	private final void initializeModuleEnvironment(SBQLStack stack,
			DBModule mod, boolean base, boolean session)
			throws DatabaseException {
		this.initializeNestedRootEnvironment(stack, mod.getDatabaseEntry(),
				base);
		if (session)
			this.initializeNestedRootEnvironment(stack, Session
					.getModuleSessionEntry(mod), base);

		if (base) {
			stack.enterBaseFrame(new Binder(
					Database.getStore().addName("self"), new ReferenceResult(
							mod.getDatabaseEntry())));
			stack.enterBaseFrame(new Binder(mod.getOID().getObjectNameId(),
					new ReferenceResult(mod.getDatabaseEntry())));
			initializeImports(mod, stack);
			initializeInterfaces(mod, stack);
		}
	}

	/**
	 * @param mod
	 * @param stack
	 */
	private final void initializeImports(DBModule mod, SBQLStack stack)
			throws DatabaseException {
		OID[] imports = mod.getCompiledImports();
		OID[] aliases = mod.getImportsAliases();
		for (int i = 0; i < imports.length; i++) {
			DBModule imported = new DBModule(imports[i].derefReference());
			String alias = aliases[i].derefString();
			if (!"".equals(alias))
				stack.enter(new Binder(Database.getStore().addName(alias),
						new ReferenceResult(imported.getDatabaseEntry())));
			stack.enter(new Binder(imported.getOID().getObjectNameId(),
					new ReferenceResult(imported.getDatabaseEntry())));
		}

	}

	private final void initializeInterfaces(DBModule mod, SBQLStack stack)
			throws DatabaseException {
		// this is for interfaces
		OID dbroot = mod.getDatabaseEntry();
		OID[] rootch = dbroot.derefComplex();

		for (OID oid : rootch) {
			if (!oid.isComplexObject())
				continue;

			DBInterface dbint = new DBInterface(oid);

			if (dbint.isValid()) {
				System.out.println("wykrylismy interfejs!!!!");

				OID[] trgt = dbint.getTargets();

				for (OID t : trgt) {
					System.out.println("wkladamy do sekcji bazowej "
							+ dbint.getName());

					stack.enterBaseFrame(new Binder(Database.getStore()
							.addName(dbint.getName()), new ReferenceResult(t
							.derefReference())));
				}
			}
		}
	}

	final void initializeNestedRootEnvironment(SBQLStack stack, OID env,
			boolean base) throws DatabaseException {
		assert env.isComplexObject() : "complex object required";

		StackFrame frame = this.nestingManager.nestedRootEnvironment(env);
		if (base) {
			stack.enterAllBaseFrame(frame);
		} else
			stack.enterAll(frame);
	}

	final void createSubroutineEnvironment(ReturnData subData) {
		this.environmentMetaData.push(new ProcedureEnvironmentMetaData(1,
				EnvironmentMetaData.Type.INTERNAL_ENV, null, null,
				this.sbqlStack.size(), this.localBlocksLevel, subData));
		this.localBlocksLevel = 0;
	}

	final ReturnData getReturnData() {
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.PROCEDURE_ENV
				|| this.environmentMetaData.peek().type == EnvironmentMetaData.Type.INTERNAL_ENV : "";
		return ((ProcedureEnvironmentMetaData) this.environmentMetaData.peek())
				.getReturnData();
	}

	final void createProcedureEnvironment(ReferenceResult procres,
			ReturnData procData) throws DatabaseException {
		ProcedureEnvironmentMetaData procMetaData = null;
		int paramNumber = (((IntegerResult) this.sbqlStack.peek()).value + 1); // + 1
							// because the number of params is on the stack
		Stack<StackFrame> buildedEnvironment;

		int resultStackBaseLevel = this.sbqlStack.size() - paramNumber;
		OID localEnvEntry = Session.createLocalEnvironment();
		if (procres.parent == null) { // global procedure
			this.sbqlStack.createScopedEnvironment();
			procMetaData = new ProcedureEnvironmentMetaData(1,
					EnvironmentMetaData.Type.PROCEDURE_ENV, procres.value,
					localEnvEntry, resultStackBaseLevel, this.localBlocksLevel,
					procData);

		} else if (procres.parent instanceof VirtualReferenceResult) { // generic
			// view procedure
			buildedEnvironment = this.nestingManager
					.nestedSeed((VirtualReferenceResult) procres.parent);
			procMetaData = new ProcedureEnvironmentMetaData(buildedEnvironment
					.size(), EnvironmentMetaData.Type.PROCEDURE_ENV,
					procres.value, localEnvEntry, resultStackBaseLevel,
					this.localBlocksLevel, procData);
			this.sbqlStack.createNestedScopedEnvironment(buildedEnvironment);
		} else {
			switch (new DBObject(procres.parent.value).getObjectKind()
					.getKindAsInt()) {
			case DataObjectKind.DATA_OBJECT:
				// if(procres.parent.value.derefInstanceOfReference() != null) {
				// //class method
				buildedEnvironment = new Stack<StackFrame>();
				buildedEnvironment.add(new StackFrame());
				buildedEnvironment.addAll(this.nestingManager.nested(procres.parent));
				
				procMetaData = new ProcedureEnvironmentMetaData(
						buildedEnvironment.size(),
						EnvironmentMetaData.Type.PROCEDURE_ENV, procres.value,
						localEnvEntry, resultStackBaseLevel,
						this.localBlocksLevel, procData);
				this.sbqlStack.createNestedScopedEnvironment(buildedEnvironment);
				// }
				// else
				// assert false :"method parent must be a module or a class or a
				// view?";
				break;
			case DataObjectKind.CLASS_OBJECT:
				assert false : "unimplemented static method calls";
				break;
			case DataObjectKind.VIEW_OBJECT:
				buildedEnvironment = this.nestingManager.nested(procres.parent);
				procMetaData = new ProcedureEnvironmentMetaData(
						buildedEnvironment.size(),
						EnvironmentMetaData.Type.PROCEDURE_ENV, procres.value,
						localEnvEntry, resultStackBaseLevel,
						this.localBlocksLevel, procData);
				this.sbqlStack
						.createNestedScopedEnvironment(buildedEnvironment);
				break;
			}
		}
		this.environmentMetaData.push(procMetaData);

		// this.sbqlStack.setLocalEnv(Session.createLocalEnvironment());
		this.localBlocksLevel = 0;
	}

	final void createLocalEnvironment() throws DatabaseException {
		this.sbqlStack.createEnvironment();
		OID localEnvEntry = Session.createLocalEnvironment();
		// for create
		// this.sbqlStack.setLocalEnv(localEnv);
		this.environmentMetaData.push(new LocalEnvironmentMetaData(1,
				EnvironmentMetaData.Type.LOCAL_BLOCK_ENV, localEnvEntry));
		this.localBlocksLevel++;
	}

	void createCounterEnvironment(int limit) {
		this.sbqlStack.createEnvironment();
		this.sbqlStack.setCounterData(new CounterData(limit));
		this.environmentMetaData.push(new EnvironmentMetaData(1,
				EnvironmentMetaData.Type.COUNTER_ENV));
	}

	final void destroyProcedureEnvironment(boolean clearResultStack)
			throws DatabaseException {
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.PROCEDURE_ENV : "wrong stack "
				+ this.environmentMetaData.peek().type;
		ProcedureEnvironmentMetaData procMetaData = (ProcedureEnvironmentMetaData) this.environmentMetaData
				.peek();
		assert this.localBlocksLevel == 0 : "there are still local blocks left";
		this.localBlocksLevel = procMetaData.localBlocksBaseLevel;
		if (clearResultStack) {
			while (sbqlStack.size() > procMetaData.resultStackBaseLevel) {
				sbqlStack.pop();
			}
		}

		this.destroyLocalEnvironment(procMetaData);
	}

	void destroyNestedEnvironment() throws DatabaseException {
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.NESTED_ENV : "wrong stack "
				+ this.environmentMetaData.peek().type;
		this.destroyTopEnvironment();
	}

	final void destroyCounterEnvironment() throws DatabaseException {
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.COUNTER_ENV : "wrong stack "
				+ this.environmentMetaData.peek().type;
		this.destroyTopEnvironment();
	}

	final void destroyLocalBlockEnvironment() throws DatabaseException {
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.LOCAL_BLOCK_ENV : "wrong stack "
				+ this.environmentMetaData.peek().type;
		this
				.destroyLocalEnvironment((LocalEnvironmentMetaData) this.environmentMetaData
						.peek());
		this.localBlocksLevel--;
		assert this.localBlocksLevel >= 0 : "wrong local block";
	}

	private final void destroyLocalEnvironment(
			LocalEnvironmentMetaData localMetaData) throws DatabaseException {
		OID local = localMetaData.localStore;
		assert local != null : "local store == null";
		local.delete();

		this.destroyTopEnvironment();
	}

	final void destroyLocalBlockEnvironmentsUpTo(int number)
			throws DatabaseException {
		assert this.localBlocksLevel >= number : "wrong stack ";
		while (this.localBlocksLevel > number)
			this.destroyEnvironment();

	}

	void destroyEnvironment() throws DatabaseException {
		// assert environmentMetaData.peek().size == 1: "wrong stack size " +
		// environmentMetaData.peek().size;
		switch (environmentMetaData.peek().type) {
		case PROCEDURE_ENV:
			this.destroyProcedureEnvironment(false);
			break;
		case NESTED_ENV:
			this.destroyNestedEnvironment();
			break;
		case LOCAL_BLOCK_ENV:
			this.destroyLocalBlockEnvironment();
			break;
		case COUNTER_ENV:
			this.destroyCounterEnvironment();
			break;
		case INTERNAL_ENV:
			this.destroySubroutineEnvironment();
			break;
		default:
			assert false : "wrong stack "
					+ this.environmentMetaData.peek().type;
			break;
		}
	}

	final void destroySubroutineEnvironment() {
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.INTERNAL_ENV;
		this.localBlocksLevel = ((ProcedureEnvironmentMetaData) this.environmentMetaData
				.pop()).localBlocksBaseLevel;

	}

	private final void destroyTopEnvironment() {
		int sectionsToRemove = environmentMetaData.pop().size;
		for (int i = 0; i < sectionsToRemove; i++) {
			this.sbqlStack.destroyEnvironment();
		}
	}

	final void cleanBeforeReturn() throws DatabaseException {
		while (this.environmentMetaData.peek().type != EnvironmentMetaData.Type.PROCEDURE_ENV) {
			this.destroyEnvironment();
		}
		assert this.environmentMetaData.peek().type == EnvironmentMetaData.Type.PROCEDURE_ENV : "wrong stack "
				+ this.environmentMetaData.peek().type;
	}

	final boolean isInProcedure() {
		return this.hasProcedureEnvironment();
	}

	final OID getLocalStoreEntry() {
		EnvironmentMetaData env = null;
		for (int i = this.environmentMetaData.size() - 1; i >= 0; i--) {
			env = this.environmentMetaData.get(i);
			if (env.type.equals(EnvironmentMetaData.Type.PROCEDURE_ENV)
					|| env.type
							.equals(EnvironmentMetaData.Type.LOCAL_BLOCK_ENV))
				break;
		}
		assert (env != null) : "wrong stack: no local environment";
		assert (env instanceof LocalEnvironmentMetaData) : "wrong stack (this.environmentMetaData.peek() instanceof LocalEnvironmentMetaData) == true : "
				+ this.environmentMetaData.peek().getClass().getSimpleName();
		assert (((LocalEnvironmentMetaData) env).localStore != null) : "localStore != null";

		return ((LocalEnvironmentMetaData) env).localStore;
	}

	private final boolean hasProcedureEnvironment() {
		for (int i = this.environmentMetaData.size() - 1; i >= 0; i--) {
			if (this.environmentMetaData.get(i).type == EnvironmentMetaData.Type.PROCEDURE_ENV)
				return true;
		}
		return false;
	}

	AbstractQueryResult[] bind(int nameid, DBModule cmod)
			throws DatabaseException {
		AbstractQueryResult[] resarr = this.sbqlStack.bind(nameid);

		if (resarr.length == 0)
			resarr = this.dynGlobalBind(nameid, cmod);
		if (resarr.length == 0)
			resarr = this.systemModuleBind(nameid);
		return resarr;
	}

	AbstractQueryResult[] bindAggregate(int nameid, DBModule cmod)
	throws DatabaseException {
		IBindingGuru guru = this.sbqlStack.getGuru();
		this.sbqlStack.setGuru(aggregateBindingManager);
		AbstractQueryResult[] resarr = this.sbqlStack.bind(nameid);		
		this.sbqlStack.setGuru(guru);
		if (resarr.length == 0){
			resarr = this.dynGlobalAggregateBind(nameid, cmod);
		}
		return resarr;
	}

	private final AbstractQueryResult[] dynGlobalBind(int name_id, DBModule cmod)
			throws DatabaseException {

		OID[] impoids = cmod.getCompiledImports();

		SBQLStack gstack = new SBQLStack(new RuntimeBindingManager(Session
				.getUserContext()));
		gstack.initialize();

		for (int i = 1; i < impoids.length; i++) {
			DBModule mod = new DBModule(impoids[i].derefReference());
			initializeModuleEnvironment(gstack, mod, false, true);

		}

		return gstack.bind(name_id);
	}
	
	private final AbstractQueryResult[] dynGlobalAggregateBind(int name_id,
			DBModule cmod) throws DatabaseException {

		OID[] impoids = cmod.getCompiledImports();

		SBQLStack gstack = new SBQLStack(aggregateBindingManager);
		gstack.initialize();

		for (int i = 1; i < impoids.length; i++) {
			DBModule mod = new DBModule(impoids[i].derefReference());
			initializeModuleEnvironment(gstack, mod, false, true);

		}

		return gstack.bind(name_id);
	}

	private final AbstractQueryResult[] systemModuleBind(int name_id)
			throws DatabaseException {
		return this.systemModule.bind(name_id);
	}

	final ExceptionTable getExceptionTable() throws DatabaseException {
		int size = this.environmentMetaData.size();
		for (int i = size - 1; i >= 0; i--) {
			EnvironmentMetaData mtd = this.environmentMetaData.get(i);
			if (mtd.type == EnvironmentMetaData.Type.PROCEDURE_ENV) {
				return ((ProcedureEnvironmentMetaData) mtd).getExceptionTable();
			}

		}

		return null;
	}
	
	final SBQLInstructionTable getInstructionTable() throws DatabaseException {
		int size = this.environmentMetaData.size();
		for (int i = size - 1; i >= 0; i--) {
			EnvironmentMetaData mtd = this.environmentMetaData.get(i);
			if (mtd.type == EnvironmentMetaData.Type.PROCEDURE_ENV) {
				return ((ProcedureEnvironmentMetaData) mtd).getInstructionTable();
			}

		}

		return null;
	}

	
	/**
	 * @return the on_navigate
	 */
	ReferenceResult on_navigate() {
		return this.nestingManager.getOn_navigate();
	}

	private static class EnvironmentMetaData {
		/**
		 * number of SBQLStack sections for the environment
		 */
		private int size;

		/**
		 * type of the environment
		 */
		Type type;

		/**
		 * @param size
		 * @param type
		 */
		EnvironmentMetaData(int size, Type type) {
			this.size = size;
			this.type = type;
		}

		public enum Type {
			PROCEDURE_ENV, COUNTER_ENV, NESTED_ENV, LOCAL_BLOCK_ENV, INTERNAL_ENV
		}

	}

	private static class LocalEnvironmentMetaData extends EnvironmentMetaData {
		/**
		 * root of the local volatile store entry
		 */
		OID localStore;

		/**
		 * @param size
		 * @param type
		 */
		LocalEnvironmentMetaData(int size, Type type, OID localStore) {
			super(size, type);
			this.localStore = localStore;
			// TODO Auto-generated constructor stub
		}

	}

	private static class ProcedureEnvironmentMetaData extends
			LocalEnvironmentMetaData {
		/**
		 * id of the procedure
		 */
		private OID procOid;

		private int resultStackBaseLevel;

		private int localBlocksBaseLevel;

		private ReturnData procData;
		private SBQLInstructionTable instructionTable;
		private ExceptionTable exceptionTable;

		/**
		 * @param size
		 * @param type
		 */
		ProcedureEnvironmentMetaData(int size, Type type, OID procOID,
				OID localStore, int resultStackBaseLevel,
				int localBlocksBaseLevel, ReturnData procData) {
			super(size, type, localStore);
			assert resultStackBaseLevel >= 0 : "wrong result stack";
			this.procOid = procOID;
			this.resultStackBaseLevel = resultStackBaseLevel;
			this.localBlocksBaseLevel = localBlocksBaseLevel;
			this.procData = procData;
		}

		SBQLInstructionTable getInstructionTable() throws DatabaseException {
			if (this.instructionTable != null) {
				return this.instructionTable;

			} else {
				assert this.procOid != null : "procedure required";
				DBProcedure proc = new DBProcedure(this.procOid);
				return this.instructionTable = new SBQLInstructionTable(proc
						.getDebugCode());
			}

		}

		ExceptionTable getExceptionTable() throws DatabaseException {
			if (this.exceptionTable != null)
				return this.exceptionTable;
			else {
				assert this.procOid != null : "procedure required";
				DBProcedure proc = new DBProcedure(this.procOid);
				return this.exceptionTable = new ExceptionTable(proc
						.getExceptionTable());

			}

		}

		/**
		 * @return the procData
		 */
		ReturnData getReturnData() {
			return procData;
		}

		/**
		 * @param procData
		 *                the procData to set
		 */
		void setProcData(ReturnData procData) {
			this.procData = procData;
		}

	}

	public void injectRemoteParms() throws DatabaseException {
		if (Session.exists() && Session.getCurrent().isParmDependent()) {
			Vector<AbstractQueryResult> rStack = Session.getCurrent()
					.getRemoteResultsStack();

			for (AbstractQueryResult res : rStack) {
				if (res instanceof BinderResult) {
					BinderResult br = (BinderResult) res;
					Binder binder = new Binder(Database.getNameIndex().name2id(
							br.getName()), br.value);

					sbqlStack.enter(binder);
				}
			}

			Session.getCurrent().setRemoteResultsStack(null);
		}
	}

	/**
	 * @throws DatabaseException 
	 * 
	 */
	String getCurrentProcedureName() throws DatabaseException {
		
		int size = this.environmentMetaData.size();
		for (int i = size - 1; i >= 0; i--) {
			EnvironmentMetaData mtd = this.environmentMetaData.get(i);
			if (mtd.type == EnvironmentMetaData.Type.PROCEDURE_ENV) {
				return ((ProcedureEnvironmentMetaData) mtd).procOid.getObjectName();
			}

		}

		return null;
	}
}
