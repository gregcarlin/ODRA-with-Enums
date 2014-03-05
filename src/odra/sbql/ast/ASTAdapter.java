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
import odra.transactions.TransactionCapabilities;

/**
 * Dummy implementation of AST visitor methods.
 * 
 * @author raist
 */

public class ASTAdapter implements ASTVisitor {
    private String modName = "";
	/* (non-Javadoc)
     * @see odra.sbql.ast.ASTVisitor#getSourceModuleName()
     */
    public String getSourceModuleName() {
	return modName;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.ASTVisitor#setSourceModuleName(java.lang.String)
     */
    public void setSourceModuleName(String name) {
	this.modName = name;
	
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitBreakStatement(odra.sbql.ast.statements.BreakStatement,
	 *      java.lang.Object)
	 */
	public Object visitBreakStatement(BreakStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement,
	 *      java.lang.Object)
	 */
	public Object visitContinueStatement(ContinueStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitDerefExpression(DerefExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitRefExpression(RefExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitAsExpression(AsExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitSimpleBinaryExpression(SimpleBinaryExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitSimpleUnaryExpression(SimpleUnaryExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitBooleanExpression(BooleanExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitCommaExpression(CommaExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitIfThenElseExpression(IfThenElseExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitIfThenExpression(IfThenExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitInterfaceDeclaration(InterfaceDeclaration node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}
	
	public Object visitInterfaceFieldDeclaration(InterfaceFieldDeclaration node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}
	
	
	public Object visitDotExpression(DotExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());	}

	public Object visitGroupAsExpression(GroupAsExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitIntegerExpression(IntegerExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitJoinExpression(JoinExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitNameExpression(NameExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitExternalNameExpression(ExternalNameExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitOrderByExpression(OrderByExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitRealExpression(RealExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitStringExpression(StringExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitWhereExpression(WhereExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitReturnWithValueStatement(ReturnWithValueStatement node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitEmptyStatement(EmptyStatement node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitForEachStatement(ForEachStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitForStatement(ForStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitWhileStatement(WhileStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitIfStatement(IfStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitIfElseStatement(IfElseStatement stmt, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitReturnWithoutValueStatement(
			ReturnWithoutValueStatement node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitSequentialStatement(SequentialStatement node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitVariableDeclarationStatement(
			VariableDeclarationStatement node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitBlockStatement(BlockStatement node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitExpressionStatement(ExpressionStatement node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitAvgExpression(AvgExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitCountExpression(CountExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitEqualityExpression(EqualityExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitExistsExpression(ExistsExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitForAllExpression(ForAllExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitForSomeExpression(ForSomeExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitInExpression(InExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitIntersectExpression(IntersectExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitMaxExpression(MaxExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitMinExpression(MinExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitMinusExpression(MinusExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitSumExpression(SumExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitToBooleanExpression(ToBooleanExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitToIntegerExpression(ToIntegerExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitToRealExpression(ToRealExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitToStringExpression(ToStringExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitUnionExpression(UnionExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitUniqueExpression(UniqueExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}


	public Object visitModuleDeclaration(ModuleDeclaration node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitProcedureFieldDeclaration(
			ProcedureFieldDeclaration node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitProcedureDeclaration(ProcedureDeclaration node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitVariableFieldDeclaration(VariableFieldDeclaration node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}
	
	public Object visitVariableDeclaration(VariableDeclaration node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitSessionVariableFieldDeclaration(
			SessionVariableFieldDeclaration node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitDbLinkFieldDeclaration(odra.sbql.ast.declarations.DbLinkFieldDeclaration, java.lang.Object)
	 */
	public Object visitExternalSchemaDefFieldDeclaration(ExternalSchemaDefFieldDeclaration decl,
		Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitDbLinkDeclaration(odra.sbql.ast.declarations.DbLinkDeclaration, java.lang.Object)
	 */
	public Object visitExternalSchemaDefDeclaration(ExternalSchemaDefDeclaration decl, Object attr)
		throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	public Object visitRecordDeclaration(RecordDeclaration node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitTypeDefFieldDeclaration(TypeDefFieldDeclaration node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitTypeDefDeclaration(TypeDefDeclaration node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitNamedTypeDeclaration(NamedTypeDeclaration node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitRecordTypeDeclaration(RecordTypeDeclaration node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitProcedureCallExpression(ProcedureCallExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	// TW
	public Object visitExternalProcedureCallExpression(
			ExternalProcedureCallExpression node, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitSequentialExpression(SequentialExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitEmptyExpression(EmptyExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitAssignExpression(AssignExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitToSingleExpression(ToSingleExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitToBagExpression(ToBagExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitRangeExpression(RangeExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitCreateExpression(CreateExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitCreatePermanentExpression(
			CreatePermanentExpression expr, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitCreateTemporalExpression(CreateTemporalExpression expr,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitCreateLocalExpression(CreateLocalExpression expr,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitInsertExpression(InsertExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitInsertCopyExpression(InsertCopyExpression expr,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression,
	 *      java.lang.Object)
	 */
	public Object visitDeleteExpression(DeleteExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitViewFieldDeclaration(odra.sbql.ast.declarations.ViewFieldDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitViewFieldDeclaration(ViewFieldDeclaration node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitViewDeclaration(odra.sbql.ast.declarations.ViewFieldDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitViewDeclaration(ViewDeclaration node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}


	public Object visitBagExpression(BagExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitStructExpression(StructExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitEnumFieldDeclaration(odra.sbql.ast.declarations.EnumFieldDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitEnumFieldDeclaration(EnumFieldDeclaration decl, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitEnumDeclaration(odra.sbql.ast.declarations.EnumDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitEnumDeclaration(EnumDeclaration decl, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitClassDeclaration(odra.sbql.ast.declarations.ClassDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitClassDeclaration(ClassDeclaration decl, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitClassFieldDeclaration(odra.sbql.ast.declarations.ClassFieldDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitClassFieldDeclaration(ClassFieldDeclaration decl,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitClassInstanceTypeDeclaration(odra.sbql.ast.declarations.ClassInstanceTypeDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitClassInstanceDeclaration(
			ClassInstanceDeclaration decl, Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitMethodFieldDeclaration(odra.sbql.ast.declarations.MethodFieldDeclaration,
	 *      java.lang.Object)
	 */
	public Object visitMethodFieldDeclaration(MethodFieldDeclaration decl,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	public Object visitDateExpression(DateExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitDateprecissionExpression(DateprecissionExpression expr,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitToDateExpression(ToDateExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitRandomExpression(RandomExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitInstanceOfExpression(InstanceOfExpression expr,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitCastExpression(CastExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitCloseByExpression(CloseByExpression expr, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	public Object visitCloseUniqueByExpression(CloseUniqueByExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitLeavesByExpression(LeavesByExpression node, Object attr)
			throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression node,
			Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	private final static String UNIMPLEMENTED_VISIT = "unimplemented visit ";

	public Object visitTransactionCapabilities(TransactionCapabilities decl,
			Object attr) throws ParserException {
		return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	public Object visitTransactionAbortStatement(
			TransactionAbortStatement stmt, Object attr) throws SBQLException {
		return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}

	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException
	{
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}
	
	public Object visitAtMostExpression(AtMostExpression expr, Object attr) throws SBQLException{
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}
	
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr) throws SBQLException{
	    return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}
	
	public Object visitThrowStatement(ThrowStatement stmt, Object attr)
	throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, stmt, this.getClass());
	}
	
	public Object visitParallelUnionExpression(ParallelUnionExpression node, Object attr) throws SBQLException
	{
	    return new ParserException(UNIMPLEMENTED_VISIT, node, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitProcedureHeaderFieldDeclaration(odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration, java.lang.Object)
	 */
	public Object visitProcedureHeaderFieldDeclaration(
		ProcedureHeaderFieldDeclaration decl,
		Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitProcedureHeaderDeclaration(odra.sbql.ast.declarations.ProcedureHeader, java.lang.Object)
	 */
	public Object visitProcedureHeaderDeclaration(ProcedureHeaderDeclaration decl,
		Object attr) throws SBQLException {
	    return new ParserException(UNIMPLEMENTED_VISIT, decl, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitRangeOfExpression(odra.sbql.ast.expressions.RangeOfExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr,
			Object attr) {
		return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
		return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(
			DeserializeOidExpression expr, Object attr)
			throws SBQLException {
		return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr)
			throws SBQLException {
		return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}

	@Override
	public Object visitLazyFailureExpression(LazyFailureExpression expr,
			Object attr) throws SBQLException {
		return new ParserException(UNIMPLEMENTED_VISIT, expr, this.getClass());
	}
	
	
}