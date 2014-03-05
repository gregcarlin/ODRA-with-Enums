package odra.sbql.builder;


/**
 * Exception class used to signal that during the process of module linking
 * something went wrong.
 * 
 * @author raist
 */

public class LinkerException extends BuilderException {

    
    /**
     * @param msg
     */
    public LinkerException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    
    /**
     * @param ex
     */
    public LinkerException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }
  
}