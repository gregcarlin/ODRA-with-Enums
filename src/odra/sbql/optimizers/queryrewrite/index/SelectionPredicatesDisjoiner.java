package odra.sbql.optimizers.queryrewrite.index;

import java.util.Vector;

import odra.sbql.ast.expressions.Expression;

/**
 * This class is responsible for finding all possible combinations
 * to decompose selection predicates joined with <b>or</b> logical operator 
 * to build seperate queries.<br>
 * Decomposition can take in an account all or some <b>or</b> operators.
 *
 * @author tkowals
 * @version 1.0
 */
class SelectionPredicatesDisjoiner {
 
	int predicatesProcessed;
	
	int numberOfOrExpressions;		
	int[] parentPredicates;
	
	Expression[] predicateASTs;
	
	SelectionPredicatesDisjoiner(int numberOfOrExpressions) {
		parentPredicates = new int[1 + 2 * numberOfOrExpressions];
		this.numberOfOrExpressions = numberOfOrExpressions;
		predicateASTs = new Expression[1 + 2 * numberOfOrExpressions];
	}
	
	void addRootPredicate(Expression predicateAST) {
		predicateASTs[0] = predicateAST;
		parentPredicates[0] = 0;
		predicatesProcessed = 1;
	}
	
	void addOrPredicate(Expression leftPredicateAST, Expression rightPredicateAST, int parentPredicate) {
		predicateASTs[predicatesProcessed] = leftPredicateAST;
		parentPredicates[predicatesProcessed++] = parentPredicate;
		predicateASTs[predicatesProcessed] = rightPredicateAST;
		parentPredicates[predicatesProcessed++] = parentPredicate;
	}

	int predicatesCount() {
		return predicatesProcessed;
	}
	
	Expression getPredicateAST(int condition_num) {
		return predicateASTs[condition_num];
	}
	
	Expression[] getUnnecessaryPredicateASTs(boolean[] combination, boolean[] useOr) {
		Vector<Expression> unnecessaryPredicateASTs = new Vector<Expression>();
		
		for(int i = 0; i < combination.length; i++)
			if ((combination[i] == false) && (useOr[(i - 1) / 2] == true))
				unnecessaryPredicateASTs.add(predicateASTs[i]);
		
		return unnecessaryPredicateASTs.toArray(new Expression[0]);
	}
	
	Vector<boolean[]> generateOrCombinations(int upToCondition) {
		if (upToCondition == 0) {
			Vector<boolean[]> result = new Vector<boolean[]>();
			result.add(new boolean[] {});
			return result;
		}
		int upToOr = (upToCondition) / 2;
		boolean[] combination = new boolean[upToOr];
		setOr(upToOr - 1, combination);
		return generateOrCombinations(upToOr - 1, combination);
	}
	
	Vector<boolean[]> generateOrCombinations(int upToOr, boolean[] initUseOr) {
		Vector<boolean[]> combinationSet = new Vector<boolean[]>();
		if ((upToOr == 0) || (initUseOr[upToOr - 1] == false)) 
			combinationSet.add(initUseOr.clone());		
		if (upToOr == 0)  
			return combinationSet;	
		boolean[] combination = initUseOr.clone();
		setOr(upToOr - 1, combination);
		combinationSet.add(combination);
		Vector<boolean[]> resultSet = new Vector<boolean[]>();
		for (boolean[] useOr : combinationSet)
			resultSet.addAll(generateOrCombinations(upToOr - 1, useOr));
		return resultSet;
	}
	
	void setOr(int or_num, boolean[] useOr) {
		useOr[or_num] = true;
		int parentcond_num = 1 + 2 * or_num;
		while ((parentcond_num = parentPredicates[parentcond_num]) != 0) 
			useOr[(parentcond_num - 1) / 2] = true; 
	}
	
	Vector<boolean[]> generateConditionCombinations(boolean[] useOr) {
		Vector<boolean[]> combinationSet = new Vector<boolean[]>();
		
		boolean[] combination = new boolean[1 + 2 * useOr.length];
		combination[0] = true;
		
		combinationSet.add(combination);
		
		for(int i_or = 0; i_or < useOr.length; i_or++) {
			if (useOr[i_or]) {
				Vector<boolean[]> prevCombinationSet = combinationSet;
				combinationSet = new Vector<boolean[]>();
				for(int j = 0; j < 2; j++) {
					for(boolean[] prevCombination : prevCombinationSet) {
						combination = prevCombination.clone();
						if (combination[parentPredicates[1 + 2 * i_or + j]]) {
							combination[1 + 2 * i_or + j] = true;
							combinationSet.add(combination);
						}
					}
				}			
			}
		}

		return combinationSet;
	}
	
}
