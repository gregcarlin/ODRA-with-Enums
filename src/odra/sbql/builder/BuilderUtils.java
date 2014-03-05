package odra.sbql.builder;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBVirtualObjectsProcedure;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ParserException;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.serializer.ASTDeserializer;
import odra.sbql.ast.serializer.ASTSerializer;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.builder.procedures.ProcedureLocalEnvironmentConstructor;
import odra.sbql.builder.procedures.ReturnPathChecker;
import odra.sbql.debugger.compiletime.ASTJulietCodeIndexesInserter;
import odra.sbql.debugger.compiletime.DebugCodeSerializer;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.exceptions.CompiletimeExceptionTable;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.parser.SBQLLexer;
import odra.sbql.parser.SBQLParser;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.typechecker.SBQLProcedureTypeChecker;
import odra.sbql.typechecker.SBQLTypeChecker;
import odra.sbql.typechecker.SBQLTypeCheckerHelper;
import odra.system.config.ConfigServer;

/**
 * BuilderUtils helper class with common operations used in compilers, schema organizers and constructors
 * 
 * @author Radek Adamus last modified: 2006-12-24
 * @version 1.0
 */
public class BuilderUtils {

	/**
	 * @return instance of Module Linker
	 */
	public static ModuleLinker getModuleLinker() {
		return new ModuleLinkerWithNested();
	}

	/**
	 * @return instance of module compiler
	 */
	public static ModuleCompiler getModuleCompiler() {
		return new ModuleCompiler();
	}

	
	/**
	 * @param src - SBQL source code
	 * @return - abstract syntax tree representation of the source code
	 * @throws Exception
	 */
	public static ASTNode parseSBQL(String src) throws Exception {
		return (ASTNode) new SBQLParser(new SBQLLexer(new StringReader(src))).parse().value;
	}

	/**
	 * @param module - name of the context module
	 * @param src - SBQL source code
	 * @return - abstract syntax tree representation of the source code
	 * @throws Exception
	 */
	public static ASTNode parseSBQL(String module, String src) throws Exception {
		try
		{
			return (ASTNode) new SBQLParser(new SBQLLexer(new StringReader(src)), module).parse().value;
		}
		catch(ParserException exc)
		{
		    	
			BufferedReader reader = new BufferedReader(new StringReader(src));
			String line;
			int lineNumber = 0;
			do
			{
				line = reader.readLine();
				lineNumber++;
			}
			while(lineNumber < exc.getLine());
			
			line += NEW_LINE;
			char[] chars = line.toCharArray();
			int i = 0;
			for(char c : chars)
			{
				if(i >= exc.getColumn() - 1)
					break;
				
				if(c == '\t')
					line += '\t';
				else
					line += " ";
				
				i++;
			}
			line += "^";
			
			ParserException newException = new ParserException(
				exc.getMessage() + NEW_LINE + line,
				exc.getModule(),
				exc.getLine(),
				exc.getColumn());
			
			throw newException;
		}
	}

	/**
	 * default ast serialization (with column/line info present)
	 * 
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public static byte[] serializeAST(ASTNode s) throws BuilderException {
		return serializeAST(s, true);
	}
	/**
	 * serialize abstract syntax tree (AST)
	 * 
	 * @param s
	 * @param withLineColumnPositionInfo -
	 *           should we also serialize line/column info?
	 * @return serialized AST
	 * @throws Exception
	 */

	public static byte[] serializeAST(ASTNode s, boolean withLineColumnPositionInfo)
				throws BuilderException {

		byte[] serast = null;
		ASTSerializer serializer = new ASTSerializer();
		serast = serializer.writeAST(s, withLineColumnPositionInfo);
		return serast;
	}

	/**
	 * deserialize abstract syntax tree (AST) of the procedure
	 * 
	 * @param proc
	 * @return AST in deserialized form
	 * @throws DatabaseException
	 * @throws CompilerException
	 */
	public static ASTNode deserializeAST(byte[] rawast)  {
		ASTNode stmt = null;

		ASTDeserializer des = new ASTDeserializer();
		stmt = des.readAST(rawast);
		return stmt;
	}

	/** Generates procedure byte code
	 * @param mod - context module
	 * @param mproc - metabase procedure
	 * @param dproc - database procedure
	 * @param stmt - abstract syntax tree representation of the procedure body source code
	 * @throws Exception
	 */
	public static void generateProcedureByteCode(DBModule mod, MBProcedure mproc, DBProcedure dproc, ASTNode stmt)
				throws BuilderException{
		IJulietCodeGenerator gen = EmiterFactory.getProcedureJulietCodeGenerator(mod, mproc);
		
		
			gen.generateWithDebug(stmt);
		
		/**
		 * add beginTrans as the first instruction
		 * radamus: code moved to JulietCodeGenerator.generateProcedureHeader 
		 */
		JulietCode bodycode = gen.getCode();

		try
		{
		    dproc.setBinaryCode(bodycode.getByteCode());
		    dproc.setConstantPool(gen.getConstantPool().getAsBytes());
		    CompiletimeExceptionTable cc = gen.getCatchTable();
		    dproc.setExceptionTable(cc.getAsBytes());
		    stmt = new ASTJulietCodeIndexesInserter().fill(stmt);

	    	if (ConfigServer.DEBUG_INCLUDE_SOURCE) 		    	
	    		dproc.setDebugCode(DebugCodeSerializer.generateFull(stmt, ConfigServer.DEBUG_INCLUDE_EXPRESSIONS));		    	
	    	 else
	    		dproc.setDebugCode(DebugCodeSerializer.generate(stmt, ConfigServer.DEBUG_INCLUDE_EXPRESSIONS));

		} catch (DatabaseException e)
		{
		    throw new CompilerException(e);
		} 		
	}

	
	/**
	 * @param type - oid of the matabase object representing structural type
	 * @return true if the type is recurrent
	 * @throws DatabaseException
	 */
	public static boolean isRecursiveType(OID type) throws DatabaseException {
		return isRecursiveType(type, new Hashtable<OID, OID>());
	}

	private static boolean isRecursiveType(OID moid, Hashtable<OID, OID> marked) throws DatabaseException {
		// Hashtable<OID, OID> checked = new Hashtable<OID, OID>();
		OID mType = moid;
		if (marked.get(moid) != null) {
			return true;
		}
		MetaObjectKind kind = new MBObject(mType).getObjectKind();

		if (kind == MetaObjectKind.TYPEDEF_OBJECT) {
			marked.put(moid, moid);
			do {
				mType = new MBTypeDef(mType).getType();
			} while (new MBTypeDef(mType).isValid());
			kind = new MBObject(mType).getObjectKind();
		}

		Vector<OID> types = new Vector<OID>();
		switch (kind) {
			case STRUCT_OBJECT:
				types.add(mType);
				break;

			case CLASS_OBJECT:
				marked.put(moid, moid);
				types.addAll(new MBClass(moid).getFullType());
				break;

			default:
				return false;
		}

		for (OID type : types) {
			for (OID field : new MBStruct(type).getFields()) {
				MBVariable fieldvar = new MBVariable(field);
				assert fieldvar.isValid() : "struct field not a meta-variable";
				if (fieldvar.getRefIndicator() > 0) {
					continue;
				}
				if (fieldvar.getMinCard() > 0) {
					if (moid.equals(fieldvar.getType())
								|| isRecursiveType(fieldvar.getType(), (Hashtable<OID, OID>) marked.clone())) {
						return true;
					}
				}

			}
		}
		return false;
	}
	
	public static void alterProcedureBody(String moduleName, String procPathName, Statement ast){
		DBModule module;
		try {
			module = Database.getModuleByName(moduleName);
		} catch (DatabaseException e) {
			throw new OrganizerException("unable to find module '" + moduleName + "'");
		}
		String[] names = procPathName.split("\\.");
	    String procName = names[names.length - 1];
	    String[] relativePath = new String[names.length - 1];
	    for(int i = 0; i < names.length - 1; i ++){
	    	relativePath[i] = names[i];
	    }
	    ModuleOrganizer modorg = new ModuleOrganizer(module, false); 	  
	    OID found = modorg.findMetaObject(relativePath, procName);	    
	    if(found == null)
	    	throw new OrganizerException("unable to find " + procPathName + " in module " + moduleName);
	    
	    if(ast instanceof BlockStatement){
	    	ast = ((BlockStatement)ast).getStatement();
	    }
	    OdraProcedureSchema procInfo = new OdraProcedureSchema(procName, new ProcArgument[0], new ProcedureAST(BuilderUtils.serializeAST(ast)), null);
		ProcedureLocalEnvironmentConstructor constructor = new ProcedureLocalEnvironmentConstructor(module, procInfo);
		constructor.constructProcedureLocalMetadata(ast);
	    modorg.alterProcedureBody(found, procInfo);
	    	    
	}
	
	public static MBProcedure getMetaProcedure(DBModule module, String procPathName) throws DatabaseException, Exception{
	    String[] names = procPathName.split("\\.");
	    String procName = names[names.length - 1];
	    String[] relativePath = new String[names.length - 1];
	    for(int i = 0; i < names.length - 1; i ++){
		relativePath[i] = names[i];
	    }
	    OID found = new ModuleOrganizer(module, false).findMetaObject(relativePath, procName);
	    if(found == null)
		throw new OrganizerException("unable to find " + procPathName + " in module " + module.getModuleGlobalName());
	    MBProcedure mbproc = new MBProcedure(found);
	    if(mbproc.isValid())
		return mbproc;

	    throw  new OrganizerException(procPathName + " is not a procedure" );
	    
	}
	public static DBProcedure getDataProcedure(DBModule module, String procPathName) throws DatabaseException, Exception{
	    String[] names = procPathName.split("\\.");
	    String procName = names[names.length - 1];
	    String[] relativePath = new String[names.length - 1];
	    for(int i = 0; i < names.length - 1; i ++){
		relativePath[i] = names[i];
	    }
	    OID found = new ModuleOrganizer(module, false).findDataObject(relativePath, procName);
	    if(found == null)
		throw new OrganizerException("unable to find '" + procPathName + "' in module '" + module.getModuleGlobalName() +"'");
	    DBProcedure dbproc = new DBProcedure(found);
	    if(dbproc.isValid())
		return dbproc;
	    DBVirtualObjectsProcedure mbvproc = new DBVirtualObjectsProcedure(found);
	    if(mbvproc.isValid())
		return mbvproc;
	    throw new OrganizerException(procPathName + " is not a procedure" );
	    
	}
	
	
	
	/**
	 * compiles procedure (AST -> [typecheckedAST] -> bytecode)
	 * @param checker - properly initialized procedure typechecker <br>(e.g. initialized for class method compilation) 
	 * @param mod -
	 *           module context
	 * @param mproc -
	 *           metabase procedure object
	 * @param dproc -
	 *           database procedure object       
	 * @throws DatabaseException
	 * @throws CompilerException
	 * @throws JulietCodeGeneratorException
	 */
	public static void compileProcedure(SBQLProcedureTypeChecker checker, DBModule mod, MBProcedure mproc, DBProcedure dproc, OptimizationSequence optSequence) throws DatabaseException,
			SBQLException{
	    assert checker != null && mod != null && mproc!= null && dproc != null : "checker != null && mod != null && mproc!= null && dproc != null"; 
		try {
			Statement stmt = (Statement) BuilderUtils.deserializeAST(mproc.getAST());
			ReturnPathChecker rtpc = new ReturnPathChecker(mod.getName(), mproc);
			stmt = rtpc.check(stmt);

			/**
			 * type checking
			 */
			if (ConfigServer.TYPECHECKING) {
			    	checker.setOptimization(optSequence);
				stmt = checker.typecheckProcedure(stmt, mproc);				
			}

			/**
			 * compilation
			 */
			BuilderUtils.generateProcedureByteCode(mod, mproc, dproc, stmt);
			
		}catch (CompilerException e) {
			throw e;
		}
		catch (SBQLException e) {
			throw new CompilerException(e);
		}
	}
	
	public static void compileEnum(DBModule mod, OID mtenu) throws DatabaseException,
		SBQLException {
		
		try {
			
			MBEnum menum = new MBEnum(mtenu);			
			
			Signature sigtype = SBQLTypeCheckerHelper.getSignature(menum.getType());
			for (OID field : menum.getFields())
				menum.createFieldValue(field.getObjectName(), 1, 1, menum.getName(), 0);		
			
			OID dbenumoid = mod.findFirstByName(menum.getName(), mod.getDatabaseEntry());
			
			SBQLTypeChecker checker = new SBQLTypeChecker(mod);
			
			SBQLInterpreter interpreter = new SBQLInterpreter(mod);
			IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
			
			for(OID field : menum.getFields()){
				Expression expr = (Expression)BuilderUtils.deserializeAST(field.derefBinary());
				expr = (Expression) checker.typecheckAdHocQuery(expr);
				checker.checkSigTypeCompatibility(sigtype, expr.getSignature(), expr, null, false);				
//1				expr = new CreateExpression(new Name(field.getObjectName()),expr);
//1				interpreter.setResult(new ReferenceResult(rteid));
//2				expr = new InsertCopyExpression(new NameExpression(new Name(menum.getName())), expr, new Name(field.getObjectName()));
				
//				expr = (Expression) checker.typecheckAdHocQuery(expr);
				generator.generate(expr);
				byte[] cnstPool = generator.getConstantPool().getAsBytes();
				interpreter.runCode(generator.getCode().getByteCode(), cnstPool);
				Result res = interpreter.getResult();
				if(res instanceof StringResult){
					dbenumoid.getStore().createStringObject(field.getObjectNameId(), dbenumoid, ((StringResult)res).value, 0);
				}				
			}
			
			
			
			
			
			
			
			
			
			
			
			
		}catch (CompilerException e) {
			throw e;
		}
		catch (SBQLException e) {
			throw new CompilerException(e);
		}
		
		
	}

	public final static String NEW_LINE = System.getProperty("line.separator");

	

	
	
}