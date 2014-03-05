package odra.sbql.ast.declarations;


public class EmptyExtendsDeclaration extends ExtendsDeclaration {

	/* (non-Javadoc)
     * @see odra.sbql.ast.declarations.ExtendsDeclaration#flattenArguments()
     */
    @Override
    public SingleExtendsDeclaration[] flattenExtends() {
	return new SingleExtendsDeclaration[0];
    }

	

	

}
