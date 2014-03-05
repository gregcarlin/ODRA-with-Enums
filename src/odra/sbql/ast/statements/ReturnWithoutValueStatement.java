package odra.sbql.ast.statements;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ReturnWithoutValueStatement extends Statement {

   public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
      return vis.visitReturnWithoutValueStatement(this, attr);
   }
}
