/**
 * 
 */
package odra.sbql.ast;

import java.util.Stack;

import odra.sbql.SBQLException;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.Declaration;
import odra.sbql.ast.declarations.InterfaceDeclaration;
import odra.sbql.ast.declarations.InterfaceFieldDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.ModuleDeclaration;
import odra.sbql.ast.declarations.NamedSingleImportDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.ProcedureFieldDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration;
import odra.sbql.ast.declarations.RecordDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.ReverseVariableDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefFieldDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.SingleArgumentDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.SingleImplementDeclaration;
import odra.sbql.ast.declarations.SingleImportDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;

/**
 * AST2TextDeclarationDumper
 * 
 * @author Radek Adamus
 * @since 2008-04-27 last modified: 2008-04-27
 * @version 1.0
 */
public class AST2TextDeclarationPrinter extends TraversingDeclarationAdapter {
    StringBuffer str = new StringBuffer();

    Stack<String> intendStack = new Stack<String>();

    public static String AST2Text(ASTNode node) throws SBQLException {
	return AST2Text(node, "");
    }

    public static String AST2Text(ASTNode node, String intend)
	    throws SBQLException {
	AST2TextDeclarationPrinter astd = new AST2TextDeclarationPrinter();
	astd.intendStack.push(intend);
	node.accept(astd, null);
	return astd.getString();
    }

    /**
     * @return the textual representation of the AST query
     */
    private String getString() {
	return str.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#commonVisitDeclaration(odra.sbql.ast.declarations.Declaration,
     *      java.lang.Object)
     */
    @Override
    protected Object commonVisitDeclaration(Declaration decl, Object attr)
	    throws SBQLException {
	super.commonVisitDeclaration(decl, attr);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#commonVisitSingleFieldDeclaration(odra.sbql.ast.declarations.SingleFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    protected Object commonVisitSingleFieldDeclaration(
	    SingleFieldDeclaration decl, Object attr) throws SBQLException {
	newLine();
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitClassDeclaration(odra.sbql.ast.declarations.ClassDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitClassDeclaration(ClassDeclaration decl, Object attr)
	    throws SBQLException {
	str.append("class " + decl.getName());
	String[] ed = decl.getExtends();
	if (ed.length > 0) {
	    str.append(" extends ");
	    for (int i = 0; i < ed.length - 1; i++) {
		str.append(ed[i] + ", ");
	    }
	    str.append(ed[ed.length - 1]);
	}

	str.append("{");

	addIntend(INTEND);
	newLine();
	decl.getInstanceDeclaration().accept(this, attr);
	for (SingleFieldDeclaration d : decl.getFieldsDeclaration())
	    d.accept(this, attr);
	restoreIntend();
	newLine();
	str.append("}");
	newLine();

	return commonVisitDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitClassFieldDeclaration(odra.sbql.ast.declarations.ClassFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitClassFieldDeclaration(ClassFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getClassDeclaration().accept(this, attr);
	return commonVisitSingleFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitClassInstanceDeclaration(odra.sbql.ast.declarations.ClassInstanceDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitClassInstanceDeclaration(ClassInstanceDeclaration decl,
	    Object attr) throws SBQLException {
	// str.append(this.intend);
	str.append("instance " + decl.getInstanceName() + ": { ");
	addIntend(INTEND);
	newLine();
	for (SingleFieldDeclaration d : decl.getInstanceType()
		.getRecordTypeFields()) {
	    d.getDeclaration().accept(this, attr);
	    str.append(";");
	    newLine();
	}
	restoreIntend();

	str.append("}");
	newLine();
	return commonVisitDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitDbLinkDeclaration(odra.sbql.ast.declarations.DbLinkDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitExternalSchemaDefDeclaration(ExternalSchemaDefDeclaration decl, Object attr)
	    throws SBQLException {
	// TODO Auto-generated method stub
	return super.visitExternalSchemaDefDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitDbLinkFieldDeclaration(odra.sbql.ast.declarations.DbLinkFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitExternalSchemaDefFieldDeclaration(ExternalSchemaDefFieldDeclaration decl,
	    Object attr) throws SBQLException {
	// TODO Auto-generated method stub
	return super.visitExternalSchemaDefFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitInterfaceDeclaration(odra.sbql.ast.declarations.InterfaceDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitInterfaceDeclaration(InterfaceDeclaration decl,
	    Object attr) throws SBQLException {
	str.append("interface " + decl.getInterfaceName() + "{");
	addIntend(INTEND);
	newLine();
	str.append("objects " + decl.getInstanceName() + ";");
	for (SingleFieldDeclaration d : decl.getInterfaceBody()
		.getFields()) {
	    newLine();
	    d.getDeclaration().accept(this, attr);
	    str.append(";");
	}
	restoreIntend();
	newLine();
	str.append("}");
	return commonVisitDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitInterfaceFieldDeclaration(odra.sbql.ast.declarations.InterfaceFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitInterfaceFieldDeclaration(
	    InterfaceFieldDeclaration decl, Object attr) throws SBQLException {
	decl.getInterfaceDeclaration().accept(this, attr);
	return commonVisitSingleFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitMethodFieldDeclaration(odra.sbql.ast.declarations.MethodFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitMethodFieldDeclaration(MethodFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getProcedureDeclaration().accept(this, attr);
	return super.visitMethodFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitModuleDeclaration(odra.sbql.ast.declarations.ModuleDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitModuleDeclaration(ModuleDeclaration decl, Object attr)
	    throws SBQLException {
	str.append("module " + decl.getName() + "{");
	addIntend(INTEND);
	newLine();
	for (SingleImplementDeclaration d : decl.getModuleBody()
		.getImplementDeclaration()) {
	    str.append("implements " + d.getName() + ";");
	    newLine();
	}
	
	for (SingleImportDeclaration d : decl.getModuleBody()
		.getImportDeclaration()){
	    String alias = "";
	    if(d instanceof NamedSingleImportDeclaration)
	    {
		alias = " as " + ((NamedSingleImportDeclaration)d).getAlias();
	    }
	    str.append("import " + d.N.nameAsString() + alias + ";");
	    newLine();
	}
	for (SingleFieldDeclaration d : decl.getModuleBody()
		.getFieldDeclaration())
	    d.accept(this, attr);
	restoreIntend();
	newLine();
	str.append("}");
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitNamedTypeDeclaration(odra.sbql.ast.declarations.NamedTypeDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitNamedTypeDeclaration(NamedTypeDeclaration decl,
	    Object attr) throws SBQLException {
	str.append(decl.getTypeName());
	return super.visitNamedTypeDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitProcedureDeclaration(odra.sbql.ast.declarations.ProcedureDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitProcedureDeclaration(ProcedureDeclaration decl,
	    Object attr) throws SBQLException {
	printProcedure(decl, attr);
	return null;
    }

    /**
     * @param decl
     */
    private void printProcedure(ProcedureDeclaration decl, Object attr) {
	printProcedureHeader(decl.getProcedureHeader(), attr);
	newLine();
	str.append("{");
	addIntend(INTEND);
	newLine();
	str.append("<code>");
	restoreIntend();
	newLine();
	str.append("}");
	newLine();
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitProcedureFieldDeclaration(odra.sbql.ast.declarations.ProcedureFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitProcedureFieldDeclaration(
	    ProcedureFieldDeclaration decl, Object attr) throws SBQLException {
	decl.getProcedureDeclaration().accept(this, attr);
	return commonVisitSingleFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitRecordDeclaration(odra.sbql.ast.declarations.RecordDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitRecordDeclaration(RecordDeclaration decl, Object attr)
	    throws SBQLException {
	printRecord(decl.getFieldsDeclaration(), attr);
	return commonVisitDeclaration(decl, attr);
    }

    /**
     * @param fieldsDeclaration
     */
    private void printRecord(SingleFieldDeclaration[] fieldsDeclaration,
	    Object attr) {
	str.append("record {");
	addIntend(INTEND);
	if (fieldsDeclaration.length == 1) {
	    fieldsDeclaration[0].getDeclaration().accept(this, attr);
	    str.append(";");
	} else {
	    for (SingleFieldDeclaration d : fieldsDeclaration) {
		newLine();
		d.getDeclaration().accept(this, attr);
		str.append(";");

	    }
	}
	str.append(" }");
	restoreIntend(); // newLine();

    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitRecordTypeDeclaration(odra.sbql.ast.declarations.RecordTypeDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitRecordTypeDeclaration(RecordTypeDeclaration decl,
	    Object attr) throws SBQLException {
	printRecord(decl.getRecordTypeFields(), attr);
	return commonVisitDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitSessionVariableDeclaration(odra.sbql.ast.declarations.SessionVariableDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitSessionVariableFieldDeclaration(
	    SessionVariableFieldDeclaration node, Object attr) throws SBQLException {
	str.append("session ");
	node.getVariableDeclaration().accept(this, attr);
	str.append(";");
	return commonVisitSingleFieldDeclaration(node, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitTypeDefDeclaration(odra.sbql.ast.declarations.TypeDefDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitTypeDefDeclaration(TypeDefDeclaration decl, Object attr)
	    throws SBQLException {
	if(decl.isDistinct())
	    str.append("distinct ");
	str.append("type " + decl.getName() + " is ");
	return super.visitTypeDefDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitTypeDefFieldDeclaration(odra.sbql.ast.declarations.TypeDefFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitTypeDefFieldDeclaration(TypeDefFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getTypeDefDeclaration().accept(this, attr);
	return super.visitTypeDefFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitVariableDeclaration(odra.sbql.ast.declarations.VariableDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitVariableDeclaration(VariableDeclaration decl, Object attr)
	    throws SBQLException {
	// newLine();
	str.append(decl.getName() + ":");
	decl.getType().accept(this, attr);
	printCardinality(decl.getMinCard(), decl.getMaxCard());
	if(decl instanceof ReverseVariableDeclaration){
	    str.append(" reverse " + ((ReverseVariableDeclaration)decl).getReverseName());
	}	
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitVariableFieldDeclaration(odra.sbql.ast.declarations.VariableFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitVariableFieldDeclaration(VariableFieldDeclaration decl,
	    Object attr) throws SBQLException {

	decl.getVariableDeclaration().accept(this, attr);
	str.append(";");
	//	
	return commonVisitSingleFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitViewDeclaration(odra.sbql.ast.declarations.ViewDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitViewDeclaration(ViewDeclaration decl, Object attr)
	    throws SBQLException {
	str.append("view " + decl.getViewName() + "{");
	addIntend(INTEND);
	newLine();
	str.append("virtual ");
	decl.getBody().getVirtualObjectDeclaration().accept(this, attr);
	newLine();
	str.append("seed: ");
	decl.getBody().getSeedProcedure().getProcedureResult().getResultType()
		.accept(this, attr);
	printCardinality(decl.getBody().getSeedProcedure().getProcedureResult()
		.getResultMinCard(), decl.getBody().getSeedProcedure()
		.getProcedureResult().getResultMaxCard());
	newLine();
	str.append("{");
	addIntend(INTEND);
	newLine();
	str.append("<code>");
	restoreIntend();
	newLine();
	str.append("}");
	newLine();
	for (ProcedureDeclaration d : decl.getBody().getGenericProcedures()) {
	    d.accept(this, attr);
	}
	for (SingleFieldDeclaration d : decl.getBody().getFields()) {
	    d.getDeclaration().accept(this, attr);
	}
	restoreIntend();
	newLine();
	str.append("}");
	newLine();
	return commonVisitDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitViewFieldDeclaration(odra.sbql.ast.declarations.ViewFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitViewFieldDeclaration(ViewFieldDeclaration decl,
	    Object attr) throws SBQLException {
	decl.getViewDeclaration().accept(this, attr);
	return super.visitViewFieldDeclaration(decl, attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitProcedureHeaderDeclaration(odra.sbql.ast.declarations.ProcedureHeaderDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitProcedureHeaderDeclaration(
	    ProcedureHeaderDeclaration decl, Object attr) throws SBQLException {
	printProcedureHeader(decl, attr);
	return commonVisitDeclaration(decl, attr);
    }

    /**
     * @param decl
     */
    private void printProcedureHeader(ProcedureHeaderDeclaration decl,
	    Object attr) {
	str.append(decl.getName() + "(");
	for (SingleArgumentDeclaration d : decl.getProcedureArguments()) {
	    d.accept(this, attr);
	}
	str.append("):");
	decl.getProcedureResult().getResultType().accept(this, attr);
	printCardinality(decl.getProcedureResult().getResultMinCard(), decl
		.getProcedureResult().getResultMaxCard());

    }

    /*
     * (non-Javadoc)
     * 
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitProcedureHeaderFieldDeclaration(odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration,
     *      java.lang.Object)
     */
    @Override
    public Object visitProcedureHeaderFieldDeclaration(
	    ProcedureHeaderFieldDeclaration decl, Object attr)
	    throws SBQLException {
	decl.getProcedureHeaderDeclaration().accept(this, attr);
	return commonVisitSingleFieldDeclaration(decl, attr);
    }

    private void newLine() {
	str.append(NEW_LINE);
	str.append(intendStack.peek());

    }

    private final void printCardinality(int min, int max) {
	if (min == 1 && max == 1)
	    return;
	str.append("[" + min + ".." + (max == Integer.MAX_VALUE ? "*" : max)
		+ "]");
    }

    private String NEW_LINE = System.getProperty("line.separator");

    private String INTEND = "\t";

    private void addIntend(String intend) {
	this.intendStack.push(this.intendStack.peek() + intend);
    }

    private void restoreIntend() {
	this.intendStack.pop();
    }
}
