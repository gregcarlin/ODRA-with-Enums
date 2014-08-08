package odra.sbql.optimizers.costmodel;

import java.util.Vector;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AtLeastExpression;
import odra.sbql.ast.expressions.AtMostExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CastExpression;
import odra.sbql.ast.expressions.CloseByExpression;
import odra.sbql.ast.expressions.CloseUniqueByExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.CreateLocalExpression;
import odra.sbql.ast.expressions.CreatePermanentExpression;
import odra.sbql.ast.expressions.CreateTemporalExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DeserializeOidExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.ExternalProcedureCallExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.InsertExpression;
import odra.sbql.ast.expressions.InstanceOfExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.LazyFailureExpression;
import odra.sbql.ast.expressions.LeavesByExpression;
import odra.sbql.ast.expressions.LeavesUniqueByExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.RangeAsExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.RenameExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.SerializeOidExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToDateExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
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
import odra.sbql.ast.statements.TransactionAbortStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.optimizers.queryrewrite.index.SingleIndexFitter;

/** 
 * 
 * A cost model for optimizations and a "best" index selecting method.
 * 
 * @author tkowals, Greg Carlin
 * 
 */


public class CostModel extends TraversingASTAdapter {
    private static final boolean WARN = true; // TODO link up with some server setting?

	private CostModel() {
		
	}
	
	public static CostModel getCostModel() {
		return new CostModel();
	}
	
	private double estimate = 0.0;
	
	/**
	 * Estimates the running time of a given query. Note that estimates are not absolute, but they can be compared.
	 * 
	 * @param query
	 * @param module
	 * @return
	 */
	public double estimate(ASTNode query, DBModule module) {
	    query.accept(this, null);
	    return estimate;
	}
	
	private void warn() {
	    if(WARN) System.out.println("WARNING: Unsupported operator encountered.");
	}
	
	@Override
	protected Object commonVisitStatement(Statement stmt, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	protected Object commonVisitExpression(Expression expr, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	protected Object commonVisitLiteral(Expression expr, Object attr) throws SBQLException {
	    // do nothing
	    return null;
	}
	
	@Override
	protected Object commonVisitUnaryExpression(UnaryExpression expr, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	protected Object commonVisitBinaryExpression(BinaryExpression expr, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	protected Object commonVisitAlgebraicExpression(BinaryExpression expr, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	protected Object commonVisitNonAlgebraicExpression(NonAlgebraicExpression expr, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	protected Object commonVisitParallelExpression(ParallelExpression expr, Object attr) throws SBQLException {
	    warn();
	    return null;
	}

	@Override
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
	    // TODO implement as
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	public Object visitAvgExpression(AvgExpression expr, Object attr)
	        throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	public Object visitBooleanExpression(BooleanExpression expr, Object attr)
	        throws SBQLException {
	    return commonVisitLiteral(expr, attr);
	}

	public Object visitCommaExpression(CommaExpression expr, Object attr)
	        throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	public Object visitIfThenElseExpression(IfThenElseExpression expr,
	        Object attr) throws SBQLException {
	    expr.getConditionExpression().accept(this, attr);
	    expr.getThenExpression().accept(this, attr);
	    expr.getElseExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	public Object visitIfThenExpression(IfThenExpression expr,
	        Object attr) throws SBQLException {
	    expr.getConditionExpression().accept(this, attr);
	    expr.getThenExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	public Object visitCountExpression(CountExpression expr, Object attr)
	        throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
	 */
	 @Override
	 public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {

	    return commonVisitUnaryExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions.CreateLocalExpression, java.lang.Object)
	 */
	 @Override
	 public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {

	    return commonVisitUnaryExpression(expr, attr);
	 }

	 /* (non-Javadoc)
	  * @see odra.sbql.ast.ASTAdapter#visitCreatePermanentExpression(odra.sbql.ast.expressions.CreatePermanentExpression, java.lang.Object)
	  */
	 @Override
	 public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 /* (non-Javadoc)
	  * @see odra.sbql.ast.ASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.expressions.CreateTemporalExpression, java.lang.Object)
	  */
	 @Override
	 public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
	     expr.getExpression().accept(this, attr);
	     return commonVisitExpression(expr, attr);
	 }

	 public Object visitDerefExpression(DerefExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }



	 /* (non-Javadoc)
	  * @see odra.sbql.ast.ASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression, java.lang.Object)
	  */
	 @Override
	 public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
	     expr.getExpression().accept(this, attr);
	     return commonVisitExpression(expr, attr);
	 }

	 /* (non-Javadoc)
	  * @see odra.sbql.ast.ASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions.InsertCopyExpression, java.lang.Object)
	  */
	 @Override
	 public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
	     return commonVisitBinaryExpression(expr, attr);
	 }

	 public Object visitRefExpression(RefExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 /* (non-Javadoc)
	  * @see odra.sbql.ast.ASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression, java.lang.Object)
	  */
	 @Override
	 public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 public Object visitDotExpression(DotExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 public Object visitEmptyStatement(EmptyStatement stmt, Object attr)
	         throws SBQLException {
	     return commonVisitStatement(stmt, attr);
	 }

	 public Object visitEmptyExpression(EmptyExpression expr, Object attr)
	         throws SBQLException {
	     return this.commonVisitExpression(expr, attr);
	 }

	 public Object visitEqualityExpression(EqualityExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 public Object visitExistsExpression(ExistsExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitForAllExpression(ForAllExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 public Object visitGroupAsExpression(GroupAsExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitInExpression(InExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 public Object visitIntegerExpression(IntegerExpression expr, Object attr)
	         throws SBQLException {

	     return commonVisitLiteral(expr, attr);
	 }

	 public Object visitIntersectExpression(IntersectExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 public Object visitJoinExpression(JoinExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 public Object visitLazyFailureExpression(LazyFailureExpression expr,
	         Object attr) throws SBQLException {
	     // TODO Auto-generated method stub
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitMaxExpression(MaxExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitMinExpression(MinExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitMinusExpression(MinusExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 public Object visitNameExpression(NameExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitExpression(expr, attr);
	 }

	 public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitExpression(expr, attr);
	 }   

	 public Object visitOrderByExpression(OrderByExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 public Object visitProcedureCallExpression(ProcedureCallExpression expr,
	         Object attr) throws SBQLException {
	     expr.getProcedureSelectorExpression().accept(this, attr);
	     expr.getArgumentsExpression().accept(this, attr);
	     return this.commonVisitExpression(expr, attr);
	 }

	 //TW
	 public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr,
	         Object attr) throws SBQLException {
	     expr.getLeftExpression().accept(this, attr);
	     expr.getRightExpression().accept(this, attr);
	     return this.commonVisitExpression(expr, attr);
	 }

	 public Object visitRealExpression(RealExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitLiteral(expr, attr);
	 }

	 public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt,
	         Object attr) throws SBQLException {
	     stmt.getExpression().accept(this, attr);
	     return commonVisitStatement(stmt, attr);
	 }

	 public Object visitReturnWithoutValueStatement(
	         ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
	     return commonVisitStatement(stmt, attr);
	 }

	 public Object visitSequentialExpression(SequentialExpression expr,
	         Object attr) throws SBQLException {
	     expr.getFirstExpression().accept(this, attr);
	     expr.getSecondExpression().accept(this, attr);
	     return this.commonVisitExpression(expr, attr);
	 }

	 public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr,
	         Object attr) throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr,
	         Object attr) throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitStringExpression(StringExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitLiteral(expr, attr);
	 }

	 public Object visitSumExpression(SumExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitToRealExpression(ToRealExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitToStringExpression(ToStringExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException
	 {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitUnionExpression(UnionExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 public Object visitUniqueExpression(UniqueExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitUnaryExpression(expr, attr);
	 }

	 public Object visitWhereExpression(WhereExpression expr, Object attr)
	         throws SBQLException {
	     return commonVisitNonAlgebraicExpression(expr, attr);
	 }

	 /*
	  * (non-Javadoc)
	  * 
	  * @see odra.sbql.ast.ASTAdapter#visitRangeExpression(odra.sbql.ast.expressions.RangeExpression,
	  *      java.lang.Object)
	  */
	  @Override
	  public Object visitRangeExpression(RangeExpression expr, Object attr)
	          throws SBQLException {
	     return commonVisitAlgebraicExpression(expr, attr);
	 }

	 /*
	  * (non-Javadoc)
	  * 
	  * @see odra.sbql.ast.ASTAdapter#visitToBagExpression(odra.sbql.ast.expressions.ToBagExpression,
	  *      java.lang.Object)
	  */
	  @Override
	  public Object visitToBagExpression(ToBagExpression expr, Object attr)
	          throws SBQLException {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitToSingleExpression(odra.sbql.ast.expressions.ToSingleExpression,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitToSingleExpression(ToSingleExpression expr, Object attr)
	          throws SBQLException {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitBagExpression(odra.sbql.ast.expressions.BagExpression,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitBagExpression(BagExpression expr, Object attr)
	          throws SBQLException {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitStructExpression(odra.sbql.ast.expressions.StructExpression,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitStructExpression(StructExpression expr, Object attr)
	          throws SBQLException {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitExecSqlExpression(odra.sbql.ast.expressions.ExecSqlExpression,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
	      expr.query.accept(this, attr);
	      expr.pattern.accept(this, attr);
	      expr.module.accept(this, attr);
	      return commonVisitExpression(expr, attr);
	  }



	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitVariableDeclarationStatement(VariableDeclarationStatement stmt, Object attr) throws SBQLException {


	      stmt.getInitExpression().accept(this, attr);
	      return this.commonVisitStatement(stmt, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitBlockStatement(BlockStatement stmt, Object attr)
	          throws SBQLException {
	      stmt.getStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitExpressionStatement(odra.sbql.ast.statements.ExpressionStatement,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitExpressionStatement(ExpressionStatement stmt, Object attr)
	          throws SBQLException {
	      stmt.getExpression().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitForEachStatement(odra.sbql.ast.statements.ForEachStatement,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitForEachStatement(ForEachStatement stmt, Object attr)
	          throws SBQLException {
	      stmt.getExpression().accept(this, attr);
	      stmt.getStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitDoWhileStatement(odra.sbql.ast.statements.DoWhileStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {
	      stmt.getStatement().accept(this, attr);
	      stmt.getExpression().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitForStatement(odra.sbql.ast.statements.ForStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {
	      stmt.getInitExpression().accept(this, attr);
	      stmt.getConditionalExpression().accept(this, attr);
	      stmt.getIncrementExpression().accept(this, attr);
	      stmt.getStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
	      return commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
	      return commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitInsertExpression(odra.sbql.ast.expressions.InsertExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {  
	      return commonVisitBinaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitWhileStatement(odra.sbql.ast.statements.WhileStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {
	      stmt.getExpression().accept(this, attr);
	      stmt.getStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitIfElseStatement(IfElseStatement stmt, Object attr)
	          throws SBQLException {
	      stmt.getExpression().accept(this, attr);
	      stmt.getIfStatement().accept(this, attr);
	      stmt.getElseStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitIfStatement(odra.sbql.ast.statements.IfStatement,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitIfStatement(IfStatement stmt, Object attr)
	          throws SBQLException {
	      stmt.getExpression().accept(this, attr);
	      stmt.getStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see odra.sbql.ast.ASTAdapter#visitSequentialStatement(odra.sbql.ast.statements.SequentialStatement,
	   *      java.lang.Object)
	   */
	  @Override
	  public Object visitSequentialStatement(SequentialStatement stmt, Object attr)
	          throws SBQLException {
	      stmt.getFirstStatement().accept(this, attr);
	      stmt.getSecondStatement().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  @Override
	  public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException
	  {
	      return commonVisitLiteral(expr, attr);
	  }

	  @Override
	  public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException
	  {
	      return commonVisitBinaryExpression(expr, attr);
	  }

	  @Override
	  public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException
	  {
	      return commonVisitBinaryExpression(expr, attr);
	  }
	  public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException
	  {
	      return commonVisitBinaryExpression(expr, attr);
	  }

	  public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException
	  {
	      return commonVisitBinaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitCloseUniqueByExpression(odra.sbql.ast.expressions.CloseUniqueByExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitCloseUniqueByExpression(CloseUniqueByExpression node, Object attr) throws SBQLException {
	      return commonVisitNonAlgebraicExpression(node, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitLeavesByExpression(odra.sbql.ast.expressions.LeavesByExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitLeavesByExpression(LeavesByExpression node, Object attr) throws SBQLException {
	      return commonVisitNonAlgebraicExpression(node, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitLeavesUniqueByExpression(odra.sbql.ast.expressions.LeavesUniqueByExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression node, Object attr) throws SBQLException {
	      return commonVisitNonAlgebraicExpression(node, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitLeavesUniqueByExpression(odra.sbql.ast.expressions.LeavesUniqueByExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException
	  {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitAtMostExpression(AtMostExpression expr, Object attr)
	          throws SBQLException {      
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitAtLeastExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitAtLeastExpression(AtLeastExpression expr, Object attr)
	          throws SBQLException {      
	      return commonVisitUnaryExpression(expr, attr);
	  }
	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitTryCatchFinallyStatement(
	          TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	  {
	      stmt.getTryStatement().accept(this, attr);
	      for(SingleCatchBlock cb : stmt.getCatchBlocks().flattenCatchBlocks())
	          cb.getStatement().accept(this, attr);
	      stmt.getFinallyStatement().accept(this, attr);

	      return this.commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitThrowStatement(ThrowStatement stmt, Object attr)
	          throws SBQLException
	  {
	      stmt.getExpression().accept(this, attr);
	      return commonVisitStatement(stmt, attr);
	  }

	  public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr)
	          throws SBQLException {
	      return commonVisitParallelExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitTransactionAbortStatement(odra.sbql.ast.statements.TransactionAbortStatement, java.lang.Object)
	   */
	  @Override
	  public Object visitTransactionAbortStatement(
	          TransactionAbortStatement stmt, Object attr)
	                  throws SBQLException {
	      return this.commonVisitStatement(stmt, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitRangeAsExpression(odra.sbql.ast.expressions.RangeAsExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
	      return commonVisitUnaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitDeserializeOidExpression(DeserializeOidExpression expr,
	          Object attr) throws SBQLException {     
	      return commonVisitBinaryExpression(expr, attr);
	  }

	  /* (non-Javadoc)
	   * @see odra.sbql.ast.ASTAdapter#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	   */
	  @Override
	  public Object visitRenameExpression(RenameExpression expr, Object attr)
	          throws SBQLException {
	      return commonVisitUnaryExpression(expr, attr);
	  }
	
	public double indexSelectivity(SingleIndexFitter index, boolean[] combination) {
		double selectivity = 1;
		for(Integer cardOfEqualCondKey: index.getEqualCondKeysCard(combination)) {
			if (cardOfEqualCondKey == Integer.MAX_VALUE)
				selectivity *= 0.02;
			else 
				selectivity *= 1.0 / (cardOfEqualCondKey);
		}
		for(Integer cardOfInCondKey: index.getInCondKeysCard(combination)) {
			if (cardOfInCondKey == Integer.MAX_VALUE)
				selectivity *= 0.1;
			else 
				selectivity *= 1.0 / (cardOfInCondKey); 
		}
		return selectivity * Math.pow(0.5, index.countRangeConditions(combination))
				* Math.pow(0.25, index.countLimitedRangeConditions(combination));
	}
	
	public int indexSelector(Vector<SingleIndexFitter> indices, boolean[] combination) {
		if (indices.size() == 1)
			return 0;
		
		// table of approximate indices selectivity 
		double[] idxsel = new double[indices.size()];
		
		int minIS = 0;
		
		for(int i = 0; i < indices.size(); i++) {
			idxsel[i] = indexSelectivity(indices.elementAt(i), combination);
			if (idxsel[i] < idxsel[minIS])
				minIS = i;
		}
		
		return minIS;
	}

	public double indicesAlternativeSelectivity(double selectivity1, double selectivity2) {
		return selectivity1 + selectivity2;
	}
	
	public static final double INDEXING_THRESHOLD_SELECTIVITY = 0.51;
	
}
