package odra.sbql.ast;

import odra.sbql.SBQLException;
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
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.util.DateUtils;


/**
 * AST2TextQueryDumper
 * Converts AST of a SBQL query to its equivalent textual representation 
 * @author radamus, jacenty
 */
public class AST2TextQueryDumper extends TraversingASTAdapter {
	StringBuffer str = new StringBuffer();
	String intend = "";
	/** node to mark */
	private ASTNode nodeToMark;
	/** marked node position */
	private int markedPosition = -1;
	/** marked node length */
	private int markedLength = -1;
	private boolean includeNonParseable = false;
	private boolean includeEnforced = true;
	
	public static String AST2Text(ASTNode node) throws SBQLException  {	
		AST2TextQueryDumper astd = new AST2TextQueryDumper();		
		node.accept(astd, null);		
		return astd.getQuery();	
	}
	
	public String dumpAST(ASTNode node) throws SBQLException {
	    	intend = "\t";	
		node.accept(this, null);
		return str.toString();
	}
	/**
	 * dump the entire AST including non parseable nodes
	 * (e.g. toSingle, atMost, etc.)
	 * @param node
	 * @return
	 * @throws SBQLException
	 */
	public String dumpAllAST(ASTNode node) throws SBQLException {
		intend = "\t";
		this.includeNonParseable = true;		
		node.accept(this, null);
		return str.toString();
	}

	/**
	 * dump the AST including only those nodes
	 * that are not marked as 'enforced'
	 * (e.g. toSingle, atMost, etc.)
	 * @param node
	 * @return
	 * @throws SBQLException
	 */
	public String dumpOriginalAST(ASTNode node) throws SBQLException {
		intend = "\t";
		this.includeEnforced = false;
		node.accept(this, null);
		return str.toString();
	}
	/**
	 * Dumps the node to text and provides the location of the node to mark in the string.
	 * 
	 * @param node query
	 * @param nodeToMark node to mark
	 * @return array [dumped node, marked position, length]
	 * @throws Exception
	 * 
	 * @author jacenty
	 */
	public Object[] dumpASTWithMark(ASTNode node, ASTNode nodeToMark) throws Exception 
	{
		this.nodeToMark = nodeToMark;
		markedLength = new AST2TextQueryDumper().dumpAST(nodeToMark).length();
		
		node.accept(this, null);
		return new Object[] {str.toString(), markedPosition, markedLength};
	}
	
	/**
	 * Checks if the current node is the node to mark; if so its position in the string buffer is remembered.
	 * 
	 * @param current current (checked) node
	 */
	private void checkNodeToMark(ASTNode current)
	{
		if(nodeToMark != null && current.equals(nodeToMark))
			markedPosition = str.length();
	}
	
	/**
	 * @return the textual representation of the AST query 
	 */
	public String getQuery(){
		return str.toString();
	}
	
	public Object visitAsExpression(AsExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("(");
		expr.getExpression().accept(this, attr);
		str.append(") as " + expr.name().value());
		return null;
	}

	public Object visitAssignExpression(AssignExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, ":=");
		return null;
	}

	public Object visitAvgExpression(AvgExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "avg");
		return null;
	}

	public Object visitBooleanExpression(BooleanExpression expr, Object attr)
			throws SBQLException {

		str.append(expr.getLiteral().value());
		return null;
	}
	
	public Object visitCommaExpression(CommaExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, ", ");
		return null;
	}

	public Object visitCountExpression(CountExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "count");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr)
		throws SBQLException {
	    	checkNodeToMark(expr);
	    	str.append("delete" + "(");
		expr.getExpression().accept(this, attr);
		str.append(")");
	    return null;
	}

	public Object visitDerefExpression(DerefExpression expr, Object attr)
			throws SBQLException {
		if(expr.isEnforced()) {
			if(includeEnforced)
				printUnary(expr, attr, "deref");
			else
				expr.getExpression().accept(this, attr);
		} else
			printUnary(expr, attr, "deref");
		return null;
	}

	public Object visitDotExpression(DotExpression expr, Object attr)
			throws SBQLException {
	
		printBinary(expr, attr, ".");
		return null;
	}

	public Object visitEmptyStatement(EmptyStatement stmt, Object attr)
			throws SBQLException {
		checkNodeToMark(stmt);
		
		return null;
	}

	public Object visitEqualityExpression(EqualityExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, expr.O.spell());
		return null;
	}
	

	public Object visitExistsExpression(ExistsExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "exists");
		return null;
	}

	public Object visitForAllExpression(ForAllExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("forall(");
		expr.getLeftExpression().accept(this, attr);
		str.append(")");
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("forsome(");
		expr.getLeftExpression().accept(this, attr);
		str.append(")");
		expr.getRightExpression().accept(this, attr);
		return null;
	}

	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("(");
		expr.getExpression().accept(this, attr);
		str.append(") groupas " + expr.name().value() + " ");
		return null;
	}

	public Object visitInExpression(InExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "in");
		return null;
	}

	public Object visitIntegerExpression(IntegerExpression expr, Object attr)
			throws SBQLException {
	    	checkNodeToMark(expr);
		str.append(expr.getLiteral().value());
		return null;
	}

	public Object visitIntersectExpression(IntersectExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "intersect");
		return null;
	}

	public Object visitJoinExpression(JoinExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "join");
		return null;
	}

	public Object visitLazyFailureExpression(LazyFailureExpression expr,
			Object attr) throws SBQLException {

		printUnary(expr, attr, "lazy_failure");
		return null;
	}
	
	public Object visitMaxExpression(MaxExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "max");
		return null;
	}

	public Object visitMinExpression(MinExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "min");
		return null;
	}

	public Object visitMinusExpression(MinusExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "subtract");
		return null;
	}

	public Object visitNameExpression(NameExpression expr, Object attr)
			throws SBQLException {
	    	checkNodeToMark(expr);
		str.append(expr.name().value());
		return null;

	}
	
	public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr)
			throws SBQLException {
	    	checkNodeToMark(expr);
		str.append(expr.name().value());
		return null;
	}

	public Object visitOrderByExpression(OrderByExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "orderby");
		return null;
	}

	public Object visitProcedureCallExpression(ProcedureCallExpression expr,
			Object attr) throws SBQLException {
		checkNodeToMark(expr);
		
		expr.getProcedureSelectorExpression().accept(this, attr);
		str.append("(");
		expr.getArgumentsExpression().accept(this, attr);
		str.append(")");
		return null;
	}
	
	//TW
	public Object visitExternalProcedureCallExpression(ProcedureCallExpression expr,
			Object attr) throws Exception {
		checkNodeToMark(expr);
		
		expr.getProcedureSelectorExpression().accept(this, attr);
		str.append("(");
		expr.getArgumentsExpression().accept(this, attr);
		str.append(")");
		return null;
	}
	
	public Object visitRealExpression(RealExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append(expr.getLiteral().value());
		return null;
	}
	
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException
	{
		checkNodeToMark(expr);
		
		str.append(DateUtils.format(expr.getLiteral().value()));
		return null;
	}

	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt,
			Object attr) throws SBQLException {
		checkNodeToMark(stmt);
		intend();
		str.append("return ");
		stmt.getExpression().accept(this, attr);
		printStatementEnd();
		return null;
	}

	public Object visitReturnWithoutValueStatement(
			ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		checkNodeToMark(stmt);
		intend();
		str.append("return");
		printStatementEnd();
		return null;
	}

	public Object visitSequentialExpression(SequentialExpression expr,
			Object attr) throws SBQLException {
		checkNodeToMark(expr);
		
		expr.getFirstExpression().accept(this, attr);
		if(!(expr.getSecondExpression() instanceof EmptyExpression)) {
		    str.append("; ");		
		    expr.getSecondExpression().accept(this, attr);
		}
		return null;
	}

	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr,
			Object attr) throws SBQLException {

		printBinary(expr, attr, expr.O.spell());
		return null;
	}

	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr,
			Object attr) throws SBQLException {

		printUnary(expr, attr, expr.O.spell());
		return null;
	}

	public Object visitStringExpression(StringExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("\"" + expr.getLiteral().value() + "\"");
		return null;
	}

	public Object visitSumExpression(SumExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "sum");
		return null;
	}

	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "(boolean)");

		return null;
	}

	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr)
			throws SBQLException {

		printUnary(expr, attr, "(integer)");

		return null;
	}

	public Object visitToRealExpression(ToRealExpression expr, Object attr)
			throws SBQLException {
	
		printUnary(expr, attr, "(real)");

		return null;
	}
	
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException
	{
	
		printUnary(expr, attr, "(date)");
		
		return null;
	}

	public Object visitToStringExpression(ToStringExpression expr, Object attr)
			throws SBQLException {
	
		printUnary(expr, attr, "(string)");

		return null;
	}

	public Object visitUnionExpression(UnionExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "union");
		return null;
	}

	public Object visitUniqueExpression(UniqueExpression expr, Object attr)
			throws SBQLException {

		if (expr.isUniqueref())
			printUnary(expr, attr, "uniqueref");
		else
			printUnary(expr, attr, "unique");
		return null;
	}

	public Object visitWhereExpression(WhereExpression expr, Object attr)
			throws SBQLException {

		printBinary(expr, attr, "where");
		return null;
	}
	
	
	public Object visitBagExpression(BagExpression expr, Object attr) 
		throws SBQLException {
	
		printUnary(expr, attr, "bag");
		return null;
	}
	
	public Object visitRangeExpression(RangeExpression expr, Object attr) 
		throws SBQLException {
	
		str.append(" ( (");
		expr.getLeftExpression().accept(this, attr);
		str.append(") ");
		str.append("[");
		expr.getRightExpression().accept(this, attr);
		str.append("] ) ");
		return null;
	}
	
	public Object visitStructExpression(StructExpression expr, Object attr) 
		throws SBQLException {

		printUnary(expr, attr, "struct");
		return null;
	}
	
	public Object visitToBagExpression(ToBagExpression expr, Object attr) 
		throws SBQLException {

		printUnary(expr, attr, "bag");
		return null;
	}
	
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		checkNodeToMark(expr);
		if(includeNonParseable)
			printUnary(expr, attr, "toSingle");
		else
			expr.getExpression().accept(this, attr);
		return null;
	}

	/**
	 * @author jacenty
	 */
	@Override
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) 
		throws SBQLException	{
		checkNodeToMark(expr);
		
		str.append("execsql(");
		expr.query.accept(this, attr);
		str.append(", ");
		expr.pattern.accept(this, attr);
		str.append(", ");
		expr.module.accept(this, attr);
		str.append(")");
		return null;
	}
	
	private void printUnary(UnaryExpression expr, Object attr, String oper) 
		{
		checkNodeToMark(expr);		
		if((expr.isEnforced()&& this.includeEnforced) || !expr.isEnforced()){			
			str.append("(");
			str.append(oper + "(");
			expr.getExpression().accept(this, attr);
			str.append(")");
			str.append(")");
		}else 
			expr.getExpression().accept(this, attr);
	}
	private void printBinary(BinaryExpression expr, Object attr, String oper) 
	{		
		checkNodeToMark(expr);
		str.append("(");
		str.append("(");
		expr.getLeftExpression().accept(this, attr);
		str.append(")");
		str.append(" " + oper + " ");
		str.append("(");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		str.append(")");
	}

	private void printParallel(ParallelExpression expr, Object attr, String oper) 
	 {
	checkNodeToMark(expr);
	
	str.append(oper+"(");
	for(Expression subexpr: expr.getParallelExpressions()) {
		subexpr.accept(this, attr);
			str.append(", ");	
	}
	str.delete(str.length()-2, str.length());
	str.append(")");
}
	
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("if (");
		expr.getConditionExpression().accept(this, attr);
		str.append(") then (");
		expr.getThenExpression().accept(this, attr);
		str.append(") else (");
		expr.getElseExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	public Object visitIfThenExpression(IfThenExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("if (");
		expr.getConditionExpression().accept(this, attr);
		str.append(") then (");
		expr.getThenExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("create " + expr.name().value() + "(");
		expr.getExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions.CreateLocalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("create local " + expr.name().value() + "(");
		expr.getExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreatePermanentExpression(odra.sbql.ast.expressions.CreatePermanentExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr)
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("create permanent " + expr.name().value() + "(");
		expr.getExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.expressions.CreateTemporalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("create temporal " + expr.name().value() + "(");
		expr.getExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInsertExpression(odra.sbql.ast.expressions.InsertExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("(");
		expr.getLeftExpression().accept(this, attr);
		str.append(" " + ":<" + " ");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions.InsertCopyExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		expr.getLeftExpression().accept(this, attr);
		str.append(" " + ":<<" + expr.name().value());
		str.append("(");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		return null;

	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRefExpression(odra.sbql.ast.expressions.RefExpression, java.lang.Object)
	 */
	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) 
			throws SBQLException {
		printUnary(expr, attr, "ref");
		return null;
	}

	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) 
			throws SBQLException{
		checkNodeToMark(expr);
		
		str.append("dateprec(");
		expr.getLeftExpression().accept(this, attr);
		str.append(", ");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		return null;
	}
	
	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) 
			throws SBQLException{
		checkNodeToMark(expr);
		
		str.append("random(");
		if(expr.getRightExpression() instanceof EmptyExpression)
			expr.getLeftExpression().accept(this, attr);
		else
		{
			expr.getLeftExpression().accept(this, attr);
			str.append(", ");
			expr.getRightExpression().accept(this, attr);
		}
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCastExpression(odra.sbql.ast.expressions.CastExpression, java.lang.Object)
	 */
	@Override
	public Object visitCastExpression(CastExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("(");
		str.append("(");
		expr.getLeftExpression().accept(this, attr); 
		str.append(")");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInstanceOfExpression(odra.sbql.ast.expressions.InstanceOfExpression, java.lang.Object)
	 */
	@Override
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) 
			throws SBQLException {
		checkNodeToMark(expr);
		
		str.append("(");
		expr.getLeftExpression().accept(this, attr);
		str.append(") instanceof (");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCloseByExpression(odra.sbql.ast.expressions.CloseByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr)
			throws SBQLException {
			
		printBinary(expr, attr, " close by ");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitCloseUniqueByExpression(odra.sbql.ast.expressions.CloseUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression node, Object attr)
		throws SBQLException {

		printBinary(node, attr, " close unique by ");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitLeavesByExpression(odra.sbql.ast.expressions.LeavesByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesByExpression(LeavesByExpression node, Object attr) 
	throws SBQLException {
		
		printBinary(node, attr, " leaves by ");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitLeavesUniqueByExpression(odra.sbql.ast.expressions.LeavesUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression node, Object attr) 
		throws SBQLException {
		
		printBinary(node, attr, " leaves unique by ");
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement, java.lang.Object)
	 */
	@Override
	public Object visitBlockStatement(BlockStatement stmt, Object attr)
		throws SBQLException {
	    String prevIntend = this.intend;
	    
	    checkNodeToMark(stmt);
	    intend();
	    str.append("{");
	    str.append(NEW_LINE);
	    
	    this.intend += "\t";
	    stmt.getStatement().accept(this, attr);
	    this.intend = prevIntend;
	    intend();
	    str.append("}");
	    str.append(NEW_LINE);
	   
	     
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement, java.lang.Object)
	 */
	@Override
	public Object visitBreakStatement(BreakStatement stmt, Object attr)
		throws SBQLException {
	    checkNodeToMark(stmt);
	    intend();
	    str.append("break");
	    this.printStatementEnd();
	    return null;
	}

	/**
	 * 
	 */
	private final void intend() {
		str.append(this.intend);
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement, java.lang.Object)
	 */
	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr)
		throws SBQLException {
	    checkNodeToMark(stmt);
	    intend();
	    str.append("continue");
	    this.printStatementEnd();
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDoWhileStatement(odra.sbql.ast.statements.DoWhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitDoWhileStatement(DoWhileStatement stmt, Object attr)
		throws SBQLException {
	    String previntend = this.intend;
	    checkNodeToMark(stmt);
	    intend();
	    str.append("do ");
	    if(!(stmt.getStatement() instanceof BlockStatement))
		this.intend += "\t";
	    stmt.getStatement().accept(this, attr);
	    this.intend = previntend;
	    intend();
	    str.append("while(");
	    stmt.getExpression().accept(this, attr);
	    str.append(")");
	    this.printStatementEnd();
	    
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitExpressionStatement(odra.sbql.ast.statements.ExpressionStatement, java.lang.Object)
	 */
	@Override
	public Object visitExpressionStatement(ExpressionStatement stmt,
		Object attr) throws SBQLException {
	    checkNodeToMark(stmt);
	    intend();
	    stmt.getExpression().accept(this, attr);
	    this.printStatementEnd();
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitExternalProcedureCallExpression(odra.sbql.ast.expressions.ExternalProcedureCallExpression, java.lang.Object)
	 */
	@Override
	public Object visitExternalProcedureCallExpression(
		ExternalProcedureCallExpression expr, Object attr)
		throws SBQLException {
	    	checkNodeToMark(expr);
		
		expr.getLeftExpression().accept(this, attr);
		str.append("(");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		return null;
	    
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitForEachStatement(odra.sbql.ast.statements.ForEachStatement, java.lang.Object)
	 */
	@Override
	public Object visitForEachStatement(ForEachStatement stmt, Object attr)
		throws SBQLException {
	    String previntend = this.intend;
	    checkNodeToMark(stmt);
	    intend();
	    str.append("foreach (");
	    stmt.getExpression().accept(this, attr);
	    str.append(")" +NEW_LINE);
	    intend();
	    str.append("do");
	    str.append(NEW_LINE);
	    if(!(stmt.getStatement() instanceof BlockStatement))
		this.intend += "\t";
	    stmt.getStatement().accept(this, attr);
	    this.intend = previntend;
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitForStatement(odra.sbql.ast.statements.ForStatement, java.lang.Object)
	 */
	@Override
	public Object visitForStatement(ForStatement stmt, Object attr)
		throws SBQLException {
	    String previntend = this.intend;
	    
	    checkNodeToMark(stmt);
	    intend();
	    str.append("for(");
	    stmt.getInitExpression().accept(this, attr);
	    str.append("; ");
	    stmt.getConditionalExpression().accept(this, attr);
	    str.append("; ");
	    stmt.getIncrementExpression().accept(this, attr);
	    str.append(") ");
	    str.append("do ");
	    str.append(NEW_LINE);
	    if(!(stmt.getStatement() instanceof BlockStatement))
		this.intend += "\t";
	    stmt.getStatement().accept(this, attr);
	    this.intend = previntend;
	    
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitIfElseStatement(odra.sbql.ast.statements.IfElseStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfElseStatement(IfElseStatement stmt, Object attr)
		throws SBQLException {
	    String previntend = this.intend;
	    
	    checkNodeToMark(stmt);
	    intend();
	    str.append("if (");
	    stmt.getExpression().accept(this, attr);
	    str.append(") ");
	    str.append(NEW_LINE);
	    if(!(stmt.getIfStatement() instanceof BlockStatement))
		this.intend += "\t";
	    stmt.getIfStatement().accept(this, attr);
	    this.intend = previntend;
	    intend();
	    str.append(" else ");
	    str.append(NEW_LINE);
	    if(!(stmt.getElseStatement() instanceof BlockStatement))
		this.intend += "\t";
	    stmt.getElseStatement().accept(this, attr);
	    this.intend = previntend;
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitIfStatement(odra.sbql.ast.statements.IfStatement, java.lang.Object)
	 */
	@Override
	public Object visitIfStatement(IfStatement stmt, Object attr)
		throws SBQLException {
	    String previntend = this.intend;	  
	    checkNodeToMark(stmt);
	    intend();
	    str.append("if (");
	    stmt.getExpression().accept(this, attr);
	    str.append(") ");
	    str.append(NEW_LINE);
	   if(!(stmt.getStatement() instanceof BlockStatement)) 
	       this.intend += "\t";
	    stmt.getStatement().accept(this, attr);
	    this.intend = previntend;
	   
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitSequentialStatement(odra.sbql.ast.statements.SequentialStatement, java.lang.Object)
	 */
	@Override
	public Object visitSequentialStatement(SequentialStatement stmt,
		Object attr) throws SBQLException {
//	    str.append(this.intend);
	    stmt.getFirstStatement().accept(this, attr);
//	    str.append(this.intend);
	    stmt.getSecondStatement().accept(this, attr);
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitWhileStatement(odra.sbql.ast.statements.WhileStatement, java.lang.Object)
	 */
	@Override
	public Object visitWhileStatement(WhileStatement stmt, Object attr)
		throws SBQLException {
	    String previntend = this.intend;
	    
	    checkNodeToMark(stmt);
	    intend();
	    str.append("while (");
	    stmt.getExpression().accept(this, attr);
	    str.append(")");
	    str.append(NEW_LINE);
	    intend();
	    str.append(" do ");
	    if(!(stmt.getStatement() instanceof BlockStatement))
		this.intend += "\t";
	    stmt.getStatement().accept(this, attr);
	   // this.printStatementEnd();
	    this.intend = previntend;
	    return null;
	}
	
	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException
	{
		if(includeNonParseable)
			printUnary(expr, attr, "remote_query");
		else
			expr.getExpression().accept(this, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr)
		throws SBQLException {	
		if(includeNonParseable)
			printUnary(expr, attr, "atMost");
		else
			expr.getExpression().accept(this, attr);
			return null;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAtLeastExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr)
		throws SBQLException {
		if(includeNonParseable)
			printUnary(expr, attr, "atLeast");
		else
			expr.getExpression().accept(this, attr);
			return null;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement, java.lang.Object)
	 */
	@Override
	public Object visitVariableDeclarationStatement(
		VariableDeclarationStatement stmt, Object attr)
		throws SBQLException {
		intend();
	   str.append(stmt.getVariableName() + ":");
	   if(stmt.getReflevel() > 0)
	       str.append(" ref ");
	   str.append(AST2TextDeclarationPrinter.AST2Text(stmt.getTypeDeclaration(), this.intend));
	  //  String typeName = stmt.getVariableTypeName();
	  // str.append(typeName);
	   this.printCardinality(stmt.getMinCard(), stmt.getMaxCard());
	   if(!(stmt.getInitExpression() instanceof EmptyExpression)){
	       str.append(" := ");
	       stmt.getInitExpression().accept(this, attr);
	   }
	   this.printStatementEnd();
	    return null;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	@Override
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
		intend();
	    str.append("try");
	    str.append(NEW_LINE);
	    stmt.getTryStatement().accept(this, attr);
	    for(SingleCatchBlock sb :stmt.getCatchBlocks().flattenCatchBlocks()){
	    	intend();
	    	str.append("catch");
		    str.append("(" + sb.getExceptionName() + ":" +sb.getExceptionTypeName() + ")");
		    str.append(NEW_LINE);
		    sb.getStatement().accept(this, attr);
	    }
	    if(!(stmt.getFinallyStatement() instanceof EmptyStatement)){
	    	intend();
	    	str.append("finally");
	    	str.append(NEW_LINE);
		stmt.getFinallyStatement().accept(this, attr);
	    }
	    
		
	    return null;
	}

	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	 */
	@Override
	public Object visitThrowStatement(ThrowStatement stmt, Object attr)
		throws SBQLException
	{
		intend();
	    str.append("throw ");
	    stmt.getExpression().accept(this, attr);
	    this.printStatementEnd();
	    return commonVisitStatement(stmt, attr);
	}

	public Object visitParallelUnionExpression(ParallelUnionExpression expr, Object attr)
	throws SBQLException {
		
		printParallel(expr, attr, "parallel_union");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
		printUnary(expr, attr, "serialize");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr,
			Object attr) throws SBQLException {
		checkNodeToMark(expr);
		str.append("(");
		str.append("(");		
		expr.getLeftExpression().accept(this, attr);
		str.append(")");
		str.append(" deserialize to ");
		str.append("(");
		expr.getRightExpression().accept(this, attr);
		str.append(")");
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRangeAsExpression(odra.sbql.ast.expressions.RangeAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
		str.append("(");
		str.append("(");
		expr.getExpression().accept(this, attr);
		str.append(") rangeas ");
		str.append(expr.name().value());
		str.append(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr)
			throws SBQLException {
		str.append("(");
		str.append("(");
		expr.getExpression().accept(this, attr);
		str.append(") rename to ");
		str.append(expr.name().value());
		str.append(")");
		return null;
	}

	private void printStatementEnd(){
	    str.append(";");
	    str.append(NEW_LINE);
	}
	
	private final void printCardinality(int min, int max){
		if(min == 1 && max == 1) return;
		    str.append("[" + min + ".."+(max == Integer.MAX_VALUE ? "*" : max) + "]");
	    }
	
	private String NEW_LINE = System.getProperty("line.separator");	
}