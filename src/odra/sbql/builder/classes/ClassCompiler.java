package odra.sbql.builder.classes;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBProcedure;
import odra.sbql.builder.BuilderException;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.CompilerException;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.typechecker.SBQLProcedureTypeChecker;
import odra.sbql.typechecker.TypeCheckerException;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;

/**
 * ClassCompiler
 * @author Radek Adamus
 *last modified: 2007-02-09
 *@version 1.0
 */
public class ClassCompiler {
    	/** compiles the class
	 * @param mod - parent view module 
	 * @param mbclassid - meta-class oid
	 * @return OID of the database class object
	 * @throws DatabaseException
	 * @throws CompilerException
	 * @throws TypeCheckerException
	 * @throws JulietCodeGeneratorException
	 * @throws Exception
	 */
	public void compile( DBModule mod, OID mbclassid, OID dbclassid) throws BuilderException {
	
		try
		{
		    MBClass mbclass = new MBClass(mbclassid);
		    DBClass dbclass = new DBClass(dbclassid);
		    
		    OID[] mbmethods = mbclass.getMethods();
		    OID[] dbmethods = dbclass.getMethodsEntry().derefComplex();
		    
		    if(ConfigDebug.ASSERTS) assert mbmethods.length == dbmethods.length : "metabase class methods number != database class methods number";
		    if (mbmethods.length > 0){
		    	SBQLProcedureTypeChecker checker = null;
		    	if(ConfigServer.TYPECHECKING){
		    		//all methods has the same environment so we have one typechecker object
		    		checker = new SBQLProcedureTypeChecker(mod, mbclass);
		    	}
		    	for(int i = 0; i < mbmethods.length; i++){
		    		MBProcedure mbmethod = new MBProcedure(mbmethods[i]);
		    		DBProcedure dbmethod = new DBProcedure(dbmethods[i]);
		    		if(ConfigDebug.ASSERTS) assert mbmethod.getName().compareTo(dbmethod.getName()) == 0 : "mbprocedure does not suite dbprocedure";
		    		this.compileMethod(mod, mbmethod, dbmethod, checker);
		    	}
		    }
		}  catch (DatabaseException e)
		{
		    throw new CompilerException(e);
		}
		 
		
	}
	
	private void compileMethod(DBModule mod, MBProcedure mbmethod, DBProcedure dbmethod, SBQLProcedureTypeChecker checker) throws DatabaseException{
	    //TODO optimization, debug
	    	BuilderUtils.compileProcedure(checker, mod, mbmethod, dbmethod, OptimizationSequence.getForName(OptimizationSequence.NONE));
	}
}
