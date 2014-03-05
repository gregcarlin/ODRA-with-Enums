package odra.sbql.debugger.compiletime;


public class NoDebugNodeData extends DebugNodeData {

	public static final NoDebugNodeData NODEBUGDATA = new NoDebugNodeData(); 
	private NoDebugNodeData(){
		this.startIndex = this.endIndex = this.column = this.line = 0;
	}
}
