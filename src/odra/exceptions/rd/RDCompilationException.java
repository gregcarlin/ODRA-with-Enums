package odra.exceptions.rd;

public class RDCompilationException extends RDException {
	public String module;
	public int line, column;
	
	public RDCompilationException(String msg, String module, int line, int column) {
		super(msg);
		
		this.module = module;
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
