package odra.sbql.ast.declarations;

public abstract class SingleFieldDeclaration extends FieldDeclaration {

   public SingleFieldDeclaration[] flattenFields() {
      return new SingleFieldDeclaration[] { this };
   }
   public abstract Declaration getDeclaration();
   public abstract String getName();
}