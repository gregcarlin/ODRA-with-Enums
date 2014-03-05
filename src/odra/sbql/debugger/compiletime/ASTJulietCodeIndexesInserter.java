/**
 * 
 */
package odra.sbql.debugger.compiletime;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.statements.Statement;

/**
 * ASTJulietCodeIndexesInserter
 * postprocess the ast debug info <br>
 * during codegeneration we save start and end JulietCode instruction <br>
 * but we are not able to acquire the bytecode index of the them <br>
 * this class does it as a final stage of code generation
 * @author Radek Adamus
 *@since 2007-06-11
 *last modified: 2007-06-11
 *@version 1.0
 */
public class ASTJulietCodeIndexesInserter extends TraversingASTAdapter {

    public ASTNode fill(ASTNode ast) throws SBQLException{
    	ast.accept(this, null);
    	return ast;
	
    }
    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#commonVisitExpression(odra.sbql.ast.expressions.Expression, java.lang.Object)
     */
    @Override
    protected Object commonVisitExpression(Expression expr, Object attr) {
    	assert expr.getDebug() != null : "no debug info in ast node";
    	if(!(expr.getDebug().equals(NoDebugNodeData.NODEBUGDATA)))
    	{
    		this.setIndexes(expr.getDebug());
    	}
    	return super.commonVisitExpression(expr, attr);
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#commonVisitStatement(odra.sbql.ast.statements.Statement, java.lang.Object)
     */
    @Override
    protected Object commonVisitStatement(Statement stmt, Object attr)  {
		assert stmt.getDebug() != null : "no debug info in ast node";
		if(!(stmt.getDebug().equals(NoDebugNodeData.NODEBUGDATA)))
		{
		    this.setIndexes(stmt.getDebug());
		}
		return super.commonVisitStatement(stmt, attr);
    }

    private void setIndexes(DebugNodeData debug){
		assert debug.getStart() != null && debug.getEnd() != null : "wrong node debug info";
		int start = debug.getStart().getIndex();
		assert start != -1 : "start != -1";
		debug.setStartIndex(start);
		int end = debug.getEnd().getIndex();
		assert end != -1 : "start != -1";
		debug.setEndIndex(end);
    }
}
