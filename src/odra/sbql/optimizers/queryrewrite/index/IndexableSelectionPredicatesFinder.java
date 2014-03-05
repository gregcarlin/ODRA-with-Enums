package odra.sbql.optimizers.queryrewrite.index;

import java.util.HashMap;
import java.util.Vector;

import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.patterns.ASTNodePattern;
import odra.sbql.ast.utils.patterns.SimpleBinaryPattern;

class IndexableSelectionPredicatesFinder {

	private final SimpleBinaryPattern andPattern = new SimpleBinaryPattern(Operator.opAnd);
	
	private HashMap<String, ExistsExpression> exists = new HashMap<String, ExistsExpression>();
	
	private Expression predicatesRoot;
	private HashMap<Expression, Vector<Expression>> subPredicatesGroup = new HashMap<Expression, Vector<Expression>>(); 
	private Expression currentRoot;
	private Vector<SimpleBinaryExpression> orPredicates = new Vector<SimpleBinaryExpression>(); 

	private Vector<ExistsExpression> currentExists = new Vector<ExistsExpression>();

	private HashMap<Expression, ExistsExpression> associatedExistsPredicates = new HashMap<Expression, ExistsExpression>();
	
	public IndexableSelectionPredicatesFinder(WhereExpression whexpr) {
		predicatesRoot = whexpr.getRightExpression();
		currentRoot = predicatesRoot;
		subPredicatesGroup.put(predicatesRoot, new Vector<Expression>());
		visitWherePredicates(whexpr);
	}
		
	public Vector<Expression> getPredicatesGroup(Expression rootexpr) {
		return subPredicatesGroup.get(rootexpr);
	}

	public int getOrCount() {
		return orPredicates.size();
	}
	
	public ExistsExpression getAssociatedExistsPredicate(Expression predicate) {
		return associatedExistsPredicates.get(predicate);
	}
	
	/**
	 * Performs traversing through series predicates joined with where expression.
	 * @param expr expression containing selection predicates
	 * @return boolean indicating if evaluation of this query is not obligatory for all objects (enables conjunction commutativity)
	 */
	private void visitWherePredicates(WhereExpression whexpr) {
		currentExists.removeAllElements();
		
		Vector<Expression> rootPredicates = subPredicatesGroup.get(predicatesRoot);
		int beginRootPredicatesCount = rootPredicates.size();
		int beginOrPredicatesCount = orPredicates.size();
		
		if (!visitPredicates(whexpr.getRightExpression())) {
			while (rootPredicates.size() > beginRootPredicatesCount)
				rootPredicates.removeElementAt(rootPredicates.size() - 1);
			while (orPredicates.size() > beginOrPredicatesCount) {
				subPredicatesGroup.remove(orPredicates.lastElement().getLeftExpression());
				subPredicatesGroup.remove(orPredicates.lastElement().getRightExpression());
				orPredicates.removeElementAt(orPredicates.size() - 1);			
			}			
			return;
		}
		
		for(ExistsExpression exexpr: currentExists) {
			try {
				String existAST = AST2TextQueryDumper.AST2Text(exexpr.getExpression());
				if (!exists.containsKey(existAST))
					exists.put(existAST, exexpr);
			} catch (Exception E) {
				assert false : "shouldn't be here";
			}
		}
		
		if (whexpr.getParentExpression() instanceof WhereExpression)
			visitWherePredicates((WhereExpression) whexpr.getParentExpression());

		
	}
	
	/**
	 * Performs traversing through AND and OR predicates.
	 * @param expr expression containing selection predicates
	 * @return boolean indicating if evaluation of this query is not obligatory for all objects (enables conjunction commutativity)
	 */
	protected boolean visitPredicates(Expression expr) {
		if (expr instanceof SimpleBinaryExpression) {
			SimpleBinaryExpression binexpr = (SimpleBinaryExpression) expr;
			switch (binexpr.O.getAsInt()) {
			case (Operator.AND):
				return visitConjunctionPredicate(binexpr);
			case (Operator.OR):
				return visitDisjunctionPredicate(binexpr);
			case (Operator.EQUALS):
			case (Operator.LOWER):
			case (Operator.LOWEREQUALS):
			case (Operator.GREATER):
			case (Operator.GREATEREQUALS):
				if (!isCardinalitySingular(binexpr.getLeftExpression()))
					return false;
				if (!isCardinalitySingular(binexpr.getRightExpression()))
					return false;
				if (!(isCommutativeConjunctionSelectionPredicate(binexpr.getLeftExpression()) && isCommutativeConjunctionSelectionPredicate(binexpr.getRightExpression())))
					return false;
				addPredicateIfDeteministic(expr);
			default:
				if (!isCommutativeConjunctionSelectionPredicate(expr))
					return false;
			}
		} else if (expr instanceof InExpression) {
			InExpression inexpr = (InExpression) expr;
			if (!isCardinalitySingular(inexpr.getLeftExpression()))
				return false;
			if (!(isCommutativeConjunctionSelectionPredicate(inexpr.getLeftExpression()) && isCommutativeConjunctionSelectionPredicate(inexpr.getRightExpression())))
				return false;
			addPredicateIfDeteministic(expr);
		} else if (expr instanceof EqualityExpression) { 
			EqualityExpression eqexpr = (EqualityExpression) expr;
			if (!isCardinalitySingular(eqexpr.getLeftExpression()))
				return false;
			if (!isCardinalitySingular(eqexpr.getRightExpression()))
				return false;
			if (!(isCommutativeConjunctionSelectionPredicate(eqexpr.getLeftExpression()) && isCommutativeConjunctionSelectionPredicate(eqexpr.getRightExpression())))
				return false;
			addPredicateIfDeteministic(expr);
		} else if (expr instanceof ExistsExpression) {
			visitExistsPredicate((ExistsExpression) expr);
			if (!isCommutativeConjunctionSelectionPredicate(expr))
				return false;
			addPredicateIfDeteministic(expr);
		} else if (!isCommutativeConjunctionSelectionPredicate(expr))
			return false;
		return true;
	}

	protected boolean visitConjunctionPredicate(SimpleBinaryExpression andexpr) {
		assert andexpr.O.equals(Operator.opAnd) : "and operator expected.";
		boolean runSafe = true;
		runSafe = runSafe && visitPredicates(andexpr.getLeftExpression());
		if (runSafe) runSafe = runSafe && visitPredicates(andexpr.getRightExpression());
		return runSafe;
	}	
	
	protected boolean visitDisjunctionPredicate(SimpleBinaryExpression orexpr) {
		assert orexpr.O.equals(Operator.opOr) : "or operator expected.";

		Expression prevRoot = currentRoot;
		orPredicates.add(orexpr);
		subPredicatesGroup.put(orexpr.getLeftExpression(), new Vector<Expression>());
		subPredicatesGroup.put(orexpr.getRightExpression(), new Vector<Expression>());
		int beginOrPredicatesCount = orPredicates.size();
		
		currentRoot = orexpr.getLeftExpression();
		boolean runSafe = true;
		boolean foundIndexablePredicates = true;
		runSafe = runSafe && visitPredicates(currentRoot);
		int orPredicatesCount = orPredicates.size();
		foundIndexablePredicates = foundIndexablePredicates && (((subPredicatesGroup.get(currentRoot).size() > 0) || (orPredicatesCount > beginOrPredicatesCount))); 

		if (runSafe) { 
			currentRoot = orexpr.getRightExpression();
			runSafe = runSafe && visitPredicates(orexpr.getRightExpression());
			foundIndexablePredicates = foundIndexablePredicates && (((subPredicatesGroup.get(currentRoot)).size() > 0) || (orPredicates.size() > orPredicatesCount));
		}
		
		currentRoot = prevRoot;
		if (!runSafe)
			return false;
			
		if (!foundIndexablePredicates) {
			while (orPredicates.size() >= beginOrPredicatesCount) {
				subPredicatesGroup.remove(orPredicates.lastElement().getLeftExpression());
				subPredicatesGroup.remove(orPredicates.lastElement().getRightExpression());
				orPredicates.removeElementAt(orPredicates.size() - 1);
			}
		} else
			subPredicatesGroup.get(currentRoot).add(orexpr);
		
		return true;
	}

	void visitExistsPredicate(ExistsExpression existsexpr) {
		
		Expression parent = existsexpr.getParentExpression();
		while (andPattern.matches(parent))
			parent = parent.getParentExpression();
		
		if (parent instanceof WhereExpression)
			currentExists.add(existsexpr);
		
	}
	
	private void addPredicateIfDeteministic(Expression predicate) {
		if (isDeterministic(predicate))
			subPredicatesGroup.get(currentRoot).add(predicate);
	}
	
	/**
	 * TODO: Add all other necessary conditions
	 * @param expr expression to be checked
	 * @return boolean indicating commutativity of this expression as conjunction selection predicate 
	 */
	private boolean isCommutativeConjunctionSelectionPredicate(Expression expr) {
		// TODO Auto-generated method stub
		return isDataPreservingExpression(expr) && isRunTimeSafeExpression(expr);
	}
	
	/**
	 * TODO: This is only a stub
	 * @param expr expression to be checked
	 * @return boolean indicating if evaluation of this query never changes the database store contents or the program variables 
	 */
	private boolean isDataPreservingExpression(Expression expr) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * TODO: This is only a stub
	 * @param expr expression to be checked
	 * @return boolean indicating if evaluation of this query is safe from predictable run-time errors (e.g. wrong cardinality, etc.)
	 */
	private boolean isRunTimeSafeExpression(Expression expr) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * TODO: This is only a stub, because procedures and views and binders can introduce randomization
	 * @param expr expression to be checked
	 * @return boolean indicating if evaluation of this query is not random for a given database and program state 
	 */
	private boolean isDeterministic(Expression expr) {
		try {
			return new ASTNodeFinder(new ASTNodePattern(RandomExpression.class), true).findNodes(expr).size() == 0;
		} catch (Exception e) { }
		assert false : "No supposed to be here!";
		return false;
	}
	
	/**
	 * @param expr expression to be checked
	 * @return boolean indicating if cardinality is [1..1] (exists operator is taken into consideration)
	 */
	private boolean isCardinalitySingular(Expression expr) {
		if (IndexFitter.skipDecoration(expr).getSignature().getMaxCard() != 1) 
			return false;
		if (IndexFitter.skipDecoration(expr).getSignature().getMinCard() == 1)
			return true;
		if (IndexFitter.skipDecoration(expr).getSignature().getMinCard() == 0) {
			try {
				String exprAST = AST2TextQueryDumper.AST2Text(IndexFitter.skipDecoration(expr));
				if (exists.containsKey(exprAST)) {
					associatedExistsPredicates.put(expr.getParentExpression(), exists.get(exprAST));
					return true; 
				} 
			} catch (Exception E) {
				assert false : "shouldn't be here";
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return subPredicatesGroup.toString();
	}
	
	
}
