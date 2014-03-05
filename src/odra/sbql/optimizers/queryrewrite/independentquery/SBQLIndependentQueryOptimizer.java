package odra.sbql.optimizers.queryrewrite.independentquery;

import java.util.ArrayList;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.system.config.ConfigDebug;

/**
 * SBQLIndependentQueryOptimizer - class implementing 
 * independent query optimization method (pushing and factoring)
 * @author radamus
 */
public class SBQLIndependentQueryOptimizer extends TraversingASTAdapter implements ISBQLOptimizer{
	
	private int nameSuffix = 0;
	//TODO - possibly we should create and work on the copy of the original tree 
	private ASTNode root;
	
	//TODO - it should be better expressed!!
	private ASTAdapter staticEval;
	
	
	private Vector<Expression> operators = new Vector<Expression>();
	
	public void setStaticEval(ASTAdapter staticEval) {
		this.staticEval = staticEval;
		
	}
	public void reset(){
		nameSuffix = 0; 
	}
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#optimize(odra.sbql.ast.ASTNode)
	 */
	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException{
		if(ConfigDebug.ASSERTS) assert query != null: "query != null";
		this.root = query;
		try {
		    this.setSourceModuleName(module.getName());
		} catch (DatabaseException e) {
		    throw new OptimizationException(e,query,this);
		}
		query.accept(this, OptimizationKind.ANY);
		return root;
	}
	
	
public Object visitDotExpression(DotExpression expr, Object attr)
			throws SBQLException {
		applyIndependentSubQueryMethod(expr, attr);

		return null;
	}

	public Object visitForAllExpression(ForAllExpression expr, Object attr)
			throws SBQLException {
		applyIndependentSubQueryMethod(expr, attr);
		return null;
	}

	public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
			throws SBQLException {
		applyIndependentSubQueryMethod(expr, attr);

		return null;
	}

	public Object visitJoinExpression(JoinExpression expr, Object attr)
			throws SBQLException {
		applyIndependentSubQueryMethod(expr, attr);
		return null;
	}

	

	public Object visitOrderByExpression(OrderByExpression expr, Object attr)
			throws SBQLException {
		applyIndependentSubQueryMethod(expr, attr);
		return null;
	}

	public Object visitWhereExpression(WhereExpression expr, Object attr)
			throws SBQLException {
		applyIndependentSubQueryMethod(expr, attr);
		return null;
	}


/** Perform optimization: 
	 * 1. search for the independent query and possible optimization kind
	 * 2. rewrite the query (change the AST)
	 * @param expr - non-algebraic AST node - context for the optimization process
	 * @param attr - visitor parameter - unused
	 * @throws Exception
	 */
	private void applyIndependentSubQueryMethod(NonAlgebraicExpression expr, Object attr) throws SBQLException{
		if(ConfigDebug.ASSERTS) assert expr.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		IndependentSubQuerySearcher searcher = new IndependentSubQuerySearcher(expr); 
		Expression subExpr = (Expression)expr.getRightExpression().accept(searcher, attr);
		while(subExpr != null){ //TODO recurrent optimization 
		
			switch(searcher.optKind){
			case PUSHING:
				pushing(expr, subExpr, searcher.pushPredicateType);
				break;
			case FACTORING:
				factoring(expr, subExpr);
				break;
			default:
				assert false: "unknown optimization kind " + searcher.optKind;
				break;
			}
	//determine the scope number and binding levels for the new form of a whole query 
		 root.accept(staticEval, null);
	//apply the method to the independent sub-query
		 subExpr.accept(this, attr);
		 //find another sub-query of E2 independent from expr (operator Q) 
		 searcher = new IndependentSubQuerySearcher(expr);
		 subExpr = (Expression)expr.getRightExpression().accept(searcher, attr);
		 
		}
//		if(subExpr != null){
			expr.getLeftExpression().accept(this, attr);
			expr.getRightExpression().accept(this, attr);
//			}
			
	}
	
	
	/** Factor indepented subquery before operator
	 * @param expr node of the non-algebraic operator 
	 * @param subExpr - sub-query that should be factored out
	 */
	private void factoring(NonAlgebraicExpression expr, Expression subExpr){
		
		String auxname = "$aux" + nameSuffix++;
		subExpr.getParentExpression ().replaceSubexpr(subExpr, new NameExpression(new Name(auxname)));
		
		
		
		
		
		/*if(expr.getParentExpression () != null){
			expr.getParentExpression ().replaceSubexpr(expr, new DotExpression(new GroupAsExpression(subExpr, new Name(auxname)) ,expr));
		}else {
			root = new DotExpression(new GroupAsExpression(subExpr, new Name(auxname)) ,expr);
		}*/
		
		
		if(operators.contains(expr)){
			Expression lExpr=((DotExpression)(expr.getParentExpression())).getLeftExpression();
			lExpr.getParentExpression().replaceSubexpr(lExpr, new CommaExpression(lExpr,new GroupAsExpression(subExpr, new Name(auxname))));
		}
		else{
			if(expr.getParentExpression () != null){
				expr.getParentExpression ().replaceSubexpr(expr, new DotExpression(new GroupAsExpression(subExpr, new Name(auxname)) ,expr));
			}else {
				root = new DotExpression(new GroupAsExpression(subExpr, new Name(auxname)) ,expr);
			}
		}
		
		operators.add(expr);
		
	}
		
	/**
	 * Push indepented subquery before operator
	 * @param expr - node of the non-algebraic operator
	 * @param subExpr - sub-query that should be pushed
	 * @param predicateType - tells whether we push whole predicate or only a part of it 
	 */
	private void pushing(NonAlgebraicExpression expr, Expression subExpr, int predicateType){
		
		switch(predicateType){
		case IndependentSubQuerySearcher.WHOLE:
			if(ConfigDebug.ASSERTS) assert subExpr.getParentExpression () instanceof WhereExpression: "Not a whole predicate: " + subExpr.getParentExpression ().getClass().getSimpleName();
			if(ConfigDebug.ASSERTS) assert subExpr.getParentExpression ().getParentExpression () != null : "nonexistent parent of selection operator";
			Expression leftexpr = ((NonAlgebraicExpression)subExpr.getParentExpression()).getLeftExpression();
			//attach left subquery to 'where' parent
			subExpr.getParentExpression().getParentExpression ().replaceSubexpr(subExpr.getParentExpression(), leftexpr);
			//now push where predicate
			expr.replaceSubexpr(expr.getLeftExpression(), new WhereExpression(expr.getLeftExpression(), subExpr));
			break;
		case IndependentSubQuerySearcher.PART:
			if(ConfigDebug.ASSERTS) assert (subExpr.getParentExpression () instanceof SimpleBinaryExpression) && (((SimpleBinaryExpression)subExpr.getParentExpression ()).O.equals(Operator.opAnd)): "Not a whole predicate: " + subExpr.getParentExpression ().getClass().getSimpleName();
			SimpleBinaryExpression andExpr = (SimpleBinaryExpression)subExpr.getParentExpression();
			if(andExpr.getLeftExpression().equals(subExpr)){
				andExpr.getParentExpression().replaceSubexpr(andExpr, andExpr.getRightExpression());
			}else{ //andExpr.E2.equals(subExpr)
				andExpr.getParentExpression().replaceSubexpr(andExpr, andExpr.getLeftExpression());
			}
			expr.replaceSubexpr(expr.getLeftExpression(), new WhereExpression(expr.getLeftExpression(),subExpr));
			break;
		default:
			if(ConfigDebug.ASSERTS) assert false: "unknown pushing type";
			break;
		}
		
	}

	public enum OptimizationKind {
		NONE,
		PUSHING, 
		FACTORING,
		ANY;
	}

	
	
}
