package odra.wrapper.viewgenerator;

import java.util.List;

import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.filters.XML.XMLImportFilter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.builder.BuilderUtils;
import odra.wrapper.model.ForeignKey;
import odra.wrapper.model.Table;

/**
 * This is base class for views generators implementing different view
 * generation strategies The class arise because we are not quite sure which of
 * the strategies will be the best (so this is typical 'research' class
 * hierarchy :) ) ViewGenerator
 * 
 * @author Radek Adamus last modified: 2007-03-01
 * @version 1.0
 */
public abstract class ViewGenerator {
    public static ViewGenerator getDefaultViewGenerator() {
	return new WrapperViewGeneratorAuxNavigation();
    }

    protected ViewGenerator() {
    }

    /**
     * Generates view schema definition info
     * 
     * @param mod -
     *                wrapper module
     * @param mbvar
     *                metavariable representing relational table/view
     *                metainformation in the ODRA metamodel
     * @param table -
     *                relational table info
     * @return view schema definition info
     * @throws Exception
     */
    public OdraViewSchema generateViewForRelationalTable(DBModule mod,
	    MBVariable mbvar, Table table) throws Exception {
	assert mod.isModuleLinked() : "module must be linked before generating views";
	this.table = table;
	this.metamanager = new MetabaseManager(mod);
	fks = table.getForeignKeys().values().toArray(
		new ForeignKey[table.getForeignKeys().size()]);

	return this.generateView(mbvar, mod, true);
    }

    protected OdraProcedureSchema generateTableOnRetrieve(String tableseed,
	    String resultType, List<String> names, OdraViewSchema[] subViews)
	    throws Exception {
	Expression expr;
	expr = new AsExpression(new DerefExpression(new DotExpression(this
		.nameExpr(tableseed), new DotExpression(nameExpr(names.get(0)),
		nameExpr(_value)))), new Name(odra.wrapper.model.Name.o2r(names
		.get(0))));
	for (int i = 1; i < subViews.length; i++) {
	    expr = new CommaExpression(expr, new AsExpression(
		    new DerefExpression(new DotExpression(this
			    .nameExpr(tableseed), new DotExpression(
			    nameExpr(names.get(i)), nameExpr(_value)))),
		    new Name(odra.wrapper.model.Name.o2r(names.get(i)))));

	}

	return new OdraProcedureSchema(
		OdraViewSchema.GenericNames.ON_RETRIEVE_NAME.toString(),
		new ProcArgument[0], new ProcedureAST(BuilderUtils.serializeAST(new ReturnWithValueStatement(
			expr))), new OdraTypeSchema(resultType, 1, 1, 0));
    }

    protected final OdraProcedureSchema generateTableOnDelete(String seedname)
	    throws Exception {
	Expression expr = new DeleteExpression(new NameExpression(new Name(
		seedname)));
	return createProcedure(
		OdraViewSchema.GenericNames.ON_DELETE_NAME.toString(),
		new ProcArgument[0],
		new ExpressionStatement(expr), new OdraTypeSchema(
			"void", 1, 1, 0));

    }

    protected OdraProcedureSchema generateTableOnNew(String tableName,
	    String argumentType, List<String> names,
	    OdraViewSchema[] subViews) {
	ProcArgument updArgument = new ProcArgument(ARGUMENT_NAME, argumentType, 0, Integer.MAX_VALUE, 0);
	Expression expr;
	expr = new AsExpression(new AsExpression(new DotExpression(this
		.nameExpr(ARGUMENT_NAME), nameExpr(odra.wrapper.model.Name
		.o2r(names.get(0)))), new Name(_value)), new Name(names.get(0)));
	for (int i = 1; i < subViews.length; i++) {
	    expr = new CommaExpression(expr, new AsExpression(
		    new AsExpression(
			    new DotExpression(this.nameExpr(ARGUMENT_NAME),
				    nameExpr(odra.wrapper.model.Name.o2r(names
					    .get(i)))), new Name(_value)),
		    new Name(names.get(i))));
	}

	expr = new CreateExpression(new Name(tableName), expr);

	return createProcedure(OdraViewSchema.GenericNames.ON_NEW_NAME
		.toString(), new ProcArgument[] { updArgument }, new ExpressionStatement(expr), new OdraTypeSchema("void", 1, 1, 0));
    }

    protected Expression nameExpr(String name) {
	return new NameExpression(new Name(name));
    }

    protected abstract OdraViewSchema generateView(MBVariable mbvar,
	    DBModule mod, boolean isTable) throws Exception;

    /**
     * @param virtObjName
     * @param procArgs
     * @param returnWithValueStatement
     * @param typeDescriptor
     * @return
     */
    protected OdraProcedureSchema createProcedure(String name,
	    ProcArgument[] procArgs,
	    Statement statement,
	    OdraTypeSchema typeDescriptor) {
	
	return new OdraProcedureSchema(name, procArgs, new ProcedureAST(BuilderUtils.serializeAST(statement)), typeDescriptor);
    }
    public final static String VIEW_SUFFIX = "Def";

    protected final static String ARGUMENT_NAME = OdraViewSchema.DEFAULT_PARAM_NAME;

    protected final String _value = XMLImportFilter.PCDATA;

    protected Table table;
    protected MetabaseManager metamanager;

    protected ForeignKey[] fks;
}
