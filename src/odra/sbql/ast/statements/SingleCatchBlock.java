/**
 * 
 */
package odra.sbql.ast.statements;

import odra.sbql.ast.declarations.VariableDeclaration;

/**
 * SingleCatchStatement
 * @author Radek Adamus
 *@since 2007-09-18
 *last modified: 2007-09-18
 *@version 1.0
 */
public class SingleCatchBlock extends CatchBlock {
    private VariableDeclaration VD;
    private BlockStatement S;
    private String exceptionName;
    private String exceptionTypeName;
    private String catchBlockName;
   

    
    /**
     * @param vd
     * @param s
     */
    public SingleCatchBlock(String exceptionName, String exceptionTypeName, BlockStatement s) {
	this.VD = null;
	this.exceptionName = exceptionName;
	this.exceptionTypeName = exceptionTypeName; 
	this.S = s;
    }
    /**
     * @param vd
     * @param s
     */
    public SingleCatchBlock(VariableDeclaration vd, BlockStatement s) {
	VD = vd;
	this.exceptionName = vd.getName();
	this.exceptionTypeName = vd.getType().getTypeName();
	S = s;
    }
    /**
     * @return the exception declacerion
     * can be null for catchall
     */
    public VariableDeclaration getCatchVariable()
    {
        return VD;
    }
    /**
     * @return the s
     */
    public BlockStatement getStatement()
    {
        return S;
    }
    /* (non-Javadoc)
     * @see odra.sbql.ast.statements.CatchStatement#flattenCatchStatements()
     */
    @Override
    public SingleCatchBlock[] flattenCatchBlocks()
    {
	return new SingleCatchBlock[] {this};
    }
    

    /**
     * @return the exceptionName
     */
    public String getExceptionName()
    {
        return exceptionName;
    }
    /**
     * @return the exceptionTypeName
     */
    public String getExceptionTypeName()
    {
        return exceptionTypeName;
    }
    /**
     * @param exceptionName the exceptionName to set
     */
    public void setExceptionName(String exceptionName)
    {
        this.exceptionName = exceptionName;
    }
    /**
     * @param exceptionTypeName the exceptionTypeName to set
     */
    public void setExceptionTypeName(String exceptionTypeName)
    {
        this.exceptionTypeName = exceptionTypeName;
    }
    /**
     * @return the catchBlockName
     */
    public String getCatchBlockName()
    {
	assert catchBlockName != null : "catch block name == null";
        return catchBlockName;
    }
    /**
     * @param catchBlockName the catchBlockName to set
     */
    public void setCatchBlockName(String catchBlockName)
    {
        this.catchBlockName = catchBlockName;
    }
    
}
