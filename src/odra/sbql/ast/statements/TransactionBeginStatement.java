package odra.sbql.ast.statements;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public final class TransactionBeginStatement extends Statement {
   public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
      return null;
      // return vis.visitReturnWithoutValueStatement(this, attr);
   }
}