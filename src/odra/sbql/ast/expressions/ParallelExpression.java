package odra.sbql.ast.expressions;

import java.util.ArrayList;

import odra.sbql.emiter.JulietCode;

/**
 * @author janek
 *
 */
public abstract class ParallelExpression extends Expression
{
	protected ArrayList<Expression> parallelExpressions;
	
	public ParallelExpression()
	{
		parallelExpressions = new ArrayList<Expression>();
	}

	public ArrayList<Expression> getParallelExpressions()
	{
		return parallelExpressions;
	}

	public ArrayList<JulietCode> getParallelExpressionsJulietCodes()
	{
		ArrayList<JulietCode> julietCodes = new ArrayList<JulietCode>();
		for(Expression subexpr: parallelExpressions)
			julietCodes.add(subexpr.getJulietCode());
		return julietCodes;
	}
	
	public void addExpression(Expression expr)
	{
		parallelExpressions.add(expr);
	}
}

