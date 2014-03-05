package odra.wrapper;

import odra.exceptions.OdraCoreRuntimeException;

/**
 * Wrapper exception class. 
 * @author jacenty
 * @version   2008-01-28
 * @since   2006-05-21
 */
public class WrapperException extends OdraCoreRuntimeException
{
	public enum Error
	{
		UNKNOWN(1),
		XML_SCHEMA_NOT_FOUND(2),
		XML_SCHEMA_PARSE(3),
		DRIVER_CLASS_NOT_FOUND(4),
		SQL_CONNECTION(5),
		SQL_QUERY_EXECUTION(6),
		RESULT_STRUCTURE(7),
		QUERY_SYNTAX(7),
		QUERY_TYPE(8),
		INVALID_COLUMN(9),
		CLIENT_SERVER_CONNECTION(10),
		CLIENT_PARAMS(11),
		RESULT_PATTERN(12);
		
		/** error code */
		private final int code;
		
		/**
		 * Constructor.
		 * 
		 * @param code error code
		 */
		Error(int code)
		{
			this.code = code;
		}
		
		/**
		 * Returns the error code.
		 * 
		 * @return error code
		 */
		public int getCode()
		{
			return code;
		}
		
		@Override
		public String toString()
		{
			return Integer.toString(code);
		}
		
		public static Error getForCode(int code)
		{
			for(Error error : values())
				if(error.getCode() == code)
					return error;
			
			return UNKNOWN;
		}
	}
	
	/** error */
	private final Error error;
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param error error
	 */
	public WrapperException(String message, Error error)
	{
		super(message);
		this.error = error;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 */
	public WrapperException(String message)
	{
		this(message, Error.UNKNOWN);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause cause
	 * @param error error
	 */
	public WrapperException(String message, Throwable cause, Error error)
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
	public WrapperException(String message, Throwable cause)
	{
		this(message, cause, Error.UNKNOWN);
	}
	
	/**
	 * Returns the error for this exception.
	 * 
	 * @return error
	 */
	public Error getError()
	{
		return error;
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + " (error #" + getError() + ")";
	}
}
