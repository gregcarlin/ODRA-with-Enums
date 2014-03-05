package odra.transactions.ast;

import java.util.Iterator;

import odra.sbql.SBQLException;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.ModuleDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.ProcedureFieldDeclaration;
import odra.sbql.ast.declarations.RecordDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
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
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.ExistsExpression;
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
import odra.sbql.ast.expressions.LeavesByExpression;
import odra.sbql.ast.expressions.LeavesUniqueByExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
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
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TransactionAbortStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;

public final class TransactionASTVisitor extends ITransactionASTVisitor {

	private TransactionASTVisitor() {
	}

	public static ITransactionASTVisitor getInstance() {
		return new TransactionASTVisitor();
	}

	public Object visitProcedureDeclaration(ProcedureDeclaration declProcedure, Object attr) throws SBQLException {
		if (declProcedure.isTransactionCapableMainASTNode()) {
			Statement stmt = declProcedure.getStatement();
			stmt.setTransactionCapableParentASTNode(declProcedure);
			stmt.accept(this, attr);
		}
		return returnCommonValue();
	}

	public Object visitBlockStatement(BlockStatement stmtBlock, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtBlock, attr);
	}

	public Object visitSequentialStatement(SequentialStatement stmtSeq, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtSeq, attr);
	}

	public Object visitDoWhileStatement(DoWhileStatement stmtDoWhile, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtDoWhile, attr);
	}

	public Object visitForEachStatement(ForEachStatement stmtForEach, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtForEach, attr);
	}

	public Object visitForStatement(ForStatement stmtFor, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtFor, attr);
	}

	public Object visitIfElseStatement(IfElseStatement stmtIfElse, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtIfElse, attr);
	}

	public Object visitIfStatement(IfStatement stmtIf, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtIf, attr);
	}

	public Object visitWhileStatement(WhileStatement stmtWhile, Object attr) throws SBQLException {
		return conveyTransactionCapabilitiesToChildrenASTNodes(stmtWhile, attr);
	}

	private static Object returnCommonValue() {
		return null;
	}

	private Object conveyTransactionCapabilitiesToChildrenASTNodes(ITransactionCapableASTNode superNode, Object attr)
				throws SBQLException {
		if (superNode.hasTransactionCapableParentASTNode()) {
			ITransactionCapableASTNode transParentAst = superNode.getTransactionCapableParentASTNode();
			Iterator<ITransactionCapableASTNode> i = superNode.getTransactionCapableChildrenASTNodes().iterator();
			while (i.hasNext()) {
				ITransactionCapableASTNode child = i.next();
				child.setTransactionCapableParentASTNode(transParentAst);
				child.accept(this, attr);
			}
		}
		return returnCommonValue();
	}

	private Object skipConveyingTransactionCapabilitiesToChildrenASTNodes(ITransactionCapableASTNode node, Object attr) {
		return returnCommonValue();
	}

	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr, Object attr)
				throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitClassDeclaration(ClassDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitClassFieldDeclaration(ClassFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitClassInstanceDeclaration(ClassInstanceDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitCloseUniqueByExpression(CloseUniqueByExpression decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitLeavesByExpression(LeavesByExpression decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitMethodFieldDeclaration(MethodFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitModuleDeclaration(ModuleDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitNamedTypeDeclaration(NamedTypeDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitProcedureFieldDeclaration(ProcedureFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitRecordDeclaration(RecordDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitRecordTypeDeclaration(RecordTypeDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitSessionVariableFieldDeclaration(SessionVariableFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitTransactionAbortStatement(TransactionAbortStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitTypeDefDeclaration(TypeDefDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitTypeDefFieldDeclaration(TypeDefFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitVariableDeclaration(VariableDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitVariableDeclarationStatement(VariableDeclarationStatement stmt, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	public Object visitVariableFieldDeclaration(VariableFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitViewDeclaration(ViewDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}

	public Object visitViewFieldDeclaration(ViewFieldDeclaration decl, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(decl, attr);
	}


	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException
	{
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	public Object visitAtMostExpression(AtMostExpression expr, Object attr)
		throws SBQLException {
	    return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    
	    return conveyTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	 */
	public Object visitThrowStatement(ThrowStatement stmt, Object attr)
		throws SBQLException
	{
	    return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(stmt, attr);
	}
	
	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr) throws SBQLException {
		return this.skipConveyingTransactionCapabilitiesToChildrenASTNodes(expr, attr);
	}
	
}