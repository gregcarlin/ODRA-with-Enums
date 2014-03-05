/**
 * 
 */
package odra.sbql.optimizers.queryrewrite.viewrewrite;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;

/**
 * UnstrictViewRewriter
 * defines optional rewrite 
 * that does not care about 
 * proper result signature
 * @author Radek Adamus
 *@since 2008-10-31
 *last modified: 2008-10-31
 *@version 1.0
 */
public class UnstrictViewRewriter extends ViewRewriter {

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.queryrewrite.viewrewrite.ViewRewriter#optimize(odra.sbql.ast.ASTNode, odra.db.objects.data.DBModule)
	 */
	@Override
	public ASTNode optimize(ASTNode query, DBModule mod) throws SBQLException {
		// TODO Auto-generated method stub
		return super.optimizeInternal(query, mod, false);
	}

}
