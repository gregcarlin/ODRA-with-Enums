package odra.sbql.optimizers.queryrewrite.procedures;
import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.system.config.ConfigDebug;

/** Base procedure rewrite class
 * it performes only procedures rewriting
 * ProcedureRewriter
 * @author Radek Adamus
 *last modified: 2007-02-05
 *@version 1.0
 */
public class ProcedureRewriter extends TraversingASTAdapter implements ISBQLOptimizer{

	ASTNode root;
	ASTAdapter staticEval;
	
public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException {
		root = query;
		try {
		    this.setSourceModuleName(module.getName());
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, query, this);
		}
		root.accept(this, null);
		this.root.accept(this.staticEval, null);
		return root;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#setStaticEval(odra.sbql.ast.ASTAdapter)
	 */
	public void setStaticEval(ASTAdapter staticEval) {
		this.staticEval = staticEval;
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitProcedureCallExpression(odra.sbql.ast.expressions.ProcedureCallExpression, java.lang.Object)
	 */
	@Override
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		if(ConfigDebug.ASSERTS) assert expr.getSignature() != null : "there is no signature in the AST node";
		MBProcedure proc;
		try {
		    proc = new MBProcedure(((ReferenceSignature)(expr.getProcedureSelectorExpression().getSignature())).value);
		    if (!proc.isValid()) return null;
		} catch (DatabaseException e) {
		   throw new OptimizationException(e, expr,this);
		}
		Expression replacement = new ProcedureCallReplacer().getReplacementExpression(this.getSourceModuleName(), proc,expr.getArgumentsExpression(), true);
		if(replacement != null){
			if(expr.getParentExpression() != null){
				expr.getParentExpression().replaceSubexpr(expr, replacement);
			}else {
				root = replacement;
			}
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#optimize(odra.sbql.ast.ASTNode)
	 */
	
	public void reset() {}
	
	
	
	
	
	
}
