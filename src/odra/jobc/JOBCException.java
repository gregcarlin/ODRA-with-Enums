package odra.jobc;

/**
 * JOBC exception. 
 * @author jacenty
 * @version   2007-03-11
 * @since   2007-03-11
 */
public class JOBCException extends Exception
{
     
	
	
	public static int UNKNOWN_ERROR = 1;
	public static int CONNECTION_ERROR = 2;
	public static int RESULT_TYPE_ERROR = 3;
	public static int EXECUTION_ERROR = 4;
	public static int COMMUNICATION_ERROR = 5;
	public static int RESULT_NOT_FOUND_ERROR = 6;
	public static int QUERY_PARAMETER_ERROR = 7;
		
	
	
	
	/** error */
	private final int error;
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param error error
	 */
	public JOBCException(String message, int error)
	{
		super(message);
		this.error = error;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 */
	public JOBCException(String message)
	{
		this(message, UNKNOWN_ERROR);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause cause
	 * @param error error
	 */
	public JOBCException(String message, Throwable cause, int error)
	{
		super(message, cause);
		this.error = error;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause cause
	 */
	public JOBCException(String message, Throwable cause)
	{
		this(message, cause, UNKNOWN_ERROR);
	}
	
	/**
	 * Returns the error for this exception.
	 * 
	 * @return error
	 */
	public int getError()
	{
		return error;
	}

	public String getMessage()
	{
		return super.getMessage() + " (error #" + Integer.toString(getError()) + ")";
	}
}