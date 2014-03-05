package odra.sbql.optimizers.queryrewrite.wrapper;

import java.util.Hashtable;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.StdEnvironment;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.patterns.Pattern;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.AggregateFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.AssignFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.CreateFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.ExecSqlFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.JoinFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.OrderByFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.RangeExpressionFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.TableFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.WhereFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.AggregatePattern;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.DeletePattern;
import odra.sbql.results.compiletime.ValueSignature;
import odra.system.config.ConfigDebug;
import odra.wrapper.Wrapper;
import odra.wrapper.WrapperException;
import odra.wrapper.model.Database;
import odra.wrapper.model.Name;
import odra.wrapper.resultpattern.ConstantPattern;
import odra.wrapper.resultpattern.OperatorPattern;
import odra.wrapper.resultpattern.ResultPattern;
import odra.wrapper.sql.Type;
import odra.wrapper.sql.builder.Aggregate;
import odra.wrapper.sql.builder.Operator;
import odra.wrapper.sql.builder.QueryBuilder;
import odra.wrapper.sql.builder.Table;
import odra.wrapper.sql.builder.WhereBuilder;
import Zql.ZExpression;

/**
 * An optimizer rewriting subqueries transformable into <code>execsql</code> expressions with 
 * application of optmization procedures.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2007-02-19
 */
public class WrapperOptimizer extends TraversingASTAdapter implements ISBQLOptimizer
{	
	/** wrappers referenced by a query */
	Hashtable<String, Wrapper> wrappers;
	/** typechecker */
	private ASTAdapter staticEval;
	
	public void setStaticEval(@SuppressWarnings("unused") ASTAdapter staticEval)
	{
		this.staticEval = staticEval;
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
		
		Expression resultQuery = (Expression)DeepCopyAST.copy(query);

		//find a create operator (check if a query is an insert)
		Vector<ASTNode> creates = new CreateFinder().findNodes(resultQuery);
		//transform create
		if(!creates.isEmpty())
		{
			try
			{
				return transformCreate(resultQuery, (CreateExpression)creates.firstElement(), module);
			}
			catch(OptimizationException exc)//the created object is not wrapper-related, but some value still can be...
			{
				if(ConfigDebug.DEBUG_EXCEPTIONS)
				{
					System.err.println("This exception is controlled, the query is sent to the wrapperrewriter: ");
					exc.printStackTrace();
				}
				return new WrapperRewriter().optimize(query, module);
			}
		}
		
		//wrappers cannot be established from name expressions for create, therefore it is served before
		wrappers = Utils.markWrappers(resultQuery);

		//check if wrapper optimization is valid for the whole expresion
		if(!canMethodBeApplied(resultQuery))
			return new WrapperRewriter().optimize(query, module);
		
		//find an assign operator (check if a query is an update)
		Vector<ASTNode> assigns = new AssignFinder().findNodes(resultQuery);
		//transform assignment
		if(!assigns.isEmpty())
			return transformAssign(resultQuery, (AssignExpression)assigns.firstElement());
		
		//find a delete operator (check if a query is a delete)
		if(new DeletePattern().matches(resultQuery))
			return transformDelete((DeleteExpression)resultQuery);

		try
		{
			//rewrite aggregate functions
			resultQuery = transformExpression(resultQuery, new AggregateFinder());
			//rewrite JoinExpressions
			resultQuery = transformExpression(resultQuery, new JoinFinder());
			//rewrite WhereExpressions
			resultQuery = transformExpression(resultQuery, new WhereFinder());
			//rewrite single-table common expressions
			resultQuery = transformExpression(resultQuery, new TableFinder(wrappers));
		}
		catch(Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
			{
				System.err.println("This exception is controlled, the query is sent to the wrapperrewriter: ");
				exc.printStackTrace();
			}
			return new WrapperRewriter().optimize(query, module);
		}
		
		//re-typecheck the query
		resultQuery.accept(staticEval, null);
		return resultQuery;
	}
	
	/**
	 * Restores auxiliary names introduced for "external" expressions.
	 * 
	 * @param query resulting (optimized) query
	 * @param auxCount auxiliary expression count
	 * @return query with restored acrual expressions
	 * @throws Exception 
	 */
	private Expression restoreAuxExpressions(Expression query, Hashtable<String, Expression> auxBuffer) throws SBQLException
	{
		//rewrite SQL strings in all ExecSqlExpressions
		ExecSqlFinder execSqlFinder = new ExecSqlFinder();
		for(ASTNode node : execSqlFinder.findNodes(query))
		{
			ExecSqlExpression execSqlExpression = (ExecSqlExpression)node;
			
			Expression substitite;
			for(final String aux : auxBuffer.keySet())
			{
				Expression expression = auxBuffer.get(aux);
				ASTNodeFinder auxContainerStringFinder = new ASTNodeFinder(new Pattern()
				{
					public boolean matches(Object obj)
					{
						if(obj instanceof StringExpression)
							return ((StringExpression)obj).getLiteral().value().indexOf(aux) >= 0;
						
						return false;
					}
				}, true);
				
				Vector<ASTNode> found = auxContainerStringFinder.findNodes(query);
				if(!found.isEmpty())
				{
					StringExpression auxContainerStringExpression = (StringExpression)auxContainerStringFinder.findNodes(query).get(0);
					
					String[] split = auxContainerStringExpression.getLiteral().value().split(aux);
					StringExpression left = new StringExpression(new StringLiteral(split[0]));
					StringExpression right = new StringExpression(new StringLiteral(split[1]));
					substitite = new SimpleBinaryExpression(
						new SimpleBinaryExpression(left, expression, odra.sbql.ast.terminals.Operator.opPlus),
						right,
						odra.sbql.ast.terminals.Operator.opPlus);
				
					if(auxContainerStringExpression.getParentExpression() != null)
						auxContainerStringExpression.getParentExpression().replaceSubexpr(auxContainerStringExpression, substitite);
					else
					{
						execSqlExpression = new ExecSqlExpression(
							substitite,
							execSqlExpression.pattern,
							execSqlExpression.module);
					}
					
					if(((Expression)node).getParentExpression() != null)
						((Expression)node).getParentExpression().replaceSubexpr((Expression)node, execSqlExpression);
					else
						query = execSqlExpression;
				}
			}
		}
		
		//substitute back auxiliary expressions in the rest of the query
		for(final String aux : auxBuffer.keySet())
		{
			Expression expression = auxBuffer.get(aux);
			ASTNodeFinder auxFinder = new ASTNodeFinder(new Pattern()
			{
				public boolean matches(Object obj)
				{
					if(obj instanceof StringExpression)
						return ((StringExpression)obj).getLiteral().value().equals(aux);
					
					return false;
				}
			}, true);

			Vector<ASTNode> found = auxFinder.findNodes(query);
			if(!found.isEmpty())
			{
				Expression auxExpression = (Expression)found.get(0);
				auxExpression.getParentExpression().replaceSubexpr(auxExpression, expression);
				auxBuffer.remove(aux);
			}
		}
		
		return query;
	}
	
	/**
	 * Rewrites an {@link AssignExpression}.
	 * 
	 * @param query query containing assignment
	 * @param assignExpression found {@link AssignExpression} node
	 * @return rewritten query
	 * @throws Exception
	 */
	private Expression transformAssign(Expression query, AssignExpression assignExpression) throws SBQLException
	{
	    
		//replace non-wrapper names and expressions with auxiliary expressions
		ExternalExpressionExtractor analyzer = new ExternalExpressionExtractor();
		query.accept(analyzer, null);
		Hashtable<String, Expression> auxBuffer = analyzer.getAuxBuffer();
		
		//the method applies to a single wrapper query
		Wrapper wrapper = wrappers.elements().nextElement();
		Database model = wrapper.getModel();
		DBModule module = wrapper.getModule();
		String moduleGlobalName;
		try {
		    moduleGlobalName = module.getModuleGlobalName();
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, assignExpression, this);
		}
		
		ResultPattern resultPattern = Utils.analyzeSignatureForResultPattern(assignExpression.getSignature(), model, module);
		Expression assignedValue = assignExpression.getRightExpression();
		
		ZExpression where = null;
		WhereFinder whereFinder = new WhereFinder();
		for(ASTNode node : whereFinder.findNodes(query))
		{
			Expression target = Utils.analyzeWhereExpressionForTarget((WhereExpression)node);
			
			ResultPattern targetPattern = Utils.analyzeSignatureForResultPattern(target.getSignature(), model, module);
			odra.wrapper.model.Table table = model.getTable(Name.o2r(targetPattern.getTableName()));
			
			Vector<Expression> chainedWhereConditions = Utils.analyzeWhereExpressionForConditions(target);
			for(Expression chainedWhereCondition : chainedWhereConditions)
			{
				ZExpression subWhere = Utils.buildWhere(table, chainedWhereCondition, model, module);
				if(where == null)
					where = subWhere;
				else
					where = WhereBuilder.build(Operator.AND, where, subWhere);
			}
		}
		
		odra.wrapper.model.Table table = model.getTable(Name.o2r(resultPattern.getTableName()));
		QueryBuilder queryBuilder;
		String sql;
		
		//check count of updated records
		Table checkTable = new Table(table);
		checkTable.addAggregate(Table.ALL, Aggregate.COUNT);
		queryBuilder = new QueryBuilder(model);
		queryBuilder.addFrom(checkTable);
		queryBuilder.addSelect(checkTable);
		queryBuilder.setWhere(where);
		sql = queryBuilder.build(Type.SELECT).toString();
		ExecSqlExpression checkExecSqlExpression = new ExecSqlExpression(
			new StringExpression(new StringLiteral(sql)),
			new StringExpression(new StringLiteral("")),
			new StringExpression(new StringLiteral(moduleGlobalName)));
		EqualityExpression checkExpression = new EqualityExpression(
			checkExecSqlExpression,
			new IntegerExpression(new IntegerLiteral(1)),
			odra.sbql.ast.terminals.Operator.opEquals);
		checkExpression.setSignature(new ValueSignature(StdEnvironment.getStdEnvironment().booleanType));
		checkExecSqlExpression.setSignature(new ValueSignature(StdEnvironment.getStdEnvironment().integerType));
		
		//actual update SQL
		table = model.getTable(Name.o2r(resultPattern.getTableName()));
		queryBuilder = new QueryBuilder(model);
		queryBuilder.setImperativeTable(table);
		queryBuilder.addColumnValue(table.getColumn(Name.o2r(resultPattern.getColumnName())), Utils.resolveValue(assignedValue, model, module));
		queryBuilder.setWhere(where);
		sql = queryBuilder.build(Type.UPDATE).toString();
		ExecSqlExpression execSqlExpression = new ExecSqlExpression(
			new StringExpression(new StringLiteral(sql)),
			new StringExpression(new StringLiteral("")),
			new StringExpression(new StringLiteral(moduleGlobalName)));	

		//restore back replaced expressions separately for each subexpression
		//if the procedure is performed globally, some errors can occure due to the simple string search algorithm used
		IfThenExpression ifThenExpression = new IfThenExpression(
			restoreAuxExpressions(checkExpression, auxBuffer),
			restoreAuxExpressions(execSqlExpression, auxBuffer));
		//TODO some exception should be thrown if checkExpression fails...
		
		ifThenExpression.setSignature(new ValueSignature(StdEnvironment.getStdEnvironment().integerType));

		return ifThenExpression;
	}
	
	/**
	 * Rewrites an {@link AssignExpression}.
	 * 
	 * @param query query containing assignment
	 * @param expression found a {@link CreateExpression} node
	 * @param queryModule {@link DBModule} where the query is issued
	 * @return rewritten query
	 * @throws Exception
	 */
	private Expression transformCreate(Expression query, CreateExpression createExpression, DBModule queryModule) throws SBQLException
	{
		//replace non-wrapper names and expressions with auxiliary expressions
		ExternalExpressionExtractor analyzer = new ExternalExpressionExtractor();
		query.accept(analyzer, null);
		Hashtable<String, Expression> auxBuffer = analyzer.getAuxBuffer();
		
		DBModule module;
		String moduleGlobalName;
		Wrapper wrapper;
		Database model;
		try
		{
			int importNumber = createExpression.importModuleRef;
			if(importNumber == CreateExpression.CURRENT_MODULE)
				module = queryModule;
			else
				module = new DBModule(queryModule.getCompiledImportAt(createExpression.importModuleRef).derefReference());
			
			if(module.isWrapper())
				wrapper = module.getWrapper();
			else
				throw new OptimizationException("The create operator does not refer to a wrapper module");
			
			moduleGlobalName = module.getModuleGlobalName();
			model = wrapper.getModel();
		}
		catch(DatabaseException exc)
		{
			throw new OptimizationException(exc);
		}
		
		String tableName = createExpression.name().value();
		Hashtable<String, Object> params = Utils.analyzeCreateForValues(
			(DotExpression)query, 
			createExpression, 
			wrapper.getModel(), 
			module);

		odra.wrapper.model.Table table = model.getTable(Name.o2r(tableName));
		QueryBuilder queryBuilder = new QueryBuilder(model);
		queryBuilder.setImperativeTable(table);
		for(String columnName : params.keySet())
			queryBuilder.addColumnValue(table.getColumn(Name.o2r(columnName)), params.get(columnName));
		String sql = queryBuilder.build(Type.INSERT).toString();
		
		ExecSqlExpression execSqlExpression = new ExecSqlExpression(
			new StringExpression(new StringLiteral(sql)),
			new StringExpression(new StringLiteral("")),
			new StringExpression(new StringLiteral(moduleGlobalName)));
		return restoreAuxExpressions(execSqlExpression, auxBuffer);
	}
	
	/**
	 * Transforms all expressions in a query retrieved by the {@link ASTNodeFinder} argument.
	 * 
	 * @param query query to transform
	 * @param finder {@link ASTNodeFinder}
	 * @return transformed query
	 * @throws Exception
	 */
	private Expression transformExpression(Expression query, ASTNodeFinder finder) throws Exception
	{
		for(ASTNode node : finder.findNodes(query))
		{
			Expression subExpression = (Expression)node;
			
			Expression root = Utils.findRoot(query);
			if(root instanceof DotExpression && !(finder instanceof AggregateFinder))
				subExpression = root; 
			
			Expression expression = transformExpression(subExpression);
			expression.setSignature(subExpression.getSignature());
			
			if(subExpression.getParentExpression() != null)
				subExpression.getParentExpression().replaceSubexpr(subExpression, expression);
			else
				return expression;
		}
		
		return query;
	}
	
	/**
	 * Checks the result pattern for SBQL operators or constants. Such patterns cannot be currently 
	 * reconstrcted into query results and no wrapper optimization is possible.
	 * 
	 * @param resultPattern result pattern
	 * @return operators or constants found?
	 */
	private boolean validatePatternForOperatorOrConstant(ResultPattern resultPattern)
	{
		if(resultPattern instanceof ConstantPattern || resultPattern instanceof OperatorPattern)
			return false;
		else
			for(ResultPattern subPattern : resultPattern)
				if(!validatePatternForOperatorOrConstant(subPattern))
					return false;
		
		return true;
	}
	
	/**
	 * Transform a SQBL {@link Expression} into equivalent {@link ExecSqlExpression}.
	 * 
	 * @param expression expression to transform
	 * @return {@link ExecSqlExpression}
	 * @throws Exception
	 */
	private Expression transformExpression(Expression expression) throws Exception
	{
		//replace non-wrapper names and expressions with auxiliary expressions
		ExternalExpressionExtractor analyzer = new ExternalExpressionExtractor();
		expression.accept(analyzer, null);
		Hashtable<String, Expression> auxBuffer = analyzer.getAuxBuffer();
		
		//the method applies to a single wrapper query
		Wrapper wrapper = wrappers.elements().nextElement();
		Database model = wrapper.getModel();
		DBModule module = wrapper.getModule();
		
		ResultPattern sortPattern = null;
		OrderByFinder orderByFinder = new OrderByFinder();
		Vector<ASTNode> orderBys = orderByFinder.findNodes(expression);
		if(!orderBys.isEmpty())
		{
			OrderByExpression orderByExpression = (OrderByExpression)orderBys.firstElement();
			Expression sortExpression = orderByExpression.getRightExpression();
			sortPattern = Utils.analyzeSignatureForResultPattern(sortExpression.getSignature(), model, module);
		}
		
		ResultPattern resultPattern = Utils.analyzeSignatureForResultPattern(expression.getSignature(), model, module);
		if(!validatePatternForOperatorOrConstant(resultPattern))
			throw new WrapperException("Operators and constants are not supported in result patterns currently.", WrapperException.Error.RESULT_PATTERN);
		
		Aggregate aggregate = null;
		boolean isAggregate = new AggregatePattern().matches(expression);
		if(isAggregate)
			aggregate = Aggregate.getAggregateForExpression((UnaryExpression)expression);
		
		ZExpression where = null;
		WhereFinder whereFinder = new WhereFinder();
		Vector<ASTNode> wheres = whereFinder.findNodes(expression);
		for(ASTNode node : wheres)
		{
			Expression target = Utils.analyzeWhereExpressionForTarget((WhereExpression)node);
			
			ResultPattern targetPattern = Utils.analyzeSignatureForResultPattern(target.getSignature(), model, module);
			odra.wrapper.model.Table table = model.getTable(Name.o2r(targetPattern.getTableName()));
			
			Vector<Expression> chainedWhereConditions = Utils.analyzeWhereExpressionForConditions(target);
			for(Expression chainedWhereCondition : chainedWhereConditions)
			{
				ZExpression subWhere = Utils.buildWhere(table, chainedWhereCondition, model, module);
				if(where == null)
					where = subWhere;
				else
					where = WhereBuilder.build(Operator.AND, where, subWhere);
			}
		}
		
		QueryBuilder builder = new QueryBuilder(model);
		if(resultPattern.isEmpty())
		{
			Table table = new Table(model.getTable(Name.o2r(resultPattern.getTableName())));
			if(resultPattern.getColumnName() != null)
			{
				if(!isAggregate)
					table.addSelectedColumn(Name.o2r(resultPattern.getColumnName()));
				else
					table.addAggregate(Name.o2r(resultPattern.getColumnName()), aggregate);
			}
			else
			{
				if(!isAggregate)
					table.addSelectedColumn(Table.ALL);
				else
					table.addAggregate(Table.ALL, aggregate);
			}
			builder.addSelect(table);
			builder.addFrom(table);
		}
		else
			getTablesFromResultPattern(builder, isAggregate, aggregate, model, resultPattern);

		builder.setWhere(where);
		
		if(sortPattern != null)
		{
			if(sortPattern.getColumnName() != null)
			{
				odra.wrapper.model.Table table = model.getTable(Name.o2r(sortPattern.getTableName()));
				builder.addOrderBy(table.getColumn(Name.o2r(sortPattern.getColumnName())));
			}
			else
				for(ResultPattern subPattern : sortPattern)
				{
					odra.wrapper.model.Table table = model.getTable(Name.o2r(subPattern.getTableName()));
					builder.addOrderBy(table.getColumn(Name.o2r(subPattern.getColumnName())));
				}
		}
		
		if(where != null)
		{
			Vector<String> fromTables = Utils.getTablesFromWhere(where);
			for(String fromTable : fromTables)
				builder.addFrom(new Table(model.getTable(Name.o2r(fromTable))));
		}
				
		ExecSqlExpression execSqlExpression = new ExecSqlExpression(
			new StringExpression(new StringLiteral(builder.build(Type.SELECT).getQueryString())), 
			new StringExpression(new StringLiteral(resultPattern.toString())),
			new StringExpression(new StringLiteral(module.getModuleGlobalName())));
		
		//restore back replaced expressions
		return restoreAuxExpressions(execSqlExpression, auxBuffer);
	}
	
	/**
	 * Transform a SQBL {@link DeleteExpression} into equivalent {@link ExecSqlExpression}.
	 * 
	 * @param expression expression to transform
	 * @return {@link ExecSqlExpression}
	 * @throws Exception
	 */
	private Expression transformDelete(DeleteExpression expression) throws SBQLException
	{
		//replace non-wrapper names and expressions with auxiliary expressions
		ExternalExpressionExtractor analyzer = new ExternalExpressionExtractor();
		expression.accept(analyzer, null);
		Hashtable<String, Expression> auxBuffer = analyzer.getAuxBuffer();
		
		//the method applies to a single wrapper query
		Wrapper wrapper = wrappers.elements().nextElement();
		Database model = wrapper.getModel();
		DBModule module = wrapper.getModule();
		String moduleGlobalName;
		try {
		    moduleGlobalName = module.getModuleGlobalName();
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, expression, this);
		}
		
		ResultPattern resultPattern = Utils.analyzeSignatureForResultPattern(expression.getSignature(), model, module);
		if(!validatePatternForOperatorOrConstant(resultPattern))
			throw new WrapperException("Operators and constants are not supported in result patterns currently.", WrapperException.Error.RESULT_PATTERN);
						
		ZExpression where = null;
		WhereFinder whereFinder = new WhereFinder();
		Vector<ASTNode> wheres = whereFinder.findNodes(expression);
		for(ASTNode node : wheres)
		{
			Expression target = Utils.analyzeWhereExpressionForTarget((WhereExpression)node);
			
			ResultPattern targetPattern = Utils.analyzeSignatureForResultPattern(target.getSignature(), model, module);
			odra.wrapper.model.Table table = model.getTable(Name.o2r(targetPattern.getTableName()));
			
			Vector<Expression> chainedWhereConditions = Utils.analyzeWhereExpressionForConditions(target);
			for(Expression chainedWhereCondition : chainedWhereConditions)
			{
				ZExpression subWhere = Utils.buildWhere(table, chainedWhereCondition, model, module);
				if(where == null)
					where = subWhere;
				else
					where = WhereBuilder.build(Operator.AND, where, subWhere);
			}
		}
		
		QueryBuilder builder = new QueryBuilder(model);
		if(resultPattern.isEmpty())
		{
			Table table = new Table(model.getTable(Name.o2r(resultPattern.getTableName())));
			builder.setImperativeTable(table.getTable());
			if(resultPattern.getColumnName() != null)
				table.addSelectedColumn(Name.o2r(resultPattern.getColumnName()));
			else
				table.addSelectedColumn(Table.ALL);
			builder.addSelect(table);
			builder.addFrom(table);
		}
		else
			getTablesFromResultPattern(builder, false, null, model, resultPattern);

		builder.setWhere(where);
		
		if(where != null)
		{
			Vector<String> fromTables = Utils.getTablesFromWhere(where);
			for(String fromTable : fromTables)
				builder.addFrom(new Table(model.getTable(Name.o2r(fromTable))));
		}

		ExecSqlExpression execSqlExpression = new ExecSqlExpression(
			new StringExpression(new StringLiteral(builder.build(Type.DELETE).getQueryString())), 
			new StringExpression(new StringLiteral("")),
			new StringExpression(new StringLiteral(moduleGlobalName)));
		
		//restore back replaced expressions
		return restoreAuxExpressions(execSqlExpression, auxBuffer);
	}
	
	/**
	 * Recursively analyzes the result pattern for queried tables and adds them to the query builder provided.
	 * 
	 * @param builder query builder
	 * @param isAggregate is aggregate query?
	 * @param model relational database model
	 * @param resultPattern result pattern
	 * @return list of tables
	 * @throws WrapperException 
	 */
	private void getTablesFromResultPattern(QueryBuilder builder, boolean isAggregate, Aggregate aggregate, Database model, ResultPattern resultPattern) throws WrapperException
	{
		for(ResultPattern subPattern : resultPattern)
		{
			if(subPattern.getTableName() != null)
			{
				Table table = new Table(model.getTable(Name.o2r(subPattern.getTableName())));
				if(subPattern.getColumnName() != null)
				{
					if(!isAggregate)
						table.addSelectedColumn(Name.o2r(subPattern.getColumnName()));
					else
						table.addAggregate(Name.o2r(subPattern.getColumnName()), aggregate);
				}
				else
				{
					if(!isAggregate)
						table.addSelectedColumn(Table.ALL);
					else
						table.addAggregate(Table.ALL, aggregate);
				}
				builder.addSelect(table);
				builder.addFrom(table);
			}
			
			getTablesFromResultPattern(builder, isAggregate, aggregate, model, subPattern);
		}
	}
	
	/**
	 * Returns if a wrapper optimization can be applied to a query. 
	 * 
	 * @param query expression
	 * @return can be applied?
	 * @throws Exception 
	 */
	private boolean canMethodBeApplied(Expression query) throws SBQLException
	{
		if(wrappers.size() != 1)//reject if no wrappers or more than 1 wrapper
			return false;
		
		if(!new RangeExpressionFinder().findNodes(query).isEmpty())//find any RangeExpression, reject query if any exists
			throw new OptimizationException("RangeExpressions are not allowed in wrapper queries due to the dynamic character of data retrieved...");
		
		return true;
	}
}