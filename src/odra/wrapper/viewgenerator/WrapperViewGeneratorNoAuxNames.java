package odra.wrapper.viewgenerator;

import java.util.List;
import java.util.Vector;

import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.wrapper.model.Column;
import odra.wrapper.model.ForeignKey;
import odra.wrapper.model.Table;

/**
 * WrapperViewGeneratorNoAuxNames
 * 
 * @author Radek Adamus last modified: 2007-02-24 since : 2007-01-31
 * @version 1.0
 */
public class WrapperViewGeneratorNoAuxNames extends ViewGenerator {

    /**
     * Main view generator method
     * 
     * @param mbvar
     *                metavariable representing relational table/view or column
     *                metainformation in the ODRA metamodel
     * @param mod -
     *                wrapper module
     * @param isTable -
     *                true if the mbvar represents table, false if column
     * @return view schema definition info to feed View Schema Manager
     * @throws Exception
     */
    protected OdraViewSchema generateView(MBVariable mbvar, DBModule mod,
	    boolean isTable) throws Exception {
	assert mod.isModuleLinked() : "module must be linked before generating views";
	String varName = mbvar.getName();
	// String seedname = "_" + varName;
	// if(isTable ) tableseed = seedname;

	String virtObjName = odra.wrapper.model.Name.o2r(varName);
	String viewDefName = odra.wrapper.model.Name.o2r(varName) + VIEW_SUFFIX;

	// seed generator procedure
	OdraProcedureSchema seed;
	if (isTable) {
	    seed = this.generateTableVirtualObjectsProcedure(virtObjName,
		    varName, mbvar.getName(), mbvar.getMinCard(), mbvar
			    .getMaxCard());
	} else {
	    seed = this.generateColumnVirtualObjectsProcedure(virtObjName,
		    varName, odra.wrapper.model.Name.r2o(table.getName()) + "."
			    + mbvar.getName(), mbvar.getMinCard(), mbvar
			    .getMaxCard());
	}
	OdraViewSchema viewInfo = new OdraViewSchema(viewDefName, seed);
	// subviews only if it is a view for table
	if (isTable) {
	    Vector<String> attrNames = new Vector<String>();
	    for (OID attr : new MBStruct(new MBTypeDef(mbvar.getType())
		    .getType()).getFields()) {
		// check if the field name corresponds to table column name
		if (table.getColumn(odra.wrapper.model.Name.o2r(attr
			.getObjectName())) != null) {
		    // generater sub-views for columns
		    OdraViewSchema subView = this.generateView(new MBVariable(
			    attr), mod, false);
		    viewInfo.addViewField(subView);
		    attrNames.add(attr.getObjectName());
		}
	    }
	    // create generic procedures for complex view
	    // on_retrieve (the result is a dereference of sub-views)
	    OID mretrstrid = metamanager.createMetaStruct(
		    viewInfo.getSubViews().length);
	    MBStruct mbretrstr = new MBStruct(mretrstrid);
	    OdraViewSchema[] subViews = viewInfo.getSubViews();
	    for (int i = 0; i < subViews.length; i++) {
		OdraProcedureSchema retr = subViews[i].getGenericProcedure(
			OdraViewSchema.GenericNames.ON_RETRIEVE_NAME);
		mbretrstr.createField(attrNames.get(i), retr.getMincard(), retr
			.getMaxcard(), retr.getTypeName(), retr.getRefs());
	    }
	    viewInfo.addGenericProcedure(this.generateTableOnRetrieve(null,
		    mbretrstr.getName(), attrNames, subViews));

	} else {
	    String tname;
	    // we assume that we have a _VALUE subbobject

	    OID[] fields = new MBStruct(mbvar.getType()).getFields();
	    assert fields.length == 1 : "only one structure field expected ("
		    + _value + ")";
	    tname = new MBVariable(fields[0]).getTypeName();
	    viewInfo.addGenericProcedure(this.generateColumnOnRetrieve(tname));
	    viewInfo.addGenericProcedure(this.generateColumnOnUpdate(varName,
		    tname));
	    OdraProcedureSchema navigate = this.generateVirtualPointer(varName);
	    if (navigate != null)
		viewInfo.addGenericProcedure(navigate);
	}

	return viewInfo;
    }

    /**
     * Generates virtual object procedure for view on table
     * 
     * @param virtObjName -
     *                name for virtual object
     * @param tableName -
     *                name of table which the view cover
     * @param resultType -
     *                result type name
     * @param minc -
     *                minimal result cardinality
     * @param maxc -
     *                maximal result cardinality
     * @return procedure schema definition info
     * @throws Exception
     */
    private final OdraProcedureSchema generateTableVirtualObjectsProcedure(
	    String virtObjName, String tableName, String resultType, int minc,
	    int maxc) throws Exception {

	return this.generateVirtualObjectsProcedure(virtObjName, tableName,
		resultType, minc, maxc);
    }

    /**
     * Generates virtual object procedure for view on column
     * 
     * @param virtObjName -
     *                name for virtual object
     * @param columnName -
     *                name of column which the view cover
     * @param resultType -
     *                result type name
     * @param minc -
     *                minimal result cardinality
     * @param maxc -
     *                maximal result cardinality
     * @return procedure schema definition info
     * @throws Exception
     */
    private final OdraProcedureSchema generateColumnVirtualObjectsProcedure(
	    String virtObjName, String columnName, String resultType, int minc,
	    int maxc) throws Exception {

	return this.generateVirtualObjectsProcedure(virtObjName, columnName,
		resultType, minc, maxc);
    }

    private final OdraProcedureSchema generateVirtualObjectsProcedure(
	    String virtObjName, String name, String resultType, int minc,
	    int maxc) throws Exception {
	return createProcedure(virtObjName, new ProcArgument[0],

	new ReturnWithValueStatement(nameExpr(name)), new OdraTypeSchema(
		resultType, minc, maxc, 0));
    }

    private final OdraProcedureSchema generateColumnOnRetrieve(String resultType)
	    throws Exception {

	Expression valueAccessExpression = nameExpr(_value);

	return createProcedure(OdraViewSchema.GenericNames.ON_RETRIEVE_NAME
		.toString(), new ProcArgument[0],

		new ReturnWithValueStatement(new DerefExpression(
			valueAccessExpression)), new OdraTypeSchema(resultType,
			1, 1, 0));

    }

    private final OdraProcedureSchema generateColumnOnUpdate(String columnName,
	    String argumentType) throws Exception {
	String argumentName = "new" + columnName;
	ProcArgument updArgument = new ProcArgument(argumentName, argumentType, 1, 1, 0);
	Expression valueUpdateExpression = new AssignExpression(
		nameExpr(_value), nameExpr(argumentName), Operator.opAssign);

	return createProcedure(OdraViewSchema.GenericNames.ON_UPDATE_NAME
		.toString(), new ProcArgument[] { updArgument },
		new ExpressionStatement(valueUpdateExpression),
		new OdraTypeSchema("void", 1, 1, 0));

    }

    private final OdraProcedureSchema generateVirtualPointer(String columnName)
	    throws Exception {
	ForeignKey fk = null;
	for (ForeignKey tfk : this.fks) {
	    List<Column> locColumns = tfk.getLocalColumns();
	    if (locColumns.size() == 1) {
		if (locColumns.get(0).getName().compareTo(columnName) == 0)
		    fk = tfk;
		break;
	    }
	}
	if (fk == null)
	    return null;

	// only if single column foregin key
	Table refTable = fk.getRefTable();
	Column refColumn = fk.getRefColumns().get(0);
	return this.generateColumnOnNavigate(odra.wrapper.model.Name
		.r2o(refTable.getName()), odra.wrapper.model.Name.r2o(refColumn
		.getName()));

    }

    // private final SchemaProcedureInfo generateColumnOnNavigate( String
    // targetTableName, String keyColumnName) throws Exception{
    //		
    // Name val = new Name("_VALUE");
    // Expression valueAccessExpression = new DerefExpression(
    // new NameExpression(val));
    // Expression targetTableExpression = new NameExpression(new
    // Name(targetTableName));
    //		
    // Expression targetColumnValueAccessExpression = new DotExpression(new
    // NameExpression(new Name(keyColumnName)),
    // new DerefExpression(new NameExpression(val)));
    //		
    // Expression joinOnExpression = new EqualityExpression(
    // targetColumnValueAccessExpression,
    // valueAccessExpression, Operator.opEquals);
    //		
    // return createProcedure(
    // SchemaViewInfo.ON_NAVIGATE_NAME,
    // new ProcArg[0],
    // BuilderUtils.serializeAST(
    // new ReturnWithValueStatement(
    // new WhereExpression(
    // targetTableExpression, joinOnExpression))),
    // new byte[0],
    // new byte[0],
    // new byte[0],
    // new ProcRes(targetTableName, 1, 1, 0));
    //		
    //		
    // }

    private final OdraProcedureSchema generateColumnOnNavigate(
	    String targetTableName, String keyColumnName) throws Exception {

	Name val = new Name("_VALUE");
	Expression valueAccessExpression = new DerefExpression(
		new NameExpression(val));
	Expression targetTableExpression = nameExpr(targetTableName);

	Expression targetColumnAccessExpression = nameExpr(keyColumnName);

	Expression joinOnExpression = new EqualityExpression(
		targetColumnAccessExpression, valueAccessExpression,
		Operator.opEquals);

	return createProcedure(
		OdraViewSchema.GenericNames.ON_NAVIGATE_NAME.toString(),
		new ProcArgument[0],

		// new ReturnWithValueStatement( new AsExpression(
		// new WhereExpression(targetTableExpression, joinOnExpression),
		// new Name(targetTableName + VIRTUAL_OBJECT_SUFFIX)))),
		new ReturnWithValueStatement(new WhereExpression(
			targetTableExpression, joinOnExpression)),
		new OdraTypeSchema(targetTableName, 1, 1, 0));

    }

}
