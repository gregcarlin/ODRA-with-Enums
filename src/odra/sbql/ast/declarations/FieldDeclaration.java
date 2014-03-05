package odra.sbql.ast.declarations;

public abstract class FieldDeclaration extends Declaration {
   public abstract SingleFieldDeclaration[] flattenFields();
}