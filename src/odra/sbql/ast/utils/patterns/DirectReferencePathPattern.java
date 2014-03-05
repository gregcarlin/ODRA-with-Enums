package odra.sbql.ast.utils.patterns;

import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.results.compiletime.ReferenceSignature;

public class DirectReferencePathPattern implements Pattern {
	
	public DirectReferencePathPattern() {
	}

	public boolean matches(Object obj) {
		return obj instanceof DotExpression || 
			(obj instanceof NameExpression && ((ReferenceSignature) ((NameExpression) obj).getSignature()).reflevel == 0);
	}

}
