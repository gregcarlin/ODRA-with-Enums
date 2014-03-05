package odra.sbql.builder.classes;


import odra.db.schema.OdraClassSchema;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.builder.ModuleConstructor;
import odra.sbql.builder.ModuleOrganizer;

public class ClassConstructor extends ASTAdapter  {
	private ModuleConstructor modconstr;
	private ModuleOrganizer modorg;
	
	OdraClassSchema schemaClass;
	
	public ClassConstructor(ModuleConstructor modconstr, ModuleOrganizer modorg) {
		this.modconstr = modconstr;
		this.modorg = modorg;
		this.setSourceModuleName(modconstr.getSourceModuleName());
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitClassDeclaration(odra.sbql.ast.declarations.ClassDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitClassDeclaration(ClassDeclaration decl, Object attr) throws SBQLException {
		schemaClass = new OdraClassSchema();
		schemaClass.setName(decl.getName());
		String [] superClassNames = decl.getExtends();

		schemaClass.setSuperClassesNames(superClassNames);
		//visit instance type declaration
		decl.getInstanceDeclaration().accept(this, attr);
		//visit class body fields
		for(SingleFieldDeclaration sfd: decl.getFieldsDeclaration()){
			sfd.accept(this, attr);
		}
		
		new ClassOrganizer(this.modconstr.getConstructedModule()).createClass(schemaClass);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitClassInstanceTypeDeclaration(odra.sbql.ast.declarations.ClassInstanceTypeDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitClassInstanceDeclaration(ClassInstanceDeclaration decl, Object attr) throws SBQLException {
		schemaClass.setInstanceName(decl.getInstanceName());
		//visit record decl with use of module constructor
		decl.getInstanceType().accept(this.modconstr, attr);
		//get the type name
		schemaClass.setTypeName(decl.getInstanceType().getTypeName());
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTAdapter#visitMethodFieldDeclaration(odra.sbql.ast.declarations.MethodFieldDeclaration, java.lang.Object)
	 */
	@Override
	public Object visitMethodFieldDeclaration(MethodFieldDeclaration decl, Object attr) throws SBQLException {
		schemaClass.addMethod(modconstr.createSchemaProcedureInfo(decl.getProcedureDeclaration()));
		return null;
	}
	
	
}
