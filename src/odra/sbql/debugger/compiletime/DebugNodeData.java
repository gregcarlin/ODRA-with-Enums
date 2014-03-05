package odra.sbql.debugger.compiletime;

import odra.sbql.emiter.instructions.Instruction;

/**
 * Class representing debug information
 * for a particular AST node
 * @author radamus
 *
 */
public class DebugNodeData {

	private Instruction start;
	private Instruction end;
	protected int startIndex;
	protected int endIndex;
	protected int line, column;
	
	
	/**
	 * @param start
	 * @param end
	 */
	public DebugNodeData(Instruction start, Instruction end, int line, int column) {
	    this.start = start;
	    this.end = end;
	    this.line = line;
	    this.column = column;
	}

	/** 
	 * @param start
	 * @param end
	 */
	public DebugNodeData(int start, int end) {
	    this.startIndex = start;
	    this.endIndex = end;
	}

	/**
	 * @return the end
	 */
	public Instruction getEnd() {
	    return end;
	}

	

	/**
	 * @return the start
	 */
	public Instruction getStart() {
	    return start;
	}

	
	/**
	 * @return the endIndex
	 */
	public int getEndIndex() {
	    return endIndex;
	}
	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
	    return startIndex;
	}
	/**
	 * @param endIndex the endIndex to set
	 */
	public void setEndIndex(int endIndex) {
	    this.endIndex = endIndex;
	}
	/**
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
	    this.startIndex = startIndex;
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
	public DebugNodeData() {
	    
	}
}
