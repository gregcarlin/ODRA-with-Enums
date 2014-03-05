package odra.sbql.optimizers.queryrewrite.distributed;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;

/**
 * ASTLinkFinder searches for link node within remote query
 * 
 * @author janek
 *
 */
public class ASTLinkFinder extends TraversingASTAdapter {

	private NameExpression linkExpression;
	private String linkName;

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr)
			throws SBQLException {

		if ((expr.name().value().equals(linkName)) && (linkExpression==null) )
			linkExpression = expr;
		
		return null;
	}

	public ASTLinkFinder(String linkName) {
		linkExpression = null;
		this.linkName = linkName;
	}

	public Expression getLinkExpression() {
		return this.linkExpression;
	}

}
