package odra.exceptions.rd;

public class RDSecurityException extends RDException {
	public String module;
	public int line, column;

	public RDSecurityException(String msg, int line, int column) {
		super(msg);
		
		this.module = msg;
		this.line = line;
		this.column = column;
	}
	
	public String getModule() {
		return module;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
}
