/**
 * 
 */
package odra.sbql.builder.views;

import odra.db.schema.OdraObjectSchema;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
import odra.sbql.builder.ModuleConstructor;

/**
 * ViewConstructor
 * responsible for creating view schema information from the source code
 * @author Radek Adamus
 * @since 2007-05-06 last modified: 2007-05-06
 * @version 1.0
 */
public class ViewConstructor extends ASTAdapter {

    private ModuleConstructor modconstr;


    public ViewConstructor(ModuleConstructor modconstr) {
	this.modconstr = modconstr;
	this.setSourceModuleName(modconstr.getSourceModuleName());
	
    }

    /*
         * (non-Javadoc)
         * 
         * @see odra.sbql.ast.ASTAdapter#visitViewDeclaration(odra.sbql.ast.declarations.ViewDeclaration,
         *      java.lang.Object)
         */
    @Override
    public OdraViewSchema visitViewDeclaration(ViewDeclaration decl, Object attr)
	    throws SBQLException {
	
	// seed procedure
	OdraProcedureSchema seed = modconstr
		.createSchemaProcedureInfo(decl.getBody().getSeedProcedure());

	OdraViewSchema  viewInfo = new OdraViewSchema(decl.getViewName(), seed);
	//visit virtual object type declaration
	VariableDeclaration voDecl = decl.getBody().getVirtualObjectDeclaration();
	voDecl.getType().accept(this.modconstr, attr);
	
	//create virtual object description
	OdraVariableSchema virtualObject = new OdraVariableSchema(voDecl.getName(), voDecl.getType().getTypeName(), voDecl.getCardinality().getMinCard(), voDecl.getCardinality().getMaxCard(), voDecl.getReflevel());
	viewInfo.setVirtualObject(virtualObject);
	
	// generic procedures
	for(OdraViewSchema.GenericNames gn :OdraViewSchema.GenericNames.values()){
	    ProcedureDeclaration genProcDecl = decl.getBody().getGenericProcedure(gn);
	    if(genProcDecl != null) {
		OdraProcedureSchema genericOperator = modconstr
		.createSchemaProcedureInfo(genProcDecl);
		viewInfo.addGenericProcedure(genericOperator);
	    }
	}
	
	for(SingleFieldDeclaration d :decl.getBody().getFields())
	    viewInfo.addViewField((OdraObjectSchema)d.accept(this, attr));
	return viewInfo;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.ASTAdapter#visitVariableDeclaration(odra.sbql.ast.declarations.VariableDeclaration, java.lang.Object)
     */
    @Override
    public OdraVariableSchema visitVariableDeclaration(VariableDeclaration decl, Object attr) throws SBQLException {
	decl.getType().accept(this.modconstr, attr);
	
	return new OdraVariableSchema(decl.getName(), decl.getType().getTypeName(), decl.getCardinality().getMinCard(), decl.getCardinality().getMaxCard(), decl.getReflevel());
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.ASTAdapter#visitVariableFieldDeclaration(odra.sbql.ast.declarations.VariableFieldDeclaration, java.lang.Object)
     */
    @Override
    public Object visitVariableFieldDeclaration(VariableFieldDeclaration node,
	    Object attr) throws SBQLException {
	
	return node.getVariableDeclaration().accept(this, attr);
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.ASTAdapter#visitViewFieldDeclaration(odra.sbql.ast.declarations.ViewFieldDeclaration, java.lang.Object)
     */
    @Override
    public Object visitViewFieldDeclaration(ViewFieldDeclaration node,
	    Object attr) throws SBQLException {	
	return node.getViewDeclaration().accept(this, attr);
    }

}
