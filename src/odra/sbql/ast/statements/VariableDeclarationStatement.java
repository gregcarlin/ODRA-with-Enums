package odra.sbql.ast.statements;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.declarations.CardinalityDeclaration;
import odra.sbql.ast.declarations.TypeDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.terminals.Name;

public class VariableDeclarationStatement extends Statement {
    
    private VariableDeclaration D;

    /**
     * @param variableName
     * @param variableTypeName
     * @param minCard
     * @param maxCard
     */
    public VariableDeclarationStatement(String variableName,
	    TypeDeclaration variableType, int minCard, int maxCard, int reflevel,
	    Expression init) {
	D = new VariableDeclaration(new Name(variableName), variableType, new CardinalityDeclaration(minCard, maxCard),  init, reflevel);
    }

    // constructor used by the parser
    public VariableDeclarationStatement(VariableDeclaration d) {
	D = d;
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitVariableDeclarationStatement(this, attr);
    }

    /**
     * @param initExpr
     *                the initExpr to set
     */
    public void setInitExpression(Expression initExpr) {
	this.D.setInitExpression(initExpr);
    }

    /**
     * @return the initExpr
     */
    public Expression getInitExpression() {
	return D.getInitExpression();
    }

    

    /**
     * @return the variableName
     */
    public String getVariableName() {
	return D.getName();
    }

    /**
     * @param variableTypeName
     *                the variableTypeName to set
     */
    public void setVariableTypeName(String variableTypeName) {
	this.D.getType().setTypeName(variableTypeName);
    }

    /**
     * @return the variableTypeName
     */
    public String getVariableTypeName() {
	return D.getType().getTypeName();
    }

   

    /**
     * @return the minCard
     */
    public int getMinCard() {
	return D.getMinCard();
    }

    
    /**
     * @return the maxCard
     */
    public int getMaxCard() {
	return D.getMaxCard();
    }

    

    /**
     * @return the reflevel
     */
    public int getReflevel() {
	return D.getReflevel();
    }

    /**
     * @return the declaration of the variable type (if exists),
     *         <code>null</code> otherwise.
     */
    public TypeDeclaration getTypeDeclaration() {
	return D.getType();
    }
}
