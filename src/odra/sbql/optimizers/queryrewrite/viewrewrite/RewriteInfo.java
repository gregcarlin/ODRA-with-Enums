package odra.sbql.optimizers.queryrewrite.viewrewrite;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * RewriteInfo This class is used to transfer the informations that connects the
 * existing expression and the replacement expression to (possible) rewrite
 * (e.g. procedure ast)
 * 
 * @author Radek Adamus last modified: 2007-03-27
 * @version 1.0
 */
class RewriteInfo
{
	/** name finder */
	private final NameFinder nameFinder = new NameFinder();
	
	Expression expr;
	Expression replacement; // can be null

	RewriteInfo(Expression expr, Expression replacement, boolean saveSubstitutedSignature) throws SBQLException
	{
		this.expr = expr;
		this.replacement = replacement;
		
		if(this.replacement != null)
		{
			this.replacement.isViewSubstituted = true;
			if (saveSubstitutedSignature)
				this.replacement.setSubstitutedSignature(this.expr.getSignature());
			
			for(ASTNode node : nameFinder.findNodes(this.replacement))
				((NameExpression)node).isViewSubstituted = true;
		}
	}

	/**
	 * {@link NameExpression} finder utility class.
	 * 
	 * @author jacenty
	 */
	private class NameFinder extends ASTNodeFinder
	{
		NameFinder()
		{
			super(new Pattern()
			{
				public boolean matches(Object obj)
				{
					return obj instanceof NameExpression;
				}
			}, false);
		}
	}
}
