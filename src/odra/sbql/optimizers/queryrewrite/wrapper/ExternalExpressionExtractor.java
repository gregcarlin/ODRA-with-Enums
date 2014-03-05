package odra.sbql.optimizers.queryrewrite.wrapper;

import java.util.Hashtable;
import java.util.Vector;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NowExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * Utility class analyzing expressions to determine if they are bound to any wrapper or they 
 * are "external" to the wrapper, e.g. procedure calls, some variables, etc. Such expressions are 
 * substituted by auxiliary expressions, the actual ones are buffered for final replacement after 
 * the wrapper optimization process.
 * 
 * @author jacenty
 * @version 2008-01-28
 * @since 2007-06-27
 */
public class ExternalExpressionExtractor extends TraversingASTAdapter
{
	public static final String AUX_PREFIX = "___expr_";
	/** auxiliary name index */
	private int auxIndex = 0;
	/** buffer for non-wrapper expressions */
	private Hashtable<String, Expression> buffer = new Hashtable<String, Expression>();
	
	@Override
	protected Object commonVisitExpression(Expression expr, Object attr) throws SBQLException
	{
		if(
				expr instanceof NameExpression && expr.wrapper == null ||
				expr instanceof NowExpression ||
				expr instanceof DateprecissionExpression ||
				expr instanceof RandomExpression ||
				expr instanceof ProcedureCallExpression ||
				expr instanceof ExecSqlExpression)
		{
			while(
					expr.getParentExpression() instanceof UnaryExpression && !(expr.getParentExpression() instanceof AsExpression) || //reject auxiliary aliases for updates
					expr.getParentExpression() instanceof DateprecissionExpression ||
					expr.getParentExpression() instanceof ProcedureCallExpression)
				expr = expr.getParentExpression();
			
			if(!buffer.containsValue(expr))//FIXME some expressions (proc. calls) are detected twice - why?
			{
				//check if the expression is not untroduced as alias for update
				//TODO
				if(expr instanceof NameExpression)
				{
					//move up to the tree root
					Expression wholeQuery = Utils.findRoot(expr);
					
					final NameExpression nameExpression = (NameExpression)expr;
					ASTNodeFinder aliasFinder = new ASTNodeFinder(new Pattern()
					{
						public boolean matches(Object obj)
						{
							return obj instanceof AsExpression && ((AsExpression)obj).name().value().equals(nameExpression.name().value());
						}
					}, true);
					
					if(!aliasFinder.findNodes(wholeQuery).isEmpty())
						return super.commonVisitExpression(expr, attr);//no replacement
				}
				
				String aux = AUX_PREFIX + (auxIndex++);
				buffer.put(aux, expr);
				
				Expression replacement = new StringExpression(new StringLiteral(aux));
	
				//auxiliary name introduced for updates, the associator is set for resolving value in Utils.java
				//the whole query is finally re-typechecked, so this associator disappears before evaluation
				if(expr.getParentExpression() instanceof AsExpression)
				{
					//move up to the tree root
					Expression wholeQuery = Utils.findRoot(expr);
	
					final Name name = ((AsExpression)expr.getParentExpression()).name();
					ASTNodeFinder nameExpressionFinder = new ASTNodeFinder(new Pattern()
					{
						public boolean matches(Object obj)
						{
							return obj instanceof NameExpression && ((NameExpression)obj).name().value().equals(name.value());
						}
					}, true);
					
					Vector<ASTNode> result = nameExpressionFinder.findNodes(wholeQuery);
					if(!result.isEmpty())
						((NameExpression)result.firstElement()).getSignature().getOwnerExpression().getSignature()
							.setAssociatedExpression(
								replacement);
				}
				
				expr.getParentExpression().replaceSubexpr(expr, replacement);
			}
		}
		
		return super.commonVisitExpression(expr, attr);
	}
	
	/**
	 * Returns the auxiliary expression buffer.
	 * 
	 * @return expression buffer
	 */
	public Hashtable<String, Expression> getAuxBuffer()
	{
		return buffer;
	}
}