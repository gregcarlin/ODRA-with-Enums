package odra.sbql.optimizers.debug;

import java.util.Set;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBLink;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.optimizers.OptimizationException;

public class ASTLinksDumper extends TraversingASTAdapter
{

	private StringBuffer str = new StringBuffer();

	public String dumpAST(ASTNode node) throws SBQLException
	{		
		node.accept(this, null);		
		
		return str.toString();
	}

	private void printUnaryWithLinks(UnaryExpression expr, Object attr, String oper) throws SBQLException
	{
		str.append(oper + "(");
		expr.getExpression().accept(this, attr);
		printLinks(expr);
		str.append(")");
	}
	
	private void printUnary(UnaryExpression expr, Object attr, String oper) throws SBQLException
	{
		str.append(oper + "(");
		expr.getExpression().accept(this, attr);
		str.append(")");
	}

	private void printBinary(BinaryExpression expr, Object attr, String oper) throws SBQLException
	{
		str.append("(");
		expr.getLeftExpression().accept(this, attr);
		str.append(" " + oper);
		printLinks(expr);
		str.append(" ");
		expr.getRightExpression().accept(this, attr);
		str.append(")");

	}

	private void printLinks(Expression expr) throws SBQLException
	{
	    	Set<OID> links = expr.links;
		if (links.size() < 1)
			return;

		StringBuffer tmp = new StringBuffer();

		tmp.append("@[");
		try {
		    for (OID oid : links)
		    	tmp.append(new MBLink(oid).getName() + "; ");
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, expr, this);
		}

		tmp.delete(tmp.length() - 2, tmp.length());
		tmp.append("]");

		str.append(tmp);

	}

	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, ".");
		return null;
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException
	{
		str.append(expr.name().value());
		printLinks(expr);
		return null;
	}

	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "join");
		return null;
	}

	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "union");
		return null;
	}

	@Override
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "where");
		return null;
	}

	@Override
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException
	{
		printUnary(expr, attr, "deref");
		return null;
	}

	@Override
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "=");
		return null;
	}

	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException
	{
		str.append("(");
		expr.getExpression().accept(this, attr);
		str.append(") groupas " + expr.name().value() + " ");
		return null;
	}

	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "in");
		return null;
	}

	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException
	{
		str.append(expr.getLiteral().value());
		printLinks(expr);
		return null;
	}

	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException
	{
		printUnaryWithLinks(expr, attr, "max");
		return null;
	}

	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException
	{
		printUnaryWithLinks(expr, attr, "min");
		return null;
	}

	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "minus");
		return null;
	}

	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, "orderby");
		return null;
	}

	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException
	{
		expr.getProcedureSelectorExpression().accept(this, attr);
		str.append("(");
		expr.getArgumentsExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException
	{
		str.append(expr.getLiteral().value());
		return null;
	}

	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException
	{
		str.append(expr.getLiteral().value());
		return null;
	}

	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException
	{
		str.append("\"" + expr.getLiteral().value() + "\"");
		return null;
	}

	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException
	{
		printUnaryWithLinks(expr, attr, "sum");
		return null;
	}

	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException
	{
		printUnary(expr, attr, "(boolean)");

		return null;
	}

	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException
	{
		printUnary(expr, attr, "unique");
		return null;
	}

	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException
	{
		str.append(expr.getLiteral().value());
		return null;
	}

	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException
	{
		printBinary(expr, attr, expr.O.spell());
		return null;
	}

	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException
	{
		printUnary(expr, attr, expr.O.spell());
		return null;
	}

	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException
	{
		printUnaryWithLinks(expr, attr, "count");
		return null;
	}

}
