package odra.sbql.optimizers.queryrewrite.index;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.indices.keytypes.KeyType;
import odra.db.indices.keytypes.KeyTypeKind;
import odra.db.indices.recordtypes.RecordType;
import odra.db.objects.meta.MBIndex;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.DateLiteral;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.terminals.RealLiteral;
import odra.sbql.ast.terminals.StringLiteral;

/** 
 * 
 * Class is responsible: 
 * <ul>
 * <li>for checking if given index query suits analized by IndexOptimizer query,</li> 
 * <li>for partial rewriting of analized query if given index is used,</li>  
 * <li>for informations about possible indexing to be passed to cost model
 * in order to apply the best index.</li>
 * </ul>
 * Rewriting is finished by IndexOptimizer.
 *
 * @author tkowals
 * @version 1.0
 */
public class SingleIndexFitter {
	
	private MBIndex mbidx;
	KeyValues[] keys;
	
	SingleIndexFitter(MBIndex mbidx, int numberOfConditionGroups) throws DatabaseException {
		this.mbidx = mbidx;
		keys = new KeyValues[mbidx.countKeys()];
		for(int i = 0; i < keys.length; i++)
			keys[i] = new KeyValues(mbidx.getRecordType(i), numberOfConditionGroups);			
	}

	Expression rewriteQuery(WhereExpression whexpr, IndexableSelectionPredicatesFinder ispfinder, boolean[] combination, Expression[] unnecessaryPredicates) throws Exception {
		Name idxname = new Name(mbidx.getName());
		NameExpression idxnameexpr = new NameExpression(idxname);

		ProcedureCallExpression idxcallexpr = new ProcedureCallExpression(idxnameexpr, new EmptyExpression());
		
		Expression runIdxExpr = null;
		
		for(KeyValues key : keys) {
			addParamTo(idxcallexpr, key.prepareParam(combination));
			runIdxExpr = IndexFitter.concatCondition(runIdxExpr, key.prepareRunCondition(combination));
		}
		
		Expression rewrittenWhere = idxcallexpr; 
		
		if (!mbidx.getAreNonkeysUnique()) 
			rewrittenWhere = new UniqueExpression(rewrittenWhere, true);
		
		if (runIdxExpr != null)
			rewrittenWhere = new IfThenExpression(runIdxExpr, rewrittenWhere);
		
		do {
			Expression selectionPredicates = filterPredicates(whexpr.getRightExpression(), ispfinder, combination, unnecessaryPredicates);
			
			if (selectionPredicates != null)
				rewrittenWhere = new WhereExpression(rewrittenWhere, selectionPredicates);
		
			if ((whexpr.getParentExpression() != null) && (whexpr.getParentExpression() instanceof WhereExpression)) 
				whexpr = (WhereExpression) whexpr.getParentExpression();
			else whexpr = null;
		} while (whexpr != null);
		
		return rewrittenWhere;
		
	}

	private Expression filterPredicates(Expression predicates, IndexableSelectionPredicatesFinder ispfinder, boolean[] combination, Expression[] unnecessaryPredicates) throws Exception {

		HashSet<ASTNode> filterExpressions = new HashSet<ASTNode>();
		
		for(KeyValues key: keys)
			for(Expression expr : key.getAllOpValuesCombination(combination)) {
				filterExpressions.add(expr.getParentExpression ());	
				ExistsExpression associatedExistsPredicate = ispfinder.getAssociatedExistsPredicate(expr.getParentExpression());
				if (associatedExistsPredicate != null)
					filterExpressions.add(associatedExistsPredicate);
			}
		for(Expression predicate : unnecessaryPredicates)
			filterExpressions.add(predicate);
		
		NodeFindingDeepCopyAST nfdcAST = new NodeFindingDeepCopyAST(filterExpressions);
		
		predicates = (Expression) nfdcAST.findandcopy(predicates);
		
		for(ASTNode node: nfdcAST.getCopiesSet()) {
			Expression expr = (Expression) node;
			if (expr.getParentExpression () != null) {
				SimpleBinaryExpression logexpr = ((SimpleBinaryExpression) expr.getParentExpression ());
				Expression subPredicate;
				if (expr == logexpr.getLeftExpression())
					subPredicate = logexpr.getRightExpression();
				else
					subPredicate = logexpr.getLeftExpression();

				if (logexpr.getParentExpression () == null) {
					predicates = subPredicate;
					predicates.setParentExpression( null );
				} else  
					logexpr.getParentExpression ().replaceSubexpr(logexpr, subPredicate);
						
			} else
				predicates = null;
		}
					
		return predicates;
	}

	private void addParamTo(ProcedureCallExpression idxcallexpr, Expression paramexpr) {
		if (idxcallexpr.getArgumentsExpression() instanceof EmptyExpression)
			idxcallexpr.replaceSubexpr(idxcallexpr.getArgumentsExpression(), paramexpr);
		else 
			idxcallexpr.replaceSubexpr(idxcallexpr.getArgumentsExpression(), new SequentialExpression(idxcallexpr.getArgumentsExpression(), paramexpr));	
		
	}

	boolean isUsable(boolean[] combination) {
	
		for(KeyValues key: keys)
			if (!key.isUsable(combination))
				return false;
		
		if (countSelective(combination) == 0)
			return false;
			
		return true;
	}

	int countSelective(boolean[] combination) {
		int count = 0;
		for(KeyValues key: keys)
			if (key.isSelective(combination))
				count++;
		return count;
	}

	/**
	 * @param combination predicates used for current index
	 * @return number of <b>equality</b> conditions used in index
	 */
	public int countEqualConditions(boolean[] combination) {
		int count = 0;
		for(KeyValues key: keys)
			count += key.countEqualValues(combination);
		return count;
	}
	
	public Vector<Integer> getEqualCondKeysCard(boolean[] combination) {
		Vector<Integer> card = new Vector<Integer>(); 
		for(KeyValues key: keys)
			card.addAll(key.getEqualCondKeyCard(combination));
		return card;
	}
	
	/**
	 * @param combination predicates used for current index
	 * @return number of <b>in</b> conditions used in index
	 */
	public int countInConditions(boolean[] combination) {
		int count = 0;
		for(KeyValues key: keys)
			count += key.countInValues(combination);
		return count;
	}
	
	public Vector<Integer> getInCondKeysCard(boolean[] combination) {
		Vector<Integer> card = new Vector<Integer>(); 
		for(KeyValues key: keys)
			card.addAll(key.getInCondKeyCard(combination));
		return card;
	}
	
	/**
	 * @param combination predicates used for current index
	 * @return number of <b>range</b> conditions used in index
	 */
	public int countRangeConditions(boolean[] combination) {
		int count = 0;
		for(KeyValues key: keys)
			count += key.countRangeValues(combination);
		return count;
	}
	
	/**
	 * @param combination predicates used for current index
	 * @return number of <b>range</b> conditions with lower and upper limit used in index
	 */
	public int countLimitedRangeConditions(boolean[] combination) {
		int count = 0;
		for(KeyValues key: keys)
			if ((key.isRange) && (key.isLimitedRange(combination)))
				count++;
		return count;
	}
	
	@SuppressWarnings("unchecked")
	class KeyValues {
		Vector<Expression>[][] opvalues;
		
		RecordType recordType;
		boolean isRange;
		boolean isObligatory;
		
		KeyValues(RecordType recordType, int numberOfConditionGroups){
			this.isRange = recordType.supportRangeQueries();
			this.isObligatory = recordType.isObligatory();
			this.recordType = recordType;
			
			opvalues = new Vector[numberOfConditionGroups][IndexFitter.OPERATORS_NUM];
			for(int cond_group = 0; cond_group < numberOfConditionGroups; cond_group++)
				for(int i = 0; i < IndexFitter.OPERATORS_NUM; i++)
					opvalues[cond_group][i] = new Vector<Expression>();

		}

		Vector<Expression> getAllOpValuesCombination(boolean[] combination) {
			Vector<Expression> resval = new Vector<Expression>();
			for(int i = 0; i < IndexFitter.OPERATORS_NUM; i++)
				resval.addAll(getOpValuesCombination(i, combination));
			return resval;
		}
		
		Vector<Expression> getOpValuesCombination(int op, boolean[] combination) {
			Vector<Expression> resval = new Vector<Expression>();
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					resval.addAll(opvalues[i][op]);
				}
			return resval;
		}
		
		Expression prepareParam(boolean[] combination) throws Exception {
			
			if (countEqualValues(combination) > 0) 
				return new GroupAsExpression(
						(Expression) DeepCopyAST.copy(getOpValuesCombination(IndexFitter.EQ_OP, combination).firstElement()),
						new Name(KeyType.EQUAL_KEY_LABEL));
			
			if (countInValues(combination) > 0) {
				Expression param = null;
				for(Expression expr: getOpValuesCombination(IndexFitter.IN_OP, combination))
					param = IndexFitter.intersectSubparams(param, (Expression) DeepCopyAST.copy(expr));
				//param = new UniqueExpression(param);
				if (countRangeValues(combination) > 0) {
					param = new AsExpression(param, new Name("$auxidx"));
					Expression condsexpr = null;
					for(int op = IndexFitter.RANGE_MIN_OP; op <= IndexFitter.RANGE_MAX_OP; op++) {
						Expression rcond = null;
						for(Expression expr: getOpValuesCombination(op, combination))
							rcond = IndexFitter.unionSubparams(rcond, (Expression) DeepCopyAST.copy(expr)); 
						rcond = IndexFitter.filterUnitedParams(op, rcond);
						if (rcond != null)
							condsexpr = IndexFitter.concatCondition(condsexpr, 
									new SimpleBinaryExpression(
											new NameExpression(new Name("$auxidx")),
											rcond, IndexFitter.ifop2Operator(op)));
					}
					param = new WhereExpression(param, condsexpr);
					param = new DotExpression(param, new NameExpression(new Name("$auxidx")));
				}
				return new GroupAsExpression(param, new Name(KeyType.IN_KEY_LABEL));
			}
			
			if (countRangeValues(combination) == 0)
				if (recordType.keyType.getKeyTypeID() == KeyTypeKind.BOOLEANKEYTYPE_ID)
					return new GroupAsExpression(IndexFitter.unionSubparams(
								new BooleanExpression(new BooleanLiteral(false)),
								new BooleanExpression(new BooleanLiteral(true)))
							, new Name(KeyType.IN_KEY_LABEL));
			
			Expression mincond = null;
			Expression minlimit = null;
			Expression grexpr = null;
			Expression greqexpr = null;
			for(Expression expr: getOpValuesCombination(IndexFitter.GR_OP, combination))
				grexpr = IndexFitter.unionSubparams(grexpr, (Expression) DeepCopyAST.copy(expr)); 
			for(Expression expr: getOpValuesCombination(IndexFitter.GREQ_OP, combination))
				greqexpr = IndexFitter.unionSubparams(greqexpr, (Expression) DeepCopyAST.copy(expr));
						
			mincond = IndexFitter.unionSubparams(grexpr, greqexpr);
			mincond = IndexFitter.filterUnitedParams(IndexFitter.GR_OP, mincond);
						
			if (mincond == null)
				mincond = getKeyMinLiteral();

			grexpr = IndexFitter.filterUnitedParams(IndexFitter.GR_OP, grexpr);
			greqexpr = IndexFitter.filterUnitedParams(IndexFitter.GREQ_OP, greqexpr);
			if (grexpr == null)
				minlimit = new BooleanExpression(new BooleanLiteral(true));
			else if (greqexpr == null)
				minlimit = new BooleanExpression(new BooleanLiteral(false));
			else 
				minlimit = new SimpleBinaryExpression(grexpr, greqexpr, Operator.opLower);

			Expression maxcond = null;
			Expression maxlimit = null;
			Expression lwexpr = null;
			Expression lweqexpr = null;
			for(Expression expr: getOpValuesCombination(IndexFitter.LW_OP, combination))
				lwexpr = IndexFitter.unionSubparams(lwexpr, (Expression) DeepCopyAST.copy(expr)); 
			for(Expression expr: getOpValuesCombination(IndexFitter.LWEQ_OP, combination))
				lweqexpr = IndexFitter.unionSubparams(lweqexpr, (Expression) DeepCopyAST.copy(expr));
						
			maxcond = IndexFitter.unionSubparams(lwexpr, lweqexpr);
			maxcond = IndexFitter.filterUnitedParams(IndexFitter.LW_OP, maxcond);
						
			if (maxcond == null)
				maxcond = getKeyMaxLiteral();

			lwexpr = IndexFitter.filterUnitedParams(IndexFitter.LW_OP, lwexpr);
			lweqexpr = IndexFitter.filterUnitedParams(IndexFitter.LWEQ_OP, lweqexpr);
			if (lwexpr == null)
				maxlimit = new BooleanExpression(new BooleanLiteral(true));
			else if (lweqexpr == null)
				maxlimit = new BooleanExpression(new BooleanLiteral(false));
			else 
				maxlimit = new SimpleBinaryExpression(lwexpr, lweqexpr, Operator.opGreater);
			
			Expression sparamsexpr = IndexFitter.concatSubparams(mincond, maxcond);
			sparamsexpr = IndexFitter.concatSubparams(sparamsexpr, minlimit);
			sparamsexpr = IndexFitter.concatSubparams(sparamsexpr, maxlimit);
			
			return new GroupAsExpression(sparamsexpr,
					new Name(KeyType.RANGE_KEY_LABEL));
		}

		Expression prepareRunCondition(boolean[] combination) throws Exception {
			Expression condsexpr = null;
			
			if (countEqualValues(combination) > 0) {
				if (countRangeValues(combination) > 0) {				
					for(int op = IndexFitter.RANGE_MIN_OP; op <= IndexFitter.RANGE_MAX_OP; op++) {
						Expression rcond = null;
						for(Expression expr: getOpValuesCombination(op, combination))
							rcond = IndexFitter.unionSubparams(rcond, (Expression) DeepCopyAST.copy(expr)); 
						rcond = IndexFitter.filterUnitedParams(op, rcond);
						if (rcond != null)
							condsexpr = IndexFitter.concatCondition(condsexpr, 
									new SimpleBinaryExpression(
											(Expression) DeepCopyAST.copy(getOpValuesCombination(IndexFitter.EQ_OP, combination).firstElement()),
											rcond, IndexFitter.ifop2Operator(op)));
					}	
				}
				if (countInValues(combination) > 0) {
					Expression rcond = null;
					for(Expression expr: getOpValuesCombination(IndexFitter.IN_OP, combination))
						rcond = IndexFitter.intersectSubparams(rcond, (Expression) DeepCopyAST.copy(expr)); 
					if (rcond != null)
						condsexpr = IndexFitter.concatCondition(condsexpr, 
								new InExpression(
										(Expression) DeepCopyAST.copy(getOpValuesCombination(IndexFitter.EQ_OP, combination).firstElement()), rcond));
				}			
				if (countEqualValues(combination) > 1) {
					for(int i = 1; i < countEqualValues(combination); i++)
						condsexpr = IndexFitter.concatCondition(condsexpr, 
								new SimpleBinaryExpression(
										(Expression) DeepCopyAST.copy(getOpValuesCombination(IndexFitter.EQ_OP, combination).firstElement()),
										(Expression) DeepCopyAST.copy(getOpValuesCombination(IndexFitter.EQ_OP, combination).elementAt(i)), Operator.opEquals));
				}	
			}
			
			return condsexpr;
		}
		
		boolean isUsable(boolean[] combination) {
			if (!isObligatory)
				return true;	
			return isSelective(combination);
		}
		
		private Expression getKeyMinLiteral() throws DatabaseException {
			switch (recordType.keyType.getKeyTypeID()) {
			//case (KeyTypeKind.BOOLEANKEYTYPE_ID): return new BooleanExpression(new BooleanLiteral(false));
			case (KeyTypeKind.DOUBLEKEYTYPE_ID): return new RealExpression(new RealLiteral(Double.MIN_VALUE));
			case (KeyTypeKind.INTEGERKEYTYPE_ID): return new IntegerExpression(new IntegerLiteral(Integer.MIN_VALUE));		
			// TODO : Resolve minimal value for string (maybe use empty string ""???!!!
			case (KeyTypeKind.STRINGKEYTYPE_ID): return new StringExpression(new StringLiteral((String) recordType.getMin()));
			case (KeyTypeKind.DATEKEYTYPE_ID): return new DateExpression(new DateLiteral((Date) recordType.getMin()));
			default:
				assert false: "unimplemented Minimal literal";
			}
			return null;
		}
		
		private Expression getKeyMaxLiteral() throws DatabaseException {
			switch (recordType.keyType.getKeyTypeID()) {
			//case (KeyTypeKind.BOOLEANKEYTYPE_ID): return new BooleanExpression(new BooleanLiteral(true));
			case (KeyTypeKind.DOUBLEKEYTYPE_ID): return new RealExpression(new RealLiteral(Double.MAX_VALUE));
			case (KeyTypeKind.INTEGERKEYTYPE_ID): return new IntegerExpression(new IntegerLiteral(Integer.MAX_VALUE));		
			// TODO : Resolve maximum value for string (maybe use empty string ""???)!!!
			case (KeyTypeKind.STRINGKEYTYPE_ID): return new StringExpression(new StringLiteral((String) recordType.getMax()));
			case (KeyTypeKind.DATEKEYTYPE_ID): return new DateExpression(new DateLiteral((Date) recordType.getMax()));			
			default:
				assert false: "unimplemented Maximal literal";
			}
			return null;
		}
		
		boolean isSelective(boolean[] combination) {
			if (countEqualValues(combination) > 0)
				return true;
			if (countInValues(combination) > 0)
				return true;
			if ((isRange) && (countRangeValues(combination) > 0))
				return true;
			return false;
		}


		int countEqualValues(boolean[] combination) {
			int count = 0;			
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					count += opvalues[i][IndexFitter.EQ_OP].size();  				
				}
			return count;
		}
		
		Vector<Integer> getEqualCondKeyCard(boolean[] combination) {
			Vector<Integer> card = new Vector<Integer>(); 
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					for (int j = 0; j < opvalues[i][IndexFitter.EQ_OP].size(); j++) 
						card.add(recordType.valuesCardinality());
				}
			return card;
		}
		
		int countInValues(boolean[] combination) {
			int count = 0;			
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					count += opvalues[i][IndexFitter.IN_OP].size();  				
				}
			return count;
		}

		Vector<Integer> getInCondKeyCard(boolean[] combination) {
			Vector<Integer> card = new Vector<Integer>(); 
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					for (int j = 0; j < opvalues[i][IndexFitter.IN_OP].size(); j++) 
						card.add(recordType.valuesCardinality());
				}
			return card;
		}
		
		int countRangeValues(boolean[] combination) {
			int count = 0;			
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					count += opvalues[i][IndexFitter.LW_OP].size() 
							+ opvalues[i][IndexFitter.LWEQ_OP].size()
							+ opvalues[i][IndexFitter.GR_OP].size() 
							+ opvalues[i][IndexFitter.GREQ_OP].size(); 				
				}
			return count;
		}
		
		boolean isLimitedRange(boolean[] combination) {
			int countLeft = 0;
			int countRight = 0;
			for(int i = 0; i < combination.length; i++) 
				if (combination[i]) {
					countLeft += opvalues[i][IndexFitter.LW_OP].size() 
							+ opvalues[i][IndexFitter.LWEQ_OP].size();
					countRight += opvalues[i][IndexFitter.GR_OP].size() 
							+ opvalues[i][IndexFitter.GREQ_OP].size(); 				
				}
			
			return (countLeft > 0) && (countRight > 0);
		}
		
	}
	
}
