package odra.sbql.typechecker;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.StdEnvironment;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBBinaryOperator;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBIndex;
import odra.db.objects.meta.MBLink;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBObjectFactory;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBUnaryOperator;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MBVirtualVariable;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.OdraViewSchema.GenericNames;
import odra.sbql.SBQLException;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
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
import odra.sbql.ast.expressions.TransitiveClosureExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.CollectionKind;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.sbql.stack.BindingInfo;
import odra.sbql.typechecker.utils.DereferenceEnforcer;
import odra.sbql.typechecker.utils.IsDistributedAST;
import odra.sbql.typechecker.utils.MaximalCardinalityEnforcer;
import odra.sbql.typechecker.utils.MinimalCardinalityEnforcer;
import odra.sbql.typechecker.utils.SingleElementEnforcer;
import odra.sessions.Session;
import odra.system.Names;
import odra.system.config.ConfigServer;

/**
 * This class implements the type checker. It is an instance of the Visitor
 * design pattern.
 * 
 * @author raist, stencel, radamus
 */

public class SBQLTypeChecker extends ASTAdapter {
	protected ASTNode root; // typechecked AST

	protected StdEnvironment env;

	protected boolean bindTop = false; // should name binding be performed only

	// on top ENVS

	protected DBModule module;

	protected StaticEnvironmentManager staticEnvsManager;
	protected String errorInfoPrefix;

	// protected EnvironmentInfo topEnvironment;

	/**
	 * Initializes a new SBQLTypeChecker object
	 * 
	 * @param module
	 *            is an existing module. The type check will be performed within
	 *            this module's environment.
	 */
	public SBQLTypeChecker(DBModule module, boolean isParmDependent) throws TypeCheckerException {
		assert module != null : "module != null";
		this.module = module;

		env = StdEnvironment.getStdEnvironment();
		try {
			this.setSourceModuleName(module.getName());
			staticEnvsManager = new StaticEnvironmentManager(this.module, isParmDependent);
		} catch (DatabaseException e) {
			throw new TypeCheckerException("Error while typechecker initialization " + e.getMessage(), e);
		}

	}

	public SBQLTypeChecker(DBModule module) throws TypeCheckerException {
		this(module, false);
	}
	public ASTNode typecheckAdHocQuery(Expression node, Signature required) throws TypeCheckerException {
		Expression expr = (Expression)typecheckAdHocQuery(node);
		checkSigTypeCompatibility(required, expr.getSignature(), expr, null, false);
		return node;
	}
	/**
	 * Type-checks ad'hoc query
	 * 
	 * @param node
	 *            - the root node of the query AST
	 * @param autoderef
	 *            - if true the final result will be dereferenced,
	 * @return the type-checked AST
	 * @throws TypeCheckerException
	 */
	public ASTNode typecheckAdHocQuery(ASTNode node) throws TypeCheckerException {
		root = node;		
		try {

			root.accept(this, null);
			
			return root;
		} catch (TypeCheckerException e) {

			if (isASTNodeLinksMetabasesUptodate(root))
				throw new TypeCheckerException("Stale links metabase. The links metabases have been refresh.");
			String dump = "";
			try {
				dump = AST2TextQueryDumper.AST2Text(root);
				ConfigServer.getLogWriter().getLogger().log(Level.FINEST,
						"Query compilation error: \n query: " + dump + "\n error: " + e.getMessage());
			} catch (Exception e1) {
				ConfigServer.getLogWriter().getLogger().log(Level.FINEST, "error while trying to create detiled excetion info");
			}
			throw e;

		}

	}

	/**
	 * Typechecks an AsExpression
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);
		Signature sig = expr.getExpression().getSignature();

		// clone the signature sig and set its cardinality to [1..1]
		Signature csig = sig.clone();
		csig.setMinCard(1);
		csig.setMaxCard(1);

		// csig.associator = sig.generator;
		// csig.generator = expr;

		// build the signature of the result of the visited expression
		SBQLTypeCheckerHelper.name2id(expr.name().value());
		BinderSignature bsig = new BinderSignature(expr.name().value(), csig, true);
		bsig.setMinCard(sig.getMinCard());
		bsig.setMaxCard(sig.getMaxCard());

		// assign the signature to the visited expression
		expr.setSignature(bsig);
		// set the current node the generator of the signature
		// for the as expression the signature associator is the source
		// signature generator
		bsig.setAssociatedExpression(sig.getOwnerExpression());

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks an GroupAsExpression
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);
		Signature sig = expr.getExpression().getSignature();
		// build the signature of the result of the visited expression
		// resulting cardinality [1..1] is not assigned, since it is the default
		// for signatures
		SBQLTypeCheckerHelper.name2id(expr.name().value());

		Signature csig = sig.clone();

		// csig.associator = sig.generator;
		// csig.generator = expr;
		BinderSignature bsig = new BinderSignature(expr.name().value(), csig, true);

		// assign the signature to the visited expression
		expr.setSignature(bsig);
		// for the group as expression the signature associator is the source
		// signature generator
		expr.getSignature().setAssociatedExpression(sig.getOwnerExpression());

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a CommaExpression
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		expr.getRightExpression().accept(this, attr);
		Signature sig2 = expr.getRightExpression().getSignature();
		StructSignature ssig = SBQLTypeCheckerHelper.createStructSignature(sig1, sig2);

		// assign the signature to the visited expression
		expr.setSignature(ssig);

		// TODO : JM decoration with links

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitIfThenExpression(odra.sbql.ast.expressions
	 * .IfThenExpression, java.lang.Object)
	 */
	@Override
	public Object visitIfThenExpression(IfThenExpression expr, Object attr) throws SBQLException {
		expr.getConditionExpression().accept(this, attr);
		this.enforceDereference(expr.getConditionExpression(), attr);
		this.enforceSingleElement(expr.getConditionExpression(), attr);
		Signature sig1 = expr.getConditionExpression().getSignature();
		expr.getThenExpression().accept(this, attr);
		Signature sig2 = expr.getThenExpression().getSignature();
		// check the conditional expression
		requireValue(sig1, new OID[] { env.booleanType }, "", expr.getConditionExpression());
		Signature csig2 = sig2.clone();
		// set the minimal caldinality to 0 (if the condition is false the
		// result i an empty bag)
		csig2.setMinCard(0);

		// assign the signature to the visited expression
		expr.setSignature(csig2);

		return null;
	}

	/**
	 * Typechecks a IfThenElseExpression
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	@Override
	public Object visitIfThenElseExpression(IfThenElseExpression expr, Object attr) throws SBQLException {
		expr.getConditionExpression().accept(this, attr);
		this.enforceDereference(expr.getConditionExpression(), attr);
		this.enforceSingleElement(expr.getConditionExpression(), attr);
		Signature sig1 = expr.getConditionExpression().getSignature();
		expr.getThenExpression().accept(this, attr);
		Signature sig2 = expr.getThenExpression().getSignature();

		expr.getElseExpression().accept(this, attr);
		Signature sig3 = expr.getElseExpression().getSignature();

		// check the conditional expression
		requireValue(sig1, new OID[] { env.booleanType }, "", expr.getConditionExpression());

		// clone the second signature before checking comparablity
		// we don't want the cardinality to blur the comparability
		Signature csig2 = sig2.clone();
		csig2.setMinCard(sig3.getMinCard());
		csig2.setMaxCard(sig3.getMaxCard());
		if (!csig2.isComparableTo(sig3))
			// FIXME: variants!!!
			throw exception("Conditional expression non-comparable signatures not implemented yet.", expr);

		// FIXME result signature without variants is any of sig2/sig3 with
		// cardinality tuned
		csig2.setMinCard(sig3.getMinCard() > sig2.getMinCard() ? sig2.getMinCard() : sig3.getMinCard());
		csig2.setMaxCard(sig3.getMaxCard() > sig2.getMaxCard() ? sig3.getMaxCard() : sig2.getMaxCard());

		// assign the signature to the visited expression
		expr.setSignature(csig2);
		return null;
	}

	/**
	 * Typechecks a CountExpression. Type check is very simple since this
	 * expression is always type correct, provided the argument is type correct.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);

		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(StdEnvironment.getStdEnvironment().integerType));

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a MinExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);

		// add an authomatic derefence to the second argument if neccessatry
		this.enforceDereference(expr.getExpression(), attr);
		Signature sig = expr.getExpression().getSignature();
		// check id the argument is a primitive value
		ValueSignature vsig = requireValue(sig, new OID[0], "", expr);

		// check if an appropriate comparison operator exists
		MBBinaryOperator okmbop = findBinaryOperator("<", vsig.value, vsig.value, expr);

		if (okmbop == null)
			throw exception(SBQLTypeCheckerHelper.getObjectName(vsig.value) + " has no comparison op; cannot be arg of Min", expr);

		// build the result signature by cloning the argument signature
		Signature rsig = vsig.clone();
		// if the min argument cardinality is 0 greater than runtime-error may
		// occur
		// the result min cardinality is set to 1
		rsig.setMinCard(1);

		// set the maximum cardinality to 1
		rsig.setMaxCard(1);

		// assign the signature to the visited expression
		expr.setSignature(rsig);

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a MaxExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);

		// add an automatic dereference to the second argument if necessary
		this.enforceDereference(expr.getExpression(), attr);
		Signature sig = expr.getExpression().getSignature();
		// check id the argument is a primitive value
		ValueSignature vsig = requireValue(sig, new OID[0], "", expr);
		// check if an appropriate comparison operator exists
		MBBinaryOperator okmbop = findBinaryOperator("<", vsig.value, vsig.value, expr);

		if (okmbop == null)
			throw exception(SBQLTypeCheckerHelper.getObjectName(vsig.value) + " has no comparison op; cannot be arg of Max", expr);

		// build the result signature by cloning the argument signature
		Signature rsig = vsig.clone();
		// if the min argument cardinality is 0 greater than runtime-error may
		// occur
		// the result min cardinality is set to 1
		rsig.setMinCard(1);

		// set the maximum cardinality to 1
		rsig.setMaxCard(1);

		// assign the signature to the visited expression
		expr.setSignature(rsig);
		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a AvgExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);

		// add an automatic dereference to the second argument if necessary
		this.enforceDereference(expr.getExpression(), attr);
		// add runtime check for at least one element bag
		this.enforceMinimalCardinality(1, expr.getExpression(), attr);
		Signature sig = expr.getExpression().getSignature();

		// check id the argument is a primitive value
		ValueSignature vsig = requireValue(sig, new OID[] { env.realType, env.integerType }, "", expr);

		// build the result signature by cloning the argument signature
		Signature rsig = new ValueSignature(env.realType);
		// set the result cardinality to [1..1]
		rsig.setMinCard(1);
		rsig.setMaxCard(1);
		// copy the type name if the argument signature is real
		if (vsig.value.equals(env.realType))
			rsig.setTypeName(vsig.getTypeName());

		// assign the signature to the visited expression
		expr.setSignature(rsig);
		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a SumExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);

		// add an automatic dereference to the argument (if necessary)
		this.enforceDereference(expr.getExpression(), attr);
		// add runtime check for at least one element bag (if necessary)
		this.enforceMinimalCardinality(1, expr.getExpression(), attr);
		Signature sig = expr.getExpression().getSignature();

		// check id the argument is a primitive value
		ValueSignature vsig = requireValue(sig, new OID[] { env.realType, env.integerType }, "", expr.getExpression());

		// build the result signature by cloning the argument signature
		Signature rsig = vsig.clone();
		// set the result cardinality to [1..1]
		rsig.setMinCard(1);
		rsig.setMaxCard(1);

		// assign the signature to the visited expression
		expr.setSignature(rsig);
		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a BooleanExpression. Type check is very simple since this
	 * expression is always type correct.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitBooleanExpression(BooleanExpression expr, Object attr) throws SBQLException {
		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.booleanType));
		return null;
	}

	/**
	 * Typechecks a IntegerExpression. Type check is very simple since this
	 * expression is always type correct.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitIntegerExpression(IntegerExpression expr, Object attr) throws SBQLException {
		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.integerType));

		return null;
	}

	/**
	 * Typechecks a RealExpression. Type check is very simple since this
	 * expression is always type correct.
	 * 
	 * @param expr
	 *            is the RealExpression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitRealExpression(RealExpression expr, Object attr) throws SBQLException {
		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.realType));
		return null;
	}

	/**
	 * Typechecks a StringExpression. Type check is very simple since this
	 * expression is always type correct.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitStringExpression(StringExpression expr, Object attr) throws SBQLException {
		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.stringType));
		return null;
	}
	
	public Object visitLazyFailureExpression(LazyFailureExpression expr, Object attr) throws SBQLException {

		if ((expr.getParentExpression() == null) || !(expr.getParentExpression() instanceof AsExpression))
			throw exception("LazyFailureExpression should be surrounded by AsExpression", expr);

		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);

		// assign the signature to the visited expression
		Signature sig = expr.getExpression().getSignature();
		Signature csig = sig.clone();
		// assign the signature to the visited expression
		expr.setSignature(csig);
		
		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}
	

	/**
	 * Typechecks a DotExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getLeftExpression().accept(this, attr);

		// try to avoid false binding
		if (expr.getRightExpression() instanceof NameExpression)
			this.bindTop = true;
		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);
		this.bindTop = false;

		// get the signature of the first argument expression
		Signature sig1 = expr.getLeftExpression().getSignature();

		Signature sig2 = expr.getRightExpression().getSignature();

		// build the signature of the result of the visited expression
		Signature rsig = sig2.clone();

		rsig.setMinCard(Signature.cardinalityMult(sig1.getMinCard(), sig2.getMinCard()));
		rsig.setMaxCard(Signature.cardinalityMult(sig1.getMaxCard(), sig2.getMaxCard()));

		// assign the signature to the visited expression
		expr.setSignature(rsig);
		// set the current node the generator of the signature
		expr.getSignature().setAssociatedExpression(sig2.getOwnerExpression());

		// decorate expression & signature with links
		decorateNonAlgebraicWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a JoinExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression
		expr.getLeftExpression().accept(this, attr);
		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);

		// get the signature of the first argument expression
		Signature sig1 = expr.getLeftExpression().getSignature();
		// get the signature of the second argument expression
		Signature sig2 = expr.getRightExpression().getSignature();

		// build the signature of the result of the visited expression
		StructSignature ssig = SBQLTypeCheckerHelper.createStructSignature(sig1, sig2);

		// assign the signature to the visited expression
		expr.setSignature(ssig);
		// decorate expression & signature with links
		decorateNonAlgebraicWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a WhereExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitWhereExpression(WhereExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getLeftExpression().accept(this, attr);

		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);
		// add an authomatic derefence to the second argument if neccessatry
		this.enforceDereference(expr.getRightExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getRightExpression(), attr);

		// get the signature of the second argument expression
		Signature sig2 = expr.getRightExpression().getSignature();
		requireValue(sig2, new OID[] { env.booleanType }, "", expr.getRightExpression());

		// build the signature of the result of the visited expression
		Signature sig1 = expr.getLeftExpression().getSignature();
		Signature rsig = sig1.clone();
		rsig.setMinCard(0); // all objects may fail the test

		// assign the signature to the visited expression
		expr.setSignature(rsig);
		expr.getSignature().setAssociatedExpression(sig1.getOwnerExpression());

		// decorate expression & signature with links
		decorateNonAlgebraicWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a ForAllExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getLeftExpression().accept(this, attr);

		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);
		// add an authomatic derefence to the second argument if neccessatry
		this.enforceDereference(expr.getRightExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getRightExpression(), attr);

		// get the signature of the second argument expression
		Signature sig2 = expr.getRightExpression().getSignature();
		requireValue(sig2, new OID[] { env.booleanType }, "", expr.getRightExpression());

		expr.setSignature(new ValueSignature(env.booleanType));

		// decorate expression & signature with links
		decorateNonAlgebraicWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a ForSomeExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getLeftExpression().accept(this, attr);

		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);
		// add an authomatic derefence to the second argument if neccessatry
		this.enforceDereference(expr.getRightExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getRightExpression(), attr);

		// get the signature of the second argument expression
		Signature sig2 = expr.getRightExpression().getSignature();

		requireValue(sig2, new OID[] { env.booleanType }, "", expr.getRightExpression());

		expr.setSignature(new ValueSignature(env.booleanType));

		return null;
	}

	/**
	 * Typechecks an ExistsExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitExistsExpression(ExistsExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getExpression().accept(this, attr);
		// build the signature of the result
		expr.setSignature(new ValueSignature(env.booleanType));

		return null;
	}

	/**
	 * Typechecks an UniqueExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getExpression().accept(this, attr);

		if (!expr.isUniqueref())
			this.enforceDereference(expr.getExpression(), attr);

		Signature sig1 = expr.getExpression().getSignature();

		if (expr.isUniqueref()) {
			if (!(sig1 instanceof ReferenceSignature))
				throw exception("uniqueref expression requires a bag of references", expr);
		} else if (sig1 instanceof ReferenceSignature)
			throw exception("unique expression requires a bag of values", expr);

		// build the signature of the result
		expr.setSignature(sig1.clone());

		// unique may leave only one item as the result
		// (all of them might be duplicates)
		if (sig1.getMinCard() > 1)
			expr.getSignature().setMinCard(1);

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks an OrderByExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitOrderByExpression(OrderByExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression
		expr.getLeftExpression().accept(this, attr);
		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);

		// add single element coercions and dereferences to all struct items
		addStructuralCoercions(expr.getRightExpression());

		// revisit the expression
		this.evalAgainstNestedEnvironment(expr, attr);

		// get the signature of the first argument expression
		Signature sig1 = expr.getLeftExpression().getSignature();
		// get the signature of the second argument expression
		Signature sig2 = expr.getRightExpression().getSignature();

		// if sig2 is not StructSignature, build one, so that further we deal
		// with fields of a struct
		StructSignature sig2prime;

		if (!(sig2 instanceof StructSignature)) {
			sig2prime = new StructSignature();
			sig2prime.addField(sig2);
		} else
			sig2prime = (StructSignature) sig2;

		for (Signature ssig : sig2prime.getFields()) {
			if (!(ssig instanceof ValueSignature))
				throw exception("2nd arg of OrderBy should be a primitive value or a struct of those ", expr);

			ValueSignature vsig = (ValueSignature) ssig;
			// check if an appropriate comparison operator exists
			MBBinaryOperator okmbop = findBinaryOperator("<", vsig.value, vsig.value, expr);

			if (okmbop == null)
				throw exception(SBQLTypeCheckerHelper.getObjectName(vsig.value) + " has no comparison op; cannot be second arg of OrderBy", expr);
		}

		// check id the argument is a primitive value
		// build the signature of the result of the visited expression
		Signature rsig = sig1.clone();
		rsig.setCollectionKind(new CollectionKind(CollectionKind.SEQUENCE_COLLECTION));

		// assign the signature to the visited expression
		expr.setSignature(rsig);
		expr.getSignature().setAssociatedExpression(sig1.getOwnerExpression());
		// decorate expression & signature with links
		decorateNonAlgebraicWithLinks(expr);

		return null;
	}

	/**
	 * Add a dereference and single element coercion if necessary to the current
	 * node and to its subnodes in case of CommaExpression.
	 * 
	 * @param e
	 *            is the node to be augmented.
	 */

	private void addStructuralCoercions(Expression e) throws SBQLException {
		if (!(e instanceof CommaExpression)) {
			// add an automatic dereference to the second argument if necessary
			this.enforceDereference(e, null);

			if (e.getParentExpression() instanceof DerefExpression)
				e = e.getParentExpression();
			// add coercion to [1..1] cardinality (run-time check) if necessary
			this.enforceSingleElement(e, null);
		} else {
			CommaExpression ce = (CommaExpression) e;
			this.addStructuralCoercions(ce.getLeftExpression());
			this.addStructuralCoercions(ce.getRightExpression());
		}
	}

	/**
	 * Typechecks a NameExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		// bind the name on the static environment stack
		BindingInfo bi = new BindingInfo();

		AbstractQueryResult[] sig;
		try {
			if (!this.bindTop)
				sig = this.staticEnvsManager.bind(expr.name().value(), bi);
			else
				sig = this.staticEnvsManager.bindTop(expr.name().value(), bi);
		} catch (DatabaseException e1) {
			throw exception(e1, expr);
		}
		// if the name cannot be bound
		// FIXME: multiple bindings are possible (as a result of join) -
		// variants
		if (sig.length != 1) {
			if (sig.length == 0)
				throw exception(
						"Name '" + expr.name().value() + "' (" + SBQLTypeCheckerHelper.name2id(expr.name().value()) + ") cannot be resolved.", expr);
			throw exception(ERROR_NAME_CONFLICT + expr.name().value() + "' (" + SBQLTypeCheckerHelper.name2id(expr.name().value()) + ")", expr);
		}

		// R.A. assign the information about the stack section number where
		// the
		// name was bound
		expr.setBindingInfo(bi);

		boolean[] isAuxiliary = new boolean[1];
		isAuxiliary[0] = false;
		expr.setAssociated(SBQLTypeCheckerHelper.findAssociatedNameExpression((Signature) sig[0], expr.name().value(), isAuxiliary));
		expr.setAuxiliary(isAuxiliary[0]);

		// assign the signature to the visited expression
		// radamus - signature cloning added
		expr.setSignature(((Signature) sig[0]).clone());
		// expr.getSignature().associator = (((Signature) sig[0]).generator);
		try {
			Signature ressig = expr.getSignature();
			if (ressig instanceof ReferenceSignature) {
				ReferenceSignature refressig = (ReferenceSignature) ressig;
				if (refressig.isVirtual())
					expr.virtualBind = true;
				// else {
				this.enforceVirtual(refressig);
				// if(!this.enforceVirtual(refressig))
				// this.enforceProcedureCall(expr);
				// }
				
				if (new MBLink(refressig.value).isValid() && bi.boundat == 0) {
					// decorate siganture with link
					refressig.addLinkDecoration(refressig.value);
				} else if ((bi.boundat == 0) && (refressig.links.size() == 0)) {
					// decorate with localhost
					AbstractQueryResult[] sigLocalhost = this.staticEnvsManager.bind(Names.namesstr[Names.LOCALHOST_LINK], new BindingInfo());
					if ((sigLocalhost.length == 1) && (((Signature) sigLocalhost[0]) instanceof ReferenceSignature)) {
						refressig.addLinkDecoration(((ReferenceSignature) sigLocalhost[0]).value);
					}
				}

				// copy links' deocrations form signature to AST
				expr.links.clear();
				expr.links.addAll(refressig.links);
			}

		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		return null;
	}

	// TW
	public Object visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException {

		// TODO implement
		return null;
	}

	/**
	 * Typechecks an EqualityExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);

		// add automatic dereferences (if neccesary)
		this.enforceDereference(expr.getLeftExpression(), attr);
		this.enforceDereference(expr.getRightExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getLeftExpression(), attr);
		this.enforceSingleElement(expr.getRightExpression(), attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		Signature sig2 = expr.getRightExpression().getSignature();
		// check if the operands are primitive values
		// if no then, the signature method isComparableTo is employed
		if (!sig1.isComparableTo(sig2)) {
			if (!(sig1 instanceof ValueSignature) || !(sig2 instanceof ValueSignature) || !sig1.isTypeNameCompatible(sig2)) {
				throw exception("The values of the following signatures are not comparable:\n" + sig1.dump("") + "\n" + sig2.dump(""), expr);
			}
			// both signature are values and are type name compatible and
			// directly non-comparable
			// we will try coercion
			ValueSignature vsig1 = (ValueSignature) sig1;
			ValueSignature vsig2 = (ValueSignature) sig2;

			MBBinaryOperator okmbop = findBinaryOperator(expr.O.spell(), vsig1.value, vsig2.value, expr);

			// unknown operator
			try {
				if (okmbop == null) {
					MBPrimitiveType leftType = new MBPrimitiveType(vsig1.value);
					MBPrimitiveType rightType = new MBPrimitiveType(vsig2.value);

					throw exception("Operator '" + expr.O.spell() + "' cannot be applied to values of types '"
							+ SBQLTypeCheckerHelper.getObjectName(leftType) + "' and '" + SBQLTypeCheckerHelper.getObjectName(rightType) + "'", expr);
				}

				// insert coercions (if necessary)
				if (okmbop.getLeftCoercion() != null)
					insertPrimitiveTypeCoercion(okmbop.getLeftCoercion(), expr.getLeftExpression(), attr);

				if (okmbop.getRightCoercion() != null)
					insertPrimitiveTypeCoercion(okmbop.getRightCoercion(), expr.getRightExpression(), attr);
			} catch (DatabaseException e) {
				throw exception(e, expr);
			}
		}

		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.booleanType));
		// decorate expression & signature with links
		decorateBinaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a InExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitInExpression(InExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);

		// add automatic dereferences (if neccesary)
		this.enforceDereference(expr.getLeftExpression(), attr);
		this.enforceDereference(expr.getRightExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getLeftExpression(), attr);

		Signature sig1 = expr.getLeftExpression().getSignature();

		// clone the second signature and set its cardinality to [1..1]
		// this way we get comparable signatures, even though the second
		// argument of "in" can be bulk
		Signature sig2 = expr.getRightExpression().getSignature().clone();
		sig2.setMinCard(1);
		sig2.setMaxCard(1);

		// check if the operands are comparable
		if (!sig1.isComparableTo(sig2))
			throw exception("In used with incomparable values:\n" + sig1.dump("") + "\n" + sig2.dump(""), expr);

		expr.setSignature(new ValueSignature(StdEnvironment.getStdEnvironment().booleanType));

		// decorate expression & signature with links
		decorateBinaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a SimpleBinaryExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);

		// add automatic dereferences (if neccesary)
		this.enforceDereference(expr.getLeftExpression(), attr);
		this.enforceDereference(expr.getRightExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		Signature sig1 = this.enforceSingleElement(expr.getLeftExpression(), attr).getSignature();
		Signature sig2 = this.enforceSingleElement(expr.getRightExpression(), attr).getSignature();

		// check the operands
		ValueSignature vsig1 = requireValue(sig1, new OID[0], "", expr.getLeftExpression());
		ValueSignature vsig2 = requireValue(sig2, new OID[0], "", expr.getRightExpression());

		try {
			MBBinaryOperator okmbop = findBinaryOperator(expr.O.spell(), vsig1.value, vsig2.value, expr);

			// unknown operator
			if (okmbop == null) {
				MBPrimitiveType leftType = new MBPrimitiveType(vsig1.value);
				MBPrimitiveType rightType = new MBPrimitiveType(vsig2.value);

				throw exception("Operator '" + expr.O.spell() + "' cannot be applied to values of types '"
						+ SBQLTypeCheckerHelper.getObjectName(leftType) + "' and '" + SBQLTypeCheckerHelper.getObjectName(rightType) + "'", expr);
			}

			expr.operator = okmbop.getOID();

			// assign the signature to the visited expression
			expr.setSignature(new ValueSignature(okmbop.getResultType()));

			// insert coercions (if necessary)
			if (okmbop.getLeftCoercion() != null)
				insertPrimitiveTypeCoercion(okmbop.getLeftCoercion(), expr.getLeftExpression(), attr);

			if (okmbop.getRightCoercion() != null)
				insertPrimitiveTypeCoercion(okmbop.getRightCoercion(), expr.getRightExpression(), attr);
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		// decorate expression & signature with links
		decorateBinaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Find the binary operator aplicable to the given primitive types.
	 * 
	 * @param opstr
	 *            is the textual symbol representing the operator.
	 * @param ltype
	 *            is the OID of the metabase object representing the type of the
	 *            left operand.
	 * @param rtype
	 *            is the OID of the metabase object representing the type of the
	 *            right operand.
	 */
	private MBBinaryOperator findBinaryOperator(String opstr, OID ltype, OID rtype, Expression expr) throws TypeCheckerException {
		// bind the operator
		try {
			AbstractQueryResult[] rsig = this.staticEnvsManager.bind(opstr);
			//		
			// issue a type error, if no such operator exists
			if (rsig.length == 0)
				throw exception("Unknown operator '" + opstr + "'", expr);

			// find the version of the operator appropriate for the given
			// operands'
			// types
			for (int i = 0; i < rsig.length; i++) {
				ReferenceSignature r = (ReferenceSignature) rsig[i];

				MBBinaryOperator mbop = new MBBinaryOperator(r.value);

				// is it a binary operator?
				if (!mbop.isValid())
					continue;

				// does it fit the given argument types?
				if (mbop.getLeftType().equals(ltype) && mbop.getRightType().equals(rtype))
					return mbop;
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		return null;
	}

	/**
	 * Find the unary operator aplicable to the given primitive types.
	 * 
	 * @param opstr
	 *            is the textual symbol representing the operator.
	 * @param type
	 *            is the OID of the metabase object representing the type of the
	 *            operand.
	 */
	private MBUnaryOperator findUnaryOperator(String opstr, OID type, Expression expr) throws TypeCheckerException {
		// bind the operator
		try {
			AbstractQueryResult[] rsig = this.staticEnvsManager.bind(opstr);

			// issue a type error, if no such operator exists
			if (rsig.length == 0)
				throw exception("Unknown operator '" + opstr + "'", expr);

			for (int i = 0; i < rsig.length; i++) {
				ReferenceSignature r = (ReferenceSignature) rsig[i];

				MBUnaryOperator mbop = new MBUnaryOperator(r.value);

				// is it a unary operator?
				if (!mbop.isValid())
					continue;

				// does it fit the given argument type?
				if (mbop.getArgType().equals(type)) {
					return mbop;
				}
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		return null;
	}

	/**
	 * Typechecks a SimpleUnaryExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get their signatures
		expr.getExpression().accept(this, attr);

		// add dereference (if neccessary)
		this.enforceDereference(expr.getExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		Signature sig = this.enforceSingleElement(expr.getExpression(), attr).getSignature();

		// check the operand
		ValueSignature vsig = requireValue(sig, new OID[0], "", expr.getExpression());

		MBUnaryOperator okmbop = findUnaryOperator(expr.O.spell(), vsig.value, expr);

		// unknown operator
		try {
			if (okmbop == null) {
				MBPrimitiveType type = new MBPrimitiveType(vsig.value);

				throw exception("Operator '" + expr.O.spell() + "' cannot be applied to values of types '"
						+ SBQLTypeCheckerHelper.getObjectName(type.getOID()) + "'", expr);
			}

			expr.operator = okmbop.getOID();

			// assign the signature to the visited expression
			expr.setSignature(new ValueSignature(okmbop.getResultType()));
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a CloseByExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	@Override
	public Object visitCloseByExpression(CloseByExpression expr, Object attr) throws SBQLException {
		typecheckTrasitiveClosureExpression(expr, attr);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeodra.sbql.ast.ASTAdapter#visitCloseUniqueByExpression(odra.sbql.ast.
	 * expressions.CloseUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitCloseUniqueByExpression(CloseUniqueByExpression expr, Object attr) throws SBQLException {
		typecheckTrasitiveClosureExpression(expr, attr);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitLeavesByExpression(odra.sbql.ast.expressions
	 * .LeavesByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesByExpression(LeavesByExpression expr, Object attr) throws SBQLException {
		typecheckTrasitiveClosureExpression(expr, attr);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitLeavesUniqueByExpression(odra.sbql.ast.
	 * expressions.LeavesUniqueByExpression, java.lang.Object)
	 */
	@Override
	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression expr, Object attr) throws SBQLException {
		typecheckTrasitiveClosureExpression(expr, attr);
		return null;
	}

	/**
	 * Typechecks a UnionExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		expr.getRightExpression().accept(this, attr);
		Signature sig2 = expr.getRightExpression().getSignature();

		// clone the first signature before checking comparablity
		// we don't want the cardinality to blur the comparability
		Signature csig1 = sig1.clone();
		csig1.setMinCard(sig2.getMinCard());
		csig1.setMaxCard(sig2.getMaxCard());
		Signature sig;

		// commented by janek
		// Variants are still not implemented. However this functionality is
		// required in distributed queries

		// if (!csig1.isComparableTo(sig2)) {
		//		    
		// // FIXME: variants!!!
		// throw exception(
		// "Unioning non-comparable signatures not implemented yet.",
		// expr);
		// }

		sig = sig1.clone();

		// build the signature of the result of the visited expression
		// FIXME: variants!!!

		sig.setMinCard(Signature.cardinalityAdd(sig1.getMinCard(), sig2.getMinCard()));
		sig.setMaxCard(Signature.cardinalityAdd(sig1.getMaxCard(), sig2.getMaxCard()));

		// assign the signature to the visited expression
		expr.setSignature(sig);

		// decorate with links
		if (expr.getSignature() instanceof ReferenceSignature) {
			// decorate AST with links
			expr.links.clear();
			expr.links.addAll(((ReferenceSignature) sig1).links);
			expr.links.addAll(((ReferenceSignature) sig2).links);

			// decorate siganture with links
			((ReferenceSignature) expr.getSignature()).links.clear();
			((ReferenceSignature) expr.getSignature()).links.addAll(expr.links);
		}

		return null;
	}

	/**
	 * Typechecks a IntersectExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitIntersectExpression(IntersectExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		expr.getRightExpression().accept(this, attr);
		Signature sig2 = expr.getRightExpression().getSignature();

		// clone the first signature before checking comparablity
		// we don't want the cardinality to blur the comparability
		Signature csig1 = sig1.clone();
		csig1.setMinCard(sig2.getMinCard());
		csig1.setMaxCard(sig2.getMaxCard());
		if (!csig1.isComparableTo(sig2))
			throw exception("Cannot intersect expression of different signatures: " + sig1.dump("") + "\n" + sig2.dump(""), expr);

		// build the signature of the result of the visited expression
		Signature sig = sig1.clone();
		// the intersection may be empty
		sig.setMinCard(0);
		sig.setMaxCard(sig1.getMaxCard() < sig2.getMaxCard() ? sig1.getMaxCard() : sig2.getMaxCard());

		// assign the signature to the visited expression
		expr.setSignature(sig);

		return null;
	}

	/**
	 * Typechecks a MinusExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitMinusExpression(MinusExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		expr.getRightExpression().accept(this, attr);
		Signature sig2 = expr.getRightExpression().getSignature();

		// clone the first signature before checking comparablity
		// we don't want the cardinality to blur the comparability
		Signature csig1 = sig1.clone();
		csig1.setMinCard(sig2.getMinCard());
		csig1.setMaxCard(sig2.getMaxCard());
		if (!csig1.isComparableTo(sig2)) {
			throw exception("Cannot difference expression of different signatures: " + sig1.dump("") + "\n" + sig2.dump(""), expr);
		}

		// build the signature of the result of the visited expression
		Signature sig = sig1.clone();
		// all items may be subtracted
		sig.setMinCard(0);
		// all item may remain
		sig.setMaxCard(sig1.getMaxCard());

		// assign the signature to the visited expression
		expr.setSignature(sig);

		// decorate expression & signature with links
		decorateBinaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks an AssignExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		expr.getLeftExpression().accept(this, attr);
		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getLeftExpression(), attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		expr.getRightExpression().accept(this, attr);
		// add coercion to [1..1] cardinality (run-time check) if neccessary
		this.enforceSingleElement(expr.getRightExpression(), attr);
		Signature sig2 = expr.getRightExpression().getSignature();

		ReferenceSignature rsig1 = requireReference(sig1, expr.getLeftExpression());
		requireMutablility(rsig1, expr.getLeftExpression());
		// infer types of the operands
		// check if the reference is virtual
		try {
			if (rsig1.isVirtual()) {
				MBView viewdef = new MBView(rsig1.value);

				assert viewdef.isValid() : "virtual reference does not points on a view definition";
				OID derefProcId = requireViewOperator(viewdef, OdraViewSchema.GenericNames.ON_UPDATE_NAME, "update", expr.getLeftExpression());
				MBProcedure mbproc = new MBProcedure(derefProcId);

				this.checkProcedureArguments(mbproc, expr.getRightExpression(), attr);

			} else {
				MBVariable mv = requireVariable(rsig1.value, expr.getLeftExpression());
				// variable can have different cardinality so we must set
				// signature manually to [1..1]
		//		SBQLTypeCheckerHelper.checkEnumVariable(mv,sig2);
				Signature varsig = SBQLTypeCheckerHelper.inferSignature(mv.getType(), 1, 1);
				this.checkSigTypeCompatibility(varsig, sig2, expr.getRightExpression(), attr, true);
				//here if(varsig.getEnumerator()!=null)
					//sig1.setEnumerator(varsig.getEnumerator());
					
				try {
					if (mv.isTypeReference()) {
						requireReference(sig2, expr.getRightExpression());
						// this.checkReferenceCompatibility(mv.getType(),
						// (ReferenceSignature) sig2, expr, attr);
						if (mv.hasReverseReference()) {
							MBVariable reversevar = new MBVariable(mv.getReversePointer());
							if (reversevar.getMinCard() == 1 && reversevar.getMaxCard() == 1) {
								throw exception("cannot update pointer object '" + mv.getName()
										+ "'. Reverse pointer cannot be deleted (cardinality [1..1])", expr);
							}
						}

					} else {
						// this.enforceDereference(expr.getRightExpression(),
						// attr);
						// this.checkTypeCompatibility(mv.getType(), expr
						// .getRightExpression().getSignature(), expr
						// .getRightExpression(), attr);
					}

					// sig1 = this.performDeref(rsig1.value);
				} catch (TypeCheckerException e) {
					throw exception(" incompatible types: " + e.getMessage(), expr);
				}
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		// // assign the signature to the visited expression
		expr.setSignature(expr.getLeftExpression().getSignature().clone());

		return null;
	}

	/**
	 * @param viewdef
	 * @param on_update_name
	 * @return
	 */
	private OID requireViewOperator(MBView viewdef, GenericNames name, String operatorName, ASTNode node) {
		OID derefProcId;
		try {
			derefProcId = viewdef.getGenericProc(name.toString());
		} catch (DatabaseException e) {
			throw exception(e, node);
		}
		if (derefProcId == null)
			throw exception(operatorName + " is forbidden", node);
		return derefProcId;
	}

	/**
	 * Typechecks a DerefExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to performe recurrent check
		if (attr == null)
			expr.getExpression().accept(this, attr);
		Signature sig = expr.getExpression().getSignature();

		// check if it is a reference
		if (!(sig instanceof ReferenceSignature)) {
			// throw exception("Reference expected", expr);
			// 21.05.07 we do not throw an throw exception any more we simply
			// remove
			// unnecessary deref

			// 2007-07-29, JW: non-reference signatures are also structures,
			// previously dereference was just lost
			if ((sig instanceof StructSignature) || (sig instanceof BinderSignature)) {
				insertTraversingDereference(expr.getExpression());
				expr.getExpression().accept(this, attr);
			}

			replaceSubExpression(expr.getParentExpression(), expr, expr.getExpression());
			return null;
		}

		Signature derefSig;

		ReferenceSignature rsig = (ReferenceSignature) sig;
		if (rsig.hasRefFlag()) { // we have a ref flag - no dereference (should
									// we
			// take the flag off??)
			expr.setSignature(rsig.clone());
			return null;
		}
		// check if the reference is virtual
		try {
			if (rsig.isVirtual()) {
				MBView viewdef = new MBView(rsig.value);

				assert viewdef.isValid() : "virtual reference not points on a view definition";
				OID derefProcId = requireViewOperator(viewdef, OdraViewSchema.GenericNames.ON_RETRIEVE_NAME, "dereference", expr.getExpression());
				MBProcedure mbproc = new MBProcedure(derefProcId);

				derefSig = SBQLTypeCheckerHelper.inferSignature(mbproc.getType(), mbproc.getMinCard(), mbproc.getMaxCard());

			} else {
				derefSig = SBQLTypeCheckerHelper.performDeref(rsig.value);
				//here if(derefSig.getEnumerator()!=null)
					//expr.getExpression().getSignature().setEnumerator(derefSig.getEnumerator());
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		// copy cardinalities
		derefSig.setMinCard(sig.getMinCard());
		derefSig.setMaxCard(sig.getMaxCard());

		// assign the signature to the visited expression
		expr.setSignature(derefSig);

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a RefExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		expr.getExpression().accept(this, attr);
		requireReference(expr.getExpression().getSignature(), expr.getExpression());

		expr.setSignature(expr.getExpression().getSignature().clone());
		((ReferenceSignature) expr.getSignature()).setRefFlag(true);
		expr.getSignature().setAssociatedExpression(expr.getExpression());

		decorateUnaryExpressionWithLinks(expr);
		return null;
	}

	/**
	 * Typechecks a ToBooleanExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature

		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}

		// add dereference, if necessary
		this.enforceDereference(expr.getExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		Signature sig = this.enforceSingleElement(expr.getExpression(), attr).getSignature();
		requireValue(sig, new OID[] { env.booleanType, env.stringType }, "", expr.getExpression());

		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.booleanType));

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a ToIntegerExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}

		// add dereference, if necessary
		this.enforceDereference(expr.getExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if necessary
		Signature sig = this.enforceSingleElement(expr.getExpression(), attr).getSignature();
		requireValue(sig, new OID[] { env.stringType, env.realType, env.integerType }, "", expr.getExpression());

		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.integerType));

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a ToRealExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}

		// add dereference, if necessary
		this.enforceDereference(expr.getExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if necessary
		Signature sig = this.enforceSingleElement(expr.getExpression(), attr).getSignature();
		requireValue(sig, new OID[] { env.stringType, env.realType, env.integerType }, "", expr.getExpression());
		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.realType));

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a ToStringExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to performe recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}

		// add dereference, if neccessary
		this.enforceDereference(expr.getExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if neccessary
		Signature sig = this.enforceSingleElement(expr.getExpression(), attr).getSignature();
		requireValue(sig, new OID[0], "", expr.getExpression());

		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.stringType));

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a {@link ToDateExpression}.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null)
			expr.getExpression().accept(this, attr);

		// add dereference, if necessary
		this.enforceDereference(expr.getExpression(), attr);

		// add coercion to [1..1] cardinality (run-time check) if necessary
		// Signature sig = enforceSingleElement(expr.E, attr).sign;
		Signature sig = expr.getExpression().getSignature();
		requireValue(sig, new OID[] { env.stringType, env.dateType }, "", expr.getExpression());

		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(env.dateType));

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Insert the coercion to a primitive type.
	 * 
	 * @param ceorctype
	 *            is the type to be coerced to.
	 * @param expr
	 *            is the expression to be augmented with this coercion.
	 * @return the modified expression radamus 14.12.06 changed return type form
	 *         Signature to Expression
	 */
	private final Expression insertPrimitiveTypeCoercion(OID coerctype, Expression expr, Object attr) throws SBQLException {
		if (coerctype == null)
			return null;

		Expression dexpr = null;
		if (expr.getParentExpression() == null) {

			if (coerctype.equals(env.integerType))
				dexpr = new ToIntegerExpression(expr);
			else if (coerctype.equals(env.stringType))
				dexpr = new ToStringExpression(expr);
			else if (coerctype.equals(env.realType))
				dexpr = new ToRealExpression(expr);
			else if (coerctype.equals(env.booleanType))
				dexpr = new ToBooleanExpression(expr);
			else if (coerctype.equals(env.dateType))
				dexpr = new ToDateExpression(expr);
			else
				throw exception("No primitive coercion to " + SBQLTypeCheckerHelper.getObjectName(coerctype), expr);
			if (!(root instanceof Statement))
				root = dexpr;
		} else {
			if (coerctype.equals(env.integerType))
				expr.getParentExpression().replaceSubexpr(expr, dexpr = new ToIntegerExpression(expr));
			else if (coerctype.equals(env.stringType))
				expr.getParentExpression().replaceSubexpr(expr, dexpr = new ToStringExpression(expr));
			else if (coerctype.equals(env.realType))
				expr.getParentExpression().replaceSubexpr(expr, dexpr = new ToRealExpression(expr));
			else if (coerctype.equals(env.booleanType))
				expr.getParentExpression().replaceSubexpr(expr, dexpr = new ToBooleanExpression(expr));
			else if (coerctype.equals(env.dateType))
				expr.getParentExpression().replaceSubexpr(expr, dexpr = new ToDateExpression(expr));
			else
				throw exception("No primitive coercion to " + SBQLTypeCheckerHelper.getObjectName(coerctype), expr);
		}

		// visit the newly created expression
		expr.getParentExpression().accept(this, new Object());
		// and return the expression
		return dexpr;
	}

	/**
	 * Typechecks a ProcedureCallExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		// visit the argument expressions and get their signatures
		// it is possible that we do not need to perform recurrent check
		if (attr == null)
			expr.getProcedureSelectorExpression().accept(this, attr);
		this.enforceSingleElement(expr.getProcedureSelectorExpression(), attr);

		expr.getArgumentsExpression().accept(this, attr);

		// if the left operand is not a reference (of procedure <- it is checked
		// later)
		requireReference(expr.getProcedureSelectorExpression().getSignature(), expr.getProcedureSelectorExpression());
		this.enforceReferenceLevel(0, expr.getProcedureSelectorExpression(), attr);
		ReferenceSignature rsig1 = (ReferenceSignature) expr.getProcedureSelectorExpression().getSignature();
		Signature retSign;
		try {
			MBProcedure proc = new MBProcedure(rsig1.value);

			// if the left operand is not a reference to a procedure
			if (!proc.isValid()) {
				MBIndex idx = new MBIndex(rsig1.value);
				if (!idx.isValid())
					throw exception("Procedure reference expected", expr);
				expr.setSignature(new ReferenceSignature(idx.getIdxVar()));
				((ReferenceSignature) expr.getSignature()).setVirtual(new MBView(idx.getIdxVar()).isValid());
				expr.getSignature().setMinCard(0);
				expr.getSignature().setMaxCard(Integer.MAX_VALUE);
				expr.getSignature().setAssociatedExpression(expr.getProcedureSelectorExpression());
				return null;
			}

			// typecheck arguments
			this.checkProcedureArguments(proc, expr.getArgumentsExpression(), attr);

			// assign the signature to the visited expression

			retSign = SBQLTypeCheckerHelper.inferSignature(proc.getType(), proc.getMinCard(), proc.getMaxCard());
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		expr.setSignature(retSign);

		expr.getSignature().setAssociatedExpression(expr.getProcedureSelectorExpression());
		return null;
	}

	@Override
	public Object visitParallelUnionExpression(ParallelUnionExpression node, Object attr) throws SBQLException {
		node.links.clear();

		for (Expression e : node.getParallelExpressions()) {
			e.accept(this, attr);
			node.links.addAll(e.links);
		}

		// TODO : ParallelUnion should work on variants
		// This functionality is required for distributed queries testing
		Signature sig = node.getParallelExpressions().get(0).getSignature().clone();
		sig.setMinCard(1);
		sig.setMaxCard(2);

		node.setSignature(sig);
		node.getSignature().links.clear();
		node.getSignature().links.addAll(node.links);

		return null;
	}

	// TW
	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr, Object attr) throws SBQLException {

		if (!(expr.getLeftExpression() instanceof ExternalNameExpression))
			throw exception("external name required in the context of external procedure call", expr);
		ExternalNameExpression enx = (ExternalNameExpression) expr.getLeftExpression();

		if (enx.name().value().equals("invoke_integer"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("invoke_string"))
			expr.setSignature(new ValueSignature(env.stringType));
		else if (enx.name().value().equals("load_class"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("new_object"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("add_parameters"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("init_parameters"))
			expr.setSignature(new ValueSignature(env.integerType));
		// Pietia
		else if (enx.name().value().equals("invoke_webservice")) {

			IntegerExpression no = (IntegerExpression) expr.getRightExpression();
			System.out.println("no:" + no);
			Signature s = odra.sbql.external.ExternalRoutines.getSignature(no.getLiteral().value());
			expr.setSignature(s);
		} else if (enx.name().value().equals("config_webservice"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("addfield_webservice"))
			expr.setSignature(new StructSignature());
		else if (enx.name().value().equals("addattr_webservice"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("addtxt_webservice"))
			expr.setSignature(new StructSignature());
		else if (enx.name().value().equals("new_webservice"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("delete_webservice"))
			expr.setSignature(new ValueSignature(env.integerType));
		else if (enx.name().value().equals("reset_webservice"))
			expr.setSignature(new ValueSignature(env.integerType));

		return null;
	}

	/**
	 * Typechecks a SequentialExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitSequentialExpression(SequentialExpression expr, Object attr) throws SBQLException {
		// visit the argument expression
		expr.getFirstExpression().accept(this, attr);
		expr.getSecondExpression().accept(this, attr);

		// assign the signature to the visited expression
		// the result signature is a signature of E2
		expr.setSignature(expr.getSecondExpression().getSignature().clone());
		
		return null;
	}

	/**
	 * Typechecks an EmptyExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
		// assign the signature to the visited expression
		expr.setSignature(new ValueSignature(StdEnvironment.getStdEnvironment().voidType));
		expr.getSignature().setMinCard(0);
		expr.getSignature().setMaxCard(0);

		return null;
	}

	/**
	 * Typechecks a ToSingleExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToSingleExpression(ToSingleExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}
		Signature sig = expr.getExpression().getSignature();

		// build the signature of the result of the visited expression
		// by cloning the signature sig and setting its cardinality to [1..1]
		Signature csig = sig.clone();
		csig.setMinCard(1);
		csig.setMaxCard(1);

		// assign the signature to the visited expression
		expr.setSignature(csig);
		csig.setAssociatedExpression(sig.getOwnerExpression());
		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a ToBagExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitToBagExpression(ToBagExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}
		Signature sig = expr.getExpression().getSignature();

		// build the signature of the result of the visited expression
		// by cloning the signature sig and setting it collection kind to
		// BAG_COLLECTION
		Signature csig = sig.clone();
		csig.setCollectionKind(new CollectionKind(CollectionKind.BAG_COLLECTION));

		// assign the signature to the visited expression
		expr.setSignature(csig);

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typechecks a RangeExpression.
	 * 
	 * @param expr
	 *            is the expression to be visited.
	 * @param attr
	 *            points additional arguments of the visit.
	 */
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		Signature sig1 = expr.getLeftExpression().getSignature();

		// add dereference, if neccessary
		this.enforceDereference(expr.getRightExpression(), attr);

		Signature sig2 = expr.getRightExpression().getSignature();
		// check if the second arument is of integer type
		requireValue(sig2, new OID[] { env.integerType }, "", expr.getRightExpression());

		// build the signature of the result of the visited expression
		// by cloning the signature sig and setting it collection kind to
		// BAG_COLLECTION
		Signature csig = sig1.clone();
		// the result can be empty if the index value is out of range
		csig.setMinCard(0);
		// if (sig2.getMinCard() < sig1.getMinCard())
		// csig.setMinCard(sig2.getMinCard());
		// adjust the cardinalities; inherit them from the indices (mostly)
		csig.setMaxCard(sig2.getMaxCard());

		// assign the signature to the visited expression
		expr.setSignature(csig);

		expr.getSignature().setAssociatedExpression(sig1.getOwnerExpression());

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitBagExpression(odra.sbql.ast.expressions
	 * .BagExpression, java.lang.Object)
	 */
	@Override
	public Object visitBagExpression(BagExpression expr, Object attr) throws SBQLException {
		expr.setExpression(convertCommasToUnions(expr.getExpression()));
		expr.getExpression().accept(this, attr);
		expr.setSignature(expr.getExpression().getSignature().clone());
		return null;
	}

	/**
	 * Converts CommaExpressions to UnionExpressions in the arguments of
	 * BagExpressions
	 * 
	 * @param expr
	 *            is the the root of the expression to be converted
	 * @param commaLevel
	 *            - the nesting level of CommaExpression in the recurent call
	 * @return is the converted expression
	 */
	private Expression convertCommasToUnions(Expression expr) {
		if (expr instanceof CommaExpression) {
			CommaExpression cExpr = (CommaExpression) expr;

			UnionExpression result = new UnionExpression(convertCommasToUnions(cExpr.getLeftExpression()), cExpr.getRightExpression());
			// convertCommasToUnions(cExpr.getRightExpression()));
			result.line = cExpr.line;
			result.column = cExpr.column;
			result.links = cExpr.links;
			result.setMarked(cExpr.isMarked());
			// result.dblink = cExpr.dblink;
			result.isViewSubstituted = cExpr.isViewSubstituted;
			replaceSubExpression(cExpr.getParentExpression(), cExpr, result);

			return result;
		}
		return expr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitStructExpression(odra.sbql.ast.expressions
	 * .StructExpression, java.lang.Object)
	 */
	@Override
	public Object visitStructExpression(StructExpression expr, Object attr) throws SBQLException {
		expr.getExpression().accept(this, attr);
		// get the signature of the operand
		Signature sig = expr.getExpression().getSignature();
		if (sig instanceof StructSignature) {
			expr.setSignature(sig.clone());

		} else {
			if (sig.getMaxCard() == Integer.MAX_VALUE) {
				throw exception("unable to convert to struct the result with maximal cardinality = *", expr);
			}
			if (sig.getMinCard() == 0) {
				throw exception("unable to convert to struct the result with minimal cardinality = 0", expr);
			}
			StructSignature ssig = new StructSignature();
			for (int i = 0; i < sig.getMinCard(); i++) {
				Signature fsig = sig.clone();
				fsig.setMinCard(1);
				fsig.setMaxCard(1);
				ssig.addField(fsig);
			}

			if (sig.getMinCard() != sig.getMaxCard()) {

				for (int j = sig.getMinCard(); j < sig.getMaxCard(); j++) {
					Signature fsig = sig.clone();
					fsig.setMinCard(0);
					fsig.setMaxCard(1);
					ssig.addField(fsig);
				}
			}
			ssig.setCardinality(sig.getMinCard(), sig.getMaxCard());
			expr.setSignature(ssig);
		}
		expr.getSignature().setAssociatedExpression(sig.getOwnerExpression());
		return null;
	}

	// //////////////////////helper
	// operations////////////////////////////////////////

	/**
	 * Inserts a dereference if the expression is assigned a ReferenceSignature
	 * 
	 * @param expr
	 *            is the expression which is to be dereferenced
	 * @return the possibly modified expression
	 */
	protected Expression enforceDereference(Expression expr, Object attr) throws SBQLException {
		 
		Expression deref = null;
		
		Signature sig = expr.getSignature();
		DereferenceEnforcer enforcer = new DereferenceEnforcer(this);
		deref = enforcer.enforce(expr,new Object());
		if(expr.equals(root) && enforcer.enforcePerformed())
		{
			root = deref;
		}
		
		return deref;
	}

	protected boolean enforceVirtual(ReferenceSignature sign) throws DatabaseException {

		if (!sign.isVirtual()) {

			if (SBQLTypeCheckerHelper.isVirtualObject(sign.value)) {
				// redirect to view definition
				(sign).value = new MBVirtualVariable(sign.value).getView();
				// set the reference signature as virtual
				(sign).setVirtual(true);
				return true;
			}

		}
		return false;

	}

	private void enforceProcedureCall(NameExpression expr) throws DatabaseException {
		Signature sig = expr.getSignature();
		Expression parent = expr.getParentExpression();
		MBProcedure proc = new MBProcedure(((ReferenceSignature) sig).value);
		if (!proc.isValid())
			return;
		// if this is a procedure it cannot be a collection
		sig.setCardinality(1, 1);
		if (parent != null) {

			if (parent instanceof RefExpression) {
				return;
			}
			if (parent instanceof ProcedureCallExpression && ((ProcedureCallExpression) parent).getProcedureSelectorExpression().equals(expr)) {
				return; // this is already procedure selector expression so do
						// nothing
			}
		}
		if (parent != null && (parent instanceof ProcedureCallExpression || (parent instanceof RefExpression))) {
			if (((ProcedureCallExpression) parent).getProcedureSelectorExpression().equals(expr))
				return; // this is procedure selector expression so do nothing
		}
		if (sig instanceof ReferenceSignature && !((ReferenceSignature) sig).hasRefFlag()) {

			if (proc.isValid()) {

				// add the ProcedureCallExpression
				ProcedureCallExpression pexpr;
				if (parent != null) {
					parent.replaceSubexpr(expr, pexpr = new ProcedureCallExpression(expr, new EmptyExpression()));
				} else {
					pexpr = new ProcedureCallExpression(expr, new EmptyExpression());
					if (!(root instanceof Statement))
						root = pexpr;
				}
				pexpr.accept(this, new Object());
				pexpr.setEnforced(true);

			}
		}
	}

	/**
	 * Inserts a coercion to single element if the expression has the
	 * cardinality other than [1..1]
	 * 
	 * @param expr
	 *            is the expression which is to be coerced to [1..1]
	 * @return the modified expression radamus 14.12.06 changed return type form
	 *         Signature to Expression
	 */
	protected Expression enforceSingleElement(Expression expr, Object attr) throws SBQLException {
		if ((expr instanceof GroupAsExpression)) {
			this.enforceSingleElement(((GroupAsExpression) expr).getExpression(), attr);
			expr.accept(this, attr);
			return expr;
		}
		if (expr.getSignature().getMinCard() > 1 || expr.getSignature().getMaxCard() < 1)
			throw exception("Operand should allow cardinality 1. found " + SBQLTypeCheckerHelper.printCardinality(expr.getSignature().getMinCard(), expr.getSignature().getMaxCard()), expr);
		SingleElementEnforcer enforcer = new SingleElementEnforcer(this);
		Expression toSingleExp = enforcer.enforce(expr, new Object());
		if(expr.equals(root) && enforcer.enforcePerformed())
		{
			root = toSingleExp;
		}
		return toSingleExp;
	}

	protected Expression enforceMinimalCardinality(int minCard, Expression expr, Object attr) throws SBQLException {
		if ((expr instanceof GroupAsExpression)) {
			this.enforceMinimalCardinality(minCard, ((GroupAsExpression) expr).getExpression(), attr);
			return expr;
		}
		if (expr.getSignature().getMaxCard() < minCard)
			throw exception("Operand should allow cardinality " + minCard, expr);

		MinimalCardinalityEnforcer enforcer = new MinimalCardinalityEnforcer(this, minCard);
		Expression atLeastExp = enforcer.enforce(expr, attr);
		if(expr.equals(root) && enforcer.enforcePerformed())
		{
			root = atLeastExp;
		}
		return atLeastExp;
	}

	protected Expression enforceMaximalCardinality(int maxCard, Expression expr, Object attr) throws SBQLException {
		if ((expr instanceof GroupAsExpression)) {
			this.enforceMaximalCardinality(maxCard, ((GroupAsExpression) expr).getExpression(), attr);	
			return expr;
		}
		if (expr.getSignature().getMinCard() > maxCard)
			throw exception("Operand should allow cardinality " + maxCard, expr);
		
		MaximalCardinalityEnforcer enforcer = new MaximalCardinalityEnforcer(this, maxCard);		
		Expression atMostExp = enforcer.enforce(expr, attr);
		if(expr.equals(root) && enforcer.enforcePerformed())
		{
			root = atMostExp;
		}
		return atMostExp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitCreateExpression(odra.sbql.ast.expressions
	 * .CreateExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
		// visit parameter expression
		evalCreateParamExpression(expr, attr);
		Signature sig = expr.getExpression().getSignature();

		ReferenceSignature rsig = typecheckCreate(expr, this.requireVariableDeclaration(expr), attr);

		rsig.setMinCard(sig.getMinCard());
		rsig.setMaxCard(sig.getMaxCard());
		expr.setSignature(rsig);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitCreatePermanentExpression(odra.sbql.ast
	 * .expressions.CreatePermanentExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
		// visit parameter expression
		evalCreateParamExpression(expr, attr);
		Signature sig = expr.getExpression().getSignature();

		ReferenceSignature declaredSig = this.requireVariableDeclaration(expr);
		// TODO persistent environment indicator
		// throw exception("Unable to create object. The name '"
		// + expr.name().value()
		// + "' is not a name of the declared persistent variable.",
		// expr);
		//	
		ReferenceSignature rsig = typecheckCreate(expr, declaredSig, attr);

		rsig.setMinCard(sig.getMinCard());
		rsig.setMaxCard(sig.getMaxCard());
		expr.setSignature(rsig);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.
	 * expressions.CreateTemporalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
		evalCreateParamExpression(expr, attr);
		Signature sig = expr.getExpression().getSignature();
		// search for variable declaration

		ReferenceSignature rsig = typecheckCreate(expr, this.requireVariableDeclaration(expr), attr);

		rsig.setMinCard(sig.getMinCard());
		rsig.setMaxCard(sig.getMaxCard());
		expr.setSignature(rsig);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions
	 * .CreateLocalExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
		throw exception("procedure/method context required", expr);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions
	 * .InsertCopyExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		this.enforceSingleElement(expr.getLeftExpression(), attr);
		ReferenceSignature lrefsig = requireReference(expr.getLeftExpression().getSignature(), expr.getLeftExpression());

		expr.getRightExpression().accept(this, attr);
		this.insertTraversingDereference(expr.getRightExpression());
		expr.getRightExpression().accept(this, attr);

		if (lrefsig.reflevel > 0) {
			lrefsig = (ReferenceSignature) this.enforceDereference(expr.getLeftExpression(), attr).getSignature();
		}
		String name = expr.name().value();
		try {
			if (lrefsig.isVirtual()) {
				MBView view = new MBView(lrefsig.value);
				// find sub view that declares object with a name of inserted
				// object
				OID subvid = SBQLTypeCheckerHelper.findSubViewByVirtualObjectName(view, SBQLTypeCheckerHelper.name2id(name));
				if (subvid != null) {
					// search for on new procedure
					MBView sview = new MBView(subvid);
					OID onnew = requireViewOperator(sview, OdraViewSchema.GenericNames.ON_NEW_NAME, "insert ", expr.getLeftExpression());
					// set variable to on_new argument
					MBProcedure onnewproc = new MBProcedure(onnew);
					this.checkProcedureArguments(onnewproc, expr.getRightExpression(), attr);

					expr.setSignature(new ReferenceSignature(subvid));
					((ReferenceSignature) expr.getSignature()).setVirtual(true);
				} else
					throw exception("Variable " + view.getVirtualObject().getObjectName() + " has no declared subobject named: " + name, expr);
			} else {
				MBVariable foundvar = requireSubVariable(name, lrefsig.value, this.module, expr);

				try {
					this.requireChangeableCollection(foundvar.getMinCard(), foundvar.getMaxCard(), expr.getLeftExpression());
					Signature foundVarSig = SBQLTypeCheckerHelper.inferSignature(foundvar.getType(), foundvar.getMinCard(), foundvar.getMaxCard());

					this.checkSigTypeCompatibility(foundVarSig, expr.getRightExpression().getSignature(), expr.getRightExpression(), attr, true);
					// if (foundvar.isTypeReference()) {
					//requireReference(expr.getRightExpression().getSignature(),
					// expr.getRightExpression());
					// this.checkReferenceCompatibility(foundvar.getType(),
					// (ReferenceSignature) expr.getRightExpression()
					// .getSignature(), expr, attr);
					// } else {
					// this.checkTypeCompatibility(foundvar.getType(), expr
					// .getRightExpression().getSignature(), expr
					// .getRightExpression(), attr);
					// }
					// // check the cardinality ??
					// if (foundvar.getMinCard() == 1
					// && foundvar.getMaxCard() == 1) {
					// throw exception(foundvar.getName()
					// + " has the cardinality [1..1]", expr);
					// }
					// this.enforceMaximalCardinality(foundvar.getMaxCard(),
					// expr.getRightExpression(), attr);
					expr.setSignature(new ReferenceSignature(foundvar.getOID()));
					// empty bag
					expr.getSignature().setMinCard(0);
					expr.getSignature().setMaxCard(0);

				} catch (TypeCheckerException e) {
					throw exception("Insert copy operator: type incompatibility: " + e.getMessage(), expr);
				}
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitInsertExpression(odra.sbql.ast.expressions
	 * .InsertExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {

		try {
			typecheckInsert(expr, attr);
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitInstanceOfExpression(odra.sbql.ast.expressions
	 * .InstanceOfExpression, java.lang.Object)
	 */
	@Override
	public Object visitInstanceOfExpression(InstanceOfExpression expr, Object attr) throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		this.enforceSingleElement(expr.getLeftExpression(), attr);
		Signature lsig = expr.getLeftExpression().getSignature();
		Signature rsig = expr.getRightExpression().getSignature();
		try {
			ReferenceSignature lrsig = requireReference(lsig, expr.getLeftExpression());
			MBVariable lvar = requireVariable(lrsig.value, expr.getLeftExpression());
			ReferenceSignature rrsig = requireReference(rsig, expr.getRightExpression());
			MBClass rcls = requireClass(rrsig.value, expr.getRightExpression());

			if (!lvar.isTypeClass())
				throw exception("variable " + lvar.getName() + " is not a class instance", expr);
			MBClass lcls = new MBClass(lvar.getType());
			if (lcls.getOID().equals(rcls.getOID()) || lcls.isSubClassOf(rcls.getOID()) || rcls.isSubClassOf(lcls.getOID())
					|| rcls.haveCommonSuperClass(lcls.getOID()))
				expr.setSignature(new ValueSignature(env.booleanType));
			else
				throw exception("incompatible instanceof operands: " + lvar.getName() + " cannot be instance of " + rcls.getName(), expr);
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		return null;
	}

	/**
	 * @param value
	 * @param rightExpression
	 */
	protected MBClass requireClass(OID value, ASTNode node) {

		try {
			MBClass clazz = new MBClass(value);
			if (!clazz.isValid())
				throw exception("class reference expected", node);
			return clazz;
		} catch (DatabaseException e) {
			throw exception(e, node);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitCastExpression(odra.sbql.ast.expressions
	 * .CastExpression, java.lang.Object)
	 */
	@Override
	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		Signature sig1 = expr.getLeftExpression().getSignature();
		ReferenceSignature lrsig1 = requireReference(sig1, expr.getLeftExpression());

		if (SBQLTypeCheckerHelper.isSimpleType(lrsig1.value)) {
			convertCast2CoerceOperator(expr, lrsig1, attr);

		} else if (isTypeDef(lrsig1.value, expr)) {
			this.enforceDereference(expr.getRightExpression(), attr);
			typecheckTypeDefCoerce(expr, attr);
		} else if (isEnum(lrsig1.value, expr)){
			this.enforceDereference(expr.getRightExpression(), attr);
			typecheckEnumCoerce(expr, attr);
		}
		else {

			// class cast
			typecheckClassCast(expr);
			// decorate expression & signature with links
			decorateBinaryExpressionWithLinks(expr);
		}

		return null;
	}

	/**
	 * @param expr
	 */
	private void typecheckClassCast(CastExpression expr) {
		boolean isDownCast = false;
		try {
			ReferenceSignature rrsig = requireReference(expr.getRightExpression().getSignature(), expr.getRightExpression());
			MBVariable rvar = requireVariable(rrsig.value, expr.getRightExpression());

			ReferenceSignature lrsig = requireReference(expr.getLeftExpression().getSignature(), expr.getLeftExpression());
			MBClass castTocls = requireClass(lrsig.value, expr.getLeftExpression());

			if (!rvar.isTypeClass()) {
				throw exception("cast expression requires reference to class instance", expr);
			}

			MBClass varcls = new MBClass(rvar.getType());

			// if it is not a downcast
			if (!castTocls.isSubClassOf(varcls.getOID())) {
				// and if it is not an upcast
				if (!varcls.isSubClassOf(castTocls.getOID())) {
					// it could be a cross cast
					if (!varcls.haveCommonSuperClass(castTocls.getOID())) {
						throw exception("unable to cast '" + rvar.getName() + "' to '" + castTocls.getName() + "'", expr);
					}
				}
			} else
				isDownCast = true;
			expr.setSignature(new ReferenceSignature(castTocls.getSelfVariable()));
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		if (isDownCast) {
			// it is safe and always possible
			expr.getSignature().setMinCard(expr.getRightExpression().getSignature().getMinCard());
		} else {
			// it is possible that none object will be casted
			expr.getSignature().setMinCard(0);
		}
		expr.getSignature().setMaxCard(expr.getRightExpression().getSignature().getMaxCard());

	}

	/**
	 * @param expr
	 * @param rsig1
	 * @param attr
	 */
	private void typecheckTypeDefCoerce(CastExpression expr, Object attr) {
		assert expr.getLeftExpression().getSignature() != null && expr.getRightExpression().getSignature() != null : "expr.getLeftExpression().getSignature() != null && expr.getRightExpression().getSignature() != null";
		assert expr.getLeftExpression().getSignature() instanceof ReferenceSignature : "expr.getLeftExpression().getSignature() instanceof ReferenceSignature";

		try {
			// pass through typedef
			Signature lsig = SBQLTypeCheckerHelper.inferSignature(((ReferenceSignature) expr.getLeftExpression().getSignature()).value);
			Signature rsig = expr.getRightExpression().getSignature();
			if (rsig.isStructuralTypeCompatible(lsig)) {
				lsig.setCardinality(rsig.getMinCard(), rsig.getMaxCard());
				expr.getRightExpression().setSignature(lsig);
				replaceSubExpression(expr.getParentExpression(), expr, expr.getRightExpression());
			} else
				throw exception("unable to coerce " + rsig.dump("") + " to " + lsig.dump(""), expr);

		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

	}
	
	/**
	 * @param expr
	 * @param attr
	 */
	private void typecheckEnumCoerce(CastExpression expr, Object attr) {
		assert expr.getLeftExpression().getSignature() != null && expr.getRightExpression().getSignature() != null : "expr.getLeftExpression().getSignature() != null && expr.getRightExpression().getSignature() != null";
		assert expr.getLeftExpression().getSignature() instanceof ReferenceSignature : "expr.getLeftExpression().getSignature() instanceof ReferenceSignature";

		try {
			ReferenceSignature refSig = (ReferenceSignature) expr.getLeftExpression().getSignature();
			MBEnum mbenu = new MBEnum(refSig.value);
			Signature lsig = SBQLTypeCheckerHelper.inferSignature(refSig.value);
			Signature rsig = expr.getRightExpression().getSignature();
			if (rsig.isStructuralTypeCompatible(lsig)) {
				lsig.setCardinality(rsig.getMinCard(), rsig.getMaxCard());
				expr.getRightExpression().setSignature(rsig);
				Signature sig = lsig;
				sig.setEnumerator(mbenu.getName());
				expr.setSignature(sig);
				//ForSomeExpression fse = new ForSomeExpression(new NameExpression(new Name("$enum_values"+mbenu.getName()+"_values")),null);
				//replaceSubExpression(expr.getParentExpression(), expr, expr.getRightExpression());
			} else
				throw exception("unable to coerce " + rsig.dump("") + " to " + lsig.dump(""), expr);

		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

	}

	/**
	 * @param parent
	 * @param oldSub
	 * @param newSub
	 */
	protected void replaceSubExpression(Expression parent, Expression oldSub, Expression newSub) {
		if (parent != null)
			parent.replaceSubexpr(oldSub, newSub);
		else
			root = newSub;
		//newSub.setParentExpression(null);

	}

	/**
	 * @param value
	 * @return
	 * @throws DatabaseException
	 */
	private boolean isTypeDef(OID value, ASTNode node) {
		try {
			return new MBTypeDef(value).isValid();
		} catch (DatabaseException e) {
			throw exception(e, node);
		}
	}
	
	/**
	 * @param value
	 * @return
	 * @throws DatabaseException
	 */
	private boolean isEnum(OID value, ASTNode node) {
		try {
			return new MBEnum(value).isValid();
		} catch (DatabaseException e) {
			throw exception(e, node);
		}
	}

	/**
	 * @param expr
	 * @param attr
	 * @param attr
	 */
	private void convertCast2CoerceOperator(CastExpression expr, ReferenceSignature coerceType, Object attr) {
		if (coerceType.value.equals(this.env.voidType))
			throw exception("unable to coerce to void", expr);
		this.enforceDereference(expr.getRightExpression(), attr);
		requireValue(expr.getRightExpression().getSignature(), new OID[0], "", expr.getRightExpression());

		this.enforceSingleElement(expr.getRightExpression(), attr);
		this.insertPrimitiveTypeCoercion(coerceType.value, expr.getRightExpression(), attr);
		// we do not need cast expression
		replaceSubExpression(expr.getParentExpression(), expr, expr.getRightExpression());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions
	 * .DeleteExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeleteExpression(DeleteExpression expr, Object attr) throws SBQLException {
		expr.getExpression().accept(this, attr);
		Signature sig = expr.getExpression().getSignature();
		// we need reference signature
		ReferenceSignature refsig = requireReference(sig, expr.getExpression());

		// check if the reference is virtual
		try {
			if (refsig.isVirtual()) {
				MBView view = new MBView(refsig.value);
				OID ondelete = requireViewOperator(view, OdraViewSchema.GenericNames.ON_DELETE_NAME, "delete", expr.getExpression());
			} else {
				MBVariable deletedVariable = requireVariable(refsig.value, expr);

				if (deletedVariable.getMinCard() == 1 && deletedVariable.getMaxCard() == 1)
					throw exception("unable to delete object with cardinality [1..1]", expr);
				requireReversePointerDeletable(deletedVariable, expr);
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		expr.setSignature(expr.getExpression().getSignature().clone());
		// ??
		expr.getSignature().setMinCard(0);
		expr.getSignature().setMaxCard(0);
		expr.getSignature().setAssociatedExpression(expr.getExpression().getSignature().getOwnerExpression());
		return null;
	}

	/**
	 * @param deletedVariable
	 */
	private void requireReversePointerDeletable(MBVariable deletedVariable, ASTNode node) {
		try {
			if (deletedVariable.isTypeReference() && deletedVariable.hasReverseReference()) {
				MBVariable reversevar = new MBVariable(deletedVariable.getReversePointer());
				if (reversevar.getMinCard() == 1 && reversevar.getMaxCard() == 1) {
					throw exception("pointer object '" + deletedVariable.getName() + "' cannot be deleted. Reverse pointer '" + reversevar.getName()
							+ "' cardinality is [1..1]", node);
				}
			}

			// TODO if deleted object is complex check if its fields are reverse
			// pointers and check reverse cardinality

		} catch (DatabaseException e) {
			throw exception(e, node);
		}

	}

	protected ReferenceSignature typecheckCreate(CreateExpression expr, ReferenceSignature rsig, Object attr) throws SBQLException {
		OID declared = rsig.value;
		try {
			this.enforceVirtual(rsig);
			if (rsig.isVirtual()) {
				MBView view = new MBView(rsig.value);
				OID onnew = requireViewOperator(view, OdraViewSchema.GenericNames.ON_NEW_NAME, "create", expr);
				MBProcedure mbproc = new MBProcedure(onnew);
				declared = mbproc.getArguments()[0];
			}
			MBVariable mbvar = requireVariable(declared, expr);
			int minCard = mbvar.getMinCard();
			int maxCard = mbvar.getMaxCard();
			if (!rsig.isVirtual())
				this.requireChangeableCollection(minCard, maxCard, expr);
			OID modoid = mbvar.getModule().getOID();
			if (!modoid.equals(module.getOID())) {
				OID[] impmods = module.getCompiledImports();
				int i;
				for (i = 0; i < impmods.length; i++) {
					if (impmods[i].derefReference().equals(modoid)) {
						expr.importModuleRef = i;
						break;
					}
				}
				assert i < impmods.length : "unable to find imported module";
			}
			try {
				Signature mbvarsig = SBQLTypeCheckerHelper.inferSignature(mbvar.getType(), mbvar.getMinCard(), mbvar.getMaxCard());
				Signature paramsig = expr.getExpression().getSignature();
				if(expr.getExpression() instanceof EmptyExpression)
				{
					paramsig.setCardinality(1, 1);
				}
				this.checkSigTypeCompatibility(mbvarsig, paramsig, expr.getExpression(), attr, true);
				// if (mbvar.isTypeReference()) {
				// requireReference(expr.getExpression().getSignature(),
				// expr.getExpression());
				//	    	
				// this.checkReferenceCompatibility(mbvar.getType(),
				// (ReferenceSignature) expr.getExpression()
				// .getSignature(), expr.getExpression(), attr);
				//	    	
				// } else
				// this.checkTypeCompatibility(mbvar.getType(), expr
				// .getExpression().getSignature(), expr
				// .getExpression(), attr);
			} catch (TypeCheckerException e) {
				throw exception("incompatible types: " + e.getMessage(), expr);
			}
			if (!rsig.isVirtual())
				this.checkCardinality(minCard, maxCard, expr.getExpression(), attr);

		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		return rsig;

	}

	@Override
	public Object visitDateExpression(DateExpression expr, Object attr) throws SBQLException {
		expr.setSignature(new ValueSignature(env.dateType));

		return null;
	}

	@Override
	public Object visitDateprecissionExpression(DateprecissionExpression expr, Object attr) throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);

		// add automatic dereferences (if neccesary)
		this.enforceDereference(expr.getLeftExpression(), attr);

		// insert coercions (if necessary)
		if (!((ValueSignature) expr.getLeftExpression().getSignature()).value.equals(env.dateType))
			insertPrimitiveTypeCoercion(env.dateType, expr.getLeftExpression(), attr);

		expr.setSignature(new ValueSignature(env.dateType));
		expr.getSignature().setMinCard(expr.getLeftExpression().getSignature().getMinCard());
		expr.getSignature().setMaxCard(expr.getLeftExpression().getSignature().getMaxCard());

		return null;
	}

	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException {
		// create environment for links' signatures
		this.staticEnvsManager.createStaticNestedEnvironment(expr.getSignature());

		expr.getExpression().accept(this, attr);
		decorateUnaryExpressionWithLinks(expr);

		expr.setSignature(expr.getExpression().getSignature());

		return null;
	}

	@Override
	public Object visitRandomExpression(RandomExpression expr, Object attr) throws SBQLException {
		if (expr.getRightExpression() instanceof EmptyExpression) {
			// expr.replaceSubexpr(expr.getLeftExpression(), new
			// ToBagExpression(expr.getLeftExpression()));

			expr.getLeftExpression().accept(this, attr);

			expr.setSignature(expr.getLeftExpression().getSignature());
			expr.getSignature().setMinCard(0);
			expr.getSignature().setMaxCard(1);
		} else {
			expr.getLeftExpression().accept(this, attr);
			expr.getRightExpression().accept(this, attr);

			// add automatic dereferences (if neccesary)
			this.enforceDereference(expr.getLeftExpression(), attr);
			this.enforceDereference(expr.getRightExpression(), attr);

			// add coercion to [1..1] cardinality (run-time check) if neccessary
			this.enforceSingleElement(expr.getLeftExpression(), attr);
			this.enforceSingleElement(expr.getRightExpression(), attr);

			// insert coercions (if necessary)
			if (!((ValueSignature) expr.getLeftExpression().getSignature()).value.equals(env.integerType))
				insertPrimitiveTypeCoercion(env.integerType, expr.getLeftExpression(), attr);

			if (!((ValueSignature) expr.getRightExpression().getSignature()).value.equals(env.integerType))
				insertPrimitiveTypeCoercion(env.integerType, expr.getRightExpression(), attr);

			expr.setSignature(new ValueSignature(env.integerType));
			expr.getSignature().setMinCard(1);
			expr.getSignature().setMaxCard(1);
		}

		// decorate expression & signature with links
		decorateBinaryExpressionWithLinks(expr);

		return null;
	}

	/**
	 * Typecheck procedure parameters
	 * 
	 * @param proc
	 *            - meta-procedure
	 * @param argExpr
	 *            - expression node for procedure params
	 * @param attr
	 *            - visitor second argument
	 * @throws Exception
	 */
	private void checkProcedureArguments(MBProcedure proc, Expression argExpr, Object attr) throws DatabaseException {
		// get procedure arguments
		OID[] arguments = proc.getArguments();
		// flatten the actual params expression
		Expression[] actualparams = argExpr.flatten();

		// check the number of parameters
		if (arguments.length != actualparams.length) {
			throw exception("Wrong number of procedure arguments: " + " found: " + actualparams.length + ", expected: " + arguments.length, argExpr);
		}

		// foreach formal param check compatibility with actual param
		for (int i = 0; i < arguments.length; i++) {
			MBVariable mbarg = new MBVariable(arguments[i]);
			Signature argSig = SBQLTypeCheckerHelper.inferSignature(mbarg.getType(), mbarg.getMinCard(), mbarg.getMaxCard());
			this.checkSigTypeCompatibility(argSig, actualparams[i].getSignature(), actualparams[i], attr, true);
		}
	}

	protected void checkProcedureSignatureCompatibility(OID declared, ReferenceSignature sig, ASTNode owner, Object attr) throws DatabaseException {
		MBProcedure prototype = new MBProcedure(declared);
		MBProcedure compared = new MBProcedure(sig.value);
		if (!compared.isValid())
			throw exception("procedure reference required", owner);
		// check return type
		if (!prototype.getType().equals(compared.getType())) {
			throw exception("incompatible result types, required:" + prototype.getTypeName() + " found:" + compared.getTypeName(), owner);
		}
		if ((prototype.getMinCard() < compared.getMinCard()) || (prototype.getMaxCard() > compared.getMaxCard())) {
			throw exception("incompatible result cardinality, required:"
					+ SBQLTypeCheckerHelper.printCardinality(prototype.getMinCard(), prototype.getMaxCard()) + " found:"
					+ SBQLTypeCheckerHelper.printCardinality(compared.getMinCard(), compared.getMaxCard()), owner);
		}
		// check arguments
		OID[] protoargs = prototype.getArguments();
		OID[] compargs = compared.getArguments();
		if (protoargs.length != compargs.length)
			throw exception("wrong argument numbers, required:" + protoargs.length + " found:" + compargs.length, owner);

		for (int i = 0; i < protoargs.length; i++) {
			MBVariable protoarg = new MBVariable(protoargs[i]);
			MBVariable comparg = new MBVariable(compargs[i]);
			assert protoarg.isValid() && comparg.isValid() : "current impelementation assumes that procedure arguments are MBVariables";
			if (!protoarg.getType().equals(comparg.getType())) {
				throw exception("argument " + i + " incompatible types, required:" + protoarg.getTypeName() + " found:" + comparg.getTypeName(),
						owner);
			}
			if ((protoarg.getMinCard() < comparg.getMinCard()) || (protoarg.getMaxCard() > comparg.getMaxCard())) {
				throw new TypeCheckerException("argument " + i + " incompatible cardinality, required:"
						+ SBQLTypeCheckerHelper.printCardinality(prototype.getMinCard(), prototype.getMaxCard()) + " found:"
						+ SBQLTypeCheckerHelper.printCardinality(comparg.getMinCard(), comparg.getMaxCard()));
			}
		}

	}

	public boolean checkSigTypeCompatibility(Signature pattern, Signature sig, Expression ownerExpr, Object attr, boolean withCardinality) {
		boolean modified = false;
		if (pattern instanceof ReferenceSignature) {
			modified = checkReferenceSigCompatibility((ReferenceSignature) pattern, sig, ownerExpr, attr, withCardinality) || modified;
		} else {
			if (sig instanceof ReferenceSignature) {
				Expression expr = enforceDereference(ownerExpr, attr);
				modified = !expr.equals(ownerExpr);
				sig = expr.getSignature();
			}
			if (pattern instanceof ValueSignature) {
				modified = checkValueSigCompatibility((ValueSignature) pattern, sig, ownerExpr, attr, withCardinality) || modified;
			} else if (pattern instanceof StructSignature) {
				modified = checkStructSigCompatibility((StructSignature) pattern, sig, ownerExpr, attr, withCardinality) || modified;
			} else if (pattern instanceof BinderSignature) {
				modified = checkBinderSigCompatibility((BinderSignature) pattern, sig, ownerExpr, attr, withCardinality) || modified;
			} else {
				assert false : "unknown signature type";
			}
		}
		return modified;
	}

	/**
	 * @param pattern
	 * @param sig
	 * @param ownerExpr
	 * @param attr
	 * @return
	 */
	private boolean checkReferenceSigCompatibility(ReferenceSignature pattern, Signature sig, Expression ownerExpr, Object attr, boolean withCardinality) {
		boolean modified = false;
		ReferenceSignature rsig = requireReference(sig, ownerExpr);
		OID patternOid = pattern.value;
		OID sigOid = rsig.value;
		if (!patternOid.equals(sigOid)) {

			if (pattern.reflevel < rsig.reflevel) {
				Expression expr = enforceDereference(ownerExpr, attr);
				modified = !expr.equals(ownerExpr);
				rsig = requireReference(expr.getSignature(), expr);
				sigOid = rsig.value;
			}
			try {
				switch (new MBObject(patternOid).getObjectKind()) {
				case PROCEDURE_OBJECT:
					checkProcedureSignatureCompatibility(patternOid, rsig, ownerExpr, attr);
					break;
				case VIRTUAL_VARIABLE_OBJECT:
					MBVirtualVariable virtVar = new MBVirtualVariable(patternOid);
					OID viewOID = virtVar.getView();
					if (!rsig.isVirtual() || !rsig.value.equals(viewOID))
						throw exception("reference incompatibility: found '" + sigOid.getObjectName() + "', required '" + patternOid.getObjectName()
								+ "'", ownerExpr);
					break;
				case CLASS_OBJECT:
					MBClass clazz = new MBClass(patternOid);
					patternOid = clazz.getDefaultVariable();
					// no break here!!
				case VARIABLE_OBJECT:
					MBVariable var = new MBVariable(patternOid);
					MBVariable sigVar = requireVariable(sigOid, ownerExpr);

					modified = checkVariableSigCompatiblity(pattern, rsig, ownerExpr, attr, true) || modified;
					break;
				default:
					throw exception("wrong reference kind '" + new MBObject(patternOid).getObjectKind().toString() + "'", ownerExpr);
				}

			} catch (DatabaseException e) {
				throw exception(e, ownerExpr);
			}
		}
		if(withCardinality)
			modified = this.checkSigCardinality(pattern.getMinCard(), pattern.getMaxCard(), rsig, ownerExpr, attr) || modified;
		return modified;
	}

	/**
	 * @param pattern
	 * @param sig
	 * @param ownerExpr
	 * @param attr
	 * @return
	 */
	private boolean checkBinderSigCompatibility(BinderSignature pattern, Signature sig, Expression ownerExpr, Object attr, boolean withCardinality) {
		boolean modified = false;
		BinderSignature bsig = requireBinder(sig, ownerExpr);
		if (!pattern.name.equals(bsig.name))
			throw exception("wrong binder name: found '" + bsig.name + "', required '" + pattern.name + "'", ownerExpr);
		modified = checkSigTypeCompatibility(pattern.value, bsig.value, bsig.value.getOwnerExpression(), attr, withCardinality);
		if(withCardinality)
			modified = checkSigCardinality(pattern.getMinCard(), pattern.getMaxCard(), bsig, ownerExpr, attr) | modified;
		return modified;
	}

	/**
	 * @param pattern
	 * @param sig
	 * @param ownerExpr
	 * @param attr
	 * @return
	 */
	private boolean checkStructSigCompatibility(StructSignature pattern, Signature sig, Expression ownerExpr, Object attr, boolean withCardinality) {
		boolean modified = false;
		Signature[] patternFields = pattern.getFields();
		Signature[] sigFields;

		if (sig instanceof StructSignature) {
			sigFields = ((StructSignature) sig).getFields();
		} else {
			if(sig instanceof ValueSignature && ((ValueSignature)sig).value.equals(env.voidType))
				sigFields = new Signature[0];
			else
				sigFields = new Signature[] { sig };
		}

		Map<String, BinderSignature> bsigFields = new HashMap<String, BinderSignature>();

		for (Signature sigField : sigFields) {
			if (sigField instanceof BinderSignature) {
				BinderSignature binderSignature = (BinderSignature) sigField;
				bsigFields.put(binderSignature.name, binderSignature);
			} else
				throw exception("unnamed structure field", ownerExpr);
		}

		for (Signature patternField : patternFields) {
			assert patternField instanceof BinderSignature : "patternField instanceof BinderSignature";
			BinderSignature patternFieldBinder = (BinderSignature) patternField;
			int patternMinCard = patternFieldBinder.getMinCard();
			int patternMaxCard = patternFieldBinder.getMaxCard();
			BinderSignature sigField = bsigFields.remove(patternFieldBinder.name);
			if (sigField == null) {
				if (patternMinCard > 0)
					throw exception("structure field with name '" + patternFieldBinder.name + "' is not optional", ownerExpr);
				else
					continue;
			}
			try {
				checkBinderSigCompatibility(patternFieldBinder, sigField, sigField.getOwnerExpression(), attr, false);
				checkSigCardinality(patternMinCard, patternMaxCard, sigField, sigField.getOwnerExpression(), attr);
			} catch (TypeCheckerException e) {
				throw exception("Field '" + patternFieldBinder.name + "'" + e.getMessage(), ownerExpr);
			}
		}
		if (bsigFields.size() > 0) {
			String names = "";
			for (String name : bsigFields.keySet()) {
				name += "'" + name + "' ";
			}
			throw exception("some of the structure fields does not occur in the pattern struct (" + names + ")", ownerExpr);
		}
		if(withCardinality)
			modified = this.checkSigCardinality(pattern.getMinCard(), pattern.getMaxCard(), sig, ownerExpr, attr) || modified;
		return modified;
	}

	/**
	 * @param patternMinCard
	 * @param patternMaxCard
	 * @param sigField
	 * @param ownerExpression
	 * @param attr
	 */
	private boolean checkSigCardinality(int patternMinCard, int patternMaxCard, Signature sig, Expression ownerExpression, Object attr) {

		boolean modified = false;
		int sigMinCard = sig.getMinCard();
		int sigMaxCard = sig.getMaxCard();
		if ((patternMinCard <= sigMinCard && patternMaxCard >= sigMaxCard))
			return false;
		if (ownerExpression == null)
			throw exception("cardinality error: required " + SBQLTypeCheckerHelper.printCardinality(patternMinCard, patternMaxCard), ownerExpression);
		Expression enforced;
		if (patternMinCard == 1 && patternMaxCard == 1) {
			enforced = enforceSingleElement(ownerExpression, attr);
			if (!enforced.equals(ownerExpression))
				modified = true;
		} else {
			enforced = enforceMaximalCardinality(patternMaxCard, ownerExpression, attr);
			if (!enforced.equals(ownerExpression))
				modified = true;
			enforced = enforceMinimalCardinality(patternMinCard, ownerExpression, attr);
			if (!enforced.equals(ownerExpression))
				modified = true;
		}
		return modified;
	}

	/**
	 * @param pattern
	 * @param sig
	 * @param ownerExpr
	 * @param attr
	 * @return
	 */
	private boolean checkValueSigCompatibility(ValueSignature pattern, Signature sig, Expression ownerExpr, Object attr, boolean withCardinality) {
		boolean modified = false;
		if (sig instanceof ReferenceSignature) {
			enforceDereference(ownerExpr, attr);
			modified = true;
		}
		if(pattern.getEnumerator()!=null){
			if(sig.getEnumerator()==null || (!pattern.getEnumerator().equals(sig.getEnumerator()))){
				throw exception("try to cast on '" + pattern.getEnumerator() + "'", ownerExpr);
			}
		}
		ValueSignature vsig = requireValue(sig, new OID[0], "", ownerExpr);
		// check the type and perform coerce (if needed & possible)
		if (!pattern.value.equals(vsig.value)) {
			// try coerce
			OID coerce = SBQLTypeCheckerHelper.findPrimitiveCoerce(pattern.value, vsig.value);
			if (coerce != null) {
				this.insertPrimitiveTypeCoercion(coerce, sig.getOwnerExpression(), attr);
				modified = true;
			} else
				try {
					throw exception("unable to cast '" + vsig.value.getObjectName() + "' to '" + pattern.value.getObjectName() + "'", ownerExpr);
				} catch (DatabaseException e) {
					throw exception(e, ownerExpr);
				}
		}

		return modified;
	}

	/**
	 * @param type
	 * @param sig
	 * @param ownerExpr
	 * @param attr
	 * @return
	 * @throws DatabaseException
	 */
	private boolean checkProcedureReferenceCompatibility(OID type, Signature sig, ASTNode ownerExpr, Object attr) throws DatabaseException {
		ReferenceSignature rsig = requireReference(sig, ownerExpr);
		checkProcedureSignatureCompatibility(type, rsig, ownerExpr, attr);
		return false;
	}

	/**
	 * Checks if the result Expression signature has compatible cardinality this
	 * kind of check is performed for procedure param and return value, update
	 * operations etc.
	 * 
	 * @param requiredMinCard
	 *            - required minimal cardinality
	 * @param requiredMaxCard
	 *            - required maximal cardinality
	 * @param expr
	 *            - expression to be checked
	 * @param attr
	 *            - visitor attribute
	 * @throws DatabaseException
	 * @throws Exception
	 */
	protected Expression checkCardinality(int requiredMinCard, int requiredMaxCard, Expression expr, Object attr) {
		assert expr != null : "expr != null";
		// check for single element
		if (requiredMinCard == 1 && requiredMaxCard == 1) {
			return this.enforceSingleElement(expr, attr);
		}
		expr = this.enforceMaximalCardinality(requiredMaxCard, expr, attr);
		expr = this.enforceMinimalCardinality(requiredMinCard, expr, attr);

		return expr;
	}

	private void typecheckTrasitiveClosureExpression(TransitiveClosureExpression expr, Object attr) throws SBQLException {
		// visit the first argument expression and get its signature
		expr.getLeftExpression().accept(this, attr);

		// eval second argument against new environment
		this.evalAgainstNestedEnvironment(expr, attr);

		Signature sig1 = expr.getLeftExpression().getSignature();
		Signature sig2 = expr.getRightExpression().getSignature();

		// clone the first signature before checking comparablity
		// we don't want the cardinality to blur the comparability
		Signature csig1 = sig1.clone();
		csig1.setMinCard(sig2.getMinCard());
		csig1.setMaxCard(sig2.getMaxCard());
		if (!csig1.isComparableTo(sig2))
			// FIXME: variants!!!
			throw exception("Unioning non-comparable signatures not implemented yet.", expr);

		// build the signature of the result of the visited expression
		// FIXME: variants!!!
		Signature sig = sig1.clone();
		sig.setMinCard(0);// Signature.cardinalityAdd(sig1.minCard,
		// sig2.minCard);
		sig.setMaxCard(Integer.MAX_VALUE);

		// assign the signature to the visited expression
		expr.setSignature(sig);

	}

	private void typecheckInsert(InsertExpression expr, Object attr) throws DatabaseException, SBQLException {

		// evaluate left expression
		expr.getLeftExpression().accept(this, attr);
		// evaluate right expression
		expr.getRightExpression().accept(this, attr);
		// l-value must be single (possibly runtime check)
		this.enforceSingleElement(expr.getLeftExpression(), attr);
		// the operands must be a reference
		ReferenceSignature lrefsig = requireReference(expr.getLeftExpression().getSignature(), expr.getLeftExpression());
		// left signature must be additionally mutable
		requireMutablility(lrefsig, expr.getLeftExpression());
		if (lrefsig.reflevel > 0) {
			lrefsig = (ReferenceSignature) this.enforceDereference(expr.getLeftExpression(), attr).getSignature();
		}

		// get the rValue name
		String rname; // name of the target variable
		ReferenceSignature rrefsig; // right reference
		MBVariable rvar; // right variable
		if (expr.getRightExpression().getSignature() instanceof BinderSignature) {
			BinderSignature bsig = ((BinderSignature) expr.getRightExpression().getSignature());
			rname = bsig.name;
			rrefsig = requireReference(bsig.value, expr);
			rvar = requireVariable(rrefsig.value, bsig.value.getOwnerExpression());
		} else {

			rrefsig = requireReference(expr.getRightExpression().getSignature(), expr.getRightExpression());
			rvar = requireVariable(rrefsig.value, expr.getRightExpression());
			rname = rvar.getName();
		}

		OID subvid = null; // view oid if we have insert into virtual object

		// we cannot insert virtual object
		if (rrefsig.isVirtual())
			throw exception("virtual right hand insert operand unsupported", expr);

		MBVariable targetVar = null;

		// check if the lvalue is virtual
		if (lrefsig.isVirtual()) {
			MBView view = new MBView(lrefsig.value);
			// find sub view that declares object with a name of inserted object
			subvid = SBQLTypeCheckerHelper.findSubViewByVirtualObjectName(view, rrefsig.value.getObjectNameId());
			if (subvid != null) {
				// search for on new procedure
				MBView sview = new MBView(subvid);
				OID onnew = requireViewOperator(sview, OdraViewSchema.GenericNames.ON_NEW_NAME, "insert", expr.getLeftExpression());

				// set variable to on_new argument
				MBProcedure onnewproc = new MBProcedure(onnew);
				this.enforceDereference(expr.getRightExpression(), attr);
				this.checkProcedureArguments(onnewproc, expr.getRightExpression(), attr);

			} else
				throw exception("Variable " + view.getVirtualObject().getObjectName() + " has no declared subobject named: "
						+ rrefsig.value.getObjectName(), expr);

		} else {

			targetVar = requireSubVariable(rname, lrefsig.value, this.module, expr);
			try {
				Signature targetVarSig = SBQLTypeCheckerHelper.inferSignature(targetVar.getOID(), targetVar.getMinCard(), targetVar.getMaxCard());
				this.checkSigTypeCompatibility(targetVarSig, rrefsig, rrefsig.getOwnerExpression(), attr,true);
				// if (targetVar.isTypeReference()) {
				// if (!rvar.isTypeReference()) {
				// throw exception("pointer object required: "
				// + rvar.getName(), expr);
				// }
				// this.checkReferenceCompatibility(targetVar.getType(),
				// new ReferenceSignature(rvar.getType()), expr, attr);
				// } else {
				// checkVariableCompatiblity(targetVar, rvar, expr, attr);
				// // this.checkTypeCompatibility(foundvar.getType(),
				// // SBQLTypeCheckerHelper.performDeref(rvar.getType()), expr,
				// // attr);
				// }
				// check the cardinality
				this.requireChangeableCollection(rvar.getMinCard(), rvar.getMaxCard(), expr.getRightExpression());
				if (!lrefsig.isVirtual()) {
					this.requireChangeableCollection(targetVar.getMinCard(), targetVar.getMaxCard(), expr.getRightExpression());
				}
			} catch (TypeCheckerException e) {
				throw exception("Insert operator: type incompatibility: " + e.getMessage(), expr);
			}
		}
		if (subvid == null)
			expr.setSignature(new ReferenceSignature(targetVar.getOID()));
		else {
			expr.setSignature(new ReferenceSignature(subvid));
			((ReferenceSignature) expr.getSignature()).setVirtual(true);
		}
		// ??
		expr.getSignature().setMinCard(0);// rrefsig.minCard;
		expr.getSignature().setMaxCard(0);// rrefsig.maxCard;
		expr.getSignature().setAssociatedExpression(rrefsig.getOwnerExpression());
	}

	/**
	 * @param foundvar
	 * @param rvar
	 * @param expr
	 * @param attr
	 * @return true if the AST was modified during typecheck
	 * @throws DatabaseException
	 */
	private boolean checkVariableSigCompatiblity(ReferenceSignature pattern, ReferenceSignature rsig, Expression owner, Object attr, boolean withCardinality)
			throws DatabaseException {
		MBVariable declared = new MBVariable(pattern.value);
		MBVariable actual = new MBVirtualVariable(rsig.value);
		if (declared.equals(actual))
			return false; // this is the same variable - no type error

		if (declared.isTypeClass()) {
			if (!actual.isTypeClass())
				throw exception("'" + actual.getName() + "' must be an instance of '" + declared.getType().getObjectName()
						+ "' class or its sub-class", owner);
			MBClass declclass = new MBClass(declared.getType());
			MBClass actualclass = new MBClass(actual.getType());
			if (!actualclass.equals(declclass)) {
				if (!actualclass.isSubClassOf(declclass.getOID()))
					throw exception("variable '" + actual.getName() + "' is not type compatible with '" + declared.getName(), owner);
			}
			return false;
		} else {
			Signature declaredTypeSig = SBQLTypeCheckerHelper.inferSignature(declared.getType());
			Signature actualTypeSig = SBQLTypeCheckerHelper.inferSignature(actual.getType());
			return this.checkSigTypeCompatibility(declaredTypeSig, actualTypeSig, owner, attr, withCardinality);
			// return checkTypeCompatibility(declared.getType(),
			// SBQLTypeCheckerHelper.performDeref(actual.getType()), owner,
			// attr);
		}

	}

	/**
	 * @param lrefsig
	 */
	private void requireMutablility(ReferenceSignature lrefsig, ASTNode owner) {
		if (!lrefsig.isMutable())
			throw exception("left hand of the insert operator cannot be immutable", owner);

	}

	private void evalAgainstNestedEnvironment(NonAlgebraicExpression expr, Object attr) throws SBQLException {

		EnvironmentInfo envsInfo = this.staticEnvsManager.createStaticNestedEnvironment(expr.getLeftExpression().getSignature());

		// visit the second argument expression
		try {
			expr.getRightExpression().accept(this, attr);
		} catch (TypeCheckerException ex) {
			if (ex.getCause() == null || !(ex.getCause() instanceof DatabaseException))
				envsInfo = this.recheckWithEllipse(expr.getLeftExpression(), expr.getRightExpression(), attr, ex);

		} finally {
			// destroy the newly added environment
			// regardless of whether an error has occured or not
			this.staticEnvsManager.destroyEnvironment();
		}
		expr.setEnvsInfo(envsInfo);
	}

	protected EnvironmentInfo recheckWithEllipse(Expression e, ASTNode rightNode, Object attr, SBQLException ex) throws SBQLException {
		EnvironmentInfo envsInfo;

		// if left hand operand signature is a pointer variable recheck with
		// ellipse
		if (e.getSignature() instanceof ReferenceSignature) {
			try {
				MBVariable lvar = new MBVariable(((ReferenceSignature) e.getSignature()).value);
				if (lvar.isValid() && lvar.isTypeReference()) {
					this.staticEnvsManager.destroyEnvironment();
					Signature sig = this.enforceDereference(e, attr).getSignature();
					envsInfo = this.staticEnvsManager.createStaticNestedEnvironment(sig);

					rightNode.accept(this, attr);
				} else
					throw ex;
			} catch (DatabaseException e1) {
				throw new TypeCheckerException(e1, e, this);
			} finally {
			}
		}
		// the scope may be opened by dot yet before the algebraic expression
		else if (e.getSignature() instanceof StructSignature) {
			if (e instanceof NonAlgebraicExpression) {
				envsInfo = this.staticEnvsManager.createStaticNestedEnvironment(((NonAlgebraicExpression) e).getLeftExpression().getSignature());
				rightNode.accept(this, attr);
			} else
				throw ex;
		} else
			throw ex;
		return envsInfo;
	}

	/**
	 * Decorates non-algebraic expression with links.
	 * 
	 * @param expr
	 *            expression to be decorated
	 */
	private void decorateNonAlgebraicWithLinks(NonAlgebraicExpression expr) {
		// decorate expression with servers taken from right node
		if (expr.getSignature() instanceof ReferenceSignature) {
			ReferenceSignature ref = (ReferenceSignature) expr.getSignature();
			ref.links.clear();
			expr.links.clear();

			if ((!expr.getLeftExpression().links.isEmpty()) && expr.getLeftExpression().links.equals(expr.getRightExpression().links)) {
				expr.links.addAll(expr.getLeftExpression().links);
				ref.links.addAll(expr.getLeftExpression().links);
			} else if ((!expr.getLeftExpression().links.isEmpty()) && (!expr.getRightExpression().links.isEmpty())
					&& expr.getLeftExpression().links.containsAll(expr.getRightExpression().links)) {
				expr.getRightExpression().links.clear();
				expr.getRightExpression().links.addAll(expr.getLeftExpression().links);

				expr.links.addAll(expr.getLeftExpression().links);
				ref.links.addAll(expr.getLeftExpression().links);
			}
			// if nodes are decorated with different set of links,
			// the query optimizer will decide where to execute the subqueries
			else {
				// decorate with empty set
				expr.links.clear();
			}
		} else if (expr.getSignature() instanceof ValueSignature) {
			expr.links.clear();
			expr.getSignature().links.clear();

			expr.links.addAll(expr.getLeftExpression().links);
			expr.getSignature().links.addAll(expr.getLeftExpression().links);
		}
	}

	/**
	 * Decorates binary expression with links.
	 * 
	 * @param expr
	 *            expression to be decorated
	 * 
	 */
	private void decorateBinaryExpressionWithLinks(BinaryExpression expr) {
		expr.links.clear();

		// if both nodes are deocrated with identical links
		if ((!expr.getLeftExpression().links.isEmpty()) && (expr.getLeftExpression().links.equals(expr.getRightExpression().links))) {
			expr.links.addAll(expr.getLeftExpression().links);

			expr.getSignature().links.clear();
			expr.getSignature().links.addAll(expr.getLeftExpression().links);
		}
		// if one of the nodes is a literal
		else if ((expr.getLeftExpression().getSignature() instanceof ValueSignature && expr.getLeftExpression() instanceof DerefExpression
				&& expr.getLeftExpression().links.size() != 0 && expr.getRightExpression().getSignature() instanceof ValueSignature)
				|| (expr.getRightExpression().getSignature() instanceof ValueSignature && expr.getRightExpression() instanceof DerefExpression
						&& expr.getRightExpression().links.size() != 0 && expr.getLeftExpression().getSignature() instanceof ValueSignature)) {

			if (expr.getLeftExpression() instanceof DerefExpression) {
				expr.getRightExpression().links.clear();
				expr.getRightExpression().links.addAll(expr.getLeftExpression().links);

				expr.getRightExpression().getSignature().links.clear();
				expr.getRightExpression().getSignature().links.addAll(expr.getLeftExpression().links);
			} else {
				expr.getLeftExpression().links.clear();
				expr.getLeftExpression().links.addAll(expr.getRightExpression().links);

				expr.getLeftExpression().getSignature().links.clear();
				expr.getLeftExpression().getSignature().links.addAll(expr.getRightExpression().links);
			}

			expr.links.addAll(expr.getRightExpression().links);
			expr.getSignature().links.addAll(expr.getRightExpression().links);
		}
		// if nodes are decorated with different set of links,
		// the query optimizer will decide where to execute the subqueries
		else {
			// decorate with empty set
			expr.links.clear();
			expr.getSignature().links.clear();
		}
	}

	/**
	 * Decorates unary expression with links.
	 * 
	 * @param expr
	 *            expression to be decorated
	 */
	private void decorateUnaryExpressionWithLinks(UnaryExpression expr) {
		expr.links.clear();
		expr.links.addAll(expr.getExpression().links);

		expr.getSignature().links.clear();
		expr.getSignature().links.addAll(expr.getExpression().links);
	}

	private boolean isASTNodeLinksMetabasesUptodate(ASTNode root) {
		boolean isStaleLinksMetabase = false;

		try {
			IsDistributedAST isDistributedAST = new IsDistributedAST();
			root.accept(isDistributedAST, null);

			if (isDistributedAST.isDistributed()) {
				// check if links' metabases are not stale
				for (OID linkOID : isDistributedAST.getLinks()) {
					MBLink mblink = new MBLink(linkOID);
					DBLink dblink = LinkManager.getInstance().findLink(mblink.getName(), mblink.getHost(), mblink.getSchema(), mblink.getPort(),
							Session.getUserContext());

					if (!LinkManager.getInstance().isLinkMetaBaseUptoDate(dblink)) {
						isStaleLinksMetabase = true;
						LinkManager.getInstance().refreshLinkMetadata(dblink);
					}
				}
			}
		} catch (Exception e1) {
		}

		return isStaleLinksMetabase;
	}

	@Override
	public Object visitExecSqlExpression(ExecSqlExpression expr, Object attr) throws SBQLException {
		// the expression signature has been already given by the wrapper
		// optimizer and it should not be changed
		expr.query.accept(this, attr);
		expr.pattern.accept(this, attr);
		expr.module.accept(this, attr);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitAtMostExpression(odra.sbql.ast.expressions
	 * .AtMostExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtMostExpression(AtMostExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}
		Signature sig = expr.getExpression().getSignature();

		// build the signature of the result of the visited expression
		// by cloning the signature sig and setting its maximal cardinality
		// to the maximal allowed by the AtMostExpression
		Signature csig = sig.clone();
		csig.setMaxCard(expr.getMaxCardinality());

		// assign the signature to the visited expression
		expr.setSignature(csig);

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitOidExpression(odra.sbql.ast.expressions
	 * .OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) throws SBQLException {
		expr.getExpression().accept(this, attr);
		Signature sig = expr.getExpression().getSignature();
		ReferenceSignature rsig = requireReference(sig, expr.getExpression());
		requireVariable(rsig.value, expr.getExpression());
		try {
			Signature result = SBQLTypeCheckerHelper.inferSignature(env.oidType);
			result.setCardinality(sig.getMinCard(), sig.getMaxCard());
			expr.setSignature(result);
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitDeserializeOidExpression(odra.sbql.ast.
	 * expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr, Object attr) throws SBQLException {
		if (attr == null) {
			expr.getLeftExpression().accept(this, attr);
			expr.getRightExpression().accept(this, attr);
			enforceDereference(expr.getLeftExpression(), attr);
		}

		ReferenceSignature rsig = requireReference(expr.getRightExpression().getSignature(), expr);
		// deserialization parameter is OID value
		ValueSignature lsig;
		try {
			lsig = requireValue(expr.getLeftExpression().getSignature(), new OID[] { env.stringType }, env.oidType.getObjectName(), expr
					.getLeftExpression());
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		requireVariable(rsig.value, expr);
		// the rest of the type check has to be done in runtime
		ReferenceSignature resultSig = new ReferenceSignature(rsig.value);
		resultSig.setCardinality(lsig.getMinCard(), lsig.getMaxCard());
		expr.setSignature(resultSig);
		return null;
	}

	/**
	 * @param value
	 * @param expr
	 */
	protected MBVariable requireVariable(OID value, ASTNode node) {
		try {
			MBVariable mbvar = new MBVariable(value);
			if (!mbvar.isValid())
				throw exception("variable reference exprected", node);
			return mbvar;
		} catch (DatabaseException e) {
			throw exception(e, node);
		}

	}

	/**
	 * @param type
	 * @param string
	 * @param expr
	 */
	protected ValueSignature requireValue(Signature sig, OID[] types, String typeName, ASTNode node) {
		if (!(sig instanceof ValueSignature))
			throw exception("value expected", node);
		ValueSignature vsig = (ValueSignature) sig;
		try {
			if (types.length > 0) {
				boolean found = false;
				for (OID type : types) {
					if (vsig.value.equals(type)) {
						found = true;
						break;
					}
				}
				if (!found) {
					String message = "expected value of '";
					for (OID type : types)
						message += type.getObjectName() + "' ";
					message += "type/types";
					throw exception(message, node);
				}
			}
			if (typeName != null && !("".equals(typeName))) {
				if (!typeName.equals(vsig.getTypeName()))
					throw exception("expected value of distinct type '" + typeName + "'", node);
			}
		} catch (DatabaseException e) {
			throw exception(e, node);
		}
		return (ValueSignature) sig;
	}

	/**
	 * @param sig
	 *            - signature to cast to referencesignature
	 */
	protected ReferenceSignature requireReference(Signature sig, ASTNode node) {
		if (!(sig instanceof ReferenceSignature))
			throw exception("reference expected", node);
		else
			return (ReferenceSignature) sig;

	}

	/**
	 * @param sig
	 *            - signature to cast to referencesignature
	 */
	protected BinderSignature requireBinder(Signature sig, ASTNode node) {
		if (!(sig instanceof BinderSignature))
			throw exception("binder expected", node);
		else
			return (BinderSignature) sig;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitAtLeastExpression(odra.sbql.ast.expressions
	 * .AtLeastExpression, java.lang.Object)
	 */
	@Override
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr) throws SBQLException {
		// visit the argument expression and get its signature
		// it is possible that we do not need to perform recurrent check
		if (attr == null) {
			expr.getExpression().accept(this, attr);
		}
		Signature sig = expr.getExpression().getSignature();

		// build the signature of the result of the visited expression
		// by cloning the signature sig and setting its minimal cardinality
		// to the minimal allowed by the AtLeastExpression
		Signature csig = sig.clone();
		csig.setMaxCard(expr.getMinCardinality());

		// assign the signature to the visited expression
		expr.setSignature(csig);

		// decorate expression & signature with links
		decorateUnaryExpressionWithLinks(expr);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitRangeAsExpression(odra.sbql.ast.expressions
	 * .RangeAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
		// visit the argument expression and get its signature
		expr.getExpression().accept(this, attr);
		Signature sig1 = expr.getExpression().getSignature();

		// create signature of a 'range as'
		// (BinderSignature(ValueSignature(integer)))
		Signature sig2 = new BinderSignature(expr.name().value(), new ValueSignature(env.integerType));
		// the result signature is StructSignature
		expr.setSignature(SBQLTypeCheckerHelper.createStructSignature(sig1, sig2));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * odra.sbql.ast.ASTAdapter#visitRenameExpression(odra.sbql.ast.expressions
	 * .RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr) throws SBQLException {
		MBVariable targetVar, currentVar;
		expr.getExpression().accept(this, attr);
		ReferenceSignature paramsig = this.requireReference(expr.getExpression().getSignature(), expr);
		currentVar = this.requireVariable(paramsig.value, expr);
		BindingInfo bi = new BindingInfo();
		AbstractQueryResult[] sig;
		try {
			sig = this.staticEnvsManager.bind(expr.name().value(), bi);
			if (sig.length != 1) {
				throw exception("Unable to bind name '" + expr.name().value() + "'", expr);
			}
			ReferenceSignature targetsig = requireReference((Signature) sig[0], expr);
			targetVar = requireVariable(targetsig.value, expr);
			this.checkVariableSigCompatiblity(targetsig, paramsig, expr, attr, true);
			requireChangeableCollection(targetVar.getMinCard(), targetVar.getMaxCard(), expr);
			this.checkCardinality(targetVar.getMinCard(), targetVar.getMaxCard(), expr.getExpression(), attr);
			Signature resultSig = new ReferenceSignature(targetVar.getOID());
			// the result signature is a reference to taget variable with
			// cardinality of param signature
			resultSig.setCardinality(paramsig.getMinCard(), paramsig.getMaxCard());
			expr.setSignature(resultSig);
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}
		return null;
	}

	/**
	 * @param targetVar
	 */
	private void requireChangeableCollection(int minCard, int maxCard, Expression context) {
		if (minCard == maxCard)
			throw exception("The expression require changeable collection (" + SBQLTypeCheckerHelper.printCardinality(minCard, maxCard) + ")",
					context);

	}

	protected Expression enforceReferenceLevel(int requiredLevel, Expression expr, Object attr) throws SBQLException {
		Signature sig1 = expr.getSignature();
		assert sig1 instanceof ReferenceSignature : "reference signature expected";
		ReferenceSignature rsig1 = (ReferenceSignature) sig1;
		if (rsig1.reflevel < requiredLevel) {
			throw exception("references levels incompatibility. Required:  " + requiredLevel + " found: " + rsig1.reflevel, expr);
		}
		while (rsig1.reflevel > requiredLevel) {
			expr = this.enforceDereference(expr, attr);
			sig1 = expr.getSignature();
			rsig1 = requireReference(sig1, expr);
		}
		return expr;
	}

	/**
	 * perform final (CLI user defined ) dereference
	 * 
	 * @param expr
	 *            - expression root node
	 * @param strict
	 *            - if true perform dereference only for reference signatures, <br>
	 *            if false perform 'deep' dereference also for other signatures
	 *            (binders, structures)
	 * @return
	 * @throws Exception
	 */
	public Expression performFinalDereference(Expression expr, boolean strict) throws SBQLException {

		Signature sig = expr.getSignature();
		if (sig instanceof ReferenceSignature) {
			expr = this.enforceReferenceLevel(0, expr, null);
			expr = this.enforceDereference(expr, null);
		} else {

			if (this.insertTraversingDereference(expr))
				expr.accept(this, null);
		}
		return expr;

	}

	protected boolean insertTraversingDereference(Expression expr) throws SBQLException {
		boolean modified = false;
		Signature sig = expr.getSignature();

		// if the argument expression is a reference
		// add an automatic dereference before
		if (sig instanceof ReferenceSignature && !((ReferenceSignature) sig).hasRefFlag()) {
			modified = true;
			if (expr.getParentExpression() != null) {
				expr.getParentExpression().replaceSubexpr(expr, new DerefExpression(expr));
			} else {
				if (root instanceof Expression)
					root = new DerefExpression(expr);
			}
		} else {
			if (SBQLTypeCheckerHelper.isAuxiliaryNameGeneratorExpression(expr)) {
				modified = this.insertTraversingDereference(((UnaryExpression) expr).getExpression());

			} else if (SBQLTypeCheckerHelper.isStructConstructorExpression(expr)) {
				modified = this.insertTraversingDereference(((BinaryExpression) expr).getLeftExpression()) || modified;
				modified = this.insertTraversingDereference(((BinaryExpression) expr).getRightExpression()) || modified;

			} else if (expr instanceof UnionExpression) {
				modified = this.insertTraversingDereference(((BinaryExpression) expr).getLeftExpression()) || modified;
				modified = this.insertTraversingDereference(((BinaryExpression) expr).getRightExpression()) || modified;
			} else if (expr instanceof DotExpression) {
				modified = this.insertTraversingDereference(((DotExpression) expr).getRightExpression());
			}
		}
		return modified;
	}

	protected void evalCreateParamExpression(CreateExpression expr, Object attr) throws SBQLException {
		expr.getExpression().accept(this, attr);
		this.insertTraversingDereference(expr.getExpression());
		expr.getExpression().accept(this, attr);
	}

	/**
	 * Search for a variable declaration
	 * 
	 * @param name
	 * @return
	 * @throws DatabaseException
	 */
	protected ReferenceSignature requireVariableDeclaration(CreateExpression expr) throws SBQLException {
		String name = expr.name().value();
		OID declared;
		// search local variable declaration
		try {

			// search for session variable declaration
			declared = SBQLTypeCheckerHelper.findTemporalMetaVariable(name, this.module, true);
			if (declared != null) {
				expr.declaration_environment = CreateExpression.TEMPORAL;
				return new ReferenceSignature(declared, true);
			}
			// search for global variable declaration
			declared = SBQLTypeCheckerHelper.findGlobalMetaVariable(name, this.module, true, true);
			if (declared != null) {
				expr.declaration_environment = CreateExpression.PERSISTENT;
				return new ReferenceSignature(declared, true);
			}
		} catch (DatabaseException e) {
			throw exception(e, expr);
		}

		throw exception("Unable to find variable. The name '" + expr.name().value() + "' is not a name of the declared variable.", expr);
	}

	protected SBQLException exception(String message, ASTNode node) {
		message = "(l:" + node.line + " c:" + node.column + ") " + message;
		message += " in: '" + AST2TextQueryDumper.AST2Text(node) + "'";		
			
		if (node !=null && node instanceof Expression) {
			Expression expr = (Expression) node;
			if (this.root != null && !root.equals(node))
				message += " in the context of: '" + AST2TextQueryDumper.AST2Text(root) + "'";
			else if (expr.getParentExpression() != null) {
				message += " in the context of: " + AST2TextQueryDumper.AST2Text(expr.getParentExpression());
			}
		} else {
			if (this.root != null && !root.equals(node))
				message += " in the context of: " + AST2TextQueryDumper.AST2Text(root);
		}

		return new TypeCheckerException(message, node, this);
	}

	protected SBQLException exception(DatabaseException e, ASTNode node) {
		return new TypeCheckerException("Database error while typechecking: " + e.getMessage(), e, node, this);
	}

	private final MBVariable requireSubVariable(String name, OID where, DBModule module, Expression owner) throws DatabaseException {
		OID found = null;
		if (new DBModule(where).isValid()) {
			DBModule mod = new DBModule(where);

			found = module.findFirstByName(name, mod.getMetabaseEntry());

		} else {
			OID[] entries = MBObjectFactory.getTypedMBObject(where).getNestedMetabaseEntries();
			for (OID entry : entries) {
				found = module.findFirstByName(name, entry);
				if (found != null)
					break;
			}
		}
		if (found == null) {
			throw exception("Object " + where.getObjectName() + " has no declared subobject named: " + name, owner);
		}
		MBVariable mbvar = new MBVariable(found);
		if (!(mbvar.isValid())) {
			throw exception("Object " + where.getObjectName() + " has no declared subobject named: " + name, owner);
		}
		return mbvar;
	}

	private static String ERROR_NAME_CONFLICT = "Name conflict for '";
	private static String REQ_PRIMITIVE_VALUE = " primitive value required ";
	private static String RIGHT_OPERAND = " right hand operand ";
	private static String LEFT_OPERAND = " right hand operand ";
}