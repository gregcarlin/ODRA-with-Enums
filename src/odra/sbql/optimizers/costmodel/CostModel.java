package odra.sbql.optimizers.costmodel;

import java.util.List;
import java.util.Vector;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.*;
import odra.sbql.ast.statements.*;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.optimizers.queryrewrite.index.SingleIndexFitter;
import odra.sbql.results.compiletime.Signature;
import odra.system.config.ConfigServer;

/** 
 * 
 * A cost model for optimizations and a "best" index selecting method.
 * 
 * @author tkowals, Greg Carlin
 * 
 */


public class CostModel extends TraversingASTAdapter {
    private static final boolean WARN = ConfigServer.DEBUG_ENABLE; // true

	private CostModel() {
		
	}
	
	public static CostModel getCostModel() {
		return new CostModel();
	}
	
	private double estimate = 0.0;
	private boolean debug = true;
	
	/**
     * Estimates the running time of a given query. Note that estimates are not absolute, but they can be compared.
     * 
     * @param query
     * @param module
     * @return
     */
	public double estimate(ASTNode query, DBModule module) {
	    return estimate(query, module, true);
	}
	
	/**
	 * Estimates the running time of a given query. Note that estimates are not absolute, but they can be compared.
	 * 
	 * @param query
	 * @param module
	 * @param debug When running CostModel on a similar query multiple times, setting debug to false will prevent warnings.
	 * @return
	 */
	public double estimate(ASTNode query, DBModule module, boolean debug) {
	    this.debug = debug;
	    estimate = 0.0;
	    query.accept(this, null);
	    return estimate;
	}
	
	private void warn(ASTNode operator) {
	    if(WARN && debug) System.out.printf("WARNING: Unsupported operator (%s) encountered.%n", operator.getClass().getSimpleName());
	}
	
	/**
	 * Estimates the number of items returned by a given query.
	 * 
	 * @param expr
	 * @return
	 */
	private int estimateNumItems(Expression expr) {
	    if(expr instanceof AsExpression) {
	        return estimateNumItems(((AsExpression) expr).getExpression());
	    } else if(expr instanceof AssignExpression) {
	        
	    } else if(expr instanceof AtLeastExpression) {
	        // unsupported and unknown
	    } else if(expr instanceof AtMostExpression) {
	        // unsupported and unknown
	    } else if(expr instanceof AvgExpression) {
	        
	    } else if(expr instanceof BagExpression) {
	        return estimateNumItems(((BagExpression) expr).getExpression());
	    } else if(expr instanceof BooleanExpression) {
	        
	    } else if(expr instanceof CastExpression) {
	        
	    } else if(expr instanceof CloseByExpression) {
	        NameExpression nameExpr = getRightNameExpression((CloseByExpression) expr);
	        if(nameExpr != null) return nameExpr.getSignature().getMaxCard();
	    } else if(expr instanceof CloseUniqueByExpression) {
	        NameExpression nameExpr = getRightNameExpression((CloseUniqueByExpression) expr);
            if(nameExpr != null) return nameExpr.getSignature().getMaxCard();
	    } else if(expr instanceof CommaExpression) {
	        CommaExpression ce = (CommaExpression) expr;
	        return estimateNumItems(ce.getLeftExpression()) + estimateNumItems(ce.getRightExpression());
	    } else if(expr instanceof CountExpression) {
	        
	    } else if(expr instanceof CreateLocalExpression) {
	        
	    } else if(expr instanceof CreatePermanentExpression) {
	        
	    } else if(expr instanceof CreateTemporalExpression) {
	        
	    } else if(expr instanceof DateExpression) {
	        
	    } else if(expr instanceof DateprecissionExpression) {
	        
	    } else if(expr instanceof DeleteExpression) {
	        
	    } else if(expr instanceof DerefExpression) {
	        return estimateNumItems(((DerefExpression) expr).getExpression());
	    } else if(expr instanceof DeserializeOidExpression) {
	        // unsupported because unknown
	    } else if(expr instanceof DotExpression) {
	        DotExpression de = (DotExpression) expr;
	        return estimateNumItems(de.getLeftExpression()) * estimateNumItems(de.getRightExpression()); // i think this is good
	    } else if(expr instanceof EmptyExpression) {
	        return 0;
	    } else if(expr instanceof EqualityExpression) {
	        
	    } else if(expr instanceof ExecSqlExpression) {
	        // unsupported
	    } else if(expr instanceof ExistsExpression) {
	        
	    } else if(expr instanceof ExternalNameExpression) {
	        return expr.getSignature().getMaxCard();
	    } else if(expr instanceof ExternalProcedureCallExpression) {
	        // unsupported
	    } else if(expr instanceof ForAllExpression) {
	        
	    } else if(expr instanceof ForSomeExpression) {
	        
	    } else if(expr instanceof GroupAsExpression) {
	        return estimateNumItems(((GroupAsExpression) expr).getExpression());
	    } else if(expr instanceof IfThenElseExpression) {
	        IfThenElseExpression ifee = (IfThenElseExpression) expr;
	        return Math.max(estimateNumItems(ifee.getThenExpression()), estimateNumItems(ifee.getElseExpression()));
	    } else if(expr instanceof IfThenExpression) {
	        return estimateNumItems(((IfThenExpression) expr).getConditionExpression());
	    } else if(expr instanceof InExpression) {
	        
	    } else if(expr instanceof InsertCopyExpression) {
	        // unknown
	    } else if(expr instanceof InsertExpression) {
	        // unknown
	    } else if(expr instanceof InstanceOfExpression) {
	        
	    } else if(expr instanceof IntegerExpression) {
	        
	    } else if(expr instanceof IntersectExpression) {
	        IntersectExpression ie = (IntersectExpression) expr;
	        return Math.max(estimateNumItems(ie.getLeftExpression()), estimateNumItems(ie.getRightExpression()));
	    } else if(expr instanceof JoinExpression) {
	        JoinExpression je = (JoinExpression) expr;
	        return estimateNumItems(je.getLeftExpression()) + estimateNumItems(je.getRightExpression());
	    } else if(expr instanceof LazyFailureExpression) {
	        // unsupported because unknown
	    } else if(expr instanceof LeavesByExpression) {
	        NameExpression nameExpr = getRightNameExpression((LeavesByExpression) expr);
            if(nameExpr != null) return nameExpr.getSignature().getMaxCard();
	    } else if(expr instanceof LeavesUniqueByExpression) {
	        NameExpression nameExpr = getRightNameExpression((LeavesUniqueByExpression) expr);
            if(nameExpr != null) return nameExpr.getSignature().getMaxCard();
	    } else if(expr instanceof MatchStringExpression) {
	        
	    } else if(expr instanceof MaxExpression) {
	        
	    } else if(expr instanceof MinExpression) {
	        
	    } else if(expr instanceof MinusExpression) {
	        
	    } else if(expr instanceof NameExpression) {
	        return expr.getSignature().getMaxCard();
	    } else if(expr instanceof NowExpression) {
	        
	    } else if(expr instanceof OrderByExpression) {
	        return estimateNumItems(((OrderByExpression) expr).getLeftExpression());
	    } else if(expr instanceof ParallelUnionExpression) {
	        int total = 0;
	        for(Expression e : ((ParallelUnionExpression) expr).getParallelExpressions()) {
	            total += estimateNumItems(e);
	        }
	        return total;
	    } else if(expr instanceof ProcedureCallExpression) {
	        // unsupported
	    } else if(expr instanceof RandomExpression) {
	        
	    } else if(expr instanceof RangeAsExpression) {
	        return estimateNumItems(((RangeAsExpression) expr).getExpression());
	    } else if(expr instanceof RangeExpression) {
	        
	    } else if(expr instanceof RealExpression) {
	        
	    } else if(expr instanceof RefExpression) {
	        return estimateNumItems(((RefExpression) expr).getExpression());
	    } else if(expr instanceof RemoteQueryExpression) {
	        return estimateNumItems(((RemoteQueryExpression) expr).getExpression()); // guess
	    } else if(expr instanceof RenameExpression) {
	        return estimateNumItems(((RenameExpression) expr).getExpression());
	    } else if(expr instanceof SequentialExpression) {
	        SequentialExpression se = (SequentialExpression) expr;
	        return estimateNumItems(se.getFirstExpression()) + estimateNumItems(se.getSecondExpression());
	    } else if(expr instanceof SerializeOidExpression) {
	        // unsupported because unknown
	    } else if(expr instanceof SimpleBinaryExpression) {
	        
	    } else if(expr instanceof SimpleUnaryExpression) {
	        
	    } else if(expr instanceof StringExpression) {
	        
	    } else if(expr instanceof StructExpression) {
	        return estimateNumItems(((StructExpression) expr).getExpression());
	    } else if(expr instanceof SumExpression) {
	        
	    } else if(expr instanceof ToBagExpression) {
	        return estimateNumItems(((ToBagExpression) expr).getExpression());
	    } else if(expr instanceof ToBooleanExpression) {
	        
	    } else if(expr instanceof ToDateExpression) {
	        
	    } else if(expr instanceof ToIntegerExpression) {
	        
	    } else if(expr instanceof ToRealExpression) {
	        
	    } else if(expr instanceof ToSingleExpression) {
	        
	    } else if(expr instanceof ToStringExpression) {
	        
	    } else if(expr instanceof UnionExpression) {
	        UnionExpression ue = (UnionExpression) expr;
	        return estimateNumItems(ue.getLeftExpression()) + estimateNumItems(ue.getRightExpression());
	    } else if(expr instanceof UniqueExpression) {
	        return estimateNumItems(((UniqueExpression) expr).getExpression());
	    } else if(expr instanceof WhereExpression) {
	        return estimateNumItems(((WhereExpression) expr).getLeftExpression());
	    }
	    return 1;
	}
	
	private void addEstimate(double e) {
	    if(e > 0.0) estimate += e;
	}
	
	private NameExpression getRightNameExpression(BinaryExpression e) {
	    Expression right = e.getRightExpression();
	    if(right instanceof NameExpression) return (NameExpression) right;
	    if(right instanceof BinaryExpression) return getRightNameExpression((BinaryExpression) right);
	    return null;
	}
	
	@Override
	protected Object commonVisitStatement(Statement stmt, Object attr) throws SBQLException {
	    warn(stmt);
	    return null;
	}

	@Override
	protected Object commonVisitExpression(Expression expr, Object attr) throws SBQLException {
	    warn(expr);
	    return null;
	}

	@Override
	protected Object commonVisitLiteral(Expression expr, Object attr) throws SBQLException {
	    // do nothing
	    return null;
	}
	
	@Override
	protected Object commonVisitUnaryExpression(UnaryExpression expr, Object attr) throws SBQLException {
	    warn(expr);
	    return null;
	}

	@Override
	protected Object commonVisitBinaryExpression(BinaryExpression expr, Object attr) throws SBQLException {
	    warn(expr);
	    return null;
	}

	@Override
	protected Object commonVisitAlgebraicExpression(BinaryExpression expr, Object attr) throws SBQLException {
	    warn(expr);
	    return null;
	}

	@Override
	protected Object commonVisitNonAlgebraicExpression(NonAlgebraicExpression expr, Object attr) throws SBQLException {
	    warn(expr);
	    return null;
	}

	@Override
	protected Object commonVisitParallelExpression(ParallelExpression expr, Object attr) throws SBQLException {
	    warn(expr);
	    return null;
	}

	@Override
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    // unaccounted for
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
	    Expression child = expr.getExpression();
	    child.accept(this, attr);
	    int x = estimateNumItems(child) - 1;
	    addEstimate(0.171303 + 0.00296116 * x + 0.000000791336 * x * x);
	    return null;
	}

	@Override
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException {
	    // literal
	    // negligible
	    return null;
	}

	@Override
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
	    // assume expression contains only literals
	    int x = estimateNumItems(expr) - 1;
	    if(x >= 182) addEstimate(0.226851 - 0.00550817 * x + 0.0000146711 * x * x + 0.0000000480612 * x * x * x);
	    return null;
	}

	@Override
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
	    expr.getConditionExpression().accept(this, attr);
	    expr.getThenExpression().accept(this, attr);
	    expr.getElseExpression().accept(this, attr);
	    return null;
	}

	@Override
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
	    expr.getConditionExpression().accept(this, attr);
	    expr.getThenExpression().accept(this, attr);
	    return null;
	}

	@Override
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
	    // unaccounted for
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
	    // unaccounted for
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
	    // unaccounted for
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
	    // unaccounted for
	    expr.getExpression().accept(this, attr);
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
	    // unaccounted for
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitBinaryExpression(expr, attr);
	}

	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    NameExpression nameExpr = getRightNameExpression(expr);
	    if(nameExpr != null) {
	        System.out.println("maxCard = " + nameExpr.getSignature().getMaxCard());
    	    // TODO implement close by
	        return null;
	    }
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
	    // negligible
	    return null;
	}

	@Override
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
	    // negligible
	    return null;
	}

	@Override
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
	    Expression itemExpr = expr.getLeftExpression();
	    itemExpr.accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    int x = estimateNumItems(itemExpr) - 1;
	    addEstimate(0.368536 + 0.000115264 * x);
	    return null;
	}

	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
	    Expression itemExpr = expr.getLeftExpression();
	    itemExpr.accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    int x = estimateNumItems(itemExpr) - 1;
	    addEstimate(-0.0183684 + 0.000000663148 * x * x);
	    return null;
	}

	@Override
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
	    Expression child = expr.getExpression();
	    child.accept(this, attr);
	    int x = estimateNumItems(child) - 1;
	    addEstimate(-0.00591995 + 0.000000201902 * x * x);
	    return null;
	}

	@Override
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    int x = estimateNumItems(expr.getRightExpression()) - 1;
	    addEstimate(-0.0438918 + 0.000000617165 * x * x);
	    return null;
	}

	@Override
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException {
	    // negligible
	    return null;
	}

	@Override
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    // negligible
	    return commonVisitAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    int x = estimateNumItems(expr) - 5;
	    addEstimate(0.993985 + 0.0000860779 * x * x);
	    return null;
	}

	@Override
	public Object visitLazyFailureExpression(LazyFailureExpression expr, Object attr) throws SBQLException {
	    // unsupported because unknown
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
	    Expression child = expr.getExpression();
	    child.accept(this, attr);
	    int x = estimateNumItems(child) - 1;
	    addEstimate(-0.00206558 + 0.0000179761 * x);
	    return null;
	}

	@Override
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
	    Expression child = expr.getExpression();
	    child.accept(this, attr);
	    int x = estimateNumItems(child) - 1;
	    if(x >= 831) addEstimate(0.247735 - 0.00106815 * x + 0.00000092684 * x * x);
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
	    System.out.printf("name = %s%n", expr.name().value());
	    Signature sig = expr.getSignature();
	    System.out.printf("min = %d, max = %d%n", sig.getMinCard(), sig.getMaxCard());
	    // TODO implement name look-up
	    return commonVisitExpression(expr, attr);
	}

	@Override
	public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException {
	    // TODO implement name look-up
	    return commonVisitExpression(expr, attr);
	}   

	@Override
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
	    Expression left = expr.getLeftExpression();
	    left.accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    int x = estimateNumItems(left) - 1;
	    addEstimate(-10.0698 + 0.893485 * x);
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
	    // negligible
	    return null;
	}

	@Override
	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, Object attr) throws SBQLException {
	    stmt.getExpression().accept(this, attr);
	    return null;
	}

	@Override
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
	    // assumed negligible
	    return null;
	}

	@Override
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
	    expr.getFirstExpression().accept(this, attr);
	    expr.getSecondExpression().accept(this, attr);
	    return null;
	}

	@Override
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    switch(expr.O.getAsInt()) {
	    case Operator.PLUS:
	    case Operator.MINUS:
	    case Operator.MULTIPLY:
	    case Operator.DIVIDE:
	    case Operator.EQUALS:
	    case Operator.GREATER:
	    case Operator.LOWER:
	    case Operator.GREATEREQUALS:
	    case Operator.LOWEREQUALS:
	    case Operator.OR:
	    case Operator.AND:
	    case Operator.NOT:
	    case Operator.DIFFERENT:
	    case Operator.MODULO:
	        // negligible
	        return null;
	    case Operator.MATCH_STRING:
	    case Operator.NOT_MATCH_STRING:
	        int x = ((StringExpression) expr.getRightExpression()).getLiteral().value().length();
	        addEstimate(x < 103 ? 0.676408 : 2.00131);
	        return null;
	    case Operator.ASSIGN:
	    default:
	        return commonVisitAlgebraicExpression(expr, attr);
	    }
	}

	@Override
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException {
	    // literal
	    // negligible
	    return null;
	}

	@Override
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
	    Expression child = expr.getExpression();
	    child.accept(this, attr);
	    int x = estimateNumItems(child) - 1;
	    addEstimate(-0.161127 + 0.00383586 * x);
	    return null;
	}

	@Override
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    int x = estimateNumItems(expr) - 4;
	    if(x >= 654) addEstimate(0.195218 - 0.000825259 * x + 0.000000805861 * x * x);
	    return null;
	}

	@Override
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException { // also handles uniqueref
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
	    // TODO implement where
	    return commonVisitNonAlgebraicExpression(expr, attr);
	}

	@Override
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException { // Object[index]
	    expr.getLeftExpression().accept(this, attr);
	    expr.getRightExpression().accept(this, attr);
	    // negligible
	    return null;
	}
	
	@Override
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // assumed negligible
	    return null;
	}

	@Override
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
	}

	@Override
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
	    expr.getExpression().accept(this, attr);
	    int x = estimateNumItems(expr) - 1;
	    addEstimate(4.00191 + 0.0000150654 * x * x);
	    return null;
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
	    // unknown
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
	    // assume negligible
	    return null;
	}

	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
	    // assume negligible
	    return null;
	}

	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {
	    // TODO what is this?
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
	    // literal
	    // negligible
	    return null;
	}

	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitBinaryExpression(expr, attr);
	}

	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitBinaryExpression(expr, attr);
	}
	  
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitBinaryExpression(expr, attr);
	}

	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitBinaryExpression(expr, attr);
	}

	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression node, Object attr) throws SBQLException {
	    // TODO implement closeuniqueby
	    return commonVisitNonAlgebraicExpression(node, attr);
	}

	@Override
	public Object visitLeavesByExpression(LeavesByExpression node, Object attr) throws SBQLException {
	    // TODO implement leavesby
	    return commonVisitNonAlgebraicExpression(node, attr);
	}

	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression node, Object attr) throws SBQLException {
	    // unknown
	    return commonVisitNonAlgebraicExpression(node, attr);
	}

	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException {
	    return commonVisitUnaryExpression(expr, attr);
	}

	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr) throws SBQLException {
	    // TODO what is this?
	    return commonVisitUnaryExpression(expr, attr);
	}
	
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr) throws SBQLException {
	    // TODO what is this?
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
	    List<Expression> exprs = expr.getParallelExpressions();
	    for(Expression e : exprs) {
	        e.accept(this, attr);
	    }
	    int x = exprs.size() - 1;
	    addEstimate(-0.277017 + 0.0000292739 * x * x);
	    return null;
	}

	@Override
	public Object visitTransactionAbortStatement(TransactionAbortStatement stmt, Object attr) throws SBQLException {
	    return this.commonVisitStatement(stmt, attr);
	}
	
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
	    expr.getExpression().accept(this, attr);
	    // negligible
	    return null;
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
