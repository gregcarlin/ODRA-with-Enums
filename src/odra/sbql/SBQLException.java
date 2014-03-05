package odra.sbql;

import odra.exceptions.OdraCoreRuntimeException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.system.config.ConfigDebug;


/**
 * Abstract class for all exception classes which in order to print information about an error on the screen require
 * such data as line number, column number and module name.
 * 
 * @author raist, edek (exception hierarchy)
 */

public abstract class SBQLException extends OdraCoreRuntimeException {

	private String module = "";

	private int line;

	private int column;

	protected SBQLException(String msg) {
		super(msg);
	}
	public SBQLException(String msg, Throwable ex) {
		super(msg, ex);
		if(ex instanceof SBQLException){
		    this.column = ((SBQLException)ex).column;
		    this.line = ((SBQLException)ex).line;
		    this.module = ((SBQLException)ex).module;
		}
	}
	
	protected SBQLException(String msg, Throwable ex, String module, int line, int column) {
	
	    super(msg, ex);
	    this.init(module, line, column);
	}

	protected SBQLException(Throwable ex, String module, int line, int column) {
		super(ex);
		this.init(module, line, column);
	}
	public SBQLException(Throwable ex) {
		super(ex);
		if(ex instanceof SBQLException){
		    this.column = ((SBQLException)ex).column;
		    this.line = ((SBQLException)ex).line;
		    this.module = ((SBQLException)ex).module;
		}
	}
	protected SBQLException(String msg, String module, int line, int column) {
		this(msg);

		if (ConfigDebug.ASSERTS) {
			assert module != null && msg != null : "module != null && msg != null";
		}

		this.init(module, line, column);
	}

	public SBQLException(Throwable ex, ASTNode node, ASTVisitor visitor){
	    this(ex, visitor.getSourceModuleName(),node.line,node.column);
	    
	}
	public SBQLException(String msg, ASTNode node, ASTVisitor visitor){
	    this(msg, visitor.getSourceModuleName(),node.line,node.column);
	    
	}
	
	public SBQLException(String msg, Throwable ex, ASTNode node, ASTVisitor visitor){
	    this(msg,ex, visitor.getSourceModuleName(),node.line,node.column);
	    
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
	
	private void init(String module, int line, int column){
	    this.module = module;
	    this.column = column;
	    this.line = line;
	    
	}
}