package odra.sbql.ast;

import odra.sbql.SBQLException;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.EnumDeclaration;
import odra.sbql.ast.declarations.EnumFieldDeclaration;
import odra.sbql.ast.declarations.InterfaceDeclaration;
import odra.sbql.ast.declarations.InterfaceFieldDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.ModuleDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.ProcedureFieldDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration;
import odra.sbql.ast.declarations.RecordDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefFieldDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
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
import odra.sbql.ast.expressions.RenameExpression;
import odra.sbql.ast.expressions.SerializeOidExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RangeAsExpression;
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
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TransactionAbortStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;

/**
 * Visitor methods of SBQL AST nodes.
 * 
 * @author raist
 */

public interface ASTVisitor {
    	public void setSourceModuleName(String name);
    	public String getSourceModuleName();
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException;
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException;
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException;
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException;
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException;
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException;
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException;
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException;
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException;
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException;
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException;
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException;
	public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException;	
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException;
	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException;
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException;
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException;
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException;
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException;
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException;
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException;
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException;
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException;
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException;
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException;
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException;
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException;
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException;
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException;
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException;
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException;
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException;
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException;
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException;
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException;
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException;
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException;
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException;
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException;
	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr, Object attr) throws SBQLException;	
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException;
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException;
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException;
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException;
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException;
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException;
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException;
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException;
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException;
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException;
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException;
	public Object visitLazyFailureExpression(LazyFailureExpression expr, Object attr) throws SBQLException;

	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr) throws SBQLException;
	
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException;
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException;
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException;
	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr) throws SBQLException;
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException;
	public Object visitIfStatement(IfStatement stmt,Object attr) throws SBQLException;
	public Object visitIfElseStatement(IfElseStatement stmt,Object attr) throws SBQLException;
	public Object visitForEachStatement(ForEachStatement stmt,Object attr) throws SBQLException;
	public Object visitForStatement(ForStatement stmt,Object attr) throws SBQLException;
	public Object visitWhileStatement(WhileStatement stmt,Object attr) throws SBQLException;
	public Object visitDoWhileStatement(DoWhileStatement stmt,Object attr) throws SBQLException;
	public Object visitBreakStatement(BreakStatement stmt,Object attr) throws SBQLException;
	public Object visitContinueStatement(ContinueStatement stmt,Object attr) throws SBQLException;
	public Object visitVariableDeclarationStatement(VariableDeclarationStatement node, Object attr) throws SBQLException;
	public Object visitSessionVariableFieldDeclaration(SessionVariableFieldDeclaration sessionVariableFieldDeclaration,Object attr);
	public Object visitBlockStatement(BlockStatement node, Object attr) throws SBQLException;
	
	public Object visitModuleDeclaration(ModuleDeclaration decl, Object attr) throws SBQLException;
	public Object visitProcedureFieldDeclaration(ProcedureFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitProcedureDeclaration(ProcedureDeclaration decl, Object attr) throws SBQLException;
	public Object visitVariableFieldDeclaration(VariableFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitVariableDeclaration(VariableDeclaration decl, Object attr) throws SBQLException;
	
	public Object visitExternalSchemaDefFieldDeclaration(ExternalSchemaDefFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitExternalSchemaDefDeclaration(ExternalSchemaDefDeclaration decl, Object attr) throws SBQLException;
	
	public Object visitRecordDeclaration(RecordDeclaration decl, Object attr) throws SBQLException;

	public Object visitTypeDefFieldDeclaration(TypeDefFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitTypeDefDeclaration(TypeDefDeclaration decl, Object attr) throws SBQLException;

	public Object visitNamedTypeDeclaration(NamedTypeDeclaration decl, Object attr) throws SBQLException;
	public Object visitRecordTypeDeclaration(RecordTypeDeclaration decl, Object attr) throws SBQLException;
	
	public Object visitViewFieldDeclaration(ViewFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitViewDeclaration(ViewDeclaration decl,Object attr) throws SBQLException;
	
	public Object visitEnumFieldDeclaration(EnumFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitEnumDeclaration(EnumDeclaration decl, Object attr) throws SBQLException;
		
	public Object visitClassDeclaration(ClassDeclaration decl, Object attr) throws SBQLException;
	public Object visitClassFieldDeclaration(ClassFieldDeclaration decl, Object attr) throws SBQLException;
	public Object visitClassInstanceDeclaration(ClassInstanceDeclaration decl, Object attr) throws SBQLException;
	public Object visitMethodFieldDeclaration(MethodFieldDeclaration decl, Object attr) throws SBQLException;
	
	public Object visitInterfaceDeclaration(InterfaceDeclaration decl, Object attr) throws SBQLException;
	public Object visitInterfaceFieldDeclaration(InterfaceFieldDeclaration decl, Object attr) throws SBQLException;
	
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException;
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException;
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException;
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException;
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException;
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException;
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException;
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException;
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException;
	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException;
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException;
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression decl,Object attr) throws SBQLException;
	public Object visitLeavesByExpression(LeavesByExpression decl,Object attr) throws SBQLException;
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression decl,Object attr) throws SBQLException;
	
	public Object visitTransactionAbortStatement(TransactionAbortStatement stmt, Object attr) throws SBQLException;
	public Object visitTryCatchFinallyStatement(TryCatchFinallyStatement stmt, Object attr) throws SBQLException;
	
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException;
	public Object visitAtMostExpression(AtMostExpression expr, Object attr) throws SBQLException;
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr) throws SBQLException;
	public Object visitThrowStatement(ThrowStatement stmt, Object attr) throws SBQLException;
	
	public Object visitProcedureHeaderFieldDeclaration(ProcedureHeaderFieldDeclaration decl, Object attr)throws SBQLException;
	
	public Object visitProcedureHeaderDeclaration(ProcedureHeaderDeclaration decl, Object attr) throws SBQLException;
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) throws SBQLException;
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) throws SBQLException;
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr, Object attr)throws SBQLException;
	public Object visitRenameExpression(RenameExpression expr,Object attr) throws SBQLException;
	
	
}
