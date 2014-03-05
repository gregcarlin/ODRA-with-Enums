package odra.sbql.ast.statements;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;

public final class ReturnWithValueStatement extends ExpressionStatement {

   public ReturnWithValueStatement(Expression e) {
      super(e);
   }

   public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
      return vis.visitReturnWithValueStatement(this, attr);
   }
}