package odra.sbql.ast.utils.patterns;

import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.terminals.Operator;

public class SimpleUnaryPattern implements Pattern {

	Operator O = null;
	
	public SimpleUnaryPattern(Operator O) {
		this.O = O;
	}

	public boolean matches(Object obj) {
		return obj instanceof SimpleUnaryExpression && ((SimpleUnaryExpression) obj).O == O;
	}

}
