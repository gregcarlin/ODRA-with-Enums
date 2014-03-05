/**
 * 
 */
package odra.sbql.ast.serializer.declarations;

import odra.network.transport.AutoextendableBuffer;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingDeclarationAdapter;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.CompoundName;
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
import odra.sbql.ast.declarations.ProcedureResult;
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
import odra.sbql.ast.declarations.TypeDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
import odra.sbql.ast.serializer.ASTSerializer;
import odra.sbql.ast.serializer.SerializationUtil;
import odra.sbql.ast.terminals.Name;
import odra.sbql.builder.CompilerException;

/**
 * DeclarationSerializer
 * 
 * @author Radek Adamus
 * @since 2008-04-25 last modified: 2008-04-25
 * @version 1.0
 */
public class DeclarationSerializer extends TraversingDeclarationAdapter {
    AutoextendableBuffer buffer;

    boolean withPositionInfo;

    public byte[] writeDeclarationAST(ASTNode node, boolean withPositionInfo)
	    throws CompilerException {

	buffer = new AutoextendableBuffer();

	this.withPositionInfo = withPositionInfo;

	buffer.put(withPositionInfo ? (byte) 1 : (byte) 0);
	try {
	    node.accept(this, null);
	} catch (Exception e) {
	    throw new CompilerException(e);
	}

	return buffer.getBytes();
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
	if (this.withPositionInfo) {
	    SerializationUtil.serializePosition(this.buffer, decl);
	}
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
	decl.getDeclaration().accept(this, attr);
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
	buffer.put(IDeclarationDescriptor.CLASS_DECL);
	serializeClass(decl, attr);
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
	buffer.put(IDeclarationDescriptor.CLASS_FLD_DECL);
	return super.visitClassFieldDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.CLASS_INST_DECL);
	serializeClassInstance(decl, attr);

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
	buffer.put(IDeclarationDescriptor.LINK_DECL);
	serializeLink(decl);
	return commonVisitDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.LINK_FLD_DECL);
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
	buffer.put(IDeclarationDescriptor.IFACE_DECL);
	serializeInterface(decl, attr);
	return this.commonVisitDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.IFACE_FLD_DECL);
	return super.visitInterfaceFieldDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.METHOD_FLD_DECL);
	return super.visitMethodFieldDeclaration(decl, attr);
    }

    /**
     * @param procedureDeclaration
     */
    private void serializeMethod(ProcedureDeclaration procedureDeclaration,
	    Object attr) {
	serializeProcedure(procedureDeclaration, attr);

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
	buffer.put(IDeclarationDescriptor.MODULE_DECL);
	serializeModule(decl, attr);

	return commonVisitDeclaration(decl, attr);
    }

    /**
     * @param decl
     */
    private void serializeModule(ModuleDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getName());

	serializeImports(decl.getModuleBody().getImportDeclaration());
	serializeImplements(decl.getModuleBody().getImplementDeclaration());
	serializeFields(decl.getModuleBody().getFieldDeclaration(), attr);

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
	buffer.put(IDeclarationDescriptor.NAMED_TYPE_DECL);
	serializeNamedType(decl);
	return commonVisitDeclaration(decl, attr);
    }

    /**
     * @param decl
     */
    private void serializeNamedType(NamedTypeDeclaration decl) {
	serializeName(decl.getName());

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
	buffer.put(IDeclarationDescriptor.PROCEDURE_DECL);
	serializeProcedure(decl, attr);

	return commonVisitDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.PROCEDURE_FLD_DECL);
	return super.visitProcedureFieldDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.RECORD_DECL);
	serializeRecord(decl, attr);
	return commonVisitDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.RECORD_TYPE_DECL);
	serializeRecordType(decl, attr);
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
	buffer.put(IDeclarationDescriptor.SESSION_VARIABLE_FLD_DECL);
	return super.visitSessionVariableFieldDeclaration(node, attr);
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
	buffer.put(IDeclarationDescriptor.TYPEDEF_DECL);
	serializeTypeDef(decl, attr);
	return commonVisitDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.TYPEDEF_FLD_DECL);
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
	if(decl instanceof ReverseVariableDeclaration){
	    buffer.put(IDeclarationDescriptor.REV_VARIABLE_DECL);
	    serializeReverseVariable((ReverseVariableDeclaration)decl, attr);
	}else {
	    buffer.put(IDeclarationDescriptor.VARIABLE_DECL);
	    serializeVariable(decl, attr);
	}
	return commonVisitDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.VARIABLE_FLD_DECL);
	return super.visitVariableFieldDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.VIEW_DECL);
	serializeView(decl, attr);
	return commonVisitDeclaration(decl, attr);
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitProcedureHeaderDeclaration(odra.sbql.ast.declarations.ProcedureHeaderDeclaration, java.lang.Object)
     */
    @Override
    public Object visitProcedureHeaderDeclaration(
	    ProcedureHeaderDeclaration decl, Object attr) throws SBQLException {
	buffer.put(IDeclarationDescriptor.PROC_HEADER_DECL);
	serializeProcedureHeader(decl, attr);
	return commonVisitDeclaration(decl, attr);
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingDeclarationAdapter#visitProcedureHeaderFieldDeclaration(odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration, java.lang.Object)
     */
    @Override
    public Object visitProcedureHeaderFieldDeclaration(
	    ProcedureHeaderFieldDeclaration decl, Object attr)
	    throws SBQLException {
	buffer.put(IDeclarationDescriptor.PROC_HEADER_FLD_DECL);
	return super.visitProcedureHeaderFieldDeclaration(decl, attr);
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
	buffer.put(IDeclarationDescriptor.VIEW_FLD_DECL);
	return super.visitViewFieldDeclaration(decl, attr);
    }

    /**
     * @param recordType -
     *                RecordType to serialize
     */
    private void serializeRecordType(RecordTypeDeclaration recordType,
	    Object attr) {
	SerializationUtil.serializeString(buffer, recordType.getTypeName());
	serializeFields(recordType.getRecordTypeFields(), attr);

    }

    /**
     * @param fieldDeclaration
     */
    private void serializeFields(SingleFieldDeclaration[] fieldDeclaration,
	    Object attr) {
	buffer.putInt(fieldDeclaration.length);
	for (SingleFieldDeclaration fd : fieldDeclaration)
	    fd.accept(this, attr);

    }

    /**
     * @param implementDeclaration
     */
    private void serializeImplements(
	    SingleImplementDeclaration[] implementDeclaration) {
	buffer.putInt(implementDeclaration.length);
	for (SingleImplementDeclaration si : implementDeclaration)
	    SerializationUtil.serializeString(buffer, si.getName());

    }

    /**
     * @param importDeclaration
     */
    private void serializeImports(SingleImportDeclaration[] importDeclaration) {
	buffer.putInt(importDeclaration.length);	
	for (SingleImportDeclaration si : importDeclaration){
	    serializeName(si.N);
	    if(si instanceof NamedSingleImportDeclaration){
		buffer.put((byte)1);
		SerializationUtil.serializeString(buffer, ((NamedSingleImportDeclaration)si).getAlias());		
	    }else
		buffer.put((byte)0);
	    
	}
    }

    /**
     * @param n
     */
    private void serializeName(CompoundName n) {
	String[] names = n.nameAsArray();
	buffer.putInt(names.length);
	for (String name : names)
	    SerializationUtil.serializeString(buffer, name);
    }

    /**
     * @param n
     */
    private void serializeName(Name n) {
	SerializationUtil.serializeString(buffer, n.value());
    }

    /**
     * @param linkDeclaration
     */
    private void serializeLink(ExternalSchemaDefDeclaration linkDeclaration) {
	SerializationUtil.serializeString(buffer, linkDeclaration.getName());

    }

    /**
     * @param d
     */
    private void serializeInterface(InterfaceDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getInterfaceName());
	SerializationUtil.serializeString(buffer, decl.getInstanceName());
	serializeExtends(decl.getExtends());
	serializeFields(decl.getInterfaceBody().getFields(), attr);

    }

    /**
     * @param decl
     */
    private void serializeProcedure(ProcedureDeclaration decl, Object attr) {
	serializeProcedureHeader(decl.getProcedureHeader(), attr);
	
	serialize(decl.getStatement());
    }

    /**
     * @param procedureHeader
     */
    private void serializeProcedureHeader(
	    ProcedureHeaderDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getName());
	serializeResult(decl.getProcedureResult(), attr);
	serializeArguments(decl.getProcedureArguments(), attr);
	
    }

    /**
     * @param procedureArguments
     */
    private void serializeArguments(
	    SingleArgumentDeclaration[] procedureArguments, Object attr) {
	buffer.putInt(procedureArguments.length);
	for (SingleArgumentDeclaration d : procedureArguments) {
	    serializeVariable(d.D, attr);
	}

    }

    /**
     * @param procedureResult
     */
    private void serializeResult(ProcedureResult procedureResult, Object attr) {

	serializeType(procedureResult.getResultType(), attr);
	serializeCardinality(procedureResult.getResultMinCard(),
		procedureResult.getResultMaxCard());
	buffer.putInt(procedureResult.getReflevel());
    }

    /**
     * @param resultMinCard
     * @param resultMaxCard
     */
    private void serializeCardinality(int minCard, int maxCard) {
	buffer.putInt(minCard);
	buffer.putInt(maxCard);

    }

    /**
     * @param resultType
     */
    private void serializeType(TypeDeclaration resultType, Object attr) {
	resultType.accept(this, attr);

    }

    /**
     * @param decl
     * @param attr
     */
    private void serializeRecord(RecordDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getName());
	serializeFields(decl.getFieldsDeclaration(), attr);
    }

    /**
     * @param expr -
     *                expression to serialize
     */
    private void serialize(ASTNode expr) {
	ASTSerializer serializer = new ASTSerializer();
	byte[] serast = serializer.writeAST(expr, withPositionInfo);
	buffer.putInt(serast.length);
	buffer.put(serast);

    }

    /**
     * @param decl
     * @param attr
     */
    private void serializeClass(ClassDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getName());

	serializeExtends(decl.getExtends());
	serializeImplements(decl.getImplementsDeclaration());
	serializeClassInstance(decl.getInstanceDeclaration(), attr);
	serializeFields(decl.getFieldsDeclaration(), attr);

    }

    /**
     * @param instanceDeclaration
     */
    private void serializeClassInstance(
	    ClassInstanceDeclaration instanceDeclaration, Object attr) {
	SerializationUtil.serializeString(buffer, instanceDeclaration
		.getInstanceName());
	serializeRecordType(instanceDeclaration.getInstanceType(), attr);

    }

    /**
     * @param extends1
     */
    private void serializeExtends(String[] extendsnames) {
	buffer.putInt(extendsnames.length);
	for (String name : extendsnames) {
	    SerializationUtil.serializeString(buffer, name);
	}

    }

    /**
     * @param decl
     */
    private void serializeVariable(VariableDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getName());
	serializeType(decl.getType(), attr);
	serializeCardinality(decl.getMinCard(), decl.getMaxCard());
	buffer.putInt(decl.getReflevel());
	serialize(decl.getInitExpression());

    }
    /**
     * @param decl
     * @param attr
     */
    private void serializeReverseVariable(ReverseVariableDeclaration decl, Object attr) {
	serializeVariable(decl, attr);	
	SerializationUtil.serializeString(buffer, decl.getReverseName());
    }
    /**
     * @param decl
     */
    private void serializeView(ViewDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getViewName());
	serializeVariable(decl.getBody().getVirtualObjectDeclaration(), attr);
	serializeProcedure(decl.getBody().getSeedProcedure(), attr);
	ProcedureDeclaration[] pdecls = decl.getBody().getGenericProcedures();
	buffer.putInt(pdecls.length);
	for (ProcedureDeclaration d : pdecls)
	    serializeProcedure(d, attr);
	serializeFields(decl.getBody().getFields(), attr);

    }
    
    /**
     * @param decl
     * @param attr
     */
    private void serializeTypeDef(TypeDefDeclaration decl, Object attr) {
	SerializationUtil.serializeString(buffer, decl.getName());
	buffer.put(decl.isDistinct() ? (byte)1 : (byte)0);
	serializeType(decl.getType(), attr);
	

    }
}
