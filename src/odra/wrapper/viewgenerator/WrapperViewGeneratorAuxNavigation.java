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
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;

public class WrapperViewGeneratorAuxNavigation extends ViewGenerator {

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
	boolean isFK = false;
	String varName = mbvar.getName();
	String seedname = "_" + varName;
	if (isTable)
	    tableseed = seedname;
	// else {
	// if(this.isVirtualPointer(varName))
	// isFK = true;
	// seedname = tableseed + seedname;
	// }

	String virtObjName = odra.wrapper.model.Name.o2r(varName);
	String viewDefName = odra.wrapper.model.Name.o2r(varName) + VIEW_SUFFIX;

	OdraProcedureSchema seed;
	if (isTable) {
	    OID mstrid = metamanager.createMetaStruct(1);
	    MBStruct mbstr = new MBStruct(mstrid);
	    mbstr.createField(tableseed, 1, 1, mbvar.getName(), 0);
	    seed = this.generateTableVirtualObjectsProcedure(virtObjName,
		    varName, mbstr.getName(), mbvar.getMinCard(), mbvar
			    .getMaxCard());

	} else {
	    String typename;
	    if (isFK) {
		OID mstrid = metamanager.createMetaStruct(1);
		MBStruct mbstr = new MBStruct(mstrid);
		mbstr.createField(seedname, 1, 1, odra.wrapper.model.Name
			.r2o(table.getName())
			+ "." + mbvar.getName(), 0);
		typename = mbstr.getName();
		seed = this.generateColumnVirtualObjectsProcedure(virtObjName,
			varName, seedname, typename, mbvar.getMinCard(), mbvar
				.getMaxCard());
	    } else {
		typename = odra.wrapper.model.Name.r2o(table.getName()) + "."
			+ mbvar.getName();
		seed = this.generateColumnVirtualObjectsProcedure(virtObjName,
			varName, null, typename, mbvar.getMinCard(), mbvar
				.getMaxCard());
	    }
	}
	OdraViewSchema viewInfo = new OdraViewSchema(viewDefName, seed);

	// subviews only if it is a view for table
	if (isTable) {
	    Vector<String> attrNames = new Vector<String>();
	    for (OID attr : new MBStruct(new MBTypeDef(mbvar.getType())
		    .getType()).getFields()) {
		// check if the field name corresponds to table column name
		if (table.containsColumn(odra.wrapper.model.Name.o2r(attr
			.getObjectName()))) {
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
		mbretrstr.createField(odra.wrapper.model.Name.o2r(attrNames
			.get(i)), retr.getMincard(), retr.getMaxcard(), retr
			.getTypeName(), retr.getRefs());
	    }
	    viewInfo.setVirtualObject(new OdraVariableSchema(virtObjName,
		    mbretrstr.getName(), mbvar.getMinCard(),
		    mbvar.getMaxCard(), mbvar.getRefIndicator()));
	    viewInfo.addGenericProcedure(this.generateTableOnRetrieve(
		    tableseed, mbretrstr.getName(), attrNames, subViews));
	    viewInfo.addGenericProcedure(this.generateTableOnDelete(tableseed));
	    viewInfo.addGenericProcedure(this.generateTableOnNew(varName,
		    mbretrstr.getName(), attrNames, subViews));
	} else {
	    String tname;
	    OID[] fields = new MBStruct(mbvar.getType()).getFields();
	    assert fields.length == 1 : "only one structure field expected (_VALUE)";
	    tname = new MBVariable(fields[0]).getTypeName();

	    viewInfo.setVirtualObject(new OdraVariableSchema(virtObjName,
		    tname, mbvar.getMinCard(), mbvar.getMaxCard(), mbvar
			    .getRefIndicator()));
	    viewInfo.addGenericProcedure(this.generateColumnOnRetrieve(tname,
		    isFK ? seedname : null));
	    viewInfo.addGenericProcedure(this.generateColumnOnUpdate(varName,
		    tname, isFK ? seedname : null));
	    // if(isFK){
	    // SchemaProcedureInfo navigate =
	    // this.generateVirtualPointer(varName, seedname);
	    // if(navigate!= null) viewInfo.addGenericProcedure(navigate);
	    // }
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
	Expression returnExpr = seedname != null ? new AsExpression(
		new DotExpression(nameExpr(tableseed), nameExpr(columnName)),
		new Name(seedname)) : new DotExpression(nameExpr(tableseed),
		nameExpr(columnName));
	return createProcedure(virtObjName, new ProcArgument[0],
		new ReturnWithValueStatement(returnExpr),
		new OdraTypeSchema(resultType, minc, maxc, 0));

    }

    private final OdraProcedureSchema generateColumnOnRetrieve(
	    String resultType, String seedname) throws Exception {

	Expression valueAccessExpression = seedname != null ? new DotExpression(
		nameExpr(seedname), nameExpr(_value))
		: nameExpr(_value);

	return createProcedure(
		OdraViewSchema.GenericNames.ON_RETRIEVE_NAME.toString(),
		new ProcArgument[0], new ReturnWithValueStatement(
			new DerefExpression(valueAccessExpression)),
		new OdraTypeSchema(resultType, 1, 1, 0));

    }

    private final OdraProcedureSchema generateColumnOnUpdate(String columnName,
	    String argumentType, String seedname) throws Exception {
	ProcArgument updArgument = new ProcArgument(ARGUMENT_NAME, argumentType, 1, 1, 0);

	Expression lvalueAccessExpression = seedname != null ? new DotExpression(
		nameExpr(seedname), nameExpr(_value))
		: nameExpr(_value);

	Expression valueUpdateExpression = new AssignExpression(
		lvalueAccessExpression, nameExpr(ARGUMENT_NAME),
		Operator.opAssign);

	return createProcedure(
		OdraViewSchema.GenericNames.ON_UPDATE_NAME.toString(),
		new ProcArgument[] { updArgument }, 
			new ExpressionStatement(valueUpdateExpression),
		new OdraTypeSchema("void", 1, 1, 0));

    }
    // private final SchemaProcedureInfo generateVirtualPointer(String
    // columnName, String seedname) throws Exception{
    // ForeignKey fk = null;
    // for(ForeignKey tfk: this.fks){
    // List<Column> locColumns = tfk.getLocalColumns();
    // if(locColumns.size() == 1){
    // if(locColumns.get(0).getName().compareTo(columnName)==0)
    // fk = tfk;
    // break;
    // }
    // }
    // if(fk == null)
    // return null;
    //			
    // //only if single column foregin key
    // Table refTable = fk.getRefTable();
    // Column refColumn = fk.getRefColumns().get(0);
    // return
    // this.generateColumnOnNavigate(odra.wrapper.model.Name.r2o(refTable.getName()),
    // odra.wrapper.model.Name.r2o(refColumn.getName()), seedname);
    //			
    //		
    // }

    // private final boolean isVirtualPointer(String columnName) throws
    // Exception{
    // ForeignKey fk = null;
    // for(ForeignKey tfk: this.fks){
    // List<Column> locColumns = tfk.getLocalColumns();
    // if(locColumns.size() == 1){
    // if(locColumns.get(0).getName().compareTo(columnName)==0)
    // fk = tfk;
    // break;
    // }
    // }
    // if(fk == null)
    // return false;
    //			
    // return true;
    // }

    // private final SchemaProcedureInfo generateColumnOnNavigate( String
    // targetTableName, String keyColumnName, String seedname) throws Exception{
    //		
    //		
    // Expression valueAccessExpression = new DerefExpression(new
    // DotExpression(nameExpr(seedname),
    // nameExpr(_value)));
    //		
    // Expression targetTableExpression = nameExpr(targetTableName);
    //		
    // Expression targetColumnAccessExpression = nameExpr(keyColumnName);
    //		
    // Expression equalityOnExpression = new EqualityExpression(
    // targetColumnAccessExpression,
    // valueAccessExpression, Operator.opEquals);
    //		
    // return new SchemaProcedureInfo(
    // SchemaViewInfo.ON_NAVIGATE_NAME,
    // new ProcArg[0],
    // BuilderUtils.serializeAST(
    // // new ReturnWithValueStatement( new AsExpression(
    // // new WhereExpression(targetTableExpression, joinOnExpression), new
    // Name(targetTableName + VIRTUAL_OBJECT_SUFFIX)))),
    // new ReturnWithValueStatement(
    // new WhereExpression(targetTableExpression, equalityOnExpression))),
    // new byte[0],
    // new byte[0],
    // new byte[0],
    // new ProcRes(targetTableName, 1, 1, 0));
    //		
    //		
    // }

}
