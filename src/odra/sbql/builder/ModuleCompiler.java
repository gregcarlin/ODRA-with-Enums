package odra.sbql.builder;

import java.util.HashSet;
import java.util.Hashtable;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBInterface;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.sbql.SBQLException;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.builder.classes.ClassCompiler;
import odra.sbql.builder.views.ViewCompiler;
import odra.sbql.emiter.ConstantPool;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.interpreter.SBQLInterpreter.ExecutionMode;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.typechecker.SBQLProcedureTypeChecker;
import odra.sbql.typechecker.SBQLTypeChecker;
import odra.sbql.typechecker.SBQLTypeCheckerHelper;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;
import odra.ws.facade.WSProxyException;

/**
 * This class iterates over module fields and initializes them. The
 * initialization consists in the instantiation of global variables and the
 * compilation of procedures using their ASTs. Before a module is compiled, it
 * must be linked to other modules (the ModuleLinker class).
 * 
 * @author raist modifications: radamus
 */

public class ModuleCompiler {
	SBQLProcedureTypeChecker checker;

	boolean debug = true;

	ModuleCompiler() {
	}

	/**
	 * Initiates the compilation of a module.
	 * 
	 * @param mod
	 *            module that should be compiled
	 */
	public void compileModule(DBModule mod) throws SBQLException {
		try {
			this.compile(mod, new HashSet<String>());
			this.updateInterfaces(mod);
		} catch (DatabaseException e) {
			throw new CompilerException(e);
		}
	}

	private OID findInterfaceInModule(DBModule imod, String iname) throws DatabaseException {
		assert imod.isValid() : "invalid module";

		System.out.println("Szukamy " + iname + " w " + imod.getModuleGlobalName());
		OID f = imod.findFirstByName(iname, imod.getDatabaseEntry());

		if (f == null)
			return null;

		DBInterface iface = new DBInterface(f);

		return iface.isValid() ? f : null;
	}

	private void updateInterfaces(DBModule mod) throws DatabaseException {
		OID[] impstr = mod.getImplements();
		OID[] impmod = mod.getCompiledImports();

		for (OID i : impstr) {
			String impval = i.derefString();
			OID iface = findInterfaceInModule(mod, impval);

			if (iface == null) {
				for (OID m : impmod) {
					iface = findInterfaceInModule(new DBModule(m.derefReference()), impval);

					if (iface != null)
						break;
				}
			}

			if (iface != null) {
				DBInterface dbi = new DBInterface(iface);
				dbi.addTarget(mod.getDatabaseEntry());

				continue;
			}

			throw new DatabaseException("Cannot find interface '" + impval + "'");
		}
	}

	/**
	 * Initiates the compilation of a module.
	 * 
	 * @param mod
	 *            module that should be compiled
	 */
	public void compileModuleWithDebug(DBModule mod) throws SBQLException {
		debug = true;
		try {
			this.compile(mod, new HashSet<String>());
		} catch (DatabaseException e) {
			throw new CompilerException(e);
		}
	}

	/**
	 * Compiles a module and all modules imported by it.
	 * 
	 * @param mod
	 *            module that should be compiled
	 * @param resmods
	 *            a hash set responsible for keeping track what modules are
	 *            already subjects of compilation
	 */
	private void compile(DBModule mod, HashSet<String> resmods) throws DatabaseException, SBQLException {
		/**
		 * necessary to get rid of import cycles (module a importing module b
		 * importing module a)
		 */
		if (resmods.contains(mod.getModuleGlobalName())) {
			return;
		}

		/**
		 * generate session initialization code
		 * this.generateSessionBootstrap(mod); initialize module's global
		 * variables
		 */
		this.initGlobals(mod);

		/**
		 * compile dependendent modules
		 */
		resmods.add(mod.getModuleGlobalName());
		IProxyFacade pm = WSManagersFactory.createProxyManager();

		try {
			for (OID i : mod.getImports()) {
				DBModule m = Database.getModuleByName(i.derefString());

				if (pm != null && pm.isProxy(m.getOID())) {
					continue;
				}

				this.compile(m, resmods);

			}
		} catch (WSProxyException ex) {
			throw new DatabaseException("Error in ProxyManager", ex);
		}

		/**
		 * set the module as compiled
		 */
		mod.setModuleCompiled(true);
	}

	/**
	 * Initializes procedures and variables. When a procedure is initialized, it
	 * is compiled. When variable is initialized, then depending on the
	 * cardinality, one or more objects are created in the database.
	 * 
	 * @param mod
	 *            module which should be initialized
	 */
	private void initGlobals(DBModule mod) throws DatabaseException, SBQLException {
		OID[] modflds = mod.getMetabaseEntry().derefComplex();
		Hashtable<OID, OID> compiled = new Hashtable<OID, OID>();
		String modname = mod.getName();
		if (ConfigServer.TYPECHECKING) {
			checker = new SBQLProcedureTypeChecker(mod);
		}
		try {
			for (OID fldid : modflds) {
				/**
				 * module 'system' has aggregate objects
				 */
				if (!fldid.isComplexObject()) {
					continue;
				}

				MBObject obj = new MBObject(fldid);
				if (modname.equals(obj.getName())) {
					throw new CompilerException("The name of the module global object cannot be the same as the name of the module");
				}

				MetaObjectKind mokind = obj.getObjectKind();
				switch (mokind) {
				case VARIABLE_OBJECT:
					this.compileGlobalVariable(mod, fldid, compiled);
					break;

				case PROCEDURE_OBJECT:
					OID rtpid = mod.findFirstByName(obj.getName(), mod.getDatabaseEntry());
					DBProcedure dbproc = new DBProcedure(rtpid);
					MBProcedure mbproc = new MBProcedure(fldid);
					this.compileProcedure(mod, mbproc, dbproc);
					break;

				case VIEW_OBJECT:
					new ViewCompiler().compile(mod, fldid);
					break;

				case CLASS_OBJECT:
					this.compileClass(mod, fldid, compiled);
					break;

				case ENUM_OBJECT:
					// OID rteid = mod.findFirstByName(obj.getName(),
					// mod.getDatabaseEntry());
					// DBEnum dbenu = new DBEnum(rteid);
					// MBEnum mbenu = new MBEnum(fldid);
					this.generateEnum(mod, fldid, compiled);
					break;
				case TYPEDEF_OBJECT:
					compileTypeDef(fldid, compiled);
					break;
				}
			}
			/**
			 * generate session initialization code
			 */
			this.generateSessionBootstrap(mod, compiled);
		} catch (Exception ex) {
			if (ConfigDebug.DEBUG_EXCEPTIONS)
				ex.printStackTrace();
			throw new CompilerException(ex);
		}
	}

	/**
	 * compiles procedure (AST -> [typecheckedAST] -> bytecode)
	 * 
	 * @param mod
	 *            - module context
	 * @param mproc
	 *            - metabase procedure object
	 * @param dproc
	 *            - database procedure object
	 * @throws DatabaseException
	 * @throws CompilerException
	 * @throws JulietCodeGeneratorException
	 */
	private void compileProcedure(DBModule mod, MBProcedure mproc, DBProcedure dproc) throws DatabaseException, SBQLException {
		// TODO optimization
		BuilderUtils.compileProcedure(checker, mod, mproc, dproc, OptimizationSequence.getForName(OptimizationSequence.NONE));
	}

	private void compileClass(DBModule mod, OID mbclassid, Hashtable<OID, OID> compiled) throws DatabaseException, Exception {
		// TODO rethink && discuss

		// import
		DBModule container = new DBModule(mbclassid.getParent().getParent().getParent());
		if (!container.isValid()) {
			// direct
			container = mod;
		}

		OID tmp = mod.findFirstByName(mbclassid.getObjectName(), container.getDatabaseEntry());
		IProxyFacade pm = WSManagersFactory.createProxyManager();
		if ((tmp != null) && (pm != null) && (pm.isProxy(tmp))) {
			return;
		}

		if (compiled.get(mbclassid) != null)
			return;
		if (BuilderUtils.isRecursiveType(mbclassid))
			throw new CompilerException("recursive types unsupported " + mbclassid.getObjectName());
		MBClass mcls = new MBClass(mbclassid);
		this.compileStruct(mcls.getType(), compiled, true);
		OID[] mbsuperclasses = mcls.getDirectSuperClasses();
		for (OID supeclsid : mbsuperclasses) {
			if (compiled.get(supeclsid) != null) {
				continue;
			}
			MBClass superClass = new MBClass(supeclsid);
			if (!superClass.isValid())
				throw new CompilerException("A superclass of class  '" + mbclassid.getObjectName() + "' named '" + supeclsid.getObjectName()
						+ "' is not a class and cannot appear in 'extends' clause");
			this.compileClass(superClass.getModule(), supeclsid, compiled);
		}

		/**
		 * find class object in the database
		 */
		OID dbclassid = mod.findFirstByName(mbclassid.getObjectName(), mcls.getModule().getDatabaseEntry());
		if (ConfigDebug.ASSERTS) {
			assert new DBClass(dbclassid).isValid() : "DBClass required";
		}
		new ClassCompiler().compile(mod, mbclassid, dbclassid);

		DBClass dbclass = new DBClass(dbclassid);
		/**
		 * update database class-to-superclass references to avoid removing
		 * superclasses references at each module compilation the code is a bit
		 * more complicated ;)
		 */
		OID[] dbsuperclassesrefs = dbclass.getSuperClassesRefs();
		if (mbsuperclasses.length > dbsuperclassesrefs.length) {
			int i;
			for (i = 0; i < dbsuperclassesrefs.length; i++) {
				if (ConfigDebug.ASSERTS)
					assert compiled.get(mbsuperclasses[i]) != null : "uncompiled class (?)";
				dbsuperclassesrefs[i].updatePointerObject(compiled.get(mbsuperclasses[i]));
			}
			for (int j = i; j < mbsuperclasses.length; j++) {
				dbclass.addSuperClass(compiled.get(mbsuperclasses[j]));
			}
		} else if (mbsuperclasses.length < dbsuperclassesrefs.length) {
			int i;
			for (i = 0; i < mbsuperclasses.length; i++) {
				if (ConfigDebug.ASSERTS)
					assert compiled.get(mbsuperclasses[i]) != null : "uncompiled class (?)";
				dbsuperclassesrefs[i].updatePointerObject(compiled.get(mbsuperclasses[i]));
			}
			for (int j = i; j < dbsuperclassesrefs.length; j++) {
				dbsuperclassesrefs[j].delete();
			}
		} else {
			for (int i = 0; i < dbsuperclassesrefs.length; i++) {
				if (ConfigDebug.ASSERTS)
					assert compiled.get(mbsuperclasses[i]) != null : "uncompiled class (?)";
				dbsuperclassesrefs[i].updatePointerObject(compiled.get(mbsuperclasses[i]));
			}
		}

		compiled.put(mbclassid, dbclassid);
	}

	private void generateSessionBootstrap(DBModule mod, Hashtable<OID, OID> compiled) throws DatabaseException, Exception {
		OID[] sflds = mod.getSessionMetaDataEntry().derefComplex();
		String modname = mod.getName();
		if (sflds.length > 0) {
			JulietCode bootcode = new JulietCode();

			for (OID sfld : sflds) {
				MBVariable var = new MBVariable(sfld);
				if (modname.equals(var.getName())) {
					throw new CompilerException("The name of the module global object cannot be the same as the name of the module");
				}
				if (var.isValid()) { // should it be always valid?
					bootcode.append(this.compileSessionGlobalVariable(mod, var, compiled));
				}
			}
			mod.setSessionInitalizationCode(bootcode.getByteCode());
		}
	}

	private void compileTypeDef(OID mtdef, Hashtable<OID, OID> compiled) throws DatabaseException {
		if (compiled.get(mtdef) != null)
			return;
		if (BuilderUtils.isRecursiveType(mtdef))
			throw new CompilerException("recursive types unsupported " + mtdef.getObjectName());
		compiled.put(mtdef, mtdef);
	}

	private void compileEnum(DBModule mod, OID mtenu, Hashtable<OID, OID> compiled) throws DatabaseException {
		if (compiled.get(mtenu) != null)
			return;
		BuilderUtils.compileEnum(mod, mtenu);
		compiled.put(mtenu, mtenu);
	}

	private void compileStruct(OID mtstr, Hashtable<OID, OID> compiled, boolean classType) throws DatabaseException {
		if (compiled.get(mtstr) != null)
			return;
		MBStruct str = new MBStruct(mtstr);
		for (OID field : str.getFields()) {
			MBVariable var = new MBVariable(field);
			if (!(var.isTypeReference()) && (var.getRefIndicator() > 0)) {
				throw new CompilerException("field '" + var.getName() + "' - - pointer object cannot point on a type");
			}
			if (var.isTypeReference()) {
				if (var.hasReverseReference()) {
					if (!classType) {
						throw new CompilerException("'" + var.getName() + "' - reverse pointers can be declared only in classes");
					}
					MBVariable reverse = new MBVariable(var.getReversePointer());
					if (!reverse.isValid() || !reverse.isTypeReference()) {
						throw new CompilerException("reverse pointer for '" + var.getName() + "' is not a pointer");
					}
					if (!reverse.hasReverseReference()) {
						throw new CompilerException("reverse pointer for '" + var.getName() + "' must be also declared as reverse");
					}
					if (!reverse.getReversePointer().equals(var.getOID())) {
						throw new CompilerException("reverse pointer for '" + var.getName() + "' must be a reverse pointer to '" + var.getName()
								+ "'");
					}
				}
			}

		}
		compiled.put(mtstr, mtstr);
	}

	private void compileGlobalVariable(DBModule mod, OID mvarid, Hashtable<OID, OID> compiled) throws DatabaseException, Exception {
		MBVariable var = new MBVariable(mvarid);
		this.compileVariable(mod, var, compiled);//dla prostych typów to nic nie robi, jak typu typedef i on skom. też nic się nie dzieje
		// check if the module field is already defined. if yes, do nothing.
		OID rtvar = mod.findFirstByName(var.getName(), mod.getDatabaseEntry());
		if (rtvar != null) {
			return;
		}

		// create as many new object as the min cardinality
		SBQLInterpreter interp = new SBQLInterpreter(mod, ExecutionMode.Compilation);
		ConstantPool pool = new ConstantPool();

		JulietCode crcode;

		if (var.getMinCard() != 1 || var.getMaxCard() != 1) {
			crcode = JulietGen.genLoadPersistentEnvironment();
			crcode.append(JulietGen.genInitVariable(var, var.getMinCard(), var.getMaxCard()));
			crcode.append(JulietGen.genPopQRES());
			interp.runCode(crcode.getByteCode(), pool.getAsBytes());
			assert !interp.hasResult() : "inconsistent stack state";

		}

		if (var.getMinCard() > 0) {
			crcode = JulietGen.genLoadPersistentEnvironment();
			crcode.append(JulietGen.genCreate(var));

			for (int i = 0; i < var.getMinCard(); i++) {
				interp.runCode(crcode.getByteCode(), pool.getAsBytes());
				assert !interp.hasResult() : "inconsistent stack state";
			}
		}

	}

	private JulietCode compileSessionGlobalVariable(DBModule mod, MBVariable var, Hashtable<OID, OID> compiled) throws DatabaseException, Exception {
		this.compileVariable(mod, var, compiled);
		JulietCode bootcode = new JulietCode();
		if (var.getMinCard() != 1 || var.getMaxCard() != 1) {
			bootcode.append(JulietGen.genLoadModuleSessionEnvironment());
			bootcode.append(JulietGen.genInitVariable(var, var.getMinCard(), var.getMaxCard()));
			bootcode.append(JulietGen.genPopQRES());
		}
		for (int i = 0; i < var.getMinCard(); i++) {
			bootcode.append(JulietGen.genLoadModuleSessionEnvironment());
			bootcode.append(JulietGen.genCreate(var));
		}
		return bootcode;
	}

	private void compileVariable(DBModule mod, MBVariable var, Hashtable<OID, OID> compiled) throws DatabaseException, Exception {
		int minCard = var.getMinCard();
		int maxCard = var.getMaxCard();
		if (minCard > maxCard) {
			throw new CompilerException("variable '" + var.getName() + "': the cardinality is inconsistent");
		}
		if (minCard == 0 && maxCard == 0) {
			throw new CompilerException("variable '" + var.getName() + "': unable to define cardinality [0..0]");
		}
		if (minCard == Integer.MAX_VALUE && maxCard == Integer.MAX_VALUE) {
			throw new CompilerException("variable '" + var.getName() + "': unable to define cardinality [*..*]");
		}
		if (var.isTypeReference()) {
			if (minCard > 0)
				throw new CompilerException("pointer variable '" + var.getName() + "' must have minimal cardinality = 0");
		} else if (var.getRefIndicator() > 0) {
			throw new CompilerException("'" + var.getName() + "' - pointer variable cannot point on a type");
		} else if (var.isTypeTypeDef()) {
			this.compileTypeDef(var.getType(), compiled);//jak typ skompilowany nie robi nic
		} else if (var.isTypeStruct()) {
			this.compileStruct(var.getType(), compiled, false);
		} else if (var.isTypeClass()) {
			this.compileClass(mod, var.getType(), compiled);
		} else if (var.isTypeEnum()) {
			//this.generateEnum(mod, var.getType(), compiled);//od razu był return w tym przypadku
			this.compileEnumVar(mod,var,compiled);
			
		}
	}


	private void compileEnumVar(DBModule mod, MBVariable var, Hashtable<OID, OID> compiled) throws DatabaseException {
		if(compiled.get(var.getOID())!=null) return;
		if(compiled.get(var.getType())==null) generateEnum(mod,var.getType(),compiled);
		
		MBEnum mbenum = new MBEnum(var.getType());
		
		SBQLTypeChecker checker = new SBQLTypeChecker(mod);
		SBQLInterpreter interp = new SBQLInterpreter(mod, ExecutionMode.Compilation);
		IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
		
		Signature sigtype = SBQLTypeCheckerHelper.getSignature(mbenum.getType());
		Expression expr = (Expression) BuilderUtils.deserializeAST(mbenum.getFields()[0].derefBinary());
		expr = (Expression) checker.typecheckAdHocQuery(expr, sigtype);
		
		DotExpression ed = new DotExpression(new NameExpression(new Name(mbenum.getName())),new NameExpression(new Name(mbenum.getFieldsValue()[0].getObjectName())));
		
		
		JulietCode crcode = new JulietCode();
		//OID o = mod.findFirstByName("$data", mod.getDatabaseEntry());
		//crcode.append(JulietGen.genBind(o.getObjectNameId())); //na QRES wynik tego wiązania wiązanie do plec
		crcode = JulietGen.genLoadPersistentEnvironment();
		crcode.append(JulietGen.genCreate(var));	//skonsumuje ten wynik z qres to referencja rodzica							
		interp.runCode(crcode.getByteCode(), new ConstantPool().getAsBytes());	
		//plec.Kob=K
		
		SBQLInterpreter interpreter = new SBQLInterpreter(mod);
		AssignExpression aexpr = 
			new AssignExpression(new NameExpression(new Name(var.getName())), ed, Operator.opAssign);
		checker.typecheckAdHocQuery(aexpr);
		generator.generate(aexpr);
		interpreter.runCode(generator.getCode().getByteCode(), generator.getConstantPool().getAsBytes());
		
		compiled.put(var.getOID(), var.getOID());
	}

	private void generateEnum(DBModule mod, OID mbenumid, Hashtable<OID, OID> compiled) throws DatabaseException {
		if(compiled.get(mbenumid)!= null){
			return;
		}
		MBEnum mbenum = new MBEnum(mbenumid);		
		int enameid = mbenumid.getObjectNameId();
		OID[] fields = mbenum.getFields();
		Signature sigtype = SBQLTypeCheckerHelper.getSignature(mbenum.getType());
		//??
		
		OID dbenumsid = mod.findFirstByName("$enums", mod.getDatabaseEntry());
		OID dbenumid = mod.findFirstByNameId(enameid, dbenumsid);
		
		
		//OID dbenumid = mod.findFirstByNameId(enameid, mod.getDatabaseEntry());
		
		assert dbenumid != null : "dbenumid != null";
		dbenumid.deleteAllChildren();
		
		// create as many new object as the min cardinality
		SBQLTypeChecker checker = new SBQLTypeChecker(mod);
		SBQLInterpreter interp = new SBQLInterpreter(mod, ExecutionMode.Compilation);
		IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
		OID[] valueFields = mbenum.getFieldsValue();
		for (int i = 0 ; i < fields.length; i++ ) {
			Expression expr = (Expression) BuilderUtils.deserializeAST(fields[i].derefBinary());
			expr = (Expression) checker.typecheckAdHocQuery(expr, sigtype);
			
						
			MBVariable var = new MBVariable(valueFields[i]);
			
			
			
			JulietCode crcode = new JulietCode();
			crcode.append(JulietGen.genBind(enameid)); //na QRES wynik tego wiązania wiązanie do plec
			crcode.append(JulietGen.genCreate(var));	//skonsumuje ten wynik z qres to referencja rodzica							
			interp.runCode(crcode.getByteCode(), new ConstantPool().getAsBytes());	
			//plec.Kob=K
			AssignExpression aexpr = 
				new AssignExpression(
						new DotExpression(
								new NameExpression(new Name(mbenum.getName())),
								new NameExpression(new Name(var.getName()))
								), expr, Operator.opAssign);
			checker.typecheckAdHocQuery(aexpr);
			generator.generate(aexpr);
			interp.runCode(generator.getCode().getByteCode(), generator.getConstantPool().getAsBytes());
			
			
			
			
		}
		
		mbenum.setState(true);
		compiled.put(mbenumid, mbenumid);

	}
}