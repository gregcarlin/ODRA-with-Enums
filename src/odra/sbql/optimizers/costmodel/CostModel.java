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

	@Override
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException {
	    return commonVisitLiteral(expr, attr);
	}

	@Override
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
	    expr.getConditionExpression().accept(this, attr);
	    expr.getThenExpression().accept(this, attr);
	    expr.getElseExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
	    expr.getConditionExpression().accept(this, attr);
	    expr.getThenExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
	    return commonVisitBinaryExpression(expr, attr);
	}

	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
	    return this.commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException {
	    return commonVisitLiteral(expr, attr);
	}

	@Override
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitLazyFailureExpression(LazyFailureExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException {
	    return commonVisitExpression(expr, attr);
	}   

	@Override
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
	    expr.getProcedureSelectorExpression().accept(this, attr);
	    expr.getArgumentsExpression().accept(this, attr);
	    return this.commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    return this.commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException {
	    return commonVisitLiteral(expr, attr);
	}

	@Override
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
	    expr.getFirstExpression().accept(this, attr);
	    expr.getSecondExpression().accept(this, attr);
	    return this.commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException {
	    return commonVisitLiteral(expr, attr);
	}

	@Override
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException {
	    return commonVisitAlgebraicExpression(expr, attr);
	}
	
	@Override
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
	    expr.query.accept(this, attr);
	    expr.pattern.accept(this, attr);
	    expr.module.accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitVariableDeclarationStatement(VariableDeclarationStatement stmt, Object attr) throws SBQLException {
	    stmt.getInitExpression().accept(this, attr);
	    return this.commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitBlockStatement(BlockStatement stmt, Object attr) throws SBQLException {
	    stmt.getStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    stmt.getStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {
	    stmt.getStatement().accept(this, attr);
	    stmt.getExpression().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {
	    stmt.getInitExpression().accept(this, attr);
	    stmt.getConditionalExpression().accept(this, attr);
	    stmt.getIncrementExpression().accept(this, attr);
	    stmt.getStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {  
	    return commonVisitBinaryExpression(expr, attr);
	}

	@Override
	public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    stmt.getStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    stmt.getIfStatement().accept(this, attr);
	    stmt.getElseStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    stmt.getStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
	    stmt.getFirstStatement().accept(this, attr);
	    stmt.getSecondStatement().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	@Override
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException {
	    return commonVisitLiteral(expr, attr);
	}

	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
	    return commonVisitBinaryExpression(expr, attr);
	}

	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException {
	    return commonVisitBinaryExpression(expr, attr);
	}
	  
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
	    return commonVisitBinaryExpression(expr, attr);
	}

	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
	    return commonVisitBinaryExpression(expr, attr);
	}

	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression node, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(node, attr);
	}

	@Override
	public Object visitLeavesByExpression(LeavesByExpression node, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(node, attr);
	}

	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression node, Object attr) throws SBQLException {
	    return commonVisitNonAlgebraicExpression(node, attr);
	}

	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr) throws SBQLException {      
	    return commonVisitUnaryExpression(expr, attr);
	}
	
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr) throws SBQLException {      
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitTryCatchFinallyStatement(TryCatchFinallyStatement stmt, Object attr) throws SBQLException {
	    stmt.getTryStatement().accept(this, attr);
	    for(SingleCatchBlock cb : stmt.getCatchBlocks().flattenCatchBlocks())
	        cb.getStatement().accept(this, attr);
	    stmt.getFinallyStatement().accept(this, attr);

	    return this.commonVisitStatement(stmt, attr);
	}
	
	@Override
	public Object visitThrowStatement(ThrowStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    return commonVisitStatement(stmt, attr);
	}

	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr) throws SBQLException {
	    return commonVisitParallelExpression(expr, attr);
	}

	@Override
	public Object visitTransactionAbortStatement(TransactionAbortStatement stmt, Object attr) throws SBQLException {
	    return this.commonVisitStatement(stmt, attr);
	}
	
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr, Object attr) throws SBQLException {     
	    return commonVisitBinaryExpression(expr, attr);
	}
	
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr) throws SBQLException {
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
