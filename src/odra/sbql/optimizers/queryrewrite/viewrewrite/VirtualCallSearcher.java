package odra.sbql.optimizers.queryrewrite.viewrewrite;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBView;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.OdraViewSchema.GenericNames;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.ResultPathAwareASTNodeFinder;
import odra.sbql.ast.utils.patterns.Pattern;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.queryrewrite.procedures.ProcedureCallReplacer;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;

/**
 * Look for the virtual calls in the AST. Collects replacement ASTs from the
 * views definitions (if rewrite is possible)
 * 
 * @author Radek Adamus last modified: 2007-02-07
 * @version 1.0
 */
class VirtualCallSearcher extends TraversingASTAdapter {

    ASTAdapter staticEval;

    DBModule mod;

    Map<GenericNames, Map<Expression, RewriteInfo>> operatorsRewrite = new HashMap<GenericNames, Map<Expression, RewriteInfo>>();

    Vector<RewriteInfo> on_new = new Vector<RewriteInfo>();
    Map<NameExpression, Vector<RewriteInfo>> virtChains = new Hashtable<NameExpression, Vector<RewriteInfo>>();

    Vector<RewriteInfo> roots = new Vector<RewriteInfo>();

    boolean saveSubstitutedSignature;
    
    /**
     * @param staticEval -
     *                static evaluator
     */
    protected VirtualCallSearcher(ASTAdapter staticEval, boolean saveSubstitutedSignature) {
	this.staticEval = staticEval;
	this.saveSubstitutedSignature = saveSubstitutedSignature;
	
	for (GenericNames name : GenericNames.values()) {
	    if(!name.equals(GenericNames.ON_NEW_NAME))
		this.operatorsRewrite.put(name,
			new HashMap<Expression, RewriteInfo>());
	}

    }

    ASTNode find(ASTNode node, DBModule mod) throws SBQLException {
	this.mod = mod;
	try {
	    this.setSourceModuleName(mod.getName());
	} catch (DatabaseException e) {
	    throw new OptimizationException(e, node, this);
	}
	node.accept(this, null);
	return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingASTAdapter#visitNameExpression(odra.sbql.ast.expressions.NameExpression,
     *      java.lang.Object)
     */
    @Override
    public Object visitNameExpression(NameExpression expr, Object attr)
	    throws SBQLException {

	if (!expr.isAuxiliaryName()
		&& isVirtualReferenceSignature(expr.getSignature())) {
	    boolean isSubView = false;
	    ReferenceSignature virtSig = (ReferenceSignature) expr
		    .getSignature();
	    try {
		Expression replacement = this
			.getReplacerNoParamNeedReturn((new MBProcedure(
				new MBView(virtSig.value).getSeedProc())));
		NameExpression associatedNameExpression = this
			.getSuperViewGeneratorExpression(expr);
		if (associatedNameExpression != null
			&& isVirtualReferenceSignature(associatedNameExpression
				.getSignature())) {
		    isSubView = this.isSubView(virtSig.value,
			    ((ReferenceSignature) associatedNameExpression
				    .getSignature()).value);
		    if (isSubView) {
			Vector<RewriteInfo> virtChain = virtChains
				.get(associatedNameExpression);
			if (virtChain != null)
			    virtChain.add(new RewriteInfo(expr, replacement, saveSubstitutedSignature));
		    } else {// we add new root but impossible to rewrite
			// possibly inside the on_navigate call we have another
			// view call (possibly it would be rewritten in next
			// pass)
			roots.add(new RewriteInfo(expr, null, saveSubstitutedSignature));
		    }
		} else { // we separately save root
		    roots.add(new RewriteInfo(expr, replacement, saveSubstitutedSignature));
		}
	    } catch (DatabaseException e) {
		throw new OptimizationException(e, expr, this);
	    }

	    // it always becomes a new chain
	    assert virtChains.get(expr) == null : "duplicate expression";
	    virtChains.put(expr, new Vector<RewriteInfo>());

	}
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingASTAdapter#commonVisitNonAlgebraicExpression(odra.sbql.ast.expressions.NonAlgebraicExpression,
     *      java.lang.Object)
     */
    @Override
    protected Object commonVisitNonAlgebraicExpression(
	    NonAlgebraicExpression expr, Object attr) throws SBQLException {
	expr.getLeftExpression().accept(this, attr);
	if (this.isVirtualReferenceSignature(expr.getLeftExpression()
		.getSignature())) {
	    ReferenceSignature virtSig = (ReferenceSignature) expr
		    .getLeftExpression().getSignature();
	    try {
		MBView view = new MBView(virtSig.value);
		OID navigate = view
			.getGenericProc(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME
				.toString());
		if (navigate != null) {
		    MBProcedure mbproc = new MBProcedure(navigate);
		    Expression replacement = this
			    .getReplacerNoParamNeedReturn(mbproc);
		    if (replacement != null) {
			replacement = new AsExpression(replacement, new Name(
				mbproc.getType().getObjectName()));
		    }
		    NameExpression associatedExpression = this
			    .findAssociatedNameExpression(virtSig);
		    assert this.operatorsRewrite.get(
			    GenericNames.ON_DELETE_NAME).get(
			    associatedExpression) == null : "map cannot contains this key "
			    + associatedExpression;
		    this.operatorsRewrite.get(GenericNames.ON_NAVIGATE_NAME)
			    .put(associatedExpression,
				    new RewriteInfo(expr, replacement, saveSubstitutedSignature));
		}
	    } catch (DatabaseException e) {
		throw new OptimizationException(e, expr, this);
	    }

	}
	expr.getRightExpression().accept(this, attr);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingASTAdapter#visitDerefExpression(odra.sbql.ast.expressions.DerefExpression,
     *      java.lang.Object)
     */
    @Override
    public Object visitDerefExpression(DerefExpression expr, Object attr)
	    throws SBQLException {
	Object o = super.visitDerefExpression(expr, attr);
	if (isVirtualReferenceSignature(expr.getExpression().getSignature())) {
	    ReferenceSignature virtSig = (ReferenceSignature) expr
		    .getExpression().getSignature();
	    Expression replacement;
	    try {
		replacement = this
			.getReplacerNoParamNeedReturn(new MBProcedure(
				new MBView(virtSig.value)
					.getGenericProc(OdraViewSchema.GenericNames.ON_RETRIEVE_NAME
						.toString())));
	    } catch (DatabaseException e) {
		throw new OptimizationException(e, expr, this);
	    }
	    NameExpression associatedExpression = this
		    .findAssociatedNameExpression(virtSig);
	    assert this.operatorsRewrite.get(GenericNames.ON_RETRIEVE_NAME)
		    .get(associatedExpression) == null : "map cannot contains this key "
		    + associatedExpression;
	    this.operatorsRewrite.get(GenericNames.ON_RETRIEVE_NAME).put(
		    associatedExpression, new RewriteInfo(expr, replacement, saveSubstitutedSignature));

	}

	return o;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingASTAdapter#visitAssignExpression(odra.sbql.ast.expressions.AssignExpression,
     *      java.lang.Object)
     */
    @Override
    public Object visitAssignExpression(AssignExpression expr, Object attr)
	    throws SBQLException {
	Object o = super.visitAssignExpression(expr, attr);
	if (isVirtualReferenceSignature(expr.getLeftExpression().getSignature())) {
	    ReferenceSignature virtSig = (ReferenceSignature) expr
		    .getLeftExpression().getSignature();
	    Expression replacement;
	    try {
		replacement = this
			.getReplacerWithParamNoReturn(
				new MBProcedure(
					new MBView(virtSig.value)
						.getGenericProc(OdraViewSchema.GenericNames.ON_UPDATE_NAME
							.toString())), expr
					.getRightExpression());
	    } catch (DatabaseException e) {
		throw new OptimizationException(e, expr, this);
	    }
	    NameExpression associatedExpression = this
		    .findAssociatedNameExpression(virtSig);
	    assert this.operatorsRewrite.get(GenericNames.ON_UPDATE_NAME).get(
		    associatedExpression) == null : "map cannot contains this key "
		    + associatedExpression;
	    this.operatorsRewrite.get(GenericNames.ON_UPDATE_NAME).put(
		    associatedExpression, new RewriteInfo(expr, replacement, saveSubstitutedSignature));
	}

	return o;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingASTAdapter#visitDeleteExpression(odra.sbql.ast.expressions.DeleteExpression,
     *      java.lang.Object)
     */
    @Override
    public Object visitDeleteExpression(DeleteExpression expr, Object attr)
	    throws SBQLException {
	Object o = super.visitDeleteExpression(expr, attr);
	if (isVirtualReferenceSignature(expr.getExpression().getSignature())) {
	    ReferenceSignature virtSig = (ReferenceSignature) expr
		    .getExpression().getSignature();
	    try {
		Expression replacement = this
			.getReplacerNoParamNoReturn((new MBProcedure(
				new MBView(virtSig.value)
					.getGenericProc(OdraViewSchema.GenericNames.ON_DELETE_NAME
						.toString()))));
		NameExpression associatedExpression = this
			.findAssociatedNameExpression(virtSig);
		assert this.operatorsRewrite.get(GenericNames.ON_DELETE_NAME)
			.get(associatedExpression) == null : "map cannot contains this key "
			+ associatedExpression;
		this.operatorsRewrite.get(GenericNames.ON_DELETE_NAME).put(
			associatedExpression,
			new RewriteInfo(expr, replacement, saveSubstitutedSignature));

	    } catch (DatabaseException e) {
		throw new OptimizationException(e, expr, this);
	    }
	}
	return o;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
     */
    @Override
    public Object visitCreateExpression(CreateExpression expr, Object attr)
	    throws SBQLException {	
	Object o = super.visitCreateExpression(expr, attr);
	if(this.isVirtualReferenceSignature(expr.getSignature())){
	    ReferenceSignature virtSig = (ReferenceSignature) expr.getSignature();
	    Expression replacement;
	    try {
		replacement = this
			.getReplacerWithParamNoReturn(
				new MBProcedure(
					new MBView(virtSig.value)
						.getGenericProc(OdraViewSchema.GenericNames.ON_NEW_NAME
							.toString())), expr.getExpression());
	    } catch (DatabaseException e) {
		throw new OptimizationException(e, expr, this);
	    } 
	    
	    
	    this.on_new.add(new RewriteInfo(expr, replacement, saveSubstitutedSignature));
	}
	
	return o;
    }

    private boolean isVirtualReferenceSignature(Signature sign) {
	return (sign instanceof ReferenceSignature && ((ReferenceSignature) sign).isVirtual());
    }

    /**
     * Check if the calledView is a sub view of the generatorView if not it
     * should be connected with on_navigate evaluation
     * 
     * @param calledView
     * @param generatorView
     * @return
     */
    private boolean isSubView(OID calledView, OID generatorView)
	    throws DatabaseException {
	MBView view = new MBView(generatorView);
	if (!view.isValid())
	    return false;
	for (OID sview : view.getSubViewsEntry().derefComplex()) {
	    if (calledView.equals(sview))
		return true;
	}
	assert view.getGenericProc(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME
		.toString()) != null : "there is no on_navigate and the view is not a subview: "
		+ calledView.getObjectName()
		+ " -> "
		+ generatorView.getObjectName();
	return false;
    }

    private NameExpression getSuperViewGeneratorExpression(NameExpression expr) {
	NameExpression associated = expr.getAssociated();

	while (associated != null) {
	    if (associated.isAuxiliaryName())
		associated = associated.getAssociated();
	    else
		break;
	}
	return associated;
    }

    /**
     * serch the name expression that bound this signature
     * 
     * @param sign
     * @return
     */
    private NameExpression findAssociatedNameExpression(
	    final ReferenceSignature sign) {

	ASTNodeFinder finder = new ResultPathAwareASTNodeFinder(new Pattern() {
	    public boolean matches(Object obj) {
		if (obj instanceof NameExpression) {
		    // if(((NameExpression)obj).isAuxiliaryName()){
		    // ((NameExpression)obj).getAssociated();
		    // }
		    if (((NameExpression) obj).getSignature() instanceof ReferenceSignature
			    && ((ReferenceSignature) ((NameExpression) obj)
				    .getSignature()).value.equals(sign.value))
			return true;
		}
		return false;
	    }
	}, true);
	Vector<ASTNode> nodes = finder.findNodes(sign.getOwnerExpression());
	if (nodes.size() == 1) {
	    NameExpression ne = (NameExpression) nodes.get(0);
	    while (ne.isAuxiliaryName()) {
		ne = ne.getAssociated();
		assert ne != null : "associated must not be null if source is auxiliary";
	    }
	    return ne;
	} else
	    throw new OptimizationException("to many nodes: " + nodes.size()
		    + " for " + sign.getOwnerExpression(), sign
		    .getOwnerExpression(), this);

    }

    private Expression getReplacerNoParamNeedReturn(MBProcedure proc) {
	return getReplacer(proc, new EmptyExpression(), true);
    }

    private Expression getReplacerNoParamNoReturn(MBProcedure proc) {
	return getReplacer(proc, new EmptyExpression(), false);
    }

    private Expression getReplacerWithParamNoReturn(MBProcedure proc,
	    Expression params) {
	return getReplacer(proc, params, false);
    }

    private Expression getReplacer(MBProcedure proc, Expression params,
	    boolean needReturn) {
	return new ProcedureCallReplacer().getReplacementExpression(this
		.getSourceModuleName(), proc, params, needReturn);
    }
}
