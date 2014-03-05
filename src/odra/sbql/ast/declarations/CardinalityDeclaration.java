package odra.sbql.ast.declarations;

import odra.sbql.ast.terminals.IntegerLiteral;

public class CardinalityDeclaration extends Declaration {
    private IntegerLiteral L1, L2;

    public CardinalityDeclaration() {
	L1 = new IntegerLiteral(1);
	L2 = new IntegerLiteral(1);
    }

    public CardinalityDeclaration(int min, int max) {
	L1 = new IntegerLiteral(min);
	L2 = new IntegerLiteral(max);
    }

    public CardinalityDeclaration(IntegerLiteral l1) {
	L1 = l1;
	L2 = new IntegerLiteral(Integer.MAX_VALUE);
    }

    public CardinalityDeclaration(IntegerLiteral l1, IntegerLiteral l2) {
	L1 = l1;
	L2 = l2;
    }

    public final int getMinCard() {
	return L1.value();
    }

    public final int getMaxCard() {
	return L2.value();
    }

    /**
     * @param l1
     *                the l1 to set
     */
    public void setMinimalCardinality(int l1) {
	L1 = new IntegerLiteral(l1);
    }

    /**
     * @param l2
     *                the l2 to set
     */
    public void setMaximalCardinality(int l2) {
	L2 = new IntegerLiteral(l2);
    }

}
