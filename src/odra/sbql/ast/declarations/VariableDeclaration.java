package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.terminals.Name;

public class VariableDeclaration extends Declaration {
    protected Name N;

    protected TypeDeclaration D1;

    protected CardinalityDeclaration D2;

    protected Expression E; // initial value of the variable

    protected int reflevel;

    public VariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2) {
	this(n1, d1, d2, new EmptyExpression(), 0);

    }

    public VariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2, int reflevel) {
	this(n1, d1, d2, new EmptyExpression(), reflevel);
    }

    public VariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2, Expression e) {
	this(n1, d1, d2, e, 0);
    }

    public VariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2, Expression e, int reflevel) {
	assert n1 != null && d1 != null && d2 != null && e != null : "n1 != null && d1 != null && d2 != null && e != null";
	N = n1;
	D1 = d1;
	D2 = d2;
	this.reflevel = reflevel;
	E = e;
    }

    /**
     * @return the n
     */
    public String getName() {
	return N.value();
    }

    /**
     * @param n
     *                the n to set
     */
    public void setName(String n) {
	N = new Name(n);
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitVariableDeclaration(this, attr);
    }

    /**
     * @param d1
     *                the d1 to set
     */
    public void setTypeDeclaration(TypeDeclaration d1) {
	D1 = d1;
    }

    /**
     * @return the d1
     */
    public TypeDeclaration getType() {
	return D1;
    }

    public int getMinCard() {
	return getCardinality().getMinCard();
    }

    public int getMaxCard() {
	return getCardinality().getMaxCard();
    }

    /**
     * @return the d2
     */
    public CardinalityDeclaration getCardinality() {
	return D2;
    }

    /**
     * @param e
     *                the e to set
     */
    public void setInitExpression(Expression e) {
	E = e;
    }

    /**
     * @return the e
     */
    public Expression getInitExpression() {
	return E;
    }

    /**
     * @param reflevel
     *                the reflevel to set
     */
    public void setReflevel(int reflevel) {
	this.reflevel = reflevel;
    }

    /**
     * @return the reflevel
     */
    public int getReflevel() {
	return reflevel;
    }
}
