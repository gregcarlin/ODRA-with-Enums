package odra.sbql.optimizers.queryrewrite.index;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.IndexManager;
import odra.db.indices.NonkeyIndexRegister;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.patterns.ASTNodePattern;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.results.compiletime.ReferenceSignature;

/** 
 * Base optimizer class for performing rewriting using indices.<br>
 * It uses information about indices from index register, cost model and given query
 * to select and apply best indices to quicken query evaluation.
 * 
 * @author tkowals
 * @version 1.0
 */
public class IndexOptimizer implements ISBQLOptimizer {

	private boolean indexUsed = false;
	
	private IndexManager iman;

	//TODO - it should be better expressed!!!
	private ASTAdapter staticEval;
	
	private ASTNode query;
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#setStaticEval(odra.sbql.ast.ASTAdapter)
	 */
	public void setStaticEval(ASTAdapter staticEval) {
		this.staticEval = staticEval;
	}
	
	/**
	 * Creates new instance of index optimizer.
	 * @throws OptimizationException
	 */
	public IndexOptimizer() throws OptimizationException
	{
		try {
			iman = new IndexManager();
		}
		catch(DatabaseException exc)
		{
			throw new OptimizationException("Error in index manager, optimization failed", exc);
		}
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#optimize(odra.sbql.ast.ASTNode, odra.db.objects.data.DBModule)
	 */
	public ASTNode optimize(ASTNode query, DBModule mod) throws SBQLException {
		assert query instanceof Expression: "Index Optimizer expects Expression not " + query.getClass().getName();
		this.query = query;
		try {
		    if (query instanceof WhereExpression) { 
		    	query = optimizeWhereClause((WhereExpression) query);
		    	if (indexUsed)
		    		query.accept(staticEval, null);
		    	return query;
		    }
		    	
		    return optimizeSubQuery((Expression) query);
		} catch (DatabaseException e) {
		 throw new OptimizationException(e);
		}
	}
		
	private Expression optimizeSubQuery(Expression subQuery) throws DatabaseException {
		assert (!(subQuery instanceof WhereExpression)): "Where expression not expected in this method - inform Tomek Kowalski :)";
		ASTNodeFinder finder = new ASTNodeFinder(new ASTNodePattern(WhereExpression.class), true);
		finder.findNodes(subQuery);
				
		for (ASTNode whexpr: finder.getResult())
			optimizeWhereClause((WhereExpression) whexpr);			
		
		if (subQuery.getParentExpression () == null)
			query = subQuery;
		
		if (indexUsed)
			query.accept(staticEval, null);
		
		indexUsed = false;
		
		return subQuery;
	}

	private Expression optimizeWhereClause(WhereExpression whexpr) throws DatabaseException {
		
		whexpr.setRightExpression((Expression) this.optimizeSubQuery(whexpr.getRightExpression()));
		
		if(whexpr.getLeftExpression() instanceof WhereExpression)
			return this.optimizeWhereClause((WhereExpression) whexpr.getLeftExpression());
		 
		if (whexpr.getLeftExpression().getSignature() instanceof ReferenceSignature) {		
			OID unkoid = iman.lookupNonkeyRef(AST2TextQueryDumper.AST2Text(whexpr.getLeftExpression()), ((ReferenceSignature) whexpr.getLeftExpression().getSignature()).value);				
			if (unkoid != null)
			{						
				// check whether where expression is bound at the root database level		
				IndexASTChecker checker = new IndexASTChecker();
				checker.markIndexSubAST(whexpr.getLeftExpression());					
				if (checker.isWholeASTBoundedTo(whexpr.getLeftExpression(), null))			
				{
					IndexFitter ifit = fitConditionsInIndices(whexpr, unkoid);
					if (ifit != null)
					    try {
						return ifit.applyIndexing();
					    } catch (Exception e) {
						throw new IndexOptimizerException(e);
					    }
				}
			}
		}
		whexpr.setLeftExpression(this.optimizeSubQuery(whexpr.getLeftExpression()));
		
		while  ((whexpr.getParentExpression() != null) && (whexpr.getParentExpression() instanceof WhereExpression)) 
			whexpr = (WhereExpression) whexpr.getParentExpression();
		
		return whexpr;
	}
	
	IndexFitter fitConditionsInIndices(WhereExpression whexpr, OID unkoid) throws DatabaseException {
		
		Index keyidx = NonkeyIndexRegister.getKeyIndex(unkoid);
		
		IndexableSelectionPredicatesFinder finder = new IndexableSelectionPredicatesFinder(whexpr);
		
		SelectionPredicatesDisjoiner spd = new SelectionPredicatesDisjoiner(finder.getOrCount());
		IndexFitter ifit = new IndexFitter(whexpr, finder, iman, unkoid, spd, 1 + 2 * finder.getOrCount()); 
		
		spd.addRootPredicate(whexpr.getRightExpression());
		
		for(int condition_num = 0; condition_num < spd.predicatesCount(); condition_num++) {

			// find all indexable 'and' children predicates	
			for(Expression cond: finder.getPredicatesGroup(spd.getPredicateAST(condition_num))) {

				int oper = -1;
				Expression leftExpr, rightExpr;
				if (!(cond instanceof BinaryExpression)) {
					leftExpr = cond;
					rightExpr = new BooleanExpression(new BooleanLiteral(true));
					rightExpr.accept(staticEval, null);
					rightExpr.setParentExpression(leftExpr);
					oper = IndexFitter.EQ_OP;
				} else {
					BinaryExpression bincond = (BinaryExpression) cond; 
					leftExpr = bincond.getLeftExpression();
					rightExpr = bincond.getRightExpression();
					if (bincond instanceof EqualityExpression) {
						if (((EqualityExpression) bincond).O.equals(Operator.opEquals)) 
							oper = IndexFitter.EQ_OP;
					}else if (bincond instanceof InExpression) {
						if ((IndexFitter.skipDecoration(bincond.getLeftExpression()).getSignature().getMinCard() == 1)
								&& (IndexFitter.skipDecoration(bincond.getLeftExpression()).getSignature().getMaxCard() == 1)
								&& (IndexFitter.skipDecoration(bincond.getRightExpression()).getSignature().getMinCard() == 1)
								&& (IndexFitter.skipDecoration(bincond.getRightExpression()).getSignature().getMaxCard() == 1))
							oper = IndexFitter.EQ_OP;
						else 
							oper = IndexFitter.IN_OP;
					}else if (bincond instanceof SimpleBinaryExpression) {
						if (((SimpleBinaryExpression) bincond).O.getAsInt() == Operator.OR) {
							spd.addOrPredicate(bincond.getLeftExpression(), bincond.getRightExpression(), condition_num);	
							continue;
						}	
						oper = IndexFitter.operator2IFOP(((SimpleBinaryExpression) bincond).O);
					}
				}
					
				if (oper == -1) continue;							
				
				OID ukeyoid = (OID) keyidx.lookupItem(AST2TextQueryDumper.AST2Text(IndexFitter.skipDecoration(leftExpr)));
					
				if (ukeyoid != null)
					ifit.fitCondition(ukeyoid, leftExpr, rightExpr, oper, condition_num);

				
				ukeyoid = (OID) keyidx.lookupItem(AST2TextQueryDumper.AST2Text(IndexFitter.skipDecoration(rightExpr)));
				
				if (ukeyoid != null)
					ifit.fitCondition(ukeyoid, rightExpr, leftExpr, IndexFitter.revOperators(oper), condition_num);			
				
			}	
			
			if (condition_num % 2 == 0) {
				if (ifit.fitIndexing(condition_num)) { 
					indexUsed = true;
					if (QUICK_MODE) return ifit;
				}
			}
		}
		
		if (indexUsed)
			return ifit;
		
		return null;
		
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#reset()
	 */
	public void reset() {
		// nothing happens

	}

	// if false all or conditions are taken into consideration before applying indexing
	private final static boolean QUICK_MODE = false; 
	
}
