package odra.sbql.optimizers.debug;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.system.config.ConfigServer;

public class BindingLevelDumper extends TraversingASTAdapter {
	
	
	public Object visitDotExpression(DotExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		ConfigServer.getLogWriter().getLogger().info("Stack size: " + (expr.getEnvsInfo() != null ? (expr.getEnvsInfo().baseEnvsSize + ".." + (expr.getEnvsInfo().baseEnvsSize + expr.getEnvsInfo().framesOpened - 1)) : "information unavaliabe"));
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitForAllExpression(ForAllExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		ConfigServer.getLogWriter().getLogger().info("Stack size: " + (expr.getEnvsInfo() != null ? (expr.getEnvsInfo().baseEnvsSize + ".." + (expr.getEnvsInfo().baseEnvsSize + expr.getEnvsInfo().framesOpened - 1)) : "information unavaliabe"));
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		ConfigServer.getLogWriter().getLogger().info("Stack size: " + (expr.getEnvsInfo() != null ? (expr.getEnvsInfo().baseEnvsSize + ".." + (expr.getEnvsInfo().baseEnvsSize + expr.getEnvsInfo().framesOpened - 1)) : "information unavaliabe"));
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitJoinExpression(JoinExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		ConfigServer.getLogWriter().getLogger().info("Stack size: " + (expr.getEnvsInfo() != null ? (expr.getEnvsInfo().baseEnvsSize + ".." + (expr.getEnvsInfo().baseEnvsSize + expr.getEnvsInfo().framesOpened - 1)) : "information unavaliabe"));
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitNameExpression(NameExpression expr, Object attr)
			throws SBQLException {

		if (expr.getBindingInfo() != null)
			ConfigServer.getLogWriter().getLogger().info(
					"Name: " + expr.name().value() + " bound at section "
							+ expr.getBindingInfo().boundat);

		return null;

	}

	public Object visitOrderByExpression(OrderByExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		ConfigServer.getLogWriter().getLogger().info("Stack size: " + (expr.getEnvsInfo() != null ? (expr.getEnvsInfo().baseEnvsSize + ".." + (expr.getEnvsInfo().baseEnvsSize + expr.getEnvsInfo().framesOpened - 1)) : "information unavaliabe"));
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitWhereExpression(WhereExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		ConfigServer.getLogWriter().getLogger().info("Stack size: " + (expr.getEnvsInfo() != null ? (expr.getEnvsInfo().baseEnvsSize + ".." + (expr.getEnvsInfo().baseEnvsSize + expr.getEnvsInfo().framesOpened - 1)) : "information unavaliabe"));
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	
}
