package odra.sbql.builder;

import java.util.Hashtable;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.EnumDeclaration;
import odra.sbql.ast.declarations.EnumFieldDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefFieldDeclaration;
import odra.sbql.ast.declarations.InterfaceFieldDeclaration;
import odra.sbql.ast.declarations.ModuleDeclaration;
import odra.sbql.ast.declarations.NamedSingleImportDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.ProcedureFieldDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.ReverseVariableDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.SingleArgumentDeclaration;
import odra.sbql.ast.declarations.SingleEnumeratorDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.SingleImplementDeclaration;
import odra.sbql.ast.declarations.SingleImportDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
import odra.sbql.builder.classes.ClassConstructor;
import odra.sbql.builder.interfaces.InterfaceConstructor;
import odra.sbql.builder.procedures.ProcedureLocalEnvironmentConstructor;
import odra.sbql.builder.views.ViewConstructor;
import odra.transactions.ast.IASTTransactionCapabilities;
import odra.transactions.ast.ITransactionASTVisitor;
import odra.transactions.ast.TransactionASTVisitor;

/**
 * This class is responsible for the creation of database modules according to
 * their source code sent by the user from the client side. The class only
 * analyses the source code, while an additional class (ModuleManager) is
 * accountable for the process of schema management.
 * 
 * Modules can be created in the following modes:
 * <ul>
 * <li>break on conflict - if a conflict between the database and the new
 * source code occurs, the process of module creation is interrupted.</li>
 * <li>alter on conflict - if such a conflict occurs, the conflicting database
 * objects are deleted and recreated (default).</li>
 * <li>drop unlisted - fields not listed in the source code but existing in the
 * database are deleted (default).</li>
 * <li>leave unlisted - such fields are deleted from the database.</li>
 * </ul>
 * 
 * If there is no conflict between a field in the source code and its database
 * representation, the corresponding database objects are left intact.
 * 
 * @author raist, radamus
 */

// TODO : make sure there are no fields with the same names
public class ModuleConstructor extends ASTAdapter {

    private DBModule pntmod; // parent module

    private DBModule constructedModule; // our constructed module

    private ModuleOrganizer cruder;

    private boolean breakOnConflicts = true;

    private boolean dropUnlisted = true;

    /**
     * Initializes the module constructor.
     * 
     * @param mod
     *                parent module where the new module is being created
     */
    public ModuleConstructor(DBModule mod) {
	pntmod = mod;
    }

    /**
     * Initializes the module constructor.
     * 
     * @param mod
     *                parent module where the new module is being created
     * @param conf
     *                indicates what should happen on conflicts between the
     *                source code and database objects
     * @param unus
     *                indicates what should happen with database objects when
     *                they are not listed in the source code
     */
    public ModuleConstructor(DBModule mod, boolean conf, boolean unus) {
	pntmod = mod;
	breakOnConflicts = conf;
	dropUnlisted = unus;
    }

    public DBModule getConstructedModule() {
	return constructedModule;
    }

    /**
     * set constructed module in case we use the module constructor to
     * reconstruct existing module
     * 
     * @param mod
     */
    public void setConstructedModule(DBModule mod) {
	constructedModule = mod;
	breakOnConflicts = true;
	cruder = new ModuleOrganizer(constructedModule, breakOnConflicts);
    }

    public Object visitModuleDeclaration(ModuleDeclaration decl, Object attr)
	    throws SBQLException {
	// gather all the information necessary to create a new module
	String name = decl.getName();
	SingleFieldDeclaration[] fields = decl.getModuleBody()
		.getFieldDeclaration();
	SingleImportDeclaration[] imports = decl.getModuleBody()
		.getImportDeclaration();
	SingleImplementDeclaration[] impls = decl.getModuleBody()
		.getImplementDeclaration();

	/*
	 * check if the module already exists. if not create it --- if it does
	 * --- don't touch it
	 */
	try {
	    OID modid = pntmod.getSubmodule(name);

	    if (modid == null) {
		constructedModule = new DBModule(pntmod.createSubmodule(name));
	    } else {
		constructedModule = new DBModule(modid);
	    }

	    // invalidate the module
	    constructedModule.setModuleCompiled(false);
	    constructedModule.setModuleLinked(false);

	    // create a schema manager object for our new (or old) module
	    cruder = new ModuleOrganizer(constructedModule, breakOnConflicts);

	    // delete module fields and import list elements that are not
	    // listed in the source code being processed. database objects
	    // are deleted only then, when they have a metabase declaration.

	    if (dropUnlisted) {
		constructedModule.removeImports();

		OID[] exmtoids = constructedModule.getMetabaseEntry()
			.derefComplex();
		OID[] exrtoids = constructedModule.getDatabaseEntry()
			.derefComplex();
		Hashtable<String, OID> removed = new Hashtable<String, OID>();
		for (OID oid : exmtoids) {
		    boolean found = false;
		    if (oid.getObjectName().startsWith("$")
			    || removed.contains(oid))
			continue;
		    MBObject mbo = new MBObject(oid);

		    for (SingleFieldDeclaration f : fields) {
			if (f.getName().equals(mbo.getName())) {
			    found = true;
			    break;
			}
		    }
		    if (!found) {

			switch (mbo.getObjectKind()) {
			case VIRTUAL_VARIABLE_OBJECT:
			    break;
			case VIEW_OBJECT:
			    String vobName = new MBView(oid).getVirtualObject()
				    .getObjectName();
			    removed.put(vobName, new MBView(oid)
				    .getVirtualObject());
			    cruder.deleteMetabaseObject(mbo);
			    cruder.deleteDatabaseView(vobName);
			    break;

			default:
			    cruder.deleteDatabaseObject(mbo.getName());
			    cruder.deleteMetabaseObject(mbo);
			    break;
			}
		    }
		}
	    }

	    // set the module's import list and it's interface list
	    for (SingleImportDeclaration i : imports) {
		if (i instanceof NamedSingleImportDeclaration)
		    constructedModule.addImport(i.N.nameAsString(),
			    ((NamedSingleImportDeclaration) i).getAlias());
		else
		    constructedModule.addImport(i.N.nameAsString());
	    }

	    for (SingleImplementDeclaration j : impls)
		constructedModule.addImplement(j.getName());

	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

	/*
	 * create objects representing module's (members) fields --- i.e.
	 * attribute (variable) fields, procedures, and type definitions
	 */
	for (SingleFieldDeclaration f : fields) {
	    f.accept(this, null);
	}

	return null;
    }

    /**
     * Called when there is a global variable field in the module source code.
     */
    public Object visitVariableFieldDeclaration(VariableFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getVariableDeclaration().accept(this, null);

	return null;
    }

    /**
     * Called when there is a global procedure field in the module source code.
     */
    public Object visitProcedureFieldDeclaration(
	    ProcedureFieldDeclaration decl, Object attr) throws SBQLException {
	decl.getProcedureDeclaration().accept(this, null);

	return null;
    }

    /**
     * Called when there is a type definition field in the module source code.
     */
    public Object visitTypeDefFieldDeclaration(TypeDefFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getTypeDefDeclaration().accept(this, null);

	return null;
    }

	@Override
	public Object visitExternalSchemaDefFieldDeclaration(
			ExternalSchemaDefFieldDeclaration decl, Object attr)
			throws SBQLException {
		decl.getDeclaration().accept(this, attr);
		
		return null;
	}

	/**
     * Called when a global variable must be declared. Variable declaration
     * consists in entering a corresponding record in the metabase. Before we do
     * that, we need to know the name of the variable's type. If the type is an
     * anonymous structure, it is created in the metabase. If it is a simple
     * name, we simply use that name.
     */
    public Object visitVariableDeclaration(VariableDeclaration decl, Object attr)
	    throws SBQLException {
	decl.getType().accept(this, null);

	cruder.createVariable(getSchemaVariableInfor(decl));

	return null;
    }

    /**
     * @param decl
     * @return
     */
    private OdraVariableSchema getSchemaVariableInfor(VariableDeclaration decl) {
	return new OdraVariableSchema(decl.getName(), decl.getType()
		.getTypeName(), decl.getCardinality().getMinCard(), decl
		.getCardinality().getMaxCard(), decl.getReflevel());
    }

    /**
     * Called when a session variable must be declared. Variable declaration
     * consists in entering a corresponding record in the metabase. Before we do
     * that, we need to know the name of the variable's type. If the type is an
     * anonymous structure, it is created in the metabase. If it is a simple
     * name, we simply use that name.
     */
    @Override
    public Object visitSessionVariableFieldDeclaration(
	    SessionVariableFieldDeclaration decl, Object attr)
	    throws SBQLException {
	VariableDeclaration svdecl = decl.getVariableDeclaration();
	svdecl.getType().accept(this, null);
	cruder.createSessionVariable(svdecl.getName(), svdecl.getType()
		.getTypeName(), svdecl.getCardinality().getMinCard(), svdecl
		.getCardinality().getMaxCard(), svdecl.getReflevel(), svdecl
		.getInitExpression());
	return null;
    }

    /**
     * Called when a global procedure must be created.
     */
    public Object visitProcedureDeclaration(ProcedureDeclaration decl,
	    Object attr) throws SBQLException {

	OdraProcedureSchema spi = this.createSchemaProcedureInfo(decl);
	cruder.createProcedure(spi);
	return null;
    }

    /**
     * Called when there is a type definition in the module source code. To
     * complete it, we must first obtain the name of the base type. If the base
     * type is an anonymous structure then it is also created in the metabase.
     */
    public Object visitTypeDefDeclaration(TypeDefDeclaration decl, Object attr)
	    throws SBQLException {
	decl.getType().accept(this, null);

	cruder.createTypeDef(decl.getName(), decl.getType().getTypeName(), decl
		.isDistinct());
	return null;
    }

    
    @Override
	public Object visitExternalSchemaDefDeclaration(
			ExternalSchemaDefDeclaration decl, Object attr)
			throws SBQLException {

    	cruder.createExternalSchamaDef(decl.getName());
		return null;
	}

    
    /**
     * When a type is an anonymous structure, we create an MBStruct object in
     * the metabase with a random name, inaccesible to the end user.
     */
    public Object visitRecordTypeDeclaration(RecordTypeDeclaration decl,
	    Object attr) throws SBQLException {
	try {
	    MBStruct mbrec = new MBStruct(new MetabaseManager(
		    constructedModule, breakOnConflicts).createMetaStruct(0));

	    for (SingleFieldDeclaration f : decl.getRecordTypeFields()) {
		VariableDeclaration vd = ((VariableFieldDeclaration) f)
			.getVariableDeclaration();

		vd.getType().accept(this, null);
		if (vd instanceof ReverseVariableDeclaration) {
		    mbrec.createBinaryAssociationField(vd.getName(), vd
			    .getCardinality().getMinCard(), vd.getCardinality()
			    .getMaxCard(), vd.getType().getTypeName(), vd
			    .getReflevel(), ((ReverseVariableDeclaration) vd)
			    .getReverseName());
		} else {
		    mbrec.createField(vd.getName(), vd.getCardinality()
			    .getMinCard(), vd.getCardinality().getMaxCard(), vd
			    .getType().getTypeName(), vd.getReflevel());
		}

	    }

	    decl.setTypeName(mbrec.getName());
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
	return null;
    }

    /**
     * Called when a type is a simple name (that is it not an anonymous
     * structure).
     */
    public Object visitNamedTypeDeclaration(NamedTypeDeclaration decl,
	    Object attr) throws SBQLException {
	decl.setTypeName(decl.getName().nameAsString());
	return null;
    }

    /**
     * Called when there is a view definition field in the module source code.
     */
    @Override
    public Object visitViewFieldDeclaration(ViewFieldDeclaration node,
	    Object attr) throws SBQLException {

	// SchemaViewInfo viewInfo = new SchemaViewInfo();
	// node.vb.accept(this, viewInfo);
	// cruder.createView(viewInfo);
	OdraViewSchema viewInfo = (OdraViewSchema) node.getViewDeclaration()
		.accept(new ViewConstructor(this), attr);
	cruder.createView(viewInfo);
	return null;

    }

    /**
     * Converts ProcedureDeclaration (AST procedure declaration representation)
     * into SchemaProcedureInfo (a DTO like object) to feed Organizers (schema
     * managers)
     * 
     * @param decl -
     *                procedure declaration
     * @return SchemaProcedureInfo object
     * @throws Exception
     */
    public OdraProcedureSchema createSchemaProcedureInfo(
	    ProcedureDeclaration decl) {
	OdraProcedureSchema spinfo = this.getSchemaProcedureInfo(decl);
	return spinfo;
    }

    private OdraProcedureSchema getSchemaProcedureInfo(
	    ProcedureDeclaration declProc) {

	SingleArgumentDeclaration[] argsdecl = declProc.getProcedureArguments();
	ProcArgument[] args = new ProcArgument[argsdecl.length];
	for (int i = 0; i < args.length; i++) {
	    SingleArgumentDeclaration arg = argsdecl[i];
	    // analyze the argument type
	    arg.D.getType().accept(this, null);

	    args[i] = new ProcArgument(arg.D.getName(), arg.D.getType()
		    .getTypeName(), arg.D.getCardinality().getMinCard(), arg.D
		    .getCardinality().getMaxCard(), arg.D.getReflevel());

	}

	// analyze the result type
	declProc.getProcedureResult().getResultType().accept(this, null);

	OdraTypeSchema res = new OdraTypeSchema(declProc.getProcedureResult()
		.getResultType().getTypeName(), declProc.getProcedureResult()
		.getResultMinCard(), declProc.getProcedureResult()
		.getResultMaxCard(), declProc.getProcedureResult()
		.getReflevel());
	// TODO: REF INDICATOR

	/**
	 * convey transaction capabilities down the abstract syntax subtree
	 * representation of the declared procedure
	 */
	conveyTransactionCapabilitiesThroughoutAllStatments(declProc);

	IASTTransactionCapabilities capsTransaction = getTransactionCapabilities(declProc);
	OdraProcedureSchema spinfo = new OdraProcedureSchema(
		declProc.getName(), capsTransaction);
	spinfo.setArgs(args);
	spinfo.setResult(res);
	ProcedureLocalEnvironmentConstructor envConstructor = new ProcedureLocalEnvironmentConstructor(
		this.constructedModule, spinfo);
	envConstructor.constructProcedureLocalMetadata(declProc.getStatement());
	spinfo.setAst(BuilderUtils.serializeAST(declProc.getStatement()));
	return spinfo;
    }

    private static IASTTransactionCapabilities getTransactionCapabilities(
	    ProcedureDeclaration decl) {
	IASTTransactionCapabilities capsTransaction = null;
	if (decl.isTransactionCapable()) {
	    capsTransaction = decl.getASTTransactionCapabilities();
	}
	return capsTransaction;
    }

    private static void conveyTransactionCapabilitiesThroughoutAllStatments(
	    ProcedureDeclaration declProc) throws SBQLException {
	if (declProc.isTransactionCapableMainASTNode()) {
	    ITransactionASTVisitor transVisitor = TransactionASTVisitor
		    .getInstance();
	    transVisitor.visitProcedureDeclaration(declProc, null);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.ASTAdapter#visitClassFieldDeclaration(odra.sbql.ast.declarations.ClassFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitClassFieldDeclaration(ClassFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getClassDeclaration().accept(
		new ClassConstructor(this, this.cruder), null);

	return null;
    }

    public Object visitInterfaceFieldDeclaration(
	    InterfaceFieldDeclaration decl, Object attr) throws SBQLException {
	decl.getInterfaceDeclaration().accept(
		new InterfaceConstructor(this, this.cruder), null);

	return null;
    }
    
    /**
     * Called when there is a enum field in the module source code.
     */
    public Object visitEnumFieldDeclaration(
	    EnumFieldDeclaration decl, Object attr) throws SBQLException {
    	
    		decl.getEnumDeclaration().accept(this, null);

    	return null;
    }
    
    /**
     * Called when an enum must be created.
     */
    public Object visitEnumDeclaration(
    	EnumDeclaration decl,Object attr) throws SBQLException {
	   
    	try{ 	
    	MBEnum mbenu = new MBEnum(new MetabaseManager(
    		    constructedModule, breakOnConflicts).createMetaEnum(decl.getName(),decl.getBaseTypeName(),0));

    	for (SingleEnumeratorDeclaration e : decl.getListEnumeratorDeclaration().flattenEnumerators()) {
    		mbenu.createField(e.N.value(),BuilderUtils.serializeAST(e.E));
    		mbenu.createFieldValue(e.N.value(), 1, 1, mbenu.getName(), 0);
    	}
    	
    	new DatabaseManager(constructedModule).createEnum(decl.getName(),0);
    	
    	} catch (DatabaseException e) {
    	    throw new OrganizerException(e);
    	}
	    	
	   return null;
    }
}
