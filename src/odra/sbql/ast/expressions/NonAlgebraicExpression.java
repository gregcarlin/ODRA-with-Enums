package odra.sbql.ast.expressions;

import odra.sbql.typechecker.EnvironmentInfo;

public abstract class NonAlgebraicExpression extends BinaryExpression {
//	public transient int stackSize;
	private transient EnvironmentInfo envsInfo;
	public NonAlgebraicExpression(Expression e1, Expression e2) {
		super(e1,e2);
	}
	/**
	 * @param envsInfo the envsInfo to set
	 */
	public void setEnvsInfo(EnvironmentInfo envsInfo)
	{
	    this.envsInfo = envsInfo;
	}
	/**
	 * @return the envsInfo
	 */
	public EnvironmentInfo getEnvsInfo()
	{
	    return envsInfo;
	}
	
}
