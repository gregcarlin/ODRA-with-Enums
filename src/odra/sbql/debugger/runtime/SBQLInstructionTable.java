/**
 * 
 */
package odra.sbql.debugger.runtime;

import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.serializer.ASTDeserializer;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.ast.utils.patterns.SourcePositionPattern;



/**
 * SBQLInstructionIndex
 * @author Radek Adamus
 *@since 2008-06-29
 *last modified: 2008-06-29
 *@version 1.0
 */
public class SBQLInstructionTable {
	private CodeRange[] instructions;
	private Map<Integer, SourcePosition> positions = new Hashtable<Integer, SourcePosition>();
	private boolean sourceAvaliable;
	ASTNode code;
	/**
	 * 
	 */
	public SBQLInstructionTable(byte[] serializedIndex) {
		deserialize(serializedIndex);
		
		
	}
	public SourcePosition getSourcePositionForCodeOffset(int offset){			
		assert offset >= 0 : "offset >=0";
		Vector<Integer> temp = new Vector<Integer>();
		for(int i = 0; i < instructions.length; i++){
			CodeRange range = instructions[i];
			if(range.isInRange(offset)) {
				temp.add(i);
			}
		}
		if(temp.size() == 1)
		    return positions.get(temp.get(0));
		CodeRange found = instructions[temp.get(0)];
//		int latestStart = instructions[temp.get(0)].start;
//		int latestEnd = instructions[temp.get(0)].end;
		int searched = 0;
		for(int i = 0; i < temp.size(); i++){
			CodeRange range = instructions[temp.get(i)];
		    //if(found.includes(range) ){
		    if(found.end - found.start > range.end - range.start ){
		    	found = range;
		    	searched = i;
		    }
		}
		return positions.get(temp.get(searched));		
	}
	
	/**
	 * @param serializedIndex 
	 * 
	 */
	private void deserialize(byte[] serialized) {
		ByteBuffer buffer = ByteBuffer.wrap(serialized);
		int size = buffer.getInt();
		this.sourceAvaliable = buffer.get() == 1 ? true : false; 
		this.instructions = new CodeRange[size];
		for(int i =0; i < size; i++){
			CodeRange range = readCodeIndex(buffer);
			instructions[i] = range;
			SourcePosition position = readSourcePosition(buffer);
			positions.put(i, position);
		}		
		if(sourceAvaliable){
			ASTDeserializer deserializer = new ASTDeserializer();
			int length = buffer.getInt();
			byte [] source = new byte[length];
			buffer.get(source);
			this.code = deserializer.readAST(source);
		}
	}

	/**
	 * @param buffer 
	 * @return
	 */
	protected SourcePosition readSourcePosition(ByteBuffer buffer) {
		return new SourcePosition(buffer.getInt(),buffer.getInt());
		
	}

	/**
	 * @param buffer 
	 * @return
	 */
	protected CodeRange readCodeIndex(ByteBuffer buffer) {
		return new CodeRange(buffer.getInt(),buffer.getInt());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for(int i = 0; i < this.instructions.length; i++){
			str.append(i);
			str.append(": ");
			str.append(this.instructions[i].toString());
			str.append(" : ");
			str.append(this.positions.get(i).toString());
			str.append(NEW_LINE);
		}
		return str.toString();
	}

	protected static class CodeRange{
		int start;
		int end;
		/**
		 * @param start
		 * @param end
		 */
		public CodeRange(int start, int end) {
			this.start = start;
			this.end = end;
		}
		boolean isInRange(int offset){
			return ((this.start <= offset) && (this.end >= offset));
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			
			return "from: " + this.start + " to: " + this.end;
		}
		
		
		boolean includes(CodeRange range){
			if(this.start < range.start && this.end > range.end)
				return true;
			return false;
		}
	}
	
	public static class SourcePosition{
		int line;
		int column;
		/**
		 * @param line
		 * @param column
		 */
		public SourcePosition(int line, int column) {
			this.line = line;
			this.column = column;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {			
			return "line: " + this.line + " column: " + this.column;
		}
		/**
		 * @return the line
		 */
		public int getLine() {
			return line;
		}
		/**
		 * @return the column
		 */
		public int getColumn() {
			return column;
		}
		
		
	}
	private static String NEW_LINE = System.getProperty("line.separator");
	/**
	 * @param pos
	 * @return
	 */
	public String getSourceForPosition(SourcePosition pos) {
		if(this.code == null)
			return "";
		ASTNodeFinder finder = new ASTNodeFinder(new SourcePositionPattern(pos.line, pos.column), false);
		Vector<ASTNode> nodes = finder.findNodes(code);
		if(nodes.size() > 0){
			ASTNode found = nodes.get(0);
//			if(found instanceof Expression && ((Expression)found).getParentExpression() != null)
//				found =((Expression)found).getParentExpression();
			return AST2TextQueryDumper.AST2Text(found);
		}
		else
			return "";
	}
}
