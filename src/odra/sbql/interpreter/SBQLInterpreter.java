package odra.sbql.interpreter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.transform.Source;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.links.LinkManager;
import odra.db.links.RemoteDefaultStoreOID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBSchema;
import odra.db.objects.data.DBView;
import odra.db.objects.data.DBVirtualObjectsProcedure;
import odra.db.objects.data.DataObjectKind;
import odra.sbql.debugger.runtime.SBQLInstructionTable;
import odra.sbql.debugger.runtime.SBQLInstructionTable.SourcePosition;
import odra.sbql.emiter.ConstantPool;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietSubroutines;
import odra.sbql.emiter.OpCodes;
import odra.sbql.interpreter.exceptions.ExceptionTable;
import odra.sbql.interpreter.exceptions.ExceptionTable.TryCatchBlock;
import odra.sbql.interpreter.helper.SBQLInterpreterHelper;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.compiletime.util.ValueSignatureInfo;
import odra.sbql.results.compiletime.util.ValueSignatureType;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.CollectionResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.LazyFailureResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.sbql.stack.Binder;
import odra.sbql.stack.BindingInfo;
import odra.sbql.stack.CounterData;
import odra.sbql.stack.IBindingGuru;
import odra.sbql.stack.SBQLStack;
import odra.sessions.Session;
import odra.store.DefaultStoreOID;
import odra.store.sbastore.CardinalityException;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.util.DateUtils;
import odra.util.RandomUtils;
import odra.util.StringUtils;
import odra.wrapper.Wrapper;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;
/**
 * SBQLInterpreter - intepreter main class
 * @author raist, Radek Adamus
 */
public class SBQLInterpreter {
	SBQLStack stack;
	private JulietSubroutines subroutines = new JulietSubroutines();
	private ExecutionMode  mode;
	private DBModule module;
	private IDataStore store;
	private RuntimeEnvironmentManager envManager;
	
	Stack<DBModule> currmod = new Stack<DBModule>();
	private Stack<Integer> ret = new Stack<Integer>();

	private Stack<LazyFailureBlock> lazyFailureBlocks = new Stack<LazyFailureBlock>();
	
	public SBQLInterpreter(DBModule module) throws DatabaseException {
	    this(module, ExecutionMode.Data);
	}

	public SBQLInterpreter(DBModule module, ExecutionMode mode) throws DatabaseException {
		this(module, mode, new RuntimeBindingManager(Session.getUserContext()));
	}

	protected SBQLInterpreter(DBModule module, ExecutionMode mode, RuntimeBindingManager runtimeBindingManager) throws DatabaseException {
	    	assert module != null : "module != null";
		this.module = module;
		this.mode = mode;
		this.store = module.getOID().getStore();
		currmod.push(module);
		
		this.stack = new SBQLStack(runtimeBindingManager);
		switch(mode)
		{
		case Meta:
		    this.envManager = new RuntimeMetabaseEnvironmentManager(this.stack);
		    envManager.initializeModuleEnvironment(this.module,true);
		    break;
		case Data:
		    this.envManager = new RuntimeEnvironmentManager(this.stack);
		    envManager.initializeModuleEnvironment(this.module,true);
		    break;
		case Compilation:
		    this.envManager = new RuntimeEnvironmentManager(this.stack);
		    envManager.initializeCompilationModuleEnvironment(this.module,true);
		    break;
		default:
		    assert false: "unknown execition mode";

		}		

	}
	
	private void init(){
	    
	}

	/**
	 * @return result of the evaluation
	 */
	public Result getResult() {
		return (Result) stack.pop();
	}

	/**
	 * @return true if the result stack is not empty, false otherwise
	 */
	public boolean hasResult() {
		return  !stack.empty();
	}

	/**
	 * Sets stack result for changing evaluation context
	 */
	public void setResult(Result res) {
		stack.push(res);
	}

	public void runCode(byte[] code, byte[] pool, int startIndex, int endIndex) throws InterpreterException {
	    assert endIndex <= (code.length) : "endIndex out of code " + endIndex + " > " +(code.length);
	    assert endIndex % JulietCode.INSTRUCTION_LENGTH == 0 : "wrong endIndex value";
	    ByteBuffer buffer = ByteBuffer.wrap(code);
	    buffer.position(startIndex);
	    buffer.limit(endIndex);
	    ConstantPool cp;
	    if(pool == null )
		cp = new ConstantPool();
	    else
		cp = new ConstantPool(pool);
	    this.runCode(buffer, cp);
	}

	public void runCode(byte[] code, byte[] pool) throws InterpreterException {

	    this.runCode(code, pool, 0, code.length);
	}
	/** interpreter main method for opcodes evaluation
	 * @param sourceCode - code byte array
	 * @param pool - constant pool byte array
	 * @throws InterpreterException
	 */
	private void runCode(ByteBuffer buffer, ConstantPool cp) throws InterpreterException {

		Result res, res1, res2;
		SingleResult sinres1, sinres2;
		StructResult strres1, strres2;
		BagResult bagres;
		CollectionResult colres1, colres2;
		ReferenceResult refres, refres1, refres2;

		BooleanResult bres1, bres2;
		IntegerResult ires1, ires2;
		DoubleResult dres1, dres2;
		StringResult sres1, sres2;
		DateResult dateResult1, dateResult2;
		BinderResult binres;

		SingleResult[] sresarr;
		OID ref, agg;

		int op = 0, param;
		OpCodes opcode = null;
	
		try {
			while (buffer.hasRemaining()) {
				try {				
					op = buffer.getInt();
					param = buffer.getInt();
					opcode = OpCodes.getOpCode(op);
	
	//				System.out.println("Executing " + OpCodes.opstr[opcode]);
					switch (opcode) {
						case ldE:
							if(param == 0)
								stack.push(new ReferenceResult(this.currmod.peek().getDatabaseEntry()));
							else
								stack.push(new ReferenceResult(new DBModule(store.offset2OID(param)).getDatabaseEntry()));
							break;
						case ldSE:
							if(param == 0)
								stack.push(new ReferenceResult(Session.getModuleSessionEntry(this.currmod.peek())));
							else
								stack.push(new ReferenceResult(Session.getModuleSessionEntry(new DBModule(store.offset2OID(param)))));
							break;
						case ldLE:
							stack.push(new ReferenceResult(this.envManager.getLocalStoreEntry()));
							break;
						case swap:
							AbstractQueryResult qelem2 = stack.pop();
							AbstractQueryResult qelem1 = stack.pop();
							stack.push(qelem2);
							stack.push(qelem1);
							break;
	
						case dup:
							stack.push(stack.peek());
							break;
	
						case pop:
							stack.pop();
							break;
							
						case ldcR_0:
							stack.push(new DoubleResult(0));
							break;
	
						case dsEnv:
						    	this.envManager.destroyEnvironment();
							break;
	
						case bind:
							AbstractQueryResult[] resarr = this.envManager.bind(param, this.currmod.peek());

							// TODO: add special opcode for lazyFailure check (can be foreseen by typechecker
							if ((resarr.length == 1) && (resarr[0] instanceof LazyFailureResult)) 
								throw ((LazyFailureResult) resarr[0]).getException();
						
							stack.push(resarr.length == 0 ? new BagResult() : resarr[0]);
							break;
	
						case bindTop:
							resarr = stack.bindTop(param);
							stack.push(resarr.length == 0 ? new BagResult() : resarr[0]);
							break;
							
						case bindAgg:						
							resarr = this.envManager.bindAggregate(param, this.currmod.peek());
							stack.push(resarr.length == 0 ? new BagResult() : resarr[0]);						
							break;
							
						case enterBinder:
							res = (Result)stack.pop();
							stack.enter(new Binder(param,res));
							break;
						case enterRefAsBinder:
							res = (Result)stack.pop();
							if(!(res instanceof ReferenceResult))
							    throw error("reference required, found " + SBQLInterpreterHelper.printFriendyResultType(res), buffer);
	
							if(res instanceof VirtualReferenceResult){
							    OID virtobj = new DBView(((VirtualReferenceResult)res).value).getVirtualObject();
							    stack.enter(new Binder(virtobj.getObjectNameId(), res));
							}else
							    stack.enter(new Binder(((ReferenceResult)res).value.getObjectNameId(), res));
							break;
						case ldI:
							stack.push(new IntegerResult(param));
							break;
	
						case ldcR:
							double valR = cp.lookUpDouble(param);
							stack.push(new DoubleResult(valR));
							break;
	
						case ldcS:
							String valS = cp.lookUpString(param);
							stack.push(new StringResult(valS));
							break;
	
						case ldTrue:
							stack.push(new BooleanResult(true));
							break;
	
						case ldFalse:
							stack.push(new BooleanResult(false));
							break;
	
						case ldBag:
							stack.push(new BagResult());
							break;
	
						case crI:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createIntegerChild(param, 0);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crB:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createBooleanChild(param, false);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crAgg:
							ires1 = (IntegerResult) stack.pop();
							refres = (ReferenceResult) stack.pop();
	//						ref = refres.value.getStore().createAggregateObject(param, refres.value, ires1.value);
							ref = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,ires1.value);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crAggCard:
							IntegerResult mincard = (IntegerResult) stack.pop(); //min card
							IntegerResult maxcard = (IntegerResult) stack.pop(); //max card
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createAggregateChild(param, mincard.value, mincard.value,maxcard.value);
	//						ref = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,ires1.value);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crCpx:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createComplexChild(param, 0);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crR:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createDoubleChild(param, 0);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crRef:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createReferenceChild(param, null);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crS:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createStringChild(param, "", 0);
							stack.push(new ReferenceResult(ref));
							break;
	
						case crAggCpx:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createComplexChild(param, 0);
							stack.push(new ReferenceResult(ref));
	
							break;
	
						case crAggRef:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createReferenceChild(param, null);
							stack.push(new ReferenceResult(ref));
	
							break;
	
						case crAggS:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createStringChild(param, "", 0);
							stack.push(new ReferenceResult(ref));
	
							break;
	
						case crAggR:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createDoubleChild(param, 0);
							stack.push(new ReferenceResult(ref));
	
							break;
	
						case crAggB:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createBooleanChild(param, false);
							stack.push(new ReferenceResult(ref));
	
							break;
	
						case crAggI:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createIntegerChild(param, 0);
							stack.push(new ReferenceResult(ref));
	
							break;
						case crAggD:
							refres = (ReferenceResult) stack.pop();
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,1);
							ref = agg.createDateChild(param, new Date());
							stack.push(new ReferenceResult(ref));
	
							break;
						case crDyn:
							res2 = (Result) stack.pop();
							res1 = (Result) stack.pop();
							if(!(res1 instanceof ReferenceResult)){
								throw error("create operator requires reference to parent", buffer);
							}
	
							refres = (ReferenceResult)res1;
							OID vid = SBQLInterpreterHelper.findVirtualObject(refres.value.getStore(), param, refres.value);
							if(vid != null){
								DBVirtualObjectsProcedure virt = new DBVirtualObjectsProcedure(vid);
								//we have virtual create
								stack.push(res2); //param
								stack.push(new ReferenceResult(virt.getView())); //view
								buffer = this.createVirtualSubroutine(buffer, cp);
								break;
	
							}
							agg = SBQLInterpreterHelper.findAggObject(refres.value.getStore(), param, refres.value,res2.elementsCount());
							bagres = SBQLInterpreterHelper.dynamicCreate(refres.value, param, res2);
	
							stack.push(bagres);
	
							break;
	
						case addI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new IntegerResult(ires1.value + ires2.value));
							break;
	
						case addR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new DoubleResult(dres1.value + dres2.value));
							break;
	
						case conS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new StringResult(sres1.value + sres2.value));
							break;
	
						case subI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new IntegerResult(ires1.value - ires2.value));
							break;
	
						case subR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new DoubleResult(dres1.value - dres2.value));
							break;
	
						case mulI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new IntegerResult(ires1.value * ires2.value));
							break;
	
						case mulR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new DoubleResult(dres1.value * dres2.value));
							break;
	
						case divI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
	
							if (ires2.value == 0)
								throw error("Divide by zero error.", buffer);
							stack.push(new IntegerResult(ires1.value / ires2.value));
							break;
	
						case divR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
	
							if (dres2.value == 0)
								throw error("Divide by zero error.", buffer);
							stack.push(new DoubleResult(dres1.value / dres2.value));
							break;
						case remI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new IntegerResult(ires1.value % ires2.value));
							break;
	
						case remR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new DoubleResult(dres1.value % dres2.value));
							break;
						case grI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new BooleanResult(ires1.value > ires2.value));
							break;
	
						case grR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new BooleanResult(dres1.value > dres2.value));
							break;
	
						case grS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(sres1.value.compareTo(sres2.value) > 0));
							break;
	
						case grEqI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new BooleanResult(ires1.value >= ires2.value));
							break;
	
						case grEqR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new BooleanResult(dres1.value >= dres2.value));
							break;
	
						case grEqS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(sres1.value.compareTo(sres2.value) >= 0));
							break;
	
						case loI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new BooleanResult(ires1.value < ires2.value));
							break;
	
						case loR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new BooleanResult(dres1.value < dres2.value));
							break;
	
						case loS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(sres1.value.compareTo(sres2.value) < 0));
							break;
	
						case loEqI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new BooleanResult(ires1.value <= ires2.value));
							break;
	
						case loEqR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new BooleanResult(dres1.value <= dres2.value));
							break;
	
						case loEqS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(sres1.value.compareTo(sres2.value) <= 0));
							break;
	
						case eqI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new BooleanResult(ires1.value == ires2.value));
							break;
	
						case eqR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new BooleanResult(dres1.value == dres2.value));
							break;
	
						case eqS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(sres1.value.compareTo(sres2.value) == 0));
							break;
	
						case eqB:
							bres2 = (BooleanResult) stack.pop();
							bres1 = (BooleanResult) stack.pop();
							stack.push(new BooleanResult(bres1.value == bres2.value));
							break;
	
						case eqStruct:
							strres2 = (StructResult) stack.pop();
							strres1 = (StructResult) stack.pop();
							stack.push(new BooleanResult(strres1.equals(strres2)));
							break;
						case eqRef:
							refres2 = (ReferenceResult) stack.pop();
							refres1 = (ReferenceResult) stack.pop();
							stack.push(new BooleanResult(refres1.value.equals(refres2.value)));
							break;
						case nEqI:
							ires2 = (IntegerResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							stack.push(new BooleanResult(ires1.value != ires2.value));
							break;
	
						case nEqR:
							dres2 = (DoubleResult) stack.pop();
							dres1 = (DoubleResult) stack.pop();
							stack.push(new BooleanResult(dres1.value != dres2.value));
							break;
	
						case nEqB:
							bres2 = (BooleanResult) stack.pop();
							bres1 = (BooleanResult) stack.pop();
							stack.push(new BooleanResult(bres1.value != bres2.value));
							break;
	
						case nEqS:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(sres1.value.compareTo(sres2.value) != 0));
							break;
						case nEqRef:
							refres2 = (ReferenceResult) stack.pop();
							refres1 = (ReferenceResult) stack.pop();
							stack.push(new BooleanResult(!refres1.value.equals(refres2.value)));
							break;
						case nEqStruct:
							strres2 = (StructResult) stack.pop();
							strres1 = (StructResult) stack.pop();
							stack.push(new BooleanResult(!strres1.equals(strres2)));
							break;
						case negI:
							ires1 = (IntegerResult) stack.pop();
							stack.push(new IntegerResult(-ires1.value));
							break;
						case negR:
							dres1 = (DoubleResult) stack.pop();
							stack.push(new DoubleResult(-dres1.value));
							break;
						case dynNeg:
							res1 = (Result) stack.pop();
							if(res1 instanceof CollectionResult)
								throw error("negation cannot be applied to " + SBQLInterpreterHelper.printFriendyResultType(res1), buffer);
							sinres1 = (SingleResult)res1;
							if(res1 instanceof IntegerResult)
								stack.push(new IntegerResult(-((IntegerResult)sinres1).value));
							else if(sinres1 instanceof DoubleResult)
								stack.push(new DoubleResult(-((DoubleResult)sinres1).value));
							else throw error("negation requires number value: " + SBQLInterpreterHelper.printFriendyResultType(sinres1), buffer);
							break;
						case notB:
							bres1 = (BooleanResult) stack.pop();
							stack.push(new BooleanResult(!bres1.value));
							break;
						case and:
							bres2 = (BooleanResult) stack.pop();
							bres1 = (BooleanResult) stack.pop();
							stack.push(new BooleanResult(bres1.value && bres2.value));
							break;
	
						case matchString:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(Pattern.matches(StringUtils.rewritePatternToRegEx(sres2.value), sres1.value)));
							break;
						case notMatchString:
							sres2 = (StringResult) stack.pop();
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(!Pattern.matches(StringUtils.rewritePatternToRegEx(sres2.value), sres1.value)));
							break;
	
						case or:
							bres2 = (BooleanResult) stack.pop();
							bres1 = (BooleanResult) stack.pop();
							stack.push(new BooleanResult(bres1.value || bres2.value));
							break;
	
						case crCntr:
							ires1 = (IntegerResult) stack.pop();
							this.envManager.createCounterEnvironment(ires1.value);
	//						stack.setCounterData(new CounterData(ires1.value));
							break;
	
						case endCntr:
							if (ConfigDebug.ASSERTS) assert stack.getCounterData() != null : "no counter at the top ENVS frame";
							CounterData cdt = stack.getCounterData();
							stack.push(new BooleanResult(cdt.current >= cdt.limit));
							break;
	
						case ldCntr:
							if (ConfigDebug.ASSERTS) assert stack.getCounterData() != null : "no counter at the top ENVS frame";
							stack.push(new IntegerResult(stack.getCounterData().current));
							break;
	
						case incCntr:
							if (ConfigDebug.ASSERTS) assert stack.getCounterData() != null : "no counter at the top ENVS frame";
							stack.getCounterData().current++;
							break;
	
						case extr:
							ires1 = (IntegerResult) stack.pop();
							res = (Result) stack.peek();
	
							stack.push(res.elementAt(ires1.value));
							break;
	
						case insPrt:
							res = (Result) stack.pop();
							bagres = (BagResult) stack.peek();
							for (SingleResult sr : res.elementsToArray()) {
								bagres.addElement(sr);
							}
							break;
	
						case insPrt2:
							res = (Result) stack.pop();
							bagres = (BagResult) stack.peek2();
							for (SingleResult sr : res.elementsToArray()) {
								bagres.addElement(sr);
							}
							break;
						case insPrt3:
							res = (Result) stack.pop();
							res1 = (Result) stack.pop();
							bagres = (BagResult) stack.peek2();
							for (SingleResult sr : res.elementsToArray()) {
								bagres.addElement(sr);
							}
							stack.push(res1);
							break;
	
						case bra:
							buffer.position(param);
							break;
	
						case lRet:
							ret.push(param);
							break;
	
						case braRet:
							buffer.position(ret.pop());
							break;
	
						case braTrue:
							res1 = (Result) stack.pop();
							if(!(res1 instanceof BooleanResult))
								throw error("boolean value required: " + SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							if (((BooleanResult)res1).value)
								buffer.position(param);
	
							break;
	
						case braFalse:
							res1 = (Result) stack.pop();
							if(!(res1 instanceof BooleanResult))
								throw error("boolean value required: " + SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							if (!((BooleanResult)res1).value)
								buffer.position(param);
	
							break;
	
						case cnt:
							res = (Result) stack.pop();
	
							stack.push(new IntegerResult(res.elementsCount()));
	
							break;
						case as:
							sinres1 = (SingleResult) stack.pop();
							stack.push(new BinderResult(Database.getStore().getName(param), sinres1));
							break;
	
						case colAs:
							res1 = (Result) stack.pop();
							colres1 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult sr : res1.elementsToArray())
								colres1.addElement(new BinderResult(Database.getStore().getName(param), sr));
							stack.push(colres1);
							break;
	
						case grAs:
							res2 = (Result) stack.pop();
	
							stack.push(new BinderResult(Database.getStore().getName(param), res2));
	
							break;
						case exist:
							res = (Result) stack.pop();
	
							stack.push(new BooleanResult(res.elementsCount() > 0 ? true : false));
	
							break;
						case iinc:
							((IntegerResult) stack.peek()).value += param;
							break;
						case idec:
							((IntegerResult) stack.peek()).value -= param;
							break;
	
						case crpd:
							res2 = (Result) stack.pop();
							res1 = (Result) stack.pop();
							bagres = new BagResult();
							for (SingleResult sr1 : res1.elementsToArray()) {
								for (SingleResult sr2 : res2.elementsToArray()) {
									strres2 = new StructResult();
									strres2.addField(sr1);
									strres2.addField(sr2);
									bagres.addElement(strres2);
								}
							}
	
							stack.push(bagres);
							break;
	
						case union:
							res2 = (Result) stack.pop();
							res1 = (Result) stack.pop();
							bagres = new BagResult();
	
							for(SingleResult sr: res1.elementsToArray())
							{
								bagres.addElement(sr);
							}
							for(SingleResult sr: res2.elementsToArray())
							{
								bagres.addElement(sr);
							}
							stack.push(bagres);
							break;
	
						case fltn:
							res = (Result) stack.pop();
							stack.push(res.elementsCount() == 1 ? (SingleResult) res.elementAt(0) : res);
							break;
	
						case single:
							res = (Result) stack.pop();
							if(res.elementsCount() == 1)
								stack.push( res.elementAt(0));
							else
								throw error("single value required, found: " + SBQLInterpreterHelper.printFriendyResultType(res) + " (" + res.elementsCount() + ") ->" + res.toString() +"<- ",buffer);
							break;
						case nonEmpty:
							if(((Result) stack.peek()).elementsCount() == 0)
								throw error("non empty bag required " ,buffer);
							break;
						case ref:
						    	refres1 = ((ReferenceResult) stack.pop()).clone();
							refres1.refFlag = true;
							stack.push(refres1);
							break;
						case colRef:
							res1 = (Result) stack.peek();
							for (SingleResult rr : res1.elementsToArray()){
									((ReferenceResult) rr).refFlag = true;
							}
	
							break;
						case dynRef:
							res1 = (Result) stack.peek();
							for (SingleResult rr :  res1.elementsToArray()){
								if(rr instanceof ReferenceResult)
									((ReferenceResult) rr).refFlag = true;
								else
									throw error("cannot performe 'ref' on non-reference: " + SBQLInterpreterHelper.printFriendyResultType(rr),buffer);
							}
	
							break;
						case derefI:
							refres = (ReferenceResult) stack.pop();
							stack.push(new IntegerResult(refres.value.derefInt()));
							break;
						case derefR:
							refres = (ReferenceResult) stack.pop();
							stack.push(new DoubleResult(refres.value.derefDouble()));
							break;
						case derefB:
							refres = (ReferenceResult) stack.pop();
							stack.push(new BooleanResult(refres.value.derefBoolean()));
							break;
						case derefCpx:
							refres = (ReferenceResult) stack.pop();
							stack.push(SBQLInterpreterHelper.derefComplex(refres.value));
							break;
						case derefS:
							refres = (ReferenceResult) stack.pop();
							stack.push(new StringResult(refres.value.derefString()));
							break;
						case derefRef:
							refres = (ReferenceResult) stack.pop();
							OID value = refres.value.derefReference();
							if(value == null)
							    throw error("'" + value.getObjectName() + "' is a dangling pointer",buffer);
							stack.push(new ReferenceResult(refres.value.derefReference()));
							break;
						case derefColI:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray())
								colres2.addElement(new IntegerResult(((ReferenceResult) rr).value.derefInt()));
							stack.push(colres2);
							break;
						case derefColR:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray())
								colres2.addElement(new DoubleResult(((ReferenceResult) rr).value.derefDouble()));
							stack.push(colres2);
							break;
						case derefColB:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray())
								colres2.addElement(new BooleanResult(((ReferenceResult) rr).value.derefBoolean()));
							stack.push(colres2);
							break;
						case derefColCpx:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray()) {
								colres2.addElement(SBQLInterpreterHelper.derefComplex(((ReferenceResult)rr).value));
							}
							stack.push(colres2);
							break;
						case derefColS:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray())
								colres2.addElement(new StringResult(((ReferenceResult) rr).value.derefString()));
							stack.push(colres2);
							break;
						case derefColRef:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray())
							{
							    OID refvalue = ((ReferenceResult) rr).value.derefReference();
							    assert !refvalue.isAggregateObject() : "! refvalue.isAggregateObject()";
								if(refvalue == null)
								    throw error("'" + refvalue.getObjectName() + "' is a dangling pointer", buffer);
								colres2.addElement(new ReferenceResult(refvalue));
							}
							stack.push(colres2);
							break;
						case dynDeref:
							res1 = (Result) stack.peek();
							if(res1 instanceof VirtualReferenceResult && !((VirtualReferenceResult)res1).refFlag)
							{
								buffer = this.derefVirtualSubroutine(buffer, cp);
							}
							else if(res1 instanceof StructResult)
							{
								buffer = this.derefStructSubroutine(buffer, cp);
							}
							else if(res1 instanceof BinderResult)
							{
								buffer = this.derefBinderSubroutine((BinderResult)stack.pop(), buffer, cp);
							}
							else {
							    switch(this.mode){
							    case Meta:
								stack.push(SBQLInterpreterHelper.doDynamicDereferenceMetabase((Result) stack.pop()));
							    case Data:
							    default:
								stack.push(SBQLInterpreterHelper.doDynamicDereference((Result) stack.pop()));
							    }
							}
							break;
						case i2r:
							ires1 = (IntegerResult) stack.pop();
							stack.push(new DoubleResult(ires1.value));
							break;
						case r2i:
							dres1 = (DoubleResult) stack.pop();
							stack.push(new IntegerResult((int) dres1.value));
							break;
						case i2s:
							ires1 = (IntegerResult) stack.pop();
							stack.push(new StringResult(String.valueOf(ires1.value)));
							break;
						case r2s:
							dres1 = (DoubleResult) stack.pop();
							stack.push(new StringResult(String.valueOf(dres1.value)));
							break;
						case d2s:
							dateResult1 = (DateResult) stack.pop();
							stack.push(new StringResult(dateResult1.format()));
							break;
						case b2s:
							bres1 = (BooleanResult) stack.pop();
							stack.push(new StringResult(String.valueOf(bres1.value)));
							break;
						case s2i:
							sres1 = (StringResult) stack.pop();
							try {
								stack.push(new IntegerResult(Integer.parseInt(sres1.value)));
							}
							catch (NumberFormatException e) {
								throw error("unable to convert string '" + sres1.value+ "' to integer",buffer);
							}
							break;
						case s2r:
							sres1 = (StringResult) stack.pop();
							try {
								stack.push(new DoubleResult(Double.parseDouble(sres1.value)));
							}
							catch (NumberFormatException e) {
								throw error("unable to convert string '" + sres1.value+ "' to real",buffer);
							}
							break;
						case s2b:
							sres1 = (StringResult) stack.pop();
							stack.push(new BooleanResult(Boolean.parseBoolean(sres1.value)));
							break;
						case dyn2r:
							res1 = (Result) stack.pop();
							if(res1 instanceof CollectionResult)
								throw error("to real coerce cannot be applied to " +  SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							stack.push(SBQLInterpreterHelper.dynamic2Real((SingleResult) res1));
							break;
						case dyn2i:
							res1 = (Result) stack.pop();
							if(res1 instanceof CollectionResult)
								throw error("to integer coerce cannot be applied to " +  SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							stack.push(SBQLInterpreterHelper.dynamic2Int((SingleResult) res1));
							break;
						case dyn2s:
							res1 = (Result) stack.pop();
							if(res1 instanceof CollectionResult)
								throw error("to string coerce cannot be applied to " +  SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							stack.push(SBQLInterpreterHelper.dynamic2String((SingleResult) res1));
							break;
						case dyn2b:
							res1 = (Result) stack.pop();
							if(res1 instanceof CollectionResult)
								throw error("to boolean coerce cannot be applied to " +  SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							stack.push(SBQLInterpreterHelper.dynamic2Bool((SingleResult) res1));
							break;
						case storeB:
							refres1 = (ReferenceResult) stack.pop();
							refres1.value.updateBooleanObject(((BooleanResult) stack.pop()).value);
							stack.push(refres1);
							break;
						case storeI:
							refres1 = (ReferenceResult) stack.pop();
							ires1 = (IntegerResult) stack.pop();
							refres1.value.updateIntegerObject(ires1.value);
							stack.push(refres1);
							break;
						case storeR:
							refres1 = (ReferenceResult) stack.pop();
							refres1.value.updateDoubleObject(((DoubleResult) stack.pop()).value);
							stack.push(refres1);
							break;
						case storeS:
							refres1 = (ReferenceResult) stack.pop();
							refres1.value.updateStringObject(((StringResult) stack.pop()).value);
							stack.push(refres1);
							break;
						case storeRef:
							refres1 = (ReferenceResult) stack.pop();
							refres2 = (ReferenceResult) stack.pop();
							assert !refres2.value.isAggregateObject():"!refres2.value.isAggregateObject()";
							refres1.value.updateReferenceObject(refres2.value);
							stack.push(refres1);
							break;
						case storeRevRef:
							refres1 = (ReferenceResult) stack.pop();
							refres2 = (ReferenceResult) stack.pop();
							assert refres2.value.isReferenceObject():"refres2.value.isReferenceObject() == true";
							assert refres1.value.isReferenceObject():"refres1.value.isReferenceObject() == true";
							refres1.value.setReversePointer(refres2.value);
							refres2.value.setReversePointer(refres1.value);
	//						stack.push(refres2);
	//						stack.push(refres1);
							break;
						case dynStore:
							res1 = (Result) stack.pop();
							res2 = (Result) stack.pop();
							if(res1 instanceof SingleResult && res2 instanceof SingleResult){
								if(res1 instanceof VirtualReferenceResult){
									stack.push(res1); //result
									stack.push(res2); //param
	//								push the view reference onto the top
	//								stack.push(new ReferenceResult(((VirtualReferenceResult)res1).value));
									stack.push(res1); //virtual ref (to nested)
	
									buffer = this.updateVirtualSubroutine(buffer, cp);
								}
								else if(res1 instanceof ReferenceResult){
									stack.push(SBQLInterpreterHelper.doDynamicUpdate( (ReferenceResult)res1, (SingleResult)res2));
								}
								else { throw error("Left value of an update operator must be a reference",buffer); }
							}else throw error("update cannot be performed: '" + SBQLInterpreterHelper.printFriendyResultType(res1) + "' := '" + SBQLInterpreterHelper.printFriendyResultType(res2) +"'",buffer);
	
							break;
						case move:
							refres2 = (ReferenceResult)stack.pop();
							refres1 = (ReferenceResult)stack.pop();						
							agg = SBQLInterpreterHelper.findAggObject(refres1.value.getStore(), refres2.value.getObjectNameId(), refres1.value, 1);
							SBQLInterpreterHelper.move(refres2.value, agg);						
							stack.push(new BagResult());
							break;
						case moveCol:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(!(res1 instanceof ReferenceResult))
								throw error("Lvalue - insert operator requires reference to environment: '" + SBQLInterpreterHelper.printFriendyResultType(res1) +"'",buffer);
							refres1 = (ReferenceResult)res1;
							for(SingleResult sres: res2.elementsToArray()){
								if(!(sres instanceof ReferenceResult)){
									throw error("rvalue - insert operator requires reference: '" + SBQLInterpreterHelper.printFriendyResultType(sres) + "'",buffer);
								}
								refres2 = (ReferenceResult)sres;
								agg = SBQLInterpreterHelper.findAggObject(refres1.value.getStore(), refres2.value.getObjectNameId(), refres1.value, 1);
								SBQLInterpreterHelper.move(refres2.value, agg);
							}
							stack.push(new BagResult());
							break;
						case moveNamed:
							binres = (BinderResult)stack.pop();
							refres1 = (ReferenceResult)stack.pop();						
							agg = SBQLInterpreterHelper.findAggObject(refres1.value.getStore(), Database.getNameIndex().addName(binres.getName()), refres1.value, 1);
							refres2 = (ReferenceResult)binres.value;
							refres2.value.renameObject(Database.getNameIndex().addName(binres.getName()));
							SBQLInterpreterHelper.move(refres2.value, agg);						
							stack.push(new BagResult());
							break;
						case moveNamedCol:
							binres = (BinderResult)stack.pop();
							refres1 = (ReferenceResult)stack.pop();
							int nameid = Database.getNameIndex().addName(binres.getName());
							agg = SBQLInterpreterHelper.findAggObject(refres1.value.getStore(), nameid, refres1.value, 1);						
							for(SingleResult sres: binres.value.elementsToArray()){																					
								refres2 = (ReferenceResult)sres;
								refres2.value.renameObject(nameid);
								SBQLInterpreterHelper.move(refres2.value, agg);
							}
							stack.push(new BagResult());
							break;
	
						case virtMove:
							refres2 = (ReferenceResult) stack.pop();
							VirtualReferenceResult virtrefres1 = (VirtualReferenceResult) stack.pop();
	
							 vid = SBQLInterpreterHelper.findVirtualObject(virtrefres1.value.getStore(), /*refres2.value.getObjectNameId()*/ param, virtrefres1.value);
							if(vid != null){
								DBVirtualObjectsProcedure virt = new DBVirtualObjectsProcedure(vid);
								//we have virtual create
								stack.push(refres2); //param
								VirtualReferenceResult virtres = new VirtualReferenceResult(virt.getView(), virtrefres1.getSeed());
								virtres.parent = virtrefres1;
								stack.push(virtres);
							//	stack.push(new ReferenceResult(virt.getView(), virtrefres1)); //view
								buffer = this.createVirtualSubroutine(buffer, cp);
								break;
	
							}
							assert false : "virtual insert requires on_new operator";
							break;
						case rename:
							refres1 = (ReferenceResult)stack.pop();
							assert refres1.value.isAggregateObject() : "QRES.top() == aggregate object reference";
							refres2 = (ReferenceResult)stack.peek();
							refres2.value.renameObject(param);						
							SBQLInterpreterHelper.move(refres2.value, refres1.value);
							
							break;
						case colRename:
							refres1 = (ReferenceResult)stack.pop();
							assert refres1.value.isAggregateObject() : "QRES.top() == aggregate object reference";
							res = (Result)stack.peek();						
							for(SingleResult sres : res.elementsToArray()){
								refres2 = ((ReferenceResult)sres);
								refres2.value.renameObject(param);
								SBQLInterpreterHelper.move(refres2.value, refres1.value);
							}
							break;
						case del:
							res1 = (Result)stack.pop();
							if(!(res1 instanceof ReferenceResult))
							    throw error("delete operator requires reference: '" + SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							if(res1 instanceof VirtualReferenceResult){
							    stack.push(res1);
							    buffer = this.deleteVirtualSubroutine(buffer, cp);
							    break;
							}
							((ReferenceResult)res1).value.deleteSafe();
							break;
	
						case delChildren:
							res1 = (Result)stack.peek();
							if(!(res1 instanceof ReferenceResult))
							    throw error("delete children operator requires reference: '" + SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							if(res1 instanceof VirtualReferenceResult)
								throw error("delete children operator requires reference: '" + SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							((ReferenceResult)res1).value.deleteAllChildrenSafe();
							//TODO we also should remove from the ENVS corresponding environment
							break;
							
						case crLE:
						    this.envManager.createLocalEnvironment();
							break;
						case initLoc:
							//for bind
	//						assert stack.getLocalEnv() != null :"no local environment";
							envManager.initializeNestedRootEnvironment(stack, this.envManager.getLocalStoreEntry(), false);
							break;
						case call:
							res1 = (Result) stack.pop();
							if(!(res1 instanceof ReferenceResult))
								throw error("(" + SBQLInterpreterHelper.printFriendyResultType(res1) + ") - procedure call requires a reference to procedure",buffer);
							refres1 = (ReferenceResult)res1;
							if(refres1.value instanceof RemoteDefaultStoreOID){
								ires1 = (IntegerResult)stack.pop(); //number of parameters
								LinkManager lm = LinkManager.getInstance();
								stack.push(lm.remoteCall(refres1, stack, ires1.value, Session.getUserContext()));
								break;
							}
							switch (new DBObject(refres1.value).getObjectKind().getKindAsInt()) {
								case (DataObjectKind.PROCEDURE_OBJECT):
								case (DataObjectKind.VIRTUAL_OBJECTS_PROCEDURE_OBJECT):
									// intercepts proxy procedures calls and direct them to
									// web service stack handling routine
	
									IProxyFacade pm = WSManagersFactory.createProxyManager();
									DBProcedure proc = new DBProcedure(refres1.value);
									OID container = proc.getOID().getParent().getParent();
	
									if (pm != null) {
	
										if (pm.isProxy(container))
										{
											Result output = pm.remoteProxyCall(refres1.value, stack);
	
											if (output != null) {
												stack.push(output);
											} else {
												stack.push(new BagResult());
											}
	
											break;
										}
									}
	
									byte[] procCode = proc.getBinaryCode();
									byte[] procPool = proc.getConstantPool();								
									this.envManager.createProcedureEnvironment(refres1, new ReturnData(buffer,cp));
									buffer = ByteBuffer.wrap(procCode);
									cp = new ConstantPool(procPool);
									break;
								case (DataObjectKind.INDEX_OBJECT):
									bagres = SBQLInterpreterHelper.callIndex(refres1.value, stack);
									stack.push(bagres.elementsCount() != 1 ? bagres: bagres.elementAt(0));
									break;
								case (DataObjectKind.SCHEMA_OBJECT):
									bagres = new BagResult();
									for (OID linkoid : new DBSchema(refres1.value).getLinksOIDs())
										bagres.addElement(new ReferenceResult(linkoid));
									stack.push(bagres.elementsCount() != 1 ? bagres: bagres.elementAt(0));
									break;
								default:
										throw error("unable to call object with name " + refres1.value.getObjectName(),buffer);
							}
							break;
						case external: //TW
							sres1 = (StringResult)stack.pop(); //library function name
							res = (Result)stack.pop(); //external call parameters
	
							stack.push(Session.getCurrent().extRoutines.invoke(sres1.value, res));
	
							break;
						case ret:
							stack.push(new BagResult());
							//no break here
						case retv:
							this.cleanEnvironmentOnReturn();
							ReturnData pd = this.envManager.getReturnData();
							buffer = pd.getReturnToCode();
							cp = pd.getPool();
							this.envManager.destroyProcedureEnvironment(false);
							break;
						case retSub:
						    	ReturnData subdata = this.envManager.getReturnData();
						    	buffer = subdata.getReturnToCode();
						    	this.envManager.destroySubroutineEnvironment();
							break;
						case bswap2:
							AbstractQueryResult a = stack.pop();
							AbstractQueryResult b = stack.pop();
							AbstractQueryResult c = stack.pop();
							stack.push(b);
							stack.push(a);
							stack.push(c);
							break;
						case dup2:
							a = stack.peek();
							b = stack.peek2();
							stack.push(b);
							stack.push(a);
							break;
						case dup_x1:
							a = stack.pop();
							b = stack.pop();
							stack.push(a);
							stack.push(b);
							stack.push(a);
							break;
						case dup_x2:
							a = stack.pop();
							b = stack.pop();
							c = stack.pop();
							stack.push(a);
							stack.push(c);
							stack.push(b);
							stack.push(a);
							break;
						case in:
							res2 = (Result) stack.pop();
							res1 = (Result) stack.pop();
							stack.push(SBQLInterpreterHelper.doIn(res1, res2));
							break;
						case intersect:
							res2 = (Result) stack.pop();
							res1 = (Result) stack.pop();
							stack.push(SBQLInterpreterHelper.doIntersect(res1, res2));
							break;
						case diff:
							res2 = (Result) stack.pop();
							res1 = (Result) stack.pop();
							stack.push(SBQLInterpreterHelper.doDifference(res1, res2));
							break;
						case unique:
							res1 = (Result) stack.pop();
							stack.push(SBQLInterpreterHelper.doUnique(res1));
							break;
						// added by raist, 01.08.06
	
						case modPush:
							DBModule nwmod = new DBModule(store.offset2OID(param));
	
							currmod.push(nwmod);
	
							stack.resetBaseFrame();
							envManager.initializeModuleEnvironment(nwmod, true);
							break;
	
						case modPop:
							currmod.pop();
							break;
	
						case fnd:
							refres = (ReferenceResult) stack.pop();
							stack.push(new BagResult());
	
							for (OID oid : refres.value.derefComplex()) {
								if (oid.getObjectNameId() == param) {
									stack.pop();
									stack.push(new ReferenceResult(oid));
								}
							}
	
							break;
	
						case orby:
							res1 = (Result) stack.pop();
							//TODO sequences!
							bagres = new BagResult();
							if(res1.elementsCount() > 0){
								sresarr = res1.elementsToArray();
								sresarr = SBQLInterpreterHelper.sort(sresarr);
	
								//extract the sorted element from the structure (it is named!)
								//and add it to a final result
								for(SingleResult s: sresarr){
									StructResult str = (StructResult) s;
									SingleResult sortedValue = (SingleResult)((BinderResult)str.fieldsToArray()[0]).value;
									bagres.addElement(sortedValue);
								}
							}
							stack.push(bagres);
							break;
	
						case dynAdd:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(res2 instanceof SingleResult && res1 instanceof SingleResult){
								if(res1 instanceof StringResult || res2 instanceof StringResult){
									sinres1 = SBQLInterpreterHelper.dynamic2String((SingleResult)res1);
									sinres2 = SBQLInterpreterHelper.dynamic2String((SingleResult)res2);
									stack.push(new StringResult(((StringResult)sinres1).value + ((StringResult)sinres2).value));
								}else stack.push(SBQLInterpreterHelper.doDynamicBinaryArithmetic((SingleResult)res1,(SingleResult)res2, opcode));
							}else throw error("operator '+' cannot be applied to: " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							break;
						case dynSub:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(res2 instanceof SingleResult && res1 instanceof SingleResult){
							stack.push(SBQLInterpreterHelper.doDynamicBinaryArithmetic((SingleResult)res1,(SingleResult)res2, opcode));
							}else throw error("operator '-' cannot be applied to: " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							break;
						case dynMul:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(res2 instanceof SingleResult && res1 instanceof SingleResult){
							stack.push(SBQLInterpreterHelper.doDynamicBinaryArithmetic((SingleResult)res1,(SingleResult)res2, opcode));
							}else throw error("operator '*' cannot be applied to: " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							break;
						case dynDiv:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(res2 instanceof SingleResult && res1 instanceof SingleResult){
							stack.push(SBQLInterpreterHelper.doDynamicBinaryArithmetic((SingleResult)res1,(SingleResult)res2, opcode));
							}else throw error("operator '/' cannot be applied to: " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							break;
						case dynRem:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(res2 instanceof SingleResult && res1 instanceof SingleResult){
							stack.push(SBQLInterpreterHelper.doDynamicBinaryArithmetic((SingleResult)res1,(SingleResult)res2, opcode));
							}else throw error("operator '%' cannot be applied to: " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							break;
						case dynGr:
						case dynLo:
						case dynGrEq:
						case dynLoEq:
						case dynEq:
						case dynNEq:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(!(res2 instanceof SingleResult) || !(res1 instanceof SingleResult))
								throw error("comparison operator cannot be applied to " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							stack.push(SBQLInterpreterHelper.doDynamicComparison((SingleResult)res1,(SingleResult)res2,opcode));
							break;
						case dynOr:
						case dynAnd:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							if(!(res2 instanceof SingleResult) || !(res1 instanceof SingleResult))
								throw error("logical operator cannot be applied to: " + SBQLInterpreterHelper.printFriendyResultType(res1) + " and " + SBQLInterpreterHelper.printFriendyResultType(res2),buffer);
							stack.push(SBQLInterpreterHelper.doDynamicLogicalOperator((SingleResult)res1,(SingleResult)res2,opcode));
							break;
						case dynNot:
							res1 = ((Result)stack.peek());
							if(res1 instanceof BooleanResult)
								((BooleanResult)res1).value = !((BooleanResult)res1).value;
							else throw error("'not' operator cannot be applied to " + SBQLInterpreterHelper.printFriendyResultType(res1) + " type",buffer);
							break;
	
						 case crvid:
							res1 = (Result) stack.pop(); //virtual objects result
							refres2 = (ReferenceResult) stack.pop(); //reference to virtual objects procedure
							DBVirtualObjectsProcedure vop = new DBVirtualObjectsProcedure(refres2.value);
							if(ConfigDebug.ASSERTS) assert vop.isValid() : "reference of not a virtual objects procedure";
							BagResult superseed = new BagResult();
							if(refres2.parent != null && refres2.parent instanceof VirtualReferenceResult){
								for(SingleResult s : ((VirtualReferenceResult)refres2.parent).getSeed().elementsToArray())
									superseed.addElement(s);
							}
							bagres = new BagResult();
							for (SingleResult seed : res1.elementsToArray()){
								BagResult newseed = new BagResult();
								newseed.addAll(superseed);
								newseed.addElement(seed);
								VirtualReferenceResult vref = new VirtualReferenceResult(vop.getView(),newseed.elementsCount() == 1 ? newseed.elementAt(0) : newseed);
								bagres.addElement(vref);
							}
							stack.push(bagres);
							break;
	
						case rstCntr:
							if(ConfigDebug.ASSERTS) assert stack.getCounterData() != null : "there is no counter on ENVS: rstCntr";
							stack.getCounterData().current = 0;
							break;
						case rbdCntr:
							if(ConfigDebug.ASSERTS) assert stack.getCounterData() != null : "there is no counter on ENVS: rbdCntr";
							ires1 = (IntegerResult) stack.pop();
							stack.getCounterData().limit = ires1.value;
							break;
						case ldvGenPrc:
							refres1 = (ReferenceResult)stack.pop();
							DBView view = new DBView(refres1.value);
							//get generic procedure
							OID genproid = view.getGenericProcByName(Database.getStore().getName(param));
							if(genproid == null)
								throw error("unable to performe requested operation on the object",buffer);
							if(refres1 instanceof VirtualReferenceResult)
							    stack.push(new ReferenceResult(view.getGenericProcByName(Database.getStore().getName(param)), refres1));
							else
							    stack.push(new ReferenceResult(view.getGenericProcByName(Database.getStore().getName(param)), null));
							break;
	
						case ifnSack:
							res1 = (Result)stack.pop();
							if(res1 instanceof ReferenceResult && ((ReferenceResult)res1).value instanceof DefaultStoreOID &&((ReferenceResult)res1).value.isComplexObject())
							{
								vop = new DBVirtualObjectsProcedure(((ReferenceResult)res1).value);
								if(!vop.isValid())
									buffer.position(param);
							}else buffer.position(param);
	
							break;
						case max:
							stack.push(SBQLInterpreterHelper.doMax((Result)stack.pop()));
							break;
						case min:
							stack.push(SBQLInterpreterHelper.doMin((Result)stack.pop()));
							break;
						case avg:
							stack.push(SBQLInterpreterHelper.doAvg((Result)stack.pop()));
							break;
						case rng:
							res2 = (Result)stack.pop();
							res1 = (Result)stack.pop();
							colres1 = SBQLInterpreterHelper.createProperCollection(res1);
	
							for(SingleResult sr: res2.elementsToArray()){
								if(!(sr instanceof IntegerResult))
									throw error("(" + SBQLInterpreterHelper.printFriendyResultType(sr)+")" + " - index must be an integer value " ,buffer);
								if(((IntegerResult)sr).value > 0 && ((IntegerResult)sr).value <= res1.elementsCount()){
									colres1.addElement(res1.elementAt(((IntegerResult)sr).value - 1));
								}
							}
							stack.push(colres1);
							break;
	
						case tobag:
							res1 = (Result)stack.pop();
							if(res1 instanceof StructResult){
								strres1 = (StructResult)res1;
								bagres = new BagResult();
								for(SingleResult sr : strres1.fieldsToArray()){
									bagres.addElement(sr);
								}
								stack.push(bagres);
							}
							else stack.push(res1);
							break;
						case tostruct:
							res1 = (Result)stack.pop();
							switch(res1.elementsCount())
							{
								case 0:
								    throw error("cannot convert an empty bag to a struct",buffer);
								case 1:
								    stack.push(res1);
								    break;
								default: {
								    strres1 = new StructResult();
								    for(SingleResult sr: res1.elementsToArray()){
									strres1.addField(sr);
								    }
								    stack.push(strres1);
								}
							}
							break;
						case derefStruct:
							buffer = this.derefStructSubroutine(buffer, cp);
							break;
						case derefBinder:
							binres = (BinderResult)stack.pop();
							buffer = this.derefBinderSubroutine(binres, buffer, cp);
							break;
						case exception:
							//TODO design exceptions/signals in virtual machine
							throw error("exception in virtual machine",buffer);
	
	
						case crnEnv:
							if (ConfigDebug.ASSERTS)
								assert stack.peek() instanceof SingleResult : "nested expects a single result";
							sinres1 = (SingleResult)stack.pop();
							envManager.createNestedEnvironment(sinres1);
							if(envManager.on_navigate() != null){
							    stack.push(envManager.on_navigate());
							    buffer = this.navigateVirtualSubroutine(buffer, cp);
							}
	
							break;
						case dsnEnv:
							envManager.destroyNestedEnvironment();
							break;
	
						case execsql:
							StringResult module = (StringResult)stack.pop();
							StringResult pattern = (StringResult)stack.pop();
							StringResult query = (StringResult)stack.pop();
	
							Wrapper wrapper = Database.getModuleByName(module.value).getWrapper();
							Result result = wrapper.executeSqlQuery(query.value, pattern.value, this);
							stack.push(result);
							break;
	
	//////////// DATES //////////////////////////////////////////////
						case crD:
							refres = (ReferenceResult) stack.pop();
							ref = refres.value.createDateChild(param, new Date());
							stack.push(new ReferenceResult(ref));
							break;
						case derefD:
							refres = (ReferenceResult) stack.pop();
							stack.push(new DateResult(refres.value.derefDate()));
							break;
						case ldcD:
							Date valD = cp.lookUpDate(param);
							stack.push(new DateResult(valD));
							break;
						case storeD:
							refres1 = (ReferenceResult) stack.pop();
							refres1.value.updateDateObject(((DateResult) stack.pop()).value);
							stack.push(refres1);
							break;
						case dyn2d:
							res1 = (Result) stack.pop();
							if(res1 instanceof CollectionResult)
								throw error("to date coerce cannot be applied to " +  SBQLInterpreterHelper.printFriendyResultType(res1),buffer);
							stack.push(SBQLInterpreterHelper.dynamic2Date((SingleResult) res1));
							break;
						case eqD:
							dateResult2 = (DateResult)stack.pop();
							dateResult1 = (DateResult)stack.pop();
							stack.push(new BooleanResult(dateResult1.value.compareTo(dateResult2.value) == 0));
							break;
						case grD:
							dateResult2 = (DateResult)stack.pop();
							dateResult1 = (DateResult)stack.pop();
							stack.push(new BooleanResult(dateResult1.value.compareTo(dateResult2.value) > 0));
							break;
						case grEqD:
							dateResult2 = (DateResult)stack.pop();
							dateResult1 = (DateResult)stack.pop();
							stack.push(new BooleanResult(dateResult1.value.compareTo(dateResult2.value) >= 0));
							break;
						case loD:
							dateResult2 = (DateResult)stack.pop();
							dateResult1 = (DateResult)stack.pop();
							stack.push(new BooleanResult(dateResult1.value.compareTo(dateResult2.value) < 0));
							break;
						case loEqD:
							dateResult2 = (DateResult)stack.pop();
							dateResult1 = (DateResult)stack.pop();
							stack.push(new BooleanResult(dateResult1.value.compareTo(dateResult2.value) <= 0));
							break;
						case dateprec:
							sres1 = (StringResult)stack.pop();
							res = (Result)stack.pop();
	
							if(!(res instanceof BagResult))
							{
								bagres = new BagResult();
								bagres.addAll(res);
								res = bagres;
							}
	
							bagres = (BagResult)res;
							res = new BagResult();
							for(Result date : bagres.elementsToArray())
								((BagResult)res).addElement(new DateResult(DateUtils.formatDatePrecission(((DateResult)date).value, sres1.value)));
	
							stack.push(res);
							break;
						case derefColD:
							res1 = (Result) stack.pop();
							colres2 = SBQLInterpreterHelper.createProperCollection(res1);
							for (SingleResult rr : res1.elementsToArray())
								colres2.addElement(new DateResult(((ReferenceResult) rr).value.derefDate()));
							stack.push(colres2);
							break;
						case s2d:
						    stack.push(SBQLInterpreterHelper.dynamic2Date((StringResult) stack.pop()));
						    break;
						case random:
							ires2 = (IntegerResult)stack.pop();
							ires1 = (IntegerResult)stack.pop();
							stack.push(new IntegerResult(RandomUtils.next(ires1.value, ires2.value)));
							break;
	
						case randomObj:
							res = (Result)stack.pop();
							if(!(res instanceof BagResult))
							{
								bagres = new BagResult();
								bagres.addAll(res);
								res = bagres;
							}
							bagres = (BagResult)res;
							if(bagres.elementsCount() > 0)
								stack.push(bagres.elementAt(RandomUtils.next(0, bagres.elementsCount() - 1)));
							else
								stack.push(bagres);
							break;
						case crInstRef:
							refres2 = (ReferenceResult) stack.pop();
							refres1 = (ReferenceResult) stack.pop();
							refres2.value.setInstanceOfReference(refres1.value);
							stack.push(refres2);
						break;
						case instof:
							refres1 = (ReferenceResult)stack.pop();
							refres2 = (ReferenceResult)stack.pop();
							stack.push(new BooleanResult(false));
							OID clsid = refres2.value.derefInstanceOfReference();
							if(clsid != null){
								if(clsid.equals(refres1.value)){
									((BooleanResult)stack.peek()).value = true;
								}else {
									DBClass cls = new DBClass(clsid);
									assert cls.isValid() : "class must be DBClass";
									((BooleanResult)stack.peek()).value = cls.isSubClassOf(refres1.value);
									break;
	
								}
							}
							break;
						case castClass:
						    res2 = (Result)stack.pop();
						    refres1 = (ReferenceResult)stack.pop();
						    DBClass cls = new DBClass(refres1.value);
						    bagres = new BagResult();
						    for(SingleResult sinres: res2.elementsToArray()){
							refres = (ReferenceResult)sinres;
							OID directClassid = refres.value.derefInstanceOfReference();
							DBClass directClass = new DBClass(directClassid);
							if(directClassid.equals(cls.getOID()) || directClass.isSubClassOf(cls.getOID())){
							    bagres.addElement(refres);
							}
						    }
						    stack.push(bagres);
						    break;
						case dynCast:
						    res2 = (Result)stack.pop();
						    res1 = (Result)stack.pop();
						    stack.push(SBQLInterpreterHelper.doDynamicCast(res1, res2));
	
						    break;
						case reflName:
						    res1 = (Result)stack.pop();
						    if(res1 instanceof ReferenceResult){
						    	stack.push(new StringResult(((ReferenceResult)res1).value.getObjectName()));
						    }else
						    	throw error("object reference required",buffer);
						    break;
						case reflParent:
						    res1 = (Result)stack.pop();
						    if(res1 instanceof ReferenceResult){
						    	OID parent = ((ReferenceResult)res1).value.getParent();
						    	if(parent == null){
						    		throw error("unable to find parent object for " + ((ReferenceResult)res1).value.getObjectName(),buffer);
						    	}
						    	while(parent.isAggregateObject()){
						    		parent = parent.getParent();
						    	}
						    	stack.push(new ReferenceResult(parent));
						    	}else
						    		throw error("object reference required",buffer);
						    break;
						case reflBind:
						    res1 = (Result)stack.pop();
						    if(res1 instanceof StringResult){
						    	resarr = this.envManager.bind(Database.getNameIndex().name2id(((StringResult)res1).value), this.currmod.peek());
						    	stack.push(resarr.length == 0 ? new BagResult() : resarr[0]);
						    }else
						    	throw error("string value required",buffer);
						    break;
						case atleast:
						    res1 = (Result)stack.peek();
						    if(res1.elementsCount() < param)
						    	throw error("bag contains to few elements: found " + res1.elementsCount() + ", required at least " + param,buffer);
						break;
						case atmost:
						    res1 = (Result)stack.peek();
						    if(res1.elementsCount() > param)
						    	throw error("bag contains to many elements: found " + res1.elementsCount() + ", required at most " + param,buffer);
						break;
						case remoteAsyncQuery:
						case remoteQuery:
	
							IntegerResult port = (IntegerResult) stack.pop();
							StringResult schema = (StringResult) stack.pop();
							StringResult host = (StringResult) stack.pop();
							StringResult lname = (StringResult) stack.pop();
	
							sres1 = (StringResult) stack.pop();
	
							if ( !sres1.value.endsWith(";" ))
									sres1.value = sres1.value + ";";
	
							// find link
						    LinkManager lm = LinkManager.getInstance();
						    DBLink link = lm.findLink(lname.value, host.value, schema.value, port.value, Session.getUserContext() );
	
						    if ( link == null)
						    	throw error("unalbe to find link",buffer);
	
						    // get Dependent parms
						    IntegerResult parmCount = (IntegerResult) stack.pop();
						    HashMap<StringResult, SingleResult > parmRes = new HashMap<StringResult,SingleResult>();
						    ArrayList<ValueSignatureInfo> parmSign = new ArrayList<ValueSignatureInfo>();
	
						    for ( int i = 0 ; i < parmCount.value ; i++)
						    {
						    	StringResult pName =  (StringResult) stack.pop();
	
						    	sinres1 = (SingleResult) stack.pop();
						    	parmRes.put(pName, sinres1);
	
						    	ires1 = (IntegerResult) stack.pop();
	
						    	ValueSignatureInfo sigInfo = new ValueSignatureInfo();
						    	sigInfo.setName(pName.value);
						    	sigInfo.setSigType(ValueSignatureType.getForInteger( ires1.value ));
						    	sigInfo.setBindingInfo(new BindingInfo(0));
	
						    	parmSign.add( sigInfo );
						    }
	
	
	
						    for ( StringResult pName : parmRes.keySet())
						    	stack.push(new BinderResult( pName.value , parmRes.get(pName) ));
	
						    lm.evaluateRemoteQuery(link, sres1.value , parmCount.value, stack, parmSign , Session.getUserContext(), opcode == OpCodes.remoteAsyncQuery, param);
						break;
						case waitForAsync:
							odra.db.links.AsynchronousRemoteQueriesManager.getCurrent().waitForAsynchronousRemoteQueries(param);
						break;
						case athrow:
						    refres = (ReferenceResult) stack.pop();
							clsid = refres.value.derefInstanceOfReference();
		
							if (clsid != null) {
								TryCatchBlock handler = this
										.findCurrentProcedureExceptionHandler(clsid,
											buffer.position()
													- JulietCode.INSTRUCTION_LENGTH);
							while (handler == null) {
								refres.value.move(Session.getModuleSessionEntry(this.currmod.peek()));
								this.cleanEnvironmentOnReturn();
								pd = this.envManager.getReturnData();
								buffer = pd.getReturnToCode();
								cp = pd.getPool();
								this.envManager.destroyProcedureEnvironment(true);
								if (this.envManager.isInProcedure()) {
									handler = this
											.findCurrentProcedureExceptionHandler(
													clsid,
													buffer.position()- JulietCode.INSTRUCTION_LENGTH);
								} else {
									// the exception handler not found
									// TODO maybe we should have default exception
									// handler
									String excName = refres.value.getObjectName();
									String className = clsid.getObjectName();
									refres.value.delete();
	
									throw error(
											"uncatched exception '" + className +"' has been thrown",
											buffer);
								}
							}
	
							this.prepareCatchException(handler, refres);
							buffer.position(handler.getOffset());
							break;
	
						    }
	
	
						    throw error("exception is not a class instance",buffer);
						case parallelUnion:
							int counter = ((IntegerResult) stack.pop()).value;
							bagres = new BagResult();
							for (int i = 0; i < counter; i++)
								bagres.addAll((Result) stack.pop());
	
							stack.push(bagres);
							break;
						case serializeOID:
							res1 = (Result)stack.pop();
							bagres = new BagResult();
							for(SingleResult sres : res1.elementsToArray()){
								if(sres instanceof ReferenceResult)
								{
									bagres.addElement((SBQLInterpreterHelper.serializeOID(((ReferenceResult)sres).value)));
								}else throw error("reference expected",buffer);
							}
							stack.push(bagres);
						    break;
						case deserializeOID:
							res1 = (Result)stack.pop();
							bagres = new BagResult();
							for(SingleResult sres : res1.elementsToArray()){							
									bagres.addElement((SBQLInterpreterHelper.deserializeOID(sres)));							
							}
							stack.push(bagres);
							break;
						case commitStmt:
							Session.removeTemporaryIndices();
							
							break;
						case beginLazyFailure:
							LazyFailureBlock lazyFailure = new LazyFailureBlock(param, stack.environmentSize(), stack.size());
							lazyFailureBlocks.add(lazyFailure);					
							break;
						case endLazyFailure:
							lazyFailureBlocks.pop();					
							break;
						default:
							assert false : "unknown opcode (" + opcode + ": " + opcode + ")";
					}
				} 
				catch (Exception e) {
					if (!lazyFailureBlocks.empty()) {
						LazyFailureBlock lazyFailure = lazyFailureBlocks.peek();
						while (stack.environmentSize() > lazyFailure.envsSize)
							stack.destroyEnvironment();
						while (stack.size() > lazyFailure.qresSize)
							stack.pop();
				
						buffer.position(lazyFailure.endPosition);
						stack.push(new LazyFailureResult(e));
						continue;
					}
					throw e;
				}
			}
		}
		catch(InterpreterException e){			
			
			throw e;
		}
		catch(CardinalityException e){			
			
			try {
				throw error(e,buffer);
			} catch (DatabaseException e1) {
				throw error(e1,buffer);
			}
		}
		catch (DatabaseException e){
	//		if(ConfigDebug.DEBUG_EXCEPTIONS)
	//			e.printStackTrace();
		
			throw error(e,buffer);
		}
		catch (ClassCastException e) {
	//		if(ConfigDebug.DEBUG_EXCEPTIONS){
	//			e.printStackTrace();
	//		
			// ZAMIENIĆ W JEDNĄ LINIĘ???
			
	//		else
			throw error(e, buffer);
		}
		catch (Exception e) {
			// automatic index updating while insertcopy into reference object need to work on NULL reference which causes NullPointerException
	//		if(ConfigDebug.DEBUG_EXCEPTIONS)
	//			e.printStackTrace();
			// TODO: Add corrupted flag to interpreter indicating whether runtime error occurred 
			throw error(e,buffer);
		}
		
	//	assert stack.size() == 1 : "improper stack state - size = " + stack.size();
		return;
	}

	private boolean isInLazyFailureRange() {
		// TODO Auto-generated method stub
		return false;
	}

	private final ByteBuffer derefStructSubroutine(ByteBuffer buffer, ConstantPool pool){
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getDerefStructSubroutine());

	}
	private final ByteBuffer derefVirtualSubroutine(ByteBuffer buffer, ConstantPool pool){
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getDerefVirtualSubroutine());

	}
	private final ByteBuffer derefBinderSubroutine(BinderResult binres, ByteBuffer buffer, ConstantPool pool) throws DatabaseException{
		stack.push(binres.value);
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getDerefBinderSubroutine(Database.getStore().addName(binres.getName())));
	}

	private final ByteBuffer updateVirtualSubroutine(ByteBuffer buffer, ConstantPool pool){
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getUpdateVirtualSubroutine());

	}
	private final ByteBuffer createVirtualSubroutine(ByteBuffer buffer, ConstantPool pool){
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getCreateVirtualSubroutine());

	}
	private final ByteBuffer deleteVirtualSubroutine(ByteBuffer buffer, ConstantPool pool){
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getDeleteVirtualSubroutine());

	}

	private final ByteBuffer navigateVirtualSubroutine(ByteBuffer buffer, ConstantPool pool){
		initSubroutine(buffer, pool);
		return ByteBuffer.wrap(subroutines.getNavigateVirtualSubroutine());


	}

	private final void initSubroutine(ByteBuffer buffer, ConstantPool cp){
	        this.envManager.createSubroutineEnvironment(new ReturnData(buffer, cp));

	}

	private final void prepareCatchException(TryCatchBlock exception, ReferenceResult exceptionResult) throws DatabaseException{
	    exceptionResult.value.move(Session.getModuleSessionEntry(this.currmod.peek()));
	    this.envManager.destroyLocalBlockEnvironmentsUpTo(exception.getLevel());
	    stack.push(exceptionResult);
	    this.envManager.createLocalEnvironment();
	    exceptionResult.value.move(this.envManager.getLocalStoreEntry());
	}

	private TryCatchBlock findCurrentProcedureExceptionHandler(OID exceptionClassid, int position) throws DatabaseException{
	    ExceptionTable excTable = envManager.getExceptionTable();
	    List<TryCatchBlock> handlers = excTable.getHandlersForOffset(position);
	    DBClass cls = new DBClass(exceptionClassid);
	    int nameid = exceptionClassid.getObjectNameId();
	    for(TryCatchBlock handler : handlers){
	    	if(handler.isCatchAnyBlock() || handler.getExceptionClassNameid() == nameid || cls.isSubClassOf(handler.getExceptionClassNameid())){
	    		return handler;
	    	}
	    }
	    return null;
	}

	private void cleanEnvironmentOnReturn()throws DatabaseException{
	    this.currmod.pop();
	    this.stack.resetBaseFrame();
	    this.envManager.initializeModuleEnvironment(currmod.peek(), true);
	    this.envManager.cleanBeforeReturn();
	}

	public void injectRemoteResults() throws DatabaseException
	{
		this.envManager.injectRemoteParms();
	}

	private InterpreterException error(String message, ByteBuffer buffer){
		String source = getInstructionForCodePosition(buffer);
		return new InterpreterException("(runtime error) " + message + source);
	}	
	private InterpreterException error(DatabaseException e, ByteBuffer buffer){
		String source = getInstructionForCodePosition(buffer);
		return new InterpreterException("(runtime error) Database error " + e.getMessage() + " "+ source, e);
	}
	
	private InterpreterException error(CardinalityException e, ByteBuffer buffer) throws DatabaseException{
		String source = getInstructionForCodePosition(buffer);
		return new InterpreterException("(runtime error) Cardinality error for '" + Database.getNameIndex().id2name(e.getObjectNameId()) + "[" + e.getMinCard() + ".." + e.getMaxCard() + "]' "+ e.getMessage() + " "+ source, e);
	}
	private InterpreterException error(ClassCastException e, ByteBuffer buffer){
		String source = getInstructionForCodePosition(buffer);
		return new InterpreterException("(runtime error) wrong type " + e.getMessage() + " "+ source, e);
	}
	private InterpreterException error(Exception e, ByteBuffer buffer){
		String source = getInstructionForCodePosition(buffer);
		return new InterpreterException("(runtime error) " + e.getMessage() + " "+ source, e);
	}
	
	
	
	public String getInstructionForCodePosition(ByteBuffer buffer) {
		String info = "";
		if(!ConfigServer.DEBUG_ENABLE)
			return info;
		int position = buffer.position() - JulietCode.INSTRUCTION_LENGTH;
		if(this.envManager.isInProcedure()){			
			SBQLInstructionTable table;
			String procName;
			try {
				table = this.envManager.getInstructionTable();
				procName = this.envManager.getCurrentProcedureName();
			} catch (DatabaseException e) {
				return "error getting source position info";
			}
			
			SourcePosition pos =  table.getSourcePositionForCodeOffset(position);
			info = " in procedure '" + procName + "' Line: " + pos.getLine() + " column: " +	pos.getColumn();
			if(ConfigServer.DEBUG_INCLUDE_SOURCE){
				String text = table.getSourceForPosition(pos);
				info += " (" + text + ")";
			}
			
		
		}
		if(ConfigServer.DEBUG_INCLUDE_BYTECODE) {
			buffer.position(position);
			OpCodes code = OpCodes.getOpCode(buffer.getInt());
			buffer.getInt(); //param
			info += " for opcode " + code.toString();
		}
		return info;
	}
	
	public enum ExecutionMode {
	    Data, Meta, Compilation;

	}
	
	public class LazyFailureBlock {
	
		int endPosition;
		int envsSize;
		int qresSize;
		
		public LazyFailureBlock(int endPosition, int envsSize, int qresSize) {
			this.endPosition = endPosition;
			this.envsSize = envsSize;
			this.qresSize = qresSize;
		}

	}
}
