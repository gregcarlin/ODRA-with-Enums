/**
 * 
 */
package odra.sbql.ast.utils;

import odra.sbql.SBQLException;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * ResultAwareASTNodeFinder
 * it searches for a nodes only in sub-nodes that can be part of the result
 * e.g. for dot expression it searches only right sub-expression 
 * @author Radek Adamus
 *@since 2007-12-06
 *last modified: 2007-12-06
 *@version 1.0
 */
public class ResultPathAwareASTNodeFinder extends ASTNodeFinder {

    /**
     * @param pattern
     * @param skipTraversOnMatch
     */
    public ResultPathAwareASTNodeFinder(Pattern pattern,
	    boolean skipTraversOnMatch) {
	super(pattern, skipTraversOnMatch);
	// TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitDotExpression(odra.sbql.ast.expressions.DotExpression, java.lang.Object)
     */
    @Override
    public Object visitDotExpression(DotExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		if (skipTraversOnMatch) {
			result.add(expr);
			return null;
		} 
		//expr.getLeftExpression().accept(this, attr);
		result.add(expr);
		expr.getRightExpression().accept(this, attr);
		return null;
	}
	
	//expr.getLeftExpression().accept(this, attr);
	expr.getRightExpression().accept(this, attr);
	
	return null;
    }

   

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitOrderByExpression(odra.sbql.ast.expressions.OrderByExpression, java.lang.Object)
     */
    @Override
    public Object visitOrderByExpression(OrderByExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		if (skipTraversOnMatch) {
			result.add(expr);
			return null;
		} 
		expr.getLeftExpression().accept(this, attr);
		result.add(expr);
		//expr.getRightExpression().accept(this, attr);
		return null;
	}
	
	expr.getLeftExpression().accept(this, attr);
	//expr.getRightExpression().accept(this, attr);
	
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitWhereExpression(odra.sbql.ast.expressions.WhereExpression, java.lang.Object)
     */
    @Override
    public Object visitWhereExpression(WhereExpression expr, Object attr)
	    throws SBQLException {
	
	if (pattern.matches(expr)) {
		if (skipTraversOnMatch) {
			result.add(expr);
			return null;
		} 
		expr.getLeftExpression().accept(this, attr);
		result.add(expr);
		//expr.getRightExpression().accept(this, attr);
		return null;
	}
	
	expr.getLeftExpression().accept(this, attr);
	//expr.getRightExpression().accept(this, attr);
	
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitAvgExpression(odra.sbql.ast.expressions.AvgExpression, java.lang.Object)
     */
    @Override
    public Object visitAvgExpression(AvgExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitCountExpression(odra.sbql.ast.expressions.CountExpression, java.lang.Object)
     */
    @Override
    public Object visitCountExpression(CountExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitEqualityExpression(odra.sbql.ast.expressions.EqualityExpression, java.lang.Object)
     */
    @Override
    public Object visitEqualityExpression(EqualityExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitExistsExpression(odra.sbql.ast.expressions.ExistsExpression, java.lang.Object)
     */
    @Override
    public Object visitExistsExpression(ExistsExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitForAllExpression(odra.sbql.ast.expressions.ForAllExpression, java.lang.Object)
     */
    @Override
    public Object visitForAllExpression(ForAllExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitForSomeExpression(odra.sbql.ast.expressions.ForSomeExpression, java.lang.Object)
     */
    @Override
    public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitInExpression(odra.sbql.ast.expressions.InExpression, java.lang.Object)
     */
    @Override
    public Object visitInExpression(InExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitMaxExpression(odra.sbql.ast.expressions.MaxExpression, java.lang.Object)
     */
    @Override
    public Object visitMaxExpression(MaxExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitMinExpression(odra.sbql.ast.expressions.MinExpression, java.lang.Object)
     */
    @Override
    public Object visitMinExpression(MinExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitSumExpression(odra.sbql.ast.expressions.SumExpression, java.lang.Object)
     */
    @Override
    public Object visitSumExpression(SumExpression expr, Object attr)
	    throws SBQLException {
	if (pattern.matches(expr)) {
		
	    result.add(expr);	    
	} 
	return null;
    }

    
}
