package odra.sbql.optimizers.queryrewrite.viewrewrite;

import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import odra.db.objects.data.DBModule;
import odra.db.schema.OdraViewSchema.GenericNames;
import odra.sbql.SBQLException;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;

/** 
 *  View rewrite optimizer
 * rewrite is possible if and only if
 * all the view elements used in the query
 * (virtual objects procedure,
 * generic procedures and recurrently sub-view)
 * can be rewritten 
 * optimizer work on the typechecked AST query
 * if the query was modified 
 * this is experimental version!!, not very generic I think
 * created mostly to rewrite simple views generated in relational wrapper 
 * where the rewrite is always possible (and required!)
 * @author Radek Adamus
 *last modified: 2007-12-07
 *@version 2.0
 */
public class ViewRewriter implements ISBQLOptimizer {
	/** invalid name finder */
	
	private ASTAdapter staticEval;
	private DBModule mod;
	private Vector<RewriteInfo> roots;
	private Map<NameExpression, Vector<RewriteInfo>> virtChains;
	private Map<GenericNames, Map<Expression, RewriteInfo>> operatorsRewrite;
	private Vector<RewriteInfo> on_new = new Vector<RewriteInfo>();
	private Expression root;
	
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#setStaticEval(odra.sbql.ast.ASTAdapter)
	 */
	public void setStaticEval(ASTAdapter staticEval) {
		this.staticEval = staticEval;
	
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#optimize(odra.sbql.ast.ASTNode)
	 */
	public ASTNode optimize(ASTNode query, DBModule mod) throws SBQLException {
	    
		return optimizeInternal(query, mod, true);
		
	}

	
	protected ASTNode optimizeInternal(ASTNode query, DBModule mod, boolean strict) {
		assert query instanceof Expression :"instanceof Expession";
		this.root = (Expression)query;
		this.mod = mod;
		boolean rewritten = false;

		do {
			rewritten = false;
			if (strict && !analyzeResultSignature(this.root.getSignature())) {
				String dump = AST2TextQueryDumper.AST2Text(root);
				ConfigServer.getLogWriter().getLogger().log(Level.INFO,
						"Query cannot be viewrewrited because the result contains unconsumed vitual element: '" + dump + "'");
				break;
			}
			// check if the virtual calls in the query can be rewritten
			// and collect replacement ast from the view definitions
			searchVirtualCalls(this.root, strict);

			// we can have more than one view call in the query tree
			for (RewriteInfo ri : this.roots) {
				if (rewrite(ri))
					rewritten = true;
			}
			// than check if we have on_new call (it is treated differently and
			// should be rewritten separately)
			if (!rewritten && on_new.size() > 0) {
				for (RewriteInfo newRewrite : on_new) {
					if (newRewrite.replacement != null) {
						this.rewriteOnNew(newRewrite.expr, newRewrite.replacement);
						rewritten = true;
					}
				}
			}
			if (rewritten) {
				//		  
				// run static evaluator to fill up ast info
				try {
					this.root.accept(this.staticEval, null);
				} catch (SBQLException e) {

					String dump = AST2TextQueryDumper.AST2Text(root);
					ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Optimized query compilation error: " + dump);
					throw new OptimizationException(e);
				}

				// break;

				// root.accept(auxRemover, null);

			}
		} while (rewritten);
		// try to remove redundant auxiliary names
		// AuxiliaryNamesRemover auxRemover = new AuxiliaryNamesRemover();
		// this.root = auxRemover.remove((Expression)this.root);
		return this.root;
	}
	
	private void searchVirtualCalls(ASTNode query, boolean strict) throws SBQLException {
		VirtualCallSearcher vcs = new VirtualCallSearcher(this.staticEval, !strict);
		// check if the virtual calls in the query can be rewritten
		// and collect required ast from the view procedures
		query = vcs.find(query, this.mod);
		this.roots = vcs.roots;
		this.virtChains = vcs.virtChains;
		this.operatorsRewrite = vcs.operatorsRewrite;
		this.on_new = vcs.on_new;
	}
	
	/**
	 * The views can be rewritten if and only if all used view elements
	 * (generic procedures, subviews) used in the outer query can be rewritten  
	 */
	private boolean canViewsBeRewritten(Vector<RewriteInfo> vri){
		
		for(RewriteInfo cri: vri){
			if(cri.replacement == null) return false; //virtual objects cannot be rewriten			
			if(!this.canGenericOperatorsBeRewrite(cri.expr)) return false; //generic procedures used in the query cannot be rewritten 
			if(!canViewsBeRewritten(this.virtChains.get(cri.expr))) return false; //subviews cannot be rewritten

		}
		return true;
	}
	
	/** Checks if the generic procedures that are used in the query 
	 * can be rewritten
	 * @param virtExpr
	 * @return
	 */
	private boolean canGenericOperatorsBeRewrite(Expression virtExpr){

	    for(GenericNames name : GenericNames.values()){
		if(!name.equals(GenericNames.ON_NEW_NAME))
		    if(!this.canOperatorBeRewrittenForExpression(name, virtExpr))
			return false;
	    }
		return true;
	}
	
	private boolean canOperatorBeRewrittenForExpression(GenericNames operatorName, Expression virtExpr) {
		RewriteInfo rewrite = this.operatorsRewrite.get(operatorName).get(virtExpr);
		if (rewrite == null)
			return true; // there is no call to on retrieve for a given view in
							// the ast

		return rewrite.replacement != null;
	}
	
	
	private boolean rewrite(RewriteInfo ri) {
		// if it is possible to rewrite root virtual objects call ...
		if (ri.replacement != null) {
			// ... and possible root generic procs calls ...
			if (canGenericOperatorsBeRewrite(ri.expr)) {
				// ... and all subviews calls ...
				if (this.canViewsBeRewritten(this.virtChains.get(ri.expr))) {

					// first rewrite root virtual objects procedure ...
					this.rewriteVirtualObjectsProcedure(ri.expr, ri.replacement);
					// ... then we can rewrite the whole view
					this.rewriteView(ri, this.virtChains.get(ri.expr));
					return true;
				}
			}
		}
		return false;
	}
	
	/** Rewrites the view
	 * @param view - rewrite info for the seed 
	 * @param subviewscalls rewrite info for the subviews
	 */
	private void rewriteView(RewriteInfo view, Vector<RewriteInfo> subviewscalls) {
		// rewrite generic procs
		this.rewriteGenericProcedures(view.expr);
		// for all subviews ...
		for (RewriteInfo sview : subviewscalls) {
			// ... rewrite virtual objects procedures...
			this.rewriteVirtualObjectsProcedure(sview.expr, sview.replacement);
			// rewrite generic procedures
			this.rewriteGenericProcedures(sview.expr);
			// ... then check if we have a sub-views
			if (this.virtChains.get(sview.expr).size() > 0) {
				// ...
				rewriteView(sview, this.virtChains.get(sview.expr));
			}

		}
	}
	
	
	/**
	 * Rewrite view generic procedures
	 * 
	 * @param view
	 *            - ast expression that generate virtual identifier
	 */
	private void rewriteGenericProcedures(Expression view) {

		RewriteInfo rewrite = this.operatorsRewrite.get(GenericNames.ON_RETRIEVE_NAME).get(view);
		if (rewrite != null) {
			// rewrite on_retrieve
			this.rewriteOnRetrieve(rewrite.expr, rewrite.replacement);
		}
		rewrite = this.operatorsRewrite.get(GenericNames.ON_UPDATE_NAME).get(view);
		if (rewrite != null) {
			// rewrite on_update
			this.rewriteOnUpdate(rewrite.expr, rewrite.replacement);
		}
		rewrite = this.operatorsRewrite.get(GenericNames.ON_DELETE_NAME).get(view);
		if (rewrite != null) {
			// rewrite on_delete
			this.rewriteOnDelete(rewrite.expr, rewrite.replacement);
		}
		rewrite = this.operatorsRewrite.get(GenericNames.ON_NAVIGATE_NAME).get(view);
		if (rewrite != null) {
			// rewrite on_navigate
			this.rewriteOnNavigate(rewrite.expr, rewrite.replacement);
		}
	}
	private void rewriteVirtualObjectsProcedure(Expression pexpr, Expression replacement) {
		if (pexpr.getParentExpression() != null) {
			pexpr.getParentExpression().replaceSubexpr(pexpr, replacement);
		} else {
			root = replacement;
		}
	}

	private void rewriteOnRetrieve(Expression pexpr, Expression replacement) {
		if (ConfigDebug.ASSERTS)
			assert pexpr instanceof DerefExpression : "on_retrieve procedure can be rewritten only on DerefExpression";
		DerefExpression dexpr = (DerefExpression) pexpr;
		if (dexpr.getParentExpression() != null) {
			dexpr.getParentExpression().replaceSubexpr(dexpr, new DotExpression(dexpr.getExpression(), replacement));
		} else {
			root = new DotExpression(dexpr.getExpression(), replacement);
		}
	}
	
	private void rewriteOnUpdate(Expression pexpr, Expression replacement) {
		if (ConfigDebug.ASSERTS)
			assert pexpr instanceof AssignExpression : "on_update procedure can be rewritten only on AssignExpression";
		AssignExpression aexpr = (AssignExpression) pexpr;
		if (aexpr.getParentExpression() != null) {
			aexpr.getParentExpression().replaceSubexpr(aexpr, new DotExpression(aexpr.getLeftExpression(), replacement));
		} else {
			root = new DotExpression(aexpr.getLeftExpression(), replacement);
		}
	}
	private void rewriteOnDelete(Expression pexpr, Expression replacement) {
		if (ConfigDebug.ASSERTS)
			assert pexpr instanceof DeleteExpression : "on_delete procedure can be rewritten only on DeleteExpression";
		DeleteExpression dexpr = (DeleteExpression) pexpr;
		Expression result = null;
		if (replacement instanceof DeleteExpression) {
			result = new DeleteExpression(new DotExpression(dexpr.getExpression(), ((DeleteExpression) replacement).getExpression()));
		} else {
			throw new OptimizationException("unimplemented rewrite");
		}
		if (dexpr.getParentExpression() != null) {
			dexpr.getParentExpression().replaceSubexpr(dexpr, result);
		} else {
			root = result;
		}
	}
	private void rewriteOnNew(Expression pexpr, Expression replacement) {
		if (ConfigDebug.ASSERTS)
			assert pexpr instanceof CreateExpression : "on_delete procedure can be rewritten only on CreateExpression";
		CreateExpression cexpr = (CreateExpression) pexpr;
		Expression result = replacement;
		if (cexpr.getParentExpression() != null) {
			cexpr.getParentExpression().replaceSubexpr(cexpr, result);
		} else {
			root = result;
		}

	}
	
	private void rewriteOnNavigate(Expression pexpr, Expression replacement) {
		assert pexpr instanceof NonAlgebraicExpression : "on_navigate procedure can be rewritten only on NonAlgebraicExpression";
		NonAlgebraicExpression aexpr = (NonAlgebraicExpression) pexpr;
		Expression e2 = aexpr.getRightExpression();
		aexpr.replaceSubexpr(aexpr.getRightExpression(), replacement);
		if (aexpr.getParentExpression() != null) {
			aexpr.getParentExpression().replaceSubexpr(aexpr, new DotExpression(aexpr, e2));
		} else {
			root = new DotExpression(aexpr, e2);
		}
	}
	
	/**
	 * @param expr
	 * @return true if the result signature is proper from the point of view rewrite
	 */
	private boolean analyzeResultSignature(Signature sig) {

		// cannot be a virtual reference
		if (isVirtual(sig)) {
			return false;
		}
		if (sig instanceof StructSignature) {
			StructSignature ssig = (StructSignature) sig;
			for (Signature fsig : ssig.getFields()) {
				if (isVirtual(fsig)) {
					return false;
				}
			}
		}
		if (sig instanceof BinderSignature) {
			analyzeResultSignature(((BinderSignature) sig).value);
		}

		return true;
	}
	
	private boolean isVirtual(Signature sig) {
		return (sig instanceof ReferenceSignature && ((ReferenceSignature) sig).isVirtual());
	}
}
