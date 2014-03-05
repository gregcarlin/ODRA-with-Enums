/**
 * 
 */
package odra.security;

/**
 * SecurityException
 * @author Radek Adamus
 *@since 2007-11-29
 *last modified: 2007-11-29
 *@version 1.0
 */
public abstract class SecurityException extends Exception {

    /**
     * @param arg0
     * @param arg1
     */
    protected SecurityException(String arg0, Throwable arg1) {
	super(arg0, arg1);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    protected SecurityException(String arg0) {
	super(arg0);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    protected SecurityException(Throwable arg0) {
	super(arg0);
	// TODO Auto-generated constructor stub
    }

}
