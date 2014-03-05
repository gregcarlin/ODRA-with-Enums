/**
 * 
 */
package odra.sbql.ast.statements;

import java.util.Vector;



/**
 * SequentialCatchStatement
 * @author Radek Adamus
 *@since 2007-09-18
 *last modified: 2007-09-18
 *@version 1.0
 */
public class SequentialCatchBlock extends CatchBlock {
    private CatchBlock S1,S2;
    
    /**
     * @param s1
     * @param s2
     */
    public SequentialCatchBlock(CatchBlock s1, CatchBlock s2) {
	this.S1 = s1;
	this.S2 = s2;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.statements.CatchStatement#flattenCatchStatements()
     */
    @Override
    public SingleCatchBlock[] flattenCatchBlocks()
    {
	Vector v = new Vector();
	
	for (SingleCatchBlock i : S1.flattenCatchBlocks())
		v.addElement(i);

	for (SingleCatchBlock i : S2.flattenCatchBlocks())
		v.addElement(i);

	return (SingleCatchBlock[]) v.toArray(new SingleCatchBlock[v.size()]);
    }

}
