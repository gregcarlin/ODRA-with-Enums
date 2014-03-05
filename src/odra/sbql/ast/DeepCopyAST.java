package odra.sbql.ast;

import java.util.Vector;

import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AtLeastExpression;
import odra.sbql.ast.expressions.AtMostExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
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
import odra.sbql.ast.expressions.RenameExpression;
import odra.sbql.ast.expressions.SerializeOidExpression;
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
import odra.sbql.ast.expressions.SequentialExpression;
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
import odra.sbql.ast.statements.SequentialCatchBlock;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.SingleCatchBlock;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TransactionAbortStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.ast.terminals.Name;

/**
 * DeepCopyAST
 * Visitor that performs deep copy of the given AST Node
 * 
 * @author murlewski
 */
public class DeepCopyAST extends TraversingASTAdapter
{

	private boolean copySignatures = true;
	 
//	public DeepCopyAST() {
//	}
	
	public DeepCopyAST(boolean copySignatures) {
		super();
		this.copySignatures = copySignatures;
	}

	public static ASTNode copy(ASTNode ast) throws SBQLException {
		return (ASTNode) ast.accept(new DeepCopyAST(true), null);
	}
	
	public static ASTNode copyWithoutSign(ASTNode ast) throws SBQLException {
		return (ASTNode) ast.accept(new DeepCopyAST(false), null);
	}
	
	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		JoinExpression copy = new JoinExpression(e1, e2);
		commonNonAlgebraicExpressionCopy(expr, copy); 
		return copy;
	}

	@Override
	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException
	{
		RealExpression copy = new RealExpression(expr.getLiteral());
		commonExpressionCopy(expr, copy);
		return copy;

	}

	@Override
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException
	{
		StringExpression copy = new StringExpression(expr.getLiteral());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException
	{

		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		UnionExpression copy = new UnionExpression(e1, e2);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException
	{
		BooleanExpression copy = new BooleanExpression(expr.getLiteral());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		EqualityExpression copy = new EqualityExpression(e1, e2, expr.O);
		commonExpressionCopy(expr, copy);
		return copy;
	}
	

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException
	{

		NameExpression copy = new NameExpression(expr.name());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException
	{

		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		WhereExpression copy = new WhereExpression(e1, e2);
		commonNonAlgebraicExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		SimpleBinaryExpression copy = new SimpleBinaryExpression(e1, e2,expr.O);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		SimpleUnaryExpression copy = new SimpleUnaryExpression(e,expr.O);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException
	{
		IntegerExpression copy = new IntegerExpression(expr.getLiteral());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		DotExpression copy = new DotExpression(e1, e2);
		commonNonAlgebraicExpressionCopy(expr, copy);
		return copy;

	}

	@Override
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		DerefExpression derefExp = new DerefExpression(e);

		return derefExp;
	}

	@Override
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		AsExpression copy= new AsExpression(e, expr.name());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		AvgExpression copy = new AvgExpression(e);
		commonExpressionCopy(expr, copy);
		return copy;

	}

	@Override
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		CommaExpression copy = new CommaExpression(e1, e2);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		CountExpression copy = new CountExpression(e);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		ExistsExpression copy = new ExistsExpression(e);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		GroupAsExpression copy = new GroupAsExpression(e, expr.name());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		InExpression copy = new InExpression(e1, e2);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		IntersectExpression copy = new IntersectExpression(e1, e2);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		MaxExpression copy = new MaxExpression(e);

		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		MinExpression minExp = new MinExpression(e);

		return minExp;
	}

	@Override
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		MinusExpression copy = new MinusExpression(e1, e2);

		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		RangeExpression copy = new RangeExpression(e1, e2);

		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		SumExpression copy = new SumExpression(e);

		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException
	{
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		UniqueExpression copy = new UniqueExpression(e, expr.isUniqueref());

		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);
		
		AssignExpression copy = new AssignExpression(e1, e2, expr.O);
		copy.operator = expr.operator;
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		BagExpression copy = new BagExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitBlockStatement(BlockStatement stmt, Object attr) throws SBQLException {
		Statement s = (Statement) stmt.getStatement().accept(this, attr);
		
		BlockStatement blockStmt = new BlockStatement(s);
		
		return blockStmt;
	}

	@Override
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {

		return new EmptyExpression();
	}

	@Override
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		
		return new EmptyStatement();
	}

	@Override
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
		StringExpression query = (StringExpression)expr.query.accept(this, attr);
		StringExpression pattern = (StringExpression)expr.pattern.accept(this, attr);
		StringExpression module = (StringExpression)expr.module.accept(this, attr);
		Expression copy = new ExecSqlExpression(query, pattern, module); 
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {
		Expression e = (Expression) stmt.getExpression().accept(this, attr);
		
		ExpressionStatement exprStmt = new ExpressionStatement(e);

		return exprStmt;
	}

	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		ForAllExpression copy = new ForAllExpression(e1, e2);
		
		commonNonAlgebraicExpressionCopy(expr, copy); 
		return copy;
	}

	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);

		ForSomeExpression copy = new ForSomeExpression(e1, e2);
		
		commonNonAlgebraicExpressionCopy(expr, copy); 
		return copy;
	}

	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
		Expression e = (Expression) stmt.getExpression().accept(this, attr);
		Statement s = (Statement) stmt.getStatement().accept(this, attr);

		ForEachStatement forEachStmt = new ForEachStatement(e, s);
		
		return forEachStmt;
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {
		Expression e = (Expression) stmt.getExpression().accept(this, attr);
		Statement s1 = (Statement) stmt.getIfStatement().accept(this, attr);
		Statement s2 = (Statement) stmt.getElseStatement().accept(this, attr);
		
		IfElseStatement ifElseStmt = new IfElseStatement(e, s1, s2);
		
		return ifElseStmt;
	}

	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
		Expression e = (Expression) stmt.getExpression().accept(this, attr);
		Statement s = (Statement) stmt.getStatement().accept(this, attr);

		IfStatement ifStmt = new IfStatement(e, s);
		
		return ifStmt;
	}

	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getConditionExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getThenExpression().accept(this, attr);
		Expression e3 = (Expression) expr.getElseExpression().accept(this, attr);

		IfThenElseExpression copy = new IfThenElseExpression(e1, e2, e3);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getConditionExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getThenExpression().accept(this, attr);

		IfThenExpression copy = new IfThenExpression(e1, e2);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);
		
		OrderByExpression copy = new OrderByExpression(e1, e2);
		
		commonNonAlgebraicExpressionCopy(expr, copy); 
		return copy;
	}

	@Override
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getProcedureSelectorExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getArgumentsExpression().accept(this, attr);
		
		ProcedureCallExpression copy = new ProcedureCallExpression(e1, e2);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		
		return new ReturnWithoutValueStatement();
	}

	@Override
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
		Expression e = (Expression) stmt.getExpression().accept(this, attr);
		
		ReturnWithValueStatement returnValExpr = new ReturnWithValueStatement(e);
		
		return returnValExpr;
	}

	@Override
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getFirstExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getSecondExpression().accept(this, attr);
		
		SequentialExpression copy = new SequentialExpression(e1, e2);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
		Statement s1 = (Statement) stmt.getFirstStatement().accept(this, attr);
		Statement s2 = (Statement) stmt.getSecondStatement().accept(this, attr);
		
		SequentialStatement seqStmt = new SequentialStatement(s1, s2);
		
		return seqStmt;
	}

	@Override
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		StructExpression copy = new StructExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToBagExpression copy = new ToBagExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToBooleanExpression copy = new ToBooleanExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToIntegerExpression copy = new ToIntegerExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToRealExpression copy = new ToRealExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToSingleExpression copy = new ToSingleExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToStringExpression copy = new ToStringExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}
	
	@Override
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		
		ToDateExpression copy = new ToDateExpression(e);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException
	{
		Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
		
		RandomExpression copy = new RandomExpression(e1, e2);
		
		commonExpressionCopy(expr, copy);
		return copy;
	}
	
	@Override
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException {
	
		DateExpression copy = new DateExpression(expr.getLiteral());
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		RefExpression copy = new RefExpression(e);

		commonExpressionCopy(expr, copy);
		return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement, java.lang.Object)
	 */
	@Override
	public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
	    
	    return new BreakStatement();
	}

	

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCastExpression(odra.sbql.ast.expressions.CastExpression, java.lang.Object)
	 */
	@Override
	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    Expression copy =  new CastExpression(e1,e2);
	    
	    commonExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    CloseByExpression copy =  new CloseByExpression(e1,e2);
	    
	    commonNonAlgebraicExpressionCopy(expr, copy); 
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCloseUniqueByExpression(odra.sbql.ast.expressions.CloseUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    CloseUniqueByExpression copy = new CloseUniqueByExpression(e1,e2);
	    
	    commonNonAlgebraicExpressionCopy(expr, copy); 
	    return copy;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitLeavesByExpression(odra.sbql.ast.expressions.LeavesByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesByExpression(LeavesByExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    LeavesByExpression copy = new LeavesByExpression(e1,e2);
	    
	    commonNonAlgebraicExpressionCopy(expr, copy); 
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitLeavesUniqueByExpression(odra.sbql.ast.expressions.LeavesUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    LeavesUniqueByExpression copy = new LeavesUniqueByExpression(e1,e2);
	    
	    commonNonAlgebraicExpressionCopy(expr, copy); 
	    return copy;
	}

	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement, java.lang.Object)
	 */
	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
	    
	    return new ContinueStatement();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
	    Expression e = (Expression)expr.getExpression().accept(this, attr);
	    CreateExpression copy = new CreateExpression(expr.name(), e);
	    commonCreateExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions.CreateLocalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
	    Expression e = (Expression)expr.getExpression().accept(this, attr);
	    CreateExpression copy = new CreateLocalExpression(expr.name(), e);
	    commonCreateExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCreatePermanentExpression(odra.sbql.ast.expressions.CreatePermanentExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
	    Expression e = (Expression)expr.getExpression().accept(this, attr);
	    CreateExpression copy = new CreatePermanentExpression(expr.name(), e);
	    commonCreateExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.expressions.CreateTemporalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
	    Expression e = (Expression)expr.getExpression().accept(this, attr);
	    CreateExpression copy = new CreateTemporalExpression(expr.name(), e);
	    commonCreateExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDateprecissionExpression(odra.sbql.ast.expressions.DateprecissionExpression, java.lang.Object)
	 */
	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    StringExpression e2 = (StringExpression)expr.getRightExpression().accept(this, attr);
	    DateprecissionExpression copy = new DateprecissionExpression(e1,e2);
	    commonExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
	    Expression e = (Expression)expr.getExpression().accept(this, attr);
	    DeleteExpression copy = new DeleteExpression(e);
	    commonExpressionCopy(expr, copy);
	    return copy;
	}


	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDoWhileStatement(odra.sbql.ast.statements.DoWhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {
	    Expression e = (Expression) stmt.getExpression().accept(this, attr);
	    Statement s = (Statement)stmt.getStatement().accept(this, attr);
	    return new DoWhileStatement(s,e);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitForStatement(odra.sbql.ast.statements.ForStatement, java.lang.Object)
	 */
	@Override
	public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {
	    Expression e1 = (Expression)stmt.getInitExpression().accept(this, attr);
	    Expression e2 = (Expression)stmt.getConditionalExpression().accept(this, attr);
	    Expression e3 = (Expression)stmt.getIncrementExpression().accept(this, attr);
	    Statement s = (Statement)stmt.getStatement().accept(this, attr);
	    return new ForStatement(e1,e2,e3,s);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions.InsertCopyExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    Expression copy = new InsertCopyExpression(e1,e2, expr.name());
	    commonExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitInsertExpression(odra.sbql.ast.expressions.InsertExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {
	    Expression e1 = (Expression)expr.getLeftExpression().accept(this, attr);
	    Expression e2 = (Expression)expr.getRightExpression().accept(this, attr);
	    Expression copy = new InsertExpression(e1,e2);
	    commonExpressionCopy(expr, copy);
	    return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitInstanceOfExpression(odra.sbql.ast.expressions.InstanceOfExpression, java.lang.Object)
	 */
	@Override
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
	    // TODO Auto-generated method stub
	    return super.visitInstanceOfExpression(expr, attr);
	}

	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitWhileStatement(odra.sbql.ast.statements.WhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {
	    Expression e = (Expression) stmt.getExpression().accept(this, attr);
	    Statement s = (Statement)stmt.getStatement().accept(this, attr);
	    return new WhileStatement(e,s);
	}

	private void commonCreateExpressionCopy(CreateExpression source, CreateExpression copy) {
	    copy.importModuleRef = source.importModuleRef;
	    copy.declaration_environment = source.declaration_environment;
	    commonExpressionCopy(source, copy);
	}
	private void commonNonAlgebraicExpressionCopy(NonAlgebraicExpression source, NonAlgebraicExpression copy) {
	    copy.setEnvsInfo(source.getEnvsInfo());
	    
	    commonExpressionCopy(source, copy);
	}
	private void commonExpressionCopy(Expression source, Expression copy) {
	    
		for(OID oid : source.links)
			copy.links.add(oid);

	    copy.setMarked(source.isMarked());
	    copy.isViewSubstituted = source.isViewSubstituted;
	    copy.line = source.line;
	    copy.column = source.column;
	    if (copySignatures)
		
		   copy.setSignature(source.getSignature().clone());
		
	    copy.wrapper = source.wrapper;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr)
		throws SBQLException {
	    Expression e = (Expression) expr.getExpression().accept(this, attr);
		
	    AtMostExpression copy = new AtMostExpression(e, expr.getMaxCardinality());
		
	    commonExpressionCopy(expr, copy);
	    return copy;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtLeastExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr)
		throws SBQLException {
	    Expression e = (Expression) expr.getExpression().accept(this, attr);
		
	    AtLeastExpression copy = new AtLeastExpression(e, expr.getMinCardinality());
		
	    commonExpressionCopy(expr, copy);
	    return copy;
	}
	@Override
	public Object visitLazyFailureExpression(LazyFailureExpression expr,
			Object attr) throws SBQLException {
		
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		LazyFailureExpression copy = new LazyFailureExpression(e);
		commonExpressionCopy(expr, copy);
		return copy;

	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	@Override
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    BlockStatement tryStmt = (BlockStatement)stmt.getTryStatement().accept(this, attr);
	    Statement finallyStmt = (Statement)stmt.getFinallyStatement().accept(this, attr);
	    Vector<SingleCatchBlock> catchBlocks = new Vector<SingleCatchBlock>();
	    for(SingleCatchBlock cb : stmt.getCatchBlocks().flattenCatchBlocks())
	    {
		BlockStatement catchStatement = (BlockStatement)cb.getStatement().accept(this, attr);
		catchBlocks.add(new SingleCatchBlock(cb.getExceptionName(), cb.getExceptionTypeName(), catchStatement));
	    }
	    if(catchBlocks.size() > 1){
		SequentialCatchBlock blocks = new SequentialCatchBlock(catchBlocks.get(0), catchBlocks.get(1));
		for(int i = 2; i < catchBlocks.size(); i++){
		    blocks = new SequentialCatchBlock(blocks, catchBlocks.get(i));
		}
	    }else 
		return  new TryCatchFinallyStatement(tryStmt, catchBlocks.get(0), finallyStmt);
	    return super.visitTryCatchFinallyStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement, java.lang.Object)
	 */
	@Override
	public Object visitVariableDeclarationStatement(
		VariableDeclarationStatement stmt, Object attr)
		throws SBQLException
	{
	    Expression init = (Expression)stmt.getInitExpression().accept(this, attr);
	    VariableDeclarationStatement copy = new VariableDeclarationStatement(stmt.getVariableName(), stmt.getTypeDeclaration(), stmt.getMinCard(), stmt.getMaxCard(), stmt.getReflevel(), init);
	    return copy;
	}
	
	
	
	@Override
	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr) throws SBQLException
	{
		return copyParallelExpression(new ParallelUnionExpression() ,expr,attr);
	}

	private Object copyParallelExpression(ParallelExpression copy, ParallelExpression expr,  Object attr) 
	{
		for(Expression subexpr: expr.getParallelExpressions())
			copy.addExpression((Expression) subexpr.accept(this, attr));
		
		commonExpressionCopy(expr, copy);
		return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRangeAsExpression(odra.sbql.ast.expressions.RangeAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		RangeAsExpression copy = new RangeAsExpression(e, expr.name());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRemoteQueryExpression(odra.sbql.ast.expressions.RemoteQueryExpression, java.lang.Object)
	 */
	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr,
			Object attr) throws SBQLException {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		RemoteQueryExpression copy = new RemoteQueryExpression(e);
		copy.setParmDependent(expr.isParmDependent());
		copy.setParmDependentNames(expr.getParmDependentNames());
		if(expr.isAsynchronous())
			copy.runAsynchronously();
		copy.setRemoteLink(expr.getRemoteLink());
		commonExpressionCopy(expr, copy);
		return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	 */
	@Override
	public Object visitThrowStatement(ThrowStatement stmt, Object attr)
			throws SBQLException {
		Expression e = (Expression)stmt.getExpression().accept(this, attr);
		return new ThrowStatement(e);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTransactionAbortStatement(odra.sbql.ast.statements.TransactionAbortStatement, java.lang.Object)
	 */
	@Override
	public Object visitTransactionAbortStatement(
			TransactionAbortStatement stmt, Object attr) throws SBQLException {
		return new TransactionAbortStatement();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitExternalNameExpression(odra.sbql.ast.expressions.ExternalNameExpression, java.lang.Object)
	 */
	@Override
	public Object visitExternalNameExpression(ExternalNameExpression expr,
			Object attr) throws SBQLException {		
		return new ExternalNameExpression(new Name(expr.name().value()));
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitExternalProcedureCallExpression(odra.sbql.ast.expressions.ExternalProcedureCallExpression, java.lang.Object)
	 */
	@Override
	public Object visitExternalProcedureCallExpression(
			ExternalProcedureCallExpression expr, Object attr)
			throws SBQLException {
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);
		Expression copy = new ExternalProcedureCallExpression(e1,e2);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
		Expression e = (Expression) expr.getExpression().accept(this, attr);
		Expression copy = new SerializeOidExpression(e);
		commonExpressionCopy(expr, copy);
		return super.visitSerializeOidExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr,
			Object attr) throws SBQLException {
		Expression e1 = (Expression) expr.getLeftExpression().accept(this, attr);
		Expression e2 = (Expression) expr.getRightExpression().accept(this, attr);
		DeserializeOidExpression copy = new DeserializeOidExpression(e1, e2);
		commonExpressionCopy(expr, copy);
		return copy;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr)
			throws SBQLException {
		Expression e = expr.getExpression();
		RenameExpression copy = new RenameExpression(e, new Name(expr.name().value()));
		commonExpressionCopy(expr, copy);
		return copy;
	}
	
	
}
