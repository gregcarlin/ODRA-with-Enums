package odra.sbql.emiter;


import java.util.HashMap;
import java.util.Map;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBVariable;
import odra.db.schema.OdraProcedureSchema;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AtLeastExpression;
import odra.sbql.ast.expressions.AtMostExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CloseByExpression;
import odra.sbql.ast.expressions.CloseUniqueByExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.ExternalProcedureCallExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InstanceOfExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.LazyFailureExpression;
import odra.sbql.ast.expressions.LeavesByExpression;
import odra.sbql.ast.expressions.LeavesUniqueByExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.RangeAsExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.SerializeOidExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.WhereExpression;
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
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.debugger.compiletime.DebugNodeData;
import odra.sbql.emiter.exceptions.CompiletimeExceptionTable;
import odra.system.config.ConfigServer;

/**
 * JulietCodeGenerator base class for 'safe' and 'dynamic' codegenerators implements codegeneration for
 * expressions/statements that execution is common for both environments
 * 
 * @author Radek Adamus
 * @since 2007-05-07 last modified: 2007-05-07
 * @version 1.0
 */
public abstract class JulietCodeGenerator extends TraversingASTAdapter implements IJulietCodeGenerator {
	private JulietCode code = new JulietCode();
	
	// used to generate procedure block initialization code
	protected JulietCode init;

	// generated constant pool
	protected ConstantPool pool;

	protected DBModule mod;
	
	// proc != null if we generate code for procedure
	protected MBProcedure proc;
	//TODO refactorization
	
	// flag indicating if we generate code that is inside loop expression
	// used for break; and continue; statements.
	protected boolean isInLoop = false;
	protected int loopBlockLevel; //value indicating the current loop base nesting level (valid if isInLoop == true) 
	// flag indicating if we should generate debug information
	protected boolean debug = false;

	// value indicating the current code block nesting level (for proper
	// environment cleaning during return)
	protected int blockLevel = 0;
	String currentBlockName;
	// value indicating what id should be assigned to asynchronous remote query calls
	// if it is below 0 then query calls are not asynchronous
	protected int asynchronous_id = -1;
	
	CompiletimeExceptionTable catchTable = new CompiletimeExceptionTable();
	Map<String, VariableDeclarationStatement> localVariableDeclaration = new HashMap<String, VariableDeclarationStatement>();	

	/**
	 * creates new JulietCodeGenerator object
	 */
	JulietCodeGenerator(DBModule mod) {
		this.mod = mod;
		try {
		    this.setSourceModuleName(mod.getName());
		} catch (DatabaseException e) {
		    throw new EmiterException(e);
		}
		pool = new ConstantPool();

	}

	/**
	 * creates new JulietCodeGenerator object
	 */
	JulietCodeGenerator(DBModule mod, MBProcedure proc) {
		this(mod);		
		this.proc = proc;		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.emiter.IJulietCodeGenerator#generate()
	 */
	public void generate(ASTNode node) throws SBQLException {
		this.init = new JulietCode();
		try {
			node.accept(this, null);
			if (node instanceof Expression) {
				this.code = ((Expression) node).getJulietCode();
			} else {
				this.code = ((Statement) node).getJulietCode();
			}
			if (this.proc != null) {
			    	currentBlockName = OdraProcedureSchema.MAIN_LOCAL_BLOCK_NAME;
				OID mainblock = this.proc.getLocalBlockEntry(currentBlockName);
				if (mainblock != null) {
					OID[] localvars = mainblock.derefComplex();
					if (localvars.length > 0) {

						for (OID var : mainblock.derefComplex()) {
							MBVariable mbvar = new MBVariable(var);
							if (mbvar.isValid()) {
								this.init.append(this.initializeLocalVariable(mbvar));
							}
						}
					}
				}				
				this.init.append(JulietGen.genInitLocalEnvironment());
				JulietCode procedureHeader = this.generateProcedureBodyHeader();
				procedureHeader.append(this.init);
				this.code = procedureHeader.append(this.code);
				
				
			}
		} catch (DatabaseException e) {
			if (ConfigServer.DEBUG_EXCEPTIONS) {
				e.printStackTrace();
			}
			throw new EmiterException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.emiter.IJulietCodeGenerator#generateDebug(odra.sbql.ast.ASTNode)
	 */
	public void generateWithDebug(ASTNode node) throws SBQLException {
		this.debug = true;
		this.generate(node);
	}

	public ConstantPool getConstantPool() {
		return pool;
	}

	public JulietCode getCode() {
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitBooleanExpression(odra.sbql.ast.expressions.BooleanExpression,
	 *      java.lang.Object)
	 */
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException {
		super.visitBooleanExpression(expr, attr);
		return expr.setJulietCode(JulietGen.genBooleanExpression(expr.getLiteral().value()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitCommaExpression(odra.sbql.ast.expressions.CommaExpression, java.lang.Object)
	 */
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
		super.visitCommaExpression(expr, attr);
		expr.setJulietCode(JulietGen.genCartesianProductExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr); 
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitWhereExpression(odra.sbql.ast.expressions.WhereExpression, java.lang.Object)
	 */
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
		super.visitWhereExpression(expr, attr);
		expr.setJulietCode(JulietGen.genWhereExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitDotExpression(odra.sbql.ast.expressions.DotExpression, java.lang.Object)
	 */
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
		super.visitDotExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDotExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitGroupAsExpression(odra.sbql.ast.expressions.GroupAsExpression,
	 *      java.lang.Object)
	 */
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
		super.visitGroupAsExpression(expr, attr);
		expr.setJulietCode(JulietGen.genGroupAsExpression(expr.getExpression().getJulietCode(), this.name2Id(
					expr.name().value())));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitIntegerExpression(odra.sbql.ast.expressions.IntegerExpression,
	 *      java.lang.Object)
	 */
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException {
		super.visitIntegerExpression(expr, attr);
		expr.setJulietCode(JulietGen.genIntegerExpression(expr.getLiteral().value()));
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException {
		super.visitDateExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDateExpression(pool.addDate(expr.getLiteral().value())));
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
		super.visitDateprecissionExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDateformatExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitJoinExpression(odra.sbql.ast.expressions.JoinExpression, java.lang.Object)
	 */
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
		super.visitJoinExpression(expr, attr);
		expr.setJulietCode(JulietGen.genJoinExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitForAllExpression(odra.sbql.ast.expressions.ForAllExpression, java.lang.Object)
	 */
	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		super.visitForAllExpression(expr, attr);
		expr.setJulietCode(JulietGen.genForallExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitForSomeExpression(odra.sbql.ast.expressions.ForSomeExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		super.visitForSomeExpression(expr, attr);
		expr.setJulietCode(JulietGen.genForsomeExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitOrderByExpression(odra.sbql.ast.expressions.OrderByExpression,
	 *      java.lang.Object)
	 */
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
		super.visitOrderByExpression(expr, attr);
		expr.setJulietCode(JulietGen.genOrderByExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
		super.visitCloseByExpression(expr, attr);
		expr.setJulietCode(JulietGen.genCloseByExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression expr, Object attr) throws SBQLException {
		super.visitCloseUniqueByExpression(expr, attr);
		expr.setJulietCode(JulietGen.genCloseUniqueByExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitLeavesByExpression(LeavesByExpression expr, Object attr) throws SBQLException {
		super.visitLeavesByExpression(expr, attr);
		expr.setJulietCode(JulietGen.genLeavesByExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression expr, Object attr) throws SBQLException {
		super.visitLeavesUniqueByExpression(expr, attr);
		expr.setJulietCode(JulietGen.genLeavesUniqueByExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitRealExpression(odra.sbql.ast.expressions.RealExpression, java.lang.Object)
	 */
	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException {
		super.visitRealExpression(expr, attr);
		expr.setJulietCode(JulietGen.genRealExpression(pool.addDouble(expr.getLiteral().value())));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitStringExpression(odra.sbql.ast.expressions.StringExpression, java.lang.Object)
	 */
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException {
		super.visitStringExpression(expr, attr);
		expr.setJulietCode(JulietGen.genStringExpression(pool.addString(expr.getLiteral().value())));
		return postProcessExpressionCodeGeneration(expr);
	}

	// *********************************
	// created by raist, 30.07.06
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
		super.visitSequentialStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genCodeSequence(stmt.getFirstStatement().getJulietCode(), stmt.getSecondStatement().getJulietCode()));
		return postProcessStatementCodeGeneration(stmt);
	}


	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {
		super.visitExpressionStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genExpressionStatement(stmt.getExpression().getJulietCode()));
		return postProcessStatementCodeGeneration(stmt);
	}

	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		super.visitReturnWithoutValueStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genReturnWithoutValueStatement());
		return postProcessStatementCodeGeneration(stmt);
	}

	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
		super.visitReturnWithValueStatement(stmt, attr);
		JulietCode exprCode = stmt.getExpression().getJulietCode();
		JulietCode resCode = JulietGen.genReturnWithValueStatement(exprCode);
		stmt.setJulietCode(resCode);
		if (stmt.hasTransactionCapableParentASTNode()) {
			JulietGen.genCommitTrans(stmt.getJulietCode());
		}
		stmt.getJulietCode();
		return postProcessStatementCodeGeneration(stmt);
	}

	private void appendCommitTrans(JulietCode code) {

	}

	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		super.visitEmptyStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genEmptyStatement());
		return null;
	}

	// **********************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitForEachStatement(odra.sbql.ast.statements.ForEachStatement, java.lang.Object)
	 */
	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
		boolean prev = this.isInLoop;
		int prevLoopLevel = this.loopBlockLevel; 
		stmt.getExpression().accept(this, attr);
		JulietCode exprJt = stmt.getExpression().getJulietCode();
		this.isInLoop = true;
		this.loopBlockLevel = this.blockLevel;
		stmt.getStatement().accept(this, attr);				
		this.isInLoop = prev;
		this.loopBlockLevel = prevLoopLevel;
		
		JulietCode stmtJt = stmt.getStatement().getJulietCode();
		stmt.setJulietCode(JulietGen.genForeachExpression(exprJt, stmtJt));
		super.commonVisitStatement(stmt, attr);
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitDoWhileStatement(odra.sbql.ast.statements.DoWhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {
		boolean prev = this.isInLoop;
		int prevLoopLevel = this.loopBlockLevel; 

		this.isInLoop = true;
		this.loopBlockLevel = this.blockLevel;
		stmt.getStatement().accept(this, attr);
		JulietCode stmtJt = stmt.getStatement().getJulietCode();
		isInLoop = prev;
		this.loopBlockLevel = prevLoopLevel;
		stmt.getExpression().accept(this, attr);
		JulietCode exprJt = stmt.getExpression().getJulietCode();
		stmt.setJulietCode(JulietGen.genDoWhileLoop(exprJt, stmtJt));
		super.commonVisitStatement(stmt, attr);
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitWhileStatement(odra.sbql.ast.statements.WhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {
		boolean prev = this.isInLoop;
		int prevLoopLevel = this.loopBlockLevel; 
		stmt.getExpression().accept(this, attr);
		JulietCode exprJt = stmt.getExpression().getJulietCode();
		isInLoop = true;
		this.loopBlockLevel = this.blockLevel;
		stmt.getStatement().accept(this, attr);
		JulietCode stmtJt = stmt.getStatement().getJulietCode();
		this.isInLoop = prev;
		this.loopBlockLevel = prevLoopLevel;
		stmt.setJulietCode(JulietGen.genWhileLoop(exprJt, stmtJt));
		super.commonVisitStatement(stmt, attr);
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitForStatement(odra.sbql.ast.statements.ForStatement, java.lang.Object)
	 */
	@Override
	public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {
		boolean prev = this.isInLoop;
		int prevLoopLevel = this.loopBlockLevel;
		stmt.getInitExpression().accept(this, attr);
		JulietCode initJt = stmt.getInitExpression().getJulietCode();
		stmt.getConditionalExpression().accept(this, attr);
		JulietCode condJt = stmt.getConditionalExpression().getJulietCode();
		stmt.getIncrementExpression().accept(this, attr);
		JulietCode incrJt = stmt.getIncrementExpression().getJulietCode();

		isInLoop = true;
		this.loopBlockLevel = this.blockLevel;
		stmt.getStatement().accept(this, attr);
		JulietCode stmtJt = stmt.getStatement().getJulietCode();
		this.isInLoop = prev;
		this.loopBlockLevel = prevLoopLevel;
		stmt.setJulietCode(JulietGen.genForLoop(initJt, condJt, incrJt, stmtJt));
		super.commonVisitStatement(stmt, attr);
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
		super.visitIfStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genIfThenStatement(stmt.getExpression().getJulietCode(), stmt.getStatement().getJulietCode()));
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {
		super.visitIfElseStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genIfThenElseStatement(stmt.getExpression().getJulietCode(), stmt.getIfStatement().getJulietCode(),
					stmt.getElseStatement().getJulietCode()));
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCountExpression(odra.sbql.ast.expressions.CountExpression, java.lang.Object)
	 */
	@Override
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
		super.visitCountExpression(expr, attr);
		expr.setJulietCode(JulietGen.genCountExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
		super.visitDeleteExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDeleteExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitIfThenElseExpression(odra.sbql.ast.expressions.IfThenElseExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
		super.visitIfThenElseExpression(expr, attr);
		expr.setJulietCode(JulietGen.genConditionalExpression(expr.getConditionExpression().getJulietCode(), expr.getThenExpression().getJulietCode(),
					expr.getElseExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitIfThenExpression(odra.sbql.ast.expressions.IfThenExpression, java.lang.Object)
	 */
	@Override
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
		super.visitIfThenExpression(expr, attr);
		expr.setJulietCode(JulietGen.genConditionalExpression(expr.getConditionExpression().getJulietCode(), expr.getThenExpression().getJulietCode(), JulietGen
					.genEmptyBag()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitExistsExpression(odra.sbql.ast.expressions.ExistsExpression, java.lang.Object)
	 */
	@Override
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
		super.visitExistsExpression(expr, attr);
		expr.setJulietCode(JulietGen.genExistExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitUnionExpression(odra.sbql.ast.expressions.UnionExpression, java.lang.Object)
	 */
	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
		super.visitUnionExpression(expr, attr);
		expr.setJulietCode(JulietGen.genUnionExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitEmptyExpression(odra.sbql.ast.expressions.EmptyExpression, java.lang.Object)
	 */
	@Override
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
		super.visitEmptyExpression(expr, attr);
		expr.setJulietCode(JulietGen.genEmptyExpression());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitIntersectExpression(odra.sbql.ast.expressions.IntersectExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
		super.visitIntersectExpression(expr, attr);
		expr.setJulietCode(JulietGen.genIntersectExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitMinusExpression(odra.sbql.ast.expressions.MinusExpression, java.lang.Object)
	 */
	@Override
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
		super.visitMinusExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDifferenceExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitSequentialExpression(odra.sbql.ast.expressions.SequentialExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
		super.visitSequentialExpression(expr, attr);
		expr.setJulietCode(JulietGen.genCodeSequence(expr.getFirstExpression().getJulietCode(), expr.getSecondExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToBagExpression(odra.sbql.ast.expressions.ToBagExpression, java.lang.Object)
	 */
	@Override
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
		super.visitToBagExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode());
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToSingleExpression(odra.sbql.ast.expressions.ToSingleExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		super.visitToSingleExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.gen2Single()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitBagExpression(odra.sbql.ast.expressions.BagExpression, java.lang.Object)
	 */
	@Override
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
		super.visitBagExpression(expr, attr);
		expr.setJulietCode(JulietGen.genBagExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitStructExpression(odra.sbql.ast.expressions.StructExpression, java.lang.Object)
	 */
	@Override
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
		super.visitStructExpression(expr, attr);
		expr.setJulietCode(JulietGen.genStructExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		int paramNum = expr.getArgumentsExpression().flatten().length;
		super.visitProcedureCallExpression(expr, attr);
		expr.setJulietCode(JulietGen.genProcCallExpression(expr.getProcedureSelectorExpression().getJulietCode(), expr.getArgumentsExpression().getJulietCode(), paramNum));
		return postProcessExpressionCodeGeneration(expr);
	}

	// TW
	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr, Object attr)
				throws SBQLException {
		StringExpression stexpr = new StringExpression(new StringLiteral(((ExternalNameExpression) expr.getLeftExpression()).name().value()));
		stexpr.accept(this, null);
		super.visitExternalProcedureCallExpression(expr, attr);
		expr.setJulietCode(JulietGen.genExtProcCallExpression(stexpr.getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
		super.visitExecSqlExpression(expr, attr);
		expr.query.accept(this, attr);
		expr.pattern.accept(this, attr);
		expr.module.accept(this, attr);
		expr.setJulietCode(JulietGen.genExecSqlExpression(expr.query.getJulietCode(), expr.pattern.getJulietCode(),
					expr.module.getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException {
		super.visitRandomExpression(expr, attr);
		if (expr.getRightExpression() instanceof EmptyExpression) {
			return expr.setJulietCode(JulietGen.genRandomExpression(expr.getLeftExpression().getJulietCode()));
		} 
		expr.setJulietCode(JulietGen.genRandomExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
		
	}

	// protected JulietCode initializeLocalVariable(MBVariable mbvar) throws
	// JulietCodeGeneratorException, DatabaseException{
	// JulietCode init = JulietGen.genLoadLocalEnvironment();
	// init.append(initializeVariable(mbvar));
	// return init;
	// }

	@Override
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
		super.visitInstanceOfExpression(expr, attr);
		expr.setJulietCode(JulietGen.genInstanceofExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr)
		throws SBQLException {
	    
	    super.visitAtMostExpression(expr, attr);
	    expr.setJulietCode(JulietGen.genAtMostCardinality(expr.getExpression().getJulietCode(), expr.getMaxCardinality()));
	    return postProcessExpressionCodeGeneration(expr);
	    
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtLeastExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr)
		throws SBQLException {
	    
	    super.visitAtLeastExpression(expr, attr);
	    expr.setJulietCode(JulietGen.genAtLeastCardinality(expr.getExpression().getJulietCode(), expr.getMinCardinality()));
	    return postProcessExpressionCodeGeneration(expr);
	    
	}
	@Override
	public Object visitLazyFailureExpression(LazyFailureExpression expr,
			Object attr) throws SBQLException {
		super.visitLazyFailureExpression(expr, attr);
		expr.setJulietCode(JulietGen.genLazyFailureExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	 */
	@Override
	public Object visitThrowStatement(ThrowStatement stmt, Object attr)
		throws SBQLException
	{
	    
	    super.visitThrowStatement(stmt, attr);
	    
	    stmt.setJulietCode(JulietGen.genThrowStatement(stmt.getExpression().getJulietCode()));
	    return postProcessStatementCodeGeneration(stmt);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRangeAsExpression(odra.sbql.ast.expressions.RangeAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
		super.visitRangeAsExpression(expr, attr);
		JulietCode code = JulietGen.getRangeAs(expr.getExpression().getJulietCode(), this.name2Id(expr.name().value()));
		expr.setJulietCode(code);
		return postProcessExpressionCodeGeneration(expr);
	}

	/**
	 * Generate code for init variable. Assumes that on the stack we have a reference to the parent object the variable
	 * should be created in. The generated code will create as many objects as the variable minimal cardinality
	 * 
	 * @param mbvar
	 * @return
	 */
	protected JulietCode initializeLocalVariable(MBVariable mbvar) 
	{

		JulietCode jtc = new JulietCode();
		try {
			int minCard = mbvar.getMinCard();
			int maxCard = mbvar.getMaxCard();
		    if (minCard != 1 || maxCard != 1) {
		    	jtc.append(JulietGen.genLoadLocalEnvironment());
		    	jtc.append(JulietGen.genInitVariable(mbvar, minCard, maxCard));
		    	jtc.append(JulietGen.genPopQRES());
		    }
		    for (int i = 0; i < minCard; i++) {
		    	jtc.append(JulietGen.genLoadLocalEnvironment());
		    	jtc.append(JulietGen.genCreate(mbvar));		    	
		    }
		    postprocesVariableInitialization(mbvar.getName(), jtc);
		} catch (DatabaseException e) {
		    throw new EmiterException(e);
		}
		
		return jtc;
	}

	/**
	 * @param name
	 * @param jtc
	 */
	private void postprocesVariableInitialization(String name, JulietCode jtc) {
		if(this.debug){
			VariableDeclarationStatement stmt = this.localVariableDeclaration.get(name);
			stmt.setDebug(new DebugNodeData(jtc.getStart(), jtc.getEnd(), stmt.line, stmt.column));
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitParallelUnionExpression(odra.sbql.ast.expressions.ParallelUnionExpression, java.lang.Object)
	 */
	@Override
	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr) throws SBQLException {
		updateAsynchronousRemoteQueriesID();
		super.visitParallelUnionExpression(expr, attr);
		
		expr.setJulietCode(JulietGen.genParallelUnionExpression(expr.getParallelExpressionsJulietCodes(), releaseAsynchronousRemoteQueriesID()));
		return postProcessExpressionCodeGeneration(expr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
		
		super.visitSerializeOidExpression(expr, attr);
		expr.setJulietCode(JulietGen.getSerializeOIDExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}



	private void generateExpressionDebugInfo(Expression expr) {
		if (this.debug) {
			assert expr.getJulietCode() != null : "no expression ast node juliet code";
			expr.setDebug(new DebugNodeData(expr.getJulietCode().getStart(), expr.getJulietCode().getEnd(), expr.line, expr.column));
		}
	}

	private void generateStatementDebugInfo(Statement stmt) {
		if (this.debug) {
			assert stmt.getJulietCode() != null : "no statement ast node juliet code";
			stmt.setDebug(new DebugNodeData(stmt.getJulietCode().getStart(), stmt.getJulietCode().getEnd(), stmt.line, stmt.column));
		}
	}

	/* (non-Javadoc)
	 * @see odra.sbql.emiter.IJulietCodeGenerator#getCatchCode()
	 */
	public CompiletimeExceptionTable getCatchTable() throws SBQLException
	{
	    
	    return catchTable;
	}

	private JulietCode generateProcedureBodyHeader() {
	    JulietCode procedureHeader;
	    try {
		procedureHeader = JulietGen.genProcedureBodyHeader(this.proc, ConfigServer.TYPECHECKING);
	    } catch (DatabaseException e) {
		throw new EmiterException(e);
	    }
		
	    procedureHeader.append(JulietGen.genBeginTransForMBObject(this.proc));
	    return procedureHeader;
	}
	
	// Use in the beggining of visitor for expression containing asynchronous remote queries block
	private void updateAsynchronousRemoteQueriesID() {
		asynchronous_id++;
	}
	
	// Returns id used for queries in current asynchronous remote queries block
	// Use for code generation
	private int releaseAsynchronousRemoteQueriesID() {
		assert asynchronous_id >= 0: "Error in asynchronous remote queries managing code generation (asynchronous_id >= 0)";
		return asynchronous_id--;
	}
	protected int name2Id(String name) {
	    try {
		return Database.getNameIndex().addName(name);
	    } catch (DatabaseException e) {
		throw new EmiterException(e);
	    }	    
	}
	
	/**
	 * @param expr
	 * @return
	 */
	protected JulietCode postProcessExpressionCodeGeneration(Expression expr) {
		this.generateExpressionDebugInfo(expr);
		return expr.getJulietCode();
	}

	/**
	 * @param stmt
	 * @return
	 */
	protected JulietCode postProcessStatementCodeGeneration(Statement stmt) {
		this.generateStatementDebugInfo(stmt);
		return stmt.getJulietCode();
	}
}