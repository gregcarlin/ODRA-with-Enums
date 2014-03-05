package odra.sbql.optimizers.queryrewrite.procedures;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBVariable;
import odra.sbql.SBQLException;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.optimizers.OptimizationException;

/**
 * ProcedureCallReplacer
 * Generic procedure call replacer
 * @author Radek Adamus
 *last modified: 2007-02-16
 *@version 1.0
 */
public class ProcedureCallReplacer extends TraversingASTAdapter{
	MBProcedure proc;
	boolean needReturnWithValue;
	
	/** Return replacement expression for a procedure call
	 * if impossible - returns null
	 * @param mbproc - meta procedure
	 * @param argumentExpression - arguments (or EmptyExpression if procedure without arguments)
	 * @param needReturnWithValue - do we need functional procedure
	 * @return replacement expression or null (if procedure cannot be replaced with single expression)
	 * @throws Exception
	 */
	public Expression getReplacementExpression(String modName, MBProcedure mbproc, Expression argumentExpression, boolean needReturnWithValue) throws SBQLException{
	        this.setSourceModuleName(modName);
		proc = mbproc;
		try {
		    Statement procast = (Statement)BuilderUtils.deserializeAST(mbproc.getAST());
		    Expression replacement = (Expression)procast.accept(this, null);
		    if(replacement != null){
		    	OID[] argsids = proc.getArguments();
		    	
		    	if(argsids.length == 0)
		    		return replacement;
		    		//we have params
		    	MBVariable[] args = new MBVariable[argsids.length]; 	
		    		//collect names
		    	String[] argnames = new String[args.length];
		    	for(int i = 0; i < args.length; i++){
		    			argnames[i] = argsids[i].getObjectName();
		    			args[i] = new MBVariable(argsids[i]);
		    	}
		    	ArgumentsMutabilityChecker mcheck = new ArgumentsMutabilityChecker(argnames);
		    	if(mcheck.check(procast)){
		    			return this.rewriteArguments(args, argnames, argumentExpression, replacement);
		    	}
		    }
		} catch (DatabaseException e) {
		    throw new OptimizationException(e);
		} 
		return null;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement, java.lang.Object)
	 */
	@Override
	public Object visitBlockStatement(BlockStatement stmt, Object attr) throws SBQLException {
		return stmt.getStatement().accept(this, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitEmptyStatement(odra.sbql.ast.statements.EmptyStatement, java.lang.Object)
	 */
	@Override
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		// TODO Auto-generated method stub
		//??
		return new EmptyExpression();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitExpressionStatement(odra.sbql.ast.statements.ExpressionStatement, java.lang.Object)
	 */
	@Override
	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {
		if(this.needReturnWithValue)
			return null;
		return stmt.getExpression();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitForEachStatement(odra.sbql.ast.statements.ForEachStatement, java.lang.Object)
	 */
	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
		if(this.needReturnWithValue)
			return null;
		//TODO rethink
//		Expression e = (Expression)stmt.S.accept(this, attr);
//		if(e != null)
//			return new DotExpression(stmt.E, e);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {
		Expression thenE = (Expression)stmt.getIfStatement().accept(this, attr);
		if(thenE == null)
			return null;
		Expression elseE = (Expression)stmt.getElseStatement().accept(this, attr);
		if(elseE == null)
			return null;
		
		return new IfThenElseExpression(stmt.getExpression(), thenE, elseE);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitIfStatement(odra.sbql.ast.statements.IfStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
		Expression thenE = (Expression)stmt.getStatement().accept(this, attr);
		if(thenE == null)
			return null;
		return new IfThenExpression(stmt.getExpression(), thenE);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitReturnWithoutValueStatement(odra.sbql.ast.statements.ReturnWithoutValueStatement, java.lang.Object)
	 */
	@Override
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		if(this.needReturnWithValue)
			return null;
		return new EmptyExpression();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitReturnWithValueStatement(odra.sbql.ast.statements.ReturnWithValueStatement, java.lang.Object)
	 */
	@Override
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
	    
		return stmt.getExpression();
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitSequentialStatement(odra.sbql.ast.statements.SequentialStatement, java.lang.Object)
	 */
	@Override
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
		return null;
	}
	
	private Expression rewriteArguments(MBVariable[] args, String[] argNames, Expression argumentExpression, Expression replacement) throws SBQLException{
		
	    	Expression[] params = ((Expression)DeepCopyAST.copy(argumentExpression)).flatten();
			if(argNames.length != params.length) return null;
		//if the argument cardinality == 1 we can use AsExpression, otherwise GroupAs
		Expression lexpr;
		try {
		    lexpr = (args[0].getMinCard() == 1 && args[0].getMinCard() == 1)
		    												? new AsExpression(params[0], new Name(argNames[0]))
		    												: new GroupAsExpression(params[0], new Name(argNames[0]));
		    for(int i = 1; i < argNames.length; i++){
		    	Expression rexpr = (args[i].getMinCard() == 1 && args[i].getMinCard() == 1)
		    														? new AsExpression(params[0], new Name(argNames[i]))
		    														: new GroupAsExpression(params[i], new Name(argNames[i]));
		    	lexpr = new CommaExpression(lexpr, rexpr);
		    }
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, argumentExpression,this);
		}
		
		replacement = new DotExpression(lexpr, replacement);
		return replacement;

	}
	
}
