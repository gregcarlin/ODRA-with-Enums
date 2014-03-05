/**
 * 
 */
package odra.sbql.ast;

import odra.sbql.SBQLException;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.Declaration;
import odra.sbql.ast.declarations.InterfaceDeclaration;
import odra.sbql.ast.declarations.InterfaceFieldDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.ModuleDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.ProcedureFieldDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration;
import odra.sbql.ast.declarations.RecordDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefFieldDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.SingleArgumentDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.SingleImportDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;

/**
 * TraversingDeclarationAdapter
 Visitor performs tree traversal for Declarations
 * auxiliary methods for generalizing common behavior of all Declarations, FieldDeclarations
 * @author Radek Adamus
 *@since 2008-04-25
 *last modified: 2008-04-25
 *@version 1.0
 */
public class TraversingDeclarationAdapter extends ASTAdapter {
    protected Object commonVisitDeclaration(Declaration decl, Object attr)
	throws SBQLException{
		return null;
	}
	
	protected Object commonVisitSingleFieldDeclaration(SingleFieldDeclaration decl, Object attr)
	throws SBQLException{
	    	decl.getDeclaration().accept(this, attr);
		return commonVisitDeclaration(decl, attr);
	}
    /* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitClassDeclaration(odra.sbql.ast.declarations.ClassDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitClassDeclaration(ClassDeclaration decl, Object attr)
		throws SBQLException {
	    decl.getInstanceDeclaration().accept(this, attr);
	    for(SingleFieldDeclaration d : decl.getFieldsDeclaration())
		d.accept(this, attr);	    
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitClassFieldDeclaration(odra.sbql.ast.declarations.ClassFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitClassFieldDeclaration(ClassFieldDeclaration decl,
		Object attr) throws SBQLException {	    
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitClassInstanceTypeDeclaration(odra.sbql.ast.declarations.ClassInstanceTypeDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitClassInstanceDeclaration(
		ClassInstanceDeclaration decl, Object attr)
		throws SBQLException {
	    decl.getInstanceType().accept(this, attr);
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDbLinkDeclaration(odra.sbql.ast.declarations.DbLinkDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitExternalSchemaDefDeclaration(ExternalSchemaDefDeclaration decl, Object attr)
		throws SBQLException {	    
	    
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitDbLinkFieldDeclaration(odra.sbql.ast.declarations.DbLinkFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitExternalSchemaDefFieldDeclaration(ExternalSchemaDefFieldDeclaration decl,
		Object attr) throws SBQLException {	    
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitInterfaceDeclaration(odra.sbql.ast.declarations.InterfaceDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitInterfaceDeclaration(InterfaceDeclaration decl,
		Object attr) throws SBQLException {
	    for(SingleFieldDeclaration d :decl.getInterfaceBody().getFields())
		d.accept(this, attr);
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitInterfaceFieldDeclaration(odra.sbql.ast.declarations.InterfaceFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitInterfaceFieldDeclaration(
		InterfaceFieldDeclaration decl, Object attr)
		throws SBQLException {
	    
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitMethodFieldDeclaration(odra.sbql.ast.declarations.MethodFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitMethodFieldDeclaration(MethodFieldDeclaration decl,
		Object attr) throws SBQLException {	    
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitModuleDeclaration(odra.sbql.ast.declarations.ModuleDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitModuleDeclaration(ModuleDeclaration decl, Object attr)
		throws SBQLException {	    	    
	    for(SingleFieldDeclaration d : decl.getModuleBody().getFieldDeclaration())
		d.accept(this, attr);	    
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitNamedTypeDeclaration(odra.sbql.ast.declarations.NamedTypeDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitNamedTypeDeclaration(NamedTypeDeclaration decl,
		Object attr) throws SBQLException {
	    // TODO Auto-generated method stub
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitProcedureDeclaration(odra.sbql.ast.declarations.ProcedureDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitProcedureDeclaration(ProcedureDeclaration decl,
		Object attr) throws SBQLException {
	    decl.getProcedureHeader().accept(this, attr);
	    
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitProcedureFieldDeclaration(odra.sbql.ast.declarations.ProcedureFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitProcedureFieldDeclaration(
		ProcedureFieldDeclaration decl, Object attr)
		throws SBQLException {
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitRecordDeclaration(odra.sbql.ast.declarations.RecordDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitRecordDeclaration(RecordDeclaration decl, Object attr)
		throws SBQLException {
	    for(SingleFieldDeclaration d :decl.getFieldsDeclaration()){
		d.accept(this, attr);
	    }
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitRecordTypeDeclaration(odra.sbql.ast.declarations.RecordTypeDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitRecordTypeDeclaration(RecordTypeDeclaration decl,
		Object attr) throws SBQLException {
	    
	    for(SingleFieldDeclaration d :decl.getRecordTypeFields()){
		d.accept(this, attr);
	    }
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitSessionVariableDeclaration(odra.sbql.ast.declarations.SessionVariableDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitSessionVariableFieldDeclaration(
		SessionVariableFieldDeclaration node, Object attr)
		throws SBQLException {
	    
	    return commonVisitSingleFieldDeclaration(node, attr);
	}

	

	

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitTypeDefDeclaration(odra.sbql.ast.declarations.TypeDefDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitTypeDefDeclaration(TypeDefDeclaration decl,
		Object attr) throws SBQLException {
	    decl.getTypeDeclaration().accept(this, attr);
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitTypeDefFieldDeclaration(odra.sbql.ast.declarations.TypeDefFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitTypeDefFieldDeclaration(
		TypeDefFieldDeclaration decl, Object attr) throws SBQLException {
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitVariableDeclaration(odra.sbql.ast.declarations.VariableDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitVariableDeclaration(VariableDeclaration decl,
		Object attr) throws SBQLException {
	    decl.getType().accept(this, attr);
	    decl.getInitExpression().accept(this, attr);	   
	    return this.commonVisitDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitVariableFieldDeclaration(odra.sbql.ast.declarations.VariableFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitVariableFieldDeclaration(
		VariableFieldDeclaration decl, Object attr)
		throws SBQLException {
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitViewDeclaration(odra.sbql.ast.declarations.ViewDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitViewDeclaration(ViewDeclaration decl, Object attr)
		throws SBQLException {
	    decl.getBody().getSeedProcedure().accept(this, attr);
	    decl.getBody().getVirtualObjectDeclaration().accept(this, attr);
	    for(SingleFieldDeclaration d :decl.getBody().getFields())
		d.accept(this, attr);
	    return super.visitViewDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitViewFieldDeclaration(odra.sbql.ast.declarations.ViewFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitViewFieldDeclaration(ViewFieldDeclaration decl,
		Object attr) throws SBQLException {
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitProcedureHeaderDeclaration(odra.sbql.ast.declarations.ProcedureHeader, java.lang.Object)
	 */
	@Override
	public Object visitProcedureHeaderDeclaration(ProcedureHeaderDeclaration decl,
		Object attr) throws SBQLException {
	    decl.getProcedureResult().getResultType().accept(this, attr);
	    for(SingleArgumentDeclaration d : decl.getProcedureArguments())
		d.accept(this, attr);
	    return super.visitProcedureHeaderDeclaration(decl, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitProcedureHeaderFieldDeclaration(odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitProcedureHeaderFieldDeclaration(
		ProcedureHeaderFieldDeclaration decl, Object attr)
		throws SBQLException {
	    // TODO Auto-generated method stub
	    return this.commonVisitSingleFieldDeclaration(decl, attr);
	}

}
