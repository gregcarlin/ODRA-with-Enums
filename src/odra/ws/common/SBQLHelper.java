package odra.ws.common;

import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MetaObjectKind;
import odra.network.encoders.results.QueryResultEncoder;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleCompiler;
import odra.sbql.builder.ModuleLinker;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.typechecker.SBQLTypeChecker;
import odra.security.AuthenticationException;
import odra.sessions.Session;
import odra.system.config.ConfigServer;


/**
 * SBQL Helper class for endpoitns and proxies
 *
 * @since 2007-12-24
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 * TODO refactor callMethod and callProcedure are very similar
 */
public class SBQLHelper {
	private ModuleLinker linker;
	private ModuleCompiler compiler;
	private SBQLTypeChecker checker;
	private IJulietCodeGenerator generator;
	private SBQLInterpreter interpreter;

	private QueryResultEncoder qresEncoder; // encodes query results

	private static String username = ConfigServer.WS_CONTEXT_USER;
	private static String password = ConfigServer.WS_CONTEXT_USER;

	public SBQLHelper() {
		this.linker = BuilderUtils.getModuleLinker();
		this.compiler = BuilderUtils.getModuleCompiler();

	}

	/** Calls database procedure
	 * @param mbProc metadata object
	 * @param dbProc data object
	 * @param params parameters
	 * @return result of query
	 * @throws DatabaseException
	 */
	public Result callMethod(MBClass mbClass, MBProcedure mbProc, ParamDef[] params,
			InitializationDef preInitialization) throws DatabaseException {
		OID classInstance = null;
		DBModule module = null;

		try {
			String tempObjectName = mbClass.getInstanceName();
			module = mbClass.getModule();
			procedurePreexecutionInitialization(preInitialization, module);

			classInstance = module.createSessionMetaVariable(tempObjectName,  1, 1, mbClass.getName(), 0);
			module.setModuleCompiled(false);
			module.setModuleLinked(false);

			StringBuilder sb = new StringBuilder();
			sb.append(tempObjectName);
			sb.append(".");
			sb.append(mbProc.getName());
			sb.append("(");

			for (int i = 0; i < params.length; i++) {
				sb.append(params[i].getValue());
				if (i != params.length - 1) {


					if (!params[i].getName().equals(params[i+1].getName())) {
						sb.append(";");
					} else {
						sb.append(" union ");
					}
				}
			}

			sb.append(");");

			String callCode = sb.toString();

			Result res = this.execSBQL(callCode, mbProc.getModule());
			if (mbProc.getMaxCard() > 1 && (res instanceof SingleResult))
			{
				BagResult bag = new BagResult();
				bag.addElement((SingleResult) res);
				return bag;
			}
			else {
				return res;
			}
		} catch (DatabaseException ex) {
			throw ex;

		}  catch (Exception ex) {
			throw new DatabaseException("Internal Error");

		} finally {
			if (module != null && classInstance != null) {
				classInstance.delete();
			}
			Session.close();
		}
	}

	/** Calls database procedure
	 * @param mbProc metadata object
	 * @param params parameters
	 * @return result of query
	 * @throws DatabaseException
	 */
	public Result callProcedure(MBProcedure mbProc, ParamDef[] params,
			InitializationDef preInitialization) throws DatabaseException {

		try {

			DBModule module = null;
			module = mbProc.getModule();
			procedurePreexecutionInitialization(preInitialization, module);

			module.setModuleCompiled(false);
			module.setModuleLinked(false);

			// building the call
			StringBuilder sb = new StringBuilder();
			sb.append(mbProc.getName());
			sb.append("(");

			for (int i = 0; i < params.length; i++) {
				sb.append(params[i].getValue());
				if (i != params.length - 1) {


					if (!params[i].getName().equals(params[i+1].getName())) {
						sb.append(";");
					} else {
						sb.append(" union ");
					}
				}
			}

			sb.append(");");
			String callCode = sb.toString();

			// execute in the pre-initialized context and return the result
			Result res = this.execSBQL(callCode, mbProc.getModule());
			if (mbProc.getMaxCard() > 1 && (res instanceof SingleResult))
			{
				BagResult bag = new BagResult();
				bag.addElement((SingleResult) res);
				return bag;
			}
			else {
				return res;
			}

		} catch (DatabaseException ex) {
			throw ex;

		}  catch (AuthenticationException ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Web service user context cannot be initialized, check odra configuration. ");
			throw new DatabaseException("Internal web service endpoint eror.");

		} catch (Exception ex) {
			throw new DatabaseException("Internal Error");

		} finally {
			Session.close();
		}
	}

	private void procedurePreexecutionInitialization(
			InitializationDef preInitialization, DBModule module)
			throws DatabaseException, Exception {
		for (String className : preInitialization.getClassNames()) {
			String variableName = "ws" + className;
			module.createSessionMetaVariable(variableName, 0, Integer.MAX_VALUE, className, 0);
		}
		for (String statement : preInitialization.getStatements()) {
			execSBQL(statement, module);
		}
	}
	/**
	 * Executes an sbql ad-hoc query.
	 * @param prog sbql source code to be executed
	 * @param mod module which should be used as a context of the execution
	 */
	public final Result execSBQL(String prog, DBModule mod) throws Exception {

	    if(!Session.exists())
	    {
	    	Session.create();
	    }
	    if(!Session.isInitialized())
	    {
	    	Session.initialize(SBQLHelper.username, SBQLHelper.password);
	    }

		if(!mod.isModuleLinked()) {
			this.linker.linkModule(mod);
		}
		if(!mod.isModuleCompiled()) {
			this.compiler.compileModule(mod);
		}

		ASTNode node = BuilderUtils.parseSBQL(prog);
		if(node instanceof ExpressionStatement)
			node = ((ExpressionStatement)node).getExpression();
		if (node instanceof Expression) {
			if (ConfigServer.TYPECHECKING) {
				this.checker = new SBQLTypeChecker(mod);
				node = this.checker.typecheckAdHocQuery(node);
				//System.out.println(((Expression) node).sign.dump(""));
			}
			this.generator = EmiterFactory.getJulietCodeGenerator(mod);

			this.interpreter = new SBQLInterpreter(mod);

			this.generator.generate(node);


			// no derefs
			JulietCode code = this.generator.getCode();
			byte[] byteCode = JulietGen.genDynDeref(code).getByteCode();

			byte[] cnstPool = this.generator.getConstantPool().getAsBytes();

			this.interpreter.runCode(byteCode, cnstPool);

			return this.interpreter.getResult();
		} else {
			return null;
		}
	}

	/** Expands typedef (one step)
	 * @param type Type to expand
	 * @return Expanded type
	 * @throws DatabaseException
	 */
	public MBObject expandTypeDef(MBObject type) throws DatabaseException {
		MBTypeDef mbtype = new MBTypeDef(type.getOID());

		if (mbtype.isValid()) {
			// if the type of the variable is a typeDef, expand it
			return new MBObject(mbtype.getType());
		}
		else {
			// otherwise leave the type unexpanded
			return type;
		}

	}

	/** Filters name of type
	 * @param type
	 * @return Instance name for classes and name for all other objects.
	 * @throws DatabaseException
	 */
	public String filterTypeName(OID type) throws DatabaseException {
		String paramTypeName;
		MBObject object = new MBObject(type);
		if (object.getObjectKind() == MetaObjectKind.CLASS_OBJECT) {
			paramTypeName = new MBClass(type).getInstanceName();
		} else {
			paramTypeName = object.getName();
		}
		return paramTypeName;
	}

}
