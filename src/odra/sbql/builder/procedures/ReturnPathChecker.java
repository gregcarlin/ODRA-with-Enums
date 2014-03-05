package odra.sbql.builder.procedures;

import odra.db.DatabaseException;
import odra.db.StdEnvironment;
import odra.db.objects.meta.MBProcedure;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.BreakStatement;
import odra.sbql.ast.statements.ContinueStatement;
import odra.sbql.ast.statements.DoWhileStatement;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.ForStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.SingleCatchBlock;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.builder.CompilerException;

public class ReturnPathChecker extends ASTAdapter {
   MBProcedure mbproc;
   String procName;
   boolean withValue;

   
   public ReturnPathChecker(String modname, MBProcedure mbproc) throws CompilerException {
      this.mbproc = mbproc;
      this.setSourceModuleName(modname);    
      try {
	  this.procName = mbproc.getName();
	if (mbproc.getType().equals(StdEnvironment.getStdEnvironment().voidType))
	     withValue = false;
	  else
	     withValue = true;
    } catch (DatabaseException e) {
	throw new CompilerException(e);
    }
   }

   public Statement check(Statement rootstmt) throws CompilerException {
      

         if (!(Boolean) rootstmt.accept(this, null)) {
            if (withValue) {
               throw new CompilerException("procedure " + this.procName
                        + " not all control paths return a value", rootstmt, this);
            } 
            rootstmt = new SequentialStatement(rootstmt, new ReturnWithoutValueStatement());
         }

         return rootstmt;

   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitEmptyStatement(odra.sbql.ast.statements.EmptyStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitEmptyStatement(EmptyStatement node, Object attr) throws SBQLException {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitExpressionStatement(odra.sbql.ast.statements.ExpressionStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitExpressionStatement(ExpressionStatement node, Object attr) throws SBQLException {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitForEachStatement(odra.sbql.ast.statements.ForEachStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
      return stmt.getStatement().accept(this, attr);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {

      return (Boolean) stmt.getIfStatement().accept(this, attr) && (Boolean) stmt.getElseStatement().accept(this, attr);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitIfStatement(odra.sbql.ast.statements.IfStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
      stmt.getStatement().accept(this, attr);
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitReturnWithoutValueStatement(odra.sbql.ast.statements.ReturnWithoutValueStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement node, Object attr)
            throws SBQLException {
      if (!this.withValue)
         return true;
      
      throw new CompilerException("Procedure '" + this.procName + "' must return a value", node, this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitReturnWithValueStatement(odra.sbql.ast.statements.ReturnWithValueStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitReturnWithValueStatement(ReturnWithValueStatement node, Object attr) throws SBQLException {
      if (this.withValue)
         return true;
     
      throw new CompilerException("Procedure '" + this.procName + "' cannot return a value", node, this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitSequentialStatement(odra.sbql.ast.statements.SequentialStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
      Statement[] stmts = stmt.flatten();
      this.checkUnreachableCode(stmts, attr);
      return stmts[stmts.length - 1].accept(this, null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitBlockStatement(BlockStatement node, Object attr) throws SBQLException {
      return node.getStatement().accept(this, attr);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitVariableDeclarationStatement(VariableDeclarationStatement node, Object attr)
            throws SBQLException {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitDoWhileStatement(odra.sbql.ast.statements.DoWhileStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {
      return stmt.getStatement().accept(this, attr);

   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitForStatement(odra.sbql.ast.statements.ForStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {
      return stmt.getStatement().accept(this, attr);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitWhileStatement(odra.sbql.ast.statements.WhileStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {
      return stmt.getStatement().accept(this, attr);
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see odra.sbql.ast.ASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement,
    *      java.lang.Object)
    */
   @Override
   public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
      return false;
   }

   /* (non-Javadoc)
 * @see odra.sbql.ast.ASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
 */
   @Override
   public Object visitTryCatchFinallyStatement(TryCatchFinallyStatement stmt, Object attr) throws SBQLException
  {
       Boolean tryRes = (Boolean)stmt.getTryStatement().accept(this, attr);
       Boolean finallyRes = (Boolean)stmt.getFinallyStatement().accept(this, attr);
       SingleCatchBlock[] catchBlocks = stmt.getCatchBlocks().flattenCatchBlocks();
       Boolean catchRes = (Boolean)catchBlocks[0].getStatement().accept(this, attr);       
       for(int i = 0 ; i < catchBlocks.length ;  i++){	   

	   catchRes = catchRes && (Boolean)catchBlocks[i].getStatement().accept(this, attr) ;
       }
   //    Boolean res = (tryRes || finallyRes) && (catchRes || finallyRes);	          	   
       return (tryRes || finallyRes) && (catchRes || finallyRes);
      // return tryRes || finallyRes || catchRes ;
  }
   
   /* (non-Javadoc)
 * @see odra.sbql.ast.ASTAdapter#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
 */
   @Override
   public Object visitThrowStatement(ThrowStatement stmt, Object attr)
	throws SBQLException {
    
       return true;
   }

private void checkUnreachableCode(Statement[] stmts, Object attr) throws SBQLException {
      for (int i = 0; i < stmts.length - 1; i++) {
         if ((Boolean) stmts[i].accept(this, attr)) {
            throw new CompilerException("Procedure '" + this.procName + "' :unreachable code", stmts[i], this);
         }
      }
   }

}