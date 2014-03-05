package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * Expression for SQL <i>execute immediately</i> clauses.
 * 
 * @author jacenty
 * @version 2007-06-30
 * @since 2006-01-18
 */
public class ExecSqlExpression extends Expression
{
	public Expression query;
	public StringExpression pattern;
	public StringExpression module;
	
	public ExecSqlExpression(Expression query, StringExpression pattern, StringExpression module)
	{
		this.query = query;
		this.pattern = pattern;
		this.module = module;
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException
	{
		return vis.visitExecSqlExpression(this, attr);
	}	
}
