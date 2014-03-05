package odra.sbql.optimizers.queryrewrite.distributed;

import java.util.ArrayList;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.results.compiletime.ValueSignature;
import odra.system.config.ConfigDebug;

/**
 * DistributedInDependencyChecker searches for dependant parameters within remote query
 * 
 * @author janek
 *
 */
public class DistributedInDependencyChecker extends TraversingASTAdapter
{

	private boolean isParmDependent;
	private NonAlgebraicExpression context; // non algebraic operator for which we check
	private ArrayList<NameExpression> parmDependentNames;

	/**
	 * @param context -
	 *            non-algebraic operator - the context for independency checking
	 *            process
	 */
	public DistributedInDependencyChecker(NonAlgebraicExpression context)
	{
		if (ConfigDebug.ASSERTS)
			assert context.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";

		this.context = context;
		isParmDependent = false;
		parmDependentNames = new ArrayList<NameExpression>();
	}

	public Object visitNameExpression(NameExpression expr, Object attr)
			throws SBQLException {


		if ( ( (expr.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize) || (expr.getBindingInfo().boundat >= context.getEnvsInfo().baseEnvsSize + context.getEnvsInfo().framesOpened) ) 
				&& (expr.name().value().startsWith("$aux"))  && ( expr.getSignature() instanceof ValueSignature) )
		{
			isParmDependent = true;
			parmDependentNames.add(expr);
		}

		return null;
	}

	public ArrayList<NameExpression> getParmDependentNames()
	{
		return parmDependentNames;
	}

	public boolean isParmDependent()
	{
		return isParmDependent;
	}
}
