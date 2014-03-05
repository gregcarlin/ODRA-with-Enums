/**
 * 
 */
package odra.db.objects.meta;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.IMetaBaseHolder;
import odra.db.schema.OdraClassSchema;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.sbql.builder.OrganizerException;
import odra.system.config.ConfigDebug;

/**
 * MetabaseManager manages metabase objects
 * 
 * @author Radek Adamus
 * @since 2008-04-29 last modified: 2008-04-29
 * @version 1.0
 */
public class MetabaseManager {

    private MetaBase metabase;

    private boolean breakOnConflicts;

    public OID getMetaBaseEntry() throws SchemaException {
	try {
	    return metabase.getMetabaseEntry();
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }
    public MetabaseManager(IMetaBaseHolder mbholder)throws SchemaException {
	this(mbholder,true);
    }
    /**
     * @param metabase
     * @param breakOnConflicts
     */
    public MetabaseManager(IMetaBaseHolder mbholder, boolean breakOnConflicts)
	    throws SchemaException {
	try {
	    this.metabase = mbholder.getMetaBase();
	} catch (DatabaseException e) {
	    throw exception(e);
	}
	this.breakOnConflicts = breakOnConflicts;

    }

    /**
     * @param metabase
     * @param breakOnConflicts
     */
    MetabaseManager(MetaBase metabase, boolean breakOnConflicts) {
	this.metabase = metabase;
	this.breakOnConflicts = breakOnConflicts;
    }

    /**
     * @param metaBase
     */
    public MetabaseManager(MetaBase metaBase) {
	this.metabase = metaBase;
    }

    /**
     * @param pname
     */
    public void deleteMetabaseRootObject(String name) throws SchemaException {
	try {
	    deleteMetabaseObject(metabase.getMetabaseEntry(), name);
	} catch (DatabaseException e) {
	    throw exception(e);
	}

    }

    /**
     * @param pname
     */
    public void deleteMetabaseObject(OID parent, String name)
	    throws SchemaException {
	OID exmtoid;

	exmtoid = findMetaObjectByName(name, parent);

	if (exmtoid == null)
	    return;

	deleteMetabaseObject(new MBObject(exmtoid));

    }

    /**
     * Deletes an object representing a declaration from the metabase. If an
     * object has a declaration in the metabase, it is possible to find out if
     * and what additional objects from the database should also be deleted. For
     * example, if we delete a variable then (apart from the metabase record and
     * the database corresponding object) it may be necessary to delete an
     * object representing an anonymous structure declaration.
     * 
     * @param mbo
     *                object declaration.
     */
    public void deleteMetabaseObject(MBObject mbo) throws SchemaException {
	try {
	    switch (mbo.getObjectKind()) {
	    case VARIABLE_OBJECT:
		MBVariable mv = new MBVariable(mbo.getOID());
		deleteMetaVariable(mv);
		break;

	    case PROCEDURE_OBJECT:
		MBProcedure mp = new MBProcedure(mbo.getOID());
		deleteMetaProcedure(mp);
		break;

	    case TYPEDEF_OBJECT:
		MBTypeDef mt = new MBTypeDef(mbo.getOID());
		deleteMetaTypeDef(mt);
		break;
	    case VIEW_OBJECT:
		MBView mvw = new MBView(mbo.getOID());
		deleteMetaView(mvw);
		break;
	    case VIRTUAL_VARIABLE_OBJECT:
		MBVirtualVariable mvo = new MBVirtualVariable(mbo.getOID());
		// it means delete view definition
		deleteMetaView(new MBView(mvo.getView()));
		break;
	    case CLASS_OBJECT:
		MBClass mbc = new MBClass(mbo.getOID());
		deleteMetaClass(mbc);
		break;

	    default:
		mbo.getOID().delete();
	    }
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    /**
     * Deletes a variable's representation from the metabase. If the type is a
     * an anonymous structure, it is also deleted.
     * 
     * @param var
     *                the variable being deleted
     */
    public void deleteMetaVariable(MBVariable var) throws SchemaException {
	try {
	    deleteTypeIfAnonStruct(getVariableTypeName(var));

	    var.getOID().delete();
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    /**
     * Deletes a metabase record representing a procedure declaration. If the
     * resulting type or the type of a parameter is an anonymous structure, its
     * object is deleted too.
     * 
     * @param procedure
     *                object declaration.
     */
    void deleteMetaProcedure(MBProcedure proc) throws DatabaseException {
	deleteTypeIfAnonStruct(getProcedureTypeName(proc));

	for (OID oid : proc.getArguments())
	    deleteMetaVariable(new MBVariable(oid));
	for (OID oid : proc.getLocalBlocksEntries()) {
	    for (OID mvoid : oid.derefComplex()) {
		deleteMetaVariable(new MBVariable(mvoid));
	    }
	}
	proc.getOID().delete();
    }

    /**
     * Change the procedure/method body the signature remains unchanged
     * 
     * @param mbproc -
     *                meta-prcedure to change
     * @param procInfo -
     *                schema procedure information (only the ast and locals are
     *                used)
     * @throws OrganizerException
     */
    public void alterProcedureBody(OID mbprocid,
	    OdraProcedureSchema procInfo) throws OrganizerException {
	try {
		MBProcedure mbproc = new MBProcedure(mbprocid);
		if(!mbproc.isValid())
			throw new OrganizerException("'"+ mbprocid.getObjectName() + "' is not a procedure");
	    createProcedureLocalMetadata(mbproc, procInfo.getLocals(),
		    procInfo.getExceptions());
	    mbproc.setAST(procInfo.getAstAsBytes());
	    
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
    }
    /**
     * Deletes a metabase record representing a type definition. If the base
     * type is an anonymous structure, its object is deleted too.
     * 
     * @param type
     *                object representing the type.
     */
    private void deleteMetaTypeDef(MBTypeDef type) throws DatabaseException {

	deleteTypeIfAnonStruct(getTypeDefTypeName(type));

	type.getOID().delete();
    }

    /**
     * @param view
     * @throws DatabaseException
     */
    private void deleteMetaView(MBView view) throws DatabaseException {

	// delete virtual variable
	MBVirtualVariable voproc = new MBVirtualVariable(view
		.getVirtualObject());
	this.deleteMetaVirtualVariable(voproc);

	// delete seed procedure
	this.deleteMetaProcedure(new MBProcedure(view.getSeedProc()));

	for (OID procoid : view.getGenProcs()) {
	    MBProcedure genPrc = new MBProcedure(procoid);
	    this.deleteMetaProcedure(genPrc);
	}
	// delete subviews
	for (OID subviewid : view.getSubViewsEntry().derefComplex()) {
	    this.deleteMetabaseObject(new MBObject(subviewid));
	}
	/*
	 * virtual objects are deleted with sub views
	 */
	for (OID fieldid : view.getViewFieldsEntry().derefComplex()) {
	    this.deleteMetabaseObject(new MBObject(fieldid));
	}

	view.getOID().delete();

    }

    /**
     * @param vo
     */
    void deleteMetaVirtualVariable(MBVirtualVariable vo)
	    throws DatabaseException {
	this.deleteMetaVariable(vo);
    }

    /**
     * @param mbc
     * @throws DatabaseException
     */
    void deleteMetaClass(MBClass mbc) throws DatabaseException {
	/*
	 * delete type object
	 */
	deleteTypeIfAnonStruct(this.getClassTypeName(mbc));
	/*
	 * delete methods
	 */
	for (OID methodid : mbc.getMethods()) {
	    this.deleteMetabaseObject(new MBObject(methodid));
	}
	mbc.getOID().delete();
    }

    /**
     * Returns the name of a procedure result type.
     * 
     * @param proc
     *                the procedure
     * @return the name of the type
     */
    public String getProcedureTypeName(MBProcedure proc) throws DatabaseException {
	return metabase.getMetaReferenceAt(proc.getTypeNameId()).derefString();
    }

    /**
     * Returns the name of a class instance type.
     * 
     * @param proc
     *                the procedure
     * @return the name of the type
     */
    String getClassTypeName(MBClass cls) throws DatabaseException {
	return metabase.getMetaReferenceAt(cls.getClassTypeNameId())
		.derefString();
    }

    /**
     * Checks if the type of the name given as a parameters represents an
     * anonymous structure. If yes, it is deleted from the metabase. Because its
     * fields may be themselves of types being anonymous structures, they are
     * analyzed recursively.
     * 
     * @param structname
     *                name of the type
     */
    public void deleteTypeIfAnonStruct(String structname)
	    throws DatabaseException {
	/*
	 * anonymous structures have names starting with the character $
	 */
	if (structname.startsWith("$")) {
	    OID mtvartype = findRootMetaObjectByName(structname);

	    if (ConfigDebug.ASSERTS)
		assert mtvartype != null : "expected type " + structname;

	    // delete fields one by one
	    OID[] fields = new MBStruct(mtvartype).getFields();

	    for (OID fld : fields)
		deleteMetaVariable(new MBVariable(fld));

	    // delete the struct
	    mtvartype.delete();

	    // delete the name of the struct from the list of logical references
	    metabase.removeMetaReference(structname);
	}
    }

    public void deleteMetaBaseContent() throws DatabaseException {
	this.metabase.deleteMetaBaseContent();
    }
    /**
     * Returns the name of the type definition base type.
     * 
     * @param type
     *                the type definition
     * @return the name of the base type
     */
    public String getTypeDefTypeName(MBTypeDef type) throws DatabaseException {
	return metabase.getMetaReferenceAt(type.getTypeNameId()).derefString();
    }

    /**
     * Returns the name of a variable type.
     * 
     * @param var
     *                the variable
     * @return the name of the type
     */
    public String getVariableTypeName(MBVariable var) throws DatabaseException {
	return metabase.getMetaReferenceAt(var.getTypeNameId()).derefString();
    }

    public OID findMetaObjectByName(String name, OID parent)
	    throws SchemaException {
	try {
	    return metabase.findFirstByName(name, parent);
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    public OID findRootMetaObjectByName(String name) throws SchemaException {
	try {
	    return metabase.findFirstByName(name, metabase.getMetabaseEntry());
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    /**
     * @param viewName
     * @param virtualObjectName
     * @param virtualObjectTypeName
     * @param mincard
     * @param maxcard
     * @param refs
     * @param typeName
     * @param astAsBytes
     * @return
     */
    public OID createMetaView(OdraViewSchema svi) throws SchemaException {
	try {
	    OdraProcedureSchema seed = svi.getSeed();
	    OdraVariableSchema virtObj = svi.getVirtualObject();
	    MBView metaView = new MBView(metabase.createMetaView(svi
		    .getViewName(), svi.getVirtualObjectName(), svi
		    .getVirtualObjectTypeName(), virtObj.getMinCard(), virtObj
		    .getMaxCard(), virtObj.getRefLevel(), seed.getTypeName(),
		    seed.getAstAsBytes()));

	    createViewElements(metaView, svi);

	    return metaView.getOID();
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    /**
     * @param metaView
     * @param svi
     * @throws DatabaseException
     */
    private void createViewElements(MBView metaView, OdraViewSchema svi)
	    throws DatabaseException {
	for (OdraProcedureSchema genericProc : svi.getGenericProcedures()) {
	    createGenericViewOperator(metaView, genericProc);
	}

	for (OdraVariableSchema varinfo : svi.geVariables()) {
	    createViewVariables(metaView, varinfo);
	}
	for (OdraViewSchema subView : svi.getSubViews()) {
	    createSubView(metaView, subView);
	}

    }

    /**
     * @param metaView
     * @param viewInfo
     * @throws DatabaseException
     */
    private void createSubView(MBView metaView, OdraViewSchema viewInfo)
	    throws DatabaseException {
	OdraProcedureSchema seedProc = viewInfo.getSeed();
	MBView mbsview = new MBView(metaView.createSubView(viewInfo
		.getViewName(), viewInfo.getVirtualObjectName(), viewInfo
		.getVirtualObjectTypeName(), seedProc.getMincard(), seedProc
		.getMaxcard(), seedProc.getRefs(), seedProc.getTypeName(),
		seedProc.getAstAsBytes()));
	createViewElements(mbsview, viewInfo);

    }

    /**
     * @param metaView
     * @param varinfo
     * @throws DatabaseException
     */
    private void createViewVariables(MBView metaView, OdraVariableSchema varinfo)
	    throws DatabaseException {
	metaView
		.createVariableField(varinfo.getName(), varinfo.getMinCard(),
			varinfo.getMaxCard(), varinfo.getTName(), varinfo
				.getRefLevel());

    }

    /**
     * @param genericProc
     * @throws DatabaseException
     */
    private void createGenericViewOperator(MBView mbview,
	    OdraProcedureSchema genericProc) throws DatabaseException {
	MBProcedure mbproc = new MBProcedure(mbview.createGenericProcedure(
		genericProc.getPname(), genericProc.getMincard(), genericProc
			.getMaxcard(), genericProc.getTypeName(), genericProc
			.getRefs(), genericProc.getArgs().length, genericProc
			.getAstAsBytes()));

	for (ProcArgument arg : genericProc.getArgs())
	    mbproc.addArgument(arg.getName(), arg.getTypeName(), arg
		    .getMinCard(), arg.getMaxCard(), arg.getRefs());
	createProcedureLocalMetadata(mbproc, genericProc.getLocals(),
		genericProc.getExceptions());

    }

    private void createProcedureLocalMetadata(MBProcedure mbproc,
	    Map<String, Vector<OdraVariableSchema>> locals,
	    Map<String, OdraVariableSchema> exceptions) throws SchemaException {
	/*
	 * first delete old (if exists)
	 */
	try {
	    for (OID block : mbproc.getLocalBlocksEntries()) {
		for (OID locVarId : block.derefComplex()) {
		    deleteMetaVariable(new MBVariable(locVarId));
		}
		block.delete();
	    }
	    for (OID block : mbproc.getExceptionsBlocksEntries()) {
		for (OID locVarId : block.derefComplex()) {
		    deleteMetaVariable(new MBVariable(locVarId));
		}
		block.delete();
	    }

	    for (Iterator<String> es = locals.keySet().iterator(); es.hasNext();) {
		String blockName = es.next();

		Vector<OdraVariableSchema> variables = locals.get(blockName);

		mbproc.addLocalBlock(blockName);
		for (OdraVariableSchema svi : variables) {
		    mbproc.addLocalVariable(blockName, svi.getName(), svi
			    .getTName(), svi.getMinCard(), svi.getMaxCard(),
			    svi.getRefLevel());
		}

	    }
	    for (Iterator<String> es = exceptions.keySet().iterator(); es
		    .hasNext();) {
		String blockName = es.next();
		OdraVariableSchema exceptionVar = exceptions.get(blockName);
		mbproc.addCatchBlock(blockName);
		mbproc.addCatchedExceptionVariable(blockName, exceptionVar
			.getName(), exceptionVar.getTName(), exceptionVar
			.getMinCard(), exceptionVar.getMaxCard(), exceptionVar
			.getRefLevel());
	    }

	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    /**
     * @param name
     * @param instanceName
     * @param typeName
     * @param superclasses
     * @return
     */
    public OID createMetaClass(OdraClassSchema sci) throws SchemaException {
	MBClass mbclass;
	String[] superclasses = sci.getSuperClassesNames();
	try {
	    if (sci.getInstanceName().compareTo(
		    OdraClassSchema.NO_INVARIANT_NAME) == 0) {
		mbclass = new MBClass(metabase.createMetaClass(sci.getName(),
			sci.getTypeName(), superclasses));
	    } else {
		mbclass = new MBClass(metabase.createMetaClass(sci.getName(),
			sci.getInstanceName(), sci.getTypeName(), superclasses));
	    }

	    for (OdraProcedureSchema methinfo : sci.getMethods()) {
		this.createMetaMethod(mbclass, methinfo);
	    }
	} catch (DatabaseException e) {
	    throw exception(e);
	}
	return mbclass.getOID();
    }

    private OID createMetaMethod(MBClass mbclass, OdraProcedureSchema methinfo)
	    throws DatabaseException {

	MBProcedure mbproc = new MBProcedure(mbclass.createMethod(methinfo
		.getPname(), methinfo.getMincard(), methinfo.getMaxcard(),
		methinfo.getTypeName(), methinfo.getRefs(),
		methinfo.getArgs().length, methinfo.getAstAsBytes()));

	for (ProcArgument arg : methinfo.getArgs())
	    mbproc.addArgument(arg.getName(), arg.getTypeName(), arg
		    .getMinCard(), arg.getMaxCard(), arg.getRefs());
	createProcedureLocalMetadata(mbproc, methinfo.getLocals(), methinfo
		.getExceptions());
	return mbproc.getOID();
    }

    public OID createMetaProcedure(OdraProcedureSchema pi)
	    throws SchemaException {
	try {
	    MBProcedure mbproc = new MBProcedure(metabase.createMetaProcedure(
		    pi.getPname(), pi.getMincard(), pi.getMaxcard(), pi
			    .getTypeName(), pi.getRefs(), 0,
		    pi.getAstAsBytes(), pi.getASTTransactionCapabilities()));
	    for (ProcArgument arg : pi.getArgs()) {
		mbproc.addArgument(arg.getName(), arg.getTypeName(), arg
			.getMinCard(), arg.getMaxCard(), arg.getRefs());
	    }
	    createProcedureLocalMetadata(mbproc, pi.getLocals(), pi
		    .getExceptions());
	    return mbproc.getOID();
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    public OID createMetaVariable(OdraVariableSchema svi)
	    throws SchemaException {
	try {
	    return metabase.createMetaVariable(svi.getName(), svi.getMinCard(),
		    svi.getMaxCard(), svi.getTName(), svi.getRefLevel());
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    public OID createMetaAnnotatedVariable(OdraVariableSchema svi)
	    throws SchemaException {
	try {
	    return metabase.createMetaAnnotatedVariable(svi.getName(), svi.getMinCard(),
		    svi.getMaxCard(), svi.getTName(), svi.getRefLevel());
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }

    /**
     * @param name
     * @param tname
     */
    public OID createMetaTypeDef(String name, String typename,
	    boolean isDistinct) throws SchemaException {
	try {
	    return metabase.createMetaTypeDef(name, typename, isDistinct);
	} catch (DatabaseException e) {
	    throw exception(e);
	}

    }
    
    /**
     * @param name
     * @param tname
     */
    public OID createMetaTypeDef(String name, String typename) throws SchemaException {

	    return this.createMetaTypeDef(name, typename, false);

    }
    
    public OID createMetaExternalSchemaDef(String name) {
    	try {
    	    return metabase.createExternalSchemaDef(name);
    	} catch (DatabaseException e) {
    	    throw exception(e);
    	}	
	}
    
    /**
     * @param i
     * @return
     */
    public OID createMetaStruct(int i) throws SchemaException {
	try {
	    return metabase.createMetaStruct(i);
	} catch (DatabaseException e) {
	    throw exception(e);
	}
    }
    
    public OID createMetaEnum(String name, String typename,int i) throws SchemaException {
    	try {
    	    return metabase.createMetaEnum(name,typename,i);
    	} catch (DatabaseException e) {
    	    throw exception(e);
    	}
    }

    private final SchemaException exception(DatabaseException e) {
	return new SchemaException(e);
    }

    private final SchemaException exception(String message) {
	return new SchemaException("message");
    }
	
    
}
