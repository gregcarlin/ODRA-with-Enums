/**
 * 
 */
package odra.sbql.ast.statements;



/**
 * CatchStatement
 * @author Radek Adamus
 *@since 2007-09-18
 *last modified: 2007-09-18
 *@version 1.0
 */
public abstract class CatchBlock  {
    public abstract SingleCatchBlock[] flattenCatchBlocks();
    
}
