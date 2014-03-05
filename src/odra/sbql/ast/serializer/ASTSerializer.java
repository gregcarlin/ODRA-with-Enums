package odra.sbql.ast.serializer;



import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBLink;
import odra.network.transport.AutoextendableBuffer;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.declarations.TypeDeclaration;
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
import odra.sbql.ast.serializer.declarations.DeclarationSerializer;
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
import odra.sbql.builder.CompilerException;

/**
 * ASTSerializer 
 * native AST serialization
 * @author Radek Adamus
 *since: 2007-02-02
 *last modified: 2007-04-07
 *@version 1.0
 */
public class ASTSerializer extends TraversingASTAdapter  {
	AutoextendableBuffer buffer;
	boolean withPositionInfo;
	public byte[] writeAST(ASTNode node,  boolean withPositionInfo) throws CompilerException{
		
		buffer = new AutoextendableBuffer();
		
		this.withPositionInfo = withPositionInfo;

		buffer.put(withPositionInfo ? (byte)1 : (byte)0);
		
		
		try
		{
		    node.accept(this, null);
		} catch (Exception e)
		{
		   throw new CompilerException(e);
		}
		
		return buffer.getBytes();
	}
	
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitAsExpression(odra.sbql.ast.expressions.AsExpression, java.lang.Object)
	 */
	@Override
	public Object visitAsExpression(AsExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.AS_EXPRESSION);
		SerializationUtil.serializeString(buffer, node.name().value());
		return super.visitAsExpression(node, attr);
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitAssignExpression(odra.sbql.ast.expressions.AssignExpression, java.lang.Object)
	 */
	@Override
	public Object visitAssignExpression(AssignExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.ASSIGN_EXPRESSION);
		return super.visitAssignExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr)
		throws SBQLException {
	    buffer.put(IASTDescriptor.ATMOST_EXPRESSION);
	    buffer.putInt(expr.getMaxCardinality());
	    return super.visitAtMostExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr)
		throws SBQLException {
	    buffer.put(IASTDescriptor.ATLEAST_EXPRESSION);
	    buffer.putInt(expr.getMinCardinality());
	    return super.visitAtLeastExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitAvgExpression(odra.sbql.ast.expressions.AvgExpression, java.lang.Object)
	 */
	@Override
	public Object visitAvgExpression(AvgExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.AVG_EXPRESSION);
		return super.visitAvgExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitBagExpression(odra.sbql.ast.expressions.BagExpression, java.lang.Object)
	 */
	@Override
	public Object visitBagExpression(BagExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.BAG_EXPRESSION);
		return super.visitBagExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement, java.lang.Object)
	 */
	@Override
	public Object visitBlockStatement(BlockStatement node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.BLOCK_STATEMENT);
		return super.visitBlockStatement(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitBooleanExpression(odra.sbql.ast.expressions.BooleanExpression, java.lang.Object)
	 */
	@Override
	public Object visitBooleanExpression(BooleanExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.BOOLEAN_EXPRESSION);
		buffer.put(node.getLiteral().value() == true ? (byte)1 : (byte)0);
		return super.visitBooleanExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCommaExpression(odra.sbql.ast.expressions.CommaExpression, java.lang.Object)
	 */
	@Override
	public Object visitCommaExpression(CommaExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.COMMA_EXPRESSION);
		return super.visitCommaExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCoditionalExpression(odra.sbql.ast.expressions.IfThenElseExpression, java.lang.Object)
	 */
	@Override
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.IF_THEN_ELSE_EXPRESSION);
		return super.visitIfThenElseExpression(expr, attr);
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitIfThenExpression(odra.sbql.ast.expressions.IfThenExpression, java.lang.Object)
	 */
	@Override
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.IF_THEN_EXPRESSION);
		return super.visitIfThenExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitCountExpression(odra.sbql.ast.expressions.CountExpression, java.lang.Object)
	 */
	@Override
	public Object visitCountExpression(CountExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.COUNT_EXPRESSION);
		return super.visitCountExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CREATE_EXPRESSION);
		SerializationUtil.serializeString(buffer, expr.name().value());
		return super.visitCreateExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions.CreateLocalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CREATE_LOCAL_EXPRESSION);
		SerializationUtil.serializeString(buffer, expr.name().value());
		return super.visitCreateLocalExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreatePermanentExpression(odra.sbql.ast.expressions.CreatePermanentExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CREATE_PERMANENT_EXPRESSION);
		SerializationUtil.serializeString(buffer, expr.name().value());
		return super.visitCreatePermanentExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.expressions.CreateTemporalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CREATE_TEMPORAL_EXPRESSION);
		SerializationUtil.serializeString(buffer, expr.name().value());
		return super.visitCreateTemporalExpression(expr, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
	    buffer.put(IASTDescriptor.DELETE_EXPRESSION);
	    return super.visitDeleteExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDerefExpression(odra.sbql.ast.expressions.DerefExpression, java.lang.Object)
	 */
	@Override
	public Object visitDerefExpression(DerefExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.DEREF_EXPRESSION);
		return super.visitDerefExpression(node, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDotExpression(odra.sbql.ast.expressions.DotExpression, java.lang.Object)
	 */
	@Override
	public Object visitDotExpression(DotExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.DOT_EXPRESSION);
		serializeEnvironmentInfo(node);
		return super.visitDotExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitEmptyExpression(odra.sbql.ast.expressions.EmptyExpression, java.lang.Object)
	 */
	@Override
	public Object visitEmptyExpression(EmptyExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EMPTY_EXPRESSION);
		return super.visitEmptyExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitEmptyStatement(odra.sbql.ast.statements.EmptyStatement, java.lang.Object)
	 */
	@Override
	public Object visitEmptyStatement(EmptyStatement node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EMPTY_STATEMENT);
		return super.visitEmptyStatement(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitEqualityExpression(odra.sbql.ast.expressions.EqualityExpression, java.lang.Object)
	 */
	@Override
	public Object visitEqualityExpression(EqualityExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EQUALITY_EXPRESSION);
		buffer.put((byte)node.O.getAsInt());
		return super.visitEqualityExpression(node, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitExecSqlExpression(odra.sbql.ast.expressions.ExecSqlExpression, java.lang.Object)
	 */
	@Override
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EXECSQL_EXPRESSION);
		return super.visitExecSqlExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitExistsExpression(odra.sbql.ast.expressions.ExistsExpression, java.lang.Object)
	 */
	@Override
	public Object visitExistsExpression(ExistsExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EXISTS_EXPRESSION);
		return super.visitExistsExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitExpressionStatement(odra.sbql.ast.statements.ExpressionStatement, java.lang.Object)
	 */
	@Override
	public Object visitExpressionStatement(ExpressionStatement node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EXPRESSION_STATEMENT);
		return super.visitExpressionStatement(node, attr);
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitForAllExpression(odra.sbql.ast.expressions.ForAllExpression, java.lang.Object)
	 */
	@Override
	public Object visitForAllExpression(ForAllExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.FORALL_EXPRESSION);
		return super.visitForAllExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitForSomeExpression(odra.sbql.ast.expressions.ForSomeExpression, java.lang.Object)
	 */
	@Override
	public Object visitForSomeExpression(ForSomeExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.FORSOME_EXPRESSION);
		return super.visitForSomeExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitForEachStatement(odra.sbql.ast.statements.ForEachStatement, java.lang.Object)
	 */
	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.FOREACH_STATEMENT);
		return super.visitForEachStatement(stmt, attr);
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitGroupAsExpression(odra.sbql.ast.expressions.GroupAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitGroupAsExpression(GroupAsExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.GROUPAS_EXPRESSION);
		SerializationUtil.serializeString(buffer, node.name().value());
		return super.visitGroupAsExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.IFELSE_STATEMENT);
		return super.visitIfElseStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitIfStatement(odra.sbql.ast.statements.IfStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.IF_STATEMENT);
		return super.visitIfStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitInExpression(odra.sbql.ast.expressions.InExpression, java.lang.Object)
	 */
	@Override
	public Object visitInExpression(InExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.IN_EXPRESSION);
		return super.visitInExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitIntegerExpression(odra.sbql.ast.expressions.IntegerExpression, java.lang.Object)
	 */
	@Override
	public Object visitIntegerExpression(IntegerExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.INTEGER_EXPRESSION);
		buffer.putInt(node.getLiteral().value());
		return super.visitIntegerExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitIntersectExpression(odra.sbql.ast.expressions.IntersectExpression, java.lang.Object)
	 */
	@Override
	public Object visitIntersectExpression(IntersectExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.INTERSECT_EXPRESSION);
		return super.visitIntersectExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitJoinExpression(odra.sbql.ast.expressions.JoinExpression, java.lang.Object)
	 */
	@Override
	public Object visitJoinExpression(JoinExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.JOIN_EXPRESSION);
		serializeEnvironmentInfo(node);
		return super.visitJoinExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitMaxExpression(odra.sbql.ast.expressions.MaxExpression, java.lang.Object)
	 */
	@Override
	public Object visitMaxExpression(MaxExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.MAX_EXPRESSION);
		return super.visitMaxExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitMinExpression(odra.sbql.ast.expressions.MinExpression, java.lang.Object)
	 */
	@Override
	public Object visitMinExpression(MinExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.MIN_EXPRESSION);
		return super.visitMinExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitMinusExpression(odra.sbql.ast.expressions.MinusExpression, java.lang.Object)
	 */
	@Override
	public Object visitMinusExpression(MinusExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.MINUS_EXPRESSION);
		return super.visitMinusExpression(node, attr);
	}

	

	

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitNameExpression(odra.sbql.ast.expressions.NameExpression, java.lang.Object)
	 */
	@Override
	public Object visitNameExpression(NameExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.NAME_EXPRESSION);
		SerializationUtil.serializeString(buffer, node.name().value());
		serializeBindingInfo(node);
		return super.visitNameExpression(node, attr);
	}
	
	//TW
	public Object visitExternalNameExpression(ExternalNameExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EXTERNAL_NAME_EXPRESSION);
		SerializationUtil.serializeString(buffer, node.name().value());
		return super.visitExternalNameExpression(node, attr);
	}	

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitOrderByExpression(odra.sbql.ast.expressions.OrderByExpression, java.lang.Object)
	 */
	@Override
	public Object visitOrderByExpression(OrderByExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.ORDERBY_EXPRESSION);
		serializeEnvironmentInfo(node);
		return super.visitOrderByExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitProcedureCallExpression(odra.sbql.ast.expressions.ProcedureCallExpression, java.lang.Object)
	 */
	@Override
	public Object visitProcedureCallExpression(ProcedureCallExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.PROCCALL_EXPRESSION);
		return super.visitProcedureCallExpression(node, attr);
	}

	//TW
	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.EXTERNAL_PROCCALL_EXPRESSION);
		return super.visitExternalProcedureCallExpression(node, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitRangeExpression(odra.sbql.ast.expressions.RangeExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeExpression(RangeExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.RANGE_EXPRESSION);
		return super.visitRangeExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitRealExpression(odra.sbql.ast.expressions.RealExpression, java.lang.Object)
	 */
	@Override
	public Object visitRealExpression(RealExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.REAL_EXPRESSION);
		buffer.putDouble(node.getLiteral().value());
		return super.visitRealExpression(node, attr);
	}

	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRefExpression(odra.sbql.ast.expressions.RefExpression, java.lang.Object)
	 */
	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.REF_EXPRESSION);
		return super.visitRefExpression(expr, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRefExpression(odra.sbql.ast.expressions.RefExpression, java.lang.Object)
	 */
	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException
	{
		buffer.put(IASTDescriptor.REMOTE_QUERY_EXPRESSION);
		return super.visitRemoteQueryExpression(expr, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitReturnWithoutValueStatement(odra.sbql.ast.statements.ReturnWithoutValueStatement, java.lang.Object)
	 */
	@Override
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.RETURN_WITHOUT_VALUE_STATEMENT);
		return super.visitReturnWithoutValueStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitReturnWithValueStatement(odra.sbql.ast.statements.ReturnWithValueStatement, java.lang.Object)
	 */
	@Override
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.RETURN_WITH_VALUE_STATEMENT);
		return super.visitReturnWithValueStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitSequentialExpression(odra.sbql.ast.expressions.SequentialExpression, java.lang.Object)
	 */
	@Override
	public Object visitSequentialExpression(SequentialExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.SEQUENTIAL_EXPRESSION);
		return super.visitSequentialExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitSequentialStatement(odra.sbql.ast.statements.SequentialStatement, java.lang.Object)
	 */
	@Override
	public Object visitSequentialStatement(SequentialStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.SEQUENTIAL_STATEMENT);
		
		Statement [] flattenedStmts = stmt.flatten();
		assert flattenedStmts.length > 1 : "sequential statements > 1";
		buffer.putInt(flattenedStmts.length);
		for(int i = flattenedStmts.length - 1; i >=0 ; i--){
		    flattenedStmts[i].accept(this, attr);
		}
		return commonVisitStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitSimpleBinaryExpression(odra.sbql.ast.expressions.SimpleBinaryExpression, java.lang.Object)
	 */
	@Override
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.SIMPLE_BINARY_EXPRESSION);
		buffer.put((byte)node.O.getAsInt());
		return super.visitSimpleBinaryExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitSimpleUnaryExpression(odra.sbql.ast.expressions.SimpleUnaryExpression, java.lang.Object)
	 */
	@Override
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.SIMPLE_UNARY_EXPRESSION);
		buffer.put((byte)node.O.getAsInt());
		return super.visitSimpleUnaryExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitStringExpression(odra.sbql.ast.expressions.StringExpression, java.lang.Object)
	 */
	@Override
	public Object visitStringExpression(StringExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.STRING_EXPRESSION);
		SerializationUtil.serializeString(buffer, node.getLiteral().value());
		return super.visitStringExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitStructExpression(odra.sbql.ast.expressions.StructExpression, java.lang.Object)
	 */
	@Override
	public Object visitStructExpression(StructExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.STRUCT_EXPRESSION);
		return super.visitStructExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitSumExpression(odra.sbql.ast.expressions.SumExpression, java.lang.Object)
	 */
	@Override
	public Object visitSumExpression(SumExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.SUM_EXPRESSION);
		return super.visitSumExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitToBagExpression(odra.sbql.ast.expressions.ToBagExpression, java.lang.Object)
	 */
	@Override
	public Object visitToBagExpression(ToBagExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_BAG_EXPRESSION);
		return super.visitToBagExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitToBooleanExpression(odra.sbql.ast.expressions.ToBooleanExpression, java.lang.Object)
	 */
	@Override
	public Object visitToBooleanExpression(ToBooleanExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_BOOLEAN_EXPRESSION);
		return super.visitToBooleanExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitToIntegerExpression(odra.sbql.ast.expressions.ToIntegerExpression, java.lang.Object)
	 */
	@Override
	public Object visitToIntegerExpression(ToIntegerExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_INTEGER_EXPRESSION);
		return super.visitToIntegerExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitToRealExpression(odra.sbql.ast.expressions.ToRealExpression, java.lang.Object)
	 */
	@Override
	public Object visitToRealExpression(ToRealExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_REAL_EXPRESSION);
		return super.visitToRealExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitToSingleExpression(odra.sbql.ast.expressions.ToSingleExpression, java.lang.Object)
	 */
	@Override
	public Object visitToSingleExpression(ToSingleExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_SINGLE_EXPRESSION);
		return super.visitToSingleExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitToStringExpression(odra.sbql.ast.expressions.ToStringExpression, java.lang.Object)
	 */
	@Override
	public Object visitToStringExpression(ToStringExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_STRING_EXPRESSION);
		return super.visitToStringExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitUnionExpression(odra.sbql.ast.expressions.UnionExpression, java.lang.Object)
	 */
	@Override
	public Object visitUnionExpression(UnionExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.UNION_EXPRESSION);
		return super.visitUnionExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitUniqueExpression(odra.sbql.ast.expressions.UniqueExpression, java.lang.Object)
	 */
	@Override
	public Object visitUniqueExpression(UniqueExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.UNIQUE_EXPRESSION);
		buffer.put(node.isUniqueref() ? (byte)1 : (byte)0);
		return super.visitUniqueExpression(node, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement, java.lang.Object)
	 */
	@Override
	public Object visitVariableDeclarationStatement(VariableDeclarationStatement node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.VARIABLE_DECLARATION_STATEMENT);
		
		
		//	variable name
		SerializationUtil.serializeString(buffer, node.getVariableName());			
			
			//variable typename
		//SerializationUtil.serializeString(buffer, node.getVariableTypeName());
			
		
		//variable type
		serializeType(node.getTypeDeclaration());
		
		
		//variable cardinality
		buffer.putInt(node.getMinCard());
		buffer.putInt(node.getMaxCard());
			
			//reference indicator
		buffer.putInt(node.getReflevel());
		
		return super.visitVariableDeclarationStatement(node, attr);
	}

	

	/**
	 * @param typeDeclaration
	 */
	private void serializeType(TypeDeclaration typeDeclaration) {
	    DeclarationSerializer dser = new DeclarationSerializer();
		byte[] styped = dser.writeDeclarationAST(typeDeclaration, withPositionInfo);		
		buffer.putInt(styped.length);
		buffer.put(styped);
	    
	}



	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitWhereExpression(odra.sbql.ast.expressions.WhereExpression, java.lang.Object)
	 */
	@Override
	public Object visitWhereExpression(WhereExpression node, Object attr) throws SBQLException {		
		buffer.put(IASTDescriptor.WHERE_EXPRESSION);
		serializeEnvironmentInfo(node);
		return super.visitWhereExpression(node, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInsertExpression(odra.sbql.ast.expressions.InsertExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.INSERT_EXPRESSION);
		return super.visitInsertExpression(expr, attr);
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions.InsertCopyExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
	    buffer.put(IASTDescriptor.INSERT_COPY_EXPRESSION);
	    SerializationUtil.serializeString(buffer, expr.name().value());
	    return super.visitInsertCopyExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInstanceOfExpression(odra.sbql.ast.expressions.InstanceOfExpression, java.lang.Object)
	 */
	@Override
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.INSTANCEOF_EXPRESSION);
		return super.visitInstanceOfExpression(expr, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitWhileStatement(odra.sbql.ast.statements.WhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitWhileStatement(WhileStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.WHILE_STATEMENT);
		return super.visitWhileStatement(stmt, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDoWhileStatement(odra.sbql.ast.statements.DoWhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.DO_WHILE_STATEMENT);
		return super.visitDoWhileStatement(stmt, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitForStatement(odra.sbql.ast.statements.ForStatement, java.lang.Object)
	 */
	@Override
	public Object visitForStatement(ForStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.FOR_STATEMENT);
		return super.visitForStatement(stmt, attr);
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement, java.lang.Object)
	 */
	@Override
	public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.BREAK_STATEMENT);
		return super.visitBreakStatement(stmt, attr);
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCastExpression(odra.sbql.ast.expressions.CastExpression, java.lang.Object)
	 */
	@Override
	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CAST_EXPRESSION);
//		SerializationUtil.serializeString(expr.N.V);
		return super.visitCastExpression(expr, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement, java.lang.Object)
	 */
	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CONTINUE_STATEMENT);
		return super.visitContinueStatement(stmt, attr);
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDateExpression(odra.sbql.ast.expressions.DateExpression, java.lang.Object)
	 */
	@Override
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.DATE_EXPRESSION);
		buffer.putLong(expr.getLiteral().value().getTime());
		return super.visitDateExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDateprecissionExpression(odra.sbql.ast.expressions.DateprecissionExpression, java.lang.Object)
	 */
	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.DATEPREC_EXPRESSION);
		return super.visitDateprecissionExpression(expr, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRandomExpression(odra.sbql.ast.expressions.RandomExpression, java.lang.Object)
	 */
	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.RANDOM_EXPRESSION);
		return super.visitRandomExpression(expr, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitToDateExpression(odra.sbql.ast.expressions.ToDateExpression, java.lang.Object)
	 */
	@Override
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.TO_DATE_EXPRESSION);
		return super.visitToDateExpression(expr, attr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CLOSE_BY_EXPRESSION);
		return super.visitCloseByExpression(expr, attr);
	}
	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCloseUniqueByExpression(odra.sbql.ast.expressions.CloseUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.CLOSE_UNIQUE_BY_EXPRESSION);
		return super.visitCloseUniqueByExpression(node, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitLeavesByExpression(odra.sbql.ast.expressions.LeavesByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesByExpression(LeavesByExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.LEAVES_BY_EXPRESSION);
		return super.visitLeavesByExpression(node, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitLeavesUniqueByExpression(odra.sbql.ast.expressions.LeavesUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.LEAVES_UNIQUE_BY_EXPRESSION);	
		return super.visitLeavesUniqueByExpression(node, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	@Override
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    buffer.put(IASTDescriptor.TRY_CATCH_FINALLY_STATEMENT);
	    stmt.getTryStatement().accept(this, attr);
	    SingleCatchBlock[] blocks = stmt.getCatchBlocks().flattenCatchBlocks();
	    buffer.putInt(blocks.length);
	    for(SingleCatchBlock cb : blocks){
		SerializationUtil.serializeString(buffer, cb.getCatchBlockName());
		if(cb.getCatchVariable() != null){
		    SerializationUtil.serializeString(buffer, cb.getCatchVariable().getName());
		    SerializationUtil.serializeString(buffer, cb.getCatchVariable().getType().getTypeName());		    
		}else {
		    SerializationUtil.serializeString(buffer, cb.getExceptionName());
		    SerializationUtil.serializeString(buffer, cb.getExceptionTypeName());
		}
		cb.getStatement().accept(this, attr);		    
	    }
	    stmt.getFinallyStatement().accept(this, attr);
	    return this.commonVisitStatement(stmt, attr);
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	 */
	@Override
	public Object visitThrowStatement(ThrowStatement stmt, Object attr)
		throws SBQLException
	{
	    buffer.put(IASTDescriptor.THROW_STATEMENT);
	    return super.visitThrowStatement(stmt, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitParallelUnionExpression(odra.sbql.ast.expressions.ParallelUnionExpression, java.lang.Object)
	 */
	@Override
	public Object visitParallelUnionExpression(ParallelUnionExpression node, Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.PARALLEL_UNION_EXPRESSION);
		buffer.putInt(node.getParallelExpressions().size());
		return super.visitParallelUnionExpression(node, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTransactionAbortStatement(odra.sbql.ast.statements.TransactionAbortStatement, java.lang.Object)
	 */
	@Override
	public Object visitTransactionAbortStatement(
		TransactionAbortStatement stmt, Object attr)
		throws SBQLException {
	    buffer.put(IASTDescriptor.ABORT_STATEMENT);
	    
	    return super.visitTransactionAbortStatement(stmt, attr);
	}



	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRangeAsExpression(odra.sbql.ast.expressions.RangeAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
		buffer.put(IASTDescriptor.RANGE_AS_EXPRESSION);
		SerializationUtil.serializeString(buffer, expr.name().value());
		return super.visitRangeAsExpression(expr, attr);
	}

	

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
		buffer.put(IASTDescriptor.SERIALIZE_OID_EXPRESSION);
		return super.visitSerializeOidExpression(expr, attr);
	}



	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr,
			Object attr) throws SBQLException {
		buffer.put(IASTDescriptor.DESERIALIZE_OID_EXPRESSION);
		return super.visitDeserializeOidExpression(expr, attr);
	}


	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr)
			throws SBQLException {
		buffer.put(IASTDescriptor.RENAME_EXPRESSION);
		SerializationUtil.serializeString(buffer, expr.name().value());
		return super.visitRenameExpression(expr, attr);
	}



	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#commonVisitExpression(odra.sbql.ast.expressions.Expression, java.lang.Object)
	 */
	@Override
	protected Object commonVisitExpression(Expression expr, Object attr) throws SBQLException {
	    this.serializeLinksDecoration(expr);
	    serializePosition(expr);
	    return null;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#commonVisitStatement(odra.sbql.ast.statements.Statement, java.lang.Object)
	 */
	@Override
	protected Object commonVisitStatement(Statement stmt, Object attr) throws SBQLException {
		serializePosition(stmt);	
		return null;
	}
	private final void serializePosition(ASTNode node){
		if(this.withPositionInfo){
		    SerializationUtil.serializePosition(this.buffer,node);
		}
	}
	
	
	
	
	/**
	 * Serializes links decoration of node as array of String
	 * Format is as follows:
	 * - links count 		( 4 bytes )
	 * - name lenght 		( 4 bytes )
	 * - name 				( n bytes )
	 * 
	 * @param node 
	 */
	private final void serializeLinksDecoration(Expression node) 
	{			
		if (node.links.size() == 0 )
		{
			buffer.putInt(0);
		}
		else
		{
			StringBuffer str = new StringBuffer();
			buffer.putInt(node.links.size());
			
			for (OID oid : node.links)
			{
				try
				{
					String name = new MBLink(oid).getName();
					SerializationUtil.serializeString(buffer, name);
				}
				catch (DatabaseException e)
				{
					throw new SerializerException(e);
				}
			}
		}	
	}
	
	private void serializeBindingInfo(NameExpression node)
	{
		if (node.getBindingInfo() != null)
			buffer.putInt(node.getBindingInfo().boundat);
		else
			buffer.putInt(-1);

	}
	
	private void serializeEnvironmentInfo(NonAlgebraicExpression node)
	{
		if (node.getEnvsInfo() != null)
		{
			buffer.putInt(node.getEnvsInfo().baseEnvsSize);
			buffer.putInt(node.getEnvsInfo().framesOpened);
		}
		else
		{
			buffer.putInt(-1);
			buffer.putInt(-1);
		}
	}	
	

	
}