package odra.sbql.ast.utils.patterns;

import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.terminals.Operator;

public class SimpleBinaryPattern implements Pattern {

	Operator O = null;
	
	public SimpleBinaryPattern(Operator O) {
		this.O = O;
	}

	public boolean matches(Object obj) {
		return obj instanceof SimpleBinaryExpression && ((SimpleBinaryExpression) obj).O.equals(O);
	}

}
