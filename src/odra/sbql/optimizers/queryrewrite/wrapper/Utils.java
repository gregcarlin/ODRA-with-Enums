package odra.sbql.optimizers.queryrewrite.wrapper;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MetaObjectKind;
import odra.filters.XML.XMLImportFilter;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToDateExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.OpCodes;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.ComplexConditionFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.NameExpressionFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.PrimitiveConditionFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.TableFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.finders.WhereFinder;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.wrapper.Wrapper;
import odra.wrapper.model.Column;
import odra.wrapper.model.Database;
import odra.wrapper.model.Name;
import odra.wrapper.resultpattern.ConstantPattern;
import odra.wrapper.resultpattern.OperatorPattern;
import odra.wrapper.resultpattern.ResultPattern;
import odra.wrapper.resultpattern.ResultPattern.Deref;
import odra.wrapper.resultpattern.ResultPattern.Type;
import odra.wrapper.sql.builder.Table;
import odra.wrapper.sql.builder.WhereBuilder;
import Zql.ZConstant;
import Zql.ZExpression;

/**
 * Wrapper optmizer utility class.
 * 
 * @author jacenty
 * @version 2008-01-27
 * @since 2007-02-19
 */
public class Utils
{
	/**
	 * Decomposes a distributive expression into a list of subexpressions.
	 * 
	 * @param expression binary expression to decompose
	 * @return list of subexpressions
	 */
	static Vector<Expression> decomposeBinaryExpression(BinaryExpression expression)
	{
		Vector<Expression> result = new Vector<Expression>();
		
		Expression left = expression.getLeftExpression();
		Expression right = expression.getRightExpression();
		
		if(!isDistributive(left))
			result.addElement(left);
		else
			for(Expression subexpression : decomposeBinaryExpression((BinaryExpression)left))
				result.addElement(subexpression);
		
		if(!isDistributive(right))
			result.addElement(right);
		else
			for(Expression subexpression : decomposeBinaryExpression((BinaryExpression)right))
				result.addElement(subexpression);
		
		return result;
	}
	
	/**
	 * Check if the expression is distributive (in terms of this pattern).
	 * 
	 * @param expression expression
	 * @return is distributive?
	 */
	static boolean isDistributive(Expression expression)
	{
		return 
			expression instanceof JoinExpression ||
			expression instanceof CommaExpression;
	}
	
	/**
	 * Analyzes a simple expression to find a table and a selected column (columns). 
	 * 
	 * @param wrappers wrappers referenced by a query
	 * @param expression expression
	 * @param fillColumns fill with selected columns?
	 * @return {@link Table}
	 * @throws Exception 
	 */
	static Table analyzeExpressionForTable(Hashtable<String, Wrapper> wrappers, Expression expression, boolean fillColumns) throws Exception
	{
		String tableName = null;
		String columnName = Table.ALL;
		if(expression instanceof NameExpression)
		{
			NameExpression nameExpression = (NameExpression)expression;
			tableName = nameExpression.name().value();
		}
		else if(expression instanceof DotExpression)
		{
			DotExpression dotExpression = (DotExpression)expression;
			Expression left = dotExpression.getLeftExpression();
			Expression right = dotExpression.getRightExpression();
			
			if(left instanceof NameExpression)
			{
				tableName = ((NameExpression)left).name().value();
				columnName = ((NameExpression)right).name().value();
			}
			else if(left instanceof DotExpression)
			{
				tableName = ((NameExpression)((DotExpression)left).getLeftExpression()).name().value();
				columnName = ((NameExpression)((DotExpression)left).getRightExpression()).name().value();
			}
		}
		else if(expression instanceof DerefExpression)
			return analyzeExpressionForTable(wrappers, ((DerefExpression)expression).getExpression(), fillColumns);

		if(tableName == null)
		{
			TableFinder tableFinder = new TableFinder(wrappers);
			expression.accept(tableFinder, null);
			if(tableFinder.getResult().size() == 1)
				tableName = ((NameExpression)tableFinder.getResult().firstElement()).name().value();
		}
		
		for(Wrapper wrapper : wrappers.values())
			if(wrapper.getModel().containsTable(Name.o2r(tableName)))
			{
				Table table = new Table(wrapper.getModel().getTable(Name.o2r(tableName)));
				if(fillColumns)
					table.addSelectedColumn(Name.o2r(columnName));
				
				return table;
			}
		
		return null;
	}
	
	/**
	 * Analyzes (a nested) {@link WhereExpression} and returns it most left subexpression 
	 * (preferably a {@link NameExpression}).
	 * 
	 * @param expression {@link WhereExpression}
	 * @return most left nested subexpression
	 */
	static Expression analyzeWhereExpressionForTarget(WhereExpression expression)
	{
		while(expression.getLeftExpression() instanceof WhereExpression)
			return analyzeWhereExpressionForTarget((WhereExpression)expression.getLeftExpression());
		
		return expression.getLeftExpression();
	}
	
	/**
	 * Analyzes (a nested) {@link WhereExpression} and returns a list of condition expressions.
	 * 
	 * @param target {@link Expression} being a left-hand-side subexpression of {@link WhereExpression}
	 * @return list of conditions
	 */
	static Vector<Expression> analyzeWhereExpressionForConditions(Expression target)
	{
		Vector<Expression> conditions = new Vector<Expression>();
		
		WhereExpression parent = (WhereExpression)target.getParentExpression();
		if(
				parent.getRightExpression() instanceof EqualityExpression || 				
				parent.getRightExpression() instanceof SimpleBinaryExpression)
			conditions.addElement(parent.getRightExpression());
		
		if(target.getParentExpression().getParentExpression() instanceof WhereExpression)
			for(Expression operand : analyzeWhereExpressionForConditions(target.getParentExpression()))
				conditions.addElement(operand);

		return conditions;
	}
	
	/**
	 * Analyzes a {@link DotExpression} (its right-hand-side expression) to retrieve a list 
	 * of column names. It is assumed that a left-hand-side expression "contais" a table name.
	 * 
	 * @param expression {@link DotExpression}
	 * @return list of columnNames
	 */
	static Vector<String> analyzeDotExpressionForColumnNames(DotExpression expression)
	{
		Vector<String> result = new Vector<String>();
		
		Expression right = expression.getRightExpression();
		if(right instanceof NameExpression)
			result.addElement(((NameExpression)right).name().value());
		else if(right instanceof DotExpression)
			result.addElement(((NameExpression)((DotExpression)right).getLeftExpression()).name().value());
		else if(right instanceof CommaExpression || right instanceof JoinExpression || right instanceof IntersectExpression)
		{
			Vector<Expression> subExpressions = Utils.decomposeBinaryExpression((BinaryExpression)right);
			for(Expression subExpression : subExpressions)
			{
				if(subExpression instanceof NameExpression)
					result.addElement(((NameExpression)subExpression).name().value());
				else if(subExpression instanceof DotExpression)
					result.addElement(((NameExpression)((DotExpression)subExpression).getLeftExpression()).name().value());
			}
		}
		
		return result;
	}
	
	/**
	 * Transforms SBQL {@link Expression} into {@link ZExpression} for building SQL queries.
	 * 
	 * @param expression condition to transform
	 * @param model relational database model
	 * @param module module
	 * @return {@link ZExpression}
	 * @throws Exception 
	 */
	static ZExpression transformCondition(Expression expression, Database model, DBModule module) throws SBQLException
	{
		Expression left = null;
		Expression right = null;
		Operator operator = null;
		if(expression instanceof SimpleBinaryExpression)
		{
			SimpleBinaryExpression simpleBinaryExpression = (SimpleBinaryExpression)expression;
			left = simpleBinaryExpression.getLeftExpression();
			right = simpleBinaryExpression.getRightExpression();
			operator = simpleBinaryExpression.O;
		}
		else if(expression instanceof EqualityExpression)
		{
			EqualityExpression equalityExpression = (EqualityExpression)expression;
			left = equalityExpression.getLeftExpression();
			right = equalityExpression.getRightExpression();
			operator = equalityExpression.O;
		}
		
		else
			assert false : "unsuported (yet) condition (" + expression + ")";
		
		if(left instanceof ToSingleExpression)
			left = ((ToSingleExpression)left).getExpression();
		if(right instanceof UnaryExpression)
			right = ((UnaryExpression)right).getExpression();
		
		WhereFinder whereFinder = new WhereFinder();
		Vector<ASTNode> internalWhereExpressioms = whereFinder.findNodes(left);
		ZExpression internalWhere = null;
		for(ASTNode node : internalWhereExpressioms)
		{
			WhereExpression internalWhereExpression = (WhereExpression)node;
			Expression internalTarget = analyzeWhereExpressionForTarget(internalWhereExpression);
			
			ResultPattern internalTargetPattern = Utils.analyzeSignatureForResultPattern(internalTarget.getSignature(), model, module);
			odra.wrapper.model.Table internalTable = model.getTable(Name.o2r(internalTargetPattern.getTableName()));
			
			Vector<Expression> internalChainedWhereConditions = Utils.analyzeWhereExpressionForConditions(internalTarget);
			for(Expression chainedWhereCondition : internalChainedWhereConditions)
			{
				ZExpression subWhere = Utils.buildWhere(internalTable, chainedWhereCondition, model, module);
				if(internalWhere == null)
					internalWhere = subWhere;
				else
					internalWhere = WhereBuilder.build(odra.wrapper.sql.builder.Operator.AND, internalWhere, subWhere);
			}
		}

		Object value = resolveValue(right, model, module);
		ResultPattern leftPattern = analyzeSignatureForResultPattern(left.getSignature(), model, module);
		Column column = model.getTable(Name.o2r(leftPattern.getTableName())).getColumn(Name.o2r(leftPattern.getColumnName()));
		
		ZExpression where = WhereBuilder.build(
			odra.wrapper.sql.builder.Operator.getOperator(operator), 
			column, 
			value);
		if(internalWhere != null)
			where = WhereBuilder.build(
				odra.wrapper.sql.builder.Operator.AND,
				where, 
				internalWhere);
		
		return where;
	}
	
	/**
	 * Returns a value involved in the expression given.
	 * 
	 * @param expression expression
	 * @param model relational database model
	 * @param module module
	 * @return value 
	 * @throws Exception 
	 */
	static Object resolveValue(Expression expression, Database model, DBModule module) throws SBQLException
	{
		if(expression instanceof ToStringExpression)
			return resolveValue(((ToStringExpression)expression).getExpression(), model, module);
		else if(expression instanceof ToIntegerExpression)
			return resolveValue(((ToIntegerExpression)expression).getExpression(), model, module);
		else if(expression instanceof ToDateExpression)
			return resolveValue(((ToDateExpression)expression).getExpression(), model, module);
		else if(expression instanceof ToBooleanExpression)
			return resolveValue(((ToBooleanExpression)expression).getExpression(), model, module);
		else if(expression instanceof ToRealExpression)
			return resolveValue(((ToRealExpression)expression).getExpression(), model, module);
		else if(expression instanceof ToDateExpression)
			return resolveValue(((ToDateExpression)expression).getExpression(), model, module);
		else if(expression instanceof StringExpression)
			return ((StringExpression)expression).getLiteral().value();
		else if(expression instanceof IntegerExpression)
			return ((IntegerExpression)expression).getLiteral().value();
		else if(expression instanceof BooleanExpression)
			return ((BooleanExpression)expression).getLiteral().value();
		else if(expression instanceof RealExpression)
			return ((RealExpression)expression).getLiteral().value();
		else if(expression instanceof DateExpression)
			return ((DateExpression)expression).getLiteral().value();
		else if(expression instanceof NameExpression)//auxiliary name?
			return resolveValue(expression.getSignature().getOwnerExpression().getSignature().getAssociatedExpression(), model, module);
		else if(expression instanceof DotExpression)
		{
			DotExpression dotExpression = (DotExpression)expression;
			if(dotExpression.getRightExpression() instanceof NameExpression)
			{
				if(((NameExpression)dotExpression.getRightExpression()).name().value().equals(XMLImportFilter.PCDATA))
				{
					ResultPattern pattern = analyzeSignatureForResultPattern(dotExpression.getLeftExpression().getSignature(), model, module);
					return model.getTable(Name.o2r(pattern.getTableName())).getColumn(Name.o2r(pattern.getColumnName()));
				}
			}
			else if(dotExpression.getRightExpression() instanceof DerefExpression)
			{
				DerefExpression derefExpression = (DerefExpression)dotExpression.getRightExpression();
				if(derefExpression.getExpression() instanceof NameExpression && ((NameExpression)derefExpression.getExpression()).name().value().equals(XMLImportFilter.PCDATA))
				{
					ResultPattern pattern = analyzeSignatureForResultPattern(dotExpression.getLeftExpression().getSignature(), model, module);
					return model.getTable(Name.o2r(pattern.getTableName())).getColumn(Name.o2r(pattern.getColumnName()));
				}
			}
		}
		else if(expression instanceof DerefExpression)
			return resolveValue(((DerefExpression)expression).getExpression(), model, module);
		else if(expression instanceof AsExpression)
			return resolveValue(((AsExpression)expression).getExpression(), model, module);

		throw new OptimizationException("Unsupported (yet) expression type (" + expression + ").");
	}

	/**
	 * Builds SQL where conditions as {@link ZExpression} basing on the expression provided. 
	 * 
	 * @param table table
	 * @param expression expression to rewrite
	 * @param model relational database model
	 * @param module module
	 * @return {@link ZExpression}
	 * @throws Exception
	 */
	static ZExpression buildWhere(odra.wrapper.model.Table table, Expression expression, Database model, DBModule module) throws SBQLException
	{
		PrimitiveConditionFinder primitiveConditionFinder = new PrimitiveConditionFinder();
		expression.accept(primitiveConditionFinder, null);
		if(primitiveConditionFinder.getResult().size() == 1)//expression is a primitive condition
			return Utils.transformCondition(expression, model, module);
		
		ComplexConditionFinder complexConditionFinder = new ComplexConditionFinder(expression);
		expression.accept(complexConditionFinder, null);
		odra.wrapper.sql.builder.Operator operator = odra.wrapper.sql.builder.Operator.getOperator(((SimpleBinaryExpression)expression).O);
		ZExpression left = buildWhere(table, (Expression)complexConditionFinder.getResult().get(0), model, module);
		ZExpression right = buildWhere(table, (Expression)complexConditionFinder.getResult().get(1), model, module);
		return WhereBuilder.build(operator, left, right);
	}
	
	/**
	 * Analyzes a signature to find table name - column name pair for a {@link ResultPattern} creation.
	 * 
	 * @param signature signature
	 * @param model relational database model
	 * @param module module
	 * @return {@link ResultPattern}
	 * @throws DatabaseException 
	 * @throws OptimizationException 
	 */
	public static ResultPattern analyzeSignatureForResultPattern(Signature signature, Database model, DBModule module) throws OptimizationException
	{
		String tableName = null;
		String columnName = null;
		String alias = null;
		
		try {
		    if(signature instanceof ReferenceSignature)
		    {
		    	OID meta = module.getMetabaseEntry();
		    	ReferenceSignature referenceSignature = (ReferenceSignature)signature;
		    	String name = new MBObject(referenceSignature.value).getName();

		    	if(name.equals(XMLImportFilter.PCDATA))
		    	{
		    		OID parent = referenceSignature.value.getParent().getParent();
		    		MBStruct struct = new MBStruct(parent);
		    		String structName = struct.getName();
		    		
		    		for(int i = 0; i < meta.countChildren(); i++)
		    			if(new MBObject(meta.getChildAt(i)).getObjectKind() == MetaObjectKind.VARIABLE_OBJECT)
		    			{
		    				MBVariable variable = new MBVariable(meta.getChildAt(i));
		    				OID fields = variable.getNestedMetabaseEntries()[0];
		    				for(int j = 0; j < fields.countChildren(); j++)
		    				{
		    					MBVariable fieldVariable = new MBVariable(fields.getChildAt(j));
		    					String fieldStructName = module.getMetaReferenceAt(fieldVariable.getTypeNameId()).derefString();

		    					if(fieldStructName.equals(structName))
		    					{
		    						columnName = fieldVariable.getName();
		    						tableName = variable.getName();
		    						break;
		    					}
		    				}
		    			}
		    	}
		    	else
		    	{
		    		OID parent = new MBObject(referenceSignature.value).getOID().getParent().getParent().getParent();
		    		
		    		if(parent.equals(module.getOID()))
		    			tableName = name;
		    		else if(parent.equals(module.getMetabaseEntry()))
		    		{
		    			parent = new MBObject(referenceSignature.value).getOID().getParent().getParent();
		    			
		    			MBStruct struct = new MBStruct(parent);
		    			String structName = struct.getName();
		    			
		    			for(int i = 0; i < meta.countChildren(); i++)
		    				if(new MBObject(meta.getChildAt(i)).getObjectKind() == MetaObjectKind.VARIABLE_OBJECT)
		    				{
		    					MBVariable variable = new MBVariable(meta.getChildAt(i));
		    					MBTypeDef typeDef = new MBTypeDef(variable.getType());
		    					String metaObjectName = module.getMetaReferenceAt(typeDef.getTypeNameId()).derefString();
		    
		    					if(metaObjectName.equals(structName))
		    					{
		    						tableName = variable.getName();
		    						columnName = name;
		    						break;
		    					}
		    				}
		    		}
		    		else
		    			throw new OptimizationException("unsupported (yet) result signature: " + signature.dump(""));
		    	}

		    	ResultPattern pattern = new ResultPattern(tableName, columnName, alias, Deref.NONE, Type.REF);
		    	return pattern;
		    }
		    else if(signature instanceof BinderSignature)
		    {
		    	BinderSignature binderSignature = (BinderSignature)signature;
		    	alias = binderSignature.name;
		    	Expression associator = signature.getAssociatedExpression();

		    	ResultPattern pattern = analyzeSignatureForResultPattern(associator.getSignature(), model, module);
		    	
		    	// Added by TK for Volatile Index
		    	//if (pattern.getType().equals(Type.BINDER) || pattern.getType().equals(Type.VIRTREF)) {
		    	if (pattern.getType().equals(Type.VIRTREF)) {
		    		ResultPattern subpattern = pattern;
		    		pattern = new ResultPattern();
		    		pattern.add(subpattern);
		    	}
		    	
		    	pattern.setAlias(alias);
		    	pattern.setType(Type.BINDER);
		    	
		    	// Added by TK for Volatile Index
		    	Signature subsig = signature.getOwnerExpression().getSubstitutedSignature();
		    	if (subsig != null)
					if (subsig instanceof ReferenceSignature) {
						ReferenceSignature refsig = (ReferenceSignature) subsig;
						if (refsig.isVirtual()) {
							ResultPattern subpattern = pattern;
							pattern = new ResultPattern();
				    		pattern.setAlias(new MBView(refsig.value).getVirtualObject().getObjectName());
				    		pattern.setType(Type.VIRTREF);
							pattern.add(subpattern);
						}				
					}
		    	
		    	return pattern;
		    }
		    else if(signature instanceof ValueSignature)
		    {
		    	Deref deref = Deref.getDerefForName(((ValueSignature)signature).value.getObjectName());
		    	
		    	Expression generator = signature.getOwnerExpression();
		    	if(generator instanceof DotExpression)
		    	{
		    		Expression left = ((DotExpression)generator).getLeftExpression();
		    		Expression right = ((DotExpression)generator).getRightExpression();
		    		if(left instanceof DotExpression)
		    		{
		    			ResultPattern pattern = analyzeSignatureForResultPattern(left.getSignature(), model, module);
		    			pattern.setDeref(deref);
		    			pattern.setType(Type.VALUE);
		    			return pattern;
		    		}
		    		else if(right instanceof DotExpression)
		    		{
		    			if(left instanceof NameExpression)
		    			{
		    				tableName = ((NameExpression)left).name().value();
		    				columnName = ((NameExpression)((DotExpression)right).getLeftExpression()).name().value();
		    				if(!model.containsTable(tableName))//some strange query form might happen
		    				{
		    					ResultPattern tempPattern = analyzeSignatureForResultPattern(left.getSignature(), model, module);
		    					tableName = tempPattern.getTableName();
		    					columnName = tempPattern.getColumnName();
		    				}
		    				
		    				ResultPattern pattern = new ResultPattern(tableName, columnName, alias, deref, Type.VALUE);
		    				return pattern;
		    			}
		    			else if(left instanceof AsExpression)
		    			{
		    				AsExpression asExpression = (AsExpression)left;
		    				ResultPattern pattern = analyzeSignatureForResultPattern(asExpression.getExpression().getSignature(), model, module);
		    				pattern.setAlias(asExpression.name().value());
		    				return pattern;
		    			}
		    			else
		    				return analyzeSignatureForResultPattern(right.getSignature(), model, module);
		    		}
		    		else if(right instanceof DerefExpression)
		    			return analyzeSignatureForResultPattern(right.getSignature(), model, module);
		    		else if(right instanceof SimpleBinaryExpression)
		    		{
		    			SimpleBinaryExpression binaryExpression = (SimpleBinaryExpression)right;
		    			left = binaryExpression.getLeftExpression();
		    			right = binaryExpression.getRightExpression();
		    			
		    			ResultPattern leftPattern = analyzeSignatureForResultPattern(left.getSignature(), model, module);
		    			ResultPattern rightPattern = analyzeSignatureForResultPattern(right.getSignature(), model, module);				
		    			OperatorPattern pattern = new OperatorPattern(binaryExpression.O, leftPattern, rightPattern);
		    			
		    			return pattern;
		    		}
		    		else if(
		    				right instanceof CountExpression ||
		    				right instanceof AvgExpression ||
		    				right instanceof MinExpression ||
		    				right instanceof MaxExpression ||
		    				right instanceof SumExpression)
		    		{
		    			return analyzeSignatureForResultPattern(right.getSignature(), model, module);
		    		}
		    		else
		    			throw new OptimizationException("unsupported (yet) result signature generator (" + signature.getOwnerExpression() + ").");
		    	}
		    	else if(generator instanceof DerefExpression)
		    	{
		    		ResultPattern pattern = analyzeSignatureForResultPattern(((UnaryExpression)generator).getExpression().getSignature(), model, module);
		    		pattern.setDeref(deref);
		    		pattern.setType(Type.VALUE);
		    		return pattern;
		    	}
		    	else if(
		    			generator instanceof CountExpression ||
		    			generator instanceof AvgExpression ||
		    			generator instanceof MinExpression ||
		    			generator instanceof MaxExpression ||
		    			generator instanceof SumExpression)
		    	{
		    		//dereference is set to false - aggregates are calculated by SQL queries
		    		ResultPattern pattern = analyzeSignatureForResultPattern(((UnaryExpression)generator).getExpression().getSignature(), model, module);
		    		return pattern;
		    	}
		    	else if(generator instanceof SimpleBinaryExpression)
		    	{
		    		SimpleBinaryExpression binaryExpression = (SimpleBinaryExpression)generator;
		    		Expression left = binaryExpression.getLeftExpression();
		    		Expression right = binaryExpression.getRightExpression();
		    		
		    		ResultPattern leftPattern = analyzeSignatureForResultPattern(left.getSignature(), model, module);
		    		ResultPattern rightPattern = analyzeSignatureForResultPattern(right.getSignature(), model, module);				
		    		OperatorPattern pattern = new OperatorPattern(binaryExpression.O, leftPattern, rightPattern);
		    		
		    		return pattern;
		    	}
		    	else if(generator instanceof ToSingleExpression)
		    		return analyzeSignatureForResultPattern(((ToSingleExpression)generator).getExpression().getSignature(), model, module);
		    	else if(generator instanceof StringExpression)
		    		return new ConstantPattern((StringExpression)generator);
		    	else if(generator instanceof IntegerExpression)
		    		return new ConstantPattern((IntegerExpression)generator);
		    	else if(generator instanceof RealExpression)
		    		return new ConstantPattern((RealExpression)generator);
		    	else if(generator instanceof BooleanExpression)
		    		return new ConstantPattern((BooleanExpression)generator);
		    	else if(generator instanceof DateExpression)
		    		return new ConstantPattern((DateExpression)generator);
		    	else if(generator instanceof DateprecissionExpression)
		    	{
		    		try
		    		{
		    			return new ConstantPattern((DateprecissionExpression)generator);
		    		}
		    		catch (ParseException exc)
		    		{
		    			throw new OptimizationException(exc.getMessage());
		    		}
		    	}
		    	else
		    		throw new OptimizationException("unsupported (yet) result signature (" + signature.dump("") + ").");
		    }
		    else if(signature instanceof StructSignature)
		    {
		    	ResultPattern pattern = new ResultPattern();
		    	for(Signature subSignature : ((StructSignature)signature).getFields())
		    	{
		    		ResultPattern subPattern = analyzeSignatureForResultPattern(subSignature, model, module);
		    		pattern.addElement(subPattern);
		    	}
		    	pattern.setType(Type.STRUCT);
		    	return pattern;
		    }
		    else
		    	throw new OptimizationException("unsupported (yet) result signature (" + signature.dump("") + ").");
		} catch (DatabaseException e) {
		    throw new OptimizationException(e);
		}
	}
	
	/**
	 * Analyzes 'where' expression for table names recursively.
	 * 
	 * @param where {@link ZExpression} where condition
	 * @return list of table names
	 */
	static Vector<String> getTablesFromWhere(ZExpression where)
	{
		Vector<String> tableNames = new Vector<String>();
		
		Vector operands = where.getOperands();
		for(Object operand : operands)
		{
			if(operand instanceof ZExpression)
			{
				odra.wrapper.sql.builder.Operator operator = odra.wrapper.sql.builder.Operator.getOperator(((ZExpression)operand).getOperator());
				if(operator.equals(odra.wrapper.sql.builder.Operator.AND) || operator.equals(odra.wrapper.sql.builder.Operator.OR))
					for(String tableName : getTablesFromWhere((ZExpression)operand))
						tableNames.addElement(tableName);
				else
				{
					Vector<ZConstant> subOperands = ((ZExpression)operand).getOperands();
					for(ZConstant constant : subOperands)
						if(constant.getType() == ZConstant.COLUMNNAME)
							tableNames.addElement(constant.getValue().split("\\.")[0]);
				}
			}
			else if(operand instanceof ZConstant && ((ZConstant)operand).getType() == ZConstant.COLUMNNAME)
					tableNames.addElement(((ZConstant)operand).getValue().split("\\.")[0]);
		}
		
		return tableNames;
	}
	
	/**
	 * Analyzes a query and marks name expressions with wrapper identifiers 
	 * corresponding to their indices in a returned {@link Vector}.
	 * 
	 * @param query query
	 * @return hashtable of referenced wrappers
	 * @throws Exception 
	 */
	static Hashtable<String, Wrapper> markWrappers(Expression query) throws SBQLException
	{
		Hashtable<String, Wrapper> wrappers = new Hashtable<String, Wrapper>();
		
		for(ASTNode node : new NameExpressionFinder().findNodes(query))
		{
			NameExpression nameExpression = (NameExpression)node;
			
			if(nameExpression.getSignature() instanceof ReferenceSignature)
			{
				try {
				    MBObject object = new MBObject(((ReferenceSignature)nameExpression.getSignature()).value);
				    DBModule nameModule = object.getModule();
				    
				    if(nameModule.isWrapper())
				    {
				    	nameExpression.wrapper = nameModule.getModuleGlobalName();
				    	wrappers.put(nameModule.getModuleGlobalName(), nameModule.getWrapper());
				    }
				} catch (DatabaseException e) {
				    throw new OptimizationException(e);
				}
			}
		}

		return wrappers;
	}
	
	/**
	 * Returns if a query contains only relational names.
	 * 
	 * @param query query
	 * @return only relational names?
	 * @throws Exception 
	 */
	static boolean hasOnlyRelationalNames(Expression query) throws Exception
	{
		boolean onlyRelational = true;
		for(ASTNode node : new NameExpressionFinder().findNodes(query))
		if(((NameExpression)node).wrapper == null && !((NameExpression)node).isViewSubstituted)
		{
			onlyRelational = false;
			break;
		}
		
		return onlyRelational;
	}
	
	/**
	 * Returns if a query contains only non-relational names.
	 * 
	 * @param query query
	 * @return only non-relational names?
	 * @throws Exception 
	 */
	static boolean hasOnlyNonRelationalNames(Expression query) throws SBQLException
	{
		boolean onlyNonrelational = true;
		for(ASTNode node : new NameExpressionFinder().findNodes(query))
		{
			NameExpression nameExpression = (NameExpression)node;
			if(nameExpression.wrapper != null)
			{
				onlyNonrelational = false;
				break;
			}
		}
		
		return onlyNonrelational;
	}
	
	/**
	 * Traverses the tree upwards and returns its root (the whole query).
	 * 
	 * @param expression some expression in the query
	 * @return root expression
	 */
	static Expression findRoot(Expression expression)
	{
		Expression root = expression;
		while(root.getParentExpression() != null)
			root = root.getParentExpression();
		
		return root;
	}
	
	/**
	 * Analyses a {@link CreateExpression} for column values.
	 * 
	 * @param query the whole query as a {@link DotExpression}
	 * @param createExpression {@link CreateExpression}
	 * @param model {@link Database}
	 * @param module {@link DBModule}
	 * @return {@link Hashtable} column name : value
	 */
	static Hashtable<String, Object> analyzeCreateForValues(DotExpression query, CreateExpression createExpression, Database model, DBModule module)
	{
		Hashtable<String, Object> values = new Hashtable<String, Object>();
		
		Vector<Expression> columnExpressions = new Vector<Expression>();
		if(createExpression.getExpression() instanceof CommaExpression)//multiple column insert
			columnExpressions = decomposeBinaryExpression((CommaExpression)createExpression.getExpression());
		else//single column insert
			columnExpressions.addElement(createExpression.getExpression());

		for(Expression expression : columnExpressions)
		{
			AsExpression columnExpression = (AsExpression)expression;
			String columnName = columnExpression.name().value();
			
			DotExpression valueExpression = (DotExpression)((AsExpression)columnExpression.getExpression()).getExpression();
			AsExpression associator = (AsExpression)valueExpression.getRightExpression().getSignature().getAssociatedExpression();
			
			values.put(columnName, digestForValue(associator, model, module).toString());
		}
		
		return values;
	}
	
	/**
	 * Digests deeply for an inserted value.
	 * 
	 * @param asExpression {@link AsExpression} involving arbitrarily deeply an inserted value
	 * @param model {@link Database}
	 * @param module {@link DBModule}
	 * @return
	 */
	private static Object digestForValue(AsExpression asExpression, Database model, DBModule module)
	{
		try
		{
			return resolveValue(asExpression.getExpression(), model, module);
		}
		catch(Exception exc)
		{
			DotExpression valueExpression = (DotExpression)asExpression.getExpression();
			AsExpression associator = (AsExpression)valueExpression.getRightExpression().getSignature().getAssociatedExpression();
			
			return digestForValue(associator, model, module);
		}
	}
}