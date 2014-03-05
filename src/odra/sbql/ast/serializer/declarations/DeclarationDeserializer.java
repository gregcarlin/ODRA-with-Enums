/**
 * 
 */
package odra.sbql.ast.serializer.declarations;

import java.nio.ByteBuffer;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.declarations.ArgumentDeclaration;
import odra.sbql.ast.declarations.CardinalityDeclaration;
import odra.sbql.ast.declarations.ClassBody;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.CompoundName;
import odra.sbql.ast.declarations.EmptyArgumentDeclaration;
import odra.sbql.ast.declarations.EmptyExtendsDeclaration;
import odra.sbql.ast.declarations.EmptyFieldDeclaration;
import odra.sbql.ast.declarations.EmptyImplementDeclaration;
import odra.sbql.ast.declarations.EmptyImportDeclaration;
import odra.sbql.ast.declarations.ExtendsDeclaration;
import odra.sbql.ast.declarations.FieldDeclaration;
import odra.sbql.ast.declarations.ImplementDeclaration;
import odra.sbql.ast.declarations.ImportDeclaration;
import odra.sbql.ast.declarations.InterfaceBody;
import odra.sbql.ast.declarations.InterfaceDeclaration;
import odra.sbql.ast.declarations.InterfaceFieldDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.ModuleBody;
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
import odra.sbql.ast.declarations.SequentialArgumentDeclaration;
import odra.sbql.ast.declarations.SequentialExtendsDeclaration;
import odra.sbql.ast.declarations.SequentialFieldDeclaration;
import odra.sbql.ast.declarations.SequentialImplementDeclaration;
import odra.sbql.ast.declarations.SequentialImportDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.SingleArgumentDeclaration;
import odra.sbql.ast.declarations.SingleExtendsDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.SingleImplementDeclaration;
import odra.sbql.ast.declarations.SingleImportDeclaration;
import odra.sbql.ast.declarations.TypeDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewBody;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.serializer.ASTDeserializer;
import odra.sbql.ast.serializer.SerializationUtil;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;

/**
 * DeclarationDeserializer
 * 
 * @author Radek Adamus
 * @since 2008-04-25 last modified: 2008-04-25
 * @version 1.0
 */
public class DeclarationDeserializer {
    boolean withPositionInfo;

    ByteBuffer serast;

    public ASTNode readDeclarationAST(byte[] bast) {
	serast = ByteBuffer.wrap(bast);
	this.withPositionInfo = readBoolean();

	return read();
    }

    private ASTNode read() {
	ASTNode node = null;

	byte type = serast.get();

	switch (type) {
	case IDeclarationDescriptor.CLASS_DECL: {
	    node = readClass();
	    break;
	}
	case IDeclarationDescriptor.LINK_DECL:
	    node = readLink();
	    break;
	case IDeclarationDescriptor.IFACE_DECL: {
	    node = readInterface();
	    break;
	}
	case IDeclarationDescriptor.MODULE_DECL:
	    node = readModule();
	    break;
	case IDeclarationDescriptor.VIEW_DECL:
	    node = readView();
	    break;
	case IDeclarationDescriptor.PROCEDURE_DECL:
	    node = readProcedure();
	    break;
	case IDeclarationDescriptor.METHOD_DECL:
	    node = readMethod();
	    break;
	case IDeclarationDescriptor.VARIABLE_DECL:
	    node = readVariable();
	    break;
	case IDeclarationDescriptor.REV_VARIABLE_DECL:
	    node = readReverseVariable();
	    break;
	case IDeclarationDescriptor.NAMED_TYPE_DECL:
	    node = readNamedType();
	    break;
	case IDeclarationDescriptor.RECORD_TYPE_DECL:
	    node = readRecordType();
	    break;
	case IDeclarationDescriptor.TYPEDEF_DECL:
	    node = readTypeDef();
	    break;
	case IDeclarationDescriptor.PROC_HEADER_DECL:
	    node = readProcedureHeader();
	    break;
	default:
	    assert false : "unknown declaration";
	}

	if (withPositionInfo)
	    deserializePositionInfo(node, serast);

	return node;
    }

    /**
     * @return
     */
    private ProcedureHeaderDeclaration readProcedureHeader() {
	Name name = readName();
	ProcedureResult res = readResult();
	ArgumentDeclaration arg = readArguments();
	return new ProcedureHeaderDeclaration(name, arg, res);
    }

    /**
     * @return
     */

    /**
     * @return
     */
    private ModuleDeclaration readModule() {
	Name name = readName();
	ImportDeclaration imp = readImports();
	ImplementDeclaration impl = readImplements();
	FieldDeclaration flds = readFields();
	return new ModuleDeclaration(name, new ModuleBody(imp, impl, flds));

    }

    /**
     * @return
     */
    private ImplementDeclaration readImplements() {
	int length = serast.getInt();
	if (length == 0)
	    return new EmptyImplementDeclaration();
	ImplementDeclaration fields = new SingleImplementDeclaration(readName());
	for (int i = 1; i < length; i++) {
	    fields = new SequentialImplementDeclaration(fields,
		    new SingleImplementDeclaration(readName()));
	}
	return fields;

    }

    /**
     * @return
     */
    private ImportDeclaration readImports() {
	int length = serast.getInt();
	if (length == 0)
	    return new EmptyImportDeclaration();

	ImportDeclaration fields = readImport();

	for (int i = 1; i < length; i++) {
	    fields = new SequentialImportDeclaration(fields, readImport());
	}
	return fields;
    }

    /**
     * @return
     */
    private SingleImportDeclaration readImport() {
	CompoundName name = readCompoundName();
	if (readBoolean()) {
	    Name alias = readName();
	    return new NamedSingleImportDeclaration(name, alias);
	}
	return new SingleImportDeclaration(name);
    }

    /**
     * @return
     */
    private boolean readBoolean() {
	return serast.get() == 1 ? true : false;
    }

    private Name readName() {
	return new Name(SerializationUtil.deserializeString(serast));
    }

    private FieldDeclaration readFields() {
	int length = serast.getInt();
	if (length == 0)
	    return new EmptyFieldDeclaration();
	FieldDeclaration fields = readFieldDeclaration();
	for (int i = 1; i < length; i++) {
	    fields = new SequentialFieldDeclaration(fields,
		    readFieldDeclaration());
	}
	return fields;
    }

    private SingleFieldDeclaration readFieldDeclaration() {
	SingleFieldDeclaration decl = null;
	byte type = serast.get();
	switch (type) {
	case IDeclarationDescriptor.VARIABLE_FLD_DECL:
	    decl = new VariableFieldDeclaration((VariableDeclaration) read());
	    break;
	case IDeclarationDescriptor.SESSION_VARIABLE_FLD_DECL:
	    decl = new SessionVariableFieldDeclaration(
		    (VariableDeclaration) read());
	    break;
	case IDeclarationDescriptor.PROCEDURE_FLD_DECL:
	    decl = new ProcedureFieldDeclaration((ProcedureDeclaration) read());
	    break;
	case IDeclarationDescriptor.CLASS_FLD_DECL:
	    decl = new ClassFieldDeclaration((ClassDeclaration) read());
	    break;
	case IDeclarationDescriptor.IFACE_FLD_DECL:
	    decl = new InterfaceFieldDeclaration((InterfaceDeclaration) read());
	    break;
	case IDeclarationDescriptor.LINK_FLD_DECL:
	    decl = new ExternalSchemaDefFieldDeclaration(
		    (ExternalSchemaDefDeclaration) read());
	    break;
	case IDeclarationDescriptor.METHOD_FLD_DECL:
	    decl = new MethodFieldDeclaration((ProcedureDeclaration) read());
	    break;
	case IDeclarationDescriptor.VIEW_FLD_DECL:
	    decl = new ViewFieldDeclaration((ViewDeclaration) read());
	    break;
	case IDeclarationDescriptor.TYPEDEF_FLD_DECL:
	    decl = new TypeDefFieldDeclaration((TypeDefDeclaration) read());
	    break;
	case IDeclarationDescriptor.PROC_HEADER_FLD_DECL:
	    decl = new ProcedureHeaderFieldDeclaration(
		    (ProcedureHeaderDeclaration) read());
	    break;
	default:
	    assert false : "unknown field";
	}

	// if(withPositionInfo)
	// deserializePositionInfo(decl, serast);
	return decl;
    }

    /**
     * @return
     */
    private TypeDefDeclaration readTypeDef() {
	Name name = readName();
	boolean distinct = (serast.get() == 1);

	TypeDeclaration type = readType();
	return new TypeDefDeclaration(name, type, distinct);
    }

    /**
     * @return
     */
    private ViewDeclaration readView() {
	Name name = readName();
	VariableDeclaration virt = readVariable();
	ProcedureDeclaration seed = readProcedure();
	int length = serast.getInt();
	ProcedureDeclaration[] generic = new ProcedureDeclaration[length];
	for (int i = 0; i < length; i++)
	    generic[i] = readProcedure();
	FieldDeclaration flds = readFields();
	return new ViewDeclaration(name,
		new ViewBody(virt, seed, generic, flds));
    }

    /**
     * @return
     */
    private ProcedureDeclaration readMethod() {
	return readProcedure();
    }

    /**
     * @return
     */
    private ExternalSchemaDefDeclaration readLink() {
	assert false : "unimplemented";
	return null;
    }

    /**
     * @return
     */
    private InterfaceDeclaration readInterface() {
	return new InterfaceDeclaration(readName(), readName(), readExtends(),
		new InterfaceBody(readFields()));
    }

    private ExtendsDeclaration readExtends() {
	int length = serast.getInt();
	ExtendsDeclaration extendsdec;
	if (length == 0)
	    extendsdec = new EmptyExtendsDeclaration();
	else {
	    extendsdec = new SingleExtendsDeclaration(readName());
	    for (int i = 1; i < length; i++) {
		extendsdec = new SequentialExtendsDeclaration(extendsdec,
			new SingleExtendsDeclaration(readName()));
	    }
	}
	return extendsdec;
    }

    /**
     * @return
     */
    private ClassDeclaration readClass() {
	Name name = readName();

	ExtendsDeclaration extendsdec = readExtends();

	ImplementDeclaration impldec = readImplements();

	ClassInstanceDeclaration clsInst = readClassInstance();

	FieldDeclaration flds = readFields();
	return new ClassDeclaration(name, extendsdec, new ClassBody(clsInst,
		impldec, flds));
    }

    ClassInstanceDeclaration readClassInstance() {
	Name name = readName();
	RecordTypeDeclaration rcrd = readRecordType();

	return new ClassInstanceDeclaration(name, rcrd);
    }

    /**
     * @return
     */
    private RecordDeclaration readRecord() {
	Name name = readName();
	FieldDeclaration flds = readFields();

	return new RecordDeclaration(name, flds);
    }

    /**
     * @return
     */
    private ProcedureDeclaration readProcedure() {
	ProcedureHeaderDeclaration header = readProcedureHeader();

	Statement code = (Statement) readExpressionOrStatement();
	return new ProcedureDeclaration(header, code);
    }

    /**
     * @return
     */
    private ArgumentDeclaration readArguments() {
	int length = serast.getInt();
	if (length == 0)
	    return new EmptyArgumentDeclaration();

	ArgumentDeclaration args = new SingleArgumentDeclaration(readVariable());
	for (int i = 1; i < length; i++) {
	    args = new SequentialArgumentDeclaration(args,
		    new SingleArgumentDeclaration(readVariable()));
	}
	return args;
    }

    /**
     * @return
     */
    private ProcedureResult readResult() {
	TypeDeclaration type = readType();
	CardinalityDeclaration card = readCardinality();
	int reflevel = serast.getInt();
	return new ProcedureResult(type, card, reflevel);
    }

    /**
     * @return
     */
    private CardinalityDeclaration readCardinality() {
	return new CardinalityDeclaration(new IntegerLiteral(serast.getInt()),
		new IntegerLiteral(serast.getInt()));
    }

    /**
     * @return
     */
    private TypeDeclaration readType() {

	return (TypeDeclaration) read();
    }

    /**
     * @return
     */
    private RecordTypeDeclaration readRecordType() {

	Name name = readName();
	FieldDeclaration flds = readFields();
	return new RecordTypeDeclaration(name, flds);
    }

    /**
     * @return
     */
    private NamedTypeDeclaration readNamedType() {
	return new NamedTypeDeclaration(readCompoundName());
    }

    private VariableDeclaration readVariable() {
	Name name = readName();
	TypeDeclaration type = readType();
	CardinalityDeclaration card = readCardinality();

	int reflevel = serast.getInt();
	Expression e = (Expression) readExpressionOrStatement();
	return new VariableDeclaration(name, type, card, e, reflevel);
    }

    /**
     * @return
     */
    private ASTNode readReverseVariable() {
	VariableDeclaration vdec = readVariable();
	Name reverseName = readName();
	return new ReverseVariableDeclaration(vdec, reverseName);
    }

    /**
     * @return
     */
    private ASTNode readExpressionOrStatement() {
	int bufflength = serast.getInt();
	byte[] exprbuff = new byte[bufflength];
	serast.get(exprbuff, 0, bufflength);
	ASTDeserializer deser = new ASTDeserializer();
	return deser.readAST(exprbuff);
    }

    CompoundName readCompoundName() {
	int length = serast.getInt();
	assert length > 0 : "length > 0";
	CompoundName cname = new CompoundName(readName());
	for (int i = 1; i < length; i++) {
	    cname = new CompoundName(cname, readName());
	}
	return cname;
    }

    private void deserializePositionInfo(ASTNode node, ByteBuffer serast) {
	node.line = serast.getInt();
	node.column = serast.getInt();
    }
}
