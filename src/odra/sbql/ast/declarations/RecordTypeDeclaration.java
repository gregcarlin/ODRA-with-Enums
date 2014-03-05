package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class RecordTypeDeclaration extends TypeDeclaration {
	private RecordDeclaration D;

	public RecordTypeDeclaration(RecordDeclaration d) {
		D = d;
	}

	public RecordTypeDeclaration(Name n, FieldDeclaration d) {
		D = new RecordDeclaration(n, d);
	}
	public Object accept(ASTVisitor node, Object attr) throws SBQLException {
		return node.visitRecordTypeDeclaration(this, attr);
	}
		
	
	public String getRecordTypeName(){
	    return D.getName();
	}
	
	public SingleFieldDeclaration[] getRecordTypeFields(){
	    return D.getFieldsDeclaration();
	}
}
