package odra.sbql.optimizers.queryrewrite.wrapper;

import java.util.Hashtable;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.AssignFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.RangeExpressionFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.TableFinder;
import odra.system.config.ConfigDebug;
import odra.wrapper.Wrapper;
import odra.wrapper.model.Name;
import odra.wrapper.resultpattern.ResultPattern;
import odra.wrapper.sql.Type;
import odra.wrapper.sql.builder.QueryBuilder;
import odra.wrapper.sql.builder.Table;

/**
 * An optimizer rewriting subqueries transformable into <code>execsql</code> expressions with no optmization.
 * 
 * @author jacenty
 * @version 2007-07-23
 * @since 2007-02-02
 */
public class WrapperRewriter extends TraversingASTAdapter implements ISBQLOptimizer
{
	public void setStaticEval(@SuppressWarnings("unused") ASTAdapter staticEval)
	{
		//nothing happens
	}

	public void reset()
	{
		//nothing happens
	}

	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException
	{
		if(ConfigDebug.ASSERTS)
			assert query != null : "query == null";
		if(ConfigDebug.ASSERTS)
			assert module != null : "module == null";
		
		if(!(query instanceof Expression))
			return query;
		
		Expression resultQuery = (Expression)query;
		Hashtable<String, Wrapper> wrappers = Utils.markWrappers(resultQuery);
		
		
		//check if wrapper rewriting is valid for the whole expresion
		if(!canMethodBeApplied(resultQuery))
			throw new OptimizationException("Wrapper rewriting cannot be applied to the query: \n" + query, query, this);
		
		if(!new AssignFinder().findNodes(query).isEmpty())
		{
			//reject assignent with mixed relational and non-relational names
			//return unmodified input assign expression with only non-relational names otherwise
			if(!Utils.hasOnlyNonRelationalNames(resultQuery))
				throw new OptimizationException("Assign in wraper rewriter is not available - use wrapper optimizer instead...", query, this);
			else
				return resultQuery;
		}		
		//find all wrapper-rewritable queries and rewrite them
		TableFinder tableFinder = new TableFinder(wrappers);
		for(ASTNode node : tableFinder.findNodes(resultQuery))
		{
			Expression parent = ((Expression)node).getParentExpression();
			Expression toRewrite = (Expression)node;
			if(toRewrite instanceof UnaryExpression)
			{
				parent = toRewrite;
				toRewrite = ((UnaryExpression)toRewrite).getExpression();
			}

			Wrapper wrapper = wrappers.get(toRewrite.wrapper);
			String moduleGlobalName;
			try {
			    moduleGlobalName = wrapper.getModule().getModuleGlobalName();
			} catch (DatabaseException e) {
			    	throw new OptimizationException(e, query, this);
			}
			ResultPattern resultPattern = Utils.analyzeSignatureForResultPattern(toRewrite.getSignature(), wrapper.getModel(), wrapper.getModule());
			Table table = new Table(wrapper.getModel().getTable(Name.o2r(resultPattern.getTableName())));
			table.addSelectedColumn(Table.ALL);
			
			QueryBuilder builder = new QueryBuilder(wrapper.getModel());
			builder.addSelect(table);
			builder.addFrom(table);
			String sql = builder.build(Type.SELECT).toString();
			
			Expression rewritten = new ExecSqlExpression(
				new StringExpression(new StringLiteral(sql)),
				new StringExpression(new StringLiteral(resultPattern.toString())),
				new StringExpression(new StringLiteral(moduleGlobalName)));
			rewritten.setSignature(toRewrite.getSignature());
			
			if(parent == null)
				resultQuery = rewritten;
			else
				parent.replaceSubexpr(toRewrite, rewritten);
		}
		
		return resultQuery;
	}
	
	/**
	 * Returns if a wrapper rewriting can be applied to a query. 
	 * 
	 * @param query expression
	 * @return can be applied?
	 * @throws Exception 
	 */
	private boolean canMethodBeApplied(Expression query) throws SBQLException
	{
		if(!new RangeExpressionFinder().findNodes(query).isEmpty())//find any RangeExpression, reject query if any exists
			throw new OptimizationException("RangeExpressions are not allowed in wrapper queries due to the dynamic character of data retrieved...");
		
		return true;
	}
}