package odra.sbql.ast.expressions;

public abstract class TransitiveClosureExpression extends NonAlgebraicExpression {

	public TransitiveClosureExpression(Expression e1, Expression e2) {
		super(e1, e2);
	}

}
