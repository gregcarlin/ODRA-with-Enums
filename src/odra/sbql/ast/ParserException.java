package odra.sbql.ast;

import odra.sbql.SBQLException;


public class ParserException extends SBQLException {

    /**
     * @param msg
     */
    public ParserException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param module
     * @param line
     * @param column
     */
    public ParserException(String msg, String module, int line, int column) {
	super(msg, module, line, column);
	// TODO Auto-generated constructor stub
    }

   
    /**
     * @param ex
     */
    public ParserException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

	public ParserException(String message, Object node, Class<? extends ASTAdapter> classAdapter) {
		super(getMessage(message, node, classAdapter));
	}

	private static String getMessage(String message, Object node, Class<? extends ASTAdapter> classAdapter) {
		return message + getClassName(node) + " in " + classAdapter;
	}

	private static String getClassName(Object o) {
		return o.getClass().getSimpleName();
	}
	
}