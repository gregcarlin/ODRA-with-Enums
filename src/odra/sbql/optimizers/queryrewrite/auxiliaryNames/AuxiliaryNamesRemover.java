package odra.sbql.optimizers.queryrewrite.auxiliaryNames;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;

/** 
 * AuxiliaryNamesRemover
 * @author Radek Adamus
 * @since 2007-02-06
 * last modified: 2007-07-14
 *@version 2.0
 * 
 * current algorithm (works only for navigation):
 * 
 * if navigation (dot) expression right hand operand is a name expression
 * and left hand operand is 'as' expression <br>(2008-01-10 TODO: or it is an operator like e.g. RangeExpression that sub-expression is AsExpression)<br>
 * and the auxiliary name equals the name that is bound by the right hand
 * than:
 * 	the as expression is removed, right hand name expression is removed
 * 	dot expression is removed. The result is the operand of as expression
 * 
 * else 
 * 
 * if navigation (dot) expression right hand operand is a name expression
 * and left hand operand is binary expression
 * and the right hand operand of the binary expression is 'as' expression 
 * and the auxiliary name equals the name that is bound by the right hand
 * than:
 * 	right hand name expression is removed
 * 	dot expression is removed. The result is the left hand binary expression
 * 	where the right hand as expression is replaced with the 'as' expression operand 
 * 
 * 13.07.07 (Friday) algorithm extended
 * (all above)
 * else
 * if navigation (dot) expression right hand operand is 'dot' expression (named D1)
 *    and left hand operand is an 'as' expression 
 *    and the D1 left hand operand is a name expression
 *    and the auxiliary name equals the name that is bound by the D1 left
 *    
 * than:
 *      D1 left operand is replaced with 'as' expression operand 
 * 	currently processed dot expression is replaced with D1
 * 
 * 14.07.07 one another case implemented
 * * (all above)
 * else
 * if navigation (dot) expression right hand operand is 'dot' expression (named D1)
 *  	and left hand operand is a binary expression (named B1)
 * 	and B1 right hand operand is an 'as' expression
 * 	and the D1 left hand operand is a name expression
 * 	and the auxiliary name (from B1 right ) equals the name that is bound by the D1 left
 * 
 * than:
 *      D1 is replaced with its right operand 
 * 	B1 right hand operand ('as' expression) is replaced with its operand
 * 
 * 2007.12.06 promoted to separate optimization method 
 */

public class AuxiliaryNamesRemover extends TraversingASTAdapter implements ISBQLOptimizer{
	private Expression root;
	private boolean removed = false;
	ASTAdapter staticEval;
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#optimize(odra.sbql.ast.ASTNode, odra.db.objects.data.DBModule)
	 */
	public ASTNode optimize(ASTNode query, DBModule module)
		throws SBQLException {
	    try {
		this.setSourceModuleName(module.getName());
	    } catch (DatabaseException e) {
		throw new OptimizationException(e, query, this);
	    }
	    if(query instanceof Expression){
		Expression result = this.remove((Expression)query);
		assert this.staticEval != null : "static evaluator != null";
		result.accept(this.staticEval, null);
		return result;
	    }
	    throw new OptimizationException("expression type required", query, this);
	    
	}
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#reset()
	 */
	public void reset() {
	    removed = false;
	    
	}
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#setStaticEval(odra.sbql.ast.ASTAdapter)
	 */
	public void setStaticEval(ASTAdapter staticEval) {
	    this.staticEval = staticEval;
	    
	}
	public Expression remove(Expression query)throws SBQLException{
	    this.root = query;
	    do{
		removed = false;
		root.accept(this, null);
	    }while(removed);
	    return root;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDotExpression(odra.sbql.ast.expressions.DotExpression, java.lang.Object)
	 */
	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
	   expr.getLeftExpression().accept(this, attr);
	   expr.getRightExpression().accept(this, attr);
	   
	   if(expr.getRightExpression() instanceof NameExpression){
	       NameExpression nameExpr = (NameExpression)expr.getRightExpression();
	       if(expr.getLeftExpression() instanceof BinaryExpression && ((BinaryExpression)expr.getLeftExpression()).getRightExpression() instanceof AsExpression){
		   BinaryExpression binExpr = (BinaryExpression)expr.getLeftExpression();
		   AsExpression asExpr = (AsExpression)binExpr.getRightExpression();
		   if(nameExpr.name().value().equals(asExpr.name().value())) {
		       binExpr.replaceSubexpr(asExpr, asExpr.getExpression());
		   
		       if(expr.getParentExpression() != null){
			   expr.getParentExpression().replaceSubexpr(expr, binExpr);		
		       }else {
		      	 binExpr.setParentExpression(null);
		      	 root = binExpr;
		       }
		       removed = true;
		   }
		   
	       }else if(expr.getLeftExpression() instanceof AsExpression){
		   AsExpression asExpr = (AsExpression)expr.getLeftExpression();
		   if(nameExpr.name().value().equals(asExpr.name().value())) {
		       if(expr.getParentExpression() != null){
   				expr.getParentExpression().replaceSubexpr(expr, asExpr.getExpression());			
		       }else {
		      	 asExpr.getExpression().setParentExpression(null);
		      	 root = asExpr.getExpression();
			    }
		       removed = true;
		   }
	       }
	   }else if(expr.getRightExpression() instanceof DotExpression && ((DotExpression)expr.getRightExpression()).getLeftExpression() instanceof NameExpression){
	       	NameExpression nameExpr = ((NameExpression)((DotExpression)expr.getRightExpression()).getLeftExpression());
		    if(expr.getLeftExpression() instanceof AsExpression){			 
			 AsExpression asExpr = (AsExpression)expr.getLeftExpression();
			 if(asExpr.name().value().equals(nameExpr.name().value())){
			     expr.getRightExpression().replaceSubexpr(nameExpr, asExpr.getExpression());
			     if(expr.getParentExpression() != null)
			     {
					expr.getParentExpression().replaceSubexpr(expr, expr.getRightExpression());			
			       }else
			       {
			      	 expr.getRightExpression().setParentExpression(null);
			      	 root = expr.getRightExpression();
			       }
			     removed = true;
			 }
		     }else if(expr.getLeftExpression() instanceof BinaryExpression && ((BinaryExpression)expr.getLeftExpression()).getRightExpression() instanceof AsExpression){
			   BinaryExpression binExpr = (BinaryExpression)expr.getLeftExpression();
			   AsExpression asExpr = (AsExpression)binExpr.getRightExpression();
			   if(asExpr.name().value().equals(nameExpr.name().value())){
			     expr.getLeftExpression().replaceSubexpr(asExpr, asExpr.getExpression());
			     expr.replaceSubexpr(expr.getRightExpression(), ((DotExpression)expr.getRightExpression()).getRightExpression());
			     removed = true;
			   }
		     }
	  }
	    return null;
	}

	/**
	 * Experimental attempt to deal with joins.
	 * 
	 * @author jacenty
	 */
	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException
	{
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);

		if(
				expr.getLeftExpression() instanceof AsExpression && 
				expr.getRightExpression() instanceof DotExpression && ((DotExpression)expr.getRightExpression()).getLeftExpression() instanceof NameExpression)
		{
			AsExpression asExpression = (AsExpression)expr.getLeftExpression();
			DotExpression dotExpression = (DotExpression)expr.getRightExpression();
			
			if(((DotExpression)expr.getRightExpression()).getRightExpression() instanceof NameExpression)
			{
				if(asExpression.name().value().equals(((NameExpression)dotExpression.getLeftExpression()).name().value()))
				{
					JoinExpression newJoin = new JoinExpression(asExpression.getExpression(), dotExpression.getRightExpression());
					if(expr.getParentExpression() == null)
						this.root = newJoin;
					else
					{
						expr.getParentExpression().replaceSubexpr(expr, newJoin);
					}
					removed = true;
				}
			}
		}
		
		return null;
	}
	
	//TODO 'as' can be hidden under other "connecting" expression
	private boolean isUnaryConnectingOperator(Expression expr){
	    if(expr instanceof RangeExpression)
		return true;
	    return false;
	}
}

