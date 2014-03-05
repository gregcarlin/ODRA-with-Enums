package odra.sbql.ast.declarations;

import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.terminals.Name;

public class SingleEnumeratorDeclaration extends EnumeratorDeclaration{
	
	public Name N;
	public Expression E;
		
		public SingleEnumeratorDeclaration(Name n,Expression e) {
			N = n;
			E=e;
		}
		
		public SingleEnumeratorDeclaration[] flattenEnumerators() {
			return new SingleEnumeratorDeclaration[]{this};
		}

	}
