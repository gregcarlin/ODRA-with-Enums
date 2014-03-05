/**
 * 
 */
package odra.sbql.ast.statements;

import java.util.LinkedList;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.transactions.ast.ITransactionCapableASTNode;

/**
 * TryCatchFinallyStatement
 * @author Radek Adamus
 *@since 2007-09-18
 *last modified: 2007-09-18
 *@version 1.0
 */
public class TryCatchFinallyStatement extends Statement {
    private BlockStatement S1; 
    private CatchBlock S2;
    private Statement S3;
    private int level;

    /**
     * @param s1
     * @param s2
     * @param s3
     */
    public TryCatchFinallyStatement(BlockStatement tryS, CatchBlock catchS, Statement finallyS) {
	S1 = tryS;
	S2 = catchS;
	S3 = finallyS;
    }

    /**
     * @return the s1
     */
    public BlockStatement getTryStatement()
    {
        return S1;
    }

    /**
     * @return the s2
     */
    public CatchBlock getCatchBlocks()
    {
        return S2;
    }

    /**
     * @return the s3
     */
    public Statement getFinallyStatement()
    {
        return S3;
    }
    
    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitTryCatchFinallyStatement(this, attr);
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.ASTNode#getTransactionCapableChildrenASTNodes()
     */
    @Override
    public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes()
    {
	List<ITransactionCapableASTNode> children = new LinkedList<ITransactionCapableASTNode>();
	for(SingleCatchBlock cb :this.getCatchBlocks().flattenCatchBlocks())
	    children.add(cb.getStatement());
	children.add(this.getFinallyStatement());
	return children;
    }

    /**
     * @return the level
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level)
    {
        this.level = level;
    }
    
}
