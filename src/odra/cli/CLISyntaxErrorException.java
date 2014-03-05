package odra.cli;

/**
 * This exception class is used to signal syntax errors detected
 * during parsing of cli commands.
 * 
 * @author raist
 */

public class CLISyntaxErrorException extends Exception {
	public CLISyntaxErrorException(String msg) {
		super(msg);
	}
	
	public CLISyntaxErrorException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
