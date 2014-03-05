package odra.sbql.builder.views;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBView;
import odra.db.objects.data.DBVirtualObjectsProcedure;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.sbql.SBQLException;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.CompilerException;
import odra.sbql.builder.procedures.ReturnPathChecker;
import odra.sbql.emiter.ConstantPool;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.typechecker.SBQLProcedureTypeChecker;
import odra.sbql.typechecker.TypeCheckerException;
import odra.system.config.ConfigServer;

/**
 * ViewCompiler - compiles view definition and recurrently its sub-views
 * @author radamus
 *last modified: 2006-12-24
 *@version 1.0
 */
public class ViewCompiler {
	
	Vector<Signature> seed = new Vector<Signature>(); 
	public ViewCompiler(){
		
	}
	
	/** Used to compile sub-views
	 * @param seed - vector of super-views seeds
	 */
	private ViewCompiler(Vector<Signature> seed) {
		this.seed.addAll(seed);
	}
	
	/** compiles the view definition
	 * @param mod - parent view module 
	 * @param mviewid - meta-view oid
	 * @throws DatabaseException
	 * @throws CompilerException
	 * @throws TypeCheckerException
	 * @throws JulietCodeGeneratorException
	 * @throws Exception
	 */
	public void compile( DBModule mod, OID mviewid) throws  SBQLException{
		try
		{
		    OID rtvoid = mod.findFirstByName(mviewid.getObjectName(), mod.getDatabaseEntry());
		    MBView mbvw = new MBView(mviewid);
		    DBView dbvw = new DBView(rtvoid);
		    MBProcedure mbvop = new MBProcedure(mbvw.getSeedProc());
		    OID rtvooid = mod.findFirstByName(mbvw.getVirtualObjectName(), mod.getDatabaseEntry());
		    DBVirtualObjectsProcedure dbvop = new DBVirtualObjectsProcedure(rtvooid);
		    compileView(mod, mbvw,dbvw, mbvop, dbvop);
		} catch (DatabaseException e)
		{
		    throw new CompilerException(e);
		}
	}
	
	/** Compiles view elements (virtual objects procedure, generic procedures) (with sub-views recursively) 
	 * @param mod - module context
	 * @param mbview - metabase view object
	 * @param dbview - database view object
	 * @param mbvop - metabase virtual objects procedure object
	 * @param dbvop - database virtual objects procedure object
	 * @throws DatabaseException
	 * @throws CompilerException
	 * @throws TypeCheckerException
	 * @throws JulietCodeGeneratorException
	 * @author radamus
	 */
	private void compileView(DBModule mod, MBView mbview, DBView dbview, MBProcedure mbvop, DBVirtualObjectsProcedure dbvop) throws DatabaseException, SBQLException{
		
		
			
		//first compile virtual objects procedure
		compileSeedProcedure(mod, mbvop, dbvop);		
		//create seed signature
		
		
		//next generic procedures
		OID[] mbgenericProcsoids = mbview.getGenProcsEntry().derefComplex();
		if(mbgenericProcsoids.length > 0){
			SBQLProcedureTypeChecker checker = null;
			if(ConfigServer.TYPECHECKING){
			//all view generic procedures are compiled against the same environment
				checker = new SBQLProcedureTypeChecker(mod,mbview, seed);
			}
			for(OID mbgenericProcoid: mbgenericProcsoids){
				MBProcedure genmp = new MBProcedure(mbgenericProcoid);
				DBProcedure gendp;
				gendp = new DBProcedure(dbview.getGenericProcByName(mbgenericProcoid.getObjectName()));
				compileViewGenericProcedure(mod, genmp, gendp, checker);
			}
		}
		//view fields
		for(OID mviewfld: mbview.getViewFieldsEntry().derefComplex()){
		    
		    compileViewField(mod, mbview, dbview, mviewfld);
		}
		//finally sub-views
		OID[] mbsviewsoids = mbview.getSubViewsEntry().derefComplex();
		for(OID mbsviewoid : mbsviewsoids){
			MBView mbsview = new MBView(mbsviewoid);
			DBView dbsview = new DBView(dbview.getSubViewByName(mbsview.getName()));
			MBProcedure mbsvop = new MBProcedure(mbsview.getSeedProc());
			DBVirtualObjectsProcedure dbsvop = new DBVirtualObjectsProcedure(dbsview.getVirtualObject());
			//and compile sub-view 
			new ViewCompiler(seed).compileView(mod, mbsview, dbsview, mbsvop, dbsvop);
		}
	}
	
	private void compileViewGenericProcedure(DBModule mod,  MBProcedure mbgproc, DBProcedure dbgproc, SBQLProcedureTypeChecker checker) throws DatabaseException, SBQLException{
		//TODO debug
		BuilderUtils.compileProcedure(checker, mod, mbgproc, dbgproc, OptimizationSequence.getForName(OptimizationSequence.INDEX));			
	}
	
	/** Compiles virtual objects procedure
	 * @param mod - db module the view belongs to
	 * @param mbvop - meta virtual objects procedure
	 * @param dbvop - database virtual objects procedure
	 * @throws DatabaseException
	 * @throws CompilerException
	 * @throws TypeCheckerException
	 * @throws JulietCodeGeneratorException
	 * @throws Exception
	 */
	private void compileSeedProcedure(DBModule mod, MBProcedure mbvop, DBVirtualObjectsProcedure dbvop) throws DatabaseException, SBQLException{
		assert mbvop.isValid() : "mbvop.isValid() == true";
	    //deserialize ast
		Statement ast = (Statement)BuilderUtils.deserializeAST(mbvop.getAST());
		
		ReturnPathChecker rtpc = new ReturnPathChecker(mod.getName(),mbvop);
		ast = rtpc.check(ast);
	//	typecheck 
		if(ConfigServer.TYPECHECKING){
			SBQLProcedureTypeChecker checker = new SBQLProcedureTypeChecker(mod, null, this.seed);
			optimizeSeedProcedure(ast, mod, checker, mbvop);
			checker.typecheckProcedure(ast, mbvop);
			
			
			
			//get the result signature (seed signature)
			Statement[] stmts = ast.flatten();
	//		assert stmts[stmts.length - 1] instanceof ReturnWithValueStatement: "last statement in virtual objects procedure not a return statemen";
			Signature sseed = ((ReturnWithValueStatement)stmts[stmts.length - 1]).getExpression().getSignature();
			seed.add(sseed);
		}
		//codegeneration
		BuilderUtils.generateProcedureByteCode(mod, mbvop, dbvop, ast);
		
		
	}

	// optimization (introduced as an temporary indexing patch and for wrapper)		
	private void optimizeSeedProcedure(Statement ast, DBModule mod,
			SBQLProcedureTypeChecker checker, MBProcedure mbproc) throws DatabaseException {
		if (mod.isWrapper()) {
		    	checker.setOptimization(OptimizationSequence.getForName(OptimizationSequence.WRAPPER));
		}
		 else { 
		     checker.setOptimization(OptimizationSequence.getForName(OptimizationSequence.INDEX));
		 }
	}

	private void compileViewField(DBModule mod,MBView mbview,DBView dbview,OID mviewfld) throws  SBQLException{
	    try
	    {
		switch(new MBObject(mviewfld).getObjectKind()){
			case VARIABLE_OBJECT:
			MBVariable var = new MBVariable(mviewfld);
		if(var.isTypeReference() && var.getMinCard() > 0)
		    throw new CompilerException("pointer variable '" + var.getName() + "' must have minimal cardinality = 0");
		OID rtvar = mod.findFirstByName(var.getName(), dbview.getViewFieldsEntry());
		if (rtvar != null)
			break;
//		 create as many new object as the min cardinality
		SBQLInterpreter interp = new SBQLInterpreter(mod);
		ConstantPool pool = new ConstantPool();

		JulietCode crcode;

		if (var.getMinCard() != 1 || var.getMaxCard() != 1) {
				crcode = JulietGen.genNameExpression(dbview.getOID().getObjectNameId());
				crcode.append(JulietGen.createNestedEnvironment());
				crcode.append(JulietGen.genNameExpression(dbview.getViewFieldsEntry().getObjectNameId()));
				crcode.append(JulietGen.genInitVariable(var, var.getMinCard(), var.getMaxCard()));
				crcode.append(JulietGen.genPopQRES());
				crcode.append(JulietGen.destroyNestedEnvironment());
				interp.runCode(crcode.getByteCode(), pool.getAsBytes());

		}
			
		if(var.getMinCard() > 0){
		    	crcode = JulietGen.genNameExpression(dbview.getOID().getObjectNameId());
			crcode.append(JulietGen.createNestedEnvironment());
			crcode.append(JulietGen.genNameExpression(dbview.getViewFieldsEntry().getObjectNameId()));
			crcode.append(JulietGen.genCreate(var));

			for (int i = 0; i < var.getMinCard(); i++)
				interp.runCode(crcode.getByteCode(), pool.getAsBytes());
		}

			break;
			case PROCEDURE_OBJECT:
			    assert false: "unimplemented";
			break;
		}
	    } catch (DatabaseException e)
	    {
		throw new CompilerException(e);
	    }
	    
	}
}
