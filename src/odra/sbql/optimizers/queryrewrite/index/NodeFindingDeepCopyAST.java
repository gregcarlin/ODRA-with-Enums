package odra.sbql.optimizers.queryrewrite.index;

import java.util.HashSet;
import java.util.Set;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.ast.statements.SequentialStatement;

/**
 * 
 * Visitor that performs deep copy of the given AST Node
 * and prepares AST nodes set which contains copies of orignal nodes given by the parameter.
 * 
 * @author tkowals
 * @version 1.0
 */
class NodeFindingDeepCopyAST extends DeepCopyAST {

	Set<ASTNode> originalNodesSet;
	Set<ASTNode> copyNodesSet = new HashSet<ASTNode>();
		
	public NodeFindingDeepCopyAST(Set<ASTNode> originalNodesSet){
		super(false);
		this.originalNodesSet = originalNodesSet;
	}

	public ASTNode findandcopy(ASTNode ast) throws Exception {
		return (ASTNode) ast.accept(this, null);
	}
	
	public Set<ASTNode> getCopiesSet() {
		return copyNodesSet;
	}	
	//if (filterSet.contains(expr)) return null;	
	
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {		
		ASTNode node = (ASTNode) super.visitAsExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitAssignExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitAvgExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitBagExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitBlockStatement(BlockStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitBlockStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitBooleanExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitCommaExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitCountExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitDerefExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitDotExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitEmptyExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitEmptyStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitEqualityExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitExecSqlExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitExistsExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitExpressionStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitForAllExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitForSomeExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitForEachStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitGroupAsExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitIfElseStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitIfStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitIfThenElseExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitIfThenExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitInExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitIntegerExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitIntersectExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitJoinExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitMaxExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitMinExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitMinusExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitNameExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitOrderByExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitProcedureCallExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitRangeExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitRealExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitReturnWithoutValueStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitReturnWithValueStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitSequentialExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitSequentialStatement(stmt, attr);
		if (originalNodesSet.contains(stmt)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitSimpleBinaryExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitSimpleUnaryExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitStringExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitStructExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitSumExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitToBagExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitToBooleanExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitToIntegerExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitToRealExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitToSingleExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitToStringExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitUnionExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitUniqueExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}

	
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
		ASTNode node = (ASTNode) super.visitWhereExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}
	
	@Override
	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr) throws SBQLException
	{
		ASTNode node = (ASTNode) super.visitParallelUnionExpression(expr, attr);
		if (originalNodesSet.contains(expr)) copyNodesSet.add(node);
		return node;
	}
}
