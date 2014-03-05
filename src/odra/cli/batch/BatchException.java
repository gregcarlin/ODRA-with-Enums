package odra.cli.batch;

import odra.cli.CLISyntaxErrorException;

/**
 * Batch file exception.
 * 
 * @author jacenty
 * @version 2007-07-05
 * @since 2007-07-05
 */
public class BatchException extends CLISyntaxErrorException
{
	public BatchException(String msg)
	{
		super(msg);
	}
	
	public BatchException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
