package odra.sbql.optimizers.queryrewrite.independentquery;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.TransitiveClosureExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.optimizers.queryrewrite.independentquery.SBQLIndependentQueryOptimizer.OptimizationKind;

/**
 * IndependentSubQuerySearcher
 * search engine for independent query optimization method 
 * @author radamus
 * 07.04.07 TransitiveClosureExpression checking added
 * last modified 15.01.08 error fix (check if the where subexpression is a selection predicate - it could be a left sub-expression)
 */
class IndependentSubQuerySearcher extends TraversingASTAdapter {
	private NonAlgebraicExpression context;
	OptimizationKind optKind = OptimizationKind.NONE;
	int pushPredicateType = 0;
//	Expression independentSubQuery = null;
	

	/**
	 * @param context - the non-algebraic operator AST node 
	 * - context for the independency searcher
	 */
	public IndependentSubQuerySearcher(NonAlgebraicExpression context) {
		assert !(context instanceof TransitiveClosureExpression) : "TransitiveClosureExpression canot be optimzed";
		this.context = context;
		
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitAsExpression(odra.sbql.ast.expressions.AsExpression, java.lang.Object)
	 */
	@Override
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}
	
	public Object visitIfThenExpression(IfThenExpression expr,
			Object attr) throws SBQLException {
		if(this.canMethodBeApplied(expr, (OptimizationKind)attr)){
			return expr;
		}
			Expression e = (Expression)expr.getConditionExpression().accept(this, attr);
			return (Expression)(e != null ? e :  expr.getThenExpression().accept(this, attr));
	}

	public Object visitIfThenElseExpression(IfThenElseExpression expr,
			Object attr) throws SBQLException {
		if(this.canMethodBeApplied(expr, (OptimizationKind)attr)){
			return expr;
		}
		else{
			if(this.canMethodBeApplied(expr.getConditionExpression(), (OptimizationKind)attr))
				return expr.getConditionExpression();
				Expression e = (Expression)expr.getThenExpression().accept(this, attr);
				return (Expression)(e != null ? e :  expr.getElseExpression().accept(this, attr));	
				
		}
			
			
	}	

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitAssignExpression(odra.sbql.ast.expressions.AssignExpression, java.lang.Object)
	 */
	@Override
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitAvgExpression(odra.sbql.ast.expressions.AvgExpression, java.lang.Object)
	 */
	@Override
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitCommaExpression(odra.sbql.ast.expressions.CommaExpression, java.lang.Object)
	 */
	@Override
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitCountExpression(odra.sbql.ast.expressions.CountExpression, java.lang.Object)
	 */
	@Override
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitDerefExpression(odra.sbql.ast.expressions.DerefExpression, java.lang.Object)
	 */
	@Override
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRangeExpression(odra.sbql.ast.expressions.RangeExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException {
		// TODO Auto-generated method stub
		return binaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRefExpression(odra.sbql.ast.expressions.RefExpression, java.lang.Object)
	 */
	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		// TODO Auto-generated method stub
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitDotExpression(odra.sbql.ast.expressions.DotExpression, java.lang.Object)
	 */
	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitEqualityExpression(odra.sbql.ast.expressions.EqualityExpression, java.lang.Object)
	 */
	@Override
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitExistsExpression(odra.sbql.ast.expressions.ExistsExpression, java.lang.Object)
	 */
	@Override
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitForAllExpression(odra.sbql.ast.expressions.ForAllExpression, java.lang.Object)
	 */
	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitForSomeExpression(odra.sbql.ast.expressions.ForSomeExpression, java.lang.Object)
	 */
	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitGroupAsExpression(odra.sbql.ast.expressions.GroupAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitInExpression(odra.sbql.ast.expressions.InExpression, java.lang.Object)
	 */
	@Override
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitIntersectExpression(odra.sbql.ast.expressions.IntersectExpression, java.lang.Object)
	 */
	@Override
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitJoinExpression(odra.sbql.ast.expressions.JoinExpression, java.lang.Object)
	 */
	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitMaxExpression(odra.sbql.ast.expressions.MaxExpression, java.lang.Object)
	 */
	@Override
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitMinExpression(odra.sbql.ast.expressions.MinExpression, java.lang.Object)
	 */
	@Override
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitMinusExpression(odra.sbql.ast.expressions.MinusExpression, java.lang.Object)
	 */
	@Override
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitOrderByExpression(odra.sbql.ast.expressions.OrderByExpression, java.lang.Object)
	 */
	@Override
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitSimpleBinaryExpression(odra.sbql.ast.expressions.SimpleBinaryExpression, java.lang.Object)
	 */
	@Override
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitSimpleUnaryExpression(odra.sbql.ast.expressions.SimpleUnaryExpression, java.lang.Object)
	 */
	@Override
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitSumExpression(odra.sbql.ast.expressions.SumExpression, java.lang.Object)
	 */
	@Override
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitToBooleanExpression(odra.sbql.ast.expressions.ToBooleanExpression, java.lang.Object)
	 */
	@Override
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
		if(!isLiteralExpression(expr.getExpression()))
			return unaryCheck(expr, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitToIntegerExpression(odra.sbql.ast.expressions.ToIntegerExpression, java.lang.Object)
	 */
	@Override
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
		if(!isLiteralExpression(expr.getExpression()))
			return unaryCheck(expr, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitToRealExpression(odra.sbql.ast.expressions.ToRealExpression, java.lang.Object)
	 */
	@Override
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		if(!isLiteralExpression(expr.getExpression()))
			return unaryCheck(expr, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitToStringExpression(odra.sbql.ast.expressions.ToStringExpression, java.lang.Object)
	 */
	@Override
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
		if(!isLiteralExpression(expr.getExpression()))
			return unaryCheck(expr, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitUnionExpression(odra.sbql.ast.expressions.UnionExpression, java.lang.Object)
	 */
	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitUniqueExpression(odra.sbql.ast.expressions.UniqueExpression, java.lang.Object)
	 */
	@Override
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizer.ASTOptimizerAdapter#visitWhereExpression(odra.sbql.ast.expressions.WhereExpression, java.lang.Object)
	 */
	@Override
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
		return binaryCheck(expr, attr);
	}



	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitBagExpression(odra.sbql.ast.expressions.BagExpression, java.lang.Object)
	 */
	@Override
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitStructExpression(odra.sbql.ast.expressions.StructExpression, java.lang.Object)
	 */
	@Override
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitToBagExpression(odra.sbql.ast.expressions.ToBagExpression, java.lang.Object)
	 */
	@Override
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitToSingleExpression(odra.sbql.ast.expressions.ToSingleExpression, java.lang.Object)
	 */
	@Override
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		return unaryCheck(expr, attr);
	}


	/**
	 * Returns the AST node representing direct non-algebraic operator for the expr
	 * @param expr
	 * @return the AST node representing direct non-algebraic operator for the expr
	 */
	private NonAlgebraicExpression directNonAlgebraicOperator(Expression expr){
		Expression e = expr.getParentExpression ();
		while(!(e instanceof NonAlgebraicExpression)){
			e = e.getParentExpression ();
		}
		return (NonAlgebraicExpression)e;
	}
	
	/** Algorithim main method
	 * @param expr - expression (sub-query) that should be checked
	 * @param reqKind - what kind of optimization do we search
	 * for the possibility of pushing or factoring out
	 * @return
	 * @throws Exception
	 */
	private boolean canMethodBeApplied(Expression expr, OptimizationKind reqKind) throws SBQLException{
		NonAlgebraicExpression nexpr = directNonAlgebraicOperator(expr);
		
		//we cannot optimize TransitiveClosureExpressions
		while(nexpr instanceof TransitiveClosureExpression){
			nexpr = directNonAlgebraicOperator(nexpr);
		}
		
		boolean distributive = isDistributive(context);
		
		
		//if subquery (expr) is independent of all non-algebraic operators that pushes 
		//their scopes, being bottom scopes for subquery, onto the scope opened by 'context'
		//if there are any, if there are none the condition evaluates to TRUE
		while(nexpr != context){
			IndependencyChecker ind = new IndependencyChecker(nexpr);
			expr.accept(ind, null);
			if(!ind.isIndependent)
				return false;
			if(distributive && !isDistributive(nexpr)) distributive = false; 
			nexpr = directNonAlgebraicOperator(nexpr);
		}
		
		/*
		 * if direct non-algebraic operator is where AND all non-algebraic operators up to 'context'
		 * are distributive AND ('expr' is whole predicate OR 'expr' is predicate part connected with 'and')
		 */ 
		if( 	(reqKind == OptimizationKind.ANY || reqKind == OptimizationKind.PUSHING)  
			&&	distributive
			&& (directNonAlgebraicOperator(expr) != context) //RA modification
			&& (directNonAlgebraicOperator(expr) instanceof WhereExpression)
			&&(isWholeSelectionPredicate(expr) || isProperPartOfSelectionPredicate(expr))){
			optKind = OptimizationKind.PUSHING;
			return true;
		}else if((reqKind == OptimizationKind.ANY || reqKind == OptimizationKind.FACTORING)) { //expr cannot be pushed, check whether it can be factored out 
			IndependencyChecker ind = new IndependencyChecker(context);
			expr.accept(ind, null);
			if(ind.isIndependent){
				optKind = OptimizationKind.FACTORING;
				return true;
			}
		}
		return false;
		
	}
	
	/**
	 * Check if the operator is distributive
	 * @param expr - AST node representing the operator
	 * @return true if distributive, false otherwise
	 */
	private boolean isDistributive(Expression expr){
		if((expr instanceof JoinExpression) 
				|| (expr instanceof DotExpression) 
				|| (expr instanceof WhereExpression)
				|| (expr instanceof IntersectExpression)
				|| (expr instanceof CommaExpression)){
			return true;
		}
		return false;
	}
	
	/** 
	 * Check if the expr represents the whole selection predicate  
	 * @param expr
	 * @return true if whole, false otherwise
	 */
	private boolean isWholeSelectionPredicate(Expression expr){
	    
		if(expr.getParentExpression () instanceof WhereExpression && ((WhereExpression)expr.getParentExpression()).getLeftExpression() != expr){
			this.pushPredicateType = WHOLE;
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the expr represents part of the selection predicate that can be pushed
	 * (connected with the remaining with use of logical AND)
	 * @param expr
	 * @return true if suitable, false otherwise
	 */
	private boolean isProperPartOfSelectionPredicate(Expression expr){
		if((expr.getParentExpression () instanceof SimpleBinaryExpression) && (((SimpleBinaryExpression)expr.getParentExpression ()).O.equals(Operator.opAnd))){
			this.pushPredicateType = PART;
			return true;
		}
		return false;
	}
	
	
	private Expression unaryCheck(UnaryExpression expr, Object attr) throws SBQLException{
		if(this.canMethodBeApplied(expr, (OptimizationKind)attr)){
					return expr;
				}
		return (Expression)expr.getExpression().accept(this, attr);
	}
	
	private Expression binaryCheck(BinaryExpression expr, Object attr) throws SBQLException{
		if(this.canMethodBeApplied(expr, (OptimizationKind)attr)){
			return expr;
		}
		Expression e = (Expression)expr.getLeftExpression().accept(this, attr);
		return (Expression)(e != null ? e :  expr.getRightExpression().accept(this, attr));
		
	}
	private boolean isLiteralExpression(Expression expr){ 
		if(expr instanceof BooleanExpression ||
			expr instanceof IntegerExpression ||
			expr instanceof RealExpression ||
			expr instanceof StringExpression)
			return true;
		return false;
	}
	static final int WHOLE = 1;
	static final int PART = 2;

}
