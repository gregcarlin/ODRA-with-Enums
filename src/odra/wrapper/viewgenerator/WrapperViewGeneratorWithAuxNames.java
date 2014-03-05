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
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.wrapper.model.Column;
import odra.wrapper.model.ForeignKey;
import odra.wrapper.model.Table;

/**
 * WrapperViewGeneratorWithAuxNames
 * 
 * @author Radek Adamus last modified: 2007-02-24 since : 2007-01-31
 * @version 1.0
 */
public class WrapperViewGeneratorWithAuxNames extends ViewGenerator {

    private String tableseed;

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
	String seedname = "_" + varName;
	if (isTable)
	    tableseed = seedname;
	else
	    seedname = tableseed + seedname;

	String virtObjName = odra.wrapper.model.Name.o2r(varName);
	String viewDefName = odra.wrapper.model.Name.o2r(varName) + VIEW_SUFFIX;

	// virtual objects procedure
	// because the virtual objects returns named result
	// we need to create special "one field" struct
	OID mstrid = metamanager.createMetaStruct(1);
	MBStruct mbstr = new MBStruct(mstrid);

	// seed generator procedure
	OdraProcedureSchema seed;
	if (isTable) {
	    mbstr.createField(tableseed, 1, 1, mbvar.getName(), 0);
	    seed = this.generateTableVirtualObjectsProcedure(virtObjName,
		    varName, mbstr.getName(), mbvar.getMinCard(), mbvar
			    .getMaxCard());
	} else {
	    mbstr.createField(seedname, 1, 1, odra.wrapper.model.Name.r2o(table
		    .getName())
		    + "." + mbvar.getName(), 0);
	    seed = this.generateColumnVirtualObjectsProcedure(virtObjName,
		    varName, seedname, mbstr.getName(), mbvar.getMinCard(),
		    mbvar.getMaxCard());
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
		    // generated sub-views for columns
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
	    viewInfo.addGenericProcedure(this.generateTableOnRetrieve(
		    tableseed, mbretrstr.getName(), attrNames, subViews));
	    viewInfo.addGenericProcedure(this.generateTableOnDelete(tableseed));

	} else {
	    String tname;

	    OID[] fields = new MBStruct(mbvar.getType()).getFields();
	    assert fields.length == 1 : "only one structure field expected (_VALUE)";
	    tname = new MBVariable(fields[0]).getTypeName();

	    viewInfo.addGenericProcedure(this.generateColumnOnRetrieve(tname,
		    seedname));
	    viewInfo.addGenericProcedure(this.generateColumnOnUpdate(varName,
		    tname, seedname));
	    OdraProcedureSchema navigate = this.generateVirtualPointer(varName,
		    seedname);
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

	return createProcedure(virtObjName, new ProcArgument[0],
		new ReturnWithValueStatement(new AsExpression(
			nameExpr(tableName), new Name(tableseed))),
		new OdraTypeSchema(resultType, minc, maxc, 0));

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
	    String virtObjName, String columnName, String seedname,
	    String resultType, int minc, int maxc) throws Exception {

	return createProcedure(virtObjName, new ProcArgument[0],
		new ReturnWithValueStatement(new AsExpression(
			new DotExpression(nameExpr(tableseed),
				nameExpr(columnName)), new Name(seedname))),
		new OdraTypeSchema(resultType, minc, maxc, 0));

    }

    private final OdraProcedureSchema generateColumnOnRetrieve(
	    String resultType, String seedname) throws Exception {

	Expression valueAccessExpression = new DotExpression(
		nameExpr(seedname), nameExpr(_value));

	return createProcedure(
		OdraViewSchema.GenericNames.ON_RETRIEVE_NAME.toString(),
		new ProcArgument[0], new ReturnWithValueStatement(
			new DerefExpression(valueAccessExpression)),
		new OdraTypeSchema(resultType, 1, 1, 0));

    }

    private final OdraProcedureSchema generateColumnOnUpdate(String columnName,
	    String argumentType, String seedname) throws Exception {
	String argumentName = "new" + columnName;
	ProcArgument updArgument = new ProcArgument(argumentName, argumentType, 1, 1, 0);

	Expression lvalueAccessExpression = new DotExpression(
		nameExpr(seedname), nameExpr(_value));

	Expression valueUpdateExpression = new AssignExpression(
		lvalueAccessExpression, nameExpr(argumentName),
		Operator.opAssign);

	return createProcedure(
		OdraViewSchema.GenericNames.ON_UPDATE_NAME.toString(),
		new ProcArgument[] { updArgument }, 
			new ExpressionStatement(valueUpdateExpression),
		new OdraTypeSchema("void", 1, 1, 0));

    }

    private final OdraProcedureSchema generateVirtualPointer(String columnName,
	    String seedname) throws Exception {
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
		.getName()), seedname);

    }

    private final OdraProcedureSchema generateColumnOnNavigate(
	    String targetTableName, String keyColumnName, String seedname)
	    throws Exception {

	Expression valueAccessExpression = new DerefExpression(
		new DotExpression(nameExpr(seedname), nameExpr(_value)));

	Expression targetTableExpression = nameExpr(targetTableName);

	Expression targetColumnAccessExpression = nameExpr(keyColumnName);

	Expression equalityOnExpression = new EqualityExpression(
		targetColumnAccessExpression, valueAccessExpression,
		Operator.opEquals);

	return createProcedure(
		OdraViewSchema.GenericNames.ON_NAVIGATE_NAME.toString(),
		new ProcArgument[0], 
		// new ReturnWithValueStatement( new AsExpression(
			// new WhereExpression(targetTableExpression,
			// joinOnExpression), new Name(targetTableName +
			// VIRTUAL_OBJECT_SUFFIX)))),
			new ReturnWithValueStatement(new WhereExpression(
				targetTableExpression, equalityOnExpression)),
		new OdraTypeSchema(targetTableName, 1, 1, 0));

    }

}
