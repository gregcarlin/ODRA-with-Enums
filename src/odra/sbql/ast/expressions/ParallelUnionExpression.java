package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * @author janek
 *
 */
public class ParallelUnionExpression extends ParallelExpression
{

	public ParallelUnionExpression()
	{
		super();
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitParallelUnionExpression(this, attr);
	}
	
}
