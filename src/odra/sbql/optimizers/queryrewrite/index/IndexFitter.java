package odra.sbql.optimizers.queryrewrite.index;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.IndexManager;
import odra.db.indices.NonkeyIndexRegister;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.optimizers.costmodel.CostModel;

/** 
 * Class responsible for checking which indices from given group
 * suits analyzed by IndexOptimizer query
 * 
 * If more then one index suits than the best is chosen using Cost model module
 * 
 * The chosen index is partly rewritten in analyzed query. Rewriting is finished 
 * in IndexOptimizer.  
 * 
 * @author tkowals
 * @version 1.0
 */
class IndexFitter {

	SingleIndexFitter[] indices;
	IndexManager iman; 
	WhereExpression whexpr;
	IndexableSelectionPredicatesFinder ispfinder;
	
	boolean[] useOr = null;
	SelectionPredicatesDisjoiner spd;
	double indexingSelectivity = CostModel.INDEXING_THRESHOLD_SELECTIVITY;
	
	IndexFitter(WhereExpression whexpr, IndexableSelectionPredicatesFinder ispfinder, IndexManager iman, OID unkoid, SelectionPredicatesDisjoiner spd, int numberOfConditionGroups) throws DatabaseException {
		this.iman = iman;
		this.whexpr = whexpr;
		this.ispfinder = ispfinder;
		indices = new SingleIndexFitter[NonkeyIndexRegister.countUNKIndices(unkoid)];
		this.spd = spd;
		for(int i = 0; i < indices.length; i++) {
			indices[i] = new SingleIndexFitter(NonkeyIndexRegister.getUNKMBIndex(unkoid, i), numberOfConditionGroups);
		}
	}
	
	boolean fitCondition(OID ukeyoid, Expression keyexpr, Expression cvalexpr, int oper, int conditionGroup) throws DatabaseException {
		//TODO: replace checker with Independency and Dependency Checkers
		IndexASTChecker checker = new IndexASTChecker();
		checker.markIndexSubAST(keyexpr);
		checker.markIndexSubAST(cvalexpr);
		Expression nkexpr  = keyexpr;
		while (!(nkexpr instanceof WhereExpression))
			nkexpr = nkexpr.getParentExpression();
		nkexpr = ((WhereExpression) nkexpr).getLeftExpression();
		
/*		try {
			// TODO: analyze cases when check is necessary (if is necessary at all)
			checker.checkASTBoundTo(keyexpr, nkexpr);
		} catch (IndicesException ie) {
			return false;
		}*/
			
		if (!checker.isWholeASTNotBoundedTo(cvalexpr, nkexpr))
			return false;	

		
		if (oper == IN_EQ_OP)
			oper = EQ_OP;
		
		for(int i = 0; i < NonkeyIndexRegister.countUniqueKeyList(ukeyoid); i++) {
			indices[NonkeyIndexRegister.getIndexNr(ukeyoid, i)]
			        .keys[NonkeyIndexRegister.getKeyNr(ukeyoid, i)]
			              .opvalues[conditionGroup][oper].add(cvalexpr);			
		}
		
		return true;
		
	}

	boolean fitIndexing(int upToCondition) throws DatabaseException {		
		boolean found = false;

		for(boolean[] orCombination : spd.generateOrCombinations(upToCondition))	
			found |= fitIndexing(orCombination);
		
		return found;
	}

	private boolean fitIndexing(boolean[] orCombination) throws DatabaseException {
					
		for(boolean[] combination : spd.generateConditionCombinations(orCombination)) {
			boolean possible = false;
			for(SingleIndexFitter sidx: indices) {				
				if (sidx.isUsable(combination)) {
					possible = true;
					break;
				}
			}
			if (!possible) 
				return false;
		}

		double selectivity = 0;
		
		for(boolean[] combination : spd.generateConditionCombinations(orCombination)) {

			double combination_selectivity = 1;
			
			for(SingleIndexFitter sidx: indices) {
				if (sidx.isUsable(combination)) {
					double index_selectivity =
						CostModel.getCostModel().indexSelectivity(sidx, combination);
					if (combination_selectivity > index_selectivity)
						combination_selectivity = index_selectivity;
				}
			}	
			
			selectivity = CostModel.getCostModel().indicesAlternativeSelectivity(
					selectivity, combination_selectivity);
		
			if (selectivity >= indexingSelectivity)
				return false;
			
		}
				
		useOr = orCombination;
		indexingSelectivity = selectivity;
		
		return true;
	
	}
	
	
	public Expression applyIndexing() throws Exception {
		Expression rewrittenWhere = null;
		
		for(boolean[] combination : spd.generateConditionCombinations(useOr)) {

			Vector<SingleIndexFitter> fitidxs = new Vector<SingleIndexFitter>();
			
			for(SingleIndexFitter sidx: indices) {
				if (sidx.isUsable(combination))
					fitidxs.add(sidx);			
			}
			
			assert (fitidxs.size() > 0) : "IsIndexingPossible not checked";		
	
			int selidx = CostModel.getCostModel().indexSelector(fitidxs, combination);

			if (rewrittenWhere == null) 
				rewrittenWhere = fitidxs.elementAt(selidx).rewriteQuery(whexpr, ispfinder, combination, spd.getUnnecessaryPredicateASTs(combination, useOr));
			else 
				rewrittenWhere = new UnionExpression(rewrittenWhere, fitidxs.elementAt(selidx).rewriteQuery(whexpr, ispfinder, combination, spd.getUnnecessaryPredicateASTs(combination, useOr)));

		}	
 
		if (rewrittenWhere instanceof UnionExpression)
			rewrittenWhere = new UniqueExpression(rewrittenWhere, true);
		
		while  ((whexpr.getParentExpression() != null) && (whexpr.getParentExpression() instanceof WhereExpression)) 
			whexpr = (WhereExpression) whexpr.getParentExpression();
		
		if (whexpr.getParentExpression() != null)
			whexpr.getParentExpression().replaceSubexpr(whexpr, rewrittenWhere);
					
		return rewrittenWhere;
		
	}
	
	public static Expression skipDecoration(Expression expr) {
		if (expr instanceof ToSingleExpression)
			expr = ((ToSingleExpression) expr).getExpression();
		
		if (expr instanceof DerefExpression) 
			expr = ((DerefExpression) expr).getExpression();
		
		//TODO: coerce and maybe more?
		
		return expr;
	}

	public static Expression concatCondition(Expression condsexpr, Expression cexpr) {
		if (cexpr == null) 
			return condsexpr;
		if (condsexpr == null)
			return cexpr;
		return new SimpleBinaryExpression(cexpr, condsexpr, Operator.opAnd);		
	}
	
	public static Expression concatSubparams(Expression sparamsexpr, Expression param) {
		if (param == null)
			return sparamsexpr;
		if (sparamsexpr == null)
			return param; 
		return new CommaExpression(sparamsexpr, param);			
	}
	
	public static Expression unionSubparams(Expression sparamsexpr, Expression param) {
		if (param == null)
			return sparamsexpr;
		if (sparamsexpr == null)
			return param; 
		return new UnionExpression(sparamsexpr, param);			
	}
	
 	public static Expression filterSubParams(int op, Expression sparamsexpr) {
		if ((sparamsexpr != null) && (sparamsexpr instanceof CommaExpression))
			switch (op) {
			case GREQ_OP: 
			case GR_OP: return new MaxExpression(new BagExpression(sparamsexpr));
			case LWEQ_OP: 
			case LW_OP: return new MinExpression(new BagExpression(sparamsexpr));
			}
		return sparamsexpr;
	}

 	public static Expression filterUnitedParams(int op, Expression sparamsexpr) {
		if ((sparamsexpr != null) && (sparamsexpr instanceof UnionExpression))
			switch (op) {
			case GREQ_OP: 
			case GR_OP: return new MaxExpression(sparamsexpr);
			case LWEQ_OP: 
			case LW_OP: return new MinExpression(sparamsexpr);
			}
		return sparamsexpr;
	}
 	
	public static Expression intersectSubparams(Expression sparamsexpr, Expression param) {
		if (param == null)
			return sparamsexpr;
		if (sparamsexpr == null)
			return param; 
		return new IntersectExpression(sparamsexpr, param);			
	}
 	
	public static int revOperators(int op) {
		switch (op) {
		case GREQ_OP: return LWEQ_OP;
		case GR_OP: return LW_OP;
		case LWEQ_OP: return GREQ_OP;
		case LW_OP: return GR_OP;
		case IN_OP: return IN_EQ_OP;
		}
		return op;
	}

	public static int operator2IFOP(Operator O) {
		switch (O.getAsInt()) {
		case Operator.EQUALS: return IndexFitter.EQ_OP;
		case Operator.GREATEREQUALS: return IndexFitter.GREQ_OP;
		case Operator.GREATER: return IndexFitter.GR_OP;
		case Operator.LOWER: return IndexFitter.LW_OP;
		case Operator.LOWEREQUALS: return IndexFitter.LWEQ_OP;
		}
		return -1;
	}

	public static Operator ifop2Operator(int op) {
		switch (op) {
		case IndexFitter.EQ_OP: return Operator.opEquals;
		case IndexFitter.GREQ_OP: return Operator.opGreaterEquals;
		case IndexFitter.GR_OP: return Operator.opGreater;
		case IndexFitter.LW_OP: return Operator.opLower;
		case IndexFitter.LWEQ_OP: return Operator.opLowerEquals;
		}
		return null;
	}
	
	public static final int EQ_OP = 0;
	public static final int GREQ_OP = 1;
	public static final int GR_OP = 2;
	public static final int LW_OP = 3;
	public static final int LWEQ_OP = 4;
	public static final int IN_OP = 5;
	public static final int IN_EQ_OP = 10;
	
	public static final int RANGE_MIN_OP = GREQ_OP;
	public static final int RANGE_MAX_OP = LWEQ_OP;
	
	public static final int OPERATORS_NUM = 6;
	
}


