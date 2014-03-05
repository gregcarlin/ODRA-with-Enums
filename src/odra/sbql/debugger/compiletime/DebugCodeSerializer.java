/**
 * 
 */
package odra.sbql.debugger.compiletime;

import odra.network.transport.AutoextendableBuffer;
import odra.sbql.SBQLException;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.serializer.ASTSerializer;
import odra.sbql.ast.serializer.SerializationUtil;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.Statement;

/** Serialize debug data to byte array
 * DebugCodeGenerator
 * @author Radek Adamus
 *@since 2007-12-09
 *last modified: 2007-12-09
 *@version 1.0
 */
public class DebugCodeSerializer extends TraversingASTAdapter {
    private AutoextendableBuffer buffer;
    private int index = 0;
    private final boolean withSource;
    private final boolean includeExpressions;
    
    private DebugCodeSerializer(boolean withsource, boolean includeExpressions){
    	this.withSource = withsource;
		this.includeExpressions = includeExpressions;
    }
    private void init() {
    	buffer = new AutoextendableBuffer();
    	//placeholder for number of records    	
    	buffer.putInt(0);
    	buffer.put(this.withSource ? (byte)1 : (byte)0);
    }
    private void serialize(ASTNode node){
    	init();
    	node.accept(this, null);
    	if(withSource){
    		ASTSerializer serializer = new ASTSerializer();
    		byte[] source = serializer.writeAST(node, true);
    		buffer.putInt(source.length);
    		buffer.put(source);
    	}
    	int position = buffer.position();
    	buffer.position(0);
    	buffer.putInt(index);
    	buffer.position(position);
    }
    private byte[] getBytes(){
    	return buffer.getBytes();
    }
    
    public static byte[] generate(ASTNode node, boolean includeExpressions){
    	DebugCodeSerializer serializer = new DebugCodeSerializer(false, includeExpressions); 	
    	serializer.serialize(node);    	
    	return serializer.getBytes();
	
    }
    
    public static byte[] generateFull(ASTNode node, boolean includeExpressions){
    	DebugCodeSerializer serializer = new DebugCodeSerializer(true, includeExpressions);     	
    	serializer.serialize(node);
    	return serializer.getBytes();	
    }
    

    /* (non-Javadoc)
     * @see odra.sbql.ast.TraversingASTAdapter#commonVisitStatement(odra.sbql.ast.statements.Statement, java.lang.Object)
     */
    @Override
    protected Object commonVisitStatement(Statement stmt, Object attr)
	    throws SBQLException {
    	addDebug(stmt.getDebug());
	return null;
    }
    
    
    
    /* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#commonVisitExpression(odra.sbql.ast.expressions.Expression, java.lang.Object)
	 */
	@Override
	protected Object commonVisitExpression(Expression expr, Object attr)
			throws SBQLException {
		if(this.includeExpressions)
			addDebug(expr.getDebug());
		return null;
	}

	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitEmptyStatement(odra.sbql.ast.statements.EmptyStatement, java.lang.Object)
	 */
	@Override
	public Object visitEmptyStatement(EmptyStatement stmt, Object attr) throws SBQLException {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitEmptyExpression(odra.sbql.ast.expressions.EmptyExpression, java.lang.Object)
	 */
	@Override
	public Object visitEmptyExpression(EmptyExpression expr, Object attr) throws SBQLException {
		return null;
	}
	private void addDebug(DebugNodeData debug){
    	assert debug != null && debug.getStartIndex() != -1 && debug.getEndIndex() != -1 : "debug != null start index != -1 end index != -1";
    	index++; //the number
    	this.buffer.putInt(debug.getStartIndex()); //bytecode start index
    	this.buffer.putInt(debug.getEndIndex()); //bytecode end index
    	this.buffer.putInt(debug.getLine()); //the line number
    	this.buffer.putInt(debug.getColumn()); //the column number
    }
}
