package odra.sbql.interpreter;

import odra.sbql.SBQLException;

public class InterpreterException extends SBQLException {
    private final static long serialVersionUID = 20062304L;

    /**
     * @param msg
     */
    public InterpreterException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     */
    public InterpreterException(String msg, Throwable ex) {
	super(msg,ex);
	// TODO Auto-generated constructor stub
    }

    

    

    /**
     * @param ex
     */
    public InterpreterException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

}