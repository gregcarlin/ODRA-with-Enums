package odra.sbql.typechecker;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBSystemModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MBVirtualVariable;
import odra.db.schema.OdraProcedureSchema;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.CreateLocalExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.InsertExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.BreakStatement;
import odra.sbql.ast.statements.ContinueStatement;
import odra.sbql.ast.statements.DoWhileStatement;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.ForStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.SingleCatchBlock;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.optimizers.OptimizationFramework;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.stack.Binder;
import odra.system.config.ConfigDebug;

/**
 * Typechecker extension for procedure statements SBQLProcedureTypeChecker
 * refactorized from SBQLTypeChecker by radamus last modified: 2008-04-20
 * 
 * @version 1.0
 */
public class SBQLProcedureTypeChecker extends SBQLTypeChecker {
	private MBProcedure proc;
	private String procName;

	StaticLocalEnvironmentManager localEnvManager;
	private boolean isInLoop = false; // are we in loop
	OptimizationFramework opfrm;

	/**
	 * instantiates typechecker for compiling class methods
	 * 
	 * @param module
	 * @param mbclass
	 * @throws DatabaseException
	 */
	public SBQLProcedureTypeChecker(DBModule module, MBClass mbclass) throws TypeCheckerException {
		this(module);
		assert mbclass != null : "mbclass != null";
		try {
			this.errorInfoPrefix = mbclass.getName() + " class method '";
			// initialize classes environment
			staticEnvsManager.generateClassMethodCompilationEnvironment(mbclass);
		} catch (DatabaseException e) {
			throw new TypeCheckerException("error while procedure typechecker initialization " + e.getMessage(), e);
		}
	}

	/**
	 * instantiates typechecker for compiling view procedures
	 * 
	 * @param module
	 *            is an existing module. The type check will be performed within
	 *            this module's environment.
	 * @param mbview
	 *            - meta view
	 * @param seed
	 *            - vector of Signatures (view seed ) - implicit view generic
	 *            procedure parameter
	 * @throws DatabaseException
	 */
	public SBQLProcedureTypeChecker(DBModule module, MBView mbview, Vector<Signature> seed) throws DatabaseException {

		this(module);
		assert seed != null : "seed != null";
		if (mbview != null)
			this.errorInfoPrefix = mbview.getName() + " view procedure '";
		else
			this.errorInfoPrefix = "seed procedure ";
		for (Signature sseed : seed) {
			this.staticEnvsManager.createStaticNestedEnvironment(sseed);
		}

		// R.A. new view semantics for generic procedures!!
		// nested(viewDef)
		// stack.createEnvironment();
		// for(OID field :mbview.getVirtualFieldsEntry().derefComplex()){
		// stack.enter(new Binder(field.getObjectNameId(), new
		// ReferenceSignature(field)));
		// }
		// R.A. another experiment: access to subviews only through 'self'
		if (mbview != null) {
			ReferenceSignature selfsig = new ReferenceSignature(mbview.getOID());
			selfsig.setVirtual(true);
			this.staticEnvsManager.enterBinder(new Binder(SBQLTypeCheckerHelper.name2id("self"), selfsig));
		}

	}

	/**
	 * instantiates typechecker for compiling global (module) procedures
	 * 
	 * @param module
	 * @throws DatabaseException
	 */
	public SBQLProcedureTypeChecker(DBModule module) throws TypeCheckerException {
		super(module);
		opfrm = new OptimizationFramework(this);
		opfrm.setOptimizationSequence(OptimizationSequence.getForName(OptimizationSequence.NONE));
		this.errorInfoPrefix = "Procedure '";
	}

	public void setOptimization(OptimizationSequence optseq) {
		opfrm.setOptimizationSequence(optseq);
	}

	/**
	 * typecheck procedure
	 * 
	 * @param node
	 *            - procedure AST
	 * @param proc
	 *            - meta procedure
	 * @return typecheked AST
	 * @throws TypeCheckerException
	 */
	public Statement typecheckProcedure(Statement node, MBProcedure proc) throws TypeCheckerException {
		prepareProcedureEnvironment(proc);
		try {
			node.accept(this, null);
		} catch (TypeCheckerException e) {
			throw new TypeCheckerException(this.errorInfoPrefix + procName + "': " + e.getMessage(), e);
		}
		destroyProcedureEnvironment();
		return node;
	}

	/**
	 * prepare procedure for typecheck
	 * 
	 * @param proc
	 *            - meta procedure
	 * @return typechecked AST
	 * @throws TypeCheckerException
	 */
	public void prepareProcedureEnvironment(MBProcedure proc) throws TypeCheckerException {
		this.proc = proc;
		this.procName = "";
		try {

			procName = proc.getName();
			this.initializeProcedureEnvironment(proc);
		} catch (Exception e) {
			throw new TypeCheckerException(this.errorInfoPrefix + procName + "': " + e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitBlockStatement(odra.sbql.ast.statements
	 * .BlockStatement, java.lang.Object)
	 */
	@Override
	public Object visitBlockStatement(BlockStatement node, Object attr) throws SBQLException {
		this.initializeLocalBlockEnvironment(node);
		node.getStatement().accept(this, attr);
		this.destroyLocalBlockEnvironment();
		return null;
	}

	/**
	 * Typechecks an EmptyStatement.
	 * 
	 * @param stmt
	 *            is the statement to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		// do nothing
		return null;
	}

	/**
	 * Typechecks a SequentialStatement.
	 * 
	 * @param stmt
	 *            is the statement to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
		// visit the argument expressions
		stmt.getFirstStatement().accept(this, attr);
		stmt.getSecondStatement().accept(this, attr);

		return null;
	}

	/**
	 * Typechecks an ExpressionStatement.
	 * 
	 * @param stmt
	 *            is the statement to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {

		stmt.setExpression(typecheckExpression(stmt.getExpression(), attr, false, false));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitIfElseStatement(odra.sbql.ast.statements
	 * .IfElseStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {

		stmt.setExpression(this.typecheckExpression(stmt.getExpression(), attr, true, true));
		Signature sig = stmt.getExpression().getSignature();
		// check the expression
		requireValue(sig, new OID[] { env.booleanType }, "", stmt.getExpression());

		// check 'then' statement
		stmt.getIfStatement().accept(this, attr);
		// check 'else' statement
		stmt.getElseStatement().accept(this, attr);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeodra.sbql.ast.ASTAdapter#visitIfStatement(odra.sbql.ast.statements.
	 * IfStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
		stmt.setExpression(this.typecheckExpression(stmt.getExpression(), attr, true, true));

		Signature sig = stmt.getExpression().getSignature();
		// check the expression
		requireValue(sig, new OID[] { env.booleanType }, "", stmt.getExpression());

		// check 'then' statement
		stmt.getStatement().accept(this, attr);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitForEachStatement(odra.sbql.ast.statements
	 * .ForEachStatement, java.lang.Object)
	 */
	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
		boolean prev = this.isInLoop;

		stmt.setExpression(typecheckExpression(stmt.getExpression(), attr, false, false));
		Signature sig1 = stmt.getExpression().getSignature();

		this.staticEnvsManager.createStaticNestedEnvironment(sig1);
		// visit the foreach statement agains new environment
		try {

			this.isInLoop = true;
			stmt.getStatement().accept(this, attr);
		} catch (SBQLException ex) {
			this.recheckWithEllipse(stmt.getExpression(), stmt.getStatement(), attr, ex);
			stmt.fixUpExpression();
		} finally {
			// destroy the newly added enviroment
			// regardless of whether an error has occured or not
			this.staticEnvsManager.destroyEnvironment();
			this.isInLoop = prev;
		}
		return null;
	}

	@Override
	public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {

		// eval initialization expression
		stmt.setInitExpression(this.typecheckExpression(stmt.getInitExpression(), attr, false, false));
		// eval termination expression
		if ((stmt.getConditionalExpression() instanceof EmptyExpression)) {
			stmt.setConditionalExpression(new BooleanExpression(new BooleanLiteral(true)));
		}
		stmt.setConditionalExpression(this.typecheckExpression(stmt.getConditionalExpression(), attr, true, true));
		// eval increment expression
		stmt.setIncrementExpression(this.typecheckExpression(stmt.getIncrementExpression(), attr, false, false));

		// eval statement inside loop
		typecheckLoopStatement(stmt.getStatement(), attr);

		// terminate expression should be a boolean
		requireValue(stmt.getConditionalExpression().getSignature(), new OID[] { env.booleanType }, "", stmt.getConditionalExpression());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitBreakStatement(odra.sbql.ast.statements
	 * .BreakStatement, java.lang.Object)
	 */
	@Override
	public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
		if (!this.isInLoop) {
			throw new TypeCheckerException("'break' cannot be used outside of a loop");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitContinueStatement(odra.sbql.ast.statements
	 * .ContinueStatement, java.lang.Object)
	 */
	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
		if (!this.isInLoop) {
			throw new TypeCheckerException("'continue' cannot be used outside of a loop");
		}
		return null;
	}

	/**
	 * Typechecks a ReturnWithoutValueStatement.
	 * 
	 * @param stmt
	 *            is the statement to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS)
			assert proc != null : "return statement can appear only in the context of a procedure";
		try {
			if (!proc.getType().equals(env.voidType)) {
				throw exception("return value required", stmt);
			}
		} catch (DatabaseException e) {
			throw exception(e, stmt);
		}
		return null;
	}

	/**
	 * Typechecks a ReturnWithValueStatement.
	 * 
	 * @param stmt
	 *            is the statement to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS)
			assert proc != null : "return statement can appear only in the context of a procedure";
		boolean wasModified = false;
		// check if the procedure really returns a value
		try {
			if (proc.getType().equals(env.voidType)) {
				throw exception("cannot return a value", stmt);
			}

			stmt.setExpression(typecheckExpression(stmt.getExpression(), attr, proc.getRefIndicator() == 0 ? true : false, false));
			Signature resSig = stmt.getExpression().getSignature();
			Signature procResSig = SBQLTypeCheckerHelper.inferSignature(proc.getType(), proc.getMinCard(), proc.getMaxCard());

			try {
				// we re-clip (possibly) modified expression
				wasModified = this.checkSigTypeCompatibility(procResSig, resSig, stmt.getExpression(), attr,true);
				stmt.fixUpExpression();
				if (wasModified) {
					stmt.getExpression().accept(this, attr);
					stmt.fixUpExpression();
				}
			} catch (TypeCheckerException e) {
				throw exception("return type incompatibility: " + e.getMessage(), stmt);
			}
		} catch (DatabaseException e) {
			throw exception(e, stmt);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitVariableDeclarationStatement(odra.sbql.
	 * ast.statements.VariableDeclarationStatement, java.lang.Object)
	 */
	@Override
	public Object visitVariableDeclarationStatement(VariableDeclarationStatement node, Object attr) throws SBQLException {
		MBVariable locVar = localEnvManager.getLocalVariable(node.getVariableName());
		try {
			this.staticEnvsManager.enterBinder(new Binder(locVar.getNameId(), new ReferenceSignature(locVar.getOID(), true)));
		} catch (DatabaseException e) {
			throw exception(e, node);
		}
		if (!(node.getInitExpression() instanceof EmptyExpression)) {
			if (node.getMinCard() != 1 || node.getMinCard() != 1)
				throw new TypeCheckerException(": local variable '" + node.getVariableName() + "' requires cardinality [1..1] to be initialized");
			NameExpression nexpr = new NameExpression(new Name(node.getVariableName()));
			nexpr.line = node.line;
			nexpr.column = node.column;
			Expression init = new AssignExpression(nexpr, node.getInitExpression(), Operator.opAssign);
			init.line = node.line;
			init.column = node.column;
			init.accept(this, attr);
			node.setInitExpression(init);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.typechecker.SBQLTypeChecker#visitCreateLocalExpression(odra
	 * .sbql.ast.expressions.CreateLocalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
		// visit parameter expression
		evalCreateParamExpression(expr, attr);
		Signature sig = expr.getExpression().getSignature();
		ReferenceSignature rsig = typecheckCreate(expr, this.requireVariableDeclaration(expr), attr);

		rsig.setMinCard(sig.getMinCard());
		rsig.setMaxCard(sig.getMaxCard());

		expr.setSignature(rsig);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.typechecker.SBQLTypeChecker#visitCreateExpression(odra.sbql
	 * .ast.expressions.CreateExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
		// visit parameter expression
		evalCreateParamExpression(expr, attr);

		Signature sig = expr.getExpression().getSignature();

		ReferenceSignature rsig = typecheckCreate(expr, this.requireVariableDeclaration(expr), attr);

		rsig.setMinCard(sig.getMinCard());
		rsig.setMaxCard(sig.getMaxCard());
		expr.setSignature(rsig);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {

		typecheckExpression(stmt.getExpression(), attr, true, true);
		requireValue(stmt.getExpression().getSignature(), new OID[] { env.booleanType }, "", stmt.getExpression());
		typecheckLoopStatement(stmt.getStatement(), attr);
		return null;
	}

	@Override
	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {

		typecheckLoopStatement(stmt.getStatement(), attr);

		typecheckExpression(stmt.getExpression(), attr, true, true);
		requireValue(stmt.getExpression().getSignature(), new OID[] { env.booleanType }, "", stmt.getExpression());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.typechecker.SBQLTypeChecker#visitAssignExpression(odra.sbql
	 * .ast.expressions.AssignExpression, java.lang.Object)
	 */
	@Override
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		Object o = super.visitAssignExpression(expr, attr);
		try {
			MBVariable var = new MBVariable(((ReferenceSignature) expr.getSignature()).value);
			if (var.isValid() && var.isTypeReference()) {
				this.localEnvManager.setInitialized(var.getOID());
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.typechecker.SBQLTypeChecker#visitDerefExpression(odra.sbql.
	 * ast.expressions.DerefExpression, java.lang.Object)
	 */
	@Override
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		Object o = super.visitDerefExpression(expr, attr);
		if (expr.getExpression().getSignature() instanceof ReferenceSignature) {
			try {
				MBVariable var = new MBVariable(((ReferenceSignature) expr.getExpression().getSignature()).value);
				if (var.isValid() && var.isTypeReference()) {
					if (!this.localEnvManager.isInitialized(var.getOID())) {
						throw exception("the local variable '" + var.getName() + "' might not have been initialized", expr);
					}
				}
			} catch (DatabaseException e) {
				throw exception(e, expr);
			}
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.
	 * statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	@Override
	public Object visitTryCatchFinallyStatement(TryCatchFinallyStatement stmt, Object attr) throws SBQLException {
		// type-check try statement
		stmt.getTryStatement().accept(this, attr);
		Vector<MBClass> excClasses = new Vector<MBClass>();
		// foreach catch block
		for (SingleCatchBlock cb : stmt.getCatchBlocks().flattenCatchBlocks()) {
			// typecheck catch block statement
			cb.getStatement().accept(this, attr);
			// check exception variable
			MBClass excCls;
			try {
				OID exceptionId = proc.getCatchBlockExceptionVariable(cb.getStatement().getBlockName());
				MBVariable excVar = new MBVariable(exceptionId);
				excCls = new MBClass(excVar.getType());
				// the exception variable must be an instance of
				// system.Exception
				// class
				if (!excCls.isValid()) {
					throw exception("exception for a catch block must be a class instance", cb.getStatement());
				}

				if (!this.isExceptionClass(excCls))
					throw exception("exception for a catch block must be an instance of system Exception class", cb.getStatement());

				// check the order of catch blocks
				for (MBClass previous : excClasses) {
					if (excCls.isSubClassOf(previous.getOID())) {
						throw exception("exception of type '" + excCls.getName() + "' is already catched", cb.getStatement());
					}
				}
			} catch (DatabaseException e) {
				throw exception(e, cb.getStatement());
			}
			excClasses.add(excCls);
		}

		// typecheck finally statement
		stmt.getFinallyStatement().accept(this, attr);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitThrowStatement(odra.sbql.ast.statements
	 * .ThrowStatement, java.lang.Object)
	 */
	@Override
	public Object visitThrowStatement(ThrowStatement stmt, Object attr) throws SBQLException {
		stmt.getExpression().accept(this, attr);
		Signature sig = stmt.getExpression().getSignature();
		// throw expression signature must be reference ...
		ReferenceSignature rsig = requireReference(sig, stmt.getExpression());
		// ... that value is a variable...
		MBVariable var = requireVariable(rsig.value, stmt.getExpression());

		try {
			// ... that represent the instance of system.Exception class
			MBClass excCls = new MBClass(var.getType());
			if ((var.isValid() && this.isExceptionClass(excCls)))
				return null;
			else
				throw exception("exception has to be instance of ExceptionClass", stmt);
		} catch (DatabaseException e) {
			throw exception(e, stmt);
		}

	}

	/**
	 * initialize environment for compiling procedures
	 * 
	 * @param mbproc
	 * @throws DatabaseException
	 */
	private void initializeProcedureEnvironment(MBProcedure mbproc) throws DatabaseException {
		localEnvManager = new StaticLocalEnvironmentManager();
		localEnvManager.initProcedureEnvironment();

		// create local environment section
		this.staticEnvsManager.createEnvironment();
		// add binders to procedure arguments
		for (OID argid : mbproc.getArguments()) {
			MBVariable arg = new MBVariable(argid);
			assert arg.isValid() : "procedure argument not a meta-variable";
			// 01.04.07 radamus
			// previous: stack.enter(new Binder(arg.getNameId(), new
			// ReferenceSignature(argid, true)));
			// changes to argument semantics
			// until now it was a variable, currently it is a binder
			// TODO in the metabase it is still MBVariable
			Signature argsig = SBQLTypeCheckerHelper.inferSignature(arg.getType(), arg.getMinCard(), arg.getMaxCard());
			this.staticEnvsManager.enterBinder(new Binder(arg.getNameId(), argsig));

		}
		// add binders to local variables declared in main block if (exists)
		this.addCodeBlockLocalVariables();

	}

	public void destroyProcedureEnvironment() {

		this.staticEnvsManager.destroyEnvironment();
		localEnvManager.destroyProcedureEnvironment();

	}

	/**
	 * push local variables declared in the block (as binders) onto top EVNS
	 * section
	 * 
	 * @return true if block has local variables declared
	 * @throws DatabaseException
	 */
	private boolean addCodeBlockLocalVariables() throws SBQLException {

		String blockName = localEnvManager.getCurrentBlockName();
		try {
			OID blockid = proc.getLocalBlockEntry(blockName);
			// it might be null because we do not store blocks in which
			// there is no local variable declared
			if (blockid != null) {
				localEnvManager.addCodeBlockLocalVariables(blockid.derefComplex());
				return true;
			}
		} catch (DatabaseException e) {
			throw new TypeCheckerException(e);
		}
		return false;
	}

	public void initializeLocalBlockEnvironment(BlockStatement node) {
		// each statements block has local section (it might be empty)
		node.setBlockName(localEnvManager.initializeLocalBlockEnvironment());
		// trace local block names
		this.staticEnvsManager.createEnvironment();
		node.sethasLocalData(this.addCodeBlockLocalVariables());
		this.addCatchBlockExceptionVariable();
	}

	public void destroyLocalBlockEnvironment() {
		this.staticEnvsManager.destroyEnvironment();
		localEnvManager.destroyLocalBlockEnvironment();
	}

	/**
	 * push local variables declared in the block (as binders) onto top EVNS
	 * section
	 * 
	 * @return true if block has local variables declared
	 * @throws DatabaseException
	 */
	private boolean addCatchBlockExceptionVariable() throws SBQLException {
		String blockName = localEnvManager.getCurrentBlockName();
		try {
			if (proc.isCatchBlock(blockName)) {
				OID excVar = proc.getCatchBlockExceptionVariable(blockName);
				MBVariable excVariable = new MBVariable(excVar);
				assert excVariable.isValid() : "exception must be a variable";
				this.staticEnvsManager.enterBinder(new Binder(excVariable.getNameId(), new ReferenceSignature(excVar)));
				return true;

			}
		} catch (DatabaseException e) {
			throw new TypeCheckerException(e);
		}
		return false;

	}

	/**
	 * Search for a variable declaration
	 * 
	 * @param name
	 *            - variable name
	 * @param expr
	 * @return
	 * @throws DatabaseException
	 */
	protected ReferenceSignature requireVariableDeclaration(CreateExpression expr) throws SBQLException {
		String name = expr.name().value();
		OID declared;
		// search local variable declaration
		try {
			String[] blockName = new String[1];
			declared = localEnvManager.findLocalMetaVariable(name, blockName);

			if (declared != null) {
				expr.declaration_environment = CreateExpression.LOCAL;
				if (expr instanceof CreateLocalExpression)
					((CreateLocalExpression) expr).setBlockName(blockName[0]);
				return new ReferenceSignature(declared, true);
			}
			return super.requireVariableDeclaration(expr);
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

	}

	private final OID getSystemExceptionMetaClass() throws DatabaseException {
		DBSystemModule sysmod = Database.getSystemModule();
		return sysmod.findFirstByName("Exception", sysmod.getMetabaseEntry());

	}

	private final boolean isExceptionClass(MBClass excCls) throws DatabaseException {
		if (!excCls.getOID().equals(this.getSystemExceptionMetaClass()) && !excCls.isSubClassOf(this.getSystemExceptionMetaClass())) {
			return false;
		}
		return true;
	}

	private Expression typecheckExpression(Expression e, Object attr, boolean enforceDereference, boolean enforceSingle) {

		Expression result;
		ASTNode oldRoot = root;
		root = e;
		// visit the argument expression
		root.accept(this, attr);

		if (enforceDereference)
			this.enforceDereference((Expression) root, attr);
		if (enforceSingle)
			this.enforceSingleElement((Expression) root, attr);
		result = (Expression) root;
		// below is temporary due to an error
		if (!(e instanceof InsertCopyExpression || e instanceof InsertExpression))
			result = (Expression) this.opfrm.optimize(result, module);
		root = oldRoot;
		return result;
	}

	private Statement typecheckLoopStatement(Statement stmt, Object attr) {
		boolean prev = this.isInLoop;
		this.isInLoop = true;
		stmt.accept(this, attr);
		this.isInLoop = prev;
		return stmt;

	}

	private class StaticLocalEnvironmentManager {

		private Stack<Map<String, MBVariable>> locals = new Stack<Map<String, MBVariable>>();
		private Stack<List<String>> blocksInfo = new Stack<List<String>>();

		private Stack<Integer> blocksNumbers = new Stack<Integer>();
		private Stack<List<OID>> uninitilizedLocalVariables = new Stack<List<OID>>();

		private void initProcedureEnvironment() throws DatabaseException {
			// local blocks names tracer initialization
			this.blocksInfo.push(new Vector<String>());
			this.blocksInfo.peek().add(OdraProcedureSchema.MAIN_LOCAL_BLOCK_NAME);
			this.blocksNumbers.push(0);

			this.uninitilizedLocalVariables.push(new Vector<OID>());
			this.locals.push(new Hashtable<String, MBVariable>());

		}

		public String initializeLocalBlockEnvironment() {
			// trace local block names
			int num = this.blocksNumbers.pop();
			this.blocksNumbers.push(num + 1);
			String blockName = this.blocksInfo.peek().get(0) + "." + (num + 1);

			this.blocksInfo.push(new Vector<String>());
			this.blocksInfo.peek().add(blockName);
			this.blocksNumbers.push(0);

			this.uninitilizedLocalVariables.push(new Vector<OID>());
			this.locals.push(new Hashtable<String, MBVariable>());

			return blockName;
		}

		/**
		 * push local variables declared in the block (as binders) onto top EVNS
		 * section
		 * 
		 * @return true if block has local variables declared
		 * @throws DatabaseException
		 */
		private void addCodeBlockLocalVariables(OID[] variables) throws SBQLException {

			try {

				// it might be null because we do not store blocks in which
				// there is no local variable declared
				List<OID> uninitialized = this.uninitilizedLocalVariables.peek();
				Map<String, MBVariable> localVars = this.locals.peek();
				for (OID locvarid : variables) {
					MBVariable locvar = new MBVariable(locvarid);
					assert locvar.isValid() : "procedure local variable not a meta-variable";
					localVars.put(locvar.getName(), locvar);
					if (locvar.isTypeReference() && locvar.getMinCard() == 1 && locvar.getMaxCard() == 1) {
						uninitialized.add(locvarid);
					}
				}
			} catch (DatabaseException e) {
				throw new TypeCheckerException(e);
			}
		}

		public void destroyLocalBlockEnvironment() {
			this.blocksInfo.pop();
			this.blocksNumbers.pop();
			this.uninitilizedLocalVariables.pop();
			this.locals.pop();
		}

		public void destroyProcedureEnvironment() {
			this.blocksInfo.clear();
			this.blocksNumbers.clear();
			this.uninitilizedLocalVariables.clear();
			this.locals.clear();
		}

		public String getCurrentBlockName() {
			return this.blocksInfo.peek().get(0);
		}

		public MBVariable getLocalVariable(String varName) {
			MBVariable locVar = this.locals.peek().get(varName);
			assert locVar != null : "variable expected";
			return locVar;
		}

		private OID findLocalMetaVariable(String name, String[] blockName) throws DatabaseException {
			// search for the variable name starting from the most
			// nested block
			for (int i = this.locals.size() - 1; i >= 0; i--) {
				MBVariable var = this.locals.get(i).get(name);
				if (var != null) {
					blockName[0] = this.blocksInfo.get(i).get(0);
					return var.getOID();
				}
			}
			return null;
		}

		private void setInitialized(OID varid) {
			for (Object o : this.uninitilizedLocalVariables.toArray()) {
				Vector<OID> blockVars = (Vector<OID>) o;
				blockVars.removeElement(varid);
			}
		}

		private boolean isInitialized(OID varid) {
			for (Object o : this.uninitilizedLocalVariables.toArray()) {
				Vector<OID> blockVars = (Vector<OID>) o;
				if (blockVars.contains(varid))
					return false;
			}
			return true;
		}
	}

}
